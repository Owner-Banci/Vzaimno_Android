package com.vzaimno.app.core.map

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Coordinates the process-wide MapKit lifecycle across multiple Compose screens.
 *
 * MapKitFactory is global, while every MapView has its own lifecycle. Tracking
 * started MapView instances prevents one screen from stopping MapKit while
 * another map screen is still alive.
 */
object YandexMapKitLifecycle {
    private val startedMapViews: MutableSet<MapView> = Collections.newSetFromMap(IdentityHashMap())

    fun start(mapView: MapView) {
        if (!startedMapViews.add(mapView)) return

        if (startedMapViews.size == 1) {
            MapKitFactory.getInstance().onStart()
        }
        mapView.onStart()
    }

    fun stop(mapView: MapView) {
        if (!startedMapViews.remove(mapView)) return

        mapView.onStop()
        if (startedMapViews.isEmpty()) {
            MapKitFactory.getInstance().onStop()
        }
    }

    fun refreshSurface(mapView: MapView, resyncCamera: Boolean = true) {
        fun refreshOnce() {
            mapView.requestLayout()
            mapView.invalidate()
            if (resyncCamera && mapView.width > 0 && mapView.height > 0) {
                runCatching {
                    val map = mapView.mapWindow.map
                    map.move(map.cameraPosition)
                }
            }
        }

        mapView.post {
            refreshOnce()
            mapView.postDelayed(::refreshOnce, 80L)
        }
    }
}
