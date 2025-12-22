package com.mentorme.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mentorme.app.R
import com.mentorme.app.data.model.NotificationType
import kotlin.random.Random

object NotificationHelper {
    const val CHANNEL_GENERAL = "mentorme_general"
    const val CHANNEL_BOOKING = "mentorme_booking"
    const val CHANNEL_MESSAGES = "mentorme_messages"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_GENERAL,
                "MentorMe Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "System updates and announcements"
            },
            NotificationChannel(
                CHANNEL_BOOKING,
                "Booking Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Booking confirmations, reminders, and cancellations"
            },
            NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Chat messages from mentors"
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        type: NotificationType = NotificationType.SYSTEM
    ) {
        if (!hasPostPermission(context)) return
        ensureChannels(context)
        val channelId = channelFor(type)
        Log.d("NotificationHelper", "Show notification channel=$channelId title=$title")
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.mentorme)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }

    fun channelFor(type: NotificationType): String {
        return when (type) {
            NotificationType.MESSAGE -> CHANNEL_MESSAGES
            NotificationType.BOOKING_CONFIRMED,
            NotificationType.BOOKING_REMINDER,
            NotificationType.BOOKING_CANCELLED -> CHANNEL_BOOKING
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
