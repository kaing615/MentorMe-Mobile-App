package com.mentorme.app.data.model

import java.util.UUID

enum class NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_REMINDER,
    BOOKING_CANCELLED,
    MESSAGE,
    SYSTEM;

    companion object {
        fun fromKey(value: String?): NotificationType {
            return when (value?.lowercase()) {
                "booking_confirmed" -> BOOKING_CONFIRMED
                "booking_reminder" -> BOOKING_REMINDER
                "booking_cancelled" -> BOOKING_CANCELLED
                "message" -> MESSAGE
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
