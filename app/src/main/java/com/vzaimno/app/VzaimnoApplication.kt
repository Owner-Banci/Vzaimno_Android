package com.vzaimno.app

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VzaimnoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val apiKey = BuildConfig.YANDEX_MAPKIT_API_KEY
        if (apiKey.isNotBlank()) {
            MapKitFactory.setApiKey(apiKey)
        }
        MapKitFactory.initialize(this)
    }
}
