package com.akirachang.ai.photographer.presentation.camera

import android.graphics.Bitmap
import android.os.Build
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import com.akirachang.ai.photographer.data.sensor.DeviceSensorManager
import com.akirachang.ai.photographer.data.sensor.OrientationData
// v2.5.2 Phase 7: Removed CameraSelector import - using CameraLens instead
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.model.GuidelineAnalysisResult
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.domain.usecase.AnalyzeCompositionUseCase
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.akirachang.ai.photographer.data.source.analysis.GuidelineAnalyzer
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.domain.config.FeatureFlags
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val analyzeCompositionUseCase: AnalyzeCompositionUseCase,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val sensorManager: DeviceSensorManager,
    private val overlayPreferences: OverlayPreferences,
    private val capabilityDetector: com.akirachang.ai.photographer.data.source.camera.CameraCapabilityDetector
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"

        // Phase 0: A/B Test Flags for Graphics Memory Investigation (v2.8.6)
        // Phase 0 completed - Test A confirmed root cause, proceeding to Phase 2
        // Test A: Disable bitmap preview to isolate Graphics memory source
        // Test B: Disable overlays to check if overlay rendering causes leak
        // Test C: Disable analysis to check if ML/composition analysis causes leak
        const val DEBUG_DISABLE_BITMAP_PREVIEW = false  // Test A switch - DISABLED (Phase 0 complete)
        const val DEBUG_DISABLE_OVERLAYS = false         // Test B switch
        const val DEBUG_DISABLE_ANALYSIS = false         // Test C switch
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

    // Guideline analysis results (v2.7.0 Phase 2 Sprint 4)
    private val _guidelineResults = MutableStateFlow<GuidelineAnalysisResult?>(null)
    val guidelineResults: StateFlow<GuidelineAnalysisResult?> = _guidelineResults.asStateFlow()

    // Preview dimensions (v2.0 Phase 2)
    private var previewWidth: Int? = null
    private var previewHeight: Int? = null

    // Tracking state management (v2.9.10: Traycer)
    private var targetLostFrameCount = 0
    private val TARGET_LOST_THRESHOLD = 10  // Stop tracking after 10 consecutive frames without target

    // v2.x.x Phase 7: EMA removed — LK quality avg 0.91 after aspect-ratio + FPS fixes,
    // noise jitter no longer significant.  Direct raw coordinates used for zero-lag tracking.

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

    /**
     * FPS performance metrics stream (v2.8.4 Quick Win)
     *
     * Real-time frame rate monitoring for performance validation.
     * Null when camera is not active or metrics not available.
     */
    val currentFps: StateFlow<Float?> = cameraRepository.getFpsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // GuidelineAnalyzer instance (v2.7.0 Phase 2 Sprint 4)
    // Created once and reused to avoid performance overhead
    // Note: Callback is unused because we use synchronous analyzeBitmap() instead
    private val guidelineAnalyzer = GuidelineAnalyzer { /* unused callback */ }

    init {
        // v2.5.1: Analyze preview bitmap stream directly (Phase 7)
        // This replaces the old ImageProxy-based analysis
        observePreviewForAnalysis()

        // v2.7.0 Phase 2 Sprint 4: Observe preview for guideline analysis (OpenCV)
        observePreviewForGuidelineAnalysis()

        // 訂閱偵測結果 (deprecated - will be removed once bitmap analysis is stable)
        // observeDetectionResults()

        // 訂閱光影分析結果 (v2.2.0)
        // observeLightingResults()

        // 訂閱感測器方向資料 (v2.1.0)
        observeOrientation()

        // ML 偵測需手動啟動（透過 CameraUiEvent.StartAnalysis）
        // 或根據需求自動啟動
        // startAnalysis()  // Deprecated - now using bitmap-based analysis

        // v2.x.x Phase 5: Setup optical flow tracking callback
        setupOpticalFlowTracking()

        // v2.x.x Phase 7: High-rate optical flow feed (bypass sample(100) throttle)
        observePreviewForOpticalFlow()

        // v2.8.5: Pixel 9a memory mitigation warning
        if (Build.MODEL == "Pixel 9a") {
            showError(
                "Pixel 9a 記憶體優化模式:\n" +
                "• AI 分析頻率已降低至 2 FPS\n" +
                "• 相機解析度已調整至 480x480\n" +
                "• 如遇記憶體問題請重啟 App"
            )
        }
    }

    /**
     * Setup optical flow tracking callback
     * v2.x.x Phase 5: Optical flow point tracking integration
     *
     * Sets up the callback to receive tracking results from SmartCameraAnalyzer.
     * Updates CameraState with tracking point coordinates and quality.
     */
    private fun setupOpticalFlowTracking() {
        if (!FeatureFlags.enableOpticalFlowTracking) {
            Log.d(TAG, "Optical flow tracking disabled by feature flag")
            return
        }

        cameraRepository.setOpticalFlowCallback { trackingResult ->
            if (trackingResult.isValid) {
                val pointX = trackingResult.point.x.toFloat()
                val pointY = trackingResult.point.y.toFloat()

                // Update tracking state with raw LK coordinates (zero-lag)
                // Note: Do NOT reset focusIndicatorFading here — fade sequence controls it
                _state.update {
                    it.copy(
                        focusPoint = Pair(pointX, pointY),
                        trackedPointActive = true,
                        trackedPointQuality = trackingResult.quality,
                        trackedPointLost = false
                    )
                }
                Log.v(TAG, "Optical flow tracking updated: point=(${pointX}, ${pointY}), quality=${trackingResult.quality}")

            } else {
                // Tracking lost — fade out then clear
                _state.update {
                    it.copy(
                        trackedPointActive = false,
                        trackedPointLost = true,
                        focusIndicatorFading = true  // Fade to Alpha 0.4
                    )
                }
                Log.w(TAG, "Optical flow tracking lost — starting fade-out")

                // Clear focus indicator after 1.5 seconds (1s fade hold + 0.5s animation)
                viewModelScope.launch {
                    delay(1500)
                    _state.update {
                        it.copy(
                            focusPoint = null,
                            focusIndicatorFading = false,
                            trackedPointLost = false
                        )
                    }
                    cameraRepository.stopOpticalFlowTracking()
                    Log.i(TAG, "Optical flow tracking stopped — focus indicator cleared")
                }
            }
        }
        Log.i(TAG, "Optical flow tracking callback registered")
    }

    /**
     * Manual stop optical flow tracking (called from other contexts if needed)
     * v2.x.x Phase 5: Stop tracking and clear state
     */
    private fun stopOpticalFlowTracking() {
        cameraRepository.stopOpticalFlowTracking()
        Log.i(TAG, "Optical flow tracking stopped (manual)")
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
     * v2.6.1: Only applies in AUTO mode - manual ON/OFF modes bypass this logic
     */
    private fun handleAutoHDR(
        repository: com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl,
        lightingState: LightingState
    ) {
        // v2.6.1: Skip auto HDR if in manual mode (ON or OFF)
        val currentHDRMode = _state.value.hdrMode
        if (!currentHDRMode.isAuto) {
            Log.d(TAG, "Auto HDR skipped - manual mode active: ${currentHDRMode.displayName}")
            return
        }

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
            is CameraUiEvent.ToggleHDR -> toggleHDR()  // v2.6.1
            is CameraUiEvent.StartAnalysis -> startAnalysis()
            is CameraUiEvent.StopAnalysis -> stopAnalysis()
            is CameraUiEvent.ShowError -> showError(event.message)
            is CameraUiEvent.FocusOnPoint -> focusOnPoint(event.x, event.y, event.viewWidth, event.viewHeight)  // v2.9.9
            is CameraUiEvent.LockAEAF -> lockAEAF(event.x, event.y, event.viewWidth, event.viewHeight)  // v2.9.9 Day 1-2
            is CameraUiEvent.UnlockAEAF -> unlockAEAF()  // v2.9.9 Day 1-2
            is CameraUiEvent.SetPreviewSize -> setPreviewSize(event.width, event.height)  // v2.x.x Phase 5
        }
    }

    /**
     * Set preview size for optical flow coordinate mapping
     * v2.x.x Phase 5: Required for PreviewView ↔ ImageAnalysis coordinate conversion
     */
    private fun setPreviewSize(width: Int, height: Int) {
        if (FeatureFlags.enableOpticalFlowTracking) {
            cameraRepository.setPreviewSize(width, height)
            Log.d(TAG, "Preview size set for optical flow: ${width}x${height}")
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
        // v2.6.1 Fix: Execute camera switching asynchronously
        // switchCamera() internally handles Main thread requirement for CameraX
        viewModelScope.launch {
            // v2.9.10: Clear tracking state before switching camera
            // v2.9.9 Day 1-2: Clear AE/AF lock state before switching camera
            _state.update {
                it.copy(
                    trackedObjectId = null,
                    trackedObjectType = null,
                    focusPoint = null,
                    isFocusing = false,
                    focusSuccess = null,
                    isAEAFLocked = false,
                    lockPoint = null
                )
            }

            // Cancel focus metering if locked
            cameraRepository.cancelFocusAndMetering()

            cameraRepository.switchCamera()
            _state.update {
                it.copy(currentCamera = cameraRepository.getCurrentCamera())
            }
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

    /**
     * Toggle HDR mode (v2.6.1)
     *
     * Cycles through: AUTO → ON → OFF → AUTO
     *
     * Behavior by mode:
     * - AUTO: Enable auto HDR based on lighting analysis (existing behavior)
     * - ON: Force enable HDR, disable auto HDR logic
     * - OFF: Force disable HDR, disable auto HDR logic
     */
    private fun toggleHDR() {
        val newMode = _state.value.hdrMode.next()
        _state.update { it.copy(hdrMode = newMode) }

        // Apply HDR mode immediately
        applyHDRMode(newMode)

        Log.i(TAG, "HDR mode changed: ${newMode.displayName}")
    }

    /**
     * Apply HDR mode to camera (v2.6.1)
     *
     * @param mode Target HDR mode
     */
    private fun applyHDRMode(mode: com.akirachang.ai.photographer.presentation.camera.model.HDRMode) {
        val repository = cameraRepository as? com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl
        if (repository == null) {
            Log.w(TAG, "Cannot apply HDR mode - repository is not CameraRepositoryImpl")
            return
        }

        when (mode) {
            com.akirachang.ai.photographer.presentation.camera.model.HDRMode.AUTO -> {
                // Re-enable auto HDR logic
                // Let handleAutoHDR() manage HDR based on lighting state
                val currentLighting = _lightingState.value
                handleAutoHDR(repository, currentLighting)
                Log.i(TAG, "HDR mode: AUTO - Re-evaluating based on lighting state")
            }
            com.akirachang.ai.photographer.presentation.camera.model.HDRMode.ON -> {
                // Force enable HDR
                repository.enableHDRMode()
                _isAutoHDRActive.value = true  // Update state for UI consistency
                Log.i(TAG, "HDR mode: ON - Force enabled")
            }
            com.akirachang.ai.photographer.presentation.camera.model.HDRMode.OFF -> {
                // Force disable HDR
                repository.disableHDRMode()
                _isAutoHDRActive.value = false  // Update state for UI consistency
                Log.i(TAG, "HDR mode: OFF - Force disabled")
            }
        }
    }

    private fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Find tapped face or object for tracking
     * v2.9.10: Traycer collision detection
     *
     * @param x Tap coordinate X (PreviewView coordinates)
     * @param y Tap coordinate Y (PreviewView coordinates)
     * @return Pair<trackingId, type> or null if no target found
     */
    private fun findTappedTarget(x: Float, y: Float): Pair<Int, TrackedObjectType>? {
        val detectionResult = _state.value.detectionResult ?: return null

        // 🔍 DIAGNOSTIC: Log detection result image dimensions
        Log.d(TAG, "🔍 [Coordinate Debug] DetectionResult: ${detectionResult.imageWidth}x${detectionResult.imageHeight}, rotation=${detectionResult.rotationDegrees}°")
        Log.d(TAG, "🔍 [Coordinate Debug] Faces: ${detectionResult.faces.size}, Objects: ${detectionResult.objects.size}")

        // Log all face bounding boxes
        detectionResult.faces.forEachIndexed { index, face ->
            Log.d(TAG, "🔍 [Coordinate Debug] Face[$index] BoundingBox: ${face.boundingBox}")
        }

        // Log all object bounding boxes
        detectionResult.objects.forEachIndexed { index, obj ->
            Log.d(TAG, "🔍 [Coordinate Debug] Object[$index] BoundingBox: ${obj.boundingBox}")
        }

        // Priority 1: Check faces (more stable tracking)
        detectionResult.faces.forEachIndexed { index, face ->
            val contains = face.boundingBox.contains(x.toInt(), y.toInt())
            Log.d(TAG, "🔍 [Coordinate Debug] Face[$index] contains($x, $y)? $contains")
            if (contains) {
                val trackingId = face.trackingId ?: (index + 10000)  // Fallback ID: 10000+
                Log.i(TAG, "✅ Tapped on face: trackingId=$trackingId, boundingBox=${face.boundingBox}")
                return Pair(trackingId, TrackedObjectType.FACE)
            }
        }

        // Priority 2: Check objects
        detectionResult.objects.forEachIndexed { index, obj ->
            val contains = obj.boundingBox.contains(x.toInt(), y.toInt())
            Log.d(TAG, "🔍 [Coordinate Debug] Object[$index] contains($x, $y)? $contains")
            if (contains) {
                val trackingId = obj.trackingId ?: (index + 20000)  // Fallback ID: 20000+
                Log.i(TAG, "✅ Tapped on object: trackingId=$trackingId, boundingBox=${obj.boundingBox}")
                return Pair(trackingId, TrackedObjectType.OBJECT)
            }
        }

        Log.d(TAG, "🔍 [Coordinate Debug] No target found at ($x, $y)")
        return null
    }

    /**
     * Update tracked object position
     * v2.9.10: Traycer continuous tracking
     *
     * Called after each new DetectionResult to update focus indicator position
     */
    private fun updateTrackedObjectPosition(detectionResult: DetectionResult) {
        val trackedId = _state.value.trackedObjectId ?: return
        val trackedType = _state.value.trackedObjectType ?: return

        // Find target by type
        val targetBoundingBox: android.graphics.Rect? = when (trackedType) {
            TrackedObjectType.FACE -> {
                detectionResult.faces.firstOrNull { face ->
                    (face.trackingId ?: -1) == trackedId
                }?.boundingBox
            }
            TrackedObjectType.OBJECT -> {
                detectionResult.objects.firstOrNull { obj ->
                    (obj.trackingId ?: -1) == trackedId
                }?.boundingBox
            }
        }

        if (targetBoundingBox != null) {
            // Target found, update position
            val centerX = targetBoundingBox.exactCenterX()
            val centerY = targetBoundingBox.exactCenterY()

            // Prevent excessive updates: only update if position changed > 5 pixels
            val currentFocusPoint = _state.value.focusPoint
            val shouldUpdate = currentFocusPoint == null ||
                kotlin.math.abs(currentFocusPoint.first - centerX) > 5f ||
                kotlin.math.abs(currentFocusPoint.second - centerY) > 5f

            if (shouldUpdate) {
                _state.update { it.copy(focusPoint = Pair(centerX, centerY)) }
                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Tracking update: ID=$trackedId, position=($centerX, $centerY)")
                }
            }

            // Reset lost frame counter
            targetLostFrameCount = 0

        } else {
            // Target not found, increment lost counter
            targetLostFrameCount++

            if (targetLostFrameCount >= TARGET_LOST_THRESHOLD) {
                Log.i(TAG, "Tracking lost: ID=$trackedId after $targetLostFrameCount frames")
                _state.update {
                    it.copy(
                        trackedObjectId = null,
                        trackedObjectType = null,
                        focusPoint = null,
                        isFocusing = false,
                        focusSuccess = null
                    )
                }
                targetLostFrameCount = 0
            }
        }
    }

    /**
     * Focus on a specific point in the preview
     * v2.9.9: Tap-to-Focus implementation
     * v2.9.10: Traycer tracking integration
     *
     * @param x X coordinate in pixels
     * @param y Y coordinate in pixels
     * @param viewWidth View width in pixels
     * @param viewHeight View height in pixels
     */
    private fun focusOnPoint(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        viewModelScope.launch {
            try {
                // 🔍 DIAGNOSTIC: Log tap coordinates and view size
                Log.d(TAG, "🔍 [Coordinate Debug] Tap: ($x, $y), View: ${viewWidth}x${viewHeight}")

                // v2.9.9 Day 1-2: Unlock AE/AF if locked (tap to unlock)
                if (_state.value.isAEAFLocked) {
                    unlockAEAF()
                    Log.i(TAG, "AE/AF unlocked by tap")
                    return@launch  // Don't start new focus, just unlock
                }

                // v2.9.10: Find tapped target for tracking
                val tappedTarget = findTappedTarget(x, y)

                if (tappedTarget != null && FeatureFlags.enableFocusTracking) {
                    // ML Kit Tracking mode: Start continuous tracking
                    val (trackingId, targetType) = tappedTarget
                    _state.update {
                        it.copy(
                            focusPoint = Pair(x, y),
                            isFocusing = true,
                            focusSuccess = null,
                            trackedObjectId = trackingId,
                            trackedObjectType = targetType,
                            focusIndicatorFading = false  // Reset fade animation
                        )
                    }
                    Log.i(TAG, "ML Kit tracking started: ID=$trackingId, type=$targetType")

                } else if (tappedTarget == null && FeatureFlags.enableOpticalFlowTracking) {
                    // v2.x.x Phase 5: Optical Flow tracking mode (tap-to-track arbitrary point)
                    _state.update {
                        it.copy(
                            focusPoint = Pair(x, y),
                            isFocusing = true,
                            focusSuccess = null,
                            trackedObjectId = null,
                            trackedObjectType = null,
                            trackedPointActive = true,
                            trackedPointLost = false,
                            focusIndicatorFading = false  // Reset fade animation
                        )
                    }

                    // Start optical flow tracking
                    cameraRepository.startOpticalFlowTracking(x, y)
                    Log.i(TAG, "Optical flow tracking started at ($x, $y)")

                } else {
                    // Static focus mode: Traditional tap-to-focus
                    _state.update {
                        it.copy(
                            focusPoint = Pair(x, y),
                            isFocusing = true,
                            focusSuccess = null,
                            trackedObjectId = null,
                            trackedObjectType = null,
                            focusIndicatorFading = false  // Reset fade animation
                        )
                    }
                    Log.d(TAG, "Static focus mode at ($x, $y)")
                }

                // Execute CameraX focus (both modes)
                val result = cameraRepository.focusOnPoint(x, y, viewWidth, viewHeight)

                if (result.isSuccess) {
                    _state.update { it.copy(isFocusing = false, focusSuccess = true) }
                    Log.d(TAG, "Focus successful at ($x, $y)")

                    // ML Kit tracking: Alpha managed by updateTrackedObjectPosition (skip fade)
                    // Optical Flow + Static: Alpha 1.0 for 1s → fade 500ms → Alpha 0.4 (persistent)
                    // focusIndicatorFading=true 後保持 Alpha 0.4，直到下次點擊重置
                    if (_state.value.trackedObjectId == null) {
                        delay(1000)  // Keep for 1 second
                        _state.update { it.copy(focusIndicatorFading = true, focusIndicatorFadeStartTime = System.currentTimeMillis()) }
                        delay(500)   // Fade out for 500ms
                        // v2.x.x: Keep focusPoint visible at alpha 0.4 (persistent until next tap)
                        // Keep focusIndicatorFading=true to maintain alpha 0.4 (reset on next tap)
                    }

                } else {
                    _state.update { it.copy(isFocusing = false, focusSuccess = false) }
                    Log.w(TAG, "Focus failed at ($x, $y): ${result.exceptionOrNull()?.message}")
                    delay(500)
                    _state.update {
                        it.copy(
                            focusPoint = null,
                            focusSuccess = null,
                            trackedObjectId = null,
                            trackedObjectType = null
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Focus error", e)
                _state.update {
                    it.copy(
                        isFocusing = false,
                        focusSuccess = false,
                        trackedObjectId = null,
                        trackedObjectType = null
                    )
                }
                delay(500)
                _state.update { it.copy(focusPoint = null, focusSuccess = null) }
            }
        }
    }

    /**
     * Lock AE/AF at specified point (long press)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock
     *
     * Locks auto exposure and auto focus at the specified point.
     * Creates a persistent focus metering action without auto-cancel.
     * Displays yellow square indicator at lock point.
     */
    private fun lockAEAF(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        viewModelScope.launch {
            try {
                Log.i(TAG, "Locking AE/AF at ($x, $y)")

                // Clear tracking state if active (lock mode is exclusive)
                val wasTracking = _state.value.trackedObjectId != null
                if (wasTracking) {
                    _state.update {
                        it.copy(
                            trackedObjectId = null,
                            trackedObjectType = null
                        )
                    }
                    Log.i(TAG, "Tracking cleared for AE/AF lock")
                }

                // Update state to show lock indicator (yellow square)
                _state.update {
                    it.copy(
                        focusPoint = Pair(x, y),
                        isFocusing = true,
                        focusSuccess = null,
                        lockPoint = Pair(x, y),
                        isAEAFLocked = false  // Will be set to true after successful lock
                    )
                }

                // Execute CameraX lock (persistent focus metering without auto-cancel)
                val result = cameraRepository.lockAEAF(x, y, viewWidth, viewHeight)

                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isFocusing = false,
                            focusSuccess = true,
                            isAEAFLocked = true  // Lock successful
                        )
                    }
                    Log.i(TAG, "AE/AF locked successfully at ($x, $y)")

                } else {
                    _state.update {
                        it.copy(
                            isFocusing = false,
                            focusSuccess = false,
                            lockPoint = null,
                            isAEAFLocked = false
                        )
                    }
                    Log.w(TAG, "AE/AF lock failed: ${result.exceptionOrNull()?.message}")
                    delay(500)
                    _state.update { it.copy(focusPoint = null, focusSuccess = null) }
                }

            } catch (e: Exception) {
                Log.e(TAG, "AE/AF lock error", e)
                _state.update {
                    it.copy(
                        isFocusing = false,
                        focusSuccess = false,
                        lockPoint = null,
                        isAEAFLocked = false
                    )
                }
                delay(500)
                _state.update { it.copy(focusPoint = null, focusSuccess = null) }
            }
        }
    }

    /**
     * Unlock AE/AF (return to auto mode)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock
     *
     * Cancels the persistent focus metering action and returns to auto mode.
     * Clears lock indicator.
     */
    private fun unlockAEAF() {
        viewModelScope.launch {
            try {
                Log.i(TAG, "Unlocking AE/AF")

                // Cancel focus metering to return to auto mode
                cameraRepository.cancelFocusAndMetering()

                // Clear lock state and indicators
                _state.update {
                    it.copy(
                        isAEAFLocked = false,
                        lockPoint = null,
                        focusPoint = null,
                        isFocusing = false,
                        focusSuccess = null
                    )
                }

                Log.i(TAG, "AE/AF unlocked, returned to auto mode")

            } catch (e: Exception) {
                Log.e(TAG, "AE/AF unlock error", e)
            }
        }
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
                    detectionResult = null,
                    // v2.9.10: Clear tracking state when stopping analysis
                    trackedObjectId = null,
                    trackedObjectType = null,
                    focusPoint = null
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
     * High-rate optical flow feed (v2.x.x Phase 7)
     *
     * Subscribes to previewBitmap WITHOUT sample() throttle so LK runs at full
     * camera frame rate (~28 FPS on Pixel 9a).  The heavier ML Kit / guideline
     * pipeline in observePreviewForAnalysis() stays at 10 FPS via sample(100).
     */
    private fun observePreviewForOpticalFlow() {
        if (!FeatureFlags.enableOpticalFlowTracking) return

        previewBitmap
            .filterNotNull()
            .onEach { bitmap ->
                if (_state.value.trackedPointActive) {
                    cameraRepository.processOpticalFlowFrame(bitmap)
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
        // v2.8.6 Phase 0: A/B Test C - Skip analysis if DEBUG_DISABLE_ANALYSIS is true
        if (DEBUG_DISABLE_ANALYSIS) {
            Log.d(TAG, "[A/B Test C] Analysis disabled for Graphics memory investigation")
            return
        }

        previewBitmap
            .filterNotNull()
            // Throttle analysis to avoid processing every frame
            // Process approximately every 100ms (10 FPS)
            .sample(100)
            .onEach { bitmap ->
                try {
                    // v2.8.4: End-to-end profiling
                    val e2eStart = System.nanoTime()

                    // Convert Bitmap to ML Kit InputImage
                    val convertStart = System.nanoTime()
                    val inputImage = com.google.mlkit.vision.common.InputImage.fromBitmap(
                        bitmap,
                        0  // Rotation: 0 degrees (bitmap already correctly oriented)
                    )
                    val convertTime = (System.nanoTime() - convertStart) / 1_000_000.0

                    // Create detection result by analyzing the bitmap
                    val mlkitStart = System.nanoTime()
                    val detectionResult = analyzeImageWithMLKit(inputImage)
                    val mlkitTime = (System.nanoTime() - mlkitStart) / 1_000_000.0

                    // Update detection state with stabilization logic
                    var compositionTime = 0.0
                    var lightingTime = 0.0

                    if (detectionResult != null) {
                        val hasFaces = detectionResult.faces.isNotEmpty()

                        if (hasFaces) {
                            consecutiveFaceDetections++
                            consecutiveNoFaceDetections = 0

                            // Only update state if we have enough consecutive detections
                            if (consecutiveFaceDetections >= MIN_DETECTIONS_TO_SHOW ||
                                _state.value.detectionResult?.faces?.isNotEmpty() == true) {
                                _state.update { it.copy(detectionResult = detectionResult) }

                                // v2.9.10: Update tracked object position (Traycer)
                                updateTrackedObjectPosition(detectionResult)

                                // Perform composition analysis
                                val compStart = System.nanoTime()
                                analyzeComposition(detectionResult)
                                compositionTime = (System.nanoTime() - compStart) / 1_000_000.0

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

                                // v2.9.10: Update tracking (will clear if target lost)
                                updateTrackedObjectPosition(detectionResult)

                                _compositionState.value = CompositionState.NoSubject

                                Log.i(TAG, "Detection cleared: No stable face detected")
                            }
                        }

                        // Analyze lighting (v2.2.0) - Use Bitmap-based analysis with subject context
                        val lightStart = System.nanoTime()
                        val lightingState = if (detectionResult.faces.isNotEmpty() || detectionResult.objects.isNotEmpty()) {
                            analyzeLightingUseCase.analyzeWithSubject(bitmap, detectionResult)
                        } else {
                            analyzeLightingUseCase(bitmap)
                        }
                        lightingTime = (System.nanoTime() - lightStart) / 1_000_000.0
                        _lightingState.value = lightingState
                        Log.d(TAG, "Lighting state updated: ${lightingState.javaClass.simpleName}")

                        // Handle Auto HDR (v2.3.0)
                        val repositoryImpl = cameraRepository as? com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl
                        if (repositoryImpl != null) {
                            handleAutoHDR(repositoryImpl, lightingState)
                        }
                    }

                    // v2.x.x Phase 7: Optical flow feed moved to observePreviewForOpticalFlow()
                    // (runs at full frame rate, independent of this sample(100) throttle)

                    // v2.8.4: End-to-end profiling summary
                    val e2eTotal = (System.nanoTime() - e2eStart) / 1_000_000.0
                    Log.i(TAG, "[v2.8.4 E2E] Total=${"%.1f".format(e2eTotal)}ms " +
                               "(Convert=${"%.1f".format(convertTime)}ms, " +
                               "MLKit=${"%.1f".format(mlkitTime)}ms, " +
                               "Composition=${"%.1f".format(compositionTime)}ms, " +
                               "Lighting=${"%.1f".format(lightingTime)}ms)")
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

    /**
     * Bind camera to lifecycle
     * v2.6.1: Required for LiveCameraSource to start real camera
     */
    fun bindCameraToLifecycle(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        Log.i(TAG, "bindCameraToLifecycle: Binding camera to lifecycle")
        cameraRepository.bindToLifecycle(lifecycleOwner)
        Log.i(TAG, "bindCameraToLifecycle: Camera bound successfully")
    }

    /**
     * Observes preview bitmap stream for OpenCV guideline analysis (v2.7.0 Phase 2 Sprint 4)
     *
     * Samples previewBitmap at 10 FPS (100ms intervals) and performs OpenCV Hough Line Transform
     * to detect composition guideline lines. Results are exposed via guidelineResults StateFlow.
     *
     * Performance considerations:
     * - Runs on Dispatchers.Default (background thread) to avoid blocking UI
     * - Sampling at 10 FPS reduces overhead compared to 30 FPS camera stream
     * - OpenCV analysis target: <15ms average, <30ms P95
     *
     * Feature flag: Can be disabled in future via FeatureFlags.enableOpenCvGuidelineBinding
     */
    @OptIn(FlowPreview::class)
    private fun observePreviewForGuidelineAnalysis() {
        // v2.8.6 Phase 0: A/B Test C - Skip guideline analysis if DEBUG_DISABLE_ANALYSIS is true
        if (DEBUG_DISABLE_ANALYSIS) {
            Log.d(TAG, "[A/B Test C] Guideline analysis disabled for Graphics memory investigation")
            return
        }

        previewBitmap
            .filterNotNull()
            // Throttle analysis to 10 FPS (same as ML Kit analysis)
            .sample(100)
            .onEach { bitmap ->
                try {
                    // v2.8.4: Profiling for guideline analysis
                    val guidelineStart = System.nanoTime()

                    // Perform OpenCV analysis on background thread
                    val result = withContext(Dispatchers.Default) {
                        guidelineAnalyzer.analyzeBitmap(bitmap)
                    }

                    val guidelineTotal = (System.nanoTime() - guidelineStart) / 1_000_000.0

                    // Update state (StateFlow updates automatically switch to Main thread)
                    _guidelineResults.value = result

                    // v2.8.4: Enhanced logging with total time (includes thread switching overhead)
                    Log.i(
                        TAG,
                        "[v2.8.4 Guideline] Lines=${result.lines.size}, " +
                        "OpenCV=${result.processingTimeMs}ms, " +
                        "Total=${"%.1f".format(guidelineTotal)}ms"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to analyze guidelines", e)
                    // Clear results on error
                    _guidelineResults.value = null
                }
            }
            .launchIn(viewModelScope)

        Log.i(TAG, "Guideline analysis initialized (v2.7.0 Phase 2 Sprint 4)")
    }

    /**
     * Set PreviewView surface provider for native preview rendering
     *
     * v2.8.6 Phase 2: PreviewView migration for Graphics memory fix
     *
     * Connects PreviewView to CameraX Preview UseCase via Repository → CameraInputSource
     *
     * @param surfaceProvider PreviewView's surface provider, or null to clear
     */
    fun setPreviewSurfaceProvider(surfaceProvider: androidx.camera.core.Preview.SurfaceProvider?) {
        Log.d(TAG, "[Phase 2] setPreviewSurfaceProvider called")
        cameraRepository.setPreviewSurfaceProvider(surfaceProvider)
    }

    /**
     * Check if native preview is supported
     *
     * v2.8.6 Phase 2: PreviewView migration
     *
     * @return true for real devices (LiveCameraSource), false for emulator (StaticImageSource)
     */
    fun supportsNativePreview(): Boolean {
        return cameraRepository.supportsNativePreview()
    }

    /**
     * Detect camera capabilities after permission granted - v2.9.5
     *
     * Triggers hardware detection on first launch (cache miss).
     * Subsequent launches use cached result.
     */
    fun detectCameraCapabilitiesIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check if already cached
                if (capabilityDetector.isCapabilitiesCached()) {
                    Log.i(TAG, "[v2.9.5] Capabilities already cached, skipping detection")
                    return@launch
                }

                // Perform detection
                Log.i(TAG, "[v2.9.5] Detecting camera capabilities...")
                val result = capabilityDetector.detectAndCacheCapabilities()

                result.onSuccess { strategy ->
                    Log.i(TAG, "[v2.9.5] Detection success: " +
                              "supports800x600=${strategy.supports800x600}, " +
                              "target=${strategy.targetResolution.width}x${strategy.targetResolution.height}")
                }.onFailure { error ->
                    Log.e(TAG, "[v2.9.5] Detection failed: ${error.message}", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[v2.9.5] Unexpected error during detection: ${e.message}", e)
            }
        }
    }

}
