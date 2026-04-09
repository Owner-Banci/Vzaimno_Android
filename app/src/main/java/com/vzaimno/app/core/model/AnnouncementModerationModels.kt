package com.vzaimno.app.core.model

import com.vzaimno.app.core.common.asJsonArrayOrNull
import com.vzaimno.app.core.common.asJsonObjectOrNull
import com.vzaimno.app.core.common.boolOrNullCompat
import com.vzaimno.app.core.common.jsonAt
import com.vzaimno.app.core.common.stringOrNullCompat

enum class ModerationSeverity {
    None,
    Warning,
    Danger,
}

data class AnnouncementModerationReason(
    val field: String,
    val code: String,
    val details: String,
    val canAppeal: Boolean,
) {
    val severity: ModerationSeverity
        get() = if (canAppeal) ModerationSeverity.Warning else ModerationSeverity.Danger

    val isTechnicalIssue: Boolean
        get() {
            val normalizedCode = code.trim().uppercase()
            val normalizedDetails = details.trim().lowercase()

            if (normalizedCode == "TEXT_SYSTEM_UNAVAILABLE" || normalizedCode == "MEDIA_SYSTEM_UNAVAILABLE") {
                return true
            }

            return normalizedCode == "TEXT_UNKNOWN" && (
                normalizedDetails.contains("ollama error") ||
                    normalizedDetails.contains("timed out") ||
                    normalizedDetails.contains("connection refused") ||
                    normalizedDetails.contains("non-json") ||
                    normalizedDetails.contains("не-json")
                )
        }
}

data class AnnouncementModerationDecision(
    val status: String?,
    val message: String?,
)

data class AnnouncementModerationPayload(
    val decision: AnnouncementModerationDecision?,
    val reasons: List<AnnouncementModerationReason>,
    val suggestions: List<String>,
)

val Announcement.moderationPayload: AnnouncementModerationPayload?
    get() {
        val moderationObject = data["moderation"].asJsonObjectOrNull() ?: return null

        val decision = moderationObject["decision"].asJsonObjectOrNull()?.let { decisionObject ->
            AnnouncementModerationDecision(
                status = decisionObject["status"].stringOrNullCompat(),
                message = decisionObject["message"].stringOrNullCompat(),
            )
        }

        val reasons = moderationObject["reasons"]
            .asJsonArrayOrNull()
            ?.mapNotNull { item ->
                val reasonObject = item.asJsonObjectOrNull() ?: return@mapNotNull null
                AnnouncementModerationReason(
                    field = reasonObject["field"].stringOrNullCompat().orEmpty(),
                    code = reasonObject["code"].stringOrNullCompat().orEmpty(),
                    details = reasonObject["details"].stringOrNullCompat().orEmpty(),
                    canAppeal = reasonObject["can_appeal"].boolOrNullCompat() ?: true,
                )
            }
            .orEmpty()

        val suggestions = moderationObject["suggestions"]
            .asJsonArrayOrNull()
            ?.mapNotNull { item -> item.stringOrNullCompat() }
            .orEmpty()

        if (decision == null && reasons.isEmpty() && suggestions.isEmpty()) return null

        return AnnouncementModerationPayload(
            decision = decision,
            reasons = reasons,
            suggestions = suggestions,
        )
    }

val Announcement.visibleModerationReasons: List<AnnouncementModerationReason>
    get() = moderationPayload?.reasons?.filterNot(AnnouncementModerationReason::isTechnicalIssue).orEmpty()

val Announcement.hasModerationIssues: Boolean
    get() = visibleModerationReasons.isNotEmpty()

val Announcement.hasOnlyTechnicalModerationIssues: Boolean
    get() {
        val reasons = moderationPayload?.reasons.orEmpty()
        return reasons.isNotEmpty() && reasons.all(AnnouncementModerationReason::isTechnicalIssue)
    }

val Announcement.maxModerationSeverity: ModerationSeverity
    get() = visibleModerationReasons
        .map(AnnouncementModerationReason::severity)
        .maxByOrNull(ModerationSeverity::ordinal)
        ?: ModerationSeverity.None

val Announcement.rawModerationDecisionMessage: String?
    get() = moderationPayload?.decision?.message?.trim().takeUnless { it.isNullOrEmpty() }

val Announcement.canAppealModeration: Boolean
    get() = visibleModerationReasons.any { it.canAppeal } ||
        data.jsonAt(listOf("moderation", "image", "can_appeal")).boolOrNullCompat() == true

val Announcement.needsStatusPolling: Boolean
    get() = taskStateProjection.lifecycleStatus == TaskLifecycleStatus.PendingReview

fun Announcement.moderationSeverityForField(field: String): ModerationSeverity =
    visibleModerationReasons
        .asSequence()
        .filter { it.field == field }
        .map(AnnouncementModerationReason::severity)
        .maxByOrNull(ModerationSeverity::ordinal)
        ?: ModerationSeverity.None
