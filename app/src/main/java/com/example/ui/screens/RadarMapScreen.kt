package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AermosWordmark
import com.example.ui.components.WeatherRadarCanvas
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun RadarMapScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeLoc by viewModel.activeLocation.collectAsState()

    val currentData = (uiState as? WeatherUiState.Success)?.data

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 88.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            AermosWordmark(fontSize = 17, showSubtitle = true)
            Text(
                text = "INTERACTIVE DOPPLER RADAR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }

        WeatherRadarCanvas(
            centerLat = activeLoc.latitude,
            centerLon = activeLoc.longitude,
            cityName = activeLoc.name,
            windSpeedKmh = currentData?.windSpeedKmh ?: 14.0,
            windDirectionDeg = currentData?.windDirectionDeg ?: 230,
            temperatureC = currentData?.currentTempC ?: 20.0,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
