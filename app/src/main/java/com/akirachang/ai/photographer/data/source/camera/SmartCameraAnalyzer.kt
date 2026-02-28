/**
 * ClassName: SmartCameraAnalyzer
 * Description: Custom ImageAnalysis.Analyzer that integrates ML Kit detection with lighting analysis
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - ML Kit face and object detection
 * - Lighting analysis with histogram calculation
 * - Exposure detection (backlight, overexposure, low light)
 * - Adaptive throttle via EWMA (v2.12.0-03): auto-tunes interval per device (100–500ms)
 * - Proper resource management
 * - v2.x.x Phase 4: Optical flow point tracking integration
 *
 * Architecture:
 * - Replaces MlKitAnalyzer to gain ImageProxy access
 * - Provides both DetectionResult and LightingState
 * - Uses ViewModel callbacks for state updates
 */
package com.akirachang.ai.photographer.data.source.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.akirachang.ai.photographer.data.opencv.OpticalFlowTracker
import com.akirachang.ai.photographer.data.opencv.TrackingResult
import com.akirachang.ai.photographer.util.CoordinateMapper
import org.opencv.core.Mat
import org.opencv.core.Point
import android.graphics.Bitmap
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extension function to convert Google Tasks to Kotlin Coroutines
 */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

/**
 * Custom analyzer that performs both ML Kit detection and lighting analysis
 *
 * @param faceDetector ML Kit face detector
 * @param objectDetector ML Kit object detector
 * @param analyzeLightingUseCase Lighting analysis use case
 * @param onDetectionResult Callback for detection results
 * @param onLightingResult Callback for lighting analysis results
 * @param minObjectConfidence Minimum confidence threshold for objects (default 0.5)
 */
class SmartCameraAnalyzer(
    private val faceDetector: FaceDetector,
    private val objectDetector: ObjectDetector,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val onDetectionResult: (DetectionResult) -> Unit,
    private val onLightingResult: (LightingState) -> Unit,
    private val minObjectConfidence: Float = 0.5f
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "SmartCameraAnalyzer"

        // v2.12.0-03: Adaptive ML Kit throttle — replaces per-device hardcoded values
        private const val DEFAULT_THROTTLE_MS    = 200L   // initial interval
        private const val MIN_THROTTLE_MS        = 100L   // fastest allowed (10 FPS)
        private const val MAX_THROTTLE_MS        = 500L   // slowest allowed (2 FPS)
        private const val WARMUP_FRAMES          = 3      // frames skipped from EWMA
        private const val EMA_ALPHA              = 0.2    // EWMA smoothing factor
        private const val SAFETY_FACTOR          = 1.25   // headroom multiplier
        private const val DEADBAND_RATIO         = 0.10   // ±10% change ignored
        private const val MAX_EXPAND_PER_FRAME   = 50L    // ms max increase per frame
        private const val MAX_CONTRACT_PER_FRAME = 20L    // ms max decrease per frame
        private const val PANIC_RATIO            = 1.5    // measured > interval×1.5 → panic
        private const val PANIC_SCALE            = 1.2    // panic: interval × 1.2
        private const val LOG_EVERY_N_FRAMES     = 30     // logcat throttle interval
    }

    private val analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val isProcessing = AtomicBoolean(false)
    private var lastAnalysisTime = 0L

    // v2.12.0-03: Adaptive throttle state
    private val adaptiveThrottleMs = AtomicLong(DEFAULT_THROTTLE_MS)
    private var emaMs = 0.0
    private var warmupCount = 0
    private var processedFrames = 0L

    // Cached detection results for lighting analysis
    private var cachedDetectionResult: DetectionResult? = null

    // v2.x.x Phase 4: Optical flow tracking
    private var opticalFlowTracker: OpticalFlowTracker? = null
    private var onOpticalFlowResult: ((TrackingResult) -> Unit)? = null
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    // v2.11.3 Fix H: Dedicated analyze-pipeline dimensions (always = ImageProxy size, e.g. 1920×1920 for 1:1 mode).
    // Separate from imageWidth/imageHeight which may be set first by trackFromBitmap() (bitmap ~720px).
    // @Volatile for cross-thread visibility between analyze() (Default dispatcher) and trackFromBitmap().
    @Volatile private var analyzeImageWidth: Int = 0
    @Volatile private var analyzeImageHeight: Int = 0

    // v2.x.x Phase 6: Pending tracking start (applied when first frame arrives)
    private var pendingTrackX: Float = 0f
    private var pendingTrackY: Float = 0f
    private var hasPendingTrack: Boolean = false
    // v2.11.2 Fix G: true when pending coords are already in image space (set by startOpticalFlowTrackingImageCoords)
    private var isPendingInImageCoords: Boolean = false

    /**
     * Set optical flow tracking result callback
     * v2.x.x Phase 4: Optical flow point tracking
     *
     * @param callback Callback invoked when tracking result is available
     */
    fun setOpticalFlowCallback(callback: (TrackingResult) -> Unit) {
        onOpticalFlowResult = callback
    }

    /**
     * Set preview size for coordinate mapping
     * v2.x.x Phase 4: Required for PreviewView ↔ ImageAnalysis coordinate conversion
     *
     * @param width Preview width in pixels
     * @param height Preview height in pixels
     */
    fun setPreviewSize(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
        Log.d(TAG, "Preview size set: ${width}x${height}")
    }

    /**
     * Start optical flow tracking at a specific point
     * v2.x.x Phase 4: Tap-to-track point feature
     *
     * @param x X coordinate in PreviewView (0.0 to previewWidth)
     * @param y Y coordinate in PreviewView (0.0 to previewHeight)
     */
    fun startOpticalFlowTracking(x: Float, y: Float) {
        if (previewWidth == 0 || previewHeight == 0) {
            Log.w(TAG, "Cannot start tracking: preview size not set")
            return
        }

        if (imageWidth == 0 || imageHeight == 0) {
            // v2.x.x Phase 6: Store as pending - applied when first frame sets image size
            pendingTrackX = x
            pendingTrackY = y
            hasPendingTrack = true
            Log.d(TAG, "Tracking start pending: preview ($x, $y), waiting for first frame")
            return
        }

        // Convert PreviewView coordinates to ImageAnalysis coordinates
        val imagePoint = CoordinateMapper.previewToImage(
            x, y,
            previewWidth, previewHeight,
            imageWidth, imageHeight
        )

        // Create tracker if needed
        if (opticalFlowTracker == null) {
            opticalFlowTracker = OpticalFlowTracker()
        }

        // Start tracking
        opticalFlowTracker?.startTracking(imagePoint, imageWidth, imageHeight)
        Log.i(TAG, "Optical flow tracking started at preview ($x, $y) -> image (${imagePoint.x}, ${imagePoint.y})")
    }

    /**
     * [v2.11.2 Fix G] Start optical flow tracking using image/analysis coordinates directly.
     * Bypasses CoordinateMapper (CENTER_CROP) — caller has already converted via CoordinateTransformer (FIT_CENTER).
     *
     * @param imageX X coordinate in image analysis space
     * @param imageY Y coordinate in image analysis space
     */
    fun startOpticalFlowTrackingImageCoords(imageX: Float, imageY: Float) {
        // v2.11.3 Fix H: image coords are in the CoordinateTransformer's image space.
        // On real device (Pixel 9a), analyze() (CameraX ImageAnalysis) does NOT run — so we cannot
        // depend on analyzeImageWidth. Instead:
        // - If imageWidth > 0 (set by a previous trackFromBitmap call), use it directly — it equals
        //   the bitmap width which matches the CoordinateTransformer's image space.
        // - If imageWidth == 0 (trackFromBitmap not yet called), store as pending; trackFromBitmap()
        //   will apply it with bitmap.width/height (the correct dimensions).
        Log.i(TAG, "[Fix H] startOpticalFlowTrackingImageCoords: imageXY=($imageX,$imageY), imageWidth=$imageWidth")
        if (imageWidth == 0 || imageHeight == 0) {
            pendingTrackX = imageX
            pendingTrackY = imageY
            hasPendingTrack = true
            isPendingInImageCoords = true
            Log.i(TAG, "[Fix H] OF tracking start pending (image coords): ($imageX, $imageY) — imageWidth not yet set")
            return
        }
        if (opticalFlowTracker == null) {
            opticalFlowTracker = OpticalFlowTracker()
        }
        opticalFlowTracker?.startTracking(Point(imageX.toDouble(), imageY.toDouble()), imageWidth, imageHeight)
        Log.i(TAG, "[Fix H] OF tracking started at image ($imageX, $imageY), imageSize=${imageWidth}x${imageHeight}")
    }

    /**
     * Stop optical flow tracking
     * v2.x.x Phase 4: Stop tracking and release resources
     */
    fun stopOpticalFlowTracking() {
        opticalFlowTracker?.stopTracking()
        Log.i(TAG, "Optical flow tracking stopped")
    }

    /**
     * Check if optical flow tracking is active
     * v2.x.x Phase 4: Query tracking state
     *
     * @return True if tracking is active, false otherwise
     */
    fun isOpticalFlowTracking(): Boolean {
        return opticalFlowTracker?.isTracking() ?: false
    }

    /**
     * Track optical flow point from a Bitmap frame
     * v2.x.x Phase 6: Bitmap pipeline fallback for emulator (CameraX ImageAnalysis not available)
     *
     * Called from CameraViewModel's bitmap analysis loop when optical flow tracking is active.
     */
    fun trackFromBitmap(bitmap: Bitmap) {
        // Set image dimensions from bitmap if not yet known
        if (imageWidth == 0 || imageHeight == 0) {
            imageWidth = bitmap.width
            imageHeight = bitmap.height
            Log.d(TAG, "Image size set from bitmap: ${imageWidth}x${imageHeight}")
        }

        // Apply pending tracking start if exists
        if (hasPendingTrack) {
            if (isPendingInImageCoords) {
                // v2.11.3 Fix H: Pending coords are in image space; bitmap IS the image space
                // (Pixel 9a bitmap=1920x1920 matches the image-space coords directly).
                // Use bitmap.width/height as tracker dimensions — no dependency on analyzeImageWidth.
                hasPendingTrack = false
                isPendingInImageCoords = false
                val imagePoint = Point(pendingTrackX.toDouble(), pendingTrackY.toDouble())
                if (opticalFlowTracker == null) {
                    opticalFlowTracker = OpticalFlowTracker()
                }
                opticalFlowTracker?.startTracking(imagePoint, bitmap.width, bitmap.height)
                Log.i(TAG, "[Fix H] Applied pending image-coord track: (${imagePoint.x}, ${imagePoint.y}), bitmapSize=${bitmap.width}x${bitmap.height}")
            } else {
                // Legacy preview-coord path: use bitmap dimensions (imageWidth set by this path)
                hasPendingTrack = false
                val imagePoint = CoordinateMapper.previewToImage(
                    pendingTrackX, pendingTrackY,
                    previewWidth, previewHeight,
                    imageWidth, imageHeight
                )
                if (opticalFlowTracker == null) {
                    opticalFlowTracker = OpticalFlowTracker()
                }
                opticalFlowTracker?.startTracking(imagePoint, imageWidth, imageHeight)
                Log.i(TAG, "Applied pending preview-coord track: image (${imagePoint.x}, ${imagePoint.y})")
            }
        }

        // Skip if not actively tracking
        if (opticalFlowTracker?.isTracking() != true) return

        // Convert Bitmap to grayscale Mat (uses existing extension function)
        val grayMat = bitmap.toGrayMat()

        try {
            val trackingResult = opticalFlowTracker?.track(grayMat) ?: return

            // [Fix G] Pass image coords directly — CameraViewModel converts via CoordinateTransformer (FIT_CENTER)
            // (Removed CoordinateMapper.imageToPreview — CENTER_CROP scale caused 2.24x drift)
            onOpticalFlowResult?.invoke(trackingResult)
        } finally {
            grayMat.release()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // v2.12.0-03: Adaptive throttle gate (replaces hardcoded THROTTLE_INTERVAL_MS)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < adaptiveThrottleMs.get()) {
            imageProxy.close()
            return
        }

        // Skip if already processing
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        lastAnalysisTime = currentTime

        analysisScope.launch {
            val startNs = SystemClock.elapsedRealtimeNanos() // v2.12.0-03: measure ML Kit latency
            try {
                // v2.x.x Phase 4: Store image dimensions for coordinate mapping
                if (imageWidth == 0 || imageHeight == 0) {
                    imageWidth = imageProxy.width
                    imageHeight = imageProxy.height
                    Log.d(TAG, "Image size captured: ${imageWidth}x${imageHeight}")
                }

                // v2.11.3 Fix H: Always update analyze-pipeline dimensions unconditionally.
                // Must NOT be inside the if-guard above: trackFromBitmap() sets imageWidth=720 first
                // (28 FPS vs 2 FPS), so the if-guard is never true when analyze() runs.
                // analyzeImageWidth always reflects the ImageProxy resolution (e.g. 1920 for 1:1 mode).
                analyzeImageWidth = imageProxy.width
                analyzeImageHeight = imageProxy.height

                // Create InputImage for ML Kit
                val inputImage = InputImage.fromMediaImage(
                    imageProxy.image!!,
                    imageProxy.imageInfo.rotationDegrees
                )

                // Parallel ML Kit detection with proper Task handling
                val faces = try {
                    faceDetector.process(inputImage).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Face detection failed", e)
                    emptyList()
                }

                val objects = try {
                    objectDetector.process(inputImage).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Object detection failed", e)
                    emptyList()
                }

                // Filter low confidence objects
                val filteredObjects = objects.filter { obj ->
                    val confidence = obj.labels.firstOrNull()?.confidence ?: 0f
                    confidence >= minObjectConfidence
                }

                // Create detection result with VIEW_REFERENCED coordinates
                // Note: ML Kit returns coordinates in image space, but we're using
                // CameraX's coordinate system which handles the transformation
                val detectionResult = DetectionResult(
                    faces = faces,
                    objects = filteredObjects,
                    imageWidth = imageProxy.width,
                    imageHeight = imageProxy.height,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                    timestamp = currentTime
                )

                // Cache for lighting analysis
                cachedDetectionResult = detectionResult

                // Notify detection result on main thread
                onDetectionResult(detectionResult)

                // Perform lighting analysis
                // Use analyzeWithSubject if faces detected, otherwise basic analysis
                val lightingState = if (faces.isNotEmpty()) {
                    analyzeLightingUseCase.analyzeWithSubject(imageProxy, detectionResult)
                } else {
                    analyzeLightingUseCase(imageProxy)
                }

                // Notify lighting result on main thread
                onLightingResult(lightingState)

                Log.d(
                    TAG,
                    "Analysis complete: ${faces.size} faces, ${filteredObjects.size} objects, " +
                            "lighting: ${lightingState.javaClass.simpleName}"
                )

                // v2.x.x Phase 4: Optical flow tracking (if active)
                if (isOpticalFlowTracking()) {
                    try {
                        // Convert ImageProxy to grayscale Mat
                        val grayMat = imageProxy.toGrayMat()

                        try {
                            // Track point
                            val trackingResult = opticalFlowTracker?.track(grayMat)

                            if (trackingResult != null) {
                                // [Fix G] Pass image coords directly — CameraViewModel converts via CoordinateTransformer (FIT_CENTER)
                                // (Removed CoordinateMapper.imageToPreview — CENTER_CROP scale caused 2.24x drift)
                                onOpticalFlowResult?.invoke(trackingResult)
                            }
                        } finally {
                            // Release Mat to prevent memory leak
                            grayMat.release()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Optical flow tracking failed", e)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Analysis error", e)
            } finally {
                val elapsedNs = SystemClock.elapsedRealtimeNanos() - startNs // v2.12.0-03
                updateAdaptiveThrottle(elapsedNs)
                imageProxy.close()
                isProcessing.set(false)
            }
        }
    }

    /**
     * v2.12.0-03: Update adaptive throttle interval based on measured ML Kit latency.
     *
     * Algorithm:
     *   1. Warmup: skip first WARMUP_FRAMES to exclude JIT/cold-start spikes
     *   2. EWMA: smooth measured latency with α=0.2
     *   3. Panic backoff: if measured > interval×PANIC_RATIO → immediate scale-up
     *   4. Target = EWMA × SAFETY_FACTOR
     *   5. Deadband: skip update if |target - current| < current × DEADBAND_RATIO
     *   6. Step-rate limit: cap change to ±MAX_EXPAND/MAX_CONTRACT per frame
     *   7. Clamp: [MIN_THROTTLE_MS, MAX_THROTTLE_MS]
     */
    private fun updateAdaptiveThrottle(measuredNs: Long) {
        val measuredMs = measuredNs / 1_000_000.0

        // 1. Warmup — skip noisy first frames
        if (warmupCount < WARMUP_FRAMES) {
            warmupCount++
            return
        }

        // 2. EWMA smoothing
        emaMs = if (emaMs == 0.0) measuredMs
                else EMA_ALPHA * measuredMs + (1.0 - EMA_ALPHA) * emaMs

        val currentMs = adaptiveThrottleMs.get().toDouble()
        processedFrames++

        // 3. Panic backoff: measured far exceeds current interval
        if (measuredMs > currentMs * PANIC_RATIO) {
            val panicMs = (currentMs * PANIC_SCALE).toLong()
                .coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
            adaptiveThrottleMs.set(panicMs)
            if (processedFrames % LOG_EVERY_N_FRAMES == 0L) {
                Log.i(TAG, "[v2.12.0-03] PANIC throttle=${panicMs}ms measured=${measuredMs.toInt()}ms EMA=${emaMs.toInt()}ms")
            }
            return
        }

        // 4. Target with safety headroom
        val targetMs = (emaMs * SAFETY_FACTOR).toLong()
            .coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)

        // 5. Deadband — ignore tiny fluctuations
        if (Math.abs(targetMs - currentMs) < currentMs * DEADBAND_RATIO) return

        // 6. Step-rate limit
        val newMs = when {
            targetMs > currentMs -> (currentMs + MAX_EXPAND_PER_FRAME).toLong()
                .coerceAtMost(targetMs).coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
            else                 -> (currentMs - MAX_CONTRACT_PER_FRAME).toLong()
                .coerceAtLeast(targetMs).coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
        }

        adaptiveThrottleMs.set(newMs)

        if (processedFrames % LOG_EVERY_N_FRAMES == 0L) {
            Log.i(TAG, "[v2.12.0-03] throttle=${newMs}ms EMA=${emaMs.toInt()}ms measured=${measuredMs.toInt()}ms frames=$processedFrames")
        }
    }

    /**
     * Clean up resources
     * Call this when stopping analysis
     */
    fun cleanup() {
        analysisScope.cancel()

        // v2.x.x Phase 4: Release optical flow tracker
        stopOpticalFlowTracking()
        opticalFlowTracker = null
        onOpticalFlowResult = null

        Log.d(TAG, "Analyzer cleaned up")
    }
}
