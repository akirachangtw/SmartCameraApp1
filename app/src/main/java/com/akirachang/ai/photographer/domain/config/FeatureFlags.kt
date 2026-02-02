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
     * Use ImageProxy direct path for guideline analysis (v2.8.0 Phase 2)
     *
     * When enabled, GuidelineAnalyzer receives ImageProxy directly from ImageAnalysis
     * instead of processing previewBitmap Flow. This bypasses Bitmap.toGrayMat() conversion
     * and uses ImageProxy.toGrayMat() instead, saving ~25ms per frame.
     *
     * Performance impact:
     * - Old path: ImageProxy → Bitmap → RGBA Mat → Gray Mat (60ms)
     * - New path: ImageProxy → Gray Mat (10ms, direct Y plane extraction)
     *
     * Requires: enableOpenCvAnalysis = true
     * Default: false (rollout after Phase 2 validation)
     */
    val useImageProxyGuidelinePipeline: Boolean
        get() = BuildConfig.USE_IMAGEPROXY_GUIDELINE_PIPELINE

    /**
     * Enable manual focus (Tap-to-Focus)
     * v2.9.9: Day 1-2 - Tap-to-Focus implementation
     *
     * When enabled, users can tap on the preview to trigger autofocus at that point.
     * Uses CameraX FocusMeteringAction API with 5-second auto-cancel.
     *
     * Default: true (enabled for testing)
     */
    val enableManualFocus: Boolean
        get() = true  // v2.9.9: Always enabled (no BuildConfig flag needed for now)

    /**
     * Enable focus tracking (Traycer)
     * v2.9.10: Continuous object tracking
     *
     * When enabled, tapping on a detected face/object will start continuous tracking.
     * The focus indicator (white circle) will follow the target as it moves.
     *
     * Requires: enableManualFocus = true
     * Default: true (enabled for testing)
     */
    val enableFocusTracking: Boolean
        get() = true  // v2.9.10: Always enabled (Traycer tracking)

    /**
     * Enable AE/AF Lock (Traycer v2)
     * v2.9.9 Day 1-2: Long press to lock exposure and focus
     *
     * When enabled, long pressing on the preview will lock AE (auto exposure) and
     * AF (auto focus) at that point. The lock is persistent until unlocked by tapping.
     * Displays yellow dashed square indicator at lock point.
     *
     * Requires: enableManualFocus = true
     * Default: true (enabled for testing)
     */
    val enableAEAFLock: Boolean
        get() = true  // v2.9.9 Day 1-2: Always enabled (Traycer v2 AE/AF Lock)

    /**
     * Check if all guideline features are fully enabled
     */
    val isGuidelineFullyEnabled: Boolean
        get() = enableGuidelineOverlay && enableOpenCvAnalysis && enableOpenCvGuidelineBinding
}
