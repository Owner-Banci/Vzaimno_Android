package com.vzaimno.app.feature.chats

import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.model.ChatThreadPreview
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ChatReportTarget(
    val type: String,
    val id: String,
    val summary: String,
)

fun sortChatThreads(items: List<ChatThreadPreview>): List<ChatThreadPreview> = items.sortedWith(
    compareByDescending<ChatThreadPreview> { it.isPinned }
        .thenByDescending { it.lastMessageAtEpochSeconds ?: Long.MIN_VALUE },
)

fun ChatThreadPreview.toUi(): ChatThreadPreviewUi = ChatThreadPreviewUi(
    threadId = threadId,
    kind = ChatConversationKind.fromRaw(kind),
    title = partnerName,
    subtitle = announcementTitle,
    lastMessage = lastMessageText,
    timeLabel = formatThreadTimestamp(lastMessageAtEpochSeconds),
    unreadCount = unreadCount.coerceAtLeast(0),
    avatarUrl = partnerAvatarUrl,
    avatarFallback = partnerName.chatInitials(
        fallback = if (kind == ChatConversationKind.Support.rawValue) "SV" else "V",
    ),
    announcementTitle = announcementTitle,
    isPinned = isPinned,
    isSupport = kind == ChatConversationKind.Support.rawValue,
)

fun ChatThreadPreview.toHeaderUi(): ChatThreadHeaderUi = ChatThreadHeaderUi(
    title = partnerName,
    subtitle = announcementTitle,
    avatarUrl = partnerAvatarUrl,
    avatarFallback = partnerName.chatInitials(
        fallback = if (kind == ChatConversationKind.Support.rawValue) "SV" else "V",
    ),
    isSupport = kind == ChatConversationKind.Support.rawValue,
    announcementTitle = announcementTitle,
)

fun ChatMessage.toUi(currentUserId: String?): ChatMessageUi = ChatMessageUi(
    id = id,
    senderId = senderId,
    text = text,
    createdAtEpochSeconds = createdAtEpochSeconds,
    timeLabel = formatMessageTimestamp(createdAtEpochSeconds),
    isCurrentUser = !isSystem && currentUserId != null && senderId == currentUserId,
    isSystem = isSystem,
)

fun buildSupportPreview(threadId: String): ChatThreadPreview = ChatThreadPreview(
    threadId = threadId,
    kind = ChatConversationKind.Support.rawValue,
    partnerId = null,
    partnerName = "Поддержка Vzaimno",
    partnerAvatarUrl = null,
    lastMessageText = "Мы ответим здесь, как только появятся новые детали.",
    lastMessageAtEpochSeconds = null,
    unreadCount = 0,
    announcementId = null,
    announcementTitle = null,
    isPinned = true,
)

fun mergeMessages(
    existing: List<ChatMessage>,
    incoming: List<ChatMessage>,
): List<ChatMessage> = (existing + incoming)
    .distinctBy(ChatMessage::id)
    .sortedBy(ChatMessage::createdAtEpochSeconds)

fun resolveReportTarget(
    preview: ChatThreadPreview?,
    messages: List<ChatMessage>,
    currentUserId: String?,
): ChatReportTarget? {
    val latestIncomingMessage = messages.lastOrNull { message ->
        !message.isSystem && message.senderId != currentUserId
    }
    if (latestIncomingMessage != null) {
        return ChatReportTarget(
            type = "message",
            id = latestIncomingMessage.id,
            summary = "Жалоба будет связана с последним сообщением собеседника.",
        )
    }

    preview?.partnerId
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.let { partnerId ->
            return ChatReportTarget(
                type = "user",
                id = partnerId,
                summary = "Жалоба будет отправлена на пользователя ${preview.partnerName}.",
            )
        }

    preview?.announcementId
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.let { announcementId ->
            return ChatReportTarget(
                type = "task",
                id = announcementId,
                summary = "Жалоба будет связана с заданием ${preview.announcementTitle ?: "без названия"}.",
            )
        }

    return null
}

fun String.chatInitials(fallback: String = "V"): String = trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString(separator = "") { token ->
        token.first().uppercase()
    }
    .ifBlank { fallback }

private fun formatThreadTimestamp(epochSeconds: Long?): String {
    if (epochSeconds == null) return ""

    val locale = Locale.getDefault()
    val zoneId = ZoneId.systemDefault()
    val instant = Instant.ofEpochSecond(epochSeconds)
    val dateTime = instant.atZone(zoneId)
    val now = Instant.now().atZone(zoneId)

    return when {
        dateTime.toLocalDate() == now.toLocalDate() -> {
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm", locale))
        }

        dateTime.year == now.year -> {
            dateTime.format(DateTimeFormatter.ofPattern("d MMM", locale))
        }

        else -> {
            dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
        }
    }
}

private fun formatMessageTimestamp(epochSeconds: Long): String {
    val locale = Locale.getDefault()
    val zoneId = ZoneId.systemDefault()
    val instant = Instant.ofEpochSecond(epochSeconds.coerceAtLeast(0L))
    val dateTime = instant.atZone(zoneId)
    val now = Instant.now().atZone(zoneId)

    return if (dateTime.toLocalDate() == now.toLocalDate()) {
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    } else {
        dateTime.format(DateTimeFormatter.ofPattern("d MMM, HH:mm", locale))
    }
}

fun shouldShowDateDivider(
    previousEpochSeconds: Long?,
    currentEpochSeconds: Long,
): Boolean {
    if (previousEpochSeconds == null) return true

    val zoneId = ZoneId.systemDefault()
    val previousDate = Instant.ofEpochSecond(previousEpochSeconds).atZone(zoneId).toLocalDate()
    val currentDate = Instant.ofEpochSecond(currentEpochSeconds).atZone(zoneId).toLocalDate()
    return previousDate != currentDate
}

fun formatDateDivider(epochSeconds: Long): String {
    val locale = Locale.getDefault()
    val zoneId = ZoneId.systemDefault()
    val date = Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)

    return when (date) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
    }
}
