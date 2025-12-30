package com.mentorme.app.data.dto.wallet

import com.google.gson.annotations.SerializedName

data class WalletDto(
    @SerializedName("walletId")
    val id: String,
    @SerializedName("balanceMinor")
    val balanceMinor: Long,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("transactions")
    val transactions: List<WalletTransactionDto> = emptyList()
)

data class WalletTransactionDto(
    @SerializedName("id") val id: String,

    @SerializedName("type") val type: String,       // CREDIT | DEBIT | REFUND
    @SerializedName("source") val source: String,   // MANUAL_TOPUP | BOOKING_PAYMENT | ...

    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,

    @SerializedName("balanceBeforeMinor") val balanceBeforeMinor: Long,
    @SerializedName("balanceAfterMinor") val balanceAfterMinor: Long,

    @SerializedName("referenceType") val referenceType: String?,
    @SerializedName("referenceId") val referenceId: String?,

    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

// Request / Response for topup
data class TopupRequest(
    val amount: Long,
    val currency: String = "VND",
    val clientRequestId: String
)

data class TopupResponse(
    val idempotent: Boolean,
    val wallet: WalletDto?,
    val transaction: WalletTransactionDto?
)

data class DebitRequest(
    val amount: Long,
    val currency: String = "VND",
    val clientRequestId: String,
    val paymentMethodId: String? = null
)

data class DebitResponse(
    val idempotent: Boolean,
    val wallet: WalletDto?,
    val transaction: WalletTransactionDto?
)

data class WalletTransactionListResponse(
    val items: List<WalletTransactionDto>,
    val nextCursor: String?
)