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
import kotlinx.coroutines.Job
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

    // Smart Subject Focus (v2.11.0: AI Auto-Focus)
    private var lastManualTapTime: Long = 0L
    private val MANUAL_TAP_OVERRIDE_DURATION = 5000L  // 5 seconds
    // v2.11.4: Separate throttle for auto-focus (avoid conflating with manual tap override)
    private var lastAutoFocusTime: Long = 0L
    private val AUTO_FOCUS_THROTTLE_MS = 2000L  // 2 seconds between auto-focus triggers

    // v2.11.2 Fix E: Track the active focusOnPoint() coroutine to cancel stale jobs.
    // Prevents race condition where stale delay(500) clears focusPoint after lockAEAF sets it.
    private var focusJob: Job? = null

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

        // v2.10.2: Monitor aspect ratio mode changes from Settings (continuous listening)
        viewModelScope.launch {
            overlayPreferences.aspectRatioMode.collect { savedMode ->
                val mode = try {
                    com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode.valueOf(savedMode)
                } catch (e: IllegalArgumentException) {
                    // Fallback to default if invalid string
                    Log.w(TAG, "Invalid aspect ratio mode: $savedMode, falling back to RATIO_16_9")
                    com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode.RATIO_16_9
                }

                // Update CameraState
                _state.update { it.copy(aspectRatioMode = mode) }

                // Apply to camera repository (triggers camera rebind)
                cameraRepository.setAspectRatioMode(mode)

                Log.d(TAG, "Aspect ratio mode updated from settings: $mode")
            }
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
                // [v2.11.2 Fix G] trackingResult.point is in IMAGE coords (SmartCameraAnalyzer no longer converts).
                // Use CoordinateTransformer (FIT_CENTER) instead of CoordinateMapper (CENTER_CROP) to
                // avoid the 2.24x scale error that caused excessive drift.
                val transformer = _state.value.coordinateTransformer
                val (screenX, screenY) = if (transformer != null) {
                    transformer.imageToScreen(
                        trackingResult.point.x.toFloat(),
                        trackingResult.point.y.toFloat()
                    )
                } else {
                    // Fallback: transformer not ready — use raw coords (may drift, but non-crashing)
                    Log.w(TAG, "[Fix G] No coordinateTransformer in OF callback — using raw image coords (fallback)")
                    trackingResult.point.x.toFloat() to trackingResult.point.y.toFloat()
                }

                // Update tracking state with screen-space coordinates
                // Note: Do NOT reset focusIndicatorFading here — fade sequence controls it
                _state.update {
                    it.copy(
                        focusPoint = Pair(screenX, screenY),
                        trackedPointActive = true,
                        trackedPointQuality = trackingResult.quality,
                        trackedPointLost = false
                    )
                }
                Log.v(TAG, "[Fix G] OF callback: image=(${trackingResult.point.x.toFloat()}, ${trackingResult.point.y.toFloat()}) → screen=($screenX, $screenY), quality=${trackingResult.quality}")

            } else {
                // v2.11.2 fix: Guard against repeated LOST callbacks (fires every frame after LOST).
                // Only process LOST once: when trackedPointActive is still true (first LOST event).
                val wasActive = _state.value.trackedPointActive
                if (!wasActive) return@setOpticalFlowCallback  // Already handling LOST, skip duplicate

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
            // v2.11.0 Phase 2: ToggleAspectRatio removed - now in Settings
            is CameraUiEvent.StartAnalysis -> startAnalysis()
            is CameraUiEvent.StopAnalysis -> stopAnalysis()
            is CameraUiEvent.ShowError -> showError(event.message)
            is CameraUiEvent.FocusOnPoint -> focusOnPoint(event.x, event.y, event.viewWidth, event.viewHeight)  // v2.9.9
            is CameraUiEvent.LockAEAF -> lockAEAF(event.x, event.y, event.viewWidth, event.viewHeight)  // v2.9.9 Day 1-2
            is CameraUiEvent.UnlockAEAF -> unlockAEAF()  // v2.9.9 Day 1-2
            is CameraUiEvent.SetPreviewSize -> setPreviewSize(event.width, event.height)  // v2.x.x Phase 5
            is CameraUiEvent.ShowPhotoInfo -> showPhotoInfo(event.uri)  // v2.11.0 Phase 3
            is CameraUiEvent.DismissPhotoInfo -> dismissPhotoInfo()  // v2.11.0 Phase 3
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

    /**
     * Update content bounds and coordinate transformer
     * v2.11.1 P0-2: Coordinate system unification
     *
     * Called by PreviewViewportContainer when content bounds are calculated.
     * Creates CoordinateTransformer for accurate Image ↔ Preview ↔ Screen mapping.
     *
     * @param bounds Actual preview content bounds (excludes FIT_CENTER letterbox)
     */
    fun updateContentBounds(bounds: android.graphics.RectF) {
        val detection = _state.value.detectionResult
        val rawImageWidth = detection?.imageWidth ?: 0
        val rawImageHeight = detection?.imageHeight ?: 0
        val rotation = detection?.rotationDegrees ?: 0
        val isFrontCamera = _state.value.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT

        // v2.11.2 Fix D: Swap dimensions for 90°/270° rotation.
        // ImageProxy reports sensor-native (landscape) dimensions: e.g., width=1280, height=720.
        // ML Kit bounding boxes and OF tracked positions are in the ROTATED (upright) image space.
        // In portrait (rotationDegrees=90 or 270), the effective upright space is 720×1280.
        // Without this swap, CoordinateTransformer maps image coords to wrong screen positions.
        val (effectiveImageWidth, effectiveImageHeight) = if (rotation == 90 || rotation == 270) {
            rawImageHeight to rawImageWidth   // landscape sensor → portrait upright: 1280×720 → 720×1280
        } else {
            rawImageWidth to rawImageHeight
        }

        // Create coordinate transformer if image dimensions are available
        val transformer = if (effectiveImageWidth > 0 && effectiveImageHeight > 0) {
            com.akirachang.ai.photographer.presentation.camera.components.CoordinateTransformer(
                contentBounds = bounds,
                imageWidth = effectiveImageWidth,
                imageHeight = effectiveImageHeight,
                isFrontCamera = isFrontCamera
            )
        } else {
            null
        }

        _state.update {
            it.copy(
                contentBounds = bounds,
                coordinateTransformer = transformer
            )
        }

        Log.d(TAG, "[v2.11.2 Fix D] Content bounds updated: " +
                "left=${bounds.left}, top=${bounds.top}, " +
                "right=${bounds.right}, bottom=${bounds.bottom}, " +
                "width=${bounds.width()}, height=${bounds.height()}, " +
                "rawImage=${rawImageWidth}x${rawImageHeight}, rotation=${rotation}°, " +
                "effectiveImage=${effectiveImageWidth}x${effectiveImageHeight}, " +
                "transformer=${if (transformer != null) "created" else "null (no image dims)"}")
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

    /**
     * Toggle aspect ratio mode (v2.10.0)
     *
     * Cycles through: 4:3 → 16:9 → 1:1 → 4:3
     *
     * Behavior:
     * - Updates UI state with new aspect ratio
     * - Sets isRebindingCamera flag during transition
     * - Triggers camera rebind via repository with new ViewPort
     */
    // v2.11.0 Phase 2: toggleAspectRatio() removed - aspect ratio now managed in Settings

    private fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Show photo EXIF information
     * v2.11.0 Phase 3: EXIF display feature
     *
     * Reads EXIF metadata from captured photo and displays in BottomSheet.
     * Launches coroutine to read EXIF asynchronously, updates state when ready.
     *
     * @param uri Content URI of the captured photo
     */
    private fun showPhotoInfo(uri: android.net.Uri) {
        viewModelScope.launch {
            val exifInfo = cameraRepository.readExifInfo(uri)
            if (exifInfo != null) {
                _state.update {
                    it.copy(
                        showPhotoInfo = true,
                        currentPhotoExifInfo = exifInfo
                    )
                }
                Log.d(TAG, "Photo EXIF info loaded: ${exifInfo.fileName}")
            } else {
                showError("無法讀取照片資訊")
                Log.e(TAG, "Failed to read EXIF info from $uri")
            }
        }
    }

    /**
     * Dismiss photo EXIF information
     * v2.11.0 Phase 3: EXIF display feature
     */
    private fun dismissPhotoInfo() {
        _state.update {
            it.copy(
                showPhotoInfo = false,
                currentPhotoExifInfo = null
            )
        }
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
        val transformer = _state.value.coordinateTransformer

        // Convert tap point from screen space to ML Kit image space for boundingBox comparison.
        // face.boundingBox / obj.boundingBox are in image coordinates; touch x,y are in screen coords.
        val (imageX, imageY) = if (transformer != null) {
            transformer.screenToImage(x, y) ?: run {
                Log.d(TAG, "findTappedTarget: tap ($x, $y) outside content bounds — no target")
                return null
            }
        } else {
            Log.w(TAG, "findTappedTarget: no coordinateTransformer, using raw screen coords (legacy fallback)")
            x to y
        }

        Log.d(TAG, "findTappedTarget: screen=($x, $y) → image=($imageX, $imageY)")

        // Priority 1: Check faces (more stable tracking)
        detectionResult.faces.forEachIndexed { index, face ->
            if (face.boundingBox.contains(imageX.toInt(), imageY.toInt())) {
                val trackingId = face.trackingId ?: (index + 10000)
                Log.i(TAG, "findTappedTarget: tapped face trackingId=$trackingId, bbox=${face.boundingBox}")
                return Pair(trackingId, TrackedObjectType.FACE)
            }
        }

        // Priority 2: Check objects
        detectionResult.objects.forEachIndexed { index, obj ->
            if (obj.boundingBox.contains(imageX.toInt(), imageY.toInt())) {
                val trackingId = obj.trackingId ?: (index + 20000)
                Log.i(TAG, "findTappedTarget: tapped object trackingId=$trackingId, bbox=${obj.boundingBox}")
                return Pair(trackingId, TrackedObjectType.OBJECT)
            }
        }

        Log.d(TAG, "findTappedTarget: no target at image=($imageX, $imageY)")
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

        // Find target by type and calculate focus position
        var centerX: Float? = null
        var centerY: Float? = null

        when (trackedType) {
            TrackedObjectType.FACE -> {
                val face = detectionResult.faces.firstOrNull { face ->
                    (face.trackingId ?: -1) == trackedId
                }

                if (face != null) {
                    // v2.11.0 Eye-AF: Use eye position if available
                    val eyePosition = detectionResult.eyePositions.firstOrNull { it.faceId == trackedId }

                    if (eyePosition?.nearestEye != null) {
                        // Eye-AF: Focus on nearest eye for professional portrait
                        centerX = eyePosition.nearestEye.x
                        centerY = eyePosition.nearestEye.y
                        if (BuildConfig.DEBUG) {
                            Log.v(TAG, "Eye-AF tracking: Face $trackedId at eye ($centerX, $centerY)")
                        }
                    } else {
                        // Fallback: Use face bounding box center
                        centerX = face.boundingBox.exactCenterX()
                        centerY = face.boundingBox.exactCenterY()
                        if (BuildConfig.DEBUG) {
                            Log.v(TAG, "Face tracking (no eye): Face $trackedId at center ($centerX, $centerY)")
                        }
                    }
                }
            }
            TrackedObjectType.OBJECT -> {
                val obj = detectionResult.objects.firstOrNull { obj ->
                    (obj.trackingId ?: -1) == trackedId
                }

                if (obj != null) {
                    centerX = obj.boundingBox.exactCenterX()
                    centerY = obj.boundingBox.exactCenterY()
                }
            }
        }

        if (centerX != null && centerY != null) {
            // Target found — convert image-space center to screen space for FocusIndicator.
            // centerX/centerY are in ML Kit image coordinates; focusPoint must be in screen space.
            val transformer = _state.value.coordinateTransformer
            val (screenX, screenY) = if (transformer != null) {
                transformer.imageToScreen(centerX, centerY)
            } else {
                Log.w(TAG, "updateTrackedObjectPosition: no coordinateTransformer, using raw image coords (legacy fallback)")
                centerX to centerY
            }

            // v2.11.0 Fade-out optimization: Reset on significant motion during dwell state
            // Comparison uses screen-space coordinates on both sides now.
            if (_state.value.focusIndicatorFading) {
                val oldFocusPoint = _state.value.focusPoint
                if (oldFocusPoint != null) {
                    val positionDelta = kotlin.math.sqrt(
                        (screenX - oldFocusPoint.first) * (screenX - oldFocusPoint.first) +
                        (screenY - oldFocusPoint.second) * (screenY - oldFocusPoint.second)
                    )

                    // Motion threshold: 30 pixels (about 1cm on typical phone screen)
                    val motionThreshold = 30f

                    if (positionDelta > motionThreshold) {
                        // Significant motion detected: reset fade and refocus using screen coords
                        // v2.11.4-01 Fix Bug 2: TOCTOU guard — abort if trackedObjectId changed
                        // since the start of updateTrackedObjectPosition() (path B).
                        _state.update { current ->
                            if (current.trackedObjectId != trackedId) return@update current
                            current.copy(focusIndicatorFading = false, isFocusing = true)
                        }

                        val viewWidth = previewWidth ?: return
                        val viewHeight = previewHeight ?: return
                        // v2.11.4: isUserTap=false — motion-triggered refocus, not manual tap
                        focusOnPoint(screenX, screenY, viewWidth, viewHeight, isUserTap = false)

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Fade-out reset: object moved ${positionDelta.toInt()}px (threshold: ${motionThreshold.toInt()}px)")
                        }
                        return  // Skip normal position update since we just refocused
                    }
                }
            }

            // Prevent excessive updates: only update if position changed > 5 pixels
            val currentFocusPoint = _state.value.focusPoint
            val shouldUpdate = currentFocusPoint == null ||
                kotlin.math.abs(currentFocusPoint.first - screenX) > 5f ||
                kotlin.math.abs(currentFocusPoint.second - screenY) > 5f

            if (shouldUpdate) {
                // v2.11.4-01 Fix Bug 2: TOCTOU guard — abort if trackedObjectId changed
                // since the start of updateTrackedObjectPosition() (path A).
                _state.update { current ->
                    if (current.trackedObjectId != trackedId) return@update current
                    current.copy(focusPoint = Pair(screenX, screenY))
                }
                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Tracking update: ID=$trackedId, image=($centerX, $centerY), screen=($screenX, $screenY)")
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
    /**
     * @param isUserTap v2.11.4: true = user manual tap (sets lastManualTapTime to suppress auto-focus
     *   for 5s), false = auto-focus or motion-triggered refocus (does NOT set lastManualTapTime).
     */
    private fun focusOnPoint(x: Float, y: Float, viewWidth: Int, viewHeight: Int, isUserTap: Boolean = true) {
        // v2.11.2 Fix E: Cancel any stale focusOnPoint() coroutine before launching new one.
        // Prevents stale delay(500) → focusPoint=null from racing with lockAEAF state update.
        focusJob?.cancel()
        // v2.11.4-01 Fix Bug 2: Set lastManualTapTime synchronously BEFORE launch{} so
        // autoFocusOnSalientTarget() always sees the updated timestamp when the ML Kit
        // analysis resumes on Main thread. Eliminates the ~0.1ms race window where
        // autoFocusOnSalientTarget() could read the stale value and cancel this tap job.
        if (isUserTap) {
            lastManualTapTime = System.currentTimeMillis()
        }
        focusJob = viewModelScope.launch {
            try {

                // 🔍 DIAGNOSTIC: Log tap coordinates and view size
                Log.d(TAG, "🔍 [Coordinate Debug] Tap: ($x, $y), View: ${viewWidth}x${viewHeight}")

                // v2.11.2 fix Bug C: Unlock AE/AF then immediately re-focus at the new tap point.
                // Previous behavior: return@launch after unlock (required two taps to restart tracking).
                // New behavior: unlock + continue to process the tap, so one tap restores auto-focus
                // and starts tracking at the new location.
                if (_state.value.isAEAFLocked) {
                    cameraRepository.cancelFocusAndMetering()
                    _state.update {
                        it.copy(
                            isAEAFLocked = false,
                            lockPoint = null,
                            focusIndicatorFading = false
                        )
                    }
                    Log.i(TAG, "AE/AF unlocked by tap — continuing to focus at ($x, $y)")
                    // Do NOT return — fall through to process the tap as new focus/tracking
                }

                // v2.9.10: Find tapped target for tracking
                val tappedTarget = findTappedTarget(x, y)
                // v2.11.4 Fix 3: Track which branch was taken at the START of the coroutine.
                // Cannot rely on _state.value.trackedObjectId at focus-success time because:
                //   (a) ML Kit detection may briefly clear trackedObjectId during the 1-2s focus window
                //   (b) Even on empty-area taps, ML Kit detection keeps setting trackedObjectId for
                //       any face present in the frame — making the state check unreliable.
                // Using a local variable ensures the fade decision matches the branch entered here.
                var enteredMLKitTrackingMode = false

                if (tappedTarget != null && FeatureFlags.enableFocusTracking) {
                    // ML Kit Tracking mode: Start continuous tracking
                    enteredMLKitTrackingMode = true
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

                    // [v2.11.2 Fix G] Convert screen → image via CoordinateTransformer (FIT_CENTER, correct scale).
                    // Bypasses CoordinateMapper CENTER_CROP to eliminate 2.24x scale error.
                    val transformer = _state.value.coordinateTransformer
                    if (transformer != null) {
                        val imageCoords = transformer.screenToImage(x, y)
                        if (imageCoords != null) {
                            cameraRepository.startOpticalFlowTrackingImageCoords(imageCoords.first, imageCoords.second)
                            Log.i(TAG, "[Fix G] OF start: screen=($x, $y) → image=(${imageCoords.first}, ${imageCoords.second}) via CoordinateTransformer")
                        } else {
                            // Tap outside content bounds (letterbox area) — fallback to old path
                            cameraRepository.startOpticalFlowTracking(x, y)
                            Log.w(TAG, "[Fix G] OF start: tap outside content bounds, fallback to CoordinateMapper")
                        }
                    } else {
                        // Transformer not ready — fallback to old path (may drift)
                        cameraRepository.startOpticalFlowTracking(x, y)
                        Log.w(TAG, "[Fix G] OF start: no coordinateTransformer, fallback to CoordinateMapper")
                    }

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

                    // v2.11.4 Fix 3: Skip dwell fade when ML Kit continuous tracking is active.
                    // enteredMLKitTrackingMode is captured at coroutine START (not at success time)
                    // to avoid race conditions where ML Kit reassigns trackedObjectId during the
                    // 1-2s focus window, making the state check at this point unreliable.
                    // ML Kit tracking: keep alpha 1.0 (indicator stays visible while tracking).
                    // OF/Static: alpha 1.0 for 1s → fade 500ms → alpha 0.4 (dwell indicator).
                    if (!enteredMLKitTrackingMode) {
                        delay(1000)  // Keep for 1 second
                        _state.update { it.copy(focusIndicatorFading = true, focusIndicatorFadeStartTime = System.currentTimeMillis()) }
                        delay(500)   // Fade out for 500ms
                        // Keep focusIndicatorFading=true to maintain alpha 0.4 (reset on next tap)
                    }
                    // else: ML Kit tracking — no fade, indicator stays bright (alpha 1.0)

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
        // v2.11.2 Fix E: Cancel stale focusOnPoint() coroutine to prevent its delay(500) → focusPoint=null
        // from firing AFTER this lockAEAF sets focusPoint=B (race window: ~800ms for focus failure path).
        focusJob?.cancel()
        Log.i(TAG, "focusJob cancelled for AE/AF lock at ($x, $y)")

        viewModelScope.launch {
            try {
                Log.i(TAG, "Locking AE/AF at ($x, $y)")

                // v2.11.2 fix Bug A: Stop Optical Flow BEFORE state update to prevent
                // OF callback from overwriting focusPoint=B with the old A coordinates.
                val wasOpticalFlowTracking = _state.value.trackedPointActive
                if (wasOpticalFlowTracking) {
                    cameraRepository.stopOpticalFlowTracking()
                    Log.i(TAG, "Optical flow tracking stopped for AE/AF lock")
                }

                // Single atomic update: clear ALL tracking state + set lock indicator.
                // trackedPointActive=false ensures OF callback no longer writes focusPoint.
                _state.update {
                    it.copy(
                        trackedObjectId = null,       // Clear ML Kit tracking
                        trackedObjectType = null,
                        trackedPointActive = false,   // Stop OF callback from overwriting focusPoint
                        trackedPointLost = false,
                        focusIndicatorFading = false, // Reset dwell fade for fresh lock indicator
                        focusPoint = Pair(x, y),      // B point (safe: OF already stopped)
                        isFocusing = true,
                        focusSuccess = null,
                        lockPoint = Pair(x, y),
                        isAEAFLocked = false          // Will be set to true after successful lock
                    )
                }

                // Execute CameraX lock (persistent focus metering without auto-cancel)
                val result = cameraRepository.lockAEAF(x, y, viewWidth, viewHeight)

                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isFocusing = false,
                            focusSuccess = true,
                            isAEAFLocked = true,  // Lock successful
                            focusPoint = Pair(x, y)  // v2.11.2 Fix E: Restore in case focusJob cancel arrived late
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
     * Calculate saliency score for Smart Subject Focus (v2.11.0)
     *
     * Uses heuristic weight calculation (ML Kit Proxy approach):
     * Score (0-100 range) = (BoxSize% * 60) + (Centrality * 40) + (Face Bonus: +30)
     *
     * v2.11.4 fix: Changed weight multipliers from 0.6f/0.4f to 60f/40f so the base score
     * is 0-100 (not 0-1). Face bonus +30 on a 0-100 scale means ~30% boost, making the
     * autoFocusOnSalientTarget threshold of 50f achievable for typical faces.
     * With 0.6f/0.4f the max face score was ~31, which NEVER reached the 50f threshold.
     *
     * @param boundingBox Detection bounding box
     * @param isFace Whether this is a face detection
     * @param viewWidth View width in pixels
     * @param viewHeight View height in pixels
     * @return Saliency score (0-130 range; higher = more salient)
     */
    private fun calculateSaliencyScore(
        boundingBox: android.graphics.Rect,
        isFace: Boolean,
        viewWidth: Int,
        viewHeight: Int
    ): Float {
        // 1. Box size component (normalized area ratio, 0-1)
        val boxSize = (boundingBox.width() * boundingBox.height()).toFloat()
        val normalizedSize = boxSize / (viewWidth * viewHeight)

        // 2. Centrality component (distance from screen center, 0-1)
        val centerX = boundingBox.exactCenterX()
        val centerY = boundingBox.exactCenterY()
        val screenCenterX = viewWidth / 2f
        val screenCenterY = viewHeight / 2f

        val distance = kotlin.math.sqrt(
            (centerX - screenCenterX) * (centerX - screenCenterX) +
            (centerY - screenCenterY) * (centerY - screenCenterY)
        )
        val maxDistance = kotlin.math.sqrt(
            screenCenterX * screenCenterX + screenCenterY * screenCenterY
        )
        val centrality = 1 - (distance / maxDistance)

        // 3. Base score: 0-100 range (size 0-60, centrality 0-40)
        // v2.11.4 fix: 0.6f→60f, 0.4f→40f — score is now 0-100 compatible with threshold 50f
        var score = (normalizedSize * 60f) + (centrality * 40f)

        // 4. Face bonus: +30 on 0-100 scale = 30% boost (Eye-AF priority)
        if (isFace) score += 30f

        return score
    }

    /**
     * Auto-focus on most salient subject (v2.11.0 Smart Subject Focus)
     *
     * Automatically identifies and focuses on the most important subject
     * when no manual tap has occurred recently.
     *
     * Priority: Manual tap > Eye-AF > Smart Subject Focus > None
     */
    private fun autoFocusOnSalientTarget() {
        val currentState = _state.value
        val now = System.currentTimeMillis()

        // Don't auto-focus if:
        // 1. AE/AF is locked
        // 2. Currently focusing
        // 3. Manual tap occurred within last 5 seconds (user has override priority)
        // 4. v2.11.4: Auto-focus triggered within last 2 seconds (independent throttle)
        val canAutoFocus = !currentState.isAEAFLocked &&
            !currentState.isFocusing &&
            (now - lastManualTapTime) > MANUAL_TAP_OVERRIDE_DURATION &&
            (now - lastAutoFocusTime) > AUTO_FOCUS_THROTTLE_MS

        if (!canAutoFocus) return

        val detectionResult = currentState.detectionResult ?: return
        val viewWidth = previewWidth ?: return
        val viewHeight = previewHeight ?: return

        // Collect all candidates with scores
        val candidates = mutableListOf<Pair<Pair<Float, Float>, Float>>()

        val transformer = _state.value.coordinateTransformer

        // Faces (with Eye-AF integration)
        // Candidate points are in ML Kit image space and will be converted to screen space before use.
        detectionResult.faces.forEach { face ->
            val score = calculateSaliencyScore(face.boundingBox, true, viewWidth, viewHeight)
            val eyePosition = detectionResult.eyePositions.firstOrNull { it.faceId == face.trackingId }
            val imageCenterX = eyePosition?.nearestEye?.x ?: face.boundingBox.exactCenterX()
            val imageCenterY = eyePosition?.nearestEye?.y ?: face.boundingBox.exactCenterY()
            candidates.add((imageCenterX to imageCenterY) to score)
        }

        // Objects
        detectionResult.objects.forEach { obj ->
            val score = calculateSaliencyScore(obj.boundingBox, false, viewWidth, viewHeight)
            val imageCenterX = obj.boundingBox.exactCenterX()
            val imageCenterY = obj.boundingBox.exactCenterY()
            candidates.add((imageCenterX to imageCenterY) to score)
        }

        // Find top candidate
        val topCandidate = candidates.maxByOrNull { it.second }

        // Auto-focus if score is above threshold
        if (topCandidate != null && topCandidate.second > 50f) {
            val (imageX, imageY) = topCandidate.first
            // Convert image-space point to screen space before calling focusOnPoint.
            val (screenX, screenY) = if (transformer != null) {
                transformer.imageToScreen(imageX, imageY)
            } else {
                imageX to imageY  // Legacy fallback
            }
            // v2.11.4: isUserTap=false (auto-focus call, not user manual tap)
            // Record auto-focus time here (independent throttle, not lastManualTapTime)
            lastAutoFocusTime = now
            focusOnPoint(screenX, screenY, viewWidth, viewHeight, isUserTap = false)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Smart Subject Focus: score=${topCandidate.second}, image=($imageX, $imageY), screen=($screenX, $screenY)")
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

                            // v2.11.2 Fix F: Rebuild CoordinateTransformer on first detection with valid dims.
                            // updateContentBounds() runs at layout time (before ML Kit), so transformer is null.
                            // Now that imageWidth/imageHeight are available, recreate with correct dimensions.
                            if (result.imageWidth > 0 && _state.value.coordinateTransformer == null &&
                                _state.value.contentBounds.width() > 0) {
                                updateContentBounds(_state.value.contentBounds)
                            }

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

                            // v2.11.2 Fix F: Rebuild transformer even on no-face result — imageWidth still valid
                            if (result.imageWidth > 0 && _state.value.coordinateTransformer == null &&
                                _state.value.contentBounds.width() > 0) {
                                updateContentBounds(_state.value.contentBounds)
                            }

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

                                // v2.11.2 Fix F: Rebuild CoordinateTransformer on first detection with valid dims
                                if (detectionResult.imageWidth > 0 && _state.value.coordinateTransformer == null &&
                                    _state.value.contentBounds.width() > 0) {
                                    updateContentBounds(_state.value.contentBounds)
                                }

                                // v2.9.10: Update tracked object position (Traycer)
                                updateTrackedObjectPosition(detectionResult)

                                // v2.11.0: Smart Subject Focus (AI auto-focus)
                                autoFocusOnSalientTarget()

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

                                // v2.11.2 Fix F: Rebuild transformer even on no-face result — imageWidth still valid
                                if (detectionResult.imageWidth > 0 && _state.value.coordinateTransformer == null &&
                                    _state.value.contentBounds.width() > 0) {
                                    updateContentBounds(_state.value.contentBounds)
                                }

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
     * Set PreviewView reference for accurate coordinate transformation
     *
     * v2.11.1 P0-1: Coordinate system unification (Bug fix for out-of-preview tap focus)
     * Connects PreviewView to CameraInputSource for accurate metering point creation
     *
     * @param previewView PreviewView instance, or null to clear
     */
    fun setPreviewView(previewView: androidx.camera.view.PreviewView?) {
        Log.d(TAG, "[v2.11.1 P0-1] setPreviewView called")
        cameraRepository.setPreviewView(previewView)

        // v2.11.1 P0-3 FIX: Get actual PreviewView content bounds after layout
        previewView?.post {
            val contentBounds = getActualPreviewContentBounds(previewView)
            if (contentBounds != null) {
                updateContentBounds(contentBounds)
                Log.d(TAG, "[v2.11.1 P0-3 FIX] Actual preview content bounds: " +
                        "left=${contentBounds.left}, top=${contentBounds.top}, " +
                        "right=${contentBounds.right}, bottom=${contentBounds.bottom}")
            }
        }
    }

    /**
     * Get actual preview content bounds from PreviewView
     *
     * v2.11.1 P0-3 FIX: PreviewView uses FIT_CENTER, which means the actual preview
     * may be smaller than the View and centered with letterbox.
     * This method finds the actual preview surface and gets its bounds.
     */
    private fun getActualPreviewContentBounds(previewView: androidx.camera.view.PreviewView): android.graphics.RectF? {
        // Find the SurfaceView/TextureView inside PreviewView
        val previewSurface = findPreviewSurface(previewView) ?: run {
            Log.w(TAG, "[v2.11.1 P0-3 FIX] No preview surface found in PreviewView")
            return null
        }

        // Get surface size
        val width = previewSurface.width.toFloat()
        val height = previewSurface.height.toFloat()

        if (width <= 0 || height <= 0) {
            Log.w(TAG, "[v2.11.1 P0-3 FIX] Preview surface size is zero: ${width}x${height}")
            return null
        }

        // Get position relative to screen
        val location = IntArray(2)
        previewSurface.getLocationInWindow(location)

        // Content bounds in screen coordinates
        val left = location[0].toFloat()
        val top = location[1].toFloat()
        val right = left + width
        val bottom = top + height

        return android.graphics.RectF(left, top, right, bottom)
    }

    /**
     * Find the actual Surface (SurfaceView or TextureView) inside PreviewView
     */
    private fun findPreviewSurface(previewView: androidx.camera.view.PreviewView): android.view.View? {
        if (previewView.childCount == 0) return null

        // PreviewView typically has one child: SurfaceView or TextureView
        for (i in 0 until previewView.childCount) {
            val child = previewView.getChildAt(i)
            when (child) {
                is android.view.SurfaceView -> return child
                is android.view.TextureView -> return child
            }
        }
        return null
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
