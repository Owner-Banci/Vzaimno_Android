package com.vzaimno.app.feature.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.feature.auth.AuthRoute
import com.vzaimno.app.feature.shell.MainShellRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.vzaimno.app.R

@Composable
fun AppRootRoute(
    viewModel: RootViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val navController = rememberNavController()
    val targetRoute = when (state.sessionState) {
        SessionState.Restoring -> RootDestination.Splash.route
        SessionState.Unauthenticated -> RootDestination.Auth.route
        is SessionState.Authenticated -> RootDestination.Main.route
        is SessionState.RestoreFailed -> RootDestination.RestoreFailed.route
    }

    LaunchedEffect(targetRoute) {
        navController.navigateToRootDestination(targetRoute)
    }

    NavHost(
        navController = navController,
        startDestination = RootDestination.Splash.route,
    ) {
        composable(route = RootDestination.Splash.route) {
            FullscreenStatus(
                title = stringResource(R.string.root_restoring_title),
                message = stringResource(R.string.root_restoring_message),
                showProgress = true,
            )
        }

        composable(route = RootDestination.Auth.route) {
            AuthRoute()
        }

        composable(route = RootDestination.RestoreFailed.route) {
            val restoreState = state.sessionState as? SessionState.RestoreFailed
            if (restoreState != null) {
                RestoreFailedScreen(
                    state = restoreState,
                    onRetry = viewModel::retryRestore,
                    onReset = viewModel::clearSession,
                )
            } else {
                FullscreenStatus(
                    title = stringResource(R.string.root_restoring_title),
                    message = stringResource(R.string.root_restoring_message),
                    showProgress = true,
                )
            }
        }

        composable(route = RootDestination.Main.route) {
            val authenticatedState = state.sessionState as? SessionState.Authenticated
            if (authenticatedState != null) {
                MainShellRoute(
                    sessionState = authenticatedState,
                    isLoggingOut = state.isLoggingOut,
                    isOnline = state.isOnline,
                    onRetryRestore = viewModel::retryRestore,
                    onLogout = viewModel::logout,
                )
            } else {
                FullscreenStatus(
                    title = stringResource(R.string.root_restoring_title),
                    message = stringResource(R.string.root_restoring_message),
                    showProgress = true,
                )
            }
        }
    }
}

@Composable
private fun FullscreenStatus(
    title: String,
    message: String,
    showProgress: Boolean,
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(MaterialTheme.spacing.xxxLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showProgress) {
                CircularProgressIndicator()
            }
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.large),
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RestoreFailedScreen(
    state: SessionState.RestoreFailed,
    onRetry: () -> Unit,
    onReset: () -> Unit,
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(MaterialTheme.spacing.xxxLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (state.isOffline) {
                    stringResource(R.string.root_restore_failed_offline_title)
                } else {
                    stringResource(R.string.root_restore_failed_title)
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                text = state.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.xxLarge),
                onClick = onRetry,
            ) {
                Text(text = stringResource(R.string.root_retry))
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.medium),
                onClick = onReset,
            ) {
                Text(text = stringResource(R.string.root_clear_session))
            }
        }
    }
}

private sealed class RootDestination(val route: String) {
    data object Splash : RootDestination("root/splash")
    data object Auth : RootDestination("root/auth")
    data object RestoreFailed : RootDestination("root/restore_failed")
    data object Main : RootDestination("root/main")
}

private fun NavHostController.navigateToRootDestination(targetRoute: String) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute == targetRoute || (currentRoute == null && targetRoute == RootDestination.Splash.route)) {
        return
    }

    navigate(targetRoute) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
