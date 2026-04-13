package com.vzaimno.app.feature.chats

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

object ChatsDestination {
    const val homeRoute = "shell/tab_chats/home"
    const val supportRoute = "shell/tab_chats/support"

    const val threadIdArgument = "threadId"
    const val threadKindArgument = "threadKind"

    const val threadRoutePattern = "shell/tab_chats/thread/{$threadIdArgument}?$threadKindArgument={$threadKindArgument}"

    val threadArguments = listOf(
        navArgument(threadIdArgument) {
            type = NavType.StringType
        },
        navArgument(threadKindArgument) {
            type = NavType.StringType
            defaultValue = ChatConversationKind.Direct.rawValue
        },
    )

    fun threadRoute(
        threadId: String,
        threadKind: String,
    ): String = buildString {
        append("shell/tab_chats/thread/")
        append(Uri.encode(threadId))
        append("?")
        append(threadKindArgument)
        append("=")
        append(Uri.encode(threadKind))
    }
}
