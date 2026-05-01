package com.vzaimno.app.core.designsystem.theme

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppThemeState(
    val isDarkTheme: Boolean,
)

@Singleton
class AppThemeRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val defaultDarkTheme = context.resources.configuration.uiMode
        .and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private val _themeState = MutableStateFlow(
        AppThemeState(
            isDarkTheme = preferences.getBoolean(KeyDarkTheme, defaultDarkTheme),
        ),
    )
    val themeState: StateFlow<AppThemeState> = _themeState.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KeyDarkTheme, enabled)
            .apply()
        _themeState.value = AppThemeState(isDarkTheme = enabled)
    }

    private companion object {
        const val PreferencesName = "vzaimno_theme"
        const val KeyDarkTheme = "dark_theme"
    }
}
