package com.akirachang.ai.photographer.domain.config

import com.akirachang.ai.photographer.BuildConfig

/**
 * Feature Flags - Centralized feature toggle control
 *
 * v2.7.0 Phase 2: Guideline Overlay Feature Flags
 *
 * Controls the rollout and testing of new features through build-time flags.
 * All flags default to false for safety, and can be enabled per build type.
 *
 * Feature Flag Hierarchy:
 * - ENABLE_GUIDELINE_OVERLAY: Master switch for guideline overlay UI
 * - ENABLE_OPENCV_ANALYSIS: Controls OpenCV analyzer execution
 * - ENABLE_OPENCV_GUIDELINE_BINDING: Controls whether OpenCV results drive the overlay
 *
 * Rollback Strategy:
 * - Level 1: Disable ENABLE_OPENCV_GUIDELINE_BINDING -> Overlay shows but not driven by OpenCV
 * - Level 2: Disable ENABLE_OPENCV_ANALYSIS -> No OpenCV processing (performance stop-gap)
 * - Level 3: Disable ENABLE_GUIDELINE_OVERLAY -> Complete feature rollback
 */
object FeatureFlags {

    /**
     * Enable guideline overlay on camera preview
     *
     * When enabled, displays composition guidelines (e.g., rule of thirds grid)
     * over the camera preview.
     *
     * Default: false (Sprint 1 will enable for testing)
     */
    val enableGuidelineOverlay: Boolean
        get() = BuildConfig.ENABLE_GUIDELINE_OVERLAY

    /**
     * Enable OpenCV analysis for guideline generation
     *
     * When enabled, runs OpenCV analyzer on camera frames to generate
     * guideline data (e.g., horizon detection, rule of thirds).
     *
     * Requires: OpenCV dependency (Sprint 2+)
     * Default: false
     */
    val enableOpenCvAnalysis: Boolean
        get() = BuildConfig.ENABLE_OPENCV_ANALYSIS

    /**
     * Enable binding of OpenCV results to guideline overlay
     *
     * When enabled, the guideline overlay will be driven by OpenCV analysis
     * results instead of showing fixed guidelines.
     *
     * Requires: enableGuidelineOverlay = true AND enableOpenCvAnalysis = true
     * Default: false
     */
    val enableOpenCvGuidelineBinding: Boolean
        get() = BuildConfig.ENABLE_OPENCV_GUIDELINE_BINDING

    /**
     * Check if all guideline features are fully enabled
     */
    val isGuidelineFullyEnabled: Boolean
        get() = enableGuidelineOverlay && enableOpenCvAnalysis && enableOpenCvGuidelineBinding
}
