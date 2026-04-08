package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoPointDto(
    val lat: Double,
    val lon: Double,
)

@Serializable
data class CurrentUserProfileDto(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class UserProfileSectionDto(
    @SerialName("display_name") val displayName: String? = null,
    val bio: String? = null,
    val city: String? = null,
    @SerialName("preferred_address") val preferredAddress: String? = null,
    @SerialName("home_location") val homeLocation: GeoPointDto? = null,
)

@Serializable
data class UserStatsDto(
    @SerialName("rating_avg") val ratingAverage: Double,
    @SerialName("rating_count") val ratingCount: Int,
    @SerialName("completed_count") val completedCount: Int,
    @SerialName("cancelled_count") val cancelledCount: Int,
)

@Serializable
data class MeProfileResponseDto(
    val user: CurrentUserProfileDto,
    val profile: UserProfileSectionDto,
    val stats: UserStatsDto,
)

@Serializable
data class UpdateMyProfileRequestDto(
    @SerialName("display_name") val displayName: String,
    val bio: String? = null,
    val city: String? = null,
    @SerialName("preferred_address") val preferredAddress: String? = null,
    @SerialName("home_location") val homeLocation: GeoPointDto? = null,
)

@Serializable
data class MyReviewsResponseDto(
    val items: List<UserProfileReviewDto> = emptyList(),
)

@Serializable
data class UserProfileReviewDto(
    val id: String? = null,
    @SerialName("from_user_display_name") val fromUserDisplayName: String? = null,
    val stars: Int,
    val text: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("target_role") val targetRole: String? = null,
)

@Serializable
data class ReviewSummaryDto(
    val average: Double,
    val count: Int,
)

@Serializable
data class ReviewsFeedResponseDto(
    val items: List<UserProfileReviewDto> = emptyList(),
    @SerialName("selected_role") val selectedRole: String,
    val summary: ReviewSummaryDto,
)

@Serializable
data class ReviewEligibilityDto(
    @SerialName("can_submit") val canSubmit: Boolean,
    @SerialName("already_submitted") val alreadySubmitted: Boolean,
    @SerialName("announcement_id") val announcementId: String,
    @SerialName("announcement_title") val announcementTitle: String? = null,
    @SerialName("thread_id") val threadId: String? = null,
    @SerialName("counterpart_user_id") val counterpartUserId: String? = null,
    @SerialName("counterpart_display_name") val counterpartDisplayName: String? = null,
    @SerialName("counterpart_role") val counterpartRole: String? = null,
    val message: String? = null,
)

@Serializable
data class SubmitReviewRequestDto(
    val stars: Int,
    val text: String? = null,
)

@Serializable
data class DeviceRegistrationRequestDto(
    @SerialName("device_id") val deviceId: String,
    val platform: String,
    @SerialName("push_token") val pushToken: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
    @SerialName("device_name") val deviceName: String? = null,
)

@Serializable
data class UnregisterDeviceRequestDto(
    @SerialName("device_id") val deviceId: String,
    @SerialName("push_token") val pushToken: String? = null,
)
