package com.mentorme.app.data.network.api.review

import com.mentorme.app.data.dto.review.CreateReviewRequest
import com.mentorme.app.data.dto.review.CreateReviewResponse
import com.mentorme.app.data.dto.review.GetMentorReviewsResponse
import com.mentorme.app.data.dto.review.GetMyReviewsResponse
import retrofit2.Response
import retrofit2.http.*

interface ReviewApiService {

    /**
     * POST /bookings/{id}/review
     * Create review for a completed booking
     * Auth: required (mentee only)
     */
    @POST("bookings/{id}/review")
    suspend fun createReview(
        @Path("id") bookingId: String,
        @Body request: CreateReviewRequest
    ): Response<CreateReviewResponse>

    /**
     * GET /mentors/{id}/reviews
     * Get all reviews for a mentor (public endpoint)
     * Cursor-based pagination (newest first)
     */
    @GET("mentors/{id}/reviews")
    suspend fun getMentorReviews(
        @Path("id") mentorId: String,
        @Query("limit") limit: Int? = 20,
        @Query("cursor") cursor: String? = null
    ): Response<GetMentorReviewsResponse>

    /**
     * GET /reviews/me
     * Get my reviews (mentee)
     * Auth: required
     * Cursor-based pagination (newest first)
     */
    @GET("reviews/me")
    suspend fun getMyReviews(
        @Query("limit") limit: Int? = 20,
        @Query("cursor") cursor: String? = null
    ): Response<GetMyReviewsResponse>
}

