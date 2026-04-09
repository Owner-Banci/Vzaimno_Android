package com.vzaimno.app

import com.vzaimno.app.core.model.canAppealModeration
import com.vzaimno.app.core.model.hasModerationIssues
import com.vzaimno.app.core.model.hasOnlyTechnicalModerationIssues
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import com.vzaimno.app.feature.ads.AdsFilterBucket
import com.vzaimno.app.feature.ads.adsBucket
import com.vzaimno.app.feature.ads.shouldShowAppealAction
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementAdsPresentationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `rejected moderation announcement lands in actions and can be appealed`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-actions-1",
              "user_id": "user-1",
              "category": "delivery",
              "title": "Rejected announcement",
              "status": "rejected",
              "created_at": "2026-04-08T12:00:00Z",
              "data": {
                "moderation": {
                  "decision": {
                    "status": "rejected",
                    "message": "Нужно уточнить описание"
                  },
                  "reasons": [
                    {
                      "field": "notes",
                      "code": "TEXT_POLICY",
                      "details": "Описание слишком размытое",
                      "can_appeal": true
                    }
                  ]
                }
              }
            }
        """.trimIndent()).toDomain()

        assertEquals(AdsFilterBucket.Actions, announcement.adsBucket())
        assertTrue(announcement.hasModerationIssues)
        assertTrue(announcement.canAppealModeration)
        assertTrue(announcement.shouldShowAppealAction())
    }

    @Test
    fun `technical moderation issue stays invisible for active announcement`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-active-1",
              "user_id": "user-2",
              "category": "help",
              "title": "Active announcement",
              "status": "active",
              "created_at": "2026-04-08T12:00:00Z",
              "data": {
                "moderation": {
                  "reasons": [
                    {
                      "field": "media",
                      "code": "TEXT_SYSTEM_UNAVAILABLE",
                      "details": "Timed out while checking content",
                      "can_appeal": false
                    }
                  ]
                }
              }
            }
        """.trimIndent()).toDomain()

        assertTrue(announcement.hasOnlyTechnicalModerationIssues)
        assertFalse(announcement.hasModerationIssues)
        assertEquals(AdsFilterBucket.Active, announcement.adsBucket())
    }

    @Test
    fun `completed announcement belongs to archive bucket`() {
        val announcement = json.decodeFromString(AnnouncementDto.serializer(), """
            {
              "id": "ann-archive-1",
              "user_id": "user-3",
              "category": "delivery",
              "title": "Completed announcement",
              "status": "completed",
              "created_at": "2026-04-08T12:00:00Z",
              "data": {
                "task": {
                  "lifecycle": {
                    "status": "completed"
                  },
                  "execution": {
                    "status": "completed"
                  }
                }
              }
            }
        """.trimIndent()).toDomain()

        assertEquals(AdsFilterBucket.Archive, announcement.adsBucket())
    }
}
