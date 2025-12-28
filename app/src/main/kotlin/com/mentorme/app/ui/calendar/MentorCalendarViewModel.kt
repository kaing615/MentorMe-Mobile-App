package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.domain.usecase.availability.CreateAvailabilitySlotUseCase
import com.mentorme.app.domain.usecase.availability.DeleteAvailabilityOccurrenceUseCase
import com.mentorme.app.domain.usecase.availability.GetPublicCalendarUseCase
import com.mentorme.app.domain.usecase.availability.PublishSlotUseCase
import com.mentorme.app.domain.usecase.availability.UpdateAvailabilitySlotUseCase
import com.mentorme.app.domain.usecase.availability.DeleteAvailabilitySlotUseCase
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
import com.mentorme.app.data.dto.availability.slotIdOrEmpty
import com.mentorme.app.data.dto.availability.slotPriceVndOrNull

@HiltViewModel
class MentorCalendarViewModel @Inject constructor(
    private val getPublicCalendar: GetPublicCalendarUseCase,
    private val createAvailabilitySlot: CreateAvailabilitySlotUseCase,
    private val publishSlot: PublishSlotUseCase,
    private val updateAvailabilitySlot: UpdateAvailabilitySlotUseCase,
    private val deleteAvailabilitySlot: DeleteAvailabilitySlotUseCase,
    private val deleteAvailabilityOccurrence: DeleteAvailabilityOccurrenceUseCase
) : ViewModel() {

    private val _slots = MutableStateFlow<List<AvailabilitySlot>>(emptyList())
    val slots: StateFlow<List<AvailabilitySlot>> = _slots.asStateFlow()

    // Cache the last loaded window to refresh after publish
    private data class Window(val mentorId: String, val from: String, val to: String)
    private var lastWindow: Window? = null

    // Debounce concurrent loads
    private var loadJob: kotlinx.coroutines.Job? = null
    private var cleanupJob: kotlinx.coroutines.Job? = null

    // Enrich occurrences immediately after creation when calendar API lacks meta
    private val recentSlots: java.util.LinkedHashMap<String, Pair<String, String?>> = object : java.util.LinkedHashMap<String, Pair<String, String?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<String, String?>>?): Boolean = size > 20
    }

    fun loadWindow(mentorId: String, fromIsoUtc: String, toIsoUtc: String) {
        lastWindow = Window(mentorId, fromIsoUtc, toIsoUtc)
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val res = getPublicCalendar(mentorId, fromIsoUtc, toIsoUtc, includeClosed = true)) {
                is AppResult.Success -> {
                    val items = res.data
                    val nowSkew = Instant.now().minusSeconds(30)
                    val expiredOpenOccurrenceIds = mutableListOf<String>()
                    val ui = items.mapNotNull { item ->
                        val s = item.start ?: return@mapNotNull null
                        val e = item.end ?: return@mapNotNull null
                        val startInstant = runCatching { java.time.Instant.parse(s) }.getOrNull() ?: return@mapNotNull null
                        val endInstant = runCatching { java.time.Instant.parse(e) }.getOrNull() ?: return@mapNotNull null
                        val st = item.status?.lowercase()
                        val isOpen = st == "open"
                        if (isOpen && !endInstant.isAfter(nowSkew)) {
                            val occurrenceId = item.id?.takeIf { it.isNotBlank() }
                            if (occurrenceId != null) {
                                expiredOpenOccurrenceIds.add(occurrenceId)
                            }
                            return@mapNotNull null
                        }
                        val startLocal = startInstant.atZone(java.time.ZoneId.systemDefault())
                        val endLocal = endInstant.atZone(java.time.ZoneId.systemDefault())

                        // --- New: extract slotId and status flags ---
                        val slotId = item.slotIdOrEmpty()
                        val isBooked = st == "booked"
                        val isActive = isOpen // "closed" => paused

                        // --- New: parse marker from ROOT fields (title/description) ---
                        val rawTitle = (item.title ?: "").trim()
                        val rawDesc = (item.description ?: "").trim()
                        val marker = Regex("""^\[type=(video|in-person)]\s*""")
                        val typeFromMarker = marker.find(rawTitle)?.groupValues?.getOrNull(1)
                        val cleanedTitle = rawTitle.replace(marker, "")
                        val sessionType = when (typeFromMarker) {
                            "in-person" -> "in-person"
                            "video" -> "video"
                            else -> "video"
                        }
                        val uiDesc: String? = cleanedTitle.ifBlank { rawDesc.ifBlank { null } }

                        val startHHmm = startLocal.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        val endHHmm = endLocal.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        val durationMin = java.time.Duration.between(startLocal, endLocal)
                            .toMinutes().toInt().let { if (it < 0) it + 24 * 60 else it }
                        val priceVnd = item.slotPriceVndOrNull()?.toLong() ?: 0L

                        com.mentorme.app.ui.calendar.core.AvailabilitySlot(
                            id = item.id.orEmpty(),
                            date = startLocal.toLocalDate().toString(),
                            startTime = startHHmm,
                            endTime = endHHmm,
                            duration = durationMin,
                            priceVnd = priceVnd,
                            description = uiDesc,
                            isActive = isActive,
                            sessionType = sessionType,
                            isBooked = isBooked,
                            backendOccurrenceId = item.id.orEmpty(),
                            backendSlotId = slotId
                        )
                    }
                    _slots.update { ui }
                    Logx.d(TAG) { "loadWindow success items=${ui.size}" }
                    if (expiredOpenOccurrenceIds.isNotEmpty()) {
                        cleanupJob?.cancel()
                        cleanupJob = viewModelScope.launch {
                            val uniqueIds = expiredOpenOccurrenceIds.distinct()
                            Logx.d(TAG) { "cleanup expired open occurrences count=${uniqueIds.size}" }
                            uniqueIds.forEach { occurrenceId ->
                                when (val delRes = deleteAvailabilityOccurrence(occurrenceId)) {
                                    is AppResult.Success -> Unit
                                    is AppResult.Error -> Logx.e(TAG, { "cleanup occurrence=$occurrenceId failed: ${delRes.throwable}" })
                                    AppResult.Loading -> Unit
                                }
                            }
                        }
                    }
                }
                is AppResult.Error -> {
                    Logx.e(TAG, { "loadWindow error: ${res.throwable}" })
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
        description: String?,
        priceVnd: Long?,
        bufferBeforeMin: Int,
        bufferAfterMin: Int
    ): AppResult<com.mentorme.app.data.dto.availability.PublishResult> {
        return try {
            val tz = java.time.ZoneId.of("Asia/Ho_Chi_Minh")
            // Debug log as requested
            Logx.d(TAG) { "addSlot buffers => before=${bufferBeforeMin}, after=${bufferAfterMin}" }
            val localDate = java.time.LocalDate.parse(dateIso)
            val startLocal = java.time.LocalTime.parse(startHHmm)
            val endLocal = java.time.LocalTime.parse(endHHmm)
            val endDate = if (endLocal.isBefore(startLocal)) localDate.plusDays(1) else localDate
            val startUtc = com.mentorme.app.core.time.localToUtcIsoZ(localDate, startLocal, tz)
            val endUtc = com.mentorme.app.core.time.localToUtcIsoZ(endDate, endLocal, tz)

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
                bufferBeforeMin = bufferBeforeMin,
                bufferAfterMin = bufferAfterMin,
                priceVnd = priceVnd?.toDouble(),
                visibility = "public",
                publishHorizonDays = 90
            )

            Logx.d(TAG) { "addSlot: creating slot $startUtc - $endUtc (bufBefore=$bufferBeforeMin, bufAfter=$bufferAfterMin)" }
            val createRes = createAvailabilitySlot(body)
            if (createRes is AppResult.Error) {
                Logx.e(TAG, { "addSlot: create failed: ${createRes.throwable}" })
                return createRes
            }

            val slotId = (createRes as AppResult.Success<String>).data
            // Seed recent cache so UI can render type/desc even if calendar API lacks meta
            val effectiveDesc = if (desc.isNotBlank()) desc else if (normalizedType == "in-person") "Phiên Trực tiếp" else "Phiên Video Call"
            recentSlots[slotId] = normalizedType to effectiveDesc

            Logx.d(TAG) { "addSlot: created slotId=$slotId, publishing..." }
            val publishRes = publishSlot(slotId)
            if (publishRes is AppResult.Error) {
                Logx.e(TAG, { "addSlot: publish failed: ${publishRes.throwable}" })
                return publishRes as AppResult.Error
            }

            // Reload window after publish
            Logx.d(TAG) { "addSlot: published; reloading window" }
            lastWindow?.let { w ->
                val zone = ZoneId.systemDefault()
                val createdStartLocal = LocalDate.parse(dateIso).atStartOfDay(zone).toInstant()
                val winFrom = runCatching { Instant.parse(w.from) }.getOrNull()
                val winTo = runCatching { Instant.parse(w.to) }.getOrNull()

                if (winFrom == null || winTo == null) {
                    val newFrom = createdStartLocal.toString()
                    val newTo = createdStartLocal.atZone(zone)
                        .plusDays(30).withHour(23).withMinute(59).withSecond(59)
                        .toInstant().toString()
                    lastWindow = Window(w.mentorId, newFrom, newTo)
                    loadWindow(w.mentorId, newFrom, newTo)
                } else {
                    if (createdStartLocal.isBefore(winFrom) || createdStartLocal.isAfter(winTo)) {
                        val newFrom = createdStartLocal.toString()
                        val newTo = createdStartLocal.atZone(zone)
                            .plusDays(30).withHour(23).withMinute(59).withSecond(59)
                            .toInstant().toString()
                        lastWindow = Window(w.mentorId, newFrom, newTo)
                        loadWindow(w.mentorId, newFrom, newTo)
                    } else {
                        loadWindow(w.mentorId, w.from, w.to)
                    }
                }
            }
            publishRes as AppResult.Success<com.mentorme.app.data.dto.availability.PublishResult>
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }

    fun updateSlotMeta(
        slotId: String,
        patch: com.mentorme.app.data.dto.availability.UpdateSlotRequest,
        onDone: (AppResult<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            // --- Optimistic UI: if patch has new start/end, reflect in _slots before calling API ---
            val zone = ZoneId.systemDefault()
            val newDateTime = runCatching {
                val newDate = patch.start?.let { Instant.parse(it).atZone(zone) }
                val newEnd  = patch.end  ?.let { Instant.parse(it).atZone(zone) }
                newDate to newEnd
            }.getOrNull()

            if (newDateTime != null) {
                val (zStart, zEnd) = newDateTime
                if (zStart != null && zEnd != null) {
                    _slots.update { list ->
                        list.map {
                            if (it.backendSlotId == slotId) {
                                it.copy(
                                    date = zStart.toLocalDate().toString(),
                                    startTime = zStart.format(HH_MM),
                                    endTime   = zEnd.format(HH_MM),
                                    duration  = java.time.Duration.between(zStart, zEnd)
                                        .toMinutes().toInt().let { if (it < 0) it + 24 * 60 else it }
                                )
                            } else it
                        }
                    }
                }
            }
            if (patch.priceVnd != null) {
                val nextPrice = patch.priceVnd.toLong().coerceAtLeast(0L)
                _slots.update { list ->
                    list.map {
                        if (it.backendSlotId == slotId) it.copy(priceVnd = nextPrice) else it
                    }
                }
            }

            val res = updateAvailabilitySlot(slotId, patch)
            // Always reload the current window to reconcile with server
            lastWindow?.let { w -> loadWindow(w.mentorId, w.from, w.to) }
            onDone(res)
        }
    }

    fun pauseSlot(slotId: String, onDone: (AppResult<Unit>) -> Unit = {}) =
        updateSlotMeta(slotId, com.mentorme.app.data.dto.availability.UpdateSlotRequest(action = "pause"), onDone)

    fun resumeSlot(slotId: String, onDone: (AppResult<Unit>) -> Unit = {}) =
        updateSlotMeta(slotId, com.mentorme.app.data.dto.availability.UpdateSlotRequest(action = "resume"), onDone)

    fun deleteSlot(slotId: String, onDone: (AppResult<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = deleteAvailabilitySlot(slotId)
            if (res is AppResult.Success) lastWindow?.let { w -> loadWindow(w.mentorId, w.from, w.to) }
            onDone(res)
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
