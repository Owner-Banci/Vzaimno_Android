package com.vzaimno.app.feature.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.data.repository.AnnouncementRepository
import com.vzaimno.app.data.repository.AuthRepository
import com.vzaimno.app.data.repository.ChatRepository
import com.vzaimno.app.data.repository.DeviceRepository
import com.vzaimno.app.data.repository.ProfileRepository
import com.vzaimno.app.data.repository.RouteRepository
import com.vzaimno.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class BootstrapStatusItem(
    val title: String,
    val summary: String,
)

data class BootstrapUiState(
    val environment: String,
    val apiBaseUrl: String,
    val webSocketBaseUrl: String,
    val authEnabled: Boolean,
    val sessionSummary: String,
    val foundationItems: List<BootstrapStatusItem>,
    val nextStageItems: List<String>,
)

@HiltViewModel
class BootstrapViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val sessionManager: SessionManager,
    authRepository: AuthRepository,
    profileRepository: ProfileRepository,
    deviceRepository: DeviceRepository,
    announcementRepository: AnnouncementRepository,
    chatRepository: ChatRepository,
    routeRepository: RouteRepository,
) : ViewModel() {

    private val repositoryLabels = listOf(
        authRepository::class.simpleName.orEmpty(),
        profileRepository::class.simpleName.orEmpty(),
        deviceRepository::class.simpleName.orEmpty(),
        announcementRepository::class.simpleName.orEmpty(),
        chatRepository::class.simpleName.orEmpty(),
        routeRepository::class.simpleName.orEmpty(),
    )

    val uiState = sessionManager.activeSession
        .map { session ->
            BootstrapUiState(
                environment = appConfig.environment.rawValue,
                apiBaseUrl = appConfig.normalizedApiBaseUrl,
                webSocketBaseUrl = appConfig.normalizedWebSocketBaseUrl,
                authEnabled = appConfig.authEnabled,
                sessionSummary = when {
                    !session.authEnabled -> "Auth bypass prepared for local development"
                    session.isAuthenticated -> "Session manager is ready and can provide bearer token"
                    else -> "Session manager is ready; no token is stored yet"
                },
                foundationItems = listOf(
                    BootstrapStatusItem(
                        title = "Config",
                        summary = "BuildConfig-backed environment, API and WebSocket URLs",
                    ),
                    BootstrapStatusItem(
                        title = "Network",
                        summary = "Retrofit + OkHttp + kotlinx.serialization + unified API error mapping",
                    ),
                    BootstrapStatusItem(
                        title = "Data models",
                        summary = "DTO/domain/mappers for auth, profile, announcement, offer, chat and route",
                    ),
                    BootstrapStatusItem(
                        title = "Task compatibility",
                        summary = "Nested task payload + legacy flat key fallback + visibility projection",
                    ),
                    BootstrapStatusItem(
                        title = "Repositories",
                        summary = repositoryLabels.joinToString(separator = " • "),
                    ),
                    BootstrapStatusItem(
                        title = "Design system",
                        summary = "Material 3 theme with brand-aligned turquoise/milk/peach palette",
                    ),
                ),
                nextStageItems = listOf(
                    "Auth flow and token persistence",
                    "Profile screens",
                    "Announcements and offers UI",
                    "Map and route experience",
                    "Chats and support threads",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BootstrapUiState(
                environment = appConfig.environment.rawValue,
                apiBaseUrl = appConfig.normalizedApiBaseUrl,
                webSocketBaseUrl = appConfig.normalizedWebSocketBaseUrl,
                authEnabled = appConfig.authEnabled,
                sessionSummary = "Initializing foundation",
                foundationItems = emptyList(),
                nextStageItems = emptyList(),
            ),
        )
}
