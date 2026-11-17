# Projekt: „Veturilo Wawa” (Android Studio + Jetpack Compose)

Dokumentacja techniczna aplikacji mobilnej. Aplikacja korzysta z otwartych danych miejskich m.st. Warszawy (system Veturilo / standard GBFS).

---

## 1. Informacje ogólne
* **Nazwa aplikacji:** Veturilo Wawa (lub Stacje Rowerowe Warszawa)
* **Platforma:** Android (Android Studio)
* **Język:** Kotlin
* **Interfejs graficzny:** Jetpack Compose (Material 3)
* **Klucz API:** **Brak** (dane są publiczne i darmowe, zero rejestracji)
* **Główny cel:** Pobieranie danych JSON z otwartego API miejskiego, prezentacja stacji rowerowych na liście, wyszukiwanie oraz uruchomienie nawigacji w Mapach Google.

---

## 2. Źródło danych (Otwarte Dane Warszawy)
Aplikacja korzysta z oficjalnego, publicznego feedu **GBFS (General Bikeshare Feed Specification)** dla systemu rowerów w Warszawie:

1. **Informacje o stacjach (nazwy, współrzędne GPS):**
   * URL: `https://gbfs.urbansharing.com/rowerowawarszawa.pl/station_information.json`
   * Zawiera: `station_id`, `name`, `address`, `lat`, `lon`, `capacity`.
2. **Status stacji na żywo (dostępność rowerów):**
   * URL: `https://gbfs.urbansharing.com/rowerowawarszawa.pl/station_status.json`
   * Zawiera: `station_id`, `num_bikes_available`, `num_docks_available`, `is_installed`, `is_renting`.

---

## 3. Kluczowe funkcjonalności

### Ekran 1: Główna lista stacji
* **Pasek wyszukiwania:** Filtrowanie stacji w czasie rzeczywistym po wpisaniu tekstu (np. „Marszałkowska”, „Metro”).
* **Lista stacji (`LazyColumn`):** Kafelki z nazwą stacji oraz czytelnym wskaźnikiem (np. ` 6 wolnych rowerów | 4 wolne stojaki`).
* **Stany aplikacji:**
  * Wskaźnik ładowania (`CircularProgressIndicator`), gdy aplikacja pobiera dane.
  * Obsługa braku internetu (czytelny komunikat z przyciskiem „Spróbuj ponownie”).

### Ekran 2: Szczegóły stacji (lub rozwijana karta)
* Dokładna nazwa i adres stacji.
* Liczba wolnych rowerów tradycyjnych i elektrycznych.
* **Przycisk „Prowadź do stacji”:** Uruchamia fabryczną aplikację Google Maps za pomocą mechanizmu `Intent` (brak konieczności trudnej konfiguracji map w samej aplikacji!).

---

## 4. Architektura i struktura projektu

Projekt trzyma się prostego i czytelnego wzorca **MVVM (Model - View - ViewModel)**:

```
app/src/main/java/com/example/veturilowawa/
│
├── data/
│   ├── model/
│   │   └── StationModels.kt       # Klasy danych odwzorowujące JSON
│   └── network/
│       └── VeturiloApiService.kt  # Interfejs Retrofit do pobierania danych
│
├── ui/
│   ├── MainViewModel.kt          # Logika biznesowa, stan (ładowanie, lista, filtry)
│   ├── MainActivity.kt           # Główna aktywność
│   └── screens/
│       └── StationListScreen.kt  # Komponenty Jetpack Compose (wygląd)
│
└── AndroidManifest.xml           # Zezwolenie na dostęp do Internetu
```

---

## 5. Zależności (`build.gradle.kts` - moduł `:app`)

Wystarczy dodać standardowe biblioteki do komunikacji sieciowej (Retrofit + Converter Gson):

```kotlin
dependencies {
    // Jetpack Compose & Material 3 (dodane domyślnie w szablonie Empty Activity)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)

    // ViewModel dla Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // Retrofit do obsługi zapytań HTTP / JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
```

W pliku `AndroidManifest.xml` (nad znacznikiem `<application>`):
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 6. Szkielet kodu (Najważniejsze fragmenty)

### A. Model danych (`StationModels.kt`)
```kotlin
package com.example.veturilowawa.data.model

import com.google.gson.annotations.SerializedName

data class StationInfoResponse(
    val data: StationInfoData
)

data class StationInfoData(
    val stations: List<StationItem>
)

data class StationItem(
    @SerializedName("station_id") val id: String,
    val name: String,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val capacity: Int
)
```

### B. Serwis sieciowy (`VeturiloApiService.kt`)
```kotlin
package com.example.veturilowawa.data.network

import com.example.veturilowawa.data.model.StationInfoResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface VeturiloApiService {
    @GET("station_information.json")
    suspend fun getStations(): StationInfoResponse

    companion object {
        private const val BASE_URL = "https://gbfs.urbansharing.com/rowerowawarszawa.pl/"

        fun create(): VeturiloApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VeturiloApiService::class.java)
        }
    }
}
```

### C. Logika w ViewModel (`MainViewModel.kt`)
```kotlin
package com.example.veturilowawa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veturilowawa.data.model.StationItem
import com.example.veturilowawa.data.network.VeturiloApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val stations: List<StationItem>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    private val api = VeturiloApiService.create()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var allStations = listOf<StationItem>()
    val searchQuery = MutableStateFlow("")

    init {
        loadStations()
    }

    fun loadStations() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = api.getStations()
                allStations = response.data.stations
                filterStations(searchQuery.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Błąd pobierania danych: ${e.localizedMessage}")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        filterStations(query)
    }

    private fun filterStations(query: String) {
        val filtered = if (query.isBlank()) {
            allStations
        } else {
            allStations.filter { it.name.contains(query, ignoreCase = true) }
        }
        _uiState.value = UiState.Success(filtered)
    }
}
```

### D. Nawigacja do Google Maps (Intent)
Prosty kod, który otwiera mapę w telefonie:
```kotlin
fun openGoogleMaps(context: Context, lat: Double, lon: Double, stationName: String) {
    // lat (szerokość geograficzna) , lon (długość geograficzna)
    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($stationName)")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
```

---

## 7. Q/A

1. **Dlaczego Jetpack Compose a nie XML?**
   * Compose to deklaratywny UI w czystym Kotlinie – eliminuje pliki XML, `findViewById` i skomplikowane adaptery list (`RecyclerView`). Kod jest krótszy i mniej podatny na błędy.
2. **Co robi `suspend` przy funkcji `getStations()`?**
   * Oznacza funkcję zawieszalną (Kotlin Coroutines), która wykonuje zapytanie sieciowe asynchronicznie w tle, dzięki czemu interfejs aplikacji nie zacina się podczas pobierania danych.
3. **Czym jest `ViewModel` i dlaczego jest użyty?**
   * Ponieważ przetrzymuje stan ekranu (listę, stan ładowania) i przetrwa obrócenie ekranu telefonu, zapobiegając niepotrzebnemu ponownemu pobieraniu danych z internetu.
4. **Dlaczego jest użyty `Intent` zamiast Google Maps SDK?**
   * Zamiast obciążać aplikację zewnętrznym SDK i kluczami API, wykorzystujemy natywną aplikację zainstalowaną w systemie Android (`Intent.ACTION_VIEW`), co oszczędza zasoby i baterię.
