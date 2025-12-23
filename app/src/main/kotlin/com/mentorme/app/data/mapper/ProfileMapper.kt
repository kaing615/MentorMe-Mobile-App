package com.mentorme.app.data.mapper

import com.mentorme.app.data.dto.profile.MePayload
import com.mentorme.app.ui. profile.UserProfile
import com.mentorme.app.ui.profile.UserRole
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import java.util.Date
import android.util.Log
import androidx.core.text.HtmlCompat

private val UTC_TZ:  TimeZone = TimeZone. getTimeZone("UTC")
private val ISO_MS = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = UTC_TZ }
private val ISO_NO_MS = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = UTC_TZ }

private fun parseIsoDateOrNow(src: String?): Date {
    if (src.isNullOrBlank()) return Date()
    runCatching { return Date. from(Instant.parse(src)) }
    return runCatching { ISO_MS.parse(src) }
        .getOrElse { runCatching { ISO_NO_MS.parse(src) }.getOrNull() }
        ?: Date()
}

private val HTML_TAG_REGEX = Regex("</?[a-zA-Z][^>]*>")
private val HTML_ENTITY_REGEX = Regex("&[a-zA-Z]{2,8};")

private fun cleanHtmlText(value: String?): String? {
    if (value.isNullOrBlank()) return null
    val trimmed = value.trim()
    val looksLikeHtml = HTML_TAG_REGEX.containsMatchIn(trimmed) || HTML_ENTITY_REGEX.containsMatchIn(trimmed)
    val text = if (looksLikeHtml) {
        HtmlCompat.fromHtml(trimmed, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    } else {
        trimmed
    }
    val cleaned = text.replace('\u00A0', ' ').trim()
    return cleaned.ifBlank { null }
}

fun MePayload.toUi(): Pair<UserProfile, UserRole> {
    val role = when (user?.role?. lowercase(Locale.ROOT)) {
        "mentor" -> UserRole. MENTOR
        else -> UserRole.MENTEE
    }

    val p = profile
    val u = user

    Log.d("ProfileMapper", "Mapping MePayload to UI")
    Log.d("ProfileMapper", "user. name: ${u?.name}")
    Log.d("ProfileMapper", "user.userName: ${u?.userName}")
    Log.d("ProfileMapper", "profile.fullName: ${p?.fullName}")
    Log.d("ProfileMapper", "profile.phone: ${p?.phone}")
    Log.d("ProfileMapper", "profile.location: ${p?.location}")
    Log.d("ProfileMapper", "profile.bio: ${p?.bio}")
    Log.d("ProfileMapper", "profile.avatarUrl: ${p?.avatarUrl}")

    val ui = UserProfile(
        id = p?.id ?: u?. id ?: "",
        fullName = when {
            !p?. fullName.isNullOrBlank() -> p.fullName!!
            !u?.name.isNullOrBlank() -> u.name!!
            ! u?.userName.isNullOrBlank() -> u.userName!!
            else -> "Người dùng"
        },
        email = u?.email.orEmpty(),
        phone = p?.phone,
        location = p?.location,
        bio = cleanHtmlText(p?.bio?.takeIf { it.isNotBlank() } ?: p?.description),
        avatar = p?.avatarUrl,
        joinDate = parseIsoDateOrNow(u?.createdAt),
        totalSessions = 0,
        totalSpent = 0L,
        interests = p?.skills ?: emptyList(),
        preferredLanguages = p?.languages ?: emptyList()
    )
    return ui to role
}
