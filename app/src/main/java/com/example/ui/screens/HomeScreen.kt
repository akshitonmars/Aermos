package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherData
import com.example.ui.components.*
import com.example.ui.theme.AermosDarkSurface
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.util.AermosHaptics
import com.example.ui.viewmodel.LocationTarget
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeLocation by viewModel.activeLocation.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                AermosAtmosphereCanvas(
                    condition = com.example.data.model.WeatherCondition.PARTLY_CLOUDY_DAY,
                    solarCycle = com.example.data.model.DayNightCycle.AFTERNOON,
                    windSpeedKmh = 12.0,
                    precipitationMm = 0.0
                )
                LoadingSkeletonView(
                    locationName = activeLocation.name,
                    onRefresh = { viewModel.refreshWeather() }
                )
            }
            is WeatherUiState.Error -> {
                AermosAtmosphereCanvas(
                    condition = com.example.data.model.WeatherCondition.OVERCAST,
                    solarCycle = com.example.data.model.DayNightCycle.TWILIGHT,
                    windSpeedKmh = 15.0,
                    precipitationMm = 0.0
                )
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.refreshWeather() }
                )
            }
            is WeatherUiState.Success -> {
                val data = state.data
                // Dynamic Environmental Canvas
                AermosAtmosphereCanvas(
                    condition = data.condition,
                    solarCycle = data.solarCycle,
                    windSpeedKmh = data.windSpeedKmh,
                    precipitationMm = data.precipitationMm
                )

                // Foreground Scrollable Weather Content
                WeatherHomeContent(
                    data = data,
                    isRefreshing = state.isRefreshing,
                    settings = settings,
                    onRefresh = { viewModel.refreshWeather() },
                    onOpenLocations = { viewModel.selectTab(com.example.ui.viewmodel.AermosTab.LOCATIONS) }
                )
            }
        }
    }
}

@Composable
private fun WeatherHomeContent(
    data: WeatherData,
    isRefreshing: Boolean,
    settings: com.example.data.model.UserSettings,
    onRefresh: () -> Unit,
    onOpenLocations: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Refresh icon animation
    val infiniteTransition = rememberInfiniteTransition(label = "RefreshAnim")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Spin"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Top App Bar: Aermos by DeBroglie Brand + Location Pill + Refresh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AermosWordmark(fontSize = 17, showSubtitle = true)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Location selector pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(AermosDarkSurface.copy(alpha = 0.85f))
                        .clickable {
                            AermosHaptics.click(context)
                            onOpenLocations()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Locations",
                        tint = AermosSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = data.locationName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Switch",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Refresh button
                IconButton(
                    onClick = {
                        AermosHaptics.heavyClick(context)
                        onRefresh()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AermosDarkSurface.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh telemetry",
                        tint = if (isRefreshing) AermosSkyBlue else Color.White,
                        modifier = Modifier.rotate(if (isRefreshing) spinAngle else 0f)
                    )
                }
            }
        }

        // Hero Weather Status Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${data.regionName}${if (data.countryName.isNotEmpty()) ", " + data.countryName else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = settings.tempUnit.format(data.currentTempC),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 80.sp,
                        lineHeight = 84.sp
                    )

                    Text(
                        text = data.condition.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f)
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Feels like ${settings.tempUnit.format(data.feelsLikeC)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val today = data.dailyForecast.firstOrNull()
                        if (today != null) {
                            Text(
                                text = "H: ${settings.tempUnit.format(today.maxTempC)}  L: ${settings.tempUnit.format(today.minTempC)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AermosSkyBlue
                            )
                        }
                    }
                }

                // Dynamic large animated weather glyph
                AermosWeatherIcon(
                    condition = data.condition,
                    size = 88.dp,
                    animated = true
                )
            }
        }

        // Aermos Atmospheric Intelligence Panel
        WeatherIntelligenceCard(intelligence = data.intelligence)

        // Hourly Forecast Timeline
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "HOURLY TELEMETRY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            HourlyForecastRow(
                hourlyPoints = data.hourlyForecast,
                tempUnit = settings.tempUnit,
                speedUnit = settings.speedUnit
            )
        }

        // Interactive Temperature Graph
        InteractiveTemperatureGraph(
            hourlyPoints = data.hourlyForecast,
            tempUnit = settings.tempUnit
        )

        // 10-Day Outlook
        DailyForecastList(
            dailyPoints = data.dailyForecast,
            tempUnit = settings.tempUnit,
            speedUnit = settings.speedUnit
        )

        // Weather Details: Wind & Air Quality
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "WIND & AERODYNAMICS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            InteractiveWindCompass(
                windSpeedKmh = data.windSpeedKmh,
                windDirectionDeg = data.windDirectionDeg,
                windGustsKmh = data.windGustsKmh,
                speedUnit = settings.speedUnit
            )
        }

        // Air Quality Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ATMOSPHERIC PURITY & AQI",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            AqiGauge(aqiData = data.aqi)
        }

        // Celestial Solar Arc
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SOLAR TRANSIT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            SolarArcView(
                sunrise = data.sunrise,
                sunset = data.sunset,
                daylightProgress = data.daylightProgress
            )
        }

        // 2x2 Grid of Technical Metrics: UV, Humidity, Pressure, Visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UvTile(
                uvIndex = data.uvIndex,
                peakUvTime = data.intelligence.peakUvTime,
                modifier = Modifier.weight(1f)
            )
            HumidityTile(
                humidity = data.humidity,
                dewPointC = data.dewPointC,
                tempUnit = settings.tempUnit,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BarometerTile(
                pressureHpa = data.pressureHpa,
                trend = data.intelligence.pressureTrend,
                pressureUnit = settings.pressureUnit,
                modifier = Modifier.weight(1f)
            )
            VisibilityTile(
                visibilityKm = data.visibilityKm,
                cloudCoverPct = data.cloudCoverPct,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoadingSkeletonView(
    locationName: String,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AermosEmblem(size = 56.dp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "CALIBRATING ATMOSPHERIC SENSORS",
            style = MaterialTheme.typography.labelMedium,
            color = AermosSkyBlue,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Acquiring telemetry for $locationName...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        LinearProgressIndicator(
            color = AermosSkyBlue,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .width(180.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun ErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudOff,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TELEMETRY UNAVAILABLE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AermosSkyBlue)
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry Connection", color = Color(0xFF041E34), fontWeight = FontWeight.Bold)
        }
    }
}
