package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.model.DayNightCycle
import com.example.data.model.WeatherCondition
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class AtmosphericParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var speed: Float = 0f,
    var length: Float = 0f,
    var size: Float = 2f,
    var alpha: Float = 0.5f,
    var depthLayer: Int = 0,
    var oscillationOffset: Float = 0f
)

private class SplashParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 0f,
    var maxRadius: Float = 14f,
    var alpha: Float = 0.6f,
    var active: Boolean = false
)

@Composable
fun AermosAtmosphereCanvas(
    condition: WeatherCondition,
    solarCycle: DayNightCycle,
    windSpeedKmh: Double,
    precipitationMm: Double,
    modifier: Modifier = Modifier
) {
    // Rain particles (pre-allocated across 3 depth planes)
    val rainParticles = remember {
        List(110) { i ->
            val layer = i % 3 // 0 = far, 1 = mid, 2 = near
            AtmosphericParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = when (layer) {
                    0 -> 0.45f + Random.nextFloat() * 0.25f
                    1 -> 0.75f + Random.nextFloat() * 0.35f
                    else -> 1.15f + Random.nextFloat() * 0.45f
                },
                length = when (layer) {
                    0 -> 12f + Random.nextFloat() * 10f
                    1 -> 22f + Random.nextFloat() * 16f
                    else -> 36f + Random.nextFloat() * 24f
                },
                size = when (layer) {
                    0 -> 0.9f
                    1 -> 1.4f
                    else -> 2.1f
                },
                alpha = when (layer) {
                    0 -> 0.20f + Random.nextFloat() * 0.2f
                    1 -> 0.35f + Random.nextFloat() * 0.3f
                    else -> 0.55f + Random.nextFloat() * 0.35f
                },
                depthLayer = layer
            )
        }
    }

    // Snow particles (pre-allocated with sinusoidal float)
    val snowParticles = remember {
        List(75) { i ->
            val layer = i % 3
            AtmosphericParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.08f + Random.nextFloat() * 0.22f,
                size = when (layer) {
                    0 -> 1.5f + Random.nextFloat() * 1.5f
                    1 -> 2.5f + Random.nextFloat() * 2.0f
                    else -> 4.0f + Random.nextFloat() * 3.0f
                },
                alpha = when (layer) {
                    0 -> 0.25f + Random.nextFloat() * 0.2f
                    1 -> 0.45f + Random.nextFloat() * 0.25f
                    else -> 0.70f + Random.nextFloat() * 0.25f
                },
                oscillationOffset = Random.nextFloat() * 6.283f,
                depthLayer = layer
            )
        }
    }

    // Sunlight atmospheric dust motes
    val sunMotes = remember {
        List(30) {
            AtmosphericParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.03f + Random.nextFloat() * 0.05f,
                size = 1.5f + Random.nextFloat() * 2.5f,
                alpha = 0.15f + Random.nextFloat() * 0.35f,
                oscillationOffset = Random.nextFloat() * 6.283f
            )
        }
    }

    // Stars catalog (position, magnitude, twinkle speed)
    val stars = remember {
        List(55) { i ->
            val mag = Random.nextFloat()
            Triple(Random.nextFloat(), Random.nextFloat(), mag)
        }
    }

    // High performance frame clock
    var animationTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastTime != 0L) {
                    val dt = (frameTimeNanos - lastTime) / 1_000_000_000f
                    animationTime += dt
                }
                lastTime = frameTimeNanos
            }
        }
    }

    // Thunderstorm dramatic lightning sequence
    val infiniteTransition = rememberInfiniteTransition(label = "DramaticAtmosphere")
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7500
                0f at 0
                0f at 2800
                0.90f at 2860
                0.20f at 2920
                1.00f at 2970
                0.15f at 3040
                0.50f at 3090
                0f at 3200
                0f at 7500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "LightningFlash"
    )

    // Sun flare slow rotation
    val sunRayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRayRotation"
    )

    val windFactor = (windSpeedKmh / 22.0).toFloat().coerceIn(0.2f, 2.8f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Multi-band dynamic atmospheric sky gradient
        drawRefinedSky(condition, solarCycle, lightningFlash)

        // 2. Night celestial dome: stars & lunar glow
        if (solarCycle == DayNightCycle.NIGHT || solarCycle == DayNightCycle.TWILIGHT) {
            drawCelestialNightDome(stars, animationTime, w, h, condition)
        }

        // 3. Day atmospheric radiance: crepuscular light rays & solar corona
        if (solarCycle != DayNightCycle.NIGHT) {
            drawAtmosphericSolarRadiance(solarCycle, sunRayRotation, animationTime, w, h, condition)
            if (condition == WeatherCondition.CLEAR_DAY || condition == WeatherCondition.MAINLY_CLEAR_DAY) {
                drawSunMotes(sunMotes, animationTime, w, h)
            }
        }

        // 4. Multi-level procedural cloud decks with atmospheric shading
        if (condition == WeatherCondition.PARTLY_CLOUDY_DAY || condition == WeatherCondition.PARTLY_CLOUDY_NIGHT ||
            condition == WeatherCondition.OVERCAST || condition == WeatherCondition.RAIN ||
            condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM ||
            condition == WeatherCondition.MAINLY_CLEAR_DAY || condition == WeatherCondition.MAINLY_CLEAR_NIGHT
        ) {
            drawVolumetricCloudStrata(condition, animationTime, windFactor, w, h)
        }

        // 5. Fog / Haze vapor dispersion
        if (condition == WeatherCondition.FOG) {
            drawFogStrata(animationTime, w, h)
        }

        // 6. Precipitation: Depth-layered rain streaks & ground splashes
        if (condition == WeatherCondition.RAIN || condition == WeatherCondition.HEAVY_RAIN ||
            condition == WeatherCondition.THUNDERSTORM || condition == WeatherCondition.DRIZZLE
        ) {
            drawVolumetricRain(rainParticles, animationTime, windFactor, condition, w, h)
        }

        // 7. Snow precipitation with natural aerodynamic sway
        if (condition == WeatherCondition.SNOW || condition == WeatherCondition.HEAVY_SNOW) {
            drawVolumetricSnow(snowParticles, animationTime, windFactor, w, h)
        }

        // 8. Lightning bolt branch during thunderstorm flash peak
        if (condition == WeatherCondition.THUNDERSTORM && lightningFlash > 0.4f) {
            drawLightningBranch(lightningFlash, w, h)
        }
    }
}

private fun DrawScope.drawRefinedSky(
    condition: WeatherCondition,
    solarCycle: DayNightCycle,
    lightningFlash: Float
) {
    val gradientColors = when {
        condition == WeatherCondition.THUNDERSTORM -> {
            listOf(
                Color(0xFF070B12),
                Color(0xFF0F1724),
                Color(0xFF182234),
                Color(0xFF0E1522)
            )
        }
        condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.RAIN -> {
            listOf(
                Color(0xFF090F18),
                Color(0xFF101B2B),
                Color(0xFF18263A),
                Color(0xFF131D2D)
            )
        }
        condition == WeatherCondition.OVERCAST -> {
            listOf(
                Color(0xFF0D141F),
                Color(0xFF162130),
                Color(0xFF223044),
                Color(0xFF1A2636)
            )
        }
        condition == WeatherCondition.FOG -> {
            listOf(
                Color(0xFF111822),
                Color(0xFF1B2432),
                Color(0xFF283444),
                Color(0xFF1E2835)
            )
        }
        condition == WeatherCondition.SNOW || condition == WeatherCondition.HEAVY_SNOW -> {
            listOf(
                Color(0xFF0C131D),
                Color(0xFF152233),
                Color(0xFF1F2F45),
                Color(0xFF1A2738)
            )
        }
        else -> when (solarCycle) {
            DayNightCycle.DAWN -> listOf(
                Color(0xFF0D1222),
                Color(0xFF241C38),
                Color(0xFF482645),
                Color(0xFF753A47)
            )
            DayNightCycle.MORNING -> listOf(
                Color(0xFF091A30),
                Color(0xFF103358),
                Color(0xFF1B4E7E),
                Color(0xFF2B689C)
            )
            DayNightCycle.AFTERNOON -> listOf(
                Color(0xFF0A1E38),
                Color(0xFF123966),
                Color(0xFF1B518E),
                Color(0xFF2967A8)
            )
            DayNightCycle.GOLDEN_HOUR -> listOf(
                Color(0xFF141730),
                Color(0xFF322345),
                Color(0xFF5B304C),
                Color(0xFF8B4740)
            )
            DayNightCycle.TWILIGHT -> listOf(
                Color(0xFF080D18),
                Color(0xFF12172E),
                Color(0xFF1E2245),
                Color(0xFF2B2E58)
            )
            DayNightCycle.NIGHT -> listOf(
                Color(0xFF05080F),
                Color(0xFF09101C),
                Color(0xFF0E182A),
                Color(0xFF121E33)
            )
        }
    }

    drawRect(
        brush = Brush.verticalGradient(colors = gradientColors),
        size = size
    )

    // Lightning ambient illumination wash
    if (condition == WeatherCondition.THUNDERSTORM && lightningFlash > 0.05f) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF93C5FD).copy(alpha = lightningFlash * 0.45f),
                    Color(0xFF60A5FA).copy(alpha = lightningFlash * 0.25f),
                    Color.Transparent
                )
            ),
            size = size
        )
    }
}

private fun DrawScope.drawCelestialNightDome(
    stars: List<Triple<Float, Float, Float>>,
    time: Float,
    w: Float,
    h: Float,
    condition: WeatherCondition
) {
    val cloudDimming = if (condition == WeatherCondition.OVERCAST || condition == WeatherCondition.RAIN) 0.15f else 1.0f

    // Stars with natural scintillation
    stars.forEachIndexed { i, (xR, yR, mag) ->
        val twinkleSpeed = 1.2f + (i % 5) * 0.4f
        val pulse = 0.4f + 0.6f * sin(time * twinkleSpeed + i * 1.7f)
        val alpha = (mag * pulse * cloudDimming).coerceIn(0.08f, 0.95f)

        val cx = xR * w
        val cy = yR * (h * 0.55f)
        val starRadius = if (mag > 0.8f) 1.8f else 1.1f

        drawCircle(
            color = Color(0xFFF1F5F9).copy(alpha = alpha),
            radius = starRadius,
            center = Offset(cx, cy)
        )

        // Subtle cross diffraction for brightest stars
        if (mag > 0.85f && cloudDimming > 0.5f) {
            drawLine(
                color = Color.White.copy(alpha = alpha * 0.5f),
                start = Offset(cx - 3.5f, cy),
                end = Offset(cx + 3.5f, cy),
                strokeWidth = 0.8f
            )
            drawLine(
                color = Color.White.copy(alpha = alpha * 0.5f),
                start = Offset(cx, cy - 3.5f),
                end = Offset(cx, cy + 3.5f),
                strokeWidth = 0.8f
            )
        }
    }

    // Lunar atmospheric disc & aura
    val moonX = w * 0.76f
    val moonY = h * 0.13f
    val moonRadius = 24f

    // Lunar corona
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE2E8F0).copy(alpha = 0.18f * cloudDimming),
                Color(0xFF94A3B8).copy(alpha = 0.05f * cloudDimming),
                Color.Transparent
            ),
            center = Offset(moonX, moonY),
            radius = 90f
        ),
        radius = 90f,
        center = Offset(moonX, moonY)
    )

    // Moon body with subtle crater shading
    drawCircle(
        color = Color(0xFFE2E8F0).copy(alpha = 0.85f * cloudDimming),
        radius = moonRadius,
        center = Offset(moonX, moonY)
    )
    drawCircle(
        color = Color(0xFFCBD5E1).copy(alpha = 0.40f * cloudDimming),
        radius = moonRadius * 0.35f,
        center = Offset(moonX - 5f, moonY - 3f)
    )
}

private fun DrawScope.drawAtmosphericSolarRadiance(
    cycle: DayNightCycle,
    rotation: Float,
    time: Float,
    w: Float,
    h: Float,
    condition: WeatherCondition
) {
    if (condition == WeatherCondition.OVERCAST || condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM) return

    val sunX = if (cycle == DayNightCycle.MORNING) w * 0.22f else w * 0.78f
    val sunY = h * 0.12f + sin(time * 0.15f) * 6f

    val coreColor = when (cycle) {
        DayNightCycle.GOLDEN_HOUR -> Color(0xFFF97316)
        DayNightCycle.DAWN -> Color(0xFFFB923C)
        else -> Color(0xFFFBBF24)
    }

    // Wide atmospheric daylight glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coreColor.copy(alpha = 0.22f),
                coreColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(sunX, sunY),
            radius = w * 0.85f
        ),
        radius = w * 0.85f,
        center = Offset(sunX, sunY)
    )

    // Radiant diffraction spikes
    rotate(rotation, pivot = Offset(sunX, sunY)) {
        val rayCount = 8
        for (i in 0 until rayCount) {
            val angle = (i * (360f / rayCount)) * (Math.PI / 180.0)
            val rayLen = 120f + (sin(time * 2f + i) * 15f)
            val endX = sunX + (cos(angle) * rayLen).toFloat()
            val endY = sunY + (sin(angle) * rayLen).toFloat()

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        coreColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(sunX, sunY),
                    end = Offset(endX, endY)
                ),
                start = Offset(sunX, sunY),
                end = Offset(endX, endY),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }

    // Brilliant Solar Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                coreColor.copy(alpha = 0.65f),
                Color.Transparent
            ),
            center = Offset(sunX, sunY),
            radius = 38f
        ),
        radius = 38f,
        center = Offset(sunX, sunY)
    )
}

private fun DrawScope.drawSunMotes(
    motes: List<AtmosphericParticle>,
    time: Float,
    w: Float,
    h: Float
) {
    motes.forEach { p ->
        val y = (p.y * h + time * (h * p.speed)) % (h * 0.7f)
        val sway = sin(time * 0.8f + p.oscillationOffset) * 16f
        val x = (p.x * w + sway) % w

        drawCircle(
            color = Color(0xFFFEF08A).copy(alpha = p.alpha * 0.6f),
            radius = p.size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawVolumetricCloudStrata(
    condition: WeatherCondition,
    time: Float,
    windFactor: Float,
    w: Float,
    h: Float
) {
    val cloudColor = when (condition) {
        WeatherCondition.THUNDERSTORM -> Color(0xFF090E17).copy(alpha = 0.55f)
        WeatherCondition.HEAVY_RAIN, WeatherCondition.RAIN -> Color(0xFF101926).copy(alpha = 0.45f)
        WeatherCondition.OVERCAST -> Color(0xFF172233).copy(alpha = 0.38f)
        else -> Color(0xFF2E3E55).copy(alpha = 0.22f)
    }

    // High Cirrus Deck (Slowest)
    val drift1 = ((time * 6f * windFactor) % (w + 400f)) - 200f
    drawOval(
        color = cloudColor.copy(alpha = cloudColor.alpha * 0.7f),
        topLeft = Offset(drift1, h * 0.05f),
        size = Size(w * 0.90f, 130f)
    )

    // Mid Stratocumulus Deck (Medium speed with soft puff shapes)
    val drift2 = (((time * 12f * windFactor) + w * 0.35f) % (w + 500f)) - 250f
    drawOval(
        color = cloudColor,
        topLeft = Offset(drift2, h * 0.12f),
        size = Size(w * 1.1f, 160f)
    )

    // Low Cumulus Puffs (Fastest foreground movement)
    val drift3 = (((time * 18f * windFactor) - w * 0.20f) % (w + 450f)) - 225f
    drawOval(
        color = cloudColor.copy(alpha = cloudColor.alpha * 1.25f),
        topLeft = Offset(drift3, h * 0.18f),
        size = Size(w * 0.80f, 140f)
    )
}

private fun DrawScope.drawFogStrata(time: Float, w: Float, h: Float) {
    val fogTint = Color(0xFF94A3B8).copy(alpha = 0.12f)
    for (i in 0..4) {
        val y = h * (0.12f + i * 0.14f)
        val sway = sin(time * 0.35f + i * 1.2f) * 70f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    fogTint.copy(alpha = 0.05f),
                    fogTint,
                    fogTint.copy(alpha = 0.18f),
                    fogTint,
                    Color.Transparent
                )
            ),
            topLeft = Offset(-120f + sway, y),
            size = Size(w + 240f, 110f)
        )
    }
}

private fun DrawScope.drawVolumetricRain(
    particles: List<AtmosphericParticle>,
    time: Float,
    windFactor: Float,
    condition: WeatherCondition,
    w: Float,
    h: Float
) {
    val rainColor = Color(0xFFA5C9EB)
    val angle = windFactor * 0.18f
    val dx = sin(angle)
    val dy = cos(angle)

    val count = when (condition) {
        WeatherCondition.DRIZZLE -> 45
        WeatherCondition.RAIN -> 80
        WeatherCondition.HEAVY_RAIN, WeatherCondition.THUNDERSTORM -> 110
        else -> 60
    }

    for (i in 0 until count) {
        val p = particles[i]
        val totalY = (p.y * h + time * (h * 0.85f * p.speed)) % h
        val totalX = (p.x * w + (time * windFactor * 130f) + totalY * dx) % w

        val startX = if (totalX < 0) totalX + w else totalX
        val startY = totalY
        val endX = startX + dx * p.length
        val endY = startY + dy * p.length

        drawLine(
            color = rainColor.copy(alpha = p.alpha),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = p.size,
            cap = StrokeCap.Round
        )

        // Ground splash ring at bottom (near screen edge)
        if (startY > h * 0.88f && i % 4 == 0) {
            val splashProg = ((totalY - h * 0.88f) / (h * 0.12f)).coerceIn(0f, 1f)
            val splashR = splashProg * 8f
            drawCircle(
                color = rainColor.copy(alpha = (1f - splashProg) * 0.4f),
                radius = splashR,
                center = Offset(endX, h - 10f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}

private fun DrawScope.drawVolumetricSnow(
    particles: List<AtmosphericParticle>,
    time: Float,
    windFactor: Float,
    w: Float,
    h: Float
) {
    particles.forEach { p ->
        val y = (p.y * h + time * (h * 0.18f * p.speed)) % h
        val sway = sin(time * 1.1f + p.oscillationOffset) * 22f
        val x = (p.x * w + sway + time * (windFactor * 30f)) % w
        val finalX = if (x < 0) x + w else x

        drawCircle(
            color = Color.White.copy(alpha = p.alpha),
            radius = p.size,
            center = Offset(finalX, y)
        )
    }
}

private fun DrawScope.drawLightningBranch(flash: Float, w: Float, h: Float) {
    val boltAlpha = (flash * 1.2f).coerceIn(0f, 1f)
    val startX = w * 0.48f
    val startY = h * 0.05f

    val mainBolt = Path().apply {
        moveTo(startX, startY)
        lineTo(startX - 25f, h * 0.20f)
        lineTo(startX + 18f, h * 0.35f)
        lineTo(startX - 15f, h * 0.52f)
        lineTo(startX + 10f, h * 0.68f)
        lineTo(startX - 8f, h * 0.84f)
    }

    // Outer glow
    drawPath(
        path = mainBolt,
        color = Color(0xFF60A5FA).copy(alpha = boltAlpha * 0.4f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = StrokeCap.Round)
    )

    // Inner electric core
    drawPath(
        path = mainBolt,
        color = Color.White.copy(alpha = boltAlpha),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}
