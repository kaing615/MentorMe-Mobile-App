package com.mentorme.app.core.webrtc

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RTCStatsReport
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcClient(
    private val context: Context,
    private val iceServers: List<PeerConnection.IceServer>,
    private val events: WebRtcEvents
) {
    interface WebRtcEvents {
        fun onIceCandidate(candidate: IceCandidate)
        fun onConnectionStateChanged(state: PeerConnection.PeerConnectionState)
        fun onRemoteTrack(track: VideoTrack)
    }

    private val eglBase: EglBase = EglBase.create()
    val eglContext: EglBase.Context get() = eglBase.eglBaseContext
    private val peerConnectionFactory: PeerConnectionFactory by lazy { createPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
    private var localRenderer: SurfaceViewRenderer? = null
    private var remoteRenderer: SurfaceViewRenderer? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var screenCapturer: ScreenCapturer? = null
    private var videoSource: VideoSource? = null
    private var screenVideoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var screenVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var localTracksAttached = false
    private var isScreenSharing = false

    private var isVideoEnabled = true
    private var isAudioEnabled = true
    private var isSpeakerEnabled = true
    
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private var audioFocusRequest: AudioFocusRequest? = null

    companion object {
        private const val TAG = "WebRtcClient"
        private var initialized = false

        fun initialize(context: Context) {
            if (initialized) return
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                    .setEnableInternalTracer(false)
                    .createInitializationOptions()
            )
            initialized = true
        }
    }

    init {
        initialize(context.applicationContext)
    }

    fun attachLocalRenderer(renderer: SurfaceViewRenderer) {
        Log.d(TAG, "attachLocalRenderer called, localVideoTrack exists: ${localVideoTrack != null}")
        try {
            renderer.init(eglBase.eglBaseContext, null)
            renderer.setEnableHardwareScaler(true)
            renderer.setMirror(true)
            renderer.setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            Log.d(TAG, "Local renderer initialized successfully with mirror=true")
            
            localRenderer = renderer
            
            // Add existing track if already created and not disposed
            localVideoTrack?.let { track ->
                try {
                    // Check if track is still valid by checking state
                    if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                        Log.d(TAG, "Adding local video track to renderer")
                        // Ensure track is added on the renderer's thread
                        renderer.post {
                            try {
                                track.addSink(renderer)
                                Log.d(TAG, "Local video track sink added to renderer")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to add sink to local renderer: ${e.message}")
                            }
                        }
                    } else {
                        Log.w(TAG, "Local video track is ended, cannot add sink")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking local track state: ${e.message}")
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Local renderer already initialized: ${e.message}")
            localRenderer = renderer
            // Still try to add the track if it exists and is valid
            localVideoTrack?.let { track ->
                try {
                    if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                        renderer.post {
                            try {
                                track.addSink(renderer)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to add sink in catch block: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in catch block: ${e.message}")
                }
            }
        }
    }

    fun attachRemoteRenderer(renderer: SurfaceViewRenderer) {
        Log.d(TAG, "attachRemoteRenderer called")
        try {
            renderer.init(eglBase.eglBaseContext, null)
            renderer.setEnableHardwareScaler(true)
            renderer.setMirror(false)
            renderer.setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            Log.d(TAG, "Remote renderer initialized successfully with SCALE_ASPECT_FILL")
            
            remoteRenderer = renderer
            // Remote track will be added via onRemoteTrack callback when received
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Remote renderer already initialized: ${e.message}")
            remoteRenderer = renderer
            // Try to set scaling anyway
            try {
                renderer.setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            } catch (ex: Exception) {
                Log.w(TAG, "Could not set scaling type: ${ex.message}")
            }
        }
    }

    fun ensurePeerConnection() {
        if (peerConnection == null) {
            peerConnection = createPeerConnection()
        }
        attachLocalTracksIfPossible()
    }

    fun startLocalMedia() {
        Log.d(TAG, "startLocalMedia called, current tracks - video: ${localVideoTrack != null}, audio: ${localAudioTrack != null}")
        
        // Setup audio for the call
        setupAudioForCall()
        
        if (localVideoTrack == null && localAudioTrack == null) {
            val videoCapturer = createCameraCapturer() ?: return
            val surfaceHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
            val videoSource = peerConnectionFactory.createVideoSource(false)
            videoCapturer.initialize(surfaceHelper, context, videoSource.capturerObserver)
            videoCapturer.startCapture(720, 1280, 30)
            Log.d(TAG, "Camera capturer started: 720x1280@30fps")

            val videoTrack = peerConnectionFactory.createVideoTrack("VIDEO", videoSource)
            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            val audioTrack = peerConnectionFactory.createAudioTrack("AUDIO", audioSource)
            
            Log.d(TAG, "Audio track created - ID: ${audioTrack.id()}, state: ${audioTrack.state()}, enabled: ${audioTrack.enabled()}")

            this.videoCapturer = videoCapturer
            this.videoSource = videoSource
            this.localVideoTrack = videoTrack
            this.audioSource = audioSource
            this.localAudioTrack = audioTrack
            localTracksAttached = false

            localRenderer?.let { renderer ->
                Log.d(TAG, "Adding newly created video track to local renderer")
                // Ensure track is added on the renderer's thread
                renderer.post {
                    try {
                        if (videoTrack.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                            videoTrack.addSink(renderer)
                            renderer.visibility = android.view.View.VISIBLE
                            Log.d(TAG, "Newly created local video track sink added to renderer")
                        } else {
                            Log.w(TAG, "Newly created track is already ended")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add newly created track to renderer: ${e.message}")
                    }
                }
            } ?: Log.w(TAG, "No local renderer available to add video track")
            localVideoTrack?.setEnabled(isVideoEnabled)
            localAudioTrack?.setEnabled(isAudioEnabled)
            Log.d(TAG, "Local media tracks created - video enabled: $isVideoEnabled, audio enabled: $isAudioEnabled")
        }

        attachLocalTracksIfPossible()
    }

    fun createOffer(onSdpReady: (SessionDescription) -> Unit) {
        val pc = peerConnection ?: return
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }
        pc.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                pc.setLocalDescription(SimpleSdpObserver(), sdp)
                onSdpReady(sdp)
            }
        }, constraints)
    }

    fun createAnswer(onSdpReady: (SessionDescription) -> Unit) {
        val pc = peerConnection ?: return
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }
        pc.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                pc.setLocalDescription(SimpleSdpObserver(), sdp)
                onSdpReady(sdp)
            }
        }, constraints)
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun setVideoEnabled(enabled: Boolean) {
        isVideoEnabled = enabled
        val track = localVideoTrack
        if (track != null && track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
            track.setEnabled(enabled)
            Log.d(TAG, "Video track enabled: $enabled, state: ${track.state()}")
        } else {
            Log.w(TAG, "Cannot set video enabled - track is null or ended")
        }
    }

    fun setAudioEnabled(enabled: Boolean) {
        isAudioEnabled = enabled
        val track = localAudioTrack
        if (track != null && track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
            track.setEnabled(enabled)
            Log.d(TAG, "Audio track enabled: $enabled, state: ${track.state()}")
        } else {
            Log.w(TAG, "Cannot set audio enabled - track is null or ended")
        }
    }
    
    private var backgroundBlurProcessor: BackgroundBlurProcessor? = null
    private var isBackgroundBlurEnabled = false
    
    /**
     * Enable/disable background blur effect
     * Note: This is a beta feature and may impact performance
     */
    fun setBackgroundBlurEnabled(enabled: Boolean) {
        isBackgroundBlurEnabled = enabled
        
        if (enabled) {
            if (backgroundBlurProcessor == null) {
                backgroundBlurProcessor = BackgroundBlurProcessor(context)
            }
            backgroundBlurProcessor?.setEnabled(true)
            Log.d(TAG, "Background blur enabled")
        } else {
            backgroundBlurProcessor?.setEnabled(false)
            Log.d(TAG, "Background blur disabled")
        }
    }
    
    fun isBackgroundBlurEnabled(): Boolean = isBackgroundBlurEnabled

    fun switchCamera() {
        val capturer = videoCapturer ?: return
        capturer.switchCamera(null)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        try {
            isSpeakerEnabled = enabled
            
            if (enabled) {
                // Switch to speaker (loa ngoài)
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isSpeakerphoneOn = true
                audioManager.setStreamVolume(
                    AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    0
                )
                Log.d(TAG, "Audio routed to SPEAKER")
            } else {
                // Switch to earpiece (loa trong)
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isSpeakerphoneOn = false
                Log.d(TAG, "Audio routed to EARPIECE")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting speaker mode: ${e.message}", e)
        }
    }
    
    /**
     * Start screen sharing - replaces camera video with screen capture
     * @param mediaProjectionData Intent data from MediaProjection permission result
     */
    fun startScreenSharing(mediaProjectionData: android.content.Intent): Boolean {
        if (isScreenSharing) {
            Log.w(TAG, "Already screen sharing")
            return false
        }
        
        try {
            Log.d(TAG, "Starting screen sharing")
            
            // Start foreground service for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ScreenCaptureService.start(context)
                // Small delay to ensure service is started
                Thread.sleep(100)
            }
            
            // Stop camera capture but keep the track
            videoCapturer?.stopCapture()
            Log.d(TAG, "Camera capture stopped")
            
            // Create screen capturer with cloned intent
            val clonedIntent = mediaProjectionData.clone() as android.content.Intent
            Log.d(TAG, "Intent cloned: $clonedIntent")
            
            val screenCapturer = ScreenCapturer(context, clonedIntent)
            val surfaceHelper = SurfaceTextureHelper.create("ScreenCapture", eglBase.eglBaseContext)
            Log.d(TAG, "SurfaceTextureHelper created: $surfaceHelper")
            
            val screenSource = peerConnectionFactory.createVideoSource(true) // isScreencast = true
            Log.d(TAG, "Screen video source created: $screenSource")
            
            screenCapturer.initialize(surfaceHelper, context, screenSource.capturerObserver)
            Log.d(TAG, "ScreenCapturer initialized")
            
            screenCapturer.startCapture(1280, 720, 15) // Landscape for screen share
            Log.d(TAG, "Screen capture started at 1280x720@15fps")
            
            val screenTrack = peerConnectionFactory.createVideoTrack("SCREEN_${System.currentTimeMillis()}", screenSource)
            screenTrack.setEnabled(true)
            Log.d(TAG, "Screen track created: $screenTrack, enabled: ${screenTrack.enabled()}")
            
            // Find existing video sender and replace track
            val videoSender = peerConnection?.senders?.find { 
                it.track()?.kind() == "video" 
            }
            Log.d(TAG, "Found video sender: $videoSender")
            
            if (videoSender != null) {
                // Replace track instead of add/remove
                val result = videoSender.setTrack(screenTrack, false)
                Log.d(TAG, "Replaced video track with screen track, result: $result")
            } else {
                // No existing sender, add new track
                val sender = peerConnection?.addTrack(screenTrack, listOf("stream"))
                Log.d(TAG, "Added new screen track, sender: $sender")
            }
            
            // Update local preview
            localRenderer?.let { renderer ->
                // Remove camera track from renderer
                localVideoTrack?.let { track ->
                    try { track.removeSink(renderer) } catch (e: Exception) {}
                }
                // Add screen track to renderer
                renderer.post {
                    try {
                        screenTrack.addSink(renderer)
                        renderer.setMirror(false) // Don't mirror screen share
                        Log.d(TAG, "Screen track added to local renderer")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add screen track to renderer: ${e.message}")
                    }
                }
            }
            
            this.screenCapturer = screenCapturer
            this.screenVideoSource = screenSource
            this.screenVideoTrack = screenTrack
            this.isScreenSharing = true
            
            Log.d(TAG, "Screen sharing started successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start screen sharing: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Stop screen sharing and return to camera
     */
    fun stopScreenSharing() {
        if (!isScreenSharing) {
            Log.w(TAG, "Not screen sharing")
            return
        }
        
        try {
            Log.d(TAG, "Stopping screen sharing")
            
            // Find video sender and replace with camera track
            val videoSender = peerConnection?.senders?.find { 
                it.track()?.kind() == "video" 
            }
            
            localVideoTrack?.let { cameraTrack ->
                if (videoSender != null && cameraTrack.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                    videoSender.setTrack(cameraTrack, false)
                    Log.d(TAG, "Replaced screen track with camera track")
                }
            }
            
            // Update local preview
            localRenderer?.let { renderer ->
                // Remove screen track from renderer
                screenVideoTrack?.let { track ->
                    try { track.removeSink(renderer) } catch (e: Exception) {}
                }
                // Add camera track back to renderer
                localVideoTrack?.let { track ->
                    if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                        renderer.post {
                            try {
                                track.addSink(renderer)
                                renderer.setMirror(true) // Mirror camera
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to add camera track to renderer: ${e.message}")
                            }
                        }
                    }
                }
            }
            
            // Stop and dispose screen capturer
            screenCapturer?.stopCapture()
            screenCapturer?.dispose()
            screenVideoSource?.dispose()
            screenVideoTrack?.dispose()
            
            screenCapturer = null
            screenVideoSource = null
            screenVideoTrack = null
            isScreenSharing = false
            
            // Stop foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ScreenCaptureService.stop(context)
            }
            
            // Restart camera capture
            videoCapturer?.startCapture(720, 1280, 30)
            
            Log.d(TAG, "Screen sharing stopped, returned to camera")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop screen sharing: ${e.message}", e)
        }
    }
    
    fun isScreenSharing(): Boolean = isScreenSharing
    
    private fun setupAudioForCall() {
        try {
            // Request audio focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d(TAG, "Audio focus changed: $focusChange")
                    }
                    .build()
                
                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
            
            // Set audio mode for communication
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            
            // Apply current speaker setting
            setSpeakerEnabled(isSpeakerEnabled)
            
            Log.d(TAG, "Audio setup completed - Mode: ${audioManager.mode}, Speaker: ${audioManager.isSpeakerphoneOn}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up audio: ${e.message}", e)
        }
    }
    
    private fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
            
            // Reset audio mode
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            
            Log.d(TAG, "Audio focus released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio focus: ${e.message}", e)
        }
    }

    fun getStats(callback: (RtcStats?) -> Unit) {
        val pc = peerConnection ?: return
        pc.getStats { report ->
            callback(parseStats(report))
        }
    }

    fun release() {
        try {
            // Release audio focus first
            releaseAudioFocus()
            
            // Remove sinks before disposing tracks to prevent crashes
            try {
                localVideoTrack?.let { track ->
                    if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                        track.removeSink(localRenderer)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error removing local video sink: ${e.message}")
            }
            
            videoCapturer?.stopCapture()
        } catch (e: Exception) {
            android.util.Log.w("WebRtcClient", "Error during cleanup: ${e.message}")
        }
        
        try {
            videoCapturer?.dispose()
            videoSource?.dispose()
            audioSource?.dispose()
            localVideoTrack?.dispose()
            localAudioTrack?.dispose()
            peerConnection?.dispose()
            localRenderer?.release()
            remoteRenderer?.release()
        } catch (e: Exception) {
            android.util.Log.w("WebRtcClient", "Error disposing resources: ${e.message}")
        }
        
        videoCapturer = null
        videoSource = null
        audioSource = null
        localVideoTrack = null
        localAudioTrack = null
        peerConnection = null
        localTracksAttached = false
    }

    private fun createPeerConnection(): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        return peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnectionObserver() {
            override fun onIceCandidate(candidate: IceCandidate) {
                events.onIceCandidate(candidate)
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                events.onConnectionStateChanged(newState)
            }

            override fun onTrack(transceiver: RtpTransceiver) {
                val track = transceiver.receiver.track()
                Log.d(TAG, "onTrack received: type=${track?.kind()}, id=${track?.id()}, enabled=${track?.enabled()}, state=${track?.state()}")
                
                if (track is VideoTrack) {
                    Log.d(TAG, "Remote video track received, renderer available: ${remoteRenderer != null}")
                    remoteRenderer?.let { renderer ->
                        Log.d(TAG, "Adding remote video track to renderer")
                        // Ensure track is added on the renderer's thread
                        renderer.post {
                            try {
                                if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                                    track.addSink(renderer)
                                    // Ensure visibility
                                            renderer.visibility = android.view.View.VISIBLE
                                    Log.d(TAG, "Remote video track sink added to renderer, visibility set to VISIBLE")
                                } else {
                                    Log.w(TAG, "Remote track is ended, cannot add sink")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to add remote track to renderer: ${e.message}")
                            }
                        }
                    } ?: Log.w(TAG, "Remote renderer not available to add track")
                    events.onRemoteTrack(track)
                } else if (track is AudioTrack) {
                    Log.d(TAG, "Remote AUDIO track received - ID: ${track.id()}, enabled: ${track.enabled()}, state: ${track.state()}")
                    // Ensure audio track is enabled for playback
                    track.setEnabled(true)
                    Log.d(TAG, "Remote audio track enabled for playback")
                } else {
                    Log.d(TAG, "Non-video/audio track received: ${track?.kind()}")
                }
            }
        })
    }

    private fun attachLocalTracksIfPossible() {
        if (localTracksAttached) return
        val pc = peerConnection ?: return
        val videoTrack = localVideoTrack
        val audioTrack = localAudioTrack
        if (videoTrack == null && audioTrack == null) return
        
        videoTrack?.let { 
            pc.addTrack(it)
            Log.d(TAG, "Local video track attached to peer connection")
        }
        audioTrack?.let { 
            pc.addTrack(it)
            Log.d(TAG, "Local audio track attached to peer connection - enabled: ${it.enabled()}, state: ${it.state()}")
        }
        
        localTracksAttached = true
        Log.d(TAG, "Local tracks attached - video: ${videoTrack != null}, audio: ${audioTrack != null}")
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        val encoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            true,
            true
        )
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        val frontName = deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
        val backName = deviceNames.firstOrNull { enumerator.isBackFacing(it) }

        val chosen = frontName ?: backName
        if (chosen == null) return null
        return enumerator.createCapturer(chosen, object : CameraVideoCapturer.CameraEventsHandler {
            override fun onCameraError(errorDescription: String) {
                Log.w(TAG, "Camera error: $errorDescription")
            }

            override fun onCameraDisconnected() {
                Log.w(TAG, "Camera disconnected")
            }

            override fun onCameraFreezed(errorDescription: String) {
                Log.w(TAG, "Camera freeze: $errorDescription")
            }

            override fun onCameraOpening(cameraName: String) {
                Log.d(TAG, "Camera opening: $cameraName")
            }

            override fun onFirstFrameAvailable() {
                Log.d(TAG, "First frame available")
            }

            override fun onCameraClosed() {
                Log.d(TAG, "Camera closed")
            }
        })
    }

    private fun parseStats(report: RTCStatsReport): RtcStats? {
        val statsMap = report.statsMap
        val candidatePair = statsMap.values.firstOrNull { stat ->
            stat.type == "candidate-pair" && (stat.members["state"] == "succeeded") &&
                (stat.members["nominated"] == true || stat.members["selected"] == true)
        }
        val rttSeconds = candidatePair?.members?.get("currentRoundTripTime") as? Number
        val rttMs = rttSeconds?.toDouble()?.times(1000.0)
        if (rttMs == null) return null
        return RtcStats(rttMs = rttMs)
    }
}

data class RtcStats(
    val rttMs: Double
)

open class SimpleSdpObserver : org.webrtc.SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String) {
        Log.w("WebRtcClient", "SDP create failed: $error")
    }
    override fun onSetFailure(error: String) {
        Log.w("WebRtcClient", "SDP set failed: $error")
    }
}

open class PeerConnectionObserver : PeerConnection.Observer {
    override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
    override fun onIceConnectionReceivingChange(receiving: Boolean) {}
    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
    override fun onIceCandidate(candidate: IceCandidate) {}
    override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {}
    override fun onAddStream(stream: org.webrtc.MediaStream) {}
    override fun onRemoveStream(stream: org.webrtc.MediaStream) {}
    override fun onDataChannel(dataChannel: org.webrtc.DataChannel) {}
    override fun onRenegotiationNeeded() {}
    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {}
    override fun onTrack(transceiver: RtpTransceiver) {}
}
