package com.vzaimno.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class VzaimnoSpacing(
    val xSmall: Dp = 6.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val xLarge: Dp = 20.dp,
    val xxLarge: Dp = 24.dp,
    val xxxLarge: Dp = 32.dp,
)

val LocalVzaimnoSpacing = staticCompositionLocalOf { VzaimnoSpacing() }
