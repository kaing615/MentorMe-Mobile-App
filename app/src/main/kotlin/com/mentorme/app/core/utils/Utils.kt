package com.mentorme.app.core.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateTimeUtils {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }

    fun formatTime(date: Date): String {
        return timeFormatter.format(date)
    }

    fun formatDateTime(date: Date): String {
        return dateTimeFormatter.format(date)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            dateFormatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentDateTime(): String {
        return dateTimeFormatter.format(Date())
    }

    /**
     * Format ISO datetime string to Vietnamese readable format
     * Examples:
     * - "Hôm nay 14:30"
     * - "Ngày mai 09:00"
     * - "31/12/2025 15:45"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatIsoToReadable(isoDateTime: String?): String {
        if (isoDateTime.isNullOrBlank()) return ""

        return try {
            // Parse ISO datetime (supports formats like "2025-12-31T14:30:00Z" or "2025-12-31T14:30:00")
            val zonedDateTime = try {
                ZonedDateTime.parse(isoDateTime)
            } catch (e: Exception) {
                // Try without zone
                val localDateTime = LocalDateTime.parse(
                    isoDateTime.replace("Z", "").replace(" ", "T"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                localDateTime.atZone(ZoneId.systemDefault())
            }

            val sessionDate = zonedDateTime.toLocalDate()
            val sessionTime = zonedDateTime.toLocalTime()
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            val timeStr = sessionTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            when (sessionDate) {
                today -> "Hôm nay $timeStr"
                tomorrow -> "Ngày mai $timeStr"
                else -> {
                    val dateStr = sessionDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    "$dateStr $timeStr"
                }
            }
        } catch (e: Exception) {
            // Fallback: just return the original string
            isoDateTime
        }
    }
    
    /**
     * Format last seen time to Vietnamese readable format
     * Examples:
     * - "Vừa xong" (just now)
     * - "5 phút trước"
     * - "2 giờ trước"
     * - "Hôm qua"
     * - "31/12/2025"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLastSeen(lastSeenIso: String?): String {
        if (lastSeenIso.isNullOrBlank()) return "Không hoạt động"
        
        return try {
            val lastSeenTime = try {
                ZonedDateTime.parse(lastSeenIso)
            } catch (e: Exception) {
                val localDateTime = LocalDateTime.parse(
                    lastSeenIso.replace("Z", "").replace(" ", "T"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                localDateTime.atZone(ZoneId.systemDefault())
            }
            
            val now = ZonedDateTime.now()
            val diffMinutes = java.time.Duration.between(lastSeenTime, now).toMinutes()
            val diffHours = diffMinutes / 60
            val diffDays = diffHours / 24
            
            when {
                diffMinutes < 1 -> "Vừa xong"
                diffMinutes < 60 -> "$diffMinutes phút trước"
                diffHours < 24 -> "$diffHours giờ trước"
                diffDays == 1L -> "Hôm qua"
                diffDays < 7 -> "$diffDays ngày trước"
                else -> lastSeenTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        } catch (e: Exception) {
            "Không hoạt động"
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^[+]?[0-9]{10,15}$"))
    }
}
