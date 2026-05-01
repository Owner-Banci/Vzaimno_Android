package com.vzaimno.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Soft white + turquoise palette used by the map surface.
private val Turquoise = Color(0xFF5ECFCB)
private val TurquoiseDark = Color(0xFF28AAA5)
private val TurquoiseContainer = Color(0xFFD8F6F3)
private val TurquoiseSoft = Color(0xFFEAF8F6)
private val TurquoiseMuted = Color(0xFFBFE9E5)
private val Milk = Color(0xFFF7FAF9)
private val Ink = Color(0xFF1C1C1E)
private val Mist = Color(0xFFEAF1EF)
private val Charcoal = Color(0xFF111315)
private val Slate = Color(0xFF6B7280)

// ===== Dark mockup tokens =====
val Background = Color(0xFF0D1C21)
val Surface = Color(0xFF13262B)
val SurfaceVariant = Color(0xFF173136)
val SurfaceAlt = Color(0xFF102126)

val Outline = Color(0xFF274A4D)
val OutlineVariant = Color(0xFF263C42)

val Primary = Color(0xFF54B8B6)
val PrimaryBright = Color(0xFF62CCC9)
val PrimaryPressed = Color(0xFF57C1BC)
val PrimaryDark = Color(0xFF3CA6A3)

val TextPrimary = Color(0xFFEDEEF0)
val TextSecondary = Color(0xFF969FAB)
val TextTertiary = Color(0xFF6C7985)

val ChipBackground = Color(0xFF263C42)
val NavBarBackground = Color(0xFF102126)

val Success = PrimaryBright

internal val VzaimnoLightColorScheme = lightColorScheme(
    primary = TurquoiseDark,
    onPrimary = Color.White,
    primaryContainer = TurquoiseContainer,
    onPrimaryContainer = Ink,
    secondary = Turquoise,
    onSecondary = Color.White,
    secondaryContainer = TurquoiseSoft,
    onSecondaryContainer = Ink,
    tertiary = Turquoise,
    onTertiary = Color.White,
    background = Milk,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Slate,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE1E8E6),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCFEFD),
    surfaceContainer = Color(0xFFF4FAF9),
    surfaceContainerHigh = TurquoiseSoft,
    surfaceContainerHighest = TurquoiseMuted,
    inverseSurface = Ink,
    inverseOnSurface = Milk,
    error = Color(0xFFDC3545),
    onError = Color.White,
    errorContainer = Color(0xFFFDE8EA),
    onErrorContainer = Color(0xFF9B1B2A),
)

internal val VzaimnoDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextPrimary,
    secondary = PrimaryPressed,
    onSecondary = Background,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = PrimaryBright,
    onTertiary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceContainerLowest = Background,
    surfaceContainerLow = SurfaceAlt,
    surfaceContainer = Surface,
    surfaceContainerHigh = SurfaceVariant,
    surfaceContainerHighest = ChipBackground,
    inverseSurface = TextPrimary,
    inverseOnSurface = Background,
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFF3D1520),
    onErrorContainer = Color(0xFFFFB3BC),
)
