package com.vzaimno.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {

    val screen: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val card: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val elevated: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val bottomBar: Color
        get() = NavBarBackground

    val chip: Color
        get() = ChipBackground

    val textPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val textSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val textMuted: Color
        get() = TextTertiary

    val accent: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val accentBright: Color
        get() = PrimaryBright

    val border: Color
        @Composable get() = MaterialTheme.colorScheme.outline

    val divider: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
}
