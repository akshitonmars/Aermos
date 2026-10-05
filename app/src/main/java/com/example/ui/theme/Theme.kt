package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AermosSkyBlue,
    onPrimary = Color(0xFF041E34),
    primaryContainer = Color(0xFF0C385A),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = AermosAtmosphereTeal,
    onSecondary = Color(0xFF023630),
    secondaryContainer = Color(0xFF0C4D44),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = AermosSolarGold,
    onTertiary = Color(0xFF3B2D00),
    background = AermosDarkBackground,
    onBackground = AermosDarkTextPrimary,
    surface = AermosDarkSurface,
    onSurface = AermosDarkTextPrimary,
    surfaceVariant = AermosDarkSurfaceVariant,
    onSurfaceVariant = AermosDarkTextSecondary,
    outline = AermosDarkOutline,
    outlineVariant = AermosDarkOutlineVariant,
    surfaceContainer = AermosDarkSurfaceContainer
)

private val LightColorScheme = lightColorScheme(
    primary = AermosDeepBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = AermosLightBackground,
    onBackground = AermosLightTextPrimary,
    surface = AermosLightSurface,
    onSurface = AermosLightTextPrimary,
    surfaceVariant = AermosLightSurfaceVariant,
    onSurfaceVariant = AermosLightTextSecondary,
    outline = AermosLightOutline,
    outlineVariant = AermosLightOutlineVariant,
    surfaceContainer = AermosLightSurfaceContainer
)

@Composable
fun AermosTheme(
    darkTheme: Boolean = true, // Default to deep atmospheric dark theme as typical in premium weather instruments
    dynamicColor: Boolean = false, // Keep atmospheric palette consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = AermosTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
