package com.vzaimno.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOfferRequestDto(
    val message: String? = null,
    @SerialName("proposed_price") val proposedPrice: Int? = null,
    @SerialName("pricing_mode") val pricingMode: String? = null,
    @SerialName("agreed_price") val agreedPrice: Int? = null,
    @SerialName("minimum_price_accepted") val minimumPriceAccepted: Boolean? = null,
)

@Serializable
data class OfferPerformerProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    val city: String? = null,
    val contact: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class OfferPerformerStatsDto(
    @SerialName("rating_avg") val ratingAverage: Double,
    @SerialName("rating_count") val ratingCount: Int,
    @SerialName("completed_count") val completedCount: Int,
    @SerialName("cancelled_count") val cancelledCount: Int,
)

@Serializable
data class AnnouncementOfferDto(
    val id: String,
    @SerialName("announcement_id") val announcementId: String,
    @SerialName("performer_id") val performerId: String,
    val message: String? = null,
    @SerialName("proposed_price") val proposedPrice: Int? = null,
    @SerialName("agreed_price") val agreedPrice: Int? = null,
    @SerialName("pricing_mode") val pricingMode: String? = null,
    @SerialName("minimum_price_accepted") val minimumPriceAccepted: Boolean? = null,
    @SerialName("can_reoffer") val canReoffer: Boolean? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("performer_profile") val performerProfile: OfferPerformerProfileDto? = null,
    @SerialName("performer_stats") val performerStats: OfferPerformerStatsDto? = null,
)

@Serializable
data class AcceptOfferResponseDto(
    @SerialName("thread_id") val threadId: String,
    val offer: AnnouncementOfferDto,
)
