package com.mentorme.app.ui.chat.ai

import com.mentorme.app.data.dto.mentors.MentorCardDto
import com.mentorme.app.data.dto.ai.AiAnalysis

sealed class AiChatMessage {
    data class User(
        val text: String
    ) : AiChatMessage()

    data class Ai(
        val text: String,
        val type: AiResponseType,
        val mentors: List<MentorCardDto> = emptyList(),
        val aiAnalysis: AiAnalysis? = null,
        val suggestions: List<String> = emptyList()
    ) : AiChatMessage()
}

enum class AiResponseType {
    MENTOR_RECOMMEND,  // AI analyzed and recommended mentors
    APP_QA,            // Answer to app-related questions
    GENERAL            // Greetings, farewells, etc.
}
