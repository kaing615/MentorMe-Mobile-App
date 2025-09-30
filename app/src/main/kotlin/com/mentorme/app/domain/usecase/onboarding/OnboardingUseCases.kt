package com.mentorme.app.domain.usecase.onboarding

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import com.mentorme.app.data.repository.ProfileRepository
import javax.inject.Inject

data class RequiredProfileParams(
    val fullName: String,
    val jobTitle: String?,
    val location: String,
    val category: String,
    val bio: String?,
    val skills: String,
    val experience: String?,
    val headline: String?,
    val mentorReason: String?,
    val greatestAchievement: String?,
    val introVideo: String?,
    val description: String?,
    val goal: String?,
    val education: String?,
    val languages: String,
    val website: String?,
    val twitter: String?,
    val linkedin: String?,
    val github: String?,
    val youtube: String?,
    val facebook: String?,
    val avatarPath: String?,   // (không dùng khi JSON, nhưng giữ nếu sau này support upload)
    val avatarUrl: String?
)

class CreateRequiredProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(p: RequiredProfileParams): AppResult<ProfileCreateResponse> {
        return repo.createRequiredProfile(p)
    }
}
