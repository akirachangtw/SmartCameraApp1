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
     * Default: 0.3f - conservative threshold to avoid false positives
     */
    val minQuality: Float = 0.3f,

    /**
     * Maximum tracking distance per frame (pixels)
     *
     * If point moves more than this distance, tracking is considered lost
     * Prevents tracking jumps to unrelated features
     *
     * Default: 200f - reasonable for handheld camera movement
     */
    val maxTrackingDistance: Float = 200f,

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
    val maxDistancePerFrame: Float = 25f,  // Phase 9B-fix: 15f→25f to reduce per-frame jump magnitude

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
     * Default: 0.5f - at 10px median motion the threshold is 5px
     */
    val ransacDistanceThreshold: Float = 0.5f,

    /**
     * Frames between full feature re-detection via goodFeaturesToTrack.
     * Replenishes point set as LK drops failed points between redetections.
     *
     * Default: 15 - at 15 FPS throttle this is ~1 second
     */
    val redetectionInterval: Int = 15
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
