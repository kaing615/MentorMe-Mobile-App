package com.mentorme.app.data.mapper

import com.mentorme.app.data.dto.wallet.WalletTransactionDto
import com.mentorme.app.ui.profile.TxStatus
import com.mentorme.app.ui.profile.TxType
import com.mentorme.app.ui.profile.WalletTx
import java.time.Instant

fun WalletTransactionDto.toUi(): WalletTx {
    val signedAmount = when (type) {
        "CREDIT", "REFUND" -> this.amount
        else -> -this.amount
    }

    val txType = when (source) {
        "MANUAL_TOPUP" -> TxType.TOP_UP
        "MANUAL_WITHDRAW", "MENTOR_PAYOUT" -> TxType.WITHDRAW
        "BOOKING_PAYMENT" -> TxType.PAYMENT
        "BOOKING_REFUND", "MENTOR_PAYOUT_REFUND" -> TxType.REFUND
        else -> TxType.PAYMENT
    }

    return WalletTx(
        id = id,
        amount = signedAmount,
        type = txType,
        status = TxStatus.SUCCESS,
        note = source.replace("_", " ").lowercase()
            .replaceFirstChar { it.titlecase() },
        date = Instant.parse(createdAt).toEpochMilli()
    )
}

