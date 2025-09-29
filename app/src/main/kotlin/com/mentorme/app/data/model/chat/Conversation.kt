package com.mentorme.app.data.model.chat

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
    val nextSessionDateTimeIso: String? = null
)
