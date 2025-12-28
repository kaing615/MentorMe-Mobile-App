package com.mentorme.app.ui.calendar.core

/** "HH:mm" -> total minutes */
fun hhmmToMinutes(hhmm: String): Int =
    hhmm.split(":").let { it[0].toInt() * 60 + it[1].toInt() }

/** Minutes difference end - start (wrap overnight). */
fun durationMinutes(start: String, end: String): Int {
    val diff = hhmmToMinutes(end) - hhmmToMinutes(start)
    return if (diff < 0) diff + 24 * 60 else diff
}
