package com.mentorme.app.core.notifications

import com.mentorme.app.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationStore {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun seed(items: List<NotificationItem>) {
        if (_notifications.value.isEmpty()) {
            _notifications.value = items
        }
    }

    fun add(item: NotificationItem) {
        _notifications.update { current -> listOf(item) + current }
    }

    fun markAllRead() {
        _notifications.update { current -> current.map { it.copy(read = true) } }
    }

    fun markRead(id: String) {
        _notifications.update { current ->
            current.map { if (it.id == id) it.copy(read = true) else it }
        }
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}
