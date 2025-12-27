package com.mentorme.app.data.repository.notifications

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.notifications.NotificationListPayload
import com.mentorme.app.data.model.NotificationPreferences

interface NotificationRepository {
    suspend fun getNotifications(
        read: Boolean? = null,
        type: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): AppResult<NotificationListPayload>

    suspend fun getUnreadCount(): AppResult<Int>

    suspend fun markRead(id: String): AppResult<Boolean>

    suspend fun markAllRead(): AppResult<Int>

    suspend fun getPreferences(): AppResult<NotificationPreferences>

    suspend fun updatePreferences(prefs: NotificationPreferences): AppResult<NotificationPreferences>
}
