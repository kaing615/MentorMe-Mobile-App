package com.mentorme.app.data.repository

import com.mentorme.app.core.utils.AppResult
// Specific imports for DTOs (used for API requests/responses)
import com.mentorme.app.data.dto.AuthResponse
import com.mentorme.app.data.dto.AvailabilitySlot as ApiAvailabilitySlot
import com.mentorme.app.data.dto.BookingListResponse
import com.mentorme.app.data.dto.MentorListResponse
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.dto.auth.AuthResponse as AuthResponseDto

// Specific imports for Models (used for business entities)
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.Mentor
import com.mentorme.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    // New methods for auth flow
    suspend fun signUp(request: SignUpRequest): AppResult<AuthResponseDto>
    suspend fun signUpMentor(request: SignUpRequest): AppResult<AuthResponseDto>
    suspend fun signIn(request: SignInRequest): AppResult<AuthResponseDto>
    suspend fun verifyOtp(request: VerifyOtpRequest): AppResult<AuthResponseDto>
    suspend fun signOut(): AppResult<AuthResponseDto>
}

interface MentorRepository {
    suspend fun getMentors(
        page: Int = 1,
        limit: Int = 10,
        expertise: String? = null,
        minRate: Double? = null,
        maxRate: Double? = null,
        minRating: Double? = null
    ): AppResult<MentorListResponse>

    suspend fun getMentorById(mentorId: String): AppResult<Mentor>
    suspend fun getMentorAvailability(mentorId: String): AppResult<List<ApiAvailabilitySlot>>
}

interface BookingRepository {
    suspend fun createBooking(
        mentorId: String,
        scheduledAt: String,
        duration: Int,
        topic: String,
        notes: String? = null
    ): AppResult<Booking>

    suspend fun getBookings(
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): AppResult<BookingListResponse>

    suspend fun getBookingById(bookingId: String): AppResult<Booking>
    suspend fun updateBooking(bookingId: String, status: String? = null): AppResult<Booking>
    suspend fun rateBooking(bookingId: String, rating: Int, feedback: String? = null): AppResult<Booking>
}
