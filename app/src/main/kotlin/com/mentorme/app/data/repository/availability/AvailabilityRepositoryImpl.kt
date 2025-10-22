package com.mentorme.app.data.repository.availability

import com.mentorme.app.data.dto.AvailabilitySlot as ApiAvailabilitySlot
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.core.utils.AppResult
import kotlinx.coroutines.runBlocking
import com.mentorme.app.core.utils.Logx
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityRepositoryImpl @Inject constructor(
    private val api: MentorMeApi
) : AvailabilityRepository {

    override fun getMentorAvailability(mentorId: String): AppResult<List<ApiAvailabilitySlot>> {
        return try {
            val response = runBlocking { api.getMentorAvailability(mentorId) }
            Logx.d("AvailabilityRepo") { "getMentorAvailability mentorId=$mentorId code=${response.code()} success=${response.isSuccessful}" }
            if (response.isSuccessful) {
                AppResult.success(response.body().orEmpty())
            } else {
                AppResult.failure("HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Logx.e("AvailabilityRepo", { "getMentorAvailability exception mentorId=$mentorId" }, e)
            AppResult.failure(e)
        }
    }
}
