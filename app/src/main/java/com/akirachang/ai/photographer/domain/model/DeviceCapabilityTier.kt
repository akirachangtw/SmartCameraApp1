package com.akirachang.ai.photographer.domain.model

/**
 * Device hardware capability tier for adaptive pipeline selection.
 * Cueshot v3.3 SSOT — maps to Camera2 INFO_SUPPORTED_HARDWARE_LEVEL.
 *
 * FULL    → Camera2 FULL or LEVEL_3: supports AE/AF lock, manual controls
 * LIMITED → Camera2 LIMITED: partial manual control support
 * LEGACY  → Camera2 LEGACY: minimal control, fixed-focus fallback path
 */
sealed class DeviceCapabilityTier {
    /** Camera2 FULL / LEVEL_3 — full AE/AF lock, manual lens control */
    data object FULL : DeviceCapabilityTier()

    /** Camera2 LIMITED — partial manual control, no guaranteed AE lock */
    data object LIMITED : DeviceCapabilityTier()

    /** Camera2 LEGACY — minimal control, fixed-focus synthetic lock path */
    data object LEGACY : DeviceCapabilityTier()

    companion object {
        /**
         * Map Camera2 INFO_SUPPORTED_HARDWARE_LEVEL integer to [DeviceCapabilityTier].
         * @param level Raw value from CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
         */
        fun fromCamera2Level(level: Int): DeviceCapabilityTier = when (level) {
            1, 3 -> FULL    // FULL=1, LEVEL_3=3
            0    -> LIMITED // LIMITED=0
            else -> LEGACY  // LEGACY=2, EXTERNAL=4, unknown
        }
    }
}
