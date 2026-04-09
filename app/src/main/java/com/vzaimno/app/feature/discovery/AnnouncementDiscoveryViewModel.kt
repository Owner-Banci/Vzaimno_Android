package com.vzaimno.app.feature.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.BuildConfig
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.OfferPricingMode
import com.vzaimno.app.core.model.canAppearOnMap
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ActiveSession
import com.vzaimno.app.data.repository.AnnouncementRepository
import com.vzaimno.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AnnouncementDiscoveryViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val sessionManager: SessionManager,
    appConfig: AppConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DiscoveryUiState(
            apiBaseUrl = appConfig.normalizedApiBaseUrl,
            isMapConfigured = BuildConfig.GOOGLE_MAPS_API_KEY.isNotBlank(),
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

    init {
        viewModelScope.launch {
            sessionManager.activeSession.collectLatest { session ->
                activeSession = session
                syncPresentation()
            }
        }
    }

    fun loadIfNeeded() {
        if (hasLoaded || loadJob?.isActive == true) return
        load(showLoadingState = true)
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
            state.copy(filterDraft = DiscoveryFilterState())
        }
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
        syncPresentation()
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
        val items = AnnouncementDiscoveryFilterEngine.buildItems(
            announcements = allAnnouncements,
            apiBaseUrl = state.apiBaseUrl,
            filters = state.filters,
            query = state.searchQuery,
            currentUserId = activeSession.user?.id,
            canRespondWithoutGate = canRespondWithoutGate(),
            locallyRespondedIds = locallyRespondedIds,
        )
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

    private fun nextMapFocusRequest(point: com.vzaimno.app.core.model.GeoPoint): DiscoveryMapFocusRequest {
        mapFocusToken += 1
        return DiscoveryMapFocusRequest(
            token = mapFocusToken,
            point = point,
        )
    }
}
