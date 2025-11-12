package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.availability.UpdateSlotRequest
import com.mentorme.app.data.remote.MentorMeApi
import javax.inject.Inject

class UpdateAvailabilitySlotUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    suspend operator fun invoke(slotId: String, req: UpdateSlotRequest): AppResult<Unit> = try {
        val res = api.updateAvailabilitySlot(slotId, req)
        if (res.isSuccessful) AppResult.success(Unit)
        else AppResult.failure("HTTP ${res.code()} ${res.message()}")
    } catch (t: Throwable) {
        AppResult.failure(t)
    }
}
