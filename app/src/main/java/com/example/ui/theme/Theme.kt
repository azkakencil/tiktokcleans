package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class LiquidGlassColors(
    val isDark: Boolean,
    val background: Color,
    val secondarySurface: Color,
    val glassFill: Color,
    val glassElevatedFill: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

private val DarkGlassTokens = LiquidGlassColors(
    isDark = true,
    background = ObsidianBackground,
    secondarySurface = SecondarySurface,
    glassFill = GlassDarkFill,
    glassElevatedFill = Color(0x1FFFFFFF),
    glassBorder = GlassDarkBorder,
    glassHighlight = GlassDarkHighlight,
    primary = ElectricViolet,
    accent = SkyCyan,
    success = EmeraldSuccess,
    danger = CrimsonDanger,
    warning = AmberWarning,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark
)

private val LightGlassTokens = LiquidGlassColors(
    isDark = false,
    background = FrostedLightBackground,
    secondarySurface = FrostedLightSecondary,
    glassFill = GlassLightFill,
    glassElevatedFill = Color(0xD9FFFFFF),
    glassBorder = GlassLightBorder,
    glassHighlight = GlassLightHighlight,
    primary = DeepViolet,
    accent = Color(0xFF0284C7),
    success = Color(0xFF16A34A),
    danger = Color(0xFFDC2626),
    warning = Color(0xFFD97706),
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight
)

val LocalGlassColors = staticCompositionLocalOf { DarkGlassTokens }

private val DarkMaterialColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    secondary = SkyCyan,
    onSecondary = Color(0xFF031525),
    tertiary = DeepViolet,
    background = ObsidianBackground,
    onBackground = TextPrimaryDark,
    surface = SecondarySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1A1D2E),
    onSurfaceVariant = TextSecondaryDark,
    outline = GlassDarkBorder,
    error = CrimsonDanger,
    onError = Color.White
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = DeepViolet,
    onPrimary = Color.White,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = ElectricViolet,
    background = FrostedLightBackground,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = FrostedLightSecondary,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun TikTokCleanerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val glassTokens = if (darkTheme) DarkGlassTokens else LightGlassTokens
    val colorScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme

    CompositionLocalProvider(LocalGlassColors provides glassTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
