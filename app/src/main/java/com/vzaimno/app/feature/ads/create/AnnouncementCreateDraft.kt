package com.vzaimno.app.feature.ads.create

import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.CreateAnnouncementDraft
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.visibleModerationReasons
import com.vzaimno.app.core.model.budgetMaxValue
import com.vzaimno.app.core.model.budgetMinValue
import com.vzaimno.app.core.model.budgetValue
import com.vzaimno.app.core.model.destinationPoint
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.sourcePoint
import com.vzaimno.app.core.model.structuredData
import com.vzaimno.app.core.model.taskBoolValue
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

// ════════════════════════════════════════════════════════════════════════
//  SUBMISSION PLAN
// ════════════════════════════════════════════════════════════════════════

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

// ════════════════════════════════════════════════════════════════════════
//  PAYLOAD BUILDER — flat legacy + nested task V2
// ════════════════════════════════════════════════════════════════════════

fun AnnouncementCreateFormDraft.toRepositoryDraft(
    requestStatus: String,
): CreateAnnouncementDraft {
    val normalizedTitle = generatedTitle.ifBlank { "Новое объявление" }
    val sourceAddress = source.address.trim()
    val destinationAddress = destination.address.trim()
    val itemRaw = itemType?.rawValue
    val purchaseRaw = purchaseType?.rawValue
    val helpRaw = helpType?.rawValue
    val actionRaw = actionType?.rawValue
    val resolvedCategoryRaw = resolvedCategory.rawValue
    val normalizedPhone = normalizePhone(contacts.phone)
    val genDescription = resolvedDescription
    val budgetMin = budget.min.toIntOrNull()
    val budgetMax = budget.max.toIntOrNull()
    val quickOfferPrice = maxOf(0, budgetMin ?: budgetMax ?: recommendedPriceRange.min)
    val tags = generatedTags
    val hints = generatedHints
    val normalizedHints = (hints + aiHints).distinct()
    val travelMode = when {
        attributes.requiresVehicle -> "driving"
        actionType == AnnouncementStructuredData.ActionType.Ride -> "driving"
        mainGroup == AnnouncementMainGroup.Delivery -> "driving"
        else -> "walking"
    }
    val startAt = resolveStartAt()
    val endAt = resolveEndAt()

    // ── Nested task object (V2) ─────────────────────────────────────────

    val taskPayload = buildJsonObject {
        put("schema_version", JsonPrimitive(2))
        put("lifecycle", buildJsonObject {
            put("status", JsonPrimitive(if (requestStatus == "active") "open" else requestStatus))
            put("deleted_at", JsonNull)
        })
        put("builder", buildJsonObject {
            put("main_group", jsonString(mainGroup.rawValue))
            put("action_type", jsonString(actionRaw))
            put("user_action_type", jsonString(actionRaw))
            put("resolved_category", jsonString(resolvedCategoryRaw))
            put("item_type", jsonString(itemRaw))
            put("purchase_type", jsonString(purchaseRaw))
            put("help_type", jsonString(helpRaw))
            put("source_kind", jsonString(sourceKind?.rawValue))
            put("destination_kind", jsonString(destinationKind?.rawValue))
            put("urgency", jsonString(urgency.rawValue))
            put("task_brief", jsonString(taskBrief.trim()))
            put("notes", jsonString(notes.trim()))
        })
        put("attributes", buildJsonObject {
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
            put("cargo", buildJsonObject {
                put("length_cm", jsonInt(attributes.cargoLength))
                put("width_cm", jsonInt(attributes.cargoWidth))
                put("height_cm", jsonInt(attributes.cargoHeight))
            })
            put("estimated_task_minutes", jsonInt(attributes.estimatedTaskMinutes))
            put("waiting_minutes", if (attributes.waitOnSite) jsonInt(attributes.waitingMinutes) else JsonNull)
            put("floor", if (attributes.requiresLiftToFloor) jsonInt(attributes.floor) else JsonNull)
        })
        put("budget", buildJsonObject {
            put("currency", JsonPrimitive("RUB"))
            put("recommended_min", JsonPrimitive(recommendedPriceRange.min))
            put("recommended_max", JsonPrimitive(recommendedPriceRange.max))
            put("min", budgetMin?.let(::JsonPrimitive) ?: JsonNull)
            put("max", budgetMax?.let(::JsonPrimitive) ?: JsonNull)
            put("amount", budgetMax?.let(::JsonPrimitive) ?: budgetMin?.let(::JsonPrimitive) ?: JsonNull)
        })
        put("route", buildJsonObject {
            put("travel_mode", JsonPrimitive(travelMode))
            put("start_at", JsonPrimitive(startAt.toString()))
            put("has_end_time", JsonPrimitive(hasEndTime))
            put("end_at", endAt?.let { JsonPrimitive(it.toString()) } ?: JsonNull)
            put("source", buildJsonObject {
                put("address", jsonString(sourceAddress))
                put("kind", jsonString(sourceKind?.rawValue))
                put("point", source.point?.toJsonPoint() ?: JsonNull)
            })
            put("destination", buildJsonObject {
                put("address", jsonString(destinationAddress))
                put("kind", jsonString(destinationKind?.rawValue))
                put("point", destination.point?.toJsonPoint() ?: JsonNull)
            })
        })
        put("contacts", buildJsonObject {
            put("name", jsonString(contacts.name.trim()))
            put("phone", jsonString(normalizedPhone))
            put("method", JsonPrimitive(contacts.method.rawValue))
            put("audience", JsonPrimitive(contacts.audience.rawValue))
        })
        put("search", buildJsonObject {
            put("generated_title", JsonPrimitive(normalizedTitle))
            put("generated_description", jsonString(genDescription))
            put("generated_tags", JsonArray(tags.map(::JsonPrimitive)))
            put("hints", buildJsonArray { normalizedHints.forEach { add(JsonPrimitive(it)) } })
        })
        put("offer_policy", buildJsonObject {
            put("quick_offer_enabled", JsonPrimitive(true))
            put("quick_offer_price", JsonPrimitive(quickOfferPrice))
            put("counter_price_allowed", JsonPrimitive(true))
            put("reoffer_policy", JsonPrimitive("blocked_after_reject"))
        })
        put("execution", buildJsonObject {
            put("status", JsonPrimitive("open"))
            put("assignment_id", JsonNull)
            put("performer_user_id", JsonNull)
        })
    }

    // ── Flat data object (legacy + V2 combined) ─────────────────────────

    val data = buildJsonObject {
        // Legacy flat keys
        put("category", JsonPrimitive(category))
        put("main_group", JsonPrimitive(mainGroup.rawValue))
        put("action_type", jsonString(actionRaw))
        put("user_action_type", jsonString(actionRaw))
        put("resolved_category", JsonPrimitive(resolvedCategoryRaw))
        put("item_type", jsonString(itemRaw))
        put("purchase_type", jsonString(purchaseRaw))
        put("help_type", jsonString(helpRaw))
        put("source_kind", jsonString(sourceKind?.rawValue))
        put("destination_kind", jsonString(destinationKind?.rawValue))
        put("urgency", JsonPrimitive(urgency.rawValue))
        put("source_address", jsonString(sourceAddress))
        put("destination_address", jsonString(destinationAddress))
        put("budget", budgetMax?.let(::JsonPrimitive) ?: budgetMin?.let(::JsonPrimitive) ?: JsonNull)
        put("budget_min", budgetMin?.let(::JsonPrimitive) ?: JsonNull)
        put("budget_max", budgetMax?.let(::JsonPrimitive) ?: JsonNull)
        put("quick_offer_price", JsonPrimitive(quickOfferPrice))
        put("recommended_price_min", JsonPrimitive(recommendedPriceRange.min))
        put("recommended_price_max", JsonPrimitive(recommendedPriceRange.max))
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
        put("cargo_length_cm", jsonInt(attributes.cargoLength))
        put("cargo_width_cm", jsonInt(attributes.cargoWidth))
        put("cargo_height_cm", jsonInt(attributes.cargoHeight))
        put("cargo_length", jsonInt(attributes.cargoLength))
        put("cargo_width", jsonInt(attributes.cargoWidth))
        put("cargo_height", jsonInt(attributes.cargoHeight))
        put("estimated_task_minutes", jsonInt(attributes.estimatedTaskMinutes))
        put("waiting_minutes", if (attributes.waitOnSite) jsonInt(attributes.waitingMinutes) else JsonNull)
        put("floor", if (attributes.requiresLiftToFloor) jsonInt(attributes.floor) else JsonNull)
        put("start_at", JsonPrimitive(startAt.toString()))
        put("has_end_time", JsonPrimitive(hasEndTime))
        put("end_at", endAt?.let { JsonPrimitive(it.toString()) } ?: JsonNull)
        put("task_brief", jsonString(taskBrief.trim()))
        put("notes", jsonString(notes.trim()))
        put("generated_description", jsonString(genDescription))
        put("generated_title", JsonPrimitive(normalizedTitle))
        put("generated_tags", JsonArray(tags.map(::JsonPrimitive)))
        put("ai_hints", JsonArray(normalizedHints.map(::JsonPrimitive)))
        put("media_local_identifiers", JsonArray(mediaLocalIdentifiers.map(::JsonPrimitive)))
        put("contact_name", jsonString(contacts.name.trim()))
        put("contact_phone", jsonString(normalizedPhone))
        put("contact_method", JsonPrimitive(contacts.method.rawValue))
        put("audience", JsonPrimitive(contacts.audience.rawValue))

        // Group-specific address keys
        if (mainGroup == AnnouncementMainGroup.Delivery) {
            put("pickup_address", jsonString(sourceAddress))
            put("dropoff_address", jsonString(destinationAddress))
            put("pickup_point", source.point?.toJsonPoint() ?: JsonNull)
            put("dropoff_point", destination.point?.toJsonPoint() ?: JsonNull)
        } else {
            put("address", jsonString(sourceAddress))
            put("help_point", source.point?.toJsonPoint() ?: JsonNull)
            if (destinationAddress.isNotBlank()) {
                put("help_destination_address", jsonString(destinationAddress))
            }
            put("destination_point", destination.point?.toJsonPoint() ?: JsonNull)
        }

        // Nested task object
        put("task", taskPayload)
    }

    return CreateAnnouncementDraft(
        category = category,
        title = normalizedTitle,
        status = requestStatus,
        data = data,
    )
}

// ════════════════════════════════════════════════════════════════════════
//  OPTIMISTIC ANNOUNCEMENT
// ════════════════════════════════════════════════════════════════════════

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

// ════════════════════════════════════════════════════════════════════════
//  PREFILL FROM EXISTING ANNOUNCEMENT
// ════════════════════════════════════════════════════════════════════════

fun Announcement.toCreateFormDraft(): AnnouncementCreateFormDraft {
    val structured = structuredData

    val inferredAction = structured.actionType ?: inferActionType(this)
    val inferredUrgency = structured.urgency ?: AnnouncementStructuredData.Urgency.Today

    return AnnouncementCreateFormDraft(
        actionType = inferredAction,
        itemType = AnnouncementCreateItemType.fromRaw(structured.itemType),
        purchaseType = AnnouncementCreatePurchaseType.fromRaw(structured.purchaseType),
        helpType = AnnouncementCreateHelpType.fromRaw(structured.helpType),
        sourceKind = structured.sourceKind,
        destinationKind = structured.destinationKind,
        urgency = inferredUrgency,
        source = AnnouncementAddressInput(
            address = primarySourceAddress.orEmpty(),
            point = sourcePoint,
        ),
        destination = AnnouncementAddressInput(
            address = primaryDestinationAddress.orEmpty(),
            point = destinationPoint,
        ),
        budget = AnnouncementBudgetInput(
            min = budgetMinValue?.toString().orEmpty(),
            max = (budgetMaxValue ?: budgetValue)?.toString().orEmpty(),
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
            cargoLength = taskIntValue(
                paths = listOf(listOf("task", "attributes", "cargo", "length_cm")),
                legacyKeys = listOf("cargo_length_cm", "cargo_length"),
            )?.toString().orEmpty(),
            cargoWidth = taskIntValue(
                paths = listOf(listOf("task", "attributes", "cargo", "width_cm")),
                legacyKeys = listOf("cargo_width_cm", "cargo_width"),
            )?.toString().orEmpty(),
            cargoHeight = taskIntValue(
                paths = listOf(listOf("task", "attributes", "cargo", "height_cm")),
                legacyKeys = listOf("cargo_height_cm", "cargo_height"),
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
        startDate = taskStringValue(
            paths = listOf(listOf("task", "route", "start_at")),
            legacyKeys = listOf("start_at"),
        ).orEmpty(),
        hasEndTime = taskBoolValue(
            paths = listOf(listOf("task", "route", "has_end_time")),
            legacyKeys = listOf("has_end_time"),
        ) ?: false,
        endDate = taskStringValue(
            paths = listOf(listOf("task", "route", "end_at")),
            legacyKeys = listOf("end_at"),
        ).orEmpty(),
    ).let { CreateAdActionDefaults.normalizeForAction(it) }
}

fun Announcement.toCreateFormDraftWithModerationMarks(): AnnouncementCreateFormDraft {
    val draft = toCreateFormDraft()
    val marks = extractModerationMarks(this)
    return draft.copy(moderationMarks = marks)
}

fun Announcement.hasReusablePrefillMedia(): Boolean = hasAttachedMedia

// ════════════════════════════════════════════════════════════════════════
//  MODERATION MARKS
// ════════════════════════════════════════════════════════════════════════

private fun extractModerationMarks(announcement: Announcement): Map<String, DraftModerationMark> {
    val marks = linkedMapOf<String, DraftModerationMark>()

    announcement.visibleModerationReasons.forEach { reason ->
        val key = when (reason.field) {
            "pickup_address" -> "pickup_address"
            "dropoff_address" -> "dropoff_address"
            "destination_address" -> "destination_address"
            "address" -> "address"
            "title" -> "title"
            "notes", "generated_description" -> "notes"
            "media", "images", "photos", "media_local_identifiers" -> "media"
            else -> reason.field
        }
        val mark = DraftModerationMark(
            severity = when (reason.severity) {
                com.vzaimno.app.core.model.ModerationSeverity.Warning -> ModerationSeverity.Warning
                com.vzaimno.app.core.model.ModerationSeverity.Danger -> ModerationSeverity.Error
                com.vzaimno.app.core.model.ModerationSeverity.None -> ModerationSeverity.Warning
            },
            code = reason.code,
            details = reason.details,
        )
        val existing = marks[key]
        if (existing == null || existing.severity.ordinal <= mark.severity.ordinal) {
            marks[key] = mark
        }
    }

    return marks
}

// ════════════════════════════════════════════════════════════════════════
//  PRIVATE HELPERS
// ════════════════════════════════════════════════════════════════════════

private fun inferActionType(announcement: Announcement): AnnouncementStructuredData.ActionType? {
    val structured = announcement.structuredData
    return when {
        structured.helpType != null -> AnnouncementStructuredData.ActionType.ProHelp
        structured.requiresReceipt -> AnnouncementStructuredData.ActionType.Buy
        structured.requiresConfirmationCode -> AnnouncementStructuredData.ActionType.Pickup
        announcement.category == "delivery" -> AnnouncementStructuredData.ActionType.Pickup
        structured.needsLoader || structured.requiresLiftToFloor -> AnnouncementStructuredData.ActionType.Carry
        else -> AnnouncementStructuredData.ActionType.Other
    }
}

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

private fun defaultStartAtFor(urgency: AnnouncementStructuredData.Urgency): Instant = when (urgency) {
    AnnouncementStructuredData.Urgency.Now -> Instant.now().plusSeconds(20 * 60L)
    AnnouncementStructuredData.Urgency.Today -> Instant.now().plusSeconds(2 * 60 * 60L)
    AnnouncementStructuredData.Urgency.Scheduled -> Instant.now().plusSeconds(3 * 60 * 60L)
    AnnouncementStructuredData.Urgency.Flexible -> Instant.now().plusSeconds(6 * 60 * 60L)
}

private fun AnnouncementCreateFormDraft.resolveStartAt(): Instant =
    parseStoredInstant(startDate) ?: defaultStartAtFor(urgency)

private fun AnnouncementCreateFormDraft.resolveEndAt(): Instant? =
    parseStoredInstant(endDate)

private fun parseStoredInstant(rawValue: String): Instant? =
    rawValue.trimmedOrNull()?.let { value ->
        runCatching { Instant.parse(value) }.getOrNull()
    }

private fun GeoPoint.toJsonPoint(): JsonObject = buildJsonObject {
    put("lat", JsonPrimitive(latitude))
    put("lon", JsonPrimitive(longitude))
}

private fun jsonString(value: String?): JsonElement =
    value.trimmedOrNull()?.let(::JsonPrimitive) ?: JsonNull

private fun jsonInt(value: String): JsonElement =
    parsePositiveInt(value)?.let(::JsonPrimitive) ?: JsonNull

private fun parsePositiveInt(rawValue: String): Int? {
    val normalized = rawValue
        .trim()
        .replace(" ", "")
        .replace(',', '.')
    val parsed = normalized.toDoubleOrNull()?.toInt() ?: return null
    return parsed.takeIf { it > 0 }
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
