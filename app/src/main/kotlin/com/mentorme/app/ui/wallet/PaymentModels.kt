package com.mentorme.app.ui.wallet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
enum class PayProvider : Parcelable { MOMO, ZALOPAY, BANK }

@Parcelize
data class PaymentMethod(
    val id: String,
    val provider: PayProvider,
    val label: String,
    val detail: String,
    val isDefault: Boolean = false
) : Parcelable

fun initialPaymentMethods(): List<PaymentMethod> = emptyList()

fun mockPaymentMethods(): List<PaymentMethod> = listOf(
    PaymentMethod("pm1", PayProvider.MOMO,    "MoMo",        "0987•••123", isDefault = true),
    PaymentMethod("pm2", PayProvider.ZALOPAY, "ZaloPay",     "0978•••456"),
    PaymentMethod("pm3", PayProvider.BANK,    "Vietcombank", "0123•••89")
)