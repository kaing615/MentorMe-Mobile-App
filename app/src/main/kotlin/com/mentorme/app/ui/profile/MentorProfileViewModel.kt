package com.mentorme.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.data.dto.mentor.MentorOverallStatsDto
import com.mentorme.app.data.dto.mentor.MentorWeeklyEarningsDto
import com.mentorme.app.data.dto.mentor.MentorYearlyEarningsDto
import com.mentorme.app.data.dto.profile.ProfileMePayload
import com.mentorme.app.data.mapper.toUi as toUiMapper
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.ProfileRepository
import com.mentorme.app.domain.usecase.profile.UpdateProfileParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@HiltViewModel
class MentorProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val api: MentorMeApi,
    private val dataStoreManager: DataStoreManager // ✅ Observe userId changes
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    private val _overallStats = MutableStateFlow<MentorOverallStatsDto?>(null)
    val overallStats: StateFlow<MentorOverallStatsDto?> = _overallStats

    private val _weeklyEarnings = MutableStateFlow<List<Long>>(emptyList())
    val weeklyEarnings: StateFlow<List<Long>> = _weeklyEarnings

    private val _yearlyEarnings = MutableStateFlow<List<Long>>(emptyList())
    val yearlyEarnings: StateFlow<List<Long>> = _yearlyEarnings

    private val _currentYear = MutableStateFlow<Int>(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear

    private val _walletBalance = MutableStateFlow<Long>(0L)
    val walletBalance: StateFlow<Long> = _walletBalance

    init {
        refresh()
        loadStats()
        loadWallet()
        // ✅ Observe userId changes to detect account switch
        viewModelScope.launch {
            dataStoreManager.getUserId()
                .distinctUntilChanged()
                .drop(1) // Skip initial value
                .collect { userId ->
                    android.util.Log.d("MentorProfileVM", "🔄 userId changed: $userId, refreshing profile & stats")
                    refresh()
                    loadStats()
                    loadWallet()
                }
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        when (val res = repo.getProfileMe()) {
            is AppResult.Success -> {
                val payload: ProfileMePayload = res.data
                val (uiProfile, uiRole) = payload.toUiMapper()
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

    fun loadStats() = viewModelScope.launch {
        // Load overall stats (rating, total mentees, total hours)
        try {
            val overallResp = api.getMentorOverallStats()
            if (overallResp.isSuccessful) {
                _overallStats.value = overallResp.body()?.data
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }

        // Load weekly earnings
        try {
            val weeklyResp = api.getMentorWeeklyEarnings()
            if (weeklyResp.isSuccessful) {
                _weeklyEarnings.value = weeklyResp.body()?.data?.weeklyEarnings ?: emptyList()
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }

        // Load yearly earnings
        try {
            val yearlyResp = api.getMentorYearlyEarnings()
            if (yearlyResp.isSuccessful) {
                val data = yearlyResp.body()?.data
                _yearlyEarnings.value = data?.yearlyEarnings ?: emptyList()
                _currentYear.value = data?.year ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    fun loadWallet() = viewModelScope.launch {
        try {
            val walletResp = api.getMyWallet()
            if (walletResp.isSuccessful) {
                _walletBalance.value = walletResp.body()?.data?.balanceMinor ?: 0L
            }
        } catch (e: Exception) {
            // Handle error silently or log it
            android.util.Log.e("MentorProfile", "Failed to load wallet: ${e.message}")
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
            fullName = ed.fullName.takeIf { it != _state.value.profile?.fullName },
            phone = ed.phone,
            location = ed.location,
            bio = ed.bio,
            languages = ed.preferredLanguages.distinctTrimmedOrNull(),
            skills = ed.interests.distinctTrimmedOrNull(),

            //  NEW: Mentor-specific fields
            jobTitle = ed.jobTitle,
            category = ed.category,
            hourlyRateVnd = ed.hourlyRate,
            experience = ed.experience,
            education = ed.education
        )
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
