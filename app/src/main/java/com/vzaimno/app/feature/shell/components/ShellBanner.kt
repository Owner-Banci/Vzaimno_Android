package com.vzaimno.app.feature.shell.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vzaimno.app.core.designsystem.theme.spacing

enum class ShellBannerTone {
    Connectivity,
    Session,
}

data class ShellBannerState(
    val text: String,
    val tone: ShellBannerTone,
)

@Composable
fun ShellBanner(
    state: ShellBannerState,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (state.tone) {
        ShellBannerTone.Connectivity -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer

        ShellBannerTone.Session -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Icon(
                imageVector = when (state.tone) {
                    ShellBannerTone.Connectivity -> Icons.Outlined.WifiOff
                    ShellBannerTone.Session -> Icons.Outlined.CloudDone
                },
                contentDescription = null,
            )
            Text(
                text = state.text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
