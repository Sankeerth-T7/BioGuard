package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// BioGuard Base Nature Palette
val ForestGreenPrimary = Color(0xFF14532D)
val ForestGreenDark = Color(0xFF0F3E22)
val EmeraldAccent = Color(0xFF10B981)
val MintLight = Color(0xFF34D399)
val LeafGreen = Color(0xFF22C55E)

// High Contrast Dark Theme Core
val DarkBg = Color(0xFF0A150F)
val DarkSurface = Color(0xFF122419)
val DarkSurfaceElevated = Color(0xFF183222)
val DarkBorder = Color(0xFF244430)
val DarkBorderSubtle = Color(0xFF1E3827)
val DarkTextPrimary = Color(0xFFF1F5F2)
val DarkTextSecondary = Color(0xFFCBDCD1)
val DarkTextMuted = Color(0xFF93B29E)

// High Contrast Light Theme Core
val LightBg = Color(0xFFF4F7F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF9FBFA)
val LightBorder = Color(0xFFD6E2D9)
val LightBorderSubtle = Color(0xFFE5EDE7)
val LightTextPrimary = Color(0xFF0F2618)
val LightTextSecondary = Color(0xFF284833)
val LightTextMuted = Color(0xFF4D6E57)

// Status & Accent Containers - Light
val LightGreenContainer = Color(0xFFDCFCE7)
val LightGreenText = Color(0xFF14532D)
val LightAmberContainer = Color(0xFFFEF3C7)
val LightAmberText = Color(0xFF78350F)
val LightRedContainer = Color(0xFFFEE2E2)
val LightRedText = Color(0xFF991B1B)
val LightNeutralContainer = Color(0xFFF0F4F1)
val LightNeutralText = Color(0xFF263D2E)

// Status & Accent Containers - Dark (Vibrant & High Contrast)
val DarkGreenContainer = Color(0xFF173824)
val DarkGreenText = Color(0xFF86EFAC)
val DarkAmberContainer = Color(0xFF382A0E)
val DarkAmberText = Color(0xFFFDE68A)
val DarkRedContainer = Color(0xFF3B1616)
val DarkRedText = Color(0xFFFCA5A5)
val DarkNeutralContainer = Color(0xFF1C3325)
val DarkNeutralText = Color(0xFFE2EBE5)

// Legacy alias definitions for backward compatibility
val SageBackground = LightBg
val SurfaceCard = LightSurface
val ForestGreenText = LightTextPrimary
val MutedText = LightTextMuted
val BorderSubtle = LightBorder
val EarthyAmber = Color(0xFFD97706)
val EarthyBrown = Color(0xFF78350F)
val AlertRed = Color(0xFFDC2626)
val AlertRedDark = Color(0xFFF87171)
val SoftGreenContainer = LightGreenContainer
val SoftAmberContainer = LightAmberContainer
val SoftRedContainer = LightRedContainer

data class BioGuardCustomColors(
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val containerGreen: Color,
    val onContainerGreen: Color,
    val containerAmber: Color,
    val onContainerAmber: Color,
    val containerRed: Color,
    val onContainerRed: Color,
    val containerNeutral: Color,
    val onContainerNeutral: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val bg: Color,
    val isDark: Boolean
)

val LightBioColors = BioGuardCustomColors(
    surfaceCard = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    surfaceBorder = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    containerGreen = LightGreenContainer,
    onContainerGreen = LightGreenText,
    containerAmber = LightAmberContainer,
    onContainerAmber = LightAmberText,
    containerRed = LightRedContainer,
    onContainerRed = LightRedText,
    containerNeutral = LightNeutralContainer,
    onContainerNeutral = LightNeutralText,
    heroGradientStart = Color(0xFF14532D),
    heroGradientEnd = Color(0xFF0F3E22),
    bg = LightBg,
    isDark = false
)

val DarkBioColors = BioGuardCustomColors(
    surfaceCard = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    surfaceBorder = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    containerGreen = DarkGreenContainer,
    onContainerGreen = DarkGreenText,
    containerAmber = DarkAmberContainer,
    onContainerAmber = DarkAmberText,
    containerRed = DarkRedContainer,
    onContainerRed = DarkRedText,
    containerNeutral = DarkNeutralContainer,
    onContainerNeutral = DarkNeutralText,
    heroGradientStart = Color(0xFF163C25),
    heroGradientEnd = Color(0xFF0B1F13),
    bg = DarkBg,
    isDark = true
)

val LocalBioGuardColors = staticCompositionLocalOf { LightBioColors }


