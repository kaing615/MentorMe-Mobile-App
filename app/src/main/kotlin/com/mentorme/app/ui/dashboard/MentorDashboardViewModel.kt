package com.mentorme.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.dto.mentor.MentorStatsDto
import com.mentorme.app.data.dto.review.ReviewDto
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.data.repository.review.ReviewRepository
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.ui.calendar.toMentorUpcomingUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val upcomingSessions: List<UpcomingSessionUi>,  // Changed to list for swipeable cards
        val stats: MentorStatsDto,
        val recentReviews: List<ReviewDto>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

data class UpcomingSessionUi(
    val id: String,
    val menteeName: String,
    val topic: String,
    val time: String,
    val avatarInitial: String,
    val avatarUrl: String?,  // Avatar URL for profile image
    val isOngoing: Boolean,  // Session has started and is currently happening
    val isStartingSoon: Boolean,  // Session is upcoming within 30 min (but hasn't started)
    val canJoin: Boolean
)

@HiltViewModel
class MentorDashboardViewModel @Inject constructor(
    private val api: MentorMeApi,
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val TAG = "MentorDashboardVM"

    init {
        loadDashboard()
        observeRealtimeEvents()
    }

    fun refresh() {
        loadDashboard()
    }

    /**
     * Observe realtime events to auto-refresh dashboard when bookings change
     */
    private fun observeRealtimeEvents() {
        viewModelScope.launch {
            RealtimeEventBus.events.collect { event ->
                when (event) {
                    is RealtimeEvent.BookingCreated -> {
                        // When a new booking is created, refresh dashboard
                        Logx.d(TAG) { "🔄 Booking created: ${event.bookingId}, refreshing dashboard" }
                        loadDashboard()
                    }
                    is RealtimeEvent.BookingCancelled -> {
                        // When booking is cancelled, refresh dashboard
                        Logx.d(TAG) { "🔄 Booking cancelled: ${event.bookingId}, refreshing dashboard" }
                        loadDashboard()
                    }
                    is RealtimeEvent.BookingChanged -> {
                        // When booking status changes (e.g. confirmed, paid), refresh dashboard
                        Logx.d(TAG) { "🔄 Booking changed: ${event.bookingId}, refreshing dashboard and stats" }
                        loadDashboard()
                    }
                    is RealtimeEvent.SessionEnded -> {
                        // When session ends, refresh to remove from upcoming
                        Logx.d(TAG) { "🔄 Session ended: ${event.payload.bookingId}, refreshing dashboard" }
                        loadDashboard()
                    }
                    else -> {
                        // Ignore other events
                    }
                }
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Loading

                // 1. Load upcoming bookings - lấy TẤT CẢ bookings rồi filter trên client
                Logx.d(TAG) { "🔄 Refreshing bookings (mentor role) - fetching ALL bookings" }

                val bookingsResult = bookingRepository.getBookings(
                    role = "mentor",
                    status = null, // Get ALL bookings, filter on client side
                    page = 1,
                    limit = 100
                )

                val upcomingBooking = when (bookingsResult) {
                    is AppResult.Success -> {
                        val bookings = bookingsResult.data.bookings
                        Logx.d(TAG) { "✅ Repository Success: Loaded ${bookings.size} bookings (normalized)" }

                        if (bookings.isEmpty()) {
                            Logx.d(TAG) { "⚠️ No bookings found. Mentor may not have confirmed bookings." }
                        } else {
                            bookings.forEachIndexed { index, booking ->
                                Logx.d(TAG) {
                                    "📋 Booking #${index + 1}: " +
                                    "id=${booking.id}, " +
                                    "status=${booking.status}, " +
                                    "date=${booking.date}, " +
                                    "time=${booking.startTime}-${booking.endTime}, " +
                                    "menteeId=${booking.menteeId}, " +
                                    "menteeFullName=${booking.menteeFullName}"
                                }
                            }
                        }

                        findUpcomingSessions(bookings)
                    }
                    is AppResult.Error -> {
                        Logx.e(tag = TAG, message = { "❌ Repository Error: ${bookingsResult.throwable}" })
                        emptyList()
                    }
                    AppResult.Loading -> emptyList()
                }

                // 2. Load stats
                val statsResp = api.getMentorStats()
                val stats = if (statsResp.isSuccessful) {
                    statsResp.body()?.data ?: MentorStatsDto(0, 0, 0.0, 0.0)
                } else {
                    Logx.e(tag = TAG, message = { "Failed to load stats: ${statsResp.code()}" })
                    MentorStatsDto(0, 0, 0.0, 0.0)
                }

                // 3. Load recent reviews for this mentor
                val currentUserId = dataStoreManager.getUserId().first()
                Logx.d(TAG) { "Loading reviews for mentor: $currentUserId" }

                val reviews: List<ReviewDto> = if (currentUserId != null) {
                    val reviewsResult = reviewRepository.getMentorReviews(
                        mentorId = currentUserId,
                        limit = 3
                    )
                    when (reviewsResult) {
                        is com.mentorme.app.core.utils.AppResult.Success -> {
                            Logx.d(TAG) { "Loaded ${reviewsResult.data.first.size} reviews" }
                            reviewsResult.data.first
                        }
                        is com.mentorme.app.core.utils.AppResult.Error -> {
                            Logx.e(tag = TAG, message = { "Failed to load reviews: ${reviewsResult.throwable}" })
                            emptyList()
                        }
                        else -> emptyList()
                    }
                } else {
                    Logx.e(tag = TAG, message = { "No user ID found, cannot load reviews" })
                    emptyList()
                }

                _uiState.value = DashboardUiState.Success(
                    upcomingSessions = upcomingBooking,
                    stats = stats,
                    recentReviews = reviews
                )

            } catch (e: Exception) {
                Logx.e(tag = TAG, message = { "Error loading dashboard: ${e.message}" })
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Find all upcoming sessions (max 5) for the swipeable cards
     */
    private fun findUpcomingSessions(bookings: List<Booking>): List<UpcomingSessionUi> {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        // Filter bookings: chỉ lấy CONFIRMED và chưa qua giờ
        val upcomingBookings = bookings
            .filter { booking ->
                // Must be CONFIRMED status
                if (booking.status != com.mentorme.app.data.model.BookingStatus.CONFIRMED) {
                    return@filter false
                }

                try {
                    val bookingDate = LocalDate.parse(booking.date)
                    // ✅ FIX: Handle both HH:mm and H:mm formats
                    val endTime = try {
                        LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("H:mm"))
                    }
                    val bookingEndDateTime = bookingDate.atTime(endTime).atZone(zoneId).toInstant()

                    // Lấy các booking CHƯA KẾT THÚC (endTime > now)
                    val isNotEnded = bookingEndDateTime.isAfter(now)

                    if (!isNotEnded) {
                        Logx.d(TAG) { "Booking ${booking.id} ended: ${booking.date} ${booking.endTime}" }
                    }

                    isNotEnded
                } catch (e: Exception) {
                    Logx.e(tag = TAG, message = { "Error parsing booking date/time: ${e.message}" })
                    false
                }
            }
            .sortedWith(compareBy({ it.date }, { it.startTime })) // Sort by date then time
            .take(5) // Limit to 5 upcoming sessions for swipeable cards

        Logx.d(TAG) { "Found ${upcomingBookings.size} upcoming confirmed bookings" }

        if (upcomingBookings.isEmpty()) {
            Logx.d(TAG) { "No upcoming sessions found" }
            return emptyList()
        }

        // Map each booking to UpcomingSessionUi
        return upcomingBookings.mapNotNull { booking ->
            try {
                val bookingDate = LocalDate.parse(booking.date)
                // ✅ FIX: Handle both HH:mm and H:mm formats
                val startTime = try {
                    LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) {
                    LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("H:mm"))
                }
                val endTime = try {
                    LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) {
                    LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("H:mm"))
                }

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
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        "${startTime.format(formatter)} - ${endTime.format(formatter)} hôm nay"
                    }
                    bookingDate == today.plusDays(1) -> {
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        "${startTime.format(formatter)} - ${endTime.format(formatter)} ngày mai"
                    }
                    else -> {
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                        "${timeFormatter.format(startTime)} - ${timeFormatter.format(endTime)}, ${dateFormatter.format(bookingDate)}"
                    }
                }

                // Calculate join window: 20 min before start to 15 min after end (matching server)
                val joinWindowStart = bookingStartInstant.minus(java.time.Duration.ofMinutes(20))
                val joinWindowEnd = bookingEndInstant.plus(java.time.Duration.ofMinutes(15))
                
                // Check if can join - must be within join window
                val canJoin = now.isAfter(joinWindowStart) && now.isBefore(joinWindowEnd)

                // ✅ FIX: Proper status logic
                // "Đang diễn ra": session has started and not yet ended
                val isOngoing = now.isAfter(bookingStartInstant) && now.isBefore(bookingEndInstant)

                // "Sắp diễn ra": within 30 min before start time (but hasn't started yet)
                val isStartingSoon = now.isAfter(bookingStartInstant.minus(java.time.Duration.ofMinutes(30)))
                    && now.isBefore(bookingStartInstant)

                Logx.d(TAG) { "Session ${booking.id}: startIso=${booking.startTimeIso}, start=$bookingStartInstant, end=$bookingEndInstant, now=$now, canJoin=$canJoin, isOngoing=$isOngoing, isStartingSoon=$isStartingSoon" }

                // Use mapper to get correct mentee name and avatar
                val mappedUi = booking.toMentorUpcomingUi()

                UpcomingSessionUi(
                    id = booking.id,
                    menteeName = mappedUi.menteeName,
                    topic = booking.topic ?: "Buổi tư vấn",
                    time = timeDisplay,
                    avatarInitial = mappedUi.avatarInitial,
                    avatarUrl = mappedUi.avatarUrl,
                    isOngoing = isOngoing, // ✅ FIX: Pass isOngoing separately
                    isStartingSoon = isStartingSoon, // ✅ FIX: Only true when session hasn't started but within 30 min
                    canJoin = canJoin
                )
            } catch (e: Exception) {
                Logx.e(tag = TAG, message = { "Error mapping booking ${booking.id}: ${e.message}" })
                null
            }
        }
    }
}
