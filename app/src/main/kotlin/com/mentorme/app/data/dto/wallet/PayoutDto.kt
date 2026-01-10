package com.mentorme.app.data.dto.wallet

data class MentorPayoutDto(
    val id: String,
    val amount: Long,
    val currency: String,
    val status: PayoutStatus,
    val attemptCount: Int,
    val externalId: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreatePayoutRequest(
    val amount: Long,
    val currency: String = "VND",
    val clientRequestId: String
)

data class PayoutListResponse(
    val items: List<MentorPayoutDto>,
    val nextCursor: String?
)

enum class PayoutStatus { PENDING, PROCESSING, PAID, FAILED }
