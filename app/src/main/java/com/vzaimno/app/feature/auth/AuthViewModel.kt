package com.vzaimno.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.connectivity.ConnectivityMonitor
import com.vzaimno.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectivityMonitor.isOnline.collect { isOnline ->
                _uiState.update { state ->
                    state.copy(isOffline = !isOnline)
                }
            }
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                email = value,
                emailError = null,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                password = value,
                passwordError = null,
                confirmPasswordError = if (state.mode == AuthMode.Register) null else state.confirmPasswordError,
                errorMessage = null,
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = value,
                confirmPasswordError = null,
                errorMessage = null,
            )
        }
    }

    fun onToggleMode() {
        _uiState.update { state ->
            state.copy(
                mode = if (state.mode == AuthMode.Login) AuthMode.Register else AuthMode.Login,
                password = "",
                confirmPassword = "",
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null,
            )
        }
    }

    fun submit() {
        val currentState = _uiState.value
        if (currentState.isSubmitting) return

        val validation = AuthFormValidator.validate(
            mode = currentState.mode,
            email = currentState.email,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword,
        )
        if (validation.hasErrors) {
            _uiState.update { state ->
                state.copy(
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                    confirmPasswordError = validation.confirmPasswordError,
                    errorMessage = null,
                )
            }
            return
        }

        if (currentState.isOffline) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Нет подключения к интернету. Подключитесь к сети и попробуйте снова.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSubmitting = true,
                    errorMessage = null,
                )
            }

            val trimmedEmail = currentState.email.trim()
            val trimmedPassword = currentState.password.trim()

            val errorMessage = when (currentState.mode) {
                AuthMode.Login -> sessionManager.login(
                    credentials = com.vzaimno.app.core.model.LoginCredentials(
                        email = trimmedEmail,
                        password = trimmedPassword,
                    ),
                )

                AuthMode.Register -> sessionManager.register(
                    credentials = com.vzaimno.app.core.model.RegisterCredentials(
                        email = trimmedEmail,
                        password = trimmedPassword,
                    ),
                )
            }

            _uiState.update { state ->
                state.copy(
                    isSubmitting = false,
                    errorMessage = errorMessage,
                )
            }
        }
    }
}
