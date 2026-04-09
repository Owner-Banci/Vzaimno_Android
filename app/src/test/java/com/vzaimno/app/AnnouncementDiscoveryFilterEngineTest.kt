package com.vzaimno.app

import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.canAppearOnMap
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import com.vzaimno.app.feature.discovery.AnnouncementDiscoveryFilterEngine
import com.vzaimno.app.feature.discovery.DiscoveryCategoryFilter
import com.vzaimno.app.feature.discovery.DiscoveryFilterState
import com.vzaimno.app.feature.discovery.DiscoveryResponseGate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementDiscoveryFilterEngineTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `search matches structured task fields and ignores non-public announcements`() {
        val visible = announcement(
            """
                {
                  "id": "ann-visible",
                  "user_id": "user-1",
                  "category": "delivery",
                  "title": "Нужно забрать заказ",
                  "status": "active",
                  "created_at": "2026-04-09T10:00:00Z",
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "open" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "buy",
                        "resolved_category": "buy",
                        "purchase_type": "medicine",
                        "task_brief": "Купить лекарства в аптеке"
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Тверская, 5",
                          "point": { "lat": 55.757, "lon": 37.615 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )
        val hidden = announcement(
            """
                {
                  "id": "ann-hidden",
                  "user_id": "user-2",
                  "category": "delivery",
                  "title": "Скрытое объявление",
                  "status": "pending_review",
                  "created_at": "2026-04-09T10:00:00Z",
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "pending_review" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "buy",
                        "resolved_category": "buy",
                        "purchase_type": "medicine"
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Петровка, 9",
                          "point": { "lat": 55.763, "lon": 37.618 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )

        val items = AnnouncementDiscoveryFilterEngine.buildItems(
            announcements = listOf(visible, hidden),
            apiBaseUrl = "https://api.vzaimno.app/",
            filters = DiscoveryFilterState(),
            query = "аптек",
            currentUserId = null,
            canRespondWithoutGate = true,
            locallyRespondedIds = emptySet(),
        )

        assertEquals(listOf("ann-visible"), items.map { it.announcement.id })
    }

    @Test
    fun `quick category and action filters keep only matching announcements`() {
        val deliveryBuy = announcement(
            """
                {
                  "id": "ann-delivery-buy",
                  "user_id": "user-1",
                  "category": "delivery",
                  "title": "Купить продукты",
                  "status": "active",
                  "created_at": "2026-04-09T10:00:00Z",
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "open" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "buy",
                        "resolved_category": "buy"
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Арбат, 1",
                          "point": { "lat": 55.752, "lon": 37.592 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )
        val helpPro = announcement(
            """
                {
                  "id": "ann-help-pro",
                  "user_id": "user-2",
                  "category": "help",
                  "title": "Настроить роутер",
                  "status": "active",
                  "created_at": "2026-04-09T10:00:00Z",
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "open" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "pro_help",
                        "resolved_category": "pro_help"
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Сретенка, 11",
                          "point": { "lat": 55.768, "lon": 37.635 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )

        val filters = DiscoveryFilterState(
            categories = setOf(DiscoveryCategoryFilter.Delivery),
            actions = setOf(AnnouncementStructuredData.ActionType.Buy),
        )

        val items = AnnouncementDiscoveryFilterEngine.buildItems(
            announcements = listOf(deliveryBuy, helpPro),
            apiBaseUrl = "https://api.vzaimno.app/",
            filters = filters,
            query = "",
            currentUserId = null,
            canRespondWithoutGate = true,
            locallyRespondedIds = emptySet(),
        )

        assertEquals(listOf("ann-delivery-buy"), items.map { it.announcement.id })
    }

    @Test
    fun `advanced filters match budget vehicle photo and urgency`() {
        val eligible = announcement(
            """
                {
                  "id": "ann-eligible",
                  "user_id": "user-1",
                  "category": "delivery",
                  "title": "Перевезти коробки",
                  "status": "active",
                  "created_at": "2026-04-09T10:00:00Z",
                  "media": [{ "url": "https://cdn.example.com/box.jpg" }],
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "open" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "carry",
                        "resolved_category": "carry",
                        "urgency": "today"
                      },
                      "attributes": {
                        "requires_vehicle": true
                      },
                      "budget": {
                        "min": 900,
                        "max": 1400
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Пречистенка, 3",
                          "point": { "lat": 55.741, "lon": 37.603 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )
        val notEligible = announcement(
            """
                {
                  "id": "ann-not-eligible",
                  "user_id": "user-2",
                  "category": "delivery",
                  "title": "Передать документы",
                  "status": "active",
                  "created_at": "2026-04-09T10:00:00Z",
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "open" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "pickup",
                        "resolved_category": "pickup_point",
                        "urgency": "flexible"
                      },
                      "attributes": {
                        "requires_vehicle": false
                      },
                      "budget": {
                        "min": 300,
                        "max": 500
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Садовая, 14",
                          "point": { "lat": 55.781, "lon": 37.612 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )

        val filters = DiscoveryFilterState(
            urgencies = setOf(AnnouncementStructuredData.Urgency.Today),
            budgetMinText = "800",
            withPhotoOnly = true,
            requiresVehicleOnly = true,
        )

        val items = AnnouncementDiscoveryFilterEngine.buildItems(
            announcements = listOf(eligible, notEligible),
            apiBaseUrl = "https://api.vzaimno.app/",
            filters = filters,
            query = "",
            currentUserId = null,
            canRespondWithoutGate = true,
            locallyRespondedIds = emptySet(),
        )

        assertEquals(listOf("ann-eligible"), items.map { it.announcement.id })
    }

    @Test
    fun `response gate respects own announcement local response and auth state`() {
        val announcement = announcement(
            """
                {
                  "id": "ann-response",
                  "user_id": "owner-1",
                  "category": "delivery",
                  "title": "Забрать пакет",
                  "status": "active",
                  "created_at": "2026-04-09T10:00:00Z",
                  "data": {
                    "task": {
                      "schema_version": 2,
                      "lifecycle": { "status": "open" },
                      "execution": { "status": "open" },
                      "builder": {
                        "action_type": "pickup",
                        "resolved_category": "pickup_point"
                      },
                      "route": {
                        "source": {
                          "address": "Москва, Солянка, 8",
                          "point": { "lat": 55.753, "lon": 37.644 }
                        }
                      }
                    }
                  }
                }
            """.trimIndent(),
        )

        val ownGate = AnnouncementDiscoveryFilterEngine.buildItem(
            announcement = announcement,
            apiBaseUrl = "https://api.vzaimno.app/",
            currentUserId = "owner-1",
            canRespondWithoutGate = true,
            locallyRespondedIds = emptySet(),
        ).responseGate
        val respondedGate = AnnouncementDiscoveryFilterEngine.buildItem(
            announcement = announcement,
            apiBaseUrl = "https://api.vzaimno.app/",
            currentUserId = "another-user",
            canRespondWithoutGate = true,
            locallyRespondedIds = setOf("ann-response"),
        ).responseGate
        val authGate = AnnouncementDiscoveryFilterEngine.buildItem(
            announcement = announcement,
            apiBaseUrl = "https://api.vzaimno.app/",
            currentUserId = null,
            canRespondWithoutGate = false,
            locallyRespondedIds = emptySet(),
        ).responseGate

        assertEquals(DiscoveryResponseGate.OwnAnnouncement, ownGate)
        assertEquals(DiscoveryResponseGate.AlreadyResponded, respondedGate)
        assertEquals(DiscoveryResponseGate.RequiresAuth, authGate)
        assertTrue(announcement.canAppearOnMap)
    }

    private fun announcement(raw: String) = json.decodeFromString(
        AnnouncementDto.serializer(),
        raw,
    ).toDomain()
}
