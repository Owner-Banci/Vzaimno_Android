package com.vzaimno.app.feature.shell.screens

import androidx.compose.runtime.Composable
import com.vzaimno.app.feature.route.RouteHomeRoute

@Composable
fun RouteShellScreen(
    onOpenAnnouncementDetails: (String) -> Unit,
) {
    RouteHomeRoute(
        onOpenAnnouncementDetails = onOpenAnnouncementDetails,
    )
}
