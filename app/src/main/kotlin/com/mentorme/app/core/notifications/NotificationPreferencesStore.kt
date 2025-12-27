package com.mentorme.app.core.notifications

import com.mentorme.app.data.model.NotificationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationPreferencesStore {
    private val _prefs = MutableStateFlow(NotificationPreferences())
    val prefs: StateFlow<NotificationPreferences> = _prefs.asStateFlow()

    fun update(prefs: NotificationPreferences) {
        _prefs.value = prefs
    }

    fun reset() {
        _prefs.value = NotificationPreferences()
    }
}
