package com.mentorme.app.core.model

/**
 * Model cơ bản cho Mentor – map từ mockData.ts / MentorCard.tsx.
 * Có thể mở rộng thêm khi bạn convert MentorDetailModal/MentorFilters.
 */
data class Mentor(
    val id: String,
    val fullName: String,
    val title: String,
    val avatarUrl: String?,          // url ảnh; null -> fallback chữ cái
    val bio: String,
    val skills: List<String>,
    val languages: List<String> = emptyList(),
    val experience: String,          // ví dụ: "10+ năm"
    val hourlyRate: Int,             // USD/h
    val rating: Double,              // 4.9
    val totalReviews: Int,           // 120
    val verified: Boolean = false,
    val isOnline: Boolean = false
)
