package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KrunalQuantColorScheme = darkColorScheme(
    primary = AlphaEmerald,
    onPrimary = Color(0xFF00210E),
    primaryContainer = AlphaEmeraldDim,
    onPrimaryContainer = AlphaEmerald,
    secondary = SovereignGold,
    onSecondary = Color(0xFF261C00),
    secondaryContainer = SovereignGoldDim,
    onSecondaryContainer = SovereignGold,
    tertiary = ElectricCyan,
    onTertiary = Color(0xFF00262C),
    tertiaryContainer = ElectricCyanDim,
    onTertiaryContainer = ElectricCyan,
    error = BearishCrimson,
    onError = Color.White,
    errorContainer = BearishCrimsonDim,
    onErrorContainer = BearishCrimson,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = SlateCardElevated
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KrunalQuantColorScheme,
        typography = Typography,
        content = content
    )
}
