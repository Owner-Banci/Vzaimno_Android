package com.vzaimno.app.feature.ads

import com.vzaimno.app.core.model.Announcement

enum class AdsFilterBucket {
    Active,
    Actions,
    Archive,
}

enum class AdsScreenState {
    Loading,
    Content,
    Empty,
    Error,
}

enum class AnnouncementMutationType {
    Archive,
    Delete,
    Appeal,
}

data class AnnouncementMutationState(
    val announcementId: String,
    val type: AnnouncementMutationType,
)

data class AdsSummaryUi(
    val activeCount: Int = 0,
    val actionsCount: Int = 0,
    val archiveCount: Int = 0,
)

data class MyAnnouncementsUiState(
    val apiBaseUrl: String = "",
    val screenState: AdsScreenState = AdsScreenState.Loading,
    val isRefreshing: Boolean = false,
    val selectedFilter: AdsFilterBucket = AdsFilterBucket.Active,
    val announcements: List<Announcement> = emptyList(),
    val loadErrorMessage: String? = null,
    val contentMessage: String? = null,
    val mutationState: AnnouncementMutationState? = null,
) {
    val summary: AdsSummaryUi
        get() = AdsSummaryUi(
            activeCount = announcements.count { it.adsBucket() == AdsFilterBucket.Active },
            actionsCount = announcements.count { it.adsBucket() == AdsFilterBucket.Actions },
            archiveCount = announcements.count { it.adsBucket() == AdsFilterBucket.Archive },
        )

    val filteredAnnouncements: List<Announcement>
        get() = announcements.filter { it.adsBucket() == selectedFilter }
}

data class AnnouncementDetailsUiState(
    val announcementId: String = "",
    val apiBaseUrl: String = "",
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val announcement: Announcement? = null,
    val loadErrorMessage: String? = null,
    val contentMessage: String? = null,
    val mutationState: AnnouncementMutationState? = null,
)
