package com.vzaimno.app.feature.chats

import com.vzaimno.app.core.model.ChatThreadPreview
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class ChatThreadPreviewStore @Inject constructor() {

    private val _previews = MutableStateFlow<Map<String, ChatThreadPreview>>(emptyMap())
    val previews: StateFlow<Map<String, ChatThreadPreview>> = _previews.asStateFlow()

    fun get(threadId: String?): ChatThreadPreview? = threadId?.let(_previews.value::get)

    fun replaceAll(items: List<ChatThreadPreview>) {
        _previews.update { current ->
            val updated = current.toMutableMap()
            items.forEach { preview ->
                updated[preview.threadId] = preview
            }
            updated
        }
    }

    fun upsert(item: ChatThreadPreview) {
        _previews.update { current ->
            current + (item.threadId to item)
        }
    }
}
