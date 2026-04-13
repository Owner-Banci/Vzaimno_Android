package com.vzaimno.app.feature.chats

import com.vzaimno.app.core.model.ChatMessage
import com.vzaimno.app.core.model.ChatThreadPreview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ChatPresentationTest {

    @Test
    fun `sortChatThreads keeps pinned thread first and sorts the rest by recency`() {
        val pinned = preview(
            threadId = "support",
            kind = "support",
            lastMessageAtEpochSeconds = 100L,
            isPinned = true,
        )
        val newest = preview(
            threadId = "newest",
            lastMessageAtEpochSeconds = 300L,
        )
        val older = preview(
            threadId = "older",
            lastMessageAtEpochSeconds = 200L,
        )

        val result = sortChatThreads(listOf(older, newest, pinned))

        assertEquals(listOf("support", "newest", "older"), result.map(ChatThreadPreview::threadId))
    }

    @Test
    fun `mergeMessages removes duplicates and keeps ascending order`() {
        val existing = listOf(
            message(id = "1", createdAtEpochSeconds = 10L),
            message(id = "2", createdAtEpochSeconds = 20L),
        )
        val incoming = listOf(
            message(id = "2", createdAtEpochSeconds = 20L),
            message(id = "3", createdAtEpochSeconds = 15L),
        )

        val result = mergeMessages(existing, incoming)

        assertEquals(listOf("1", "3", "2"), result.map(ChatMessage::id))
    }

    @Test
    fun `resolveReportTarget prefers latest incoming message`() {
        val preview = preview(
            threadId = "thread",
            partnerId = "partner-1",
            announcementId = "task-1",
        )
        val messages = listOf(
            message(id = "me", senderId = "me"),
            message(id = "incoming", senderId = "partner-1"),
        )

        val result = resolveReportTarget(preview, messages, currentUserId = "me")

        assertNotNull(result)
        assertEquals("message", result?.type)
        assertEquals("incoming", result?.id)
    }

    @Test
    fun `resolveReportTarget falls back to user and task identifiers`() {
        val previewWithPartner = preview(
            threadId = "thread",
            partnerId = "partner-2",
            announcementId = "task-2",
        )
        val userTarget = resolveReportTarget(previewWithPartner, emptyList(), currentUserId = "me")
        assertEquals("user", userTarget?.type)
        assertEquals("partner-2", userTarget?.id)

        val previewWithoutPartner = preview(
            threadId = "thread-2",
            partnerId = null,
            announcementId = "task-3",
        )
        val taskTarget = resolveReportTarget(previewWithoutPartner, emptyList(), currentUserId = "me")
        assertEquals("task", taskTarget?.type)
        assertEquals("task-3", taskTarget?.id)
    }

    @Test
    fun `resolveReportTarget returns null when no identifiers are available`() {
        val result = resolveReportTarget(
            preview = preview(
                threadId = "thread",
                partnerId = null,
                announcementId = null,
            ),
            messages = emptyList(),
            currentUserId = "me",
        )

        assertNull(result)
    }

    private fun preview(
        threadId: String,
        kind: String = "direct",
        partnerId: String? = "partner",
        announcementId: String? = "announcement",
        lastMessageAtEpochSeconds: Long? = null,
        isPinned: Boolean = false,
    ): ChatThreadPreview = ChatThreadPreview(
        threadId = threadId,
        kind = kind,
        partnerId = partnerId,
        partnerName = "Собеседник",
        partnerAvatarUrl = null,
        lastMessageText = "Последнее сообщение",
        lastMessageAtEpochSeconds = lastMessageAtEpochSeconds,
        unreadCount = 0,
        announcementId = announcementId,
        announcementTitle = "Объявление",
        isPinned = isPinned,
    )

    private fun message(
        id: String,
        senderId: String = "partner",
        createdAtEpochSeconds: Long = 10L,
    ): ChatMessage = ChatMessage(
        id = id,
        threadId = "thread",
        senderId = senderId,
        text = "text",
        createdAtEpochSeconds = createdAtEpochSeconds,
    )
}
