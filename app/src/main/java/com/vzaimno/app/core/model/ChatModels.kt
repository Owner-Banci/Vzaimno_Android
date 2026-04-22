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

data class DisputeQuestion(
    val id: String,
    val addressedParty: String,
    val text: String,
)

data class DisputeSettlementOption(
    val id: String,
    val lean: String,
    val title: String,
    val description: String,
    val customerAction: String,
    val performerAction: String,
    val compensationRub: Int?,
    val refundPercent: Int?,
    val resolutionKind: String,
)

data class DisputeInitiatorTerms(
    val requestedCompensationRub: Int,
    val desiredResolution: String,
    val problemTitle: String,
)

data class DisputeState(
    val id: String,
    val threadId: String,
    val status: String,
    val initiatorUserId: String,
    val counterpartyUserId: String,
    val initiatorPartyRole: String,
    val viewerSide: String,
    val viewerPartyRole: String?,
    val openedByDisplayName: String,
    val counterpartyDeadlineAtEpochSeconds: Long?,
    val activeRound: Int,
    val isModelThinking: Boolean,
    val resolutionSummary: String?,
    val selectedOptionId: String?,
    val moderatorRequired: Boolean,
    val questions: List<DisputeQuestion>,
    val requiredAnswerPartyRoles: List<String>,
    val options: List<DisputeSettlementOption>,
    val votes: Map<String, String>,
    val myVoteOptionId: String?,
    val initiatorTerms: DisputeInitiatorTerms,
    val lastModelError: String?,
) {
    val isWaitingCounterparty: Boolean
        get() = status == "open_waiting_counterparty"

    val isWaitingClarificationAnswers: Boolean
        get() = status == "waiting_clarification_answers"

    val isWaitingRound1Votes: Boolean
        get() = status == "waiting_round_1_votes"

    val isWaitingRound2Votes: Boolean
        get() = status == "waiting_round_2_votes"

    val isResolved: Boolean
        get() = status == "resolved" || status == "closed"

    val canOpenNewDispute: Boolean
        get() = !moderatorRequired &&
            !isWaitingCounterparty &&
            !isModelThinking &&
            !isWaitingClarificationAnswers &&
            !isWaitingRound1Votes &&
            !isWaitingRound2Votes
}
