package com.miterangle.gauge.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val LocalHudPalette = staticCompositionLocalOf { DarkHudPalette }

object HudTheme {
    val colors: HudPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalHudPalette.current
}

private fun darkScheme(palette: HudPalette) = darkColorScheme(
    primary = palette.primary,
    onPrimary = palette.backgroundDeep,
    primaryContainer = palette.primaryDark,
    onPrimaryContainer = palette.textPrimary,
    secondary = palette.accent,
    onSecondary = palette.backgroundDeep,
    tertiary = palette.primaryDark,
    onTertiary = palette.textPrimary,
    background = palette.background,
    onBackground = palette.textPrimary,
    surface = palette.surface,
    onSurface = palette.textPrimary,
    surfaceVariant = palette.surfaceVariant,
    onSurfaceVariant = palette.textSecondary,
    outline = palette.outline,
    outlineVariant = palette.outline,
    error = palette.danger,
    onError = palette.textPrimary
)

private fun brightScheme(palette: HudPalette) = lightColorScheme(
    primary = palette.primary,
    onPrimary = BrightSurface,
    primaryContainer = palette.primaryDark,
    onPrimaryContainer = BrightSurface,
    secondary = palette.accent,
    onSecondary = BrightSurface,
    tertiary = palette.primaryDark,
    onTertiary = BrightSurface,
    background = palette.background,
    onBackground = palette.textPrimary,
    surface = palette.surface,
    onSurface = palette.textPrimary,
    surfaceVariant = palette.surfaceVariant,
    onSurfaceVariant = palette.textSecondary,
    outline = palette.outline,
    outlineVariant = palette.outline,
    error = palette.danger,
    onError = BrightSurface
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val bright by ThemeMode.bright.collectAsStateWithLifecycle()
    val palette = if (bright) BrightHudPalette else DarkHudPalette
    CompositionLocalProvider(LocalHudPalette provides palette) {
        MaterialTheme(
            colorScheme = if (bright) brightScheme(palette) else darkScheme(palette),
            typography = HudTypography,
            content = content
        )
    }
}
