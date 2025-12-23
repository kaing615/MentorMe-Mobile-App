package com.mentorme.app.data.dto.notifications

import com.google.gson.annotations.SerializedName

data class RegisterDeviceRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("platform")
    val platform: String? = "android",
    @SerializedName("deviceId")
    val deviceId: String? = null
)

data class UnregisterDeviceRequest(
    @SerializedName("token")
    val token: String? = null
)

data class DeviceTokenPayload(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("token")
    val token: String? = null
)

data class UnregisterResult(
    @SerializedName("removed")
    val removed: Int? = null
)
