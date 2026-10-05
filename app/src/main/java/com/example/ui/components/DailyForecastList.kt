package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPoint
import com.example.data.model.SpeedUnit
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.AermosDarkSurfaceVariant
import com.example.ui.theme.AermosPrecipitationBlue
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.theme.AermosSolarWarm
import com.example.ui.util.AermosHaptics

@Composable
fun DailyForecastList(
    dailyPoints: List<DailyPoint>,
    tempUnit: TemperatureUnit,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    val globalMin = remember(dailyPoints) { dailyPoints.minOfOrNull { it.minTempC } ?: 0.0 }
    val globalMax = remember(dailyPoints) { dailyPoints.maxOfOrNull { it.maxTempC } ?: 30.0 }
    val globalRange = (globalMax - globalMin).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "10-DAY OUTLOOK",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        dailyPoints.forEachIndexed { index, day ->
            DailyRowItem(
                day = day,
                globalMin = globalMin,
                globalRange = globalRange,
                tempUnit = tempUnit,
                speedUnit = speedUnit,
                isExpanded = expandedIndex == index,
                onClick = {
                    AermosHaptics.click(context)
                    expandedIndex = if (expandedIndex == index) null else index
                }
            )

            if (index < dailyPoints.size - 1) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun DailyRowItem(
    day: DailyPoint,
    globalMin: Double,
    globalRange: Double,
    tempUnit: TemperatureUnit,
    speedUnit: SpeedUnit,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day name & date
            Column(modifier = Modifier.width(68.dp)) {
                Text(
                    text = day.dayOfWeek,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = day.formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Weather icon & rain %
            Row(
                modifier = Modifier.width(62.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AermosWeatherIcon(
                    condition = day.condition,
                    size = 24.dp,
                    animated = false
                )
                if (day.precipitationProbMax > 20) {
                    Text(
                        text = "${day.precipitationProbMax}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = AermosPrecipitationBlue,
                        fontSize = 11.sp
                    )
                }
            }

            // Low Temp
            Text(
                text = tempUnit.format(day.minTempC),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp)
            )

            // Normalized Range Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                val startFraction = ((day.minTempC - globalMin) / globalRange).toFloat().coerceIn(0f, 1f)
                val endFraction = ((day.maxTempC - globalMin) / globalRange).toFloat().coerceIn(0f, 1f)
                val widthFraction = (endFraction - startFraction).coerceAtLeast(0.08f)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(widthFraction)
                        .offset(x = (startFraction * 140).dp) // approximate visual scale
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AermosSkyBlue, AermosSolarWarm)
                            )
                        )
                )
            }

            // High Temp
            Text(
                text = tempUnit.format(day.maxTempC),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .width(42.dp)
                    .padding(start = 8.dp)
            )
        }

        // Expanded detail drawer
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(spring()) + expandVertically(spring()),
            exit = fadeOut(spring()) + shrinkVertically(spring())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AermosDarkSurfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailText(label = "CONDITION", value = day.condition.label)
                    DetailText(label = "MAX WIND", value = speedUnit.format(day.windSpeedMaxKmh))
                    DetailText(label = "MAX UV", value = String.format("%.1f", day.uvIndexMax))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailText(label = "SUNRISE", value = day.sunrise)
                    DetailText(label = "SUNSET", value = day.sunset)
                    DetailText(label = "PRECIP SUM", value = "${String.format("%.1f", day.precipitationSumMm)} mm")
                }
            }
        }
    }
}

@Composable
private fun DetailText(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
