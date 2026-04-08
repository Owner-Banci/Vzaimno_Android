package com.vzaimno.app.feature.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.connectivity.ConnectivityMonitor
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RootUiState(
    val sessionState: SessionState = SessionState.Restoring,
    val isLoggingOut: Boolean = false,
    val isOnline: Boolean = true,
)

@HiltViewModel
class RootViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val logoutInProgress = MutableStateFlow(false)

    val uiState: StateFlow<RootUiState> = combine(
        sessionManager.sessionState,
        logoutInProgress,
        connectivityMonitor.isOnline,
    ) { sessionState, isLoggingOut, isOnline ->
        RootUiState(
            sessionState = sessionState,
            isLoggingOut = isLoggingOut,
            isOnline = isOnline,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RootUiState(),
    )

    init {
        viewModelScope.launch {
            sessionManager.restoreSession()
        }
    }

    fun retryRestore() {
        viewModelScope.launch {
            sessionManager.restoreSession(force = true)
        }
    }

    fun logout() {
        if (logoutInProgress.value) return

        viewModelScope.launch {
            logoutInProgress.value = true
            try {
                sessionManager.logout()
            } finally {
                logoutInProgress.value = false
            }
        }
    }

    fun clearSession() {
        sessionManager.clearSession()
    }
}
