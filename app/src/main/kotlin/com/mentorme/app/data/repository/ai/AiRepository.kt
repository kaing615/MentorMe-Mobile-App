package com.mentorme.app.data.repository.ai

import com.mentorme.app.data.dto.mentors.MentorCardDto
import com.mentorme.app.data.remote.MentorMeApi
import jakarta.inject.Inject

class AiRepository @Inject constructor(
    private val api: MentorMeApi
) {
    suspend fun recommendMentor(
        message: String
    ): Result<List<MentorCardDto>> {
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
