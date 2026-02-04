package com.akirachang.ai.photographer.data.opencv

import android.util.Log
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.core.TermCriteria
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Optical Flow Point Tracker
 *
 * v2.x.x Phase 8: Multi-point Lucas-Kanade sparse optical flow tracking
 *
 * Tracks visual feature points using OpenCV goodFeaturesToTrack + calcOpticalFlowPyrLK().
 * Multiple points are tracked simultaneously and aggregated via median displacement
 * with RANSAC outlier filtering to reduce cumulative drift.
 *
 * Designed for tap-to-track functionality where users tap any point on the preview
 * and the focus indicator follows that point as the camera moves.
 *
 * Features:
 * - Multi-point tracking: goodFeaturesToTrack ROI detection + parallel LK
 * - RANSAC median aggregation: robust to outlier points
 * - Periodic feature redetection: prevents point-set shrinkage over time
 * - Quality assessment: inlier ratio * LK error-based quality
 * - Phase 7 anti-jitter: velocity clamping + jitter frame rejection
 * - Memory leak prevention with proper Mat resource management
 * - Frame rate throttling to reduce CPU usage
 *
 * Performance targets:
 * - Processing time: < 25ms per frame (P95)
 * - Memory overhead: < 8 MB during active tracking
 * - CPU usage: < 20% sustained
 *
 * Reference: OpenCV Lucas-Kanade optical flow
 * https://docs.opencv.org/4.x/dc/d6b/group__video__track.html#ga473e4b886d0bcc6b65831eb88ed93323
 */
class OpticalFlowTracker(
    private val config: OpticalFlowConfig = OpticalFlowConfig.DEFAULT
) {
    companion object {
        private const val TAG = "OpticalFlowTracker"

        // Downsampling: longest edge target (aspect ratio preserved dynamically)
        private const val MAX_DOWNSAMPLE_DIM = 640

        // Quality calculation parameters
        private const val MAX_LK_ERROR = 50f  // Maximum acceptable Lucas-Kanade error
        private const val ERROR_WEIGHT = 0.7f // Weight of error in quality calculation
        private const val DIST_WEIGHT = 0.3f  // Weight of distance in quality calculation

        // RANSAC: minimum displacement deviation threshold regardless of median magnitude
        private const val MIN_RANSAC_THRESHOLD = 3f
    }

    // State management
    private val isTracking = AtomicBoolean(false)
    private val lastProcessTime = AtomicLong(0)

    // Previous frame storage
    private var prevGray: Mat? = null

    // Multi-point tracking state (all coordinates in downsampled space)
    private var trackedPoints: MatOfPoint2f? = null   // Current feature point set for LK
    private var trackedCenter: Point? = null           // Median center point (output target)
    private var redetectionFrameCount: Int = 0         // Counter for periodic feature redetection

    // Downsampling dimensions (aspect-ratio-preserving, recalculated per session)
    private var downsampleWidth: Int = 0
    private var downsampleHeight: Int = 0

    // Downsampling scale factors (downsample coords / original coords)
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f

    // Phase 7: Last valid tracking result for jitter rejection fallback
    private var lastValidResult: TrackingResult? = null

    /**
     * Start tracking a point
     *
     * @param initialPoint Initial point to track (in original image coordinates)
     * @param imageWidth Width of the input image
     * @param imageHeight Height of the input image
     */
    fun startTracking(initialPoint: Point, imageWidth: Int, imageHeight: Int) {
        // Release stale state to ensure clean tracking session on re-tap
        releasePreviousFrame()
        releaseTrackedPoints()

        // v2.x.x Phase 7: Aspect-ratio-preserving downsample size
        val scale = minOf(MAX_DOWNSAMPLE_DIM.toFloat() / imageWidth, MAX_DOWNSAMPLE_DIM.toFloat() / imageHeight)
        downsampleWidth = (imageWidth * scale).toInt()
        downsampleHeight = (imageHeight * scale).toInt()

        scaleX = downsampleWidth.toFloat() / imageWidth
        scaleY = downsampleHeight.toFloat() / imageHeight

        // Store initial center in downsampled coordinates.
        // trackedPoints will be populated on the first frame via detectFeatures().
        trackedCenter = Point(
            initialPoint.x * scaleX,
            initialPoint.y * scaleY
        )

        redetectionFrameCount = 0
        lastValidResult = null
        isTracking.set(true)
        Log.i(TAG, "Started multi-point tracking at (${initialPoint.x}, ${initialPoint.y}), downsample=${downsampleWidth}x${downsampleHeight}, scale=($scaleX, $scaleY)")
    }

    /**
     * Stop tracking and release resources
     */
    fun stopTracking() {
        isTracking.set(false)
        releasePreviousFrame()
        releaseTrackedPoints()
        trackedCenter = null
        lastValidResult = null
        Log.i(TAG, "Stopped tracking")
    }

    /**
     * Track the point in a new frame
     *
     * @param currentGray Current frame (grayscale Mat, CV_8UC1)
     * @return Tracking result with updated point coordinates and quality
     */
    fun track(currentGray: Mat): TrackingResult {
        if (!isTracking.get() || trackedCenter == null) {
            return TrackingResult.LOST
        }

        // Throttle frame rate
        val currentTime = System.currentTimeMillis()
        val timeSinceLastProcess = currentTime - lastProcessTime.get()
        if (timeSinceLastProcess < config.throttleIntervalMs) {
            return TrackingResult(
                point = scalePointToOriginal(trackedCenter!!),
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
            // First frame — detect features and store as baseline; no LK to run yet
            if (prevGray == null) {
                prevGray = downsampledGray.clone()
                detectFeatures(downsampledGray, trackedCenter!!)
                return TrackingResult(
                    point = scalePointToOriginal(trackedCenter!!),
                    quality = 1.0f,
                    distance = 0f,
                    isValid = true
                )
            }

            // Guard: if point set is empty, attempt redetection before giving up
            if (trackedPoints == null || trackedPoints!!.rows() == 0) {
                detectFeatures(downsampledGray, trackedCenter!!)
                if (trackedPoints == null || trackedPoints!!.rows() == 0) {
                    Log.w(TAG, "Redetection yielded zero points — tracking lost")
                    stopTracking()
                    return TrackingResult.LOST
                }
                // New baseline frame established after redetection
                releasePreviousFrame()
                prevGray = downsampledGray.clone()
                redetectionFrameCount = 0
                return TrackingResult(
                    point = scalePointToOriginal(trackedCenter!!),
                    quality = 1.0f,
                    distance = 0f,
                    isValid = true
                )
            }

            // --- Multi-point LK optical flow ---
            // Independent copy of tracked points for LK input (must not alias trackedPoints)
            val prevPoints = MatOfPoint2f(*trackedPoints!!.toArray())
            val nextPoints = MatOfPoint2f()
            val status = MatOfByte()
            val err = MatOfFloat()

            val criteria = TermCriteria(
                TermCriteria.COUNT or TermCriteria.EPS,
                config.maxCount,
                config.epsilon
            )

            try {
                Video.calcOpticalFlowPyrLK(
                    prevGray,
                    downsampledGray,
                    prevPoints,
                    nextPoints,
                    status,
                    err,
                    config.winSize,
                    config.maxLevel,
                    criteria,
                    0,
                    0.001
                )

                val statusArray = status.toArray()
                val errorArray = err.toArray()
                val nextPointArray = nextPoints.toArray()
                val prevPointArray = prevPoints.toArray()
                val totalPoints = statusArray.size

                // --- Collect valid (status=1) displacements ---
                val validDx = mutableListOf<Float>()
                val validDy = mutableListOf<Float>()
                val validErrors = mutableListOf<Float>()
                val validNextPts = mutableListOf<Point>()

                for (i in 0 until totalPoints) {
                    if (statusArray[i].toInt() == 1) {
                        val dx = (nextPointArray[i].x - prevPointArray[i].x).toFloat()
                        val dy = (nextPointArray[i].y - prevPointArray[i].y).toFloat()
                        validDx.add(dx)
                        validDy.add(dy)
                        validErrors.add(errorArray[i])
                        validNextPts.add(nextPointArray[i])
                    }
                }

                val validCount = validDx.size
                if (validCount == 0) {
                    Log.w(TAG, "All $totalPoints points lost LK status")
                    stopTracking()
                    return TrackingResult.LOST
                }

                // --- RANSAC: median displacement + outlier filtering ---
                val medDx = median(validDx)
                val medDy = median(validDy)
                val medianMagnitude = sqrt((medDx * medDx + medDy * medDy).toDouble()).toFloat()
                val ransacThreshold = max(MIN_RANSAC_THRESHOLD, config.ransacDistanceThreshold * medianMagnitude)

                val inlierNextPts = mutableListOf<Point>()
                val inlierErrors = mutableListOf<Float>()

                for (i in validDx.indices) {
                    val devX = abs(validDx[i] - medDx)
                    val devY = abs(validDy[i] - medDy)
                    val deviation = sqrt((devX * devX + devY * devY).toDouble()).toFloat()
                    if (deviation <= ransacThreshold) {
                        inlierNextPts.add(validNextPts[i])
                        inlierErrors.add(validErrors[i])
                    }
                }

                val inlierCount = inlierNextPts.size
                val minRequired = max(1, (config.minValidPointsRatio * validCount).toInt())

                if (inlierCount < minRequired) {
                    Log.w(TAG, "RANSAC: inliers=$inlierCount / valid=$validCount < minRequired=$minRequired")
                    stopTracking()
                    return TrackingResult.LOST
                }

                // --- New center = previous center + median displacement ---
                val newCenter = Point(
                    trackedCenter!!.x + medDx,
                    trackedCenter!!.y + medDy
                )

                // --- Quality = inlier ratio * LK error-based quality ---
                val avgInlierError = inlierErrors.sum() / inlierCount
                val inlierRatio = inlierCount.toFloat() / validCount.toFloat()
                val baseQuality = calculateQuality(avgInlierError, medianMagnitude)
                val quality = (inlierRatio * baseQuality).coerceIn(0f, 1f)

                val distance = calculateDistance(trackedCenter!!, newCenter)

                val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
                Log.v(TAG, "Track: inliers=$inlierCount/valid=$validCount/total=$totalPoints, quality=${"%.2f".format(quality)}, dist=${"%.1f".format(distance)}px, med=(${"%.1f".format(medDx)},${"%.1f".format(medDy)}), elapsed=${elapsedMs}ms")

                // Phase 7: Jitter rejection
                if (isJitterFrame(quality, distance)) {
                    Log.w(TAG, "Jitter rejected: quality=${"%.2f".format(quality)}, distance=${"%.1f".format(distance)}px")
                    return lastValidResult ?: TrackingResult(
                        point = scalePointToOriginal(trackedCenter!!),
                        quality = 1.0f,
                        distance = 0f,
                        isValid = true
                    )
                }

                // --- Validate (bounds, quality, distance) ---
                val isValid = validateTracking(
                    newCenter,
                    downsampledGray.width().toFloat(),
                    downsampledGray.height().toFloat(),
                    quality,
                    distance
                )

                if (isValid) {
                    // Phase 7: Clamp velocity on center point
                    val clampedCenter = clampVelocity(trackedCenter!!, newCenter, distance)
                    trackedCenter = clampedCenter

                    // Update tracked points to inliers only (point set shrinks naturally)
                    releaseTrackedPoints()
                    trackedPoints = MatOfPoint2f(*inlierNextPts.toTypedArray())

                    // Periodic feature redetection to replenish point set
                    redetectionFrameCount++
                    if (redetectionFrameCount >= config.redetectionInterval) {
                        detectFeatures(downsampledGray, trackedCenter!!)
                        redetectionFrameCount = 0
                    }

                    // Update previous frame
                    releasePreviousFrame()
                    prevGray = downsampledGray.clone()

                    // Phase 7: Save as last valid result for jitter rejection fallback
                    val result = TrackingResult(
                        point = scalePointToOriginal(trackedCenter!!),
                        quality = quality,
                        distance = distance,
                        isValid = true
                    )
                    lastValidResult = result

                    return result
                } else {
                    Log.w(TAG, "Tracking lost: quality too low or out of bounds")
                    stopTracking()
                    return TrackingResult.LOST
                }

            } finally {
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
            downsampledGray.release()
        }
    }

    // ─── Utility: downsampling ────────────────────────────────────────────────

    /**
     * Downsample Mat to aspect-ratio-preserving target size for efficient tracking
     */
    private fun downsampleMat(src: Mat): Mat {
        val dst = Mat()
        val targetSize = Size(downsampleWidth.toDouble(), downsampleHeight.toDouble())
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

    // ─── Utility: math / quality ──────────────────────────────────────────────

    /**
     * Calculate Euclidean distance between two points
     */
    private fun calculateDistance(p1: Point, p2: Point): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * Phase 7: Clamp point velocity to prevent visual jitter.
     * Limits maximum displacement per frame while preserving movement direction.
     *
     * @param oldPoint Previous tracked point
     * @param newPoint New tracked point from LK
     * @param distance Pre-calculated distance between points
     * @return Clamped point if distance exceeds threshold, otherwise newPoint unchanged
     */
    private fun clampVelocity(oldPoint: Point, newPoint: Point, distance: Float): Point {
        if (distance <= config.maxDistancePerFrame) {
            return newPoint
        }

        val scale = config.maxDistancePerFrame / distance
        val dx = newPoint.x - oldPoint.x
        val dy = newPoint.y - oldPoint.y

        Log.v(TAG, "Velocity clamped: ${"%.1f".format(distance)}px -> ${config.maxDistancePerFrame}px")

        return Point(
            oldPoint.x + dx * scale,
            oldPoint.y + dy * scale
        )
    }

    /**
     * Phase 7: Detect jitter frame based on quality and distance thresholds.
     * A jitter frame has low quality combined with large distance — indicates tracking error.
     */
    private fun isJitterFrame(quality: Float, distance: Float): Boolean {
        return quality < config.minQualityForJumpDetection &&
               distance > config.maxDistanceForJumpDetection
    }

    /**
     * Calculate tracking quality (0.0-1.0) based on LK error and movement distance.
     *
     * Quality formula:
     * - Error component: 1 - (error / MAX_LK_ERROR)
     * - Distance component: 1 - (distance / maxTrackingDistance)
     * - Final quality = ERROR_WEIGHT * errorQuality + DIST_WEIGHT * distanceQuality
     */
    private fun calculateQuality(error: Float, distance: Float): Float {
        val errorQuality = 1f - (error / MAX_LK_ERROR).coerceIn(0f, 1f)
        val distanceQuality = 1f - (distance / config.maxTrackingDistance).coerceIn(0f, 1f)
        return (ERROR_WEIGHT * errorQuality + DIST_WEIGHT * distanceQuality).coerceIn(0f, 1f)
    }

    /**
     * Validate tracking result: bounds, quality, distance thresholds.
     */
    private fun validateTracking(
        point: Point,
        imageWidth: Float,
        imageHeight: Float,
        quality: Float,
        distance: Float
    ): Boolean {
        if (point.x < 0 || point.x >= imageWidth || point.y < 0 || point.y >= imageHeight) {
            return false
        }
        if (quality < config.minQuality) {
            return false
        }
        if (distance > config.maxTrackingDistance) {
            return false
        }
        return true
    }

    // ─── Multi-point: feature detection & RANSAC helpers ─────────────────────

    /**
     * Detect features around center using goodFeaturesToTrack.
     * Populates trackedPoints with detected corner points offset to full-frame coordinates.
     * Falls back to single center point if zero features are detected.
     *
     * @param gray  Downsampled grayscale frame (CV_8UC1)
     * @param center  Current tracked center in downsampled coordinates
     */
    private fun detectFeatures(gray: Mat, center: Point) {
        val roi = computeROI(center, gray.width(), gray.height())
        val roiMat = gray.submat(roi)  // submat is a view — do NOT release

        val corners = MatOfPoint()
        try {
            Imgproc.goodFeaturesToTrack(
                roiMat,
                corners,
                config.maxFeaturePoints,
                config.featureQualityLevel,
                config.featureMinDistance
            )

            if (corners.rows() == 0) {
                Log.w(TAG, "goodFeaturesToTrack: 0 points in ROI $roi — fallback to center")
                releaseTrackedPoints()
                trackedPoints = MatOfPoint2f(center)
                return
            }

            // Convert MatOfPoint (ROI-local integer coords) to MatOfPoint2f (full-frame double coords)
            val cornersArray = corners.toArray()
            val pointsArray = Array(cornersArray.size) { i ->
                Point(
                    cornersArray[i].x.toDouble() + roi.x,
                    cornersArray[i].y.toDouble() + roi.y
                )
            }

            releaseTrackedPoints()
            trackedPoints = MatOfPoint2f(*pointsArray)
            Log.v(TAG, "detectFeatures: ${pointsArray.size} points in ROI $roi")

        } finally {
            corners.release()
        }
    }

    /**
     * Compute square ROI rect around center, clamped to frame bounds.
     */
    private fun computeROI(center: Point, frameWidth: Int, frameHeight: Int): Rect {
        val r = config.featureROISize
        val cx = center.x.toInt()
        val cy = center.y.toInt()

        val left   = max(0, cx - r)
        val top    = max(0, cy - r)
        val right  = minOf(frameWidth, cx + r)
        val bottom = minOf(frameHeight, cy + r)

        return Rect(left, top, right - left, bottom - top)
    }

    /**
     * Compute median of a non-empty list of floats.
     */
    private fun median(values: List<Float>): Float {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2f
        } else {
            sorted[mid]
        }
    }

    // ─── Resource management ──────────────────────────────────────────────────

    /**
     * Release trackedPoints Mat to prevent memory leak.
     */
    private fun releaseTrackedPoints() {
        trackedPoints?.release()
        trackedPoints = null
    }

    /**
     * Release previous frame Mat to prevent memory leak.
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
