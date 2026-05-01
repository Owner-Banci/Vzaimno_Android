package com.vzaimno.app.feature.chats

import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.model.ChatThreadPreview
import com.vzaimno.app.core.model.DisputeSettlementOption
import com.vzaimno.app.core.model.DisputeState
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
    type = type,
    mediaUrl = mediaUrl,
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

fun disputeSummaryText(dispute: DisputeState): String = when (dispute.status) {
    "open_waiting_counterparty" ->
        "Спор открыт пользователем ${dispute.openedByDisplayName}. Ожидается ответ второй стороны."

    "model_thinking" -> "Идёт анализ спора моделью Gemini."
    "waiting_clarification_answers" ->
        "Модель задала уточняющие вопросы. Нужны официальные ответы сторон."

    "waiting_round_1_votes" ->
        "Опубликованы 3 варианта урегулирования. Ожидаются выборы сторон."

    "waiting_round_2_votes" ->
        "Запущен второй раунд с более компромиссными вариантами."

    "resolved" -> dispute.resolutionSummary ?: "Спор закрыт."
    "closed_by_acceptance" ->
        dispute.resolutionSummary ?: "Спор закрыт по согласию второй стороны."

    "awaiting_moderator" ->
        dispute.resolutionSummary ?: "Автоматическая часть завершена. Ожидается модератор."

    else -> "Спор в обработке."
}

fun disputeDeadlineText(epochSeconds: Long?): String? {
    if (epochSeconds == null) return null
    val nowSeconds = System.currentTimeMillis() / 1_000L
    val remaining = (epochSeconds - nowSeconds).toInt()
    if (remaining <= 0) return "Срок ответа истёк"
    val hours = remaining / 3600
    val minutes = (remaining % 3600) / 60
    return if (hours > 0) "$hours ч $minutes мин" else "$minutes мин"
}

fun disputeOptionCompensationRub(
    option: DisputeSettlementOption,
    requestedCompensationRub: Int,
): Int? {
    option.compensationRub?.let { if (it > 0) return it }
    val refundPercent = option.refundPercent ?: return null
    if (requestedCompensationRub <= 0) return null
    val amount = (requestedCompensationRub.toDouble() * refundPercent.toDouble() / 100.0)
    val rounded = amount.let { Math.round(it).toInt() }
    return if (rounded > 0) rounded else null
}

fun shortResolutionKindTitle(raw: String): String {
    val value = raw.trim().lowercase(Locale.getDefault())
    return when {
        value.contains("return") || value.contains("refund") -> "Возврат"
        value.contains("redo") || value.contains("rework") || value.contains("fix") -> "Переделка"
        value.contains("replace") -> "Замена"
        value.contains("cancel") -> "Отмена"
        else -> "Вариант"
    }
}

fun compactDisputeOptionTitle(
    option: DisputeSettlementOption,
    requestedCompensationRub: Int,
): String {
    val amount = disputeOptionCompensationRub(option, requestedCompensationRub)
    if (amount != null) {
        return "${formatRub(amount)} ₽"
    }
    return shortResolutionKindTitle(option.resolutionKind)
}

fun formatRub(amount: Int): String {
    // Russian grouping: 1 500 -> "1 500"
    val raw = amount.toString()
    val builder = StringBuilder()
    var count = 0
    for (index in raw.indices.reversed()) {
        builder.insert(0, raw[index])
        count++
        if (count == 3 && index != 0 && raw[index - 1] != '-') {
            builder.insert(0, ' ')
            count = 0
        }
    }
    return builder.toString()
}

fun shouldShowCounterpartyTapTarget(
    dispute: DisputeState?,
    message: ChatMessage,
    canRespondAsCounterparty: Boolean,
): Boolean = evaluateCounterpartyTapTarget(
    dispute = dispute,
    isSystem = message.isSystem,
    text = message.text,
    canRespondAsCounterparty = canRespondAsCounterparty,
)

internal fun shouldShowCounterpartyTapTargetUi(
    dispute: DisputeState?,
    message: ChatMessageUi,
    canRespondAsCounterparty: Boolean,
): Boolean = evaluateCounterpartyTapTarget(
    dispute = dispute,
    isSystem = message.isSystem,
    text = message.text,
    canRespondAsCounterparty = canRespondAsCounterparty,
)

private fun evaluateCounterpartyTapTarget(
    dispute: DisputeState?,
    isSystem: Boolean,
    text: String,
    canRespondAsCounterparty: Boolean,
): Boolean {
    if (!canRespondAsCounterparty) return false
    if (dispute == null) return false
    if (!isSystem) return false
    if (!text.contains("спор", ignoreCase = true)) return false
    if (dispute.openedByDisplayName.isBlank()) return false
    return text.contains(dispute.openedByDisplayName, ignoreCase = true) ||
        text.contains("открыл", ignoreCase = true)
}
