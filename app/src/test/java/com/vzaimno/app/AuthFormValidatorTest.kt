package com.vzaimno.app

import com.vzaimno.app.feature.auth.AuthFormValidator
import com.vzaimno.app.feature.auth.AuthMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFormValidatorTest {

    @Test
    fun `login validation rejects empty fields`() {
        val validation = AuthFormValidator.validate(
            mode = AuthMode.Login,
            email = "",
            password = "",
            confirmPassword = "",
        )

        assertTrue(validation.hasErrors)
        assertEquals("Введите email", validation.emailError)
        assertEquals("Введите пароль", validation.passwordError)
        assertNull(validation.confirmPasswordError)
    }

    @Test
    fun `register validation requires matching password confirmation`() {
        val validation = AuthFormValidator.validate(
            mode = AuthMode.Register,
            email = "user@example.com",
            password = "secret",
            confirmPassword = "different",
        )

        assertTrue(validation.hasErrors)
        assertEquals("Пароли не совпадают", validation.confirmPasswordError)
    }

    @Test
    fun `valid login fields pass without errors`() {
        val validation = AuthFormValidator.validate(
            mode = AuthMode.Login,
            email = "user@example.com",
            password = "secret",
            confirmPassword = "",
        )

        assertTrue(!validation.hasErrors)
        assertNull(validation.emailError)
        assertNull(validation.passwordError)
    }
}
