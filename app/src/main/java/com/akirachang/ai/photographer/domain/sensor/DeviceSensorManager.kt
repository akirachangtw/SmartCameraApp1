package com.akirachang.ai.photographer.domain.sensor

import kotlinx.coroutines.flow.Flow

/**
 * Raw gyroscope sample from TYPE_GYROSCOPE.
 *
 * @property x Angular velocity around X axis (rad/s)
 * @property y Angular velocity around Y axis (rad/s)
 * @property z Angular velocity around Z axis (rad/s)
 * @property timestamp Sensor event timestamp in nanoseconds
 */
data class GyroscopeData(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long,
)

/**
 * Domain contract for hardware gyroscope data stream.
 *
 * Implementations must provide a cold [Flow] of gyroscope samples.
 * Graceful degradation (no gyroscope hardware) is handled by the
 * implementation — callers receive an empty Flow and require no
 * special handling.
 *
 * v2.12.0 Ticket 1.5: Extracted as pure domain interface to enforce
 * Clean Architecture dependency inversion.
 */
interface DeviceSensorManager {

    /**
     * Returns a cold [Flow] of [GyroscopeData] samples at
     * [android.hardware.SensorManager.SENSOR_DELAY_FASTEST] rate.
     *
     * The Flow closes without error if gyroscope hardware is absent.
     * The listener is guaranteed to be unregistered when collection
     * is cancelled.
     */
    fun startGyroscopeTracking(): Flow<GyroscopeData>
}
