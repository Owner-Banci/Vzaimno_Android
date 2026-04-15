package com.vzaimno.app.feature.ads.create

import com.vzaimno.app.core.model.AnnouncementStructuredData
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class RecommendedPriceRange(
    val min: Int,
    val max: Int,
) {
    val text: String
        get() = if (min == max) "$min ₽" else "$min–$max ₽"

    val minPlaceholder: String get() = min.toString()
    val maxPlaceholder: String get() = max.toString()
}

object CreateAdPriceCalculator {

    fun calculate(draft: AnnouncementCreateFormDraft): RecommendedPriceRange {
        var base = basePrice(draft)
        base += urgencyAdjustment(draft.urgency)
        base += conditionAdjustments(draft)
        base += weightAdjustment(draft.attributes.weightCategory)
        base += sizeAdjustment(draft.attributes.sizeCategory)
        base += dimensionAdjustment(draft)

        base = max(250, base)
        val roundedBase = roundToNearest50(base)
        val calcMin = roundToNearest50(max(250, (roundedBase * 0.85).roundToInt()))
        val calcMax = roundToNearest50(max(calcMin, (roundedBase * 1.15).roundToInt()))

        return RecommendedPriceRange(min = calcMin, max = calcMax)
    }

    private fun basePrice(draft: AnnouncementCreateFormDraft): Int = when (draft.actionType) {
        AnnouncementStructuredData.ActionType.Pickup -> {
            if (draft.resolvedCategory == AnnouncementStructuredData.ResolvedCategory.Handoff) 520
            else 480
        }

        AnnouncementStructuredData.ActionType.Buy -> {
            var b = 620
            when (draft.purchaseType) {
                AnnouncementCreatePurchaseType.Medicine -> b += 80
                AnnouncementCreatePurchaseType.Electronics -> b += 120
                AnnouncementCreatePurchaseType.HomeGoods -> b += 70
                AnnouncementCreatePurchaseType.Clothing -> b += 40
                else -> {}
            }
            b
        }

        AnnouncementStructuredData.ActionType.Carry -> 720
        AnnouncementStructuredData.ActionType.Ride -> 650

        AnnouncementStructuredData.ActionType.ProHelp -> when (draft.helpType) {
            AnnouncementCreateHelpType.Consultation -> 850
            AnnouncementCreateHelpType.SetupDevice -> 980
            AnnouncementCreateHelpType.InstallOrConnect -> 1050
            AnnouncementCreateHelpType.MinorRepair -> 1100
            AnnouncementCreateHelpType.Diagnose -> 950
            AnnouncementCreateHelpType.Other, null -> 900
        }

        AnnouncementStructuredData.ActionType.Other -> 560
        null -> 500
    }

    private fun urgencyAdjustment(urgency: AnnouncementStructuredData.Urgency): Int = when (urgency) {
        AnnouncementStructuredData.Urgency.Now -> 250
        AnnouncementStructuredData.Urgency.Today -> 90
        AnnouncementStructuredData.Urgency.Scheduled -> 0
        AnnouncementStructuredData.Urgency.Flexible -> -40
    }

    private fun conditionAdjustments(draft: AnnouncementCreateFormDraft): Int {
        val attrs = draft.attributes
        var total = 0

        if (attrs.requiresVehicle && draft.actionType != AnnouncementStructuredData.ActionType.Ride) {
            total += 150
        }
        if (attrs.needsTrunk) total += 120
        if (attrs.requiresCarefulHandling) total += 90
        if (attrs.requiresLiftToFloor) {
            total += 130
            if (!attrs.hasElevator) total += 140
        }
        if (attrs.needsLoader) total += 240
        if (attrs.waitOnSite) {
            val minutes = attrs.waitingMinutes.toIntOrNull() ?: 10
            total += (minutes * 8).coerceIn(40, 280)
        }
        if (attrs.callBeforeArrival) total += 20
        if (attrs.requiresConfirmationCode) total += 40
        if (attrs.contactless) total += 20
        if (attrs.requiresReceipt) total += 35
        if (attrs.photoReportRequired) total += 40

        return total
    }

    private fun weightAdjustment(weight: AnnouncementStructuredData.WeightCategory?): Int = when (weight) {
        AnnouncementStructuredData.WeightCategory.UpTo1Kg -> 0
        AnnouncementStructuredData.WeightCategory.UpTo3Kg -> 30
        AnnouncementStructuredData.WeightCategory.UpTo7Kg -> 70
        AnnouncementStructuredData.WeightCategory.UpTo15Kg -> 120
        AnnouncementStructuredData.WeightCategory.Over15Kg -> 200
        null -> 0
    }

    private fun sizeAdjustment(size: AnnouncementStructuredData.SizeCategory?): Int = when (size) {
        AnnouncementStructuredData.SizeCategory.Pocket -> 0
        AnnouncementStructuredData.SizeCategory.Hand -> 20
        AnnouncementStructuredData.SizeCategory.Backpack -> 50
        AnnouncementStructuredData.SizeCategory.Trunk -> 110
        AnnouncementStructuredData.SizeCategory.Bulky -> 180
        null -> 0
    }

    private fun dimensionAdjustment(draft: AnnouncementCreateFormDraft): Int {
        val l = draft.attributes.cargoLength.toIntOrNull() ?: 0
        val w = draft.attributes.cargoWidth.toIntOrNull() ?: 0
        val h = draft.attributes.cargoHeight.toIntOrNull() ?: 0
        val longest = maxOf(l, w, h)
        return when {
            longest >= 100 -> 180
            longest >= 60 -> 100
            longest >= 30 -> 40
            else -> 0
        }
    }

    private fun roundToNearest50(value: Int): Int {
        return ((value + 25) / 50) * 50
    }
}
