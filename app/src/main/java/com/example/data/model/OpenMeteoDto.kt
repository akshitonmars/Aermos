package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "elevation") val elevation: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: OpenMeteoCurrent? = null,
    @Json(name = "hourly") val hourly: OpenMeteoHourly? = null,
    @Json(name = "daily") val daily: OpenMeteoDaily? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "time") val time: String? = null,
    @Json(name = "temperature_2m") val temperature: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity: Int? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "is_day") val isDay: Int? = null,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "cloud_cover") val cloudCover: Int? = null,
    @Json(name = "pressure_msl") val pressureMsl: Double? = null,
    @Json(name = "surface_pressure") val surfacePressure: Double? = null,
    @Json(name = "wind_speed_10m") val windSpeed: Double? = null,
    @Json(name = "wind_direction_10m") val windDirection: Int? = null,
    @Json(name = "wind_gusts_10m") val windGusts: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "temperature_2m") val temperature: List<Double>? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity: List<Int>? = null,
    @Json(name = "dew_point_2m") val dewPoint: List<Double>? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @Json(name = "precipitation") val precipitation: List<Double>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "pressure_msl") val pressureMsl: List<Double>? = null,
    @Json(name = "surface_pressure") val surfacePressure: List<Double>? = null,
    @Json(name = "cloud_cover") val cloudCover: List<Int>? = null,
    @Json(name = "visibility") val visibility: List<Double>? = null,
    @Json(name = "wind_speed_10m") val windSpeed: List<Double>? = null,
    @Json(name = "wind_direction_10m") val windDirection: List<Int>? = null,
    @Json(name = "wind_gusts_10m") val windGusts: List<Double>? = null,
    @Json(name = "uv_index") val uvIndex: List<Double>? = null,
    @Json(name = "is_day") val isDay: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>? = null,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>? = null,
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>? = null,
    @Json(name = "sunrise") val sunrise: List<String>? = null,
    @Json(name = "sunset") val sunset: List<String>? = null,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: List<Double>? = null,
    @Json(name = "wind_gusts_10m_max") val windGustsMax: List<Double>? = null,
    @Json(name = "wind_direction_10m_dominant") val windDirectionDominant: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "current") val current: OpenMeteoAirQualityCurrent? = null,
    @Json(name = "hourly") val hourly: OpenMeteoAirQualityHourly? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityCurrent(
    @Json(name = "time") val time: String? = null,
    @Json(name = "european_aqi") val europeanAqi: Double? = null,
    @Json(name = "us_aqi") val usAqi: Double? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double? = null,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double? = null,
    @Json(name = "ozone") val ozone: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityHourly(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "pm10") val pm10: List<Double>? = null,
    @Json(name = "pm2_5") val pm25: List<Double>? = null,
    @Json(name = "carbon_monoxide") val carbonMonoxide: List<Double>? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: List<Double>? = null,
    @Json(name = "ozone") val ozone: List<Double>? = null,
    @Json(name = "us_aqi") val usAqi: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoGeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "elevation") val elevation: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "admin2") val admin2: String? = null
)
