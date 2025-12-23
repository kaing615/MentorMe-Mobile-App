package com.mentorme.app.core.notifications

import android.util.Log
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.data.dto.notifications.RegisterDeviceRequest
import com.mentorme.app.data.dto.notifications.UnregisterDeviceRequest
import com.mentorme.app.data.remote.MentorMeApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenManager @Inject constructor(
    private val api: MentorMeApi,
    private val dataStoreManager: DataStoreManager
) {

    suspend fun onNewToken(token: String, deviceId: String? = null, source: String = "unknown") {
        dataStoreManager.saveFcmToken(token)
        registerToken(token, deviceId, source)
    }

    suspend fun registerStoredToken(deviceId: String? = null, source: String = "app_start") {
        val token = dataStoreManager.getFcmToken().first()
        if (token.isNullOrBlank()) {
            Log.d(TAG, "Skip register: no FCM token (source=$source)")
            return
        }
        registerToken(token, deviceId, source)
    }

    suspend fun unregisterStoredToken(source: String = "logout") {
        val authToken = dataStoreManager.getToken().first()
        if (authToken.isNullOrBlank()) {
            Log.d(TAG, "Skip unregister: no auth token (source=$source)")
            return
        }
        val token = dataStoreManager.getFcmToken().first()
        if (token.isNullOrBlank()) {
            Log.d(TAG, "Skip unregister: no FCM token (source=$source)")
            return
        }
        try {
            val res = api.unregisterDeviceToken(UnregisterDeviceRequest(token))
            if (res.isSuccessful) {
                Log.d(TAG, "Unregister token ok (source=$source)")
            } else {
                Log.w(TAG, "Unregister token failed ${res.code()} (source=$source)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unregister token error (source=$source): ${e.message}")
        }
    }

    private suspend fun registerToken(token: String, deviceId: String?, source: String) {
        val authToken = dataStoreManager.getToken().first()
        if (authToken.isNullOrBlank()) {
            Log.d(TAG, "Skip register: no auth token (source=$source)")
            return
        }
        try {
            val res = api.registerDeviceToken(
                RegisterDeviceRequest(
                    token = token,
                    platform = "android",
                    deviceId = deviceId
                )
            )
            if (res.isSuccessful) {
                Log.d(TAG, "Register token ok (source=$source)")
            } else {
                Log.w(TAG, "Register token failed ${res.code()} (source=$source)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Register token error (source=$source): ${e.message}")
        }
    }

    companion object {
        private const val TAG = "PushTokenManager"
    }
}
