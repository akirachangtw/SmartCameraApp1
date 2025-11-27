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
import local.mylab.smartcameraapp1.domain.usecase.AnalyzeCompositionUseCase
import local.mylab.smartcameraapp1.presentation.camera.model.CompositionState
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val analyzeCompositionUseCase: AnalyzeCompositionUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    // Composition analysis state (v1.5.0)
    private val _compositionState = MutableStateFlow<CompositionState>(CompositionState.NoSubject)
    val compositionState: StateFlow<CompositionState> = _compositionState.asStateFlow()

    // Preview dimensions (v2.0 Phase 2)
    private var previewWidth: Int? = null
    private var previewHeight: Int? = null

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

    /**
     * Updates preview dimensions for accurate composition analysis
     *
     * This should be called when PreviewView size changes.
     * Enables accurate Rule of Thirds calculation without estimation.
     *
     * @param width Preview width in pixels
     * @param height Preview height in pixels
     */
    fun updatePreviewSize(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
        Log.d(TAG, "Preview size updated: ${width}x${height}")
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
     * Analyzes composition using domain layer UseCase
     *
     * This delegates the composition analysis logic to AnalyzeCompositionUseCase,
     * following Clean Architecture principles.
     *
     * Phase 2: Now passes actual preview dimensions for accurate analysis.
     *
     * @param detectionResult Current ML Kit detection result
     */
    private fun analyzeComposition(detectionResult: DetectionResult) {
        // Delegate to UseCase (Clean Architecture)
        // Phase 2: Pass actual preview dimensions if available
        _compositionState.value = analyzeCompositionUseCase(
            detectionResult = detectionResult,
            previewWidth = previewWidth,
            previewHeight = previewHeight
        )
    }
}
