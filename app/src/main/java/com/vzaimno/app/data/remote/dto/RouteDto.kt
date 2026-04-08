package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteCoordinateDto(
    val lat: Double,
    val lon: Double,
)

@Serializable
data class RouteContextDto(
    @SerialName("entity_id") val entityId: String,
    @SerialName("start_address") val startAddress: String,
    @SerialName("end_address") val endAddress: String,
    val start: RouteCoordinateDto,
    val end: RouteCoordinateDto,
    @SerialName("radius_m") val radiusM: Int,
    @SerialName("travel_mode") val travelMode: String,
)

@Serializable
data class RouteBuildRequestDto(
    @SerialName("announcement_id") val announcementId: String? = null,
    val polyline: List<List<Double>>,
    @SerialName("start_address") val startAddress: String? = null,
    @SerialName("end_address") val endAddress: String? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("radius_m") val radiusM: Int,
    @SerialName("travel_mode") val travelMode: String,
)

@Serializable
data class RouteDetailsDto(
    @SerialName("entity_id") val entityId: String,
    @SerialName("start_address") val startAddress: String,
    @SerialName("end_address") val endAddress: String,
    @SerialName("distance_meters") val distanceMeters: Int,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("distance_text") val distanceText: String,
    @SerialName("duration_text") val durationText: String,
    val polyline: List<List<Double>>,
    @SerialName("tasks_by_route") val tasksByRoute: List<TaskByRouteDto> = emptyList(),
)

@Serializable
data class TaskByRouteDto(
    val id: String,
    val title: String,
    val category: String? = null,
    @SerialName("address_text") val addressText: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("distance_to_route_meters") val distanceToRouteMeters: Double? = null,
    @SerialName("price_text") val priceText: String? = null,
    @SerialName("preview_image_url") val previewImageUrl: String? = null,
    val status: String? = null,
)
