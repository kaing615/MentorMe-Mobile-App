package com.mentorme.app.data.remote

import com.mentorme.app.data.dto.*
import com.mentorme.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MentorMeApi {

    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>

    // Mentor endpoints
    @GET("mentors")
    suspend fun getMentors(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("expertise") expertise: String? = null,
        @Query("minRate") minRate: Double? = null,
        @Query("maxRate") maxRate: Double? = null,
        @Query("rating") minRating: Double? = null
    ): Response<MentorListResponse>

    @GET("mentors/{id}")
    suspend fun getMentorById(@Path("id") mentorId: String): Response<Mentor>

    @GET("mentors/{id}/availability")
    suspend fun getMentorAvailability(@Path("id") mentorId: String): Response<List<AvailabilitySlot>>

    // Booking endpoints
    @POST("bookings")
    suspend fun createBooking(@Body bookingRequest: CreateBookingRequest): Response<Booking>

    @GET("bookings")
    suspend fun getBookings(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<BookingListResponse>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): Response<Booking>

    @PUT("bookings/{id}")
    suspend fun updateBooking(
        @Path("id") bookingId: String,
        @Body updateRequest: UpdateBookingRequest
    ): Response<Booking>

    @POST("bookings/{id}/rate")
    suspend fun rateBooking(
        @Path("id") bookingId: String,
        @Body ratingRequest: RatingRequest
    ): Response<Booking>

    // Messages endpoints
    @GET("messages/{bookingId}")
    suspend fun getMessages(@Path("bookingId") bookingId: String): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(@Body messageRequest: SendMessageRequest): Response<Message>
}
