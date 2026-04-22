package com.vzaimno.app.feature.chats

import androidx.compose.runtime.Immutable
import com.vzaimno.app.core.model.ChatThreadPreview
import com.vzaimno.app.core.model.DisputeState
import com.vzaimno.app.core.model.ReportReasonOption
import com.vzaimno.app.core.model.ReviewEligibility

enum class ChatConversationKind(val rawValue: String) {
    Direct("direct"),
    Support("support"),
    Unknown("unknown"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): ChatConversationKind = when (rawValue?.trim()?.lowercase()) {
            Support.rawValue -> Support
            Direct.rawValue -> Direct
            else -> Unknown
        }
    }
}

enum class ChatTransportMode {
    Polling,
}

@Immutable
data class ChatThreadPreviewUi(
    val threadId: String,
    val kind: ChatConversationKind,
    val title: String,
    val subtitle: String?,
    val lastMessage: String,
    val timeLabel: String,
    val unreadCount: Int,
    val avatarUrl: String?,
    val avatarFallback: String,
    val announcementTitle: String?,
    val isPinned: Boolean,
    val isSupport: Boolean,
)

@Immutable
data class ChatsUiState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val threads: List<ChatThreadPreviewUi> = emptyList(),
    val supportThreadId: String? = null,
    val supportThreadAvailable: Boolean = false,
) {
    val isEmpty: Boolean
        get() = !isInitialLoading && threads.isEmpty()
}

@Immutable
data class ChatThreadHeaderUi(
    val title: String = "",
    val subtitle: String? = null,
    val avatarUrl: String? = null,
    val avatarFallback: String = "V",
    val isSupport: Boolean = false,
    val announcementTitle: String? = null,
)

@Immutable
data class ChatMessageUi(
    val id: String,
    val senderId: String,
    val text: String,
    val createdAtEpochSeconds: Long,
    val timeLabel: String,
    val isCurrentUser: Boolean,
    val isSystem: Boolean,
)

@Immutable
data class ChatMessagesState(
    val threadId: String? = null,
    val kind: ChatConversationKind = ChatConversationKind.Direct,
    val preview: ChatThreadPreview? = null,
    val header: ChatThreadHeaderUi = ChatThreadHeaderUi(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<ChatMessageUi> = emptyList(),
) {
    val isEmpty: Boolean
        get() = !isInitialLoading && messages.isEmpty() && errorMessage == null
}

@Immutable
data class SupportThreadState(
    val isSupportEntry: Boolean = false,
    val isResolving: Boolean = false,
    val resolvedThreadId: String? = null,
    val errorMessage: String? = null,
)

@Immutable
data class ChatReportUiState(
    val isSheetVisible: Boolean = false,
    val isLoadingOptions: Boolean = false,
    val isSubmitting: Boolean = false,
    val options: List<ReportReasonOption> = emptyList(),
    val selectedReasonCode: String? = null,
    val comment: String = "",
    val targetSummary: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !selectedReasonCode.isNullOrBlank() && !isSubmitting
}

@Immutable
data class ChatReviewUiState(
    val isLoadingEligibility: Boolean = false,
    val isDialogVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val eligibility: ReviewEligibility? = null,
    val selectedStars: Int = 5,
    val comment: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val canOpenComposer: Boolean
        get() = eligibility?.canSubmit == true && !isLoadingEligibility && !isSubmitting

    val hasVisibleCard: Boolean
        get() = eligibility != null && (
            eligibility.canSubmit ||
                eligibility.alreadySubmitted ||
                !eligibility.message.isNullOrBlank()
            )
}

@Immutable
data class ChatTransportUiState(
    val mode: ChatTransportMode? = null,
    val isActive: Boolean = false,
    val lastSyncEpochSeconds: Long? = null,
    val statusMessage: String? = null,
    val isFallback: Boolean = true,
)

enum class DisputeResolutionKind(val rawValue: String, val title: String) {
    PartialRefund("partial_refund", "Частичный возврат"),
    FullRefund("full_refund", "Полный возврат"),
    ReturnAndRefund("return_and_refund", "Возврат товара и средств"),
    Redo("redo", "Переделать работу"),
    WarningOnly("warning_only", "Только предупреждение"),
    Other("other", "Другое"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): DisputeResolutionKind = values()
            .firstOrNull { it.rawValue == rawValue?.trim()?.lowercase() }
            ?: Other
    }
}

@Immutable
data class DisputeOpenFormState(
    val isVisible: Boolean = false,
    val problemTitle: String = "",
    val problemDescription: String = "",
    val requestedCompensationText: String = "",
    val selectedResolution: DisputeResolutionKind = DisputeResolutionKind.PartialRefund,
)

@Immutable
data class DisputeCounterpartyFormState(
    val isVisible: Boolean = false,
    val isAcceptMode: Boolean = false,
    val responseDescription: String = "",
    val acceptableRefundPercentText: String = "",
    val selectedResolution: DisputeResolutionKind = DisputeResolutionKind.PartialRefund,
)

@Immutable
data class DisputeOptionDetailState(
    val isVisible: Boolean = false,
    val optionId: String? = null,
)

@Immutable
data class DisputeUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val activeDispute: DisputeState? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val openForm: DisputeOpenFormState = DisputeOpenFormState(),
    val counterpartyForm: DisputeCounterpartyFormState = DisputeCounterpartyFormState(),
    val optionDetail: DisputeOptionDetailState = DisputeOptionDetailState(),
) {
    val hasActiveDispute: Boolean
        get() = activeDispute != null

    val shouldShowThinkingState: Boolean
        get() = activeDispute?.isModelThinking == true

    val canRespondAsCounterparty: Boolean
        get() = activeDispute?.let { dispute ->
            dispute.isWaitingCounterparty && dispute.viewerSide == "counterparty"
        } ?: false

    val canVoteInCurrentRound: Boolean
        get() = activeDispute?.let { dispute ->
            val role = dispute.viewerPartyRole
            if (role != "customer" && role != "performer") return@let false
            dispute.isWaitingRound1Votes || dispute.isWaitingRound2Votes
        } ?: false

    val canAnswerClarifications: Boolean
        get() = activeDispute?.let { dispute ->
            val role = dispute.viewerPartyRole ?: return@let false
            dispute.isWaitingClarificationAnswers &&
                dispute.requiredAnswerPartyRoles.contains(role) &&
                dispute.questions.isNotEmpty()
        } ?: false
}

@Immutable
data class ChatThreadUiState(
    val messagesState: ChatMessagesState = ChatMessagesState(),
    val supportThreadState: SupportThreadState = SupportThreadState(),
    val reportState: ChatReportUiState = ChatReportUiState(),
    val reviewState: ChatReviewUiState = ChatReviewUiState(),
    val transportState: ChatTransportUiState = ChatTransportUiState(),
    val disputeState: DisputeUiState = DisputeUiState(),
    val composerText: String = "",
    val isSending: Boolean = false,
) {
    val canSend: Boolean
        get() = composerText.trim().isNotEmpty() && !isSending && !messagesState.isInitialLoading

    val canShowOpenDisputeAction: Boolean
        get() {
            if (messagesState.kind == ChatConversationKind.Support) return false
            val dispute = disputeState.activeDispute ?: return true
            return dispute.canOpenNewDispute
        }
}
