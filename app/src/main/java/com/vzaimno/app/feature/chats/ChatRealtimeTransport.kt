package com.vzaimno.app.feature.chats

import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.data.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

data class ChatTransportConfig(
    val threadId: String,
    val kind: ChatConversationKind,
    val limit: Int = 50,
    val intervalMs: Long = 8_000L,
)

sealed interface ChatRealtimeEvent {
    data class Connected(
        val mode: ChatTransportMode,
    ) : ChatRealtimeEvent

    data class MessagesSnapshot(
        val messages: List<ChatMessage>,
        val receivedAtEpochSeconds: Long,
    ) : ChatRealtimeEvent

    data class Error(
        val message: String,
    ) : ChatRealtimeEvent
}

interface ChatRealtimeTransport {
    val mode: ChatTransportMode

    fun stream(config: ChatTransportConfig): Flow<ChatRealtimeEvent>
}

@Singleton
class PollingChatRealtimeTransport @Inject constructor(
    private val chatRepository: ChatRepository,
) : ChatRealtimeTransport {
    // Polling is the verified production path for now; websocket transport can slot into the
    // same interface later once the backend event contract is confirmed locally.

    override val mode: ChatTransportMode = ChatTransportMode.Polling

    override fun stream(config: ChatTransportConfig): Flow<ChatRealtimeEvent> = flow {
        emit(ChatRealtimeEvent.Connected(mode = mode))

        while (currentCoroutineContext().isActive) {
            delay(config.intervalMs)

            when (val result = fetchMessages(config)) {
                is ApiResult.Success -> {
                    emit(
                        ChatRealtimeEvent.MessagesSnapshot(
                            messages = result.value,
                            receivedAtEpochSeconds = System.currentTimeMillis() / 1_000L,
                        ),
                    )
                }

                is ApiResult.Failure -> {
                    emit(ChatRealtimeEvent.Error(message = result.error.message))
                }
            }
        }
    }

    private suspend fun fetchMessages(config: ChatTransportConfig): ApiResult<List<ChatMessage>> =
        when (config.kind) {
            ChatConversationKind.Support -> chatRepository.fetchSupportMessages(
                threadId = config.threadId,
                limit = config.limit,
            )

            ChatConversationKind.Direct,
            ChatConversationKind.Unknown,
            -> chatRepository.fetchMessages(
                threadId = config.threadId,
                limit = config.limit,
            )
        }
}
