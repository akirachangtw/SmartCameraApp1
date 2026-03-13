package com.akirachang.ai.photographer.domain.model.focus

/**
 * Static capabilities of the current lens/camera.
 * v2.12.0-15 / §17.11.8
 */
data class LensCapability(
    val isFrontCamera: Boolean,
    val isFixedFocus: Boolean,
    val supportsAutoFocus: Boolean,
    val supportsContinuousAf: Boolean,
    val supportsAeAfLock: Boolean,
)
