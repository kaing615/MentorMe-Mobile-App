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
import com.mentorme.app.data.mapper.SessionStatusPayload
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
    Reconnecting,
    Ended,
    Error
}

enum class NetworkQuality {
    Excellent, Good, Fair, Poor, VeryPoor, Unknown
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
    val callDurationSec: Long = 0,
    val errorMessage: String? = null,
    // Network quality
    val networkQuality: NetworkQuality = NetworkQuality.Unknown,
    val rttMs: Double? = null,
    // Peer status
    val peerAudioEnabled: Boolean = true,
    val peerVideoEnabled: Boolean = true,
    val peerConnectionStatus: String? = null, // "connected", "reconnecting", "disconnected"
    // Reconnection
    val reconnectAttempt: Int = 0,
    val maxReconnectAttempts: Int = 5,
    // Preview mode
    val isPreviewMode: Boolean = true,
    // Toast messages
    val toastMessage: String? = null,
    // Booking time management
    val bookingEndTime: Long? = null, // timestamp in millis
    val remainingMinutes: Int? = null,
    val showTimeWarning: Boolean = false,
    val timeWarningMessage: String? = null
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
    private var timerJob: Job? = null
    private var callStartedAtMs: Long? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private var renderersAttached = false
    private var timeCheckJob: Job? = null
    private var participantLeftJob: Job? = null
    private var statusEmitJob: Job? = null
    private var stopEmissionJob: Job? = null

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
                markInCall()
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
        resetReconnectState()
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
                    
                    // Parse booking end time from expiresAt (if available)
                    // Assuming expiresAt is ISO timestamp, parse and add grace period
                    val bookingEnd = data.expiresAt?.let { parseIsoTimestamp(it) }
                    bookingEnd?.let {
                        val endWithGrace = it + (10 * 60 * 1000) // +10 minutes grace period
                        _state.update { state -> state.copy(bookingEndTime = endWithGrace) }
                        startTimeCheckJob()
                    }
                    
                    _state.update { it.copy(role = data.role) }
                    Log.d("VideoCallVM", "Got join token for role: ${data.role}, emitting session:join")
                    socketManager.emit("session:join", mapOf("token" to token))
                    Log.d("VideoCallVM", "Emitted session:join event")
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
        val role = currentRole ?: return
        
        Log.d("VideoCallVM", "Retrying connection - bookingId: $bookingId, role: $role, reconnectAttempt: $reconnectAttempts")
        
        // Don't restart the entire session - just recreate peer connection
        webRtcClient.release()
        webRtcClient.ensurePeerConnection()
        webRtcClient.setSpeakerEnabled(_state.value.isSpeakerEnabled)
        webRtcClient.startLocalMedia()
        
        _state.update { it.copy(phase = CallPhase.Connecting) }
        
        // Re-initiate WebRTC handshake based on role
        if (role == "mentor") {
            Log.d("VideoCallVM", "Mentor recreating offer for retry")
            webRtcClient.createOffer { offer ->
                Log.d("VideoCallVM", "Retry offer created, emitting to peer")
                emitSdp("signal:offer", offer)
            }
        } else {
            Log.d("VideoCallVM", "Mentee waiting for new offer after retry")
        }
    }

    fun setPermissionsGranted(granted: Boolean) {
        permissionsGranted = granted
        if (granted && renderersAttached) {
            startLocalPreview()
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
        checkRenderersReady()
    }

    fun bindRemoteRenderer(renderer: org.webrtc.SurfaceViewRenderer) {
        webRtcClient.attachRemoteRenderer(renderer)
        checkRenderersReady()
    }
    
    private fun checkRenderersReady() {
        if (!renderersAttached) {
            renderersAttached = true
            // Start local preview if permissions are already granted
            if (permissionsGranted) {
                startLocalPreview()
                // Don't auto-start call - wait for user to exit preview mode
            }
        }
    }

    fun toggleAudio() {
        val next = !_state.value.isAudioEnabled
        _state.update { it.copy(isAudioEnabled = next, toastMessage = if (next) "Microphone on" else "Microphone muted") }
        webRtcClient.setAudioEnabled(next)
    }

    fun toggleVideo() {
        val next = !_state.value.isVideoEnabled
        _state.update { it.copy(isVideoEnabled = next, toastMessage = if (next) "Camera on" else "Camera off") }
        webRtcClient.setVideoEnabled(next)
    }

    fun toggleSpeaker() {
        val next = !_state.value.isSpeakerEnabled
        _state.update { it.copy(isSpeakerEnabled = next, toastMessage = if (next) "Speaker on" else "Earpiece on") }
        webRtcClient.setSpeakerEnabled(next)
    }

    fun switchCamera() {
        webRtcClient.switchCamera()
    }
    
    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }
    
    fun dismissTimeWarning() {
        _state.update { it.copy(showTimeWarning = false) }
    }
    
    fun exitPreviewMode() {
        _state.update { it.copy(isPreviewMode = false) }
        // Now start the actual call
        if (sessionReady) {
            startCallIfReady()
        }
    }
    
    fun onAppGoesToBackground() {
        // Pause video when app goes to background to save battery
        if (_state.value.phase == CallPhase.InCall) {
            webRtcClient.setVideoEnabled(false)
        }
    }
    
    fun onAppComesToForeground() {
        // Resume video if it was enabled before
        if (_state.value.phase == CallPhase.InCall && _state.value.isVideoEnabled) {
            webRtcClient.setVideoEnabled(true)
        }
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
                    is RealtimeEvent.SessionStatusChanged -> handleSessionStatus(event.payload)
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
        
        Log.d("VideoCallVM", "Session joined - role: ${payload.role}, admitted: $admitted")
        
        val phase = when {
            payload.role == "mentee" && !admitted -> CallPhase.WaitingForAdmit
            payload.role == "mentor" -> CallPhase.WaitingForPeer // Mentor always waits for mentee first
            else -> CallPhase.Connecting
        }
        
        _state.update {
            it.copy(
                role = payload.role,
                admitted = admitted,
                phase = phase
            )
        }
        
        // Mentor is ready to receive events
        if (payload.role == "mentor") {
            sessionReady = true
        } else if (admitted) {
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
        
        Log.d("VideoCallVM", "Session admitted for booking: $bookingId")
        
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
        
        // Cancel delayed end call if peer rejoins
        participantLeftJob?.cancel()
        participantLeftJob = null
        
        // Reset reconnection state when peer successfully rejoins
        // This prevents accumulated reconnect attempts from ending the call
        resetReconnectState()
        
        val peerRole = payload.role
        Log.d("VideoCallVM", "Participant joined - peerRole: $peerRole, myRole: $currentRole, currentAdmitted: ${_state.value.admitted}")
        
        _state.update { it.copy(peerJoined = true, peerRole = peerRole) }
        
        // If I'm mentor and a mentee joined, show admission UI
        if (currentRole == "mentor" && peerRole == "mentee") {
            _state.update { it.copy(
                phase = CallPhase.WaitingForAdmit,
                admitted = false  // Reset admitted state to show admit button
            ) }
            Log.d("VideoCallVM", "Mentee joined, waiting for mentor to admit")
        }
    }

    private fun handleParticipantLeft(payload: SessionParticipantPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        
        // Update UI to show peer left
        _state.update { it.copy(peerJoined = false, peerRole = null) }
        
        // Don't end call immediately - give peer time to reconnect
        // Cancel any existing delayed end call
        participantLeftJob?.cancel()
        participantLeftJob = viewModelScope.launch {
            Log.d("VideoCallVM", "Peer left, waiting ${PARTICIPANT_LEFT_TIMEOUT_MS / 1000}s before ending call")
            delay(PARTICIPANT_LEFT_TIMEOUT_MS)
            Log.d("VideoCallVM", "Peer did not rejoin after ${PARTICIPANT_LEFT_TIMEOUT_MS / 1000}s, ending call")
            _state.update { it.copy(phase = CallPhase.Ended) }
            cleanupCall()
        }
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
        
        Log.d("VideoCallVM", "Received offer from peer")
        
        // Cancel participantLeftJob - peer is sending offer, they're still active
        if (participantLeftJob != null) {
            Log.d("VideoCallVM", "Received offer from peer - cancelling participantLeftJob")
            participantLeftJob?.cancel()
            participantLeftJob = null
        }
        
        val sdp = parseSessionDescription(payload.data) ?: run {
            Log.e("VideoCallVM", "Failed to parse offer SDP")
            return
        }
        
        sessionReady = true
        
        if (!permissionsGranted) {
            Log.d("VideoCallVM", "Permissions not granted, storing pending offer")
            pendingOffer = sdp
            return
        }
        
        acceptOffer(sdp)
    }

    private fun acceptOffer(sdp: SessionDescription) {
        Log.d("VideoCallVM", "Accepting offer and creating answer")
        
        if (!permissionsGranted) {
            Log.w("VideoCallVM", "Cannot accept offer - permissions not granted")
            return
        }
        
        callStarted = true
        webRtcClient.ensurePeerConnection()
        webRtcClient.startLocalMedia()
        webRtcClient.setRemoteDescription(sdp)
        
        webRtcClient.createAnswer { answer ->
            Log.d("VideoCallVM", "Answer created, emitting to peer")
            emitSdp("signal:answer", answer)
        }
        
        startQosReporting()
    }

    private fun handleSignalAnswer(payload: SessionSignalPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        
        Log.d("VideoCallVM", "Received answer from peer")
        
        // Cancel participantLeftJob - peer is sending answer, they're still active
        if (participantLeftJob != null) {
            Log.d("VideoCallVM", "Received answer from peer - cancelling participantLeftJob")
            participantLeftJob?.cancel()
            participantLeftJob = null
        }
        
        val sdp = parseSessionDescription(payload.data) ?: run {
            Log.e("VideoCallVM", "Failed to parse answer SDP")
            return
        }
        
        webRtcClient.ensurePeerConnection()
        webRtcClient.setRemoteDescription(sdp)
        
        Log.d("VideoCallVM", "Remote description set, starting QoS reporting")
        startQosReporting()
    }

    private fun handleSignalIce(payload: SessionSignalPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        
        val candidate = parseIceCandidate(payload.data) ?: run {
            Log.w("VideoCallVM", "Failed to parse ICE candidate")
            return
        }
        
        Log.d("VideoCallVM", "Received ICE candidate from peer: ${candidate.sdpMid}")
        
        // If we receive ICE candidate from peer, they're still trying to connect
        // Cancel any pending participantLeftJob
        if (participantLeftJob != null) {
            Log.d("VideoCallVM", "Received ICE from peer - cancelling participantLeftJob")
            participantLeftJob?.cancel()
            participantLeftJob = null
        }
        
        webRtcClient.ensurePeerConnection()
        webRtcClient.addIceCandidate(candidate)
    }
    
    private fun handleSessionStatus(payload: SessionStatusPayload) {
        val bookingId = payload.bookingId ?: return
        if (bookingId != currentBookingId) return
        
        val jobActive = participantLeftJob?.isActive == true
        Log.d("VideoCallVM", "Peer status changed: ${payload.status} (userId: ${payload.userId}, participantLeftJob active: $jobActive)")
        
        // Cancel delayed end call if peer is reconnecting or connected
        // This means peer is still in the session and trying to recover
        if (payload.status == "reconnecting" || payload.status == "connected") {
            val wasCancelled = participantLeftJob?.isActive == true
            participantLeftJob?.cancel()
            participantLeftJob = null
            Log.d("VideoCallVM", "Cancelled participantLeftJob (was ${if (wasCancelled) "active" else "inactive"}) - peer status: ${payload.status}")
        }
        
        // If peer reconnected successfully, reset our reconnection state
        // But keep emitting status if we're in a stable state to help peer
        if (payload.status == "connected") {
            reconnectAttempts = 0
            reconnectJob?.cancel()
            reconnectJob = null
            // Don't stop status emission yet - keep emitting to ensure peer is stable
            
            // If we're in call and we're the mentor, we need to create a new offer
            // because the mentee is waiting for us to initiate the handshake
            val currentPhase = _state.value.phase
            if (currentRole == "mentor" && (currentPhase == CallPhase.InCall || currentPhase == CallPhase.Reconnecting)) {
                Log.d("VideoCallVM", "Peer reconnected, mentor creating new offer")
                webRtcClient.createOffer { offer ->
                    Log.d("VideoCallVM", "New offer created for reconnected peer, emitting")
                    emitSdp("signal:offer", offer)
                }
            }
            
            // If we were in reconnecting state and we're the mentee, the mentor will send a new offer
            if (currentPhase == CallPhase.Reconnecting) {
                if (currentRole == "mentee") {
                    // Mentee just waits for new offer from mentor
                    _state.update { it.copy(phase = CallPhase.Connecting) }
                }
            }
        }
        
        _state.update { it.copy(peerConnectionStatus = payload.status) }
    }

    private fun startCallIfReady() {
        Log.d("VideoCallVM", "startCallIfReady - callStarted: $callStarted, permissionsGranted: $permissionsGranted, role: $currentRole")
        
        if (callStarted || !permissionsGranted) {
            Log.d("VideoCallVM", "Cannot start call - already started or permissions not granted")
            return
        }
        
        val role = currentRole ?: run {
            Log.w("VideoCallVM", "Cannot start call - no role assigned")
            return
        }
        
        if (!permissionsGranted) {
            Log.w("VideoCallVM", "Cannot start call - permissions not granted")
            return
        }
        
        callStarted = true
        Log.d("VideoCallVM", "Starting call as $role")
        
        webRtcClient.ensurePeerConnection()
        webRtcClient.setSpeakerEnabled(_state.value.isSpeakerEnabled)
        webRtcClient.startLocalMedia()
        
        if (role == "mentor") {
            Log.d("VideoCallVM", "Mentor creating offer")
            webRtcClient.createOffer { offer ->
                Log.d("VideoCallVM", "Offer created, emitting to peer")
                emitSdp("signal:offer", offer)
            }
        } else {
            Log.d("VideoCallVM", "Mentee waiting for offer")
        }
        
        startQosReporting()
    }

    private fun startLocalPreview() {
        webRtcClient.startLocalMedia()
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
        Log.d("VideoCallVM", "PeerConnection state changed: $state, current phase: ${_state.value.phase}")
        when (state) {
            PeerConnection.PeerConnectionState.CONNECTED -> {
                markInCall()
                reconnectAttempts = 0
                reconnectJob?.cancel()
                reconnectJob = null
                // Start emitting "connected" status periodically to ensure peer receives it
                // This will cancel peer's participantLeftJob
                startStatusEmission("connected")
            }
            PeerConnection.PeerConnectionState.DISCONNECTED,
            PeerConnection.PeerConnectionState.FAILED -> {
                // Immediately notify peer that we're having connection issues
                // This helps them cancel any participant-left timer
                currentBookingId?.let { bookingId ->
                    socketManager.emit("session:status", mapOf(
                        "bookingId" to bookingId,
                        "status" to "reconnecting"
                    ))
                }
                handleReconnect("Connection lost")
            }
            PeerConnection.PeerConnectionState.CLOSED -> {
                // Don't end call if we're reconnecting - CLOSED is expected when recreating peer connection
                val currentPhase = _state.value.phase
                if (currentPhase != CallPhase.Reconnecting && currentPhase != CallPhase.Connecting) {
                    Log.d("VideoCallVM", "PeerConnection closed, ending call")
                    _state.update { it.copy(phase = CallPhase.Ended) }
                    cleanupCall()
                } else {
                    Log.d("VideoCallVM", "PeerConnection closed during reconnect/connect, ignoring")
                }
            }
            else -> Unit
        }
    }

    private fun markInCall() {
        if (callStartedAtMs == null) {
            callStartedAtMs = System.currentTimeMillis()
        }
        reconnectAttempts = 0
        _state.update { it.copy(
            phase = CallPhase.InCall, 
            errorMessage = null,
            reconnectAttempt = 0,
            peerConnectionStatus = null // Clear peer status when we're connected
        ) }
        
        // Notify peer that we're connected (immediate single emit)
        // Periodic emission will continue in handleConnectionState
        currentBookingId?.let { bookingId ->
            val socketConnected = socketManager.isConnected()
            Log.d("VideoCallVM", "Marking in call, emitting connected status (socket connected: $socketConnected)")
            socketManager.emit("session:status", mapOf(
                "bookingId" to bookingId,
                "status" to "connected"
            ))
        }
        
        startCallTimer()
    }

    private fun startCallTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            while (true) {
                val start = callStartedAtMs ?: System.currentTimeMillis()
                val elapsed = (System.currentTimeMillis() - start) / 1000
                _state.update { it.copy(callDurationSec = elapsed) }
                delay(1_000)
            }
        }
    }

    private fun handleReconnect(reason: String) {
        if (_state.value.phase == CallPhase.Ended || _state.value.phase == CallPhase.Error) return
        
        // Don't start another reconnect if we're already reconnecting or connecting
        // This prevents multiple reconnect attempts from the same network issue
        val currentPhase = _state.value.phase
        if (currentPhase == CallPhase.Reconnecting || currentPhase == CallPhase.Connecting) {
            Log.d("VideoCallVM", "Already reconnecting/connecting (phase=$currentPhase), ignoring reconnect request: $reason")
            return
        }
        
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            setError("Reconnect failed after ${MAX_RECONNECT_ATTEMPTS} attempts")
            // Notify peer about disconnection
            currentBookingId?.let { bookingId ->
                socketManager.emit("session:status", mapOf(
                    "bookingId" to bookingId,
                    "status" to "disconnected"
                ))
            }
            return
        }
        reconnectAttempts += 1
        Log.d("VideoCallVM", "Starting reconnect attempt $reconnectAttempts/$MAX_RECONNECT_ATTEMPTS")
        _state.update { it.copy(
            phase = CallPhase.Reconnecting, 
            errorMessage = reason,
            reconnectAttempt = reconnectAttempts
        ) }
        
        // Start periodic status emission to ensure peer receives it
        startStatusEmission("reconnecting")
        
        if (reconnectJob == null) {
            reconnectJob = viewModelScope.launch {
                delay(RECONNECT_DELAY_MS)
                reconnectJob = null
                retry()
            }
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
    
    private fun calculateNetworkQuality(rttMs: Double): NetworkQuality {
        return when {
            rttMs < 100 -> NetworkQuality.Excellent
            rttMs < 200 -> NetworkQuality.Good
            rttMs < 400 -> NetworkQuality.Fair
            rttMs < 800 -> NetworkQuality.Poor
            else -> NetworkQuality.VeryPoor
        }
    }
    
    private fun startTimeCheckJob() {
        if (timeCheckJob != null) return
        val endTime = _state.value.bookingEndTime ?: return
        
        timeCheckJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val remainingMs = endTime - now
                val remainingMinutes = (remainingMs / 60000).toInt()
                
                _state.update { it.copy(remainingMinutes = remainingMinutes) }
                
                when {
                    remainingMs <= 0 -> {
                        // Time's up, end the call
                        _state.update { it.copy(
                            showTimeWarning = true,
                            timeWarningMessage = "Booking time has ended. The call will now disconnect."
                        ) }
                        delay(3000) // Show message for 3 seconds
                        endCall()
                        break
                    }
                    remainingMinutes == 5 && !_state.value.showTimeWarning -> {
                        // 5 minutes warning
                        _state.update { it.copy(
                            showTimeWarning = true,
                            timeWarningMessage = "Your booking will end in 5 minutes."
                        ) }
                    }
                    remainingMinutes == 2 && _state.value.timeWarningMessage != "Your booking will end in 2 minutes." -> {
                        // 2 minutes warning
                        _state.update { it.copy(
                            showTimeWarning = true,
                            timeWarningMessage = "Your booking will end in 2 minutes."
                        ) }
                    }
                    remainingMinutes == 1 && _state.value.timeWarningMessage != "Your booking will end in 1 minute!" -> {
                        // 1 minute final warning
                        _state.update { it.copy(
                            showTimeWarning = true,
                            timeWarningMessage = "Your booking will end in 1 minute!"
                        ) }
                    }
                }
                
                delay(30_000) // Check every 30 seconds
            }
        }
    }
    
    private fun parseIsoTimestamp(iso: String): Long? {
        return try {
            // Assuming ISO 8601 format like "2025-12-31T10:30:00.000Z"
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
            formatter.parse(iso)?.time
        } catch (e: Exception) {
            android.util.Log.e("VideoCallVM", "Failed to parse timestamp: $iso", e)
            null
        }
    }

    private fun startQosReporting() {
        if (qosJob != null) return
        val bookingId = currentBookingId ?: return
        qosJob = viewModelScope.launch {
            while (true) {
                webRtcClient.getStats { stats ->
                    if (stats != null) {
                        val rtt = stats.rttMs
                        val quality = calculateNetworkQuality(rtt)
                        _state.update { it.copy(
                            networkQuality = quality,
                            rttMs = rtt
                        ) }
                        
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
    
    private fun startStatusEmission(status: String) {
        // Cancel any existing emission job
        statusEmitJob?.cancel()
        stopEmissionJob?.cancel()
        
        statusEmitJob = viewModelScope.launch {
            var emissionCount = 0
            while (true) {
                currentBookingId?.let { bookingId ->
                    emissionCount++
                    val socketConnected = socketManager.isConnected()
                    Log.d("VideoCallVM", "Emitting status #$emissionCount: $status for booking: $bookingId (socket connected: $socketConnected)")
                    socketManager.emit("session:status", mapOf(
                        "bookingId" to bookingId,
                        "status" to status
                    ))
                }
                delay(3_000) // Emit every 3 seconds
            }
        }
        
        // Auto-stop emission after 30 seconds to save resources
        // By this time peer should have received the status
        stopEmissionJob = viewModelScope.launch {
            delay(30_000)
            Log.d("VideoCallVM", "Auto-stopping status emission after 30s")
            stopStatusEmission()
        }
    }
    
    private fun stopStatusEmission() {
        val wasActive = statusEmitJob?.isActive == true
        statusEmitJob?.cancel()
        statusEmitJob = null
        stopEmissionJob?.cancel()
        stopEmissionJob = null
        if (wasActive) {
            Log.d("VideoCallVM", "Stopped status emission")
        }
    }

    private fun cleanupCall() {
        qosJob?.cancel()
        qosJob = null
        timerJob?.cancel()
        timerJob = null
        reconnectJob?.cancel()
        reconnectJob = null
        timeCheckJob?.cancel()
        timeCheckJob = null
        participantLeftJob?.cancel()
        participantLeftJob = null
        statusEmitJob?.cancel()
        statusEmitJob = null
        stopEmissionJob?.cancel()
        stopEmissionJob = null
        webRtcClient.release()
        callStarted = false
        sessionReady = false
        pendingOffer = null
        callStartedAtMs = null
    }

    private fun resetCallState() {
        cleanupCall()
        currentRole = null
        _state.value = VideoCallUiState()
    }

    private fun resetReconnectState() {
        Log.d("VideoCallVM", "Resetting reconnect state")
        reconnectAttempts = 0
        reconnectJob?.cancel()
        reconnectJob = null
        stopStatusEmission()
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

    private companion object {
        const val RECONNECT_DELAY_MS = 5_000L // 5 seconds - give more time for network recovery
        const val MAX_RECONNECT_ATTEMPTS = 5 // Match UI state
        const val PARTICIPANT_LEFT_TIMEOUT_MS = 120_000L // 120 seconds - wait for socket reconnect + emit status
    }
}
