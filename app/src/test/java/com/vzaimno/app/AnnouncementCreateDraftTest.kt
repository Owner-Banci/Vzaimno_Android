package com.vzaimno.app

import com.vzaimno.app.core.common.asJsonArrayOrNull
import com.vzaimno.app.core.common.boolOrNullCompat
import com.vzaimno.app.core.common.intOrNullCompat
import com.vzaimno.app.core.common.jsonAt
import com.vzaimno.app.core.common.stringOrNullCompat
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import com.vzaimno.app.feature.ads.create.AnnouncementAddressInput
import com.vzaimno.app.feature.ads.create.AnnouncementBudgetInput
import com.vzaimno.app.feature.ads.create.AnnouncementConditionOption
import com.vzaimno.app.feature.ads.create.AnnouncementContactMethod
import com.vzaimno.app.feature.ads.create.AnnouncementCreateFormDraft
import com.vzaimno.app.feature.ads.create.AnnouncementCreateItemType
import com.vzaimno.app.feature.ads.create.AnnouncementCreatePurchaseType
import com.vzaimno.app.feature.ads.create.AnnouncementMainGroup
import com.vzaimno.app.feature.ads.create.CreateAdActionDefaults
import com.vzaimno.app.feature.ads.create.toCreateFormDraftWithModerationMarks
import com.vzaimno.app.feature.ads.create.toRepositoryDraft
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `pickup action applies defaults`() {
        val draft = CreateAdActionDefaults.applyActionChange(
            draft = AnnouncementCreateFormDraft(),
            newAction = AnnouncementStructuredData.ActionType.Pickup,
        )

        assertEquals(AnnouncementStructuredData.ActionType.Pickup, draft.actionType)
        assertEquals(AnnouncementStructuredData.SourceKind.PickupPoint, draft.sourceKind)
        assertEquals(AnnouncementStructuredData.DestinationKind.Address, draft.destinationKind)
        assertEquals(AnnouncementCreateItemType.Documents, draft.itemType)
        assertEquals("30", draft.attributes.estimatedTaskMinutes)
        assertEquals(AnnouncementMainGroup.Delivery, draft.mainGroup)
    }

    @Test
    fun `switching from carry to ride resets incompatible cargo fields and forces vehicle`() {
        val carryDraft = AnnouncementCreateFormDraft(
            actionType = AnnouncementStructuredData.ActionType.Carry,
            itemType = AnnouncementCreateItemType.BulkyItem,
            sourceKind = AnnouncementStructuredData.SourceKind.Address,
            destinationKind = AnnouncementStructuredData.DestinationKind.Entrance,
            attributes = com.vzaimno.app.feature.ads.create.AnnouncementAttributesInput(
                needsTrunk = true,
                requiresCarefulHandling = true,
                needsLoader = true,
                requiresLiftToFloor = true,
                cargoLength = "120",
                cargoWidth = "60",
                cargoHeight = "40",
                weightCategory = AnnouncementStructuredData.WeightCategory.Over15Kg,
                sizeCategory = AnnouncementStructuredData.SizeCategory.Bulky,
            ),
        )

        val draft = CreateAdActionDefaults.applyActionChange(
            draft = carryDraft,
            newAction = AnnouncementStructuredData.ActionType.Ride,
        )

        assertEquals(AnnouncementStructuredData.ActionType.Ride, draft.actionType)
        assertEquals(AnnouncementStructuredData.SourceKind.Address, draft.sourceKind)
        assertEquals(AnnouncementStructuredData.DestinationKind.Address, draft.destinationKind)
        assertTrue(draft.attributes.requiresVehicle)
        assertFalse(draft.attributes.requiresCarefulHandling)
        assertFalse(draft.attributes.needsLoader)
        assertFalse(draft.attributes.requiresLiftToFloor)
        assertEquals("", draft.attributes.cargoLength)
        assertEquals("", draft.attributes.cargoWidth)
        assertEquals("", draft.attributes.cargoHeight)
        assertEquals(null, draft.attributes.weightCategory)
        assertEquals(null, draft.attributes.sizeCategory)
    }

    @Test
    fun `available conditions and route selectors depend on scenario`() {
        val pickupDraft = CreateAdActionDefaults.applyActionChange(
            AnnouncementCreateFormDraft(),
            AnnouncementStructuredData.ActionType.Pickup,
        )
        assertTrue(pickupDraft.availableConditionOptions.contains(AnnouncementConditionOption.RequiresVehicle))
        assertFalse(pickupDraft.availableConditionOptions.contains(AnnouncementConditionOption.HasElevator))
        assertTrue(pickupDraft.availableSourceKinds.contains(AnnouncementStructuredData.SourceKind.PickupPoint))
        assertTrue(pickupDraft.availableDestinationKinds.contains(AnnouncementStructuredData.DestinationKind.Person))

        val liftedDraft = pickupDraft.copy(
            attributes = pickupDraft.attributes.copy(requiresLiftToFloor = true),
        )
        assertTrue(liftedDraft.availableConditionOptions.contains(AnnouncementConditionOption.HasElevator))

        val proHelpDraft = CreateAdActionDefaults.applyActionChange(
            AnnouncementCreateFormDraft(),
            AnnouncementStructuredData.ActionType.ProHelp,
        )
        assertFalse(proHelpDraft.showsDestinationSection)
        assertEquals(listOf(AnnouncementConditionOption.CallBeforeArrival, AnnouncementConditionOption.PhotoReportRequired), proHelpDraft.availableConditionOptions)
    }

    @Test
    fun `generated title description route and budget summary mirror iOS logic`() {
        val draft = CreateAdActionDefaults.applyActionChange(
            AnnouncementCreateFormDraft(),
            AnnouncementStructuredData.ActionType.Pickup,
        ).copy(
            itemType = AnnouncementCreateItemType.Documents,
            urgency = AnnouncementStructuredData.Urgency.Today,
            sourceKind = AnnouncementStructuredData.SourceKind.PickupPoint,
            destinationKind = AnnouncementStructuredData.DestinationKind.Address,
            source = AnnouncementAddressInput(address = "Пятницкая 12"),
            destination = AnnouncementAddressInput(address = "Москва, Лесная 10"),
        )

        assertEquals("Забрать документы", draft.generatedTitle)
        assertEquals("Пятницкая 12 -> Москва, Лесная 10", draft.routeSummary)
        assertEquals("Сегодня", draft.timeSummary)
        assertEquals("Рекомендуем 450–650 ₽", draft.budgetSummary)
        assertTrue(draft.assembledDescription.contains("Нужно забрать документы."))
        assertTrue(draft.assembledDescription.contains("Забор: из ПВЗ Пятницкая 12."))
        assertTrue(draft.assembledDescription.contains("Куда доставить: Москва, Лесная 10."))
    }

    @Test
    fun `recommended price follows exact formula for default pickup documents`() {
        val draft = CreateAdActionDefaults.applyActionChange(
            AnnouncementCreateFormDraft(),
            AnnouncementStructuredData.ActionType.Pickup,
        ).copy(
            itemType = AnnouncementCreateItemType.Documents,
            urgency = AnnouncementStructuredData.Urgency.Today,
            sourceKind = AnnouncementStructuredData.SourceKind.PickupPoint,
            destinationKind = AnnouncementStructuredData.DestinationKind.Address,
        )

        val price = draft.recommendedPriceRange

        assertEquals(450, price.min)
        assertEquals(650, price.max)
        assertEquals("450–650 ₽", price.text)
    }

    @Test
    fun `readiness issues explain missing required fields`() {
        val draft = CreateAdActionDefaults.applyActionChange(
            AnnouncementCreateFormDraft(),
            AnnouncementStructuredData.ActionType.ProHelp,
        ).copy(
            helpType = null,
            taskBrief = "",
            source = AnnouncementAddressInput(address = ""),
        )

        val issues = draft.submitReadinessIssues

        assertTrue(issues.contains("Выберите вид помощи"))
        assertTrue(issues.any { it.contains("Что") || it.contains("Опишите") })
        assertTrue(issues.any { it.contains("Где нужна помощь") })
    }

    @Test
    fun `payload builder keeps legacy flat keys and nested task payload in sync`() {
        val draft = CreateAdActionDefaults.applyActionChange(
            AnnouncementCreateFormDraft(),
            AnnouncementStructuredData.ActionType.Buy,
        ).copy(
            purchaseType = AnnouncementCreatePurchaseType.Medicine,
            sourceKind = AnnouncementStructuredData.SourceKind.Venue,
            destinationKind = AnnouncementStructuredData.DestinationKind.Address,
            source = AnnouncementAddressInput(address = "Москва, Маросейка 8"),
            destination = AnnouncementAddressInput(address = "Москва, Проспект Мира 20"),
            budget = AnnouncementBudgetInput(min = "900", max = "1200"),
            startDate = "2026-04-14T09:00:00Z",
            hasEndTime = true,
            endDate = "2026-04-14T11:00:00Z",
        )

        val payload = draft.toRepositoryDraft(requestStatus = "pending_review")

        assertEquals("delivery", payload.category)
        assertEquals("pending_review", payload.status)
        assertEquals("buy", payload.data["action_type"]?.stringOrNullCompat())
        assertEquals(900, payload.data["budget_min"]?.intOrNullCompat())
        assertEquals(1200, payload.data["budget_max"]?.intOrNullCompat())
        assertEquals("2026-04-14T09:00:00Z", payload.data["start_at"]?.stringOrNullCompat())
        assertEquals("2026-04-14T11:00:00Z", payload.data["end_at"]?.stringOrNullCompat())
        assertEquals(2, payload.data.jsonAt(listOf("task", "schema_version"))?.intOrNullCompat())
        assertEquals("buy", payload.data.jsonAt(listOf("task", "builder", "action_type"))?.stringOrNullCompat())
        assertEquals("Москва, Маросейка 8", payload.data.jsonAt(listOf("task", "route", "source", "address"))?.stringOrNullCompat())
        assertEquals(true, payload.data.jsonAt(listOf("task", "route", "has_end_time"))?.boolOrNullCompat())
        assertNotNull(payload.data["generated_tags"]?.asJsonArrayOrNull())
        assertNotNull(payload.data["ai_hints"]?.asJsonArrayOrNull())
    }

    @Test
    fun `prefill restores nested task payload and moderation marks`() {
        val announcement = json.decodeFromString(
            AnnouncementDto.serializer(),
            """
            {
              "id": "ann-prefill-1",
              "user_id": "user-1",
              "category": "delivery",
              "title": "Купить лекарства и привезти",
              "status": "needs_fix",
              "created_at": "2026-04-09T09:00:00Z",
              "media": [
                { "url": "https://cdn.example.com/image.jpg" }
              ],
              "data": {
                "moderation": {
                  "decision": {
                    "status": "needs_fix",
                    "message": "Исправьте спорные поля"
                  },
                  "reasons": [
                    { "field": "title", "code": "TEXT_GENERIC", "details": "Слишком общий заголовок", "can_appeal": true },
                    { "field": "pickup_address", "code": "ADDR_GENERIC", "details": "Нужно точнее указать адрес", "can_appeal": true },
                    { "field": "media", "code": "MEDIA_GENERIC", "details": "Фото спорное", "can_appeal": false }
                  ]
                },
                "task": {
                  "schema_version": 2,
                  "builder": {
                    "main_group": "delivery",
                    "action_type": "buy",
                    "resolved_category": "buy",
                    "purchase_type": "medicine",
                    "source_kind": "venue",
                    "destination_kind": "address",
                    "urgency": "scheduled",
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
                    "start_at": "2026-04-14T09:00:00Z",
                    "has_end_time": true,
                    "end_at": "2026-04-14T11:00:00Z",
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
            """.trimIndent(),
        ).toDomain()

        val draft = announcement.toCreateFormDraftWithModerationMarks()

        assertEquals(AnnouncementStructuredData.ActionType.Buy, draft.actionType)
        assertEquals(AnnouncementCreatePurchaseType.Medicine, draft.purchaseType)
        assertEquals("Москва, Маросейка, 8", draft.source.address)
        assertEquals("Москва, Проспект Мира, 20", draft.destination.address)
        assertEquals("900", draft.budget.min)
        assertEquals("1200", draft.budget.max)
        assertEquals("2026-04-14T09:00:00Z", draft.startDate)
        assertTrue(draft.hasEndTime)
        assertEquals("2026-04-14T11:00:00Z", draft.endDate)
        assertEquals(AnnouncementContactMethod.CallsOnly, draft.contacts.method)
        assertEquals("Слишком общий заголовок", draft.moderationMarks["title"]?.details)
        assertEquals("Нужно точнее указать адрес", draft.moderationMarks["pickup_address"]?.details)
        assertEquals(com.vzaimno.app.feature.ads.create.ModerationSeverity.Error, draft.moderationMarks["media"]?.severity)
    }
}
