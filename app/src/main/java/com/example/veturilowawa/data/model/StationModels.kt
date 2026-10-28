package com.example.veturilowawa.data.model

import com.google.gson.annotations.SerializedName

// GBFS station_information.json models
data class StationInfoResponse(
    val data: StationInfoData
)

data class StationInfoData(
    val stations: List<StationItem>
)

data class StationItem(
    @SerializedName("station_id") val id: String,
    val name: String,
    val address: String? = null,
    val lat: Double,
    val lon: Double,
    val capacity: Int? = 0
)

// GBFS station_status.json models
data class StationStatusResponse(
    val data: StationStatusData
)

data class StationStatusData(
    val stations: List<StationStatusItem>
)

data class StationStatusItem(
    @SerializedName("station_id") val stationId: String,
    @SerializedName("num_bikes_available") val numBikesAvailable: Int = 0,
    @SerializedName("num_docks_available") val numDocksAvailable: Int = 0,
    @SerializedName("is_installed") val isInstalled: Boolean? = true,
    @SerializedName("is_renting") val isRenting: Boolean? = true,
    @SerializedName("is_returning") val isReturning: Boolean? = true
)

// Połączony model danych dla UI
data class StationUiModel(
    val id: String,
    val name: String,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val bikesAvailable: Int,
    val docksAvailable: Int,
    val isRenting: Boolean = true,
    val isReturning: Boolean = true
)
