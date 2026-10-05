package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosDarkSurface
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.theme.AermosSolarWarm
import com.example.ui.util.AermosHaptics
import kotlin.math.cos
import kotlin.math.sin

enum class RadarLayer(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PRECIPITATION("Precipitation", Icons.Rounded.WaterDrop),
    WIND("Wind Streams", Icons.Rounded.Air),
    TEMPERATURE("Temperature", Icons.Rounded.Thermostat),
    CLOUDS("Cloud Cover", Icons.Rounded.Cloud)
}

@Composable
fun WeatherRadarCanvas(
    centerLat: Double,
    centerLon: Double,
    cityName: String,
    windSpeedKmh: Double,
    windDirectionDeg: Int,
    temperatureC: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeLayer by remember { mutableStateOf(RadarLayer.PRECIPITATION) }
    var isPlaying by remember { mutableStateOf(true) }
    var timelineProgress by remember { mutableFloatStateOf(0.7f) }

    // Map Pan and Zoom State
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Continuous timeline animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTime")
    val sweepPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sweep"
    )

    // Rotating physical radar sweep arm (360 degrees in 3.5s)
    val radarArmAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarArm"
    )

    val currentPhase = if (isPlaying) sweepPhase else timelineProgress

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF070C14))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                    panOffset += pan
                }
            }
    ) {
        // Map & Radar Drawing Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f + panOffset.x, h * 0.5f + panOffset.y)

            // 1. Technical geographic cartography grid & range rings
            drawCartographicBase(center, zoomScale, w, h)

            // 2. Render selected active radar layer
            when (activeLayer) {
                RadarLayer.PRECIPITATION -> drawPrecipitationRadar(center, zoomScale, currentPhase, w, h)
                RadarLayer.WIND -> drawWindParticleField(center, zoomScale, currentPhase, windDirectionDeg, windSpeedKmh, w, h)
                RadarLayer.TEMPERATURE -> drawTemperatureHeatmap(center, zoomScale, temperatureC, w, h)
                RadarLayer.CLOUDS -> drawCloudLayer(center, zoomScale, currentPhase, w, h)
            }

            // 3. Phosphor sweeping radar beam
            drawRadarSweepBeam(center, zoomScale, radarArmAngle, w, h)

            // 4. User / Station marker at center
            drawLocationMarker(center, cityName)
        }

        // DeBroglie Radar HUD Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0B1422).copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Text(
                text = "DOPPLER 4D • DEBROGLIE CORE",
                style = MaterialTheme.typography.labelSmall,
                color = AermosSkyBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                fontSize = 10.sp
            )
        }

        // Top Layer Selection Bar
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AermosDarkSurface.copy(alpha = 0.90f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RadarLayer.values().forEach { layer ->
                val selected = activeLayer == layer
                FilterChip(
                    selected = selected,
                    onClick = {
                        AermosHaptics.click(context)
                        activeLayer = layer
                    },
                    label = {
                        Text(
                            text = layer.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )
            }
        }

        // Map Control Floating Buttons (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    AermosHaptics.tick(context)
                    zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AermosDarkSurface.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Zoom In", tint = Color.White)
            }
            IconButton(
                onClick = {
                    AermosHaptics.tick(context)
                    zoomScale = (zoomScale * 0.8f).coerceAtLeast(0.6f)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AermosDarkSurface.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Zoom Out", tint = Color.White)
            }
            IconButton(
                onClick = {
                    AermosHaptics.heavyClick(context)
                    panOffset = Offset.Zero
                    zoomScale = 1.0f
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AermosDarkSurface.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = "Recenter", tint = AermosSkyBlue)
            }
        }

        // Bottom Timeline & Scrubber
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(18.dp),
            color = AermosDarkSurface.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = {
                        AermosHaptics.click(context)
                        isPlaying = !isPlaying
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = AermosSkyBlue
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "-2h Doppler Sweep",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isPlaying) "Streaming loop" else "Scrubbing frame",
                            style = MaterialTheme.typography.labelSmall,
                            color = AermosSkyBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "+1h Nowcast",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = currentPhase,
                        onValueChange = {
                            isPlaying = false
                            timelineProgress = it
                            AermosHaptics.tick(context)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AermosSkyBlue,
                            activeTrackColor = AermosSkyBlue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCartographicBase(center: Offset, scale: Float, w: Float, h: Float) {
    // Subtle cartographic grid lines
    val gridSize = 55f * scale
    val startX = (center.x % gridSize) - gridSize
    val startY = (center.y % gridSize) - gridSize

    var x = startX
    while (x < w + gridSize) {
        drawLine(
            color = Color.White.copy(alpha = 0.04f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += gridSize
    }

    var y = startY
    while (y < h + gridSize) {
        drawLine(
            color = Color.White.copy(alpha = 0.04f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += gridSize
    }

    // Range rings from center (50km, 100km, 150km, 250km)
    for (r in listOf(75f, 150f, 240f, 360f)) {
        drawCircle(
            color = AermosSkyBlue.copy(alpha = 0.08f),
            radius = r * scale,
            center = center,
            style = Stroke(width = 1f)
        )
    }
}

private fun DrawScope.drawRadarSweepBeam(
    center: Offset,
    scale: Float,
    angleDeg: Float,
    w: Float,
    h: Float
) {
    val sweepRadius = 380f * scale
    val rad = (angleDeg * Math.PI / 180.0)

    rotate(degrees = angleDeg, pivot = center) {
        // Trailing phosphor fade arc (45 degrees)
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    AermosAtmosphereTeal.copy(alpha = 0.18f),
                    AermosSkyBlue.copy(alpha = 0.35f)
                ),
                center = center
            ),
            startAngle = -45f,
            sweepAngle = 45f,
            useCenter = true,
            topLeft = Offset(center.x - sweepRadius, center.y - sweepRadius),
            size = Size(sweepRadius * 2, sweepRadius * 2)
        )

        // Leading edge sweep line
        drawLine(
            color = AermosSkyBlue.copy(alpha = 0.85f),
            start = center,
            end = Offset(center.x + sweepRadius, center.y),
            strokeWidth = 1.8f
        )
    }
}

private fun DrawScope.drawPrecipitationRadar(
    center: Offset,
    scale: Float,
    phase: Float,
    w: Float,
    h: Float
) {
    // Dynamic Doppler storm cells moving across the area
    val stormCells = listOf(
        Triple(-70f, -60f, 95f),
        Triple(115f, -20f, 140f),
        Triple(-30f, 125f, 85f),
        Triple(80f, 130f, 110f)
    )

    stormCells.forEachIndexed { i, (baseX, baseY, radius) ->
        val driftX = baseX + (phase * 85f * scale)
        val driftY = baseY + (phase * 35f * scale)
        val cx = center.x + driftX * scale
        val cy = center.y + driftY * scale
        val r = radius * scale

        // Multi-intensity Doppler color bands (Green -> Yellow -> Amber -> Crimson)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE11D48).copy(alpha = 0.70f), // Heavy core
                    Color(0xFFF59E0B).copy(alpha = 0.58f),
                    Color(0xFF10B981).copy(alpha = 0.42f),
                    Color(0xFF38BDF8).copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawWindParticleField(
    center: Offset,
    scale: Float,
    phase: Float,
    windDeg: Int,
    windSpeedKmh: Double,
    w: Float,
    h: Float
) {
    val angleRad = (windDeg * Math.PI / 180.0)
    val dx = cos(angleRad).toFloat()
    val dy = sin(angleRad).toFloat()

    val streamCount = 32
    for (i in 0 until streamCount) {
        val row = i / 4
        val col = i % 4
        val baseX = (col * (w / 4f) + 30f)
        val baseY = (row * (h / 8f) + 30f)

        val offsetProg = (phase + (i * 0.11f)) % 1f
        val lineLen = 40f * scale
        val startX = baseX + offsetProg * 90f * dx
        val startY = baseY + offsetProg * 90f * dy

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    AermosSkyBlue.copy(alpha = 0.75f),
                    AermosAtmosphereTeal.copy(alpha = 0.95f)
                ),
                start = Offset(startX, startY),
                end = Offset(startX + dx * lineLen, startY + dy * lineLen)
            ),
            start = Offset(startX, startY),
            end = Offset(startX + dx * lineLen, startY + dy * lineLen),
            strokeWidth = 2.8f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawTemperatureHeatmap(
    center: Offset,
    scale: Float,
    currentTemp: Double,
    w: Float,
    h: Float
) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                AermosSolarWarm.copy(alpha = 0.38f),
                Color(0xFF3B82F6).copy(alpha = 0.28f),
                Color(0xFF1E1B4B).copy(alpha = 0.16f)
            ),
            center = center,
            radius = 360f * scale
        ),
        size = size
    )
}

private fun DrawScope.drawCloudLayer(
    center: Offset,
    scale: Float,
    phase: Float,
    w: Float,
    h: Float
) {
    val cloudCenters = listOf(
        Pair(w * 0.3f + phase * 60f, h * 0.3f),
        Pair(w * 0.7f + phase * 40f, h * 0.6f),
        Pair(w * 0.2f + phase * 50f, h * 0.7f)
    )

    cloudCenters.forEach { (cx, cy) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.32f),
                    Color(0xFF94A3B8).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = 190f * scale
            ),
            radius = 190f * scale,
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawLocationMarker(center: Offset, cityName: String) {
    // Pulsing radar target ping
    drawCircle(
        color = AermosSkyBlue.copy(alpha = 0.30f),
        radius = 20.dp.toPx(),
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 6.dp.toPx(),
        center = center
    )
    drawCircle(
        color = AermosSkyBlue,
        radius = 4.dp.toPx(),
        center = center
    )
}
