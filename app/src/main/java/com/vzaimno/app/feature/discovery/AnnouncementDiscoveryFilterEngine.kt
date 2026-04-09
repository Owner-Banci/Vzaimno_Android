package com.vzaimno.app.feature.discovery

import com.vzaimno.app.core.common.humanizeRawValue
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.canAcceptOffers
import com.vzaimno.app.core.model.canAppearOnMap
import com.vzaimno.app.core.model.detailsDescriptionText
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.previewImageUrl
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.model.shortStructuredSubtitle
import com.vzaimno.app.core.model.sourcePoint
import com.vzaimno.app.core.model.structuredData
import com.vzaimno.app.core.model.destinationPoint
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

object AnnouncementDiscoveryFilterEngine {

    fun buildItems(
        announcements: List<Announcement>,
        apiBaseUrl: String,
        filters: DiscoveryFilterState,
        query: String,
        currentUserId: String?,
        canRespondWithoutGate: Boolean,
        locallyRespondedIds: Set<String>,
    ): List<DiscoveryAnnouncementItemUi> {
        val normalizedQuery = query.normalizeForSearch()

        return announcements
            .asSequence()
            .filter(Announcement::canAppearOnMap)
            .filter { announcement ->
                matchesFilters(
                    announcement = announcement,
                    filters = filters,
                    normalizedQuery = normalizedQuery,
                )
            }
            .sortedWith(discoveryComparator)
            .map { announcement ->
                buildItem(
                    announcement = announcement,
                    apiBaseUrl = apiBaseUrl,
                    currentUserId = currentUserId,
                    canRespondWithoutGate = canRespondWithoutGate,
                    locallyRespondedIds = locallyRespondedIds,
                )
            }
            .toList()
    }

    fun buildItem(
        announcement: Announcement,
        apiBaseUrl: String,
        currentUserId: String?,
        canRespondWithoutGate: Boolean,
        locallyRespondedIds: Set<String>,
    ): DiscoveryAnnouncementItemUi = DiscoveryAnnouncementItemUi(
        announcement = announcement,
        previewImageUrl = announcement.previewImageUrl(apiBaseUrl),
        subtitle = announcement.discoverySubtitle(),
        sourceAddress = announcement.primarySourceAddress,
        destinationAddress = announcement.primaryDestinationAddress,
        point = announcement.discoveryMapPoint(),
        responseGate = responseGate(
            announcement = announcement,
            currentUserId = currentUserId,
            canRespondWithoutGate = canRespondWithoutGate,
            locallyRespondedIds = locallyRespondedIds,
        ),
        budgetText = announcement.formattedBudgetText,
    )

    fun buildMapViewport(items: List<DiscoveryAnnouncementItemUi>): DiscoveryMapViewport {
        val points = items.mapNotNull { it.point }
        if (points.isEmpty()) return DiscoveryMapViewport.Fallback()
        if (points.size == 1) return DiscoveryMapViewport.FocusPoint(point = points.first())

        val minLat = points.minOf(GeoPoint::latitude)
        val maxLat = points.maxOf(GeoPoint::latitude)
        val minLon = points.minOf(GeoPoint::longitude)
        val maxLon = points.maxOf(GeoPoint::longitude)

        return DiscoveryMapViewport.Bounds(
            southWest = GeoPoint(latitude = minLat, longitude = minLon),
            northEast = GeoPoint(latitude = maxLat, longitude = maxLon),
        )
    }

    fun defaultCustomPrice(announcement: Announcement): Int {
        announcement.quickOfferPrice?.let { return max(0, it) }
        val structured = announcement.structuredData
        val minBudget = structured.budgetMin
        val maxBudget = structured.budgetMax

        return when {
            minBudget != null && maxBudget != null -> max(0, (minBudget + maxBudget) / 2)
            maxBudget != null -> max(0, maxBudget)
            minBudget != null -> max(0, minBudget)
            else -> 0
        }
    }

    fun responseGate(
        announcement: Announcement,
        currentUserId: String?,
        canRespondWithoutGate: Boolean,
        locallyRespondedIds: Set<String>,
    ): DiscoveryResponseGate = when {
        currentUserId != null && currentUserId == announcement.userId -> DiscoveryResponseGate.OwnAnnouncement
        locallyRespondedIds.contains(announcement.id) -> DiscoveryResponseGate.AlreadyResponded
        !announcement.canAcceptOffers -> DiscoveryResponseGate.Unavailable
        !canRespondWithoutGate -> DiscoveryResponseGate.RequiresAuth
        else -> DiscoveryResponseGate.Available
    }

    private fun matchesFilters(
        announcement: Announcement,
        filters: DiscoveryFilterState,
        normalizedQuery: String,
    ): Boolean {
        val structured = announcement.structuredData

        if (filters.categories.isNotEmpty()) {
            val matchesCategory = filters.categories.any { filter ->
                filter.rawValue.equals(announcement.category.trim(), ignoreCase = true)
            }
            if (!matchesCategory) return false
        }

        if (filters.actions.isNotEmpty() && structured.actionType !in filters.actions) return false
        if (filters.urgencies.isNotEmpty() && structured.urgency !in filters.urgencies) return false
        if (filters.withPhotoOnly && !announcement.hasAttachedMedia) return false
        if (filters.requiresVehicleOnly && !structured.requiresVehicle) return false
        if (filters.needsLoaderOnly && !structured.needsLoader) return false
        if (filters.contactlessOnly && !structured.contactless) return false

        filters.budgetMinValue?.let { minimum ->
            val maxBudget = structured.budgetMax ?: structured.budgetMin
            if (maxBudget == null || maxBudget < minimum) return false
        }

        filters.budgetMaxValue?.let { maximum ->
            val minBudget = structured.budgetMin ?: structured.budgetMax
            if (minBudget == null || minBudget > maximum) return false
        }

        if (normalizedQuery.isNotBlank() && !announcement.discoverySearchableText().contains(normalizedQuery)) {
            return false
        }

        return true
    }

    private val discoveryComparator = compareBy<Announcement>(
        { urgencyRank(it.structuredData.urgency) },
        { -discoveryBudgetRank(it) },
        { it.title.lowercase(Locale.getDefault()) },
    )

    private fun discoveryBudgetRank(announcement: Announcement): Int {
        val structured = announcement.structuredData
        return max(
            structured.budgetMax ?: 0,
            structured.budgetMin ?: 0,
        )
    }

    private fun urgencyRank(urgency: AnnouncementStructuredData.Urgency?): Int = when (urgency) {
        AnnouncementStructuredData.Urgency.Now -> 0
        AnnouncementStructuredData.Urgency.Today -> 1
        AnnouncementStructuredData.Urgency.Scheduled -> 2
        AnnouncementStructuredData.Urgency.Flexible,
        null,
        -> 3
    }
}

private fun Announcement.discoverySubtitle(): String {
    val structuredSubtitle = shortStructuredSubtitle.trim()
    if (structuredSubtitle.isNotBlank()) return structuredSubtitle

    val addressParts = listOfNotNull(
        primarySourceAddress,
        primaryDestinationAddress,
    )
    if (addressParts.isNotEmpty()) return addressParts.joinToString(separator = " • ")

    return structuredData.actionType?.title.orEmpty()
}

private fun Announcement.discoveryMapPoint(): GeoPoint? =
    sourcePoint ?: destinationPoint

private fun Announcement.discoverySearchableText(): String = listOfNotNull(
    title,
    shortStructuredSubtitle,
    primarySourceAddress,
    primaryDestinationAddress,
    detailsDescriptionText,
    formattedBudgetText,
    structuredData.actionType?.title,
    structuredData.itemType?.let(::humanizeRawValue),
    structuredData.purchaseType?.let(::humanizeRawValue),
    structuredData.helpType?.let(::humanizeRawValue),
    structuredData.sourceKind?.title,
    structuredData.destinationKind?.title,
    structuredData.urgency?.title,
    structuredData.taskBrief,
).joinToString(separator = " ")
    .normalizeForSearch()

private fun String.normalizeForSearch(): String = Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .lowercase(Locale.getDefault())
    .trim()
