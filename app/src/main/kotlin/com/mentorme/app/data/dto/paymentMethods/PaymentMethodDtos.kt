package com.mentorme.app.data.dto.paymentMethods

import com.google.gson.annotations.SerializedName

data class PaymentMethodDto(
    @SerializedName("_id")
    val id: String,

    val provider: String,
    val accountName: String?,
    val accountNumberMasked: String?,
    val isDefault: Boolean
)


data class AddPaymentMethodRequest(
    val type: String,           // "BANK" | "EWALLET"
    val provider: String,       // "MOMO", "ZALOPAY" or bank name
    val accountName: String,    // tên chủ tài khoản / tên ví
    val accountNumber: String,  // số tài khoản / số điện thoại
    val isDefault: Boolean? = false
)

data class UpdatePaymentMethodRequest(
    val accountName: String? = null,
    val accountNumber: String? = null,
    val isDefault: Boolean? = null
)
