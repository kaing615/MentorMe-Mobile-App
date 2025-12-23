package com.mentorme.app.data.remote

// DTO imports
import com.mentorme.app.data.dto.AuthResponse
import com.mentorme.app.data.dto.CancelBookingRequest
import com.mentorme.app.data.dto.CreateBookingRequest
import com.mentorme.app.data.dto.LoginRequest
import com.mentorme.app.data.dto.Message as ApiMessage
import com.mentorme.app.data.dto.MentorDeclineRequest
import com.mentorme.app.data.dto.PaymentWebhookRequest
import com.mentorme.app.data.dto.RatingRequest
import com.mentorme.app.data.dto.RegisterRequest
import com.mentorme.app.data.dto.ResendIcsResponse
import com.mentorme.app.data.dto.SendMessageRequest
import com.mentorme.app.data.dto.UpdateBookingRequest
import com.mentorme.app.data.dto.availability.UpdateSlotRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.availability.CalendarPayload
import com.mentorme.app.data.dto.mentors.MentorCardDto
import com.mentorme.app.data.dto.mentors.MentorListPayloadDto
import com.mentorme.app.data.dto.availability.PublishResult
import com.mentorme.app.data.dto.notifications.DeviceTokenPayload
import com.mentorme.app.data.dto.notifications.RegisterDeviceRequest
import com.mentorme.app.data.dto.notifications.UnregisterDeviceRequest
import com.mentorme.app.data.dto.notifications.UnregisterResult

// Model imports
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.User
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

    // Mentors (public discovery)
    @GET("mentors")
    suspend fun listMentors(
        @Query("q") q: String? = null,
        @Query("skills") skillsCsv: String? = null,
        @Query("minRating") minRating: Float? = null,
        @Query("priceMin") priceMin: Int? = null,
        @Query("priceMax") priceMax: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiEnvelope<MentorListPayloadDto>>

    @GET("mentors/{id}")
    suspend fun getMentor(@Path("id") id: String): Response<ApiEnvelope<MentorCardDto>>


    // Booking endpoints
    @POST("bookings")
    suspend fun createBooking(@Body bookingRequest: CreateBookingRequest): Response<ApiEnvelope<Booking>>

    @GET("bookings")
    suspend fun getBookings(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiEnvelope<com.mentorme.app.data.dto.BookingListResponse>>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): Response<ApiEnvelope<Booking>>

    @PUT("bookings/{id}")
    suspend fun updateBooking(
        @Path("id") bookingId: String,
        @Body updateRequest: UpdateBookingRequest
    ): Response<ApiEnvelope<Booking>>

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: String,
        @Body cancelRequest: CancelBookingRequest
    ): Response<ApiEnvelope<Booking>>

    @POST("bookings/{id}/resend-ics")
    suspend fun resendIcs(@Path("id") bookingId: String): Response<ApiEnvelope<ResendIcsResponse>>

    @POST("bookings/{id}/rate")
    suspend fun rateBooking(
        @Path("id") bookingId: String,
        @Body ratingRequest: RatingRequest
    ): Response<ApiEnvelope<Booking>>

    @POST("bookings/{id}/mentor-confirm")
    suspend fun mentorConfirmBooking(
        @Path("id") bookingId: String
    ): Response<ApiEnvelope<Booking>>

    @POST("bookings/{id}/mentor-decline")
    suspend fun mentorDeclineBooking(
        @Path("id") bookingId: String,
        @Body request: MentorDeclineRequest
    ): Response<ApiEnvelope<Booking>>

    @POST("payments/webhook")
    suspend fun simulatePaymentWebhook(
        @Body request: PaymentWebhookRequest
    ): Response<ApiEnvelope<Map<String, Any>>>

    // Availability endpoints

    /**
     * Create a draft availability slot (mentor auth with Bearer).
     */
    @POST("availability/slots")
    suspend fun createAvailabilitySlot(
        @Body body: com.mentorme.app.data.dto.availability.CreateSlotRequest
    ): retrofit2.Response<com.mentorme.app.data.dto.availability.ApiEnvelope<com.mentorme.app.data.dto.availability.SlotPayload>>

    /**
     * Publish a slot to materialize occurrences (mentor auth).
     */
    @POST("availability/slots/{id}/publish")
    suspend fun publishAvailabilitySlot(
        @Path("id") id: String
    ): retrofit2.Response<com.mentorme.app.data.dto.availability.ApiEnvelope<PublishResult>>

    /**
     * Update slot meta/time/visibility or action (pause/resume).
     */
    @PATCH("availability/slots/{id}")
    suspend fun updateAvailabilitySlot(
        @Path("id") id: String,
        @Body body: UpdateSlotRequest
    ): retrofit2.Response<kotlin.Unit>

    /**
     * Get public calendar for a mentor (no auth).
     * 'from'/'to' must be ISO-8601 UTC.
     */
    @GET("availability/calendar/{mentorId}")
    suspend fun getPublicAvailabilityCalendar(
        @Path("mentorId") mentorId: String,
        @Query("from") fromIsoUtc: String,
        @Query("to") toIsoUtc: String,
        @Query("includeClosed") includeClosed: Boolean = true
    ): Response<ApiEnvelope<CalendarPayload>>

    /**
     * Soft-delete (disable) a slot (mentor auth).
     */
    @DELETE("availability/slots/{slotId}")
    suspend fun disableAvailabilitySlot(
        @Path("slotId") slotId: String
    ): retrofit2.Response<kotlin.Unit>

    /**
     * Delete slot by id (mentor auth).
     */
    @DELETE("availability/slots/{id}")
    suspend fun deleteAvailabilitySlot(
        @Path("id") id: String
    ): retrofit2.Response<kotlin.Unit>

    /**
     * Delete a single unbooked occurrence (mentor auth).
     */
    @DELETE("availability/occurrences/{occurrenceId}")
    suspend fun deleteAvailabilityOccurrence(
        @Path("occurrenceId") occurrenceId: String
    ): retrofit2.Response<kotlin.Unit>

    // Messages endpoints
    @GET("messages/{bookingId}")
    suspend fun getMessages(@Path("bookingId") bookingId: String): Response<List<ApiMessage>>

    @POST("messages")
    suspend fun sendMessage(@Body messageRequest: SendMessageRequest): Response<ApiMessage>

    // Notifications
    @POST("notifications/devices")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceRequest
    ): Response<ApiEnvelope<DeviceTokenPayload>>

    @POST("notifications/devices/unregister")
    suspend fun unregisterDeviceToken(
        @Body request: UnregisterDeviceRequest
    ): Response<ApiEnvelope<UnregisterResult>>
}
