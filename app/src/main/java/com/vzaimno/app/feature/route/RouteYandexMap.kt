package com.vzaimno.app.feature.route

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline as YandexPolyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
internal fun RouteYandexMap(
    state: RouteMapUiState,
    onMarkerSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnMarkerSelected by rememberUpdatedState(onMarkerSelected)
    val mapCollections = remember { RouteMapCollectionsHolder() }
    val markerIconCache = remember { mutableMapOf<String, ImageProvider>() }

    val containerAndMapView = remember {
        val mapView = MapView(context)
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            addView(
                mapView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        Pair(container, mapView)
    }
    val container = containerAndMapView.first
    val mapView = containerAndMapView.second

    var isMapReady by remember { mutableStateOf(false) }
    var isMapStarted by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle

        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (mapView.width <= 0 || mapView.height <= 0 || isMapStarted) return

                mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

                MapKitFactory.getInstance().onStart()
                mapView.onStart()
                isMapStarted = true

                val rootCollection = mapView.mapWindow.map.mapObjects
                mapCollections.polylineCollection = rootCollection.addCollection()
                mapCollections.markerCollection = rootCollection.addCollection()

                mapView.mapWindow.map.move(
                    CameraPosition(
                        Point(55.751244, 37.618423),
                        11.2f,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.3f),
                    null,
                )
                isMapReady = true
            }
        }
        mapView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (isMapStarted) {
                        MapKitFactory.getInstance().onStart()
                        mapView.onStart()
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    if (isMapStarted) {
                        mapView.onStop()
                        MapKitFactory.getInstance().onStop()
                    }
                }

                else -> Unit
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            mapView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            lifecycle.removeObserver(observer)
            if (isMapStarted && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }
    }

    LaunchedEffect(state.polylines, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val polylineCollection = mapCollections.polylineCollection ?: return@LaunchedEffect
        polylineCollection.clear()

        state.polylines.forEach { line ->
            if (line.points.size < 2) return@forEach

            val polyline = polylineCollection.addPolyline(
                YandexPolyline(line.points.map { Point(it.latitude, it.longitude) }),
            )
            when (line.kind) {
                RoutePolylineKind.Main -> {
                    polyline.setStrokeColor(0xFF2BB7A7.toInt())
                    polyline.outlineColor = 0xFFA6E3DB.toInt()
                    polyline.outlineWidth = 2f
                    polyline.strokeWidth = 6f
                    polyline.zIndex = 5f
                }

                RoutePolylineKind.Preview -> {
                    polyline.setStrokeColor(0xFFF2994A.toInt())
                    polyline.outlineColor = 0x00000000
                    polyline.strokeWidth = 4f
                    polyline.dashLength = 16f
                    polyline.gapLength = 10f
                    polyline.zIndex = 2f
                }
            }
        }
    }

    LaunchedEffect(state.markers, state.selectedMarkerId, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val markerCollection = mapCollections.markerCollection ?: return@LaunchedEffect
        markerCollection.clear()

        state.markers.forEach { marker ->
            val isSelected = marker.id == state.selectedMarkerId
            val cacheKey = "${marker.kind.name}|${marker.markerLabel()}|$isSelected"
            val placemark = markerCollection.addPlacemark(
                Point(marker.point.latitude, marker.point.longitude),
                markerIconCache.getOrPut(cacheKey) {
                    ImageProvider.fromBitmap(
                        createRouteMarkerBitmap(
                            label = marker.markerLabel(),
                            kind = marker.kind,
                            isSelected = isSelected,
                        ),
                    )
                },
            )
            placemark.zIndex = when {
                isSelected -> 30f
                marker.kind == RouteMarkerKind.PrimaryTask || marker.kind == RouteMarkerKind.CustomerTask -> 20f
                marker.kind == RouteMarkerKind.AcceptedExtra -> 18f
                marker.kind == RouteMarkerKind.Start -> 16f
                else -> 12f
            }
            placemark.addTapListener(MapObjectTapListener { _, _ ->
                currentOnMarkerSelected(marker.id)
                true
            })
        }
    }

    LaunchedEffect(isMapReady, state.command?.token, state.markers, state.polylines) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapView.mapWindow.map

        when (val command = state.command) {
            null -> Unit
            is RouteMapCommand.FitAll -> fitRouteContent(mapView = mapView, state = state)
            is RouteMapCommand.FocusPoint -> {
                map.move(
                    CameraPosition(
                        Point(command.point.latitude, command.point.longitude),
                        command.zoom,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.45f),
                    null,
                )
            }
            is RouteMapCommand.ZoomIn -> {
                val current = map.cameraPosition
                map.move(
                    CameraPosition(
                        current.target,
                        (current.zoom + 0.75f).coerceIn(8f, 18f),
                        current.azimuth,
                        current.tilt,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.25f),
                    null,
                )
            }
            is RouteMapCommand.ZoomOut -> {
                val current = map.cameraPosition
                map.move(
                    CameraPosition(
                        current.target,
                        (current.zoom - 0.75f).coerceIn(8f, 18f),
                        current.azimuth,
                        current.tilt,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.25f),
                    null,
                )
            }
        }
    }

    AndroidView(
        factory = { container },
        modifier = modifier,
        update = { view ->
            view.post {
                view.requestLayout()
                view.invalidate()
            }
        },
    )
}

private class RouteMapCollectionsHolder {
    var polylineCollection: MapObjectCollection? = null
    var markerCollection: MapObjectCollection? = null
}

private fun fitRouteContent(
    mapView: MapView,
    state: RouteMapUiState,
) {
    val map = mapView.mapWindow.map
    val allPoints = buildList {
        addAll(state.markers.map { Point(it.point.latitude, it.point.longitude) })
        addAll(
            state.polylines.flatMap { polyline ->
                polyline.points.map { Point(it.latitude, it.longitude) }
            },
        )
    }.distinctBy { point -> "${point.latitude}|${point.longitude}" }

    when (allPoints.size) {
        0 -> Unit
        1 -> {
            map.move(
                CameraPosition(allPoints.first(), 14f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.45f),
                null,
            )
        }

        else -> {
            val south = allPoints.minOf { it.latitude }
            val west = allPoints.minOf { it.longitude }
            val north = allPoints.maxOf { it.latitude }
            val east = allPoints.maxOf { it.longitude }
            val boundingBox = BoundingBox(
                Point(south, west),
                Point(north, east),
            )
            val cameraPosition = map.cameraPosition(Geometry.fromBoundingBox(boundingBox))
            map.move(
                CameraPosition(
                    cameraPosition.target,
                    (cameraPosition.zoom - 0.45f).coerceAtLeast(3f),
                    0f,
                    0f,
                ),
                Animation(Animation.Type.SMOOTH, 0.55f),
                null,
            )
        }
    }
}

private fun RouteMapMarkerUi.markerLabel(): String = when (kind) {
    RouteMarkerKind.Start -> "A"
    RouteMarkerKind.PrimaryTask -> "1"
    RouteMarkerKind.AcceptedExtra -> "+"
    RouteMarkerKind.Preview -> "P"
    RouteMarkerKind.CustomerTask -> "C"
}

private fun createRouteMarkerBitmap(
    label: String,
    kind: RouteMarkerKind,
    isSelected: Boolean,
): android.graphics.Bitmap {
    val density = android.content.res.Resources.getSystem().displayMetrics.density
    val size = (44f * density).toInt().coerceAtLeast(44)
    val bitmap = android.graphics.Bitmap.createBitmap(
        size,
        size,
        android.graphics.Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)
    val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
    val radius = size / 2f

    val fillColor = when {
        isSelected -> 0xFFF2994A.toInt()
        kind == RouteMarkerKind.Start -> 0xFFFFFFFF.toInt()
        kind == RouteMarkerKind.PrimaryTask -> 0xFF2BB7A7.toInt()
        kind == RouteMarkerKind.AcceptedExtra -> 0xFF1F9D91.toInt()
        kind == RouteMarkerKind.Preview -> 0xFFFFFFFF.toInt()
        kind == RouteMarkerKind.CustomerTask -> 0xFF4A90E2.toInt()
        else -> 0xFF2BB7A7.toInt()
    }
    val strokeColor = when {
        isSelected -> 0xFFF2994A.toInt()
        kind == RouteMarkerKind.Start -> 0x332BB7A7
        kind == RouteMarkerKind.Preview -> 0x66F2994A
        else -> fillColor
    }
    val textColor = when {
        kind == RouteMarkerKind.Start || kind == RouteMarkerKind.Preview -> 0xFF1C1C1E.toInt()
        else -> 0xFFFFFFFF.toInt()
    }

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 15f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    canvas.drawRoundRect(rect, radius, radius, fillPaint)
    canvas.drawRoundRect(rect, radius, radius, strokePaint)
    val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(label, size / 2f, textY, textPaint)

    return bitmap
}
