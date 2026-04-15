package com.vzaimno.app.feature.route

import androidx.compose.runtime.Immutable
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.RouteTravelMode

enum class RouteRole {
    Performer,
    Customer,
}

enum class RouteRoleContentStatus {
    Loading,
    Content,
    Empty,
}

enum class RouteTaskKind {
    Primary,
    AcceptedExtra,
    Preview,
    CustomerObserved,
}

enum class RouteChipTone {
    Accent,
    Neutral,
    Positive,
    Warning,
    Danger,
}

enum class RouteStage {
    Accepted,
    EnRoute,
    OnSite,
    InProgress,
    Handoff,
    Completed,
}

enum class RouteStageProgressState {
    Done,
    Current,
    Pending,
}

enum class RouteMapGeometryQuality {
    Exact,
    Approximate,
    PointsOnly,
}

enum class RoutePolylineKind {
    Main,
    Preview,
}

enum class RouteMarkerKind {
    Start,
    PrimaryTask,
    AcceptedExtra,
    Preview,
    CustomerTask,
}

@Immutable
data class RouteRoleOptionUi(
    val role: RouteRole,
    val title: String,
    val subtitle: String,
)

@Immutable
data class RouteRoleSelectionUiState(
    val selectedRole: RouteRole = RouteRole.Performer,
    val options: List<RouteRoleOptionUi> = listOf(
        RouteRoleOptionUi(
            role = RouteRole.Performer,
            title = "Исполнитель",
            subtitle = "Текущий маршрут и задачи по пути",
        ),
        RouteRoleOptionUi(
            role = RouteRole.Customer,
            title = "Заказчик",
            subtitle = "Только разрешённая видимость",
        ),
    ),
)

@Immutable
data class RouteMetricUi(
    val id: String,
    val label: String,
    val value: String,
)

@Immutable
data class RouteStatusChipUi(
    val label: String,
    val tone: RouteChipTone,
)

@Immutable
data class RouteStageChipUi(
    val stage: RouteStage,
    val label: String,
    val state: RouteStageProgressState,
)

@Immutable
data class RouteTaskCardUi(
    val id: String,
    val announcementId: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val addressText: String?,
    val priceText: String?,
    val kind: RouteTaskKind,
    val kindLabel: String,
    val statusChip: RouteStatusChipUi?,
    val stageChips: List<RouteStageChipUi>,
    val nextStage: RouteStage?,
    val nextStageLabel: String?,
    val detourText: String?,
    val previewSummary: String?,
    val coordinate: GeoPoint?,
    val canOpenDetails: Boolean,
    val canAdvanceStage: Boolean,
    val canAcceptToRoute: Boolean,
    val canRemoveFromRoute: Boolean,
    val isSelected: Boolean,
)

@Immutable
data class RouteNextStepUi(
    val title: String,
    val body: String,
    val actionLabel: String? = null,
    val taskId: String? = null,
)

@Immutable
data class PerformerRouteUiState(
    val status: RouteRoleContentStatus = RouteRoleContentStatus.Loading,
    val summary: String = "",
    val metrics: List<RouteMetricUi> = emptyList(),
    val activeTasks: List<RouteTaskCardUi> = emptyList(),
    val previewTasks: List<RouteTaskCardUi> = emptyList(),
    val nextStep: RouteNextStepUi? = null,
    val emptyTitle: String = "",
    val emptyMessage: String = "",
)

@Immutable
data class CustomerRouteUiState(
    val status: RouteRoleContentStatus = RouteRoleContentStatus.Loading,
    val summary: String = "",
    val metrics: List<RouteMetricUi> = emptyList(),
    val tasks: List<RouteTaskCardUi> = emptyList(),
    val emptyTitle: String = "",
    val emptyMessage: String = "",
)

@Immutable
data class RouteMapMarkerUi(
    val id: String,
    val title: String,
    val subtitle: String?,
    val point: GeoPoint,
    val kind: RouteMarkerKind,
)

@Immutable
data class RouteMapPolylineUi(
    val id: String,
    val points: List<GeoPoint>,
    val kind: RoutePolylineKind,
)

sealed interface RouteMapCommand {
    val token: Long

    data class FitAll(
        override val token: Long,
    ) : RouteMapCommand

    data class FocusPoint(
        override val token: Long,
        val point: GeoPoint,
        val zoom: Float = 14f,
    ) : RouteMapCommand

    data class ZoomIn(
        override val token: Long,
    ) : RouteMapCommand

    data class ZoomOut(
        override val token: Long,
    ) : RouteMapCommand
}

@Immutable
data class RouteExternalNavigationUi(
    val origin: GeoPoint?,
    val destination: GeoPoint,
    val waypoints: List<GeoPoint>,
    val travelMode: RouteTravelMode,
)

@Immutable
data class RouteMapUiState(
    val isConfigured: Boolean = false,
    val isLoading: Boolean = false,
    val geometryQuality: RouteMapGeometryQuality = RouteMapGeometryQuality.Exact,
    val markers: List<RouteMapMarkerUi> = emptyList(),
    val polylines: List<RouteMapPolylineUi> = emptyList(),
    val selectedMarkerId: String? = null,
    val note: String? = null,
    val overlayTitle: String? = null,
    val overlayMessage: String? = null,
    val command: RouteMapCommand? = null,
    val externalNavigation: RouteExternalNavigationUi? = null,
)

@Immutable
data class RouteExecutionActionUiState(
    val rebuildingRoute: Boolean = false,
    val updatingStageTaskId: String? = null,
    val acceptedExtraCount: Int = 0,
    val acceptedExtraLimit: Int = 2,
)

@Immutable
data class RouteLoadingUiState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val inlineMessage: String? = null,
)

@Immutable
data class RouteUiState(
    val roleSelection: RouteRoleSelectionUiState = RouteRoleSelectionUiState(),
    val loading: RouteLoadingUiState = RouteLoadingUiState(),
    val performer: PerformerRouteUiState = PerformerRouteUiState(),
    val customer: CustomerRouteUiState = CustomerRouteUiState(),
    val map: RouteMapUiState = RouteMapUiState(),
    val actions: RouteExecutionActionUiState = RouteExecutionActionUiState(),
)
