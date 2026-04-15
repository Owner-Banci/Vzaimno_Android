package com.vzaimno.app.feature.ads.create

import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// ── Main Group ──────────────────────────────────────────────────────────

enum class AnnouncementMainGroup(val rawValue: String, val title: String) {
    Delivery("delivery", "Доставка и поручения"),
    Help("help", "Помощь"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementMainGroup? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

// ── Contact Method ──────────────────────────────────────────────────────

enum class AnnouncementContactMethod(val rawValue: String, val title: String) {
    CallsAndMessages("calls_and_messages", "Звонки и сообщения"),
    MessagesOnly("messages_only", "Только сообщения"),
    CallsOnly("calls_only", "Только звонки"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementContactMethod =
            entries.firstOrNull { it.rawValue == rawValue } ?: CallsAndMessages
    }
}

// ── Audience ────────────────────────────────────────────────────────────

enum class AnnouncementAudience(val rawValue: String, val title: String) {
    Individuals("individuals", "Частные лица"),
    Business("business", "Бизнес"),
    Both("both", "Частные лица и бизнес"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementAudience =
            entries.firstOrNull { it.rawValue == rawValue } ?: Both
    }
}

// ── Item Type ───────────────────────────────────────────────────────────

enum class AnnouncementCreateItemType(
    val rawValue: String,
    val title: String,
    val accusativeTitle: String,
) {
    Groceries("groceries", "Продукты", "продукты"),
    Documents("documents", "Документы", "документы"),
    Electronics("electronics", "Техника", "электронику"),
    FragileItem("fragile_item", "Хрупкая вещь", "хрупкое"),
    Bags("bags", "Пакеты", "сумки"),
    BulkyItem("bulky_item", "Крупная вещь", "крупное"),
    Other("other", "Другое", "предмет"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementCreateItemType? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

// ── Purchase Type ───────────────────────────────────────────────────────

enum class AnnouncementCreatePurchaseType(
    val rawValue: String,
    val title: String,
    val accusativeTitle: String,
) {
    Groceries("groceries", "Продукты", "продукты"),
    Medicine("medicine", "Лекарства", "лекарства"),
    Clothing("clothing", "Одежда", "одежду"),
    Electronics("electronics", "Техника", "электронику"),
    HomeGoods("home_goods", "Товары для дома", "товары для дома"),
    Other("other", "Другое", "товар"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementCreatePurchaseType? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

// ── Help Type ───────────────────────────────────────────────────────────

enum class AnnouncementCreateHelpType(
    val rawValue: String,
    val title: String,
    val shortTitle: String,
) {
    Consultation("consultation", "Консультация", "Консультация"),
    SetupDevice("setup_device", "Настроить устройство", "Настройка"),
    InstallOrConnect("install_or_connect", "Подключить / установить", "Установка"),
    MinorRepair("minor_repair", "Мелкий ремонт", "Ремонт"),
    Diagnose("diagnose", "Проверить / диагностировать", "Диагностика"),
    Other("other", "Другое", "Помощь"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementCreateHelpType? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

// ── Condition Option ────────────────────────────────────────────────────

enum class AnnouncementConditionOption(
    val title: String,
    val subtitle: String,
) {
    RequiresVehicle("Нужна машина", "Если без авто не справиться"),
    NeedsTrunk("Нужен багажник", "Важно место для груза"),
    RequiresCarefulHandling("Нужна аккуратная перевозка", "Хрупкое, ценное или деликатное"),
    RequiresLiftToFloor("Нужно поднять / спустить", "Важно учесть этаж"),
    HasElevator("Есть лифт", "Лифт в подъезде работает"),
    NeedsLoader("Нужна вторая пара рук", "Если одному не справиться"),
    WaitOnSite("Нужно подождать на месте", "Придётся подождать в точке"),
    CallBeforeArrival("Нужно созвониться заранее", "Договориться по телефону"),
    RequiresConfirmationCode("Нужен код / подтверждение", "Код выдачи или подтверждение"),
    Contactless("Бесконтактно", "Без личного контакта"),
    RequiresReceipt("Нужен чек", "Фискальный чек за покупку"),
    PhotoReportRequired("Нужен фотоотчёт", "Фото результата или процесса"),
}

// ── Moderation Mark ─────────────────────────────────────────────────────

enum class ModerationSeverity { Warning, Error }

data class DraftModerationMark(
    val severity: ModerationSeverity,
    val code: String,
    val details: String,
)

// ── Address Input ───────────────────────────────────────────────────────

data class AnnouncementAddressInput(
    val address: String = "",
    val placeId: String? = null,
    val point: GeoPoint? = null,
)

// ── Budget Input ────────────────────────────────────────────────────────

data class AnnouncementBudgetInput(
    val min: String = "",
    val max: String = "",
)

// ── Contacts Input ──────────────────────────────────────────────────────

data class AnnouncementContactsInput(
    val name: String = "",
    val phone: String = "",
    val method: AnnouncementContactMethod = AnnouncementContactMethod.CallsAndMessages,
    val audience: AnnouncementAudience = AnnouncementAudience.Both,
)

// ── Attributes Input ────────────────────────────────────────────────────

data class AnnouncementAttributesInput(
    val requiresVehicle: Boolean = false,
    val needsTrunk: Boolean = false,
    val requiresCarefulHandling: Boolean = false,
    val needsLoader: Boolean = false,
    val requiresLiftToFloor: Boolean = false,
    val hasElevator: Boolean = true,
    val waitOnSite: Boolean = false,
    val contactless: Boolean = false,
    val requiresReceipt: Boolean = false,
    val requiresConfirmationCode: Boolean = false,
    val callBeforeArrival: Boolean = false,
    val photoReportRequired: Boolean = false,
    val weightCategory: AnnouncementStructuredData.WeightCategory? = null,
    val sizeCategory: AnnouncementStructuredData.SizeCategory? = null,
    val estimatedTaskMinutes: String = "",
    val waitingMinutes: String = "",
    val floor: String = "",
    val cargoLength: String = "",
    val cargoWidth: String = "",
    val cargoHeight: String = "",
) {
    fun valueFor(option: AnnouncementConditionOption): Boolean = when (option) {
        AnnouncementConditionOption.RequiresVehicle -> requiresVehicle
        AnnouncementConditionOption.NeedsTrunk -> needsTrunk
        AnnouncementConditionOption.RequiresCarefulHandling -> requiresCarefulHandling
        AnnouncementConditionOption.NeedsLoader -> needsLoader
        AnnouncementConditionOption.RequiresLiftToFloor -> requiresLiftToFloor
        AnnouncementConditionOption.HasElevator -> hasElevator
        AnnouncementConditionOption.WaitOnSite -> waitOnSite
        AnnouncementConditionOption.Contactless -> contactless
        AnnouncementConditionOption.RequiresReceipt -> requiresReceipt
        AnnouncementConditionOption.RequiresConfirmationCode -> requiresConfirmationCode
        AnnouncementConditionOption.CallBeforeArrival -> callBeforeArrival
        AnnouncementConditionOption.PhotoReportRequired -> photoReportRequired
    }

    fun withCondition(option: AnnouncementConditionOption, value: Boolean): AnnouncementAttributesInput =
        when (option) {
            AnnouncementConditionOption.RequiresVehicle -> copy(requiresVehicle = value)
            AnnouncementConditionOption.NeedsTrunk -> copy(needsTrunk = value)
            AnnouncementConditionOption.RequiresCarefulHandling -> copy(requiresCarefulHandling = value)
            AnnouncementConditionOption.NeedsLoader -> copy(needsLoader = value)
            AnnouncementConditionOption.RequiresLiftToFloor -> copy(requiresLiftToFloor = value)
            AnnouncementConditionOption.HasElevator -> copy(hasElevator = value)
            AnnouncementConditionOption.WaitOnSite -> copy(waitOnSite = value)
            AnnouncementConditionOption.Contactless -> copy(contactless = value)
            AnnouncementConditionOption.RequiresReceipt -> copy(requiresReceipt = value)
            AnnouncementConditionOption.RequiresConfirmationCode -> copy(requiresConfirmationCode = value)
            AnnouncementConditionOption.CallBeforeArrival -> copy(callBeforeArrival = value)
            AnnouncementConditionOption.PhotoReportRequired -> copy(photoReportRequired = value)
        }

    val hasExactDimensions: Boolean
        get() = cargoLength.isNotBlank() || cargoWidth.isNotBlank() || cargoHeight.isNotBlank()
}

// ── Media ───────────────────────────────────────────────────────────────

data class AnnouncementSelectedMedia(
    val id: String,
    val uriString: String,
    val fileName: String?,
    val mimeType: String?,
)

// ════════════════════════════════════════════════════════════════════════
//  MAIN FORM DRAFT — mirrors iOS CreateAdDraft as immutable data class
// ════════════════════════════════════════════════════════════════════════

data class AnnouncementCreateFormDraft(
    val moderationMarks: Map<String, DraftModerationMark> = emptyMap(),
    val actionType: AnnouncementStructuredData.ActionType? = null,
    val itemType: AnnouncementCreateItemType? = null,
    val purchaseType: AnnouncementCreatePurchaseType? = null,
    val helpType: AnnouncementCreateHelpType? = null,
    val sourceKind: AnnouncementStructuredData.SourceKind? = null,
    val destinationKind: AnnouncementStructuredData.DestinationKind? = null,
    val urgency: AnnouncementStructuredData.Urgency = AnnouncementStructuredData.Urgency.Today,
    val source: AnnouncementAddressInput = AnnouncementAddressInput(),
    val destination: AnnouncementAddressInput = AnnouncementAddressInput(),
    val budget: AnnouncementBudgetInput = AnnouncementBudgetInput(),
    val attributes: AnnouncementAttributesInput = AnnouncementAttributesInput(),
    val taskBrief: String = "",
    val notes: String = "",
    val contacts: AnnouncementContactsInput = AnnouncementContactsInput(),
    val media: List<AnnouncementSelectedMedia> = emptyList(),
    val startDate: String = "",
    val hasEndTime: Boolean = false,
    val endDate: String = "",
    val userEditedNotes: Boolean = false,
    val aiHints: List<String> = emptyList(),
    val mediaLocalIdentifiers: List<String> = emptyList(),
) {

    // ── Resolved category (mirrors iOS exactly) ─────────────────────────

    val resolvedCategory: AnnouncementStructuredData.ResolvedCategory
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> {
                if (sourceKind == AnnouncementStructuredData.SourceKind.Person) {
                    AnnouncementStructuredData.ResolvedCategory.Handoff
                } else {
                    AnnouncementStructuredData.ResolvedCategory.PickupPoint
                }
            }
            AnnouncementStructuredData.ActionType.Buy -> AnnouncementStructuredData.ResolvedCategory.Buy
            AnnouncementStructuredData.ActionType.Carry -> AnnouncementStructuredData.ResolvedCategory.Carry
            AnnouncementStructuredData.ActionType.Ride -> AnnouncementStructuredData.ResolvedCategory.Ride
            AnnouncementStructuredData.ActionType.ProHelp -> AnnouncementStructuredData.ResolvedCategory.ProHelp
            AnnouncementStructuredData.ActionType.Other -> AnnouncementStructuredData.ResolvedCategory.Other
            null -> {
                when {
                    helpType != null -> AnnouncementStructuredData.ResolvedCategory.ProHelp
                    purchaseType != null -> AnnouncementStructuredData.ResolvedCategory.Buy
                    else -> AnnouncementStructuredData.ResolvedCategory.Other
                }
            }
        }

    val mainGroup: AnnouncementMainGroup
        get() = when (resolvedCategory) {
            AnnouncementStructuredData.ResolvedCategory.PickupPoint,
            AnnouncementStructuredData.ResolvedCategory.Handoff,
            AnnouncementStructuredData.ResolvedCategory.Buy,
            -> AnnouncementMainGroup.Delivery

            AnnouncementStructuredData.ResolvedCategory.Carry,
            AnnouncementStructuredData.ResolvedCategory.Ride,
            AnnouncementStructuredData.ResolvedCategory.ProHelp,
            AnnouncementStructuredData.ResolvedCategory.Other,
            -> AnnouncementMainGroup.Help
        }

    val category: String
        get() = mainGroup.rawValue

    // ── Shows / requires ────────────────────────────────────────────────

    val showsStructuredSections: Boolean get() = actionType != null

    val showsDestinationSection: Boolean
        get() = actionType in setOf(
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Buy,
            AnnouncementStructuredData.ActionType.Carry,
            AnnouncementStructuredData.ActionType.Ride,
        )

    val requiresDestinationAddress: Boolean
        get() = actionType in setOf(
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Buy,
            AnnouncementStructuredData.ActionType.Ride,
        )

    val showsCargoSection: Boolean
        get() = actionType in setOf(
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Buy,
            AnnouncementStructuredData.ActionType.Carry,
            AnnouncementStructuredData.ActionType.Other,
        )

    val showsTaskBriefField: Boolean
        get() = actionType in setOf(
            AnnouncementStructuredData.ActionType.ProHelp,
            AnnouncementStructuredData.ActionType.Other,
        )

    val requiresTaskBrief: Boolean get() = showsTaskBriefField

    val hasExactDimensions: Boolean get() = attributes.hasExactDimensions

    val sourceAddressModerationKey: String
        get() = if (mainGroup == AnnouncementMainGroup.Delivery) "pickup_address" else "address"

    val destinationAddressModerationKey: String
        get() = if (mainGroup == AnnouncementMainGroup.Delivery) "dropoff_address" else "destination_address"

    // ── Available lists (per scenario) ──────────────────────────────────

    val availableGenericItemTypes: List<AnnouncementCreateItemType>
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> AnnouncementCreateItemType.entries.toList()
            AnnouncementStructuredData.ActionType.Carry -> listOf(
                AnnouncementCreateItemType.Bags,
                AnnouncementCreateItemType.FragileItem,
                AnnouncementCreateItemType.BulkyItem,
                AnnouncementCreateItemType.Electronics,
                AnnouncementCreateItemType.Other,
            )
            else -> AnnouncementCreateItemType.entries.toList()
        }

    val availableSourceKinds: List<AnnouncementStructuredData.SourceKind>
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> listOf(
                AnnouncementStructuredData.SourceKind.Person,
                AnnouncementStructuredData.SourceKind.PickupPoint,
                AnnouncementStructuredData.SourceKind.Venue,
                AnnouncementStructuredData.SourceKind.Address,
                AnnouncementStructuredData.SourceKind.Office,
                AnnouncementStructuredData.SourceKind.Other,
            )
            AnnouncementStructuredData.ActionType.Buy -> listOf(
                AnnouncementStructuredData.SourceKind.Venue,
                AnnouncementStructuredData.SourceKind.Address,
                AnnouncementStructuredData.SourceKind.Other,
            )
            AnnouncementStructuredData.ActionType.Carry,
            AnnouncementStructuredData.ActionType.Ride,
            -> listOf(
                AnnouncementStructuredData.SourceKind.Address,
                AnnouncementStructuredData.SourceKind.Office,
                AnnouncementStructuredData.SourceKind.Other,
            )
            AnnouncementStructuredData.ActionType.ProHelp -> listOf(
                AnnouncementStructuredData.SourceKind.Address,
                AnnouncementStructuredData.SourceKind.Office,
                AnnouncementStructuredData.SourceKind.Venue,
            )
            AnnouncementStructuredData.ActionType.Other -> listOf(
                AnnouncementStructuredData.SourceKind.Address,
                AnnouncementStructuredData.SourceKind.Person,
                AnnouncementStructuredData.SourceKind.Other,
            )
            null -> emptyList()
        }

    val availableDestinationKinds: List<AnnouncementStructuredData.DestinationKind>
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> listOf(
                AnnouncementStructuredData.DestinationKind.Address,
                AnnouncementStructuredData.DestinationKind.Person,
                AnnouncementStructuredData.DestinationKind.Office,
                AnnouncementStructuredData.DestinationKind.Entrance,
                AnnouncementStructuredData.DestinationKind.Metro,
                AnnouncementStructuredData.DestinationKind.Other,
            )
            AnnouncementStructuredData.ActionType.Buy -> listOf(
                AnnouncementStructuredData.DestinationKind.Address,
                AnnouncementStructuredData.DestinationKind.Office,
                AnnouncementStructuredData.DestinationKind.Entrance,
                AnnouncementStructuredData.DestinationKind.Metro,
                AnnouncementStructuredData.DestinationKind.Other,
            )
            AnnouncementStructuredData.ActionType.Carry -> listOf(
                AnnouncementStructuredData.DestinationKind.Address,
                AnnouncementStructuredData.DestinationKind.Office,
                AnnouncementStructuredData.DestinationKind.Entrance,
                AnnouncementStructuredData.DestinationKind.Other,
            )
            AnnouncementStructuredData.ActionType.Ride -> listOf(
                AnnouncementStructuredData.DestinationKind.Address,
                AnnouncementStructuredData.DestinationKind.Metro,
                AnnouncementStructuredData.DestinationKind.Other,
            )
            AnnouncementStructuredData.ActionType.ProHelp,
            AnnouncementStructuredData.ActionType.Other,
            null,
            -> emptyList()
        }

    val availableConditionOptions: List<AnnouncementConditionOption>
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> buildList {
                add(AnnouncementConditionOption.RequiresVehicle)
                add(AnnouncementConditionOption.NeedsTrunk)
                add(AnnouncementConditionOption.RequiresCarefulHandling)
                add(AnnouncementConditionOption.RequiresLiftToFloor)
                if (attributes.requiresLiftToFloor) add(AnnouncementConditionOption.HasElevator)
                add(AnnouncementConditionOption.NeedsLoader)
                add(AnnouncementConditionOption.WaitOnSite)
                add(AnnouncementConditionOption.CallBeforeArrival)
                add(AnnouncementConditionOption.RequiresConfirmationCode)
                add(AnnouncementConditionOption.Contactless)
                add(AnnouncementConditionOption.PhotoReportRequired)
            }
            AnnouncementStructuredData.ActionType.Buy -> listOf(
                AnnouncementConditionOption.RequiresVehicle,
                AnnouncementConditionOption.NeedsTrunk,
                AnnouncementConditionOption.WaitOnSite,
                AnnouncementConditionOption.CallBeforeArrival,
                AnnouncementConditionOption.Contactless,
                AnnouncementConditionOption.RequiresReceipt,
                AnnouncementConditionOption.PhotoReportRequired,
            )
            AnnouncementStructuredData.ActionType.Carry -> buildList {
                add(AnnouncementConditionOption.RequiresVehicle)
                add(AnnouncementConditionOption.NeedsTrunk)
                add(AnnouncementConditionOption.RequiresCarefulHandling)
                add(AnnouncementConditionOption.RequiresLiftToFloor)
                if (attributes.requiresLiftToFloor) add(AnnouncementConditionOption.HasElevator)
                add(AnnouncementConditionOption.NeedsLoader)
                add(AnnouncementConditionOption.WaitOnSite)
                add(AnnouncementConditionOption.CallBeforeArrival)
            }
            AnnouncementStructuredData.ActionType.Ride -> listOf(
                AnnouncementConditionOption.NeedsTrunk,
                AnnouncementConditionOption.WaitOnSite,
                AnnouncementConditionOption.CallBeforeArrival,
            )
            AnnouncementStructuredData.ActionType.ProHelp -> listOf(
                AnnouncementConditionOption.CallBeforeArrival,
                AnnouncementConditionOption.PhotoReportRequired,
            )
            AnnouncementStructuredData.ActionType.Other -> listOf(
                AnnouncementConditionOption.RequiresVehicle,
                AnnouncementConditionOption.NeedsTrunk,
                AnnouncementConditionOption.RequiresCarefulHandling,
                AnnouncementConditionOption.CallBeforeArrival,
                AnnouncementConditionOption.Contactless,
            )
            null -> emptyList()
        }

    // ── Section titles & placeholders (per scenario) ────────────────────

    val objectSectionTitle: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> "Что именно"
            AnnouncementStructuredData.ActionType.Buy -> "Что купить"
            AnnouncementStructuredData.ActionType.Carry -> "Что именно"
            AnnouncementStructuredData.ActionType.Ride -> "Параметры поездки"
            AnnouncementStructuredData.ActionType.ProHelp -> "Тип помощи"
            AnnouncementStructuredData.ActionType.Other -> "Что нужно сделать"
            null -> "Что именно"
        }

    val objectSectionSubtitle: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> "Выберите, что нужно забрать и отвезти."
            AnnouncementStructuredData.ActionType.Buy -> "Отдельный набор для сценария покупки."
            AnnouncementStructuredData.ActionType.Carry -> "Сфокусируйтесь на том, что нужно перенести."
            AnnouncementStructuredData.ActionType.Ride -> "Минимум параметров: пассажир и багаж."
            AnnouncementStructuredData.ActionType.ProHelp -> "Короткая быстрая задача, где нужен человек с опытом."
            AnnouncementStructuredData.ActionType.Other -> "Коротко опишите задачу одной строкой."
            null -> "Сначала выберите главное действие."
        }

    val taskBriefLabel: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.ProHelp -> "Что именно нужно?"
            AnnouncementStructuredData.ActionType.Other -> "Что нужно сделать?"
            else -> "Уточнение"
        }

    val taskBriefPlaceholder: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.ProHelp -> "Например: настроить телевизор"
            AnnouncementStructuredData.ActionType.Other -> "Например: быстро проверить устройство"
            else -> "Коротко опишите задачу"
        }

    val sourceSectionTitle: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> "Откуда забрать"
            AnnouncementStructuredData.ActionType.Buy -> "Где купить"
            AnnouncementStructuredData.ActionType.Carry -> "Откуда начать"
            AnnouncementStructuredData.ActionType.Ride -> "Откуда забрать пассажира"
            AnnouncementStructuredData.ActionType.ProHelp -> "Где нужна помощь"
            AnnouncementStructuredData.ActionType.Other -> "Где это нужно"
            null -> "Откуда"
        }

    val sourceFieldLabel: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Buy -> "Адрес или место покупки"
            AnnouncementStructuredData.ActionType.ProHelp -> "Где нужна помощь"
            AnnouncementStructuredData.ActionType.Ride -> "Точка подачи"
            else -> "Адрес отправления"
        }

    val destinationSectionTitle: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> "Куда привезти"
            AnnouncementStructuredData.ActionType.Buy -> "Куда привезти покупку"
            AnnouncementStructuredData.ActionType.Carry -> "Куда перенести"
            AnnouncementStructuredData.ActionType.Ride -> "Куда подвезти"
            else -> "Куда"
        }

    val destinationFieldLabel: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Ride -> "Точка назначения"
            AnnouncementStructuredData.ActionType.Carry -> "Куда перенести (опционально)"
            else -> if (requiresDestinationAddress) "Адрес назначения" else "Адрес назначения (опционально)"
        }

    val sourceAddressPlaceholder: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> when (sourceKind) {
                AnnouncementStructuredData.SourceKind.PickupPoint -> "Например: Ozon, Пятницкая 12"
                AnnouncementStructuredData.SourceKind.Venue -> "Например: ресторан, аптека или салон"
                else -> "Введите адрес"
            }
            AnnouncementStructuredData.ActionType.Buy -> when (sourceKind) {
                AnnouncementStructuredData.SourceKind.Venue -> "Например: супермаркет или аптека"
                AnnouncementStructuredData.SourceKind.Other -> "Например: рядом с метро Павелецкая"
                else -> "Введите адрес"
            }
            AnnouncementStructuredData.ActionType.Ride -> "Например: Павелецкая площадь 1"
            AnnouncementStructuredData.ActionType.ProHelp -> "Например: Лесная 10"
            else -> "Введите адрес"
        }

    val destinationAddressPlaceholder: String
        get() = when (destinationKind) {
            AnnouncementStructuredData.DestinationKind.Metro -> "Например: Павелецкая"
            AnnouncementStructuredData.DestinationKind.Entrance -> "Например: Лесная 10"
            else -> "Введите адрес"
        }

    val notesPlaceholder: String
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.ProHelp ->
                "Коротко уточните детали, если мастеру важно знать контекст заранее."
            AnnouncementStructuredData.ActionType.Buy ->
                "Например: если товара нет, позвонить и согласовать замену."
            else -> "Коротко уточните детали, если это поможет исполнителю."
        }

    // ── Derived display values (delegated to assembler) ─────────────────

    val generatedTitle: String get() = CreateAdDescriptionAssembler.generatedTitle(this)
    val resolvedTitle: String get() = generatedTitle

    val assembledDescription: String get() = CreateAdDescriptionAssembler.assembledDescription(this)
    val resolvedDescription: String get() = if (userEditedNotes && notes.isNotBlank()) notes else assembledDescription

    val objectSummary: String get() = CreateAdDescriptionAssembler.objectSummary(this)
    val routeSummary: String get() = CreateAdDescriptionAssembler.routeSummary(this)
    val timeSummary: String get() = CreateAdDescriptionAssembler.timeSummary(this)
    val budgetSummary: String get() = CreateAdDescriptionAssembler.budgetSummary(this)
    val generatedTags: List<String> get() = CreateAdDescriptionAssembler.generatedTags(this)
    val generatedHints: List<String> get() = CreateAdDescriptionAssembler.generatedHints(this)
    val selectedConditionTitles: List<String> get() = CreateAdDescriptionAssembler.selectedConditionTitles(this)

    val recommendedPriceRange: RecommendedPriceRange get() = CreateAdPriceCalculator.calculate(this)
    val submitReadinessIssues: List<String> get() = CreateAdValidation.readinessIssues(this)
    val isReadyForSubmit: Boolean get() = submitReadinessIssues.isEmpty()
}

// ════════════════════════════════════════════════════════════════════════
//  UI STATE
// ════════════════════════════════════════════════════════════════════════

data class AnnouncementCreateUiState(
    val draft: AnnouncementCreateFormDraft = AnnouncementCreateFormDraft(),
    val isPrefillLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val inlineMessage: String? = null,
    val prefillSourceAnnouncementId: String? = null,
    val prefillSourceTitle: String? = null,
    val isSummaryExpanded: Boolean = false,
    val showsExactDimensions: Boolean = false,
) {
    val isBusy: Boolean get() = isPrefillLoading || isSubmitting
    val isCreateAgain: Boolean get() = !prefillSourceAnnouncementId.isNullOrBlank()
}

private val createAdDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

private val createAdTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

fun formatStoredDateTime(rawValue: String): String? =
    runCatching {
        Instant.parse(rawValue).atZone(ZoneId.systemDefault()).format(createAdDateTimeFormatter)
    }.getOrNull()

fun formatStoredTime(rawValue: String): String? =
    runCatching {
        Instant.parse(rawValue).atZone(ZoneId.systemDefault()).format(createAdTimeFormatter)
    }.getOrNull()
