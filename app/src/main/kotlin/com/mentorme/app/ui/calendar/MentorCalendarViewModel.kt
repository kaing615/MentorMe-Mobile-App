package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.domain.usecase.availability.CreateAvailabilitySlotUseCase
import com.mentorme.app.domain.usecase.availability.GetPublicCalendarUseCase
import com.mentorme.app.domain.usecase.availability.PublishSlotUseCase
import com.mentorme.app.ui.calendar.core.AvailabilitySlot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltViewModel
class MentorCalendarViewModel @Inject constructor(
    private val getPublicCalendar: GetPublicCalendarUseCase,
    private val createAvailabilitySlot: CreateAvailabilitySlotUseCase,
    private val publishSlot: PublishSlotUseCase
) : ViewModel() {

    private val _slots = MutableStateFlow<List<AvailabilitySlot>>(emptyList())
    val slots: StateFlow<List<AvailabilitySlot>> = _slots.asStateFlow()

    // Cache the last loaded window to refresh after publish
    private data class Window(val mentorId: String, val from: String, val to: String)
    private var lastWindow: Window? = null

    // Enrich occurrences immediately after creation when calendar API lacks meta
    private val recentSlots: java.util.LinkedHashMap<String, Pair<String, String?>> = object : java.util.LinkedHashMap<String, Pair<String, String?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<String, String?>>?): Boolean = size > 20
    }

    fun loadWindow(mentorId: String, fromIsoUtc: String, toIsoUtc: String) {
        // set cache first
        lastWindow = Window(mentorId, fromIsoUtc, toIsoUtc)
        viewModelScope.launch {
            Logx.d(TAG) { "loadWindow start mentorId=$mentorId from=$fromIsoUtc to=$toIsoUtc" }
            when (val res = getPublicCalendar(mentorId, fromIsoUtc, toIsoUtc)) {
                is AppResult.Success -> {
                    val items = res.data
                    val ui = items.mapNotNull { item ->
                        val s = item.start ?: return@mapNotNull null
                        val e = item.end ?: return@mapNotNull null
                        if (s.isBlank() || e.isBlank()) return@mapNotNull null
                        val startInstant = runCatching { Instant.parse(s) }.getOrNull() ?: return@mapNotNull null
                        val endInstant = runCatching { Instant.parse(e) }.getOrNull() ?: return@mapNotNull null
                        val startLocal = startInstant.atZone(ZoneId.systemDefault())
                        val endLocal = endInstant.atZone(ZoneId.systemDefault())

                        // Prefer root title/desc; many backends don't include them at all
                        val rawTitle = (item.title ?: "").trim()
                        val rawDesc = (item.description ?: "").trim()

                        // Loosened marker: case-insensitive + allow spaces
                        val marker = Regex("""(?i)^\[type\s*=\s*(video|in-person)]\s*""")
                        val typeFromMarker = marker.find(rawTitle)?.groupValues?.getOrNull(1)?.lowercase()
                        val cleanedTitle = rawTitle.replace(marker, "")

                        // Enrich from recent cache if marker/desc missing
                        val meta = item.slot?.let { recentSlots[it] }

                        val sessionType = when {
                            typeFromMarker == "in-person" -> "in-person"
                            typeFromMarker == "video" -> "video"
                            meta?.first == "in-person" -> "in-person"
                            meta?.first == "video" -> "video"
                            else -> "video"
                        }

                        val uiDesc: String? = when {
                            cleanedTitle.isNotBlank() -> cleanedTitle
                            rawDesc.isNotBlank() -> rawDesc
                            meta?.second?.isNotBlank() == true -> meta.second
                            else -> null
                        }

                        val startHHmm = startLocal.format(HH_MM)
                        val endHHmm = endLocal.format(HH_MM)
                        val durationMin = Duration.between(startLocal, endLocal).toMinutes().toInt()

                        AvailabilitySlot(
                            id = item.id ?: "",
                            date = startLocal.toLocalDate().toString(),
                            startTime = startHHmm,
                            endTime = endHHmm,
                            duration = durationMin,
                            description = uiDesc,
                            isActive = item.status != "reserved",
                            sessionType = sessionType,
                            isBooked = item.status == "booked"
                        )
                    }
                    _slots.update { ui }
                    Logx.d(TAG) { "loadWindow success items=${ui.size}" }
                }
                is AppResult.Error -> {
                    Logx.e(tag = TAG, message = { "loadWindow error: ${res.throwable}" })
                    // Keep previous state on error
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun addSlot(
        mentorId: String,
        dateIso: String,   // yyyy-MM-dd
        startHHmm: String, // HH:mm
        endHHmm: String,   // HH:mm
        sessionType: String?,
        description: String?
    ): AppResult<*> {
        return try {
            val tz = ZoneId.systemDefault()
            val startUtc = toIsoUtc(dateIso, startHHmm, tz)
            val endUtc = toIsoUtc(dateIso, endHHmm, tz)

            // Normalize type and compose title safely
            val normalizedType = if ((sessionType ?: "").equals("in-person", true)) "in-person" else "video"
            val desc = (description ?: "").trim()
            val composedTitle = buildString {
                append("[type=$normalizedType] ")
                if (desc.isNotBlank()) append(desc) else append(if (normalizedType == "in-person") "Phiên Trực tiếp" else "Phiên Video Call")
            }

            val body = com.mentorme.app.data.dto.availability.CreateSlotRequest(
                timezone = tz.id,
                title = composedTitle,
                description = desc,
                start = startUtc,
                end = endUtc,
                rrule = null,
                exdates = emptyList(),
                bufferBeforeMin = 0,
                bufferAfterMin = 0,
                visibility = "public",
                publishHorizonDays = 90
            )

            Logx.d(TAG) { "addSlot: creating slot $startUtc - $endUtc" }
            val createRes = createAvailabilitySlot(body)
            if (createRes is AppResult.Error) {
                Logx.e(tag = TAG, message = { "addSlot: create failed: ${createRes.throwable}" })
                return createRes
            }

            val slotId = (createRes as AppResult.Success<String>).data
            // Seed recent cache so UI can render type/desc even if calendar API lacks meta
            val effectiveDesc = if (desc.isNotBlank()) desc else if (normalizedType == "in-person") "Phiên Trực tiếp" else "Phiên Video Call"
            recentSlots[slotId] = normalizedType to effectiveDesc

            Logx.d(TAG) { "addSlot: created slotId=$slotId, publishing..." }
            val publishRes = publishSlot(slotId)
            if (publishRes is AppResult.Error) {
                Logx.e(tag = TAG, message = { "addSlot: publish failed: ${publishRes.throwable}" })
                return publishRes
            }

            Logx.d(TAG) { "addSlot: published; reloading window" }
            lastWindow?.let { w ->
                val zone = ZoneId.systemDefault()
                val createdStartLocal = LocalDate.parse(dateIso).atStartOfDay(zone).toInstant()
                val winFrom = runCatching { Instant.parse(w.from) }.getOrNull()
                val winTo = runCatching { Instant.parse(w.to) }.getOrNull()

                if (winFrom == null || winTo == null) {
                    // cache hỏng: nạp lại theo ngày vừa tạo (±30d)
                    val newFrom = createdStartLocal.toString()
                    val newTo = createdStartLocal.atZone(zone)
                        .plusDays(30).withHour(23).withMinute(59).withSecond(59)
                        .toInstant().toString()
                    lastWindow = Window(w.mentorId, newFrom, newTo)
                    loadWindow(w.mentorId, newFrom, newTo)
                } else {
                    // nếu slot ngoài cửa sổ hiện tại → nhảy cửa sổ để bao trọn slot
                    if (createdStartLocal.isBefore(winFrom) || createdStartLocal.isAfter(winTo)) {
                        val newFrom = createdStartLocal.toString()
                        val newTo = createdStartLocal.atZone(zone)
                            .plusDays(30).withHour(23).withMinute(59).withSecond(59)
                            .toInstant().toString()
                        lastWindow = Window(w.mentorId, newFrom, newTo)
                        loadWindow(w.mentorId, newFrom, newTo)
                    } else {
                        // vẫn trong cửa sổ cũ
                        loadWindow(w.mentorId, w.from, w.to)
                    }
                }
            }
            AppResult.success(Unit)
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }

    private fun toIsoUtc(dateIso: String, hhmm: String, zoneId: ZoneId): String {
        val localDate = LocalDate.parse(dateIso)
        val localTime = LocalTime.parse(hhmm)
        val zdt = localDate.atTime(localTime).atZone(zoneId)
        return zdt.toInstant().toString() // ISO-8601 UTC with Z
    }

    companion object {
        private const val TAG = "MentorCalendarVM"
        private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
