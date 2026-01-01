package com.mentorme.app.data.mapper

data class SessionJoinedPayload(
    val bookingId: String? = null,
    val role: String? = null,
    val admitted: Boolean? = null,
    val sessionStartedAt: String? = null // ISO8601 timestamp of actual session start
)

data class SessionWaitingPayload(
    val bookingId: String? = null
)

data class SessionAdmittedPayload(
    val bookingId: String? = null,
    val admittedAt: String? = null,
    val sessionStartedAt: String? = null // ISO8601 timestamp of actual session start
)

data class SessionReadyPayload(
    val bookingId: String? = null
)

data class SessionParticipantPayload(
    val bookingId: String? = null,
    val userId: String? = null,
    val role: String? = null
)

data class SessionEndedPayload(
    val bookingId: String? = null,
    val endedBy: String? = null
)

data class SessionSignalPayload(
    val bookingId: String? = null,
    val fromUserId: String? = null,
    val fromRole: String? = null,
    val data: Map<String, Any?>? = null
)

data class SessionStatusPayload(
    val bookingId: String? = null,
    val userId: String? = null,
    val status: String? = null // "connected", "reconnecting", "disconnected"
)

data class SessionMediaStatePayload(
    val bookingId: String? = null,
    val userId: String? = null,
    val audioEnabled: Boolean? = null,
    val videoEnabled: Boolean? = null
)
