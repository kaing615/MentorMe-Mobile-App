package com.mentorme.app.data.model

import java.util.UUID

enum class NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_REMINDER,
    BOOKING_CANCELLED,
    BOOKING_PENDING,
    BOOKING_DECLINED,
    BOOKING_FAILED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    MESSAGE,
    TOPUP_SUCCESS,
    TOPUP_REJECTED,
    SYSTEM;

    companion object {
        fun fromKey(value: String?): NotificationType {
            return when (value?.lowercase()) {
                "booking_confirmed" -> BOOKING_CONFIRMED
                "booking_reminder" -> BOOKING_REMINDER
                "booking_cancelled" -> BOOKING_CANCELLED
                "booking_pending" -> BOOKING_PENDING
                "booking_declined" -> BOOKING_DECLINED
                "booking_failed" -> BOOKING_FAILED
                "payment_success" -> PAYMENT_SUCCESS
                "payment_failed" -> PAYMENT_FAILED
                "message" -> MESSAGE
                "topup_success" -> TOPUP_SUCCESS
                else -> SYSTEM
            }
        }
    }
}

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val type: NotificationType,
    val timestamp: Long,
    val read: Boolean = false,
    val deepLink: String? = null
)

data class NotificationPreferences(
    val pushBooking: Boolean = true,
    val pushPayment: Boolean = true,
    val pushMessage: Boolean = true,
    val pushSystem: Boolean = true
) {
    fun isPushEnabled(type: NotificationType): Boolean {
        return when (type) {
            NotificationType.BOOKING_CONFIRMED,
            NotificationType.BOOKING_REMINDER,
            NotificationType.BOOKING_CANCELLED,
            NotificationType.BOOKING_PENDING,
            NotificationType.BOOKING_DECLINED,
            NotificationType.BOOKING_FAILED -> pushBooking
            NotificationType.PAYMENT_SUCCESS,
            NotificationType.PAYMENT_FAILED -> pushPayment
            NotificationType.MESSAGE -> pushMessage
            NotificationType.TOPUP_SUCCESS,
            NotificationType.TOPUP_REJECTED,
            NotificationType.SYSTEM -> pushSystem
        }
    }
}
