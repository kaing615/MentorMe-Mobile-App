package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject
import com.mentorme.app.core.utils.Logx

/**
 * Use case: fetch public calendar occurrences for a mentor between a time window [from, to].
 * Returns list of CalendarItemDto from ApiEnvelope<CalendarPayload> and surfaces HTTP code/message on failure.
 */
class GetPublicCalendarUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    suspend operator fun invoke(
        mentorId: String,
        fromIsoUtc: String,
        toIsoUtc: String,
        includeClosed: Boolean = true
    ): AppResult<List<com.mentorme.app.data.dto.availability.CalendarItemDto>> {
        return try {
            // Pre-call logs and guard (non-blocking)
            Logx.d("Cal") { "call calendar mentorId=$mentorId from=$fromIsoUtc to=$toIsoUtc" }
            val idOk = mentorId.length >= 16 && mentorId.matches(Regex("^[A-Za-z0-9_-]+$"))
            if (!idOk) {
                Logx.d("Cal") { "WARN suspicious mentorId: '$mentorId' (len=${mentorId.length})" }
            }

            val res = api.getPublicAvailabilityCalendar(mentorId, fromIsoUtc, toIsoUtc, includeClosed)
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
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}
