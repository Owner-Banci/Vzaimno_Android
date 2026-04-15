package com.vzaimno.app.feature.ads.create

import com.vzaimno.app.core.model.AnnouncementStructuredData

object CreateAdDescriptionAssembler {

    fun generatedTitle(draft: AnnouncementCreateFormDraft): String = when (draft.actionType) {
        AnnouncementStructuredData.ActionType.Pickup -> {
            val obj = draft.itemType?.accusativeTitle ?: "предмет"
            "Забрать $obj"
        }
        AnnouncementStructuredData.ActionType.Buy -> {
            val obj = draft.purchaseType?.accusativeTitle ?: "товар"
            "Купить $obj"
        }
        AnnouncementStructuredData.ActionType.Carry -> {
            val obj = draft.itemType?.accusativeTitle ?: "предмет"
            "Перенести $obj"
        }
        AnnouncementStructuredData.ActionType.Ride -> "Подвезти пассажира"
        AnnouncementStructuredData.ActionType.ProHelp -> "Помощь от профи"
        AnnouncementStructuredData.ActionType.Other -> "Нестандартная задача"
        null -> "Новое объявление"
    }

    fun objectSummary(draft: AnnouncementCreateFormDraft): String = when (draft.actionType) {
        AnnouncementStructuredData.ActionType.Pickup,
        AnnouncementStructuredData.ActionType.Carry,
        -> draft.itemType?.title ?: "Без деталей"

        AnnouncementStructuredData.ActionType.Buy -> draft.purchaseType?.title ?: "Без деталей"

        AnnouncementStructuredData.ActionType.Ride -> {
            if (draft.attributes.needsTrunk) "1 пассажир, нужен багажник"
            else "1 пассажир, без багажа"
        }

        AnnouncementStructuredData.ActionType.ProHelp -> buildString {
            val ht = draft.helpType?.title
            if (ht != null) append(ht)
            if (draft.taskBrief.isNotBlank()) {
                if (isNotEmpty()) append(": ")
                append(draft.taskBrief.take(40))
            }
            if (isEmpty()) append("Без деталей")
        }

        AnnouncementStructuredData.ActionType.Other -> {
            draft.taskBrief.ifBlank { "Без деталей" }.take(40)
        }

        null -> "Без деталей"
    }

    fun routeSummary(draft: AnnouncementCreateFormDraft): String {
        if (draft.actionType == null) return "Сценарий ещё не выбран"

        val sourceText = shortAddress(draft.source.address)
            ?: draft.sourceKindSummary()
            ?: "Точка старта не указана"

        return when (draft.actionType) {
            AnnouncementStructuredData.ActionType.ProHelp,
            AnnouncementStructuredData.ActionType.Other,
            -> sourceText

            AnnouncementStructuredData.ActionType.Carry -> {
                val destinationText = shortAddress(draft.destination.address)
                    ?: draft.destinationKindSummary()
                if (destinationText != null) {
                    "$sourceText -> $destinationText"
                } else {
                    sourceText
                }
            }

            else -> {
                val destText = shortAddress(draft.destination.address)
                    ?: draft.destinationKindSummary()
                    ?: "Точка назначения не указана"
                "$sourceText -> $destText"
            }
        }
    }

    fun timeSummary(draft: AnnouncementCreateFormDraft): String {
        val base = when (draft.urgency) {
            AnnouncementStructuredData.Urgency.Now -> "Сейчас"
            AnnouncementStructuredData.Urgency.Today -> "Сегодня"
            AnnouncementStructuredData.Urgency.Scheduled -> {
                formatStoredDateTime(draft.startDate)
                    ?: if (draft.startDate.isNotBlank()) draft.startDate else "Ко времени"
            }
            AnnouncementStructuredData.Urgency.Flexible -> "Не срочно"
        }
        return if (draft.hasEndTime && draft.endDate.isNotBlank()) {
            "$base, до ${formatStoredTime(draft.endDate) ?: draft.endDate}"
        } else {
            base
        }
    }

    fun budgetSummary(draft: AnnouncementCreateFormDraft): String {
        val minVal = draft.budget.min.toIntOrNull()
        val maxVal = draft.budget.max.toIntOrNull()
        return when {
            minVal != null && maxVal != null && minVal == maxVal -> "$minVal ₽"
            minVal != null && maxVal != null -> "$minVal–$maxVal ₽"
            minVal != null -> "от $minVal ₽"
            maxVal != null -> "до $maxVal ₽"
            else -> {
                val rec = CreateAdPriceCalculator.calculate(draft)
                "Рекомендуем ${rec.text}"
            }
        }
    }

    fun assembledDescription(draft: AnnouncementCreateFormDraft): String {
        val lines = mutableListOf<String>()

        leadLine(draft)?.let { lines.add(it) }
        routeLine(draft)?.let { lines.add(it) }
        destinationLine(draft)?.let { lines.add(it) }
        timingLines(draft).forEach { lines.add(it) }
        effortLines(draft).forEach { lines.add(it) }
        conditionsLine(draft)?.let { lines.add(it) }
        dimensionsLine(draft)?.let { lines.add(it) }

        return lines.distinct().joinToString("\n")
    }

    fun generatedTags(draft: AnnouncementCreateFormDraft): List<String> {
        val tags = mutableListOf<String>()

        tags += draft.mainGroup.rawValue
        tags += draft.resolvedCategory.rawValue
        tags += draft.urgency.rawValue
        draft.actionType?.rawValue?.let(tags::add)

        when (draft.actionType) {
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Carry,
            -> draft.itemType?.rawValue?.let(tags::add)

            AnnouncementStructuredData.ActionType.Buy ->
                draft.purchaseType?.rawValue?.let(tags::add)

            AnnouncementStructuredData.ActionType.Ride -> Unit

            AnnouncementStructuredData.ActionType.ProHelp ->
                draft.helpType?.rawValue?.let(tags::add)

            else -> {}
        }

        draft.sourceKind?.rawValue?.let(tags::add)
        draft.destinationKind?.rawValue?.let(tags::add)
        draft.attributes.weightCategory?.rawValue?.let(tags::add)
        draft.attributes.sizeCategory?.rawValue?.let(tags::add)

        if (draft.attributes.requiresVehicle && draft.actionType != AnnouncementStructuredData.ActionType.Ride) {
            tags += "requires_vehicle"
        }
        if (draft.attributes.needsTrunk) tags += "needs_trunk"
        if (draft.attributes.requiresCarefulHandling) tags += "careful"
        if (draft.attributes.requiresLiftToFloor) tags += "lift"
        if (draft.attributes.needsLoader) tags += "loader"
        if (draft.attributes.waitOnSite) tags += "wait_on_site"
        if (draft.attributes.contactless) tags += "contactless"
        if (draft.attributes.requiresReceipt) tags += "receipt"
        if (draft.attributes.requiresConfirmationCode) tags += "confirmation_code"
        if (draft.attributes.photoReportRequired) tags += "photo_report"

        return tags.distinct()
    }

    fun generatedHints(draft: AnnouncementCreateFormDraft): List<String> =
        generatedTags(draft)

    fun selectedConditionTitles(draft: AnnouncementCreateFormDraft): List<String> = buildList {
        val attrs = draft.attributes
        if (attrs.requiresVehicle && draft.actionType != AnnouncementStructuredData.ActionType.Ride) {
            add("Нужна машина")
        }
        if (attrs.needsTrunk) add("Нужен багажник")
        if (attrs.requiresCarefulHandling) add("Нужна аккуратная перевозка")
        if (attrs.requiresLiftToFloor) add("Нужно поднять / спустить")
        if (attrs.requiresLiftToFloor && attrs.hasElevator) add("Есть лифт")
        if (attrs.needsLoader) add("Нужна вторая пара рук")
        if (attrs.waitOnSite) add("Нужно подождать на месте")
        if (attrs.callBeforeArrival) add("Нужно созвониться заранее")
        if (attrs.requiresConfirmationCode) add("Нужен код / подтверждение")
        if (attrs.contactless) add("Бесконтактно")
        if (attrs.requiresReceipt) add("Нужен чек")
        if (attrs.photoReportRequired) add("Нужен фотоотчёт")
        if (draft.contacts.method == AnnouncementContactMethod.MessagesOnly) add("Только сообщения")
    }

    // --- Private builders ---

    private fun leadLine(draft: AnnouncementCreateFormDraft): String? {
        val obj = objectSummary(draft).lowercase()
        return when (draft.actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> "Нужно забрать $obj."
            AnnouncementStructuredData.ActionType.Buy -> "Нужно купить $obj."
            AnnouncementStructuredData.ActionType.Carry -> "Нужно перенести $obj."
            AnnouncementStructuredData.ActionType.Ride -> {
                if (draft.attributes.needsTrunk) "Нужно подвезти пассажира с багажом."
                else "Нужно подвезти пассажира без багажа."
            }
            AnnouncementStructuredData.ActionType.ProHelp -> {
                when {
                    draft.taskBrief.isNotBlank() -> "Нужна помощь от профи: ${draft.taskBrief}."
                    draft.helpType != null -> "Нужна помощь от профи по сценарию \"${draft.helpType.title}\"."
                    else -> "Нужна помощь от профи."
                }
            }
            AnnouncementStructuredData.ActionType.Other -> {
                if (draft.taskBrief.isNotBlank()) "Нестандартная задача: ${draft.taskBrief}."
                else "Нестандартная задача."
            }
            null -> null
        }
    }

    private fun routeLine(draft: AnnouncementCreateFormDraft): String? {
        val addr = shortAddress(draft.source.address) ?: return null
        return when (draft.actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> when (draft.sourceKind) {
                AnnouncementStructuredData.SourceKind.Person -> "Забор: у человека $addr."
                AnnouncementStructuredData.SourceKind.PickupPoint -> "Забор: из ПВЗ $addr."
                AnnouncementStructuredData.SourceKind.Venue -> "Забор: из заведения $addr."
                AnnouncementStructuredData.SourceKind.Address -> "Забор: с $addr."
                AnnouncementStructuredData.SourceKind.Office -> "Забор: из офиса $addr."
                AnnouncementStructuredData.SourceKind.Other, null -> "Забор: с точки $addr."
            }
            AnnouncementStructuredData.ActionType.Buy -> when (draft.sourceKind) {
                AnnouncementStructuredData.SourceKind.Venue -> "Покупка: в $addr."
                AnnouncementStructuredData.SourceKind.Address -> "Покупка: в месте $addr."
                AnnouncementStructuredData.SourceKind.Other, null -> "Покупка: рядом с $addr."
                else -> "Покупка: в $addr."
            }
            AnnouncementStructuredData.ActionType.Carry -> when (draft.sourceKind) {
                AnnouncementStructuredData.SourceKind.Address -> "Старт: с $addr."
                AnnouncementStructuredData.SourceKind.Office -> "Старт: из офиса $addr."
                AnnouncementStructuredData.SourceKind.Other, null -> "Старт: из точки $addr."
                else -> "Старт: с $addr."
            }
            AnnouncementStructuredData.ActionType.Ride -> "Подача: $addr."
            AnnouncementStructuredData.ActionType.ProHelp,
            AnnouncementStructuredData.ActionType.Other,
            -> "Адрес: $addr."
            null -> null
        }
    }

    private fun destinationLine(draft: AnnouncementCreateFormDraft): String? {
        val addr = shortAddress(draft.destination.address) ?: return null
        return when (draft.actionType) {
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Buy,
            -> "Куда доставить: $addr."

            AnnouncementStructuredData.ActionType.Carry -> "Куда перенести: $addr."
            AnnouncementStructuredData.ActionType.Ride -> "Маршрут до: $addr."
            else -> null
        }
    }

    private fun timingLines(draft: AnnouncementCreateFormDraft): List<String> = buildList {
        add("Когда: ${timeSummary(draft)}.")

        val minutes = draft.attributes.estimatedTaskMinutes.toIntOrNull()
        if (minutes != null && minutes > 0) {
            add("Оценка по времени: около $minutes мин.")
        }

        if (draft.attributes.waitOnSite) {
            val waitMin = draft.attributes.waitingMinutes.toIntOrNull()
            if (waitMin != null && waitMin > 0) {
                add("Можно подождать на месте до $waitMin мин.")
            }
        }
    }

    private fun effortLines(draft: AnnouncementCreateFormDraft): List<String> = buildList {
        draft.attributes.weightCategory?.title?.let { add("Вес: $it.") }
        draft.attributes.sizeCategory?.title?.let { add("Размер: $it.") }

        if (draft.attributes.requiresLiftToFloor) {
            val floor = draft.attributes.floor.ifBlank { "?" }
            val elevator = if (draft.attributes.hasElevator) "есть лифт" else "без лифта"
            add("Подъём на $floor этаж, $elevator.")
        }

        if (draft.attributes.needsLoader) {
            add("Нужен грузчик.")
        }
    }

    private fun conditionsLine(draft: AnnouncementCreateFormDraft): String? {
        val titles = selectedConditionTitles(draft)
        return if (titles.isNotEmpty()) "Дополнительно: ${titles.joinToString(", ")}." else null
    }

    private fun dimensionsLine(draft: AnnouncementCreateFormDraft): String? {
        val parts = buildList {
            draft.attributes.cargoLength.toIntOrNull()?.takeIf { it > 0 }?.let { add("длина $it см") }
            draft.attributes.cargoWidth.toIntOrNull()?.takeIf { it > 0 }?.let { add("ширина $it см") }
            draft.attributes.cargoHeight.toIntOrNull()?.takeIf { it > 0 }?.let { add("высота $it см") }
        }
        if (parts.isEmpty()) return null
        return "Габариты: ${parts.joinToString(", ")}."
    }

    private fun shortAddress(value: String): String? {
        val normalized = value.trim()
        if (normalized.isEmpty()) return null
        val parts = normalized
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
        return when {
            parts.size >= 2 -> parts.takeLast(2).joinToString(", ")
            parts.isNotEmpty() -> parts.first()
            else -> null
        }
    }

    private fun AnnouncementCreateFormDraft.sourceKindSummary(): String? = when (actionType) {
        AnnouncementStructuredData.ActionType.Buy -> when (sourceKind) {
            AnnouncementStructuredData.SourceKind.Venue -> "в заведении"
            AnnouncementStructuredData.SourceKind.Address -> "в конкретном месте"
            AnnouncementStructuredData.SourceKind.Other -> "где угодно рядом"
            else -> sourceKind?.title?.lowercase()
        }

        AnnouncementStructuredData.ActionType.Carry -> when (sourceKind) {
            AnnouncementStructuredData.SourceKind.Address -> "с адреса"
            AnnouncementStructuredData.SourceKind.Office -> "из офиса"
            AnnouncementStructuredData.SourceKind.Other -> "из точки"
            else -> sourceKind?.title?.lowercase()
        }

        AnnouncementStructuredData.ActionType.Ride -> when (sourceKind) {
            AnnouncementStructuredData.SourceKind.Address -> "от адреса"
            AnnouncementStructuredData.SourceKind.Office -> "от офиса"
            AnnouncementStructuredData.SourceKind.Other -> "от точки"
            else -> sourceKind?.title?.lowercase()
        }

        else -> when (sourceKind) {
            AnnouncementStructuredData.SourceKind.Person -> "у человека"
            AnnouncementStructuredData.SourceKind.PickupPoint -> "из ПВЗ"
            AnnouncementStructuredData.SourceKind.Venue -> "из заведения"
            AnnouncementStructuredData.SourceKind.Address -> "с адреса"
            AnnouncementStructuredData.SourceKind.Office -> "из офиса"
            AnnouncementStructuredData.SourceKind.Other -> "с точки"
            null -> null
        }
    }

    private fun AnnouncementCreateFormDraft.destinationKindSummary(): String? = when (actionType) {
        AnnouncementStructuredData.ActionType.Carry -> when (destinationKind) {
            AnnouncementStructuredData.DestinationKind.Address -> "до адреса"
            AnnouncementStructuredData.DestinationKind.Office -> "в офис"
            AnnouncementStructuredData.DestinationKind.Entrance -> "до подъезда"
            AnnouncementStructuredData.DestinationKind.Other -> "до точки"
            else -> destinationKind?.title?.lowercase()
        }

        AnnouncementStructuredData.ActionType.Ride -> when (destinationKind) {
            AnnouncementStructuredData.DestinationKind.Metro -> "к метро"
            AnnouncementStructuredData.DestinationKind.Address -> "по адресу"
            AnnouncementStructuredData.DestinationKind.Other -> "до точки"
            else -> destinationKind?.title?.lowercase()
        }

        else -> when (destinationKind) {
            AnnouncementStructuredData.DestinationKind.Person -> "человеку"
            AnnouncementStructuredData.DestinationKind.Address -> "по адресу"
            AnnouncementStructuredData.DestinationKind.Office -> "в офис"
            AnnouncementStructuredData.DestinationKind.Entrance -> "до подъезда"
            AnnouncementStructuredData.DestinationKind.Metro -> "к метро"
            AnnouncementStructuredData.DestinationKind.Other -> "в точку"
            null -> null
        }
    }
}
