package com.mentorme.app.core.notifications

import com.mentorme.app.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationStore {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun contains(id: String): Boolean {
        return _notifications.value.any { it.id == id }
    }

    fun setAll(items: List<NotificationItem>) {
        _notifications.value = items.sortedByDescending { it.timestamp }
    }

    fun seed(items: List<NotificationItem>) {
        if (_notifications.value.isEmpty()) {
            _notifications.value = items.sortedByDescending { it.timestamp }
        }
    }

    fun merge(items: List<NotificationItem>) {
        _notifications.update { current ->
            val merged = LinkedHashMap<String, NotificationItem>()
            items.forEach { merged[it.id] = it }
            current.forEach { if (!merged.containsKey(it.id)) merged[it.id] = it }
            merged.values.sortedByDescending { it.timestamp }
        }
    }

    fun add(item: NotificationItem) {
        _notifications.update { current ->
            if (current.any { it.id == item.id }) current
            else listOf(item) + current
        }
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
