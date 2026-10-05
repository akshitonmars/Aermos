package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedUnit
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosSkyBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveWindCompass(
    windSpeedKmh: Double,
    windDirectionDeg: Int,
    windGustsKmh: Double,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val animatedDegrees by animateFloatAsState(
        targetValue = windDirectionDeg.toFloat(),
        animationSpec = tween(700),
        label = "WindNeedle"
    )

    val cardinal = when (windDirectionDeg) {
        in 338..360, in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        else -> "NW"
    }

    val beaufortScale = when {
        windSpeedKmh < 2 -> "Calm"
        windSpeedKmh < 12 -> "Light Air / Breeze"
        windSpeedKmh < 20 -> "Gentle Breeze"
        windSpeedKmh < 29 -> "Moderate Breeze"
        windSpeedKmh < 39 -> "Fresh Breeze"
        windSpeedKmh < 50 -> "Strong Breeze"
        windSpeedKmh < 62 -> "Near Gale"
        windSpeedKmh < 75 -> "Gale"
        else -> "Severe Storm"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Dial Canvas
        Canvas(modifier = Modifier.size(110.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.5f)
            val radius = w * 0.44f

            // Outer ring
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Compass ticks
            for (i in 0 until 16) {
                val angle = (i * 22.5) * (Math.PI / 180.0)
                val isCardinal = i % 4 == 0
                val tickLength = if (isCardinal) 7.dp.toPx() else 4.dp.toPx()
                val start = Offset(
                    (center.x + (radius - tickLength) * cos(angle)).toFloat(),
                    (center.y + (radius - tickLength) * sin(angle)).toFloat()
                )
                val end = Offset(
                    (center.x + radius * cos(angle)).toFloat(),
                    (center.y + radius * sin(angle)).toFloat()
                )
                drawLine(
                    color = if (isCardinal) AermosSkyBlue else Color.White.copy(alpha = 0.3f),
                    start = start,
                    end = end,
                    strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // Rotating Needle pointing towards wind direction
            rotate(degrees = animatedDegrees, pivot = center) {
                val needlePath = Path().apply {
                    moveTo(center.x, center.y - radius * 0.75f) // Arrow tip
                    lineTo(center.x + 5.dp.toPx(), center.y)
                    lineTo(center.x, center.y + 4.dp.toPx())
                    lineTo(center.x - 5.dp.toPx(), center.y)
                    close()
                }
                drawPath(
                    path = needlePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(AermosSkyBlue, AermosAtmosphereTeal)
                    )
                )

                // Tail counter-weight
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = center,
                    end = Offset(center.x, center.y + radius * 0.45f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Pivot hub
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = center
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Wind Metrics
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = speedUnit.format(windSpeedKmh),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$cardinal (${windDirectionDeg}°)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AermosSkyBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = beaufortScale,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "PEAK GUSTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = speedUnit.format(windGustsKmh),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
