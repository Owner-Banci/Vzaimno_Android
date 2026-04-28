package com.vzaimno.app.core.map

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vzaimno.app.R
import com.yandex.mapkit.mapview.MapView

fun createMovableYandexMapView(context: Context): MapView =
    (LayoutInflater.from(context).inflate(R.layout.view_yandex_map_movable, null, false) as MapView).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
