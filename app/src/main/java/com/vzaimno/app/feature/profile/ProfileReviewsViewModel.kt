package com.vzaimno.app.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.core.model.ReviewSummary
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileReviewsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileReviewsUiState(
            selectedRole = ProfileDestination.roleFromArgument(
                savedStateHandle.get<String>(ProfileDestination.roleArgumentName),
            ),
        ),
    )
    val uiState: StateFlow<ProfileReviewsUiState> = _uiState.asStateFlow()

    private var hasLoadedCurrentRole = false
    private var loadJob: Job? = null

    fun loadIfNeeded() {
        if (hasLoadedCurrentRole || loadJob?.isActive == true) return
        load(role = _uiState.value.selectedRole, showLoadingState = true)
    }

    fun retry() {
        load(role = _uiState.value.selectedRole, showLoadingState = true)
    }

    fun refresh() {
        load(role = _uiState.value.selectedRole, showLoadingState = false)
    }

    fun selectRole(role: ReviewRole) {
        if (role == _uiState.value.selectedRole && hasLoadedCurrentRole) return
        load(role = role, showLoadingState = true)
    }

    private fun load(
        role: ReviewRole,
        showLoadingState: Boolean,
    ) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            if (showLoadingState) {
                hasLoadedCurrentRole = false
                _uiState.value = ProfileReviewsUiState(
                    selectedRole = role,
                    screenState = ProfileReviewsScreenState.Loading,
                )
            } else {
                _uiState.update { state ->
                    state.copy(
                        selectedRole = role,
                        isRefreshing = true,
                        contentMessage = null,
                    )
                }
            }

            when (
                val result = profileRepository.fetchMyReviewsFeed(
                    limit = ProfileReviewsPageSize,
                    offset = 0,
                    role = role,
                )
            ) {
                is ApiResult.Success -> {
                    hasLoadedCurrentRole = true
                    _uiState.value = ProfileReviewsUiState(
                        selectedRole = role,
                        screenState = if (result.value.reviews.isEmpty()) {
                            ProfileReviewsScreenState.Empty
                        } else {
                            ProfileReviewsScreenState.Loaded
                        },
                        isRefreshing = false,
                        summary = result.value.summary,
                        reviews = result.value.reviews,
                    )
                }

                is ApiResult.Failure -> {
                    if (showLoadingState) {
                        _uiState.value = ProfileReviewsUiState(
                            selectedRole = role,
                            screenState = ProfileReviewsScreenState.Error(result.error.message),
                            summary = ReviewSummary.Empty,
                        )
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                isRefreshing = false,
                                contentMessage = result.error.message,
                            )
                        }
                    }
                }
            }
        }
    }
}
