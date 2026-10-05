package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosSkyBlue
import kotlin.math.sin

@Composable
fun AermosWordmark(
    modifier: Modifier = Modifier,
    showEmblem: Boolean = true,
    showSubtitle: Boolean = true,
    fontSize: Int = 18,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showEmblem) {
            AermosEmblem(size = (fontSize * 1.5f).dp)
        }
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "AERMOS",
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.SansSerif,
                lineHeight = (fontSize + 2).sp
            )
            if (showSubtitle) {
                Text(
                    text = "BY DEBROGLIE",
                    color = AermosSkyBlue.copy(alpha = 0.85f),
                    fontSize = (fontSize * 0.44f).coerceAtLeast(8f).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.8.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
fun AermosEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EmblemHarmonic")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Outer atmospheric isobar ring with gradient
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    AermosSkyBlue,
                    AermosAtmosphereTeal,
                    Color(0xFF818CF8),
                    AermosSkyBlue
                )
            ),
            radius = w * 0.44f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = w * 0.08f)
        )

        // DeBroglie Harmonic Atmospheric Wave (Sinusoidal flow)
        val wavePath = Path()
        val startX = w * 0.22f
        val endX = w * 0.78f
        val centerY = h * 0.50f
        val amplitude = h * 0.12f

        for (i in 0..20) {
            val progress = i / 20f
            val x = startX + progress * (endX - startX)
            val phase = if (animated) wavePhase else 0f
            val y = centerY + sin(progress * 6.283f + phase).toFloat() * amplitude

            if (i == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        drawPath(
            path = wavePath,
            color = Color.White,
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Wave-particle nodal singularity (DeBroglie quantum/atmosphere node)
        val nodeX = w * 0.50f
        val nodeY = centerY + (if (animated) sin(wavePhase).toFloat() * amplitude else 0f)
        drawCircle(
            color = AermosSkyBlue,
            radius = w * 0.09f,
            center = Offset(nodeX, nodeY)
        )
    }
}
