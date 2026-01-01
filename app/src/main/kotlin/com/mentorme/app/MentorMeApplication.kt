package com.mentorme.app

import android.app.Application
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.mentorme.app.core.appstate.AppForegroundTracker
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.core.notifications.PushTokenManager
import com.mentorme.app.core.presence.PresencePingManager
import com.mentorme.app.core.realtime.SocketManager
import com.mentorme.app.data.session.SessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltAndroidApp
class MentorMeApplication : Application() {

    @Inject
    lateinit var pushTokenManager: PushTokenManager
    @Inject
    lateinit var socketManager: SocketManager
    @Inject
    lateinit var presencePingManager: PresencePingManager
    @Inject lateinit var dataStoreManager: DataStoreManager

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            val token = dataStoreManager.getToken().firstOrNull()
            SessionManager.setToken(token)
        }
        AppForegroundTracker.init(this)
        NotificationHelper.ensureChannels(this)
        socketManager.start()
        presencePingManager.start()
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
