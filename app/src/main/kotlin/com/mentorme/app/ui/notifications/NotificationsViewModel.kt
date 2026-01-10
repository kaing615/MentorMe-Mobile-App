package com.mentorme.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.notifications.NotificationCache
import com.mentorme.app.core.notifications.NotificationPreferencesStore
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.mapper.toNotificationItem
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationPreferences
import com.mentorme.app.data.repository.notifications.NotificationRepository
import com.mentorme.app.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val notificationCache: NotificationCache,
    private val bookingRepository: BookingRepository
) : ViewModel() {
    enum class JoinWindowState {
        CAN_JOIN,
        TOO_EARLY,
        ENDED,
        UNKNOWN
    }

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _preferences = MutableStateFlow(NotificationPreferences())
    val preferences: StateFlow<NotificationPreferences> = _preferences.asStateFlow()

    suspend fun getJoinWindowState(bookingId: String): JoinWindowState {
        return try {
            when (val result = bookingRepository.getBookingById(bookingId)) {
                is AppResult.Success -> {
                    val booking = result.data
                    val startTimeStr = booking.startTimeIso ?: "${booking.date}T${booking.startTime}:00"
                    val endTimeStr = booking.endTimeIso ?: "${booking.date}T${booking.endTime}:00"
                    val startTime = parseZonedDateTime(startTimeStr)
                    val endTime = parseZonedDateTime(endTimeStr)
                    val now = java.time.ZonedDateTime.now()
                    val windowOpen = startTime.minusMinutes(20)

                    when {
                        now.isBefore(windowOpen) -> JoinWindowState.TOO_EARLY
                        now.isAfter(endTime) -> JoinWindowState.ENDED
                        else -> JoinWindowState.CAN_JOIN
                    }
                }
                is AppResult.Error -> {
                    Logx.e(TAG, { "get join window failed: ${result.throwable}" })
                    JoinWindowState.UNKNOWN
                }
                else -> JoinWindowState.UNKNOWN
            }
        } catch (e: Exception) {
            Logx.e(TAG, { "get join window error: ${e.message}" })
            JoinWindowState.UNKNOWN
        }
    }

    /**
     * Validate if a booking can still be joined (session hasn't ended yet)
     * Returns: null if session can be joined, error message if session has ended
     */
    suspend fun validateBookingTime(bookingId: String): String? {
        return try {
            when (val result = bookingRepository.getBookingById(bookingId)) {
                is AppResult.Success -> {
                    val booking = result.data
                    // Parse endTimeIso or combine date + endTime
                    val endTimeStr = booking.endTimeIso ?: "${booking.date}T${booking.endTime}:00"
                    val endTime = try {
                        java.time.ZonedDateTime.parse(endTimeStr)
                    } catch (e: Exception) {
                        // If parsing fails, try with simple format
                        java.time.LocalDateTime.parse(endTimeStr.replace("Z", ""))
                            .atZone(java.time.ZoneId.systemDefault())
                    }
                    val now = java.time.ZonedDateTime.now()
                    
                    if (now.isAfter(endTime)) {
                        "Phiên học đã kết thúc từ lâu. Bạn không thể tham gia nữa."
                    } else {
                        null // Can join
                    }
                }
                is AppResult.Error -> {
                    Logx.e(TAG, { "validate booking failed: ${result.throwable}" })
                    "Không thể kiểm tra thông tin phiên học. Vui lòng thử lại."
                }
                else -> null
            }
        } catch (e: Exception) {
            Logx.e(TAG, { "validate booking time error: ${e.message}" })
            null // Allow join on error to avoid blocking
        }
    }

    private fun parseZonedDateTime(value: String): java.time.ZonedDateTime {
        return try {
            java.time.ZonedDateTime.parse(value)
        } catch (e: Exception) {
            java.time.LocalDateTime.parse(value.replace("Z", ""))
                .atZone(java.time.ZoneId.systemDefault())
        }
    }

    fun refresh(read: Boolean? = null, type: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val res = notificationRepository.getNotifications(read = read, type = type, page = 1, limit = 50)) {
                is AppResult.Success -> {
                    val items = res.data.items.orEmpty().mapNotNull { it.toNotificationItem() }
                    NotificationStore.setAll(items)
                    persistCache(items)
                }
                is AppResult.Error -> Logx.e(TAG, { "load notifications failed: ${res.throwable}" })
                AppResult.Loading -> Unit
            }
            _loading.value = false
            refreshUnreadCount()
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            NotificationStore.markRead(id)
            persistCache()
            when (val res = notificationRepository.markRead(id)) {
                is AppResult.Error -> Logx.e(TAG, { "mark read failed: ${res.throwable}" })
                else -> Unit
            }
            refreshUnreadCount()
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            NotificationStore.markAllRead()
            persistCache()
            when (val res = notificationRepository.markAllRead()) {
                is AppResult.Error -> Logx.e(TAG, { "mark all read failed: ${res.throwable}" })
                else -> Unit
            }
            refreshUnreadCount()
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            when (val res = notificationRepository.getUnreadCount()) {
                is AppResult.Success -> _unreadCount.value = res.data
                is AppResult.Error -> Logx.e(TAG, { "load unread count failed: ${res.throwable}" })
                AppResult.Loading -> Unit
            }
        }
    }

    fun refreshPreferences() {
        viewModelScope.launch {
            when (val res = notificationRepository.getPreferences()) {
                is AppResult.Success -> {
                    _preferences.value = res.data
                    NotificationPreferencesStore.update(res.data)
                }
                is AppResult.Error -> Logx.e(TAG, { "load preferences failed: ${res.throwable}" })
                AppResult.Loading -> Unit
            }
        }
    }

    fun updatePreferences(updated: NotificationPreferences) {
        _preferences.value = updated
        NotificationPreferencesStore.update(updated)
        viewModelScope.launch {
            when (val res = notificationRepository.updatePreferences(updated)) {
                is AppResult.Success -> {
                    _preferences.value = res.data
                    NotificationPreferencesStore.update(res.data)
                }
                is AppResult.Error -> {
                    Logx.e(TAG, { "update preferences failed: ${res.throwable}" })
                    refreshPreferences()
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun restoreCache() {
        viewModelScope.launch {
            if (NotificationStore.notifications.value.isNotEmpty()) return@launch
            val cached = notificationCache.load()
            if (cached.isNotEmpty()) {
                NotificationStore.seed(cached)
            }
        }
    }

    fun ensureNotificationAvailable(notificationId: String) {
        viewModelScope.launch {
            if (NotificationStore.contains(notificationId)) return@launch
            val cached = notificationCache.load()
            if (cached.isNotEmpty()) {
                NotificationStore.merge(cached)
            }
            if (!NotificationStore.contains(notificationId)) {
                refresh()
            }
        }
    }

    fun clearNotifications() {
        NotificationStore.clear()
        viewModelScope.launch(Dispatchers.IO) {
            notificationCache.clear()
        }
    }

    private fun persistCache(items: List<NotificationItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationCache.save(items)
        }
    }

    private fun persistCache() {
        val snapshot = NotificationStore.notifications.value
        if (snapshot.isEmpty()) return
        persistCache(snapshot)
    }

    private companion object {
        const val TAG = "NotificationsVM"
    }
}
