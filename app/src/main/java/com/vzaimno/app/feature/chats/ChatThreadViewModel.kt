package com.vzaimno.app.feature.chats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.model.ChatThreadPreview
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ChatRepository
import com.vzaimno.app.data.repository.ProfileRepository
import com.vzaimno.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val previewStore: ChatThreadPreviewStore,
    private val realtimeTransport: PollingChatRealtimeTransport,
) : ViewModel() {

    private val initialThreadId: String? = savedStateHandle[ChatsDestination.threadIdArgument]
    private val initialKind = ChatConversationKind.fromRaw(
        savedStateHandle.get<String>(ChatsDestination.threadKindArgument),
    )

    private val _uiState = MutableStateFlow(
        ChatThreadUiState(
            messagesState = ChatMessagesState(
                threadId = initialThreadId,
                kind = initialKind,
            ),
        ),
    )
    val uiState: StateFlow<ChatThreadUiState> = _uiState.asStateFlow()

    private var currentPreview: ChatThreadPreview? = previewStore.get(initialThreadId)
    private var currentMessages: List<ChatMessage> = emptyList()
    private var currentUserId: String? = sessionManager.activeSession.value.user?.id
    private var isSupportEntry = false
    private var isActive = false
    private var previewHydrated = false
    private var transportJob: Job? = null

    init {
        syncHeaderFromPreview()

        viewModelScope.launch {
            sessionManager.activeSession.collect { session ->
                currentUserId = session.user?.id
                _uiState.update { state ->
                    state.copy(
                        messagesState = state.messagesState.copy(
                            messages = currentMessages.map { message -> message.toUi(currentUserId) },
                        ),
                    )
                }
                updateReportTarget()
            }
        }
    }

    fun configure(isSupportEntry: Boolean) {
        if (this.isSupportEntry == isSupportEntry) return
        this.isSupportEntry = isSupportEntry
        _uiState.update { state ->
            state.copy(
                supportThreadState = state.supportThreadState.copy(
                    isSupportEntry = isSupportEntry,
                ),
            )
        }
    }

    fun onAppear() {
        if (isActive) return
        isActive = true

        if (isSupportEntry) {
            resolveSupportThreadAndLoad()
        } else {
            loadConversation(showLoader = true)
        }
    }

    fun onDisappear() {
        isActive = false
        stopTransport()
        _uiState.update { state ->
            state.copy(
                transportState = state.transportState.copy(
                    isActive = false,
                    statusMessage = null,
                ),
            )
        }
    }

    fun retry() {
        if (isSupportEntry && _uiState.value.supportThreadState.resolvedThreadId == null) {
            resolveSupportThreadAndLoad()
        } else {
            loadConversation(showLoader = _uiState.value.messagesState.messages.isEmpty())
        }
    }

    fun refresh() {
        loadConversation(showLoader = false)
    }

    fun updateComposerText(value: String) {
        _uiState.update { state ->
            state.copy(composerText = value)
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val threadId = resolvedThreadId() ?: return
        val messageText = state.composerText.trim()
        if (messageText.isEmpty() || state.isSending) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isSending = true)
            }

            when (val result = sendCurrentMessage(threadId = threadId, text = messageText)) {
                is ApiResult.Success -> {
                    applyMessages(
                        messages = mergeMessages(currentMessages, listOf(result.value)),
                        receivedAtEpochSeconds = System.currentTimeMillis() / 1_000L,
                    )
                    _uiState.update { current ->
                        current.copy(
                            composerText = "",
                            isSending = false,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { current ->
                        current.copy(
                            isSending = false,
                            messagesState = current.messagesState.copy(
                                errorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun dismissReportSheet() {
        _uiState.update { state ->
            state.copy(
                reportState = state.reportState.copy(
                    isSheetVisible = false,
                    errorMessage = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun showReportSheet() {
        val target = resolveReportTarget(currentPreview, currentMessages, currentUserId)
        if (target == null) {
            _uiState.update { state ->
                state.copy(
                    reportState = state.reportState.copy(
                        errorMessage = "Не удалось определить объект жалобы.",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            if (_uiState.value.reportState.options.isEmpty()) {
                _uiState.update { state ->
                    state.copy(
                        reportState = state.reportState.copy(
                            isLoadingOptions = true,
                            errorMessage = null,
                            successMessage = null,
                        ),
                    )
                }

                when (val result = chatRepository.fetchReportReasonOptions()) {
                    is ApiResult.Success -> {
                        val filteredOptions = result.value.filter { option ->
                            option.supports(target.type)
                        }.ifEmpty {
                            result.value
                        }
                        _uiState.update { state ->
                            state.copy(
                                reportState = state.reportState.copy(
                                    isSheetVisible = true,
                                    isLoadingOptions = false,
                                    options = filteredOptions,
                                    targetSummary = target.summary,
                                    selectedReasonCode = state.reportState.selectedReasonCode
                                        ?.takeIf { selected ->
                                            filteredOptions.any { option -> option.code == selected }
                                        },
                                ),
                            )
                        }
                    }

                    is ApiResult.Failure -> {
                        _uiState.update { state ->
                            state.copy(
                                reportState = state.reportState.copy(
                                    isLoadingOptions = false,
                                    errorMessage = result.error.message,
                                ),
                            )
                        }
                    }
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        reportState = state.reportState.copy(
                            isSheetVisible = true,
                            targetSummary = target.summary,
                            errorMessage = null,
                            successMessage = null,
                        ),
                    )
                }
            }
        }
    }

    fun selectReportReason(code: String) {
        _uiState.update { state ->
            state.copy(
                reportState = state.reportState.copy(
                    selectedReasonCode = code,
                    errorMessage = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun updateReportComment(value: String) {
        _uiState.update { state ->
            state.copy(
                reportState = state.reportState.copy(
                    comment = value,
                    errorMessage = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun submitReport() {
        val target = resolveReportTarget(currentPreview, currentMessages, currentUserId) ?: return
        val selectedReason = _uiState.value.reportState.selectedReasonCode
        if (selectedReason.isNullOrBlank()) {
            _uiState.update { state ->
                state.copy(
                    reportState = state.reportState.copy(
                        errorMessage = "Выберите причину жалобы.",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    reportState = state.reportState.copy(
                        isSubmitting = true,
                        errorMessage = null,
                        successMessage = null,
                    ),
                )
            }

            when (
                val result = chatRepository.submitReport(
                    targetType = target.type,
                    targetId = target.id,
                    reasonCode = selectedReason,
                    reasonText = _uiState.value.reportState.comment,
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            reportState = state.reportState.copy(
                                isSubmitting = false,
                                isSheetVisible = false,
                                comment = "",
                                selectedReasonCode = null,
                                successMessage = "Жалоба отправлена.",
                            ),
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            reportState = state.reportState.copy(
                                isSubmitting = false,
                                errorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun showReviewDialog() {
        if (_uiState.value.reviewState.canOpenComposer) {
            _uiState.update { state ->
                state.copy(
                    reviewState = state.reviewState.copy(
                        isDialogVisible = true,
                        errorMessage = null,
                        successMessage = null,
                    ),
                )
            }
        }
    }

    fun dismissReviewDialog() {
        _uiState.update { state ->
            state.copy(
                reviewState = state.reviewState.copy(
                    isDialogVisible = false,
                    errorMessage = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun updateReviewStars(stars: Int) {
        _uiState.update { state ->
            state.copy(
                reviewState = state.reviewState.copy(
                    selectedStars = stars.coerceIn(1, 5),
                    errorMessage = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun updateReviewComment(value: String) {
        _uiState.update { state ->
            state.copy(
                reviewState = state.reviewState.copy(
                    comment = value,
                    errorMessage = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun submitReview() {
        val announcementId = currentPreview?.announcementId
            ?.takeIf { it.isNotBlank() }
            ?: return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    reviewState = state.reviewState.copy(
                        isSubmitting = true,
                        errorMessage = null,
                        successMessage = null,
                    ),
                )
            }

            when (
                val result = profileRepository.submitReview(
                    announcementId = announcementId,
                    stars = _uiState.value.reviewState.selectedStars,
                    text = _uiState.value.reviewState.comment,
                )
            ) {
                is ApiResult.Success -> {
                    loadReviewEligibility()
                    _uiState.update { state ->
                        state.copy(
                            reviewState = state.reviewState.copy(
                                isDialogVisible = false,
                                isSubmitting = false,
                                comment = "",
                                successMessage = "Отзыв сохранён.",
                            ),
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            reviewState = state.reviewState.copy(
                                isSubmitting = false,
                                errorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun resolveSupportThreadAndLoad() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    supportThreadState = state.supportThreadState.copy(
                        isSupportEntry = true,
                        isResolving = true,
                        errorMessage = null,
                    ),
                    messagesState = state.messagesState.copy(
                        kind = ChatConversationKind.Support,
                        isInitialLoading = true,
                        errorMessage = null,
                    ),
                )
            }

            when (val result = chatRepository.ensureSupportThread()) {
                is ApiResult.Success -> {
                    currentPreview = previewStore.get(result.value) ?: buildSupportPreview(result.value)
                    previewStore.upsert(currentPreview ?: buildSupportPreview(result.value))
                    previewHydrated = true
                    _uiState.update { state ->
                        state.copy(
                            supportThreadState = state.supportThreadState.copy(
                                isResolving = false,
                                resolvedThreadId = result.value,
                                errorMessage = null,
                            ),
                            messagesState = state.messagesState.copy(
                                threadId = result.value,
                                kind = ChatConversationKind.Support,
                            ),
                        )
                    }
                    syncHeaderFromPreview()
                    loadConversation(showLoader = true)
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            supportThreadState = state.supportThreadState.copy(
                                isResolving = false,
                                errorMessage = result.error.message,
                            ),
                            messagesState = state.messagesState.copy(
                                isInitialLoading = false,
                                errorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun loadConversation(showLoader: Boolean) {
        val threadId = resolvedThreadId()
        if (threadId == null) {
            hydratePreviewIfNeeded()
            _uiState.update { state ->
                state.copy(
                    messagesState = state.messagesState.copy(
                        isInitialLoading = false,
                        errorMessage = "Не удалось открыть чат.",
                    ),
                )
            }
            return
        }

        hydratePreviewIfNeeded()

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    messagesState = state.messagesState.copy(
                        isInitialLoading = showLoader,
                        isRefreshing = !showLoader,
                        errorMessage = null,
                    ),
                )
            }

            when (val result = fetchMessages(threadId)) {
                is ApiResult.Success -> {
                    applyMessages(
                        messages = result.value,
                        receivedAtEpochSeconds = System.currentTimeMillis() / 1_000L,
                    )
                    _uiState.update { state ->
                        state.copy(
                            messagesState = state.messagesState.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                errorMessage = null,
                            ),
                        )
                    }
                    loadReviewEligibility()
                    if (isActive) {
                        startTransport(threadId)
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            messagesState = state.messagesState.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                errorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun hydratePreviewIfNeeded() {
        if (previewHydrated) return

        val threadId = resolvedThreadId() ?: return
        currentPreview = previewStore.get(threadId)
        if (currentPreview != null) {
            previewHydrated = true
            syncHeaderFromPreview()
            updateReportTarget()
            return
        }

        viewModelScope.launch {
            when (val result = chatRepository.fetchThreads()) {
                is ApiResult.Success -> {
                    previewStore.replaceAll(result.value)
                    currentPreview = result.value.firstOrNull { preview -> preview.threadId == threadId }
                    previewHydrated = true
                    syncHeaderFromPreview()
                    updateReportTarget()
                }

                is ApiResult.Failure -> Unit
            }
        }
    }

    private fun startTransport(threadId: String) {
        stopTransport()
        transportJob = viewModelScope.launch {
            realtimeTransport.stream(
                ChatTransportConfig(
                    threadId = threadId,
                    kind = resolvedKind(),
                ),
            ).collect { event ->
                when (event) {
                    is ChatRealtimeEvent.Connected -> {
                        _uiState.update { state ->
                            state.copy(
                                transportState = state.transportState.copy(
                                    mode = event.mode,
                                    isActive = true,
                                    statusMessage = null,
                                ),
                            )
                        }
                    }

                    is ChatRealtimeEvent.MessagesSnapshot -> {
                        applyMessages(
                            messages = event.messages,
                            receivedAtEpochSeconds = event.receivedAtEpochSeconds,
                        )
                        _uiState.update { state ->
                            state.copy(
                                transportState = state.transportState.copy(
                                    mode = realtimeTransport.mode,
                                    isActive = true,
                                    statusMessage = null,
                                    lastSyncEpochSeconds = event.receivedAtEpochSeconds,
                                ),
                            )
                        }
                    }

                    is ChatRealtimeEvent.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                transportState = state.transportState.copy(
                                    mode = realtimeTransport.mode,
                                    isActive = true,
                                    statusMessage = "Не удалось обновить чат. Повторим автоматически.",
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun stopTransport() {
        transportJob?.cancel()
        transportJob = null
    }

    private fun applyMessages(
        messages: List<ChatMessage>,
        receivedAtEpochSeconds: Long,
    ) {
        currentMessages = messages
            .distinctBy(ChatMessage::id)
            .sortedBy(ChatMessage::createdAtEpochSeconds)

        val preview = currentPreview
        val header = preview?.toHeaderUi()
            ?: _uiState.value.messagesState.header

        _uiState.update { state ->
            state.copy(
                messagesState = state.messagesState.copy(
                    threadId = resolvedThreadId(),
                    kind = resolvedKind(),
                    preview = preview,
                    header = header,
                    messages = currentMessages.map { message -> message.toUi(currentUserId) },
                ),
            )
        }

        syncPreviewStore()
        updateReportTarget()
        _uiState.update { state ->
            state.copy(
                transportState = state.transportState.copy(
                    lastSyncEpochSeconds = receivedAtEpochSeconds,
                ),
            )
        }
    }

    private fun loadReviewEligibility() {
        val announcementId = currentPreview?.announcementId
            ?.takeIf { it.isNotBlank() }

        if (announcementId == null) {
            _uiState.update { state ->
                state.copy(
                    reviewState = state.reviewState.copy(
                        isLoadingEligibility = false,
                        eligibility = null,
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    reviewState = state.reviewState.copy(
                        isLoadingEligibility = true,
                    ),
                )
            }

            when (val result = profileRepository.fetchReviewEligibility(announcementId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            reviewState = state.reviewState.copy(
                                isLoadingEligibility = false,
                                eligibility = result.value,
                            ),
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            reviewState = state.reviewState.copy(
                                isLoadingEligibility = false,
                                errorMessage = result.error.message,
                            ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchMessages(threadId: String): ApiResult<List<ChatMessage>> = when (resolvedKind()) {
        ChatConversationKind.Support -> chatRepository.fetchSupportMessages(
            threadId = threadId,
            limit = 50,
        )

        ChatConversationKind.Direct,
        ChatConversationKind.Unknown,
        -> chatRepository.fetchMessages(
            threadId = threadId,
            limit = 50,
        )
    }

    private suspend fun sendCurrentMessage(
        threadId: String,
        text: String,
    ): ApiResult<ChatMessage> = when (resolvedKind()) {
        ChatConversationKind.Support -> chatRepository.sendSupportMessage(
            threadId = threadId,
            text = text,
        )

        ChatConversationKind.Direct,
        ChatConversationKind.Unknown,
        -> chatRepository.sendMessage(
            threadId = threadId,
            text = text,
        )
    }

    private fun syncHeaderFromPreview() {
        currentPreview = previewStore.get(resolvedThreadId()) ?: currentPreview
        val preview = currentPreview

        _uiState.update { state ->
            state.copy(
                messagesState = state.messagesState.copy(
                    preview = preview,
                    header = preview?.toHeaderUi() ?: state.messagesState.header,
                ),
            )
        }
        updateReportTarget()
    }

    private fun updateReportTarget() {
        val target = resolveReportTarget(currentPreview, currentMessages, currentUserId)
        val options = _uiState.value.reportState.options.filter { option ->
            target == null || option.supports(target.type)
        }.ifEmpty {
            _uiState.value.reportState.options
        }
        _uiState.update { state ->
            state.copy(
                reportState = state.reportState.copy(
                    options = options,
                    targetSummary = target?.summary,
                ),
            )
        }
    }

    private fun syncPreviewStore() {
        val preview = currentPreview ?: buildSupportPreview(resolvedThreadId() ?: return)
        val latestMessage = currentMessages.lastOrNull()
        val updatedPreview = preview.copy(
            lastMessageText = latestMessage?.text ?: preview.lastMessageText,
            lastMessageAtEpochSeconds = latestMessage?.createdAtEpochSeconds ?: preview.lastMessageAtEpochSeconds,
            unreadCount = 0,
        )
        currentPreview = updatedPreview
        previewStore.upsert(updatedPreview)
        syncHeaderFromPreview()
    }

    private fun resolvedThreadId(): String? = when {
        isSupportEntry -> _uiState.value.supportThreadState.resolvedThreadId ?: initialThreadId
        else -> _uiState.value.messagesState.threadId ?: initialThreadId
    }

    private fun resolvedKind(): ChatConversationKind = when {
        isSupportEntry -> ChatConversationKind.Support
        _uiState.value.messagesState.kind != ChatConversationKind.Unknown -> _uiState.value.messagesState.kind
        else -> currentPreview?.let { preview ->
            ChatConversationKind.fromRaw(preview.kind)
        } ?: initialKind
    }
}
