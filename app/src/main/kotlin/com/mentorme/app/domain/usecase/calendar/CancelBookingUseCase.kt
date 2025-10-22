package com.mentorme.app.domain.usecase.calendar

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.UpdateBookingRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

/**
 * Cancel a booking by ID.
 * Returns AppResult<Booking> and surfaces server HTTP code/message on failure.
 */
class CancelBookingUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(bookingId: String): AppResult<Booking> {
        return try {
            val request = UpdateBookingRequest(status = "canceled")
            val resp = runBlocking { api.updateBooking(bookingId, request) }
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) {
                    AppResult.success(body)
                } else {
                    AppResult.failure("HTTP ${resp.code()}: empty body")
                }
            } else {
                val msg = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                AppResult.failure("HTTP ${resp.code()}: ${msg ?: resp.message()}\npath=/bookings/$bookingId")
            }
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}

