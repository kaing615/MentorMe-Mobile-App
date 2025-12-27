package com.mentorme.app.core.notifications

object NotificationDeepLink {
    const val ROUTE_NOTIFICATIONS = "notifications"

    fun routeFor(data: Map<String, *>?): String {
        val bookingId = data?.get("bookingId") ?: data?.get("booking_id")
        val id = bookingId?.toString()?.trim()
        return if (!id.isNullOrBlank()) {
            "booking_detail/$id"
        } else {
            ROUTE_NOTIFICATIONS
        }
    }
}
