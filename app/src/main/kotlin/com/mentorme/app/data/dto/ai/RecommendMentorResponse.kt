package com.mentorme.app.data.dto.ai

import com.mentorme.app.data.dto.mentors.MentorCardDto
import com.google.gson.annotations.SerializedName

data class RecommendMentorResponse(
    val type: String,
    val answer: String?,
    val ai: AiIntentResult?,
    val mentors: List<MentorWithExplanation>?
)

data class AiIntentResult(
    val intent: String?,
    val skills: List<String>?,
    val topic: String?,
    val level: String?
)

data class MentorWithExplanation(
    val mentorId: String,
    val userName: String,
    val fullName: String,
    val headline: String?,
    val avatarUrl: String?,
    val hourlyRateVnd: Int,
    val rating: RatingInfo?,
    val explanation: String?
)

data class RatingInfo(
    val average: Double?,
    val count: Int?
)
