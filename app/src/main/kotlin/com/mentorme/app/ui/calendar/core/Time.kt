package com.mentorme.app.ui.calendar.core

/** "HH:mm" -> tổng phút */
fun hhmmToMinutes(hhmm: String): Int =
    hhmm.split(":").let { it[0].toInt() * 60 + it[1].toInt() }

/** chênh lệch phút end - start (không clamp âm) */
fun durationMinutes(start: String, end: String): Int =
    hhmmToMinutes(end) - hhmmToMinutes(start)
