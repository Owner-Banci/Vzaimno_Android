package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.common.resolveAgainstBaseUrl
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.RouteBuildRequest
import com.vzaimno.app.core.model.RouteContext
import com.vzaimno.app.core.model.RouteDetails
import com.vzaimno.app.core.model.RouteTravelMode
import com.vzaimno.app.core.model.TaskByRoute
import com.vzaimno.app.data.remote.dto.RouteBuildRequestDto
import com.vzaimno.app.data.remote.dto.RouteContextDto
import com.vzaimno.app.data.remote.dto.RouteCoordinateDto
import com.vzaimno.app.data.remote.dto.RouteDetailsDto
import com.vzaimno.app.data.remote.dto.TaskByRouteDto

fun RouteCoordinateDto.toDomain(): GeoPoint = GeoPoint(
    latitude = lat,
    longitude = lon,
)

fun RouteContextDto.toDomain(): RouteContext = RouteContext(
    entityId = entityId,
    startAddress = startAddress,
    endAddress = endAddress,
    start = start.toDomain(),
    end = end.toDomain(),
    radiusM = radiusM,
    travelMode = RouteTravelMode.fromRaw(travelMode),
)

fun RouteBuildRequest.toDto(): RouteBuildRequestDto = RouteBuildRequestDto(
    announcementId = announcementId,
    polyline = polyline.map { listOf(it.latitude, it.longitude) },
    startAddress = startAddress,
    endAddress = endAddress,
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
    radiusM = radiusM,
    travelMode = travelMode.rawValue,
)

fun TaskByRouteDto.toDomain(apiBaseUrl: String): TaskByRoute = TaskByRoute(
    id = id,
    title = title,
    category = category,
    addressText = addressText,
    coordinate = if (latitude != null && longitude != null) {
        GeoPoint(latitude = latitude, longitude = longitude)
    } else {
        null
    },
    distanceToRouteMeters = distanceToRouteMeters,
    priceText = priceText,
    previewImageUrl = resolveAgainstBaseUrl(apiBaseUrl, previewImageUrl),
    status = status,
)

fun RouteDetailsDto.toDomain(apiBaseUrl: String): RouteDetails = RouteDetails(
    entityId = entityId,
    startAddress = startAddress,
    endAddress = endAddress,
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
    distanceText = distanceText,
    durationText = durationText,
    polyline = polyline.mapNotNull { pair ->
        if (pair.size < 2) {
            null
        } else {
            GeoPoint(latitude = pair[0], longitude = pair[1])
        }
    },
    tasksByRoute = tasksByRoute.map { it.toDomain(apiBaseUrl) },
)
