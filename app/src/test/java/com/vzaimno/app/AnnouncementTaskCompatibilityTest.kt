package com.vzaimno.app

import com.vzaimno.app.core.model.TaskExecutionStatus
import com.vzaimno.app.core.model.TaskLifecycleStatus
import com.vzaimno.app.core.model.canAcceptOffers
import com.vzaimno.app.core.model.canAppearOnMap
import com.vzaimno.app.core.model.customerCanSeeExecutionRoute
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.model.taskStateProjection
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementTaskCompatibilityTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `nested task payload separates lifecycle and execution visibility`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-1",
              "user_id": "user-1",
              "category": "delivery",
              "title": "Bring documents",
              "status": "published",
              "created_at": "2026-04-08T12:00:00Z",
              "data": {
                "task": {
                  "schema_version": 2,
                  "lifecycle": {
                    "status": "open"
                  },
                  "execution": {
                    "status": "accepted",
                    "accepted_confirmed": true
                  },
                  "budget": {
                    "min": 300,
                    "max": 500
                  },
                  "offer_policy": {
                    "quick_offer_price": 350
                  }
                }
              }
            }
        """.trimIndent()).toDomain()

        val projection = announcement.taskStateProjection

        assertEquals(TaskLifecycleStatus.Open, projection.lifecycleStatus)
        assertEquals(TaskExecutionStatus.Accepted, projection.executionStatus)
        assertTrue(projection.acceptedConfirmed)
        assertEquals(350, announcement.quickOfferPrice)
        assertFalse(announcement.canAppearOnMap)
        assertFalse(announcement.canAcceptOffers)
        assertTrue(announcement.customerCanSeeExecutionRoute)
    }

    @Test
    fun `legacy flat payload remains readable when nested task payload is absent`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-2",
              "user_id": "user-2",
              "category": "help",
              "title": "Need courier",
              "status": "active",
              "created_at": "2026-04-08T12:00:00Z",
              "data": {
                "budget_min": "1 250,7",
                "budget_max": "1500",
                "execution_status": "en_route",
                "execution_status_confirmed": true
              }
            }
        """.trimIndent()).toDomain()

        val projection = announcement.taskStateProjection

        assertEquals(TaskLifecycleStatus.Open, projection.lifecycleStatus)
        assertEquals(TaskExecutionStatus.EnRoute, projection.executionStatus)
        assertEquals(1251, projection.budget.min)
        assertEquals(1500, projection.budget.max)
        assertTrue(projection.acceptedConfirmed)
        assertFalse(announcement.canAppearOnMap)
        assertTrue(announcement.customerCanSeeExecutionRoute)
    }

    @Test
    fun `deleted task never stays public even if legacy status is open`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-3",
              "user_id": "user-3",
              "category": "delivery",
              "title": "Old task",
              "status": "active",
              "created_at": "2026-04-08T12:00:00Z",
              "data": {
                "task": {
                  "lifecycle": {
                    "status": "open",
                    "deleted_at": "2026-04-08T12:30:00Z"
                  }
                }
              }
            }
        """.trimIndent()).toDomain()

        val projection = announcement.taskStateProjection

        assertTrue(projection.isDeleted)
        assertEquals(TaskLifecycleStatus.Deleted, projection.lifecycleStatus)
        assertFalse(projection.visibility.isVisibleOnMap)
        assertFalse(projection.visibility.isOpenForOffers)
        assertFalse(projection.visibility.chatShouldRemainTaskBound)
    }
}
