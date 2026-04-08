package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.common.parseInstant
import com.vzaimno.app.core.common.resolveAgainstBaseUrl
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.model.AcceptedOfferResult
import com.vzaimno.app.core.model.AnnouncementOffer
import com.vzaimno.app.core.model.OfferPerformer
import com.vzaimno.app.core.model.OfferPricingMode
import com.vzaimno.app.core.model.ProfileStats
import com.vzaimno.app.data.remote.dto.AcceptOfferResponseDto
import com.vzaimno.app.data.remote.dto.AnnouncementOfferDto
import com.vzaimno.app.data.remote.dto.OfferPerformerProfileDto
import com.vzaimno.app.data.remote.dto.OfferPerformerStatsDto

fun OfferPerformerProfileDto.toDomain(apiBaseUrl: String): OfferPerformer = OfferPerformer(
    userId = userId,
    displayName = displayName.trimmedOrNull() ?: "Пользователь",
    city = city.trimmedOrNull(),
    contact = contact.trimmedOrNull(),
    avatarUrl = resolveAgainstBaseUrl(apiBaseUrl, avatarUrl),
)

fun OfferPerformerStatsDto.toDomain(): ProfileStats = ProfileStats(
    ratingAverage = ratingAverage,
    ratingCount = ratingCount,
    completedCount = completedCount,
    cancelledCount = cancelledCount,
)

fun AnnouncementOfferDto.toDomain(apiBaseUrl: String): AnnouncementOffer = AnnouncementOffer(
    id = id,
    announcementId = announcementId,
    performerId = performerId,
    message = message.trimmedOrNull(),
    proposedPrice = proposedPrice,
    agreedPrice = agreedPrice,
    pricingMode = OfferPricingMode.fromRaw(pricingMode),
    minimumPriceAccepted = minimumPriceAccepted ?: false,
    canReoffer = canReoffer ?: true,
    status = status,
    createdAtEpochSeconds = parseInstant(createdAt)?.epochSecond ?: 0L,
    performer = performerProfile?.toDomain(apiBaseUrl),
    performerStats = performerStats?.toDomain(),
)

fun AcceptOfferResponseDto.toDomain(apiBaseUrl: String): AcceptedOfferResult = AcceptedOfferResult(
    threadId = threadId,
    offer = offer.toDomain(apiBaseUrl),
)
