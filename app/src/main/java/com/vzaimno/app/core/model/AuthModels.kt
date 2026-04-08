package com.vzaimno.app.core.model

import kotlinx.serialization.Serializable

data class RegisterCredentials(
    val email: String,
    val password: String,
)

data class LoginCredentials(
    val email: String,
    val password: String,
)

@Serializable
data class AccessToken(
    val value: String,
    val type: String? = null,
)

@Serializable
data class SessionUser(
    val id: String,
    val email: String,
    val role: String,
)
