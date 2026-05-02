package com.vzaimno.app

import com.vzaimno.app.core.network.ApiErrorKind
import com.vzaimno.app.core.network.ApiErrorMapper
import java.net.ConnectException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorMapperTest {

    private val mapper = ApiErrorMapper(
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        },
    )

    @Test
    fun `maps fastapi validation array into human readable message`() {
        val error = mapper.map(
            statusCode = 422,
            responseBody = """
                {
                  "detail": [
                    {
                      "loc": ["body", "email"],
                      "msg": "value is not a valid email address",
                      "type": "value_error"
                    }
                  ]
                }
            """.trimIndent(),
        )

        assertEquals(ApiErrorKind.Validation, error.kind)
        assertEquals("Неверный email. Пример: name@mail.com", error.message)
    }

    @Test
    fun `marks unauthorized errors as session invalidating`() {
        val error = mapper.map(
            statusCode = 401,
            responseBody = """{"detail":"Not authenticated"}""",
        )

        assertEquals(ApiErrorKind.Unauthorized, error.kind)
        assertTrue(error.invalidatesSession)
        assertEquals("Not authenticated", error.message)
    }

    @Test
    fun `maps connectivity errors without technical endpoint details`() {
        val error = mapper.map(
            ConnectException("Failed to connect to /10.0.2.2:8000"),
        )

        assertEquals(ApiErrorKind.Connectivity, error.kind)
        assertEquals(
            "Не удалось загрузить данные. Проверьте подключение к интернету и попробуйте снова.",
            error.message,
        )
        assertFalse(error.message.contains("10.0.2.2"))
        assertFalse(error.message.contains("8000"))
        assertFalse(error.message.contains("Failed to connect"))
    }

    @Test
    fun `maps serialization errors to user friendly retry message`() {
        val error = mapper.map(
            SerializationException("Field 'id' is required"),
        )

        assertEquals(ApiErrorKind.Serialization, error.kind)
        assertEquals("Не удалось обновить данные. Попробуйте ещё раз.", error.message)
        assertFalse(error.message.contains("Field"))
        assertFalse(error.message.contains("required"))
    }
}
