package com.akirachang.ai.photographer.data.opencv

import android.util.Log
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.core.TermCriteria
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.sqrt

/**
 * Optical Flow Point Tracker
 *
 * v2.x.x Phase 2: Lucas-Kanade sparse optical flow tracking
 *
 * Tracks arbitrary visual feature points using OpenCV calcOpticalFlowPyrLK().
 * Designed for tap-to-track functionality where users tap any point on the preview
 * and the focus indicator follows that point as the camera moves.
 *
 * Features:
 * - Single point tracking (optimized for performance)
 * - Quality assessment based on LK error and movement distance
 * - Automatic tracking loss detection
 * - Memory leak prevention with proper Mat resource management
 * - Frame rate throttling to reduce CPU usage
 *
 * Performance targets:
 * - Processing time: < 20ms per frame (P95)
 * - Memory overhead: < 5 MB during active tracking
 * - CPU usage: < 15% sustained
 *
 * Reference: OpenCV Lucas-Kanade optical flow
 * https://docs.opencv.org/4.x/dc/d6b/group__video__track.html#ga473e4b886d0bcc6b65831eb88ed93323
 */
class OpticalFlowTracker(
    private val config: OpticalFlowConfig = OpticalFlowConfig.DEFAULT
) {
    companion object {
        private const val TAG = "OpticalFlowTracker"

        // Downsampling configuration
        private const val TARGET_WIDTH = 640  // Downsample to 640px width for tracking
        private const val TARGET_HEIGHT = 480 // Downsample to 480px height for tracking

        // Quality calculation parameters
        private const val MAX_LK_ERROR = 50f  // Maximum acceptable Lucas-Kanade error
        private const val ERROR_WEIGHT = 0.7f // Weight of error in quality calculation
        private const val DIST_WEIGHT = 0.3f  // Weight of distance in quality calculation
    }

    // State management
    private val isTracking = AtomicBoolean(false)
    private val lastProcessTime = AtomicLong(0)

    // Previous frame storage
    private var prevGray: Mat? = null
    private var trackedPoint: Point? = null

    // Downsampling scale factors
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f

    /**
     * Start tracking a point
     *
     * @param initialPoint Initial point to track (in original image coordinates)
     * @param imageWidth Width of the input image
     * @param imageHeight Height of the input image
     */
    fun startTracking(initialPoint: Point, imageWidth: Int, imageHeight: Int) {
        // Release stale previous frame to ensure clean tracking session on re-tap
        releasePreviousFrame()

        // Calculate downsampling scale
        scaleX = TARGET_WIDTH.toFloat() / imageWidth
        scaleY = TARGET_HEIGHT.toFloat() / imageHeight

        // Convert point to downsampled coordinates
        trackedPoint = Point(
            initialPoint.x * scaleX,
            initialPoint.y * scaleY
        )

        isTracking.set(true)
        Log.i(TAG, "Started tracking at (${initialPoint.x}, ${initialPoint.y}), scale=($scaleX, $scaleY)")
    }

    /**
     * Stop tracking and release resources
     */
    fun stopTracking() {
        isTracking.set(false)
        releasePreviousFrame()
        trackedPoint = null
        Log.i(TAG, "Stopped tracking")
    }

    /**
     * Track the point in a new frame
     *
     * @param currentGray Current frame (grayscale Mat, CV_8UC1)
     * @return Tracking result with updated point coordinates and quality
     */
    fun track(currentGray: Mat): TrackingResult {
        // Check if tracking is active
        if (!isTracking.get() || trackedPoint == null) {
            return TrackingResult.LOST
        }

        // Throttle frame rate
        val currentTime = System.currentTimeMillis()
        val timeSinceLastProcess = currentTime - lastProcessTime.get()
        if (timeSinceLastProcess < config.throttleIntervalMs) {
            // Return previous point without processing
            return TrackingResult(
                point = scalePointToOriginal(trackedPoint!!),
                quality = 1.0f,
                distance = 0f,
                isValid = true
            )
        }
        lastProcessTime.set(currentTime)

        val startTime = System.nanoTime()

        // Downsample current frame
        val downsampledGray = downsampleMat(currentGray)

        try {
            // First frame - just store it
            if (prevGray == null) {
                prevGray = downsampledGray.clone()
                return TrackingResult(
                    point = scalePointToOriginal(trackedPoint!!),
                    quality = 1.0f,
                    distance = 0f,
                    isValid = true
                )
            }

            // Prepare input points
            val prevPoints = MatOfPoint2f(trackedPoint)
            val nextPoints = MatOfPoint2f()
            val status = MatOfByte()
            val err = MatOfFloat()

            // Create termination criteria
            val criteria = TermCriteria(
                TermCriteria.COUNT or TermCriteria.EPS,
                config.maxCount,
                config.epsilon
            )

            try {
                // Calculate optical flow
                Video.calcOpticalFlowPyrLK(
                    prevGray,           // Previous frame
                    downsampledGray,    // Current frame
                    prevPoints,         // Previous point
                    nextPoints,         // Output: next point
                    status,             // Output: status (1 = found, 0 = not found)
                    err,                // Output: error
                    config.winSize,     // Search window size
                    config.maxLevel,    // Pyramid levels
                    criteria,           // Termination criteria
                    0,                  // Flags
                    0.001               // Min eigenvalue threshold
                )

                // Extract results
                val statusArray = status.toArray()
                val errorArray = err.toArray()
                val nextPointArray = nextPoints.toArray()

                // Check if tracking succeeded
                if (statusArray.isNotEmpty() && statusArray[0].toInt() == 1) {
                    val newPoint = nextPointArray[0]
                    val error = errorArray[0]
                    val distance = calculateDistance(trackedPoint!!, newPoint)

                    // Calculate tracking quality
                    val quality = calculateQuality(error, distance)

                    // Validate tracking result
                    val isValid = validateTracking(
                        newPoint,
                        downsampledGray.width().toFloat(),
                        downsampledGray.height().toFloat(),
                        quality,
                        distance
                    )

                    val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
                    Log.v(TAG, "Track: quality=${"%.2f".format(quality)}, distance=${"%.1f".format(distance)}px, error=${"%.1f".format(error)}, elapsed=${elapsedMs}ms")

                    if (isValid) {
                        // Update tracked point
                        trackedPoint = newPoint

                        // Update previous frame
                        releasePreviousFrame()
                        prevGray = downsampledGray.clone()

                        return TrackingResult(
                            point = scalePointToOriginal(newPoint),
                            quality = quality,
                            distance = distance,
                            isValid = true
                        )
                    } else {
                        // Tracking lost
                        Log.w(TAG, "Tracking lost: quality too low or out of bounds")
                        stopTracking()
                        return TrackingResult.LOST
                    }
                } else {
                    // Optical flow failed
                    Log.w(TAG, "Optical flow failed: status=${statusArray.firstOrNull()}")
                    stopTracking()
                    return TrackingResult.LOST
                }

            } finally {
                // Release OpenCV matrices
                prevPoints.release()
                nextPoints.release()
                status.release()
                err.release()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Tracking error", e)
            stopTracking()
            return TrackingResult.LOST
        } finally {
            // Release downsampled frame
            downsampledGray.release()
        }
    }

    /**
     * Downsample Mat to target size for efficient tracking
     */
    private fun downsampleMat(src: Mat): Mat {
        val dst = Mat()
        val targetSize = Size(TARGET_WIDTH.toDouble(), TARGET_HEIGHT.toDouble())
        Imgproc.resize(src, dst, targetSize, 0.0, 0.0, Imgproc.INTER_LINEAR)
        return dst
    }

    /**
     * Scale point from downsampled coordinates back to original coordinates
     */
    private fun scalePointToOriginal(point: Point): Point {
        return Point(
            point.x / scaleX,
            point.y / scaleY
        )
    }

    /**
     * Calculate Euclidean distance between two points
     */
    private fun calculateDistance(p1: Point, p2: Point): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * Calculate tracking quality (0.0-1.0) based on LK error and movement distance
     *
     * Quality formula:
     * - Error component: 1 - (error / MAX_LK_ERROR)
     * - Distance component: 1 - (distance / maxTrackingDistance)
     * - Final quality = ERROR_WEIGHT * errorQuality + DIST_WEIGHT * distanceQuality
     */
    private fun calculateQuality(error: Float, distance: Float): Float {
        // Error quality (inverse of error, normalized)
        val errorQuality = 1f - (error / MAX_LK_ERROR).coerceIn(0f, 1f)

        // Distance quality (inverse of distance, normalized)
        val distanceQuality = 1f - (distance / config.maxTrackingDistance).coerceIn(0f, 1f)

        // Weighted combination
        return (ERROR_WEIGHT * errorQuality + DIST_WEIGHT * distanceQuality).coerceIn(0f, 1f)
    }

    /**
     * Validate tracking result
     *
     * Checks:
     * 1. Point is within image bounds
     * 2. Quality is above minimum threshold
     * 3. Distance is below maximum threshold
     */
    private fun validateTracking(
        point: Point,
        imageWidth: Float,
        imageHeight: Float,
        quality: Float,
        distance: Float
    ): Boolean {
        // Check bounds
        if (point.x < 0 || point.x >= imageWidth || point.y < 0 || point.y >= imageHeight) {
            return false
        }

        // Check quality threshold
        if (quality < config.minQuality) {
            return false
        }

        // Check distance threshold
        if (distance > config.maxTrackingDistance) {
            return false
        }

        return true
    }

    /**
     * Release previous frame Mat to prevent memory leak
     */
    private fun releasePreviousFrame() {
        prevGray?.release()
        prevGray = null
    }

    /**
     * Check if tracker is currently tracking
     */
    fun isTracking(): Boolean = isTracking.get()
}

/**
 * Tracking Result
 *
 * Encapsulates the result of a single tracking frame
 *
 * @property point Tracked point coordinates (original image coordinates)
 * @property quality Tracking quality (0.0-1.0), based on LK error and movement
 * @property distance Movement distance from previous frame (pixels)
 * @property isValid True if tracking is valid, false if lost
 */
data class TrackingResult(
    val point: Point,
    val quality: Float,
    val distance: Float,
    val isValid: Boolean
) {
    companion object {
        /**
         * Lost tracking result (invalid)
         */
        val LOST = TrackingResult(
            point = Point(0.0, 0.0),
            quality = 0f,
            distance = 0f,
            isValid = false
        )
    }
}
