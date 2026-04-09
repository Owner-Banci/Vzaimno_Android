package com.vzaimno.app.feature.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.createdAtEpochSeconds
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MyAnnouncementsViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    appConfig: AppConfig,
) : ViewModel() {

    private val apiBaseUrl = appConfig.normalizedApiBaseUrl
    private val _uiState = MutableStateFlow(
        MyAnnouncementsUiState(
            apiBaseUrl = apiBaseUrl,
        ),
    )
    val uiState: StateFlow<MyAnnouncementsUiState> = _uiState.asStateFlow()

    private var hasLoaded = false
    private var loadJob: Job? = null
    private var announcements: List<Announcement> = emptyList()

    fun loadIfNeeded() {
        if (hasLoaded || loadJob?.isActive == true) return
        load(showLoadingState = true)
    }

    fun retry() {
        load(showLoadingState = announcements.isEmpty())
    }

    fun refresh() {
        load(showLoadingState = false)
    }

    fun selectFilter(filter: AdsFilterBucket) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                contentMessage = null,
            )
        }
    }

    fun archive(announcementId: String) {
        if (_uiState.value.mutationState != null) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    mutationState = AnnouncementMutationState(
                        announcementId = announcementId,
                        type = AnnouncementMutationType.Archive,
                    ),
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.archiveAnnouncement(announcementId)) {
                is ApiResult.Success -> {
                    announcements = announcements
                        .map { announcement ->
                            if (announcement.id == announcementId) {
                                result.value
                            } else {
                                announcement
                            }
                        }
                        .sortedForAds()

                    _uiState.update { state ->
                        state.copy(
                            screenState = if (announcements.isEmpty()) AdsScreenState.Empty else AdsScreenState.Content,
                            announcements = announcements,
                            mutationState = null,
                            contentMessage = null,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            mutationState = null,
                            contentMessage = result.error.message,
                        )
                    }
                }
            }
        }
    }

    fun delete(announcementId: String) {
        if (_uiState.value.mutationState != null) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    mutationState = AnnouncementMutationState(
                        announcementId = announcementId,
                        type = AnnouncementMutationType.Delete,
                    ),
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.deleteAnnouncement(announcementId)) {
                is ApiResult.Success -> {
                    if (result.value) {
                        announcements = announcements
                            .filterNot { it.id == announcementId }
                            .sortedForAds()
                    }

                    _uiState.update { state ->
                        state.copy(
                            screenState = if (announcements.isEmpty()) AdsScreenState.Empty else AdsScreenState.Content,
                            announcements = announcements,
                            mutationState = null,
                            contentMessage = null,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            mutationState = null,
                            contentMessage = result.error.message,
                        )
                    }
                }
            }
        }
    }

    fun clearContentMessage() {
        _uiState.update { state ->
            state.copy(contentMessage = null)
        }
    }

    private fun load(showLoadingState: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val previousAnnouncements = announcements
            _uiState.update { state ->
                state.copy(
                    screenState = if (showLoadingState && previousAnnouncements.isEmpty()) {
                        AdsScreenState.Loading
                    } else {
                        state.screenState
                    },
                    isRefreshing = !showLoadingState,
                    loadErrorMessage = null,
                    contentMessage = null,
                    mutationState = null,
                )
            }

            when (val result = announcementRepository.fetchMyAnnouncements()) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    announcements = result.value.sortedForAds()
                    _uiState.update { state ->
                        state.copy(
                            screenState = if (announcements.isEmpty()) AdsScreenState.Empty else AdsScreenState.Content,
                            isRefreshing = false,
                            announcements = announcements,
                            loadErrorMessage = null,
                            contentMessage = null,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    if (previousAnnouncements.isEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                screenState = AdsScreenState.Error,
                                isRefreshing = false,
                                announcements = emptyList(),
                                loadErrorMessage = result.error.message,
                            )
                        }
                    } else {
                        hasLoaded = true
                        _uiState.update { state ->
                            state.copy(
                                screenState = AdsScreenState.Content,
                                isRefreshing = false,
                                announcements = previousAnnouncements,
                                contentMessage = result.error.message,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun List<Announcement>.sortedForAds(): List<Announcement> = sortedWith(
    compareByDescending<Announcement> { it.createdAtEpochSeconds ?: Long.MIN_VALUE }
        .thenByDescending { it.id },
)
