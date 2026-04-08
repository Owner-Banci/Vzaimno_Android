package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.common.parseInstant
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.EditableProfileFields
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.ProfileStats
import com.vzaimno.app.core.model.ReviewEligibility
import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.core.model.ReviewSummary
import com.vzaimno.app.core.model.UserProfile
import com.vzaimno.app.core.model.UserProfileReview
import com.vzaimno.app.core.model.UserReviewFeed
import com.vzaimno.app.data.remote.dto.CurrentUserProfileDto
import com.vzaimno.app.data.remote.dto.GeoPointDto
import com.vzaimno.app.data.remote.dto.MeProfileResponseDto
import com.vzaimno.app.data.remote.dto.ReviewEligibilityDto
import com.vzaimno.app.data.remote.dto.ReviewSummaryDto
import com.vzaimno.app.data.remote.dto.ReviewsFeedResponseDto
import com.vzaimno.app.data.remote.dto.UpdateMyProfileRequestDto
import com.vzaimno.app.data.remote.dto.UserProfileReviewDto
import com.vzaimno.app.data.remote.dto.UserProfileSectionDto
import com.vzaimno.app.data.remote.dto.UserStatsDto

fun GeoPointDto.toDomain(): GeoPoint = GeoPoint(
    latitude = lat,
    longitude = lon,
)

fun GeoPoint.toDto(): GeoPointDto = GeoPointDto(
    lat = latitude,
    lon = longitude,
)

fun UserStatsDto.toDomain(): ProfileStats = ProfileStats(
    ratingAverage = ratingAverage,
    ratingCount = ratingCount,
    completedCount = completedCount,
    cancelledCount = cancelledCount,
)

fun UserProfileSectionDto.toEditableFields(currentUser: CurrentUserProfileDto? = null): EditableProfileFields = EditableProfileFields(
    displayName = displayName.trimmedOrNull() ?: currentUser?.email.orEmpty(),
    bio = bio?.trim().orEmpty(),
    city = city?.trim().orEmpty(),
    preferredAddress = preferredAddress?.trim().orEmpty(),
    homeLocation = homeLocation?.toDomain(),
)

fun MeProfileResponseDto.toDomain(): UserProfile = UserProfile(
    userId = user.id,
    email = user.email?.trimmedOrNull(),
    phone = user.phone?.trimmedOrNull(),
    displayName = profile.displayName?.trimmedOrNull(),
    bio = profile.bio?.trim().orEmpty(),
    city = profile.city?.trim().orEmpty(),
    preferredAddress = profile.preferredAddress?.trim().orEmpty(),
    homeLocation = profile.homeLocation?.toDomain(),
    stats = stats.toDomain(),
    createdAt = parseInstant(user.createdAt),
)

fun EditableProfileFields.toDto(): UpdateMyProfileRequestDto = UpdateMyProfileRequestDto(
    displayName = displayName,
    bio = bio.trimmedOrNull(),
    city = city.trimmedOrNull(),
    preferredAddress = preferredAddress.trimmedOrNull(),
    homeLocation = homeLocation?.toDto(),
)

fun UserProfileReviewDto.toDomain(): UserProfileReview {
    val authorName = fromUserDisplayName.trimmedOrNull() ?: "Пользователь"
    val textValue = text.trimmedOrNull() ?: "Без текста"
    val createdAtInstant = parseInstant(createdAt) ?: java.time.Instant.EPOCH
    val safeId = id ?: "$authorName|$createdAt|$textValue|$stars"

    return UserProfileReview(
        id = safeId,
        authorName = authorName,
        stars = stars.coerceIn(0, 5),
        text = textValue,
        createdAt = createdAtInstant,
        targetRole = ReviewRole.fromRaw(targetRole),
    )
}

fun ReviewSummaryDto.toDomain(): ReviewSummary = ReviewSummary(
    average = average,
    count = count,
)

fun ReviewsFeedResponseDto.toDomain(fallbackRole: ReviewRole): UserReviewFeed = UserReviewFeed(
    role = ReviewRole.fromRaw(selectedRole) ?: fallbackRole,
    summary = summary.toDomain(),
    reviews = items.map(UserProfileReviewDto::toDomain),
)

fun ReviewEligibilityDto.toDomain(): ReviewEligibility = ReviewEligibility(
    announcementId = announcementId,
    announcementTitle = announcementTitle.trimmedOrNull(),
    threadId = threadId.trimmedOrNull(),
    counterpartUserId = counterpartUserId.trimmedOrNull(),
    counterpartDisplayName = counterpartDisplayName.trimmedOrNull(),
    counterpartRole = ReviewRole.fromRaw(counterpartRole),
    canSubmit = canSubmit,
    alreadySubmitted = alreadySubmitted,
    message = message.trimmedOrNull(),
)
