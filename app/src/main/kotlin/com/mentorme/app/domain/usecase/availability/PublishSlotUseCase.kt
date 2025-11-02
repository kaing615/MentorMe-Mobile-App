package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.availability.PublishResult
import com.mentorme.app.data.dto.availability.ConflictBody
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Use case: publish an availability slot to generate occurrences.
 * Wraps Retrofit response into AppResult and surfaces PublishResult on success
 * and HTTP code/message on failure. For 409, include server message body so UI can detect conflicts.
 */
class PublishSlotUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(slotId: String): AppResult<PublishResult> {
        return try {
            val response = runBlocking { api.publishAvailabilitySlot(slotId) }
            if (response.isSuccessful) {
                val env: ApiEnvelope<PublishResult>? = response.body()
                val data = env?.data
                if (data != null) AppResult.success(data) else AppResult.failure("Empty body")
            } else {
                val code = response.code()
                val errBody = try { response.errorBody()?.string().orEmpty() } catch (_: Throwable) { "" }
                if (code == 409) {
                    // Try parse conflict body for more context
                    val parsed = runCatching { Gson().fromJson(errBody, ConflictBody::class.java) }.getOrNull()
                    val msg = parsed?.message ?: "Conflict"
                    AppResult.failure("HTTP 409: $msg")
                } else {
                    AppResult.failure("HTTP $code ${response.message()}${if (errBody.isNotBlank()) ": $errBody" else ""}")
                }
            }
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}
