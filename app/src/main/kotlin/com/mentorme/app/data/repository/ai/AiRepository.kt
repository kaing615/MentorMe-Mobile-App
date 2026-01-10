package com.mentorme.app.data.repository.ai

import com.mentorme.app.data.dto.ai.RecommendMentorResponse
import com.mentorme.app.data.remote.MentorMeApi
import jakarta.inject.Inject

class AiRepository @Inject constructor(
    private val api: MentorMeApi
) {
    suspend fun askAi(
        message: String,
        mode: AiChatMode = AiChatMode.MENTEE
    ): Result<RecommendMentorResponse> {
        return try {
            val resp = when (mode) {
                AiChatMode.MENTEE -> api.recommendMentor(
                    request = mapOf("message" to message)
                )
                AiChatMode.MENTOR -> api.mentorAssistant(
                    request = mapOf("message" to message)
                )
            }

            if (resp.isSuccessful) {
                val body = resp.body()
                val data = body?.data
                
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data in response"))
                }
            } else {
                Result.failure(
                    Exception("HTTP ${resp.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
