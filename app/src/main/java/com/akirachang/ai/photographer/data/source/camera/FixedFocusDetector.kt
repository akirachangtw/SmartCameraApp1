package com.akirachang.ai.photographer.data.source.camera

import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo

// ── Fixed Focus Detection (L2 §1.3 / Focus_V1 Directive A) ─────────────────
//
// Many front cameras (especially mid-range / older devices) have a fixed-focus
// lens (no VCM / voice-coil motor).  On these lenses:
//   - startFocusAndMetering() is a no-op or throws
//   - isFocusSuccessful always returns false
//   - The AF state machine enters endless FAILED → backoff loops
//
// Detection: CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE
//   - 0.0f → fixed-focus (cannot physically change focus distance)
//   - >0.0f → autofocus capable
//   - null → characteristic unavailable (treat as autofocus to be safe)
//
// When fixed-focus is detected, the FocusOrchestrator should:
//   1. Skip all startFocusAndMetering() HAL calls
//   2. Still allow UI focus indicator drawing (visual feedback only)
//   3. Skip AfTriggerThrottleController (no backoff needed)
//   4. Report AfState.Locked immediately (simulated success)

object FixedFocusDetector {

    private const val TAG = "FixedFocusDetector"

    /**
     * Check whether the given camera lens is fixed-focus (no autofocus).
     *
     * Must be called after camera is bound (CameraInfo is available).
     *
     * @param cameraInfo CameraX CameraInfo from the bound camera instance
     * @return true if the lens is fixed-focus, false if autofocus is available
     *
     * Usage:
     * ```kotlin
     * val isFixed = FixedFocusDetector.isFixedFocus(camera.cameraInfo)
     * if (isFixed) {
     *     // Skip HAL focus calls, show UI-only indicator
     *     Log.i(TAG, "Fixed-focus lens detected — UI-only mode")
     * }
     * ```
     */
    @OptIn(ExperimentalCamera2Interop::class)
    fun isFixedFocus(cameraInfo: CameraInfo): Boolean {
        return try {
            val camera2Info = Camera2CameraInfo.from(cameraInfo)

            // Condition 1: LENS_INFO_MINIMUM_FOCUS_DISTANCE == 0.0f
            val minFocusDist = camera2Info.getCameraCharacteristic(
                CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE
            )
            val minFocusDistFixed = minFocusDist != null && minFocusDist == 0.0f

            // Condition 2: CONTROL_AF_AVAILABLE_MODES == [0] (AF_MODE_OFF only)
            // Queried inline (not via getAfModes()) to stay in the same try-catch.
            // Empty result → conservative: treat as autofocus capable.
            val afModes = camera2Info.getCameraCharacteristic(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
            ) ?: intArrayOf()
            val afModesOnlyOff = afModes.size == 1 && afModes[0] == 0

            // Composite OR: either condition alone is sufficient evidence of fixed-focus
            val isFixed = minFocusDistFixed || afModesOnlyOff

            val reason = when {
                minFocusDistFixed -> "MIN_FOCUS_DIST_ZERO"
                afModesOnlyOff   -> "AF_MODES_ONLY_OFF"
                else             -> "AUTOFOCUS_CAPABLE"
            }

            Log.i(TAG, "[v2.12.0-11] isFixedFocus=$isFixed (reason=$reason), " +
                    "afModes=[${afModes.joinToString()}], minFocusDist=$minFocusDist")
            isFixed
        } catch (e: Exception) {
            Log.w(TAG, "[v2.12.0-11] Failed to query focus capability: ${e.message}")
            false  // Assume autofocus if query fails
        }
    }

    /**
     * Query the available AF modes for diagnostic logging.
     *
     * Useful for debugging HAL behaviour differences between devices.
     * Common modes:
     *   0 = AF_MODE_OFF
     *   1 = AF_MODE_AUTO
     *   2 = AF_MODE_MACRO
     *   3 = AF_MODE_CONTINUOUS_VIDEO
     *   4 = AF_MODE_CONTINUOUS_PICTURE
     */
    @OptIn(ExperimentalCamera2Interop::class)
    fun getAfModes(cameraInfo: CameraInfo): IntArray {
        return try {
            val camera2Info = Camera2CameraInfo.from(cameraInfo)
            val modes = camera2Info.getCameraCharacteristic(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
            ) ?: intArrayOf()
            Log.i(TAG, "Available AF modes: ${modes.joinToString()}")
            modes
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query AF modes: ${e.message}")
            intArrayOf()
        }
    }

    /**
     * Query the hardware support level.
     *
     * Returns:
     *   0 = LIMITED
     *   1 = FULL
     *   2 = LEGACY
     *   3 = LEVEL_3
     *   4 = EXTERNAL
     */
    @OptIn(ExperimentalCamera2Interop::class)
    fun getHardwareLevel(cameraInfo: CameraInfo): Int {
        return try {
            val camera2Info = Camera2CameraInfo.from(cameraInfo)
            val level = camera2Info.getCameraCharacteristic(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
            ) ?: -1
            val name = when (level) {
                0 -> "LIMITED"
                1 -> "FULL"
                2 -> "LEGACY"
                3 -> "LEVEL_3"
                4 -> "EXTERNAL"
                else -> "UNKNOWN($level)"
            }
            Log.i(TAG, "Hardware support level: $name")
            level
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query hardware level: ${e.message}")
            -1
        }
    }
}
