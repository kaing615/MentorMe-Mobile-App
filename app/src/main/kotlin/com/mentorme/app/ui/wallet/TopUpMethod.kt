package com.mentorme.app.ui.wallet

fun TopUpMethod.qrAsset(): String = when (this) {
    TopUpMethod.MoMo    -> "qr/qr_momo.jpg"
    TopUpMethod.ZaloPay -> "qr/qr_zalo.jpg"
    TopUpMethod.Bank   -> "qr/qr_bank.jpg"
}
