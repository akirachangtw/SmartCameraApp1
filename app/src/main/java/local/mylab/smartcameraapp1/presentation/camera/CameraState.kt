package local.mylab.smartcameraapp1.presentation.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import local.mylab.smartcameraapp1.domain.model.DetectionResult

data class CameraState(
    val isCapturing: Boolean = false,
    val lastCapturedImageUri: Uri? = null,
    val error: String? = null,
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
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
