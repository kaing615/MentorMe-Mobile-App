package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject

class DeleteAvailabilityOccurrenceUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    suspend operator fun invoke(occurrenceId: String): AppResult<Unit> = try {
        val res = api.deleteAvailabilityOccurrence(occurrenceId)
        if (res.isSuccessful) AppResult.success(Unit)
        else if (res.code() == 409) AppResult.failure("HTTP 409 occurrence is booked")
        else AppResult.failure("HTTP ${res.code()} ${res.message()}")
    } catch (t: Throwable) {
        AppResult.failure(t)
    }
}
