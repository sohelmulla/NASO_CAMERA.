package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NasoColorScheme = darkColorScheme(
    primary = NasoGold,
    onPrimary = Color.Black,
    primaryContainer = NasoGoldContainer,
    onPrimaryContainer = OnNasoGoldContainer,
    secondary = NasoGoldVariant,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = NasoGoldLight,
    tertiary = AccentCyan,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // NASO Camera operates in a dedicated pro camera darkroom aesthetic
    MaterialTheme(
        colorScheme = NasoColorScheme,
        typography = Typography,
        content = content
    )
}
