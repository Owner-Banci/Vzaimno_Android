package com.vzaimno.app.core.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import java.util.Locale
import kotlin.math.roundToInt

fun JsonObject.jsonAt(path: List<String>): JsonElement? {
    if (path.isEmpty()) return null

    var current: JsonElement = this
    path.forEach { key ->
        current = (current as? JsonObject)?.get(key) ?: return null
    }

    return current
}

fun JsonElement?.asJsonObjectOrNull(): JsonObject? = this as? JsonObject

fun JsonElement?.asJsonArrayOrNull(): JsonArray? = this as? JsonArray

fun JsonElement?.stringOrNullCompat(): String? {
    if (this == null || this is JsonNull) return null
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.content.trimmedOrNull()
}

fun JsonElement?.boolOrNullCompat(): Boolean? {
    val primitive = this as? JsonPrimitive ?: return null
    if (!primitive.isString) {
        return primitive.booleanOrNull
    }

    return when (primitive.content.trim().lowercase(Locale.ROOT)) {
        "true", "1", "yes" -> true
        "false", "0", "no" -> false
        else -> null
    }
}

fun JsonElement?.doubleOrNullCompat(): Double? {
    val primitive = this as? JsonPrimitive ?: return null
    if (!primitive.isString) {
        return primitive.doubleOrNull
    }

    val normalized = primitive.content
        .trim()
        .replace(" ", "")
        .replace(',', '.')

    return normalized.toDoubleOrNull()
}

fun JsonElement?.intOrNullCompat(): Int? {
    val primitive = this as? JsonPrimitive ?: return null
    if (!primitive.isString) {
        primitive.longOrNull?.let { return it.toInt() }
        primitive.doubleOrNull?.let { return it.roundToInt() }
    }

    val normalized = primitive.content
        .trim()
        .replace(" ", "")
        .replace(',', '.')

    return normalized.toDoubleOrNull()?.roundToInt()
}

fun JsonObject.taskStringValue(
    paths: List<List<String>>,
    legacyKeys: List<String> = emptyList(),
): String? {
    paths.forEach { path ->
        jsonAt(path).stringOrNullCompat()?.let { return it }
    }

    legacyKeys.forEach { key ->
        get(key).stringOrNullCompat()?.let { return it }
    }

    return null
}

fun JsonObject.taskBoolValue(
    paths: List<List<String>>,
    legacyKeys: List<String> = emptyList(),
): Boolean? {
    paths.forEach { path ->
        jsonAt(path).boolOrNullCompat()?.let { return it }
    }

    legacyKeys.forEach { key ->
        get(key).boolOrNullCompat()?.let { return it }
    }

    return null
}

fun JsonObject.taskIntValue(
    paths: List<List<String>>,
    legacyKeys: List<String> = emptyList(),
): Int? {
    paths.forEach { path ->
        jsonAt(path).intOrNullCompat()?.let { return it }
    }

    legacyKeys.forEach { key ->
        get(key).intOrNullCompat()?.let { return it }
    }

    return null
}

fun JsonObject.taskDateString(paths: List<List<String>>): String? {
    paths.forEach { path ->
        jsonAt(path).stringOrNullCompat()?.let { return it }
    }
    return null
}
