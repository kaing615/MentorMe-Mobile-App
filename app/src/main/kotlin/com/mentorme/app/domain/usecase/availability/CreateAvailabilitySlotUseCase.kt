package com.mentorme.app.domain.usecase.availability

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.dto.availability.CreateSlotRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.availability.SlotPayload
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Use case: create a draft availability slot for a mentor.
 * Returns the new slot id as AppResult<String> by reading ApiEnvelope<SlotPayload>.
 */
class CreateAvailabilitySlotUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    operator fun invoke(body: CreateSlotRequest): AppResult<String> {
        return try {
            val response = runBlocking { api.createAvailabilitySlot(body) }
            if (response.isSuccessful) {
                val env: ApiEnvelope<SlotPayload>? = response.body()
                // SlotDto.id is mapped from server "_id" via @SerializedName("_id")
                val id: String? = env?.data?.slot?.id
                if (!id.isNullOrBlank()) {
                    AppResult.success(id)
                } else {
                    AppResult.failure(IllegalStateException("Missing slot id"))
                }
            } else {
                val code = response.code()
                val errText = try { response.errorBody()?.string() } catch (_: Throwable) { null }
                AppResult.failure("HTTP $code: ${errText ?: response.message()}" )
            }
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}
