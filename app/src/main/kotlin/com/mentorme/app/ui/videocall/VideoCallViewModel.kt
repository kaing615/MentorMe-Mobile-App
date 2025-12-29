package com.mentorme.app.ui.videocall

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.core.realtime.SocketManager
import com.mentorme.app.core.webrtc.IceServerProvider
import com.mentorme.app.core.webrtc.WebRtcClient
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.session.SessionJoinResponse
import com.mentorme.app.data.mapper.SessionAdmittedPayload
import com.mentorme.app.data.mapper.SessionEndedPayload
import com.mentorme.app.data.mapper.SessionJoinedPayload
import com.mentorme.app.data.mapper.SessionParticipantPayload
import com.mentorme.app.data.mapper.SessionReadyPayload
import com.mentorme.app.data.mapper.SessionSignalPayload
import com.mentorme.app.data.mapper.SessionWaitingPayload
import com.mentorme.app.data.network.api.session.SessionApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

enum class CallPhase {
    Idle,
    Joining,
    WaitingForPeer,
    WaitingForAdmit,
    Connecting,
    InCall,
    Ended,
    Error
}

data class VideoCallUiState(
    val bookingId: String = "",
    val role: String? = null,
    val phase: CallPhase = CallPhase.Idle,
    val admitted: Boolean = false,
    val peerJoined: Boolean = false,
    val peerRole: String? = null,
    val isAudioEnabled: Boolean = true,
    val isVideoEnabled: Boolean = true,
    val isSpeakerEnabled: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val sessionApiService: SessionApiService,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(VideoCallUiState())
    val state: StateFlow<VideoCallUiState> = _state.asStateFlow()

    private var currentBookingId: String? = null
    private var currentRole: String? = null
    private var sessionReady = false
    private var callStarted = false
    private var permissionsGranted = false
    private var pendingOffer: SessionDescription? = null
    private var qosJob: Job? = null

    private val webRtcClient = WebRtcClient(
        context = appContext,
        iceServers = IceServerProvider.defaultIceServers(),
        events = object : WebRtcClient.WebRtcEvents {
            override fun onIceCandidate(candidate: IceCandidate) {
                emitIceCandidate(candidate)
            }

            override fun onConnectionStateChanged(state: PeerConnection.PeerConnectionState) {
                handleConnectionState(state)
            }

            override fun onRemoteTrack(track: org.webrtc.VideoTrack) {
                viewModelScope.launch {
                    _state.update { it.copy(phase = CallPhase.InCall) }
                }
            }
        }
    )

    init {
        observeRealtime()
    }

    fun start(bookingId: String) {
        if (currentBookingId == bookingId) {
            val phase = _state.value.phase
            if (phase == CallPhase.Joining ||
                phase == CallPhase.WaitingForPeer ||
                phase == CallPhase.WaitingForAdmit ||
                phase == CallPhase.Connecting ||
                phase == CallPhase.InCall
            ) {
                return
            }
        }
        resetCallState()
        currentBookingId = bookingId
        _state.update { it.copy(bookingId = bookingId, phase = CallPhase.Joining, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = sessionApiService.createJoinToken(bookingId)
                if (!response.isSuccessful) {
                    setError("HTTP ${response.code()}: ${response.message()}")
                    return@launch
                }

                val envelope: ApiEnvelope<SessionJoinResponse>? = response.body()
                val data = envelope?.data
                val token = data?.token
                if (envelope?.success == true && !token.isNullOrBlank()) {
                    currentRole = data.role
                    _state.update { it.copy(role = data.role) }
                    socketManager.emit("session:join", mapOf("token" to token))
                } else {
                    setError(envelope?.message ?: "Failed to join session")
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to join session")
            }
        }
    }

    fun retry() {
        val bookingId = currentBookingId ?: return
        start(bookingId)
    }

    fun setPermissionsGranted(granted: Boolean) {
        permissionsGranted = granted
        if (granted) {
            pendingOffer?.let { offer ->
                pendingOffer = null
                acceptOffer(offer)
            }
            if (sessionReady) {
                startCallIfReady()
            }
        }
    }

    fun bindLocalRenderer(renderer: org.webrtc.SurfaceViewRenderer) {
        webRtcClient.attachLocalRenderer(renderer)
    }

    fun bindRemoteRenderer(renderer: org.webrtc.SurfaceViewRenderer) {
        webRtcClient.attachRemoteRenderer(renderer)
    }

    fun toggleAudio() {
        val next = !_state.value.isAudioEnabled
        _state.update { it.copy(isAudioEnabled = next) }
        webRtcClient.setAudioEnabled(next)
    }

    fun toggleVideo() {
        val next = !_state.value.isVideoEnabled
        _state.update { it.copy(isVideoEnabled = next) }
        webRtcClient.setVideoEnabled(next)
    }

    fun toggleSpeaker() {
        val next = !_state.value.isSpeakerEnabled
        _state.update { it.copy(isSpeakerEnabled = next) }
        webRtcClient.setSpeakerEnabled(next)
    }

    fun switchCamera() {
        webRtcClient.switchCamera()
    }

    fun admit() {
        val bookingId = currentBookingId ?: return
        socketManager.emit("session:admit", mapOf("bookingId" to bookingId))
    }

    fun endCall() {
        val bookingId = currentBookingId ?: return
        socketManager.emit("session:end", mapOf("bookingId" to bookingId))
        leaveCall()
    }

    fun leaveCall() {
        val bookingId = currentBookingId ?: return
        socketManager.emit("session:leave", mapOf("bookingId" to bookingId))
        cleanupCall()
        _state.update { it.copy(phase = CallPhase.Ended) }
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            RealtimeEventBus.events.collect { event ->
                when (event) {
                    is RealtimeEvent.SessionJoined -> handleSessionJoined(event.payload)
                    is RealtimeEvent.SessionWaiting -> handleSessionWaiting(event.payload)
                    is RealtimeEvent.SessionAdmitted -> handleSessionAdmitted(event.payload)
                    is RealtimeEvent.SessionReady -> handleSessionReady(event.payload)
                    is RealtimeEvent.SessionParticipantJoined -> handleParticipantJoined(event.payload)
                    is RealtimeEvent.SessionParticipantLeft -> handleParticipantLeft(event.payload)
                    is RealtimeEvent.SessionEnded -> handleSessionEnded(event.payload)
                    is RealtimeEvent.SignalOfferReceived -> handleSignalOffer(event.payload)
                    is RealtimeEvent.SignalAnswerReceived -> handleSignalAnswer(event.payload)
                    is RealtimeEvent.SignalIceReceived -> handleSignalIce(event.payload)
                    else -> Unit
                }
            }
        }
    }

    private fun handleSessionJoined(payload: SessionJoinedPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        currentRole = payload.role
        val admitted = payload.admitted == true
        _state.update {
            it.copy(
                role = payload.role,
                admitted = admitted,
                phase = when {
                    payload.role == "mentee" && !admitted -> CallPhase.WaitingForAdmit
                    payload.role == "mentor" && !admitted -> CallPhase.WaitingForPeer
                    else -> CallPhase.Connecting
                }
            )
        }
        if (admitted) {
            sessionReady = true
            startCallIfReady()
        }
    }

    private fun handleSessionWaiting(payload: SessionWaitingPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        _state.update { it.copy(phase = CallPhase.WaitingForAdmit, admitted = false) }
    }

    private fun handleSessionAdmitted(payload: SessionAdmittedPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        _state.update { it.copy(admitted = true, phase = CallPhase.Connecting) }
        sessionReady = true
        startCallIfReady()
    }

    private fun handleSessionReady(payload: SessionReadyPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        _state.update { it.copy(phase = CallPhase.Connecting) }
        sessionReady = true
        startCallIfReady()
    }

    private fun handleParticipantJoined(payload: SessionParticipantPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        _state.update { it.copy(peerJoined = true, peerRole = payload.role) }
        if (currentRole == "mentor" && _state.value.admitted.not()) {
            _state.update { it.copy(phase = CallPhase.WaitingForAdmit) }
        }
    }

    private fun handleParticipantLeft(payload: SessionParticipantPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        _state.update { it.copy(peerJoined = false, peerRole = null, phase = CallPhase.Ended) }
        cleanupCall()
    }

    private fun handleSessionEnded(payload: SessionEndedPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        _state.update { it.copy(phase = CallPhase.Ended) }
        cleanupCall()
    }

    private fun handleSignalOffer(payload: SessionSignalPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        val sdp = parseSessionDescription(payload.data) ?: return
        sessionReady = true
        if (!permissionsGranted) {
            pendingOffer = sdp
            return
        }
        acceptOffer(sdp)
    }

    private fun acceptOffer(sdp: SessionDescription) {
        callStarted = true
        webRtcClient.ensurePeerConnection()
        webRtcClient.startLocalMedia()
        webRtcClient.setRemoteDescription(sdp)
        webRtcClient.createAnswer { answer ->
            emitSdp("signal:answer", answer)
        }
        startQosReporting()
    }

    private fun handleSignalAnswer(payload: SessionSignalPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        val sdp = parseSessionDescription(payload.data) ?: return
        webRtcClient.ensurePeerConnection()
        webRtcClient.setRemoteDescription(sdp)
        startQosReporting()
    }

    private fun handleSignalIce(payload: SessionSignalPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        val candidate = parseIceCandidate(payload.data) ?: return
        webRtcClient.ensurePeerConnection()
        webRtcClient.addIceCandidate(candidate)
    }

    private fun startCallIfReady() {
        if (callStarted || !permissionsGranted) return
        val role = currentRole ?: return
        callStarted = true
        webRtcClient.ensurePeerConnection()
        webRtcClient.setSpeakerEnabled(_state.value.isSpeakerEnabled)
        webRtcClient.startLocalMedia()
        if (role == "mentor") {
            webRtcClient.createOffer { offer ->
                emitSdp("signal:offer", offer)
            }
        }
        startQosReporting()
    }

    private fun emitSdp(event: String, sdp: SessionDescription) {
        val bookingId = currentBookingId ?: return
        val payload = mapOf(
            "bookingId" to bookingId,
            "data" to mapOf(
                "type" to sdp.type.canonicalForm(),
                "sdp" to sdp.description
            )
        )
        socketManager.emit(event, payload)
    }

    private fun emitIceCandidate(candidate: IceCandidate) {
        val bookingId = currentBookingId ?: return
        val payload = mapOf(
            "bookingId" to bookingId,
            "data" to mapOf(
                "sdpMid" to candidate.sdpMid,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "candidate" to candidate.sdp
            )
        )
        socketManager.emit("signal:ice", payload)
    }

    private fun handleConnectionState(state: PeerConnection.PeerConnectionState) {
        when (state) {
            PeerConnection.PeerConnectionState.CONNECTED -> {
                _state.update { it.copy(phase = CallPhase.InCall) }
            }
            PeerConnection.PeerConnectionState.DISCONNECTED,
            PeerConnection.PeerConnectionState.FAILED -> {
                setError("Connection lost")
            }
            PeerConnection.PeerConnectionState.CLOSED -> {
                _state.update { it.copy(phase = CallPhase.Ended) }
            }
            else -> Unit
        }
    }

    private fun parseSessionDescription(data: Map<String, Any?>?): SessionDescription? {
        if (data == null) return null
        val type = data["type"]?.toString()?.lowercase() ?: return null
        val sdp = data["sdp"]?.toString() ?: return null
        val sdpType = when (type) {
            "offer" -> SessionDescription.Type.OFFER
            "answer" -> SessionDescription.Type.ANSWER
            else -> return null
        }
        return SessionDescription(sdpType, sdp)
    }

    private fun parseIceCandidate(data: Map<String, Any?>?): IceCandidate? {
        if (data == null) return null
        val sdpMid = data["sdpMid"]?.toString() ?: return null
        val candidate = data["candidate"]?.toString() ?: return null
        val sdpMLineIndex = when (val raw = data["sdpMLineIndex"]) {
            is Number -> raw.toInt()
            else -> raw?.toString()?.toIntOrNull() ?: 0
        }
        return IceCandidate(sdpMid, sdpMLineIndex, candidate)
    }

    private fun startQosReporting() {
        if (qosJob != null) return
        val bookingId = currentBookingId ?: return
        qosJob = viewModelScope.launch {
            while (true) {
                webRtcClient.getStats { stats ->
                    if (stats != null) {
                        socketManager.emit(
                            "session:qos",
                            mapOf(
                                "bookingId" to bookingId,
                                "stats" to mapOf(
                                    "rttMs" to stats.rttMs,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )
                        )
                    }
                }
                delay(10_000)
            }
        }
    }

    private fun cleanupCall() {
        qosJob?.cancel()
        qosJob = null
        webRtcClient.release()
        callStarted = false
        sessionReady = false
        pendingOffer = null
    }

    private fun resetCallState() {
        cleanupCall()
        currentRole = null
        _state.value = VideoCallUiState()
    }

    private fun setError(message: String) {
        Log.w("VideoCallVM", message)
        _state.update { it.copy(phase = CallPhase.Error, errorMessage = message) }
        cleanupCall()
    }

    override fun onCleared() {
        super.onCleared()
        cleanupCall()
    }
}
