package com.vzaimno.app.feature.route

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.BuildConfig
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.RouteContext
import com.vzaimno.app.core.model.RouteDetails
import com.vzaimno.app.core.model.RouteTravelMode
import com.vzaimno.app.core.model.TaskByRoute
import com.vzaimno.app.core.model.createdAtEpochSeconds
import com.vzaimno.app.core.model.destinationPoint
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.shortStructuredSubtitle
import com.vzaimno.app.core.model.sourcePoint
import com.vzaimno.app.core.model.structuredData
import com.vzaimno.app.core.model.taskStateProjection
import com.vzaimno.app.core.network.ApiError
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.AnnouncementRepository
import com.vzaimno.app.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RouteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routeRepository: RouteRepository,
    private val announcementRepository: AnnouncementRepository,
) : ViewModel() {

    private val isMapConfigured = BuildConfig.YANDEX_MAPKIT_API_KEY.isNotBlank()
    private val savedStateHandle = savedStateHandle

    private val _uiState = MutableStateFlow(
        RouteUiState(
            roleSelection = RouteRoleSelectionUiState(
                selectedRole = savedStateHandle.restoreRouteRole(),
            ),
            loading = RouteLoadingUiState(
                isInitialLoading = true,
                isRefreshing = false,
            ),
            map = RouteMapUiState(
                isConfigured = isMapConfigured,
            ),
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var selectedRole: RouteRole = savedStateHandle.restoreRouteRole()
    private val announcementCache = linkedMapOf<String, Announcement>()

    private var hasLoadedPerformer = false
    private var hasLoadedCustomer = false

    private var performerContext: RouteContext? = null
    private var performerBaseDetails: RouteDetails? = null
    private var performerBaseFallbackPlan: ApproximateRoutePlan? = null
    private var performerBaseGeometryQuality: RouteMapGeometryQuality = RouteMapGeometryQuality.PointsOnly
    private var performerBaseNote: String? = null
    private var performerDisplayedDetails: RouteDetails? = null
    private var performerDisplayedFallbackPlan: ApproximateRoutePlan? = null
    private var performerDisplayedGeometryQuality: RouteMapGeometryQuality = RouteMapGeometryQuality.PointsOnly
    private var performerDisplayedNote: String? = null
    private var performerEmptyTitle: String = "Активного маршрута пока нет"
    private var performerEmptyMessage: String =
        "Когда появится принятое задание, здесь соберутся маршрут, остановки и задачи по пути."

    private var customerAnnouncements: List<Announcement> = emptyList()
    private val customerRouteContexts = mutableMapOf<String, RouteContext?>()
    private val customerRouteDetails = mutableMapOf<String, RouteDetails?>()
    private val customerFallbackPlans = mutableMapOf<String, ApproximateRoutePlan?>()
    private val customerGeometryQualities = mutableMapOf<String, RouteMapGeometryQuality>()
    private val customerRouteNotes = mutableMapOf<String, String?>()
    private var customerEmptyTitle: String = "У заказчика пока нет видимого маршрута"
    private var customerEmptyMessage: String =
        "Маршрут появится здесь только после того, как задача перейдёт в выполнение."
    private var loadingCustomerRouteId: String? = null

    private var acceptedExtraTaskIds: List<String> = emptyList()
    private var selectedPerformerTaskId: String? = savedStateHandle[PERFORMER_SELECTED_TASK_KEY]
    private var selectedCustomerTaskId: String? = savedStateHandle[CUSTOMER_SELECTED_TASK_KEY]

    private var loadJob: Job? = null
    private var foregroundRefreshJob: Job? = null
    private var mapCommandToken: Long = 0L
    private var currentMapCommand: RouteMapCommand = RouteMapCommand.FitAll(token = mapCommandToken)
    private var lastLoadElapsedRealtime: Long = 0L

    fun loadIfNeeded() {
        if (loadJob?.isActive == true) return

        when (selectedRole) {
            RouteRole.Performer -> if (!hasLoadedPerformer) {
                load(showLoadingState = performerSelectableTaskIds().isEmpty())
            }

            RouteRole.Customer -> if (!hasLoadedCustomer) {
                load(showLoadingState = customerSelectableTaskIds().isEmpty())
            }
        }
    }

    fun retry() {
        load(showLoadingState = activeRoleHasNoVisibleContent())
    }

    fun refresh() {
        load(showLoadingState = false)
    }

    fun clearInlineMessage() {
        _uiState.value = _uiState.value.copy(
            loading = _uiState.value.loading.copy(inlineMessage = null),
        )
    }

    fun switchRole(role: RouteRole) {
        if (selectedRole == role) return

        selectedRole = role
        savedStateHandle[SELECTED_ROLE_KEY] = role.name
        if (role == RouteRole.Customer) {
            ensureSelections()
            reduceUiState()
            if (!hasLoadedCustomer) {
                load(showLoadingState = customerSelectableTaskIds().isEmpty())
            } else {
                selectedCustomerTaskId?.let { taskId ->
                    issueFocusForTask(taskId)
                    viewModelScope.launch {
                        ensureCustomerRouteLoaded(taskId, force = false)
                    }
                }
            }
        } else {
            ensureSelections()
            reduceUiState()
            if (!hasLoadedPerformer) {
                load(showLoadingState = performerSelectableTaskIds().isEmpty())
            } else {
                selectedPerformerTaskId?.let(::issueFocusForTask)
            }
        }
    }

    fun startForegroundRefresh() {
        if (foregroundRefreshJob?.isActive == true) return

        foregroundRefreshJob = viewModelScope.launch {
            maybeRefreshForForeground()
            while (true) {
                delay(nextRefreshIntervalMillis())
                if (loadJob?.isActive == true) continue
                load(showLoadingState = false)
            }
        }
    }

    fun stopForegroundRefresh() {
        foregroundRefreshJob?.cancel()
        foregroundRefreshJob = null
    }

    fun selectTask(taskId: String) {
        when (selectedRole) {
            RouteRole.Performer -> {
                selectedPerformerTaskId = taskId
                savedStateHandle[PERFORMER_SELECTED_TASK_KEY] = taskId
            }

            RouteRole.Customer -> {
                selectedCustomerTaskId = taskId
                savedStateHandle[CUSTOMER_SELECTED_TASK_KEY] = taskId
                viewModelScope.launch {
                    ensureCustomerRouteLoaded(taskId, force = false)
                }
            }
        }

        issueFocusForTask(taskId)
        reduceUiState()
    }

    fun acceptPreviewTask(taskId: String) {
        if (acceptedExtraTaskIds.contains(taskId)) return
        if (acceptedExtraTaskIds.size >= MAX_ACCEPTED_EXTRA_TASKS) {
            emitMessage("Можно принять не больше двух дополнительных задач по пути.")
            return
        }

        acceptedExtraTaskIds = acceptedExtraTaskIds + taskId
        viewModelScope.launch {
            refreshAnnouncements(setOf(taskId))
            rebuildPerformerRoute(
                announceSuccess = "Задача добавлена в текущий маршрут.",
            )
            selectedPerformerTaskId = taskId
            savedStateHandle[PERFORMER_SELECTED_TASK_KEY] = taskId
            issueFocusForTask(taskId)
            reduceUiState()
        }
    }

    fun removeAcceptedTask(taskId: String) {
        if (!acceptedExtraTaskIds.contains(taskId)) return

        acceptedExtraTaskIds = acceptedExtraTaskIds - taskId
        viewModelScope.launch {
            if (acceptedExtraTaskIds.isEmpty()) {
                restoreBasePerformerRoute()
                emitMessage("Дополнительная задача убрана из маршрута.")
                ensureSelections()
                issueFitMap()
                reduceUiState()
            } else {
                rebuildPerformerRoute(
                    announceSuccess = "Маршрут обновлён после удаления задачи.",
                )
            }
        }
    }

    fun advanceTaskStage(taskId: String) {
        val taskAnnouncement = announcementCache[taskId]
        val currentStage = taskAnnouncement?.let(::routeStageFromAnnouncement)
            ?: performerTaskById(taskId)?.status?.let(::fallbackRouteStage)
        val targetStage = nextRouteStage(currentStage)

        if (targetStage == null) {
            emitMessage("Все этапы уже отмечены.")
            return
        }

        _uiState.value = _uiState.value.copy(
            actions = _uiState.value.actions.copy(
                updatingStageTaskId = taskId,
            ),
        )

        viewModelScope.launch {
            when (val result = announcementRepository.updateExecutionStage(taskId, targetStage.apiValue())) {
                is ApiResult.Success -> {
                    mergeAnnouncement(result.value)
                    if (targetStage == RouteStage.Completed) {
                        if (taskId == performerContext?.entityId) {
                            loadPerformerData()
                        } else {
                            acceptedExtraTaskIds = acceptedExtraTaskIds - taskId
                            rebuildPerformerRoute(announceSuccess = null)
                        }
                    }
                    emitMessage("Этап обновлён: ${stageLabel(targetStage)}.")
                }

                is ApiResult.Failure -> {
                    emitMessage(
                        when {
                            result.error.statusCode == 409 -> "Этапы нужно отмечать по порядку."
                            else -> result.error.message
                        },
                    )
                }
            }

            _uiState.value = _uiState.value.copy(
                actions = _uiState.value.actions.copy(
                    updatingStageTaskId = null,
                ),
            )
            ensureSelections()
            reduceUiState()
        }
    }

    fun zoomMapIn() {
        mapCommandToken += 1
        currentMapCommand = RouteMapCommand.ZoomIn(token = mapCommandToken)
        reduceUiState()
    }

    fun zoomMapOut() {
        mapCommandToken += 1
        currentMapCommand = RouteMapCommand.ZoomOut(token = mapCommandToken)
        reduceUiState()
    }

    fun focusMapOnUserStub() {
        val point = when (selectedRole) {
            RouteRole.Performer -> performerContext?.start
                ?: performerActiveTasks().firstOrNull { it.isSelected }?.coordinate
                ?: performerActiveTasks().firstOrNull()?.coordinate
                ?: performerPreviewTasks().firstOrNull { it.isSelected }?.coordinate
                ?: performerPreviewTasks().firstOrNull()?.coordinate

            RouteRole.Customer -> {
                val selectedId = selectedCustomerTaskId
                customerRouteContexts[selectedId]?.start
                    ?: customerTasks().firstOrNull { it.isSelected }?.coordinate
                    ?: customerTasks().firstOrNull()?.coordinate
            }
        }

        if (point != null) {
            mapCommandToken += 1
            currentMapCommand = RouteMapCommand.FocusPoint(
                token = mapCommandToken,
                point = point,
                zoom = 14.4f,
            )
        } else {
            issueFitMap()
        }
        reduceUiState()
    }

    private fun load(showLoadingState: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loading = _uiState.value.loading.copy(
                    isInitialLoading = showLoadingState && activeRoleHasNoVisibleContent(),
                    isRefreshing = !showLoadingState,
                    inlineMessage = null,
                ),
            )

            when (selectedRole) {
                RouteRole.Performer -> loadPerformerData()
                RouteRole.Customer -> loadCustomerData(forceSelectedRouteRefresh = true)
            }

            lastLoadElapsedRealtime = SystemClock.elapsedRealtime()
            _uiState.value = _uiState.value.copy(
                loading = _uiState.value.loading.copy(
                    isInitialLoading = false,
                    isRefreshing = false,
                ),
            )
            reduceUiState()
        }
    }

    private suspend fun maybeRefreshForForeground() {
        val now = SystemClock.elapsedRealtime()
        val isStale = now - lastLoadElapsedRealtime > FOREGROUND_STALE_AFTER_MS
        if (!isStale && lastLoadElapsedRealtime > 0L) return
        if (loadJob?.isActive == true) return
        load(showLoadingState = false)
    }

    private suspend fun loadPerformerData() {
        val previousRouteId = performerContext?.entityId
        val contextResult: ApiResult<RouteContext>
        val detailsResult: ApiResult<RouteDetails>

        coroutineScope {
            val contextDeferred = async { routeRepository.fetchMyCurrentRouteContext() }
            val detailsDeferred = async { routeRepository.fetchMyCurrentRoute() }
            contextResult = contextDeferred.await()
            detailsResult = detailsDeferred.await()
        }

        val context = contextResult.successOrNull()
        var details = detailsResult.successOrNull()
        var baseFallbackPlan: ApproximateRoutePlan? = null
        var baseQuality = RouteMapGeometryQuality.Exact
        var baseNote: String? = null
        var inlineMessage: String? = null

        if (context == null && details == null) {
            acceptedExtraTaskIds = emptyList()
            performerContext = null
            performerBaseDetails = null
            performerBaseFallbackPlan = null
            performerBaseGeometryQuality = RouteMapGeometryQuality.PointsOnly
            performerBaseNote = null
            performerDisplayedDetails = null
            performerDisplayedFallbackPlan = null
            performerDisplayedGeometryQuality = RouteMapGeometryQuality.PointsOnly
            performerDisplayedNote = null
            performerEmptyTitle = "Активного маршрута пока нет"
            performerEmptyMessage = when {
                contextResult.errorOrNull()?.statusCode == 404 || detailsResult.errorOrNull()?.statusCode == 404 -> {
                    "Когда вы примете активное задание, маршрут появится здесь автоматически."
                }

                else -> {
                    contextResult.errorOrNull()?.message
                        ?: detailsResult.errorOrNull()?.message
                        ?: "Не удалось загрузить маршрут исполнителя."
                }
            }
            hasLoadedPerformer = true
            reduceUiState()
            return
        }

        performerContext = context
        if (previousRouteId != null && previousRouteId != context?.entityId) {
            acceptedExtraTaskIds = emptyList()
        }

        if (details == null && context != null) {
            baseFallbackPlan = buildApproximateRoute(
                start = context.start,
                waypoints = emptyList(),
                end = context.end,
                travelMode = context.travelMode,
            )
            baseQuality = baseFallbackPlan.quality
            baseNote = "Точная геометрия недоступна: показываем приближённую схему между стартом и финишем."

            when (val rebuildResult = routeRepository.buildRoute(baseFallbackPlan.toBuildRequest(context))) {
                is ApiResult.Success -> {
                    details = rebuildResult.value
                }

                is ApiResult.Failure -> {
                    inlineMessage = if (detailsResult.errorOrNull()?.statusCode == 404) {
                        null
                    } else {
                        rebuildResult.error.message
                    }
                }
            }
        } else if (details != null && details.polyline.size < 2 && context != null) {
            baseFallbackPlan = buildApproximateRoute(
                start = context.start,
                waypoints = emptyList(),
                end = context.end,
                travelMode = context.travelMode,
            )
            baseQuality = RouteMapGeometryQuality.PointsOnly
            baseNote = "Точная геометрия пока недоступна: карта сфокусирована на ключевых точках."
        }

        performerEmptyTitle = "Маршрут готов к работе"
        performerEmptyMessage = "Основной маршрут, текущие остановки и задачи по пути собраны на одном экране."

        performerBaseDetails = details
        performerBaseFallbackPlan = baseFallbackPlan
        performerBaseGeometryQuality = when {
            details?.polyline?.size ?: 0 >= 2 && baseFallbackPlan == null -> RouteMapGeometryQuality.Exact
            baseFallbackPlan != null -> baseQuality
            else -> RouteMapGeometryQuality.PointsOnly
        }
        performerBaseNote = baseNote
        restoreBasePerformerRoute()

        val availablePreviewTaskIds = details?.tasksByRoute.orEmpty().map(TaskByRoute::id).toSet()
        acceptedExtraTaskIds = acceptedExtraTaskIds.filter { availablePreviewTaskIds.contains(it) }

        refreshAnnouncements(
            buildSet {
                context?.entityId?.let(::add)
                addAll(acceptedExtraTaskIds)
            },
        )

        if (!inlineMessage.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                loading = _uiState.value.loading.copy(
                    inlineMessage = inlineMessage,
                ),
            )
        }

        hasLoadedPerformer = true
        ensureSelections()
        if (performerSelectableTaskIds().isNotEmpty()) {
            issueFitMap()
        }
    }

    private suspend fun loadCustomerData(forceSelectedRouteRefresh: Boolean) {
        when (val result = announcementRepository.fetchMyAnnouncements()) {
            is ApiResult.Success -> {
                customerAnnouncements = visibleCustomerAnnouncements(result.value)
                result.value.forEach(::mergeAnnouncement)
                customerEmptyTitle = "У заказчика пока нет видимого маршрута"
                customerEmptyMessage = if (customerAnnouncements.isEmpty()) {
                    "Маршрут появляется здесь только после того, как задача реально перешла в исполнение."
                } else {
                    "Выберите задачу, чтобы увидеть разрешённый маршрут без чужих веток и лишних деталей."
                }
                hasLoadedCustomer = true
                ensureSelections()
                reduceUiState()
                selectedCustomerTaskId?.let { taskId ->
                    ensureCustomerRouteLoaded(taskId, force = forceSelectedRouteRefresh)
                }
            }

            is ApiResult.Failure -> {
                hasLoadedCustomer = true
                if (customerAnnouncements.isEmpty()) {
                    customerEmptyTitle = "Не удалось загрузить режим заказчика"
                    customerEmptyMessage = result.error.message
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = _uiState.value.loading.copy(
                            inlineMessage = result.error.message,
                        ),
                    )
                }
                reduceUiState()
            }
        }
    }

    private suspend fun ensureCustomerRouteLoaded(
        announcementId: String,
        force: Boolean,
    ) {
        if (!force && customerRouteDetails.containsKey(announcementId) && customerFallbackPlans.containsKey(announcementId)) {
            return
        }
        if (loadingCustomerRouteId == announcementId) return

        val announcement = customerAnnouncements.firstOrNull { it.id == announcementId } ?: return

        loadingCustomerRouteId = announcementId
        reduceUiState()

        val contextResult: ApiResult<RouteContext>
        val detailsResult: ApiResult<RouteDetails>

        coroutineScope {
            val contextDeferred = async { routeRepository.fetchRouteContext(announcementId) }
            val detailsDeferred = async { routeRepository.fetchAnnouncementRoute(announcementId) }
            contextResult = contextDeferred.await()
            detailsResult = detailsDeferred.await()
        }

        val context = contextResult.successOrNull()
        val details = detailsResult.successOrNull()
        val start = context?.start ?: announcement.sourcePoint
        val end = context?.end ?: announcement.destinationPoint ?: announcement.sourcePoint
        val inferredTravelMode = context?.travelMode ?: inferTravelMode(announcement)
        val fallbackPlan = if (start != null && end != null && details == null) {
            buildApproximateRoute(
                start = start,
                waypoints = emptyList(),
                end = end,
                travelMode = inferredTravelMode,
            )
        } else if (start != null && end != null && details?.polyline.orEmpty().size < 2) {
            buildApproximateRoute(
                start = start,
                waypoints = emptyList(),
                end = end,
                travelMode = inferredTravelMode,
            )
        } else {
            null
        }

        customerRouteContexts[announcementId] = context
        customerRouteDetails[announcementId] = details
        customerFallbackPlans[announcementId] = fallbackPlan
        customerGeometryQualities[announcementId] = when {
            details?.polyline?.size ?: 0 >= 2 -> RouteMapGeometryQuality.Exact
            fallbackPlan != null && details != null -> RouteMapGeometryQuality.PointsOnly
            fallbackPlan != null -> fallbackPlan.quality
            else -> RouteMapGeometryQuality.PointsOnly
        }
        customerRouteNotes[announcementId] = when {
            details?.polyline?.size ?: 0 >= 2 -> null
            fallbackPlan != null && details != null -> {
                "Точная геометрия пока недоступна: карта показывает только основное направление задачи."
            }

            fallbackPlan != null -> {
                "Точная геометрия недоступна: показываем только разрешённый маршрут без чужих веток."
            }

            detailsResult.errorOrNull() != null -> detailsResult.errorOrNull()?.message
            else -> null
        }

        loadingCustomerRouteId = null
        issueFitMap()
        reduceUiState()
    }

    private suspend fun rebuildPerformerRoute(
        announceSuccess: String?,
    ) {
        val context = performerContext ?: return
        val displayedDetails = performerDisplayedDetails ?: performerBaseDetails

        _uiState.value = _uiState.value.copy(
            actions = _uiState.value.actions.copy(
                rebuildingRoute = true,
            ),
        )

        if (acceptedExtraTaskIds.isEmpty()) {
            restoreBasePerformerRoute()
            _uiState.value = _uiState.value.copy(
                actions = _uiState.value.actions.copy(
                    rebuildingRoute = false,
                ),
            )
            ensureSelections()
            issueFitMap()
            reduceUiState()
            announceSuccess?.let(::emitMessage)
            return
        }

        val orderedAcceptedTasks = orderedAcceptedTasks(
            tasks = performerKnownTasksById().values.toList(),
            acceptedTaskIds = acceptedExtraTaskIds,
            basePolyline = performerDisplayPolyline(),
        )
        val waypoints = orderedAcceptedTasks.mapNotNull { task ->
            routeTaskCoordinate(task, announcementCache[task.id])
        }
        val approximatePlan = buildApproximateRoute(
            start = context.start,
            waypoints = waypoints,
            end = context.end,
            travelMode = context.travelMode,
        )

        when (val result = routeRepository.buildRoute(approximatePlan.toBuildRequest(context))) {
            is ApiResult.Success -> {
                performerDisplayedDetails = result.value
                performerDisplayedFallbackPlan = approximatePlan
                performerDisplayedGeometryQuality = RouteMapGeometryQuality.Approximate
                performerDisplayedNote =
                    "Маршрут перестроен по принятым точкам и может быть приблизительным, пока сервер не отдаёт точную геометрию."
            }

            is ApiResult.Failure -> {
                performerDisplayedFallbackPlan = approximatePlan
                performerDisplayedGeometryQuality = approximatePlan.quality
                performerDisplayedNote =
                    "Не удалось обновить подбор задач по пути. Показываем текущую схему маршрута приблизительно."
                _uiState.value = _uiState.value.copy(
                    loading = _uiState.value.loading.copy(
                        inlineMessage = result.error.message,
                    ),
                )
            }
        }

        _uiState.value = _uiState.value.copy(
            actions = _uiState.value.actions.copy(
                rebuildingRoute = false,
            ),
        )
        ensureSelections()
        issueFitMap()
        reduceUiState()
        announceSuccess?.let(::emitMessage)
    }

    private suspend fun refreshAnnouncements(ids: Set<String>) {
        ids.filter { it.isNotBlank() }.forEach { announcementId ->
            when (val result = announcementRepository.fetchAnnouncement(announcementId)) {
                is ApiResult.Success -> mergeAnnouncement(result.value)
                is ApiResult.Failure -> Unit
            }
        }
    }

    private fun mergeAnnouncement(announcement: Announcement) {
        announcementCache[announcement.id] = announcement
        customerAnnouncements = customerAnnouncements.map { existing ->
            if (existing.id == announcement.id) announcement else existing
        }
    }

    private fun restoreBasePerformerRoute() {
        performerDisplayedDetails = performerBaseDetails
        performerDisplayedFallbackPlan = performerBaseFallbackPlan
        performerDisplayedGeometryQuality = performerBaseGeometryQuality
        performerDisplayedNote = performerBaseNote
    }

    private fun reduceUiState() {
        ensureSelections()

        val performerUi = buildPerformerUiState()
        val customerUi = buildCustomerUiState()
        val mapUi = buildMapUiState()

        _uiState.value = RouteUiState(
            roleSelection = RouteRoleSelectionUiState(
                selectedRole = selectedRole,
            ),
            loading = _uiState.value.loading.copy(
                isInitialLoading = _uiState.value.loading.isInitialLoading,
                isRefreshing = _uiState.value.loading.isRefreshing,
                inlineMessage = _uiState.value.loading.inlineMessage,
            ),
            performer = performerUi,
            customer = customerUi,
            map = mapUi,
            actions = _uiState.value.actions.copy(
                acceptedExtraCount = acceptedExtraTaskIds.size,
                acceptedExtraLimit = MAX_ACCEPTED_EXTRA_TASKS,
            ),
        )
    }

    private fun buildPerformerUiState(): PerformerRouteUiState {
        val activeTasks = performerActiveTasks()
        val previewTasks = performerPreviewTasks()
        val hasContent = performerContext != null ||
            performerDisplayedDetails != null ||
            performerDisplayedFallbackPlan != null ||
            activeTasks.isNotEmpty() ||
            previewTasks.isNotEmpty()

        return if (!hasContent) {
            PerformerRouteUiState(
                status = if (hasLoadedPerformer) RouteRoleContentStatus.Empty else RouteRoleContentStatus.Loading,
                summary = performerEmptyMessage,
                metrics = emptyList(),
                activeTasks = emptyList(),
                previewTasks = emptyList(),
                nextStep = null,
                emptyTitle = performerEmptyTitle,
                emptyMessage = performerEmptyMessage,
            )
        } else {
            PerformerRouteUiState(
                status = RouteRoleContentStatus.Content,
                summary = if (acceptedExtraTaskIds.isEmpty()) {
                    "Основной маршрут, активные остановки и задачи по пути собраны в одном месте."
                } else {
                    "Маршрут перестроен с выбранными задачами по пути и подсказывает следующий шаг."
                },
                metrics = performerMetrics(),
                activeTasks = activeTasks,
                previewTasks = previewTasks,
                nextStep = performerNextStep(activeTasks = activeTasks, previewTasks = previewTasks),
                emptyTitle = performerEmptyTitle,
                emptyMessage = performerEmptyMessage,
            )
        }
    }

    private fun buildCustomerUiState(): CustomerRouteUiState {
        val tasks = customerTasks()
        return if (tasks.isEmpty()) {
            CustomerRouteUiState(
                status = if (hasLoadedCustomer) RouteRoleContentStatus.Empty else RouteRoleContentStatus.Loading,
                summary = customerEmptyMessage,
                metrics = emptyList(),
                tasks = emptyList(),
                emptyTitle = customerEmptyTitle,
                emptyMessage = customerEmptyMessage,
            )
        } else {
            CustomerRouteUiState(
                status = RouteRoleContentStatus.Content,
                summary = "Показываем только ваши задачи и только тот маршрут, который разрешён текущим статусом.",
                metrics = customerMetrics(tasks = tasks),
                tasks = tasks,
                emptyTitle = customerEmptyTitle,
                emptyMessage = customerEmptyMessage,
            )
        }
    }

    private fun buildMapUiState(): RouteMapUiState {
        return when (selectedRole) {
            RouteRole.Performer -> buildPerformerMapUiState()
            RouteRole.Customer -> buildCustomerMapUiState()
        }
    }

    private fun buildPerformerMapUiState(): RouteMapUiState {
        val markers = performerMapMarkers()
        val polylines = performerMapPolylines()
        val hasRenderableGeometry = markers.isNotEmpty() || polylines.isNotEmpty()

        return RouteMapUiState(
            isConfigured = isMapConfigured,
            isLoading = _uiState.value.loading.isInitialLoading || _uiState.value.actions.rebuildingRoute,
            geometryQuality = performerDisplayedGeometryQuality,
            markers = markers,
            polylines = polylines,
            selectedMarkerId = selectedPerformerTaskId,
            note = performerDisplayedNote,
            overlayTitle = when {
                !isMapConfigured -> "Карта недоступна"
                hasRenderableGeometry -> null
                else -> "Маршрут пока не собран"
            },
            overlayMessage = when {
                !isMapConfigured -> "Yandex MapKit key не подключён для этой сборки. Список задач и статусы остаются доступны."
                hasRenderableGeometry -> null
                else -> performerEmptyMessage
            },
            command = currentMapCommand,
            externalNavigation = performerExternalNavigation(),
        )
    }

    private fun buildCustomerMapUiState(): RouteMapUiState {
        val selectedTaskId = selectedCustomerTaskId
        val announcement = customerAnnouncements.firstOrNull { it.id == selectedTaskId }
        val routeDetails = selectedTaskId?.let(customerRouteDetails::get)
        val fallbackPlan = selectedTaskId?.let(customerFallbackPlans::get)
        val routeContext = selectedTaskId?.let(customerRouteContexts::get)
        val geometryQuality = selectedTaskId?.let(customerGeometryQualities::get)
            ?: RouteMapGeometryQuality.PointsOnly
        val source = routeContext?.start ?: announcement?.sourcePoint
        val destination = routeContext?.end ?: announcement?.destinationPoint ?: announcement?.sourcePoint

        val markers = buildList {
            source?.let {
                add(
                    RouteMapMarkerUi(
                        id = "customer-source-${selectedTaskId.orEmpty()}",
                        title = "Старт",
                        subtitle = routeContext?.startAddress ?: announcement?.primarySourceAddress,
                        point = it,
                        kind = RouteMarkerKind.Start,
                    ),
                )
            }
            destination?.let {
                add(
                    RouteMapMarkerUi(
                        id = selectedTaskId.orEmpty(),
                        title = announcement?.title ?: "Задача",
                        subtitle = routeContext?.endAddress
                            ?: announcement?.primaryDestinationAddress
                            ?: announcement?.primarySourceAddress,
                        point = it,
                        kind = RouteMarkerKind.CustomerTask,
                    ),
                )
            }
        }

        val polylinePoints = when {
            routeDetails?.polyline?.size ?: 0 >= 2 -> routeDetails?.polyline.orEmpty()
            fallbackPlan?.polyline?.size ?: 0 >= 2 -> fallbackPlan?.polyline.orEmpty()
            else -> emptyList()
        }

        return RouteMapUiState(
            isConfigured = isMapConfigured,
            isLoading = loadingCustomerRouteId == selectedTaskId,
            geometryQuality = geometryQuality,
            markers = markers,
            polylines = if (polylinePoints.size >= 2) {
                listOf(
                    RouteMapPolylineUi(
                        id = "customer-route-${selectedTaskId.orEmpty()}",
                        points = polylinePoints,
                        kind = RoutePolylineKind.Main,
                    ),
                )
            } else {
                emptyList()
            },
            selectedMarkerId = selectedTaskId,
            note = selectedTaskId?.let(customerRouteNotes::get),
            overlayTitle = when {
                !isMapConfigured -> "Карта недоступна"
                announcement == null -> "Нечего показывать"
                loadingCustomerRouteId == selectedTaskId -> "Загружаем маршрут"
                markers.isEmpty() && polylinePoints.isEmpty() -> "Маршрут пока недоступен"
                else -> null
            },
            overlayMessage = when {
                !isMapConfigured -> "Yandex MapKit key не подключён для этой сборки. Список задач остаётся доступен."
                announcement == null -> customerEmptyMessage
                loadingCustomerRouteId == selectedTaskId -> "Подтягиваем разрешённый маршрут по выбранной задаче."
                markers.isEmpty() && polylinePoints.isEmpty() -> {
                    selectedTaskId?.let(customerRouteNotes::get)
                        ?: "Покажем маршрут здесь, как только сервер вернёт доступные координаты."
                }

                else -> null
            },
            command = currentMapCommand,
            externalNavigation = customerExternalNavigation(
                announcement = announcement,
                routeContext = routeContext,
                destination = destination,
            ),
        )
    }

    private fun performerMetrics(): List<RouteMetricUi> {
        val details = performerDisplayedDetails
        val fallbackPlan = performerDisplayedFallbackPlan
        val metrics = mutableListOf<RouteMetricUi>()

        val distance = details?.distanceMeters ?: fallbackPlan?.distanceMeters
        val duration = details?.durationSeconds ?: fallbackPlan?.durationSeconds

        distance?.takeIf { it > 0 }?.let {
            metrics += RouteMetricUi(
                id = "performer-distance",
                label = "Маршрут",
                value = formatDistance(it),
            )
        }
        duration?.takeIf { it > 0 }?.let {
            metrics += RouteMetricUi(
                id = "performer-duration",
                label = "Время",
                value = formatDuration(it),
            )
        }
        metrics += RouteMetricUi(
            id = "performer-extra",
            label = "По пути",
            value = "${acceptedExtraTaskIds.size}/$MAX_ACCEPTED_EXTRA_TASKS",
        )
        metrics += RouteMetricUi(
            id = "performer-stops",
            label = "Точек",
            value = performerActiveTasks().size.toString(),
        )
        return metrics
    }

    private fun customerMetrics(tasks: List<RouteTaskCardUi>): List<RouteMetricUi> {
        val selectedTaskId = selectedCustomerTaskId
        val routeDetails = selectedTaskId?.let(customerRouteDetails::get)
        val fallbackPlan = selectedTaskId?.let(customerFallbackPlans::get)
        val metrics = mutableListOf(
            RouteMetricUi(
                id = "customer-tasks",
                label = "Задачи",
                value = tasks.size.toString(),
            ),
        )

        val distance = routeDetails?.distanceMeters ?: fallbackPlan?.distanceMeters
        val duration = routeDetails?.durationSeconds ?: fallbackPlan?.durationSeconds

        distance?.takeIf { it > 0 }?.let {
            metrics += RouteMetricUi(
                id = "customer-distance",
                label = "Маршрут",
                value = formatDistance(it),
            )
        }
        duration?.takeIf { it > 0 }?.let {
            metrics += RouteMetricUi(
                id = "customer-duration",
                label = "Время",
                value = formatDuration(it),
            )
        }
        return metrics
    }

    private fun performerActiveTasks(): List<RouteTaskCardUi> {
        val context = performerContext
        val details = performerDisplayedDetails
        val primaryAnnouncementId = context?.entityId ?: details?.entityId ?: return emptyList()
        val primaryAnnouncement = announcementCache[primaryAnnouncementId]
        val primaryTask = RouteTaskCardUi(
            id = primaryAnnouncementId,
            announcementId = primaryAnnouncementId,
            title = primaryAnnouncement?.title ?: "Основное задание",
            subtitle = primaryAnnouncement?.shortStructuredSubtitle
                ?: context?.endAddress
                ?: details?.endAddress
                ?: "Основной маршрут",
            body = routeTaskDescription(primaryAnnouncement, context?.endAddress ?: details?.endAddress),
            addressText = context?.endAddress ?: details?.endAddress,
            priceText = primaryAnnouncement?.formattedBudgetText,
            kind = RouteTaskKind.Primary,
            kindLabel = "Основное",
            statusChip = statusChipForAnnouncement(primaryAnnouncement, fallbackStatus = null, fallbackKind = RouteTaskKind.Primary),
            stageChips = buildStageChips(primaryAnnouncement?.let(::routeStageFromAnnouncement)),
            nextStage = nextRouteStage(primaryAnnouncement?.let(::routeStageFromAnnouncement)),
            nextStageLabel = nextRouteStage(primaryAnnouncement?.let(::routeStageFromAnnouncement))?.let(::stageLabel),
            detourText = null,
            previewSummary = null,
            coordinate = primaryAnnouncement?.destinationPoint ?: context?.end,
            canOpenDetails = true,
            canAdvanceStage = nextRouteStage(primaryAnnouncement?.let(::routeStageFromAnnouncement)) != null,
            canAcceptToRoute = false,
            canRemoveFromRoute = false,
            isSelected = selectedPerformerTaskId == primaryAnnouncementId,
        )

        val acceptedTasks = orderedAcceptedTasks(
            tasks = performerKnownTasksById().values.filterNot { task ->
                routeTaskIsCompleted(task, announcementCache[task.id])
            },
            acceptedTaskIds = acceptedExtraTaskIds,
            basePolyline = performerDisplayPolyline(),
        )

        return buildList {
            add(primaryTask)
            acceptedTasks.forEach { task ->
                val announcement = announcementCache[task.id]
                val currentStage = announcement?.let(::routeStageFromAnnouncement)
                    ?: fallbackRouteStage(task.status)
                add(
                    RouteTaskCardUi(
                        id = task.id,
                        announcementId = task.id,
                        title = announcement?.title ?: task.title,
                        subtitle = announcement?.shortStructuredSubtitle ?: routeTaskSubtitle(task),
                        body = routeTaskDescription(announcement, task.addressText),
                        addressText = task.addressText,
                        priceText = task.priceText ?: announcement?.formattedBudgetText,
                        kind = RouteTaskKind.AcceptedExtra,
                        kindLabel = "По пути",
                        statusChip = statusChipForAnnouncement(announcement, task.status, RouteTaskKind.AcceptedExtra),
                        stageChips = buildStageChips(currentStage),
                        nextStage = nextRouteStage(currentStage),
                        nextStageLabel = nextRouteStage(currentStage)?.let(::stageLabel),
                        detourText = performerContext?.travelMode?.let { travelMode ->
                            approximateDetourText(task.distanceToRouteMeters, travelMode)
                        },
                        previewSummary = "Уже встроено в текущий маршрут.",
                        coordinate = routeTaskCoordinate(task, announcement),
                        canOpenDetails = true,
                        canAdvanceStage = shouldAllowStageUpdates(announcement, isPrimaryTask = false) &&
                            nextRouteStage(currentStage) != null,
                        canAcceptToRoute = false,
                        canRemoveFromRoute = true,
                        isSelected = selectedPerformerTaskId == task.id,
                    ),
                )
            }
        }
    }

    private fun performerPreviewTasks(): List<RouteTaskCardUi> {
        val details = performerDisplayedDetails ?: return emptyList()

        return details.tasksByRoute
            .filterNot { acceptedExtraTaskIds.contains(it.id) }
            .filterNot { task -> routeTaskIsCompleted(task, announcementCache[task.id]) }
            .sortedWith(
                compareBy<TaskByRoute> { it.distanceToRouteMeters ?: Double.MAX_VALUE }
                    .thenBy { it.title },
            )
            .map { task ->
                val announcement = announcementCache[task.id]
                RouteTaskCardUi(
                    id = task.id,
                    announcementId = task.id,
                    title = announcement?.title ?: task.title,
                    subtitle = announcement?.shortStructuredSubtitle ?: routeTaskSubtitle(task),
                    body = routeTaskDescription(announcement, task.addressText),
                    addressText = task.addressText,
                    priceText = task.priceText ?: announcement?.formattedBudgetText,
                    kind = RouteTaskKind.Preview,
                    kindLabel = "Рядом",
                    statusChip = statusChipForAnnouncement(announcement, task.status, RouteTaskKind.Preview),
                    stageChips = emptyList(),
                    nextStage = null,
                    nextStageLabel = null,
                    detourText = performerContext?.travelMode?.let { travelMode ->
                        approximateDetourText(task.distanceToRouteMeters, travelMode)
                    },
                    previewSummary = routeTaskSummary(task).ifBlank { "Подходит к текущему коридору маршрута." },
                    coordinate = routeTaskCoordinate(task, announcement),
                    canOpenDetails = true,
                    canAdvanceStage = false,
                    canAcceptToRoute = acceptedExtraTaskIds.size < MAX_ACCEPTED_EXTRA_TASKS,
                    canRemoveFromRoute = false,
                    isSelected = selectedPerformerTaskId == task.id,
                )
            }
    }

    private fun customerTasks(): List<RouteTaskCardUi> = customerAnnouncements.map { announcement ->
        val routeContext = customerRouteContexts[announcement.id]
        val currentStage = routeStageFromAnnouncement(announcement)
        RouteTaskCardUi(
            id = announcement.id,
            announcementId = announcement.id,
            title = announcement.title,
            subtitle = announcement.shortStructuredSubtitle,
            body = routeTaskDescription(announcement, announcement.primaryDestinationAddress ?: announcement.primarySourceAddress),
            addressText = routeContext?.endAddress
                ?: announcement.primaryDestinationAddress
                ?: announcement.primarySourceAddress,
            priceText = announcement.formattedBudgetText,
            kind = RouteTaskKind.CustomerObserved,
            kindLabel = "Моя задача",
            statusChip = statusChipForAnnouncement(announcement, fallbackStatus = null, fallbackKind = RouteTaskKind.CustomerObserved),
            stageChips = buildStageChips(currentStage),
            nextStage = null,
            nextStageLabel = null,
            detourText = null,
            previewSummary = "Показываем только ваш основной маршрут без чужих ответвлений.",
            coordinate = routeContext?.end
                ?: announcement.destinationPoint
                ?: announcement.sourcePoint,
            canOpenDetails = true,
            canAdvanceStage = false,
            canAcceptToRoute = false,
            canRemoveFromRoute = false,
            isSelected = selectedCustomerTaskId == announcement.id,
        )
    }

    private fun performerNextStep(
        activeTasks: List<RouteTaskCardUi>,
        previewTasks: List<RouteTaskCardUi>,
    ): RouteNextStepUi? {
        val actionableTask = activeTasks.firstOrNull { it.canAdvanceStage && it.nextStageLabel != null }
        if (actionableTask != null) {
            return RouteNextStepUi(
                title = "Следующий шаг",
                body = "${actionableTask.title}: ${actionableTask.nextStageLabel}",
                actionLabel = "Отметить этап",
                taskId = actionableTask.id,
            )
        }

        if (previewTasks.isNotEmpty() && acceptedExtraTaskIds.size < MAX_ACCEPTED_EXTRA_TASKS) {
            return RouteNextStepUi(
                title = "Можно усилить маршрут",
                body = "Добавьте ещё до ${MAX_ACCEPTED_EXTRA_TASKS - acceptedExtraTaskIds.size} задач по пути, если они подходят по времени и отклонению.",
            )
        }

        if (acceptedExtraTaskIds.size >= MAX_ACCEPTED_EXTRA_TASKS) {
            return RouteNextStepUi(
                title = "Лимит маршрута заполнен",
                body = "Сейчас в маршрут можно держать не больше двух дополнительных задач по пути.",
            )
        }

        return RouteNextStepUi(
            title = "Маршрут под контролем",
            body = "Откройте нужную карточку, чтобы быстро посмотреть адрес, детали и текущее состояние.",
        )
    }

    private fun performerMapMarkers(): List<RouteMapMarkerUi> {
        val markers = mutableListOf<RouteMapMarkerUi>()
        performerContext?.start?.let { start ->
            markers += RouteMapMarkerUi(
                id = "performer-start",
                title = "Старт",
                subtitle = performerContext?.startAddress,
                point = start,
                kind = RouteMarkerKind.Start,
            )
        }

        performerActiveTasks().forEach { task ->
            val coordinate = task.coordinate ?: return@forEach
            markers += RouteMapMarkerUi(
                id = task.id,
                title = task.title,
                subtitle = task.addressText ?: task.previewSummary,
                point = coordinate,
                kind = when (task.kind) {
                    RouteTaskKind.Primary -> RouteMarkerKind.PrimaryTask
                    RouteTaskKind.AcceptedExtra -> RouteMarkerKind.AcceptedExtra
                    RouteTaskKind.Preview -> RouteMarkerKind.Preview
                    RouteTaskKind.CustomerObserved -> RouteMarkerKind.CustomerTask
                },
            )
        }

        performerPreviewTasks().forEach { task ->
            val coordinate = task.coordinate ?: return@forEach
            markers += RouteMapMarkerUi(
                id = task.id,
                title = task.title,
                subtitle = task.addressText ?: task.previewSummary,
                point = coordinate,
                kind = RouteMarkerKind.Preview,
            )
        }

        return markers.distinctBy(RouteMapMarkerUi::id)
    }

    private fun performerMapPolylines(): List<RouteMapPolylineUi> {
        val currentPolyline = performerDisplayPolyline()
        val polylines = mutableListOf<RouteMapPolylineUi>()

        if (currentPolyline.size >= 2) {
            polylines += RouteMapPolylineUi(
                id = "performer-main-route",
                points = currentPolyline,
                kind = RoutePolylineKind.Main,
            )
        }

        performerPreviewTasks().forEach { task ->
            val coordinate = task.coordinate ?: return@forEach
            val branchLine = buildPreviewBranchLine(
                basePolyline = currentPolyline,
                point = coordinate,
            )
            if (branchLine.size >= 2) {
                polylines += RouteMapPolylineUi(
                    id = "preview-branch-${task.id}",
                    points = branchLine,
                    kind = RoutePolylineKind.Preview,
                )
            }
        }

        return polylines
    }

    private fun performerExternalNavigation(): RouteExternalNavigationUi? {
        val context = performerContext ?: return null
        return RouteExternalNavigationUi(
            origin = context.start,
            destination = context.end,
            waypoints = orderedAcceptedTasks(
                tasks = performerKnownTasksById().values.toList(),
                acceptedTaskIds = acceptedExtraTaskIds,
                basePolyline = performerDisplayPolyline(),
            ).mapNotNull { task ->
                routeTaskCoordinate(task, announcementCache[task.id])
            },
            travelMode = context.travelMode,
        )
    }

    private fun customerExternalNavigation(
        announcement: Announcement?,
        routeContext: RouteContext?,
        destination: com.vzaimno.app.core.model.GeoPoint?,
    ): RouteExternalNavigationUi? {
        destination ?: return null

        return RouteExternalNavigationUi(
            origin = routeContext?.start ?: announcement?.sourcePoint,
            destination = destination,
            waypoints = emptyList(),
            travelMode = routeContext?.travelMode ?: announcement?.let(::inferTravelMode) ?: RouteTravelMode.Driving,
        )
    }

    private fun performerDisplayPolyline(): List<com.vzaimno.app.core.model.GeoPoint> = when {
        performerDisplayedDetails?.polyline?.size ?: 0 >= 2 -> performerDisplayedDetails?.polyline.orEmpty()
        performerDisplayedFallbackPlan?.polyline?.size ?: 0 >= 2 -> performerDisplayedFallbackPlan?.polyline.orEmpty()
        else -> emptyList()
    }

    private fun performerSelectableTaskIds(): List<String> = buildList {
        performerActiveTasks().forEach { add(it.id) }
        performerPreviewTasks().forEach { add(it.id) }
    }

    private fun customerSelectableTaskIds(): List<String> = customerAnnouncements.map(Announcement::id)

    private fun performerTaskById(taskId: String): TaskByRoute? =
        performerKnownTasksById()[taskId]

    private fun performerKnownTasksById(): Map<String, TaskByRoute> {
        val tasks = linkedMapOf<String, TaskByRoute>()
        performerBaseDetails?.tasksByRoute.orEmpty().forEach { task ->
            tasks[task.id] = task
        }
        performerDisplayedDetails?.tasksByRoute.orEmpty().forEach { task ->
            tasks[task.id] = task
        }
        return tasks
    }

    private fun activeRoleHasNoVisibleContent(): Boolean = when (selectedRole) {
        RouteRole.Performer -> performerSelectableTaskIds().isEmpty() && performerDisplayPolyline().isEmpty()
        RouteRole.Customer -> customerSelectableTaskIds().isEmpty()
    }

    private fun ensureSelections() {
        val performerIds = performerSelectableTaskIds()
        if (selectedPerformerTaskId == null || !performerIds.contains(selectedPerformerTaskId)) {
            selectedPerformerTaskId = performerIds.firstOrNull()
            savedStateHandle[PERFORMER_SELECTED_TASK_KEY] = selectedPerformerTaskId
        }

        val customerIds = customerSelectableTaskIds()
        if (selectedCustomerTaskId == null || !customerIds.contains(selectedCustomerTaskId)) {
            selectedCustomerTaskId = customerIds.firstOrNull()
            savedStateHandle[CUSTOMER_SELECTED_TASK_KEY] = selectedCustomerTaskId
        }
    }

    private fun issueFitMap() {
        mapCommandToken += 1
        currentMapCommand = RouteMapCommand.FitAll(token = mapCommandToken)
    }

    private fun issueFocusForTask(taskId: String) {
        val point = when (selectedRole) {
            RouteRole.Performer -> performerActiveTasks().firstOrNull { it.id == taskId }?.coordinate
                ?: performerPreviewTasks().firstOrNull { it.id == taskId }?.coordinate

            RouteRole.Customer -> customerTasks().firstOrNull { it.id == taskId }?.coordinate
        } ?: return

        mapCommandToken += 1
        currentMapCommand = RouteMapCommand.FocusPoint(
            token = mapCommandToken,
            point = point,
        )
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(message)
        }
    }

    private fun statusChipForAnnouncement(
        announcement: Announcement?,
        fallbackStatus: String?,
        fallbackKind: RouteTaskKind,
    ): RouteStatusChipUi? {
        val currentStage = announcement?.let(::routeStageFromAnnouncement) ?: fallbackRouteStage(fallbackStatus)
        return when {
            currentStage == RouteStage.Completed -> RouteStatusChipUi("Завершено", RouteChipTone.Positive)
            announcement == null && fallbackKind == RouteTaskKind.Preview -> RouteStatusChipUi("По пути", RouteChipTone.Neutral)
            announcement == null && currentStage == null && fallbackKind == RouteTaskKind.AcceptedExtra -> {
                RouteStatusChipUi("В маршруте", RouteChipTone.Accent)
            }

            announcement == null && currentStage != null -> {
                RouteStatusChipUi(stageLabel(currentStage), stageTone(currentStage))
            }

            announcement != null -> {
                when (announcement.taskStateProjection.executionStatus) {
                    com.vzaimno.app.core.model.TaskExecutionStatus.Open,
                    com.vzaimno.app.core.model.TaskExecutionStatus.AwaitingAcceptance,
                    -> RouteStatusChipUi("Ожидает", RouteChipTone.Neutral)

                    com.vzaimno.app.core.model.TaskExecutionStatus.Accepted ->
                        RouteStatusChipUi("Принято", RouteChipTone.Accent)

                    com.vzaimno.app.core.model.TaskExecutionStatus.EnRoute ->
                        RouteStatusChipUi("В пути", RouteChipTone.Accent)

                    com.vzaimno.app.core.model.TaskExecutionStatus.OnSite ->
                        RouteStatusChipUi("На месте", RouteChipTone.Warning)

                    com.vzaimno.app.core.model.TaskExecutionStatus.InProgress ->
                        RouteStatusChipUi("В работе", RouteChipTone.Accent)

                    com.vzaimno.app.core.model.TaskExecutionStatus.Handoff ->
                        RouteStatusChipUi("Финал", RouteChipTone.Warning)

                    com.vzaimno.app.core.model.TaskExecutionStatus.Completed ->
                        RouteStatusChipUi("Завершено", RouteChipTone.Positive)

                    com.vzaimno.app.core.model.TaskExecutionStatus.Cancelled ->
                        RouteStatusChipUi("Отменено", RouteChipTone.Neutral)

                    com.vzaimno.app.core.model.TaskExecutionStatus.Disputed ->
                        RouteStatusChipUi("Разбор", RouteChipTone.Danger)
                }
            }

            else -> null
        }
    }

    private fun buildStageChips(currentStage: RouteStage?): List<RouteStageChipUi> {
        val orderedStages = RouteStage.entries
        return orderedStages.map { stage ->
            RouteStageChipUi(
                stage = stage,
                label = stageLabel(stage),
                state = when {
                    currentStage == null && stage == RouteStage.Accepted -> RouteStageProgressState.Pending
                    currentStage == stage -> RouteStageProgressState.Current
                    currentStage != null && orderedStages.indexOf(stage) < orderedStages.indexOf(currentStage) -> {
                        RouteStageProgressState.Done
                    }

                    currentStage == RouteStage.Completed -> RouteStageProgressState.Done
                    else -> RouteStageProgressState.Pending
                },
            )
        }
    }

    private fun stageLabel(stage: RouteStage): String = when (stage) {
        RouteStage.Accepted -> "Принял"
        RouteStage.EnRoute -> "В пути"
        RouteStage.OnSite -> "На месте"
        RouteStage.InProgress -> "Выполняю"
        RouteStage.Handoff -> "Финал"
        RouteStage.Completed -> "Готово"
    }

    private fun stageTone(stage: RouteStage): RouteChipTone = when (stage) {
        RouteStage.Accepted,
        RouteStage.EnRoute,
        RouteStage.InProgress,
        -> RouteChipTone.Accent

        RouteStage.OnSite,
        RouteStage.Handoff,
        -> RouteChipTone.Warning

        RouteStage.Completed -> RouteChipTone.Positive
    }

    private fun inferTravelMode(announcement: Announcement): RouteTravelMode {
        val structured = announcement.structuredData
        return when {
            structured.requiresVehicle -> RouteTravelMode.Driving
            announcement.category.equals("delivery", ignoreCase = true) -> RouteTravelMode.Driving
            else -> RouteTravelMode.Walking
        }
    }

    private fun nextRefreshIntervalMillis(): Long = when (selectedRole) {
        RouteRole.Performer -> if (performerContext != null) 20_000L else 45_000L
        RouteRole.Customer -> if (customerAnnouncements.isNotEmpty()) 25_000L else 60_000L
    }

    private fun RouteStage.apiValue(): String = when (this) {
        RouteStage.Accepted -> "accepted"
        RouteStage.EnRoute -> "en_route"
        RouteStage.OnSite -> "on_site"
        RouteStage.InProgress -> "in_progress"
        RouteStage.Handoff -> "handoff"
        RouteStage.Completed -> "completed"
    }

    private fun SavedStateHandle.restoreRouteRole(): RouteRole =
        get<String>(SELECTED_ROLE_KEY)?.let { raw ->
            RouteRole.entries.firstOrNull { it.name == raw }
        } ?: RouteRole.Performer

    private fun <T> ApiResult<T>.successOrNull(): T? = (this as? ApiResult.Success)?.value

    private fun ApiResult<*>.errorOrNull(): ApiError? = (this as? ApiResult.Failure)?.error

    companion object {
        private const val SELECTED_ROLE_KEY = "route_selected_role"
        private const val PERFORMER_SELECTED_TASK_KEY = "route_selected_performer_task"
        private const val CUSTOMER_SELECTED_TASK_KEY = "route_selected_customer_task"
        private const val MAX_ACCEPTED_EXTRA_TASKS = 2
        private const val FOREGROUND_STALE_AFTER_MS = 15_000L
    }
}
