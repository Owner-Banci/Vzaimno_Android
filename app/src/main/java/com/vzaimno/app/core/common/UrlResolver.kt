package com.vzaimno.app.core.common

private val urlSchemeRegex = Regex("^[a-zA-Z][a-zA-Z\\d+\\-.]*://")

fun resolveAgainstBaseUrl(baseUrl: String, rawValue: String?): String? {
    val normalized = rawValue.trimmedOrNull() ?: return null
    if (urlSchemeRegex.containsMatchIn(normalized)) return normalized

    val relativePath = normalized.trim('/')
    if (relativePath.isEmpty()) return null

    return "${baseUrl.ensureTrailingSlash().trimEnd('/')}/$relativePath"
}

fun String.ensureTrailingSlash(): String = if (endsWith('/')) this else "$this/"

fun String.toWebSocketBaseUrl(): String = when {
    startsWith("https://") -> replaceFirst("https://", "wss://")
    startsWith("http://") -> replaceFirst("http://", "ws://")
    else -> this
}
