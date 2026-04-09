package com.vzaimno.app.feature.profile

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.feature.shell.navigation.ShellTabDestination

object ProfileDestination {
    val homeRoute: String = ShellTabDestination.Profile.rootRoute
    const val editRoute: String = "shell/tab_profile/edit"
    const val refreshResultKey: String = "profile_refresh_result"

    const val roleArgumentName: String = "role"
    const val reviewsRoutePattern: String = "shell/tab_profile/reviews?$roleArgumentName={$roleArgumentName}"

    val reviewsArguments = listOf(
        navArgument(roleArgumentName) {
            type = NavType.StringType
            defaultValue = ReviewRole.Performer.rawValue
        },
    )

    fun reviewsRoute(role: ReviewRole): String =
        "shell/tab_profile/reviews?$roleArgumentName=${role.rawValue}"

    fun roleFromArgument(rawValue: String?): ReviewRole =
        ReviewRole.fromRaw(rawValue) ?: ReviewRole.Performer
}
