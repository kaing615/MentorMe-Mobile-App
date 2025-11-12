package com.mentorme.app.core.time

import java.time.*
import java.time.format.DateTimeFormatter

// ISO format with trailing 'Z' for UTC instants (second precision)
private val ISO_Z: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

/**
 * Convert a local date and local time in the given timezone to UTC ISO-8601 string with trailing Z.
 */
fun localToUtcIsoZ(localDate: LocalDate, startLocalTime: LocalTime, tz: ZoneId): String {
    val zdt = ZonedDateTime.of(localDate, startLocalTime, tz).withZoneSameInstant(ZoneOffset.UTC)
    return ISO_Z.format(zdt)
}

/**
 * Add minutes to a UTC ISO-Z string and return a UTC ISO-Z string.
 */
fun plusMinutesIsoZ(utcIsoZ: String, minutes: Long): String {
    val inst = Instant.parse(utcIsoZ)
    return ISO_Z.format(inst.plusSeconds(minutes * 60).atOffset(ZoneOffset.UTC))
}
