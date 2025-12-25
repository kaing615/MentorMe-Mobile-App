package com.mentorme.app.data.repository.review

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.review.ReviewDto
import com.mentorme.app.data.dto.review.ReviewPaginationDto

/**
 * Repository interface for review operations
 */
interface ReviewRepository {

    /**
     * Create a review for a completed booking
     * @param bookingId The booking ID
     * @param rating Rating from 1-5
     * @param comment Optional comment (max 1000 chars)
     * @return AppResult with created ReviewDto
     */
    suspend fun createReview(
        bookingId: String,
        rating: Int,
        comment: String?
    ): AppResult<ReviewDto>

    /**
     * Get all reviews for a mentor (public)
     * @param mentorId The mentor ID
     * @param limit Number of reviews per page (default 20)
     * @param cursor Cursor for pagination (review ID)
     * @return AppResult with list of reviews and pagination info
     */
    suspend fun getMentorReviews(
        mentorId: String,
        limit: Int = 20,
        cursor: String? = null
    ): AppResult<Pair<List<ReviewDto>, ReviewPaginationDto>>

    /**
     * Get my reviews (mentee's own reviews)
     * @param limit Number of reviews per page (default 20)
     * @param cursor Cursor for pagination (review ID)
     * @return AppResult with list of reviews and pagination info
     */
    suspend fun getMyReviews(
        limit: Int = 20,
        cursor: String? = null
    ): AppResult<Pair<List<ReviewDto>, ReviewPaginationDto>>
}

