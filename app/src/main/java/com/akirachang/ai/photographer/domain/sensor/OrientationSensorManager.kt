package com.akirachang.ai.photographer.domain.sensor

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for device orientation tracking (accelerometer + magnetometer).
 *
 * Clean Architecture rule:
 * - Domain defines the contract only.
 * - Data layer owns all Android SensorManager implementation details.
 *
 * Separated from [DeviceSensorManager] (gyroscope) per Interface Segregation Principle:
 * LevelIndicator consumers only need orientation; IMU pipeline only needs gyroscope.
 */
interface OrientationSensorManager {
    /** Returns true if the required orientation sensors (accelerometer + magnetometer) are present. */
    fun areSensorsAvailable(): Boolean

    /**
     * Hot Flow that emits [OrientationData] while collected.
     * Sensor registration starts on collection; unregisters on cancellation.
     * Closes immediately if sensors are unavailable.
     */
    fun startOrientationTracking(): Flow<OrientationData>
}
