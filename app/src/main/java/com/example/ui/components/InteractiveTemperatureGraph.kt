package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyPoint
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.theme.AermosSolarWarm
import com.example.ui.util.AermosHaptics

@Composable
fun InteractiveTemperatureGraph(
    hourlyPoints: List<HourlyPoint>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (hourlyPoints.isEmpty()) return

    val context = LocalContext.current
    val displayPoints = remember(hourlyPoints) { hourlyPoints.take(16) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val minTemp = remember(displayPoints) { displayPoints.minOf { it.temperatureC } - 2.0 }
    val maxTemp = remember(displayPoints) { displayPoints.maxOf { it.temperatureC } + 2.0 }
    val tempRange = (maxTemp - minTemp).coerceAtLeast(1.0)

    val activePoint = selectedIndex?.let { displayPoints.getOrNull(it) } ?: displayPoints.firstOrNull()

    // Pulse animation for scrub magnifier ring
    val infiniteTransition = rememberInfiniteTransition(label = "GraphPulse")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Halo"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Active readout header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HOURLY TEMPERATURE CURVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (selectedIndex != null) "Scrubbing ${activePoint?.timeFormatted} • ${activePoint?.condition?.label}" else "Touch and scrub timeline with haptic response",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedIndex != null) AermosSkyBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (activePoint != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tempUnit.format(activePoint.temperatureC),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Feels ${tempUnit.format(activePoint.apparentTemperatureC)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Graph Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .pointerInput(displayPoints) {
                    detectTapGestures(
                        onPress = { offset ->
                            val slotWidth = size.width / (displayPoints.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / slotWidth).toInt().coerceIn(0, displayPoints.size - 1)
                            if (selectedIndex != idx) {
                                selectedIndex = idx
                                AermosHaptics.tick(context)
                            }
                        }
                    )
                }
                .pointerInput(displayPoints) {
                    detectDragGestures(
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null },
                        onDrag = { change, _ ->
                            val slotWidth = size.width / (displayPoints.size - 1).coerceAtLeast(1)
                            val idx = (change.position.x / slotWidth).toInt().coerceIn(0, displayPoints.size - 1)
                            if (selectedIndex != idx) {
                                selectedIndex = idx
                                AermosHaptics.tick(context)
                            }
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val n = displayPoints.size
            if (n < 2) return@Canvas

            val stepX = w / (n - 1)
            val paddingY = 24f
            val graphHeight = h - paddingY * 2

            val coordinates = displayPoints.mapIndexed { i, point ->
                val x = i * stepX
                val normY = (point.temperatureC - minTemp) / tempRange
                val y = (h - paddingY) - (normY * graphHeight).toFloat()
                Offset(x, y)
            }

            // Spline path
            val path = Path().apply {
                moveTo(coordinates[0].x, coordinates[0].y)
                for (i in 0 until coordinates.size - 1) {
                    val p0 = coordinates[i]
                    val p1 = coordinates[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }

            // Fill gradient below curve
            val fillPath = Path().apply {
                addPath(path)
                lineTo(coordinates.last().x, h)
                lineTo(coordinates.first().x, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AermosSkyBlue.copy(alpha = 0.32f),
                        AermosSkyBlue.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )

            // Draw curve line
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(AermosSkyBlue, AermosSolarWarm, AermosSkyBlue)
                ),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points & scrubber magnifier
            coordinates.forEachIndexed { i, offset ->
                val isSelected = selectedIndex == i

                if (isSelected) {
                    // Scrub vertical guideline
                    drawLine(
                        color = Color.White.copy(alpha = 0.45f),
                        start = Offset(offset.x, 0f),
                        end = Offset(offset.x, h),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    // Animated glowing magnifier ring
                    drawCircle(
                        color = AermosSkyBlue.copy(alpha = 0.35f),
                        radius = haloPulse.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = AermosSkyBlue,
                        radius = 6.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = offset
                    )
                } else if (i % 3 == 0) {
                    drawCircle(
                        color = AermosSkyBlue,
                        radius = 2.5.dp.toPx(),
                        center = offset
                    )
                }
            }
        }

        // Time ticks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            displayPoints.filterIndexed { index, _ -> index % 3 == 0 }.forEach { point ->
                Text(
                    text = point.timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
