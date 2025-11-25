package local.mylab.smartcameraapp1.presentation.camera

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.mylab.smartcameraapp1.domain.model.CaptureResult
import local.mylab.smartcameraapp1.domain.model.DetectionResult
import local.mylab.smartcameraapp1.domain.repository.CameraRepository
import local.mylab.smartcameraapp1.presentation.camera.model.CompositionState
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    // Composition analysis state (v1.5.0)
    private val _compositionState = MutableStateFlow<CompositionState>(CompositionState.NoSubject)
    val compositionState: StateFlow<CompositionState> = _compositionState.asStateFlow()

    init {
        // 訂閱偵測結果
        observeDetectionResults()

        // ML 偵測需手動啟動（透過 CameraUiEvent.StartAnalysis）
        // 或根據需求自動啟動
        startAnalysis()
    }

    fun getCameraController() = cameraRepository.getCameraController()

    fun onEvent(event: CameraUiEvent) {
        when (event) {
            is CameraUiEvent.CapturePhoto -> capturePhoto()
            is CameraUiEvent.SwitchCamera -> switchCamera()
            is CameraUiEvent.ToggleFlash -> toggleFlash()
            is CameraUiEvent.StartAnalysis -> startAnalysis()
            is CameraUiEvent.StopAnalysis -> stopAnalysis()
            is CameraUiEvent.ShowError -> showError(event.message)
        }
    }

    private fun capturePhoto() {
        viewModelScope.launch {
            _state.update { it.copy(isCapturing = true, error = null) }

            when (val result = cameraRepository.takePicture()) {
                is CaptureResult.Success -> {
                    _state.update {
                        it.copy(
                            isCapturing = false,
                            lastCapturedImageUri = result.uri,
                            error = null
                        )
                    }
                }
                is CaptureResult.Error -> {
                    _state.update {
                        it.copy(
                            isCapturing = false,
                            error = result.exception.message ?: "拍照失敗"
                        )
                    }
                }
            }
        }
    }

    private fun switchCamera() {
        cameraRepository.switchCamera()
        _state.update {
            it.copy(cameraSelector = cameraRepository.getCurrentCameraSelector())
        }
    }

    private fun toggleFlash() {
        val newFlashMode = when (cameraRepository.getFlashMode()) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
            else -> ImageCapture.FLASH_MODE_OFF
        }
        cameraRepository.setFlashMode(newFlashMode)
        _state.update { it.copy(flashMode = newFlashMode) }
    }

    private fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun startAnalysis() {
        try {
            cameraRepository.startImageAnalysis()
            _state.update { it.copy(isAnalysisEnabled = true) }
            Log.i(TAG, "ML Kit analysis started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ML Kit analysis", e)
            showError("啟動偵測失敗：${e.message}")
        }
    }

    private fun stopAnalysis() {
        try {
            cameraRepository.stopImageAnalysis()
            _state.update {
                it.copy(
                    isAnalysisEnabled = false,
                    detectionResult = null
                )
            }
            Log.i(TAG, "ML Kit analysis stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ML Kit analysis", e)
        }
    }

    private fun observeDetectionResults() {
        cameraRepository.detectionResults
            .onEach { result ->
                if (result != null) {
                    _state.update { it.copy(detectionResult = result) }

                    // Perform composition analysis (v1.5.0)
                    analyzeComposition(result)

                    Log.i(
                        TAG,
                        "Detection result updated: ${result.faces.size} faces, ${result.objects.size} objects"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Analyzes composition based on Rule of Thirds
     *
     * This is a simplified prototype implementation. It checks if the first detected
     * face is positioned near the Rule of Thirds intersection points.
     *
     * Tolerance: ±10% of image width for both horizontal and vertical alignment
     *
     * @param detectionResult Current ML Kit detection result
     */
    private fun analyzeComposition(detectionResult: DetectionResult) {
        val faces = detectionResult.faces

        // No subject detected
        if (faces.isEmpty()) {
            _compositionState.value = CompositionState.NoSubject
            return
        }

        // Analyze first face (simplified for prototype)
        val face = faces.first()
        val boundingBox = face.boundingBox

        // Calculate face center
        val faceCenterX = boundingBox.centerX().toFloat()
        val faceCenterY = boundingBox.centerY().toFloat()

        // Note: detectionResult.imageWidth and imageHeight are not reliable
        // For prototype, we estimate based on bounding box positions
        // In production, we should use PreviewView dimensions
        val estimatedWidth = boundingBox.right * 1.5f  // Rough estimation
        val estimatedHeight = boundingBox.bottom * 1.5f  // Rough estimation

        // Check Rule of Thirds alignment
        val isGoodComposition = checkRuleOfThirds(
            faceCenterX = faceCenterX,
            faceCenterY = faceCenterY,
            imageWidth = estimatedWidth,
            imageHeight = estimatedHeight
        )

        _compositionState.value = if (isGoodComposition) {
            CompositionState.Good
        } else {
            // Provide simple guidance
            val suggestion = generateCompositionSuggestion(
                faceCenterX = faceCenterX,
                imageWidth = estimatedWidth
            )
            CompositionState.NeedsAdjustment(suggestion)
        }
    }

    /**
     * Checks if subject position follows Rule of Thirds
     *
     * Returns true if the subject center is near any of the four
     * Rule of Thirds intersection points (with 10% tolerance).
     *
     * @param faceCenterX X coordinate of face center
     * @param faceCenterY Y coordinate of face center
     * @param imageWidth Estimated image width
     * @param imageHeight Estimated image height
     * @return true if composition is good
     */
    private fun checkRuleOfThirds(
        faceCenterX: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Boolean {
        // Calculate third positions
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f

        // Tolerance: 10% of image width
        val tolerance = imageWidth * 0.1f

        // Check horizontal alignment (left or right third line)
        val isHorizontallyAligned =
            abs(faceCenterX - leftThird) < tolerance ||
            abs(faceCenterX - rightThird) < tolerance

        // Check vertical alignment (top or bottom third line)
        val isVerticallyAligned =
            abs(faceCenterY - topThird) < tolerance ||
            abs(faceCenterY - bottomThird) < tolerance

        // Good composition if aligned both horizontally and vertically
        return isHorizontallyAligned && isVerticallyAligned
    }

    /**
     * Generates simple composition suggestion
     *
     * Provides basic guidance on which direction to move the camera.
     *
     * @param faceCenterX X coordinate of face center
     * @param imageWidth Estimated image width
     * @return Suggestion message
     */
    private fun generateCompositionSuggestion(
        faceCenterX: Float,
        imageWidth: Float
    ): String {
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f

        return when {
            faceCenterX < leftThird -> "Adjust Position"
            faceCenterX > rightThird -> "Adjust Position"
            else -> "Adjust Position"
        }
    }
}
