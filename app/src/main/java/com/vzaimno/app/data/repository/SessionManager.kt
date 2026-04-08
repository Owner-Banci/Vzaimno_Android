package com.vzaimno.app.data.repository

import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.AccessToken
import com.vzaimno.app.core.model.SessionUser
import com.vzaimno.app.core.network.AuthTokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SessionState(
    val authEnabled: Boolean,
    val accessToken: AccessToken? = null,
    val user: SessionUser? = null,
) {
    val isAuthenticated: Boolean
        get() = !authEnabled || accessToken != null
}

@Singleton
class SessionManager @Inject constructor(
    appConfig: AppConfig,
) : AuthTokenProvider {

    private val _sessionState = MutableStateFlow(
        SessionState(authEnabled = appConfig.authEnabled),
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    override fun currentToken(): String? = _sessionState.value.accessToken?.value

    fun updateAccessToken(token: AccessToken) {
        _sessionState.value = _sessionState.value.copy(accessToken = token)
    }

    fun updateUser(user: SessionUser?) {
        _sessionState.value = _sessionState.value.copy(user = user)
    }

    fun setSession(
        token: AccessToken,
        user: SessionUser? = _sessionState.value.user,
    ) {
        _sessionState.value = _sessionState.value.copy(
            accessToken = token,
            user = user,
        )
    }

    fun clearSession() {
        _sessionState.value = _sessionState.value.copy(
            accessToken = null,
            user = null,
        )
    }
}
