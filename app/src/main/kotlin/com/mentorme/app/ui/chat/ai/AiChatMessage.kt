package com.mentorme.app.ui.chat.ai

import com.mentorme.app.data.dto.mentors.MentorCardDto

sealed class AiChatMessage {
    data class User(
        val text: String
    ) : AiChatMessage()

    data class Ai(
        val text: String,
        val mentors: List<MentorCardDto>
    ) : AiChatMessage()
}
