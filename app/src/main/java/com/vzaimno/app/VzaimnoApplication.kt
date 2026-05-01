package com.vzaimno.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VzaimnoApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        val apiKey = BuildConfig.YANDEX_MAPKIT_API_KEY
        if (apiKey.isNotBlank()) {
            MapKitFactory.setApiKey(apiKey)
        }
        MapKitFactory.initialize(this)
    }

    override fun newImageLoader(): ImageLoader = imageLoader
}
