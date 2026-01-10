package com.mentorme.app.data.repository.ai

enum class AiChatMode(val id: String) {
    MENTEE("mentee"),
    MENTOR("mentor");

    companion object {
        fun fromRouteArg(value: String?): AiChatMode {
            return entries.firstOrNull { it.id.equals(value, ignoreCase = true) } ?: MENTEE
        }
    }
}
