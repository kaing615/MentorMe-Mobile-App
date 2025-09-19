package com.mentorme.app.data.model.chat

data class Message(
    val id: String,
    val conversationId: String,
    val text: String,
    val createdAtIso: String,
    val fromCurrentUser: Boolean
)
