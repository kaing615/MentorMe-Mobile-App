// File: app/src/main/kotlin/com/mentorme/app/data/repository/ai/AiChatHistoryManager.kt
package com.mentorme.app.data.repository.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mentorme.app.ui.chat.ai.AiChatMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatHistoryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val historyFile = File(context.filesDir, "ai_chat_history.json")

    suspend fun saveMessages(messages: List<AiChatMessage>) {
        withContext(Dispatchers.IO) {
            try {
                // Convert messages to serializable format
                val serializableList = messages.map { msg ->
                    when (msg) {
                        is AiChatMessage.User -> {
                            mapOf(
                                "type" to "user",
                                "text" to msg.text
                            )
                        }
                        is AiChatMessage.Ai -> {
                            mapOf(
                                "type" to "ai",
                                "text" to msg.text,
                                "messageType" to msg.type,
                                "mentors" to msg.mentors
                            )
                        }
                    }
                }

                val json = gson.toJson(serializableList)
                historyFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun loadMessages(): List<AiChatMessage> {
        return withContext(Dispatchers.IO) {
            try {
                if (!historyFile.exists()) return@withContext emptyList()

                val json = historyFile.readText()
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(json, type)

                list.mapNotNull { map ->
                    when (map["type"]) {
                        "user" -> AiChatMessage.User(map["text"] as? String ?: "")
                        "ai" -> {
                            // Parse mentors if exists
                            val mentorsJson = gson.toJson(map["mentors"])
                            val mentorsType = object : TypeToken<List<com.mentorme.app.data.dto.ai.MentorWithExplanation>>() {}.type
                            val mentors: List<com.mentorme.app.data.dto.ai.MentorWithExplanation> =
                                try {
                                    gson.fromJson(mentorsJson, mentorsType)
                                } catch (e: Exception) {
                                    emptyList()
                                }

                            AiChatMessage.Ai(
                                text = map["text"] as? String ?: "",
                                mentors = mentors,
                                type = map["messageType"] as? String ?: "text"
                            )
                        }
                        else -> null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            try {
                if (historyFile.exists()) {
                    historyFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}