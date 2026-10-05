package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AermosSolarGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SolarArcView(
    sunrise: String,
    sunset: String,
    daylightProgress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val radius = w * 0.42f
                val center = Offset(w * 0.5f, h * 0.95f)

                // Arc baseline (horizon)
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(w * 0.05f, center.y),
                    end = Offset(w * 0.95f, center.y),
                    strokeWidth = 1.dp.toPx()
                )

                // Celestial trajectory arc (semi-circle from 180 to 360 deg)
                val stroke = 3.dp.toPx()
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)

                drawArc(
                    color = Color.White.copy(alpha = 0.15f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                )

                // Solar position
                val sunAngleRad = (180.0 + (180.0 * daylightProgress)) * (Math.PI / 180.0)
                val sunX = (center.x + radius * cos(sunAngleRad)).toFloat()
                val sunY = (center.y + radius * sin(sunAngleRad)).toFloat()

                // Sun glow and core
                drawCircle(
                    color = AermosSolarGold.copy(alpha = 0.25f),
                    radius = 12.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
                drawCircle(
                    color = AermosSolarGold,
                    radius = 5.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "SUNRISE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = sunrise,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = if (daylightProgress in 0.01f..0.99f) {
                    "${Math.round((1f - daylightProgress) * 100)}% daylight remaining"
                } else if (daylightProgress >= 1f) {
                    "Sun set for today"
                } else {
                    "Before dawn"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SUNSET",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = sunset,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
