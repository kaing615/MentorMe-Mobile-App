package com.mentorme.app.data.dto.review

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /bookings/{id}/review
 */
data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int, // 1-5
    @SerializedName("comment")
    val comment: String? = null // Optional, max 1000 chars
)

/**
 * Review data from API
 */
data class ReviewDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("booking")
    val booking: Any, // Can be String (ID) or BookingDto (populated)
    @SerializedName("mentee")
    val mentee: ReviewUserDto,
    @SerializedName("mentor")
    val mentor: Any, // Can be String (ID) or ReviewUserDto (populated)
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * User info in review (populated)
 */
data class ReviewUserDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null
)

/**
 * Booking info in review (for /reviews/me endpoint)
 */
data class ReviewBookingDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("topic")
    val topic: String?
)

/**
 * Pagination metadata for review lists
 */
data class ReviewPaginationDto(
    @SerializedName("hasMore")
    val hasMore: Boolean,
    @SerializedName("nextCursor")
    val nextCursor: String?,
    @SerializedName("limit")
    val limit: Int
)

/**
 * Response for POST /bookings/{id}/review
 */
data class CreateReviewResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: CreateReviewData?
)

data class CreateReviewData(
    @SerializedName("review")
    val review: ReviewDto
)

/**
 * Response for GET /mentors/{id}/reviews
 */
data class GetMentorReviewsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: MentorReviewsData?
)

data class MentorReviewsData(
    @SerializedName("reviews")
    val reviews: List<ReviewDto>,
    @SerializedName("pagination")
    val pagination: ReviewPaginationDto
)

/**
 * Response for GET /reviews/me
 */
data class GetMyReviewsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: MyReviewsData?
)

data class MyReviewsData(
    @SerializedName("reviews")
    val reviews: List<ReviewDto>,
    @SerializedName("pagination")
    val pagination: ReviewPaginationDto
)

