package com.mentorme.app.core.notifications

import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MentorMeMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushTokenManager: PushTokenManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token updated: $token")
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        CoroutineScope(Dispatchers.IO).launch {
            pushTokenManager.onNewToken(token, deviceId, "fcm_refresh")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "MentorMe"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new update."
        val type = NotificationType.fromKey(message.data["type"])

        val serverId = message.data["notificationId"]
        val exists = serverId?.let { NotificationStore.contains(it) } == true
        Log.d(TAG, "FCM message received: title=$title body=$body data=${message.data}")
        if (!exists) {
            NotificationHelper.showNotification(this, title, body, type)
        }
        NotificationStore.add(
            NotificationItem(
                id = serverId ?: java.util.UUID.randomUUID().toString(),
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
