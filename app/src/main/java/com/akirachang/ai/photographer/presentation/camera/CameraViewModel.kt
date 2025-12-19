package com.akirachang.ai.photographer.presentation.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import com.akirachang.ai.photographer.data.sensor.DeviceSensorManager
import com.akirachang.ai.photographer.data.sensor.OrientationData
// v2.5.2 Phase 7: Removed CameraSelector import - using CameraLens instead
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.domain.usecase.AnalyzeCompositionUseCase
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val analyzeCompositionUseCase: AnalyzeCompositionUseCase,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val sensorManager: DeviceSensorManager,
    private val overlayPreferences: OverlayPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    // Composition analysis state (v1.5.0)
    private val _compositionState = MutableStateFlow<CompositionState>(CompositionState.NoSubject)
    val compositionState: StateFlow<CompositionState> = _compositionState.asStateFlow()

    // Lighting analysis state (v2.2.0)
    private val _lightingState = MutableStateFlow<LightingState>(LightingState.Normal)
    val lightingState: StateFlow<LightingState> = _lightingState.asStateFlow()

    // Auto HDR state (v2.3.0)
    private val _isAutoHDRActive = MutableStateFlow(false)
    val isAutoHDRActive: StateFlow<Boolean> = _isAutoHDRActive.asStateFlow()

    // Device orientation state (v2.1.0)
    private val _orientationState = MutableStateFlow<OrientationData?>(null)
    val orientationState: StateFlow<OrientationData?> = _orientationState.asStateFlow()

    // Overlay visibility preferences (v2.1.0)
    val isGridEnabled: Flow<Boolean> = overlayPreferences.isGridEnabled
    val isCompositionHintEnabled: Flow<Boolean> = overlayPreferences.isCompositionHintEnabled
    val isDetectionOverlayEnabled: Flow<Boolean> = overlayPreferences.isDetectionOverlayEnabled
    val isLevelIndicatorEnabled: Flow<Boolean> = overlayPreferences.isLevelIndicatorEnabled
    val isDirectionalIndicatorEnabled: Flow<Boolean> = overlayPreferences.isDirectionalIndicatorEnabled
    val isAutoHDREnabled: Flow<Boolean> = overlayPreferences.isAutoHDREnabled  // v2.3.0

    // Preview dimensions (v2.0 Phase 2)
    private var previewWidth: Int? = null
    private var previewHeight: Int? = null

    // Detection stabilization (v2.2.1) - prevent flickering
    private var consecutiveFaceDetections = 0
    private var consecutiveNoFaceDetections = 0
    private val MIN_DETECTIONS_TO_SHOW = 3  // Need 3 consecutive detections to show
    private val MIN_NO_DETECTIONS_TO_HIDE = 5  // Need 5 consecutive empty frames to hide

    /**
     * Camera preview bitmap stream (v2.5.1)
     *
     * Provides unified preview regardless of camera source:
     * - Emulator: Test images from StaticImageSource
     * - Device: Live camera from LiveCameraSource
     *
     * IMPORTANT: Must be initialized BEFORE init block to be accessible in observePreviewForAnalysis()
     */
    val previewBitmap: StateFlow<Bitmap?> = cameraRepository.getPreviewStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // v2.5.1: Analyze preview bitmap stream directly (Phase 7)
        // This replaces the old ImageProxy-based analysis
        observePreviewForAnalysis()

        // 訂閱偵測結果 (deprecated - will be removed once bitmap analysis is stable)
        // observeDetectionResults()

        // 訂閱光影分析結果 (v2.2.0)
        // observeLightingResults()

        // 訂閱感測器方向資料 (v2.1.0)
        observeOrientation()

        // ML 偵測需手動啟動（透過 CameraUiEvent.StartAnalysis）
        // 或根據需求自動啟動
        // startAnalysis()  // Deprecated - now using bitmap-based analysis
    }

    /**
     * Observes lighting analysis results from repository (v2.2.0)
     *
     * Subscribes to lighting state updates from SmartCameraAnalyzer
     */
    private fun observeLightingResults() {
        // Cast to implementation to access lightingResults property
        val repositoryImpl = cameraRepository as? com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl

        repositoryImpl?.lightingResults
            ?.onEach { lightingState ->
                _lightingState.value = lightingState
                Log.d(TAG, "Lighting state updated: ${lightingState.javaClass.simpleName}")

                // v2.3.0: Auto HDR handling
                handleAutoHDR(repositoryImpl, lightingState)
            }
            ?.launchIn(viewModelScope)
            ?: Log.w(TAG, "Could not observe lighting results - repository is not CameraRepositoryImpl")
    }

    /**
     * 處理自動 HDR 模式
     * v2.3.0: 逆光場景自動 HDR
     */
    private fun handleAutoHDR(
        repository: com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl,
        lightingState: LightingState
    ) {
        viewModelScope.launch {
            overlayPreferences.isAutoHDREnabled.first().let { autoHDREnabled ->
                if (autoHDREnabled) {
                    val shouldEnable = repository.shouldEnableAutoHDR(lightingState)

                    if (shouldEnable && !_isAutoHDRActive.value) {
                        repository.enableHDRMode()
                        _isAutoHDRActive.value = true
                        Log.i(TAG, "Auto HDR activated")
                    } else if (!shouldEnable && _isAutoHDRActive.value) {
                        repository.disableHDRMode()
                        _isAutoHDRActive.value = false
                        Log.i(TAG, "Auto HDR deactivated")
                    }
                } else if (_isAutoHDRActive.value) {
                    repository.disableHDRMode()
                    _isAutoHDRActive.value = false
                    Log.i(TAG, "Auto HDR disabled via settings")
                }
            }
        }
    }

    /**
     * Observes device orientation from sensors (v2.1.0)
     *
     * Starts orientation tracking and updates the orientation state.
     * Only tracks if sensors are available.
     */
    private fun observeOrientation() {
        if (!sensorManager.areSensorsAvailable()) {
            Log.w(TAG, "Orientation sensors not available - LevelIndicator will be disabled")
            return
        }

        sensorManager.startOrientationTracking()
            .onEach { orientation ->
                _orientationState.value = orientation
            }
            .launchIn(viewModelScope)

        Log.i(TAG, "Orientation tracking initialized")
    }


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

    /**
     * Switch camera between front and back (v2.5.2 Phase 7)
     *
     * Now uses platform-agnostic CameraLens enum instead of CameraSelector
     */
    private fun switchCamera() {
        cameraRepository.switchCamera()
        _state.update {
            it.copy(currentCamera = cameraRepository.getCurrentCamera())
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
                    // v2.2.1: Stabilization logic to prevent flickering overlays
                    val hasFaces = result.faces.isNotEmpty()

                    if (hasFaces) {
                        consecutiveFaceDetections++
                        consecutiveNoFaceDetections = 0

                        // Only update state if we have enough consecutive detections
                        // or if we already have a stable detection
                        if (consecutiveFaceDetections >= MIN_DETECTIONS_TO_SHOW ||
                            _state.value.detectionResult?.faces?.isNotEmpty() == true) {
                            _state.update { it.copy(detectionResult = result) }

                            // Perform composition analysis (v1.5.0)
                            analyzeComposition(result)

                            Log.i(
                                TAG,
                                "Detection result updated: ${result.faces.size} faces, ${result.objects.size} objects"
                            )
                        }
                    } else {
                        consecutiveNoFaceDetections++
                        consecutiveFaceDetections = 0

                        // Only clear if we have enough consecutive empty frames
                        if (consecutiveNoFaceDetections >= MIN_NO_DETECTIONS_TO_HIDE) {
                            _state.update { it.copy(detectionResult = result) }
                            _compositionState.value = CompositionState.NoSubject

                            Log.i(TAG, "Detection cleared: No stable face detected")
                        }
                    }

                    // v2.2.0: Lighting analysis is now handled automatically in SmartCameraAnalyzer
                    // Results are observed via observeLightingResults()
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes preview bitmap stream for ML Kit analysis (v2.5.1 Phase 7)
     *
     * Replaces the old ImageProxy-based analysis with unified Bitmap analysis.
     * This enables test images and live camera to use the same analysis pipeline.
     */
    @OptIn(FlowPreview::class)
    private fun observePreviewForAnalysis() {
        previewBitmap
            .filterNotNull()
            // Throttle analysis to avoid processing every frame
            // Process approximately every 100ms (10 FPS)
            .sample(100)
            .onEach { bitmap ->
                try {
                    // Convert Bitmap to ML Kit InputImage
                    val inputImage = com.google.mlkit.vision.common.InputImage.fromBitmap(
                        bitmap,
                        0  // Rotation: 0 degrees (bitmap already correctly oriented)
                    )

                    // Create detection result by analyzing the bitmap
                    val detectionResult = analyzeImageWithMLKit(inputImage)

                    // Update detection state with stabilization logic
                    if (detectionResult != null) {
                        val hasFaces = detectionResult.faces.isNotEmpty()

                        if (hasFaces) {
                            consecutiveFaceDetections++
                            consecutiveNoFaceDetections = 0

                            // Only update state if we have enough consecutive detections
                            if (consecutiveFaceDetections >= MIN_DETECTIONS_TO_SHOW ||
                                _state.value.detectionResult?.faces?.isNotEmpty() == true) {
                                _state.update { it.copy(detectionResult = detectionResult) }

                                // Perform composition analysis
                                analyzeComposition(detectionResult)

                                Log.i(
                                    TAG,
                                    "Detection result updated: ${detectionResult.faces.size} faces, ${detectionResult.objects.size} objects"
                                )
                            }
                        } else {
                            consecutiveNoFaceDetections++
                            consecutiveFaceDetections = 0

                            // Only clear if we have enough consecutive empty frames
                            if (consecutiveNoFaceDetections >= MIN_NO_DETECTIONS_TO_HIDE) {
                                _state.update { it.copy(detectionResult = detectionResult) }
                                _compositionState.value = CompositionState.NoSubject

                                Log.i(TAG, "Detection cleared: No stable face detected")
                            }
                        }

                        // Analyze lighting (v2.2.0) - Use Bitmap-based analysis with subject context
                        val lightingState = if (detectionResult.faces.isNotEmpty() || detectionResult.objects.isNotEmpty()) {
                            analyzeLightingUseCase.analyzeWithSubject(bitmap, detectionResult)
                        } else {
                            analyzeLightingUseCase(bitmap)
                        }
                        _lightingState.value = lightingState
                        Log.d(TAG, "Lighting state updated: ${lightingState.javaClass.simpleName}")

                        // Handle Auto HDR (v2.3.0)
                        val repositoryImpl = cameraRepository as? com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl
                        if (repositoryImpl != null) {
                            handleAutoHDR(repositoryImpl, lightingState)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to analyze preview bitmap", e)
                }
                // Note: InputImage doesn't require explicit cleanup - it's a lightweight wrapper
                // Memory management handled by ML Kit detectors' lifecycle
            }
            .launchIn(viewModelScope)

        Log.i(TAG, "Preview bitmap analysis initialized (v2.5.1 Phase 7)")
    }

    /**
     * Analyzes InputImage with ML Kit face and object detection
     *
     * @param inputImage ML Kit InputImage from Bitmap
     * @return DetectionResult with faces and objects
     */
    private suspend fun analyzeImageWithMLKit(inputImage: com.google.mlkit.vision.common.InputImage): DetectionResult? {
        return try {
            // Perform face and object detection through repository
            val faces = cameraRepository.detectFaces(inputImage)
            val objects = cameraRepository.detectObjects(inputImage)

            // Convert to domain model
            // v2.5.3: Fix - Add image dimensions and rotation for composition analysis
            DetectionResult(
                faces = faces,
                objects = objects,
                imageWidth = inputImage.width,
                imageHeight = inputImage.height,
                rotationDegrees = inputImage.rotationDegrees
            )
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit analysis failed", e)
            null
        }
    }

    /**
     * Analyzes composition using domain layer UseCase
     *
     * This delegates the composition analysis logic to AnalyzeCompositionUseCase,
     * following Clean Architecture principles.
     *
     * Phase 2: Now passes actual preview dimensions for accurate analysis.
     * v2.3.1: Now passes camera direction for proper mirror coordinate handling.
     * v2.5.2 Phase 7: Uses CameraLens instead of CameraSelector
     *
     * @param detectionResult Current ML Kit detection result
     */
    private fun analyzeComposition(detectionResult: DetectionResult) {
        // Delegate to UseCase (Clean Architecture)
        // Phase 2: Pass actual preview dimensions if available
        // v2.5.2 Phase 7: Use CameraLens for front camera detection
        val isFrontCamera = _state.value.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT
        _compositionState.value = analyzeCompositionUseCase(
            detectionResult = detectionResult,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            isFrontCamera = isFrontCamera
        )
    }

}
