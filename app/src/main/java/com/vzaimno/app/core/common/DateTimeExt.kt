package com.vzaimno.app.core.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun parseInstant(rawValue: String?): Instant? {
    val normalized = rawValue.trimmedOrNull() ?: return null

    return runCatching { Instant.parse(normalized) }
        .recoverCatching { OffsetDateTime.parse(normalized).toInstant() }
        .recoverCatching { LocalDateTime.parse(normalized).toInstant(ZoneOffset.UTC) }
        .getOrNull()
}
