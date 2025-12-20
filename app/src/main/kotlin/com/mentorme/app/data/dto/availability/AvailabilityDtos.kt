package com.mentorme.app.data.dto.availability

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject

// ---- BEGIN: Envelope & Slot payload (server-aligned) ----
/**
 * Generic response envelope: { success, message, data }
 */
data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

/**
 * Payload wrapper: { data: { slot: { ... } } }
 */
data class SlotPayload(
    @SerializedName("slot") val slot: SlotDto?
)

/**
 * Slot DTO with `_id` mapped to `id`.
 */
data class SlotDto(
    @SerializedName("_id") val id: String?,
    val mentor: String?,
    val title: String?,
    val description: String?,
    val timezone: String?,
    val start: String?,
    val end: String?,
    val rrule: String?,
    val exdates: List<String>?,
    val bufferBeforeMin: Int?,
    val bufferAfterMin: Int?,
    val visibility: String?,
    val status: String?,
    val publishHorizonDays: Int?
)
// ---- END: Envelope & Slot payload (server-aligned) ----

/**
 * Envelope payload for calendar response: { data: { items: [...] } }
 */
data class CalendarPayload(
    @SerializedName("items") val items: List<CalendarItemDto>?
)

// REPLACED: CalendarItemDto to support slot as String or Object
// Backend can send either: "slot": "<id>" OR "slot": { _id/id, title, description }
data class CalendarItemDto(
    @SerializedName("_id") val id: String? = null,
    val start: String? = null,
    val end: String? = null,
    val status: String? = null,
    val title: String? = null,
    val description: String? = null,
    // Backend can send either: "slot": "<id>" OR "slot": { _id/id, title, description }
    @SerializedName("slot") val slotRaw: Any? = null
)

fun CalendarItemDto.slotIdOrEmpty(): String {
    return when (val v = slotRaw) {
        is String -> v
        is Map<*, *> -> (v["_id"] as? String) ?: (v["id"] as? String) ?: ""
        is JsonObject -> when {
            v.has("_id") -> v.get("_id").asString
            v.has("id") -> v.get("id").asString
            else -> ""
        }
        else -> ""
    }
}

/**
 * Request payload to create an availability slot.
 * All fields are optional except timezone; supports one-off or recurring definitions.
 */
data class CreateSlotRequest(
    /** IANA timezone ID, e.g., "America/Los_Angeles". */
    val timezone: String,
    /** Optional title for the slot. */
    val title: String? = null,
    /** Optional human-friendly description. */
    val description: String? = null,
    /** ISO-8601 UTC start (Z), for one-off slots. */
    val start: String? = null,
    /** ISO-8601 UTC end (Z), for one-off slots. */
    val end: String? = null,
    /** RFC5545 RRULE for recurring slots (optional). */
    val rrule: String? = null,
    /** ISO-8601 UTC datetimes excluded from recurrence expansion. */
    val exdates: List<String>? = emptyList(),
    /** Minutes of buffer before the slot; default 0. */
    val bufferBeforeMin: Int? = 0,
    /** Minutes of buffer after the slot; default 0. */
    val bufferAfterMin: Int? = 0,
    /** "public" | "private"; default "public". */
    val visibility: String? = "public",
    /** Days to expand recurrence into concrete occurrences; default 90. */
    val publishHorizonDays: Int? = 90
)

/**
 * Minimal request payload to update a slot's meta/time/visibility or to issue an action.
 */
data class UpdateSlotRequest(
    val title: String? = null,
    val description: String? = null,
    val timezone: String? = null,
    val start: String? = null,
    val end: String? = null,
    val visibility: String? = null,
    /** action: "pause" | "resume" */
    val action: String? = null
)

/**
 * Response containing the created slot record (legacy flat shape, kept for compatibility).
 */
data class CreateSlotResponse(
    val slot: SlotDto
)

/**
 * Item on the public calendar representing a concrete occurrence.
 * Status: "open" | "booked" | "reserved".
 */
data class CalendarItem(
    val id: String,
    /** ID of the parent slot. */
    val slot: String,
    /** ISO-8601 UTC start (Z). */
    val start: String,
    /** ISO-8601 UTC end (Z). */
    val end: String,
    val status: String
)

/**
 * Public calendar response containing a list of occurrences.
 */
data class CalendarResponse(
    val items: List<CalendarItem> = emptyList()
)

/**
 * Result payload returned by publishAvailabilitySlot endpoint.
 */
data class PublishResult(
    val published: Boolean = true,
    val occurrencesCreated: Int = 0,
    val skippedConflict: Int = 0,
    val rrule: String? = null,
    val horizonDays: Int = 0,
    val message: String? = null
)

// ---- Conflict models for HTTP 409 ----
/** Server error body when publishing conflicts with existing occurrences (including buffer). */
data class ConflictBody(
    val message: String? = null,
    val conflicts: List<ConflictItem> = emptyList()
)

data class ConflictItem(
    val id: String,
    val start: String,
    val end: String,
    val status: String,
    val slot: ConflictSlot? = null
)

data class ConflictSlot(
    val id: String,
    val title: String? = null,
    val bufferBeforeMin: Int? = null,
    val bufferAfterMin: Int? = null
)
