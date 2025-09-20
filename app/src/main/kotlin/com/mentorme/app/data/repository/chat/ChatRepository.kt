package com.mentorme.app.data.repository.chat

import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message

interface ChatRepository {
    fun getConversations(): List<Conversation>
    fun getMessages(conversationId: String): List<Message>
    fun sendMessage(conversationId: String, text: String): Message
}
