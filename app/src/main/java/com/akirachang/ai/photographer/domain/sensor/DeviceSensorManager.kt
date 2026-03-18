package com.akirachang.ai.photographer.domain.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
 * Hardware sensor wrapper for IMU gyroscope data.
 *
 * Responsibilities:
 * - Register/unregister TYPE_GYROSCOPE listener
 * - Provide graceful degradation when gyroscope is unavailable
 * - Convert SensorEvent into Flow<GyroscopeData>
 */
class DeviceSensorManager(context: Context) {

    companion object {
        private const val TAG = "DomainSensorManager"
    }

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun hasGyroscope(): Boolean = gyroscope != null

    /**
     * Starts gyroscope tracking and emits samples as Flow<GyroscopeData>.
     *
     * Graceful degradation:
     * - If gyroscope hardware is unavailable, this flow closes without throwing.
     *
     * Defensive lifecycle:
     * - Listener is always unregistered in awaitClose to prevent leaks.
     */
    fun startGyroscopeTracking(): Flow<GyroscopeData> = callbackFlow {
        val gyroSensor = gyroscope
        if (gyroSensor == null) {
            Log.w(TAG, "Gyroscope not available, close flow safely")
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    GyroscopeData(
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                        timestamp = event.timestamp,
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        val registered = sensorManager.registerListener(
            listener,
            gyroSensor,
            SensorManager.SENSOR_DELAY_FASTEST,
        )
        if (!registered) {
            Log.e(TAG, "Failed to register gyroscope listener")
            close()
            return@callbackFlow
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
