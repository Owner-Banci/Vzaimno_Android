package com.vzaimno.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun VzaimnoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        VzaimnoDarkColorScheme
    } else {
        VzaimnoLightColorScheme
    }

    CompositionLocalProvider(
        LocalVzaimnoSpacing provides VzaimnoSpacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VzaimnoTypography,
            content = content,
        )
    }
}

val MaterialTheme.spacing: VzaimnoSpacing
    @Composable
    get() = LocalVzaimnoSpacing.current
