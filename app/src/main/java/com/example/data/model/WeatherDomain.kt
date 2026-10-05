package com.example.data.model

enum class WeatherCondition(val label: String, val iconDescription: String) {
    CLEAR_DAY("Clear Sky", "Sunny daylight with high visibility"),
    CLEAR_NIGHT("Clear Sky", "Crisp starry atmospheric visibility"),
    MAINLY_CLEAR_DAY("Mostly Clear", "Scattered high-altitude light clouds"),
    MAINLY_CLEAR_NIGHT("Mostly Clear", "Occasional transparent cloud cover"),
    PARTLY_CLOUDY_DAY("Partly Cloudy", "Passing cumulus clouds with sun intervals"),
    PARTLY_CLOUDY_NIGHT("Partly Cloudy", "Layered nocturnal cloud cover"),
    OVERCAST("Overcast", "Dense stratified cloud layer"),
    FOG("Fog & Mist", "Low-lying vapor reducing surface visibility"),
    DRIZZLE("Light Drizzle", "Fine intermittent water droplets"),
    RAIN("Rain", "Sustained atmospheric precipitation"),
    HEAVY_RAIN("Heavy Rain", "Intense precipitation with strong surface runoff"),
    THUNDERSTORM("Thunderstorm", "Atmospheric electrical discharge with rain"),
    SNOW("Snow", "Falling ice crystalline precipitation"),
    HEAVY_SNOW("Heavy Snow", "Significant snowfall with reduced visibility"),
    WINDY("High Winds", "Vigorous atmospheric pressure gradient");

    companion object {
        fun fromWmoCode(code: Int?, isDay: Boolean = true): WeatherCondition {
            return when (code) {
                0 -> if (isDay) CLEAR_DAY else CLEAR_NIGHT
                1 -> if (isDay) MAINLY_CLEAR_DAY else MAINLY_CLEAR_NIGHT
                2 -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
                3 -> OVERCAST
                45, 48 -> FOG
                51, 53, 55, 56, 57 -> DRIZZLE
                61, 63, 80, 81 -> RAIN
                65, 82 -> HEAVY_RAIN
                71, 73, 77, 85 -> SNOW
                75, 86 -> HEAVY_SNOW
                95, 96, 99 -> THUNDERSTORM
                else -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            }
        }
    }
}

enum class DayNightCycle {
    DAWN,
    MORNING,
    AFTERNOON,
    GOLDEN_HOUR,
    TWILIGHT,
    NIGHT
}

enum class AqiLevel(val label: String, val severityColorHex: Long) {
    GOOD("Good", 0xFF34D399),
    MODERATE("Moderate", 0xFFFBBF24),
    UNHEALTHY_SENSITIVE("Sensitive Groups", 0xFFFB923C),
    UNHEALTHY("Unhealthy", 0xFFF87171),
    VERY_UNHEALTHY("Very Unhealthy", 0xFFA855F7),
    HAZARDOUS("Hazardous", 0xFF881337);

    companion object {
        fun fromUsAqi(aqi: Int): AqiLevel = when {
            aqi <= 50 -> GOOD
            aqi <= 100 -> MODERATE
            aqi <= 150 -> UNHEALTHY_SENSITIVE
            aqi <= 200 -> UNHEALTHY
            aqi <= 300 -> VERY_UNHEALTHY
            else -> HAZARDOUS
        }
    }
}

enum class PressureTrend(val label: String) {
    RISING("Rising • Barometric High"),
    STEADY("Steady • Stable Atmosphere"),
    FALLING("Falling • Approaching Front")
}

data class AqiData(
    val usAqi: Int,
    val level: AqiLevel,
    val pm25: Double,
    val pm10: Double,
    val o3: Double,
    val no2: Double,
    val co: Double,
    val so2: Double,
    val healthSummary: String
)

data class HourlyPoint(
    val timeIso: String,
    val timeFormatted: String, // e.g. "14:00" or "2 PM"
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val condition: WeatherCondition,
    val precipitationProb: Int,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windGustsKmh: Double,
    val humidity: Int,
    val uvIndex: Double,
    val dewPointC: Double,
    val isDay: Boolean
)

data class DailyPoint(
    val dateIso: String,
    val dayOfWeek: String, // e.g. "Today", "Mon", "Tue"
    val formattedDate: String, // e.g. "Oct 5"
    val condition: WeatherCondition,
    val minTempC: Double,
    val maxTempC: Double,
    val apparentMinTempC: Double,
    val apparentMaxTempC: Double,
    val precipitationProbMax: Int,
    val precipitationSumMm: Double,
    val windSpeedMaxKmh: Double,
    val windDirectionDominant: Int,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String,
    val hourlySubList: List<HourlyPoint> = emptyList()
)

data class AtmosphericIntelligence(
    val naturalLanguageSummary: String,
    val outdoorScore: Int, // 0 - 100
    val outdoorTitle: String,
    val outdoorDescription: String,
    val runningScore: Int,
    val cyclingScore: Int,
    val walkingScore: Int,
    val bestWindowRecommendation: String,
    val rainWindowSummary: String,
    val pressureTrend: PressureTrend,
    val barometricComfortSummary: String,
    val clothingAdvice: String,
    val peakUvTime: String,
    val uvAdvice: String
)

data class WeatherData(
    val locationName: String,
    val regionName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val currentTempC: Double,
    val feelsLikeC: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windGustsKmh: Double,
    val pressureHpa: Double,
    val visibilityKm: Double,
    val uvIndex: Double,
    val dewPointC: Double,
    val cloudCoverPct: Int,
    val precipitationMm: Double,
    val sunrise: String,
    val sunset: String,
    val solarCycle: DayNightCycle,
    val daylightProgress: Float, // 0.0 to 1.0
    val aqi: AqiData,
    val hourlyForecast: List<HourlyPoint>,
    val dailyForecast: List<DailyPoint>,
    val intelligence: AtmosphericIntelligence,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val isFromCache: Boolean = false
)

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun convert(tempC: Double): Double = when (this) {
        CELSIUS -> tempC
        FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
    }

    fun format(tempC: Double): String = "${Math.round(convert(tempC))}°"
}

enum class SpeedUnit(val label: String) {
    KMH("km/h"),
    MPH("mph"),
    MS("m/s");

    fun convert(kmh: Double): Double = when (this) {
        KMH -> kmh
        MPH -> kmh * 0.621371
        MS -> kmh / 3.6
    }

    fun format(kmh: Double): String = "${String.format("%.1f", convert(kmh))} $label"
}

enum class PressureUnit(val label: String) {
    HPA("hPa"),
    INHG("inHg");

    fun convert(hpa: Double): Double = when (this) {
        HPA -> hpa
        INHG -> hpa * 0.02953
    }

    fun format(hpa: Double): String = when (this) {
        HPA -> "${Math.round(hpa)} hPa"
        INHG -> "${String.format("%.2f", convert(hpa))} inHg"
    }
}

data class UserSettings(
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val dynamicTheme: Boolean = true,
    val severeWeatherAlerts: Boolean = true,
    val rainAlerts: Boolean = true,
    val morningSummary: Boolean = true,
    val autoRefreshMinutes: Int = 30
)
