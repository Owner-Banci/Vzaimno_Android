package com.vzaimno.app.feature.discovery

import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint

enum class DiscoveryContentMode {
    Map,
    List,
}

enum class DiscoveryCategoryFilter(val rawValue: String) {
    Delivery("delivery"),
    Help("help"),
}

enum class DiscoveryResponseGate {
    Available,
    OwnAnnouncement,
    RequiresAuth,
    Unavailable,
    AlreadyResponded,
}

enum class DiscoveryCustomPriceError {
    Invalid,
}

enum class DiscoveryOfferSuccessState {
    Submitted,
}

data class DiscoveryFilterState(
    val categories: Set<DiscoveryCategoryFilter> = emptySet(),
    val actions: Set<AnnouncementStructuredData.ActionType> = emptySet(),
    val urgencies: Set<AnnouncementStructuredData.Urgency> = emptySet(),
    val budgetMinText: String = "",
    val budgetMaxText: String = "",
    val withPhotoOnly: Boolean = false,
    val requiresVehicleOnly: Boolean = false,
    val needsLoaderOnly: Boolean = false,
    val contactlessOnly: Boolean = false,
) {
    val budgetMinValue: Int?
        get() = budgetMinText.toPositiveIntOrNull()

    val budgetMaxValue: Int?
        get() = budgetMaxText.toPositiveIntOrNull()

    val hasAnyFilters: Boolean
        get() = activeCount > 0

    val activeCount: Int
        get() {
            var count = 0
            if (categories.isNotEmpty()) count += 1
            if (actions.isNotEmpty()) count += 1
            if (urgencies.isNotEmpty()) count += 1
            if (budgetMinValue != null || budgetMaxValue != null) count += 1
            if (withPhotoOnly) count += 1
            if (requiresVehicleOnly) count += 1
            if (needsLoaderOnly) count += 1
            if (contactlessOnly) count += 1
            return count
        }

    fun toggleCategory(category: DiscoveryCategoryFilter): DiscoveryFilterState = copy(
        categories = categories.toggle(category),
    )

    fun toggleAction(action: AnnouncementStructuredData.ActionType): DiscoveryFilterState = copy(
        actions = actions.toggle(action),
    )

    fun toggleUrgency(urgency: AnnouncementStructuredData.Urgency): DiscoveryFilterState = copy(
        urgencies = urgencies.toggle(urgency),
    )

    fun clearAll(): DiscoveryFilterState = DiscoveryFilterState()
}

sealed interface DiscoveryMapViewport {
    data class FocusPoint(
        val point: GeoPoint,
        val zoom: Float = 14f,
    ) : DiscoveryMapViewport

    data class Bounds(
        val southWest: GeoPoint,
        val northEast: GeoPoint,
    ) : DiscoveryMapViewport

    data class Fallback(
        val point: GeoPoint = GeoPoint(latitude = 55.751244, longitude = 37.618423),
        val zoom: Float = 10f,
    ) : DiscoveryMapViewport
}

data class DiscoveryMapFocusRequest(
    val token: Long,
    val point: GeoPoint,
    val zoom: Float = 14f,
)

data class DiscoveryAnnouncementItemUi(
    val announcement: Announcement,
    val previewImageUrl: String?,
    val subtitle: String,
    val sourceAddress: String?,
    val destinationAddress: String?,
    val point: GeoPoint?,
    val responseGate: DiscoveryResponseGate,
    val budgetText: String?,
)

data class DiscoveryDetailsUiState(
    val isVisible: Boolean = false,
    val announcementId: String? = null,
    val item: DiscoveryAnnouncementItemUi? = null,
    val isLoading: Boolean = false,
    val loadErrorMessage: String? = null,
    val actionErrorMessage: String? = null,
    val offerMessage: String = "",
    val customPriceInput: String = "",
    val customPriceError: DiscoveryCustomPriceError? = null,
    val isCustomPriceDialogVisible: Boolean = false,
    val isSubmittingQuickOffer: Boolean = false,
    val isSubmittingCustomOffer: Boolean = false,
    val offerSuccessState: DiscoveryOfferSuccessState? = null,
) {
    val isSubmitting: Boolean
        get() = isSubmittingQuickOffer || isSubmittingCustomOffer
}

data class DiscoveryUiState(
    val apiBaseUrl: String = "",
    val isMapConfigured: Boolean = false,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadErrorMessage: String? = null,
    val contentMessage: String? = null,
    val contentMode: DiscoveryContentMode = DiscoveryContentMode.Map,
    val searchQuery: String = "",
    val filters: DiscoveryFilterState = DiscoveryFilterState(),
    val filterDraft: DiscoveryFilterState = DiscoveryFilterState(),
    val isFiltersSheetVisible: Boolean = false,
    val totalAnnouncementCount: Int = 0,
    val announcements: List<DiscoveryAnnouncementItemUi> = emptyList(),
    val mapViewport: DiscoveryMapViewport = DiscoveryMapViewport.Fallback(),
    val mapFocusRequest: DiscoveryMapFocusRequest? = null,
    val detailsState: DiscoveryDetailsUiState = DiscoveryDetailsUiState(),
) {
    val mapAnnouncements: List<DiscoveryAnnouncementItemUi>
        get() = announcements.filter { it.point != null }

    val hasActiveSearchOrFilters: Boolean
        get() = searchQuery.isNotBlank() || filters.hasAnyFilters
}

private fun String.toPositiveIntOrNull(): Int? {
    val digits = filter(Char::isDigit)
    return digits.toIntOrNull()
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (contains(value)) {
    this - value
} else {
    this + value
}
