package com.mentorme.app.core.realtime

import com.mentorme.app.data.mapper.ChatSocketPayload
import com.mentorme.app.data.mapper.NotificationSocketPayload
import com.mentorme.app.data.mapper.SessionAdmittedPayload
import com.mentorme.app.data.mapper.SessionEndedPayload
import com.mentorme.app.data.mapper.SessionJoinedPayload
import com.mentorme.app.data.mapper.SessionMediaStatePayload
import com.mentorme.app.data.mapper.SessionParticipantPayload
import com.mentorme.app.data.mapper.SessionReadyPayload
import com.mentorme.app.data.mapper.SessionSignalPayload
import com.mentorme.app.data.mapper.SessionStatusPayload
import com.mentorme.app.data.mapper.SessionWaitingPayload
import com.mentorme.app.data.model.NotificationItem

sealed class RealtimeEvent {
    data class NotificationReceived(
        val notification: NotificationItem,
        val payload: NotificationSocketPayload? = null
    ) : RealtimeEvent()

    data class BookingChanged(val bookingId: String) : RealtimeEvent()

    data class ChatMessageReceived(
        val payload: ChatSocketPayload
    ) : RealtimeEvent()

    data class SessionJoined(
        val payload: SessionJoinedPayload
    ) : RealtimeEvent()

    data class SessionWaiting(
        val payload: SessionWaitingPayload
    ) : RealtimeEvent()

    data class SessionAdmitted(
        val payload: SessionAdmittedPayload
    ) : RealtimeEvent()

    data class SessionReady(
        val payload: SessionReadyPayload
    ) : RealtimeEvent()

    data class SessionParticipantJoined(
        val payload: SessionParticipantPayload
    ) : RealtimeEvent()

    data class SessionParticipantLeft(
        val payload: SessionParticipantPayload
    ) : RealtimeEvent()

    data class SessionEnded(
        val payload: SessionEndedPayload
    ) : RealtimeEvent()

    data class SignalOfferReceived(
        val payload: SessionSignalPayload
    ) : RealtimeEvent()

    data class SignalAnswerReceived(
        val payload: SessionSignalPayload
    ) : RealtimeEvent()

    data class SignalIceReceived(
        val payload: SessionSignalPayload
    ) : RealtimeEvent()
    
    data class SessionStatusChanged(
        val payload: SessionStatusPayload
    ) : RealtimeEvent()
    
    data class SessionMediaStateChanged(
        val payload: SessionMediaStatePayload
    ) : RealtimeEvent()
    
    data class UserOnlineStatusChanged(
        val userId: String,
        val isOnline: Boolean
    ) : RealtimeEvent()
}
