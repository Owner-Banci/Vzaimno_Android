package com.vzaimno.app.feature.route

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openRouteInMaps(
    context: Context,
    navigation: RouteExternalNavigationUi,
): Boolean {
    val googleUri = Uri.parse(buildGoogleDirectionsUrl(navigation))
    val packageManager = context.packageManager

    val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleUri)
        .setPackage(GOOGLE_MAPS_PACKAGE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (googleMapsIntent.resolveActivity(packageManager) != null) {
        context.startActivity(googleMapsIntent)
        return true
    }

    val chooserIntent = Intent.createChooser(
        Intent(Intent.ACTION_VIEW, googleUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        "Открыть маршрут",
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (chooserIntent.resolveActivity(packageManager) != null) {
        context.startActivity(chooserIntent)
        return true
    }

    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(
            "geo:${navigation.destination.latitude},${navigation.destination.longitude}" +
                "?q=${navigation.destination.latitude},${navigation.destination.longitude}",
        ),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (geoIntent.resolveActivity(packageManager) != null) {
        context.startActivity(
            Intent.createChooser(geoIntent, "Открыть маршрут").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        return true
    }

    return false
}

private fun buildGoogleDirectionsUrl(navigation: RouteExternalNavigationUi): String {
    val params = mutableListOf(
        "api=1",
        "destination=${navigation.destination.latitude},${navigation.destination.longitude}",
        "travelmode=${navigation.travelMode.toGoogleTravelMode()}",
    )

    navigation.origin?.let { origin ->
        params += "origin=${origin.latitude},${origin.longitude}"
    }

    if (navigation.waypoints.isNotEmpty()) {
        params += "waypoints=${navigation.waypoints.joinToString(separator = "|") { point ->
            "${point.latitude},${point.longitude}"
        }}"
    }

    return "https://www.google.com/maps/dir/?${params.joinToString(separator = "&")}"
}

private fun com.vzaimno.app.core.model.RouteTravelMode.toGoogleTravelMode(): String = when (this) {
    com.vzaimno.app.core.model.RouteTravelMode.Driving -> "driving"
    com.vzaimno.app.core.model.RouteTravelMode.Walking -> "walking"
}

private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
