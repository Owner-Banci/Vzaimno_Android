package com.vzaimno.app

import com.vzaimno.app.core.common.asJsonObjectOrNull
import com.vzaimno.app.core.common.boolOrNullCompat
import com.vzaimno.app.core.common.intOrNullCompat
import com.vzaimno.app.core.common.jsonAt
import com.vzaimno.app.core.common.stringOrNullCompat
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import com.vzaimno.app.feature.ads.create.AnnouncementAddressInput
import com.vzaimno.app.feature.ads.create.AnnouncementAudience
import com.vzaimno.app.feature.ads.create.AnnouncementBudgetInput
import com.vzaimno.app.feature.ads.create.AnnouncementBudgetMode
import com.vzaimno.app.feature.ads.create.AnnouncementContactMethod
import com.vzaimno.app.feature.ads.create.AnnouncementContactsInput
import com.vzaimno.app.feature.ads.create.AnnouncementCreateFormDraft
import com.vzaimno.app.feature.ads.create.AnnouncementCreateItemType
import com.vzaimno.app.feature.ads.create.AnnouncementMainGroup
import com.vzaimno.app.feature.ads.create.AnnouncementSelectedMedia
import com.vzaimno.app.feature.ads.create.hasReusablePrefillMedia
import com.vzaimno.app.feature.ads.create.submissionPlan
import com.vzaimno.app.feature.ads.create.toCreateFormDraft
import com.vzaimno.app.feature.ads.create.toOptimisticAnnouncement
import com.vzaimno.app.feature.ads.create.toRepositoryDraft
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementCreateDraftTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `repository draft builds task schema v2 payload with structured contacts and budget`() {
        val draft = AnnouncementCreateFormDraft(
            mainGroup = AnnouncementMainGroup.Delivery,
            actionType = AnnouncementStructuredData.ActionType.Pickup,
            title = "Забрать документы и привезти домой",
            itemType = AnnouncementCreateItemType.Documents,
            sourceKind = AnnouncementStructuredData.SourceKind.PickupPoint,
            destinationKind = AnnouncementStructuredData.DestinationKind.Address,
            urgency = AnnouncementStructuredData.Urgency.Today,
            source = AnnouncementAddressInput(address = "Москва, Лесная, 7"),
            destination = AnnouncementAddressInput(address = "Москва, Ямская, 11"),
            budget = AnnouncementBudgetInput(
                mode = AnnouncementBudgetMode.Range,
                min = "700",
                max = "1100",
            ),
            contacts = AnnouncementContactsInput(
                name = "Анна",
                phone = "+7 999 123-45-67",
                method = AnnouncementContactMethod.MessagesOnly,
                audience = AnnouncementAudience.Both,
            ),
            media = listOf(
                AnnouncementSelectedMedia(
                    id = "media-1",
                    uriString = "content://photos/1",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                ),
            ),
        )

        val repositoryDraft = draft.toRepositoryDraft(requestStatus = "pending_review")
        assertEquals("delivery", repositoryDraft.category)
        assertEquals("pending_review", repositoryDraft.status)
        assertEquals(2, repositoryDraft.data.jsonAt(listOf("task", "schema_version"))?.intOrNullCompat())
        assertEquals("pickup", repositoryDraft.data.jsonAt(listOf("task", "builder", "action_type"))?.stringOrNullCompat())
        assertEquals("pickup_point", repositoryDraft.data.jsonAt(listOf("task", "builder", "resolved_category"))?.stringOrNullCompat())
        assertEquals(700, repositoryDraft.data.jsonAt(listOf("task", "budget", "min"))?.intOrNullCompat())
        assertEquals(1100, repositoryDraft.data.jsonAt(listOf("task", "budget", "max"))?.intOrNullCompat())
        assertEquals(1100, repositoryDraft.data.jsonAt(listOf("task", "budget", "amount"))?.intOrNullCompat())
        assertEquals("Анна", repositoryDraft.data.jsonAt(listOf("task", "contacts", "name"))?.stringOrNullCompat())
        assertEquals("+79991234567", repositoryDraft.data.jsonAt(listOf("task", "contacts", "phone"))?.stringOrNullCompat())
        assertEquals("messages_only", repositoryDraft.data.jsonAt(listOf("task", "contacts", "method"))?.stringOrNullCompat())
        assertEquals("Москва, Лесная, 7", repositoryDraft.data.jsonAt(listOf("task", "route", "source", "address"))?.stringOrNullCompat())
        assertEquals("Москва, Ямская, 11", repositoryDraft.data.jsonAt(listOf("task", "route", "destination", "address"))?.stringOrNullCompat())
    }

    @Test
    fun `prefill draft reads nested task payload and marks existing media as non-reusable`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-prefill-1",
              "user_id": "user-1",
              "category": "delivery",
              "title": "Купить лекарства и привезти",
              "status": "pending_review",
              "created_at": "2026-04-09T09:00:00Z",
              "media": [
                { "url": "https://cdn.example.com/image.jpg" }
              ],
              "data": {
                "task": {
                  "schema_version": 2,
                  "builder": {
                    "main_group": "delivery",
                    "action_type": "buy",
                    "resolved_category": "buy",
                    "purchase_type": "medicine",
                    "source_kind": "venue",
                    "destination_kind": "address",
                    "urgency": "today",
                    "task_brief": "Забрать рецепт и купить лекарства",
                    "notes": "Важно позвонить перед выездом"
                  },
                  "attributes": {
                    "requires_vehicle": true,
                    "wait_on_site": true,
                    "waiting_minutes": 20,
                    "estimated_task_minutes": 45
                  },
                  "budget": {
                    "min": 900,
                    "max": 1200,
                    "amount": 1200
                  },
                  "route": {
                    "source": {
                      "address": "Москва, Маросейка, 8"
                    },
                    "destination": {
                      "address": "Москва, Проспект Мира, 20"
                    }
                  },
                  "contacts": {
                    "name": "Мария",
                    "phone": "+79991234567",
                    "method": "calls_only",
                    "audience": "individuals"
                  }
                }
              }
            }
        """.trimIndent()).toDomain()

        val draft = announcement.toCreateFormDraft()

        assertEquals(AnnouncementMainGroup.Delivery, draft.mainGroup)
        assertEquals(AnnouncementStructuredData.ActionType.Buy, draft.actionType)
        assertEquals("Купить лекарства и привезти", draft.title)
        assertEquals("Москва, Маросейка, 8", draft.source.address)
        assertEquals("Москва, Проспект Мира, 20", draft.destination.address)
        assertEquals(AnnouncementBudgetMode.Range, draft.budget.mode)
        assertEquals("900", draft.budget.min)
        assertEquals("1200", draft.budget.max)
        assertEquals("Мария", draft.contacts.name)
        assertEquals("+79991234567", draft.contacts.phone)
        assertEquals(AnnouncementContactMethod.CallsOnly, draft.contacts.method)
        assertEquals(AnnouncementAudience.Individuals, draft.contacts.audience)
        assertTrue(announcement.hasReusablePrefillMedia())
    }

    @Test
    fun `optimistic announcement keeps temporary id and local previews`() {
        val draft = AnnouncementCreateFormDraft(
            mainGroup = AnnouncementMainGroup.Help,
            actionType = AnnouncementStructuredData.ActionType.Other,
            title = "Нестандартное поручение",
            source = AnnouncementAddressInput(address = "Москва, Петровка, 5"),
            budget = AnnouncementBudgetInput(
                mode = AnnouncementBudgetMode.Fixed,
                amount = "1500",
            ),
            taskBrief = "Нужно быстро передать комплект ключей",
            media = listOf(
                AnnouncementSelectedMedia(
                    id = "media-1",
                    uriString = "content://photos/77",
                    fileName = "keys.jpg",
                    mimeType = "image/jpeg",
                ),
            ),
        )

        val optimistic = draft.toOptimisticAnnouncement(
            localId = "local-123",
            userId = "user-77",
            requestStatus = draft.submissionPlan().requestStatus,
        )

        assertEquals("local-123", optimistic.id)
        assertEquals("user-77", optimistic.userId)
        assertEquals("pending_review", optimistic.status)
        assertNotNull(optimistic.media.firstOrNull())
        assertEquals(
            "content://photos/77",
            optimistic.media.firstOrNull()?.asJsonObjectOrNull()?.get("url")?.stringOrNullCompat(),
        )
        assertEquals(2, optimistic.data.jsonAt(listOf("task", "schema_version"))?.intOrNullCompat())
        assertTrue(optimistic.data.jsonAt(listOf("task", "budget", "amount"))?.intOrNullCompat() == 1500)
        assertTrue(optimistic.data.jsonAt(listOf("task", "execution", "status"))?.stringOrNullCompat() == "open")
        assertTrue(optimistic.data.jsonAt(listOf("task", "route", "source", "address"))?.stringOrNullCompat() == "Москва, Петровка, 5")
        assertTrue(optimistic.data.jsonAt(listOf("task", "offer_policy", "quick_offer_enabled"))?.boolOrNullCompat() == true)
    }
}
