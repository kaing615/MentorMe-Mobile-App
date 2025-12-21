package com.mentorme.app.domain.usecase.calendar

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.CancelBookingRequest
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
    operator fun invoke(bookingId: String, reason: String? = null): AppResult<Booking> {
        return try {
            val request = CancelBookingRequest(reason = reason)
            val resp = runBlocking { api.cancelBooking(bookingId, request) }
            if (resp.isSuccessful) {
                val body = resp.body()?.data
                if (body != null) {
                    AppResult.success(body)
                } else {
                    AppResult.failure("HTTP ${resp.code()}: empty body")
                }
            } else {
                val msg = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                AppResult.failure("HTTP ${resp.code()}: ${msg ?: resp.message()}\npath=/bookings/$bookingId/cancel")
            }
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}

