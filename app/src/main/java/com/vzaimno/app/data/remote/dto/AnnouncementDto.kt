package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class AnnouncementDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val category: String,
    val title: String,
    val status: String,
    val data: JsonObject = buildJsonObject { },
    @SerialName("created_at") val createdAt: String,
    val media: List<JsonElement>? = null,
)

@Serializable
data class CreateAnnouncementRequestDto(
    val category: String,
    val title: String,
    val status: String,
    val data: JsonObject,
)

@Serializable
data class MediaModerationResponseDto(
    val announcement: AnnouncementDto,
    @SerialName("max_nsfw") val maxNsfw: Double,
    val decision: String,
    @SerialName("can_appeal") val canAppeal: Boolean,
    val message: String,
)

@Serializable
data class AppealRequestDto(
    val reason: String? = null,
)

@Serializable
data class DeleteOkResponseDto(
    val ok: Boolean,
)

@Serializable
data class ExecutionStageUpdateRequestDto(
    val stage: String,
)
