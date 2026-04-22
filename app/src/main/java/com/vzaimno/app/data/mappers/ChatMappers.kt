package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.common.parseInstant
import com.vzaimno.app.core.common.resolveAgainstBaseUrl
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.model.ChatThreadPreview
import com.vzaimno.app.core.model.DisputeInitiatorTerms
import com.vzaimno.app.core.model.DisputeQuestion
import com.vzaimno.app.core.model.DisputeSettlementOption
import com.vzaimno.app.core.model.DisputeState
import com.vzaimno.app.core.model.ReportReasonOption
import com.vzaimno.app.data.remote.dto.ChatMessageDto
import com.vzaimno.app.data.remote.dto.ChatThreadPreviewDto
import com.vzaimno.app.data.remote.dto.DisputeInitiatorTermsDto
import com.vzaimno.app.data.remote.dto.DisputeQuestionDto
import com.vzaimno.app.data.remote.dto.DisputeSettlementOptionDto
import com.vzaimno.app.data.remote.dto.DisputeStateDto
import com.vzaimno.app.data.remote.dto.ReportReasonOptionDto
import kotlin.math.max

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

fun DisputeQuestionDto.toDomain(): DisputeQuestion = DisputeQuestion(
    id = id,
    addressedParty = addressedParty,
    text = text,
)

fun DisputeSettlementOptionDto.toDomain(): DisputeSettlementOption = DisputeSettlementOption(
    id = id,
    lean = lean,
    title = title,
    description = description,
    customerAction = customerAction,
    performerAction = performerAction,
    compensationRub = compensationRub,
    refundPercent = refundPercent,
    resolutionKind = resolutionKind,
)

fun DisputeInitiatorTermsDto.toDomain(): DisputeInitiatorTerms = DisputeInitiatorTerms(
    requestedCompensationRub = requestedCompensationRub ?: 0,
    desiredResolution = desiredResolution.trimmedOrNull() ?: "other",
    problemTitle = problemTitle.trimmedOrNull() ?: "",
)

fun DisputeStateDto.toDomain(): DisputeState = DisputeState(
    id = id,
    threadId = threadId,
    status = status,
    initiatorUserId = initiatorUserId,
    counterpartyUserId = counterpartyUserId,
    initiatorPartyRole = initiatorPartyRole,
    viewerSide = viewerSide,
    viewerPartyRole = viewerPartyRole.trimmedOrNull(),
    openedByDisplayName = openedByDisplayName.trimmedOrNull() ?: "Система",
    counterpartyDeadlineAtEpochSeconds = parseInstant(counterpartyDeadlineAt)?.epochSecond,
    activeRound = max(1, activeRound ?: 1),
    isModelThinking = isModelThinking ?: false,
    resolutionSummary = resolutionSummary.trimmedOrNull(),
    selectedOptionId = selectedOptionId.trimmedOrNull(),
    moderatorRequired = moderatorRequired ?: false,
    questions = questions.orEmpty().map { it.toDomain() },
    requiredAnswerPartyRoles = requiredAnswerPartyRoles.orEmpty(),
    options = options.orEmpty().map { it.toDomain() },
    votes = votes.orEmpty(),
    myVoteOptionId = myVoteOptionId.trimmedOrNull(),
    initiatorTerms = initiatorTerms?.toDomain() ?: DisputeInitiatorTerms(
        requestedCompensationRub = 0,
        desiredResolution = "other",
        problemTitle = "",
    ),
    lastModelError = lastModelError.trimmedOrNull(),
)
