package com.vzaimno.app

import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.config.AppEnvironment
import com.vzaimno.app.core.model.AccessToken
import com.vzaimno.app.core.model.LoginCredentials
import com.vzaimno.app.core.model.RegisterCredentials
import com.vzaimno.app.core.model.SessionUser
import com.vzaimno.app.core.network.ApiError
import com.vzaimno.app.core.network.ApiErrorKind
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.core.security.SecureSessionStorage
import com.vzaimno.app.core.session.SessionAccessLevel
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.data.repository.AuthRepository
import com.vzaimno.app.data.repository.DeviceRepository
import com.vzaimno.app.data.repository.SessionManager
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private val token = AccessToken(value = "token-value", type = "bearer")
    private val user = SessionUser(id = "u1", email = "user@example.com", role = "user")

    @Test
    fun `restore without saved token becomes unauthenticated`() = runTest {
        val storage = FakeSecureSessionStorage()
        val authRepository = FakeAuthRepository()
        val manager = createSessionManager(
            storage = storage,
            authRepository = authRepository,
        )

        manager.restoreSession()
        advanceUntilIdle()

        assertEquals(SessionState.Unauthenticated, manager.sessionState.value)
        assertNull(manager.activeSession.value.accessToken)
        assertEquals(0, authRepository.fetchMeCalls)
    }

    @Test
    fun `invalid restored token clears persisted session`() = runTest {
        val storage = FakeSecureSessionStorage(
            storedToken = token,
            storedUser = user,
        )
        val authRepository = FakeAuthRepository(
            fetchMeResult = ApiResult.Failure(
                ApiError(
                    kind = ApiErrorKind.Unauthorized,
                    message = "Not authenticated",
                    statusCode = 401,
                ),
            ),
        )
        val manager = createSessionManager(
            storage = storage,
            authRepository = authRepository,
        )

        manager.restoreSession()
        advanceUntilIdle()

        assertEquals(SessionState.Unauthenticated, manager.sessionState.value)
        assertNull(storage.storedToken)
        assertNull(storage.storedUser)
    }

    @Test
    fun `restore with valid token and user becomes authenticated verified`() = runTest {
        val storage = FakeSecureSessionStorage(
            storedToken = token,
            storedUser = user,
        )
        val authRepository = FakeAuthRepository(
            fetchMeResult = ApiResult.Success(user),
        )
        val manager = createSessionManager(
            storage = storage,
            authRepository = authRepository,
        )

        manager.restoreSession()
        advanceUntilIdle()

        val state = manager.sessionState.value as SessionState.Authenticated
        assertEquals(SessionAccessLevel.Verified, state.accessLevel)
        assertEquals(user, state.user)
        assertEquals(user, storage.storedUser)
    }

    @Test
    fun `successful login transitions into authenticated state`() = runTest {
        val authRepository = FakeAuthRepository(
            loginResult = ApiResult.Success(token),
            fetchMeResult = ApiResult.Success(user),
        )
        val storage = FakeSecureSessionStorage()
        val manager = createSessionManager(
            storage = storage,
            authRepository = authRepository,
        )

        val error = manager.login(
            credentials = LoginCredentials(
                email = user.email,
                password = "secret",
            ),
        )
        advanceUntilIdle()

        assertNull(error)
        val state = manager.sessionState.value as SessionState.Authenticated
        assertEquals(SessionAccessLevel.Verified, state.accessLevel)
        assertEquals(token, storage.storedToken)
        assertEquals(user, state.user)
    }

    @Test
    fun `login keeps saved token when profile fetch is temporarily offline`() = runTest {
        val authRepository = FakeAuthRepository(
            loginResult = ApiResult.Success(token),
            fetchMeResult = ApiResult.Failure(
                ApiError(
                    kind = ApiErrorKind.Connectivity,
                    message = "offline",
                ),
            ),
        )
        val manager = createSessionManager(
            storage = FakeSecureSessionStorage(),
            authRepository = authRepository,
        )

        val error = manager.login(
            credentials = LoginCredentials(
                email = "user@example.com",
                password = "secret",
            ),
        )
        advanceUntilIdle()

        assertNull(error)
        val state = manager.sessionState.value as SessionState.Authenticated
        assertEquals(SessionAccessLevel.Cached, state.accessLevel)
        assertTrue(state.isOffline)
        assertEquals(token, manager.activeSession.value.accessToken)
        assertNull(state.user)
    }

    private fun createSessionManager(
        storage: FakeSecureSessionStorage,
        authRepository: FakeAuthRepository,
        deviceRepository: FakeDeviceRepository = FakeDeviceRepository(),
    ): SessionManager {
        val dispatcher = UnconfinedTestDispatcher()
        return SessionManager(
            appConfig = AppConfig(
                environment = AppEnvironment.Local,
                apiBaseUrl = "https://api.vzaimno.app/",
                webSocketBaseUrl = "wss://api.vzaimno.app/",
                authEnabled = true,
            ),
            authRepositoryProvider = Provider { authRepository },
            deviceRepositoryProvider = Provider { deviceRepository },
            secureSessionStorage = storage,
            ioDispatcher = dispatcher,
        )
    }

    private class FakeSecureSessionStorage(
        var storedToken: AccessToken? = null,
        var storedUser: SessionUser? = null,
    ) : SecureSessionStorage {
        override fun readToken(): AccessToken? = storedToken
        override fun writeToken(token: AccessToken) {
            storedToken = token
        }

        override fun clearToken() {
            storedToken = null
        }

        override fun readUser(): SessionUser? = storedUser
        override fun writeUser(user: SessionUser) {
            storedUser = user
        }

        override fun clearUser() {
            storedUser = null
        }

        override fun clear() {
            storedToken = null
            storedUser = null
        }
    }

    private class FakeAuthRepository(
        private val registerResult: ApiResult<AccessToken> = ApiResult.Success(AccessToken("register-token")),
        private val loginResult: ApiResult<AccessToken> = ApiResult.Success(AccessToken("login-token")),
        private val fetchMeResult: ApiResult<SessionUser> = ApiResult.Success(
            SessionUser(id = "default", email = "default@example.com", role = "user"),
        ),
    ) : AuthRepository {
        var fetchMeCalls: Int = 0

        override suspend fun register(credentials: RegisterCredentials): ApiResult<AccessToken> = registerResult

        override suspend fun login(credentials: LoginCredentials): ApiResult<AccessToken> = loginResult

        override suspend fun fetchMe(): ApiResult<SessionUser> {
            fetchMeCalls += 1
            return fetchMeResult
        }
    }

    private class FakeDeviceRepository : DeviceRepository {
        override suspend fun registerCurrentDevice(): ApiResult<Unit> = ApiResult.Success(Unit)

        override suspend fun unregisterCurrentDevice(): ApiResult<Unit> = ApiResult.Success(Unit)
    }
}
