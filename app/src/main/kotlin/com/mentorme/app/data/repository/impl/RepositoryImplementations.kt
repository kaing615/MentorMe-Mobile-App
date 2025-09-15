package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.Result
import com.mentorme.app.data.dto.*
import com.mentorme.app.data.model.*
import com.mentorme.app.data.remote.MentorMeApi
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
    ): Result<MentorListResponse> {
        return try {
            val response = api.getMentors(page, limit, expertise, minRate, maxRate, minRating)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to get mentors: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMentorById(mentorId: String): Result<Mentor> {
        return try {
            val response = api.getMentorById(mentorId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to get mentor: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMentorAvailability(mentorId: String): Result<List<AvailabilitySlot>> {
        return try {
            val response = api.getMentorAvailability(mentorId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to get availability: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
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
    ): Result<Booking> {
        return try {
            val request = CreateBookingRequest(mentorId, scheduledAt, duration, topic, notes)
            val response = api.createBooking(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to create booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBookings(status: String?, page: Int, limit: Int): Result<BookingListResponse> {
        return try {
            val response = api.getBookings(status, page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to get bookings: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBookingById(bookingId: String): Result<Booking> {
        return try {
            val response = api.getBookingById(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to get booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateBooking(bookingId: String, status: String?): Result<Booking> {
        return try {
            val request = UpdateBookingRequest(status = status)
            val response = api.updateBooking(bookingId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to update booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun rateBooking(bookingId: String, rating: Int, feedback: String?): Result<Booking> {
        return try {
            val request = RatingRequest(rating, feedback)
            val response = api.rateBooking(bookingId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to rate booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
