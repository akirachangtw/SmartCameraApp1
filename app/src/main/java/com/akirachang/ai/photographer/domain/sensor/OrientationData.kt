package com.akirachang.ai.photographer.domain.sensor

import kotlin.math.abs

/**
 * Domain model representing device orientation.
 *
 * @property roll   Device roll angle in degrees (-180 to 180).
 *                  Positive: device tilted to the right; Negative: tilted to the left; 0: level.
 * @property pitch  Device pitch angle in degrees (-180 to 180).
 * @property azimuth Device azimuth angle in degrees (0 to 360).
 */
data class OrientationData(
    val roll: Float,
    val pitch: Float,
    val azimuth: Float
) {
    /**
     * Checks if device is approximately level.
     * @param threshold Tolerance in degrees (default: 2.0°)
     */
    fun isLevel(threshold: Float = 2.0f): Boolean {
        return abs(roll) <= threshold
    }
}
