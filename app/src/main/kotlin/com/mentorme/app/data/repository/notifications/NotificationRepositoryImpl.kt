package com.mentorme.app.data.repository.notifications

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.notifications.NotificationListPayload
import com.mentorme.app.data.remote.MentorMeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: MentorMeApi
) : NotificationRepository {

    override suspend fun getNotifications(
        read: Boolean?,
        type: String?,
        page: Int,
        limit: Int
    ): AppResult<NotificationListPayload> = withContext(Dispatchers.IO) {
        try {
            val res = api.getNotifications(read = read, type = type, page = page, limit = limit)
            if (res.isSuccessful) {
                val payload = res.body()?.data
                if (payload != null) {
                    AppResult.success(payload)
                } else {
                    AppResult.failure("Empty response body")
                }
            } else {
                val err = res.errorBody()?.string()
                AppResult.failure("HTTP ${res.code()}: ${err ?: res.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to load notifications")
        }
    }

    override suspend fun getUnreadCount(): AppResult<Int> = withContext(Dispatchers.IO) {
        try {
            val res = api.getUnreadNotificationCount()
            if (res.isSuccessful) {
                val unread = res.body()?.data?.unread ?: 0
                AppResult.success(unread)
            } else {
                AppResult.failure("HTTP ${res.code()}: ${res.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to load unread count")
        }
    }

    override suspend fun markRead(id: String): AppResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res = api.markNotificationRead(id)
            if (res.isSuccessful) {
                val read = res.body()?.data?.read ?: false
                AppResult.success(read)
            } else {
                AppResult.failure("HTTP ${res.code()}: ${res.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to mark notification as read")
        }
    }

    override suspend fun markAllRead(): AppResult<Int> = withContext(Dispatchers.IO) {
        try {
            val res = api.markAllNotificationsRead()
            if (res.isSuccessful) {
                val updated = res.body()?.data?.updated ?: 0
                AppResult.success(updated)
            } else {
                AppResult.failure("HTTP ${res.code()}: ${res.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to mark notifications as read")
        }
    }
}
