package com.akirachang.ai.photographer.domain.sensor

/**
 * Raw gyroscope sample from TYPE_GYROSCOPE.
 *
 * @property x Angular velocity around X axis in rad/s
 * @property y Angular velocity around Y axis in rad/s
 * @property z Angular velocity around Z axis in rad/s
 * @property timestamp Sensor event timestamp in nanoseconds
 */
data class GyroscopeData(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long,
)
