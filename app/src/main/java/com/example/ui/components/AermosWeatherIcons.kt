package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.WeatherCondition
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AermosWeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherIconAnim")

    // Slow subtle rotation for sun rays (12 seconds)
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRot"
    )

    // Gentle cloud float oscillation
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudDrift"
    )

    // Rain drop translation (800ms loop)
    val rainDropProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainFall"
    )

    // Lightning pulse
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                0f at 0
                0f at 2200
                1f at 2280
                0.2f at 2350
                1f at 2400
                0f at 2520
                0f at 3200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "BoltPulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (condition) {
            WeatherCondition.CLEAR_DAY -> {
                drawSun(w, h, if (animated) sunRotation else 0f)
            }
            WeatherCondition.CLEAR_NIGHT -> {
                drawMoon(w, h)
            }
            WeatherCondition.MAINLY_CLEAR_DAY, WeatherCondition.PARTLY_CLOUDY_DAY -> {
                drawSunBehindCloud(w, h, if (animated) sunRotation else 0f, if (animated) cloudOffset else 0f)
            }
            WeatherCondition.MAINLY_CLEAR_NIGHT, WeatherCondition.PARTLY_CLOUDY_NIGHT -> {
                drawMoonBehindCloud(w, h, if (animated) cloudOffset else 0f)
            }
            WeatherCondition.OVERCAST -> {
                drawOvercastClouds(w, h, if (animated) cloudOffset else 0f)
            }
            WeatherCondition.FOG -> {
                drawFogIcon(w, h, if (animated) cloudOffset else 0f)
            }
            WeatherCondition.DRIZZLE -> {
                drawCloudWithRain(w, h, if (animated) cloudOffset else 0f, if (animated) rainDropProgress else 0.5f, light = true)
            }
            WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> {
                drawCloudWithRain(w, h, if (animated) cloudOffset else 0f, if (animated) rainDropProgress else 0.5f, light = false)
            }
            WeatherCondition.THUNDERSTORM -> {
                drawThunderstorm(w, h, if (animated) cloudOffset else 0f, if (animated) rainDropProgress else 0.5f, if (animated) lightningAlpha else 0.8f)
            }
            WeatherCondition.SNOW, WeatherCondition.HEAVY_SNOW -> {
                drawCloudWithSnow(w, h, if (animated) cloudOffset else 0f, if (animated) rainDropProgress else 0.5f)
            }
            WeatherCondition.WINDY -> {
                drawWindStreams(w, h, if (animated) cloudOffset else 0f)
            }
        }
    }
}

private fun DrawScope.drawSun(w: Float, h: Float, rotation: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val sunRadius = w * 0.22f

    // Rays
    rotate(rotation, pivot = center) {
        val rayCount = 8
        val innerRay = w * 0.30f
        val outerRay = w * 0.42f
        for (i in 0 until rayCount) {
            val angle = (i * (360f / rayCount)) * (Math.PI / 180.0)
            val start = Offset(
                (center.x + innerRay * cos(angle)).toFloat(),
                (center.y + innerRay * sin(angle)).toFloat()
            )
            val end = Offset(
                (center.x + outerRay * cos(angle)).toFloat(),
                (center.y + outerRay * sin(angle)).toFloat()
            )
            drawLine(
                color = Color(0xFFFBBF24),
                start = start,
                end = end,
                strokeWidth = w * 0.06f,
                cap = StrokeCap.Round
            )
        }
    }

    // Core
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = sunRadius,
        center = center
    )
}

private fun DrawScope.drawMoon(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.32f

    val moonPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(center.x - r, center.y - r, center.x + r, center.y + r))
    }
    val cutoutPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(center.x - r * 0.3f, center.y - r * 1.15f, center.x + r * 1.5f, center.y + r * 0.65f))
    }
    val finalMoon = Path.combine(PathOperation.Difference, moonPath, cutoutPath)

    drawPath(finalMoon, color = Color(0xFFE2E8F0))
}

private fun DrawScope.drawSunBehindCloud(w: Float, h: Float, sunRot: Float, cloudDx: Float) {
    val sunCenter = Offset(w * 0.68f, h * 0.34f)
    rotate(sunRot, pivot = sunCenter) {
        val rayCount = 6
        for (i in 0 until rayCount) {
            val angle = (i * 60f) * (Math.PI / 180.0)
            val start = Offset(
                (sunCenter.x + w * 0.16f * cos(angle)).toFloat(),
                (sunCenter.y + w * 0.16f * sin(angle)).toFloat()
            )
            val end = Offset(
                (sunCenter.x + w * 0.26f * cos(angle)).toFloat(),
                (sunCenter.y + w * 0.26f * sin(angle)).toFloat()
            )
            drawLine(
                color = Color(0xFFFBBF24).copy(alpha = 0.85f),
                start = start,
                end = end,
                strokeWidth = w * 0.05f,
                cap = StrokeCap.Round
            )
        }
    }
    drawCircle(color = Color(0xFFF59E0B), radius = w * 0.16f, center = sunCenter)

    // Front cloud
    drawCloudShape(w, h, dx = cloudDx, dy = 0f, color = Color(0xFFCBD5E1))
}

private fun DrawScope.drawMoonBehindCloud(w: Float, h: Float, cloudDx: Float) {
    val moonCenter = Offset(w * 0.65f, h * 0.35f)
    val r = w * 0.18f
    val moonPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenter.x - r, moonCenter.y - r, moonCenter.x + r, moonCenter.y + r))
    }
    val cutout = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenter.x - r * 0.2f, moonCenter.y - r * 1.1f, moonCenter.x + r * 1.6f, moonCenter.y + r * 0.7f))
    }
    val moon = Path.combine(PathOperation.Difference, moonPath, cutout)
    drawPath(moon, color = Color(0xFFE2E8F0))

    drawCloudShape(w, h, dx = cloudDx, dy = 0f, color = Color(0xFF94A3B8))
}

private fun DrawScope.drawOvercastClouds(w: Float, h: Float, cloudDx: Float) {
    // Back cloud
    drawCloudShape(w * 0.85f, h * 0.85f, dx = -cloudDx + w * 0.15f, dy = -h * 0.12f, color = Color(0xFF64748B))
    // Front cloud
    drawCloudShape(w, h, dx = cloudDx, dy = h * 0.05f, color = Color(0xFFCBD5E1))
}

private fun DrawScope.drawCloudShape(w: Float, h: Float, dx: Float, dy: Float, color: Color) {
    val cx = w * 0.5f + dx
    val cy = h * 0.55f + dy

    // Base pill
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - w * 0.36f, cy - h * 0.12f),
        size = Size(w * 0.72f, h * 0.32f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.16f, h * 0.16f)
    )
    // Left lobe
    drawCircle(
        color = color,
        radius = w * 0.18f,
        center = Offset(cx - w * 0.12f, cy - h * 0.12f)
    )
    // Right lobe
    drawCircle(
        color = color,
        radius = w * 0.13f,
        center = Offset(cx + w * 0.16f, cy - h * 0.06f)
    )
}

private fun DrawScope.drawCloudWithRain(w: Float, h: Float, cloudDx: Float, dropProg: Float, light: Boolean) {
    drawCloudShape(w, h * 0.9f, dx = cloudDx, dy = -h * 0.08f, color = Color(0xFF94A3B8))

    val dropColor = Color(0xFF60A5FA)
    val dropCount = if (light) 2 else 3
    val spacing = w * 0.22f
    val startX = w * 0.38f + cloudDx * 0.5f

    for (i in 0 until dropCount) {
        val x = startX + (i - (dropCount - 1) / 2f) * spacing
        val dropOffset = (dropProg + i * 0.33f) % 1.0f
        val y1 = h * 0.65f + dropOffset * (h * 0.28f)
        val y2 = y1 + h * 0.10f
        drawLine(
            color = dropColor.copy(alpha = 1f - dropOffset * 0.4f),
            start = Offset(x, y1),
            end = Offset(x - w * 0.03f, y2),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawThunderstorm(w: Float, h: Float, cloudDx: Float, dropProg: Float, lightningAlpha: Float) {
    drawCloudShape(w, h * 0.9f, dx = cloudDx, dy = -h * 0.08f, color = Color(0xFF475569))

    // Lightning bolt
    if (lightningAlpha > 0.05f) {
        val boltPath = Path().apply {
            moveTo(w * 0.52f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.68f)
            lineTo(w * 0.52f, h * 0.68f)
            lineTo(w * 0.40f, h * 0.94f)
            lineTo(w * 0.48f, h * 0.74f)
            lineTo(w * 0.42f, h * 0.74f)
            close()
        }
        drawPath(boltPath, color = Color(0xFFFBBF24).copy(alpha = lightningAlpha))
    }

    // Drops
    val dropColor = Color(0xFF93C5FD)
    for (i in 0..1) {
        val x = w * (0.35f + i * 0.32f)
        val dropOffset = (dropProg + i * 0.5f) % 1.0f
        val y1 = h * 0.65f + dropOffset * (h * 0.26f)
        drawLine(
            color = dropColor,
            start = Offset(x, y1),
            end = Offset(x - w * 0.04f, y1 + h * 0.09f),
            strokeWidth = w * 0.04f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloudWithSnow(w: Float, h: Float, cloudDx: Float, snowProg: Float) {
    drawCloudShape(w, h * 0.9f, dx = cloudDx, dy = -h * 0.08f, color = Color(0xFFCBD5E1))

    val flakeColor = Color(0xFFE2E8F0)
    for (i in 0..2) {
        val x = w * (0.33f + i * 0.17f)
        val offset = (snowProg + i * 0.33f) % 1f
        val y = h * 0.68f + offset * (h * 0.25f)
        drawCircle(color = flakeColor, radius = w * 0.035f, center = Offset(x, y))
    }
}

private fun DrawScope.drawFogIcon(w: Float, h: Float, cloudDx: Float) {
    drawCloudShape(w, h * 0.85f, dx = cloudDx, dy = -h * 0.12f, color = Color(0xFF94A3B8))

    val fogColor = Color(0xFFCBD5E1)
    drawLine(
        color = fogColor,
        start = Offset(w * 0.20f, h * 0.72f),
        end = Offset(w * 0.80f, h * 0.72f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = fogColor,
        start = Offset(w * 0.28f, h * 0.84f),
        end = Offset(w * 0.72f, h * 0.84f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawWindStreams(w: Float, h: Float, drift: Float) {
    val windColor = Color(0xFF38BDF8)
    val p1 = Path().apply {
        moveTo(w * 0.18f + drift, h * 0.38f)
        lineTo(w * 0.65f + drift, h * 0.38f)
        cubicTo(w * 0.80f + drift, h * 0.38f, w * 0.80f + drift, h * 0.22f, w * 0.65f + drift, h * 0.22f)
        cubicTo(w * 0.55f + drift, h * 0.22f, w * 0.55f + drift, h * 0.32f, w * 0.65f + drift, h * 0.32f)
    }
    drawPath(p1, color = windColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.055f, cap = StrokeCap.Round))

    val p2 = Path().apply {
        moveTo(w * 0.12f + drift * 0.6f, h * 0.58f)
        lineTo(w * 0.75f + drift * 0.6f, h * 0.58f)
        cubicTo(w * 0.88f + drift * 0.6f, h * 0.58f, w * 0.88f + drift * 0.6f, h * 0.72f, w * 0.75f + drift * 0.6f, h * 0.72f)
        cubicTo(w * 0.65f + drift * 0.6f, h * 0.72f, w * 0.65f + drift * 0.6f, h * 0.64f, w * 0.75f + drift * 0.6f, h * 0.64f)
    }
    drawPath(p2, color = windColor.copy(alpha = 0.8f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.05f, cap = StrokeCap.Round))
}
