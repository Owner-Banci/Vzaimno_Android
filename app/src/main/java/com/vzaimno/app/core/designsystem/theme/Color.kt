package com.vzaimno.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Turquoise = Color(0xFF3CC8C4)
private val TurquoiseDark = Color(0xFF1F8F8B)
private val TurquoiseContainer = Color(0xFFC4F1EF)
private val Peach = Color(0xFFFFC9A6)
private val PeachDark = Color(0xFFE8A16C)
private val PeachContainer = Color(0xFFFFE1CD)
private val Milk = Color(0xFFF7F3E9)
private val Ink = Color(0xFF1B1B1B)
private val Sand = Color(0xFFEAE3D4)
private val Charcoal = Color(0xFF111315)
private val Slate = Color(0xFF4E5A5A)

internal val VzaimnoLightColorScheme = lightColorScheme(
    primary = TurquoiseDark,
    onPrimary = Color.White,
    primaryContainer = TurquoiseContainer,
    onPrimaryContainer = Ink,
    secondary = PeachDark,
    onSecondary = Ink,
    secondaryContainer = PeachContainer,
    onSecondaryContainer = Ink,
    tertiary = Turquoise,
    onTertiary = Ink,
    background = Milk,
    onBackground = Ink,
    surface = Color(0xFFFFFCF7),
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Slate,
    outline = Color(0xFF7A7A72),
)

internal val VzaimnoDarkColorScheme = darkColorScheme(
    primary = TurquoiseContainer,
    onPrimary = Charcoal,
    primaryContainer = TurquoiseDark,
    onPrimaryContainer = Color.White,
    secondary = PeachContainer,
    onSecondary = Charcoal,
    secondaryContainer = PeachDark,
    onSecondaryContainer = Color.White,
    tertiary = Turquoise,
    onTertiary = Charcoal,
    background = Color(0xFF111315),
    onBackground = Color(0xFFF5F1E8),
    surface = Color(0xFF171A1B),
    onSurface = Color(0xFFF5F1E8),
    surfaceVariant = Color(0xFF2B3131),
    onSurfaceVariant = Color(0xFFD1D7D4),
    outline = Color(0xFF8E9592),
)
