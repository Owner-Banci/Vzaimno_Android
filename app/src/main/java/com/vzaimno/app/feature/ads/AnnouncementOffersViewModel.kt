package com.vzaimno.app.feature.ads

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.model.AnnouncementOffer
import com.vzaimno.app.core.model.AnnouncementOfferStatus
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AnnouncementOffersUiState(
    val announcementId: String = "",
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val offers: List<AnnouncementOffer> = emptyList(),
    val loadErrorMessage: String? = null,
    val contentMessage: String? = null,
    val processingOffers: Map<String, OfferDecisionAction> = emptyMap(),
) {
    fun processingActionFor(offerId: String): OfferDecisionAction? = processingOffers[offerId]
}

sealed interface AnnouncementOffersEvent {
    data class OfferAccepted(val threadId: String) : AnnouncementOffersEvent
    data object OfferRejected : AnnouncementOffersEvent
}

@HiltViewModel
class AnnouncementOffersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val announcementRepository: AnnouncementRepository,
) : ViewModel() {

    private val announcementId: String =
        savedStateHandle.get<String>(AdsDestination.announcementIdArgument).orEmpty()

    private val _uiState = MutableStateFlow(
        AnnouncementOffersUiState(
            announcementId = announcementId,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AnnouncementOffersEvent>()
    val events: SharedFlow<AnnouncementOffersEvent> = _events.asSharedFlow()

    private var hasLoaded = false
    private var loadJob: Job? = null
    private var currentOffers: List<AnnouncementOffer> = emptyList()

    fun loadIfNeeded() {
        if (announcementId.isBlank() || hasLoaded || loadJob?.isActive == true) return
        load(showLoadingState = true)
    }

    fun retry() {
        load(showLoadingState = currentOffers.isEmpty())
    }

    fun refresh() {
        load(showLoadingState = false)
    }

    fun clearContentMessage() {
        _uiState.update { state ->
            state.copy(contentMessage = null)
        }
    }

    fun acceptOffer(offerId: String) {
        if (announcementId.isBlank() || offerId.isBlank()) return

        viewModelScope.launch {
            markProcessing(offerId, OfferDecisionAction.Accept)
            _uiState.update { state ->
                state.copy(contentMessage = null)
            }

            when (val result = announcementRepository.acceptOffer(announcementId, offerId)) {
                is ApiResult.Success -> {
                    currentOffers = buildList {
                        add(result.value.offer)
                        addAll(
                            currentOffers.filterNot { offer ->
                                offer.id == result.value.offer.id
                            },
                        )
                    }
                        .sortedForOwner()

                    applyOffers(
                        offers = currentOffers,
                        isInitialLoading = false,
                        isRefreshing = false,
                        loadErrorMessage = null,
                        contentMessage = null,
                    )

                    refreshAfterMutation(
                        fallbackContentMessage = null,
                    )
                    _events.emit(AnnouncementOffersEvent.OfferAccepted(result.value.threadId))
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            contentMessage = result.error.message,
                        )
                    }
                }
            }

            unmarkProcessing(offerId)
        }
    }

    fun rejectOffer(offerId: String) {
        if (announcementId.isBlank() || offerId.isBlank()) return

        viewModelScope.launch {
            markProcessing(offerId, OfferDecisionAction.Reject)
            _uiState.update { state ->
                state.copy(contentMessage = null)
            }

            when (val result = announcementRepository.rejectOffer(announcementId, offerId)) {
                is ApiResult.Success -> {
                    currentOffers = currentOffers
                        .map { offer ->
                            if (offer.id == offerId) {
                                offer.copy(status = AnnouncementOfferStatus.Rejected.rawValue)
                            } else {
                                offer
                            }
                        }
                        .sortedForOwner()

                    applyOffers(
                        offers = currentOffers,
                        isInitialLoading = false,
                        isRefreshing = false,
                        loadErrorMessage = null,
                        contentMessage = null,
                    )

                    refreshAfterMutation(
                        fallbackContentMessage = null,
                    )
                    _events.emit(AnnouncementOffersEvent.OfferRejected)
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            contentMessage = result.error.message,
                        )
                    }
                }
            }

            unmarkProcessing(offerId)
        }
    }

    private fun load(showLoadingState: Boolean) {
        if (announcementId.isBlank() || loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val previousOffers = currentOffers
            _uiState.update { state ->
                state.copy(
                    isInitialLoading = showLoadingState && previousOffers.isEmpty(),
                    isRefreshing = !showLoadingState && previousOffers.isNotEmpty(),
                    loadErrorMessage = null,
                    contentMessage = null,
                )
            }

            when (val result = announcementRepository.fetchOffers(announcementId)) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    currentOffers = result.value.sortedForOwner()
                    applyOffers(
                        offers = currentOffers,
                        isInitialLoading = false,
                        isRefreshing = false,
                        loadErrorMessage = null,
                        contentMessage = null,
                    )
                }

                is ApiResult.Failure -> {
                    if (previousOffers.isEmpty()) {
                        hasLoaded = false
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                offers = emptyList(),
                                loadErrorMessage = result.error.message,
                            )
                        }
                    } else {
                        hasLoaded = true
                        _uiState.update { state ->
                            state.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                offers = previousOffers,
                                contentMessage = result.error.message,
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun refreshAfterMutation(
        fallbackContentMessage: String?,
    ) {
        when (val refreshResult = announcementRepository.fetchOffers(announcementId)) {
            is ApiResult.Success -> {
                hasLoaded = true
                currentOffers = refreshResult.value.sortedForOwner()
                applyOffers(
                    offers = currentOffers,
                    isInitialLoading = false,
                    isRefreshing = false,
                    loadErrorMessage = null,
                    contentMessage = fallbackContentMessage,
                )
            }

            is ApiResult.Failure -> {
                _uiState.update { state ->
                    state.copy(
                        contentMessage = refreshResult.error.message,
                    )
                }
            }
        }
    }

    private fun applyOffers(
        offers: List<AnnouncementOffer>,
        isInitialLoading: Boolean,
        isRefreshing: Boolean,
        loadErrorMessage: String?,
        contentMessage: String?,
    ) {
        _uiState.update { state ->
            state.copy(
                isInitialLoading = isInitialLoading,
                isRefreshing = isRefreshing,
                offers = offers,
                loadErrorMessage = loadErrorMessage,
                contentMessage = contentMessage,
            )
        }
    }

    private fun markProcessing(offerId: String, action: OfferDecisionAction) {
        _uiState.update { state ->
            state.copy(
                processingOffers = state.processingOffers + (offerId to action),
            )
        }
    }

    private fun unmarkProcessing(offerId: String) {
        _uiState.update { state ->
            state.copy(
                processingOffers = state.processingOffers - offerId,
            )
        }
    }
}
