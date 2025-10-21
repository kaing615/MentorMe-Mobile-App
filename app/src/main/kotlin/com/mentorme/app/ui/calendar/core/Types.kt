package com.mentorme.app.ui.calendar.core

// Tabs
enum class MentorTab { Availability, Bookings, Sessions }

// Slot lịch trống
data class AvailabilitySlot(
    val id: String,
    val date: String,         // YYYY-MM-DD
    val startTime: String,    // HH:mm
    val endTime: String,      // HH:mm
    val duration: Int,        // minutes
    val description: String?,
    val isActive: Boolean,
    val sessionType: String,  // "video" | "in-person"
    val isBooked: Boolean
)
