package com.vzaimno.app

import com.vzaimno.app.core.network.ApiErrorKind
import com.vzaimno.app.core.network.ApiErrorMapper
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
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
}
