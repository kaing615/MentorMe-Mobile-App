package com.mentorme.app.data.mapper

import com.mentorme.app.data.model.Mentor as ApiMentor
import com.mentorme.app.ui.home.Mentor as UiMentor

// ...existing code...

/** FE discovery card -> UI Mentor. Ensure calendar ID (ownerId/userId) is used for UiMentor.id. */
fun com.mentorme.app.data.dto.mentors.MentorCardDto.toUiMentor(): UiMentor = UiMentor(
    id = when {
        this.ownerId.isNotBlank() -> this.ownerId
        this.userId.isNotBlank() -> this.userId
        else -> this.id
    },
    name = this.name ?: "Mentor",
    role = this.role ?: "Mentor",
    company = this.company ?: "",
    rating = (this.rating ?: 0f).toDouble(),
    totalReviews = this.ratingCount ?: 0,
    skills = this.skills ?: emptyList(),
    hourlyRate = this.hourlyRate ?: 0,
    imageUrl = this.avatarUrl ?: "",
    isAvailable = true
)

/** Map list of discovery cards to UI Mentors. */
fun List<com.mentorme.app.data.dto.mentors.MentorCardDto>.toUiMentorsFromCards(): List<UiMentor> = map { it.toUiMentor() }

