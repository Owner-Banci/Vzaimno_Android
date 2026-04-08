package com.vzaimno.app.core.config

import androidx.compose.runtime.Immutable
import com.vzaimno.app.core.common.ensureTrailingSlash
import com.vzaimno.app.core.common.toWebSocketBaseUrl

enum class AppEnvironment(val rawValue: String) {
    Local("local"),
    Debug("debug"),
    Staging("staging"),
    Production("production"),
    Custom("custom"),
    Unknown("unknown"),
    ;

    companion object {
        fun from(rawValue: String?): AppEnvironment = entries.firstOrNull {
            it.rawValue == rawValue?.trim()?.lowercase()
        } ?: Unknown
    }
}

@Immutable
data class AppConfig(
    val environment: AppEnvironment,
    val apiBaseUrl: String,
    val webSocketBaseUrl: String,
    val authEnabled: Boolean,
) {
    val normalizedApiBaseUrl: String = apiBaseUrl.ensureTrailingSlash()
    val normalizedWebSocketBaseUrl: String = webSocketBaseUrl
        .ifBlank { normalizedApiBaseUrl.toWebSocketBaseUrl() }
        .ensureTrailingSlash()
}
