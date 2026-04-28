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
    primary = TurquoiseContainer,
    onPrimary = Charcoal,
    primaryContainer = TurquoiseDark,
    onPrimaryContainer = Color.White,
    secondary = TurquoiseContainer,
    onSecondary = Charcoal,
    secondaryContainer = TurquoiseDark,
    onSecondaryContainer = Color.White,
    tertiary = Turquoise,
    onTertiary = Charcoal,
    background = Color(0xFF111315),
    onBackground = Color(0xFFF5F1E8),
    surface = Color(0xFF1C1E20),
    onSurface = Color(0xFFF5F1E8),
    surfaceVariant = Color(0xFF2B3131),
    onSurfaceVariant = Color(0xFFD1D7D4),
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151),
    surfaceContainerLowest = Color(0xFF111315),
    surfaceContainerLow = Color(0xFF1C1E20),
    surfaceContainer = Color(0xFF232628),
    surfaceContainerHigh = Color(0xFF2C2F32),
    surfaceContainerHighest = Color(0xFF36393C),
    error = Color(0xFFFF6B7A),
    onError = Charcoal,
    errorContainer = Color(0xFF3D1520),
    onErrorContainer = Color(0xFFFFB3BC),
)
