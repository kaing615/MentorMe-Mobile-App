package com.mentorme.app.core.webrtc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.VideoFrame
import org.webrtc.VideoProcessor
import org.webrtc.VideoSink
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * Video processor that applies background blur using ML Kit Selfie Segmentation
 */
class BackgroundBlurProcessor(
    private val context: Context
) : VideoProcessor {

    companion object {
        private const val TAG = "BackgroundBlurProcessor"
        private const val BLUR_RADIUS = 25f // 1-25, higher = more blur
    }

    private var segmenter: Segmenter? = null
    private var sink: VideoSink? = null
    private var isEnabled = false
    private var processingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var renderScript: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null
    
    // Reusable bitmaps to reduce allocation
    private var inputBitmap: Bitmap? = null
    private var outputBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var blurredBitmap: Bitmap? = null

    init {
        initializeSegmenter()
    }

    private fun initializeSegmenter() {
        try {
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
                .enableRawSizeMask()
                .build()
            
            segmenter = Segmentation.getClient(options)
            
            // Initialize RenderScript for blur effect
            renderScript = RenderScript.create(context)
            blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
            blurScript?.setRadius(BLUR_RADIUS)
            
            Log.d(TAG, "Segmenter and blur initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize segmenter: ${e.message}", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        Log.d(TAG, "Background blur enabled: $enabled")
    }

    fun isEnabled(): Boolean = isEnabled

    override fun setSink(videoSink: VideoSink?) {
        sink = videoSink
    }

    override fun onCapturerStarted(success: Boolean) {
        Log.d(TAG, "Capturer started: $success")
    }

    override fun onCapturerStopped() {
        Log.d(TAG, "Capturer stopped")
        processingJob?.cancel()
    }

    override fun onFrameCaptured(frame: VideoFrame) {
        if (!isEnabled || segmenter == null) {
            // Pass through without processing
            sink?.onFrame(frame)
            return
        }

        // Process frame asynchronously
        frame.retain()
        scope.launch {
            try {
                val processedFrame = processFrame(frame)
                sink?.onFrame(processedFrame ?: frame)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame: ${e.message}")
                sink?.onFrame(frame)
            } finally {
                frame.release()
            }
        }
    }

    private suspend fun processFrame(frame: VideoFrame): VideoFrame? {
        val buffer = frame.buffer
        val width = buffer.width
        val height = buffer.height
        
        // Convert VideoFrame to Bitmap
        val bitmap = videoFrameToBitmap(frame) ?: return null
        
        // Run segmentation
        val mask = runSegmentation(bitmap) ?: return null
        
        // Apply blur to background
        val resultBitmap = applyBackgroundBlur(bitmap, mask)
        
        // Convert back to VideoFrame
        return bitmapToVideoFrame(resultBitmap, frame.timestampNs, frame.rotation)
    }

    private fun videoFrameToBitmap(frame: VideoFrame): Bitmap? {
        try {
            val buffer = frame.buffer
            val i420Buffer = buffer.toI420() ?: return null
            
            val width = i420Buffer.width
            val height = i420Buffer.height
            
            // Ensure bitmap is correct size
            if (inputBitmap?.width != width || inputBitmap?.height != height) {
                inputBitmap?.recycle()
                inputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }
            
            val bitmap = inputBitmap!!
            
            // Convert I420 to ARGB
            val yBuffer = i420Buffer.dataY
            val uBuffer = i420Buffer.dataU
            val vBuffer = i420Buffer.dataV
            
            val yStride = i420Buffer.strideY
            val uStride = i420Buffer.strideU
            val vStride = i420Buffer.strideV
            
            val pixels = IntArray(width * height)
            
            for (row in 0 until height) {
                for (col in 0 until width) {
                    val yIndex = row * yStride + col
                    val uvRow = row / 2
                    val uvCol = col / 2
                    val uIndex = uvRow * uStride + uvCol
                    val vIndex = uvRow * vStride + uvCol
                    
                    val y = (yBuffer.get(yIndex).toInt() and 0xFF) - 16
                    val u = (uBuffer.get(uIndex).toInt() and 0xFF) - 128
                    val v = (vBuffer.get(vIndex).toInt() and 0xFF) - 128
                    
                    // YUV to RGB conversion
                    val r = (1.164 * y + 1.596 * v).toInt().coerceIn(0, 255)
                    val g = (1.164 * y - 0.813 * v - 0.391 * u).toInt().coerceIn(0, 255)
                    val b = (1.164 * y + 2.018 * u).toInt().coerceIn(0, 255)
                    
                    pixels[row * width + col] = Color.argb(255, r, g, b)
                }
            }
            
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            i420Buffer.release()
            
            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error converting frame to bitmap: ${e.message}", e)
            return null
        }
    }

    private suspend fun runSegmentation(bitmap: Bitmap): SegmentationMask? {
        return suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            segmenter?.process(inputImage)
                ?.addOnSuccessListener { mask ->
                    continuation.resume(mask)
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "Segmentation failed: ${e.message}", e)
                    continuation.resume(null)
                }
        }
    }

    private fun applyBackgroundBlur(original: Bitmap, mask: SegmentationMask): Bitmap {
        val width = original.width
        val height = original.height
        
        // Create blurred version
        if (blurredBitmap?.width != width || blurredBitmap?.height != height) {
            blurredBitmap?.recycle()
            blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        val blurred = blurredBitmap!!
        
        // Apply blur using RenderScript
        try {
            val inputAllocation = Allocation.createFromBitmap(renderScript, original)
            val outputAllocation = Allocation.createFromBitmap(renderScript, blurred)
            blurScript?.setInput(inputAllocation)
            blurScript?.forEach(outputAllocation)
            outputAllocation.copyTo(blurred)
            inputAllocation.destroy()
            outputAllocation.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Blur failed: ${e.message}", e)
            return original
        }
        
        // Create output bitmap
        if (outputBitmap?.width != width || outputBitmap?.height != height) {
            outputBitmap?.recycle()
            outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        val output = outputBitmap!!
        
        // Composite: blurred background + sharp foreground
        val canvas = Canvas(output)
        
        // Draw blurred background
        canvas.drawBitmap(blurred, 0f, 0f, null)
        
        // Extract mask as bitmap
        val maskWidth = mask.width
        val maskHeight = mask.height
        val maskBuffer = mask.buffer
        
        if (maskBitmap?.width != width || maskBitmap?.height != height) {
            maskBitmap?.recycle()
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        val maskBmp = maskBitmap!!
        
        // Scale mask to match image size
        val scaleX = width.toFloat() / maskWidth
        val scaleY = height.toFloat() / maskHeight
        
        val maskPixels = IntArray(width * height)
        maskBuffer.rewind()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val maskX = (x / scaleX).toInt().coerceIn(0, maskWidth - 1)
                val maskY = (y / scaleY).toInt().coerceIn(0, maskHeight - 1)
                val maskIndex = maskY * maskWidth + maskX
                
                // Get confidence value (0-1)
                val confidence = if (maskIndex < maskBuffer.capacity()) {
                    maskBuffer.getFloat(maskIndex * 4)
                } else {
                    0f
                }
                
                // Higher confidence = more of the person (foreground)
                val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                maskPixels[y * width + x] = Color.argb(alpha, 255, 255, 255)
            }
        }
        
        maskBmp.setPixels(maskPixels, 0, width, 0, 0, width, height)
        
        // Draw original image with mask
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
        }
        
        // Create masked original
        val maskedCanvas = Canvas(output)
        val maskPaint = Paint()
        maskedCanvas.drawBitmap(maskBmp, 0f, 0f, null)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        maskedCanvas.drawBitmap(original, 0f, 0f, maskPaint)
        
        return output
    }

    private fun bitmapToVideoFrame(bitmap: Bitmap, timestampNs: Long, rotation: Int): VideoFrame? {
        // For now, return null and let the caller use original frame
        // Full implementation would convert bitmap back to I420 buffer
        // This is complex and may need native code for performance
        return null
    }

    fun release() {
        processingJob?.cancel()
        segmenter?.close()
        blurScript?.destroy()
        renderScript?.destroy()
        inputBitmap?.recycle()
        outputBitmap?.recycle()
        maskBitmap?.recycle()
        blurredBitmap?.recycle()
        Log.d(TAG, "BackgroundBlurProcessor released")
    }
}
