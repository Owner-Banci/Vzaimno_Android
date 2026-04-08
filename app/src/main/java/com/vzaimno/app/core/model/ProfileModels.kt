package com.vzaimno.app.core.model

import com.vzaimno.app.core.common.trimmedOrNull
import java.time.Instant

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class ProfileStats(
    val ratingAverage: Double,
    val ratingCount: Int,
    val completedCount: Int,
    val cancelledCount: Int,
)

enum class ReviewRole(val rawValue: String) {
    Performer("performer"),
    Customer("customer"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): ReviewRole? = entries.firstOrNull {
            it.rawValue == rawValue?.trim()?.lowercase()
        }
    }
}

data class ReviewSummary(
    val average: Double,
    val count: Int,
) {
    companion object {
        val Empty = ReviewSummary(average = 0.0, count = 0)
    }
}

data class UserReviewFeed(
    val role: ReviewRole,
    val summary: ReviewSummary,
    val reviews: List<UserProfileReview>,
)

data class ReviewEligibility(
    val announcementId: String,
    val announcementTitle: String?,
    val threadId: String?,
    val counterpartUserId: String?,
    val counterpartDisplayName: String?,
    val counterpartRole: ReviewRole?,
    val canSubmit: Boolean,
    val alreadySubmitted: Boolean,
    val message: String?,
)

data class EditableProfileFields(
    val displayName: String,
    val bio: String,
    val city: String,
    val preferredAddress: String,
    val homeLocation: GeoPoint?,
)

data class UserProfile(
    val userId: String,
    val email: String?,
    val phone: String?,
    val displayName: String?,
    val bio: String,
    val city: String,
    val preferredAddress: String,
    val homeLocation: GeoPoint?,
    val stats: ProfileStats,
    val createdAt: Instant?,
) {
    val resolvedDisplayName: String
        get() = displayName.trimmedOrNull() ?: primaryContact ?: "Пользователь"

    val primaryContact: String?
        get() = phone.trimmedOrNull() ?: email.trimmedOrNull()

    val editableFields: EditableProfileFields
        get() = EditableProfileFields(
            displayName = displayName.trimmedOrNull() ?: primaryContact.orEmpty(),
            bio = bio,
            city = city,
            preferredAddress = preferredAddress,
            homeLocation = homeLocation,
        )
}

data class UserProfileReview(
    val id: String,
    val authorName: String,
    val stars: Int,
    val text: String,
    val createdAt: Instant,
    val targetRole: ReviewRole?,
)
