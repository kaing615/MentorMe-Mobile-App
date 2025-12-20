package com.mentorme.app.domain.usecase.calendar

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import com.mentorme.app.core.utils.Logx

/**
 * Cancel a booking by ID using the dedicated cancel endpoint.
 * Returns AppResult<Booking> and surfaces server HTTP code/message on failure.
 */
class CancelBookingUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(bookingId: String): AppResult<Booking> {
        return try {
            Logx.d("CancelBookingUseCase") { "Cancelling booking $bookingId" }
            val resp = runBlocking { api.cancelBooking(bookingId) }
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) {
                    Logx.d("CancelBookingUseCase") { "Successfully cancelled booking $bookingId" }
                    AppResult.success(body)
                } else {
                    AppResult.failure("HTTP ${resp.code()}: empty body")
                }
            } else {
                val msg = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                Logx.e("CancelBookingUseCase", { "Failed to cancel booking $bookingId: HTTP ${resp.code()}" })
                AppResult.failure("HTTP ${resp.code()}: ${msg ?: resp.message()}\npath=/bookings/$bookingId/cancel")
            }
        } catch (t: Throwable) {
            Logx.e("CancelBookingUseCase", { "Exception cancelling booking $bookingId" }, t)
            AppResult.failure(t)
        }
    }
}

