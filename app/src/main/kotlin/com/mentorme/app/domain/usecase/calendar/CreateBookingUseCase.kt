package com.mentorme.app.domain.usecase.calendar

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.CreateBookingRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.dto.availability.ApiEnvelope

class CreateBookingUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(
        mentorId: String,
        occurrenceId: String,
        topic: String? = null,
        notes: String? = null
    ): AppResult<ApiEnvelope<Booking>> {
        return try {
            val request = CreateBookingRequest(
                mentorId = mentorId,
                occurrenceId = occurrenceId,
                topic = topic,
                notes = notes
            )
            Logx.d("CreateBookingUseCase") {
                "request mentorId=$mentorId occurrenceId=$occurrenceId"
            }
            val resp = runBlocking { api.createBooking(request) }
            Logx.d("CreateBookingUseCase") { "response code=${resp.code()}" }
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) {
                    AppResult.success(body)
                } else {
                    AppResult.failure("HTTP ${resp.code()}: empty body")
                }
            } else {
                val msg = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                AppResult.failure("HTTP ${resp.code()}: ${msg ?: resp.message()}\npath=/bookings")
            }
        } catch (t: Throwable) {
            Logx.e("CreateBookingUseCase", { "exception during request" }, t)
            AppResult.failure(t)
        }
    }
}
