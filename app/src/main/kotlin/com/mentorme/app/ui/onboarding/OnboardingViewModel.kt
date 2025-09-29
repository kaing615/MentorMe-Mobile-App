package com.mentorme.app.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.ErrorUtils
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import com.mentorme.app.domain.usecase.onboarding.CreateRequiredProfileUseCase
import com.mentorme.app.domain.usecase.onboarding.RequiredProfileParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createRequiredProfile: CreateRequiredProfileUseCase,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val TAG = "OnboardingViewModel"
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun update(transform: (OnboardingState) -> OnboardingState) {
        _state.value = transform(_state.value)
    }

    fun clearError() = update { it.copy(error = null) }
    fun reset() = update { OnboardingState() }
    fun setAvatarPath(path: String?) = update { it.copy(avatarPath = path) }

    // gọi khi user chọn ảnh — chỉ lưu local path để submit multipart
    fun onAvatarPicked(localPath: String) {
        update { it.copy(avatarPath = localPath, avatarUrl = null) }
    }

    fun submit() {
        val s = _state.value
        if (s.location.isBlank() || s.category.isBlank() || s.languages.isBlank() || s.skills.isBlank()) {
            update { it.copy(error = "Vui lòng nhập đủ Location, Category, Languages và Skills") }
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "🔥 SUBMIT ONBOARDING CALLED")

            val token = dataStoreManager.getToken().first()
            if (token.isNullOrBlank()) {
                Log.e(TAG, "🚨 Không có token — chặn submit để tránh gọi API lỗi!")
                update { it.copy(error = "Token chưa sẵn sàng. Vui lòng đăng nhập lại hoặc thử lại sau.") }
                return@launch
            }
            Log.d(TAG, "✅ Token đã có: $token")

            _state.value = _state.value.copy(isLoading = true, error = null, success = null, next = null)

            val params = RequiredProfileParams(
                jobTitle = s.jobTitle,
                location = s.location,
                category = s.category,
                bio = s.bio,
                skills = s.skills,
                experience = s.experience,
                headline = s.headline,
                mentorReason = s.mentorReason,
                greatestAchievement = s.greatestAchievement,
                introVideo = s.introVideo,
                description = s.description,
                goal = s.goal,
                education = s.education,
                languages = s.languages,
                website = s.website,
                twitter = s.twitter,
                linkedin = s.linkedin,
                github = s.github,
                youtube = s.youtube,
                facebook = s.facebook,
                avatarPath = s.avatarPath,   // gửi file nếu có
                avatarUrl = s.avatarUrl      // hoặc URL nếu đã có
            )

            when (val result = createRequiredProfile(params)) {
                is AppResult.Success -> handleSuccess(result.data)
                is AppResult.Error -> handleError(result.throwable ?: "Unknown error")
                AppResult.Loading -> Unit
            }
        }
    }

    private fun handleSuccess(resp: ProfileCreateResponse) {
        Log.d(TAG, "Onboarding success: $resp")
        _state.value = _state.value.copy(
            isLoading = false,
            error = null,
            success = resp.success,
            next = resp.data?.next,
            createdProfileId = resp.data?.profile?.id
        )
    }

    private fun handleError(raw: String) {
        Log.e(TAG, "Onboarding failed: $raw")
        _state.value = _state.value.copy(
            isLoading = false,
            error = ErrorUtils.getUserFriendlyErrorMessage(raw),
            success = false
        )
    }
}

data class OnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean? = null,
    val next: String? = null,
    val createdProfileId: String? = null,
    val jobTitle: String? = null,
    val location: String = "",
    val category: String = "",
    val bio: String? = null,
    val skills: String = "",
    val experience: String? = null,
    val headline: String? = null,
    val mentorReason: String? = null,
    val greatestAchievement: String? = null,
    val introVideo: String? = null,
    val description: String? = null,
    val goal: String? = null,
    val education: String? = null,
    val languages: String = "",
    val website: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val github: String? = null,
    val youtube: String? = null,
    val facebook: String? = null,
    val avatarPath: String? = null,
    val avatarUrl: String? = null
)
