package com.mentorme.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.mapper.toNotificationItem
import com.mentorme.app.data.repository.notifications.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun refresh(read: Boolean? = null, type: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            when (val res = notificationRepository.getNotifications(read = read, type = type, page = 1, limit = 50)) {
                is AppResult.Success -> {
                    val items = res.data.items.orEmpty().mapNotNull { it.toNotificationItem() }
                    NotificationStore.setAll(items)
                }
                is AppResult.Error -> Logx.e(TAG, { "load notifications failed: ${res.throwable}" })
                AppResult.Loading -> Unit
            }
            _loading.value = false
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            NotificationStore.markRead(id)
            when (val res = notificationRepository.markRead(id)) {
                is AppResult.Error -> Logx.e(TAG, { "mark read failed: ${res.throwable}" })
                else -> Unit
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            NotificationStore.markAllRead()
            when (val res = notificationRepository.markAllRead()) {
                is AppResult.Error -> Logx.e(TAG, { "mark all read failed: ${res.throwable}" })
                else -> Unit
            }
        }
    }

    private companion object {
        const val TAG = "NotificationsVM"
    }
}
