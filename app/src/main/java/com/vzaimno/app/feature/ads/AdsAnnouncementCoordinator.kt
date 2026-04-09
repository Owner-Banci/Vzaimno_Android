package com.vzaimno.app.feature.ads

import com.vzaimno.app.core.model.Announcement
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class AdsAnnouncementCoordinator @Inject constructor() {

    private val _optimisticAnnouncements = MutableStateFlow<Map<String, Announcement>>(emptyMap())
    val optimisticAnnouncements: StateFlow<Map<String, Announcement>> = _optimisticAnnouncements.asStateFlow()

    fun upsertOptimistic(announcement: Announcement) {
        _optimisticAnnouncements.update { state ->
            state + (announcement.id to announcement)
        }
    }

    fun replaceOptimistic(localId: String, announcement: Announcement) {
        _optimisticAnnouncements.update { state ->
            (state - localId) + (announcement.id to announcement)
        }
    }

    fun removeOptimistic(announcementId: String) {
        _optimisticAnnouncements.update { state ->
            state - announcementId
        }
    }

    fun reconcileWithServer(announcements: List<Announcement>) {
        val serverIds = announcements.mapTo(linkedSetOf(), Announcement::id)
        _optimisticAnnouncements.update { state ->
            state.filterKeys { optimisticId -> optimisticId !in serverIds }
        }
    }
}
