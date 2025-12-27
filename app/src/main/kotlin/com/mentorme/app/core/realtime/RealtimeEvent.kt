package com.mentorme.app.core.realtime

import com.mentorme.app.data.mapper.NotificationSocketPayload
import com.mentorme.app.data.model.NotificationItem

sealed class RealtimeEvent {
    data class NotificationReceived(
        val notification: NotificationItem,
        val payload: NotificationSocketPayload
    ) : RealtimeEvent()

    data class BookingChanged(val bookingId: String) : RealtimeEvent()
}
