package com.vzaimno.app.feature.route

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vzaimno.app.core.model.GeoPoint
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline as YandexPolyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView

@Composable
internal fun RouteYandexMap(
    state: RouteMapUiState,
    onMarkerSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMapReady by remember { mutableStateOf(false) }
    val currentOnMarkerSelected by rememberUpdatedState(onMarkerSelected)

    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        isMapReady = true
    }

    // Draw polylines and markers
    LaunchedEffect(isMapReady, state.markers, state.polylines, state.selectedMarkerId) {
        if (!isMapReady) return@LaunchedEffect
        val mapObjects = mapView.mapWindow.map.mapObjects
        mapObjects.clear()

        // Polylines
        state.polylines.forEach { line ->
            if (line.points.size < 2) return@forEach
            val points = line.points.map { Point(it.latitude, it.longitude) }
            val polyline = mapObjects.addPolyline(YandexPolyline(points))
            polyline.strokeWidth = if (line.kind == RoutePolylineKind.Main) 5f else 3f
            polyline.setStrokeColor(
                when (line.kind) {
                    RoutePolylineKind.Main -> 0xFF1888FF.toInt()
                    RoutePolylineKind.Preview -> 0xFF7DB7FF.toInt()
                },
            )
            polyline.zIndex = if (line.kind == RoutePolylineKind.Main) 1f else 0f
        }

        // Markers
        state.markers.forEach { marker ->
            val isSelected = marker.id == state.selectedMarkerId
            val point = Point(marker.point.latitude, marker.point.longitude)
            val placemark = mapObjects.addPlacemark(point)
            placemark.zIndex = if (isSelected) 10f else 1f
            placemark.addTapListener(MapObjectTapListener { _, _ ->
                currentOnMarkerSelected(marker.id)
                true
            })
        }
    }

    // Handle camera commands
    LaunchedEffect(isMapReady, state.command?.token) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapView.mapWindow.map
        when (val command = state.command) {
            null -> Unit
            is RouteMapCommand.FitAll -> {
                val allPoints = buildList {
                    addAll(state.markers.map { Point(it.point.latitude, it.point.longitude) })
                    addAll(
                        state.polylines.flatMap { line ->
                            line.points.map { Point(it.latitude, it.longitude) }
                        },
                    )
                }.distinct()

                when {
                    allPoints.isEmpty() -> Unit
                    allPoints.size == 1 -> {
                        map.move(
                            CameraPosition(allPoints.first(), 13.7f, 0f, 0f),
                            Animation(Animation.Type.SMOOTH, 0.4f),
                            null,
                        )
                    }
                    else -> {
                        val centerLat = allPoints.sumOf { it.latitude.toDouble() } / allPoints.size
                        val centerLon = allPoints.sumOf { it.longitude.toDouble() } / allPoints.size
                        map.move(
                            CameraPosition(Point(centerLat, centerLon), 12f, 0f, 0f),
                            Animation(Animation.Type.SMOOTH, 0.4f),
                            null,
                        )
                    }
                }
            }
            is RouteMapCommand.FocusPoint -> {
                map.move(
                    CameraPosition(
                        Point(command.point.latitude, command.point.longitude),
                        command.zoom,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.4f),
                    null,
                )
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
    )
}
