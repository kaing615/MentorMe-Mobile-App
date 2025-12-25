package com.mentorme.app.domain.usecase.profile

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.profile.MePayload
import com.mentorme.app.data.repository.ProfileRepository
import javax.inject.Inject

data class UpdateProfileParams(
    val phone: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val languages: List<String>? = null,
    val skills: List<String>? = null,
    val avatarPath:  String? = null
)

class GetMeUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): AppResult<MePayload> {
        return profileRepository.getMe()
    }
}
