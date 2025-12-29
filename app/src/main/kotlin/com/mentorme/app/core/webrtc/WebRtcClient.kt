package com.mentorme.app.core.webrtc

import android.content.Context
import android.media.AudioManager
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
    private val peerConnectionFactory: PeerConnectionFactory by lazy { createPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
    private var localRenderer: SurfaceViewRenderer? = null
    private var remoteRenderer: SurfaceViewRenderer? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

    private var isVideoEnabled = true
    private var isAudioEnabled = true

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
        localRenderer = renderer
        renderer.init(eglBase.eglBaseContext, null)
        renderer.setEnableHardwareScaler(true)
        renderer.setMirror(true)
        localVideoTrack?.addSink(renderer)
    }

    fun attachRemoteRenderer(renderer: SurfaceViewRenderer) {
        remoteRenderer = renderer
        renderer.init(eglBase.eglBaseContext, null)
        renderer.setEnableHardwareScaler(true)
        renderer.setMirror(false)
    }

    fun ensurePeerConnection() {
        if (peerConnection != null) return
        peerConnection = createPeerConnection()
    }

    fun startLocalMedia() {
        if (localVideoTrack != null || localAudioTrack != null) return

        val videoCapturer = createCameraCapturer() ?: return
        val surfaceHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        val videoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer.initialize(surfaceHelper, context, videoSource.capturerObserver)
        videoCapturer.startCapture(720, 1280, 30)

        val videoTrack = peerConnectionFactory.createVideoTrack("VIDEO", videoSource)
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("AUDIO", audioSource)

        this.videoCapturer = videoCapturer
        this.videoSource = videoSource
        this.localVideoTrack = videoTrack
        this.audioSource = audioSource
        this.localAudioTrack = audioTrack

        localRenderer?.let { videoTrack.addSink(it) }
        localVideoTrack?.setEnabled(isVideoEnabled)
        localAudioTrack?.setEnabled(isAudioEnabled)

        peerConnection?.addTrack(videoTrack)
        peerConnection?.addTrack(audioTrack)
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
        localVideoTrack?.setEnabled(enabled)
    }

    fun setAudioEnabled(enabled: Boolean) {
        isAudioEnabled = enabled
        localAudioTrack?.setEnabled(enabled)
    }

    fun switchCamera() {
        val capturer = videoCapturer ?: return
        capturer.switchCamera(null)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audioManager.isSpeakerphoneOn = enabled
    }

    fun getStats(callback: (RtcStats?) -> Unit) {
        val pc = peerConnection ?: return
        pc.getStats { report ->
            callback(parseStats(report))
        }
    }

    fun release() {
        try {
            videoCapturer?.stopCapture()
        } catch (_: Exception) {
        }
        videoCapturer?.dispose()
        videoSource?.dispose()
        audioSource?.dispose()
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        peerConnection?.dispose()
        localRenderer?.release()
        remoteRenderer?.release()
        videoCapturer = null
        videoSource = null
        audioSource = null
        localVideoTrack = null
        localAudioTrack = null
        peerConnection = null
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
                if (track is VideoTrack) {
                    remoteRenderer?.let { track.addSink(it) }
                    events.onRemoteTrack(track)
                }
            }
        })
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
