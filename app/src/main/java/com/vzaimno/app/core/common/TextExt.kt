package com.vzaimno.app.core.common

import java.text.NumberFormat
import java.util.Locale

fun String?.trimmedOrNull(): String? {
    val trimmed = this?.trim()
    return if (trimmed.isNullOrEmpty()) null else trimmed
}

fun Int.formatRubles(locale: Locale = Locale("ru", "RU")): String {
    val formatter = NumberFormat.getIntegerInstance(locale)
    return "${formatter.format(this)} ₽"
}

fun humanizeRawValue(rawValue: String): String = rawValue
    .replace('_', ' ')
    .replace('-', ' ')
    .replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
    }
