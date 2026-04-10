package com.akirachang.ai.photographer.presentation.camera

import android.graphics.RectF
import android.net.Uri
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.components.CoordinateTransformer
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
import com.akirachang.ai.photographer.presentation.camera.model.HDRMode
import com.akirachang.ai.photographer.domain.model.focus.FocusUiState
import com.akirachang.ai.photographer.domain.model.CompositionResult

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
    // v2.9.10: Traycer tracking state
    val trackedObjectId: Int? = null,  // ML Kit trackingId of tracked target, null if not tracking
    val trackedObjectType: TrackedObjectType? = null,  // FACE or OBJECT, null if not tracking
    // v2.9.9 Day 1-2: Traycer v2 AE/AF Lock state
    val isAEAFLocked: Boolean = false,  // True when AE/AF is locked (long press)
    val lockPoint: Pair<Float, Float>? = null,  // Lock point coordinates for yellow square indicator, null if not locked
    // v2.11.0 Phase 3: EXIF information display state
    val showPhotoInfo: Boolean = false,            // True when PhotoInfoBottomSheet should be shown
    val currentPhotoExifInfo: com.akirachang.ai.photographer.domain.model.ExifInfo? = null,  // EXIF data for display
    // v2.11.1 P0-2: Coordinate system unification
    val contentBounds: RectF = RectF(0f, 0f, 0f, 0f),  // Actual preview content bounds (excludes FIT_CENTER letterbox)
    val coordinateTransformer: CoordinateTransformer? = null,  // Coordinate transformer for Image ↔ Preview ↔ Screen mapping
    // v2.12.0-10: True when tap has no ML Kit subject (Optical Flow / Static generic tap).
    // FocusIndicator uses GENERIC_TAP_FRAME_DP (72dp square) instead of the 48dp fallback.
    val isGenericTapActive: Boolean = false,
    // v2.12.0-05: 4-way AF state machine (Eye-AF throttle + withTimeout)
    val afState: AfState = AfState.Idle,
    // v2.12.0-11: true when current lens is fixed-focus (no VCM / AF hardware)
    val isFixedFocusLens: Boolean = false,
    // v2.12.0-15: New focus pipeline output — V2 pipeline is now the single path (v2.12.0-16).
    val focusUiState: FocusUiState = FocusUiState.EMPTY,
    // Cueshot D2: Aggregated composition rule engine output. Null until first detection frame.
    val compositionResult: CompositionResult? = null,
    // Cueshot D1: High-level PTF state for Rule Engine and coaching UI coordination.
    val ptfState: PtfState = PtfState.Idle,
)

/**
 * PTF (Pass-to-Focus / Tap-to-Focus) state machine — Cueshot D1.
 *
 * State flow: Idle → Focusing → Focused/Failed → Idle (auto-reset after 2s).
 *
 * Note: The existing v2.12.0 focus pipeline (setTapToFocusIntent + FocusStateMachine)
 * drives [CameraState.focusUiState] for overlay rendering. PtfState is the
 * higher-level scene-coordinator state consumed by Rule Engine and UI coaching logic.
 * Both states coexist without conflict.
 */
sealed class PtfState {
    /** No active tap-to-focus in progress. */
    object Idle : PtfState()

    /** CameraX focus metering in progress at (x, y) in view coordinates. */
    data class Focusing(val x: Float, val y: Float) : PtfState()

    /** Focus lock achieved. Auto-resets to Idle after 2s. */
    data class Focused(val x: Float, val y: Float) : PtfState()

    /** Focus metering failed or timed out. Auto-resets to Idle after 2s. */
    data class Failed(val x: Float, val y: Float) : PtfState()
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
        val viewHeight: Int,
        // v2.12.0-18: screen density (dp→px) for zero-delay optimistic tap box sizing
        val density: Float = 3f,
    ) : CameraUiEvent()
    // v2.9.9 Day 1-2: Traycer v2 AE/AF Lock events
    data class LockAEAF(
        val x: Float,
        val y: Float,
        val viewWidth: Int,
        val viewHeight: Int
    ) : CameraUiEvent()
    object UnlockAEAF : CameraUiEvent()
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
