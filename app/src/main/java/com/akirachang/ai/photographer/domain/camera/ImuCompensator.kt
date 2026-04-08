/**
 * ClassName: ImuCompensator
 * Description: Math engine that converts gyroscope angular velocity into 2D pixel displacement
 *              for camera motion compensation (IMU-aided optical flow stabilization).
 * Author: Claude Code
 * Date: 2026-03-18
 * Version: 2.12.0
 *
 * Modifications:
 * - 2026-03-18: Initial creation — Foundation for IMU motion compensation pipeline
 */
package com.akirachang.ai.photographer.domain.camera

import android.util.Log
import com.akirachang.ai.photographer.domain.sensor.GyroscopeData

/**
 * Pure math engine: converts raw gyroscope samples into a pixel-space camera motion vector.
 *
 * ## Coordinate System
 * Android Sensor frame (TYPE_GYROSCOPE):
 * ```
 *   +Y ↑  (top of device)
 *   +X →  (right of device)
 *   +Z ⊙  (out of screen)
 * ```
 * Camera pixel frame (screen space):
 * ```
 *   origin = top-left
 *   +u → (right)
 *   +v ↓ (down)
 * ```
 *
 * ## Projection Formula
 * For a camera with focal length f (pixels) and a rotation dt·ω (radians):
 * ```
 *   Δu = -omega_y · dt · f   // yaw right  → scene shifts left  → Δu < 0
 *   Δv = +omega_x · dt · f   // pitch up   → scene shifts down  → Δv > 0
 * ```
 * Signs follow the right-hand rule combined with screen-space convention (Y-down).
 *
 * ## Thread Safety
 * Not thread-safe. [accumulate] and [consumeCameraMotion] must be called from
 * the same coroutine/thread (or under external synchronization).
 *
 * ## Graceful Degradation
 * If no gyroscope data arrives (hardware absent), [consumeCameraMotion] returns (0f, 0f)
 * — downstream callers require no special handling.
 *
 * ## Usage
 * ```kotlin
 * val compensator = ImuCompensator()
 *
 * // In gyroscope collection coroutine:
 * deviceSensorManager.startGyroscopeTracking().collect { data ->
 *     compensator.accumulate(data)
 * }
 *
 * // In per-frame motion compensation:
 * val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 800f) // f ≈ previewWidth / (2·tan(hFov/2))
 * val correctedX = rawX - dx
 * val correctedY = rawY - dy
 * ```
 */
class ImuCompensator {

    companion object {
        private const val TAG = "ImuCompensator"

        /** Reject dt values outside this range to guard against sensor restart / resume gaps. */
        private const val DT_MIN_SECONDS = 0f
        private const val DT_MAX_SECONDS = 0.1f  // 100ms — beyond this, integration error is unacceptable
    }

    /** Timestamp of the most-recently processed sample (nanoseconds). 0 = uninitialized. */
    private var lastTimestampNs: Long = 0L

    /**
     * Accumulated angular displacement along Y axis (rad·s), projected to horizontal pixel shift.
     * Sign: positive omega_y → camera yaws right → scene shifts left → accumulation is positive,
     * but pixel dx is negated in [consumeCameraMotion].
     */
    private var accumulatedOmegaYDt: Float = 0f

    /**
     * Accumulated angular displacement along X axis (rad·s), projected to vertical pixel shift.
     * Sign: positive omega_x → camera pitches up → scene shifts down in screen coords.
     */
    private var accumulatedOmegaXDt: Float = 0f

    /**
     * Integrates a new [GyroscopeData] sample into the running accumulator.
     *
     * The first call after creation (or [reset]) bootstraps the timestamp only — no
     * displacement is accumulated, preventing a large dt spike on startup.
     *
     * @param data Raw gyroscope sample from [DeviceSensorManager.startGyroscopeTracking]
     */
    fun accumulate(data: GyroscopeData) {
        if (lastTimestampNs == 0L) {
            // Bootstrap: record timestamp, skip integration to avoid huge initial dt
            lastTimestampNs = data.timestamp
            return
        }

        val dtSeconds = (data.timestamp - lastTimestampNs) / 1_000_000_000f
        lastTimestampNs = data.timestamp

        // Guard: reject unreasonable dt (sensor restart, system sleep, negative jitter)
        if (dtSeconds <= DT_MIN_SECONDS || dtSeconds > DT_MAX_SECONDS) {
            Log.w(TAG, "[v2.12.0] Rejected gyro sample: dt=${dtSeconds}s out of [${DT_MIN_SECONDS}, ${DT_MAX_SECONDS}]")
            return
        }

        // Trapezoidal integration approximation (single-sample = rectangular, sufficient at FASTEST rate)
        accumulatedOmegaYDt += data.y * dtSeconds  // yaw   → horizontal
        accumulatedOmegaXDt += data.x * dtSeconds  // pitch → vertical
    }

    /**
     * Returns the camera-motion pixel displacement accumulated since the last call
     * and **atomically resets** the accumulator to zero (consume-once semantics).
     *
     * ## Formula
     * ```
     *   dx = -accumulatedOmegaYDt * f   // yaw right  → scene drifts left
     *   dy = +accumulatedOmegaXDt * f   // pitch up   → scene drifts down
     * ```
     *
     * @param focalLengthPixels Effective focal length in pixels (f).
     *        For most mid-range phones at 16:9, ~800–1200px is a reasonable starting estimate.
     * @return [Pair] of (dx, dy) in screen pixels. Returns (0f, 0f) if no samples accumulated.
     */
    fun consumeCameraMotion(focalLengthPixels: Float): Pair<Float, Float> {
        // Negative sign for yaw: camera rotating right → scene appears to move left in frame
        val dx = -accumulatedOmegaYDt * focalLengthPixels
        // Positive sign for pitch (screen Y-down): camera pitching up → scene appears to move downward
        val dy = accumulatedOmegaXDt * focalLengthPixels

        // Consume — reset accumulator
        accumulatedOmegaYDt = 0f
        accumulatedOmegaXDt = 0f

        return Pair(dx, dy)
    }

    /**
     * Resets all accumulated state.
     *
     * Must be called on:
     * - Camera switch (lens change invalidates all accumulated motion)
     * - App resume (sensor gap makes accumulated dt unreliable)
     * - Focus pipeline reset
     */
    fun reset() {
        lastTimestampNs = 0L
        accumulatedOmegaYDt = 0f
        accumulatedOmegaXDt = 0f
        Log.d(TAG, "[v2.12.0] ImuCompensator reset")
    }
}
