package com.vzaimno.app.data.repository

import com.vzaimno.app.core.common.IoDispatcher
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.AccessToken
import com.vzaimno.app.core.model.LoginCredentials
import com.vzaimno.app.core.model.RegisterCredentials
import com.vzaimno.app.core.model.SessionUser
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.core.network.AuthTokenProvider
import com.vzaimno.app.core.security.SecureSessionStorage
import com.vzaimno.app.core.session.SessionAccessLevel
import com.vzaimno.app.core.session.SessionMessageFormatter
import com.vzaimno.app.core.session.SessionState
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class ActiveSession(
    val authEnabled: Boolean,
    val accessToken: AccessToken? = null,
    val user: SessionUser? = null,
) {
    val isAuthenticated: Boolean
        get() = !authEnabled || accessToken != null
}

@Singleton
class SessionManager @Inject constructor(
    private val appConfig: AppConfig,
    private val authRepositoryProvider: Provider<AuthRepository>,
    private val deviceRepositoryProvider: Provider<DeviceRepository>,
    private val secureSessionStorage: SecureSessionStorage,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthTokenProvider {

    private val mutationMutex = Mutex()
    private val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var hasRestoredSession = false

    private val _activeSession = MutableStateFlow(
        ActiveSession(authEnabled = appConfig.authEnabled),
    )
    val activeSession: StateFlow<ActiveSession> = _activeSession.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionState>(
        if (appConfig.authEnabled) {
            SessionState.Restoring
        } else {
            SessionState.Authenticated(
                user = DEV_USER,
                accessLevel = SessionAccessLevel.Verified,
                statusMessage = "Авторизация отключена для этой сборки.",
            )
        },
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    override fun currentToken(): String? = _activeSession.value.accessToken?.value

    suspend fun restoreSession(force: Boolean = false) {
        mutationMutex.withLock {
            if (hasRestoredSession && !force) return
            hasRestoredSession = true

            if (!appConfig.authEnabled) {
                setAuthBypassSession()
                return
            }

            _sessionState.value = SessionState.Restoring

            val token = withContext(ioDispatcher) { secureSessionStorage.readToken() }
            if (token == null) {
                clearSessionInternal()
                _sessionState.value = SessionState.Unauthenticated
                return
            }

            val cachedUser = withContext(ioDispatcher) { secureSessionStorage.readUser() }
            updateLocalSession(token = token, user = cachedUser)

            when (val result = authRepositoryProvider.get().fetchMe()) {
                is ApiResult.Success -> {
                    persistAuthenticatedSession(token = token, user = result.value)
                    emitAuthenticated(
                        user = result.value,
                        accessLevel = SessionAccessLevel.Verified,
                    )
                    registerCurrentDeviceInBackground()
                }

                is ApiResult.Failure -> {
                    if (result.error.invalidatesSession) {
                        clearSessionInternal()
                        _sessionState.value = SessionState.Unauthenticated
                    } else if (cachedUser != null) {
                        emitAuthenticated(
                            user = cachedUser,
                            accessLevel = SessionAccessLevel.Cached,
                            isOffline = result.error.kind.isOfflineLike(),
                            statusMessage = SessionMessageFormatter.cachedSessionBanner(result.error),
                        )
                    } else {
                        _sessionState.value = SessionState.RestoreFailed(
                            message = SessionMessageFormatter.restoreError(result.error),
                            isOffline = result.error.kind.isOfflineLike(),
                        )
                    }
                }
            }
        }
    }

    suspend fun login(credentials: LoginCredentials): String? = authenticate(
        mode = AuthMode.Login,
        block = { authRepositoryProvider.get().login(credentials) },
    )

    suspend fun register(credentials: RegisterCredentials): String? = authenticate(
        mode = AuthMode.Register,
        block = { authRepositoryProvider.get().register(credentials) },
    )

    suspend fun logout() {
        mutationMutex.withLock {
            if (!appConfig.authEnabled) {
                clearSessionInternal()
                _sessionState.value = SessionState.Unauthenticated
                return
            }

            val hasToken = _activeSession.value.accessToken != null
            if (hasToken) {
                when (val result = deviceRepositoryProvider.get().unregisterCurrentDevice()) {
                    is ApiResult.Failure -> {
                        // Device cleanup is best-effort and should never block local logout.
                    }

                    is ApiResult.Success -> Unit
                }
            }

            clearSessionInternal()
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    fun updateUser(user: SessionUser?) {
        if (!appConfig.authEnabled) return

        _activeSession.value = _activeSession.value.copy(user = user)
        when {
            user == null -> secureSessionStorage.clearUser()
            else -> secureSessionStorage.writeUser(user)
        }

        val currentState = _sessionState.value
        if (currentState is SessionState.Authenticated) {
            _sessionState.value = currentState.copy(user = user)
        }
    }

    fun clearSession() {
        if (!appConfig.authEnabled) {
            setAuthBypassSession()
            return
        }

        clearSessionInternal()
        _sessionState.value = SessionState.Unauthenticated
    }

    private suspend fun authenticate(
        mode: AuthMode,
        block: suspend () -> ApiResult<AccessToken>,
    ): String? = mutationMutex.withLock {
        if (!appConfig.authEnabled) {
            setAuthBypassSession()
            return null
        }

        val tokenResult = block()
        val token = when (tokenResult) {
            is ApiResult.Failure -> {
                return when (mode) {
                    AuthMode.Login -> SessionMessageFormatter.loginError(tokenResult.error)
                    AuthMode.Register -> SessionMessageFormatter.registerError(tokenResult.error)
                }
            }

            is ApiResult.Success -> tokenResult.value
        }

        persistPendingToken(token)

        return when (val meResult = authRepositoryProvider.get().fetchMe()) {
            is ApiResult.Success -> {
                persistAuthenticatedSession(token = token, user = meResult.value)
                emitAuthenticated(
                    user = meResult.value,
                    accessLevel = SessionAccessLevel.Verified,
                )
                registerCurrentDeviceInBackground()
                null
            }

            is ApiResult.Failure -> {
                if (meResult.error.invalidatesSession) {
                    clearSessionInternal()
                    _sessionState.value = SessionState.Unauthenticated
                    when (mode) {
                        AuthMode.Login -> SessionMessageFormatter.loginError(meResult.error)
                        AuthMode.Register -> SessionMessageFormatter.registerError(meResult.error)
                    }
                } else {
                    emitAuthenticated(
                        user = null,
                        accessLevel = SessionAccessLevel.Cached,
                        isOffline = meResult.error.kind.isOfflineLike(),
                        statusMessage = SessionMessageFormatter.postAuthBanner(meResult.error),
                    )
                    null
                }
            }
        }
    }

    private fun persistPendingToken(token: AccessToken) {
        _activeSession.value = _activeSession.value.copy(
            accessToken = token,
            user = null,
        )
        secureSessionStorage.writeToken(token)
        secureSessionStorage.clearUser()
    }

    private fun persistAuthenticatedSession(token: AccessToken, user: SessionUser) {
        _activeSession.value = _activeSession.value.copy(
            accessToken = token,
            user = user,
        )
        secureSessionStorage.writeToken(token)
        secureSessionStorage.writeUser(user)
    }

    private fun updateLocalSession(token: AccessToken, user: SessionUser?) {
        _activeSession.value = _activeSession.value.copy(
            accessToken = token,
            user = user,
        )
    }

    private fun emitAuthenticated(
        user: SessionUser?,
        accessLevel: SessionAccessLevel,
        isOffline: Boolean = false,
        statusMessage: String? = null,
    ) {
        _sessionState.value = SessionState.Authenticated(
            user = user,
            accessLevel = accessLevel,
            isOffline = isOffline,
            statusMessage = statusMessage,
        )
    }

    private fun registerCurrentDeviceInBackground() {
        backgroundScope.launch {
            when (deviceRepositoryProvider.get().registerCurrentDevice()) {
                is ApiResult.Failure -> Unit
                is ApiResult.Success -> Unit
            }
        }
    }

    private fun clearSessionInternal() {
        _activeSession.value = ActiveSession(authEnabled = appConfig.authEnabled)
        secureSessionStorage.clear()
    }

    private fun setAuthBypassSession() {
        _activeSession.value = ActiveSession(
            authEnabled = false,
            user = DEV_USER,
        )
        _sessionState.value = SessionState.Authenticated(
            user = DEV_USER,
            accessLevel = SessionAccessLevel.Verified,
            statusMessage = "Авторизация отключена для этой сборки.",
        )
    }

    private enum class AuthMode {
        Login,
        Register,
    }

    private companion object {
        val DEV_USER = SessionUser(
            id = "android-dev",
            email = "android@local",
            role = "developer",
        )
    }
}

private fun com.vzaimno.app.core.network.ApiErrorKind.isOfflineLike(): Boolean =
    this == com.vzaimno.app.core.network.ApiErrorKind.Connectivity
