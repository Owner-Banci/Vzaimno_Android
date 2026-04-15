package com.vzaimno.app.feature.shell.screens

import androidx.compose.runtime.Composable
import com.vzaimno.app.feature.discovery.AnnouncementDiscoveryRoute

@Composable
fun MapShellScreen(
    onOpenCreate: () -> Unit = {},
) {
    AnnouncementDiscoveryRoute(
        onOpenCreate = onOpenCreate,
    )
}
