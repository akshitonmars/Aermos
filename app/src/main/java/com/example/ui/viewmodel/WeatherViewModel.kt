package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.AermosLocationClient
import com.example.data.model.*
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AermosTab(val label: String) {
    HOME("Atmosphere"),
    RADAR("Radar"),
    AIR("Air Quality"),
    LOCATIONS("Locations"),
    SETTINGS("Settings")
}

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: WeatherData, val isRefreshing: Boolean = false) : WeatherUiState
    data class Error(val message: String, val canRetry: Boolean = true) : WeatherUiState
}

data class LocationTarget(
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentGps: Boolean = false
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application)
    private val locationClient = AermosLocationClient(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(AermosTab.HOME)
    val currentTab: StateFlow<AermosTab> = _currentTab.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _activeLocation = MutableStateFlow(
        LocationTarget(
            name = "San Francisco",
            region = "California",
            country = "United States",
            latitude = 37.7749,
            longitude = -122.4194,
            isCurrentGps = false
        )
    )
    val activeLocation: StateFlow<LocationTarget> = _activeLocation.asStateFlow()

    val savedLocations = repository.savedLocations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.example.data.model.GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<com.example.data.model.GeocodingResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Load initial weather
        loadWeatherForTarget(_activeLocation.value)
    }

    fun selectTab(tab: AermosTab) {
        _currentTab.value = tab
    }

    fun loadWeatherForTarget(target: LocationTarget, isRefresh: Boolean = false) {
        _activeLocation.value = target
        viewModelScope.launch {
            if (!isRefresh && _uiState.value !is WeatherUiState.Success) {
                _uiState.value = WeatherUiState.Loading
            } else if (isRefresh && _uiState.value is WeatherUiState.Success) {
                val currentData = (_uiState.value as WeatherUiState.Success).data
                _uiState.value = WeatherUiState.Success(currentData, isRefreshing = true)
            }

            val result = repository.getWeatherData(
                latitude = target.latitude,
                longitude = target.longitude,
                locationName = target.name,
                regionName = target.region,
                countryName = target.country
            )

            result.fold(
                onSuccess = { weatherData ->
                    _uiState.value = WeatherUiState.Success(weatherData, isRefreshing = false)
                },
                onFailure = { error ->
                    _uiState.value = WeatherUiState.Error(
                        message = error.localizedMessage ?: "Unable to fetch atmospheric telemetry. Check network connection."
                    )
                }
            )
        }
    }

    fun refreshWeather() {
        loadWeatherForTarget(_activeLocation.value, isRefresh = true)
    }

    fun fetchCurrentGpsLocation() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val loc = locationClient.getCurrentLocation()
                if (loc != null) {
                    val target = LocationTarget(
                        name = "Current Location",
                        region = "",
                        country = "",
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        isCurrentGps = true
                    )
                    // Save to Room DB as current
                    repository.saveLocation(
                        name = "Current Location",
                        region = "",
                        country = "",
                        lat = loc.latitude,
                        lon = loc.longitude,
                        isCurrent = true
                    )
                    loadWeatherForTarget(target)
                } else {
                    // Fall back to current active location
                    loadWeatherForTarget(_activeLocation.value)
                }
            } catch (e: Exception) {
                loadWeatherForTarget(_activeLocation.value)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce
            _isSearching.value = true
            val results = repository.searchCities(query.trim())
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun selectSearchResult(result: com.example.data.model.GeocodingResult) {
        val target = LocationTarget(
            name = result.name,
            region = result.admin1 ?: "",
            country = result.country ?: "",
            latitude = result.latitude,
            longitude = result.longitude,
            isCurrentGps = false
        )
        viewModelScope.launch {
            repository.saveLocation(
                name = result.name,
                region = result.admin1 ?: "",
                country = result.country ?: "",
                lat = result.latitude,
                lon = result.longitude,
                isCurrent = false
            )
        }
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _currentTab.value = AermosTab.HOME
        loadWeatherForTarget(target)
    }

    fun deleteSavedLocation(id: Long) {
        viewModelScope.launch {
            repository.deleteLocation(id)
        }
    }

    fun setTempUnit(unit: TemperatureUnit) {
        _settings.value = _settings.value.copy(tempUnit = unit)
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _settings.value = _settings.value.copy(speedUnit = unit)
    }

    fun setPressureUnit(unit: PressureUnit) {
        _settings.value = _settings.value.copy(pressureUnit = unit)
    }

    fun toggleAlert(type: String) {
        val s = _settings.value
        _settings.value = when (type) {
            "severe" -> s.copy(severeWeatherAlerts = !s.severeWeatherAlerts)
            "rain" -> s.copy(rainAlerts = !s.rainAlerts)
            "morning" -> s.copy(morningSummary = !s.morningSummary)
            else -> s
        }
    }
}
