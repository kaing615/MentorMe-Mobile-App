package com.mentorme.app.data.repository.review

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.review.CreateReviewRequest
import com.mentorme.app.data.dto.review.ReviewDto
import com.mentorme.app.data.dto.review.ReviewPaginationDto
import com.mentorme.app.data.network.api.review.ReviewApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewApiService: ReviewApiService
) : ReviewRepository {

    override suspend fun createReview(
        bookingId: String,
        rating: Int,
        comment: String?
    ): AppResult<ReviewDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateReviewRequest(rating, comment)
            val response = reviewApiService.createReview(bookingId, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data?.review != null) {
                    AppResult.success(body.data.review)
                } else {
                    AppResult.failure(body?.message ?: "Failed to create review")
                }
            } else {
                val errorBody = response.errorBody()?.string()

                // Parse specific error codes
                when (response.code()) {
                    400 -> {
                        val message = when {
                            errorBody?.contains("BOOKING_NOT_COMPLETED") == true ->
                                "Chỉ có thể đánh giá booking đã hoàn thành"
                            errorBody?.contains("INVALID_RATING") == true ->
                                "Đánh giá phải từ 1-5 sao"
                            else -> "Yêu cầu không hợp lệ"
                        }
                        AppResult.failure(message)
                    }
                    403 -> AppResult.failure("Bạn không có quyền đánh giá booking này")
                    404 -> AppResult.failure("Không tìm thấy booking")
                    409 -> AppResult.failure("Bạn đã đánh giá phiên này rồi")
                    else -> AppResult.failure("HTTP ${response.code()}: ${errorBody ?: response.message()}")
                }
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Đã có lỗi xảy ra khi tạo review")
        }
    }

    override suspend fun getMentorReviews(
        mentorId: String,
        limit: Int,
        cursor: String?
    ): AppResult<Pair<List<ReviewDto>, ReviewPaginationDto>> = withContext(Dispatchers.IO) {
        try {
            val response = reviewApiService.getMentorReviews(mentorId, limit, cursor)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val reviews = body.data.reviews
                    val pagination = body.data.pagination
                    AppResult.success(Pair(reviews, pagination))
                } else {
                    AppResult.failure(body?.data?.toString() ?: "Failed to fetch reviews")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                when (response.code()) {
                    400 -> {
                        val message = when {
                            errorBody?.contains("INVALID_MENTOR_ID") == true ->
                                "ID mentor không hợp lệ"
                            errorBody?.contains("USER_IS_NOT_MENTOR") == true ->
                                "Người dùng này không phải mentor"
                            else -> "Yêu cầu không hợp lệ"
                        }
                        AppResult.failure(message)
                    }
                    404 -> AppResult.failure("Không tìm thấy mentor")
                    else -> AppResult.failure("HTTP ${response.code()}: ${errorBody ?: response.message()}")
                }
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Đã có lỗi xảy ra khi tải reviews")
        }
    }

    override suspend fun getMyReviews(
        limit: Int,
        cursor: String?
    ): AppResult<Pair<List<ReviewDto>, ReviewPaginationDto>> = withContext(Dispatchers.IO) {
        try {
            val response = reviewApiService.getMyReviews(limit, cursor)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val reviews = body.data.reviews
                    val pagination = body.data.pagination
                    AppResult.success(Pair(reviews, pagination))
                } else {
                    AppResult.failure("Failed to fetch my reviews")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                when (response.code()) {
                    401 -> AppResult.failure("Bạn chưa đăng nhập")
                    else -> AppResult.failure("HTTP ${response.code()}: ${errorBody ?: response.message()}")
                }
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Đã có lỗi xảy ra khi tải reviews của bạn")
        }
    }
}

