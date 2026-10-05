package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AqiLevel
import com.example.ui.components.AermosWordmark
import com.example.ui.components.AqiGauge
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun AirQualityScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val weatherData = (uiState as? WeatherUiState.Success)?.data

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AermosWordmark(fontSize = 17, showSubtitle = true)
            Text(
                text = "ATMOSPHERIC PURITY",
                style = MaterialTheme.typography.labelSmall,
                color = AermosAtmosphereTeal,
                letterSpacing = 1.sp
            )
        }

        if (weatherData != null) {
            val aqi = weatherData.aqi

            // Primary Gauge Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weatherData.locationName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "AIR QUALITY INDEX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                AqiGauge(aqiData = aqi)
            }

            // Health Recommendations Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "HEALTH & ACTIVITY ADVISORIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                AdvisoryItem(
                    icon = Icons.Rounded.HealthAndSafety,
                    title = "Outdoor Physical Exertion",
                    detail = if (aqi.level == AqiLevel.GOOD) "Conditions are safe for rigorous outdoor workouts, distance running, and cycling." else "Sensitive demographics should moderate prolonged heavy outdoor training."
                )

                AdvisoryItem(
                    icon = Icons.Rounded.Air,
                    title = "Ventilation & Fresh Air",
                    detail = if (aqi.level in listOf(AqiLevel.GOOD, AqiLevel.MODERATE)) "Safe to open windows for ambient natural home air circulation." else "Keep windows closed and run HEPA air purification systems."
                )

                AdvisoryItem(
                    icon = Icons.Rounded.Shield,
                    title = "Sensitive Demographics",
                    detail = if (aqi.level in listOf(AqiLevel.GOOD, AqiLevel.MODERATE)) "No elevated respiratory symptoms expected for asthma or allergy patients." else "Carry rescue inhalers; minimize prolonged roadside and industrial exposure."
                )
            }

            // Detailed Pollutants Definition
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "POLLUTANT CONCENTRATION SENSORS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                PollutantRow("PM2.5 (Fine Particulate)", "${String.format("%.1f", aqi.pm25)} µg/m³", "Particles < 2.5µm from combustion and vehicle exhaust that penetrate deep into the lungs.")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                PollutantRow("PM10 (Inhalable Particles)", "${String.format("%.1f", aqi.pm10)} µg/m³", "Dust, pollen, and mold spores suspended in ambient air.")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                PollutantRow("O₃ (Ground-level Ozone)", "${String.format("%.1f", aqi.o3)} µg/m³", "Secondary photochemical pollutant formed by sunlight reacting with hydrocarbons.")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                PollutantRow("NO₂ (Nitrogen Dioxide)", "${String.format("%.1f", aqi.no2)} µg/m³", "Combustion byproduct from power plants and automotive engines.")
            }
        }
    }
}

@Composable
private fun AdvisoryItem(icon: ImageVector, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AermosSkyBlue,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PollutantRow(name: String, value: String, explanation: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AermosAtmosphereTeal
            )
        }
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
