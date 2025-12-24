package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.BookingListResponse
import com.mentorme.app.data.dto.CreateBookingRequest
import com.mentorme.app.data.dto.MentorListResponse
import com.mentorme.app.data.dto.RatingRequest
import com.mentorme.app.data.dto.UpdateBookingRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingUserSummary
import com.mentorme.app.data.model.Mentor
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.data.repository.MentorRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
            val q = expertise
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
                val total = payload?.total?:0
                val currentPage = payload?.page?:page
                val currentLimit = payload?.limit?:limit
                val totalPages = if (currentLimit > 0) ((total + currentLimit - 1) / currentLimit) else 0
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
    private val mentorSummaryCache = ConcurrentHashMap<String, BookingUserSummary>()

    private fun normalizeBookingLocalTime(booking: Booking): Booking {
        val startIso = booking.startTimeIso
        val endIso = booking.endTimeIso
        if (startIso.isNullOrBlank() || endIso.isNullOrBlank()) return booking

        val zone = ZoneId.systemDefault()
        val start = runCatching { Instant.parse(startIso).atZone(zone) }.getOrNull() ?: return booking
        val end = runCatching { Instant.parse(endIso).atZone(zone) }.getOrNull() ?: return booking
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

        return booking.copy(
            date = start.toLocalDate().toString(),
            startTime = start.format(timeFmt),
            endTime = end.format(timeFmt)
        )
    }

    private fun normalizeBookingList(list: List<Booking>): List<Booking> =
        list.map(::normalizeBookingLocalTime)

    private suspend fun resolveMentorSummary(mentorId: String): BookingUserSummary? {
        if (mentorId.isBlank()) return null
        mentorSummaryCache[mentorId]?.let { return it }
        return try {
            val response = api.getMentor(mentorId)
            if (!response.isSuccessful) return null
            val card = response.body()?.data
            val name = card?.name?.trim()
            if (name.isNullOrBlank()) return null
            val summary = BookingUserSummary(
                id = mentorId,
                fullName = name,
                avatar = card.avatarUrl,
                role = UserRole.MENTOR
            )
            mentorSummaryCache[mentorId] = summary
            summary
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun enrichBookingsWithMentor(list: List<Booking>): List<Booking> = coroutineScope {
        if (list.isEmpty()) return@coroutineScope list
        val mentorIds = list
            .filter { it.mentorFullName.isNullOrBlank() && it.mentor?.fullName.isNullOrBlank() }
            .map { it.mentorId }
            .distinct()
        if (mentorIds.isEmpty()) return@coroutineScope list
        val lookups = mentorIds.associateWith { id -> async { resolveMentorSummary(id) } }
        val summaries = lookups.mapValues { (_, job) -> job.await() }

        list.map { booking ->
            val summary = summaries[booking.mentorId] ?: return@map booking
            val fullName = booking.mentorFullName?.takeIf { it.isNotBlank() } ?: summary.fullName
            val mergedSummary = booking.mentor ?: summary
            booking.copy(mentorFullName = fullName, mentor = mergedSummary)
        }
    }

    override suspend fun createBooking(
        mentorId: String,
        occurrenceId: String,
        topic: String?,
        notes: String?
    ): AppResult<Booking> {
        return try {
            val request = CreateBookingRequest(mentorId, occurrenceId, topic, notes)
            val response = api.createBooking(request)
            if (response.isSuccessful) {
                // ✅ Unwrap data từ ApiEnvelope<Booking>
                val envelope = response.body()
                val booking = envelope?.data
                if (booking != null) {
                    val normalized = normalizeBookingLocalTime(booking)
                    val enriched = enrichBookingsWithMentor(listOf(normalized)).first()
                    AppResult.success(enriched)
                } else {
                    AppResult.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                AppResult.failure(Exception("Failed to create booking: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun getBookings(
        role: String?,
        status: String?,
        page: Int,
        limit: Int
    ): AppResult<BookingListResponse> {
        return try {
            val response = api.getBookings(role, status, page, limit)
            if (response.isSuccessful) {
                // ✅ Unwrap .data từ ApiEnvelope<BookingListResponse>
                val envelope = response.body()
                val bookingList = envelope?.data
                if (bookingList != null) {
                    val normalized = normalizeBookingList(bookingList.bookings)
                    val enriched = enrichBookingsWithMentor(normalized)
                    AppResult.success(bookingList.copy(bookings = enriched))
                } else {
                    AppResult.failure(Exception("Empty response body"))
                }
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
            if (response.isSuccessful) {
                // ✅ Unwrap data
                val envelope = response.body()
                val booking = envelope?.data
                if (booking != null) {
                    AppResult.success(normalizeBookingLocalTime(booking))
                } else {
                    AppResult.failure(Exception("Booking not found"))
                }
            } else {
                AppResult.failure(Exception("Failed to get booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun updateBooking(
        bookingId: String,
        updateRequest: UpdateBookingRequest
    ): AppResult<Booking> {
        return try {
            val response = api.updateBooking(bookingId, updateRequest)
            if (response.isSuccessful) {
                // ✅ Unwrap .data
                val envelope = response.body()
                val booking = envelope?.data
                if (booking != null) {
                    AppResult.success(normalizeBookingLocalTime(booking))
                } else {
                    AppResult.failure(Exception("Empty response body"))
                }
            } else {
                AppResult.failure(Exception("Failed to update booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun resendIcs(bookingId: String): AppResult<String> {
        return try {
            val response = api.resendIcs(bookingId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    AppResult.success(body.message ?: "ICS email resent successfully")
                } else {
                    AppResult.failure(Exception(body?.message ?: "Failed to resend ICS"))
                }
            } else {
                AppResult.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun rateBooking(
        bookingId: String,
        rating: Int,
        feedback: String?
    ): AppResult<Booking> {
        return try {
            val ratingRequest = RatingRequest(rating, feedback)
            val response = api.rateBooking(bookingId, ratingRequest)
            if (response.isSuccessful) {
                // ✅ Unwrap .data
                val envelope = response.body()
                val booking = envelope?.data
                if (booking != null) {
                    AppResult.success(normalizeBookingLocalTime(booking))
                } else {
                    AppResult.failure(Exception("Empty response body"))
                }
            } else {
                AppResult.failure(Exception("Failed to rate booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }

    override suspend fun cancelBooking(bookingId: String, reason: String?): AppResult<Booking> {
        return try {
            val req = com.mentorme.app.data.dto.CancelBookingRequest(reason = reason)
            val response = api.cancelBooking(bookingId, req)
            if (response.isSuccessful) {
                val booking = response.body()?.data
                if (booking != null) AppResult.success(normalizeBookingLocalTime(booking))
                else AppResult.failure(Exception("Empty response body"))
            } else {
                val err = response.errorBody()?.string()
                AppResult.failure(Exception("Failed to cancel booking: ${response.code()} - ${err ?: response.message()}"))
            }
        } catch (e: Exception) {
            AppResult.failure(e)
        }
    }
}
