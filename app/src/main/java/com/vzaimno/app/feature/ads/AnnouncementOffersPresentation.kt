package com.vzaimno.app.feature.ads

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.vzaimno.app.R
import com.vzaimno.app.core.common.formatRubles
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementOffer
import com.vzaimno.app.core.model.AnnouncementOfferStatus
import com.vzaimno.app.core.model.OfferPerformer
import com.vzaimno.app.core.model.OfferPricingMode
import com.vzaimno.app.core.model.TaskExecutionStatus
import com.vzaimno.app.core.model.TaskLifecycleStatus
import com.vzaimno.app.core.model.canAcceptOffers
import com.vzaimno.app.core.model.offersCount
import com.vzaimno.app.core.model.taskStateProjection
import java.time.Instant
import java.util.Locale

private val ownerOffersVisibleLifecycleStatuses = setOf(
    TaskLifecycleStatus.Open,
    TaskLifecycleStatus.Assigned,
    TaskLifecycleStatus.InProgress,
    TaskLifecycleStatus.Completed,
    TaskLifecycleStatus.Cancelled,
)

@Immutable
data class OwnerOffersSectionPresentation(
    val isVisible: Boolean,
    val acceptsNewOffers: Boolean,
    @StringRes val stateLabelRes: Int,
    val stateTone: AnnouncementStatusTone,
    @StringRes val summaryRes: Int,
)

@Immutable
data class OfferStatusBadgePresentation(
    @StringRes val labelRes: Int,
    val tone: AnnouncementStatusTone,
)

@Immutable
data class OfferSummaryPresentation(
    val message: String?,
    @StringRes val fallbackRes: Int? = null,
)

enum class OfferDecisionAction {
    Accept,
    Reject,
}

fun Announcement.ownerOffersSectionPresentation(): OwnerOffersSectionPresentation? {
    val projection = taskStateProjection
    val shouldShow = offersCount > 0 ||
        projection.lifecycleStatus in ownerOffersVisibleLifecycleStatuses ||
        projection.executionStatus != TaskExecutionStatus.Open

    if (!shouldShow) return null

    if (canAcceptOffers) {
        return OwnerOffersSectionPresentation(
            isVisible = true,
            acceptsNewOffers = true,
            stateLabelRes = R.string.ads_offers_state_open,
            stateTone = AnnouncementStatusTone.Accent,
            summaryRes = if (offersCount > 0) {
                R.string.ads_offers_summary_open_with_offers
            } else {
                R.string.ads_offers_summary_open_empty
            },
        )
    }

    val summaryRes = when (projection.executionStatus) {
        TaskExecutionStatus.Accepted,
        TaskExecutionStatus.EnRoute,
        TaskExecutionStatus.OnSite,
        TaskExecutionStatus.InProgress,
        TaskExecutionStatus.Handoff,
        -> R.string.ads_offers_summary_closed_assigned

        TaskExecutionStatus.Completed -> R.string.ads_offers_summary_closed_completed
        TaskExecutionStatus.Cancelled -> R.string.ads_offers_summary_closed_cancelled
        TaskExecutionStatus.Disputed -> R.string.ads_offers_summary_closed_disputed
        TaskExecutionStatus.AwaitingAcceptance,
        TaskExecutionStatus.Open,
        -> R.string.ads_offers_summary_closed_generic
    }

    return OwnerOffersSectionPresentation(
        isVisible = true,
        acceptsNewOffers = false,
        stateLabelRes = R.string.ads_offers_state_closed,
        stateTone = AnnouncementStatusTone.Neutral,
        summaryRes = summaryRes,
    )
}

fun AnnouncementOffer.statusBadgePresentation(): OfferStatusBadgePresentation =
    when (statusValue) {
        AnnouncementOfferStatus.Accepted -> OfferStatusBadgePresentation(
            labelRes = R.string.ads_offer_status_accepted,
            tone = AnnouncementStatusTone.Positive,
        )

        AnnouncementOfferStatus.Rejected -> OfferStatusBadgePresentation(
            labelRes = R.string.ads_offer_status_rejected,
            tone = AnnouncementStatusTone.Neutral,
        )

        AnnouncementOfferStatus.Withdrawn -> OfferStatusBadgePresentation(
            labelRes = R.string.ads_offer_status_withdrawn,
            tone = AnnouncementStatusTone.Neutral,
        )

        AnnouncementOfferStatus.Expired -> OfferStatusBadgePresentation(
            labelRes = R.string.ads_offer_status_expired,
            tone = AnnouncementStatusTone.Warning,
        )

        AnnouncementOfferStatus.Blocked -> OfferStatusBadgePresentation(
            labelRes = R.string.ads_offer_status_blocked,
            tone = AnnouncementStatusTone.Warning,
        )

        AnnouncementOfferStatus.Pending,
        null,
        -> OfferStatusBadgePresentation(
            labelRes = R.string.ads_offer_status_pending,
            tone = AnnouncementStatusTone.Accent,
        )
    }

fun AnnouncementOffer.canAcceptFromOwner(announcement: Announcement): Boolean =
    announcement.canAcceptOffers && statusValue == AnnouncementOfferStatus.Pending

fun AnnouncementOffer.canRejectFromOwner(announcement: Announcement): Boolean =
    announcement.canAcceptOffers && statusValue == AnnouncementOfferStatus.Pending

fun AnnouncementOffer.formattedPriceText(): String? = (agreedPrice ?: proposedPrice)?.formatRubles()

@StringRes
fun AnnouncementOffer.pricingModeLabelRes(): Int = when (pricingMode) {
    OfferPricingMode.QuickMinPrice -> R.string.ads_offer_pricing_quick_min
    OfferPricingMode.CounterPrice -> R.string.ads_offer_pricing_counter
    OfferPricingMode.AgreedPrice -> R.string.ads_offer_pricing_agreed
}

fun AnnouncementOffer.summaryPresentation(): OfferSummaryPresentation = when {
    !message.isNullOrBlank() -> OfferSummaryPresentation(message = message)
    minimumPriceAccepted || pricingMode == OfferPricingMode.QuickMinPrice -> OfferSummaryPresentation(
        message = null,
        fallbackRes = R.string.ads_offer_summary_quick_min,
    )

    proposedPrice == null && agreedPrice == null -> OfferSummaryPresentation(
        message = null,
        fallbackRes = R.string.ads_offer_summary_quick_generic,
    )

    else -> OfferSummaryPresentation(
        message = null,
        fallbackRes = R.string.ads_offer_summary_no_message,
    )
}

fun AnnouncementOffer.secondaryHintText(): Int? = when {
    canReoffer && statusValue == AnnouncementOfferStatus.Rejected -> R.string.ads_offer_hint_reoffer
    minimumPriceAccepted || pricingMode == OfferPricingMode.QuickMinPrice -> R.string.ads_offer_hint_quick_price
    else -> null
}

fun AnnouncementOffer.performerName(fallback: String): String =
    performer?.displayName?.trimmedOrNull() ?: fallback

fun AnnouncementOffer.performerContextText(): String? = performer?.secondaryContext()

fun AnnouncementOffer.performerInitials(fallback: String): String = performer?.displayName.initialsOrFallback(fallback)

fun AnnouncementOffer.ratingText(): String? {
    val stats = performerStats ?: return null
    if (stats.ratingCount <= 0) return null
    return String.format(Locale.getDefault(), "%.1f", stats.ratingAverage)
}

fun AnnouncementOffer.completedCountText(): String? {
    val stats = performerStats ?: return null
    return if (stats.completedCount > 0) stats.completedCount.toString() else null
}

fun AnnouncementOffer.createdAtLabel(): String = DateUtils.getRelativeTimeSpanString(
    Instant.ofEpochSecond(createdAtEpochSeconds.coerceAtLeast(0L)).toEpochMilli(),
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE,
).toString()

fun List<AnnouncementOffer>.sortedForOwner(): List<AnnouncementOffer> = sortedWith(
    compareBy<AnnouncementOffer>(
        { offer ->
            when (offer.statusValue) {
                AnnouncementOfferStatus.Pending,
                null,
                -> 0

                AnnouncementOfferStatus.Accepted -> 1
                AnnouncementOfferStatus.Rejected -> 2
                AnnouncementOfferStatus.Withdrawn -> 3
                AnnouncementOfferStatus.Expired -> 4
                AnnouncementOfferStatus.Blocked -> 5
            }
        },
        { -it.createdAtEpochSeconds },
    ).thenByDescending { it.id },
)

private fun OfferPerformer.secondaryContext(): String? = listOfNotNull(
    city?.trimmedOrNull(),
    contact?.trimmedOrNull()?.takeIf { it != displayName.trimmedOrNull() },
).distinct().takeIf { it.isNotEmpty() }?.joinToString(separator = " • ")

private fun String?.initialsOrFallback(fallback: String): String {
    val parts = this.orEmpty()
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }

    return parts.joinToString(separator = "").ifBlank { fallback }
}
