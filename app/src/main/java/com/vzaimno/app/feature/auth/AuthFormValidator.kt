package com.vzaimno.app.feature.auth

data class AuthFieldValidation(
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
) {
    val hasErrors: Boolean
        get() = emailError != null || passwordError != null || confirmPasswordError != null
}

object AuthFormValidator {
    private val emailRegex = Regex(
        pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        option = RegexOption.IGNORE_CASE,
    )

    fun validate(
        mode: AuthMode,
        email: String,
        password: String,
        confirmPassword: String,
    ): AuthFieldValidation {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirmation = confirmPassword.trim()

        val emailError = when {
            trimmedEmail.isEmpty() -> "Введите email"
            !emailRegex.matches(trimmedEmail) -> "Укажите корректный email"
            else -> null
        }

        val passwordError = when {
            trimmedPassword.isEmpty() -> "Введите пароль"
            else -> null
        }

        val confirmPasswordError = when {
            mode != AuthMode.Register -> null
            trimmedConfirmation.isEmpty() -> "Повторите пароль"
            trimmedConfirmation != trimmedPassword -> "Пароли не совпадают"
            else -> null
        }

        return AuthFieldValidation(
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
        )
    }
}
