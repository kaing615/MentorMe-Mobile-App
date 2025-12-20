package com.mentorme.app.domain.usecase.calendar

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import com.mentorme.app.core.utils.Logx

/**
 * Resend ICS calendar file for a confirmed booking.
 * Returns AppResult<Unit> and surfaces server HTTP code/message on failure.
 */
class ResendIcsUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(bookingId: String): AppResult<Unit> {
        return try {
            Logx.d("ResendIcsUseCase") { "Resending ICS for booking $bookingId" }
            val resp = runBlocking { api.resendICS(bookingId) }
            if (resp.isSuccessful) {
                Logx.d("ResendIcsUseCase") { "Successfully resent ICS for booking $bookingId" }
                AppResult.success(Unit)
            } else {
                val msg = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                Logx.e("ResendIcsUseCase", { "Failed to resend ICS for booking $bookingId: HTTP ${resp.code()}" })
                AppResult.failure("HTTP ${resp.code()}: ${msg ?: resp.message()}\npath=/bookings/$bookingId/resend-ics")
            }
        } catch (t: Throwable) {
            Logx.e("ResendIcsUseCase", { "Exception resending ICS for booking $bookingId" }, t)
            AppResult.failure(t)
        }
    }
}
