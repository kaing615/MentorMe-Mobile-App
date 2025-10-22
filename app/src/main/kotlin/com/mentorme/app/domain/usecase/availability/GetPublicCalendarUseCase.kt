package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Use case: fetch public calendar occurrences for a mentor between a time window [from, to].
 * Returns list of CalendarItemDto from ApiEnvelope<CalendarPayload> and surfaces HTTP code/message on failure.
 */
class GetPublicCalendarUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(
        mentorId: String,
        fromIsoUtc: String,
        toIsoUtc: String
    ): AppResult<List<com.mentorme.app.data.dto.availability.CalendarItemDto>> {
        return try {
            runBlocking {
                val res = api.getPublicAvailabilityCalendar(mentorId, fromIsoUtc, toIsoUtc)
                if (res.isSuccessful) {
                    val envelope: com.mentorme.app.data.dto.availability.ApiEnvelope<
                        com.mentorme.app.data.dto.availability.CalendarPayload
                    >? = res.body()
                    val items: List<
                        com.mentorme.app.data.dto.availability.CalendarItemDto
                    > = envelope?.data?.items.orEmpty()
                    AppResult.success(items)
                } else {
                    AppResult.failure(
                        "HTTP " + res.code() + " " + res.message()
                    )
                }
            }
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}
