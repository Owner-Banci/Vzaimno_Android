package com.vzaimno.app.feature.route

import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.RouteBuildRequest
import com.vzaimno.app.core.model.RouteContext
import com.vzaimno.app.core.model.RouteTravelMode
import com.vzaimno.app.core.model.TaskByRoute
import com.vzaimno.app.core.model.TaskExecutionStatus
import com.vzaimno.app.core.model.createdAtEpochSeconds
import com.vzaimno.app.core.model.customerCanSeeExecutionRoute
import com.vzaimno.app.core.model.detailsDescriptionText
import com.vzaimno.app.core.model.destinationPoint
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.mapPoint
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.shortStructuredSubtitle
import com.vzaimno.app.core.model.sourcePoint
import com.vzaimno.app.core.model.taskStateProjection
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class ApproximateRoutePlan(
    val polyline: List<GeoPoint>,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val quality: RouteMapGeometryQuality,
) {
    fun toBuildRequest(context: RouteContext): RouteBuildRequest = RouteBuildRequest(
        announcementId = context.entityId,
        polyline = polyline,
        startAddress = context.startAddress,
        endAddress = context.endAddress,
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        radiusM = context.radiusM,
        travelMode = context.travelMode,
    )
}

fun RouteStage.next(): RouteStage? = when (this) {
    RouteStage.Accepted -> RouteStage.EnRoute
    RouteStage.EnRoute -> RouteStage.OnSite
    RouteStage.OnSite -> RouteStage.InProgress
    RouteStage.InProgress -> RouteStage.Handoff
    RouteStage.Handoff -> RouteStage.Completed
    RouteStage.Completed -> null
}

fun nextRouteStage(currentStage: RouteStage?): RouteStage? = if (currentStage == null) {
    RouteStage.Accepted
} else {
    currentStage.next()
}

fun routeStageFromAnnouncement(announcement: Announcement): RouteStage? {
    val projection = announcement.taskStateProjection
    return when (projection.executionStatus) {
        TaskExecutionStatus.Open,
        TaskExecutionStatus.AwaitingAcceptance,
        -> null

        TaskExecutionStatus.Accepted -> if (projection.acceptedConfirmed) {
            RouteStage.Accepted
        } else {
            null
        }

        TaskExecutionStatus.EnRoute -> RouteStage.EnRoute
        TaskExecutionStatus.OnSite -> RouteStage.OnSite
        TaskExecutionStatus.InProgress -> RouteStage.InProgress
        TaskExecutionStatus.Handoff -> RouteStage.Handoff
        TaskExecutionStatus.Completed -> RouteStage.Completed
        TaskExecutionStatus.Cancelled,
        TaskExecutionStatus.Disputed,
        -> null
    }
}

fun fallbackRouteStage(rawStatus: String?): RouteStage? = when {
    rawStatus.isNullOrBlank() -> null
    rawStatus.contains("complete", ignoreCase = true) || rawStatus.contains("done", ignoreCase = true) -> {
        RouteStage.Completed
    }

    rawStatus.contains("handoff", ignoreCase = true) || rawStatus.contains("finish", ignoreCase = true) -> {
        RouteStage.Handoff
    }

    rawStatus.contains("progress", ignoreCase = true) -> {
        RouteStage.InProgress
    }

    rawStatus.contains("site", ignoreCase = true) || rawStatus.contains("arrive", ignoreCase = true) -> {
        RouteStage.OnSite
    }

    rawStatus.contains("route", ignoreCase = true) || rawStatus.contains("heading", ignoreCase = true) -> {
        RouteStage.EnRoute
    }

    rawStatus.contains("accept", ignoreCase = true) || rawStatus.contains("assigned", ignoreCase = true) -> {
        RouteStage.Accepted
    }

    else -> null
}

fun visibleCustomerAnnouncements(announcements: List<Announcement>): List<Announcement> = announcements
    .filter { it.customerCanSeeExecutionRoute }
    .sortedWith(
        compareByDescending<Announcement> { it.createdAtEpochSeconds ?: 0L }
            .thenByDescending { it.id },
    )

fun buildApproximateRoute(
    start: GeoPoint,
    waypoints: List<GeoPoint>,
    end: GeoPoint,
    travelMode: RouteTravelMode,
): ApproximateRoutePlan {
    val ordered = buildList {
        add(start)
        addAll(waypoints)
        add(end)
    }.distinctBy { "${it.latitude},${it.longitude}" }

    if (ordered.size < 2) {
        return ApproximateRoutePlan(
            polyline = ordered,
            distanceMeters = 0,
            durationSeconds = 0,
            quality = RouteMapGeometryQuality.PointsOnly,
        )
    }

    val distanceMeters = routePolylineLengthMeters(ordered)
    val durationSeconds = estimateRouteDurationSeconds(distanceMeters, travelMode)

    return ApproximateRoutePlan(
        polyline = ordered,
        distanceMeters = distanceMeters.roundToInt(),
        durationSeconds = durationSeconds,
        quality = RouteMapGeometryQuality.Approximate,
    )
}

fun routePlanFromPolyline(
    polyline: List<GeoPoint>,
    travelMode: RouteTravelMode,
    quality: RouteMapGeometryQuality,
): ApproximateRoutePlan {
    val distanceMeters = routePolylineLengthMeters(polyline)
    return ApproximateRoutePlan(
        polyline = polyline,
        distanceMeters = distanceMeters.roundToInt(),
        durationSeconds = estimateRouteDurationSeconds(distanceMeters, travelMode),
        quality = quality,
    )
}

fun routePolylineLengthMeters(polyline: List<GeoPoint>): Double {
    if (polyline.size < 2) return 0.0

    var distanceMeters = 0.0
    for (index in 0 until polyline.lastIndex) {
        distanceMeters += haversineDistanceMeters(
            start = polyline[index],
            end = polyline[index + 1],
        )
    }
    return distanceMeters
}

fun estimateRouteDurationSeconds(
    distanceMeters: Double,
    travelMode: RouteTravelMode,
): Int {
    val speedMetersPerSecond = if (travelMode == RouteTravelMode.Walking) 1.35 else 8.5
    return maxOf(1, (distanceMeters / speedMetersPerSecond).roundToInt())
}

fun orderedAcceptedTasks(
    tasks: List<TaskByRoute>,
    acceptedTaskIds: List<String>,
    basePolyline: List<GeoPoint>,
): List<TaskByRoute> {
    val acceptedIndexById = acceptedTaskIds.withIndex().associate { indexed ->
        indexed.value to indexed.index
    }

    return tasks
        .filter { acceptedTaskIds.contains(it.id) }
        .sortedWith(
            compareBy<TaskByRoute> {
                it.coordinate?.let { point -> approximateProgressOnPolyline(basePolyline, point) }
                    ?: Double.MAX_VALUE
            }.thenBy {
                acceptedIndexById[it.id] ?: Int.MAX_VALUE
            },
        )
}

fun buildPreviewBranchLine(
    basePolyline: List<GeoPoint>,
    point: GeoPoint,
): List<GeoPoint> {
    val anchor = closestPolylinePoint(basePolyline, point) ?: return listOf(point)
    return if (anchor == point) {
        listOf(point)
    } else {
        listOf(anchor, point)
    }
}

fun approximateDetourText(
    distanceToRouteMeters: Double?,
    travelMode: RouteTravelMode,
): String {
    val distance = ((distanceToRouteMeters ?: 180.0) * 2.3).roundToInt().coerceAtLeast(80)
    val speedMetersPerMinute = if (travelMode == RouteTravelMode.Walking) 75.0 else 520.0
    val minutes = maxOf(1, (distance / speedMetersPerMinute).roundToInt())
    return "Отклонение ~${formatDistance(distance)} • $minutes мин"
}

fun routeTaskSummary(task: TaskByRoute): String {
    val parts = buildList {
        task.distanceToRouteMeters?.roundToInt()?.let { add("$it м от маршрута") }
        task.priceText?.takeIf { it.isNotBlank() }?.let(::add)
    }
    return parts.joinToString(separator = " • ")
}

fun routeTaskSubtitle(task: TaskByRoute): String =
    routeTaskSummary(task).ifBlank {
        task.addressText ?: "Задача рядом с маршрутом"
    }

fun routeTaskDescription(
    announcement: Announcement?,
    fallbackAddress: String?,
): String {
    if (announcement == null) {
        return fallbackAddress ?: "Подробности подтянутся из карточки объявления."
    }

    return buildList {
        announcement.detailsDescriptionText?.takeIf { it.isNotBlank() }?.let(::add)
        announcement.primarySourceAddress?.takeIf { it.isNotBlank() }?.let { add("Откуда: $it") }
        announcement.primaryDestinationAddress?.takeIf { it.isNotBlank() }?.let { add("Куда: $it") }
        announcement.formattedBudgetText?.takeIf { it.isNotBlank() }?.let { add("Оплата: $it") }
        announcement.shortStructuredSubtitle.takeIf { it.isNotBlank() }?.let(::add)
    }.distinct().joinToString(separator = "\n").ifBlank {
        fallbackAddress ?: "Подробности подтянутся из карточки объявления."
    }
}

fun routeTaskCoordinate(
    task: TaskByRoute,
    announcement: Announcement?,
): GeoPoint? = task.coordinate
    ?: announcement?.destinationPoint
    ?: announcement?.mapPoint
    ?: announcement?.sourcePoint

fun shouldAllowStageUpdates(
    announcement: Announcement?,
    isPrimaryTask: Boolean,
): Boolean {
    if (isPrimaryTask) return true
    val projection = announcement?.taskStateProjection ?: return false
    return projection.executionStatus.blocksNewOffers || projection.acceptedConfirmed
}

fun routeTaskIsCompleted(
    task: TaskByRoute,
    announcement: Announcement?,
): Boolean {
    val projection = announcement?.taskStateProjection
    if (projection != null) {
        return projection.executionStatus == TaskExecutionStatus.Completed
    }

    return task.status?.contains("complete", ignoreCase = true) == true ||
        task.status?.contains("done", ignoreCase = true) == true
}

fun formatDistance(distanceMeters: Int): String = if (distanceMeters >= 1000) {
    String.format("%.1f км", distanceMeters / 1000.0)
} else {
    "$distanceMeters м"
}

fun formatDuration(durationSeconds: Int): String {
    val minutes = maxOf(1, (durationSeconds / 60.0).roundToInt())
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes == 0) {
            "$hours ч"
        } else {
            "$hours ч $remainingMinutes мин"
        }
    } else {
        "$minutes мин"
    }
}

private fun approximateProgressOnPolyline(
    polyline: List<GeoPoint>,
    point: GeoPoint,
): Double {
    if (polyline.isEmpty()) return Double.MAX_VALUE

    val bestIndex = polyline.indices.minByOrNull { index ->
        haversineDistanceMeters(
            start = polyline[index],
            end = point,
        )
    } ?: return Double.MAX_VALUE

    return bestIndex.toDouble() / polyline.lastIndex.coerceAtLeast(1)
}

private fun closestPolylinePoint(
    polyline: List<GeoPoint>,
    point: GeoPoint,
): GeoPoint? = polyline.minByOrNull { candidate ->
    haversineDistanceMeters(start = candidate, end = point)
}

private fun haversineDistanceMeters(
    start: GeoPoint,
    end: GeoPoint,
): Double {
    val earthRadius = 6_371_000.0
    val latDelta = (end.latitude - start.latitude).toRadians()
    val lonDelta = (end.longitude - start.longitude).toRadians()
    val startLat = start.latitude.toRadians()
    val endLat = end.latitude.toRadians()

    val a = sin(latDelta / 2.0).pow(2.0) +
        cos(startLat) * cos(endLat) * sin(lonDelta / 2.0).pow(2.0)
    val c = 2.0 * asin(sqrt(a))
    return earthRadius * c
}

private fun Double.toRadians(): Double = this * PI / 180.0
