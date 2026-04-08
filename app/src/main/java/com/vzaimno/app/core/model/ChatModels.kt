package com.vzaimno.app.core.model

data class ChatThreadPreview(
    val threadId: String,
    val kind: String,
    val partnerId: String?,
    val partnerName: String,
    val partnerAvatarUrl: String?,
    val lastMessageText: String,
    val lastMessageAtEpochSeconds: Long?,
    val unreadCount: Int,
    val announcementId: String?,
    val announcementTitle: String?,
    val isPinned: Boolean,
)

data class ChatMessage(
    val id: String,
    val threadId: String,
    val senderId: String,
    val text: String,
    val createdAtEpochSeconds: Long,
) {
    val isSystem: Boolean
        get() = senderId == "system"
}

data class ReportReasonOption(
    val code: String,
    val title: String,
    val description: String,
    val allowedTargetTypes: List<String>,
) {
    fun supports(targetType: String): Boolean = allowedTargetTypes.contains(targetType)
}
