package com.mentorme.app.data.model.chat

import com.mentorme.app.data.model.BookingStatus

data class Conversation(
    val id: String, // Now using peerId instead of bookingId
    val peerId: String,
    val primaryBookingId: String? = null, // For restriction info and sending messages
    val peerName: String,
    val peerAvatar: String? = null,
    val peerRole: String = "mentor",
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
    val lastMessage: String = "",
    val lastMessageTimeIso: String = "",
    val unreadCount: Int = 0,
    val hasActiveSession: Boolean = false,
    val activeSessionBookingId: String? = null,
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
