package com.mentorme.app.ui.home

/**
 * UI model for Mentor in Home screen
 * Mapped from MentorCardDto via MentorMappers.kt
 */
data class Mentor(
    val id: String,
    val name: String,
    val role: String,
    val company: String,
    val rating: Double,
    val totalReviews: Int,
    val skills: List<String>,
    val hourlyRate: Int,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)

