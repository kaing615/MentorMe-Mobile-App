package com.mentorme.app.data.mapper

import com.mentorme.app.data.dto.paymentMethods.PaymentMethodDto
import com.mentorme.app.ui.wallet.PayProvider
import com.mentorme.app.ui.wallet.PaymentMethod

fun PaymentMethodDto.toDomain(): PaymentMethod {
    val payProvider = PayProvider.fromApi(provider)

    return PaymentMethod(
        id = id,
        provider = payProvider,
        label = accountName ?: provider,
        detail = accountNumberMasked ?: "",
        isDefault = isDefault
    )
}


