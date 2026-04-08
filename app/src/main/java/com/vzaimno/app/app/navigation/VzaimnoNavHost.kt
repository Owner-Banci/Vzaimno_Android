package com.vzaimno.app.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vzaimno.app.feature.bootstrap.BootstrapRoute

private const val BOOTSTRAP_ROUTE = "bootstrap"

@Composable
fun VzaimnoNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BOOTSTRAP_ROUTE,
    ) {
        composable(BOOTSTRAP_ROUTE) {
            BootstrapRoute()
        }
    }
}
