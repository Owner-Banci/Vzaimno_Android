package com.vzaimno.app.core.session

import androidx.compose.runtime.Immutable
import com.vzaimno.app.core.model.SessionUser

enum class SessionAccessLevel {
    Verified,
    Cached,
}

@Immutable
sealed interface SessionState {
    @Immutable
    data object Restoring : SessionState

    @Immutable
    data object Unauthenticated : SessionState

    @Immutable
    data class Authenticated(
        val user: SessionUser?,
        val accessLevel: SessionAccessLevel,
        val isOffline: Boolean = false,
        val statusMessage: String? = null,
    ) : SessionState

    @Immutable
    data class RestoreFailed(
        val message: String,
        val isOffline: Boolean,
    ) : SessionState
}
