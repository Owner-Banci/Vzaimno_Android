package com.vzaimno.app.feature.ads

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.vzaimno.app.R
import com.vzaimno.app.core.common.humanizeRawValue
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.TaskExecutionStatus
import com.vzaimno.app.core.model.TaskLifecycleStatus
import com.vzaimno.app.core.model.canAppealModeration
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.hasModerationIssues
import com.vzaimno.app.core.model.hasOnlyTechnicalModerationIssues
import com.vzaimno.app.core.model.needsStatusPolling
import com.vzaimno.app.core.model.offersCount
import com.vzaimno.app.core.model.rawModerationDecisionMessage
import com.vzaimno.app.core.model.shortStructuredSubtitle
import com.vzaimno.app.core.model.taskStateProjection
import com.vzaimno.app.core.model.createdAtEpochSeconds
import java.time.Instant

enum class AnnouncementStatusTone {
    Accent,
    Positive,
    Warning,
    Danger,
    Neutral,
    Info,
}

data class AnnouncementStatusPresentation(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val tone: AnnouncementStatusTone,
)

data class AnnouncementDecisionSummaryUi(
    val message: String? = null,
    @StringRes val fallbackMessageRes: Int? = null,
)

fun Announcement.adsBucket(): AdsFilterBucket {
    val projection = taskStateProjection

    return when {
        projection.lifecycleStatus in setOf(
            TaskLifecycleStatus.Archived,
            TaskLifecycleStatus.Completed,
            TaskLifecycleStatus.Cancelled,
            TaskLifecycleStatus.Deleted,
        ) -> AdsFilterBucket.Archive

        projection.executionStatus == TaskExecutionStatus.Disputed -> AdsFilterBucket.Actions

        projection.lifecycleStatus in setOf(
            TaskLifecycleStatus.Draft,
            TaskLifecycleStatus.PendingReview,
            TaskLifecycleStatus.NeedsFix,
            TaskLifecycleStatus.Rejected,
        ) -> AdsFilterBucket.Actions

        hasModerationIssues -> AdsFilterBucket.Actions

        else -> AdsFilterBucket.Active
    }
}

fun Announcement.statusPresentation(): AnnouncementStatusPresentation {
    val projection = taskStateProjection

    return when (projection.lifecycleStatus) {
        TaskLifecycleStatus.Draft -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_draft,
            descriptionRes = R.string.ads_status_description_draft,
            tone = AnnouncementStatusTone.Neutral,
        )

        TaskLifecycleStatus.PendingReview -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_pending_review,
            descriptionRes = R.string.ads_status_description_pending_review,
            tone = AnnouncementStatusTone.Neutral,
        )

        TaskLifecycleStatus.NeedsFix -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_needs_fix,
            descriptionRes = R.string.ads_status_description_needs_fix,
            tone = AnnouncementStatusTone.Warning,
        )

        TaskLifecycleStatus.Rejected -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_rejected,
            descriptionRes = R.string.ads_status_description_rejected,
            tone = AnnouncementStatusTone.Danger,
        )

        TaskLifecycleStatus.Archived -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_archived,
            descriptionRes = R.string.ads_status_description_archived,
            tone = AnnouncementStatusTone.Neutral,
        )

        TaskLifecycleStatus.Completed -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_completed,
            descriptionRes = R.string.ads_status_description_completed,
            tone = AnnouncementStatusTone.Positive,
        )

        TaskLifecycleStatus.Cancelled -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_cancelled,
            descriptionRes = R.string.ads_status_description_cancelled,
            tone = AnnouncementStatusTone.Neutral,
        )

        TaskLifecycleStatus.Deleted -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_deleted,
            descriptionRes = R.string.ads_status_description_deleted,
            tone = AnnouncementStatusTone.Neutral,
        )

        TaskLifecycleStatus.Assigned,
        TaskLifecycleStatus.InProgress,
        TaskLifecycleStatus.Open,
        -> executionPresentation(projection.executionStatus)
    }
}

private fun executionPresentation(executionStatus: TaskExecutionStatus): AnnouncementStatusPresentation =
    when (executionStatus) {
        TaskExecutionStatus.Accepted -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_assigned,
            descriptionRes = R.string.ads_status_description_assigned,
            tone = AnnouncementStatusTone.Info,
        )

        TaskExecutionStatus.EnRoute -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_en_route,
            descriptionRes = R.string.ads_status_description_en_route,
            tone = AnnouncementStatusTone.Info,
        )

        TaskExecutionStatus.OnSite -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_on_site,
            descriptionRes = R.string.ads_status_description_on_site,
            tone = AnnouncementStatusTone.Info,
        )

        TaskExecutionStatus.InProgress -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_in_progress,
            descriptionRes = R.string.ads_status_description_in_progress,
            tone = AnnouncementStatusTone.Info,
        )

        TaskExecutionStatus.Handoff -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_handoff,
            descriptionRes = R.string.ads_status_description_handoff,
            tone = AnnouncementStatusTone.Info,
        )

        TaskExecutionStatus.Completed -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_completed,
            descriptionRes = R.string.ads_status_description_completed,
            tone = AnnouncementStatusTone.Positive,
        )

        TaskExecutionStatus.Cancelled -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_cancelled,
            descriptionRes = R.string.ads_status_description_cancelled,
            tone = AnnouncementStatusTone.Neutral,
        )

        TaskExecutionStatus.Disputed -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_disputed,
            descriptionRes = R.string.ads_status_description_disputed,
            tone = AnnouncementStatusTone.Warning,
        )

        TaskExecutionStatus.AwaitingAcceptance,
        TaskExecutionStatus.Open,
        -> AnnouncementStatusPresentation(
            labelRes = R.string.ads_status_active,
            descriptionRes = R.string.ads_status_description_active,
            tone = AnnouncementStatusTone.Accent,
        )
    }

fun Announcement.decisionSummaryUi(): AnnouncementDecisionSummaryUi? {
    if (taskStateProjection.lifecycleStatus == TaskLifecycleStatus.Open && hasOnlyTechnicalModerationIssues) {
        return null
    }

    if (hasOnlyTechnicalModerationIssues) {
        return AnnouncementDecisionSummaryUi(
            fallbackMessageRes = R.string.ads_decision_technical_pending,
        )
    }

    val rawMessage = rawModerationDecisionMessage
    if (!rawMessage.isNullOrBlank()) {
        return if (!hasAttachedMedia && needsStatusPolling && rawMessage.contains("проверим фото", ignoreCase = true)) {
            AnnouncementDecisionSummaryUi(
                fallbackMessageRes = R.string.ads_decision_pending_review,
            )
        } else {
            AnnouncementDecisionSummaryUi(message = rawMessage)
        }
    }

    if (needsStatusPolling) {
        return AnnouncementDecisionSummaryUi(
            fallbackMessageRes = R.string.ads_decision_pending_review,
        )
    }

    return null
}

fun Announcement.canArchiveFromAds(): Boolean = taskStateProjection.lifecycleStatus !in setOf(
    TaskLifecycleStatus.Archived,
    TaskLifecycleStatus.Deleted,
)

fun Announcement.canDeleteFromAds(): Boolean =
    taskStateProjection.lifecycleStatus != TaskLifecycleStatus.Deleted

fun Announcement.shouldShowAppealAction(): Boolean =
    canAppealModeration && taskStateProjection.lifecycleStatus in setOf(
        TaskLifecycleStatus.PendingReview,
        TaskLifecycleStatus.NeedsFix,
        TaskLifecycleStatus.Rejected,
    )

fun Announcement.shouldShowReopenEntry(): Boolean =
    taskStateProjection.lifecycleStatus == TaskLifecycleStatus.Archived

fun Announcement.shouldShowCreateAgainEntry(): Boolean =
    taskStateProjection.lifecycleStatus in setOf(
        TaskLifecycleStatus.NeedsFix,
        TaskLifecycleStatus.Rejected,
        TaskLifecycleStatus.Archived,
        TaskLifecycleStatus.Completed,
        TaskLifecycleStatus.Cancelled,
        TaskLifecycleStatus.Deleted,
    )

@Composable
fun announcementCategoryLabel(announcement: Announcement): String = when (announcement.category.trim().lowercase()) {
    "delivery" -> stringResource(R.string.ads_category_delivery)
    "help" -> stringResource(R.string.ads_category_help)
    else -> {
        val structured = announcement.shortStructuredSubtitle.substringBefore("•").trim()
        if (structured.isNotBlank()) structured else humanizeRawValue(announcement.category)
    }
}

@Composable
fun announcementDecisionSummaryText(announcement: Announcement): String? {
    val summary = announcement.decisionSummaryUi() ?: return null
    return summary.message ?: summary.fallbackMessageRes?.let { stringResource(it) }
}

@Composable
fun announcementFieldLabel(field: String): String = when (field) {
    "title" -> stringResource(R.string.ads_field_title)
    "notes" -> stringResource(R.string.ads_field_description)
    "pickup_address",
    "source_address",
    -> stringResource(R.string.ads_field_source_address)

    "dropoff_address",
    "destination_address",
    -> stringResource(R.string.ads_field_destination_address)

    "address" -> stringResource(R.string.ads_field_address)
    "media", "images", "photos" -> stringResource(R.string.ads_field_media)
    else -> humanizeRawValue(field)
}

fun Announcement.createdAtOrEpochFallback(): Instant? = createdAt ?: createdAtEpochSeconds?.let(Instant::ofEpochSecond)

fun Announcement.shouldShowOffersCount(): Boolean =
    adsBucket() == AdsFilterBucket.Active && offersCount > 0

fun AnnouncementStatusTone.containerColor(
    accentColor: Color,
    infoColor: Color,
    positiveColor: Color,
    warningColor: Color,
    dangerColor: Color,
    neutralColor: Color,
): Color = when (this) {
    AnnouncementStatusTone.Accent -> accentColor
    AnnouncementStatusTone.Positive -> positiveColor
    AnnouncementStatusTone.Warning -> warningColor
    AnnouncementStatusTone.Danger -> dangerColor
    AnnouncementStatusTone.Neutral -> neutralColor
    AnnouncementStatusTone.Info -> infoColor
}
