package com.akirachang.ai.photographer.data.opencv

import org.opencv.core.Size

/**
 * Optical Flow Configuration
 *
 * v2.x.x Phase 2: Lucas-Kanade optical flow parameters
 *
 * Configuration for sparse optical flow tracking using OpenCV calcOpticalFlowPyrLK().
 * These parameters control the tracking quality, performance, and robustness.
 *
 * Reference: OpenCV documentation for calcOpticalFlowPyrLK
 * https://docs.opencv.org/4.x/dc/d6b/group__video__track.html
 */
data class OpticalFlowConfig(
    /**
     * Search window size at each pyramid level
     *
     * Larger window = more robust to large motion, but slower
     * Smaller window = faster, but may lose tracking with fast motion
     *
     * Default: Size(31.0, 31.0) - handles handheld pan at 28+ FPS
     */
    val winSize: Size = Size(31.0, 31.0),

    /**
     * Maximum pyramid level
     *
     * 0 = no pyramid (single scale)
     * Higher levels = more robust to large motion and scale changes
     *
     * Default: 4 - max trackable motion ~240px/frame at downsample resolution
     */
    val maxLevel: Int = 4,

    /**
     * Maximum number of iterations per pyramid level
     *
     * Higher = more accurate but slower
     *
     * Default: 30 - OpenCV recommended value
     */
    val maxCount: Int = 30,

    /**
     * Convergence epsilon threshold
     *
     * Algorithm stops if point movement is less than this value
     *
     * Default: 0.01 - standard precision
     */
    val epsilon: Double = 0.01,

    /**
     * Minimum tracking quality threshold (0.0-1.0)
     *
     * Tracking is considered lost if quality drops below this value
     * Quality is calculated from LK error and movement distance
     *
     * Default: 0.2f - same as ACCURATE preset; logicCenter split reduces false positives
     * v2.11.3: 0.3→0.2 (safety net for fast-motion quality drop; logicCenter fix is primary fix)
     */
    val minQuality: Float = 0.2f,

    /**
     * Maximum tracking distance per frame (pixels)
     *
     * If point moves more than this distance, tracking is considered lost
     * Prevents tracking jumps to unrelated features
     *
     * Default: 250f - allows fast handheld panning (~39% of 640px frame per frame)
     * v2.11.3: 200→250 to prevent LOST at extreme phone pan (observed 201px/frame at 200px limit)
     */
    val maxTrackingDistance: Float = 250f,

    /**
     * Target frame rate for tracking (FPS)
     *
     * Tracking will be throttled to this rate to save CPU
     *
     * Default: 15 FPS - balanced performance/responsiveness
     */
    val targetFps: Int = 15,

    // --- Phase 7: Anti-jitter parameters ---

    /**
     * Maximum point displacement per frame (pixels, downsampled coordinates)
     * Used to clamp velocity and prevent visual jitter from large sudden jumps.
     *
     * Based on logcat analysis: normal tracking distance < 5px, jitter spikes 30-35px.
     * Default: 15f - allows normal motion while clamping extreme jumps
     */
    val maxDistancePerFrame: Float = 40f,  // v2.11.3: 25f→40f; logicCenter split removes drift risk, UI can follow faster

    // --- Multi-point LK tracking parameters ---

    /**
     * Maximum number of feature points to detect via goodFeaturesToTrack.
     * More points = more robust median, but higher LK cost per frame.
     *
     * Default: 30 - sufficient for median stability at 640px downsample
     */
    val maxFeaturePoints: Int = 30,

    /**
     * Minimum quality level for goodFeaturesToTrack (0.0-1.0).
     * Points with response below qualityLevel * maxResponse are discarded by OpenCV.
     *
     * Default: 0.01 - permissive; let count / ROI do the filtering
     */
    val featureQualityLevel: Double = 0.01,

    /**
     * Minimum pixel distance between detected features (goodFeaturesToTrack minDistance).
     * Prevents clustering of features in a single texture patch.
     *
     * Default: 10.0 - spreads features across the ROI
     */
    val featureMinDistance: Double = 10.0,

    /**
     * Radius (pixels, downsampled coordinates) of the square ROI around the tracked
     * center point used for goodFeaturesToTrack detection.
     *
     * Default: 80 - covers ~160x160 px area at 640 downsample
     */
    val featureROISize: Int = 80,

    /**
     * Minimum ratio of inlier points (after RANSAC) to valid LK points.
     * If inlierCount / validCount falls below this, tracking is declared LOST.
     *
     * Default: 0.4f - at least 40% of LK-successful points must agree
     */
    val minValidPointsRatio: Float = 0.4f,

    /**
     * RANSAC distance threshold multiplier.
     * Actual threshold = max(3.0, ransacDistanceThreshold * medianDisplacementMagnitude).
     * Adaptive: tight for small motions, wider for large motions.
     *
     * Default: 1.0f - at 10px median motion the threshold is 10px
     * v2.11.2: 0.5→1.0 to reduce over-rejection in small-motion scenes (hand-held shake)
     */
    val ransacDistanceThreshold: Float = 1.0f,

    /**
     * Frames between full feature re-detection via goodFeaturesToTrack.
     * Replenishes point set as LK drops failed points between redetections.
     *
     * Default: 5 - at 15 FPS throttle this is ~333ms
     * v2.11.2: 15→5 for faster feature replenishment, prevents point depletion → LOST
     */
    val redetectionInterval: Int = 5
) {
    companion object {
        /**
         * Default configuration - balanced performance and accuracy
         */
        val DEFAULT = OpticalFlowConfig()

        /**
         * High performance configuration - faster but less accurate
         */
        val FAST = OpticalFlowConfig(
            winSize = Size(21.0, 21.0),
            maxLevel = 3,
            maxCount = 20,
            minQuality = 0.4f,
            targetFps = 10
        )

        /**
         * High accuracy configuration - slower but more robust
         */
        val ACCURATE = OpticalFlowConfig(
            winSize = Size(31.0, 31.0),
            maxLevel = 5,
            maxCount = 40,
            minQuality = 0.2f,
            targetFps = 20
        )
    }

    /**
     * Calculate throttle interval in milliseconds
     */
    val throttleIntervalMs: Long
        get() = (1000L / targetFps)
}
