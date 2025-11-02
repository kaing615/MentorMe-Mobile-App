package com.mentorme.app.domain.usecase.calendar

import com.mentorme.app.data.dto.AvailabilitySlot as ApiAvailabilitySlot
import com.mentorme.app.data.repository.availability.AvailabilityRepository
import com.mentorme.app.core.utils.AppResult
import javax.inject.Inject

class GetMentorAvailabilityUseCase @Inject constructor(
    private val repository: AvailabilityRepository
) {
    suspend operator fun invoke(mentorId: String): AppResult<List<ApiAvailabilitySlot>> {
        return repository.getMentorAvailability(mentorId)
    }
}
