package com.vzaimno.app.feature.ads.create

import com.vzaimno.app.core.model.AnnouncementStructuredData

object CreateAdValidation {

    fun readinessIssues(draft: AnnouncementCreateFormDraft): List<String> = buildList {
        val action = draft.actionType

        if (action == null) {
            add("Выберите, что нужно сделать")
            return@buildList
        }

        // Item / purchase / help type required
        when (action) {
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Carry,
            -> if (draft.itemType == null) {
                add(
                    if (action == AnnouncementStructuredData.ActionType.Pickup) {
                        "Уточните, что именно нужно забрать"
                    } else {
                        "Уточните, что именно нужно перенести"
                    },
                )
            }

            AnnouncementStructuredData.ActionType.Buy ->
                if (draft.purchaseType == null) add("Выберите тип покупки")

            AnnouncementStructuredData.ActionType.ProHelp ->
                if (draft.helpType == null) add("Выберите вид помощи")

            else -> {}
        }

        // Task brief
        if (draft.requiresTaskBrief && draft.taskBrief.isBlank()) {
            add("Опишите задачу")
        }

        // Source kind
        if (draft.sourceKind == null) {
            add("Выберите, откуда начинается задача")
        }

        // Source address
        if (draft.source.address.isBlank()) {
            add("Укажите поле «${draft.sourceFieldLabel}»")
        }

        // Destination required
        when (action) {
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Buy,
            AnnouncementStructuredData.ActionType.Ride,
            -> {
                if (draft.destinationKind == null) add("Выберите, куда нужно доставить")
                if (draft.destination.address.isBlank()) add("Укажите поле «${draft.destinationFieldLabel}»")
            }
            else -> {}
        }

        // Addresses must differ
        val src = draft.source.address.trim()
        val dst = draft.destination.address.trim()
        when (action) {
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Buy,
            -> {
                if (src.isNotBlank() && dst.isNotBlank() && src.equals(dst, ignoreCase = true)) {
                    add("Адреса отправления и назначения не должны совпадать")
                }
            }
            AnnouncementStructuredData.ActionType.Carry -> {
                if (dst.isNotBlank() && src.isNotBlank() && src.equals(dst, ignoreCase = true)) {
                    add("Адреса отправления и назначения не должны совпадать")
                }
            }
            else -> {}
        }

        // Budget range
        val budgetMin = draft.budget.min.toIntOrNull()
        val budgetMax = draft.budget.max.toIntOrNull()
        if (budgetMin != null && budgetMax != null && budgetMin > budgetMax) {
            add("Минимальная цена не может быть больше максимальной")
        }

        val startAt = runCatching { java.time.Instant.parse(draft.startDate) }.getOrNull()
        val endAt = runCatching { java.time.Instant.parse(draft.endDate) }.getOrNull()
        if (draft.hasEndTime && startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            add("Крайнее время должно быть позже времени начала")
        }

        // Dimensions (0–500)
        listOf(
            draft.attributes.cargoLength to "длина",
            draft.attributes.cargoWidth to "ширина",
            draft.attributes.cargoHeight to "высота",
        ).forEach { (value, label) ->
            when (val error = validateIntField(value, 0..500, "Габариты ($label)")) {
                null -> Unit
                else -> add(error)
            }
        }

        // Estimated minutes (0–1440)
        validateIntField(
            rawValue = draft.attributes.estimatedTaskMinutes,
            range = 0..1440,
            label = "Оценка по времени",
        )?.let(::add)

        // Wait on site requires waiting minutes
        if (draft.attributes.waitOnSite && draft.attributes.waitingMinutes.isBlank()) {
            add("Укажите, сколько можно подождать на месте")
        }
        validateIntField(
            rawValue = draft.attributes.waitingMinutes,
            range = 0..1440,
            label = "Время ожидания",
        )?.let(::add)

        // Lift to floor requires floor
        if (draft.attributes.requiresLiftToFloor && draft.attributes.floor.isBlank()) {
            add("Укажите этаж")
        }
        validateIntField(
            rawValue = draft.attributes.floor,
            range = 0..100,
            label = "Этаж",
        )?.let(::add)

        // Generated title
        if (draft.generatedTitle.isBlank()) {
            add("Не удалось собрать заголовок объявления")
        }

        // Resolved description
        if (draft.resolvedDescription.isBlank()) {
            add("Не удалось собрать описание объявления")
        }

        // Phone optional but validate format if provided
        if (draft.contacts.phone.isNotBlank()) {
            val cleaned = draft.contacts.phone.filter { it.isDigit() || it == '+' }
            if (cleaned.length < 10) {
                add("Проверьте номер телефона")
            }
        }
    }.distinct()

    private fun validateIntField(rawValue: String, range: IntRange, label: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return null
        val value = trimmed.toIntOrNull() ?: return "$label должно быть целым числом"
        if (value !in range) return "$label должно быть в диапазоне ${range.first}–${range.last}"
        return null
    }
}
