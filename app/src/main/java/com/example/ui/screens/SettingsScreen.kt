package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PressureUnit
import com.example.data.model.SpeedUnit
import com.example.data.model.TemperatureUnit
import com.example.ui.components.AermosWordmark
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.util.AermosHaptics
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
                text = "SYSTEM CONFIGURATION",
                style = MaterialTheme.typography.labelSmall,
                color = AermosSkyBlue,
                letterSpacing = 1.sp
            )
        }

        // Units Section
        SettingsCard(title = "MEASUREMENT UNITS") {
            // Temperature Unit
            SettingsRow(
                title = "Temperature Scale",
                subtitle = "Active: ${settings.tempUnit.name.lowercase().replaceFirstChar { it.uppercase() }}"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UnitToggleChip(
                        label = "°C",
                        selected = settings.tempUnit == TemperatureUnit.CELSIUS,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setTempUnit(TemperatureUnit.CELSIUS)
                        }
                    )
                    UnitToggleChip(
                        label = "°F",
                        selected = settings.tempUnit == TemperatureUnit.FAHRENHEIT,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setTempUnit(TemperatureUnit.FAHRENHEIT)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Wind Speed Unit
            SettingsRow(
                title = "Wind & Gust Velocity",
                subtitle = "Active: ${settings.speedUnit.label}"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UnitToggleChip(
                        label = "km/h",
                        selected = settings.speedUnit == SpeedUnit.KMH,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setSpeedUnit(SpeedUnit.KMH)
                        }
                    )
                    UnitToggleChip(
                        label = "mph",
                        selected = settings.speedUnit == SpeedUnit.MPH,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setSpeedUnit(SpeedUnit.MPH)
                        }
                    )
                    UnitToggleChip(
                        label = "m/s",
                        selected = settings.speedUnit == SpeedUnit.MS,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setSpeedUnit(SpeedUnit.MS)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Barometric Pressure Unit
            SettingsRow(
                title = "Atmospheric Pressure",
                subtitle = "Active: ${settings.pressureUnit.label}"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UnitToggleChip(
                        label = "hPa",
                        selected = settings.pressureUnit == PressureUnit.HPA,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setPressureUnit(PressureUnit.HPA)
                        }
                    )
                    UnitToggleChip(
                        label = "inHg",
                        selected = settings.pressureUnit == PressureUnit.INHG,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.setPressureUnit(PressureUnit.INHG)
                        }
                    )
                }
            }
        }

        // Notification & Intelligence Alerts
        SettingsCard(title = "ATMOSPHERIC INTELLIGENCE ALERTS") {
            ToggleSettingRow(
                title = "Severe Weather Radar Warnings",
                subtitle = "Notifies on approaching squall lines, hail, and high wind fronts.",
                checked = settings.severeWeatherAlerts,
                onCheckedChange = {
                    AermosHaptics.click(context)
                    viewModel.toggleAlert("severe")
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ToggleSettingRow(
                title = "Precipitation Nowcast (Rain Starting)",
                subtitle = "Advance advisory 15 minutes before rainfall onset in your micro-climate.",
                checked = settings.rainAlerts,
                onCheckedChange = {
                    AermosHaptics.click(context)
                    viewModel.toggleAlert("rain")
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ToggleSettingRow(
                title = "Morning Atmospheric Synthesis",
                subtitle = "Daily comfort indices, UV warnings, and diurnal temperature outlook.",
                checked = settings.morningSummary,
                onCheckedChange = {
                    AermosHaptics.click(context)
                    viewModel.toggleAlert("morning")
                }
            )
        }

        // Data Sources & Precision Engineering
        SettingsCard(title = "TELEMETRY ENGINE & SOURCE") {
            InfoSettingRow(
                title = "Company & Engineering Entity",
                value = "DeBroglie Atmospheric Dynamics Lab"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoSettingRow(
                title = "Atmospheric Modeling Provider",
                value = "Open-Meteo API / DWD ICON / ECMWF High Resolution"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoSettingRow(
                title = "Air Composition Network",
                value = "Copernicus Atmosphere Monitoring Service (CAMS)"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoSettingRow(
                title = "Refresh Polling Cycle",
                value = "Every 30 minutes with live on-demand sync"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoSettingRow(
                title = "Offline Persistence Cache",
                value = "SQLite Room Database (Encrypted Local Storage)"
            )
        }

        // About Aermos By DeBroglie
        SettingsCard(title = "ABOUT AERMOS BY DEBROGLIE") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        AermosHaptics.click(context)
                        showAboutDialog = true
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aermos by DeBroglie",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 1.0.0 (Production Release) • Tap for details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("Aermos By DeBroglie", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Aermos is a flagship atmospheric intelligence product by DeBroglie.\n\nBuilt upon wave-particle atmospheric mechanics, Aermos synthesizes high-precision live meteorology telemetry from Open-Meteo and Copernicus CAMS into actionable environmental intelligence with 60fps environmental particle rendering and tactile haptic response.\n\nCrafted with Kotlin, Jetpack Compose, and Material Design 3 by DeBroglie.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AermosHaptics.click(context)
                    showAboutDialog = false
                }) {
                    Text("Close", color = AermosSkyBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        action()
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AermosSkyBlue
            )
        )
    }
}

@Composable
private fun InfoSettingRow(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun UnitToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AermosSkyBlue else Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color(0xFF041E34) else MaterialTheme.colorScheme.onSurface
        )
    }
}
