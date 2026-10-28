package com.example.veturilowawa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veturilowawa.data.model.StationUiModel
import com.example.veturilowawa.data.network.VeturiloApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val stations: List<StationUiModel>,
        val totalBikesAvailable: Int,
        val totalStationsCount: Int
    ) : UiState
    data class Error(val message: String) : UiState
}

class MainViewModel : ViewModel() {
    private val api = VeturiloApiService.create()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var allStations = listOf<StationUiModel>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _onlyAvailableBikes = MutableStateFlow(false)
    val onlyAvailableBikes = _onlyAvailableBikes.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadStations()
    }

    fun loadStations(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = UiState.Loading
            }

            try {
                // Równoległe pobranie informacji o stacjach oraz ich aktualnego statusu
                val infoDeferred = async { api.getStationInformation() }
                val statusDeferred = async { api.getStationStatus() }

                val infoResponse = infoDeferred.await()
                val statusResponse = statusDeferred.await()

                val statusMap = statusResponse.data.stations.associateBy { it.stationId }

                allStations = infoResponse.data.stations.map { info ->
                    val status = statusMap[info.id]
                    StationUiModel(
                        id = info.id,
                        name = info.name,
                        address = info.address,
                        lat = info.lat,
                        lon = info.lon,
                        capacity = info.capacity ?: 0,
                        bikesAvailable = status?.numBikesAvailable ?: 0,
                        docksAvailable = status?.numDocksAvailable ?: 0,
                        isRenting = status?.isRenting ?: true,
                        isReturning = status?.isReturning ?: true
                    )
                }.sortedBy { it.name }

                applyFilters()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Błąd pobierania danych: ${e.localizedMessage ?: "Brak połączenia z internetem"}"
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun onToggleOnlyAvailableBikes() {
        _onlyAvailableBikes.value = !_onlyAvailableBikes.value
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value.trim()
        val onlyWithBikes = _onlyAvailableBikes.value

        var filtered = allStations

        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.address?.contains(query, ignoreCase = true) == true)
            }
        }

        if (onlyWithBikes) {
            filtered = filtered.filter { it.bikesAvailable > 0 }
        }

        val totalBikes = allStations.sumOf { it.bikesAvailable }
        val totalCount = allStations.size

        _uiState.value = UiState.Success(
            stations = filtered,
            totalBikesAvailable = totalBikes,
            totalStationsCount = totalCount
        )
    }
}
