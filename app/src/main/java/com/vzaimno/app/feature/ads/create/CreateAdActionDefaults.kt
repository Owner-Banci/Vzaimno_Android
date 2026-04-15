package com.vzaimno.app.feature.ads.create

import com.vzaimno.app.core.model.AnnouncementStructuredData

/**
 * Applies action-type defaults and resets incompatible fields
 * when the user selects or changes the main action scenario.
 * Mirrors iOS CreateAdDraft.applyActionDefaults / resetIncompatibleFields exactly.
 */
object CreateAdActionDefaults {

    fun applyActionChange(
        draft: AnnouncementCreateFormDraft,
        newAction: AnnouncementStructuredData.ActionType,
    ): AnnouncementCreateFormDraft {
        val resetDraft = resetIncompatibleFields(draft, newAction)
        val withDefaults = applyDefaults(resetDraft, newAction)
        return normalizeForAction(withDefaults)
    }

    // ── Defaults (apply only if nil / blank) ────────────────────────────

    private fun applyDefaults(
        draft: AnnouncementCreateFormDraft,
        action: AnnouncementStructuredData.ActionType,
    ): AnnouncementCreateFormDraft {
        var d = draft.copy(actionType = action)
        val attrs = d.attributes

        when (action) {
            AnnouncementStructuredData.ActionType.Pickup -> {
                if (d.sourceKind == null) d = d.copy(sourceKind = AnnouncementStructuredData.SourceKind.PickupPoint)
                if (d.destinationKind == null) d = d.copy(destinationKind = AnnouncementStructuredData.DestinationKind.Address)
                if (d.itemType == null) d = d.copy(itemType = AnnouncementCreateItemType.Documents)
                if (attrs.estimatedTaskMinutes.isBlank()) d = d.copy(attributes = attrs.copy(estimatedTaskMinutes = "30"))
            }
            AnnouncementStructuredData.ActionType.Buy -> {
                if (d.sourceKind == null) d = d.copy(sourceKind = AnnouncementStructuredData.SourceKind.Venue)
                if (d.destinationKind == null) d = d.copy(destinationKind = AnnouncementStructuredData.DestinationKind.Address)
                if (d.purchaseType == null) d = d.copy(purchaseType = AnnouncementCreatePurchaseType.Groceries)
                if (d.attributes.estimatedTaskMinutes.isBlank()) d = d.copy(attributes = d.attributes.copy(estimatedTaskMinutes = "40"))
            }
            AnnouncementStructuredData.ActionType.Carry -> {
                if (d.sourceKind == null) d = d.copy(sourceKind = AnnouncementStructuredData.SourceKind.Address)
                if (d.destinationKind == null) d = d.copy(destinationKind = AnnouncementStructuredData.DestinationKind.Entrance)
                if (d.itemType == null) d = d.copy(itemType = AnnouncementCreateItemType.Bags)
                if (d.attributes.estimatedTaskMinutes.isBlank()) d = d.copy(attributes = d.attributes.copy(estimatedTaskMinutes = "45"))
            }
            AnnouncementStructuredData.ActionType.Ride -> {
                if (d.sourceKind == null) d = d.copy(sourceKind = AnnouncementStructuredData.SourceKind.Address)
                if (d.destinationKind == null) d = d.copy(destinationKind = AnnouncementStructuredData.DestinationKind.Metro)
                d = d.copy(attributes = d.attributes.copy(requiresVehicle = true))
                if (d.attributes.estimatedTaskMinutes.isBlank()) d = d.copy(attributes = d.attributes.copy(estimatedTaskMinutes = "20"))
            }
            AnnouncementStructuredData.ActionType.ProHelp -> {
                if (d.sourceKind == null) d = d.copy(sourceKind = AnnouncementStructuredData.SourceKind.Address)
                if (d.helpType == null) d = d.copy(helpType = AnnouncementCreateHelpType.Consultation)
                if (d.attributes.estimatedTaskMinutes.isBlank()) d = d.copy(attributes = d.attributes.copy(estimatedTaskMinutes = "30"))
            }
            AnnouncementStructuredData.ActionType.Other -> {
                if (d.sourceKind == null) d = d.copy(sourceKind = AnnouncementStructuredData.SourceKind.Address)
                if (d.attributes.estimatedTaskMinutes.isBlank()) d = d.copy(attributes = d.attributes.copy(estimatedTaskMinutes = "30"))
            }
        }
        return d
    }

    // ── Reset incompatible fields (exact iOS rules) ─────────────────────

    private fun resetIncompatibleFields(
        draft: AnnouncementCreateFormDraft,
        newAction: AnnouncementStructuredData.ActionType,
    ): AnnouncementCreateFormDraft {
        var d = draft
        val oldAction = draft.actionType

        when (newAction) {
            AnnouncementStructuredData.ActionType.Pickup -> {
                d = d.copy(
                    purchaseType = null,
                    helpType = null,
                    taskBrief = "",
                    attributes = d.attributes.copy(requiresReceipt = false),
                )
            }
            AnnouncementStructuredData.ActionType.Buy -> {
                d = d.copy(
                    itemType = null,
                    helpType = null,
                    taskBrief = "",
                    attributes = d.attributes.copy(requiresConfirmationCode = false),
                )
            }
            AnnouncementStructuredData.ActionType.Carry -> {
                d = d.copy(
                    purchaseType = null,
                    helpType = null,
                    taskBrief = "",
                    attributes = d.attributes.copy(
                        requiresReceipt = false,
                        requiresConfirmationCode = false,
                        contactless = false,
                    ),
                )
            }
            AnnouncementStructuredData.ActionType.Ride -> {
                d = d.copy(
                    itemType = null,
                    purchaseType = null,
                    helpType = null,
                    taskBrief = "",
                    attributes = d.attributes.copy(
                        weightCategory = null,
                        sizeCategory = null,
                        cargoLength = "",
                        cargoWidth = "",
                        cargoHeight = "",
                        requiresCarefulHandling = false,
                        requiresLiftToFloor = false,
                        hasElevator = true,
                        needsLoader = false,
                        requiresReceipt = false,
                        requiresConfirmationCode = false,
                        contactless = false,
                        photoReportRequired = false,
                    ),
                )
            }
            AnnouncementStructuredData.ActionType.ProHelp -> {
                d = d.copy(
                    itemType = null,
                    purchaseType = null,
                    destinationKind = null,
                    destination = AnnouncementAddressInput(),
                    attributes = d.attributes.copy(
                        weightCategory = null,
                        sizeCategory = null,
                        cargoLength = "",
                        cargoWidth = "",
                        cargoHeight = "",
                        requiresVehicle = false,
                        needsTrunk = false,
                        requiresCarefulHandling = false,
                        requiresLiftToFloor = false,
                        hasElevator = true,
                        needsLoader = false,
                        waitOnSite = false,
                        contactless = false,
                        requiresReceipt = false,
                        requiresConfirmationCode = false,
                    ),
                )
            }
            AnnouncementStructuredData.ActionType.Other -> {
                d = d.copy(
                    itemType = null,
                    purchaseType = null,
                    helpType = null,
                    destinationKind = null,
                    destination = AnnouncementAddressInput(),
                    attributes = d.attributes.copy(
                        requiresReceipt = false,
                        requiresConfirmationCode = false,
                    ),
                )
            }
        }

        // If leaving ride to another scenario
        if (oldAction == AnnouncementStructuredData.ActionType.Ride &&
            newAction != AnnouncementStructuredData.ActionType.Ride
        ) {
            d = d.copy(
                attributes = d.attributes.copy(
                    needsTrunk = false,
                    requiresVehicle = false,
                ),
            )
        }

        return d
    }

    // ── Normalize: ensure source/destination are still valid ────────────

    fun normalizeForAction(draft: AnnouncementCreateFormDraft): AnnouncementCreateFormDraft {
        val action = draft.actionType ?: return draft
        var d = draft

        // Validate source kind
        val availableSources = d.availableSourceKinds
        if (d.sourceKind != null && d.sourceKind !in availableSources) {
            d = d.copy(sourceKind = availableSources.firstOrNull())
        }

        // Validate destination kind
        val availableDestinations = d.availableDestinationKinds
        if (d.destinationKind != null && d.destinationKind !in availableDestinations) {
            d = d.copy(destinationKind = availableDestinations.firstOrNull())
        }

        // Clear destination if section not shown
        if (!d.showsDestinationSection) {
            d = d.copy(
                destinationKind = null,
                destination = AnnouncementAddressInput(),
            )
        }

        // Reset unavailable conditions
        val availableConditions = d.availableConditionOptions
        var attrs = d.attributes

        if (AnnouncementConditionOption.RequiresVehicle !in availableConditions) attrs = attrs.copy(requiresVehicle = false)
        if (AnnouncementConditionOption.NeedsTrunk !in availableConditions) attrs = attrs.copy(needsTrunk = false)
        if (AnnouncementConditionOption.RequiresCarefulHandling !in availableConditions) attrs = attrs.copy(requiresCarefulHandling = false)
        if (AnnouncementConditionOption.RequiresLiftToFloor !in availableConditions) {
            attrs = attrs.copy(requiresLiftToFloor = false, floor = "", hasElevator = true)
        }
        if (AnnouncementConditionOption.NeedsLoader !in availableConditions) attrs = attrs.copy(needsLoader = false)
        if (AnnouncementConditionOption.WaitOnSite !in availableConditions) {
            attrs = attrs.copy(waitOnSite = false, waitingMinutes = "")
        }
        if (AnnouncementConditionOption.Contactless !in availableConditions) attrs = attrs.copy(contactless = false)
        if (AnnouncementConditionOption.RequiresReceipt !in availableConditions) attrs = attrs.copy(requiresReceipt = false)
        if (AnnouncementConditionOption.RequiresConfirmationCode !in availableConditions) attrs = attrs.copy(requiresConfirmationCode = false)
        if (AnnouncementConditionOption.CallBeforeArrival !in availableConditions) attrs = attrs.copy(callBeforeArrival = false)
        if (AnnouncementConditionOption.PhotoReportRequired !in availableConditions) attrs = attrs.copy(photoReportRequired = false)

        // Clear floor/waiting if condition off
        if (!attrs.requiresLiftToFloor) attrs = attrs.copy(floor = "", hasElevator = true)
        if (!attrs.waitOnSite) attrs = attrs.copy(waitingMinutes = "")

        // Ride always requires vehicle
        if (action == AnnouncementStructuredData.ActionType.Ride) {
            attrs = attrs.copy(requiresVehicle = true)
        }

        d = d.copy(attributes = attrs)
        return d
    }
}
