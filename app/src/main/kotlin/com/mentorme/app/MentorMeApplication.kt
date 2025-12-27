package com.mentorme.app

import android.app.Application
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.core.notifications.PushTokenManager
import com.mentorme.app.core.realtime.SocketManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MentorMeApplication : Application() {

    @Inject
    lateinit var pushTokenManager: PushTokenManager
    @Inject
    lateinit var socketManager: SocketManager

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        socketManager.start()
        // Fetch FCM token at startup and try to register when logged in.
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("MentorMeFCM", "Manual token: $token")
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                CoroutineScope(Dispatchers.IO).launch {
                    pushTokenManager.onNewToken(token, deviceId, "app_start")
                }
            }
            .addOnFailureListener { err ->
                Log.e("MentorMeFCM", "Failed to fetch FCM token", err)
            }
    }
}
