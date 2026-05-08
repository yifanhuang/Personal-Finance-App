package com.example.finasset.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = PaperTint,
    onPrimaryContainer = Ink,
    secondary = AccentSecondary,
    onSecondary = Paper,
    secondaryContainer = PaperTint,
    onSecondaryContainer = InkTint,
    background = BackgroundLight,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = PaperTint,
    onSurfaceVariant = AccentSecondary,
    outline = InkTint.copy(alpha = 0.15f),
    error = RedUp,
    onError = Paper
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInk,
    onPrimary = DarkPaper,
    primaryContainer = InkTint,
    onPrimaryContainer = DarkInk,
    secondary = Color(0xFF95A5A6),
    onSecondary = DarkPaper,
    secondaryContainer = DarkPaperTint,
    onSecondaryContainer = DarkInkTint,
    background = BackgroundDark,
    onBackground = DarkInk,
    surface = SurfaceDark,
    onSurface = DarkInk,
    surfaceVariant = DarkPaperTint,
    onSurfaceVariant = DarkInkTint,
    outline = DarkInkTint.copy(alpha = 0.15f),
    error = RedDown,
    onError = Paper
)

@Composable
fun FinAssetTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
