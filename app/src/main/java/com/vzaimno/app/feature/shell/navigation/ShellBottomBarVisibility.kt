package com.vzaimno.app.feature.shell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Stable
class ShellBottomBarVisibilityController {
    private val hiddenReasons = mutableStateMapOf<String, Boolean>()

    val isHidden: Boolean
        get() = hiddenReasons.isNotEmpty()

    fun setHidden(hidden: Boolean, reason: String) {
        if (hidden) {
            hiddenReasons[reason] = true
        } else {
            hiddenReasons.remove(reason)
        }
    }
}

val LocalShellBottomBarVisibilityController =
    compositionLocalOf<ShellBottomBarVisibilityController?> { null }

@Composable
fun rememberShellBottomBarVisibilityController(): ShellBottomBarVisibilityController =
    remember { ShellBottomBarVisibilityController() }

@Composable
fun HideShellBottomBarEffect(reason: String) {
    val controller by rememberUpdatedState(
        LocalShellBottomBarVisibilityController.current,
    )

    DisposableEffect(controller, reason) {
        controller?.setHidden(hidden = true, reason = reason)
        onDispose {
            controller?.setHidden(hidden = false, reason = reason)
        }
    }
}
