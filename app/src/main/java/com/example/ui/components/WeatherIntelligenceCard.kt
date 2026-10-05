package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtmosphericIntelligence
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.theme.AermosSolarWarm

@Composable
fun WeatherIntelligenceCard(
    intelligence: AtmosphericIntelligence,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = AermosSkyBlue.copy(alpha = 0.2f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Intelligence Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Aermos Intelligence",
                    tint = AermosSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "AERMOS ATMOSPHERIC INTELLIGENCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = AermosSkyBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Outdoor score pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AermosAtmosphereTeal.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${intelligence.outdoorScore}% Readiness",
                    style = MaterialTheme.typography.labelSmall,
                    color = AermosAtmosphereTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Natural Language Summary
        Text(
            text = intelligence.naturalLanguageSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )

        // Best Window Highlight Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "RECOMMENDED WINDOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = AermosSolarWarm,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = intelligence.bestWindowRecommendation,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Activity Suitability Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityScoreItem(
                title = "Running",
                score = intelligence.runningScore,
                icon = Icons.Rounded.DirectionsRun,
                modifier = Modifier.weight(1f)
            )
            ActivityScoreItem(
                title = "Cycling",
                score = intelligence.cyclingScore,
                icon = Icons.Rounded.PedalBike,
                modifier = Modifier.weight(1f)
            )
            ActivityScoreItem(
                title = "Walking",
                score = intelligence.walkingScore,
                icon = Icons.Rounded.WbSunny,
                modifier = Modifier.weight(1f)
            )
        }

        // Clothing & Barometric Comfort
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "CLOTHING GUIDANCE: ${intelligence.clothingAdvice}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Text(
                text = "BAROMETRIC STATUS: ${intelligence.barometricComfortSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActivityScoreItem(
    title: String,
    score: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> AermosAtmosphereTeal
        score >= 60 -> AermosSkyBlue
        else -> AermosSolarWarm
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$score%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
