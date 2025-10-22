package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Use case: publish an availability slot to generate occurrences.
 * Wraps Retrofit response into AppResult and surfaces HTTP code/message on failure.
 */
class PublishSlotUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(slotId: String): AppResult<Unit> {
        return try {
            val response = runBlocking { api.publishAvailabilitySlot(slotId) }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) AppResult.success(Unit) else AppResult.failure("Empty body")
            } else {
                AppResult.failure("HTTP ${response.code()} ${response.message()}")
            }
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}
