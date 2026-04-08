package com.vzaimno.app.core.model

enum class OfferPricingMode(val rawValue: String) {
    QuickMinPrice("quick_min_price"),
    CounterPrice("counter_price"),
    AgreedPrice("agreed_price"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): OfferPricingMode = entries.firstOrNull {
            it.rawValue == rawValue?.trim()?.lowercase()
        } ?: CounterPrice
    }
}

enum class AnnouncementOfferStatus(val rawValue: String) {
    Pending("pending"),
    Accepted("accepted"),
    Rejected("rejected"),
    Withdrawn("withdrawn"),
    Expired("expired"),
    Blocked("blocked"),
    ;

    companion object {
        fun fromRaw(rawValue: String?): AnnouncementOfferStatus? = entries.firstOrNull {
            it.rawValue == rawValue?.trim()?.lowercase()
        }
    }
}

data class OfferPerformer(
    val userId: String,
    val displayName: String,
    val city: String?,
    val contact: String?,
    val avatarUrl: String?,
)

data class AnnouncementOffer(
    val id: String,
    val announcementId: String,
    val performerId: String,
    val message: String?,
    val proposedPrice: Int?,
    val agreedPrice: Int?,
    val pricingMode: OfferPricingMode,
    val minimumPriceAccepted: Boolean,
    val canReoffer: Boolean,
    val status: String,
    val createdAtEpochSeconds: Long,
    val performer: OfferPerformer?,
    val performerStats: ProfileStats?,
) {
    val statusValue: AnnouncementOfferStatus?
        get() = AnnouncementOfferStatus.fromRaw(status)
}

data class AcceptedOfferResult(
    val threadId: String,
    val offer: AnnouncementOffer,
)
