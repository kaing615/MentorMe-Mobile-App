package com.mentorme.app.data.repository

import com.mentorme.app.core.utils.AppResult
// Specific imports for DTOs (used for API requests/responses)
import com.mentorme.app.data.dto.AuthResponse
import com.mentorme.app.data.dto.BookingListResponse
import com.mentorme.app.data.dto.MentorListResponse
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.dto.auth.AuthResponse as AuthResponseDto
import com.mentorme.app.data.dto.auth.ResendOtpRequest
import com.mentorme.app.data.dto.UpdateBookingRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.profile.MePayload
import com.mentorme.app.data.dto.profile.ProfileMePayload
import com.mentorme.app.data.dto.profile.ProfileDto

// Specific imports for Models (used for business entities)
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.Mentor
import com.mentorme.app.data.model.User
import kotlinx.coroutines.flow.Flow
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import com.mentorme.app.domain.usecase.onboarding.RequiredProfileParams
import com.mentorme.app.domain.usecase.profile.UpdateProfileParams

interface AuthRepository {

    // New methods for auth flow
    suspend fun signUp(request: SignUpRequest): AppResult<AuthResponseDto>
    suspend fun signUpMentor(request: SignUpRequest): AppResult<AuthResponseDto>
    suspend fun signIn(request: SignInRequest): AppResult<AuthResponseDto>
    suspend fun verifyOtp(request: VerifyOtpRequest): AppResult<AuthResponseDto>
    suspend fun resendOtp(request: ResendOtpRequest): AppResult<AuthResponseDto>
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
}

interface BookingRepository {
    suspend fun createBooking(
        mentorId: String,
        occurrenceId: String,
        topic: String? = null,
        notes: String? = null
    ): AppResult<Booking>

    suspend fun getBookings(
        role: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): AppResult<BookingListResponse>

    suspend fun updateBooking(
        bookingId: String,
        updateRequest: UpdateBookingRequest
    ): AppResult<Booking>

    suspend fun getBookingById(bookingId: String): AppResult<Booking>
    suspend fun cancelBooking(bookingId: String, reason: String? = null): AppResult<Booking>
    suspend fun resendIcs(bookingId: String): AppResult<String>
    suspend fun rateBooking(bookingId: String, rating: Int, feedback: String? = null): AppResult<Booking>
    suspend fun payBooking(bookingId: String): AppResult<Unit>
}

interface ProfileRepository {

    suspend fun getMe(): AppResult<MePayload>

    suspend fun getProfileMe(): AppResult<ProfileMePayload>

    suspend fun getPublicProfile(userId: String): AppResult<ProfileDto>

    suspend fun updateProfile(
        params: UpdateProfileParams
    ): AppResult<Unit>

    suspend fun createRequiredProfile(
        params: RequiredProfileParams
    ): AppResult<ProfileCreateResponse>
}

