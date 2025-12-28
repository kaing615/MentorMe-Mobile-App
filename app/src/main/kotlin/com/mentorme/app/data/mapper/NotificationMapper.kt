package com.mentorme.app.data.mapper

import com.mentorme.app.core.notifications.NotificationDeepLink
import com.mentorme.app.data.dto.notifications.NotificationDto
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationType
import java.time.Instant

private fun parseIsoMillis(iso: String?): Long {
    if (iso.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        Instant.parse(iso).toEpochMilli()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

private fun mapToNotificationItem(
    id: String?,
    type: String?,
    title: String?,
    body: String?,
    data: Map<String, Any?>?,
    read: Boolean?,
    createdAt: String?
): NotificationItem? {
    val itemId = id?.trim().orEmpty()
    if (itemId.isBlank()) return null
    val route = NotificationDeepLink.routeFor(data)
    val deepLink = route.takeUnless { it == NotificationDeepLink.ROUTE_NOTIFICATIONS }

    return NotificationItem(
        id = itemId,
        title = title ?: "MentorMe",
        body = body ?: "",
        type = NotificationType.fromKey(type),
        timestamp = parseIsoMillis(createdAt),
        read = read ?: false,
        deepLink = deepLink
    )
}

fun NotificationDto.toNotificationItem(): NotificationItem? {
    return mapToNotificationItem(
        id = id,
        type = type,
        title = title,
        body = body,
        data = data,
        read = read,
        createdAt = createdAt
    )
}

data class NotificationSocketPayload(
    val id: String? = null,
    val userId: String? = null,
    val type: String? = null,
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, Any?>? = null,
    val read: Boolean? = null,
    val createdAt: String? = null
)

fun NotificationSocketPayload.toNotificationItem(): NotificationItem? {
    return mapToNotificationItem(
        id = id,
        type = type,
        title = title,
        body = body,
        data = data,
        read = read,
        createdAt = createdAt
    )
}
