package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenDisputeRequestDto(
    @SerialName("problem_title") val problemTitle: String,
    @SerialName("problem_description") val problemDescription: String,
    @SerialName("requested_compensation_rub") val requestedCompensationRub: Int,
    @SerialName("desired_resolution") val desiredResolution: String,
)

@Serializable
data class CounterpartyDisputeResponseRequestDto(
    @SerialName("response_description") val responseDescription: String,
    @SerialName("acceptable_refund_percent") val acceptableRefundPercent: Int,
    @SerialName("desired_resolution") val desiredResolution: String,
)

@Serializable
data class SelectDisputeOptionRequestDto(
    @SerialName("option_id") val optionId: String,
)

@Serializable
data class DisputeQuestionDto(
    val id: String,
    @SerialName("addressed_party") val addressedParty: String,
    val text: String,
)

@Serializable
data class DisputeSettlementOptionDto(
    val id: String,
    val lean: String,
    val title: String,
    val description: String,
    @SerialName("customer_action") val customerAction: String,
    @SerialName("performer_action") val performerAction: String,
    @SerialName("compensation_rub") val compensationRub: Int? = null,
    @SerialName("refund_percent") val refundPercent: Int? = null,
    @SerialName("resolution_kind") val resolutionKind: String,
)

@Serializable
data class DisputeInitiatorTermsDto(
    @SerialName("requested_compensation_rub") val requestedCompensationRub: Int? = null,
    @SerialName("desired_resolution") val desiredResolution: String? = null,
    @SerialName("problem_title") val problemTitle: String? = null,
)

@Serializable
data class DisputeStateDto(
    val id: String,
    @SerialName("thread_id") val threadId: String,
    val status: String,
    @SerialName("initiator_user_id") val initiatorUserId: String,
    @SerialName("counterparty_user_id") val counterpartyUserId: String,
    @SerialName("initiator_party_role") val initiatorPartyRole: String,
    @SerialName("viewer_side") val viewerSide: String,
    @SerialName("viewer_party_role") val viewerPartyRole: String? = null,
    @SerialName("opened_by_display_name") val openedByDisplayName: String,
    @SerialName("counterparty_deadline_at") val counterpartyDeadlineAt: String? = null,
    @SerialName("active_round") val activeRound: Int? = null,
    @SerialName("is_model_thinking") val isModelThinking: Boolean? = null,
    @SerialName("resolution_summary") val resolutionSummary: String? = null,
    @SerialName("selected_option_id") val selectedOptionId: String? = null,
    @SerialName("moderator_required") val moderatorRequired: Boolean? = null,
    val questions: List<DisputeQuestionDto>? = null,
    @SerialName("required_answer_party_roles") val requiredAnswerPartyRoles: List<String>? = null,
    val options: List<DisputeSettlementOptionDto>? = null,
    val votes: Map<String, String>? = null,
    @SerialName("my_vote_option_id") val myVoteOptionId: String? = null,
    @SerialName("initiator_terms") val initiatorTerms: DisputeInitiatorTermsDto? = null,
    @SerialName("last_model_error") val lastModelError: String? = null,
)
