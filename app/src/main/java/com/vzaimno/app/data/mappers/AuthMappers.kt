package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.model.AccessToken
import com.vzaimno.app.core.model.LoginCredentials
import com.vzaimno.app.core.model.RegisterCredentials
import com.vzaimno.app.core.model.SessionUser
import com.vzaimno.app.data.remote.dto.LoginRequestDto
import com.vzaimno.app.data.remote.dto.MeResponseDto
import com.vzaimno.app.data.remote.dto.RegisterRequestDto
import com.vzaimno.app.data.remote.dto.TokenResponseDto

fun RegisterCredentials.toDto(): RegisterRequestDto = RegisterRequestDto(
    email = email,
    password = password,
)

fun LoginCredentials.toDto(): LoginRequestDto = LoginRequestDto(
    email = email,
    password = password,
)

fun TokenResponseDto.toDomain(): AccessToken = AccessToken(
    value = accessToken,
    type = tokenType,
)

fun MeResponseDto.toDomain(): SessionUser = SessionUser(
    id = id,
    email = email,
    role = role,
)
