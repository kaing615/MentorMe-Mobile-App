package com.mentorme.app.data.repository.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.AvailabilitySlot as ApiAvailabilitySlot

interface AvailabilityRepository {
    fun getMentorAvailability(mentorId: String): AppResult<List<ApiAvailabilitySlot>>
}
