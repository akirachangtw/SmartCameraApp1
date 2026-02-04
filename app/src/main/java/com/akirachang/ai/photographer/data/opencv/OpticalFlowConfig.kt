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
    val maxDistancePerFrame: Float = 15f,

    /**
     * Minimum quality threshold for jitter detection
     * If quality is below this AND distance exceeds maxDistanceForJumpDetection,
     * the frame is considered a jitter and rejected.
     *
     * Default: 0.85f - based on logcat (jitter frames show quality ~0.80)
     */
    val minQualityForJumpDetection: Float = 0.85f,

    /**
     * Maximum distance threshold for jitter detection (pixels, downsampled coordinates)
     * Combined with minQualityForJumpDetection to identify jitter frames.
     *
     * Default: 10f - normal tracking typically < 5px
     */
    val maxDistanceForJumpDetection: Float = 10f
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
