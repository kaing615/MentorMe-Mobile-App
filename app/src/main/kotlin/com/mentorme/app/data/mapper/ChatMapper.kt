package com.mentorme.app.data.mapper

import com.mentorme.app.data.model.chat.Message

data class ChatSocketPayload(
    val id: String? = null,
    val bookingId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val content: String? = null,
    val messageType: String? = null,
    val timestamp: String? = null
)

fun ChatSocketPayload.toChatMessage(currentUserId: String?): Message? {
    val messageId = id?.trim().orEmpty()
    val conversationId = bookingId?.trim().orEmpty()
    val text = content?.trim().orEmpty()
    if (messageId.isBlank() || conversationId.isBlank() || text.isBlank()) return null

    return Message(
        id = messageId,
        conversationId = conversationId,
        text = text,
        createdAtIso = timestamp ?: "",
        fromCurrentUser = senderId == currentUserId
    )
}
