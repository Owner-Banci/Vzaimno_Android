package com.vzaimno.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// One UI 8.5 — soft, muted turquoise palette
private val Turquoise = Color(0xFF5ECFCB)
private val TurquoiseDark = Color(0xFF2BA8A4)
private val TurquoiseContainer = Color(0xFFD6F5F3)
private val Peach = Color(0xFFFFC9A6)
private val PeachDark = Color(0xFFE8A16C)
private val PeachContainer = Color(0xFFFFE8D6)
private val Milk = Color(0xFFF5F2ED)
private val Ink = Color(0xFF1C1C1E)
private val Sand = Color(0xFFE8E4DC)
private val Charcoal = Color(0xFF111315)
private val Slate = Color(0xFF6B7280)

internal val VzaimnoLightColorScheme = lightColorScheme(
    primary = TurquoiseDark,
    onPrimary = Color.White,
    primaryContainer = TurquoiseContainer,
    onPrimaryContainer = Ink,
    secondary = PeachDark,
    onSecondary = Color.White,
    secondaryContainer = PeachContainer,
    onSecondaryContainer = Ink,
    tertiary = Turquoise,
    onTertiary = Color.White,
    background = Milk,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Slate,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF9F7),
    surfaceContainer = Color(0xFFF5F3F0),
    surfaceContainerHigh = Color(0xFFEFECE8),
    surfaceContainerHighest = Color(0xFFE8E5E0),
    inverseSurface = Ink,
    inverseOnSurface = Color(0xFFF5F2ED),
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
    secondary = PeachContainer,
    onSecondary = Charcoal,
    secondaryContainer = PeachDark,
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
