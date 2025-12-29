package com.mentorme.app.data.model.chat

import com.mentorme.app.data.model.BookingStatus

data class Conversation(
    val id: String,
    val peerId: String,
    val peerName: String,
    val peerAvatar: String? = null,
    val peerRole: String = "mentor",
    val isOnline: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTimeIso: String = "",
    val unreadCount: Int = 0,
    val hasActiveSession: Boolean = false,
    val nextSessionDateTimeIso: String? = null,
    val nextSessionStartIso: String? = null,
    val nextSessionEndIso: String? = null,
    val nextSessionBookingId: String? = null,
    val bookingStatus: BookingStatus = BookingStatus.PAYMENT_PENDING,
    val myMessageCount: Int = 0,
    val sessionPhase: String = "outside", // pre, during, post, outside
    val preSessionCount: Int = 0,
    val postSessionCount: Int = 0,
    val weeklyMessageCount: Int = 0
)
