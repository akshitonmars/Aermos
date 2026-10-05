package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyPoint
import com.example.data.model.SpeedUnit
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.AermosDarkSurfaceVariant
import com.example.ui.theme.AermosPrecipitationBlue
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.util.AermosHaptics

@Composable
fun HourlyForecastRow(
    hourlyPoints: List<HourlyPoint>,
    tempUnit: TemperatureUnit,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedIndex by remember { mutableStateOf<Int?>(0) }
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hourlyPoints.take(24).forEachIndexed { index, point ->
                val isSelected = expandedIndex == index
                HourlyItemPill(
                    point = point,
                    isSelected = isSelected,
                    tempUnit = tempUnit,
                    onClick = {
                        AermosHaptics.click(context)
                        expandedIndex = if (isSelected) null else index
                    }
                )
            }
        }

        // Animated expansion detail drawer for the tapped hour
        AnimatedVisibility(
            visible = expandedIndex != null && expandedIndex in hourlyPoints.indices,
            enter = fadeIn(spring()) + expandVertically(spring()),
            exit = fadeOut(spring()) + shrinkVertically(spring())
        ) {
            val selectedPoint = expandedIndex?.let { hourlyPoints.getOrNull(it) }
            if (selectedPoint != null) {
                HourlyDetailDrawer(
                    point = selectedPoint,
                    tempUnit = tempUnit,
                    speedUnit = speedUnit,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun HourlyItemPill(
    point: HourlyPoint,
    isSelected: Boolean,
    tempUnit: TemperatureUnit,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = point.timeFormatted,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )

        AermosWeatherIcon(
            condition = point.condition,
            size = 28.dp,
            animated = isSelected
        )

        Text(
            text = tempUnit.format(point.temperatureC),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )

        if (point.precipitationProb > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${point.precipitationProb}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = AermosPrecipitationBlue,
                    fontSize = 10.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HourlyDetailDrawer(
    point: HourlyPoint,
    tempUnit: TemperatureUnit,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AermosDarkSurfaceVariant)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricItem(label = "FEELS LIKE", value = tempUnit.format(point.apparentTemperatureC))
        MetricItem(label = "WIND", value = speedUnit.format(point.windSpeedKmh))
        MetricItem(label = "HUMIDITY", value = "${point.humidity}%")
        MetricItem(
            label = "PRECIP",
            value = "${String.format("%.1f", point.precipitationMm)} mm",
            accentColor = if (point.precipitationMm > 0) AermosPrecipitationBlue else null
        )
        MetricItem(label = "DEW PT", value = tempUnit.format(point.dewPointC))
    }
}

@Composable
private fun MetricItem(label: String, value: String, accentColor: Color? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = accentColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
