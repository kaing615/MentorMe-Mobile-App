package com.mentorme.app.core.notifications

import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mentorme.app.core.appstate.AppForegroundTracker
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
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
    @Inject
    lateinit var notificationCache: NotificationCache

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
        val route = NotificationDeepLink.routeFor(message.data)
        val deepLink = route.takeUnless { it == NotificationDeepLink.ROUTE_NOTIFICATIONS }

        Log.d(TAG, "FCM message received: title=$title body=$body data=${message.data}")
        val serverId = message.data["notificationId"]
        val pushEnabled = NotificationPreferencesStore.prefs.value.isPushEnabled(type)
        
        // Show notification when app is in background
        // When app is killed, Android system auto-shows notification from FCM payload
        if (!AppForegroundTracker.isForeground.value && pushEnabled &&
            NotificationDeduper.shouldNotify(serverId, title, body, type)
        ) {
            NotificationHelper.showNotification(this, title, body, type, route)
        }
        val item = NotificationItem(
            id = serverId ?: java.util.UUID.randomUUID().toString(),
            title = title,
            body = body,
            type = type,
            timestamp = System.currentTimeMillis(),
            deepLink = deepLink
        )
        NotificationStore.add(item)
        CoroutineScope(Dispatchers.IO).launch {
            notificationCache.save(NotificationStore.notifications.value)
        }
        if (AppForegroundTracker.isForeground.value) {
            RealtimeEventBus.emit(RealtimeEvent.NotificationReceived(item, null))
        }
    }

    companion object {
        private const val TAG = "MentorMeFCM"
    }
}
