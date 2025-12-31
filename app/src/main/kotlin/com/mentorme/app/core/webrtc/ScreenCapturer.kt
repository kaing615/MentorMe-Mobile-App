package com.mentorme.app.core.webrtc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer

/**
 * Screen capturer for WebRTC screen sharing
 * Now accepts MediaProjection directly instead of Intent to avoid token reuse issues
 */
class ScreenCapturer(
    private val context: Context,
    private val mediaProjection: MediaProjection
) : VideoCapturer {

    companion object {
        private const val TAG = "ScreenCapturer"
        const val SCREEN_CAPTURE_REQUEST_CODE = 1001
        
        fun createScreenCaptureIntent(activity: Activity): Intent {
            val mediaProjectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            return mediaProjectionManager.createScreenCaptureIntent()
        }
    }

    private var virtualDisplay: VirtualDisplay? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var capturerObserver: CapturerObserver? = null
    private var width: Int = 720
    private var height: Int = 1280
    private var fps: Int = 15
    private var isCapturing = false
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    override fun initialize(
        surfaceTextureHelper: SurfaceTextureHelper,
        context: Context,
        capturerObserver: CapturerObserver
    ) {
        this.surfaceTextureHelper = surfaceTextureHelper
        this.capturerObserver = capturerObserver
        
        // Create handler thread for callbacks
        handlerThread = HandlerThread("ScreenCapturerThread").apply { start() }
        handler = Handler(handlerThread!!.looper)
        
        Log.d(TAG, "ScreenCapturer initialized")
    }

    override fun startCapture(width: Int, height: Int, framerate: Int) {
        Log.d(TAG, "startCapture: ${width}x${height}@${framerate}fps")
        this.width = width
        this.height = height
        this.fps = framerate

        try {
            // Register callback for MediaProjection
            val callback = object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped")
                    isCapturing = false
                }
            }
            mediaProjection.registerCallback(callback, handler)

            val metrics = context.resources.displayMetrics
            val screenDensity = metrics.densityDpi

            surfaceTextureHelper?.let { helper ->
                // Start listening before creating virtual display
                var frameCount = 0
                helper.startListening { videoFrame ->
                    if (isCapturing) {
                        frameCount++
                        if (frameCount <= 5 || frameCount % 100 == 0) {
                            Log.d(TAG, "Frame captured #$frameCount: ${videoFrame.buffer.width}x${videoFrame.buffer.height}")
                        }
                        capturerObserver?.onFrameCaptured(videoFrame)
                    }
                }
                
                virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    width,
                    height,
                    screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    Surface(helper.surfaceTexture),
                    object : VirtualDisplay.Callback() {
                        override fun onPaused() {
                            Log.d(TAG, "VirtualDisplay paused")
                        }
                        override fun onResumed() {
                            Log.d(TAG, "VirtualDisplay resumed")
                        }
                        override fun onStopped() {
                            Log.d(TAG, "VirtualDisplay stopped")
                        }
                    },
                    handler
                )
                
                isCapturing = true
                Log.d(TAG, "Screen capture started successfully - display: $virtualDisplay")
            } ?: run {
                Log.e(TAG, "SurfaceTextureHelper is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start screen capture: ${e.message}", e)
        }
    }

    override fun stopCapture() {
        Log.d(TAG, "stopCapture")
        isCapturing = false
        
        try {
            surfaceTextureHelper?.stopListening()
            virtualDisplay?.release()
            virtualDisplay = null
            // Don't stop mediaProjection here - it's managed by WebRtcClient
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping capture: ${e.message}", e)
        }
    }

    override fun changeCaptureFormat(width: Int, height: Int, framerate: Int) {
        Log.d(TAG, "changeCaptureFormat: ${width}x${height}@${framerate}fps")
        if (isCapturing) {
            stopCapture()
            startCapture(width, height, framerate)
        }
    }

    override fun dispose() {
        Log.d(TAG, "dispose")
        stopCapture()
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null
        handlerThread?.quitSafely()
        handlerThread = null
        handler = null
    }

    override fun isScreencast(): Boolean = true
}
