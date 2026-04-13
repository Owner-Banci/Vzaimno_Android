package com.vzaimno.app.feature.discovery

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.ViewGroup
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
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
internal fun MapCanvas(
    state: DiscoveryUiState,
    onOpenAnnouncementDetails: (String) -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
    onMapLoaded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMapReady by remember { mutableStateOf(false) }
    val currentOnOpenDetails by rememberUpdatedState(onOpenAnnouncementDetails)
    val currentOnMapFocusConsumed by rememberUpdatedState(onMapFocusConsumed)
    val currentOnMapLoaded by rememberUpdatedState(onMapLoaded)

    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        // If already started, immediately start MapKit and MapView
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            MapKitFactory.getInstance().onStart()
            mapView.onStart()
        }

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
                Lifecycle.Event.ON_RESUME -> {
                    // Force invalidate to fix partial tile rendering after tab switches
                    mapView.post { mapView.invalidate() }
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // Ensure we stop when composable leaves composition
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                mapView.onStop()
            }
        }
    }

    LaunchedEffect(Unit) {
        isMapReady = true
        currentOnMapLoaded()
    }

    // Place markers with custom price badges
    LaunchedEffect(isMapReady, state.announcements) {
        if (!isMapReady) return@LaunchedEffect
        val mapObjects = mapView.mapWindow.map.mapObjects
        mapObjects.clear()

        state.announcements.forEach { item ->
            val geo = item.point ?: return@forEach
            val point = Point(geo.latitude, geo.longitude)

            val priceLabel = item.budgetText ?: item.announcement.title.take(12)
            val markerBitmap = createPriceMarkerBitmap(priceLabel)
            val imageProvider = ImageProvider.fromBitmap(markerBitmap)

            val placemark = mapObjects.addPlacemark(point, imageProvider)
            placemark.zIndex = 1f
            placemark.addTapListener(MapObjectTapListener { _, _ ->
                currentOnOpenDetails(item.announcement.id)
                true
            })
        }
    }

    // Handle viewport changes with proper BoundingBox
    LaunchedEffect(isMapReady, state.mapViewport) {
        if (!isMapReady) return@LaunchedEffect
        val viewport = state.mapViewport
        val map = mapView.mapWindow.map

        when (viewport) {
            is DiscoveryMapViewport.FocusPoint -> {
                map.move(
                    CameraPosition(
                        Point(viewport.point.latitude, viewport.point.longitude),
                        viewport.zoom,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.4f),
                    null,
                )
            }
            is DiscoveryMapViewport.Bounds -> {
                val sw = viewport.southWest
                val ne = viewport.northEast
                val boundingBox = BoundingBox(
                    Point(sw.latitude, sw.longitude),
                    Point(ne.latitude, ne.longitude),
                )
                // Add padding for overlay elements: top search bar ~120dp, bottom card ~80dp
                val geometry = Geometry.fromBoundingBox(boundingBox)
                val cameraPosition = mapView.mapWindow.map.cameraPosition(geometry)
                val adjustedZoom = (cameraPosition.target.let { cameraPosition.zoom } - 0.5f)
                    .coerceAtLeast(3f)
                map.move(
                    CameraPosition(
                        cameraPosition.target,
                        adjustedZoom,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.5f),
                    null,
                )
            }
            is DiscoveryMapViewport.Fallback -> {
                map.move(
                    CameraPosition(
                        Point(viewport.point.latitude, viewport.point.longitude),
                        viewport.zoom,
                        0f,
                        0f,
                    ),
                    Animation(Animation.Type.SMOOTH, 0.4f),
                    null,
                )
            }
        }
    }

    // Handle one-shot focus requests (e.g. "show on map")
    LaunchedEffect(isMapReady, state.mapFocusRequest?.token) {
        if (!isMapReady) return@LaunchedEffect
        val request = state.mapFocusRequest ?: return@LaunchedEffect
        val map = mapView.mapWindow.map
        map.move(
            CameraPosition(
                Point(request.point.latitude, request.point.longitude),
                request.zoom,
                0f,
                0f,
            ),
            Animation(Animation.Type.SMOOTH, 0.4f),
            null,
        )
        currentOnMapFocusConsumed(request.token)
    }

    AndroidView(
        factory = { mapView },
        update = { view ->
            // Force relayout after recomposition to prevent partial tile rendering.
            // MapView uses a GL surface that can get clipped if the container resizes
            // without the view being re-laid-out.
            view.post {
                view.requestLayout()
                view.invalidate()
            }
        },
        modifier = modifier,
    )
}

/**
 * Creates a bitmap marker with a price label, styled as a rounded pill badge.
 */
private fun createPriceMarkerBitmap(priceText: String): android.graphics.Bitmap {
    val density = android.content.res.Resources.getSystem().displayMetrics.density
    val textSize = 11f * density
    val horizontalPadding = 10f * density
    val verticalPadding = 6f * density
    val cornerRadius = 14f * density
    val shadowOffset = 2f * density

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = 0xFF1C1C1E.toInt()
    }

    val textWidth = textPaint.measureText(priceText)
    val textHeight = textPaint.fontMetrics.let { it.descent - it.ascent }

    val bitmapWidth = (textWidth + horizontalPadding * 2).toInt() + 1
    val bitmapHeight = (textHeight + verticalPadding * 2 + shadowOffset).toInt() + 1

    val bitmap = android.graphics.Bitmap.createBitmap(
        bitmapWidth,
        bitmapHeight,
        android.graphics.Bitmap.Config.ARGB_8888,
    )

    val canvas = Canvas(bitmap)

    // Shadow
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x30000000
    }
    canvas.drawRoundRect(
        RectF(0f, shadowOffset, bitmapWidth.toFloat(), bitmapHeight.toFloat()),
        cornerRadius,
        cornerRadius,
        shadowPaint,
    )

    // Background pill
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
    }
    canvas.drawRoundRect(
        RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight - shadowOffset),
        cornerRadius,
        cornerRadius,
        bgPaint,
    )

    // Text
    val textX = horizontalPadding
    val textY = verticalPadding - textPaint.fontMetrics.ascent
    canvas.drawText(priceText, textX, textY, textPaint)

    return bitmap
}
