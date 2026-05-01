package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatThreadPreviewDto(
    @SerialName("thread_id") val threadId: String,
    val kind: String,
    @SerialName("partner_id") val partnerId: String? = null,
    @SerialName("partner_display_name") val partnerDisplayName: String,
    @SerialName("partner_avatar_url") val partnerAvatarUrl: String? = null,
    @SerialName("last_message_text") val lastMessageText: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("unread_count") val unreadCount: Int,
    @SerialName("announcement_id") val announcementId: String? = null,
    @SerialName("announcement_title") val announcementTitle: String? = null,
    @SerialName("is_pinned") val isPinned: Boolean? = null,
)

@Serializable
data class ChatMessageDto(
    val id: String,
    @SerialName("thread_id") val threadId: String,
    @SerialName("sender_id") val senderId: String,
    val text: String,
    val type: String = "text",
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SendChatMessageRequestDto(
    val text: String,
)

@Serializable
data class SupportThreadDto(
    @SerialName("thread_id") val threadId: String,
)

@Serializable
data class ReportReasonOptionDto(
    val code: String,
    val title: String,
    val description: String,
    @SerialName("allowed_target_types") val allowedTargetTypes: List<String>,
)

@Serializable
data class ReportSubmissionRequestDto(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("reason_code") val reasonCode: String,
    @SerialName("reason_text") val reasonText: String? = null,
)

@Serializable
data class ReportDto(
    val id: String,
)
