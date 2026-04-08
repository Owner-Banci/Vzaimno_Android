package com.vzaimno.app

import com.vzaimno.app.core.common.taskBoolValue
import com.vzaimno.app.core.common.taskIntValue
import com.vzaimno.app.core.common.taskStringValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonTaskHelpersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `prefers nested task value and falls back to legacy keys`() {
        val payload = json.parseToJsonElement("""
            {
              "task": {
                "builder": {
                  "action_type": "carry"
                }
              },
              "action_type": "buy"
            }
        """.trimIndent()).jsonObject

        assertEquals(
            "carry",
            payload.taskStringValue(
                paths = listOf(listOf("task", "builder", "action_type")),
                legacyKeys = listOf("action_type"),
            ),
        )
    }

    @Test
    fun `parses rounded numeric strings for compatibility projection`() {
        val payload = json.parseToJsonElement("""
            {
              "budget_min": "1 250,7"
            }
        """.trimIndent()).jsonObject

        assertEquals(
            1251,
            payload.taskIntValue(
                paths = emptyList(),
                legacyKeys = listOf("budget_min"),
            ),
        )
    }

    @Test
    fun `parses boolean compatibility values from strings and booleans`() {
        val payload = json.parseToJsonElement("""
            {
              "task": {
                "execution": {
                  "accepted_confirmed": "true"
                }
              },
              "execution_status_confirmed": false
            }
        """.trimIndent()).jsonObject

        assertTrue(
            payload.taskBoolValue(
                paths = listOf(listOf("task", "execution", "accepted_confirmed")),
                legacyKeys = listOf("execution_status_confirmed"),
            ) == true,
        )

        assertFalse(
            payload.taskBoolValue(
                paths = emptyList(),
                legacyKeys = listOf("execution_status_confirmed"),
            ) ?: true,
        )
    }
}
