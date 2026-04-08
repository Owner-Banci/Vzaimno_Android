package com.vzaimno.app

import com.vzaimno.app.core.network.ApiError
import com.vzaimno.app.core.network.ApiErrorKind
import com.vzaimno.app.core.session.SessionMessageFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionMessageFormatterTest {

    @Test
    fun `login formatter maps unauthorized to invalid credentials copy`() {
        val message = SessionMessageFormatter.loginError(
            ApiError(
                kind = ApiErrorKind.Unauthorized,
                message = "Not authenticated",
                statusCode = 401,
            ),
        )

        assertEquals("Неверный email или пароль.", message)
    }

    @Test
    fun `register formatter hides technical payloads`() {
        val message = SessionMessageFormatter.registerError(
            ApiError(
                kind = ApiErrorKind.Client,
                message = """{"detail":"duplicate key"}""",
                statusCode = 400,
            ),
        )

        assertEquals("Аккаунт с таким email уже существует.", message)
    }

    @Test
    fun `restore formatter explains offline failure`() {
        val message = SessionMessageFormatter.restoreError(
            ApiError(
                kind = ApiErrorKind.Connectivity,
                message = "socket timeout",
            ),
        )

        assertEquals("Не удалось проверить сохранённую сессию без интернета.", message)
    }
}
