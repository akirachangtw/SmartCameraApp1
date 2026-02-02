package com.akirachang.ai.photographer.presentation.camera

import android.net.Uri
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.model.HDRMode

/**
 * Camera UI State
 *
 * v2.5.2 Phase 7: Replaced CameraSelector with CameraLens for platform-agnostic state
 * v2.6.1: Added HDR mode control
 * v2.9.9: Added focus state for Tap-to-Focus
 * v2.9.10: Added tracking state for Traycer object tracking
 * v2.9.9 Day 1-2: Added AE/AF Lock state for Traycer v2
 */
data class CameraState(
    val isCapturing: Boolean = false,
    val lastCapturedImageUri: Uri? = null,
    val error: String? = null,
    val currentCamera: CameraLens = CameraLens.BACK,  // v2.5.2: Platform-agnostic camera selection
    val flashMode: Int = 0,
    val detectionResult: DetectionResult? = null,
    val isAnalysisEnabled: Boolean = false,
    val hdrMode: HDRMode = HDRMode.AUTO,  // v2.6.1: Manual HDR control
    // v2.9.9: Focus state
    val focusPoint: Pair<Float, Float>? = null,  // (x, y) coordinates in pixels, null if no active focus
    val isFocusing: Boolean = false,  // True while focus animation is showing
    val focusSuccess: Boolean? = null,  // True if last focus succeeded, false if failed, null if no focus yet
    // v2.9.10: Traycer tracking state
    val trackedObjectId: Int? = null,  // ML Kit trackingId of tracked target, null if not tracking
    val trackedObjectType: TrackedObjectType? = null,  // FACE or OBJECT, null if not tracking
    // v2.9.9 Day 1-2: Traycer v2 AE/AF Lock state
    val isAEAFLocked: Boolean = false,  // True when AE/AF is locked (long press)
    val lockPoint: Pair<Float, Float>? = null  // Lock point coordinates for yellow square indicator, null if not locked
)

sealed class CameraUiEvent {
    object CapturePhoto : CameraUiEvent()
    object SwitchCamera : CameraUiEvent()
    object ToggleFlash : CameraUiEvent()
    object ToggleHDR : CameraUiEvent()  // v2.6.1: Toggle HDR mode (AUTO → ON → OFF → AUTO)
    object StartAnalysis : CameraUiEvent()
    object StopAnalysis : CameraUiEvent()
    data class ShowError(val message: String) : CameraUiEvent()
    // v2.9.9: Tap-to-Focus event
    data class FocusOnPoint(
        val x: Float,
        val y: Float,
        val viewWidth: Int,
        val viewHeight: Int
    ) : CameraUiEvent()
    // v2.9.9 Day 1-2: Traycer v2 AE/AF Lock events
    data class LockAEAF(
        val x: Float,
        val y: Float,
        val viewWidth: Int,
        val viewHeight: Int
    ) : CameraUiEvent()
    object UnlockAEAF : CameraUiEvent()
}

/**
 * Tracked object type for Traycer continuous tracking
 * v2.9.10: Distinguishes between face and object tracking
 */
enum class TrackedObjectType {
    FACE,    // Tracking a face (more stable tracking)
    OBJECT   // Tracking an object
}
