package com.vzaimno.app.feature.discovery

import com.vzaimno.app.core.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Route math utilities for "по пути" (along the route) feature.
 * Ported from googlemapstest/RouteMath.kt, adapted to use GeoPoint.
 */
object RouteMath {
    private const val EARTH_RADIUS_METERS = 6_371_008.8

    fun filterAnnouncementsNearRoute(
        points: List<RouteAnnouncementPoint>,
        polyline: List<GeoPoint>,
        radiusMeters: Int,
    ): List<MatchedRouteAnnouncement> {
        return points.mapNotNull { item ->
            val distance = distanceFromPointToRoute(item.point, polyline)
            if (distance <= radiusMeters) {
                MatchedRouteAnnouncement(item = item, distanceToRouteMeters = distance)
            } else {
                null
            }
        }.sortedWith(
            compareBy<MatchedRouteAnnouncement> { it.distanceToRouteMeters }
                .thenBy { it.item.title.lowercase() },
        )
    }

    fun distanceFromPointToRoute(point: GeoPoint, polyline: List<GeoPoint>): Double {
        if (polyline.size < 2) return Double.POSITIVE_INFINITY

        val referenceLatitude = averageLatitude(listOf(point) + polyline)
        val (px, py) = projectCoordinate(point, referenceLatitude)
        var bestDistance = Double.POSITIVE_INFINITY

        for (index in 0 until polyline.lastIndex) {
            val start = polyline[index]
            val end = polyline[index + 1]
            val (ax, ay) = projectCoordinate(start, referenceLatitude)
            val (bx, by) = projectCoordinate(end, referenceLatitude)
            bestDistance = min(bestDistance, distancePointToSegment(px, py, ax, ay, bx, by))
        }

        return bestDistance
    }

    fun closestProjectionOnRoute(point: GeoPoint, polyline: List<GeoPoint>): RouteProjection? {
        if (polyline.size < 2) return null

        val referenceLatitude = averageLatitude(listOf(point) + polyline)
        val (px, py) = projectCoordinate(point, referenceLatitude)
        val totalLength = polylineLengthMeters(polyline)
        var bestDistance = Double.POSITIVE_INFINITY
        var bestProjection: RouteProjection? = null
        var traversed = 0.0

        for (index in 0 until polyline.lastIndex) {
            val start = polyline[index]
            val end = polyline[index + 1]
            val (ax, ay) = projectCoordinate(start, referenceLatitude)
            val (bx, by) = projectCoordinate(end, referenceLatitude)
            val projection = projectPointToSegment(px, py, ax, ay, bx, by)
            val segmentLength = haversineDistanceMeters(start, end)
            val distance = hypot(px - projection.first, py - projection.second)
            if (distance < bestDistance) {
                bestDistance = distance
                val fraction = segmentFraction(projection.first, projection.second, ax, ay, bx, by)
                bestProjection = RouteProjection(
                    coordinate = unprojectCoordinate(projection.first, projection.second, referenceLatitude),
                    distanceMeters = distance,
                    progress = if (totalLength > 0.0) {
                        (traversed + segmentLength * fraction) / totalLength
                    } else {
                        0.0
                    },
                )
            }
            traversed += segmentLength
        }

        return bestProjection
    }

    fun polylineLengthMeters(polyline: List<GeoPoint>): Double {
        if (polyline.size < 2) return 0.0
        var total = 0.0
        for (index in 0 until polyline.lastIndex) {
            total += haversineDistanceMeters(polyline[index], polyline[index + 1])
        }
        return total
    }

    fun haversineDistanceMeters(start: GeoPoint, end: GeoPoint): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val sinLat = kotlin.math.sin(dLat / 2.0)
        val sinLon = kotlin.math.sin(dLon / 2.0)
        val a = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
        return 2.0 * EARTH_RADIUS_METERS * asin(sqrt(max(0.0, min(1.0, a))))
    }

    private fun averageLatitude(points: List<GeoPoint>): Double {
        return points.sumOf { it.latitude } / points.size
    }

    private fun projectCoordinate(point: GeoPoint, referenceLatitude: Double): Pair<Double, Double> {
        val latRadians = Math.toRadians(point.latitude)
        val lonRadians = Math.toRadians(point.longitude)
        val referenceCos = cos(Math.toRadians(referenceLatitude))
        return Pair(
            EARTH_RADIUS_METERS * lonRadians * referenceCos,
            EARTH_RADIUS_METERS * latRadians,
        )
    }

    private fun unprojectCoordinate(x: Double, y: Double, referenceLatitude: Double): GeoPoint {
        val referenceCos = cos(Math.toRadians(referenceLatitude))
        val lat = Math.toDegrees(y / EARTH_RADIUS_METERS)
        val lon = Math.toDegrees(x / (EARTH_RADIUS_METERS * max(referenceCos, 1e-12)))
        return GeoPoint(latitude = lat, longitude = lon)
    }

    private fun distancePointToSegment(
        px: Double, py: Double,
        ax: Double, ay: Double,
        bx: Double, by: Double,
    ): Double {
        val projection = projectPointToSegment(px, py, ax, ay, bx, by)
        return hypot(px - projection.first, py - projection.second)
    }

    private fun projectPointToSegment(
        px: Double, py: Double,
        ax: Double, ay: Double,
        bx: Double, by: Double,
    ): Pair<Double, Double> {
        val dx = bx - ax
        val dy = by - ay
        if (kotlin.math.abs(dx) < 1e-9 && kotlin.math.abs(dy) < 1e-9) {
            return Pair(ax, ay)
        }

        var t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
        t = max(0.0, min(1.0, t))
        return Pair(ax + t * dx, ay + t * dy)
    }

    private fun segmentFraction(
        projectedX: Double, projectedY: Double,
        ax: Double, ay: Double,
        bx: Double, by: Double,
    ): Double {
        val dx = bx - ax
        val dy = by - ay
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared < 1e-9) return 0.0
        val t = ((projectedX - ax) * dx + (projectedY - ay) * dy) / lengthSquared
        return max(0.0, min(1.0, t))
    }
}
