package com.mentorme.app.core.realtime

import android.util.Log
import com.google.gson.Gson
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.network.NetworkConstants
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.mapper.NotificationSocketPayload
import com.mentorme.app.data.mapper.toNotificationItem
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
    private val dataStoreManager: DataStoreManager
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
        val payload = parsePayload(raw) ?: return
        val item = payload.toNotificationItem() ?: return

        NotificationStore.add(item)
        RealtimeEventBus.emit(RealtimeEvent.NotificationReceived(item, payload))

        val bookingId = extractBookingId(payload.data)
        if (!bookingId.isNullOrBlank()) {
            RealtimeEventBus.emit(RealtimeEvent.BookingChanged(bookingId))
        }
    }

    private fun parsePayload(raw: Any): NotificationSocketPayload? {
        return try {
            when (raw) {
                is JSONObject -> gson.fromJson(raw.toString(), NotificationSocketPayload::class.java)
                is Map<*, *> -> gson.fromJson(gson.toJson(raw), NotificationSocketPayload::class.java)
                is String -> gson.fromJson(raw, NotificationSocketPayload::class.java)
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

    companion object {
        private const val TAG = "SocketManager"
        private const val EVENT_NOTIFICATION = "notifications:new"
    }
}
