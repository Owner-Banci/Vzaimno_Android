package com.vzaimno.app.feature.ads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AnnouncementDetailsEvent {
    data object RefreshParent : AnnouncementDetailsEvent
    data object RefreshParentAndClose : AnnouncementDetailsEvent
}

@HiltViewModel
class AnnouncementDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val announcementRepository: AnnouncementRepository,
    appConfig: AppConfig,
) : ViewModel() {

    private val announcementId: String =
        savedStateHandle.get<String>(AdsDestination.announcementIdArgument).orEmpty()
    private val _uiState = MutableStateFlow(
        AnnouncementDetailsUiState(
            announcementId = announcementId,
            apiBaseUrl = appConfig.normalizedApiBaseUrl,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AnnouncementDetailsEvent>()
    val events: SharedFlow<AnnouncementDetailsEvent> = _events.asSharedFlow()

    private var hasLoaded = false
    private var loadJob: Job? = null
    private var currentAnnouncement: Announcement? = null

    fun loadIfNeeded() {
        if (announcementId.isBlank() || hasLoaded || loadJob?.isActive == true) return
        load(showLoadingState = true)
    }

    fun retry() {
        load(showLoadingState = currentAnnouncement == null)
    }

    fun refresh() {
        load(showLoadingState = false)
    }

    fun archive() {
        val id = uiState.value.announcementId
        if (id.isBlank() || uiState.value.mutationState != null) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    mutationState = AnnouncementMutationState(
                        announcementId = id,
                        type = AnnouncementMutationType.Archive,
                    ),
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.archiveAnnouncement(id)) {
                is ApiResult.Success -> {
                    currentAnnouncement = result.value
                    _uiState.update { state ->
                        state.copy(
                            announcement = result.value,
                            mutationState = null,
                            contentMessage = null,
                        )
                    }
                    _events.emit(AnnouncementDetailsEvent.RefreshParentAndClose)
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

    fun delete() {
        val id = uiState.value.announcementId
        if (id.isBlank() || uiState.value.mutationState != null) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    mutationState = AnnouncementMutationState(
                        announcementId = id,
                        type = AnnouncementMutationType.Delete,
                    ),
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.deleteAnnouncement(id)) {
                is ApiResult.Success -> {
                    if (result.value) {
                        _uiState.update { state ->
                            state.copy(
                                mutationState = null,
                            )
                        }
                        _events.emit(AnnouncementDetailsEvent.RefreshParentAndClose)
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                mutationState = null,
                            )
                        }
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

    fun appeal() {
        val id = uiState.value.announcementId
        if (id.isBlank() || uiState.value.mutationState != null) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    mutationState = AnnouncementMutationState(
                        announcementId = id,
                        type = AnnouncementMutationType.Appeal,
                    ),
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.appealAnnouncement(id, reason = null)) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    currentAnnouncement = result.value
                    _uiState.update { state ->
                        state.copy(
                            announcement = result.value,
                            mutationState = null,
                            loadErrorMessage = null,
                            contentMessage = null,
                        )
                    }
                    _events.emit(AnnouncementDetailsEvent.RefreshParent)
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
        if (announcementId.isBlank() || loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val previousAnnouncement = currentAnnouncement
            _uiState.update { state ->
                state.copy(
                    isInitialLoading = showLoadingState && previousAnnouncement == null,
                    isRefreshing = !showLoadingState,
                    loadErrorMessage = null,
                    contentMessage = null,
                    mutationState = null,
                )
            }

            when (val result = announcementRepository.fetchAnnouncement(announcementId)) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    currentAnnouncement = result.value
                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            announcement = result.value,
                            loadErrorMessage = null,
                            contentMessage = null,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    if (previousAnnouncement == null) {
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                announcement = null,
                                loadErrorMessage = result.error.message,
                            )
                        }
                    } else {
                        hasLoaded = true
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                announcement = previousAnnouncement,
                                contentMessage = result.error.message,
                            )
                        }
                    }
                }
            }
        }
    }
}
