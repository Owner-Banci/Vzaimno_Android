package com.vzaimno.app.feature.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.feature.ads.AdsDestination
import com.vzaimno.app.feature.ads.AnnouncementDetailsRoute
import com.vzaimno.app.feature.ads.MyAnnouncementsRoute
import com.vzaimno.app.feature.ads.AdsFilterBucket
import com.vzaimno.app.feature.ads.create.AnnouncementCreateRoute
import com.vzaimno.app.feature.shell.components.ShellBanner
import com.vzaimno.app.feature.shell.components.ShellBannerState
import com.vzaimno.app.feature.shell.components.ShellBannerTone
import com.vzaimno.app.feature.shell.navigation.LocalShellBottomBarVisibilityController
import com.vzaimno.app.feature.shell.navigation.ShellTabDestination
import com.vzaimno.app.feature.shell.navigation.rememberShellBottomBarVisibilityController
import com.vzaimno.app.feature.profile.ProfileDestination
import com.vzaimno.app.feature.profile.ProfileEditRoute
import com.vzaimno.app.feature.profile.ProfileReviewsRoute
import com.vzaimno.app.feature.shell.screens.ChatPreviewScreen
import com.vzaimno.app.feature.shell.screens.ChatsShellScreen
import com.vzaimno.app.feature.shell.screens.MapShellScreen
import com.vzaimno.app.feature.shell.screens.ProfileShellScreen
import com.vzaimno.app.feature.shell.screens.RouteShellScreen

@Composable
fun MainShellRoute(
    sessionState: SessionState.Authenticated,
    isOnline: Boolean,
    isLoggingOut: Boolean,
    onRetryRestore: () -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val bottomBarVisibilityController = rememberShellBottomBarVisibilityController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val offlineBanner = stringResource(R.string.shell_banner_offline)
    val bannerState = remember(sessionState, isOnline, offlineBanner) {
        when {
            !isOnline -> ShellBannerState(
                text = offlineBanner,
                tone = ShellBannerTone.Connectivity,
            )

            !sessionState.statusMessage.isNullOrBlank() -> ShellBannerState(
                text = sessionState.statusMessage,
                tone = ShellBannerTone.Session,
            )

            else -> null
        }
    }
    val showBottomBar = currentDestination.isTopLevelDestination() && !bottomBarVisibilityController.isHidden

    CompositionLocalProvider(
        LocalShellBottomBarVisibilityController provides bottomBarVisibilityController,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(visible = showBottomBar) {
                    ShellBottomBar(
                        currentDestination = currentDestination,
                        onTabSelected = { tab ->
                            navController.navigate(tab.graphRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = ShellTabDestination.Map.graphRoute,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = if (bannerState != null) 76.dp else 0.dp),
                ) {
                    navigation(
                        route = ShellTabDestination.Map.graphRoute,
                        startDestination = ShellTabDestination.Map.rootRoute,
                    ) {
                        composable(route = ShellTabDestination.Map.rootRoute) {
                            MapShellScreen()
                        }
                    }

                    navigation(
                        route = ShellTabDestination.Route.graphRoute,
                        startDestination = ShellTabDestination.Route.rootRoute,
                    ) {
                        composable(route = ShellTabDestination.Route.rootRoute) {
                            RouteShellScreen()
                        }
                    }

                    navigation(
                        route = ShellTabDestination.Ads.graphRoute,
                        startDestination = AdsDestination.homeRoute,
                    ) {
                        composable(route = AdsDestination.homeRoute) { backStackEntry ->
                            val refreshSignal by backStackEntry.savedStateHandle
                                .getStateFlow(AdsDestination.refreshResultKey, false)
                                .collectAsStateWithLifecycle()
                            val postCreateFilter by backStackEntry.savedStateHandle
                                .getStateFlow<String?>(AdsDestination.postCreateFilterResultKey, null)
                                .collectAsStateWithLifecycle()
                            val postCreateMessage by backStackEntry.savedStateHandle
                                .getStateFlow<String?>(AdsDestination.postCreateMessageResultKey, null)
                                .collectAsStateWithLifecycle()
                            MyAnnouncementsRoute(
                                onOpenDetails = { announcementId ->
                                    navController.navigate(AdsDestination.detailsRoute(announcementId))
                                },
                                onOpenCreate = {
                                    navController.navigate(AdsDestination.createRoute())
                                },
                                refreshSignal = refreshSignal,
                                postCreateFilter = postCreateFilter.toAdsFilterBucketOrNull(),
                                postCreateMessage = postCreateMessage,
                                onPostCreateHandled = {
                                    backStackEntry.savedStateHandle[AdsDestination.postCreateFilterResultKey] = null as String?
                                    backStackEntry.savedStateHandle[AdsDestination.postCreateMessageResultKey] = null as String?
                                },
                                onRefreshSignalHandled = {
                                    backStackEntry.savedStateHandle[AdsDestination.refreshResultKey] = false
                                },
                            )
                        }
                        composable(
                            route = AdsDestination.detailsRoutePattern,
                            arguments = AdsDestination.detailsArguments,
                        ) {
                            AnnouncementDetailsRoute(
                                onBack = navController::navigateUp,
                                onRequestParentRefresh = {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(AdsDestination.refreshResultKey, true)
                                },
                                onCreateAgain = { announcement ->
                                    navController.navigate(AdsDestination.createRoute(announcement.id))
                                },
                            )
                        }
                        composable(
                            route = AdsDestination.createRoutePattern,
                            arguments = AdsDestination.createArguments,
                        ) {
                            AnnouncementCreateRoute(
                                onBack = navController::navigateUp,
                                onSubmitted = { filter, message ->
                                    val homeEntry = navController.getBackStackEntry(AdsDestination.homeRoute)
                                    homeEntry.savedStateHandle[AdsDestination.refreshResultKey] = true
                                    homeEntry.savedStateHandle[AdsDestination.postCreateFilterResultKey] = filter.name
                                    homeEntry.savedStateHandle[AdsDestination.postCreateMessageResultKey] = message
                                    navController.popBackStack(AdsDestination.homeRoute, inclusive = false)
                                },
                            )
                        }
                    }

                    navigation(
                        route = ShellTabDestination.Chats.graphRoute,
                        startDestination = ShellTabDestination.Chats.rootRoute,
                    ) {
                        composable(route = ShellTabDestination.Chats.rootRoute) {
                            ChatsShellScreen(
                                onOpenPreview = {
                                    navController.navigate(ShellSecondaryDestination.ChatPreview.route)
                                },
                            )
                        }
                        composable(route = ShellSecondaryDestination.ChatPreview.route) {
                            ChatPreviewScreen(
                                onBack = navController::navigateUp,
                            )
                        }
                    }

                    navigation(
                        route = ShellTabDestination.Profile.graphRoute,
                        startDestination = ShellTabDestination.Profile.rootRoute,
                    ) {
                        composable(route = ProfileDestination.homeRoute) { backStackEntry ->
                            val refreshSignal by backStackEntry.savedStateHandle
                                .getStateFlow(ProfileDestination.refreshResultKey, false)
                                .collectAsStateWithLifecycle()
                            ProfileShellScreen(
                                sessionState = sessionState,
                                isOnline = isOnline,
                                onRetryRestore = onRetryRestore,
                                onOpenEdit = {
                                    navController.navigate(ProfileDestination.editRoute)
                                },
                                onOpenReviews = { role ->
                                    navController.navigate(ProfileDestination.reviewsRoute(role))
                                },
                                refreshSignal = refreshSignal,
                                onRefreshSignalHandled = {
                                    backStackEntry.savedStateHandle[ProfileDestination.refreshResultKey] = false
                                },
                            )
                        }
                        composable(route = ProfileDestination.editRoute) {
                            ProfileEditRoute(
                                onBack = navController::navigateUp,
                                onSaved = {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(ProfileDestination.refreshResultKey, true)
                                    navController.navigateUp()
                                },
                                onLogout = onLogout,
                                isLoggingOut = isLoggingOut,
                            )
                        }
                        composable(
                            route = ProfileDestination.reviewsRoutePattern,
                            arguments = ProfileDestination.reviewsArguments,
                        ) {
                            ProfileReviewsRoute(
                                onBack = navController::navigateUp,
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.small),
                    visible = bannerState != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                ) {
                    bannerState?.let { state ->
                        ShellBanner(
                            state = state,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (ShellTabDestination) -> Unit,
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        NavigationBar(
            modifier = Modifier.navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            ShellTabDestination.entries.forEach { tab ->
                val selected = currentDestination?.hierarchy?.any { destination ->
                    destination.route == tab.graphRoute
                } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = stringResource(tab.labelRes),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.tertiary,
                        selectedTextColor = MaterialTheme.colorScheme.tertiary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

private fun NavDestination?.isTopLevelDestination(): Boolean =
    this?.route in ShellTabDestination.topLevelRoutes

private sealed class ShellSecondaryDestination(val route: String) {
    data object ChatPreview : ShellSecondaryDestination("shell/tab_chats/thread_preview")
}

private fun String?.toAdsFilterBucketOrNull(): AdsFilterBucket? = AdsFilterBucket.entries.firstOrNull { bucket ->
    bucket.name == this
}
