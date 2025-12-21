package com.mentorme.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.profile.MePayload
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
    private val repo: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        when (val res = repo.getMe()) {
            is AppResult.Success -> {
                val me: MePayload = res.data
                val (uiProfile, uiRole) = me.toUiMapper()
                _state.value = ProfileUiState(
                    loading = false,
                    profile = uiProfile,
                    role = uiRole,
                    edited = uiProfile.copy(),
                    editing = false
                )
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
