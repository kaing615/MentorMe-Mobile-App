package com.mentorme.app.ui.chat.ai

import com.mentorme.app.data.dto.ai.MentorWithExplanation

sealed class AiChatMessage {
    data class User(
        val text: String
    ) : AiChatMessage()

    data class Ai(
        val text: String,
        val mentors: List<MentorWithExplanation> = emptyList(),
        val type: String = "text" // "text" | "mentor_recommend" | "app_qa"
    ) : AiChatMessage()
}
