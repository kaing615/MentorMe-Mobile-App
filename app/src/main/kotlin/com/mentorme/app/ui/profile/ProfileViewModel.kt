package com.mentorme.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.data.dto.profile.ProfileMePayload
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.ProfileRepository
import com.mentorme.app.domain.usecase.profile.UpdateProfileParams
import com.mentorme.app.data.mapper.toUi as toUiMapper

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val role: UserRole? = null,
    val error: String? = null,
    val editing: Boolean = false,
    val edited: UserProfile? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val api: MentorMeApi, // ✅ Inject API để gọi getMenteeStats
    private val dataStoreManager: DataStoreManager // ✅ Observe userId changes
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        refresh()
        // ✅ Observe userId changes to detect account switch
        viewModelScope.launch {
            dataStoreManager.getUserId()
                .distinctUntilChanged()
                .drop(1) // Skip initial value (already loaded in refresh())
                .collect { userId ->
                    android.util.Log.d("ProfileViewModel", "🔄 userId changed: $userId, refreshing profile")
                    refresh()
                }
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        when (val res = repo.getProfileMe()) {
            is AppResult.Success -> {
                val payload: ProfileMePayload = res.data
                val (uiProfile, uiRole) = payload.toUiMapper()

                android.util.Log.d("ProfileViewModel", "Profile loaded: role=$uiRole")

                _state.value = ProfileUiState(
                    loading = false,
                    profile = uiProfile,
                    role = uiRole,
                    edited = uiProfile.copy(),
                    editing = false
                )

                // ✅ Load stats sau khi profile đã set vào state
                if (uiRole == UserRole.MENTEE) {
                    android.util.Log.d("ProfileViewModel", "User is MENTEE, loading stats...")
                    loadMenteeStats()
                } else {
                    android.util.Log.d("ProfileViewModel", "User is NOT mentee, skip loading stats")
                }
            }
            is AppResult.Error -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = res.throwable ?: "Không thể tải hồ sơ"
                )
            }
            AppResult.Loading -> Unit
        }
    }

    // ✅ NEW: Load mentee stats từ backend
    private fun loadMenteeStats() = viewModelScope.launch {
        try {
            android.util.Log.d("ProfileViewModel", "📡 Calling getMenteeStats API...")
            val response = api.getMenteeStats()
            android.util.Log.d("ProfileViewModel", "📥 Response: code=${response.code()}, success=${response.isSuccessful}")

            if (response.isSuccessful) {
                val envelope = response.body()
                android.util.Log.d("ProfileViewModel", "📦 Envelope: $envelope")
                val stats = envelope?.data
                android.util.Log.d("ProfileViewModel", "📊 Stats: totalSessions=${stats?.totalSessions}, totalSpent=${stats?.totalSpent}")

                if (stats != null) {
                    val currentProfile = _state.value.profile
                    if (currentProfile != null) {
                        android.util.Log.d("ProfileViewModel", "✅ Updating profile with stats...")
                        val updatedProfile = currentProfile.copy(
                            totalSessions = stats.totalSessions,
                            totalSpent = stats.totalSpent
                        )
                        _state.value = _state.value.copy(profile = updatedProfile)
                        android.util.Log.d("ProfileViewModel", "✅ Profile updated successfully!")
                    } else {
                        android.util.Log.e("ProfileViewModel", "❌ Current profile is null, cannot update stats")
                    }
                } else {
                    android.util.Log.e("ProfileViewModel", "❌ Stats data is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ProfileViewModel", "❌ API error: ${response.message()}, body: $errorBody")
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileViewModel", "❌ Exception loading mentee stats: ${e.message}", e)
        }
    }

    fun toggleEdit() {
        val s = _state.value
        _state.value = s.copy(editing = !s.editing, edited = s.profile?.copy())
    }

    fun updateEdited(block: (UserProfile) -> UserProfile) {
        _state.value.edited?.let { cur ->
            _state.value = _state.value.copy(edited = block(cur))
        }
    }

    fun save(onDone: (Boolean, String?) -> Unit = { _, _ -> }) = viewModelScope.launch {
        val ed = _state.value.edited ?: return@launch
        val params = UpdateProfileParams(
            phone = ed.phone,
            location = ed.location,
            bio = ed.bio,
            languages = ed.preferredLanguages.distinctTrimmedOrNull(),
            skills = ed.interests.distinctTrimmedOrNull()
        )
        doUpdate(params, onDone)
    }

    fun saveWith(
        params: UpdateProfileParams,
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        doUpdate(params, onDone)
    }

    private suspend fun doUpdate(
        params: UpdateProfileParams,
        onDone: (Boolean, String?) -> Unit
    ) {
        _state.value = _state.value.copy(loading = true, error = null)
        when (val resp = repo.updateProfile(params)) {
            is AppResult.Success -> {
                refresh()
                _state.value = _state.value.copy(editing = false)
                onDone(true, null)
            }
            is AppResult.Error -> {
                val msg = resp.throwable ?: "Cập nhật thất bại"
                _state.value = _state.value.copy(loading = false, error = msg)
                onDone(false, msg)
            }
            AppResult.Loading -> Unit
        }
    }
}

private fun List<String>?.distinctTrimmedOrNull(): List<String>? {
    val cleaned = this?.mapNotNull { it.trim() }.orEmpty().filter { it.isNotEmpty() }.distinct()
    return if (cleaned.isEmpty()) null else cleaned
}
