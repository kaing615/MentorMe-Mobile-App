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
    private fun historyFile(historyKey: String): File {
        val key = historyKey.trim().lowercase()
        return if (key == DEFAULT_HISTORY_KEY) {
            File(context.filesDir, "ai_chat_history.json")
        } else {
            File(context.filesDir, "ai_chat_history_${key}.json")
        }
    }

    suspend fun saveMessages(messages: List<AiChatMessage>, historyKey: String = DEFAULT_HISTORY_KEY) {
        withContext(Dispatchers.IO) {
            try {
                val file = historyFile(historyKey)
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
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun loadMessages(historyKey: String = DEFAULT_HISTORY_KEY): List<AiChatMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val file = historyFile(historyKey)
                if (!file.exists()) return@withContext emptyList()

                val json = file.readText()
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

    suspend fun clearHistory(historyKey: String = DEFAULT_HISTORY_KEY) {
        withContext(Dispatchers.IO) {
            try {
                val file = historyFile(historyKey)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private companion object {
        private const val DEFAULT_HISTORY_KEY = "mentee"
    }
}
