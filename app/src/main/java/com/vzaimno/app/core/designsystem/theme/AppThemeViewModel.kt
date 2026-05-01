package com.vzaimno.app.core.designsystem.theme

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val appThemeRepository: AppThemeRepository,
) : ViewModel() {

    val themeState: StateFlow<AppThemeState> = appThemeRepository.themeState

    fun setDarkTheme(enabled: Boolean) {
        appThemeRepository.setDarkTheme(enabled)
    }
}
