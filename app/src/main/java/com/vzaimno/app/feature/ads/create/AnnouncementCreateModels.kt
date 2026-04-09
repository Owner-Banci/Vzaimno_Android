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
) {
    val isBusy: Boolean
        get() = isPrefillLoading || isSubmitting

    val isCreateAgain: Boolean
        get() = !prefillSourceAnnouncementId.isNullOrBlank()
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
