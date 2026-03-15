package com.akirachang.ai.photographer.presentation.camera

import android.graphics.RectF
import android.net.Uri
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.components.CoordinateTransformer
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
import com.akirachang.ai.photographer.presentation.camera.model.HDRMode
import com.akirachang.ai.photographer.domain.model.focus.FocusUiState

/**
 * Camera UI State
 *
 * v2.5.2 Phase 7: Replaced CameraSelector with CameraLens for platform-agnostic state
 * v2.6.1: Added HDR mode control
 * v2.9.9: Added focus state for Tap-to-Focus
 * v2.9.10: Added tracking state for Traycer object tracking
 * v2.9.9 Day 1-2: Added AE/AF Lock state for Traycer v2
 * v2.x.x Phase 1: Added Optical Flow Point Tracking state
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
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.RATIO_16_9,  // v2.10.0: Aspect ratio toggle (4:3/16:9)
    val isRebindingCamera: Boolean = false,  // v2.10.0: True when rebinding camera after aspect ratio change
    // v2.9.9: Focus state
    val focusPoint: Pair<Float, Float>? = null,  // (x, y) coordinates in pixels, null if no active focus
    val isFocusing: Boolean = false,  // True while focus animation is showing
    val focusSuccess: Boolean? = null,  // True if last focus succeeded, false if failed, null if no focus yet
    // v2.9.10: Traycer tracking state
    val trackedObjectId: Int? = null,  // ML Kit trackingId of tracked target, null if not tracking
    val trackedObjectType: TrackedObjectType? = null,  // FACE or OBJECT, null if not tracking
    // v2.9.9 Day 1-2: Traycer v2 AE/AF Lock state
    val isAEAFLocked: Boolean = false,  // True when AE/AF is locked (long press)
    val lockPoint: Pair<Float, Float>? = null,  // Lock point coordinates for yellow square indicator, null if not locked
    // v2.9.9 Day 1-2 Optimize: Focus indicator fade-out animation
    // v2.12.0-16 SB-F3: @Deprecated — legacy FocusIndicator fade animation.
    // V2 pipeline uses FocusOverlay.kt with Compose animateFloatAsState instead.
    // Keep for legacy path compatibility until FOCUS_PIPELINE_V2 is permanently enabled.
    val focusIndicatorFading: Boolean = false,  // True when fading out
    // v2.12.0-16 SB-F3: @Deprecated — legacy FocusIndicator fade timestamp.
    val focusIndicatorFadeStartTime: Long = 0L,  // Timestamp when fade started
    // v2.x.x Phase 1: Optical Flow Point Tracking state
    val trackedPoint: Pair<Float, Float>? = null,  // Tracked point coordinates (ImageAnalysis coordinate system)
    val trackedPointActive: Boolean = false,       // True when optical flow tracking is active
    val trackedPointQuality: Float = 0f,           // Tracking quality (0.0-1.0), used for confidence threshold
    val trackedPointLost: Boolean = false,         // True when tracking is lost (quality too low or out of bounds)
    // v2.11.0 Phase 3: EXIF information display state
    val showPhotoInfo: Boolean = false,            // True when PhotoInfoBottomSheet should be shown
    val currentPhotoExifInfo: com.akirachang.ai.photographer.domain.model.ExifInfo? = null,  // EXIF data for display
    // v2.11.1 P0-2: Coordinate system unification
    val contentBounds: RectF = RectF(0f, 0f, 0f, 0f),  // Actual preview content bounds (excludes FIT_CENTER letterbox)
    val coordinateTransformer: CoordinateTransformer? = null,  // Coordinate transformer for Image ↔ Preview ↔ Screen mapping
    // v2.12.0-06: Adaptive focus frame dimensions in screen pixels (0f = use default size).
    // Separate W/H replaces the single focusFrameSizePx to enable non-square (rectangle) frames.
    // v2.12.0-16 SB-F3: @Deprecated — legacy FocusIndicator frame size.
    // V2 pipeline derives frame size from FocusTarget.rectView via GeometryMapper.
    val focusFrameWidthPx: Float = 0f,
    val focusFrameHeightPx: Float = 0f,
    // v2.12.0-10: True when tap has no ML Kit subject (Optical Flow / Static generic tap).
    // FocusIndicator uses GENERIC_TAP_FRAME_DP (72dp square) instead of the 48dp fallback.
    val isGenericTapActive: Boolean = false,
    // v2.12.0-05: 4-way AF state machine (Eye-AF throttle + withTimeout)
    val afState: AfState = AfState.Idle,
    // v2.12.0-11: true when current lens is fixed-focus (no VCM / AF hardware)
    val isFixedFocusLens: Boolean = false,
    // v2.12.0-15: New focus pipeline output (Step 5b)
    // When newFocusPipelineEnabled=true, UI should read this instead of legacy focus fields.
    val focusUiState: FocusUiState = FocusUiState.EMPTY,
) {
    // v2.12.0-12: UDF-clean derived state for FocusIndicator
    // AE/AF long-press lock ONLY. Eye-AF success (afState=Locked) uses Cyan, NOT Yellow.
    // v2.12.0-16 SB-F3: @Deprecated — legacy FocusIndicator lock state proxy.
    // V2 pipeline uses ExposureLockState from DefaultFocusStateMachine instead.
    // Kept for CameraState.isAEAFLocked backward compatibility.
    val isFocusIndicatorLocked: Boolean
        get() = isAEAFLocked
}

sealed class CameraUiEvent {
    object CapturePhoto : CameraUiEvent()
    object SwitchCamera : CameraUiEvent()
    object ToggleFlash : CameraUiEvent()
    object ToggleHDR : CameraUiEvent()  // v2.6.1: Toggle HDR mode (AUTO → ON → OFF → AUTO)
    // v2.11.0 Phase 2: ToggleAspectRatio removed - aspect ratio now managed in Settings
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
    // v2.x.x Phase 5: Optical Flow tracking - set preview size for coordinate mapping
    data class SetPreviewSize(val width: Int, val height: Int) : CameraUiEvent()
    // v2.11.0 Phase 3: EXIF information display events
    data class ShowPhotoInfo(val uri: Uri) : CameraUiEvent()
    object DismissPhotoInfo : CameraUiEvent()
}

/**
 * Tracked object type for Traycer continuous tracking
 * v2.9.10: Distinguishes between face and object tracking
 */
enum class TrackedObjectType {
    FACE,    // Tracking a face (more stable tracking)
    OBJECT   // Tracking an object
}
