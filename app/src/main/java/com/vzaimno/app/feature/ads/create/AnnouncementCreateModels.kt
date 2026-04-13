package com.vzaimno.app.feature.ads.create

import androidx.annotation.StringRes
import com.vzaimno.app.R
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint

enum class AnnouncementMainGroup(
    val rawValue: String,
    @StringRes val titleRes: Int,
) {
    Delivery(
        rawValue = "delivery",
        titleRes = R.string.ads_create_main_group_delivery,
    ),
    Help(
        rawValue = "help",
        titleRes = R.string.ads_create_main_group_help,
    ),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementMainGroup? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

enum class AnnouncementBudgetMode(
    @StringRes val titleRes: Int,
) {
    Fixed(R.string.ads_create_budget_mode_fixed),
    Range(R.string.ads_create_budget_mode_range),
}

enum class AnnouncementContactMethod(
    val rawValue: String,
    @StringRes val titleRes: Int,
) {
    CallsAndMessages(
        rawValue = "calls_and_messages",
        titleRes = R.string.ads_create_contact_method_calls_and_messages,
    ),
    MessagesOnly(
        rawValue = "messages_only",
        titleRes = R.string.ads_create_contact_method_messages_only,
    ),
    CallsOnly(
        rawValue = "calls_only",
        titleRes = R.string.ads_create_contact_method_calls_only,
    ),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementContactMethod =
            entries.firstOrNull { it.rawValue == rawValue } ?: CallsAndMessages
    }
}

enum class AnnouncementAudience(
    val rawValue: String,
    @StringRes val titleRes: Int,
) {
    Individuals(
        rawValue = "individuals",
        titleRes = R.string.ads_create_audience_individuals,
    ),
    Business(
        rawValue = "business",
        titleRes = R.string.ads_create_audience_business,
    ),
    Both(
        rawValue = "both",
        titleRes = R.string.ads_create_audience_both,
    ),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementAudience =
            entries.firstOrNull { it.rawValue == rawValue } ?: Both
    }
}

enum class AnnouncementCreateItemType(
    val rawValue: String,
    @StringRes val titleRes: Int,
) {
    Groceries("groceries", R.string.ads_create_item_groceries),
    Documents("documents", R.string.ads_create_item_documents),
    Electronics("electronics", R.string.ads_create_item_electronics),
    FragileItem("fragile_item", R.string.ads_create_item_fragile),
    Bags("bags", R.string.ads_create_item_bags),
    BulkyItem("bulky_item", R.string.ads_create_item_bulky),
    Other("other", R.string.ads_create_item_other),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementCreateItemType? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

enum class AnnouncementCreatePurchaseType(
    val rawValue: String,
    @StringRes val titleRes: Int,
) {
    Groceries("groceries", R.string.ads_create_purchase_groceries),
    Medicine("medicine", R.string.ads_create_purchase_medicine),
    Clothing("clothing", R.string.ads_create_purchase_clothing),
    Electronics("electronics", R.string.ads_create_purchase_electronics),
    HomeGoods("home_goods", R.string.ads_create_purchase_home_goods),
    Other("other", R.string.ads_create_purchase_other),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementCreatePurchaseType? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

enum class AnnouncementCreateHelpType(
    val rawValue: String,
    @StringRes val titleRes: Int,
) {
    Consultation("consultation", R.string.ads_create_help_consultation),
    SetupDevice("setup_device", R.string.ads_create_help_setup_device),
    InstallOrConnect("install_or_connect", R.string.ads_create_help_install_or_connect),
    MinorRepair("minor_repair", R.string.ads_create_help_minor_repair),
    Diagnose("diagnose", R.string.ads_create_help_diagnose),
    Other("other", R.string.ads_create_help_other),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementCreateHelpType? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

enum class AnnouncementAttributeToggle(
    @StringRes val titleRes: Int,
) {
    RequiresVehicle(R.string.ads_attribute_vehicle),
    NeedsTrunk(R.string.ads_attribute_trunk),
    RequiresCarefulHandling(R.string.ads_attribute_careful),
    NeedsLoader(R.string.ads_attribute_loader),
    RequiresLiftToFloor(R.string.ads_attribute_lift),
    HasElevator(R.string.ads_attribute_elevator),
    WaitOnSite(R.string.ads_attribute_waiting),
    Contactless(R.string.ads_attribute_contactless),
    RequiresReceipt(R.string.ads_attribute_receipt),
    RequiresConfirmationCode(R.string.ads_attribute_code),
    CallBeforeArrival(R.string.ads_attribute_call_before),
    PhotoReportRequired(R.string.ads_attribute_photo_report),
}

data class AnnouncementAddressInput(
    val address: String = "",
    val placeId: String? = null,
    val point: GeoPoint? = null,
)

data class AnnouncementBudgetInput(
    val mode: AnnouncementBudgetMode = AnnouncementBudgetMode.Range,
    val amount: String = "",
    val min: String = "",
    val max: String = "",
)

data class AnnouncementContactsInput(
    val name: String = "",
    val phone: String = "",
    val method: AnnouncementContactMethod = AnnouncementContactMethod.CallsAndMessages,
    val audience: AnnouncementAudience = AnnouncementAudience.Both,
)

data class AnnouncementAttributesInput(
    val requiresVehicle: Boolean = false,
    val needsTrunk: Boolean = false,
    val requiresCarefulHandling: Boolean = false,
    val needsLoader: Boolean = false,
    val requiresLiftToFloor: Boolean = false,
    val hasElevator: Boolean = false,
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
) {
    fun valueFor(toggle: AnnouncementAttributeToggle): Boolean = when (toggle) {
        AnnouncementAttributeToggle.RequiresVehicle -> requiresVehicle
        AnnouncementAttributeToggle.NeedsTrunk -> needsTrunk
        AnnouncementAttributeToggle.RequiresCarefulHandling -> requiresCarefulHandling
        AnnouncementAttributeToggle.NeedsLoader -> needsLoader
        AnnouncementAttributeToggle.RequiresLiftToFloor -> requiresLiftToFloor
        AnnouncementAttributeToggle.HasElevator -> hasElevator
        AnnouncementAttributeToggle.WaitOnSite -> waitOnSite
        AnnouncementAttributeToggle.Contactless -> contactless
        AnnouncementAttributeToggle.RequiresReceipt -> requiresReceipt
        AnnouncementAttributeToggle.RequiresConfirmationCode -> requiresConfirmationCode
        AnnouncementAttributeToggle.CallBeforeArrival -> callBeforeArrival
        AnnouncementAttributeToggle.PhotoReportRequired -> photoReportRequired
    }

    fun withToggle(toggle: AnnouncementAttributeToggle, value: Boolean): AnnouncementAttributesInput = when (toggle) {
        AnnouncementAttributeToggle.RequiresVehicle -> copy(requiresVehicle = value)
        AnnouncementAttributeToggle.NeedsTrunk -> copy(needsTrunk = value)
        AnnouncementAttributeToggle.RequiresCarefulHandling -> copy(requiresCarefulHandling = value)
        AnnouncementAttributeToggle.NeedsLoader -> copy(needsLoader = value)
        AnnouncementAttributeToggle.RequiresLiftToFloor -> copy(requiresLiftToFloor = value)
        AnnouncementAttributeToggle.HasElevator -> copy(hasElevator = value)
        AnnouncementAttributeToggle.WaitOnSite -> copy(waitOnSite = value)
        AnnouncementAttributeToggle.Contactless -> copy(contactless = value)
        AnnouncementAttributeToggle.RequiresReceipt -> copy(requiresReceipt = value)
        AnnouncementAttributeToggle.RequiresConfirmationCode -> copy(requiresConfirmationCode = value)
        AnnouncementAttributeToggle.CallBeforeArrival -> copy(callBeforeArrival = value)
        AnnouncementAttributeToggle.PhotoReportRequired -> copy(photoReportRequired = value)
    }
}

data class AnnouncementSelectedMedia(
    val id: String,
    val uriString: String,
    val fileName: String?,
    val mimeType: String?,
)

data class AnnouncementCreateFormDraft(
    val mainGroup: AnnouncementMainGroup = AnnouncementMainGroup.Delivery,
    val actionType: AnnouncementStructuredData.ActionType? = null,
    val title: String = "",
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
) {
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

    val showsTaskBriefField: Boolean
        get() = actionType in setOf(
            AnnouncementStructuredData.ActionType.ProHelp,
            AnnouncementStructuredData.ActionType.Other,
        )

    val requiresTaskBrief: Boolean
        get() = showsTaskBriefField

    val availableActionTypes: List<AnnouncementStructuredData.ActionType>
        get() = actionTypesFor(mainGroup)

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
                AnnouncementStructuredData.SourceKind.Other,
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

    val availableItemTypes: List<AnnouncementCreateItemType>
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Carry -> listOf(
                AnnouncementCreateItemType.Bags,
                AnnouncementCreateItemType.FragileItem,
                AnnouncementCreateItemType.BulkyItem,
                AnnouncementCreateItemType.Electronics,
                AnnouncementCreateItemType.Other,
            )

            AnnouncementStructuredData.ActionType.Pickup -> AnnouncementCreateItemType.entries
            AnnouncementStructuredData.ActionType.Other -> AnnouncementCreateItemType.entries
            else -> AnnouncementCreateItemType.entries
        }

    val availableAttributeToggles: List<AnnouncementAttributeToggle>
        get() = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup -> buildList {
                add(AnnouncementAttributeToggle.RequiresVehicle)
                add(AnnouncementAttributeToggle.NeedsTrunk)
                add(AnnouncementAttributeToggle.RequiresCarefulHandling)
                add(AnnouncementAttributeToggle.RequiresLiftToFloor)
                if (attributes.requiresLiftToFloor) {
                    add(AnnouncementAttributeToggle.HasElevator)
                }
                add(AnnouncementAttributeToggle.NeedsLoader)
                add(AnnouncementAttributeToggle.WaitOnSite)
                add(AnnouncementAttributeToggle.CallBeforeArrival)
                add(AnnouncementAttributeToggle.RequiresConfirmationCode)
                add(AnnouncementAttributeToggle.Contactless)
                add(AnnouncementAttributeToggle.PhotoReportRequired)
            }

            AnnouncementStructuredData.ActionType.Buy -> listOf(
                AnnouncementAttributeToggle.RequiresVehicle,
                AnnouncementAttributeToggle.NeedsTrunk,
                AnnouncementAttributeToggle.WaitOnSite,
                AnnouncementAttributeToggle.CallBeforeArrival,
                AnnouncementAttributeToggle.Contactless,
                AnnouncementAttributeToggle.RequiresReceipt,
                AnnouncementAttributeToggle.PhotoReportRequired,
            )

            AnnouncementStructuredData.ActionType.Carry -> buildList {
                add(AnnouncementAttributeToggle.RequiresVehicle)
                add(AnnouncementAttributeToggle.NeedsTrunk)
                add(AnnouncementAttributeToggle.RequiresCarefulHandling)
                add(AnnouncementAttributeToggle.RequiresLiftToFloor)
                if (attributes.requiresLiftToFloor) {
                    add(AnnouncementAttributeToggle.HasElevator)
                }
                add(AnnouncementAttributeToggle.NeedsLoader)
                add(AnnouncementAttributeToggle.WaitOnSite)
                add(AnnouncementAttributeToggle.CallBeforeArrival)
            }

            AnnouncementStructuredData.ActionType.Ride -> listOf(
                AnnouncementAttributeToggle.RequiresVehicle,
                AnnouncementAttributeToggle.NeedsTrunk,
                AnnouncementAttributeToggle.WaitOnSite,
                AnnouncementAttributeToggle.CallBeforeArrival,
            )

            AnnouncementStructuredData.ActionType.ProHelp -> listOf(
                AnnouncementAttributeToggle.CallBeforeArrival,
                AnnouncementAttributeToggle.PhotoReportRequired,
            )

            AnnouncementStructuredData.ActionType.Other -> listOf(
                AnnouncementAttributeToggle.RequiresVehicle,
                AnnouncementAttributeToggle.NeedsTrunk,
                AnnouncementAttributeToggle.RequiresCarefulHandling,
                AnnouncementAttributeToggle.CallBeforeArrival,
                AnnouncementAttributeToggle.Contactless,
            )

            null -> emptyList()
        }
}

data class AnnouncementCreateFieldErrors(
    val actionType: String? = null,
    val title: String? = null,
    val budgetAmount: String? = null,
    val budgetMin: String? = null,
    val budgetMax: String? = null,
    val sourceAddress: String? = null,
    val destinationAddress: String? = null,
    val taskBrief: String? = null,
    val contactPhone: String? = null,
    val estimatedTaskMinutes: String? = null,
    val waitingMinutes: String? = null,
    val floor: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(
            actionType,
            title,
            budgetAmount,
            budgetMin,
            budgetMax,
            sourceAddress,
            destinationAddress,
            taskBrief,
            contactPhone,
            estimatedTaskMinutes,
            waitingMinutes,
            floor,
        ).any { it != null }

    fun firstMessage(): String? = listOfNotNull(
        actionType,
        title,
        budgetAmount,
        budgetMin,
        budgetMax,
        sourceAddress,
        destinationAddress,
        taskBrief,
        contactPhone,
        estimatedTaskMinutes,
        waitingMinutes,
        floor,
    ).firstOrNull()
}

data class AnnouncementCreateUiState(
    val draft: AnnouncementCreateFormDraft = AnnouncementCreateFormDraft(),
    val fieldErrors: AnnouncementCreateFieldErrors = AnnouncementCreateFieldErrors(),
    val isPrefillLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val inlineMessage: String? = null,
    val prefillSourceAnnouncementId: String? = null,
    val prefillSourceTitle: String? = null,
    val isSummaryExpanded: Boolean = false,
) {
    val isBusy: Boolean
        get() = isPrefillLoading || isSubmitting

    val isCreateAgain: Boolean
        get() = !prefillSourceAnnouncementId.isNullOrBlank()
}

// ── Recommended Price Calculation (ported from iOS CreateAdDraft) ─────

data class RecommendedPriceRange(
    val min: Int,
    val max: Int,
) {
    val text: String
        get() = if (min == max) "$min ₽" else "$min–$max ₽"
}

fun AnnouncementCreateFormDraft.recommendedPriceRange(): RecommendedPriceRange {
    var base = baseRecommendedPrice()

    // Urgency modifier
    base += when (urgency) {
        AnnouncementStructuredData.Urgency.Now -> 250
        AnnouncementStructuredData.Urgency.Today -> 90
        AnnouncementStructuredData.Urgency.Scheduled -> 0
        AnnouncementStructuredData.Urgency.Flexible -> -40
    }

    // Condition modifiers
    if (attributes.requiresVehicle && actionType != AnnouncementStructuredData.ActionType.Ride) base += 150
    if (attributes.needsTrunk) base += 120
    if (attributes.requiresCarefulHandling) base += 90
    if (attributes.requiresLiftToFloor) {
        base += 130
        if (!attributes.hasElevator) base += 140
    }
    if (attributes.needsLoader) base += 240
    if (attributes.waitOnSite) {
        val wm = attributes.waitingMinutes.toIntOrNull() ?: 0
        base += (wm * 8).coerceIn(40, 280)
    }
    if (attributes.callBeforeArrival) base += 20
    if (attributes.requiresConfirmationCode) base += 40
    if (attributes.contactless) base += 20
    if (attributes.requiresReceipt) base += 35
    if (attributes.photoReportRequired) base += 40

    // Weight category modifier
    base += when (attributes.weightCategory) {
        AnnouncementStructuredData.WeightCategory.UpTo1Kg -> 0
        AnnouncementStructuredData.WeightCategory.UpTo3Kg -> 30
        AnnouncementStructuredData.WeightCategory.UpTo7Kg -> 70
        AnnouncementStructuredData.WeightCategory.UpTo15Kg -> 120
        AnnouncementStructuredData.WeightCategory.Over15Kg -> 200
        null -> 0
    }

    // Size category modifier
    base += when (attributes.sizeCategory) {
        AnnouncementStructuredData.SizeCategory.Pocket -> 0
        AnnouncementStructuredData.SizeCategory.Hand -> 20
        AnnouncementStructuredData.SizeCategory.Backpack -> 50
        AnnouncementStructuredData.SizeCategory.Trunk -> 110
        AnnouncementStructuredData.SizeCategory.Bulky -> 180
        null -> 0
    }

    // Floor modifier
    if (attributes.requiresLiftToFloor) {
        val floor = attributes.floor.toIntOrNull() ?: 0
        if (floor > 3) base += (floor - 3) * 25
    }

    val roundedBase = roundToNearest50(base.coerceAtLeast(250))
    val min = roundToNearest50((roundedBase * 0.85).toInt().coerceAtLeast(250))
    val max = roundToNearest50((roundedBase * 1.15).toInt().coerceAtLeast(min))
    return RecommendedPriceRange(min = min, max = max)
}

private fun AnnouncementCreateFormDraft.baseRecommendedPrice(): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup -> {
        if (sourceKind == AnnouncementStructuredData.SourceKind.Person) 520 else 480
    }
    AnnouncementStructuredData.ActionType.Buy -> {
        620 + when (purchaseType) {
            AnnouncementCreatePurchaseType.Medicine -> 80
            AnnouncementCreatePurchaseType.Electronics -> 120
            AnnouncementCreatePurchaseType.HomeGoods -> 70
            AnnouncementCreatePurchaseType.Clothing -> 40
            else -> 0
        }
    }
    AnnouncementStructuredData.ActionType.Carry -> 720
    AnnouncementStructuredData.ActionType.Ride -> 650
    AnnouncementStructuredData.ActionType.ProHelp -> when (helpType) {
        AnnouncementCreateHelpType.Consultation -> 850
        AnnouncementCreateHelpType.SetupDevice -> 980
        AnnouncementCreateHelpType.InstallOrConnect -> 1050
        AnnouncementCreateHelpType.MinorRepair -> 1100
        AnnouncementCreateHelpType.Diagnose -> 950
        AnnouncementCreateHelpType.Other, null -> 900
    }
    AnnouncementStructuredData.ActionType.Other -> 560
    null -> 500
}

private fun roundToNearest50(value: Int): Int =
    ((value + 25) / 50) * 50

// ── Auto-Generated Title (ported from iOS) ───────────────────────────

fun AnnouncementCreateFormDraft.generatedTitle(): String = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup ->
        "Забрать ${itemType?.accusativeTitle ?: "заказ"}"
    AnnouncementStructuredData.ActionType.Buy ->
        "Купить ${purchaseType?.accusativeTitle ?: "товары"}"
    AnnouncementStructuredData.ActionType.Carry ->
        "Перенести ${itemType?.accusativeTitle ?: "вещь"}"
    AnnouncementStructuredData.ActionType.Ride ->
        if (attributes.needsTrunk) "Подвезти с багажом" else "Подвезти без багажа"
    AnnouncementStructuredData.ActionType.ProHelp ->
        "Помощь от профи: ${helpType?.shortTitle ?: "быстрая помощь"}"
    AnnouncementStructuredData.ActionType.Other ->
        "Нестандартная задача"
    null -> "Новое объявление"
}

val AnnouncementCreateItemType.accusativeTitle: String
    get() = when (this) {
        AnnouncementCreateItemType.Groceries -> "продукты"
        AnnouncementCreateItemType.Documents -> "документы"
        AnnouncementCreateItemType.Electronics -> "технику"
        AnnouncementCreateItemType.FragileItem -> "хрупкое"
        AnnouncementCreateItemType.Bags -> "сумки"
        AnnouncementCreateItemType.BulkyItem -> "крупную вещь"
        AnnouncementCreateItemType.Other -> "вещь"
    }

val AnnouncementCreatePurchaseType.accusativeTitle: String
    get() = when (this) {
        AnnouncementCreatePurchaseType.Groceries -> "продукты"
        AnnouncementCreatePurchaseType.Medicine -> "лекарства"
        AnnouncementCreatePurchaseType.Clothing -> "одежду"
        AnnouncementCreatePurchaseType.Electronics -> "технику"
        AnnouncementCreatePurchaseType.HomeGoods -> "товары для дома"
        AnnouncementCreatePurchaseType.Other -> "товары"
    }

val AnnouncementCreateHelpType.shortTitle: String
    get() = when (this) {
        AnnouncementCreateHelpType.Consultation -> "консультация"
        AnnouncementCreateHelpType.SetupDevice -> "настройка устройства"
        AnnouncementCreateHelpType.InstallOrConnect -> "подключение"
        AnnouncementCreateHelpType.MinorRepair -> "мелкий ремонт"
        AnnouncementCreateHelpType.Diagnose -> "диагностика"
        AnnouncementCreateHelpType.Other -> "быстрая помощь"
    }

// ── Summary Helpers ──────────────────────────────────────────────────

fun AnnouncementCreateFormDraft.actionSummaryText(): String = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup -> "Забрать"
    AnnouncementStructuredData.ActionType.Buy -> "Купить"
    AnnouncementStructuredData.ActionType.Carry -> "Перенести"
    AnnouncementStructuredData.ActionType.Ride -> "Подвезти"
    AnnouncementStructuredData.ActionType.ProHelp -> "Помощь"
    AnnouncementStructuredData.ActionType.Other -> "Другое"
    null -> "Черновик"
}

fun AnnouncementCreateFormDraft.objectSummaryText(): String = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup,
    AnnouncementStructuredData.ActionType.Carry,
    -> itemType?.accusativeTitle?.replaceFirstChar { it.uppercase() } ?: "Не выбрано"
    AnnouncementStructuredData.ActionType.Buy ->
        purchaseType?.accusativeTitle?.replaceFirstChar { it.uppercase() } ?: "Не выбрано"
    AnnouncementStructuredData.ActionType.ProHelp ->
        helpType?.shortTitle?.replaceFirstChar { it.uppercase() } ?: "Не выбрано"
    AnnouncementStructuredData.ActionType.Ride ->
        if (attributes.needsTrunk) "С багажом" else "Без багажа"
    AnnouncementStructuredData.ActionType.Other -> "Задача"
    null -> "Без деталей"
}

fun AnnouncementCreateFormDraft.routeSummaryText(): String {
    val src = source.address.trim().takeIf { it.isNotBlank() }
    val dst = destination.address.trim().takeIf { it.isNotBlank() && showsDestinationSection }
    return when {
        src != null && dst != null -> "${src.take(20)} → ${dst.take(20)}"
        src != null -> src.take(35)
        else -> "Маршрут не задан"
    }
}

fun AnnouncementCreateFormDraft.whenSummaryText(): String = when (urgency) {
    AnnouncementStructuredData.Urgency.Now -> "Сейчас"
    AnnouncementStructuredData.Urgency.Today -> "Сегодня"
    AnnouncementStructuredData.Urgency.Scheduled -> "Ко времени"
    AnnouncementStructuredData.Urgency.Flexible -> "Не срочно"
}

fun AnnouncementCreateFormDraft.priceSummaryText(): String {
    val rec = recommendedPriceRange()
    return when (budget.mode) {
        AnnouncementBudgetMode.Fixed -> {
            val amt = budget.amount.trim()
            if (amt.isNotBlank()) "$amt ₽" else "Рекомендуем ${rec.text}"
        }
        AnnouncementBudgetMode.Range -> {
            val min = budget.min.trim()
            val max = budget.max.trim()
            when {
                min.isNotBlank() && max.isNotBlank() -> "$min–$max ₽"
                min.isNotBlank() -> "от $min ₽"
                max.isNotBlank() -> "до $max ₽"
                else -> "Рекомендуем ${rec.text}"
            }
        }
    }
}

// ── Readiness Issues (ported from iOS submitReadinessIssues) ──────────

fun AnnouncementCreateFormDraft.readinessIssues(): List<String> = buildList {
    if (actionType == null) {
        add("Выберите, что нужно сделать")
        return@buildList
    }

    // Object selection
    when (actionType) {
        AnnouncementStructuredData.ActionType.Pickup,
        AnnouncementStructuredData.ActionType.Carry,
        -> if (itemType == null) add("Уточните, что именно нужно")
        AnnouncementStructuredData.ActionType.Buy ->
            if (purchaseType == null) add("Выберите, что нужно купить")
        AnnouncementStructuredData.ActionType.ProHelp ->
            if (helpType == null) add("Выберите тип помощи")
        else -> Unit
    }

    // Task brief
    if (showsTaskBriefField && taskBrief.isBlank()) {
        add("Опишите задачу кратко")
    }

    // Source
    if (sourceKind == null) add("Выберите, откуда начинается задача")
    if (source.address.isBlank()) add("Укажите адрес отправления")

    // Destination
    if (showsDestinationSection) {
        if (requiresDestinationAddress && destinationKind == null) {
            add("Выберите, куда нужно доставить")
        }
        if (requiresDestinationAddress && destination.address.isBlank()) {
            add("Укажите адрес назначения")
        }
        if (source.address.isNotBlank() && destination.address.isNotBlank() &&
            source.address.equals(destination.address, ignoreCase = true)
        ) {
            add("Адрес забора и адрес доставки не должны совпадать")
        }
    }

    // Budget
    when (budget.mode) {
        AnnouncementBudgetMode.Fixed -> {
            if (budget.amount.isBlank()) add("Укажите бюджет")
        }
        AnnouncementBudgetMode.Range -> {
            if (budget.min.isBlank() && budget.max.isBlank()) add("Укажите бюджет")
        }
    }

    // Waiting
    if (attributes.waitOnSite && attributes.waitingMinutes.isBlank()) {
        add("Укажите, сколько можно подождать на месте")
    }

    // Floor
    if (attributes.requiresLiftToFloor && attributes.floor.isBlank()) {
        add("Укажите этаж")
    }
}.distinct()

fun AnnouncementCreateFormDraft.isReadyForSubmit(): Boolean =
    readinessIssues().isEmpty()

// ── Active Condition Tags ────────────────────────────────────────────

fun AnnouncementCreateFormDraft.activeConditionTags(): List<String> = buildList {
    if (attributes.requiresVehicle) add("Машина")
    if (attributes.needsTrunk) add("Багажник")
    if (attributes.requiresCarefulHandling) add("Аккуратно")
    if (attributes.needsLoader) add("Грузчик")
    if (attributes.requiresLiftToFloor) add("На этаж")
    if (attributes.hasElevator) add("Лифт")
    if (attributes.waitOnSite) add("Ожидание")
    if (attributes.contactless) add("Бесконтактно")
    if (attributes.requiresReceipt) add("Чек")
    if (attributes.requiresConfirmationCode) add("Код")
    if (attributes.callBeforeArrival) add("Звонок")
    if (attributes.photoReportRequired) add("Фотоотчёт")
    attributes.weightCategory?.let { add(it.title) }
    attributes.sizeCategory?.let { add(it.title) }
}

fun actionTypesFor(mainGroup: AnnouncementMainGroup): List<AnnouncementStructuredData.ActionType> = when (mainGroup) {
    AnnouncementMainGroup.Delivery -> listOf(
        AnnouncementStructuredData.ActionType.Pickup,
        AnnouncementStructuredData.ActionType.Buy,
    )

    AnnouncementMainGroup.Help -> listOf(
        AnnouncementStructuredData.ActionType.Carry,
        AnnouncementStructuredData.ActionType.Ride,
        AnnouncementStructuredData.ActionType.ProHelp,
        AnnouncementStructuredData.ActionType.Other,
    )
}

fun mainGroupFor(actionType: AnnouncementStructuredData.ActionType?): AnnouncementMainGroup = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup,
    AnnouncementStructuredData.ActionType.Buy,
    -> AnnouncementMainGroup.Delivery

    AnnouncementStructuredData.ActionType.Carry,
    AnnouncementStructuredData.ActionType.Ride,
    AnnouncementStructuredData.ActionType.ProHelp,
    AnnouncementStructuredData.ActionType.Other,
    -> AnnouncementMainGroup.Help

    null -> AnnouncementMainGroup.Delivery
}
