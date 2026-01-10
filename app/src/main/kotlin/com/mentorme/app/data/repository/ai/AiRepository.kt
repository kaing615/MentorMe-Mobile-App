package com.mentorme.app.data.repository.ai

import com.mentorme.app.data.dto.ai.RecommendMentorResponse
import com.mentorme.app.data.remote.MentorMeApi
import jakarta.inject.Inject

class AiRepository @Inject constructor(
    private val api: MentorMeApi
) {
    /**
     * Send message to AI chatbot
     * Returns full response including:
     * - mentor_recommend: AI analysis + mentor list
     * - app_qa: Answer to app-related questions
     * - general_response: Greetings, farewells, etc.
     */
    suspend fun chatWithAi(
        message: String
    ): Result<RecommendMentorResponse> {
        return try {
            val resp = api.recommendMentor(
                request = mapOf("message" to message)
            )

            if (resp.isSuccessful) {
                val envelope = resp.body()
                val data = envelope?.data

                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(
                    Exception("HTTP ${resp.code()}: ${resp.message()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy method - only returns mentors (backward compatibility)
     * Use chatWithAi() for full AI features
     */
    @Deprecated(
        message = "Use chatWithAi() instead for full AI features",
        replaceWith = ReplaceWith("chatWithAi(message)")
    )
    suspend fun recommendMentor(
        message: String
    ): Result<List<com.mentorme.app.data.dto.mentors.MentorCardDto>> {
        return try {
            val resp = api.recommendMentor(
                request = mapOf("message" to message)
            )

            if (resp.isSuccessful) {
                val env = resp.body()

                Result.success(
                    env?.data?.mentors ?: emptyList()
                )
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
