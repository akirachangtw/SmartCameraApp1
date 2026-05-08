package com.akirachang.ai.photographer.domain.config

/** Feature Flags — Cueshot MVP (pure Kotlin + ML Kit pipeline) */
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
