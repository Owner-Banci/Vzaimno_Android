package com.vzaimno.app.feature.ads

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

object AdsDestination {
    const val homeRoute: String = "ads/home"
    const val createRoute: String = "ads/create"

    const val announcementIdArgument: String = "announcementId"
    const val prefillAnnouncementIdArgument: String = "prefillAnnouncementId"
    const val detailsRoutePattern: String = "ads/details/{$announcementIdArgument}"
    const val createRoutePattern: String = "ads/create?$prefillAnnouncementIdArgument={$prefillAnnouncementIdArgument}"
    const val refreshResultKey: String = "ads_refresh_result"
    const val postCreateFilterResultKey: String = "ads_post_create_filter_result"
    const val postCreateMessageResultKey: String = "ads_post_create_message_result"

    val detailsArguments = listOf(
        navArgument(announcementIdArgument) {
            type = NavType.StringType
        },
    )
    val createArguments = listOf(
        navArgument(prefillAnnouncementIdArgument) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
    )

    fun detailsRoute(announcementId: String): String = "ads/details/$announcementId"

    fun createRoute(prefillAnnouncementId: String? = null): String = when {
        prefillAnnouncementId.isNullOrBlank() -> createRoute
        else -> "$createRoute?$prefillAnnouncementIdArgument=${Uri.encode(prefillAnnouncementId)}"
    }
}
