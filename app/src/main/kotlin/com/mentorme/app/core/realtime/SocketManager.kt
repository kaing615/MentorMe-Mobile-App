package com.mentorme.app.core.realtime

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mentorme.app.core.appstate.AppForegroundTracker
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.network.NetworkConstants
import com.mentorme.app.core.notifications.NotificationCache
import com.mentorme.app.core.notifications.NotificationDeduper
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.core.notifications.NotificationDeepLink
import com.mentorme.app.core.notifications.NotificationPreferencesStore
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.mapper.ChatSocketPayload
import com.mentorme.app.data.mapper.NotificationSocketPayload
import com.mentorme.app.data.mapper.SessionAdmittedPayload
import com.mentorme.app.data.mapper.UserOnlinePayload
import com.mentorme.app.data.mapper.UserOfflinePayload
import com.mentorme.app.data.mapper.SessionEndedPayload
import com.mentorme.app.data.mapper.SessionJoinedPayload
import com.mentorme.app.data.mapper.SessionParticipantPayload
import com.mentorme.app.data.mapper.SessionReadyPayload
import com.mentorme.app.data.mapper.SessionSignalPayload
import com.mentorme.app.data.mapper.SessionWaitingPayload
import com.mentorme.app.data.mapper.toNotificationItem
import com.mentorme.app.data.model.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val notificationCache: NotificationCache,
    @ApplicationContext private val appContext: Context
) {
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tokenJob: Job? = null
    private var socket: Socket? = null
    private var currentToken: String? = null

    fun start() {
        if (tokenJob != null) return
        tokenJob = scope.launch {
            dataStoreManager.getToken()
                .distinctUntilChanged()
                .collect { token ->
                    if (token.isNullOrBlank()) {
                        disconnect()
                    } else {
                        connect(token)
                    }
                }
        }
    }

    fun stop() {
        tokenJob?.cancel()
        tokenJob = null
        disconnect()
    }

    fun emit(event: String, payload: Any) {
        val connected = socket?.connected() == true
        Log.d(TAG, "emit() - event: $event, connected: $connected, payload: $payload")
        if (!connected) {
            Log.w(TAG, "Socket not connected! Cannot emit event: $event")
        }
        
        // Convert payload to JSONObject for proper serialization
        val jsonPayload = when (payload) {
            is Map<*, *> -> JSONObject(payload as Map<String, Any>)
            is JSONObject -> payload
            else -> {
                try {
                    JSONObject(gson.toJson(payload))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to convert payload to JSON: ${e.message}")
                    payload
                }
            }
        }
        
        Log.d(TAG, "Emitting with JSONObject: $jsonPayload")
        socket?.emit(event, jsonPayload)
    }

    fun isConnected(): Boolean = socket?.connected() == true

    private fun connect(token: String) {
        if (token == currentToken && socket?.connected() == true) return

        disconnect()
        currentToken = token

        val options = IO.Options().apply {
            query = "token=$token"
            transports = arrayOf("websocket")
            reconnection = true
            forceNew = true
        }

        val newSocket = IO.socket(NetworkConstants.SOCKET_URL, options)
        newSocket.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Socket connected")
        }
        newSocket.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG, "Socket disconnected")
        }
        newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.w(TAG, "Socket connect error: ${args.firstOrNull()}")
        }
        newSocket.on(EVENT_NOTIFICATION) { args ->
            handleNotificationEvent(args)
        }
        newSocket.on(EVENT_CHAT_MESSAGE) { args ->
            handleChatMessageEvent(args)
        }
        newSocket.on(EVENT_CHAT_TYPING) { args ->
            handleChatTypingEvent(args)
        }
        newSocket.on(EVENT_SESSION_JOINED) { args ->
            handleSessionJoinedEvent(args)
        }
        newSocket.on(EVENT_SESSION_WAITING) { args ->
            handleSessionWaitingEvent(args)
        }
        newSocket.on(EVENT_SESSION_ADMITTED) { args ->
            handleSessionAdmittedEvent(args)
        }
        newSocket.on(EVENT_SESSION_READY) { args ->
            handleSessionReadyEvent(args)
        }
        newSocket.on(EVENT_SESSION_PARTICIPANT_JOINED) { args ->
            handleSessionParticipantJoinedEvent(args)
        }
        newSocket.on(EVENT_SESSION_PARTICIPANT_LEFT) { args ->
            handleSessionParticipantLeftEvent(args)
        }
        newSocket.on(EVENT_SESSION_ENDED) { args ->
            handleSessionEndedEvent(args)
        }
        newSocket.on(EVENT_SIGNAL_OFFER) { args ->
            handleSignalOfferEvent(args)
        }
        newSocket.on(EVENT_SIGNAL_ANSWER) { args ->
            handleSignalAnswerEvent(args)
        }
        newSocket.on(EVENT_SIGNAL_ICE) { args ->
            handleSignalIceEvent(args)
        }
        newSocket.on(EVENT_SESSION_STATUS) { args ->
            handleSessionStatusEvent(args)
        }
        newSocket.on(EVENT_SESSION_MEDIA_STATE) { args ->
            handleSessionMediaStateEvent(args)
        }
        newSocket.on(EVENT_SESSION_CHAT) { args ->
            handleSessionChatEvent(args)
        }
        newSocket.on(EVENT_USER_ONLINE) { args ->
            handleUserOnlineEvent(args)
        }
        newSocket.on(EVENT_USER_OFFLINE) { args ->
            handleUserOfflineEvent(args)
        }
        newSocket.on(EVENT_BOOKING_CREATED) { args ->
            handleBookingCreatedEvent(args)
        }
        newSocket.on(EVENT_BOOKING_CANCELLED) { args ->
            handleBookingCancelledEvent(args)
        }

        socket = newSocket
        newSocket.connect()
    }

    private fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
        currentToken = null
    }

    private fun handleNotificationEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, NotificationSocketPayload::class.java) ?: return
        val route = NotificationDeepLink.routeFor(payload.data)
        val item = payload.toNotificationItem()?.copy(deepLink = route) ?: return

        NotificationStore.add(item)
        scope.launch {
            notificationCache.save(NotificationStore.notifications.value)
        }
        RealtimeEventBus.emit(RealtimeEvent.NotificationReceived(item, payload))
        val pushEnabled = NotificationPreferencesStore.prefs.value.isPushEnabled(item.type)
        
        // For payment notifications, show popup even in foreground
        val isPaymentNotification = item.type == NotificationType.PAYMENT_SUCCESS || 
                                     item.type == NotificationType.PAYMENT_FAILED
        val shouldShowInForeground = isPaymentNotification
        
        if ((shouldShowInForeground || !AppForegroundTracker.isForeground.value) && pushEnabled &&
            NotificationDeduper.shouldNotify(item.id, item.title, item.body, item.type)
        ) {
            NotificationHelper.showNotification(appContext, item.title, item.body, item.type, route)
        }

        val bookingId = extractBookingId(payload.data)
        if (!bookingId.isNullOrBlank()) {
            RealtimeEventBus.emit(RealtimeEvent.BookingChanged(bookingId))
        }
    }

    private fun handleChatMessageEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, ChatSocketPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.ChatMessageReceived(payload))
    }

    private fun handleChatTypingEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        Log.d(TAG, "Received chat:typing event: $raw")
        try {
            val json = when (raw) {
                is JSONObject -> raw
                is Map<*, *> -> JSONObject(raw as Map<*, *>)
                is String -> JSONObject(raw)
                else -> {
                    Log.w(TAG, "Unknown typing event type: ${raw::class.java}")
                    return
                }
            }
            val userId = json.optString("userId", null) ?: return
            val isTyping = json.optBoolean("isTyping", false)
            Log.d(TAG, "Parsed typing event: userId=$userId, isTyping=$isTyping")
            RealtimeEventBus.emit(RealtimeEvent.ChatTypingIndicator(userId, isTyping))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse chat typing event", e)
        }
    }

    private fun handleSessionJoinedEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionJoinedPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SessionJoined(payload))
    }

    private fun handleSessionWaitingEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionWaitingPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SessionWaiting(payload))
    }

    private fun handleSessionAdmittedEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionAdmittedPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SessionAdmitted(payload))
    }

    private fun handleSessionReadyEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionReadyPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SessionReady(payload))
    }

    private fun handleSessionParticipantJoinedEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        Log.d(TAG, "Received session:participant-joined event: $raw")
        val payload = parsePayload(raw, SessionParticipantPayload::class.java) ?: run {
            Log.e(TAG, "Failed to parse SessionParticipantPayload")
            return
        }
        Log.d(TAG, "SessionParticipantJoined - bookingId: ${payload.bookingId}, role: ${payload.role}, userId: ${payload.userId}")
        RealtimeEventBus.emit(RealtimeEvent.SessionParticipantJoined(payload))
    }

    private fun handleSessionParticipantLeftEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionParticipantPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SessionParticipantLeft(payload))
    }

    private fun handleSessionEndedEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionEndedPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SessionEnded(payload))
    }

    private fun handleSignalOfferEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionSignalPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SignalOfferReceived(payload))
    }

    private fun handleSignalAnswerEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionSignalPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SignalAnswerReceived(payload))
    }

    private fun handleSignalIceEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, SessionSignalPayload::class.java) ?: return
        RealtimeEventBus.emit(RealtimeEvent.SignalIceReceived(payload))
    }
    
    private fun handleUserOnlineEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, com.mentorme.app.data.mapper.UserOnlinePayload::class.java) ?: return
        payload.userId?.let { userId ->
            RealtimeEventBus.emit(RealtimeEvent.UserOnlineStatusChanged(userId, true))
        }
    }
    
    private fun handleUserOfflineEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, com.mentorme.app.data.mapper.UserOfflinePayload::class.java) ?: return
        payload.userId?.let { userId ->
            RealtimeEventBus.emit(RealtimeEvent.UserOnlineStatusChanged(userId, false))
        }
    }
    
    private fun handleSessionStatusEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, com.mentorme.app.data.mapper.SessionStatusPayload::class.java) ?: return
        Log.d(TAG, "handleSessionStatusEvent - bookingId: ${payload.bookingId}, userId: ${payload.userId}, status: ${payload.status}")
        RealtimeEventBus.emit(RealtimeEvent.SessionStatusChanged(payload))
    }
    
    private fun handleSessionMediaStateEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        val payload = parsePayload(raw, com.mentorme.app.data.mapper.SessionMediaStatePayload::class.java) ?: return
        Log.d(TAG, "handleSessionMediaStateEvent - bookingId: ${payload.bookingId}, audioEnabled: ${payload.audioEnabled}, videoEnabled: ${payload.videoEnabled}")
        RealtimeEventBus.emit(RealtimeEvent.SessionMediaStateChanged(payload))
    }
    
    private fun handleSessionChatEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        Log.d(TAG, "handleSessionChatEvent - raw type: ${raw::class.java.simpleName}, raw: $raw")
        
        try {
            var bookingId = ""
            var senderId = ""
            var senderName = ""
            var message = ""
            var timestamp = System.currentTimeMillis()
            
            when (raw) {
                is JSONObject -> {
                    bookingId = raw.optString("bookingId", "")
                    senderId = raw.optString("senderId", "")
                    senderName = raw.optString("senderName", "")
                    message = raw.optString("message", "")
                    timestamp = raw.optLong("timestamp", System.currentTimeMillis())
                }
                is Map<*, *> -> {
                    bookingId = raw["bookingId"]?.toString() ?: ""
                    senderId = raw["senderId"]?.toString() ?: ""
                    senderName = raw["senderName"]?.toString() ?: ""
                    message = raw["message"]?.toString() ?: ""
                    timestamp = (raw["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                }
                is String -> {
                    val json = JSONObject(raw)
                    bookingId = json.optString("bookingId", "")
                    senderId = json.optString("senderId", "")
                    senderName = json.optString("senderName", "")
                    message = json.optString("message", "")
                    timestamp = json.optLong("timestamp", System.currentTimeMillis())
                }
                else -> {
                    Log.w(TAG, "handleSessionChatEvent - unknown raw type: ${raw::class.java}")
                    return
                }
            }
            
            Log.d(TAG, "handleSessionChatEvent - parsed: bookingId=$bookingId, senderId=$senderId, senderName=$senderName, message=$message")
            
            if (bookingId.isNotEmpty() && message.isNotEmpty()) {
                Log.d(TAG, "handleSessionChatEvent - emitting RealtimeEvent.SessionChatReceived")
                RealtimeEventBus.emit(RealtimeEvent.SessionChatReceived(
                    bookingId = bookingId,
                    senderId = senderId,
                    senderName = senderName,
                    message = message,
                    timestamp = timestamp
                ))
            } else {
                Log.w(TAG, "handleSessionChatEvent - missing required fields: bookingId=$bookingId, message=$message")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing session:chat event: ${e.message}")
        }
    }

    private fun handleBookingCreatedEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        Log.d(TAG, "handleBookingCreatedEvent - raw: $raw")

        try {
            val bookingId = when (raw) {
                is JSONObject -> raw.optString("bookingId", "")
                is Map<*, *> -> raw["bookingId"]?.toString() ?: ""
                is String -> JSONObject(raw).optString("bookingId", "")
                else -> ""
            }

            if (bookingId.isNotEmpty()) {
                Log.d(TAG, "Booking created: $bookingId - emitting BookingCreated event")
                RealtimeEventBus.emit(RealtimeEvent.BookingCreated(bookingId))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing booking:created event: ${e.message}")
        }
    }

    private fun handleBookingCancelledEvent(args: Array<Any>) {
        val raw = args.firstOrNull() ?: return
        Log.d(TAG, "handleBookingCancelledEvent - raw: $raw")

        try {
            val bookingId = when (raw) {
                is JSONObject -> raw.optString("bookingId", "")
                is Map<*, *> -> raw["bookingId"]?.toString() ?: ""
                is String -> JSONObject(raw).optString("bookingId", "")
                else -> ""
            }

            if (bookingId.isNotEmpty()) {
                Log.d(TAG, "Booking cancelled: $bookingId - emitting BookingCancelled event")
                RealtimeEventBus.emit(RealtimeEvent.BookingCancelled(bookingId))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing booking:cancelled event: ${e.message}")
        }
    }

    private fun <T> parsePayload(raw: Any, clazz: Class<T>): T? {
        return try {
            when (raw) {
                is JSONObject -> gson.fromJson(raw.toString(), clazz)
                is Map<*, *> -> gson.fromJson(gson.toJson(raw), clazz)
                is String -> gson.fromJson(raw, clazz)
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse socket payload: ${e.message}")
            null
        }
    }

    private fun extractBookingId(data: Map<String, Any?>?): String? {
        val raw = data?.get("bookingId") ?: data?.get("booking_id")
        return raw?.toString()?.trim().takeIf { !it.isNullOrBlank() }
    }
    
    /**
     * Emit typing indicator to peer
     */
    fun emitTypingIndicator(peerId: String, isTyping: Boolean) {
        val payload = org.json.JSONObject().apply {
            put("peerId", peerId)
            put("isTyping", isTyping)
        }
        Log.d(TAG, "Emitting chat:typing to peer $peerId, isTyping: $isTyping")
        socket?.emit("chat:typing", payload)
    }

    companion object {
        private const val TAG = "SocketManager"
        private const val EVENT_NOTIFICATION = "notifications:new"
        private const val EVENT_CHAT_MESSAGE = "chat:message"
        private const val EVENT_CHAT_TYPING = "chat:typing"
        private const val EVENT_SESSION_JOINED = "session:joined"
        private const val EVENT_SESSION_WAITING = "session:waiting"
        private const val EVENT_SESSION_ADMITTED = "session:admitted"
        private const val EVENT_SESSION_READY = "session:ready"
        private const val EVENT_SESSION_PARTICIPANT_JOINED = "session:participant-joined"
        private const val EVENT_SESSION_PARTICIPANT_LEFT = "session:participant-left"
        private const val EVENT_SESSION_ENDED = "session:ended"
        private const val EVENT_SIGNAL_OFFER = "signal:offer"
        private const val EVENT_SIGNAL_ANSWER = "signal:answer"
        private const val EVENT_SIGNAL_ICE = "signal:ice"
        private const val EVENT_USER_ONLINE = "user:online"
        private const val EVENT_USER_OFFLINE = "user:offline"
        private const val EVENT_SESSION_STATUS = "session:status"
        private const val EVENT_SESSION_MEDIA_STATE = "session:media-state"
        private const val EVENT_SESSION_CHAT = "session:chat"
        private const val EVENT_BOOKING_CREATED = "booking:created"
        private const val EVENT_BOOKING_CANCELLED = "booking:cancelled"
    }
}
