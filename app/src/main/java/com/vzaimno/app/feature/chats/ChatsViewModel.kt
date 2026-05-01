package com.vzaimno.app.feature.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val previewStore: ChatThreadPreviewStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    private var serverThreads: List<com.vzaimno.app.core.model.ChatThreadPreview> = emptyList()
    private var hasLoaded = false
    private var hasEnsuredSupport = false
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            previewStore.previews.collectLatest { previews ->
                syncUiState(previews)
            }
        }
    }

    fun loadIfNeeded() {
        if (hasLoaded || loadJob?.isActive == true) return
        load(showLoader = true)
    }

    fun retry() {
        load(showLoader = serverThreads.isEmpty())
    }

    fun refresh() {
        load(showLoader = false)
    }

    private fun load(showLoader: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (showLoader) {
                _uiState.update { state ->
                    state.copy(
                        isInitialLoading = true,
                        isRefreshing = false,
                        errorMessage = null,
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isRefreshing = true,
                        errorMessage = null,
                    )
                }
            }

            if (!hasEnsuredSupport) {
                when (val supportResult = chatRepository.ensureSupportThread()) {
                    is ApiResult.Success -> {
                        hasEnsuredSupport = true
                        previewStore.upsert(buildSupportPreview(supportResult.value))
                    }

                    is ApiResult.Failure -> {
                        // Support entry stays available via dedicated route even if prewarm fails.
                    }
                }
            }

            when (val result = chatRepository.fetchThreads()) {
                is ApiResult.Success -> {
                    hasLoaded = true
                    serverThreads = sortChatThreads(result.value)
                    previewStore.replaceAll(serverThreads)
                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                    syncUiState(previewStore.previews.value)
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            errorMessage = result.error.message,
                        )
                    }
                    syncUiState(previewStore.previews.value)
                }
            }
        }
    }

    private fun syncUiState(previews: Map<String, com.vzaimno.app.core.model.ChatThreadPreview>) {
        val mergedThreads = buildList {
            serverThreads.forEach { preview ->
                add(previews[preview.threadId] ?: preview)
            }

            previews.values
                .firstOrNull { candidate ->
                    ChatConversationKind.fromRaw(candidate.kind) == ChatConversationKind.Support &&
                        none { existing -> existing.threadId == candidate.threadId }
                }
                ?.let(::add)
        }

        val sorted = sortChatThreads(mergedThreads)
        val supportPreview = sorted.firstOrNull { preview ->
            ChatConversationKind.fromRaw(preview.kind) == ChatConversationKind.Support
        } ?: previews.values.firstOrNull { preview ->
            ChatConversationKind.fromRaw(preview.kind) == ChatConversationKind.Support
        }

        _uiState.update { state ->
            state.copy(
                threads = sorted
                    .filterNot { preview ->
                        ChatConversationKind.fromRaw(preview.kind) == ChatConversationKind.Support
                    }
                    .map { preview -> preview.toUi() },
                supportThreadId = supportPreview?.threadId,
                supportThreadAvailable = true,
            )
        }
    }
}
