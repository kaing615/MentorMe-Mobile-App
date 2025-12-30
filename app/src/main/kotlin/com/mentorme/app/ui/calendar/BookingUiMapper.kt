package com.mentorme.app.ui.calendar

import android.util.Log
import com.mentorme.app.data.model.Booking

/**
 * UI models for displaying bookings in mentor/mentee views
 */
data class MentorUpcomingBookingUi(
    val id: String,
    val menteeName: String,
    val avatarInitial: String,
    val topic: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String
)

private const val TAG = "MentorBookings"

/**
 * Maps a Booking to MentorUpcomingBookingUi with correct mentee name display logic.
 *
 * Display Priority:
 * 1. booking.menteeFullName (flat field from backend)
 * 2. booking.mentee?.fullName (nested object if populated)
 * 3. Fallback: "Mentee ${menteeId.takeLast(6)}"
 *
 * Avatar Initial:
 * - First character of resolved name (uppercase)
 * - Fallback: "M"
 */
fun Booking.toMentorUpcomingUi(): MentorUpcomingBookingUi {
    // Resolve mentee name using priority logic
    val resolvedMenteeName = listOf(
        menteeFullName,
        mentee?.fullName,
        mentee?.user?.fullName,     // ✅ Check nested user
        mentee?.profile?.fullName   // ✅ Check nested profile
    ).firstOrNull { !it.isNullOrBlank() }?.trim()
        ?: "Mentee ${menteeId.takeLast(6)}"

    // Compute avatar initial
    val avatarInitial = if (resolvedMenteeName.startsWith("Mentee ")) {
        "M" // Fallback case
    } else {
        resolvedMenteeName.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
    }

    // Log for debugging (safe - no sensitive info)
    Log.d(TAG, buildString {
        append("Mapping booking: ")
        append("id=${id}, ")
        append("menteeId=${menteeId}, ")
        append("menteeFullName=${menteeFullName}, ")
        append("mentee.fullName=${mentee?.fullName}, ")
        append("resolvedName=$resolvedMenteeName, ")
        append("initial=$avatarInitial")
    })

    return MentorUpcomingBookingUi(
        id = id,
        menteeName = resolvedMenteeName,
        avatarInitial = avatarInitial,
        topic = topic ?: "Buổi tư vấn",
        date = date,
        startTime = startTime,
        endTime = endTime,
        status = status.name
    )
}

/**
 * Extension for mentee view - maps mentor name (reverse direction)
 */
fun Booking.resolveMentorName(): String {
    return listOf(
        mentorFullName,
        mentor?.fullName
    ).firstOrNull { !it.isNullOrBlank() }?.trim()
        ?: "Mentor ${mentorId.takeLast(6)}"
}

/**
 * Extension for mentee view - gets mentor avatar initial
 */
fun Booking.getMentorAvatarInitial(): String {
    val name = resolveMentorName()
    return if (name.startsWith("Mentor ")) {
        "M"
    } else {
        name.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
    }
}

