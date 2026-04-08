package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.common.parseInstant
import com.vzaimno.app.core.common.resolveAgainstBaseUrl
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.model.ChatThreadPreview
import com.vzaimno.app.core.model.ReportReasonOption
import com.vzaimno.app.data.remote.dto.ChatMessageDto
import com.vzaimno.app.data.remote.dto.ChatThreadPreviewDto
import com.vzaimno.app.data.remote.dto.ReportReasonOptionDto

fun ChatThreadPreviewDto.toDomain(apiBaseUrl: String): ChatThreadPreview = ChatThreadPreview(
    threadId = threadId,
    kind = kind,
    partnerId = partnerId,
    partnerName = if (kind == "support") {
        "Поддержка Vzaimno"
    } else {
        partnerDisplayName.trimmedOrNull() ?: "Собеседник"
    },
    partnerAvatarUrl = resolveAgainstBaseUrl(apiBaseUrl, partnerAvatarUrl),
    lastMessageText = lastMessageText.trimmedOrNull()
        ?: if (kind == "support") "Чат с поддержкой открыт" else "Чат открыт",
    lastMessageAtEpochSeconds = parseInstant(lastMessageAt)?.epochSecond,
    unreadCount = unreadCount,
    announcementId = announcementId?.trimmedOrNull(),
    announcementTitle = announcementTitle?.trimmedOrNull(),
    isPinned = isPinned ?: (kind == "support"),
)

fun ChatMessageDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    threadId = threadId,
    senderId = senderId,
    text = text,
    createdAtEpochSeconds = parseInstant(createdAt)?.epochSecond ?: 0L,
)

fun ReportReasonOptionDto.toDomain(): ReportReasonOption = ReportReasonOption(
    code = code,
    title = title,
    description = description,
    allowedTargetTypes = allowedTargetTypes,
)
