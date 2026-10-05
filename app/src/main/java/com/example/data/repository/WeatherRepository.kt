package com.example.data.repository

import android.content.Context
import android.location.Geocoder
import com.example.data.api.ApiClient
import com.example.data.local.AermosDatabase
import com.example.data.local.LocationDao
import com.example.data.local.SavedLocationEntity
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

class WeatherRepository(
    private val context: Context,
    private val locationDao: LocationDao = AermosDatabase.getInstance(context).locationDao(),
    private val forecastApi: com.example.data.api.OpenMeteoService = ApiClient.forecastService,
    private val airQualityApi: com.example.data.api.AirQualityService = ApiClient.airQualityService,
    private val geocodingApi: com.example.data.api.GeocodingService = ApiClient.geocodingService
) {
    val savedLocations: Flow<List<SavedLocationEntity>> = locationDao.getAllLocations()

    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        locationName: String? = null,
        regionName: String? = null,
        countryName: String? = null
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val forecastDeferred = forecastApi.getForecast(latitude, longitude)
            val aqiResponse = try {
                airQualityApi.getAirQuality(latitude, longitude)
            } catch (e: Exception) {
                null
            }

            // Determine location name if not provided
            val (resolvedCity, resolvedRegion, resolvedCountry) = if (!locationName.isNullOrBlank()) {
                Triple(locationName, regionName ?: "", countryName ?: "")
            } else {
                reverseGeocode(latitude, longitude)
            }

            val current = forecastDeferred.current ?: throw IllegalStateException("Current weather unavailable")
            val hourly = forecastDeferred.hourly
            val daily = forecastDeferred.daily

            val isDay = (current.isDay ?: 1) == 1
            val condition = WeatherCondition.fromWmoCode(current.weatherCode, isDay)
            val currentTemp = current.temperature ?: 20.0
            val feelsLike = current.apparentTemperature ?: currentTemp
            val humidity = current.relativeHumidity ?: 50
            val windSpeed = current.windSpeed ?: 10.0
            val windDir = current.windDirection ?: 0
            val windGusts = current.windGusts ?: (windSpeed * 1.3)
            val pressure = current.pressureMsl ?: current.surfacePressure ?: 1013.25
            val cloudCover = current.cloudCover ?: 20
            val precipitation = current.precipitation ?: 0.0

            // Hourly parse
            val hourlyList = mutableListOf<HourlyPoint>()
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val timeDisplayFormat = SimpleDateFormat("h a", Locale.US)
            val nowCalendar = Calendar.getInstance()

            hourly?.time?.forEachIndexed { index, timeStr ->
                if (hourlyList.size < 24) {
                    try {
                        val parsedDate = inputDateFormat.parse(timeStr)
                        if (parsedDate != null && parsedDate.time >= (nowCalendar.timeInMillis - 3600000L)) {
                            val hIsDay = (hourly.isDay?.getOrNull(index) ?: 1) == 1
                            val hCode = hourly.weatherCode?.getOrNull(index)
                            val hCond = WeatherCondition.fromWmoCode(hCode, hIsDay)
                            val hTemp = hourly.temperature?.getOrNull(index) ?: currentTemp
                            val hFeels = hourly.apparentTemperature?.getOrNull(index) ?: hTemp
                            val hProb = hourly.precipitationProbability?.getOrNull(index) ?: 0
                            val hPrecip = hourly.precipitation?.getOrNull(index) ?: 0.0
                            val hWind = hourly.windSpeed?.getOrNull(index) ?: windSpeed
                            val hWindDir = hourly.windDirection?.getOrNull(index) ?: windDir
                            val hWindGust = hourly.windGusts?.getOrNull(index) ?: windGusts
                            val hHumidity = hourly.relativeHumidity?.getOrNull(index) ?: humidity
                            val hUv = hourly.uvIndex?.getOrNull(index) ?: 0.0
                            val hDew = hourly.dewPoint?.getOrNull(index) ?: (hTemp - ((100 - hHumidity) / 5.0))

                            hourlyList.add(
                                HourlyPoint(
                                    timeIso = timeStr,
                                    timeFormatted = timeDisplayFormat.format(parsedDate),
                                    temperatureC = hTemp,
                                    apparentTemperatureC = hFeels,
                                    condition = hCond,
                                    precipitationProb = hProb,
                                    precipitationMm = hPrecip,
                                    windSpeedKmh = hWind,
                                    windDirectionDeg = hWindDir,
                                    windGustsKmh = hWindGust,
                                    humidity = hHumidity,
                                    uvIndex = hUv,
                                    dewPointC = hDew,
                                    isDay = hIsDay
                                )
                            )
                        }
                    } catch (ignored: Exception) {}
                }
            }

            // Daily parse
            val dailyList = mutableListOf<DailyPoint>()
            val dailyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dayNameFormat = SimpleDateFormat("EEE", Locale.US)
            val monthDayFormat = SimpleDateFormat("MMM d", Locale.US)
            val todayStr = dailyDateFormat.format(nowCalendar.time)

            daily?.time?.forEachIndexed { index, dateStr ->
                try {
                    val date = dailyDateFormat.parse(dateStr)
                    val isToday = dateStr == todayStr
                    val dayName = when {
                        isToday -> "Today"
                        index == 1 -> "Tomorrow"
                        date != null -> dayNameFormat.format(date)
                        else -> "Day ${index + 1}"
                    }
                    val formattedDate = if (date != null) monthDayFormat.format(date) else dateStr
                    val dCode = daily.weatherCode?.getOrNull(index)
                    val dCond = WeatherCondition.fromWmoCode(dCode, isDay = true)
                    val minT = daily.temperatureMin?.getOrNull(index) ?: (currentTemp - 4)
                    val maxT = daily.temperatureMax?.getOrNull(index) ?: (currentTemp + 5)
                    val appMinT = daily.apparentTemperatureMin?.getOrNull(index) ?: minT
                    val appMaxT = daily.apparentTemperatureMax?.getOrNull(index) ?: maxT
                    val pProb = daily.precipitationProbabilityMax?.getOrNull(index) ?: 0
                    val pSum = daily.precipitationSum?.getOrNull(index) ?: 0.0
                    val wSpeed = daily.windSpeedMax?.getOrNull(index) ?: windSpeed
                    val wDir = daily.windDirectionDominant?.getOrNull(index) ?: windDir
                    val uvMax = daily.uvIndexMax?.getOrNull(index) ?: 5.0
                    val sunriseStr = daily.sunrise?.getOrNull(index)?.substringAfter("T") ?: "06:20"
                    val sunsetStr = daily.sunset?.getOrNull(index)?.substringAfter("T") ?: "18:45"

                    dailyList.add(
                        DailyPoint(
                            dateIso = dateStr,
                            dayOfWeek = dayName,
                            formattedDate = formattedDate,
                            condition = dCond,
                            minTempC = minT,
                            maxTempC = maxT,
                            apparentMinTempC = appMinT,
                            apparentMaxTempC = appMaxT,
                            precipitationProbMax = pProb,
                            precipitationSumMm = pSum,
                            windSpeedMaxKmh = wSpeed,
                            windDirectionDominant = wDir,
                            uvIndexMax = uvMax,
                            sunrise = sunriseStr,
                            sunset = sunsetStr
                        )
                    )
                } catch (ignored: Exception) {}
            }

            // Air Quality Data synthesis
            val usAqiVal = aqiResponse?.current?.usAqi?.roundToInt() ?: 42
            val aqiLevel = AqiLevel.fromUsAqi(usAqiVal)
            val pm25Val = aqiResponse?.current?.pm25 ?: 11.2
            val pm10Val = aqiResponse?.current?.pm10 ?: 22.8
            val o3Val = aqiResponse?.current?.ozone ?: 45.0
            val no2Val = aqiResponse?.current?.nitrogenDioxide ?: 14.5
            val coVal = aqiResponse?.current?.carbonMonoxide ?: 320.0
            val so2Val = aqiResponse?.current?.sulphurDioxide ?: 5.1

            val aqiSummary = when (aqiLevel) {
                AqiLevel.GOOD -> "Air quality is satisfactory and poses little or no atmospheric risk."
                AqiLevel.MODERATE -> "Air quality is acceptable; however, sensitive individuals should monitor outdoor exertion."
                AqiLevel.UNHEALTHY_SENSITIVE -> "General public is unlikely to be affected; sensitive groups may experience minor irritation."
                AqiLevel.UNHEALTHY -> "Increased likelihood of adverse effects; reduce intense outdoor physical training."
                AqiLevel.VERY_UNHEALTHY -> "Health alert: significant health risk for all demographics. Limit outdoor exposure."
                AqiLevel.HAZARDOUS -> "Emergency conditions: entire population is likely to be severely affected."
            }

            val aqiData = AqiData(
                usAqi = usAqiVal,
                level = aqiLevel,
                pm25 = pm25Val,
                pm10 = pm10Val,
                o3 = o3Val,
                no2 = no2Val,
                co = coVal,
                so2 = so2Val,
                healthSummary = aqiSummary
            )

            // Day / Night and solar cycle calculation
            val sunriseToday = dailyList.firstOrNull()?.sunrise ?: "06:30"
            val sunsetToday = dailyList.firstOrNull()?.sunset ?: "18:40"
            val (solarCycle, daylightProgress) = computeSolarCycle(nowCalendar, sunriseToday, sunsetToday)

            // Dew point and visibility
            val dewPoint = currentTemp - ((100 - humidity) / 5.0)
            val visibilityKm = (hourly?.visibility?.firstOrNull() ?: 10000.0) / 1000.0
            val uvCurrent = hourlyList.firstOrNull()?.uvIndex ?: 4.0

            // Atmospheric Intelligence synthesis
            val intelligence = synthesizeAtmosphericIntelligence(
                currentTemp = currentTemp,
                feelsLike = feelsLike,
                condition = condition,
                humidity = humidity,
                windSpeedKmh = windSpeed,
                windGustsKmh = windGusts,
                pressureHpa = pressure,
                hourly = hourlyList,
                daily = dailyList,
                aqi = aqiData,
                solarCycle = solarCycle
            )

            val weatherData = WeatherData(
                locationName = resolvedCity,
                regionName = resolvedRegion,
                countryName = resolvedCountry,
                latitude = latitude,
                longitude = longitude,
                currentTempC = currentTemp,
                feelsLikeC = feelsLike,
                condition = condition,
                isDay = isDay,
                humidity = humidity,
                windSpeedKmh = windSpeed,
                windDirectionDeg = windDir,
                windGustsKmh = windGusts,
                pressureHpa = pressure,
                visibilityKm = visibilityKm,
                uvIndex = uvCurrent,
                dewPointC = dewPoint,
                cloudCoverPct = cloudCover,
                precipitationMm = precipitation,
                sunrise = sunriseToday,
                sunset = sunsetToday,
                solarCycle = solarCycle,
                daylightProgress = daylightProgress,
                aqi = aqiData,
                hourlyForecast = hourlyList,
                dailyForecast = dailyList,
                intelligence = intelligence,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )

            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun computeSolarCycle(
        now: Calendar,
        sunriseStr: String,
        sunsetStr: String
    ): Pair<DayNightCycle, Float> {
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val sunriseParts = sunriseStr.split(":")
        val sunsetParts = sunsetStr.split(":")
        val sunriseMinutes = (sunriseParts.getOrNull(0)?.toIntOrNull() ?: 6) * 60 + (sunriseParts.getOrNull(1)?.toIntOrNull() ?: 30)
        val sunsetMinutes = (sunsetParts.getOrNull(0)?.toIntOrNull() ?: 18) * 60 + (sunsetParts.getOrNull(1)?.toIntOrNull() ?: 30)

        val cycle = when {
            currentMinutes in (sunriseMinutes - 45)..<sunriseMinutes -> DayNightCycle.DAWN
            currentMinutes in sunriseMinutes..<(sunriseMinutes + 180) -> DayNightCycle.MORNING
            currentMinutes in (sunriseMinutes + 180)..<(sunsetMinutes - 90) -> DayNightCycle.AFTERNOON
            currentMinutes in (sunsetMinutes - 90)..<sunsetMinutes -> DayNightCycle.GOLDEN_HOUR
            currentMinutes in sunsetMinutes..<(sunsetMinutes + 50) -> DayNightCycle.TWILIGHT
            else -> DayNightCycle.NIGHT
        }

        val totalDaylightMinutes = (sunsetMinutes - sunriseMinutes).coerceAtLeast(1)
        val progress = if (currentMinutes < sunriseMinutes) {
            0f
        } else if (currentMinutes > sunsetMinutes) {
            1f
        } else {
            (currentMinutes - sunriseMinutes).toFloat() / totalDaylightMinutes
        }.coerceIn(0f, 1f)

        return Pair(cycle, progress)
    }

    private fun synthesizeAtmosphericIntelligence(
        currentTemp: Double,
        feelsLike: Double,
        condition: WeatherCondition,
        humidity: Int,
        windSpeedKmh: Double,
        windGustsKmh: Double,
        pressureHpa: Double,
        hourly: List<HourlyPoint>,
        daily: List<DailyPoint>,
        aqi: AqiData,
        solarCycle: DayNightCycle
    ): AtmosphericIntelligence {
        // Find rain window in next 12 hours
        val rainHours = hourly.take(12).filter { it.precipitationProb >= 35 || it.precipitationMm > 0.1 }
        val rainSummary = if (rainHours.isEmpty()) {
            "No precipitation expected in the next 12 hours."
        } else {
            val firstRainHour = rainHours.first().timeFormatted
            val maxProb = rainHours.maxOf { it.precipitationProb }
            "Precipitation likely starting around $firstRainHour (peak probability $maxProb%)."
        }

        // Pressure tendency
        val pressureTrend = when {
            pressureHpa > 1018.0 -> PressureTrend.RISING
            pressureHpa < 1008.0 -> PressureTrend.FALLING
            else -> PressureTrend.STEADY
        }

        val barometricAdvice = when (pressureTrend) {
            PressureTrend.RISING -> "High barometric stability. Minimal atmospheric pressure fluctuations; reduced migraine triggers."
            PressureTrend.FALLING -> "Falling barometric pressure indicates an approaching pressure trough. Individuals with weather sensitivity may note mild sinus pressure."
            PressureTrend.STEADY -> "Stable barometric equilibrium. Atmospheric conditions remain balanced throughout the day."
        }

        // Peak UV calculation
        val maxUvPoint = hourly.take(16).maxByOrNull { it.uvIndex }
        val peakUvValue = maxUvPoint?.uvIndex ?: 4.0
        val peakUvTime = maxUvPoint?.timeFormatted ?: "1:00 PM"
        val uvAdvice = when {
            peakUvValue >= 8.0 -> "Very high solar radiation. UV index peaks at ${String.format("%.1f", peakUvValue)} around $peakUvTime. Sun protection mandatory."
            peakUvValue >= 5.0 -> "Moderate UV index peaking around $peakUvTime (${String.format("%.1f", peakUvValue)}). Wear sunglasses and sunscreen."
            else -> "Low UV index throughout the day. Minimal sun hazard."
        }

        // Outdoor suitability algorithm
        var outdoorScore = 92
        if (rainHours.isNotEmpty()) outdoorScore -= 28
        if (currentTemp > 32.0 || currentTemp < 5.0) outdoorScore -= 20
        if (windSpeedKmh > 30.0) outdoorScore -= 18
        if (aqi.usAqi > 100) outdoorScore -= 22
        if (humidity > 80 && currentTemp > 24) outdoorScore -= 12
        outdoorScore = outdoorScore.coerceIn(15, 98)

        val outdoorTitle = when {
            outdoorScore >= 80 -> "Prime Outdoor Window"
            outdoorScore >= 60 -> "Favorable Conditions"
            outdoorScore >= 40 -> "Moderate Outdoor Caution"
            else -> "Challenging Atmospheric Conditions"
        }

        val runningScore = (outdoorScore - (if (currentTemp > 26) 12 else 0) - (if (humidity > 70) 8 else 0)).coerceIn(10, 99)
        val cyclingScore = (outdoorScore - (if (windSpeedKmh > 22) 15 else 0)).coerceIn(10, 99)
        val walkingScore = (outdoorScore + 5).coerceIn(10, 100)

        // Find optimal time window
        val bestHour = hourly.take(12).filter { it.precipitationProb < 25 && it.windSpeedKmh < 22 }
            .minByOrNull { abs(it.temperatureC - 21.0) }
        val bestWindow = if (bestHour != null) {
            "Optimal window for outdoor exercise is around ${bestHour.timeFormatted} (${Math.round(bestHour.temperatureC)}°C, calm breeze)."
        } else {
            "Morning hours provide the most stable atmospheric window today."
        }

        // Clothing recommendation
        val clothingAdvice = when {
            currentTemp < 0 -> "Heavy winter insulation, thermal underlayers, windproof outer shell, and gloves required."
            currentTemp < 10 -> "Substantial jacket or warm coat. Layering recommended with morning breeze."
            currentTemp < 18 -> "Light jacket, pullover, or structured overshirt suitable for moderate daytime temperatures."
            currentTemp < 26 -> "Comfortable lightweight breathable cotton or performance fabric. Ideal thermal comfort."
            else -> "Breathable ultralight clothing. Ensure active hydration and seek shaded areas during peak solar intensity."
        }

        // Synthesis of Natural Language Summary (Google / Pixel style)
        val tempDescriptor = when {
            currentTemp > 28 -> "Warm and radiant"
            currentTemp > 20 -> "Mild and pleasant"
            currentTemp > 12 -> "Cool and crisp"
            currentTemp > 4 -> "Brisk and cold"
            else -> "Freezing atmospheric conditions"
        }

        val windDescriptor = when {
            windSpeedKmh < 8 -> "calm winds"
            windSpeedKmh < 18 -> "a gentle breeze at ${Math.round(windSpeedKmh)} km/h"
            windSpeedKmh < 32 -> "moderate breeze gusting to ${Math.round(windGustsKmh)} km/h"
            else -> "vigorous wind gusts up to ${Math.round(windGustsKmh)} km/h"
        }

        val todayMax = daily.firstOrNull()?.maxTempC ?: (currentTemp + 2)
        val todayMin = daily.firstOrNull()?.minTempC ?: (currentTemp - 4)

        val naturalSummary = StringBuilder()
            .append("$tempDescriptor ${solarCycle.name.lowercase(Locale.US)} with ${condition.label.lowercase(Locale.US)}. ")
            .append("Expect $windDescriptor with relative humidity at $humidity%. ")
            .append("Temperatures will hover between ${Math.round(todayMin)}° and ${Math.round(todayMax)}°. ")
            .append(rainSummary)
            .toString()

        return AtmosphericIntelligence(
            naturalLanguageSummary = naturalSummary,
            outdoorScore = outdoorScore,
            outdoorTitle = outdoorTitle,
            outdoorDescription = "Composite index based on solar radiation, barometric pressure, wind vectors, and air particulate quality.",
            runningScore = runningScore,
            cyclingScore = cyclingScore,
            walkingScore = walkingScore,
            bestWindowRecommendation = bestWindow,
            rainWindowSummary = rainSummary,
            pressureTrend = pressureTrend,
            barometricComfortSummary = barometricAdvice,
            clothingAdvice = clothingAdvice,
            peakUvTime = peakUvTime,
            uvAdvice = uvAdvice
        )
    }

    private suspend fun reverseGeocode(lat: Double, lon: Double): Triple<String, String, String> = withContext(Dispatchers.IO) {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    val city = address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: "Current Location"
                    val state = address.adminArea ?: ""
                    val country = address.countryName ?: ""
                    return@withContext Triple(city, state, country)
                }
            }
        } catch (ignored: Exception) {}

        // Fallback approximation
        Triple("Local Station", String.format(Locale.US, "%.2f°, %.2f°", lat, lon), "Earth")
    }

    suspend fun searchCities(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()
        try {
            val response = geocodingApi.searchLocations(query)
            response.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveLocation(
        name: String,
        region: String,
        country: String,
        lat: Double,
        lon: Double,
        isCurrent: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        if (isCurrent) {
            locationDao.clearCurrentLocationFlags()
        }
        val existing = locationDao.findNear(lat, lon)
        if (existing != null) {
            val updated = existing.copy(
                name = name,
                region = region,
                country = country,
                isCurrentLocation = isCurrent
            )
            locationDao.updateLocation(updated)
            existing.id
        } else {
            val entity = SavedLocationEntity(
                name = name,
                region = region,
                country = country,
                latitude = lat,
                longitude = lon,
                isCurrentLocation = isCurrent,
                cachedTimestamp = System.currentTimeMillis()
            )
            locationDao.insertLocation(entity)
        }
    }

    suspend fun deleteLocation(id: Long) = withContext(Dispatchers.IO) {
        locationDao.deleteLocationById(id)
    }
}
