package com.vzaimno.app.core.model

enum class TaskLifecycleStatus(val rawValue: String) {
    Draft("draft"),
    PendingReview("pending_review"),
    NeedsFix("needs_fix"),
    Open("open"),
    Assigned("assigned"),
    InProgress("in_progress"),
    Completed("completed"),
    Cancelled("cancelled"),
    Archived("archived"),
    Rejected("rejected"),
    Deleted("deleted"),
    ;

    val keepsTaskPublic: Boolean
        get() = this == Open

    companion object {
        fun from(
            statusValue: String?,
            legacyAnnouncementStatus: String?,
            isDeleted: Boolean,
        ): TaskLifecycleStatus {
            if (isDeleted) return Deleted

            return when ((statusValue ?: legacyAnnouncementStatus).orEmpty().trim().lowercase()) {
                "draft" -> Draft
                "pending_review", "pending", "review", "in_review" -> PendingReview
                "needs_fix" -> NeedsFix
                "open", "active", "published" -> Open
                "assigned" -> Assigned
                "in_progress", "executing" -> InProgress
                "completed", "done" -> Completed
                "cancelled", "canceled" -> Cancelled
                "archived" -> Archived
                "rejected" -> Rejected
                "deleted" -> Deleted
                else -> Open
            }
        }
    }
}

enum class TaskExecutionStatus(val rawValue: String) {
    Open("open"),
    AwaitingAcceptance("awaiting_acceptance"),
    Accepted("accepted"),
    EnRoute("en_route"),
    OnSite("on_site"),
    InProgress("in_progress"),
    Handoff("handoff"),
    Completed("completed"),
    Cancelled("cancelled"),
    Disputed("disputed"),
    ;

    val blocksNewOffers: Boolean
        get() = when (this) {
            Accepted,
            EnRoute,
            OnSite,
            InProgress,
            Handoff,
            Completed,
            Cancelled,
            Disputed,
            -> true

            Open,
            AwaitingAcceptance,
            -> false
        }

    val makesCustomerRouteVisible: Boolean
        get() = when (this) {
            Accepted,
            EnRoute,
            OnSite,
            InProgress,
            Handoff,
            -> true

            Open,
            AwaitingAcceptance,
            Completed,
            Cancelled,
            Disputed,
            -> false
        }

    companion object {
        fun from(statusValue: String?): TaskExecutionStatus = when (statusValue.orEmpty().trim().lowercase()) {
            "awaiting_acceptance" -> AwaitingAcceptance
            "accepted", "assigned" -> Accepted
            "en_route", "heading", "route" -> EnRoute
            "on_site", "onsite", "arrived" -> OnSite
            "in_progress", "doing", "progress" -> InProgress
            "handoff", "finishing", "delivering" -> Handoff
            "completed", "done", "finish" -> Completed
            "cancelled", "canceled", "cancelled_by_customer", "cancelled_by_performer" -> Cancelled
            "disputed" -> Disputed
            else -> Open
        }
    }
}

data class TaskBudgetProjection(
    val min: Int?,
    val max: Int?,
    val quickOfferPrice: Int?,
)

data class TaskVisibilityProjection(
    val isVisibleOnMap: Boolean,
    val isOpenForOffers: Boolean,
    val customerCanSeeRoute: Boolean,
    val chatShouldRemainTaskBound: Boolean,
)

data class TaskStateProjection(
    val schemaVersion: Int,
    val lifecycleStatus: TaskLifecycleStatus,
    val executionStatus: TaskExecutionStatus,
    val acceptedConfirmed: Boolean,
    val budget: TaskBudgetProjection,
    val visibility: TaskVisibilityProjection,
    val isDeleted: Boolean,
)
