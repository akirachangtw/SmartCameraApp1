package com.akirachang.ai.photographer.domain.config

/**
 * Feature Flags — Cueshot MVP (D0 surgery: OpenCV flags removed)
 *
 * All OpenCV / Optical Flow flags permanently removed.
 * Cueshot v3.3 pipeline uses pure Kotlin + ML Kit only.
 */
object FeatureFlags {

    /** Manual focus (Tap-to-Focus) — always enabled */
    val enableManualFocus: Boolean
        get() = true

    /** Continuous face/object tracking — always enabled */
    val enableFocusTracking: Boolean
        get() = true

    /** AE/AF Lock (long press) — always enabled */
    val enableAEAFLock: Boolean
        get() = true
}
