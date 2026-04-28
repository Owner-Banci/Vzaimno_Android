package com.vzaimno.app.feature.discovery

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.vzaimno.app.core.map.YandexMapKitLifecycle
import com.vzaimno.app.core.map.createMovableYandexMapView
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
        createMovableYandexMapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        // If already started, immediately start MapKit and MapView
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            YandexMapKitLifecycle.start(mapView)
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    YandexMapKitLifecycle.start(mapView)
                }
                Lifecycle.Event.ON_STOP -> {
                    YandexMapKitLifecycle.stop(mapView)
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Force invalidate to fix partial tile rendering after tab switches
                    YandexMapKitLifecycle.refreshSurface(mapView)
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // Ensure we stop when composable leaves composition
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                YandexMapKitLifecycle.stop(mapView)
            }
        }
    }

    LaunchedEffect(Unit) {
        isMapReady = true
        currentOnMapLoaded()
    }

    // Per-composition bitmap cache keyed by label text — avoids re-rasterizing
    // the same price pill when the list re-emits with identical items.
    val bitmapCache = remember { mutableMapOf<String, android.graphics.Bitmap>() }

    // Stable key: only re-run when the set of (id, label, lat, lng) actually
    // changes. Without this, every syncPresentation() triggers a full rebuild
    // even though the announcements are identical.
    val markersKey by remember(state.announcements) {
        derivedStateOf {
            state.announcements.joinToString("|") { item ->
                val geo = item.point
                val label = item.budgetText ?: item.announcement.title.take(12)
                "${item.announcement.id}:$label:${geo?.latitude}:${geo?.longitude}"
            }
        }
    }

    // Place markers with custom price badges. Heavy work (bitmap creation)
    // runs on Dispatchers.Default; only MapKit writes happen on the main
    // thread. A short debounce collapses rapid successive emissions.
    LaunchedEffect(isMapReady, markersKey) {
        if (!isMapReady) return@LaunchedEffect
        delay(60) // debounce burst updates

        data class Prepared(
            val id: String,
            val point: Point,
            val image: ImageProvider,
        )

        val announcements = state.announcements
        val prepared = withContext(Dispatchers.Default) {
            announcements.mapNotNull { item ->
                val geo = item.point ?: return@mapNotNull null
                val priceLabel = item.budgetText ?: item.announcement.title.take(12)
                val bitmap = bitmapCache.getOrPut(priceLabel) {
                    createPriceMarkerBitmap(priceLabel)
                }
                Prepared(
                    id = item.announcement.id,
                    point = Point(geo.latitude, geo.longitude),
                    image = ImageProvider.fromBitmap(bitmap),
                )
            }
        }

        val mapObjects = mapView.mapWindow.map.mapObjects
        mapObjects.clear()
        prepared.forEach { marker ->
            val placemark = mapObjects.addPlacemark(marker.point, marker.image)
            placemark.zIndex = 1f
            placemark.addTapListener(MapObjectTapListener { _, _ ->
                currentOnOpenDetails(marker.id)
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
            YandexMapKitLifecycle.refreshSurface(view)
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
