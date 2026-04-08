package com.vzaimno.app.core.model

import com.vzaimno.app.core.common.asJsonArrayOrNull
import com.vzaimno.app.core.common.asJsonObjectOrNull
import com.vzaimno.app.core.common.doubleOrNullCompat
import com.vzaimno.app.core.common.formatRubles
import com.vzaimno.app.core.common.humanizeRawValue
import com.vzaimno.app.core.common.intOrNullCompat
import com.vzaimno.app.core.common.jsonAt
import com.vzaimno.app.core.common.parseInstant
import com.vzaimno.app.core.common.resolveAgainstBaseUrl
import com.vzaimno.app.core.common.stringOrNullCompat
import com.vzaimno.app.core.common.taskBoolValue
import com.vzaimno.app.core.common.taskDateString
import com.vzaimno.app.core.common.taskIntValue
import com.vzaimno.app.core.common.taskStringValue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import java.time.Instant

data class Announcement(
    val id: String,
    val userId: String,
    val category: String,
    val title: String,
    val status: String,
    val data: JsonObject = buildJsonObject { },
    val createdAt: Instant?,
    val media: List<JsonElement> = emptyList(),
)

data class CreateAnnouncementDraft(
    val category: String,
    val title: String,
    val status: String = "active",
    val data: JsonObject,
)

data class MediaModerationDecision(
    val announcement: Announcement,
    val maxNsfw: Double,
    val decision: String,
    val canAppeal: Boolean,
    val message: String,
)

data class AnnouncementStructuredData(
    val actionType: ActionType?,
    val resolvedCategory: ResolvedCategory?,
    val itemType: String?,
    val purchaseType: String?,
    val helpType: String?,
    val sourceKind: SourceKind?,
    val destinationKind: DestinationKind?,
    val urgency: Urgency?,
    val requiresVehicle: Boolean,
    val needsTrunk: Boolean,
    val requiresCarefulHandling: Boolean,
    val needsLoader: Boolean,
    val requiresLiftToFloor: Boolean,
    val hasElevator: Boolean,
    val waitOnSite: Boolean,
    val contactless: Boolean,
    val requiresReceipt: Boolean,
    val requiresConfirmationCode: Boolean,
    val callBeforeArrival: Boolean,
    val photoReportRequired: Boolean,
    val weightCategory: WeightCategory?,
    val sizeCategory: SizeCategory?,
    val estimatedTaskMinutes: Int?,
    val waitingMinutes: Int?,
    val budgetMin: Int?,
    val budgetMax: Int?,
    val sourceAddress: String?,
    val destinationAddress: String?,
    val taskBrief: String?,
    val notes: String?,
) {
    enum class ActionType(val rawValue: String, val title: String) {
        Pickup("pickup", "Забрать"),
        Buy("buy", "Купить"),
        Carry("carry", "Перенести"),
        Ride("ride", "Подвезти"),
        ProHelp("pro_help", "Помощь от профи"),
        Other("other", "Другое"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): ActionType? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }

    enum class ResolvedCategory(val rawValue: String) {
        PickupPoint("pickup_point"),
        Handoff("handoff"),
        Buy("buy"),
        Carry("carry"),
        Ride("ride"),
        ProHelp("pro_help"),
        Other("other"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): ResolvedCategory? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }

    enum class SourceKind(val rawValue: String, val title: String) {
        Person("person", "У человека"),
        PickupPoint("pickup_point", "Из ПВЗ"),
        Venue("venue", "Из заведения"),
        Address("address", "С адреса"),
        Office("office", "Из офиса"),
        Other("other", "Другое"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): SourceKind? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }

    enum class DestinationKind(val rawValue: String, val title: String) {
        Person("person", "Человеку"),
        Address("address", "По адресу"),
        Office("office", "В офис"),
        Entrance("entrance", "До подъезда"),
        Metro("metro", "К метро"),
        Other("other", "Другое"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): DestinationKind? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }

    enum class Urgency(val rawValue: String, val title: String) {
        Now("now", "Сейчас"),
        Today("today", "Сегодня"),
        Scheduled("scheduled", "Ко времени"),
        Flexible("flexible", "Не срочно"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): Urgency? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }

    enum class WeightCategory(val rawValue: String, val title: String) {
        UpTo1Kg("up_to_1kg", "До 1 кг"),
        UpTo3Kg("up_to_3kg", "До 3 кг"),
        UpTo7Kg("up_to_7kg", "До 7 кг"),
        UpTo15Kg("up_to_15kg", "До 15 кг"),
        Over15Kg("over_15kg", "Тяжелее"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): WeightCategory? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }

    enum class SizeCategory(val rawValue: String, val title: String) {
        Pocket("pocket", "Карман"),
        Hand("hand", "В руку"),
        Backpack("backpack", "В рюкзак"),
        Trunk("trunk", "В багажник"),
        Bulky("bulky", "Крупное"),
        ;

        companion object {
            fun fromRaw(rawValue: String?): SizeCategory? = entries.firstOrNull { it.rawValue == rawValue }
        }
    }
}

val Announcement.createdAtEpochSeconds: Long?
    get() = createdAt?.epochSecond ?: parseInstant(data["created_at"].stringOrNullCompat())?.epochSecond

fun Announcement.imageUrls(apiBaseUrl: String): List<String> {
    val values = buildList {
        addAll(media)
        listOf("media", "images", "photos").forEach { key ->
            val raw = data[key] ?: return@forEach
            val arrayValues = raw.asJsonArrayOrNull()
            if (arrayValues != null) {
                addAll(arrayValues)
            } else {
                add(raw)
            }
        }
    }

    return values
        .mapNotNull { resolveMediaUrl(apiBaseUrl = apiBaseUrl, value = it) }
        .distinct()
}

fun Announcement.previewImageUrl(apiBaseUrl: String): String? = imageUrls(apiBaseUrl).firstOrNull()

val Announcement.hasAttachedMedia: Boolean
    get() = media.isNotEmpty() || listOf("media", "images", "photos").any { data[it] != null }

val Announcement.offersCount: Int
    get() = data["offers_count"].intOrNullCompat() ?: 0

val Announcement.budgetValue: Int?
    get() = data.taskIntValue(
        paths = listOf(listOf("task", "budget", "amount")),
        legacyKeys = listOf("budget"),
    ) ?: data["budget"].intOrNullCompat()

val Announcement.budgetMinValue: Int?
    get() = data.taskIntValue(
        paths = listOf(listOf("task", "budget", "min")),
        legacyKeys = listOf("budget_min"),
    ) ?: data["budget_min"].intOrNullCompat()

val Announcement.budgetMaxValue: Int?
    get() = data.taskIntValue(
        paths = listOf(listOf("task", "budget", "max")),
        legacyKeys = listOf("budget_max"),
    ) ?: data["budget_max"].intOrNullCompat()

val Announcement.formattedBudgetText: String?
    get() = when {
        budgetMinValue != null && budgetMaxValue != null && budgetMinValue == budgetMaxValue -> {
            budgetMinValue?.formatRubles()
        }

        budgetMinValue != null && budgetMaxValue != null -> {
            "${budgetMinValue?.formatRubles()?.removeSuffix(" ₽")}–${budgetMaxValue?.formatRubles()}"
        }

        budgetMinValue != null -> "от ${budgetMinValue?.formatRubles()}"
        budgetMaxValue != null -> "до ${budgetMaxValue?.formatRubles()}"
        budgetValue != null -> budgetValue?.formatRubles()
        else -> data["budget"].stringOrNullCompat()?.let { "$it ₽" }
    }

val Announcement.taskStateProjection: TaskStateProjection
    get() {
        val isDeleted = data.taskDateString(
            paths = listOf(listOf("task", "lifecycle", "deleted_at")),
        ) != null
        val lifecycle = TaskLifecycleStatus.from(
            statusValue = data.taskStringValue(paths = listOf(listOf("task", "lifecycle", "status"))),
            legacyAnnouncementStatus = status,
            isDeleted = isDeleted,
        )
        val execution = TaskExecutionStatus.from(
            statusValue = data.taskStringValue(
                paths = listOf(
                    listOf("task", "execution", "status"),
                    listOf("task", "assignment", "execution_status"),
                ),
                legacyKeys = listOf("execution_status"),
            ),
        )
        val acceptedConfirmed = data.taskBoolValue(
            paths = listOf(
                listOf("task", "execution", "accepted_confirmed"),
                listOf("task", "assignment", "accepted_confirmed"),
                listOf("execution", "accepted_confirmed"),
            ),
            legacyKeys = listOf("execution_status_confirmed"),
        ) ?: false

        val budgetMin = data.taskIntValue(
            paths = listOf(listOf("task", "budget", "min")),
            legacyKeys = listOf("budget_min"),
        ) ?: budgetMinValue

        val budgetMax = data.taskIntValue(
            paths = listOf(listOf("task", "budget", "max")),
            legacyKeys = listOf("budget_max", "budget"),
        ) ?: budgetMaxValue ?: budgetValue

        val quickOfferPrice = data.taskIntValue(
            paths = listOf(listOf("task", "offer_policy", "quick_offer_price")),
            legacyKeys = listOf("quick_offer_price"),
        ) ?: budgetMin ?: budgetMax

        val isVisibleOnMap = lifecycle.keepsTaskPublic && !isDeleted && !execution.blocksNewOffers
        val isOpenForOffers = lifecycle.keepsTaskPublic && !isDeleted && !execution.blocksNewOffers
        val customerCanSeeRoute = !isDeleted && execution.makesCustomerRouteVisible

        return TaskStateProjection(
            schemaVersion = data.taskIntValue(paths = listOf(listOf("task", "schema_version"))) ?: 1,
            lifecycleStatus = lifecycle,
            executionStatus = execution,
            acceptedConfirmed = acceptedConfirmed,
            budget = TaskBudgetProjection(
                min = budgetMin,
                max = budgetMax,
                quickOfferPrice = quickOfferPrice,
            ),
            visibility = TaskVisibilityProjection(
                isVisibleOnMap = isVisibleOnMap,
                isOpenForOffers = isOpenForOffers,
                customerCanSeeRoute = customerCanSeeRoute,
                chatShouldRemainTaskBound = !isDeleted,
            ),
            isDeleted = isDeleted,
        )
    }

val Announcement.quickOfferPrice: Int?
    get() = taskStateProjection.budget.quickOfferPrice

val Announcement.canAppearOnMap: Boolean
    get() = taskStateProjection.visibility.isVisibleOnMap

val Announcement.canAcceptOffers: Boolean
    get() = taskStateProjection.visibility.isOpenForOffers

val Announcement.customerCanSeeExecutionRoute: Boolean
    get() = taskStateProjection.visibility.customerCanSeeRoute

fun Announcement.taskValue(path: List<String>): JsonElement? = data.jsonAt(path)

fun Announcement.taskStringValue(
    paths: List<List<String>>,
    legacyKeys: List<String> = emptyList(),
): String? = data.taskStringValue(paths = paths, legacyKeys = legacyKeys)

fun Announcement.taskBoolValue(
    paths: List<List<String>>,
    legacyKeys: List<String> = emptyList(),
): Boolean? = data.taskBoolValue(paths = paths, legacyKeys = legacyKeys)

fun Announcement.taskIntValue(
    paths: List<List<String>>,
    legacyKeys: List<String> = emptyList(),
): Int? = data.taskIntValue(paths = paths, legacyKeys = legacyKeys)

val Announcement.primarySourceAddress: String?
    get() = data.taskStringValue(
        paths = listOf(
            listOf("task", "route", "source", "address"),
            listOf("task", "route", "start", "address"),
        ),
        legacyKeys = listOf("source_address", "pickup_address", "address"),
    )

val Announcement.primaryDestinationAddress: String?
    get() = data.taskStringValue(
        paths = listOf(
            listOf("task", "route", "destination", "address"),
            listOf("task", "route", "end", "address"),
        ),
        legacyKeys = listOf("destination_address", "dropoff_address"),
    )

val Announcement.detailsDescriptionText: String?
    get() = structuredData.notes ?: data.taskStringValue(
        paths = listOf(listOf("task", "search", "generated_description")),
        legacyKeys = listOf("generated_description"),
    )

val Announcement.sourcePoint: GeoPoint?
    get() = pointFor(
        paths = listOf(
            listOf("task", "route", "source", "point"),
            listOf("task", "route", "start", "point"),
        ),
        legacyKeys = listOf("pickup_point", "help_point", "point", "source_point"),
    )

val Announcement.destinationPoint: GeoPoint?
    get() = pointFor(
        paths = listOf(
            listOf("task", "route", "destination", "point"),
            listOf("task", "route", "end", "point"),
        ),
        legacyKeys = listOf("dropoff_point", "destination_point"),
    )

val Announcement.mapPoint: GeoPoint?
    get() = sourcePoint ?: destinationPoint

val Announcement.structuredData: AnnouncementStructuredData
    get() = AnnouncementStructuredData(
        actionType = AnnouncementStructuredData.ActionType.fromRaw(
            taskStringValue(
                paths = listOf(
                    listOf("task", "builder", "action_type"),
                    listOf("task", "builder", "user_action_type"),
                ),
                legacyKeys = listOf("user_action_type", "action_type"),
            ),
        ),
        resolvedCategory = AnnouncementStructuredData.ResolvedCategory.fromRaw(
            taskStringValue(
                paths = listOf(listOf("task", "builder", "resolved_category")),
                legacyKeys = listOf("resolved_category"),
            ),
        ),
        itemType = taskStringValue(
            paths = listOf(listOf("task", "builder", "item_type")),
            legacyKeys = listOf("item_type"),
        ),
        purchaseType = taskStringValue(
            paths = listOf(listOf("task", "builder", "purchase_type")),
            legacyKeys = listOf("purchase_type"),
        ),
        helpType = taskStringValue(
            paths = listOf(listOf("task", "builder", "help_type")),
            legacyKeys = listOf("help_type"),
        ),
        sourceKind = normalizedSourceKind(),
        destinationKind = AnnouncementStructuredData.DestinationKind.fromRaw(
            taskStringValue(
                paths = listOf(listOf("task", "builder", "destination_kind")),
                legacyKeys = listOf("destination_kind"),
            ),
        ),
        urgency = AnnouncementStructuredData.Urgency.fromRaw(
            taskStringValue(
                paths = listOf(listOf("task", "builder", "urgency")),
                legacyKeys = listOf("urgency"),
            ),
        ),
        requiresVehicle = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "requires_vehicle")),
            legacyKeys = listOf("requires_vehicle"),
        ) ?: false,
        needsTrunk = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "needs_trunk")),
            legacyKeys = listOf("needs_trunk"),
        ) ?: false,
        requiresCarefulHandling = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "requires_careful_handling")),
            legacyKeys = listOf("requires_careful_handling"),
        ) ?: false,
        needsLoader = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "needs_loader")),
            legacyKeys = listOf("needs_loader", "need_loader"),
        ) ?: false,
        requiresLiftToFloor = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "requires_lift_to_floor")),
            legacyKeys = listOf("requires_lift_to_floor"),
        ) ?: false,
        hasElevator = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "has_elevator")),
            legacyKeys = listOf("has_elevator"),
        ) ?: false,
        waitOnSite = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "wait_on_site")),
            legacyKeys = listOf("wait_on_site"),
        ) ?: false,
        contactless = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "contactless")),
            legacyKeys = listOf("contactless"),
        ) ?: false,
        requiresReceipt = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "requires_receipt")),
            legacyKeys = listOf("requires_receipt"),
        ) ?: false,
        requiresConfirmationCode = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "requires_confirmation_code")),
            legacyKeys = listOf("requires_confirmation_code"),
        ) ?: false,
        callBeforeArrival = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "call_before_arrival")),
            legacyKeys = listOf("call_before_arrival"),
        ) ?: false,
        photoReportRequired = taskBoolValue(
            paths = listOf(listOf("task", "attributes", "photo_report_required")),
            legacyKeys = listOf("photo_report_required"),
        ) ?: false,
        weightCategory = AnnouncementStructuredData.WeightCategory.fromRaw(
            taskStringValue(
                paths = listOf(listOf("task", "attributes", "weight_category")),
                legacyKeys = listOf("weight_category"),
            ),
        ),
        sizeCategory = AnnouncementStructuredData.SizeCategory.fromRaw(
            taskStringValue(
                paths = listOf(listOf("task", "attributes", "size_category")),
                legacyKeys = listOf("size_category"),
            ),
        ),
        estimatedTaskMinutes = taskIntValue(
            paths = listOf(listOf("task", "attributes", "estimated_task_minutes")),
            legacyKeys = listOf("estimated_task_minutes"),
        ),
        waitingMinutes = taskIntValue(
            paths = listOf(listOf("task", "attributes", "waiting_minutes")),
            legacyKeys = listOf("waiting_minutes"),
        ),
        budgetMin = budgetMinValue,
        budgetMax = budgetMaxValue ?: budgetValue,
        sourceAddress = primarySourceAddress,
        destinationAddress = primaryDestinationAddress,
        taskBrief = taskStringValue(
            paths = listOf(listOf("task", "builder", "task_brief")),
            legacyKeys = listOf("task_brief"),
        ),
        notes = taskStringValue(
            paths = listOf(listOf("task", "builder", "notes")),
            legacyKeys = listOf("notes"),
        ),
    )

val Announcement.shortStructuredSubtitle: String
    get() {
        val parts = listOfNotNull(
            structuredData.actionType?.title,
            structuredData.helpType ?: structuredData.purchaseType ?: structuredData.itemType,
            primarySourceAddress ?: structuredData.sourceKind?.title,
        ).map {
            if (it.contains('_') || it.contains('-')) humanizeRawValue(it) else it
        }

        return parts.take(3).joinToString(separator = " • ")
    }

private fun Announcement.pointFor(
    paths: List<List<String>>,
    legacyKeys: List<String>,
): GeoPoint? {
    paths.forEach { path ->
        parseGeoPoint(taskValue(path).asJsonObjectOrNull())?.let { return it }
    }

    legacyKeys.forEach { key ->
        parseGeoPoint(data[key].asJsonObjectOrNull())?.let { return it }
    }

    return null
}

private fun Announcement.normalizedSourceKind(): AnnouncementStructuredData.SourceKind? {
    val rawValue = taskStringValue(
        paths = listOf(listOf("task", "builder", "source_kind")),
        legacyKeys = listOf("source_kind"),
    ) ?: return null

    return when (rawValue) {
        "store", "pharmacy", "venue" -> AnnouncementStructuredData.SourceKind.Venue
        "pickupPoint" -> AnnouncementStructuredData.SourceKind.PickupPoint
        else -> AnnouncementStructuredData.SourceKind.fromRaw(rawValue)
    }
}

private fun parseGeoPoint(jsonObject: JsonObject?): GeoPoint? {
    val latitude = jsonObject?.get("lat").doubleOrNullCompat()
        ?: jsonObject?.get("lat")?.asJsonObjectOrNull()?.get("value").doubleOrNullCompat()
    val longitude = jsonObject?.get("lon").doubleOrNullCompat()
        ?: jsonObject?.get("lon")?.asJsonObjectOrNull()?.get("value").doubleOrNullCompat()

    if (latitude == null || longitude == null) return null
    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null

    return GeoPoint(latitude = latitude, longitude = longitude)
}

private fun resolveMediaUrl(apiBaseUrl: String, value: JsonElement): String? {
    value.stringOrNullCompat()?.let { return resolveAgainstBaseUrl(apiBaseUrl, it) }

    val objectValue = value.asJsonObjectOrNull() ?: return null
    val preferredKeys = listOf(
        "preview_url",
        "previewUrl",
        "thumbnail_url",
        "thumbnailUrl",
        "url",
        "image_url",
        "imageUrl",
        "file_url",
        "fileUrl",
        "path",
    )

    preferredKeys.forEach { key ->
        resolveAgainstBaseUrl(apiBaseUrl, objectValue[key].stringOrNullCompat())?.let { return it }
    }

    return objectValue["file"]?.let { nestedFile ->
        resolveMediaUrl(apiBaseUrl = apiBaseUrl, value = nestedFile)
    }
}
