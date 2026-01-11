package com.mentorme.app.data.dto.mentors

import com.google.gson.annotations.SerializedName

// FE card shape aligned with Swagger MentorCard + extended fields
data class MentorCardDto(
    val id: String,
    val ownerId: String,
    val userId: String,
    val name: String?,
    val role: String?,
    val company: String?,
    val rating: Float?,
    val ratingCount: Int?,
    val hourlyRate: Int?,
    val skills: List<String>?,
    val avatarUrl: String?,
    val isAvailable: Boolean?, // ✅ NEW: Mentor has published availability

    // ✅ Extended fields từ getMentorById
    val phone: String?,
    val bio: String?,
    val languages: List<String>?,
    val category: String?,
    val hourlyRateVnd: Int?,
    val headline: String?,
    val experience: String?,
    val education: String?,
    val location: String?
)

// List payload for mentors discovery
data class MentorListPayloadDto(
    val items: List<MentorCardDto> = emptyList(),
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0
)
