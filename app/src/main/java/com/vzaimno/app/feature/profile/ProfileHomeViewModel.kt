package com.vzaimno.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileHomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileHomeUiState())
    val uiState: StateFlow<ProfileHomeUiState> = _uiState.asStateFlow()

    private var hasLoaded = false
    private var loadJob: Job? = null

    fun loadIfNeeded() {
        if (hasLoaded || loadJob?.isActive == true) return
        load(showLoadingState = true)
    }

    fun retry() {
        load(showLoadingState = _uiState.value.profile == null)
    }

    fun refresh() {
        load(showLoadingState = false)
    }

    fun refreshAfterEdit() {
        load(showLoadingState = false)
    }

    private fun load(showLoadingState: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val previousState = _uiState.value
            _uiState.update { state ->
                state.copy(
                    isInitialLoading = showLoadingState && state.profile == null,
                    isRefreshing = !showLoadingState && state.profile != null,
                    loadErrorMessage = null,
                    contentMessage = null,
                    previewState = if (showLoadingState && state.profile == null) {
                        ProfileReviewsPreviewUiState.Loading
                    } else {
                        state.previewState
                    },
                )
            }

            val profileResult: ApiResult<com.vzaimno.app.core.model.UserProfile>
            val previewResult: ApiResult<com.vzaimno.app.core.model.UserReviewFeed>

            coroutineScope {
                val profileDeferred = async { profileRepository.fetchMeProfile() }
                val previewDeferred = async {
                    profileRepository.fetchMyReviewsFeed(
                        limit = ProfileReviewsPreviewLimit,
                        offset = 0,
                        role = ReviewRole.Performer,
                    )
                }
                profileResult = profileDeferred.await()
                previewResult = previewDeferred.await()
            }

            val previewState = previewResult.toPreviewState()
            val currentProfile = previousState.profile

            when (profileResult) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    _uiState.value = ProfileHomeUiState(
                        isInitialLoading = false,
                        isRefreshing = false,
                        profile = profileResult.value,
                        previewState = previewState,
                    )
                }

                is ApiResult.Failure -> {
                    if (currentProfile == null) {
                        _uiState.value = ProfileHomeUiState(
                            isInitialLoading = false,
                            isRefreshing = false,
                            profile = null,
                            loadErrorMessage = profileResult.error.message,
                            previewState = ProfileReviewsPreviewUiState.Loading,
                        )
                    } else {
                        hasLoaded = true
                        _uiState.value = previousState.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            loadErrorMessage = null,
                            contentMessage = profileResult.error.message,
                            previewState = previewState,
                        )
                    }
                }
            }
        }
    }
}

private fun ApiResult<com.vzaimno.app.core.model.UserReviewFeed>.toPreviewState(): ProfileReviewsPreviewUiState =
    when (this) {
        is ApiResult.Success -> {
            if (value.reviews.isEmpty()) {
                ProfileReviewsPreviewUiState.Empty
            } else {
                ProfileReviewsPreviewUiState.Content(
                    summary = value.summary,
                    reviews = value.reviews,
                )
            }
        }

        is ApiResult.Failure -> ProfileReviewsPreviewUiState.Error(error.message)
    }
