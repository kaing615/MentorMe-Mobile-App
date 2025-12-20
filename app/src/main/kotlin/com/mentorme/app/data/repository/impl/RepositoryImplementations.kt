package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.AppResult
// Specific imports for DTOs (API responses)
import com.mentorme.app.data.dto.BookingListResponse
import com.mentorme.app.data.dto.CreateBookingRequest
import com.mentorme.app.data.dto.MentorListResponse
import com.mentorme.app.data.dto.RatingRequest
import com.mentorme.app.data.dto.UpdateBookingRequest

// Specific imports for Models (business entities)
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.Mentor
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.data.repository.MentorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MentorRepositoryImpl @Inject constructor(
    private val api: MentorMeApi
) : MentorRepository {

    override suspend fun getMentors(
        page: Int,
        limit: Int,
        expertise: String?,
        minRate: Double?,
        maxRate: Double?,
        minRating: Double?
    ): AppResult<MentorListResponse> {
        return try {
            // Bridge to new discovery API (minimal): map filters as best-effort.
            val q = expertise // legacy 'expertise' -> simple search query
            val skillsCsv = expertise
            val res = api.listMentors(
                q = q,
                skillsCsv = skillsCsv,
                minRating = minRating?.toFloat(),
                priceMin = minRate?.toInt(),
                priceMax = maxRate?.toInt(),
                sort = null,
                page = page,
                limit = limit
            )
            if (res.isSuccessful) {
                val payload = res.body()?.data
                val total = payload?.total ?: 0
                val currentPage = payload?.page ?: page
                val currentLimit = payload?.limit ?: limit
                val totalPages = if (currentLimit > 0) ((total + currentLimit - 1) / currentLimit) else 0
                // Minimal bridge: return empty mentors list with server counts to keep callers from crashing.
                AppResult.success(
                    MentorListResponse(
                        mentors = emptyList(),
                        total = total,
                        page = currentPage,
                        totalPages = totalPages
                    )
                )
            } else {
                AppResult.failure(Exception("Failed to get mentors: ${res.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun getMentorById(mentorId: String): AppResult<Mentor> {
        return try {
            // The new API returns ApiEnvelope<MentorCardDto>; no direct mapping to data.model.Mentor provided here.
            val response = api.getMentor(mentorId)
            if (response.isSuccessful) {
                AppResult.failure(Exception("Legacy MentorRepository.getMentorById not supported with current API (use SearchMentorsUseCase)."))
            } else {
                AppResult.failure(Exception("Failed to get mentor: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }
}

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val api: MentorMeApi
) : BookingRepository {

    override suspend fun createBooking(
        mentorId: String,
        scheduledAt: String,
        duration: Int,
        topic: String,
        notes: String?
    ): AppResult<Booking> {
        return try {
            val request = CreateBookingRequest(mentorId, scheduledAt, duration, topic, notes)
            val response = api.createBooking(request)
            if (response.isSuccessful && response.body() != null) {
                AppResult.success(response.body()!!)
            } else {
                AppResult.failure(Exception("Failed to create booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun getBookings(status: String?, page: Int, limit: Int): AppResult<BookingListResponse> {
        return try {
            val response = api.getBookings(status, page, limit)
            if (response.isSuccessful && response.body() != null) {
                AppResult.success(response.body()!!)
            } else {
                AppResult.failure(Exception("Failed to get bookings: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun getBookingById(bookingId: String): AppResult<Booking> {
        return try {
            val response = api.getBookingById(bookingId)
            if (response.isSuccessful && response.body() != null) {
                AppResult.success(response.body()!!)
            } else {
                AppResult.failure(Exception("Failed to get booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun updateBooking(bookingId: String, status: String?): AppResult<Booking> {
        return try {
            val request = UpdateBookingRequest(status = status)
            val response = api.updateBooking(bookingId, request)
            if (response.isSuccessful && response.body() != null) {
                AppResult.success(response.body()!!)
            } else {
                AppResult.failure(Exception("Failed to update booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun rateBooking(bookingId: String, rating: Int, feedback: String?): AppResult<Booking> {
        return try {
            val request = RatingRequest(rating, feedback)
            val response = api.rateBooking(bookingId, request)
            if (response.isSuccessful && response.body() != null) {
                AppResult.success(response.body()!!)
            } else {
                AppResult.failure(Exception("Failed to rate booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }
}
