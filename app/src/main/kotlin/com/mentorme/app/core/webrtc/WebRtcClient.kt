package com.mentorme.app.core.webrtc

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
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
import org.webrtc.RtpSender
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSource
import org.webrtc.VideoSink
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
        fun onRenegotiationNeeded() {} // Optional: called when tracks change and renegotiation is needed
    }

    private val eglBase: EglBase = EglBase.create()
    val eglContext: EglBase.Context get() = eglBase.eglBaseContext
    private val peerConnectionFactory: PeerConnectionFactory by lazy { createPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
    private var localRenderer: SurfaceViewRenderer? = null
    private var remoteRenderer: SurfaceViewRenderer? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var screenCapturer: ScreenCapturer? = null
    private var mediaProjection: MediaProjection? = null
    private var videoSource: VideoSource? = null
    private var screenVideoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var screenVideoTrack: VideoTrack? = null
    private var videoSender: RtpSender? = null  // Original video sender, saved for track replacement
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
        
        // Detach from old renderer if exists
        detachOldLocalRenderer(renderer)
        
        try {
            renderer.init(eglBase.eglBaseContext, null)
            renderer.setMirror(true)
            Log.d(TAG, "Local SurfaceViewRenderer initialized successfully with mirror=true")
            
            localRenderer = renderer
            
            // Add existing track if already created and not disposed
            addTrackToLocalRenderer()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Local renderer already initialized: ${e.message}")
            localRenderer = renderer
            // Still try to add the track if it exists and is valid
            addTrackToLocalRenderer()
        }
    }
    
    private fun detachOldLocalRenderer(newRenderer: SurfaceViewRenderer) {
        localRenderer?.let { oldRenderer ->
            if (oldRenderer != newRenderer) {
                localVideoTrack?.let { track ->
                    try { track.removeSink(oldRenderer) } catch (e: Exception) {}
                }
                screenVideoTrack?.let { track ->
                    try { track.removeSink(oldRenderer) } catch (e: Exception) {}
                }
            }
        }
    }
    
    private fun addTrackToLocalRenderer() {
        val renderer = localRenderer ?: return
        
        // Add screen track if screen sharing, otherwise camera track
        val trackToAdd = if (isScreenSharing) screenVideoTrack else localVideoTrack
        
        trackToAdd?.let { track ->
            try {
                if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                    Log.d(TAG, "Adding video track to local renderer (isScreenSharing: $isScreenSharing)")
                    renderer.post {
                        try {
                            track.addSink(renderer)
                            renderer.visibility = android.view.View.VISIBLE
                            Log.d(TAG, "Video track sink added to local renderer")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add sink to local renderer: ${e.message}")
                        }
                    }
                } else {
                    Log.w(TAG, "Video track is ended, cannot add sink")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding track to local renderer: ${e.message}")
            }
        } ?: Log.d(TAG, "No video track available to add to renderer yet")
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

            // Add track to renderer using helper method
            addTrackToLocalRenderer()
            
            localVideoTrack?.setEnabled(isVideoEnabled)
            localAudioTrack?.setEnabled(isAudioEnabled)
            Log.d(TAG, "Local media tracks created - video enabled: $isVideoEnabled, audio enabled: $isAudioEnabled")
        } else {
            Log.d(TAG, "Local tracks already exist, ensuring they are added to renderer")
            // Ensure existing tracks are connected to renderer
            addTrackToLocalRenderer()
        }

        attachLocalTracksIfPossible()
    }

    fun createOffer(onSdpReady: (SessionDescription) -> Unit, iceRestart: Boolean = false) {
        val pc = peerConnection ?: return
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            if (iceRestart) {
                mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
                Log.d(TAG, "Creating offer with ICE restart")
            }
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
    
    /**
     * Get detailed connection statistics for quality monitoring
     */
    /**
     * Trigger ICE restart when connection is failing
     */
    fun restartIce(onOfferReady: (SessionDescription) -> Unit) {
        Log.d(TAG, "Triggering ICE restart")
        createOffer(onOfferReady, iceRestart = true)
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
            
            // Start foreground service for Android 10+ BEFORE creating MediaProjection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ScreenCaptureService.start(context)
                
                // Wait for service to actually be running (max 2 seconds)
                var waitTime = 0
                val maxWait = 2000
                val checkInterval = 50
                while (!ScreenCaptureService.isServiceRunning() && waitTime < maxWait) {
                    Thread.sleep(checkInterval.toLong())
                    waitTime += checkInterval
                }
                
                if (!ScreenCaptureService.isServiceRunning()) {
                    Log.e(TAG, "Foreground service failed to start within $maxWait ms")
                    return false
                }
                Log.d(TAG, "Foreground service started for screen capture (waited ${waitTime}ms)")
            }
            
            // Create MediaProjection from the intent - this can only be done ONCE per intent
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionData)
            
            if (projection == null) {
                Log.e(TAG, "Failed to create MediaProjection - null returned")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ScreenCaptureService.stop(context)
                }
                return false
            }
            
            this.mediaProjection = projection
            Log.d(TAG, "MediaProjection created successfully: $projection")
            
            // Stop camera capture but keep the track
            videoCapturer?.stopCapture()
            Log.d(TAG, "Camera capture stopped")
            
            // Create screen capturer with MediaProjection (not Intent)
            val screenCapturer = ScreenCapturer(context, projection)
            val surfaceHelper = SurfaceTextureHelper.create("ScreenCapture", eglBase.eglBaseContext)
            Log.d(TAG, "SurfaceTextureHelper created: $surfaceHelper")
            
            val screenSource = peerConnectionFactory.createVideoSource(true) // isScreencast = true
            Log.d(TAG, "Screen video source created: $screenSource")
            
            screenCapturer.initialize(surfaceHelper, context, screenSource.capturerObserver)
            Log.d(TAG, "ScreenCapturer initialized")
            
            screenCapturer.startCapture(1280, 720, 24) // Landscape for screen share with smooth fps
            Log.d(TAG, "Screen capture started at 1280x720@15fps")
            
            val screenTrack = peerConnectionFactory.createVideoTrack("SCREEN_${System.currentTimeMillis()}", screenSource)
            screenTrack.setEnabled(true)
            Log.d(TAG, "Screen track created: $screenTrack, enabled: ${screenTrack.enabled()}")
            
            // Replace video track on existing sender (MUST use same sender to keep same m= line)
            val sender = videoSender
            if (sender != null) {
                try {
                    Log.d(TAG, "Replacing camera track with screen track on sender: $sender")
                    val result = sender.setTrack(screenTrack, false)
                    Log.d(TAG, "setTrack result: $result")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to setTrack on videoSender: ${e.message}", e)
                    // Fallback: try to find sender from transceivers
                    peerConnection?.transceivers?.find { 
                        it.mediaType == org.webrtc.MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO 
                    }?.sender?.let { transceiverSender ->
                        try {
                            Log.d(TAG, "Trying transceiver sender: $transceiverSender")
                            transceiverSender.setTrack(screenTrack, false)
                            Log.d(TAG, "setTrack via transceiver succeeded")
                        } catch (e2: Exception) {
                            Log.e(TAG, "Failed to setTrack via transceiver: ${e2.message}")
                        }
                    }
                }
            } else {
                Log.e(TAG, "videoSender is null, cannot replace track")
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
                        renderer.visibility = android.view.View.VISIBLE
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
            
            // Trigger renegotiation to notify peer about track change
            Log.d(TAG, "Screen sharing started successfully, triggering renegotiation")
            events.onRenegotiationNeeded()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start screen sharing: ${e.message}", e)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ScreenCaptureService.stop(context)
            }
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
            
            // First, stop and dispose screen capturer
            screenCapturer?.stopCapture()
            screenCapturer?.dispose()
            screenCapturer = null
            Log.d(TAG, "Screen capturer stopped")
            
            // Stop MediaProjection
            try {
                mediaProjection?.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping MediaProjection: ${e.message}")
            }
            mediaProjection = null
            
            // Stop foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ScreenCaptureService.stop(context)
            }
            
            // Restart camera capture FIRST (before setting track)
            Log.d(TAG, "Restarting camera capture")
            videoCapturer?.startCapture(720, 1280, 30)
            
            // Small delay to let camera start producing frames
            Thread.sleep(100)
            
            // Replace screen track with camera track on existing sender
            val sender = videoSender
            localVideoTrack?.let { cameraTrack ->
                if (sender != null) {
                    try {
                        Log.d(TAG, "Replacing screen track with camera track on sender: $sender, track state: ${cameraTrack.state()}")
                        cameraTrack.setEnabled(true)
                        sender.setTrack(cameraTrack, false)
                        Log.d(TAG, "Camera track restored on sender")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to restore camera track: ${e.message}", e)
                    }
                } else {
                    Log.w(TAG, "Cannot restore camera - videoSender is null")
                }
            }
            
            // Update local preview
            localRenderer?.let { renderer ->
                try {
                    // Remove screen track from renderer first
                    screenVideoTrack?.let { track ->
                        try { 
                            track.removeSink(renderer)
                            Log.d(TAG, "Screen track removed from local renderer")
                        } catch (e: Exception) {
                            Log.w(TAG, "Error removing screen track from renderer: ${e.message}")
                        }
                    }
                    
                    // Add camera track back to renderer
                    localVideoTrack?.let { track ->
                        try {
                            track.addSink(renderer)
                            renderer.setMirror(true) // Mirror camera
                            renderer.visibility = android.view.View.VISIBLE
                            Log.d(TAG, "Camera track added back to local renderer")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add camera track to renderer: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating local renderer: ${e.message}")
                }
            }
            
            // Dispose screen resources after removing from renderer
            screenVideoSource?.dispose()
            screenVideoTrack?.dispose()
            screenVideoSource = null
            screenVideoTrack = null
            isScreenSharing = false
            
            // Trigger renegotiation to notify peer about track change
            Log.d(TAG, "Screen sharing stopped, triggering renegotiation")
            events.onRenegotiationNeeded()
            
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
        Log.d(TAG, "release() called - cleaning up WebRTC resources")
        try {
            // Release audio focus first
            releaseAudioFocus()
            
            // Remove sinks before disposing tracks to prevent crashes
            try {
                localVideoTrack?.let { track ->
                    if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                        localRenderer?.let { renderer ->
                            try { track.removeSink(renderer) } catch (e: Exception) {}
                        }
                    }
                }
                screenVideoTrack?.let { track ->
                    if (track.state() != org.webrtc.MediaStreamTrack.State.ENDED) {
                        localRenderer?.let { renderer ->
                            try { track.removeSink(renderer) } catch (e: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error removing local video sink: ${e.message}")
            }
            
            videoCapturer?.stopCapture()
            screenCapturer?.stopCapture()
        } catch (e: Exception) {
            Log.w(TAG, "Error during cleanup: ${e.message}")
        }
        
        try {
            videoCapturer?.dispose()
            screenCapturer?.dispose()
            videoSource?.dispose()
            screenVideoSource?.dispose()
            audioSource?.dispose()
            localVideoTrack?.dispose()
            screenVideoTrack?.dispose()
            localAudioTrack?.dispose()
            peerConnection?.dispose()
            // Stop MediaProjection if active
            try {
                mediaProjection?.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping MediaProjection: ${e.message}")
            }
            // Stop foreground service if running
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isScreenSharing) {
                ScreenCaptureService.stop(context)
            }
            // DON'T release renderers here - they are owned by Compose AndroidView
            // localRenderer?.release()
            // remoteRenderer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error disposing resources: ${e.message}")
        }
        
        videoCapturer = null
        screenCapturer = null
        mediaProjection = null
        videoSource = null
        screenVideoSource = null
        audioSource = null
        localVideoTrack = null
        localAudioTrack = null
        peerConnection = null
        localTracksAttached = false
        isScreenSharing = false
        Log.d(TAG, "release() completed")
    }
    
    /**
     * Detach and release renderers - call this when VideoCallScreen is disposed
     */
    fun releaseRenderers() {
        Log.d(TAG, "releaseRenderers() called")
        try {
            localRenderer?.let { renderer ->
                localVideoTrack?.let { track ->
                    try { track.removeSink(renderer) } catch (e: Exception) {}
                }
                screenVideoTrack?.let { track ->
                    try { track.removeSink(renderer) } catch (e: Exception) {}
                }
                try { renderer.release() } catch (e: Exception) {
                    Log.w(TAG, "Error releasing local renderer: ${e.message}")
                }
            }
            remoteRenderer?.let { renderer ->
                try { renderer.release() } catch (e: Exception) {
                    Log.w(TAG, "Error releasing remote renderer: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in releaseRenderers: ${e.message}")
        }
        localRenderer = null
        remoteRenderer = null
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
            videoSender = pc.addTrack(it)
            
            // Configure video encoding parameters for better quality
            videoSender?.let { sender ->
                val parameters = sender.parameters
                parameters.encodings.forEach { encoding ->
                    // Set max bitrate: 2 Mbps for good quality
                    encoding.maxBitrateBps = 2000000
                    // Set min bitrate: 300 Kbps to maintain quality on poor networks
                    encoding.minBitrateBps = 300000
                    // Prefer maintaining resolution over framerate
                    encoding.scaleResolutionDownBy = 1.0
                }
                // Set degradation preference to maintain resolution
                parameters.degradationPreference = org.webrtc.RtpParameters.DegradationPreference.MAINTAIN_RESOLUTION
                sender.parameters = parameters
                Log.d(TAG, "Video sender configured with bitrate range: 300kbps-2Mbps")
            }
            
            Log.d(TAG, "Local video track attached to peer connection, sender: $videoSender")
        }
        audioTrack?.let { 
            val audioSender = pc.addTrack(it)
            
            // Configure audio encoding parameters
            audioSender?.let { sender ->
                val parameters = sender.parameters
                parameters.encodings.forEach { encoding ->
                    // Set audio bitrate: 128 Kbps for high quality
                    encoding.maxBitrateBps = 128000
                    encoding.minBitrateBps = 32000
                }
                sender.parameters = parameters
                Log.d(TAG, "Audio sender configured with bitrate: 32kbps-128kbps")
            }
            
            Log.d(TAG, "Local audio track attached to peer connection - enabled: ${it.enabled()}, state: ${it.state()}")
        }
        
        localTracksAttached = true
        Log.d(TAG, "Local tracks attached - video: ${videoTrack != null}, audio: ${audioTrack != null}")
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        val encoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            true,  // enableIntelVp8Encoder
            true   // enableH264HighProfile
        )
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
        
        // Audio options for better quality
        val audioOptions = org.webrtc.audio.JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .setAudioRecordErrorCallback(object : org.webrtc.audio.JavaAudioDeviceModule.AudioRecordErrorCallback {
                override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                    Log.e(TAG, "Audio record init error: $errorMessage")
                }
                override fun onWebRtcAudioRecordStartError(
                    errorCode: org.webrtc.audio.JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                    errorMessage: String?
                ) {
                    Log.e(TAG, "Audio record start error: $errorMessage")
                }
                override fun onWebRtcAudioRecordError(errorMessage: String?) {
                    Log.e(TAG, "Audio record error: $errorMessage")
                }
            })
            .setAudioTrackErrorCallback(object : org.webrtc.audio.JavaAudioDeviceModule.AudioTrackErrorCallback {
                override fun onWebRtcAudioTrackInitError(errorMessage: String?) {
                    Log.e(TAG, "Audio track init error: $errorMessage")
                }
                override fun onWebRtcAudioTrackStartError(
                    errorCode: org.webrtc.audio.JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                    errorMessage: String?
                ) {
                    Log.e(TAG, "Audio track start error: $errorMessage")
                }
                override fun onWebRtcAudioTrackError(errorMessage: String?) {
                    Log.e(TAG, "Audio track error: $errorMessage")
                }
            })
            .createAudioDeviceModule()
        
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setAudioDeviceModule(audioOptions)
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
