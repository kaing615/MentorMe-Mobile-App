package com.mentorme.app.core.time

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

// ISO format with trailing 'Z' for UTC instants (second precision)
private val ISO_Z: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
private val ISO_WITH_FRACTION: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
    .optionalEnd()
    .toFormatter()
private val LOCAL_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

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

/**
 * Convert ISO-8601 string to local time display (yyyy-MM-dd HH:mm).
 * If no zone info is present, treat the value as local time.
 */
fun formatIsoToLocalShort(iso: String?, zoneId: ZoneId = ZoneId.systemDefault()): String? {
    if (iso.isNullOrBlank()) return null
    val trimmed = iso.trim()
    val timePart = trimmed.substringAfter('T', "")
    val hasZone = timePart.contains("Z") || timePart.contains("+") || timePart.contains("-")
    return if (hasZone) {
        val instant = runCatching { Instant.parse(trimmed) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(trimmed).toInstant() }.getOrNull()
        instant?.atZone(zoneId)?.format(LOCAL_SHORT)
    } else {
        val local = runCatching { LocalDateTime.parse(trimmed, ISO_WITH_FRACTION) }.getOrNull()
        local?.format(LOCAL_SHORT)
    }
}
