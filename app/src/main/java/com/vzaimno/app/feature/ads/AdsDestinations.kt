package com.vzaimno.app.feature.ads

import androidx.navigation.NavType
import androidx.navigation.navArgument

object AdsDestination {
    const val homeRoute: String = "ads/home"

    const val announcementIdArgument: String = "announcementId"
    const val detailsRoutePattern: String = "ads/details/{$announcementIdArgument}"
    const val refreshResultKey: String = "ads_refresh_result"

    val detailsArguments = listOf(
        navArgument(announcementIdArgument) {
            type = NavType.StringType
        },
    )

    fun detailsRoute(announcementId: String): String = "ads/details/$announcementId"
}
