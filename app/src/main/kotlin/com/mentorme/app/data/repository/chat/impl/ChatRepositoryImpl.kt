package com.mentorme.app.data.repository.chat.impl

import android.os.Build
import androidx.annotation.RequiresApi
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.data.repository.chat.ChatRepository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
class ChatRepositoryImpl : ChatRepository {

    // In-memory
    private val conversations = mutableListOf<Conversation>()
    private val messages = mutableListOf<Message>()

    init {
        // Seed từ MockData (mentor → 1 conversation)
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        MockData.mockMentors.take(5).forEachIndexed { idx, m ->
            val convId = "conv-${m.id}"
            val hasActive = Random.nextBoolean()
            val nextSessionIso = if (hasActive) now.plusMinutes(30).toString() else null

            val conv = Conversation(
                id = convId,
                peerId = m.id,
                peerName = m.fullName,
                peerAvatar = m.avatar,
                peerRole = "mentor",
                isOnline = Random.nextBoolean(),
                lastMessage = "Hi ${m.fullName.split(' ').last()}, I’m interested in your field.",
                lastMessageTimeIso = now.minusMinutes((idx * 7).toLong()).toString(),
                unreadCount = if (idx % 2 == 0) Random.nextInt(0, 4) else 0,
                hasActiveSession = hasActive,
                nextSessionDateTimeIso = nextSessionIso
            )
            conversations += conv

            // Seed 6 message mỗi hội thoại
            repeat(6) { i ->
                val fromMe = i % 2 == 0
                messages += Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = convId,
                    text = if (fromMe) "Hello ${m.fullName.split(' ').last()}!" else "Hello! How can I help you?",
                    createdAtIso = now.minusMinutes((i * 3L)).toString(),
                    fromCurrentUser = fromMe
                )
            }
        }
    }

    override fun getConversations(): List<Conversation> {
        return conversations
            .sortedByDescending { it.lastMessageTimeIso }
    }

    override fun getMessages(conversationId: String): List<Message> {
        return messages
            .filter { it.conversationId == conversationId }
            .sortedBy { it.createdAtIso }
    }

    override fun sendMessage(conversationId: String, text: String): Message {
        val msg = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = text,
            createdAtIso = OffsetDateTime.now(ZoneOffset.UTC).toString(),
            fromCurrentUser = true
        )
        messages += msg

        // update last message
        val idx = conversations.indexOfFirst { it.id == conversationId }
        if (idx >= 0) {
            val c = conversations[idx]
            conversations[idx] = c.copy(
                lastMessage = text,
                lastMessageTimeIso = msg.createdAtIso,
                unreadCount = 0
            )
        }
        return msg
    }
}
