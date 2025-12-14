/**
 * ClassName: DeviceSensorManager
 * Description: Manages device orientation sensors for level indicator functionality
 * Author: Claude Code
 * Date: 2025-12-01
 * Version: 2.1.0
 *
 * Modifications:
 * - 2025-12-01: Initial creation for V1.0 completion (LevelIndicator feature)
 */
package com.akirachang.ai.photographer.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

/**
 * Data class representing device orientation
 *
 * @property roll Device roll angle in degrees (-180 to 180)
 *                - Positive: device tilted to the right
 *                - Negative: device tilted to the left
 *                - 0: device is level (horizon aligned)
 * @property pitch Device pitch angle in degrees (-180 to 180)
 * @property azimuth Device azimuth angle in degrees (0 to 360)
 */
data class OrientationData(
    val roll: Float,
    val pitch: Float,
    val azimuth: Float
) {
    /**
     * Checks if device is approximately level
     * @param threshold Tolerance in degrees (default: 2.0°)
     */
    fun isLevel(threshold: Float = 2.0f): Boolean {
        return abs(roll) <= threshold
    }
}

/**
 * Manages device sensors for orientation tracking
 *
 * Uses accelerometer and magnetometer to compute device orientation.
 * Provides a Flow-based API for reactive orientation updates.
 *
 * Usage:
 * ```
 * val sensorManager = DeviceSensorManager(context)
 * sensorManager.startOrientationTracking()
 *     .collect { orientation ->
 *         // Update UI with roll angle
 *         updateLevelIndicator(orientation.roll)
 *     }
 * ```
 */
class DeviceSensorManager(context: Context) {

    companion object {
        private const val TAG = "DeviceSensorManager"

        // Smoothing factor for exponential moving average (0.0 - 1.0)
        // Lower = smoother but slower response
        // Higher = faster response but more jitter
        private const val SMOOTHING_FACTOR = 0.2f
    }

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val magnetometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Sensor data buffers
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Smoothed orientation values
    private var smoothedRoll: Float = 0f
    private var smoothedPitch: Float = 0f
    private var smoothedAzimuth: Float = 0f

    /**
     * Checks if required sensors are available
     *
     * @return true if both accelerometer and magnetometer are available
     */
    fun areSensorsAvailable(): Boolean {
        val hasAccelerometer = accelerometer != null
        val hasMagnetometer = magnetometer != null

        if (!hasAccelerometer) {
            Log.w(TAG, "Accelerometer not available")
        }
        if (!hasMagnetometer) {
            Log.w(TAG, "Magnetometer not available")
        }

        return hasAccelerometer && hasMagnetometer
    }

    /**
     * Starts orientation tracking and returns a Flow of orientation data
     *
     * The Flow is hot - it starts sensor registration when collected
     * and automatically unregisters when the collection is cancelled.
     *
     * @return Flow emitting OrientationData updates
     */
    fun startOrientationTracking(): Flow<OrientationData> = callbackFlow {
        if (!areSensorsAvailable()) {
            Log.e(TAG, "Cannot start orientation tracking: sensors not available")
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    }
                }

                // Compute orientation from sensor data
                updateOrientationAngles()

                // Emit smoothed orientation data
                trySend(
                    OrientationData(
                        roll = smoothedRoll,
                        pitch = smoothedPitch,
                        azimuth = smoothedAzimuth
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                Log.d(TAG, "Sensor accuracy changed: ${sensor.name}, accuracy: $accuracy")
            }
        }

        // Register sensor listeners
        val accelerometerRegistered = sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        val magnetometerRegistered = sensorManager.registerListener(
            listener,
            magnetometer,
            SensorManager.SENSOR_DELAY_UI
        )

        if (accelerometerRegistered && magnetometerRegistered) {
            Log.i(TAG, "Orientation tracking started")
        } else {
            Log.e(TAG, "Failed to register sensor listeners")
        }

        // Unregister when Flow is cancelled
        awaitClose {
            sensorManager.unregisterListener(listener)
            Log.i(TAG, "Orientation tracking stopped")
        }
    }

    /**
     * Computes device orientation from accelerometer and magnetometer data
     *
     * Uses rotation matrix to convert sensor readings into orientation angles:
     * - orientationAngles[0]: Azimuth (rotation around Z axis)
     * - orientationAngles[1]: Pitch (rotation around X axis)
     * - orientationAngles[2]: Roll (rotation around Y axis)
     */
    private fun updateOrientationAngles() {
        // Compute rotation matrix from accelerometer and magnetometer
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        if (success) {
            // Get orientation angles from rotation matrix
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Convert radians to degrees
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            // Apply exponential moving average for smoothing
            smoothedAzimuth = applySmoothing(smoothedAzimuth, azimuth)
            smoothedPitch = applySmoothing(smoothedPitch, pitch)
            smoothedRoll = applySmoothing(smoothedRoll, roll)
        }
    }

    /**
     * Applies exponential moving average smoothing
     *
     * Formula: smoothed = (smoothed * (1 - α)) + (current * α)
     * where α is the smoothing factor
     *
     * @param previous Previous smoothed value
     * @param current Current raw value
     * @return Smoothed value
     */
    private fun applySmoothing(previous: Float, current: Float): Float {
        return (previous * (1 - SMOOTHING_FACTOR)) + (current * SMOOTHING_FACTOR)
    }
}
