package com.vzaimno.app.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.R
import com.vzaimno.app.core.model.EditableProfileFields
import com.vzaimno.app.core.model.UserProfile
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private var hasLoaded = false
    private var loadJob: Job? = null
    private var baseProfile: UserProfile? = null

    fun loadIfNeeded() {
        if (hasLoaded || loadJob?.isActive == true) return
        loadProfile()
    }

    fun retry() {
        loadProfile()
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                displayName = value,
                displayNameError = null,
                formErrorMessage = null,
            )
        }
    }

    fun onBioChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                bio = value,
                bioError = null,
                formErrorMessage = null,
            )
        }
    }

    fun onCityChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                city = value,
                formErrorMessage = null,
            )
        }
    }

    fun onPreferredAddressChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                preferredAddress = value,
                preferredAddressError = null,
                formErrorMessage = null,
            )
        }
    }

    suspend fun save(): Boolean {
        val currentState = _uiState.value
        val currentProfile = baseProfile ?: return false
        if (currentState.isLoading || currentState.isSaving) return false

        val draft = buildValidatedDraft(currentState, currentProfile) ?: return false
        _uiState.update { state ->
            state.copy(
                isSaving = true,
                formErrorMessage = null,
            )
        }

        return when (val result = profileRepository.updateMyProfile(draft)) {
            is ApiResult.Success -> {
                val updatedProfile = currentProfile.applyEditableFields(result.value)
                baseProfile = updatedProfile
                _uiState.value = updatedProfile.toEditUiState()
                hasLoaded = true
                true
            }

            is ApiResult.Failure -> {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        formErrorMessage = result.error.message,
                    )
                }
                false
            }
        }
    }

    private fun loadProfile() {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    loadErrorMessage = null,
                    formErrorMessage = null,
                )
            }

            when (val result = profileRepository.fetchMeProfile()) {
                is ApiResult.Success -> {
                    baseProfile = result.value
                    hasLoaded = true
                    _uiState.value = result.value.toEditUiState()
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            loadErrorMessage = result.error.message,
                        )
                    }
                }
            }
        }
    }

    private fun buildValidatedDraft(
        state: ProfileEditUiState,
        profile: UserProfile,
    ): EditableProfileFields? {
        val trimmedDisplayName = state.displayName.trim()
        val trimmedBio = state.bio.trim()
        val trimmedCity = state.city.trim()
        val trimmedPreferredAddress = state.preferredAddress.trim()

        val displayNameError = if (trimmedDisplayName.length < 2) {
            context.getString(R.string.profile_edit_validation_name)
        } else {
            null
        }
        val bioError = if (trimmedBio.length > ProfileBioMaxLength) {
            context.getString(
                R.string.profile_edit_validation_bio,
                ProfileBioMaxLength,
            )
        } else {
            null
        }
        val preferredAddressError = if (trimmedPreferredAddress.length > ProfileAddressMaxLength) {
            context.getString(
                R.string.profile_edit_validation_address,
                ProfileAddressMaxLength,
            )
        } else {
            null
        }

        if (displayNameError != null || bioError != null || preferredAddressError != null) {
            _uiState.update { current ->
                current.copy(
                    displayNameError = displayNameError,
                    bioError = bioError,
                    preferredAddressError = preferredAddressError,
                )
            }
            return null
        }

        return EditableProfileFields(
            displayName = trimmedDisplayName,
            bio = trimmedBio,
            city = trimmedCity,
            preferredAddress = trimmedPreferredAddress,
            homeLocation = profile.homeLocation,
        )
    }
}

private fun UserProfile.toEditUiState(): ProfileEditUiState {
    val editableFields = editableFields
    return ProfileEditUiState(
        isLoading = false,
        isSaving = false,
        profile = this,
        loadErrorMessage = null,
        formErrorMessage = null,
        displayName = editableFields.displayName,
        bio = editableFields.bio,
        city = editableFields.city,
        preferredAddress = editableFields.preferredAddress,
        displayNameError = null,
        bioError = null,
        preferredAddressError = null,
    )
}

private fun UserProfile.applyEditableFields(fields: EditableProfileFields): UserProfile = copy(
    displayName = fields.displayName,
    bio = fields.bio,
    city = fields.city,
    preferredAddress = fields.preferredAddress,
    homeLocation = fields.homeLocation ?: homeLocation,
)
