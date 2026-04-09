package com.vzaimno.app.feature.shell.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector
import com.vzaimno.app.R

enum class ShellTabDestination(
    val graphRoute: String,
    val rootRoute: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Map(
        graphRoute = "shell/tab_map",
        rootRoute = "shell/tab_map/home",
        labelRes = R.string.shell_tab_map,
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
    ),
    Route(
        graphRoute = "shell/tab_route",
        rootRoute = "shell/tab_route/home",
        labelRes = R.string.shell_tab_route,
        selectedIcon = Icons.Filled.Directions,
        unselectedIcon = Icons.Outlined.Directions,
    ),
    Ads(
        graphRoute = "shell/tab_ads",
        rootRoute = "ads/home",
        labelRes = R.string.shell_tab_ads,
        selectedIcon = Icons.Filled.ViewAgenda,
        unselectedIcon = Icons.Outlined.ViewAgenda,
    ),
    Chats(
        graphRoute = "shell/tab_chats",
        rootRoute = "shell/tab_chats/home",
        labelRes = R.string.shell_tab_chats,
        selectedIcon = Icons.Filled.Forum,
        unselectedIcon = Icons.Outlined.Forum,
    ),
    Profile(
        graphRoute = "shell/tab_profile",
        rootRoute = "shell/tab_profile/home",
        labelRes = R.string.shell_tab_profile,
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
    ),
    ;

    companion object {
        val topLevelRoutes: Set<String> = entries.mapTo(linkedSetOf()) { it.rootRoute }
    }
}
