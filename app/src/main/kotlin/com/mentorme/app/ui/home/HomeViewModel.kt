package com.mentorme.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.data.repository.ProfileRepository
import com.mentorme.app.domain.usecase.SearchMentorsUseCase
import com.mentorme.app.domain.usecase.profile.GetMeUseCase
import com.mentorme.app.domain.usecase.home.GetHomeStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class WaitingSession(
    val bookingId: String,
    val menteeUserId: String?,
    val menteeName: String?
)

/**
 * UI model for mentee's upcoming sessions
 */
data class MenteeUpcomingSessionUi(
    val id: String,
    val mentorName: String,
    val topic: String,
    val time: String,
    val avatarInitial: String,
    val avatarUrl: String?,
    val isStartingSoon: Boolean,
    val canJoin: Boolean
)

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
    val onlineCount: Int = 0,
    // Session waiting
    val waitingSession: WaitingSession? = null,
    // Upcoming sessions for mentee
    val upcomingSessions: List<MenteeUpcomingSessionUi> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchMentorsUseCase: SearchMentorsUseCase,
    private val getMeUseCase: GetMeUseCase,
    private val getHomeStatsUseCase: GetHomeStatsUseCase,
    private val bookingRepository: BookingRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeRealtimeEvents()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        loadData()
    }

    fun dismissWaitingSession() {
        _uiState.update { it.copy(waitingSession = null) }
    }

    private fun observeRealtimeEvents() {
        viewModelScope.launch {
            RealtimeEventBus.events.collect { event ->
                when (event) {
                    is RealtimeEvent.SessionParticipantJoined -> {
                        // Khi mentee join vào session, hiển thị banner cho mentor
                        if (event.payload.role == "mentee") {
                            val bookingId = event.payload.bookingId ?: return@collect
                            _uiState.update {
                                it.copy(
                                    waitingSession = WaitingSession(
                                        bookingId = bookingId,
                                        menteeUserId = event.payload.userId,
                                        menteeName = null // Có thể fetch tên mentee nếu cần
                                    )
                                )
                            }
                        }
                    }
                    is RealtimeEvent.SessionReady -> {
                        // Khi session đã ready (mentor đã admit), ẩn banner
                        val bookingId = event.payload.bookingId ?: return@collect
                        if (_uiState.value.waitingSession?.bookingId == bookingId) {
                            _uiState.update { it.copy(waitingSession = null) }
                        }
                    }
                    is RealtimeEvent.SessionEnded -> {
                        // Khi session kết thúc, ẩn banner
                        val bookingId = event.payload.bookingId ?: return@collect
                        if (_uiState.value.waitingSession?.bookingId == bookingId) {
                            _uiState.update { it.copy(waitingSession = null) }
                        }
                    }
                    else -> Unit
                }
            }
        }
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
                            val user = meResult.data.user
                            val userEmail = user?.email ?: "user"
                            
                            // Try multiple sources for name
                            var name = listOf(
                                profile?.fullName,
                                user?.fullName,
                                user?.name,
                                user?.userName
                            ).firstOrNull { !it.isNullOrBlank() }
                            
                            // Try multiple sources for avatar
                            var avatarUrl = listOf(
                                profile?.avatarUrl,
                                user?.avatarUrl,
                                user?.avatar
                            ).firstOrNull { !it.isNullOrBlank() }
                            
                            android.util.Log.d(TAG, "🔍 getMeUseCase: profile.fullName=${profile?.fullName}, user.fullName=${user?.fullName}, user.name=${user?.name}, user.userName=${user?.userName}")
                            
                            // If name still not found, try getProfileMe API as fallback
                            if (name.isNullOrBlank()) {
                                android.util.Log.d(TAG, "🔍 Name not found in /auth/me, trying /profile/me...")
                                when (val profileMeResult = profileRepository.getProfileMe()) {
                                    is AppResult.Success -> {
                                        val profileMe = profileMeResult.data.profile
                                        android.util.Log.d(TAG, "🔍 getProfileMe: fullName=${profileMe?.fullName}")
                                        if (!profileMe?.fullName.isNullOrBlank()) {
                                            name = profileMe?.fullName
                                        }
                                        if (avatarUrl.isNullOrBlank() && !profileMe?.avatarUrl.isNullOrBlank()) {
                                            avatarUrl = profileMe?.avatarUrl
                                        }
                                    }
                                    is AppResult.Error -> {
                                        android.util.Log.w(TAG, "Failed to load profile/me: ${profileMeResult.throwable}")
                                    }
                                    AppResult.Loading -> Unit
                                }
                            }
                            
                            // Final fallback to email prefix
                            if (name.isNullOrBlank()) {
                                name = userEmail.substringBefore("@")
                            }
                            
                            android.util.Log.d(TAG, "🔍 Final resolved name: $name")
                            
                            _uiState.update {
                                it.copy(
                                    userName = name,
                                    userAvatar = avatarUrl
                                )
                            }
                        }
                        is AppResult.Error -> {
                            android.util.Log.w(TAG, "Failed to load user info: ${meResult.throwable}")
                        }
                        AppResult.Loading -> Unit
                    }
                }

                // Load upcoming sessions for mentee
                launch {
                    when (val bookingsResult = bookingRepository.getBookings(
                        role = "mentee",
                        status = null,
                        page = 1,
                        limit = 50
                    )) {
                        is AppResult.Success -> {
                            val sessions = findUpcomingSessions(bookingsResult.data.bookings)
                            _uiState.update { it.copy(upcomingSessions = sessions) }
                            android.util.Log.d(TAG, "Loaded ${sessions.size} upcoming sessions for mentee")
                        }
                        is AppResult.Error -> {
                            android.util.Log.w(TAG, "Failed to load bookings: ${bookingsResult.throwable}")
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

    /**
     * Find all upcoming sessions (max 5) for the mentee
     */
    private fun findUpcomingSessions(bookings: List<Booking>): List<MenteeUpcomingSessionUi> {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        // Filter: CONFIRMED + not ended
        val upcoming = bookings
            .filter { booking ->
                if (booking.status != BookingStatus.CONFIRMED) return@filter false

                try {
                    val bookingDate = LocalDate.parse(booking.date)
                    val endTime = LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val bookingEndDateTime = bookingDate.atTime(endTime).atZone(zoneId).toInstant()
                    bookingEndDateTime.isAfter(now)
                } catch (e: Exception) {
                    false
                }
            }
            .sortedWith(compareBy({ it.date }, { it.startTime }))
            .take(5)

        return upcoming.mapNotNull { booking ->
            try {
                val bookingDate = LocalDate.parse(booking.date)
                val startTime = LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                val endTime = LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                
                // Prefer ISO timestamps from server for accurate timezone handling
                val bookingStartInstant = booking.startTimeIso?.let { iso ->
                    runCatching { Instant.parse(iso) }.getOrNull()
                } ?: bookingDate.atTime(startTime).atZone(zoneId).toInstant()
                
                val bookingEndInstant = booking.endTimeIso?.let { iso ->
                    runCatching { Instant.parse(iso) }.getOrNull()
                } ?: bookingDate.atTime(endTime).atZone(zoneId).toInstant()

                // Format time display
                val timeDisplay = when {
                    bookingDate == today -> {
                        val fmt = DateTimeFormatter.ofPattern("HH:mm")
                        "${startTime.format(fmt)} - ${endTime.format(fmt)} hôm nay"
                    }
                    bookingDate == today.plusDays(1) -> {
                        val fmt = DateTimeFormatter.ofPattern("HH:mm")
                        "${startTime.format(fmt)} - ${endTime.format(fmt)} ngày mai"
                    }
                    else -> {
                        val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
                        "${timeFmt.format(startTime)} - ${timeFmt.format(endTime)}, ${dateFmt.format(bookingDate)}"
                    }
                }

                // Calculate join window: 20 min before start to 15 min after end
                val joinWindowStart = bookingStartInstant.minus(java.time.Duration.ofMinutes(20))
                val joinWindowEnd = bookingEndInstant.plus(java.time.Duration.ofMinutes(15))
                
                // Check if can join - must be within join window
                val canJoin = now.isAfter(joinWindowStart) && now.isBefore(joinWindowEnd)

                // Check if starting soon (within 30 min of start time)
                val isStartingSoon = now.isAfter(bookingStartInstant.minus(java.time.Duration.ofMinutes(30)))
                    && now.isBefore(bookingEndInstant)
                
                android.util.Log.d(TAG, "Session ${booking.id}: startIso=${booking.startTimeIso}, endIso=${booking.endTimeIso}, start=$bookingStartInstant, end=$bookingEndInstant, now=$now, canJoin=$canJoin")

                // Get mentor info
                val mentorName = booking.mentorFullName 
                    ?: booking.mentor?.fullName 
                    ?: "Mentor ${booking.mentorId.takeLast(6)}"
                val avatarInitial = if (mentorName.startsWith("Mentor ")) "M" 
                    else mentorName.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
                val avatarUrl = booking.mentor?.avatar

                MenteeUpcomingSessionUi(
                    id = booking.id,
                    mentorName = mentorName,
                    topic = booking.topic ?: "Buổi tư vấn",
                    time = timeDisplay,
                    avatarInitial = avatarInitial,
                    avatarUrl = avatarUrl,
                    isStartingSoon = isStartingSoon,
                    canJoin = canJoin
                )
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error mapping booking ${booking.id}: ${e.message}")
                null
            }
        }
    }

}
