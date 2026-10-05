package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PressureTrend
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.theme.AermosSolarGold
import com.example.ui.theme.AermosSolarWarm

@Composable
fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (content != null) {
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun UvTile(
    uvIndex: Double,
    peakUvTime: String,
    modifier: Modifier = Modifier
) {
    val level = when {
        uvIndex >= 11 -> "Extreme"
        uvIndex >= 8 -> "Very High"
        uvIndex >= 6 -> "High"
        uvIndex >= 3 -> "Moderate"
        else -> "Low"
    }

    val uvColor = when {
        uvIndex >= 8 -> Color(0xFFEF4444)
        uvIndex >= 6 -> AermosSolarWarm
        uvIndex >= 3 -> AermosSolarGold
        else -> AermosAtmosphereTeal
    }

    MetricTile(
        title = "UV Index",
        value = String.format("%.1f", uvIndex),
        subtitle = "$level solar radiation. Peak near $peakUvTime.",
        modifier = modifier
    ) {
        // Horizontal spectrum bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            val w = size.width
            val h = size.height

            // Background line
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = h,
                cap = StrokeCap.Round
            )

            // Current indicator dot
            val fraction = (uvIndex / 12.0).toFloat().coerceIn(0.05f, 0.95f)
            drawCircle(
                color = uvColor,
                radius = 6.dp.toPx(),
                center = Offset(w * fraction, h / 2)
            )
        }
    }
}

@Composable
fun HumidityTile(
    humidity: Int,
    dewPointC: Double,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    val comfort = when {
        dewPointC > 21 -> "Muggy & tropical"
        dewPointC > 16 -> "Noticeably humid"
        dewPointC > 10 -> "Comfortable"
        else -> "Dry & crisp"
    }

    MetricTile(
        title = "Humidity",
        value = "$humidity%",
        subtitle = "The dew point is ${tempUnit.format(dewPointC)} right now ($comfort).",
        modifier = modifier
    )
}

@Composable
fun BarometerTile(
    pressureHpa: Double,
    trend: PressureTrend,
    pressureUnit: PressureUnit,
    modifier: Modifier = Modifier
) {
    MetricTile(
        title = "Pressure",
        value = pressureUnit.format(pressureHpa),
        subtitle = trend.label,
        modifier = modifier
    )
}

@Composable
fun VisibilityTile(
    visibilityKm: Double,
    cloudCoverPct: Int,
    modifier: Modifier = Modifier
) {
    val desc = when {
        visibilityKm >= 10.0 -> "Clear visibility"
        visibilityKm >= 5.0 -> "Moderate haze"
        else -> "Reduced visibility"
    }

    MetricTile(
        title = "Visibility",
        value = "${String.format("%.1f", visibilityKm)} km",
        subtitle = "$desc. Cloud cover at $cloudCoverPct%.",
        modifier = modifier
    )
}
