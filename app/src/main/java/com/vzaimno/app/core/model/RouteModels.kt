package com.vzaimno.app.core.model

enum class RouteTravelMode(val rawValue: String) {
    Driving("driving"),
    Walking("walking"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): RouteTravelMode = entries.firstOrNull {
            it.rawValue == rawValue?.trim()?.lowercase()
        } ?: Driving
    }
}

data class RouteContext(
    val entityId: String,
    val startAddress: String,
    val endAddress: String,
    val start: GeoPoint,
    val end: GeoPoint,
    val radiusM: Int,
    val travelMode: RouteTravelMode,
)

data class RouteBuildRequest(
    val announcementId: String?,
    val polyline: List<GeoPoint>,
    val startAddress: String?,
    val endAddress: String?,
    val distanceMeters: Int?,
    val durationSeconds: Int?,
    val radiusM: Int,
    val travelMode: RouteTravelMode,
)

data class RouteDetails(
    val entityId: String,
    val startAddress: String,
    val endAddress: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val distanceText: String,
    val durationText: String,
    val polyline: List<GeoPoint>,
    val tasksByRoute: List<TaskByRoute>,
)

data class TaskByRoute(
    val id: String,
    val title: String,
    val category: String?,
    val addressText: String?,
    val coordinate: GeoPoint?,
    val distanceToRouteMeters: Double?,
    val priceText: String?,
    val previewImageUrl: String?,
    val status: String?,
)
