package com.mentorme.app.core.webrtc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mentorme.app.MainActivity
import com.mentorme.app.R

/**
 * Foreground service for screen capture (required on Android 10+)
 */
class ScreenCaptureService : Service() {

    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val CHANNEL_ID = "screen_capture_channel"
        private const val NOTIFICATION_ID = 2001
        
        const val ACTION_START = "com.mentorme.app.action.START_SCREEN_CAPTURE"
        const val ACTION_STOP = "com.mentorme.app.action.STOP_SCREEN_CAPTURE"
        
        @Volatile
        private var isRunning = false
        
        @Volatile
        private var isForegroundStarted = false
        
        fun isServiceRunning(): Boolean = isRunning && isForegroundStarted
        
        fun start(context: Context) {
            if (isRunning && isForegroundStarted) {
                Log.d(TAG, "Service already running")
                return
            }
            isForegroundStarted = false  // Reset before starting
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            if (!isRunning) {
                Log.d(TAG, "Service not running")
                return
            }
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Starting foreground service for screen capture")
                startForeground(NOTIFICATION_ID, createNotification())
                isRunning = true
                isForegroundStarted = true  // Set AFTER startForeground succeeds
                Log.d(TAG, "Foreground service started successfully")
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stopping foreground service")
                isForegroundStarted = false
                isRunning = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isForegroundStarted = false
        isRunning = false
        Log.d(TAG, "Service destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Sharing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Screen sharing is active"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Sharing")
            .setContentText("You are sharing your screen")
            .setSmallIcon(R.drawable.ic_screen_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
