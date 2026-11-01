package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// WicketX Official Dark Glassmorphism Core Palette (ZERO orange)
val MintPrimary = Color(0xFF46F0D2)            // Primary Accent: Neon Mint Cyan
val BackgroundDark = Color(0xFF131321)         // Main Background: Deep Midnight Dark
val ChampagneHighlight = Color(0xFFFBE2B4)     // Secondary Highlight: Warm Champagne Gold
val TextPrimary = Color(0xFFFFFFFF)            // Primary Text & Glass Highlights
val TextSecondary = Color(0xB3FFFFFF)          // 70% White
val TextMuted = Color(0x66FFFFFF)              // 40% White

// Translucent Glass Surface & Border Tokens
val GlassSurface = Color(0x1AFFFFFF)           // ~10% Frosted glass fill
val GlassSurfaceElevated = Color(0x26FFFFFF)   // ~15% Elevated glass fill
val GlassSurfaceSubtle = Color(0x0DFFFFFF)     // ~5% Subtle glass fill
val GlassBorder = Color(0x33FFFFFF)            // 20% Crisp glass hairline border
val GlassBorderSubtle = Color(0x1AFFFFFF)      // 10% Border
val MintGlow = Color(0x3346F0D2)               // 20% Mint ambient aura/glow
val MintBorder = Color(0x6646F0D2)             // 40% Mint active border
val ChampagneSurface = Color(0x1AFBE2B4)       // Champagne translucent glass

// Semantic Status Colors
val SuccessMint = Color(0xFF46F0D2)            // Mint for success, live matches, boundaries
val ErrorCrimson = Color(0xFFFF5252)           // Neon red for wickets and dismissals
val WarningChampagne = Color(0xFFFBE2B4)       // Warm champagne for pending/caution
val GlassSurfaceDark = Color(0x80131321)       // Translucent dark glass backdrop

// Backwards-compatible aliases mapping to the new palette to guarantee zero build breakage
val OrangePrimary = MintPrimary
val OrangeSecondary = ChampagneHighlight
val OrangeDark = MintPrimary
val SuccessGreen = MintPrimary
val ErrorRed = ErrorCrimson
val LightBackground = BackgroundDark
val LightCard = GlassSurfaceElevated
val DarkBackground = BackgroundDark
val DarkCard = GlassSurface
val DarkTextPrimary = TextPrimary
val DarkTextSecondary = TextSecondary
val PitchGreen = MintPrimary
val BorderLight = GlassBorder
val BorderDark = GlassBorder
val WarningAmber = ChampagneHighlight
