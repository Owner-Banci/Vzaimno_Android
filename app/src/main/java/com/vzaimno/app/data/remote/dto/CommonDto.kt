package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
object EmptyBodyDto

@Serializable
data class OperationStatusResponseDto(
    val ok: Boolean,
)
