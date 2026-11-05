package com.miterangle.gauge.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class HudPalette(
    val backgroundDeep: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val primary: Color,
    val primaryDark: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val danger: Color,
    val bright: Boolean
)

val DarkHudPalette = HudPalette(
    backgroundDeep = BackgroundDeep,
    background = BackgroundBase,
    surface = SurfaceBase,
    surfaceVariant = SurfaceVariantBase,
    outline = OutlineBase,
    primary = PrimaryGreen,
    primaryDark = PrimaryGreenDark,
    accent = AccentOrange,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    danger = DangerRed,
    bright = false
)

val BrightHudPalette = HudPalette(
    backgroundDeep = BrightBackgroundDeep,
    background = BrightBackground,
    surface = BrightSurface,
    surfaceVariant = BrightSurfaceVariant,
    outline = BrightOutline,
    primary = BrightPrimary,
    primaryDark = BrightPrimaryDark,
    accent = BrightAccent,
    textPrimary = BrightTextPrimary,
    textSecondary = BrightTextSecondary,
    textMuted = BrightTextMuted,
    danger = BrightDanger,
    bright = true
)
