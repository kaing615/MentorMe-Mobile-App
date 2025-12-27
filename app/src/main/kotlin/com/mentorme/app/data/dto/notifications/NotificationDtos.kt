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

data class NotificationDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("data")
    val data: Map<String, Any?>? = null,
    @SerializedName("read")
    val read: Boolean? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class NotificationListPayload(
    @SerializedName("items")
    val items: List<NotificationDto>? = null,
    @SerializedName("total")
    val total: Int? = null,
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("limit")
    val limit: Int? = null
)

data class NotificationUnreadCount(
    @SerializedName("unread")
    val unread: Int? = null
)

data class NotificationReadResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("read")
    val read: Boolean? = null
)

data class NotificationReadAllResponse(
    @SerializedName("updated")
    val updated: Int? = null
)

data class NotificationPreferencesDto(
    @SerializedName("pushBooking")
    val pushBooking: Boolean? = null,
    @SerializedName("pushPayment")
    val pushPayment: Boolean? = null,
    @SerializedName("pushMessage")
    val pushMessage: Boolean? = null,
    @SerializedName("pushSystem")
    val pushSystem: Boolean? = null
)
