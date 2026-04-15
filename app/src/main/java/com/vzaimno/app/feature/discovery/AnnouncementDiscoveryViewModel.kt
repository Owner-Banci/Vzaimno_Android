package com.vzaimno.app.feature.discovery

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.BuildConfig
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.OfferPricingMode
import com.vzaimno.app.core.model.canAppearOnMap
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.mapPoint
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ActiveSession
import com.vzaimno.app.data.repository.AnnouncementRepository
import com.vzaimno.app.data.repository.SessionManager
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AnnouncementDiscoveryViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context,
    appConfig: AppConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DiscoveryUiState(
            apiBaseUrl = appConfig.normalizedApiBaseUrl,
            isMapConfigured = BuildConfig.YANDEX_MAPKIT_API_KEY.isNotBlank(),
        ),
    )
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private var allAnnouncements: List<Announcement> = emptyList()
    private var activeSession: ActiveSession = sessionManager.activeSession.value
    private val locallyRespondedIds = linkedSetOf<String>()

    private var hasLoaded = false
    private var loadJob: Job? = null
    private var detailLoadJob: Job? = null
    private var searchJob: Job? = null
    private var mapFocusToken = 0L

    // Route ("по пути") state
    private val drivingRouter: DrivingRouter by lazy {
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
    }
    private var mainRouteSession: DrivingSession? = null
    private var branchRouteSession: DrivingSession? = null
    private var rebuildRouteSession: DrivingSession? = null
    private var mainRouteRequestToken = 0
    private var branchRouteRequestToken = 0
    private var rebuildRouteRequestToken = 0
    private val acceptedAnnouncementIds = mutableListOf<String>()

    init {
        viewModelScope.launch {
            sessionManager.activeSession.collectLatest { session ->
                activeSession = session
                syncPresentation()
            }
        }
    }

    fun loadIfNeeded() {
        if (loadJob?.isActive == true) return
        if (!hasLoaded) {
            load(showLoadingState = true)
        } else {
            // Silently refresh in background to pick up newly created announcements
            load(showLoadingState = false)
        }
    }

    fun retry() {
        load(showLoadingState = allAnnouncements.isEmpty())
    }

    fun refresh() {
        load(showLoadingState = false)
    }

    fun clearContentMessage() {
        _uiState.update { state ->
            state.copy(contentMessage = null)
        }
    }

    fun toggleContentMode() {
        _uiState.update { state ->
            state.copy(
                contentMode = if (state.contentMode == DiscoveryContentMode.Map) {
                    DiscoveryContentMode.List
                } else {
                    DiscoveryContentMode.Map
                },
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query)
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(260)
            syncPresentation()
        }
    }

    fun toggleQuickCategory(category: DiscoveryCategoryFilter) {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.toggleCategory(category),
            )
        }
        syncPresentation()
    }

    fun toggleQuickAction(action: AnnouncementStructuredData.ActionType) {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.toggleAction(action),
            )
        }
        syncPresentation()
    }

    fun openFilters() {
        _uiState.update { state ->
            state.copy(
                isFiltersSheetVisible = true,
                filterDraft = state.filters,
            )
        }
    }

    fun dismissFilters() {
        _uiState.update { state ->
            state.copy(isFiltersSheetVisible = false)
        }
    }

    fun resetFilterDraft() {
        _uiState.update { state ->
            state.copy(
                filterDraft = DiscoveryFilterState(),
                routeState = state.routeState.copy(
                    draftStartAddress = "",
                    draftEndAddress = "",
                ),
            )
        }
        deactivateRoute(clearDraft = true)
    }

    fun applyFilterDraft() {
        _uiState.update { state ->
            state.copy(
                filters = state.filterDraft,
                isFiltersSheetVisible = false,
            )
        }
        syncPresentation()
    }

    fun updateFilterDraftBudgetMin(text: String) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(
                    budgetMinText = text.filter(Char::isDigit),
                ),
            )
        }
    }

    fun updateFilterDraftBudgetMax(text: String) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(
                    budgetMaxText = text.filter(Char::isDigit),
                ),
            )
        }
    }

    fun toggleFilterDraftCategory(category: DiscoveryCategoryFilter) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.toggleCategory(category),
            )
        }
    }

    fun toggleFilterDraftUrgency(urgency: AnnouncementStructuredData.Urgency) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.toggleUrgency(urgency),
            )
        }
    }

    fun setFilterDraftWithPhotoOnly(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(withPhotoOnly = enabled),
            )
        }
    }

    fun setFilterDraftRequiresVehicleOnly(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(requiresVehicleOnly = enabled),
            )
        }
    }

    fun setFilterDraftNeedsLoaderOnly(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(needsLoaderOnly = enabled),
            )
        }
    }

    fun setFilterDraftContactlessOnly(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(contactlessOnly = enabled),
            )
        }
    }

    fun clearAllFilters() {
        _uiState.update { state ->
            state.copy(
                filters = DiscoveryFilterState(),
                filterDraft = DiscoveryFilterState(),
                searchQuery = "",
            )
        }
        deactivateRoute(clearDraft = true)
        syncPresentation()
    }

    fun updateFilterDraftOnlyOnRoute(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterDraft = state.filterDraft.copy(
                    onlyOnRoute = enabled,
                ),
            )
        }
    }

    fun updateRouteDraftStartAddress(value: String) {
        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    draftStartAddress = value,
                ),
            )
        }
    }

    fun updateRouteDraftEndAddress(value: String) {
        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    draftEndAddress = value,
                ),
            )
        }
    }

    fun buildRouteFromDraft() {
        val routeState = _uiState.value.routeState
        val startAddress = routeState.draftStartAddress.trim()
        val endAddress = routeState.draftEndAddress.trim()

        if (startAddress.isBlank() || endAddress.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    routeState = state.routeState.copy(
                        isBuilding = false,
                        statusMessage = "Укажите адрес отправления и прибытия.",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    routeState = state.routeState.copy(
                        isBuilding = true,
                        statusMessage = "Ищем адреса маршрута…",
                    ),
                )
            }

            val startPoint = resolveRoutePoint(startAddress)
            if (startPoint == null) {
                _uiState.update { state ->
                    state.copy(
                        routeState = state.routeState.copy(
                            isBuilding = false,
                            statusMessage = "Не удалось найти стартовый адрес.",
                        ),
                    )
                }
                return@launch
            }

            val endPoint = resolveRoutePoint(endAddress)
            if (endPoint == null) {
                _uiState.update { state ->
                    state.copy(
                        routeState = state.routeState.copy(
                            isBuilding = false,
                            statusMessage = "Не удалось найти конечный адрес.",
                        ),
                    )
                }
                return@launch
            }

            activateRoute(
                startPoint = startPoint,
                endPoint = endPoint,
                startAddress = startAddress,
                endAddress = endAddress,
            )
        }
    }

    fun openAnnouncementDetails(announcementId: String) {
        presentAnnouncementDetails(
            announcementId = announcementId,
            switchToMap = false,
            focusOnMap = false,
        )
    }

    fun showAnnouncementOnMap(announcementId: String) {
        presentAnnouncementDetails(
            announcementId = announcementId,
            switchToMap = true,
            focusOnMap = true,
        )
    }

    fun dismissDetails() {
        _uiState.update { state ->
            state.copy(
                detailsState = DiscoveryDetailsUiState(),
            )
        }
    }

    fun updateOfferMessage(message: String) {
        _uiState.update { state ->
            state.copy(
                detailsState = state.detailsState.copy(
                    offerMessage = message,
                    actionErrorMessage = null,
                    offerSuccessState = null,
                ),
            )
        }
    }

    fun openCustomPriceDialog() {
        val announcement = _uiState.value.detailsState.item?.announcement ?: return
        val defaultValue = AnnouncementDiscoveryFilterEngine.defaultCustomPrice(announcement)
        _uiState.update { state ->
            state.copy(
                detailsState = state.detailsState.copy(
                    isCustomPriceDialogVisible = true,
                    customPriceInput = state.detailsState.customPriceInput.ifBlank {
                        defaultValue.takeIf { it > 0 }?.toString().orEmpty()
                    },
                    customPriceError = null,
                    actionErrorMessage = null,
                    offerSuccessState = null,
                ),
            )
        }
    }

    fun dismissCustomPriceDialog() {
        _uiState.update { state ->
            state.copy(
                detailsState = state.detailsState.copy(
                    isCustomPriceDialogVisible = false,
                    customPriceError = null,
                ),
            )
        }
    }

    fun updateCustomPriceInput(value: String) {
        _uiState.update { state ->
            state.copy(
                detailsState = state.detailsState.copy(
                    customPriceInput = value.filter(Char::isDigit),
                    customPriceError = null,
                    actionErrorMessage = null,
                    offerSuccessState = null,
                ),
            )
        }
    }

    fun submitQuickOffer() {
        val item = _uiState.value.detailsState.item ?: return
        val price = item.announcement.quickOfferPrice ?: return
        if (item.responseGate != DiscoveryResponseGate.Available) return

        submitOffer(
            announcementId = item.announcement.id,
            pricingMode = OfferPricingMode.QuickMinPrice,
            proposedPrice = price,
            agreedPrice = price,
            minimumPriceAccepted = true,
            isQuickOffer = true,
        )
    }

    fun submitCustomOffer() {
        val detailsState = _uiState.value.detailsState
        val item = detailsState.item ?: return
        if (item.responseGate != DiscoveryResponseGate.Available) return

        val proposedPrice = detailsState.customPriceInput.filter(Char::isDigit).toIntOrNull()
        if (proposedPrice == null || proposedPrice <= 0) {
            _uiState.update { state ->
                state.copy(
                    detailsState = state.detailsState.copy(
                        customPriceError = DiscoveryCustomPriceError.Invalid,
                    ),
                )
            }
            return
        }

        submitOffer(
            announcementId = item.announcement.id,
            pricingMode = OfferPricingMode.CounterPrice,
            proposedPrice = proposedPrice,
            agreedPrice = null,
            minimumPriceAccepted = false,
            isQuickOffer = false,
        )
    }

    fun consumeMapFocus(token: Long) {
        _uiState.update { state ->
            if (state.mapFocusRequest?.token != token) {
                state
            } else {
                state.copy(mapFocusRequest = null)
            }
        }
    }

    private fun load(showLoadingState: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val hasExistingContent = allAnnouncements.isNotEmpty()
            _uiState.update { state ->
                state.copy(
                    isInitialLoading = showLoadingState && !hasExistingContent,
                    isRefreshing = !showLoadingState && hasExistingContent,
                    loadErrorMessage = if (showLoadingState && !hasExistingContent) null else state.loadErrorMessage,
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.fetchPublicAnnouncements()) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    allAnnouncements = result.value
                    syncPresentation()
                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            loadErrorMessage = null,
                            contentMessage = null,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    if (!hasExistingContent) {
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                loadErrorMessage = result.error.message,
                                announcements = emptyList(),
                            )
                        }
                    } else {
                        syncPresentation()
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                contentMessage = result.error.message,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun presentAnnouncementDetails(
        announcementId: String,
        switchToMap: Boolean,
        focusOnMap: Boolean,
    ) {
        val item = buildItemForId(announcementId) ?: return
        val announcement = item.announcement

        _uiState.update { state ->
            state.copy(
                contentMode = if (switchToMap) DiscoveryContentMode.Map else state.contentMode,
                isFiltersSheetVisible = false,
                detailsState = DiscoveryDetailsUiState(
                    isVisible = true,
                    announcementId = announcementId,
                    item = item,
                    isLoading = true,
                    customPriceInput = AnnouncementDiscoveryFilterEngine.defaultCustomPrice(announcement)
                        .takeIf { it > 0 }
                        ?.toString()
                        .orEmpty(),
                ),
                mapFocusRequest = if (focusOnMap && item.point != null) {
                    nextMapFocusRequest(item.point)
                } else {
                    state.mapFocusRequest
                },
            )
        }

        loadAnnouncementDetails(announcementId)
    }

    private fun loadAnnouncementDetails(announcementId: String) {
        detailLoadJob?.cancel()
        detailLoadJob = viewModelScope.launch {
            when (val result = announcementRepository.fetchAnnouncement(announcementId)) {
                is ApiResult.Success -> {
                    replaceAnnouncement(result.value)
                    syncPresentation()
                    _uiState.update { state ->
                        state.copy(
                            detailsState = state.detailsState.copy(
                                item = buildItemForId(announcementId),
                                isLoading = false,
                                loadErrorMessage = null,
                                actionErrorMessage = null,
                                customPriceInput = state.detailsState.customPriceInput.ifBlank {
                                    AnnouncementDiscoveryFilterEngine.defaultCustomPrice(result.value)
                                        .takeIf { it > 0 }
                                        ?.toString()
                                        .orEmpty()
                                },
                            ),
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            detailsState = state.detailsState.copy(
                                isLoading = false,
                                loadErrorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun submitOffer(
        announcementId: String,
        pricingMode: OfferPricingMode,
        proposedPrice: Int?,
        agreedPrice: Int?,
        minimumPriceAccepted: Boolean,
        isQuickOffer: Boolean,
    ) {
        if (_uiState.value.detailsState.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    detailsState = state.detailsState.copy(
                        isSubmittingQuickOffer = isQuickOffer,
                        isSubmittingCustomOffer = !isQuickOffer,
                        customPriceError = null,
                        actionErrorMessage = null,
                        offerSuccessState = null,
                    ),
                )
            }

            when (
                val result = announcementRepository.createOffer(
                    announcementId = announcementId,
                    message = _uiState.value.detailsState.offerMessage,
                    proposedPrice = proposedPrice,
                    pricingMode = pricingMode,
                    agreedPrice = agreedPrice,
                    minimumPriceAccepted = minimumPriceAccepted,
                )
            ) {
                is ApiResult.Success -> {
                    locallyRespondedIds += announcementId
                    refreshAnnouncementSilently(announcementId)
                    syncPresentation()
                    _uiState.update { state ->
                        state.copy(
                            detailsState = state.detailsState.copy(
                                item = buildItemForId(announcementId),
                                isSubmittingQuickOffer = false,
                                isSubmittingCustomOffer = false,
                                offerMessage = "",
                                customPriceError = null,
                                isCustomPriceDialogVisible = false,
                                offerSuccessState = DiscoveryOfferSuccessState.Submitted,
                                actionErrorMessage = null,
                            ),
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            detailsState = state.detailsState.copy(
                                isSubmittingQuickOffer = false,
                                isSubmittingCustomOffer = false,
                                actionErrorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshAnnouncementSilently(announcementId: String) {
        when (val result = announcementRepository.fetchAnnouncement(announcementId)) {
            is ApiResult.Success -> replaceAnnouncement(result.value)
            is ApiResult.Failure -> Unit
        }
    }

    private fun replaceAnnouncement(announcement: Announcement) {
        allAnnouncements = if (allAnnouncements.any { it.id == announcement.id }) {
            allAnnouncements.map { existing ->
                if (existing.id == announcement.id) announcement else existing
            }
        } else {
            allAnnouncements + announcement
        }
    }

    private fun syncPresentation() {
        val state = _uiState.value
        val baseItems = AnnouncementDiscoveryFilterEngine.buildItems(
            announcements = allAnnouncements,
            apiBaseUrl = state.apiBaseUrl,
            filters = state.filters,
            query = state.searchQuery,
            currentUserId = activeSession.user?.id,
            canRespondWithoutGate = canRespondWithoutGate(),
            locallyRespondedIds = locallyRespondedIds,
        )
        val routeMatchedIds = state.routeState.matchedAnnouncements
            .map { it.item.announcementId }
            .toSet()
        val items = if (state.filters.onlyOnRoute && state.routeState.isActive) {
            baseItems.filter { item -> routeMatchedIds.contains(item.announcement.id) }
        } else {
            baseItems
        }
        val refreshedDetailsItem = state.detailsState.announcementId?.let(::buildItemForId)

        _uiState.update { current ->
            current.copy(
                totalAnnouncementCount = allAnnouncements.count(Announcement::canAppearOnMap),
                announcements = items,
                mapViewport = AnnouncementDiscoveryFilterEngine.buildMapViewport(items),
                detailsState = if (current.detailsState.isVisible && refreshedDetailsItem != null) {
                    current.detailsState.copy(item = refreshedDetailsItem)
                } else {
                    current.detailsState
                },
            )
        }
    }

    private fun buildItemForId(announcementId: String): DiscoveryAnnouncementItemUi? {
        val announcement = allAnnouncements.firstOrNull { it.id == announcementId } ?: return null
        return AnnouncementDiscoveryFilterEngine.buildItem(
            announcement = announcement,
            apiBaseUrl = _uiState.value.apiBaseUrl,
            currentUserId = activeSession.user?.id,
            canRespondWithoutGate = canRespondWithoutGate(),
            locallyRespondedIds = locallyRespondedIds,
        )
    }

    private fun canRespondWithoutGate(): Boolean =
        !activeSession.authEnabled || activeSession.accessToken != null

    private fun nextMapFocusRequest(point: GeoPoint): DiscoveryMapFocusRequest {
        mapFocusToken += 1
        return DiscoveryMapFocusRequest(
            token = mapFocusToken,
            point = point,
        )
    }

    // ══════════════════════════════════════════════
    // Route ("по пути") functionality
    // ══════════════════════════════════════════════

    fun activateRoute(startPoint: GeoPoint, endPoint: GeoPoint, startAddress: String, endAddress: String) {
        val requestToken = ++mainRouteRequestToken
        branchRouteRequestToken++
        rebuildRouteRequestToken++
        acceptedAnnouncementIds.clear()

        _uiState.update { state ->
            state.copy(
                filters = state.filters.copy(onlyOnRoute = true),
                filterDraft = state.filterDraft.copy(onlyOnRoute = true),
                routeState = RouteState(
                    isActive = true,
                    isBuilding = true,
                    draftStartAddress = startAddress,
                    draftEndAddress = endAddress,
                    startAddress = startAddress,
                    endAddress = endAddress,
                    startPoint = startPoint,
                    endPoint = endPoint,
                    radiusMeters = state.routeState.radiusMeters,
                    statusMessage = "Строю маршрут…",
                ),
            )
        }

        // Show approximate route immediately
        val fallbackRoute = BuiltRoute(
            startTitle = startAddress,
            endTitle = endAddress,
            points = listOf(startPoint, endPoint),
            source = RouteSource.FALLBACK,
            durationSeconds = estimateDuration(listOf(startPoint, endPoint)),
        )
        applyMainRoute(fallbackRoute, "Показываю приблизительный маршрут…")

        // Request precise route from Yandex
        requestDrivingRoute(
            start = startPoint,
            end = endPoint,
            onSuccess = { route ->
                if (requestToken != mainRouteRequestToken) return@requestDrivingRoute
                val routePoints = route.geometry.points.map { GeoPoint(it.latitude, it.longitude) }
                applyMainRoute(
                    route = BuiltRoute(
                        startTitle = startAddress,
                        endTitle = endAddress,
                        points = routePoints,
                        source = RouteSource.YANDEX,
                        durationSeconds = estimateDuration(routePoints),
                    ),
                    status = "Маршрут построен через Yandex MapKit.",
                )
            },
            onError = { error ->
                if (requestToken != mainRouteRequestToken) return@requestDrivingRoute
                _uiState.update { state ->
                    state.copy(
                        routeState = state.routeState.copy(
                            isBuilding = false,
                            statusMessage = "Yandex не ответил: $error. Маршрут приблизительный.",
                        ),
                    )
                }
            },
            isMainRoute = true,
        )
    }

    fun deactivateRoute(clearDraft: Boolean = false) {
        mainRouteSession?.cancel()
        branchRouteSession?.cancel()
        rebuildRouteSession?.cancel()
        acceptedAnnouncementIds.clear()

        _uiState.update { state ->
            val previous = state.routeState
            state.copy(
                routeState = RouteState(
                    draftStartAddress = if (clearDraft) "" else previous.draftStartAddress.ifBlank { previous.startAddress },
                    draftEndAddress = if (clearDraft) "" else previous.draftEndAddress.ifBlank { previous.endAddress },
                    radiusMeters = previous.radiusMeters,
                ),
                filters = state.filters.copy(
                    onlyOnRoute = false,
                ),
                filterDraft = state.filterDraft.copy(
                    onlyOnRoute = false,
                ),
            )
        }
        syncPresentation()
    }

    fun updateRouteRadius(radiusMeters: Int) {
        _uiState.update { state ->
            state.copy(routeState = state.routeState.copy(radiusMeters = radiusMeters))
        }
        refreshRouteMatches()
    }

    fun selectRouteAnnouncement(announcementId: String) {
        if (acceptedAnnouncementIds.contains(announcementId)) {
            _uiState.update { state ->
                state.copy(
                    routeState = state.routeState.copy(
                        selectedAnnouncementId = announcementId,
                        selectedBranchPoints = emptyList(),
                    ),
                )
            }
            return
        }

        val routeState = _uiState.value.routeState
        val preview = routeState.previewBranches.firstOrNull { it.item.announcementId == announcementId }
            ?: return
        val requestToken = ++branchRouteRequestToken

        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    selectedAnnouncementId = announcementId,
                    selectedBranchPoints = preview.directBranchPoints,
                ),
            )
        }

        // Request precise branch from Yandex
        requestDrivingRoute(
            start = preview.projection.coordinate,
            end = preview.item.point,
            onSuccess = { branchRoute ->
                if (requestToken != branchRouteRequestToken) return@requestDrivingRoute
                if (_uiState.value.routeState.selectedAnnouncementId != announcementId) return@requestDrivingRoute
                val branchPoints = branchRoute.geometry.points.map { GeoPoint(it.latitude, it.longitude) }
                _uiState.update { state ->
                    state.copy(
                        routeState = state.routeState.copy(selectedBranchPoints = branchPoints),
                    )
                }
            },
            onError = { /* Keep fallback branch */ },
            isMainRoute = false,
        )
    }

    fun clearRouteSelection() {
        branchRouteRequestToken++
        branchRouteSession?.cancel()
        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    selectedAnnouncementId = null,
                    selectedBranchPoints = emptyList(),
                ),
            )
        }
    }

    fun acceptRouteAnnouncement(announcementId: String) {
        if (acceptedAnnouncementIds.contains(announcementId)) return
        if (acceptedAnnouncementIds.size >= MAX_ACCEPTED_TASKS) return

        acceptedAnnouncementIds.add(announcementId)
        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    selectedAnnouncementId = announcementId,
                    selectedBranchPoints = emptyList(),
                    acceptedAnnouncementIds = acceptedAnnouncementIds.toList(),
                ),
            )
        }
        rebuildRouteWithAccepted()
    }

    fun removeAcceptedRouteAnnouncement(announcementId: String) {
        if (!acceptedAnnouncementIds.remove(announcementId)) return
        val selectedId = _uiState.value.routeState.selectedAnnouncementId
        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    selectedAnnouncementId = if (selectedId == announcementId) null else selectedId,
                    selectedBranchPoints = if (selectedId == announcementId) emptyList() else state.routeState.selectedBranchPoints,
                    acceptedAnnouncementIds = acceptedAnnouncementIds.toList(),
                ),
            )
        }
        rebuildRouteWithAccepted()
    }

    private fun applyMainRoute(route: BuiltRoute, status: String) {
        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    isBuilding = false,
                    currentRoute = route,
                    baseRoute = if (state.routeState.baseRoute == null) route else state.routeState.baseRoute,
                    statusMessage = status,
                ),
            )
        }
        refreshRouteMatches()
    }

    private fun refreshRouteMatches() {
        val routeState = _uiState.value.routeState
        val route = routeState.currentRoute ?: return
        if (route.points.size < 2) return

        val announcementPoints = buildRouteAnnouncementPoints()
        val rawMatches = RouteMath.filterAnnouncementsNearRoute(
            points = announcementPoints,
            polyline = route.points,
            radiusMeters = routeState.radiusMeters,
        )

        val nonAccepted = rawMatches.filter { !acceptedAnnouncementIds.contains(it.item.announcementId) }
        val branches = nonAccepted.mapNotNull { match ->
            RouteMath.closestProjectionOnRoute(match.item.point, route.points)?.let { projection ->
                PreviewBranch(
                    item = match.item,
                    distanceToRouteMeters = match.distanceToRouteMeters,
                    projection = projection,
                )
            }
        }.sortedBy { it.projection.progress }

        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    matchedAnnouncements = rawMatches,
                    previewBranches = branches,
                ),
            )
        }
        syncPresentation()
    }

    private fun buildRouteAnnouncementPoints(): List<RouteAnnouncementPoint> {
        return allAnnouncements
            .filter(Announcement::canAppearOnMap)
            .mapNotNull { announcement ->
            val point = announcement.mapPoint ?: return@mapNotNull null
            RouteAnnouncementPoint(
                announcementId = announcement.id,
                title = announcement.title,
                addressText = announcement.primarySourceAddress ?: "",
                point = point,
                budgetText = announcement.formattedBudgetText,
            )
        }
    }

    private fun rebuildRouteWithAccepted() {
        val routeState = _uiState.value.routeState
        val base = routeState.baseRoute ?: return
        val requestToken = ++rebuildRouteRequestToken
        branchRouteRequestToken++
        rebuildRouteSession?.cancel()

        if (acceptedAnnouncementIds.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    routeState = state.routeState.copy(
                        currentRoute = base,
                        statusMessage = "Маршрут восстановлен до исходного.",
                    ),
                )
            }
            refreshRouteMatches()
            return
        }

        val orderedWaypoints = orderedAcceptedWaypoints(base)
        if (orderedWaypoints.isEmpty()) {
            _uiState.update { state ->
                state.copy(routeState = state.routeState.copy(currentRoute = base))
            }
            refreshRouteMatches()
            return
        }

        // Show approximate route immediately
        val approxPoints = mutableListOf(base.points.first())
        approxPoints.addAll(orderedWaypoints)
        approxPoints.add(base.points.last())

        _uiState.update { state ->
            state.copy(
                routeState = state.routeState.copy(
                    isBuilding = true,
                    currentRoute = BuiltRoute(
                        startTitle = base.startTitle,
                        endTitle = base.endTitle,
                        points = approxPoints,
                        source = RouteSource.FALLBACK,
                        durationSeconds = estimateDuration(approxPoints),
                    ),
                    statusMessage = "Перестраиваю маршрут через ${acceptedAnnouncementIds.size} задач…",
                ),
            )
        }
        refreshRouteMatches()

        // Request precise route with all waypoints
        val requestPoints = mutableListOf<RequestPoint>()
        requestPoints.add(RequestPoint(base.points.first().toYandexPoint(), RequestPointType.WAYPOINT, null, null, null))
        for (wp in orderedWaypoints) {
            requestPoints.add(RequestPoint(wp.toYandexPoint(), RequestPointType.WAYPOINT, null, null, null))
        }
        requestPoints.add(RequestPoint(base.points.last().toYandexPoint(), RequestPointType.WAYPOINT, null, null, null))

        val listener = object : DrivingSession.DrivingRouteListener {
            override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                if (requestToken != rebuildRouteRequestToken) return
                val bestRoute = routes.firstOrNull()
                if (bestRoute != null) {
                    val routePoints = bestRoute.geometry.points.map { GeoPoint(it.latitude, it.longitude) }
                    _uiState.update { state ->
                        state.copy(
                            routeState = state.routeState.copy(
                                isBuilding = false,
                                currentRoute = BuiltRoute(
                                    startTitle = base.startTitle,
                                    endTitle = base.endTitle,
                                    points = routePoints,
                                    source = RouteSource.YANDEX,
                                    durationSeconds = estimateDuration(routePoints),
                                ),
                                statusMessage = "Маршрут перестроен через ${acceptedAnnouncementIds.size} задач.",
                            ),
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            routeState = state.routeState.copy(
                                isBuilding = false,
                                statusMessage = "Yandex не вернул маршрут. Показан приблизительно.",
                            ),
                        )
                    }
                }
                refreshRouteMatches()
            }

            override fun onDrivingRoutesError(error: Error) {
                if (requestToken != rebuildRouteRequestToken) return
                _uiState.update { state ->
                    state.copy(
                        routeState = state.routeState.copy(
                            isBuilding = false,
                            statusMessage = "Ошибка перестроения маршрута.",
                        ),
                    )
                }
            }
        }

        rebuildRouteSession = drivingRouter.requestRoutes(
            requestPoints,
            DrivingOptions().setRoutesCount(1),
            VehicleOptions(),
            listener,
        )
    }

    private fun orderedAcceptedWaypoints(baseRoute: BuiltRoute): List<GeoPoint> {
        return allAnnouncements
            .filter { acceptedAnnouncementIds.contains(it.id) }
            .mapNotNull { announcement ->
                val point = announcement.mapPoint ?: return@mapNotNull null
                val projection = RouteMath.closestProjectionOnRoute(point, baseRoute.points)
                if (projection != null) Pair(point, projection.progress) else null
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun requestDrivingRoute(
        start: GeoPoint,
        end: GeoPoint,
        onSuccess: (DrivingRoute) -> Unit,
        onError: (String) -> Unit,
        isMainRoute: Boolean,
    ) {
        if (isMainRoute) mainRouteSession?.cancel() else branchRouteSession?.cancel()

        val listener = object : DrivingSession.DrivingRouteListener {
            override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                val bestRoute = routes.firstOrNull()
                if (bestRoute != null) onSuccess(bestRoute) else onError("Нет маршрутов")
            }

            override fun onDrivingRoutesError(error: Error) {
                onError(error.toString())
            }
        }

        val points = listOf(
            RequestPoint(start.toYandexPoint(), RequestPointType.WAYPOINT, null, null, null),
            RequestPoint(end.toYandexPoint(), RequestPointType.WAYPOINT, null, null, null),
        )
        val session = drivingRouter.requestRoutes(
            points,
            DrivingOptions().setRoutesCount(1),
            VehicleOptions(),
            listener,
        )

        if (isMainRoute) mainRouteSession = session else branchRouteSession = session
    }

    private fun estimateDuration(points: List<GeoPoint>): Int {
        val meters = RouteMath.polylineLengthMeters(points)
        return (meters / 8.33).toInt().coerceAtLeast(60) // ~30 km/h average city speed
    }

    private suspend fun resolveRoutePoint(input: String): GeoPoint? {
        parseExplicitCoordinates(input)?.let { return it }

        val geocoder = Geocoder(context)
        if (!Geocoder.isPresent()) return null

        return withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(input, 1)
                    ?.firstOrNull()
                    ?.let { address ->
                        GeoPoint(
                            latitude = address.latitude,
                            longitude = address.longitude,
                        )
                    }
            }.getOrNull()
        }
    }

    private fun parseExplicitCoordinates(raw: String): GeoPoint? {
        val match = COORDINATE_REGEX.find(raw.trim()) ?: return null
        val latitude = match.groupValues.getOrNull(1)?.toDoubleOrNull() ?: return null
        val longitude = match.groupValues.getOrNull(2)?.toDoubleOrNull() ?: return null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
        return GeoPoint(latitude = latitude, longitude = longitude)
    }

    companion object {
        private const val MAX_ACCEPTED_TASKS = 2
        private val COORDINATE_REGEX =
            Regex("""^\s*(-?\d+(?:\.\d+)?)\s*[,; ]\s*(-?\d+(?:\.\d+)?)\s*$""")
    }
}

private fun GeoPoint.toYandexPoint(): Point = Point(latitude, longitude)
