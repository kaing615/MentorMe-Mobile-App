package com.mentorme.app.data.repository.chat

import android.net.Uri
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.model.chat.ChatRestrictionInfo
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.data.network.api.chat.FileUploadResponse

interface ChatRepository {
    suspend fun getConversations(): AppResult<List<Conversation>>
    suspend fun getConversationByPeerId(peerId: String): AppResult<Conversation?>
    suspend fun getMessages(conversationId: String, limit: Int = 50): AppResult<List<Message>>
    suspend fun sendMessage(conversationId: String, text: String, messageType: String = "text"): AppResult<Message>
    suspend fun getChatRestrictionInfo(conversationId: String): AppResult<ChatRestrictionInfo>
    suspend fun getOnlinePeerIds(peerIds: List<String>): AppResult<Set<String>>
    suspend fun uploadFile(conversationId: String, fileUri: Uri, fileName: String): AppResult<FileUploadResponse>
}
