package com.vzaimno.app.feature.route

import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.RouteTravelMode
import com.vzaimno.app.core.model.TaskByRoute
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePresentationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `nextRouteStage advances only in declared order`() {
        assertEquals(RouteStage.Accepted, nextRouteStage(null))
        assertEquals(RouteStage.EnRoute, nextRouteStage(RouteStage.Accepted))
        assertEquals(RouteStage.OnSite, nextRouteStage(RouteStage.EnRoute))
        assertEquals(RouteStage.InProgress, nextRouteStage(RouteStage.OnSite))
        assertEquals(RouteStage.Handoff, nextRouteStage(RouteStage.InProgress))
        assertEquals(RouteStage.Completed, nextRouteStage(RouteStage.Handoff))
        assertEquals(null, nextRouteStage(RouteStage.Completed))
    }

    @Test
    fun `visibleCustomerAnnouncements keeps only route-visible execution states`() {
        val accepted = announcement(
            id = "accepted",
            executionStatus = "accepted",
        )
        val open = announcement(
            id = "open",
            executionStatus = "open",
        )
        val completed = announcement(
            id = "completed",
            executionStatus = "completed",
        )

        val result = visibleCustomerAnnouncements(listOf(open, completed, accepted))

        assertEquals(listOf("accepted"), result.map { it.id })
    }

    @Test
    fun `orderedAcceptedTasks prefers route progress over acceptance order`() {
        val polyline = listOf(
            GeoPoint(latitude = 55.75, longitude = 37.60),
            GeoPoint(latitude = 55.76, longitude = 37.61),
            GeoPoint(latitude = 55.77, longitude = 37.62),
        )
        val taskNearStart = previewTask(
            id = "near-start",
            point = GeoPoint(latitude = 55.755, longitude = 37.605),
        )
        val taskNearEnd = previewTask(
            id = "near-end",
            point = GeoPoint(latitude = 55.769, longitude = 37.619),
        )

        val ordered = orderedAcceptedTasks(
            tasks = listOf(taskNearEnd, taskNearStart),
            acceptedTaskIds = listOf("near-end", "near-start"),
            basePolyline = polyline,
        )

        assertEquals(listOf("near-start", "near-end"), ordered.map(TaskByRoute::id))
    }

    @Test
    fun `buildApproximateRoute stitches waypoints and estimates distance`() {
        val route = buildApproximateRoute(
            start = GeoPoint(latitude = 55.75, longitude = 37.60),
            waypoints = listOf(
                GeoPoint(latitude = 55.755, longitude = 37.605),
                GeoPoint(latitude = 55.76, longitude = 37.61),
            ),
            end = GeoPoint(latitude = 55.77, longitude = 37.62),
            travelMode = RouteTravelMode.Driving,
        )

        assertEquals(4, route.polyline.size)
        assertEquals(RouteMapGeometryQuality.Approximate, route.quality)
        assertTrue(route.distanceMeters > 0)
        assertTrue(route.durationSeconds > 0)
    }

    private fun announcement(
        id: String,
        executionStatus: String,
    ) = json.decodeFromString(AnnouncementDto.serializer(), """
        {
          "id": "$id",
          "user_id": "user-$id",
          "category": "delivery",
          "title": "Task $id",
          "status": "assigned",
          "created_at": "2026-04-10T12:00:00Z",
          "data": {
            "task": {
              "lifecycle": {
                "status": "assigned"
              },
              "execution": {
                "status": "$executionStatus"
              }
            }
          }
        }
    """.trimIndent()).toDomain()

    private fun previewTask(
        id: String,
        point: GeoPoint,
    ) = TaskByRoute(
        id = id,
        title = "Task $id",
        category = "delivery",
        addressText = "Address $id",
        coordinate = point,
        distanceToRouteMeters = 120.0,
        priceText = "500 ₽",
        previewImageUrl = null,
        status = "open",
    )
}
