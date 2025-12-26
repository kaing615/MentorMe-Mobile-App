package com.mentorme.app.domain.usecase.review

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.review.ReviewDto
import com.mentorme.app.data.dto.review.ReviewPaginationDto
import com.mentorme.app.data.repository.review.ReviewRepository
import javax.inject.Inject

/**
 * Use case for submitting a review for a completed booking
 */
class SubmitReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        bookingId: String,
        rating: Int,
        comment: String?
    ): AppResult<ReviewDto> {
        // Validate rating
        if (rating < 1 || rating > 5) {
            return AppResult.failure("Đánh giá phải từ 1 đến 5 sao")
        }

        // Validate comment length
        if (comment != null && comment.length > 1000) {
            return AppResult.failure("Nhận xét không được vượt quá 1000 ký tự")
        }

        return reviewRepository.createReview(bookingId, rating, comment)
    }
}

/**
 * Use case for getting all reviews of a mentor
 */
class GetMentorReviewsUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        mentorId: String,
        limit: Int = 20,
        cursor: String? = null
    ): AppResult<Pair<List<ReviewDto>, ReviewPaginationDto>> {
        if (limit < 1 || limit > 100) {
            return AppResult.failure("Limit phải từ 1 đến 100")
        }

        return reviewRepository.getMentorReviews(mentorId, limit, cursor)
    }
}

/**
 * Use case for getting my own reviews (mentee)
 */
class GetMyReviewsUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        limit: Int = 20,
        cursor: String? = null
    ): AppResult<Pair<List<ReviewDto>, ReviewPaginationDto>> {
        if (limit < 1 || limit > 100) {
            return AppResult.failure("Limit phải từ 1 đến 100")
        }

        return reviewRepository.getMyReviews(limit, cursor)
    }
}

