package com.mentorme.app.data.dto.ai

import com.mentorme.app.data.dto.mentors.MentorCardDto

data class RecommendMentorResponse(
    val ai: Any?,
    val mentors: List<MentorCardDto>
)
