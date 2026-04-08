package com.vzaimno.app.core.model

data class RegisterCredentials(
    val email: String,
    val password: String,
)

data class LoginCredentials(
    val email: String,
    val password: String,
)

data class AccessToken(
    val value: String,
    val type: String? = null,
)

data class SessionUser(
    val id: String,
    val email: String,
    val role: String,
)
