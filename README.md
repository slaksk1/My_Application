# Veturilo Wawa 

Aplikacja mobilna na platformę Android stworzona w **Jetpack Compose (Material 3)** oraz architekturze **MVVM**, prezentująca stan stacji roweru miejskiego w Warszawie (Veturilo) w czasie rzeczywistym.

---

## 📱 Funkcjonalności

- **Pobieranie danych na żywo z oficjalnego API GBFS** (General Bikeshare Feed Specification) systemu Nextbike Warszawa (Veturilo).
- **Lista stacji (`LazyColumn`)**:
  - Nazwa stacji oraz adres / lokalizacja.
  - Czytelne plakietki z liczbą wolnych rowerów (`🟢 X wolnych rowerów`) oraz wolnych stojaków (`🅿️ Y stojaków`).
- **Wyszukiwarka w czasie rzeczywistym**: natychmiastowe filtrowanie stacji po wpisaniu tekstu (np. *Marszałkowska*, *Metro*, *Dworzec*).
- **Szybki filtr**: przełącznik „Tylko wolne rowery (>0)” ukrywający stacje bez dostępnych jednośladów.
- **Karta szczegółów (Modal Bottom Sheet)**:
  - Dokładny identyfikator stacji, współrzędne GPS.
  - Liczba rowerów, wolnych stojaków oraz łączna pojemność stacji.
- **Nawigacja w Mapach Google**:
  - Przycisk **„Prowadź do stacji”** / **„Prowadź w Google Maps”** uruchamia natywną aplikację nawigacyjną Google Maps za pomocą mechanizmu `Intent.ACTION_VIEW` (`geo:lat,lon?q=...`) bez konieczności płatnych kluczy Google Maps SDK.
- **Obsługa stanów interfejsu (UiState)**:
  - Stan ładowania (`CircularProgressIndicator`).
  - Stan błędu (brak internetu / błąd serwera) z przyciskiem „Spróbuj ponownie”.
  - Przycisk odświeżenia danych na pasku aplikacji (`TopAppBar`).

---

## Architektura i technologie

- **Język:** Kotlin
- **Interfejs:** Jetpack Compose + Material 3 (deklaratywny UI)
- **Wzorzec projektowy:** MVVM (Model - View - ViewModel)
  - `data/model/StationModels.kt` – modele danych GBFS oraz UI model
  - `data/network/VeturiloApiService.kt` – interfejs komunikacji sieciowej Retrofit
  - `ui/MainViewModel.kt` – logika biznesowa, przetwarzanie zapytań i StateFlow
  - `ui/screens/StationListScreen.kt` – komponenty widoku Compose
  - `ui/MainActivity.kt` – aktywność wejściowa
- **Biblioteki:**
  - `Retrofit 2` + `GsonConverterFactory`
  - `Kotlin Coroutines` (`viewModelScope`, asynchroniczne zapytania równoległe `async`/`await`)
  - `StateFlow` / `Flow` (reaktywne zarządzanie stanem)
  - `Lifecycle ViewModel Compose`

---

## Źródła danych (GBFS Veturilo)

Aplikacja korzysta z publicznych feedów GBFS 2.3:
- **Informacje o stacjach:** `https://gbfs.nextbike.net/maps/gbfs/v2/nextbike_vw/pl/station_information.json`
- **Dostępność i status stacji:** `https://gbfs.nextbike.net/maps/gbfs/v2/nextbike_vw/pl/station_status.json`

---

## Jak uruchomić projekt

1. Otwórz projekt w **Android Studio** (Koala / Ladybug lub nowszy).
2. Poczekaj na zsynchronizowanie projektu przez Gradle.
3. Podłącz telefon z Androidem (debugowanie USB) lub uruchom emulator (API 24+).
4. Kliknij zielony przycisk **Run 'app'** (`Shift + F10`).
