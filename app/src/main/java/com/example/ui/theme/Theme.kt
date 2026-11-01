package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Immutable holder for custom Dark Glassmorphism design tokens accessible via MaterialTheme.glassColors
 */
@Immutable
data class GlassmorphismExtendedColors(
    val background: Color = BackgroundDark,         // #131321
    val primaryAccent: Color = MintPrimary,         // #46F0D2
    val secondaryHighlight: Color = ChampagneHighlight, // #FBE2B4
    val textPrimary: Color = TextPrimary,           // #FFFFFF
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val glassSurface: Color = GlassSurface,
    val glassSurfaceElevated: Color = GlassSurfaceElevated,
    val glassSurfaceSubtle: Color = GlassSurfaceSubtle,
    val glassBorder: Color = GlassBorder,
    val glassBorderSubtle: Color = GlassBorderSubtle,
    val mintGlow: Color = MintGlow,
    val mintBorder: Color = MintBorder,
    val champagneSurface: Color = ChampagneSurface
)

val LocalGlassColors = staticCompositionLocalOf { GlassmorphismExtendedColors() }

/**
 * Official WicketX Material 3 Dark Glassmorphism ColorScheme
 * Core Palette:
 * - Background: #131321 (BackgroundDark)
 * - Primary Accent: #46F0D2 (MintPrimary)
 * - Secondary Highlight: #FBE2B4 (ChampagneHighlight)
 * - Primary Text & Highlights: #FFFFFF (TextPrimary)
 */
val DarkGlassmorphismColorScheme: ColorScheme = darkColorScheme(
    primary = MintPrimary,                          // #46F0D2
    onPrimary = BackgroundDark,                     // #131321
    primaryContainer = MintGlow,                    // Translucent #46F0D2 container
    onPrimaryContainer = MintPrimary,               // #46F0D2
    inversePrimary = BackgroundDark,
    secondary = ChampagneHighlight,                 // #FBE2B4
    onSecondary = BackgroundDark,                   // #131321
    secondaryContainer = ChampagneSurface,          // Translucent #FBE2B4 container
    onSecondaryContainer = ChampagneHighlight,      // #FBE2B4
    tertiary = MintPrimary,                         // #46F0D2
    onTertiary = BackgroundDark,                    // #131321
    tertiaryContainer = GlassSurfaceElevated,
    onTertiaryContainer = TextPrimary,              // #FFFFFF
    background = BackgroundDark,                    // #131321
    onBackground = TextPrimary,                     // #FFFFFF
    surface = BackgroundDark,                       // #131321 base surface
    onSurface = TextPrimary,                        // #FFFFFF
    surfaceVariant = GlassSurfaceElevated,          // Frosted glass card variant
    onSurfaceVariant = TextSecondary,               // 70% #FFFFFF
    surfaceTint = MintPrimary,                      // #46F0D2 elevation tint
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDark,
    error = ErrorCrimson,
    onError = TextPrimary,
    errorContainer = Color(0x33FF5252),
    onErrorContainer = ErrorCrimson,
    outline = GlassBorder,                          // 20% #FFFFFF specular border
    outlineVariant = GlassBorderSubtle,             // 10% #FFFFFF subtle border
    scrim = Color(0xCC131321),                      // Deep midnight glass scrim
    surfaceBright = Color(0xFF1F1F35),
    surfaceDim = BackgroundDark,
    surfaceContainerLowest = Color(0xFF0E0E1A),
    surfaceContainerLow = Color(0xFF161626),
    surfaceContainer = Color(0xFF1A1A2E),
    surfaceContainerHigh = Color(0xFF22223A),
    surfaceContainerHighest = Color(0xFF2A2A46)
)

@Composable
fun WicketXTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalGlassColors provides GlassmorphismExtendedColors()) {
        MaterialTheme(
            colorScheme = DarkGlassmorphismColorScheme,
            typography = Typography,
            shapes = WicketXShapes,
            content = content
        )
    }
}

val MaterialTheme.glassColors: GlassmorphismExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalGlassColors.current
