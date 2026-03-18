package com.akirachang.ai.photographer.domain.sensor

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for hardware gyroscope data stream.
 *
 * Clean Architecture rule:
 * - Domain defines the contract only.
 * - Data layer owns all Android SensorManager implementation details.
 */
interface DeviceSensorManager {
    fun startGyroscopeTracking(): Flow<GyroscopeData>
}
