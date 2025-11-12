package com.mentorme.app.data.dto.availability

/**
 * Result of publishing a slot into concrete occurrences within a horizon.
 */
data class PublishSlotResponse(
    val published: Boolean,
    val occurrencesCreated: Int,
    val skippedConflict: Int,
    /** Effective RRULE used when publishing (if any). */
    val rrule: String? = null,
    /** Effective publish horizon in days. */
    val horizonDays: Int,
    /** Optional server message or diagnostic. */
    val message: String? = null
)

