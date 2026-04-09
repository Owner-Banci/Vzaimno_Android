package com.vzaimno.app.feature.profile

import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.core.model.ReviewSummary
import com.vzaimno.app.core.model.UserProfile
import com.vzaimno.app.core.model.UserProfileReview

const val ProfileBioMaxLength: Int = 300
const val ProfileAddressMaxLength: Int = 180
const val ProfileReviewsPreviewLimit: Int = 2
const val ProfileReviewsPageSize: Int = 50

data class ProfileHomeUiState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val profile: UserProfile? = null,
    val loadErrorMessage: String? = null,
    val contentMessage: String? = null,
    val previewState: ProfileReviewsPreviewUiState = ProfileReviewsPreviewUiState.Loading,
)

sealed interface ProfileReviewsPreviewUiState {
    data object Loading : ProfileReviewsPreviewUiState
    data object Empty : ProfileReviewsPreviewUiState
    data class Error(val message: String) : ProfileReviewsPreviewUiState
    data class Content(
        val summary: ReviewSummary,
        val reviews: List<UserProfileReview>,
    ) : ProfileReviewsPreviewUiState
}

data class ProfileEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val loadErrorMessage: String? = null,
    val formErrorMessage: String? = null,
    val displayName: String = "",
    val bio: String = "",
    val city: String = "",
    val preferredAddress: String = "",
    val displayNameError: String? = null,
    val bioError: String? = null,
    val preferredAddressError: String? = null,
)

data class ProfileReviewsUiState(
    val selectedRole: ReviewRole = ReviewRole.Performer,
    val screenState: ProfileReviewsScreenState = ProfileReviewsScreenState.Loading,
    val isRefreshing: Boolean = false,
    val summary: ReviewSummary = ReviewSummary.Empty,
    val reviews: List<UserProfileReview> = emptyList(),
    val contentMessage: String? = null,
)

sealed interface ProfileReviewsScreenState {
    data object Idle : ProfileReviewsScreenState
    data object Loading : ProfileReviewsScreenState
    data object Loaded : ProfileReviewsScreenState
    data object Empty : ProfileReviewsScreenState
    data class Error(val message: String) : ProfileReviewsScreenState
}
