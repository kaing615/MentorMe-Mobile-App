package com.mentorme.app.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationType

class MentorMeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token updated: $token")
        // TODO: send token to backend when API is ready
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "MentorMe"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new update."
        val type = NotificationType.fromKey(message.data["type"])

        NotificationHelper.showNotification(this, title, body, type)
        NotificationStore.add(
            NotificationItem(
                title = title,
                body = body,
                type = type,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    companion object {
        private const val TAG = "MentorMeFCM"
    }
}
