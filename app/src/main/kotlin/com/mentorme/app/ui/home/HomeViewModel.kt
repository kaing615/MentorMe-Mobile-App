package com.mentorme.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.domain.usecase.SearchMentorsUseCase
import com.mentorme.app.domain.usecase.profile.GetMeUseCase
import com.mentorme.app.domain.usecase.home.GetHomeStatsUseCase
import com.mentorme.app.domain.usecase.home.PingPresenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "Bạn",
    val userAvatar: String? = null,
    val featuredMentors: List<Mentor> = emptyList(),
    val topMentors: List<Mentor> = emptyList(),
    val isRefreshing: Boolean = false,
    // Stats from backend
    val mentorCount: Int = 0,
    val sessionCount: Int = 0,
    val avgRating: Double = 0.0,
    val onlineCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchMentorsUseCase: SearchMentorsUseCase,
    private val getMeUseCase: GetMeUseCase,
    private val getHomeStatsUseCase: GetHomeStatsUseCase,
    private val pingPresenceUseCase: PingPresenceUseCase
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startPresencePing()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Load stats, user profile, and mentors in parallel
                launch {
                    when (val statsResult = getHomeStatsUseCase()) {
                        is AppResult.Success -> {
                            val stats = statsResult.data
                            _uiState.update {
                                it.copy(
                                    mentorCount = stats.mentorCount,
                                    sessionCount = stats.sessionCount,
                                    avgRating = stats.avgRating,
                                    onlineCount = stats.onlineCount
                                )
                            }
                        }
                        is AppResult.Error -> {
                            android.util.Log.w(TAG, "Failed to load stats: ${statsResult.throwable}")
                        }
                        AppResult.Loading -> Unit
                    }
                }

                // Load user info (optional, non-blocking)
                launch {
                    when (val meResult = getMeUseCase()) {
                        is AppResult.Success -> {
                            val profile = meResult.data.profile
                            val userEmail = meResult.data.user?.email ?: "user"
                            val name = profile?.fullName?.takeIf { it.isNotBlank() }
                                ?: userEmail.substringBefore("@")
                            _uiState.update {
                                it.copy(
                                    userName = name,
                                    userAvatar = profile?.avatarUrl
                                )
                            }
                        }
                        is AppResult.Error -> {
                            android.util.Log.w(TAG, "Failed to load user info: ${meResult.throwable}")
                        }
                        AppResult.Loading -> Unit
                    }
                }

                // Load featured mentors (main content)
                when (val mentorsResult = searchMentorsUseCase(
                    q = null,
                    skills = emptyList(),
                    minRating = null,
                    priceMin = null,
                    priceMax = null,
                    sort = "rating",
                    page = 1,
                    limit = 20
                )) {
                    is AppResult.Success -> {
                        val mentors = mentorsResult.data
                        val top = mentors
                            .sortedByDescending { it.rating }
                            .take(4)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                featuredMentors = mentors.take(6),
                                topMentors = top,
                                errorMessage = null
                            )
                        }
                        android.util.Log.d(TAG, "Loaded ${mentors.size} mentors successfully")
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = mentorsResult.throwable
                            )
                        }
                        android.util.Log.e(TAG, "Failed to load mentors: ${mentorsResult.throwable}")
                    }
                    AppResult.Loading -> Unit
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Đã có lỗi xảy ra: ${e.message}"
                    )
                }
                android.util.Log.e(TAG, "Error loading data: ${e.message}", e)
            }
        }
    }

    private fun startPresencePing() {
        viewModelScope.launch {
            // Initial ping
            pingPresenceUseCase()

            // Ping every 90 seconds to keep presence alive (TTL is 120s)
            while (true) {
                delay(90_000) // 90 seconds
                pingPresenceUseCase()
            }
        }
    }
}

