package com.vzaimno.app.feature.ads.create

import android.content.Context
import com.vzaimno.app.R
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.CreateAnnouncementDraft
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.budgetMaxValue
import com.vzaimno.app.core.model.budgetMinValue
import com.vzaimno.app.core.model.budgetValue
import com.vzaimno.app.core.model.destinationPoint
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.sourcePoint
import com.vzaimno.app.core.model.structuredData
import com.vzaimno.app.core.model.taskIntValue
import com.vzaimno.app.core.model.taskStringValue
import java.time.Instant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class AnnouncementCreateSubmissionPlan(
    val requestStatus: String,
) {
    val lifecycleStatus: String
        get() = if (requestStatus == "active") "open" else requestStatus
}

fun AnnouncementCreateFormDraft.submissionPlan(): AnnouncementCreateSubmissionPlan =
    AnnouncementCreateSubmissionPlan(
        requestStatus = if (media.isEmpty()) "active" else "pending_review",
    )

fun AnnouncementCreateFormDraft.withMainGroup(mainGroup: AnnouncementMainGroup): AnnouncementCreateFormDraft {
    val compatibleAction = actionType.takeIf { actionTypesFor(mainGroup).contains(it) }
    return copy(
        mainGroup = mainGroup,
        actionType = compatibleAction,
    ).normalizedForCurrentAction()
}

fun AnnouncementCreateFormDraft.withActionType(
    actionType: AnnouncementStructuredData.ActionType,
): AnnouncementCreateFormDraft {
    val defaultSourceKind = defaultSourceKindFor(actionType)
    val defaultDestinationKind = defaultDestinationKindFor(actionType)
    val normalizedAttributes = attributes
        .copy(
            requiresVehicle = when (actionType) {
                AnnouncementStructuredData.ActionType.Ride -> true
                else -> attributes.requiresVehicle
            },
            weightCategory = when (actionType) {
                AnnouncementStructuredData.ActionType.Ride,
                AnnouncementStructuredData.ActionType.ProHelp,
                AnnouncementStructuredData.ActionType.Other,
                -> null

                else -> attributes.weightCategory
            },
            sizeCategory = when (actionType) {
                AnnouncementStructuredData.ActionType.Ride,
                AnnouncementStructuredData.ActionType.ProHelp,
                AnnouncementStructuredData.ActionType.Other,
                -> null

                else -> attributes.sizeCategory
            },
            requiresCarefulHandling = attributes.requiresCarefulHandling &&
                actionType !in setOf(
                    AnnouncementStructuredData.ActionType.Ride,
                    AnnouncementStructuredData.ActionType.ProHelp,
                ),
            needsLoader = attributes.needsLoader &&
                actionType !in setOf(
                    AnnouncementStructuredData.ActionType.Ride,
                    AnnouncementStructuredData.ActionType.ProHelp,
                ),
            requiresLiftToFloor = attributes.requiresLiftToFloor &&
                actionType !in setOf(
                    AnnouncementStructuredData.ActionType.Ride,
                    AnnouncementStructuredData.ActionType.ProHelp,
                ),
            hasElevator = if (attributes.requiresLiftToFloor) attributes.hasElevator else false,
            contactless = attributes.contactless &&
                actionType !in setOf(
                    AnnouncementStructuredData.ActionType.Carry,
                    AnnouncementStructuredData.ActionType.Ride,
                    AnnouncementStructuredData.ActionType.ProHelp,
                ),
            requiresReceipt = attributes.requiresReceipt &&
                actionType == AnnouncementStructuredData.ActionType.Buy,
            requiresConfirmationCode = attributes.requiresConfirmationCode &&
                actionType == AnnouncementStructuredData.ActionType.Pickup,
            photoReportRequired = attributes.photoReportRequired &&
                actionType != AnnouncementStructuredData.ActionType.Ride,
            floor = if (attributes.requiresLiftToFloor) attributes.floor else "",
            waitingMinutes = if (attributes.waitOnSite) attributes.waitingMinutes else "",
            estimatedTaskMinutes = attributes.estimatedTaskMinutes.ifBlank {
                defaultEstimatedMinutesFor(actionType)
            },
        )

    return copy(
        mainGroup = mainGroupFor(actionType),
        actionType = actionType,
        itemType = when (actionType) {
            AnnouncementStructuredData.ActionType.Pickup,
            AnnouncementStructuredData.ActionType.Carry,
            -> itemType

            else -> null
        },
        purchaseType = when (actionType) {
            AnnouncementStructuredData.ActionType.Buy -> purchaseType
            else -> null
        },
        helpType = when (actionType) {
            AnnouncementStructuredData.ActionType.ProHelp -> helpType
            else -> null
        },
        sourceKind = sourceKind
            ?.takeIf { availableSourceKindsFor(actionType).contains(it) }
            ?: defaultSourceKind,
        destinationKind = destinationKind
            ?.takeIf { availableDestinationKindsFor(actionType).contains(it) }
            ?: defaultDestinationKind,
        destination = if (actionType in setOf(
                AnnouncementStructuredData.ActionType.ProHelp,
                AnnouncementStructuredData.ActionType.Other,
            )
        ) {
            AnnouncementAddressInput()
        } else {
            destination
        },
        taskBrief = if (actionType in setOf(
                AnnouncementStructuredData.ActionType.ProHelp,
                AnnouncementStructuredData.ActionType.Other,
            )
        ) {
            taskBrief
        } else {
            ""
        },
        attributes = normalizedAttributes,
    ).normalizedForCurrentAction()
}

fun AnnouncementCreateFormDraft.normalizedForCurrentAction(): AnnouncementCreateFormDraft {
    val action = actionType ?: return this
    val availableSourceKinds = availableSourceKindsFor(action)
    val availableDestinationKinds = availableDestinationKindsFor(action)
    val availableToggles = availableAttributeTogglesFor(action, attributes.requiresLiftToFloor)

    var normalized = copy(
        sourceKind = sourceKind?.takeIf(availableSourceKinds::contains) ?: availableSourceKinds.firstOrNull(),
        destinationKind = destinationKind?.takeIf(availableDestinationKinds::contains),
        destination = if (showsDestinationSectionFor(action)) destination else AnnouncementAddressInput(),
        attributes = attributes
            .withToggle(AnnouncementAttributeToggle.RequiresVehicle, attributes.requiresVehicle)
            .copy(
                needsTrunk = if (availableToggles.contains(AnnouncementAttributeToggle.NeedsTrunk)) {
                    attributes.needsTrunk
                } else {
                    false
                },
                requiresCarefulHandling = if (availableToggles.contains(AnnouncementAttributeToggle.RequiresCarefulHandling)) {
                    attributes.requiresCarefulHandling
                } else {
                    false
                },
                needsLoader = if (availableToggles.contains(AnnouncementAttributeToggle.NeedsLoader)) {
                    attributes.needsLoader
                } else {
                    false
                },
                requiresLiftToFloor = if (availableToggles.contains(AnnouncementAttributeToggle.RequiresLiftToFloor)) {
                    attributes.requiresLiftToFloor
                } else {
                    false
                },
                hasElevator = if (availableToggles.contains(AnnouncementAttributeToggle.HasElevator)) {
                    attributes.hasElevator
                } else {
                    false
                },
                waitOnSite = if (availableToggles.contains(AnnouncementAttributeToggle.WaitOnSite)) {
                    attributes.waitOnSite
                } else {
                    false
                },
                contactless = if (availableToggles.contains(AnnouncementAttributeToggle.Contactless)) {
                    attributes.contactless
                } else {
                    false
                },
                requiresReceipt = if (availableToggles.contains(AnnouncementAttributeToggle.RequiresReceipt)) {
                    attributes.requiresReceipt
                } else {
                    false
                },
                requiresConfirmationCode = if (availableToggles.contains(AnnouncementAttributeToggle.RequiresConfirmationCode)) {
                    attributes.requiresConfirmationCode
                } else {
                    false
                },
                callBeforeArrival = if (availableToggles.contains(AnnouncementAttributeToggle.CallBeforeArrival)) {
                    attributes.callBeforeArrival
                } else {
                    false
                },
                photoReportRequired = if (availableToggles.contains(AnnouncementAttributeToggle.PhotoReportRequired)) {
                    attributes.photoReportRequired
                } else {
                    false
                },
                floor = if (attributes.requiresLiftToFloor) attributes.floor else "",
                waitingMinutes = if (attributes.waitOnSite) attributes.waitingMinutes else "",
            ),
    )

    if (action == AnnouncementStructuredData.ActionType.Ride) {
        normalized = normalized.copy(
            attributes = normalized.attributes.copy(requiresVehicle = true),
        )
    }

    return normalized
}

fun AnnouncementCreateFormDraft.validate(context: Context): AnnouncementCreateFieldErrors {
    val action = actionType
    val titleValue = title.trim()
    val sourceAddress = source.address.trim()
    val destinationAddress = destination.address.trim()
    val fixedBudget = parsePositiveInt(budget.amount)
    val minBudget = parsePositiveInt(budget.min)
    val maxBudget = parsePositiveInt(budget.max)
    val estimatedMinutes = parseOptionalBoundedInt(attributes.estimatedTaskMinutes)
    val waitingMinutes = parseOptionalBoundedInt(attributes.waitingMinutes)
    val floorValue = parseOptionalBoundedInt(attributes.floor, max = 100)
    val normalizedPhone = normalizePhone(contacts.phone)

    val budgetAmountError = when {
        budget.mode != AnnouncementBudgetMode.Fixed -> null
        budget.amount.trim().isEmpty() -> context.getString(R.string.ads_create_error_budget_amount_required)
        fixedBudget == null -> context.getString(R.string.ads_create_error_budget_number)
        else -> null
    }

    var budgetMinError: String? = null
    var budgetMaxError: String? = null
    if (budget.mode == AnnouncementBudgetMode.Range) {
        when {
            budget.min.trim().isEmpty() && budget.max.trim().isEmpty() -> {
                budgetMinError = context.getString(R.string.ads_create_error_budget_range_required)
                budgetMaxError = budgetMinError
            }

            budget.min.trim().isNotEmpty() && minBudget == null -> {
                budgetMinError = context.getString(R.string.ads_create_error_budget_number)
            }

            budget.max.trim().isNotEmpty() && maxBudget == null -> {
                budgetMaxError = context.getString(R.string.ads_create_error_budget_number)
            }

            minBudget != null && maxBudget != null && minBudget > maxBudget -> {
                budgetMaxError = context.getString(R.string.ads_create_error_budget_range_order)
            }
        }
    }

    return AnnouncementCreateFieldErrors(
        actionType = if (action == null) context.getString(R.string.ads_create_error_action_required) else null,
        title = if (titleValue.isBlank()) context.getString(R.string.ads_create_error_title_required) else null,
        budgetAmount = budgetAmountError,
        budgetMin = budgetMinError,
        budgetMax = budgetMaxError,
        sourceAddress = if (sourceAddress.isBlank()) {
            context.getString(R.string.ads_create_error_source_address_required)
        } else {
            null
        },
        destinationAddress = when {
            action != null && requiresDestinationAddressFor(action) && destinationAddress.isBlank() -> {
                context.getString(R.string.ads_create_error_destination_address_required)
            }

            action != null &&
                showsDestinationSectionFor(action) &&
                sourceAddress.isNotBlank() &&
                destinationAddress.isNotBlank() &&
                sourceAddress.equals(destinationAddress, ignoreCase = true) -> {
                context.getString(R.string.ads_create_error_addresses_must_differ)
            }

            else -> null
        },
        taskBrief = if (action != null && requiresTaskBriefFor(action) && taskBrief.trim().isBlank()) {
            context.getString(R.string.ads_create_error_task_brief_required)
        } else {
            null
        },
        contactPhone = when {
            contacts.phone.trim().isBlank() -> null
            normalizedPhone == null -> context.getString(R.string.ads_create_error_contact_phone_invalid)
            else -> null
        },
        estimatedTaskMinutes = when {
            attributes.estimatedTaskMinutes.trim().isBlank() -> null
            estimatedMinutes == null -> context.getString(R.string.ads_create_error_minutes_invalid)
            else -> null
        },
        waitingMinutes = when {
            !attributes.waitOnSite -> null
            attributes.waitingMinutes.trim().isBlank() -> context.getString(R.string.ads_create_error_waiting_required)
            waitingMinutes == null -> context.getString(R.string.ads_create_error_minutes_invalid)
            else -> null
        },
        floor = when {
            !attributes.requiresLiftToFloor -> null
            attributes.floor.trim().isBlank() -> context.getString(R.string.ads_create_error_floor_required)
            floorValue == null -> context.getString(R.string.ads_create_error_floor_invalid)
            else -> null
        },
    )
}

fun AnnouncementCreateFormDraft.toRepositoryDraft(
    requestStatus: String,
): CreateAnnouncementDraft {
    val normalizedTitle = title.trim()
    val sourceAddress = source.address.trim()
    val destinationAddress = destination.address.trim()
    val itemRaw = itemType?.rawValue
    val purchaseRaw = purchaseType?.rawValue
    val helpRaw = helpType?.rawValue
    val actionRaw = actionType?.rawValue
    val resolvedCategoryRaw = resolvedCategoryRaw()
    val normalizedPhone = normalizePhone(contacts.phone)
    val generatedDescription = generatedDescription()
    val budgetAmount = resolvedBudgetAmount()
    val budgetMin = resolvedBudgetMin()
    val budgetMax = resolvedBudgetMax()
    val quickOfferPrice = budgetAmount ?: budgetMin ?: budgetMax ?: 0
    val tags = generatedTags()
    val travelMode = when {
        attributes.requiresVehicle -> "driving"
        actionType == AnnouncementStructuredData.ActionType.Ride -> "driving"
        mainGroup == AnnouncementMainGroup.Delivery -> "driving"
        else -> "walking"
    }
    val taskPayload = buildJsonObject {
        put("schema_version", JsonPrimitive(2))
        put(
            "lifecycle",
            buildJsonObject {
                put("status", JsonPrimitive(if (requestStatus == "active") "open" else requestStatus))
                put("deleted_at", JsonNull)
            },
        )
        put(
            "builder",
            buildJsonObject {
                put("main_group", jsonString(mainGroup.rawValue))
                put("action_type", jsonString(actionRaw))
                put("resolved_category", jsonString(resolvedCategoryRaw))
                put("item_type", jsonString(itemRaw))
                put("purchase_type", jsonString(purchaseRaw))
                put("help_type", jsonString(helpRaw))
                put("source_kind", jsonString(sourceKind?.rawValue))
                put("destination_kind", jsonString(destinationKind?.rawValue))
                put("urgency", jsonString(urgency.rawValue))
                put("task_brief", jsonString(taskBrief.trim()))
                put("notes", jsonString(notes.trim()))
            },
        )
        put(
            "attributes",
            buildJsonObject {
                put("requires_vehicle", JsonPrimitive(attributes.requiresVehicle || actionType == AnnouncementStructuredData.ActionType.Ride))
                put("needs_trunk", JsonPrimitive(attributes.needsTrunk))
                put("requires_careful_handling", JsonPrimitive(attributes.requiresCarefulHandling))
                put("needs_loader", JsonPrimitive(attributes.needsLoader))
                put("requires_lift_to_floor", JsonPrimitive(attributes.requiresLiftToFloor))
                put("has_elevator", JsonPrimitive(attributes.hasElevator))
                put("wait_on_site", JsonPrimitive(attributes.waitOnSite))
                put("contactless", JsonPrimitive(attributes.contactless))
                put("requires_receipt", JsonPrimitive(attributes.requiresReceipt))
                put("requires_confirmation_code", JsonPrimitive(attributes.requiresConfirmationCode))
                put("call_before_arrival", JsonPrimitive(attributes.callBeforeArrival))
                put("photo_report_required", JsonPrimitive(attributes.photoReportRequired))
                put("weight_category", jsonString(attributes.weightCategory?.rawValue))
                put("size_category", jsonString(attributes.sizeCategory?.rawValue))
                put(
                    "cargo",
                    buildJsonObject {
                        put("length_cm", JsonNull)
                        put("width_cm", JsonNull)
                        put("height_cm", JsonNull)
                    },
                )
                put("estimated_task_minutes", jsonInt(attributes.estimatedTaskMinutes))
                put("waiting_minutes", if (attributes.waitOnSite) jsonInt(attributes.waitingMinutes) else JsonNull)
                put("floor", if (attributes.requiresLiftToFloor) jsonInt(attributes.floor) else JsonNull)
            },
        )
        put(
            "budget",
            buildJsonObject {
                put("currency", JsonPrimitive("RUB"))
                put("min", budgetMin?.let(::JsonPrimitive) ?: JsonNull)
                put("max", budgetMax?.let(::JsonPrimitive) ?: JsonNull)
                put("amount", budgetAmount?.let(::JsonPrimitive) ?: JsonNull)
            },
        )
        put(
            "route",
            buildJsonObject {
                put("travel_mode", JsonPrimitive(travelMode))
                put("start_at", JsonPrimitive(defaultStartAtFor(urgency).toString()))
                put("has_end_time", JsonPrimitive(false))
                put("end_at", JsonNull)
                put(
                    "source",
                    buildJsonObject {
                        put("address", jsonString(sourceAddress))
                        put("kind", jsonString(sourceKind?.rawValue))
                        put("point", source.point?.toJsonPoint() ?: JsonNull)
                    },
                )
                put(
                    "destination",
                    buildJsonObject {
                        put("address", jsonString(destinationAddress))
                        put("kind", jsonString(destinationKind?.rawValue))
                        put("point", destination.point?.toJsonPoint() ?: JsonNull)
                    },
                )
            },
        )
        put(
            "contacts",
            buildJsonObject {
                put("name", jsonString(contacts.name.trim()))
                put("phone", jsonString(normalizedPhone))
                put("method", JsonPrimitive(contacts.method.rawValue))
                put("audience", JsonPrimitive(contacts.audience.rawValue))
            },
        )
        put(
            "search",
            buildJsonObject {
                put("generated_title", JsonPrimitive(normalizedTitle))
                put("generated_description", jsonString(generatedDescription))
                put("generated_tags", JsonArray(tags.map(::JsonPrimitive)))
                put("hints", buildJsonArray {
                    selectedHintValues().forEach { add(JsonPrimitive(it)) }
                })
            },
        )
        put(
            "offer_policy",
            buildJsonObject {
                put("quick_offer_enabled", JsonPrimitive(true))
                put("quick_offer_price", JsonPrimitive(quickOfferPrice))
                put("counter_price_allowed", JsonPrimitive(true))
                put("reoffer_policy", JsonPrimitive("blocked_after_reject"))
            },
        )
        put(
            "execution",
            buildJsonObject {
                put("status", JsonPrimitive("open"))
                put("assignment_id", JsonNull)
                put("performer_user_id", JsonNull)
            },
        )
    }

    val data = buildJsonObject {
        put("main_group", JsonPrimitive(mainGroup.rawValue))
        put("action_type", jsonString(actionRaw))
        put("resolved_category", JsonPrimitive(resolvedCategoryRaw))
        put("item_type", jsonString(itemRaw))
        put("purchase_type", jsonString(purchaseRaw))
        put("help_type", jsonString(helpRaw))
        put("source_kind", jsonString(sourceKind?.rawValue))
        put("destination_kind", jsonString(destinationKind?.rawValue))
        put("urgency", JsonPrimitive(urgency.rawValue))
        put("source_address", jsonString(sourceAddress))
        put("destination_address", jsonString(destinationAddress))
        put("budget", budgetAmount?.let(::JsonPrimitive) ?: JsonNull)
        put("budget_min", budgetMin?.let(::JsonPrimitive) ?: JsonNull)
        put("budget_max", budgetMax?.let(::JsonPrimitive) ?: JsonNull)
        put("requires_vehicle", JsonPrimitive(attributes.requiresVehicle || actionType == AnnouncementStructuredData.ActionType.Ride))
        put("needs_trunk", JsonPrimitive(attributes.needsTrunk))
        put("requires_careful_handling", JsonPrimitive(attributes.requiresCarefulHandling))
        put("needs_loader", JsonPrimitive(attributes.needsLoader))
        put("need_loader", JsonPrimitive(attributes.needsLoader))
        put("requires_lift_to_floor", JsonPrimitive(attributes.requiresLiftToFloor))
        put("has_elevator", JsonPrimitive(attributes.hasElevator))
        put("wait_on_site", JsonPrimitive(attributes.waitOnSite))
        put("contactless", JsonPrimitive(attributes.contactless))
        put("requires_receipt", JsonPrimitive(attributes.requiresReceipt))
        put("requires_confirmation_code", JsonPrimitive(attributes.requiresConfirmationCode))
        put("call_before_arrival", JsonPrimitive(attributes.callBeforeArrival))
        put("photo_report_required", JsonPrimitive(attributes.photoReportRequired))
        put("weight_category", jsonString(attributes.weightCategory?.rawValue))
        put("size_category", jsonString(attributes.sizeCategory?.rawValue))
        put("estimated_task_minutes", jsonInt(attributes.estimatedTaskMinutes))
        put("waiting_minutes", if (attributes.waitOnSite) jsonInt(attributes.waitingMinutes) else JsonNull)
        put("floor", if (attributes.requiresLiftToFloor) jsonInt(attributes.floor) else JsonNull)
        put("task_brief", jsonString(taskBrief.trim()))
        put("notes", jsonString(notes.trim()))
        put("contact_name", jsonString(contacts.name.trim()))
        put("contact_phone", jsonString(normalizedPhone))
        put("contact_method", JsonPrimitive(contacts.method.rawValue))
        put("audience", JsonPrimitive(contacts.audience.rawValue))
        if (mainGroup == AnnouncementMainGroup.Delivery) {
            put("pickup_address", jsonString(sourceAddress))
            put("dropoff_address", jsonString(destinationAddress))
            put("pickup_point", source.point?.toJsonPoint() ?: JsonNull)
            put("dropoff_point", destination.point?.toJsonPoint() ?: JsonNull)
        } else {
            put("address", jsonString(sourceAddress))
            put("help_point", source.point?.toJsonPoint() ?: JsonNull)
            put("destination_point", destination.point?.toJsonPoint() ?: JsonNull)
        }
        put("task", taskPayload)
    }

    return CreateAnnouncementDraft(
        category = mainGroup.rawValue,
        title = normalizedTitle,
        status = requestStatus,
        data = data,
    )
}

fun AnnouncementCreateFormDraft.toOptimisticAnnouncement(
    localId: String,
    userId: String,
    requestStatus: String,
    createdAt: Instant = Instant.now(),
): Announcement {
    val requestDraft = toRepositoryDraft(requestStatus = requestStatus)
    val previewMedia = media.take(4).map { mediaItem ->
        buildJsonObject {
            put("url", JsonPrimitive(mediaItem.uriString))
        }
    }

    val data = buildJsonObject {
        requestDraft.data.forEach { (key, value) -> put(key, value) }
        put("client_submission_id", JsonPrimitive(localId))
        if (previewMedia.isNotEmpty()) {
            put("media", JsonArray(previewMedia))
        }
    }

    return Announcement(
        id = localId,
        userId = userId,
        category = requestDraft.category,
        title = requestDraft.title,
        status = requestDraft.status,
        data = data,
        createdAt = createdAt,
        media = previewMedia,
    )
}

fun Announcement.toCreateFormDraft(): AnnouncementCreateFormDraft {
    val structured = structuredData
    val budgetAmount = budgetValue ?: budgetMaxValue ?: budgetMinValue
    val budgetMode = when {
        budgetAmount != null && (budgetMinValue == null && budgetMaxValue == null || budgetMinValue == budgetMaxValue) -> {
            AnnouncementBudgetMode.Fixed
        }

        else -> AnnouncementBudgetMode.Range
    }

    return AnnouncementCreateFormDraft(
        mainGroup = AnnouncementMainGroup.fromRaw(category) ?: mainGroupFor(structured.actionType),
        actionType = structured.actionType,
        title = title,
        itemType = AnnouncementCreateItemType.fromRaw(structured.itemType),
        purchaseType = AnnouncementCreatePurchaseType.fromRaw(structured.purchaseType),
        helpType = AnnouncementCreateHelpType.fromRaw(structured.helpType),
        sourceKind = structured.sourceKind,
        destinationKind = structured.destinationKind,
        urgency = structured.urgency ?: AnnouncementStructuredData.Urgency.Today,
        source = AnnouncementAddressInput(
            address = primarySourceAddress.orEmpty(),
            point = sourcePoint,
        ),
        destination = AnnouncementAddressInput(
            address = primaryDestinationAddress.orEmpty(),
            point = destinationPoint,
        ),
        budget = AnnouncementBudgetInput(
            mode = budgetMode,
            amount = if (budgetMode == AnnouncementBudgetMode.Fixed) budgetAmount?.toString().orEmpty() else "",
            min = if (budgetMode == AnnouncementBudgetMode.Range) budgetMinValue?.toString().orEmpty() else "",
            max = if (budgetMode == AnnouncementBudgetMode.Range) {
                (budgetMaxValue ?: budgetValue)?.toString().orEmpty()
            } else {
                ""
            },
        ),
        attributes = AnnouncementAttributesInput(
            requiresVehicle = structured.requiresVehicle,
            needsTrunk = structured.needsTrunk,
            requiresCarefulHandling = structured.requiresCarefulHandling,
            needsLoader = structured.needsLoader,
            requiresLiftToFloor = structured.requiresLiftToFloor,
            hasElevator = structured.hasElevator,
            waitOnSite = structured.waitOnSite,
            contactless = structured.contactless,
            requiresReceipt = structured.requiresReceipt,
            requiresConfirmationCode = structured.requiresConfirmationCode,
            callBeforeArrival = structured.callBeforeArrival,
            photoReportRequired = structured.photoReportRequired,
            weightCategory = structured.weightCategory,
            sizeCategory = structured.sizeCategory,
            estimatedTaskMinutes = structured.estimatedTaskMinutes?.toString().orEmpty(),
            waitingMinutes = structured.waitingMinutes?.toString().orEmpty(),
            floor = taskIntValue(
                paths = listOf(listOf("task", "attributes", "floor")),
                legacyKeys = listOf("floor"),
            )?.toString().orEmpty(),
        ),
        taskBrief = structured.taskBrief.orEmpty(),
        notes = structured.notes.orEmpty(),
        contacts = AnnouncementContactsInput(
            name = contactName().orEmpty(),
            phone = contactPhone().orEmpty(),
            method = AnnouncementContactMethod.fromRaw(contactMethod()),
            audience = AnnouncementAudience.fromRaw(contactAudience()),
        ),
    ).normalizedForCurrentAction()
}

fun Announcement.hasReusablePrefillMedia(): Boolean = hasAttachedMedia

private fun Announcement.contactName(): String? = taskStringValue(
    paths = listOf(listOf("task", "contacts", "name")),
    legacyKeys = listOf("contact_name"),
)

private fun Announcement.contactPhone(): String? = taskStringValue(
    paths = listOf(listOf("task", "contacts", "phone")),
    legacyKeys = listOf("contact_phone"),
)

private fun Announcement.contactMethod(): String? = taskStringValue(
    paths = listOf(listOf("task", "contacts", "method")),
    legacyKeys = listOf("contact_method"),
)

private fun Announcement.contactAudience(): String? = taskStringValue(
    paths = listOf(listOf("task", "contacts", "audience")),
    legacyKeys = listOf("audience"),
)

private fun AnnouncementCreateFormDraft.resolvedCategoryRaw(): String = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup -> {
        if (sourceKind == AnnouncementStructuredData.SourceKind.Person) {
            "handoff"
        } else {
            "pickup_point"
        }
    }

    AnnouncementStructuredData.ActionType.Buy -> "buy"
    AnnouncementStructuredData.ActionType.Carry -> "carry"
    AnnouncementStructuredData.ActionType.Ride -> "ride"
    AnnouncementStructuredData.ActionType.ProHelp -> "pro_help"
    AnnouncementStructuredData.ActionType.Other, null -> "other"
}

private fun AnnouncementCreateFormDraft.generatedDescription(): String {
    val lines = buildList {
        taskBrief.trimmedOrNull()?.let(::add)
        notes.trimmedOrNull()?.let(::add)
        source.address.trimmedOrNull()?.let { add("Старт: $it") }
        if (showsDestinationSection && destination.address.trimmedOrNull() != null) {
            add("Куда: ${destination.address.trim()}")
        }
        add("Срочность: ${urgency.rawValue}")
        selectedHintValues().takeIf { it.isNotEmpty() }?.let { add(it.joinToString()) }
    }

    return lines
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()
        .joinToString(separator = "\n")
}

private fun AnnouncementCreateFormDraft.generatedTags(): List<String> = buildList {
    add(mainGroup.rawValue)
    actionType?.rawValue?.let(::add)
    add(resolvedCategoryRaw())
    itemType?.rawValue?.let(::add)
    purchaseType?.rawValue?.let(::add)
    helpType?.rawValue?.let(::add)
    sourceKind?.rawValue?.let(::add)
    destinationKind?.rawValue?.let(::add)
    attributes.weightCategory?.rawValue?.let(::add)
    attributes.sizeCategory?.rawValue?.let(::add)
    if (attributes.requiresVehicle) add("requires_vehicle")
    if (attributes.needsTrunk) add("needs_trunk")
    if (attributes.requiresCarefulHandling) add("requires_careful_handling")
    if (attributes.needsLoader) add("needs_loader")
    if (attributes.requiresLiftToFloor) add("requires_lift_to_floor")
    if (attributes.waitOnSite) add("wait_on_site")
    if (attributes.contactless) add("contactless")
    if (attributes.requiresReceipt) add("requires_receipt")
    if (attributes.requiresConfirmationCode) add("requires_confirmation_code")
    if (attributes.callBeforeArrival) add("call_before_arrival")
    if (attributes.photoReportRequired) add("photo_report_required")
}.distinct()

private fun AnnouncementCreateFormDraft.selectedHintValues(): List<String> = buildList {
    actionType?.rawValue?.let(::add)
    itemType?.rawValue?.let(::add)
    purchaseType?.rawValue?.let(::add)
    helpType?.rawValue?.let(::add)
    sourceKind?.rawValue?.let(::add)
    destinationKind?.rawValue?.let(::add)
    add(urgency.rawValue)
    attributes.weightCategory?.rawValue?.let(::add)
    attributes.sizeCategory?.rawValue?.let(::add)
    if (attributes.requiresVehicle) add("requires_vehicle")
    if (attributes.needsTrunk) add("needs_trunk")
    if (attributes.requiresCarefulHandling) add("requires_careful_handling")
    if (attributes.needsLoader) add("needs_loader")
    if (attributes.requiresLiftToFloor) add("requires_lift_to_floor")
    if (attributes.waitOnSite) add("wait_on_site")
    if (attributes.contactless) add("contactless")
    if (attributes.requiresReceipt) add("requires_receipt")
    if (attributes.requiresConfirmationCode) add("requires_confirmation_code")
    if (attributes.callBeforeArrival) add("call_before_arrival")
    if (attributes.photoReportRequired) add("photo_report_required")
}.distinct()

private fun AnnouncementCreateFormDraft.resolvedBudgetAmount(): Int? = when (budget.mode) {
    AnnouncementBudgetMode.Fixed -> parsePositiveInt(budget.amount)
    AnnouncementBudgetMode.Range -> parsePositiveInt(budget.max) ?: parsePositiveInt(budget.min)
}

private fun AnnouncementCreateFormDraft.resolvedBudgetMin(): Int? = when (budget.mode) {
    AnnouncementBudgetMode.Fixed -> parsePositiveInt(budget.amount)
    AnnouncementBudgetMode.Range -> parsePositiveInt(budget.min)
}

private fun AnnouncementCreateFormDraft.resolvedBudgetMax(): Int? = when (budget.mode) {
    AnnouncementBudgetMode.Fixed -> parsePositiveInt(budget.amount)
    AnnouncementBudgetMode.Range -> parsePositiveInt(budget.max)
}

private fun defaultStartAtFor(urgency: AnnouncementStructuredData.Urgency): Instant = when (urgency) {
    AnnouncementStructuredData.Urgency.Now -> Instant.now().plusSeconds(20 * 60L)
    AnnouncementStructuredData.Urgency.Today -> Instant.now().plusSeconds(2 * 60 * 60L)
    AnnouncementStructuredData.Urgency.Scheduled -> Instant.now().plusSeconds(3 * 60 * 60L)
    AnnouncementStructuredData.Urgency.Flexible -> Instant.now().plusSeconds(6 * 60 * 60L)
}

private fun defaultSourceKindFor(
    actionType: AnnouncementStructuredData.ActionType,
): AnnouncementStructuredData.SourceKind? = availableSourceKindsFor(actionType).firstOrNull()

private fun defaultDestinationKindFor(
    actionType: AnnouncementStructuredData.ActionType,
): AnnouncementStructuredData.DestinationKind? = availableDestinationKindsFor(actionType).firstOrNull()

private fun defaultEstimatedMinutesFor(
    actionType: AnnouncementStructuredData.ActionType,
): String = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup -> "30"
    AnnouncementStructuredData.ActionType.Buy -> "40"
    AnnouncementStructuredData.ActionType.Carry -> "45"
    AnnouncementStructuredData.ActionType.Ride -> "20"
    AnnouncementStructuredData.ActionType.ProHelp,
    AnnouncementStructuredData.ActionType.Other,
    -> "30"
}

private fun availableSourceKindsFor(
    actionType: AnnouncementStructuredData.ActionType,
): List<AnnouncementStructuredData.SourceKind> = when (actionType) {
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
}

private fun availableDestinationKindsFor(
    actionType: AnnouncementStructuredData.ActionType,
): List<AnnouncementStructuredData.DestinationKind> = when (actionType) {
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
    -> emptyList()
}

private fun availableAttributeTogglesFor(
    actionType: AnnouncementStructuredData.ActionType,
    requiresLiftToFloor: Boolean,
): List<AnnouncementAttributeToggle> = when (actionType) {
    AnnouncementStructuredData.ActionType.Pickup -> buildList {
        add(AnnouncementAttributeToggle.RequiresVehicle)
        add(AnnouncementAttributeToggle.NeedsTrunk)
        add(AnnouncementAttributeToggle.RequiresCarefulHandling)
        add(AnnouncementAttributeToggle.RequiresLiftToFloor)
        if (requiresLiftToFloor) add(AnnouncementAttributeToggle.HasElevator)
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
        if (requiresLiftToFloor) add(AnnouncementAttributeToggle.HasElevator)
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
}

private fun requiresDestinationAddressFor(
    actionType: AnnouncementStructuredData.ActionType,
): Boolean = actionType in setOf(
    AnnouncementStructuredData.ActionType.Pickup,
    AnnouncementStructuredData.ActionType.Buy,
    AnnouncementStructuredData.ActionType.Ride,
)

private fun showsDestinationSectionFor(
    actionType: AnnouncementStructuredData.ActionType,
): Boolean = actionType in setOf(
    AnnouncementStructuredData.ActionType.Pickup,
    AnnouncementStructuredData.ActionType.Buy,
    AnnouncementStructuredData.ActionType.Carry,
    AnnouncementStructuredData.ActionType.Ride,
)

private fun requiresTaskBriefFor(
    actionType: AnnouncementStructuredData.ActionType,
): Boolean = actionType in setOf(
    AnnouncementStructuredData.ActionType.ProHelp,
    AnnouncementStructuredData.ActionType.Other,
)

private fun GeoPoint.toJsonPoint(): JsonObject = buildJsonObject {
    put("lat", JsonPrimitive(latitude))
    put("lon", JsonPrimitive(longitude))
}

private fun jsonString(value: String?): JsonElement = value.trimmedOrNull()?.let(::JsonPrimitive) ?: JsonNull

private fun jsonInt(value: String): JsonElement = parsePositiveInt(value)?.let(::JsonPrimitive) ?: JsonNull

private fun parsePositiveInt(rawValue: String): Int? {
    val normalized = rawValue
        .trim()
        .replace(" ", "")
        .replace(',', '.')

    val parsed = normalized.toDoubleOrNull()?.toInt() ?: return null
    return parsed.takeIf { it > 0 }
}

private fun parseOptionalBoundedInt(rawValue: String, max: Int = 1440): Int? {
    val value = parsePositiveInt(rawValue) ?: return null
    return value.takeIf { it in 1..max }
}

private fun normalizePhone(rawValue: String): String? {
    val digits = rawValue.filter(Char::isDigit)
    if (digits.isBlank()) return null

    val normalized = when {
        digits.length == 11 && digits.startsWith("8") -> "7${digits.drop(1)}"
        digits.length == 10 -> "7$digits"
        digits.length == 11 && digits.startsWith("7") -> digits
        digits.length in 11..15 -> digits
        else -> return null
    }

    return "+$normalized"
}
