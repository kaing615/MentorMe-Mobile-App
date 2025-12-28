package com.mentorme.app.data.repository.notifications

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.notifications.NotificationListPayload
import com.mentorme.app.data.dto.notifications.NotificationPreferencesDto
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.model.NotificationPreferences
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

    override suspend fun getPreferences(): AppResult<NotificationPreferences> = withContext(Dispatchers.IO) {
        try {
            val res = api.getNotificationPreferences()
            if (res.isSuccessful) {
                val dto = res.body()?.data
                val prefs = dto?.toModel() ?: NotificationPreferences()
                AppResult.success(prefs)
            } else {
                AppResult.failure("HTTP ${res.code()}: ${res.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to load notification preferences")
        }
    }

    override suspend fun updatePreferences(
        prefs: NotificationPreferences
    ): AppResult<NotificationPreferences> = withContext(Dispatchers.IO) {
        try {
            val res = api.updateNotificationPreferences(prefs.toDto())
            if (res.isSuccessful) {
                val dto = res.body()?.data
                val updated = dto?.toModel() ?: prefs
                AppResult.success(updated)
            } else {
                AppResult.failure("HTTP ${res.code()}: ${res.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to update notification preferences")
        }
    }
}

private fun NotificationPreferencesDto.toModel(): NotificationPreferences {
    return NotificationPreferences(
        pushBooking = pushBooking ?: true,
        pushPayment = pushPayment ?: true,
        pushMessage = pushMessage ?: true,
        pushSystem = pushSystem ?: true
    )
}

private fun NotificationPreferences.toDto(): NotificationPreferencesDto {
    return NotificationPreferencesDto(
        pushBooking = pushBooking,
        pushPayment = pushPayment,
        pushMessage = pushMessage,
        pushSystem = pushSystem
    )
}
