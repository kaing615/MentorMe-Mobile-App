package com.mentorme.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mentorme.app.MainActivity
import com.mentorme.app.R
import com.mentorme.app.data.model.NotificationType

object NotificationHelper {
    const val CHANNEL_GENERAL = "mentorme_general"
    const val CHANNEL_BOOKING = "mentorme_booking"
    const val CHANNEL_MESSAGES = "mentorme_messages"
    const val EXTRA_NAV_ROUTE = "nav_route"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_GENERAL,
                "MentorMe Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "System updates and announcements"
                enableVibration(true)
                enableLights(true)
            },
            NotificationChannel(
                CHANNEL_BOOKING,
                "Booking Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Booking confirmations, reminders, and cancellations"
                enableVibration(true)
                enableLights(true)
            },
            NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat messages from mentors"
                enableVibration(true)
                enableLights(true)
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(
        context: Context,
        title: String,
        body: String,
        type: NotificationType = NotificationType.SYSTEM,
        route: String? = null
    ) {
        if (!hasPostPermission(context)) return
        ensureChannels(context)
        val channelId = channelFor(type)
        val targetRoute = route ?: NotificationDeepLink.ROUTE_NOTIFICATIONS

        // ✅ Tạo Intent với action và data để đảm bảo khác biệt giữa các notification
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            // ✅ Thêm các flags cần thiết để mở app từ killed state
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_ROUTE, targetRoute)
            // ✅ Thêm timestamp để mỗi notification có intent khác nhau
            putExtra("timestamp", System.currentTimeMillis())
        }

        // ✅ Sử dụng requestCode duy nhất cho mỗi notification để tránh bị ghi đè
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("NotificationHelper", "Show notification channel=$channelId title=$title route=$targetRoute")

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.mentorme)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // ✅ Auto dismiss notification when tapped
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    fun channelFor(type: NotificationType): String {
        return when (type) {
            NotificationType.MESSAGE -> CHANNEL_MESSAGES
            NotificationType.BOOKING_CONFIRMED,
            NotificationType.BOOKING_REMINDER,
            NotificationType.BOOKING_CANCELLED,
            NotificationType.BOOKING_PENDING,
            NotificationType.BOOKING_DECLINED,
            NotificationType.BOOKING_FAILED,
            NotificationType.PAYMENT_SUCCESS,
            NotificationType.PAYMENT_FAILED -> CHANNEL_BOOKING
            NotificationType.TOPUP_SUCCESS,
            NotificationType.TOPUP_REJECTED,
            NotificationType.SYSTEM -> CHANNEL_GENERAL
        }
    }

    fun hasPostPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
}
