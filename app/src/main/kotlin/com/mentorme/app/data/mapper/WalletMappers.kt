package com.mentorme.app.data.mapper

import com.mentorme.app.data.dto.wallet.WalletTransactionDto
import com.mentorme.app.ui.profile.TxStatus
import com.mentorme.app.ui.profile.TxType
import com.mentorme.app.ui.profile.WalletTx
import java.time.Instant

fun WalletTransactionDto.toUi(): WalletTx {
    val signedAmount = when (source) {
        "MANUAL_TOPUP",
        "BOOKING_REFUND" -> amount
        "MANUAL_WITHDRAW",
        "BOOKING_PAYMENT" -> -amount
        else -> amount
    }

    val txType = when (source) {
        "MANUAL_TOPUP" -> TxType.TOP_UP
        "MANUAL_WITHDRAW" -> TxType.WITHDRAW
        "BOOKING_PAYMENT" -> TxType.PAYMENT
        "BOOKING_REFUND" -> TxType.REFUND
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


