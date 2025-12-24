package com.mentorme.app.domain.usecase.profile

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.profile.ProfileDto
import com.mentorme.app.data.repository.ProfileRepository
import javax.inject.Inject

class GetPublicProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): AppResult<ProfileDto> {
        return repository.getPublicProfile(userId)
    }
}
