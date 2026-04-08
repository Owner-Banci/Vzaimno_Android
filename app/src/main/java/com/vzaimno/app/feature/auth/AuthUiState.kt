package com.vzaimno.app.feature.auth

import androidx.compose.runtime.Immutable

enum class AuthMode {
    Login,
    Register,
}

@Immutable
data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isOffline: Boolean = false,
) {
    val submitLabel: String
        get() = if (mode == AuthMode.Login) "Войти" else "Создать аккаунт"
}
