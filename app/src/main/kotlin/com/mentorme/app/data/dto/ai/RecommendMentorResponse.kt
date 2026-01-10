package com.mentorme.app.data.dto.ai

import com.mentorme.app.data.dto.mentors.MentorCardDto

/**
 * Response from AI recommend mentor endpoint
 * Supports 3 types: mentor_recommend, app_qa, general_response
 */
data class RecommendMentorResponse(
    val success: Boolean,
    val type: String, // "mentor_recommend" | "app_qa" | "general_response"
    val answer: String? = null, // For app_qa and general_response
    val ai: AiAnalysis? = null, // For mentor_recommend
    val mentors: List<MentorCardDto>? = null, // For mentor_recommend
    val context: SearchContext? = null, // For mentor_recommend
    val suggestions: List<String>? = null // Suggested follow-up questions
)

/**
 * AI analysis result for mentor search
 */
data class AiAnalysis(
    val skills: List<String>,
    val level: String?, // "beginner" | "intermediate" | "advanced"
    val priceRange: PriceRange?,
    val userQuery: String
)

data class PriceRange(
    val min: Int?,
    val max: Int?
)

/**
 * Search context for mentor recommendations
 */
data class SearchContext(
    val totalFound: Int,
    val searchCriteria: SearchCriteria
)

data class SearchCriteria(
    val skills: List<String>,
    val level: String?,
    val priceRange: PriceRange?
)
