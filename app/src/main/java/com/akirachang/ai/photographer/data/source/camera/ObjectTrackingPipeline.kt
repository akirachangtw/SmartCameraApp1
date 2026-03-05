package com.akirachang.ai.photographer.data.source.camera

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.min

// ── Data Types ──────────────────────────────────────────────────────────────

/**
 * Tracked object with screen-space bounding box and focus coordinates.
 *
 * @param trackingId  ML Kit cross-frame stable ID (STREAM_MODE required)
 * @param screenBox   Bounding box mapped to PreviewView screen coordinates
 * @param imageBox    Original bbox in image-analysis coordinate space
 * @param centerX     Screen-space center X for metering point
 * @param centerY     Screen-space center Y for metering point
 * @param label       ML Kit classification label (nullable)
 * @param confidence  Classification confidence [0, 1]
 * @param lastSeenMs  Timestamp of last ML Kit detection (for TTL grace period)
 */
data class TrackedTarget(
    val trackingId: Int?,
    val screenBox: RectF,
    val imageBox: Rect,
    val centerX: Float,
    val centerY: Float,
    val label: String? = null,
    val confidence: Float = 0f,
    val lastSeenMs: Long = System.currentTimeMillis()
)

// ── Token Bucket (L2 §2.2 / Focus_V1 §5.2) ────────────────────────────────
//
// Gates ML Kit inference to a target FPS (default 15) to prevent CPU throttle.
// CameraX delivers frames at sensor rate (~30 FPS); this drops excess frames
// without building a queue.

class TokenBucket(
    private val maxCapacity: Int = 1,
    private val refillRatePerSecond: Double = 15.0
) {
    private var tokens = maxCapacity.toDouble()
    private var lastRefillTimestamp = System.nanoTime()
    private val lock = Any()

    /** @return true → process this frame; false → drop it. */
    fun tryConsume(): Boolean = synchronized(lock) {
        refill()
        if (tokens >= 1.0) { tokens -= 1.0; true } else false
    }

    private fun refill() {
        val now = System.nanoTime()
        val elapsed = (now - lastRefillTimestamp) / 1_000_000_000.0
        val add = elapsed * refillRatePerSecond
        if (add > 0.001) {
            tokens = min(maxCapacity.toDouble(), tokens + add)
            lastRefillTimestamp = now
        }
    }
}

// ── Target State Store (L2 §3.3 — TTL Grace Period) ────────────────────────
//
// Maintains the "primary target" across frames.  When ML Kit briefly loses a
// target (partial occlusion, fast motion), the store keeps the old bbox for up
// to TTL_MS before declaring the target lost.

class TargetStateStore(
    private val ttlMs: Long = 500L
) {
    private val _primary = MutableStateFlow<TrackedTarget?>(null)
    val primary: StateFlow<TrackedTarget?> = _primary.asStateFlow()

    /**
     * Feed a new detection frame into the store.
     *
     * @param candidates All objects detected in this frame, already mapped to
     *                   screen coordinates.
     * @param nowMs      Current timestamp (injectable for testing).
     */
    fun update(candidates: List<TrackedTarget>, nowMs: Long = System.currentTimeMillis()) {
        val current = _primary.value

        if (candidates.isEmpty()) {
            // No objects — check TTL grace period
            if (current != null && (nowMs - current.lastSeenMs) > ttlMs) {
                Log.d(TAG, "Target lost (TTL expired): id=${current.trackingId}")
                _primary.value = null
            }
            // else: within TTL — keep stale target visible
            return
        }

        // If we have a current target, try to find it by trackingId continuity
        val sameTarget = if (current?.trackingId != null) {
            candidates.firstOrNull { it.trackingId == current.trackingId }
        } else null

        if (sameTarget != null) {
            // Same target refreshed — update bbox + timestamp
            _primary.value = sameTarget
        } else {
            // Current target gone or no previous — select best candidate
            _primary.value = selectPrimary(candidates)
        }
    }

    /** User tapped a specific target — force-lock to its trackingId. */
    fun lockToTarget(trackingId: Int, candidates: List<TrackedTarget>) {
        val match = candidates.firstOrNull { it.trackingId == trackingId }
        if (match != null) {
            _primary.value = match
            Log.d(TAG, "User locked target: id=$trackingId")
        }
    }

    fun reset() { _primary.value = null }

    /**
     * Default selection policy: largest bounding box area.
     * Override for center-biased or face-priority logic.
     */
    private fun selectPrimary(candidates: List<TrackedTarget>): TrackedTarget {
        return candidates.maxBy { it.screenBox.width() * it.screenBox.height() }
    }

    companion object {
        private const val TAG = "TargetStateStore"
    }
}

// ── Object Tracking Pipeline ────────────────────────────────────────────────
//
// End-to-end data flow:
//   ImageProxy → TokenBucket gate → ML Kit ObjectDetector (STREAM_MODE)
//   → coordinate transform (image→screen) → TargetStateStore (TTL)
//   → TrackedTarget (screen bbox + metering center)
//
// Thread model (L2 §3.2): ObjectDetector.process() runs on a dedicated
// single-thread executor; results are posted to the callback on that thread.

class ObjectTrackingPipeline(
    private val throttle: TokenBucket = TokenBucket(maxCapacity = 1, refillRatePerSecond = 15.0),
    val targetStore: TargetStateStore = TargetStateStore(ttlMs = 500L)
) {
    private val TAG = "ObjectTrackingPipeline"

    // Dedicated executor for ML Kit inference (L2 §3.2)
    private val mlExecutor = Executors.newSingleThreadExecutor()

    // ML Kit Object Detector — STREAM_MODE for stable cross-frame trackingIds
    private val detector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ObjectDetection.getClient(options).also {
            Log.i(TAG, "ObjectDetector initialized (STREAM_MODE, multiObject, classification)")
        }
    }

    // Coordinate mapping state — set by the hosting Activity/Fragment
    // when PreviewView layout and image dimensions are known.
    @Volatile var contentBounds: RectF = RectF()
    @Volatile var imageWidth: Int = 0
    @Volatile var imageHeight: Int = 0

    /**
     * Call from CameraX ImageAnalysis.Analyzer.analyze().
     *
     * IMPORTANT: This method closes the ImageProxy after use. Do NOT close it
     * externally when using this pipeline.
     *
     * ```kotlin
     * ImageAnalysis.Builder()
     *     .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
     *     .build()
     *     .also { analysis ->
     *         analysis.setAnalyzer(cameraExecutor) { imageProxy ->
     *             pipeline.analyze(imageProxy)
     *         }
     *     }
     * ```
     */
    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy) {
        // Token Bucket gate — drop frame if over target FPS
        if (!throttle.tryConsume()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        // Cache effective image dimensions (rotation-aware)
        val effW = if (rotation == 90 || rotation == 270) inputImage.height else inputImage.width
        val effH = if (rotation == 90 || rotation == 270) inputImage.width else inputImage.height
        imageWidth = effW
        imageHeight = effH

        detector.process(inputImage)
            .addOnSuccessListener(mlExecutor) { objects ->
                onDetectionResult(objects, effW, effH)
            }
            .addOnFailureListener(mlExecutor) { e ->
                Log.w(TAG, "Object detection failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    // ── Coordinate Transform ────────────────────────────────────────────────
    //
    // Maps ML Kit bbox (image-analysis space) → screen space, accounting for
    // CameraX FIT_CENTER letterboxing via contentBounds.
    //
    // Geometry:
    //   scaleX = contentWidth / imageWidth
    //   scaleY = contentHeight / imageHeight
    //   scale  = min(scaleX, scaleY)   ← FIT_CENTER picks the smaller axis
    //   offsetX = contentBounds.left + (contentWidth - imageWidth * scale) / 2
    //   offsetY = contentBounds.top  + (contentHeight - imageHeight * scale) / 2
    //   screenX = imageX * scale + offsetX
    //   screenY = imageY * scale + offsetY

    fun imageToScreen(imageX: Float, imageY: Float, imgW: Int, imgH: Int): Pair<Float, Float> {
        val cb = contentBounds
        if (cb.isEmpty) return imageX to imageY  // fallback: no layout yet

        val contentW = cb.width()
        val contentH = cb.height()
        val scale = min(contentW / imgW, contentH / imgH)
        val offsetX = cb.left + (contentW - imgW * scale) / 2f
        val offsetY = cb.top + (contentH - imgH * scale) / 2f

        return (imageX * scale + offsetX) to (imageY * scale + offsetY)
    }

    fun imageRectToScreen(box: Rect, imgW: Int, imgH: Int): RectF {
        val (sLeft, sTop) = imageToScreen(box.left.toFloat(), box.top.toFloat(), imgW, imgH)
        val (sRight, sBottom) = imageToScreen(box.right.toFloat(), box.bottom.toFloat(), imgW, imgH)
        return RectF(sLeft, sTop, sRight, sBottom)
    }

    // ── MeteringPoint Generation ────────────────────────────────────────────
    //
    // Produces normalized [0,1] coordinates from the primary target's screen-
    // space center.  Pass these to PreviewView.meteringPointFactory.createPoint()
    // or SurfaceOrientedMeteringPointFactory for FocusMeteringAction.
    //
    // Usage example:
    //   val target = pipeline.targetStore.primary.value ?: return
    //   val factory = previewView.meteringPointFactory
    //   val point = factory.createPoint(target.centerX, target.centerY)
    //   val action = FocusMeteringAction.Builder(point, FLAG_AF or FLAG_AE)
    //       .setAutoCancelDuration(4, TimeUnit.SECONDS)
    //       .build()
    //   cameraControl.startFocusAndMetering(action)

    /**
     * Returns the current primary target's center in screen-pixel coordinates,
     * or null if no target is tracked.
     */
    fun getPrimaryMeteringCenter(): Pair<Float, Float>? {
        val target = targetStore.primary.value ?: return null
        return target.centerX to target.centerY
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun onDetectionResult(objects: List<DetectedObject>, imgW: Int, imgH: Int) {
        val nowMs = System.currentTimeMillis()

        val candidates = objects.mapNotNull { obj ->
            val screenBox = imageRectToScreen(obj.boundingBox, imgW, imgH)
            // Skip degenerate boxes
            if (screenBox.width() < 1f || screenBox.height() < 1f) return@mapNotNull null

            val label = obj.labels.maxByOrNull { it.confidence }
            TrackedTarget(
                trackingId = obj.trackingId,
                screenBox = screenBox,
                imageBox = obj.boundingBox,
                centerX = screenBox.centerX(),
                centerY = screenBox.centerY(),
                label = label?.text,
                confidence = label?.confidence ?: 0f,
                lastSeenMs = nowMs
            )
        }

        targetStore.update(candidates, nowMs)

        if (candidates.isNotEmpty()) {
            Log.d(TAG, "Detected ${candidates.size} objects, primary=${targetStore.primary.value?.trackingId}")
        }
    }

    fun release() {
        detector.close()
        mlExecutor.shutdown()
        targetStore.reset()
        Log.i(TAG, "Pipeline released")
    }
}
