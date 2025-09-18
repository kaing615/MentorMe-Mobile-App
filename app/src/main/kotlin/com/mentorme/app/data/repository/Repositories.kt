package com.mentorme.app.data.repository

import com.mentorme.app.core.utils.Result
// Specific imports for DTOs (used for API requests/responses)
import com.mentorme.app.data.dto.AuthResponse
import com.mentorme.app.data.dto.AvailabilitySlot as ApiAvailabilitySlot
import com.mentorme.app.data.dto.BookingListResponse
import com.mentorme.app.data.dto.MentorListResponse

// Specific imports for Models (used for business entities)
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.Mentor
import com.mentorme.app.data.model.User
import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(email: String, password: String, name: String, role: String): Result<AuthResponse>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    fun getToken(): Flow<String?>
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}

interface MentorRepository {
    suspend fun getMentors(
        page: Int = 1,
        limit: Int = 10,
        expertise: String? = null,
        minRate: Double? = null,
        maxRate: Double? = null,
        minRating: Double? = null
    ): Result<MentorListResponse>

    suspend fun getMentorById(mentorId: String): Result<Mentor>
    suspend fun getMentorAvailability(mentorId: String): Result<List<ApiAvailabilitySlot>>
}

interface BookingRepository {
    suspend fun createBooking(
        mentorId: String,
        scheduledAt: String,
        duration: Int,
        topic: String,
        notes: String? = null
    ): Result<Booking>

    suspend fun getBookings(
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<BookingListResponse>

    suspend fun getBookingById(bookingId: String): Result<Booking>
    suspend fun updateBooking(bookingId: String, status: String? = null): Result<Booking>
    suspend fun rateBooking(bookingId: String, rating: Int, feedback: String? = null): Result<Booking>
}
