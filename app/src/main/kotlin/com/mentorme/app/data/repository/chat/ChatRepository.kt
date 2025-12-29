package com.mentorme.app.data.repository.chat

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.model.chat.ChatRestrictionInfo
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message

interface ChatRepository {
    suspend fun getConversations(): AppResult<List<Conversation>>
    suspend fun getMessages(conversationId: String, limit: Int = 50): AppResult<List<Message>>
    suspend fun sendMessage(conversationId: String, text: String): AppResult<Message>
    suspend fun getChatRestrictionInfo(conversationId: String): AppResult<ChatRestrictionInfo>
}
