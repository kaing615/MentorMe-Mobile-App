package com.mentorme.app.data.repository.availability

import com.mentorme.app.core.utils.AppResult

interface AvailabilityRepository {
    suspend fun getMentorAvailability(mentorId: String): AppResult<List<com.mentorme.app.data.dto.AvailabilitySlot>>
}
