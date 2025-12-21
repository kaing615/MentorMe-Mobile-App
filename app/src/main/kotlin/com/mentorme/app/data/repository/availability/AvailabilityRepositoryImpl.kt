package com.mentorme.app.data.repository.availability

import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityRepositoryImpl @Inject constructor(
    private val api: MentorMeApi
) : AvailabilityRepository {

    override suspend fun getMentorAvailability(mentorId: String): AppResult<List<com.mentorme.app.data.dto.AvailabilitySlot>> {
        return try {
            // Build default window:  today 00:00Z ..  +30d 23:59:59Z
            val zone = java.time.ZoneId.systemDefault()
            val fromIsoUtc = java.time.LocalDate.now().atStartOfDay(zone).toInstant().toString()
            val toIsoUtc = java.time.LocalDate.now().plusDays(30).atTime(23, 59, 59).atZone(zone).toInstant().toString()

            Logx.d("AvailabilityRepo") { "getMentorAvailability mentorId=$mentorId from=$fromIsoUtc to=$toIsoUtc includeClosed=true" }
            val idOk = mentorId.length >= 16 && mentorId.matches(Regex("^[A-Za-z0-9_-]+$"))
            if (!idOk) x.d("AvailabilityRepo") { "WARN suspicious mentorId:  '$mentorId' (len=${mentorId.length})" }

            val res = api.getPublicAvailabilityCalendar(mentorId, fromIsoUtc, toIsoUtc, includeClosed = true)
            if (!res.isSuccessful) {
                return AppResult.failure("HTTP ${res.code()} ${res.message()}")
            }
            val items = res.body()?.data?.items.orEmpty()

            // Minimal mapping CalendarItemDto -> legacy AvailabilitySlot (date, HH:mm, HH:mm, isAvailable)
            val HH_MM = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val mapped = items.mapNotNull { it ->
                val s = it.start ?: return@mapNotNull null
                val e = it.end ?: return@mapNotNull null
                val sIns = runCatching { java.time.Instant.parse(s) }.getOrNull() ?: return@mapNotNull null
                val eIns = runCatching { java.time.Instant.parse(e) }.getOrNull() ?: return@mapNotNull null
                val sLoc = sIns.atZone(zone)
                val eLoc = eIns.atZone(zone)
                com.mentorme.app.data.dto.AvailabilitySlot(
                    id = it.id,
                    date = sLoc.toLocalDate().toString(),
                    startTime = sLoc.format(HH_MM),
                    endTime = eLoc.format(HH_MM),
                    isAvailable = (it.status?.lowercase() == "open")
                )
            }
            AppResult.success(mapped)
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}
