package com.akirachang.ai.photographer.presentation.camera

import android.net.Uri
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.DetectionResult

/**
 * Camera UI State
 *
 * v2.5.2 Phase 7: Replaced CameraSelector with CameraLens for platform-agnostic state
 */
data class CameraState(
    val isCapturing: Boolean = false,
    val lastCapturedImageUri: Uri? = null,
    val error: String? = null,
    val currentCamera: CameraLens = CameraLens.BACK,  // v2.5.2: Platform-agnostic camera selection
    val flashMode: Int = 0,
    val detectionResult: DetectionResult? = null,
    val isAnalysisEnabled: Boolean = false
)

sealed class CameraUiEvent {
    object CapturePhoto : CameraUiEvent()
    object SwitchCamera : CameraUiEvent()
    object ToggleFlash : CameraUiEvent()
    object StartAnalysis : CameraUiEvent()
    object StopAnalysis : CameraUiEvent()
    data class ShowError(val message: String) : CameraUiEvent()
}
