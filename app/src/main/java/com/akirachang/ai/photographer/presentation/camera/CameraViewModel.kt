package com.akirachang.ai.photographer.presentation.camera

import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Build
import android.os.SystemClock
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
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import com.akirachang.ai.photographer.domain.model.CompositionResult
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.math.min
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import com.akirachang.ai.photographer.data.preferences.PtfPreferences
import com.akirachang.ai.photographer.domain.camera.CameraMode
import com.akirachang.ai.photographer.domain.camera.ImuCompensator
import com.akirachang.ai.photographer.domain.model.DeviceCapabilityTier
import com.akirachang.ai.photographer.domain.sensor.DeviceSensorManager
import com.akirachang.ai.photographer.domain.sensor.OrientationData
import com.akirachang.ai.photographer.domain.sensor.OrientationSensorManager
// v2.5.2 Phase 7: Removed CameraSelector import - using CameraLens instead
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.domain.usecase.AnalyzeCompositionUseCase
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.domain.config.FeatureFlags
import com.akirachang.ai.photographer.di.PtfEngine
import com.akirachang.ai.photographer.domain.composition.PtfAlignmentResult
import com.akirachang.ai.photographer.domain.composition.PtfRuleAggregator
import kotlin.math.abs
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val analyzeCompositionUseCase: AnalyzeCompositionUseCase,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val deviceSensorManager: DeviceSensorManager,
    private val orientationSensorManager: OrientationSensorManager,
    private val overlayPreferences: OverlayPreferences,
    private val ptfPreferences: PtfPreferences,
    private val capabilityDetector: com.akirachang.ai.photographer.data.source.camera.CameraCapabilityDetector,
    private val imuCompensator: ImuCompensator,
    val deviceCapabilityTier: DeviceCapabilityTier,
    @PtfEngine private val ptfRuleAggregator: PtfRuleAggregator,
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"

        // v2.12.0-03: Adaptive ML Kit throttle for CameraViewModel (device mode)
        private const val ADAPTIVE_THROTTLE_DEFAULT_MS  = 200L
        private const val ADAPTIVE_THROTTLE_MIN_MS      = 100L
        private const val ADAPTIVE_THROTTLE_MAX_MS      = 500L
        private const val ADAPTIVE_WARMUP_FRAMES        = 3
        private const val ADAPTIVE_EMA_ALPHA            = 0.2
        private const val ADAPTIVE_SAFETY_FACTOR        = 1.25
        private const val ADAPTIVE_DEADBAND_RATIO       = 0.10
        private const val ADAPTIVE_MAX_EXPAND_MS        = 50L
        private const val ADAPTIVE_MAX_CONTRACT_MS      = 20L
        private const val ADAPTIVE_PANIC_RATIO          = 1.5
        private const val ADAPTIVE_PANIC_SCALE          = 1.2
        private const val ADAPTIVE_LOG_EVERY_N_FRAMES   = 30

        // v2.12.0-10: Bbox scale factors shared by resolveTapTarget + updateTrackedObjectPosition.
        // Face bboxes are stable; object bboxes hug contours → needs more padding.
        const val BBOX_SCALE_FACE   = 1.5f
        const val BBOX_SCALE_OBJECT = 1.8f
        private const val TAP_INTENT_WINDOW_MS = 750L

        /**
         * Computes effective focal length in pixels from hardware factor and preview width.
         *
         * Contract (D3-T3):
         *   - If [factor] != null and [previewWidth] > 0: hardware-first → factor * previewWidth
         *   - Otherwise: fail-safe → previewWidth / 2f  (assumes hFov ≈ 90°, tan(45°) = 1)
         *   - If [previewWidth] == 0: result = 0f (sentinel — ImuCompensator returns (0,0) silently)
         *
         * @param factor  focalLengthMm / sensorWidthMm from CameraCharacteristics, null if not yet bound
         * @param previewWidth  current preview width in pixels, 0 if layout not yet measured
         */
        internal fun computeFocalLengthPixels(factor: Float?, previewWidth: Int): Float {
            return if (factor != null && previewWidth > 0) factor * previewWidth
            else previewWidth / 2f
        }

        /**
         * Rule 16 cooldown predicate — pure function, testable without ViewModel dependencies.
         *
         * Contract (D7-B2-T2):
         *   - Returns true  → shutter is still cooling down; caller must abort.
         *   - Returns false → cooldown has elapsed; caller may proceed and update [lastCaptureTimeMs].
         *   - Sentinel initial value [Long.MIN_VALUE] / 2 ensures the FIRST call always returns false.
         *
         * @param now              current epoch-ms from the caller's time provider
         * @param lastCaptureTimeMs epoch-ms of the last accepted capture (sentinel = Long.MIN_VALUE / 2)
         * @param cooldownMs       required gap between captures (default 1000 ms per Rule 16)
         */
        internal fun isPtfCooldownActive(
            now: Long,
            lastCaptureTimeMs: Long,
            cooldownMs: Long = 1000L
        ): Boolean = now - lastCaptureTimeMs < cooldownMs

        // D8-B1-T3: EMA smoothing factor for PTF face bbox center coordinates.
        internal const val PTF_EMA_ALPHA = 0.3f
    }

    private data class TapToFocusIntent(
        val pointView: PointF,
        val timestampMs: Long,
        val density: Float = 3f,  // v2.12.0-18: for optimistic tap box pixel sizing
    )

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    /**
     * Dedicated focus UI state flow for FocusIndicatorOverlay.
     * v2.12.0-19: Pass directly to FocusIndicatorOverlay — collect inside the overlay
     * to prevent CameraScreen top-level recomposition on every focus coordinate update.
     */
    val focusUiStateFlow: StateFlow<com.akirachang.ai.photographer.domain.model.focus.FocusUiState> =
        _state.map { it.focusUiState }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = com.akirachang.ai.photographer.domain.model.focus.FocusUiState.EMPTY,
            )


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

    // Cueshot D7-B2-T3: PTF onboarding flag — false until user taps "Got it".
    val isPtfOnboarded: Flow<Boolean> = ptfPreferences.isPtfOnboarded

    // Preview dimensions (v2.0 Phase 2)
    private var previewWidth: Int? = null
    private var previewHeight: Int? = null

    // Tracking state management (v2.9.10: Traycer)
    private var targetLostFrameCount = 0
    private val TARGET_LOST_THRESHOLD = 10  // Stop tracking after 10 consecutive frames without target

    // Smart Subject Focus (v2.11.0: AI Auto-Focus)
    private var lastManualTapTime: Long = 0L
    private val MANUAL_TAP_OVERRIDE_DURATION = 5000L  // 5 seconds
    // v2.12.0-05: Eye-AF dynamic throttle + 4-way state machine (Step 3+4)
    // Replaces lastAutoFocusTime / AUTO_FOCUS_THROTTLE_MS (simple 2 s gate).
    private val eyeAfThrottle = com.akirachang.ai.photographer.presentation.camera.focus.FocusTriggerThrottle()

    // Cueshot D6-B3: Gaze-driven Eye-AF controller — triggers AF when gaze is unaligned.
    private val eyeAfController = com.akirachang.ai.photographer.presentation.camera.components.EyeAfController()
    // v2.12.0-06 H2.2: 4500ms > CameraX autoCancelDuration(4s) — withTimeout fires only if HAL
    // is truly stuck. Previously 3000ms caused premature TIMEOUT before CameraX could deliver
    // success (e.g. rear-camera face focus succeeds at ~3.5s on some devices).
    private val FOCUS_TIMEOUT_MS = 4_500L
    // v2.12.0-06 Bug C: Face-first selection grace period — prevents target ping-pong when face
    // detection flickers between 0 and 1 (partial occlusion / head turn).
    private var lastFaceSeenMs = 0L
    private val FACE_GRACE_PERIOD_MS = 500L

    // Track the active focus coroutine to cancel stale jobs (camera switch, lock, etc.).
    private var focusJob: Job? = null
    private var focusSessionId: Int = 0  // v2.12.0-13: Session ID for HAL callback validation
    private var lastKnownImageWidth: Int = 0   // v2.12.0-14: Cache for transformer bootstrap
    private var lastKnownImageHeight: Int = 0  // v2.12.0-14: Cache for transformer bootstrap

    private val focusTargetTracker = com.akirachang.ai.photographer.data.focus.tracking.DefaultTargetTracker(
        iouThreshold = 0.5f,
        targetTtlMs = 300L,
    )

    private val eyeAfPolicyResolver = com.akirachang.ai.photographer.data.focus.policy.DefaultEyeAfPolicyResolver()

    private val focusStateMachine = com.akirachang.ai.photographer.presentation.camera.focus.DefaultFocusStateMachine()

    private val newFocusTriggerThrottle = com.akirachang.ai.photographer.presentation.camera.focus.FocusTriggerThrottle(
        targetLostGraceMs = 500L,
    )

    private val newPipelineGeometryMapper = com.akirachang.ai.photographer.data.focus.geometry.PreviewViewGeometryMapper()

    // v2.12.0-18 Task 3: 1-Euro Filter for primary target monopoly (visual-only smoothing)
    // Secondary targets always use raw ML Kit coords (no smoothing — saves CPU).
    private val primaryTargetSmoother = com.akirachang.ai.photographer.data.source.camera.RectFilter(
        minCutoff = 1.0f,
        beta = 0.007f,
    )
    private var lastSmoothedPrimaryId: String? = null

    private val cameraXFocusController = com.akirachang.ai.photographer.data.focus.control.CameraXFocusController(
        getCameraControl = {
            // v2.12.0-15 SV-S5a-R2: Dynamic provider via CameraRepository.getCameraControl()
            // Lifecycle-safe: returns null if camera not yet bound (no crash on cold start)
            cameraRepository.getCameraControl()
        },
        getMeteringPointFactory = {
            // v2.12.0-15 SV-S5b: Use PreviewView.meteringPointFactory via WeakReference
            // Mirrors the factory access pattern in LiveCameraSource
            previewViewRef?.get()?.meteringPointFactory
        },
    )

    private var lastNewPipelineTriggerMs: Long? = null

    // v2.12.0-15 SV-S5b: WeakReference to PreviewView for CameraXFocusController metering
    // Lifecycle-safe: WeakReference prevents memory leak; null-checked before use
    private var previewViewRef: java.lang.ref.WeakReference<androidx.camera.view.PreviewView>? = null

    // v2.12.0-15 SV-S5b: Single Job for FocusController to prevent unbounded coroutine spawning
    private var focusControllerJob: kotlinx.coroutines.Job? = null
    // Last FocusControllerResult for StateMachine re-reduce after async completion
    private var lastFocusControllerResult: com.akirachang.ai.photographer.domain.model.focus.FocusControllerResult? = null
    private var pendingTapToFocusIntent: TapToFocusIntent? = null
    private var tapClearJob: Job? = null  // SP-B: TTL active cleanup for optimistic Yellow box
    private var latestSensorToViewMatrix: android.graphics.Matrix? = null
    private var currentPtfSessionId = 0L  // PTF session token: prevents stale async results from overwriting newer taps

    // D7-B1 Rule 16 — Shutter cooldown sentinel.
    // Initial value = Long.MIN_VALUE / 2 (not 0L) so that the FIRST capture after PTF mode entry
    // always passes the cooldown check regardless of System.currentTimeMillis() value.
    private var lastCaptureTimeMs: Long = Long.MIN_VALUE / 2

    // D7-B2-T2 — Testable time provider for Rule 16 cooldown.
    // Production: System.currentTimeMillis(). Tests: inject a controllable clock.
    internal var timeProvider: () -> Long = { System.currentTimeMillis() }

    // D8-B1-T3 — EMA(α=0.3) smoothing state for PTF face bbox center.
    // Applied upstream before passing to PtfRuleAggregator (aggregator is stateless pure fn).
    // Sentinel null = no prior frame; first frame accepted as-is.
    private var ptfEmaX: Float? = null
    private var ptfEmaY: Float? = null

    // v2.x.x Phase 7: EMA removed — LK quality avg 0.91 after aspect-ratio + FPS fixes,
    // noise jitter no longer significant.  Direct raw coordinates used for zero-lag tracking.

    // Detection stabilization (v2.2.1) - prevent flickering
    private var consecutiveFaceDetections = 0
    private var consecutiveNoFaceDetections = 0
    private val MIN_DETECTIONS_TO_SHOW = 3  // Need 3 consecutive detections to show
    private val MIN_NO_DETECTIONS_TO_HIDE = 5  // Need 5 consecutive empty frames to hide

    // v2.12.0-03: Adaptive throttle state for ML Kit analysis (device mode)
    // All accessed on Main thread (viewModelScope) — no AtomicLong needed
    private var mlkitThrottleMs: Long      = ADAPTIVE_THROTTLE_DEFAULT_MS
    private var mlkitEmaMs: Double         = 0.0
    private var mlkitWarmupCount: Int      = 0
    private var mlkitProcessedFrames: Long = 0L

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


    init {
        // D0.3: Log device capability tier on VM creation (runtime wiring verification)
        android.util.Log.i(TAG, "[D0.3] DeviceCapabilityTier=$deviceCapabilityTier")

        // v2.5.1: Analyze preview bitmap stream directly (Phase 7)
        // This replaces the old ImageProxy-based analysis
        observePreviewForAnalysis()

        // v2.13.0 Ticket 2: Observe detection results for Rule-of-Thirds composition advice
        observeCompositionAdvice()

        // 訂閱偵測結果 (deprecated - will be removed once bitmap analysis is stable)
        // observeDetectionResults()

        // 訂閱光影分析結果 (v2.2.0)
        // observeLightingResults()

        // 訂閱感測器方向資料 (v2.1.0)
        observeOrientation()

        // [v2.12.0-23] 啟動陀螺儀收集 → 餵入 ImuCompensator
        startImuCompensation()

        // ML 偵測需手動啟動（透過 CameraUiEvent.StartAnalysis）
        // 或根據需求自動啟動
        // startAnalysis()  // Deprecated - now using bitmap-based analysis

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

        // v2.12.0-11: Observe fixed-focus lens capability → sync to CameraState
        viewModelScope.launch {
            cameraRepository.isFixedFocusLens.collect { isFixed ->
                _state.update { it.copy(isFixedFocusLens = isFixed) }
                Log.i(TAG, "[v2.12.0-11] isFixedFocusLens updated: $isFixed → CameraState synced")
            }
        }

        // D8-B1-T3: PTF alignment flow — active only in PassToFriend mode.
        observePtfAlignment()
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
        if (!orientationSensorManager.areSensorsAvailable()) {
            Log.w(TAG, "Orientation sensors not available - LevelIndicator will be disabled")
            return
        }

        orientationSensorManager.startOrientationTracking()
            .onEach { orientation ->
                _orientationState.value = orientation
                _state.update { it.copy(deviceRoll = orientation.roll) }
            }
            .launchIn(viewModelScope)

        Log.i(TAG, "Orientation tracking initialized")
    }

    /**
     * [v2.12.0-23] Starts gyroscope data collection and feeds each sample into ImuCompensator.
     *
     * Lifecycle: tied to viewModelScope — when the ViewModel is cleared (Activity/Fragment
     * destruction), the coroutine is automatically cancelled and the sensor listener
     * unregistered, preventing battery drain.
     *
     * Graceful degradation: [DeviceSensorManager.startGyroscopeTracking] closes the flow
     * immediately when no gyroscope hardware is present — no hasGyroscope() guard needed here.
     * Downstream ImuCompensator.consumeCameraMotion() returns (0f, 0f) when no data accumulated.
     */
    private fun startImuCompensation() {
        deviceSensorManager.startGyroscopeTracking()
            .onEach { data -> imuCompensator.accumulate(data) }
            .launchIn(viewModelScope)
        Log.i(TAG, "[v2.12.0-23] IMU compensation started")
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
            is CameraUiEvent.FocusOnPoint -> {
                setTapToFocusIntent(event.x, event.y, event.viewWidth, event.viewHeight, event.density)
                Log.i(TAG, "[SV-FIX-1] FocusOnPoint → V2 pipeline tap intent registered (${event.x}, ${event.y})")
                onTapToFocus(event.x, event.y, event.viewWidth, event.viewHeight)
            }
            is CameraUiEvent.LockAEAF -> {
                Log.i(TAG, "[v2.12.0-15 Fix] LockAEAF event received: (${event.x}, ${event.y}), isAEAFLocked=${_state.value.isAEAFLocked}")
                lockAEAF(event.x, event.y, event.viewWidth, event.viewHeight)
            }  // v2.9.9 Day 1-2
            is CameraUiEvent.UnlockAEAF -> unlockAEAF()  // v2.9.9 Day 1-2
            is CameraUiEvent.ShowPhotoInfo -> showPhotoInfo(event.uri)  // v2.11.0 Phase 3
            is CameraUiEvent.DismissPhotoInfo -> dismissPhotoInfo()  // v2.11.0 Phase 3
            is CameraUiEvent.SetCameraMode -> setCameraMode(event.mode)  // D7-B2-FIX
            is CameraUiEvent.DismissPtfOnboarding -> dismissPtfOnboarding()  // D7-B2-T3
        }
    }

    /**
     * Sets the top-level camera mode to the given [mode].
     *
     * No CameraX rebind — mode switching is a pure state-machine operation.
     * Rule 16 shutter cooldown is managed by [lastCaptureTimeMs] in [capturePhoto].
     *
     * @param mode The target [CameraMode] to transition to.
     */
    private fun setCameraMode(mode: CameraMode) {
        _state.update { current ->
            Log.i(TAG, "[CameraMode] ${current.cameraMode} → $mode")
            current.copy(cameraMode = mode)
        }
    }

    /**
     * Permanently dismisses the PTF onboarding hint — Cueshot D7-B2-T3.
     *
     * Persists [ptf_onboarded] = true via [PtfPreferences]. Subsequent PTF sessions will
     * not show the hint. No CameraState mutation needed — UI observes [isPtfOnboarded] Flow.
     */
    private fun dismissPtfOnboarding() {
        viewModelScope.launch {
            ptfPreferences.markPtfOnboarded()
            Log.i(TAG, "[PTF-Onboarding] Dismissed — will not show again")
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
        eyeAfThrottle.setScreenWidth(width)  // v2.12.0-05: update rescanThresholdPx (5% of width)
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

        // v2.12.0-14: Update cache when fresh dims are available
        if (effectiveImageWidth > 0 && effectiveImageHeight > 0) {
            lastKnownImageWidth  = effectiveImageWidth
            lastKnownImageHeight = effectiveImageHeight
        }
        val resolvedW = if (effectiveImageWidth > 0) effectiveImageWidth else lastKnownImageWidth
        val resolvedH = if (effectiveImageHeight > 0) effectiveImageHeight else lastKnownImageHeight

        // Create coordinate transformer if image dimensions are available (use cached dims as fallback)
        val transformer = if (resolvedW > 0 && resolvedH > 0) {
            com.akirachang.ai.photographer.presentation.camera.components.CoordinateTransformer(
                contentBounds = bounds,
                imageWidth = resolvedW,
                imageHeight = resolvedH,
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
        rebuildNewPipelineSensorToViewMatrix(detection, _state.value)

        Log.d(TAG, "[v2.11.2 Fix D] Content bounds updated: " +
                "left=${bounds.left}, top=${bounds.top}, " +
                "right=${bounds.right}, bottom=${bounds.bottom}, " +
                "width=${bounds.width()}, height=${bounds.height()}, " +
                "rawImage=${rawImageWidth}x${rawImageHeight}, rotation=${rotation}°, " +
                "effectiveImage=${effectiveImageWidth}x${effectiveImageHeight}, " +
                "cached=${lastKnownImageWidth}x${lastKnownImageHeight}, " +
                "transformer=${if (transformer != null) "created" else "null (no image dims)"}")
    }

    private fun capturePhoto() {
        // D7-B2-FIX Rule 16 — PTF shutter cooldown guard (1 second).
        // Guard fires ONLY when CameraMode is PassToFriend. Normal mode has no cooldown.
        if (_state.value.cameraMode is CameraMode.PassToFriend) {
            val now = timeProvider()
            if (isPtfCooldownActive(now, lastCaptureTimeMs)) {
                Log.d(TAG, "[PTF-Cooldown] Shutter blocked — ${now - lastCaptureTimeMs}ms since last capture")
                return
            }
            lastCaptureTimeMs = now
        }

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

    // v2.12.0-12: Unified focus state teardown — cancel in-flight job BEFORE state reset
    private fun resetFocusState() {
        focusJob?.cancel()
        focusSessionId++  // v2.12.0-13: Invalidate all in-flight HAL callbacks
        clearTapToFocusIntent()
        latestSensorToViewMatrix = null
        // v2.12.0-18: Reset 1-Euro smoother to prevent stale interpolation
        primaryTargetSmoother.reset()
        lastSmoothedPrimaryId = null
        _state.update {
            it.copy(
                afState = AfState.Idle,
                focusUiState = com.akirachang.ai.photographer.domain.model.focus.FocusUiState.EMPTY,
            )
        }
        eyeAfThrottle.reset()
        eyeAfController.reset()  // D6-B3: reset gaze-AF throttle on focus pipeline teardown
        lastCaptureTimeMs = Long.MIN_VALUE / 2  // D7-B2-FIX: reset PTF cooldown sentinel on focus teardown
        Log.d(TAG, "[v2.12.0-15 Fix] resetFocusState: focusUiState=EMPTY, sessionId=$focusSessionId")
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
            // v2.12.0-12: cancel focusJob BEFORE state reset to prevent Race Condition
            resetFocusState()
            Log.i(TAG, "[v2.12.0-12] switchCamera: resetFocusState() called")

            // v2.12.0-15: Reset new pipeline components on camera switch
            focusTargetTracker.reset()
            newFocusTriggerThrottle.reset()
            focusControllerJob?.cancel()
            lastNewPipelineTriggerMs = null
            lastFocusControllerResult = null
            // v2.12.0-18: Reset 1-Euro smoother (Task 3) — prevent cross-camera interpolation
            primaryTargetSmoother.reset()
            lastSmoothedPrimaryId = null
            _state.update { it.copy(focusUiState = com.akirachang.ai.photographer.domain.model.focus.FocusUiState.EMPTY) }
            Log.d(TAG, "[SV-FIX-1] New pipeline reset on camera switch")

            // v2.9.10: Clear tracking state before switching camera
            // v2.9.9 Day 1-2: Clear AE/AF lock state before switching camera
            // v2.12.0-10: Clear generic tap flag so new camera starts with neutral frame sizing.
            _state.update {
                it.copy(
                    trackedObjectId = null,
                    trackedObjectType = null,
                    isAEAFLocked = false,
                    lockPoint = null,
                    isGenericTapActive = false,
                )
            }

            // Cancel focus metering if locked
            cameraRepository.cancelFocusAndMetering()

            // [v2.12.0-23] Reset IMU accumulator — new lens has different coordinate system;
            // stale gyro integration from the previous camera session is invalid.
            imuCompensator.reset()
            Log.i(TAG, "[v2.12.0-23] switchCamera: imuCompensator.reset()")

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
     * Cycles through: 4:3 → 16:9 → 4:3
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

    // v2.12.0-10: Tap resolution result — carries screen-space bbox so the ML Kit branch can
    // update focus frame dimensions immediately (no 1-frame delay waiting for
    // updateTrackedObjectPosition), fixing Bug A (front-cam fixed frame size across distances).
    private sealed class TapTargetInfo {
        /** Tap landed on an ML Kit face or object. */
        data class Subject(
            val trackingId: Int,
            val type: TrackedObjectType,
            val screenBoxPx: android.graphics.RectF   // image bbox converted to screen space
        ) : TapTargetInfo()
        /** Tap landed on empty area — no ML Kit subject. */
        object Generic : TapTargetInfo()
    }

    // v2.12.0-10: Helper — converts an image-space Rect to a screen-space RectF using the
    // CoordinateTransformer (FIT_CENTER geometry).  Returns null when transformer is unavailable.
    // v2.12.0-11 Bug A complete fix: Normalize corners with min/max so width/height are always
    // positive. Front camera mirroring swaps left/right in image→screen mapping (sLeft > sRight),
    // which produced negative RectF.width() and caused FocusIndicator to fall back to 48dp.
    private fun imageRectToScreenRectF(
        box: android.graphics.Rect,
        transformer: com.akirachang.ai.photographer.presentation.camera.components.CoordinateTransformer
    ): android.graphics.RectF {
        val (sLeft, sTop)     = transformer.imageToScreen(box.left.toFloat(),  box.top.toFloat())
        val (sRight, sBottom) = transformer.imageToScreen(box.right.toFloat(), box.bottom.toFloat())
        return android.graphics.RectF(
            minOf(sLeft, sRight), minOf(sTop, sBottom),
            maxOf(sLeft, sRight), maxOf(sTop, sBottom)
        )
    }

    /**
     * Resolve tap into a typed TapTargetInfo (v2.12.0-10, replaces findTappedTarget).
     *
     * Returns [TapTargetInfo.Subject] when the tap hits an ML Kit face or object, carrying
     * the screen-space bounding box so callers can update focus frame dimensions immediately.
     * Returns [TapTargetInfo.Generic] when no subject is hit (OF / static focus path).
     *
     * @param x Tap X in PreviewView (screen) coordinates
     * @param y Tap Y in PreviewView (screen) coordinates
     */
    private fun resolveTapTarget(x: Float, y: Float): TapTargetInfo {
        val detectionResult = _state.value.detectionResult ?: return TapTargetInfo.Generic
        val transformer = _state.value.coordinateTransformer

        // Convert tap point from screen → image space for bbox hit-test.
        val (imageX, imageY) = if (transformer != null) {
            transformer.screenToImage(x, y) ?: run {
                Log.d(TAG, "resolveTapTarget: tap ($x, $y) outside content bounds — Generic")
                return TapTargetInfo.Generic
            }
        } else {
            Log.w(TAG, "[v2.12.0-14] resolveTapTarget: transformer not ready, returning Generic")
            return TapTargetInfo.Generic
        }

        Log.d(TAG, "resolveTapTarget: screen=($x, $y) → image=($imageX, $imageY)")

        // Priority 1: Faces (more stable tracking, Face-First Policy per L2 §1)
        detectionResult.faces.forEachIndexed { index, face ->
            if (face.boundingBox.contains(imageX.toInt(), imageY.toInt())) {
                val trackingId = face.trackingId ?: (index + 10000)
                val screenBox = imageRectToScreenRectF(face.boundingBox, transformer)
                Log.i(TAG, "resolveTapTarget: face trackingId=$trackingId, bbox=${face.boundingBox}")
                return TapTargetInfo.Subject(trackingId, TrackedObjectType.FACE, screenBox)
            }
        }

        // Priority 2: Objects
        detectionResult.objects.forEachIndexed { index, obj ->
            if (obj.boundingBox.contains(imageX.toInt(), imageY.toInt())) {
                val trackingId = obj.trackingId ?: (index + 20000)
                val screenBox = imageRectToScreenRectF(obj.boundingBox, transformer)
                Log.i(TAG, "resolveTapTarget: object trackingId=$trackingId, bbox=${obj.boundingBox}")
                return TapTargetInfo.Subject(trackingId, TrackedObjectType.OBJECT, screenBox)
            }
        }

        Log.d(TAG, "resolveTapTarget: no target at image=($imageX, $imageY) — Generic")
        return TapTargetInfo.Generic
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
            // Target found — V2 pipeline owns overlay coords; just log and reset lost counter.
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Tracking update: ID=$trackedId, image=($centerX, $centerY)")
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
                    )
                }
                targetLostFrameCount = 0
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
        // Cancel any in-flight focus coroutine to prevent stale state updates racing with lockAEAF.
        focusJob?.cancel()
        clearTapToFocusIntent()
        Log.i(TAG, "focusJob cancelled for AE/AF lock at ($x, $y)")

        viewModelScope.launch {
            try {
                Log.i(TAG, "Locking AE/AF at ($x, $y)")

                // Single atomic update: clear ALL tracking state + set lock indicator.
                _state.update {
                    it.copy(
                        trackedObjectId = null,
                        trackedObjectType = null,
                        lockPoint = Pair(x, y),
                        isAEAFLocked = false,         // Will be set to true after successful lock
                        isGenericTapActive = false,
                    )
                }

                // Execute CameraX lock (persistent focus metering without auto-cancel)
                val result = cameraRepository.lockAEAF(x, y, viewWidth, viewHeight)

                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isAEAFLocked = true,
                        )
                    }
                    Log.i(TAG, "[v2.12.0-15 Fix] AE/AF locked successfully at ($x, $y), isAEAFLocked=${_state.value.isAEAFLocked}")

                } else {
                    _state.update {
                        it.copy(
                            lockPoint = null,
                            isAEAFLocked = false
                        )
                    }
                    Log.w(TAG, "AE/AF lock failed: ${result.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "AE/AF lock error", e)
                _state.update {
                    it.copy(
                        lockPoint = null,
                        isAEAFLocked = false
                    )
                }
            }
        }
    }

    /**
     * PTF state machine entry point — drives PtfState transitions for Rule Engine coordination.
     *
     * State flow: Idle → Focusing(x,y) → Focused(x,y) or Failed(x,y) → Idle (after 2s)
     * Session token guard: each tap increments [currentPtfSessionId]; async callbacks only
     * apply if their captured sessionId still matches, preventing stale result overwrites
     * and timer-induced early resets on rapid or same-coordinate taps.
     */
    private fun onTapToFocus(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val sessionId = ++currentPtfSessionId
        _state.update { it.copy(ptfState = PtfState.Focusing(x, y)) }
        Log.d(TAG, "[PTF] Focusing at ($x, $y) session=$sessionId")

        viewModelScope.launch {
            val result = try {
                cameraRepository.focusOnPoint(x, y, viewWidth, viewHeight)
            } catch (e: Exception) {
                Log.w(TAG, "[PTF] focusOnPoint exception: ${e.message}")
                Result.failure(e)
            }

            // Defence A: skip if a newer tap has taken over
            if (sessionId != currentPtfSessionId) {
                Log.d(TAG, "[PTF] session=$sessionId superseded, dropping result")
                return@launch
            }

            val nextState = if (result.isSuccess) {
                PtfState.Focused(x, y)
            } else {
                Log.w(TAG, "[PTF] focusOnPoint failed: ${result.exceptionOrNull()?.message}")
                PtfState.Failed(x, y)
            }
            _state.update { it.copy(ptfState = nextState) }
            Log.d(TAG, "[PTF] → $nextState session=$sessionId")

            delay(2000L)

            // Defence B: skip auto-reset if a newer tap has taken over
            if (sessionId != currentPtfSessionId) {
                Log.d(TAG, "[PTF] session=$sessionId timer expired but superseded, skip reset")
                return@launch
            }
            _state.update { it.copy(ptfState = PtfState.Idle) }
            Log.d(TAG, "[PTF] → Idle (auto-reset) session=$sessionId")
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
     * @param imageWidth  Rotation-aware effective image width (swap if rotation 90/270)
     * @param imageHeight Rotation-aware effective image height (swap if rotation 90/270)
     * @return Saliency score (0-130 range; higher = more salient)
     */
    private fun calculateSaliencyScore(
        boundingBox: android.graphics.Rect,
        isFace: Boolean,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        // 1. Box size component (normalized area ratio, 0-1)
        val boxSize = (boundingBox.width() * boundingBox.height()).toFloat()
        val normalizedSize = boxSize / (imageWidth * imageHeight)

        // 2. Centrality component (distance from image center, 0-1)
        val centerX = boundingBox.exactCenterX()
        val centerY = boundingBox.exactCenterY()
        val screenCenterX = imageWidth / 2f
        val screenCenterY = imageHeight / 2f

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
        // v2.12.0-16: V2 pipeline is the only path.
        runNewFocusPipeline()
    }

    /**
     * v2.12.0-15: New focus pipeline (Step 5b).
     * Pipeline: DetectionResult → GeometryMapper → TargetTracker → EyeAfPolicyResolver
     *           → FocusController → FocusStateMachine → FocusUiState
     *
     * Called from autoFocusOnSalientTarget() (V2 pipeline, v2.12.0-16).
     * Manual tap uses setTapToFocusIntent() which is consumed on the next analysis frame.
     */
    private fun runNewFocusPipeline() {
        val currentState = _state.value
        val nowMs = System.currentTimeMillis()

        // ── Step A: Get current DetectionResult ──────────────────────────────────
        val detectionResult = currentState.detectionResult
        if (detectionResult == null) {
            val trackedState = focusTargetTracker.update(emptyList(), nowMs)
            val emptyDecision = com.akirachang.ai.photographer.domain.model.focus.EyeAfDecision(
                anchor = com.akirachang.ai.photographer.domain.model.focus.MeteringAnchor.NONE,
                visualState = com.akirachang.ai.photographer.domain.model.focus.VisualTargetState.None,
                shouldTriggerFocus = false,
                shouldTriggerMetering = false,
                allowOpticalState = false,
            )
            val focusInput = com.akirachang.ai.photographer.domain.model.focus.FocusStateInput(
                trackedTargetState = trackedState,
                eyeAfDecision = emptyDecision,
                focusControllerResult = null,
                userIntent = buildUserIntentState(currentState),
                systemEvent = com.akirachang.ai.photographer.domain.model.focus.SystemEvent.NORMAL_UPDATE,
                nowMs = nowMs,
                isFrontCamera = currentState.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT,
                isFixedFocus = currentState.isFixedFocusLens,
            )
            val newFocusUiState = focusStateMachine.reduce(focusInput)
            _state.update { it.copy(focusUiState = newFocusUiState) }
            return
        }

        // ── Step B: Build FocusTarget list from DetectionResult ──────────────────
        val rawTargets = buildFocusTargetsFromDetection(detectionResult, currentState, nowMs)

        // ── Step C: TargetTracker.update() ───────────────────────────────────────
        val trackedState = focusTargetTracker.update(rawTargets, nowMs)

        // ── Step D: Build LensCapability ─────────────────────────────────────────
        val lensCapability = buildNewPipelineLensCapability(currentState)

        // ── Step E: Build UserIntentState ────────────────────────────────────────
        val userIntent = buildUserIntentState(currentState)

        // ── Step F: EyeAfPolicyResolver.resolve() ────────────────────────────────
        val policyInput = com.akirachang.ai.photographer.domain.model.focus.EyeAfPolicyInput(
            primaryTarget = trackedState.primaryTarget,
            lensCapability = lensCapability,
            userIntent = userIntent,
            nowMs = nowMs,
            lastFocusTriggerMs = lastNewPipelineTriggerMs,
        )
        val eyeAfDecision = eyeAfPolicyResolver.resolve(policyInput)

        // ── Step F.5: FocusTriggerThrottle gate ──────────────────────────────────
        // v2.12.0-16: VisualLocked no longer bypasses HAL. Throttle is the only trigger gate.
        val primaryTarget = trackedState.primaryTarget
        if (primaryTarget == null) {
            newFocusTriggerThrottle.onTargetLost()
        } else {
            newFocusTriggerThrottle.onTargetReacquired()
        }

        val eyeX = primaryTarget?.leftEyeView?.x
            ?: primaryTarget?.rightEyeView?.x
            ?: primaryTarget?.rectView?.centerX()
            ?: 0f
        val eyeY = primaryTarget?.leftEyeView?.y
            ?: primaryTarget?.rightEyeView?.y
            ?: primaryTarget?.rectView?.centerY()
            ?: 0f

        // v2.12.0-15 Fix: VisualLocked short-circuit — do NOT spam HAL when already locked.
        // Sony XQ-BT52 Continuous AF: isFocusSuccessful=false → throttle never armed → infinite loop.
        val shouldTrigger =
            (eyeAfDecision.shouldTriggerFocus || eyeAfDecision.shouldTriggerMetering) &&
            newFocusTriggerThrottle.shouldTrigger(
                eyeScreenX = eyeX,
                eyeScreenY = eyeY,
                currentVisualState = eyeAfDecision.visualState,
            )

        val effectiveDecision = if (!shouldTrigger) {
            eyeAfDecision.copy(shouldTriggerFocus = false, shouldTriggerMetering = false)
        } else {
            eyeAfDecision
        }

        // ── Step G: FocusController.apply() (Job-controlled async) ───────────────
        if (effectiveDecision.shouldTriggerFocus || effectiveDecision.shouldTriggerMetering) {
            val geometryContext = buildNewPipelineGeometryContext(currentState) ?: return

            // SP-C: In-Flight lock — skip passive AF re-trigger if HAL job is still active.
            // Only a user tap (isTapToFocusActive) may preempt and cancel the running job.
            // This prevents the self-cancellation loop on Sony XQ-BT52 where each ML Kit frame
            // cancels the previous AF coroutine before it completes.
            val isJobActive = focusControllerJob?.isActive == true
            if (isJobActive && !userIntent.isTapToFocusActive) {
                Log.v(TAG, "[SP-C] Skipping AF request: previous job still in-flight (passive tracking)")
                // Fall through to Step H for synchronous UI update — do NOT cancel the active job
            } else {
                // v2.12.0-15 SV-S5b: Cancel previous job to prevent unbounded coroutine spawning
                focusControllerJob?.cancel()
                focusControllerJob = viewModelScope.launch {
                    try {
                        val result = cameraXFocusController.apply(
                            decision = effectiveDecision,
                            primaryTarget = trackedState.primaryTarget,
                            geometryContext = geometryContext,
                        )
                        lastNewPipelineTriggerMs = nowMs
                        lastFocusControllerResult = result
                        Log.d(TAG, "[v2.12.0-15] FocusController result: triggerSent=${result.triggerSent}, optical=${result.opticalResult}")
                        // v2.12.0-15 SV-S5b: onSuccess/onFailure throttle feedback
                        if (result.triggerSent) {
                            when (result.opticalResult) {
                                com.akirachang.ai.photographer.domain.model.focus.OpticalFocusState.FOCUSED ->
                                    newFocusTriggerThrottle.onSuccess()
                                com.akirachang.ai.photographer.domain.model.focus.OpticalFocusState.NOT_FOCUSED ->
                                    newFocusTriggerThrottle.onFailure(1)
                                // v2.12.0-15 Fix: Continuous AF (PASSIVE_CONTINUOUS) — arm throttle to prevent
                                // infinite re-trigger. Sony XQ-BT52 returns PASSIVE_CONTINUOUS, not FOCUSED.
                                com.akirachang.ai.photographer.domain.model.focus.OpticalFocusState.PASSIVE_CONTINUOUS ->
                                    newFocusTriggerThrottle.onSuccess()
                                else -> Unit
                            }
                        }
                        // v2.12.0-15 SV-S5b: Re-reduce StateMachine with actual controller result
                        val nowMsAsync = System.currentTimeMillis()
                        val updatedInput = com.akirachang.ai.photographer.domain.model.focus.FocusStateInput(
                            trackedTargetState = trackedState,
                            eyeAfDecision = effectiveDecision,
                            focusControllerResult = result,
                            userIntent = buildUserIntentState(_state.value),
                            systemEvent = com.akirachang.ai.photographer.domain.model.focus.SystemEvent.NORMAL_UPDATE,
                            nowMs = nowMsAsync,
                            isFrontCamera = _state.value.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT,
                            isFixedFocus = _state.value.isFixedFocusLens,
                        )
                        val updatedUiState = focusStateMachine.reduce(updatedInput)
                        // v2.12.0-18 Task 3: Apply 1-Euro smoother (async path)
                        val smoothedAsync = applySmoother(updatedUiState, trackedState.primaryTarget?.stableId, nowMsAsync)
                        _state.update { it.copy(focusUiState = smoothedAsync) }
                        Log.d(TAG, "[v2.12.0-15] Re-reduced after FocusController: lens=${updatedUiState.lensFocusState}")
                    } catch (e: CancellationException) {
                        // SP-C Task 2: Graceful degradation — CancellationException means a user tap
                        // preempted this passive AF job. Not a HAL failure; do NOT update throttle state.
                        Log.d(TAG, "[SP-C] FocusController job cancelled gracefully — not a HAL failure")
                        throw e  // re-throw to properly mark coroutine as cancelled
                    } catch (e: Exception) {
                        Log.w(TAG, "[v2.12.0-15] FocusController.apply() failed: ${e.message}")
                    }
                }
            }
        }

        // ── Step H: FocusStateMachine.reduce() (sync, uses last known controller result) ──
        val focusInput = com.akirachang.ai.photographer.domain.model.focus.FocusStateInput(
            trackedTargetState = trackedState,
            eyeAfDecision = effectiveDecision,
            focusControllerResult = lastFocusControllerResult,  // v2.12.0-15 SV-S5b: use last known result
            userIntent = userIntent,
            systemEvent = com.akirachang.ai.photographer.domain.model.focus.SystemEvent.NORMAL_UPDATE,
            nowMs = nowMs,
            isFrontCamera = currentState.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT,
            isFixedFocus = currentState.isFixedFocusLens,
        )
        val newFocusUiState = focusStateMachine.reduce(focusInput)

        // ── Step I: Apply 1-Euro smoother then update state ───────────────────────
        // v2.12.0-18 Task 3: Primary monopoly — only primaryTarget gets smooth filter.
        // Secondary boxes in OverlayModel use raw ML Kit coords (no smoothing).
        val smoothedUiState = applySmoother(newFocusUiState, trackedState.primaryTarget?.stableId, nowMs)
        _state.update { it.copy(focusUiState = smoothedUiState) }
        Log.d(TAG, "[v2.12.0-15] Pipeline: visual=${newFocusUiState.visualTargetState}, lens=${newFocusUiState.lensFocusState}")
    }

    /**
     * v2.12.0-15: Build FocusTarget list from current DetectionResult.
     * DetectionResult.faces is List<Face> (ML Kit), face.boundingBox is in image coordinates.
     * Uses CoordinateTransformer.imageToScreen() to derive view-space RectF.
     */
    private fun buildFocusTargetsFromDetection(
        detectionResult: DetectionResult,
        currentState: CameraState,
        nowMs: Long,
    ): List<com.akirachang.ai.photographer.domain.model.focus.FocusTarget> {
        val targets = mutableListOf<com.akirachang.ai.photographer.domain.model.focus.FocusTarget>()
        // v2.12.0-15 SV-S5b: Use GeometryMapper (SSOT) instead of legacy coordinateTransformer
        val geometryContext = buildNewPipelineGeometryContext(currentState) ?: return emptyList()

        // Convert faces
        detectionResult.faces.forEachIndexed { index, face ->
            val box = face.boundingBox
            val stableId = if (face.trackingId != null) "face_${face.trackingId}"
            else "face_gen_${nowMs}_$index"

            val rectImage = android.graphics.RectF(box)
            val rectView = newPipelineGeometryMapper.mapRectImageToView(
                android.graphics.RectF(box),
                geometryContext,
            )

            val leftEyeView = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)
                ?.position
                ?.let { pt ->
                    newPipelineGeometryMapper.mapPointImageToView(
                        android.graphics.PointF(pt.x, pt.y),
                        geometryContext,
                    )
                }
            val rightEyeView = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)
                ?.position
                ?.let { pt ->
                    newPipelineGeometryMapper.mapPointImageToView(
                        android.graphics.PointF(pt.x, pt.y),
                        geometryContext,
                    )
                }

            targets.add(
                com.akirachang.ai.photographer.domain.model.focus.FocusTarget(
                    stableId = stableId,
                    type = com.akirachang.ai.photographer.domain.model.focus.TargetType.FACE,
                    rectImage = rectImage,
                    rectView = rectView,
                    leftEyeView = leftEyeView,
                    rightEyeView = rightEyeView,
                    confidence = 1.0f,  // ML Kit Face has no per-face confidence score
                    priorityScore = 1.5f,  // Faces have higher priority than objects
                    trackingId = face.trackingId,
                    firstSeenMs = nowMs,
                    lastSeenMs = nowMs,
                    source = com.akirachang.ai.photographer.domain.model.focus.TargetSource.ML_KIT_FACE,
                )
            )
        }

        // Convert objects
        detectionResult.objects.forEachIndexed { index, obj ->
            val box = obj.boundingBox
            val stableId = if (obj.trackingId != null) "object_${obj.trackingId}"
            else "object_gen_${nowMs}_$index"

            val rectImage = android.graphics.RectF(box)
            val rectView = newPipelineGeometryMapper.mapRectImageToView(
                android.graphics.RectF(box),
                geometryContext,
            )

            val confidence = obj.labels.firstOrNull()?.confidence ?: 0.5f

            targets.add(
                com.akirachang.ai.photographer.domain.model.focus.FocusTarget(
                    stableId = stableId,
                    type = com.akirachang.ai.photographer.domain.model.focus.TargetType.OBJECT,
                    rectImage = rectImage,
                    rectView = rectView,
                    leftEyeView = null,
                    rightEyeView = null,
                    confidence = confidence,
                    priorityScore = confidence,
                    trackingId = obj.trackingId,
                    firstSeenMs = nowMs,
                    lastSeenMs = nowMs,
                    source = com.akirachang.ai.photographer.domain.model.focus.TargetSource.ML_KIT_OBJECT,
                )
            )
        }

        return targets
    }

    /**
     * v2.12.0-15: Build LensCapability from current camera state.
     */
    private fun buildNewPipelineLensCapability(
        currentState: CameraState,
    ): com.akirachang.ai.photographer.domain.model.focus.LensCapability {
        return com.akirachang.ai.photographer.domain.model.focus.LensCapability(
            isFrontCamera = currentState.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT,
            isFixedFocus = currentState.isFixedFocusLens,
            supportsAutoFocus = !currentState.isFixedFocusLens,
            supportsContinuousAf = !currentState.isFixedFocusLens,
            supportsAeAfLock = true,
        )
    }

    /**
     * v2.12.0-15: Build UserIntentState from current camera state.
     */
    private fun buildUserIntentState(
        currentState: CameraState,
    ): com.akirachang.ai.photographer.domain.model.focus.UserIntentState {
        val tapIntent = currentTapToFocusIntent()
        return com.akirachang.ai.photographer.domain.model.focus.UserIntentState(
            tapPointView = tapIntent?.pointView,
            tapTimestampMs = tapIntent?.timestampMs,
            isTapToFocusActive = tapIntent != null,
            isAeAfLockRequested = currentState.isAEAFLocked,
        )
    }

    /**
     * v2.12.0-15: Build GeometryContext from current camera state.
     * Note: CameraState.coordinateTransformer is the legacy presentation-layer transformer.
     * sensorToViewMatrix is not available from it — PreviewViewGeometryMapper falls back to FIT_CENTER.
     */
    private fun buildNewPipelineGeometryContext(
        currentState: CameraState,
    ): com.akirachang.ai.photographer.domain.model.focus.GeometryContext? {
        val detection = currentState.detectionResult
        val rawImgW = detection?.imageWidth ?: lastKnownImageWidth
        val rawImgH = detection?.imageHeight ?: lastKnownImageHeight
        val rotation = detection?.rotationDegrees ?: 0
        // SP-1: effectiveImgW/H derived from imageProxy dimensions with rotation swap — never from UI layer
        val effectiveImgW = if (rotation == 90 || rotation == 270) rawImgH else rawImgW
        val effectiveImgH = if (rotation == 90 || rotation == 270) rawImgW else rawImgH
        val contentBounds = currentState.contentBounds
        // SP-3: Use only true PreviewView size; drop the erroneous max(previewWidth, contentBounds.right) fallback
        val resolvedPreviewW = previewWidth ?: 0
        val resolvedPreviewH = previewHeight ?: 0
        // SP-A guard: abort early on degenerate image dimensions — do not construct a poisoned GeometryContext
        if (effectiveImgW <= 0 || effectiveImgH <= 0) {
            Log.e(TAG, "[CoordSystem] Invalid image dimensions: img=${effectiveImgW}x${effectiveImgH} " +
                "(raw=${rawImgW}x${rawImgH}, rot=${rotation}°) — returning null, caller must skip geometry ops")
            return null
        }
        if (resolvedPreviewW == 0 || resolvedPreviewH == 0) {
            Log.w(TAG, "[CoordSystem] PreviewView size is unknown, fallback path triggered")
        }
        val context = com.akirachang.ai.photographer.domain.model.focus.GeometryContext(
            imageWidth = effectiveImgW,
            imageHeight = effectiveImgH,
            previewWidth = resolvedPreviewW,
            previewHeight = resolvedPreviewH,
            sensorToBufferMatrix = null,
            sensorToViewMatrix = latestSensorToViewMatrix?.let { android.graphics.Matrix(it) },
            isFrontCamera = currentState.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT,
            isMirroredPreview = currentState.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT,
        )
        // SP-4: Mandatory diagnostic log — coordinate system ground truth snapshot
        Log.i(TAG, "[CoordSystem] imageProxy=${effectiveImgW}x${effectiveImgH} " +
            "(raw=${rawImgW}x${rawImgH}, rot=${rotation}°) " +
            "preview=${resolvedPreviewW}x${resolvedPreviewH} " +
            "contentBounds=[L=${contentBounds.left}, T=${contentBounds.top}, " +
            "W=${contentBounds.width()}, H=${contentBounds.height()}]")
        return context
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
                // v2.11.4-05 (rev2): Guard — skip if not locked.
                // Without this, ON_STOP (screen lock) fires UnlockAEAF even when no lock is active,
                // causing unnecessary cancelFocusAndMetering() + Camera2 option changes that disturb
                // the HAL and introduce a delay on the next long-press lock attempt after screen unlock.
                if (!_state.value.isAEAFLocked) {
                    // v2.12.0-15 Fix: Upgrade to WARNING to distinguish from normal skip (ON_STOP lifecycle)
                    // If this fires after a long-press, it means lockAEAF() failed to set isAEAFLocked=true.
                    Log.w(TAG, "[v2.12.0-15 Fix] unlockAEAF skipped — isAEAFLocked=false (expected if ON_STOP lifecycle, UNEXPECTED if after long-press)")
                    return@launch
                }

                Log.i(TAG, "Unlocking AE/AF")

                // Cancel focus metering to return to auto mode
                cameraRepository.cancelFocusAndMetering()

                // Clear lock state and indicators
                // v2.12.0-10: Also clear generic tap flag to restore neutral frame sizing.
                _state.update {
                    it.copy(
                        isAEAFLocked = false,
                        lockPoint = null,
                        isGenericTapActive = false,
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
                    val hasSubjects = hasFaces || result.objects.isNotEmpty()

                    if (hasSubjects) {
                        consecutiveFaceDetections = if (hasFaces) consecutiveFaceDetections + 1 else 0
                        consecutiveNoFaceDetections = 0

                        // Only update state if we have enough consecutive detections
                        // or if we already have a stable detection
                        val shouldPublish = !hasFaces ||
                            consecutiveFaceDetections >= MIN_DETECTIONS_TO_SHOW ||
                            _state.value.detectionResult?.faces?.isNotEmpty() == true
                        if (shouldPublish) {
                            _state.update { it.copy(detectionResult = result) }

                            // v2.11.2 Fix F: Rebuild CoordinateTransformer on first detection with valid dims.
                            // updateContentBounds() runs at layout time (before ML Kit), so transformer is null.
                            // Now that imageWidth/imageHeight are available, recreate with correct dimensions.
                            if (result.imageWidth > 0 && _state.value.coordinateTransformer == null &&
                                _state.value.contentBounds.width() > 0) {
                                updateContentBounds(_state.value.contentBounds)
                            }
                            rebuildNewPipelineSensorToViewMatrix(result, _state.value)

                            // Perform composition analysis (v1.5.0)
                            analyzeComposition(result)
                            autoFocusOnSalientTarget()

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
     * Observes preview bitmap stream for ML Kit analysis (v2.5.1 Phase 7)
     *
     * Replaces the old ImageProxy-based analysis with unified Bitmap analysis.
     * This enables test images and live camera to use the same analysis pipeline.
     */
    @OptIn(FlowPreview::class)
    private fun observePreviewForAnalysis() {
        previewBitmap
            .filterNotNull()
            // v2.12.0-03: sample at MIN rate; EWMA adds delay() after processing for adaptive pacing
            // NOTE: conflate() was tried but StateFlow.distinctUntilChanged + BitmapPool object reuse
            //       caused the channel to stop emitting. sample() uses a timer and is BitmapPool-safe.
            .sample(ADAPTIVE_THROTTLE_MIN_MS)
            .onEach { bitmap ->
                try {
                    // SP-A Heartbeat: log every 30 frames to confirm analysis pipeline is alive
                    if (mlkitProcessedFrames % 30L == 0L) {
                        Log.i("SmartCameraAnalyzer", "[SP-A Heartbeat] bitmap analysis alive frame=$mlkitProcessedFrames")
                    }

                    // v2.8.4: End-to-end profiling
                    val e2eStart = System.nanoTime()

                    // D3-T3: All tiers: hardware-first (CameraCharacteristics), fail-safe = previewWidth / 2f (hFov≈90°).
                    //   factor = focalLengthMm / sensorWidthMm; null until after first camera bind.
                    //   Fail-safe: if factor unavailable, assume hFov≈90° → f ≈ previewWidth / 2f.
                    //     previewWidth / 2f corresponds to hFov ≈ 90° (tan(45°)=1).
                    //   If previewWidth = 0, result = 0f — sentinel for "not ready";
                    //     ImuCompensator safely returns (0,0) with no motion applied.
                    val focalLengthPixels: Float = computeFocalLengthPixels(
                        factor = cameraRepository.getFocalLengthFactor(),
                        previewWidth = previewWidth ?: 0
                    )
                    val (imuDx, imuDy) = imuCompensator.consumeCameraMotion(focalLengthPixels = focalLengthPixels)
                    Log.i(TAG, "[D0.3] DeviceCapabilityTier=$deviceCapabilityTier focalLengthPixels=$focalLengthPixels imuDx=$imuDx imuDy=$imuDy")

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
                        val hasSubjects = hasFaces || detectionResult.objects.isNotEmpty()

                        if (hasSubjects) {
                            consecutiveFaceDetections = if (hasFaces) consecutiveFaceDetections + 1 else 0
                            consecutiveNoFaceDetections = 0

                            // Only update state if we have enough consecutive detections
                            val shouldPublish = !hasFaces ||
                                consecutiveFaceDetections >= MIN_DETECTIONS_TO_SHOW ||
                                _state.value.detectionResult?.faces?.isNotEmpty() == true
                            if (shouldPublish) {
                                _state.update { it.copy(detectionResult = detectionResult) }

                                // v2.11.2 Fix F: Rebuild CoordinateTransformer on first detection with valid dims
                                if (detectionResult.imageWidth > 0 && _state.value.coordinateTransformer == null &&
                                    _state.value.contentBounds.width() > 0) {
                                    updateContentBounds(_state.value.contentBounds)
                                }
                                rebuildNewPipelineSensorToViewMatrix(detectionResult, _state.value)

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
                                rebuildNewPipelineSensorToViewMatrix(detectionResult, _state.value)

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

                    // v2.8.4: End-to-end profiling summary
                    val e2eNs    = System.nanoTime() - e2eStart        // v2.12.0-03: ns for EWMA
                    val e2eTotal = e2eNs / 1_000_000.0
                    Log.i(TAG, "[v2.8.4 E2E] Total=${"%.1f".format(e2eTotal)}ms " +
                               "(Convert=${"%.1f".format(convertTime)}ms, " +
                               "MLKit=${"%.1f".format(mlkitTime)}ms, " +
                               "Composition=${"%.1f".format(compositionTime)}ms, " +
                               "Lighting=${"%.1f".format(lightingTime)}ms)")
                    updateMlkitAdaptiveThrottle(e2eNs)                 // v2.12.0-03: update EWMA
                    // v2.12.0-03: Adaptive pacing — add delay if throttle demands slower rate
                    val extraMs = mlkitThrottleMs - (e2eNs / 1_000_000L)
                    if (extraMs > 0L) delay(extraMs)
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
     * v2.12.0-03: Update adaptive throttle for CameraViewModel ML Kit analysis (device mode).
     *
     * Algorithm identical to SmartCameraAnalyzer.updateAdaptiveThrottle():
     *   1. Warmup: skip first ADAPTIVE_WARMUP_FRAMES to exclude JIT/cold-start spikes
     *   2. EWMA: smooth measured E2E latency with α=0.2
     *   3. Panic backoff: if measured > interval×PANIC_RATIO → immediate scale-up
     *   4. Target = EWMA × SAFETY_FACTOR (1.25)
     *   5. Deadband: skip if |target - current| < current × 10%
     *   6. Step-rate: cap change to +50ms / -20ms per frame
     *   7. Clamp: [100ms, 500ms]
     */
    private fun updateMlkitAdaptiveThrottle(measuredNs: Long) {
        val measuredMs = measuredNs / 1_000_000.0

        if (mlkitWarmupCount < ADAPTIVE_WARMUP_FRAMES) {
            mlkitWarmupCount++
            return
        }

        mlkitEmaMs = if (mlkitEmaMs == 0.0) measuredMs
                     else ADAPTIVE_EMA_ALPHA * measuredMs + (1.0 - ADAPTIVE_EMA_ALPHA) * mlkitEmaMs

        val currentMs = mlkitThrottleMs.toDouble()
        mlkitProcessedFrames++

        if (measuredMs > currentMs * ADAPTIVE_PANIC_RATIO) {
            val panicMs = (currentMs * ADAPTIVE_PANIC_SCALE).toLong()
                .coerceIn(ADAPTIVE_THROTTLE_MIN_MS, ADAPTIVE_THROTTLE_MAX_MS)
            mlkitThrottleMs = panicMs
            if (mlkitProcessedFrames % ADAPTIVE_LOG_EVERY_N_FRAMES == 0L) {
                Log.i(TAG, "[v2.12.0-03] PANIC throttle=${panicMs}ms measured=${measuredMs.toInt()}ms EMA=${mlkitEmaMs.toInt()}ms")
            }
            return
        }

        val targetMs = (mlkitEmaMs * ADAPTIVE_SAFETY_FACTOR).toLong()
            .coerceIn(ADAPTIVE_THROTTLE_MIN_MS, ADAPTIVE_THROTTLE_MAX_MS)

        // Log every N frames unconditionally — shows convergence even when deadband is stable
        if (mlkitProcessedFrames % ADAPTIVE_LOG_EVERY_N_FRAMES == 0L) {
            Log.i(TAG, "[v2.12.0-03] throttle=${mlkitThrottleMs}ms target=${targetMs}ms EMA=${mlkitEmaMs.toInt()}ms measured=${measuredMs.toInt()}ms frames=$mlkitProcessedFrames")
        }

        if (Math.abs(targetMs - currentMs) < currentMs * ADAPTIVE_DEADBAND_RATIO) return

        val newMs = when {
            targetMs > currentMs -> (currentMs + ADAPTIVE_MAX_EXPAND_MS).toLong()
                .coerceAtMost(targetMs).coerceIn(ADAPTIVE_THROTTLE_MIN_MS, ADAPTIVE_THROTTLE_MAX_MS)
            else                 -> (currentMs - ADAPTIVE_MAX_CONTRACT_MS).toLong()
                .coerceAtLeast(targetMs).coerceIn(ADAPTIVE_THROTTLE_MIN_MS, ADAPTIVE_THROTTLE_MAX_MS)
        }

        mlkitThrottleMs = newMs
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
        // Cueshot D2: Composition analysis fully migrated to observeCompositionAdvice() pipeline.
        // This legacy path retains NoSubject for the v2.x CompositionState UI channel (not used by D2+ UI).
        _compositionState.value = com.akirachang.ai.photographer.presentation.camera.model.CompositionState.NoSubject
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
     * Cueshot D2: Live composition rule engine pipeline.
     *
     * Observes [CameraState.detectionResult] and [_orientationState] per detection frame.
     * Normalizes face bounding boxes to [0..1] viewport space, then invokes
     * [analyzeCompositionUseCase] with the current IMU pitch/roll.
     *
     * No debounce or temporal smoothing — raw, single-frame aggregation only (D5 task).
     *
     * Guard clauses:
     *   - No detectionResult → skip, last compositionResult retained.
     *   - imageWidth or imageHeight <= 0 → skip (invalid geometry).
     */
    private fun observeCompositionAdvice() {
        _state
            .map { it.detectionResult }
            .filterNotNull()
            .onEach { det ->
                val imgW = det.imageWidth.toFloat()
                val imgH = det.imageHeight.toFloat()
                if (imgW <= 0f || imgH <= 0f) return@onEach

                val landmarkFaces = det.faces.mapIndexed { idx, face ->
                    val box = face.boundingBox
                    val boundingBoxNormalized = android.graphics.RectF(
                        box.left / imgW,
                        box.top / imgH,
                        box.right / imgW,
                        box.bottom / imgH,
                    )
                    val leftEyeNormalized = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)
                        ?.position
                        ?.let { pt -> android.graphics.PointF(pt.x / imgW, pt.y / imgH) }
                    val rightEyeNormalized = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)
                        ?.position
                        ?.let { pt -> android.graphics.PointF(pt.x / imgW, pt.y / imgH) }

                    com.akirachang.ai.photographer.domain.model.FaceLandmarkData(
                        boundingBoxNormalized = boundingBoxNormalized,
                        leftEyeNormalized = leftEyeNormalized,
                        rightEyeNormalized = rightEyeNormalized,
                        headEulerAngleY = face.headEulerAngleY,
                    )
                }

                val orientation = _orientationState.value
                val pitch = orientation?.pitch ?: 0f
                val roll = orientation?.roll ?: 0f

                val result = analyzeCompositionUseCase(landmarkFaces, pitch, roll)
                _state.update { it.copy(compositionResult = result, detectedFaces = landmarkFaces) }
                Log.d(TAG, "[D2] CompositionResult: isAllAligned=${result.isAllAligned} topRule=${result.topInstruction?.name}")

                // D6-B3: Gaze-driven Eye-AF — trigger AF when gaze unaligned, back camera only.
                // Front camera guard is enforced inside EyeAfController (first line of defense).
                val currentState = _state.value
                val gazeAfTrigger = eyeAfController.process(
                    compositionResult = result,
                    faces = landmarkFaces,
                    facing = currentState.currentCamera,
                    previewWidth = previewWidth ?: 0,
                    previewHeight = previewHeight ?: 0,
                )
                if (gazeAfTrigger != null) {
                    val pWidth = previewWidth ?: 0
                    val pHeight = previewHeight ?: 0
                    Log.i(TAG, "[D6-B3] Gaze AF trigger: stableId=${gazeAfTrigger.stableId} " +
                        "point=(${gazeAfTrigger.viewPoint.x}, ${gazeAfTrigger.viewPoint.y})")
                    viewModelScope.launch {
                        try {
                            cameraRepository.focusOnPoint(
                                x = gazeAfTrigger.viewPoint.x,
                                y = gazeAfTrigger.viewPoint.y,
                                viewWidth = pWidth,
                                viewHeight = pHeight,
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "[D6-B3] Gaze AF focusOnPoint failed: ${e.message}")
                        }
                    }
                }
            }
            .catch { e -> Log.e(TAG, "[D2] observeCompositionAdvice error", e) }
            .launchIn(viewModelScope)

        Log.i(TAG, "[D2] Composition rule engine pipeline initialized (raw, no debounce)")
    }

    /**
     * D8-B1-T3: PTF alignment pipeline.
     *
     * Observes [CameraState.detectionResult] and [CameraState.cameraMode] in lockstep.
     * When [CameraMode.PassToFriend] is active, applies EMA(α=0.3) to the first detected
     * face's bbox center, then feeds into [PtfRuleAggregator] AND-Gate.
     *
     * Guard clauses:
     *   - cameraMode != PassToFriend → emit NO_FACE sentinel, reset EMA state.
     *   - No faces detected → PtfRuleAggregator receives null face → NO_FACE sentinel.
     *   - imageWidth or imageHeight <= 0 → skip (invalid geometry, retain last result).
     */
    private fun observePtfAlignment() {
        // Clear EMA state at subscription start to prevent stale values from a prior session.
        ptfEmaX = null
        ptfEmaY = null
        _state
            .map { Pair(it.detectionResult, it.cameraMode) }
            .distinctUntilChanged()
            .onEach { (det, mode) ->
                if (mode !is CameraMode.PassToFriend) {
                    // Reset EMA state on mode exit to avoid stale smoothing on next PTF entry.
                    ptfEmaX = null
                    ptfEmaY = null
                    _state.update { it.copy(ptfAlignmentResult = PtfAlignmentResult.NO_FACE) }
                    return@onEach
                }

                if (det == null) return@onEach
                val imgW = det.imageWidth.toFloat()
                val imgH = det.imageHeight.toFloat()
                if (imgW <= 0f || imgH <= 0f) return@onEach

                val orientation = _orientationState.value
                val pitch = orientation?.pitch ?: 0f
                val roll = orientation?.roll ?: 0f

                val firstFace = det.faces.firstOrNull()
                val smoothedFace = if (firstFace != null) {
                    // Compute raw normalized bbox center from ML Kit pixel coords.
                    val rawCx = ((firstFace.boundingBox.left + firstFace.boundingBox.right) / 2f) / imgW
                    val rawCy = ((firstFace.boundingBox.top + firstFace.boundingBox.bottom) / 2f) / imgH

                    // EMA(α=0.3): first frame passes through as-is (null sentinel).
                    val smoothedCx = ptfEmaX?.let { PTF_EMA_ALPHA * rawCx + (1f - PTF_EMA_ALPHA) * it } ?: rawCx
                    val smoothedCy = ptfEmaY?.let { PTF_EMA_ALPHA * rawCy + (1f - PTF_EMA_ALPHA) * it } ?: rawCy
                    ptfEmaX = smoothedCx
                    ptfEmaY = smoothedCy

                    val halfW = ((firstFace.boundingBox.right - firstFace.boundingBox.left) / 2f) / imgW
                    val halfH = ((firstFace.boundingBox.bottom - firstFace.boundingBox.top) / 2f) / imgH
                    com.akirachang.ai.photographer.domain.model.FaceLandmarkData(
                        boundingBoxNormalized = android.graphics.RectF(
                            smoothedCx - halfW,
                            smoothedCy - halfH,
                            smoothedCx + halfW,
                            smoothedCy + halfH,
                        ),
                        leftEyeNormalized = null,
                        rightEyeNormalized = null,
                        headEulerAngleY = firstFace.headEulerAngleY,
                    )
                } else {
                    ptfEmaX = null
                    ptfEmaY = null
                    null
                }

                val ptfResult = ptfRuleAggregator.aggregate(
                    face = smoothedFace,
                    devicePitch = pitch,
                    deviceRoll = roll,
                )
                _state.update { it.copy(ptfAlignmentResult = ptfResult) }
                Log.d(TAG, "[D8-B1-T3] PTF: isAligned=${ptfResult.isAligned} " +
                    "offset=(${ptfResult.offset.dx},${ptfResult.offset.dy}) " +
                    "failed=${ptfResult.failedRules}")
            }
            .catch { e -> Log.e(TAG, "[D8-B1-T3] observePtfAlignment error", e) }
            .launchIn(viewModelScope)

        Log.i(TAG, "[D8-B1-T3] PTF alignment pipeline initialized")
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
        // v2.12.0-15 SV-S5b: Store WeakReference for CameraXFocusController
        previewViewRef = if (previewView != null) java.lang.ref.WeakReference(previewView) else null
        Log.d(TAG, "[v2.12.0-15] previewViewRef updated: ${previewView != null}")

        // v2.11.1 P0-3 FIX: Get actual PreviewView content bounds after layout
        previewView?.post {
            val contentBounds = getActualPreviewContentBounds(previewView)
            if (contentBounds != null) {
                updateContentBounds(contentBounds)
                rebuildNewPipelineSensorToViewMatrix(_state.value.detectionResult, _state.value)
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

    private fun clearTapToFocusIntent() {
        tapClearJob?.cancel()  // SP-B: Disarm TTL timer when intent is consumed or reset
        tapClearJob = null
        pendingTapToFocusIntent = null
    }

    /**
     * SP-B: Expire manual tap intent and clear the optimistic yellow placeholder if it is still visible.
     *
     * This path is shared by the TTL coroutine and any late read that notices the intent already aged out,
     * so UI cleanup still happens even if pendingTapToFocusIntent was cleared first.
     */
    private fun expireTapToFocusIntent(reason: String, cancelTimer: Boolean) {
        if (cancelTimer) {
            tapClearJob?.cancel()
            tapClearJob = null
        }

        val hadPendingIntent = pendingTapToFocusIntent != null
        pendingTapToFocusIntent = null

        val isStillPlaceholder =
            _state.value.focusUiState.overlayModel?.primaryStableId == "tap_optimistic"
        if (isStillPlaceholder) {
            Log.v(TAG, "[SP-B] $reason — clearing optimistic Yellow box.")
            _state.update {
                it.copy(
                    focusUiState = com.akirachang.ai.photographer.domain.model.focus.FocusUiState.EMPTY,
                )
            }
        } else if (hadPendingIntent) {
            Log.v(TAG, "[SP-B] $reason — real tracking active, placeholder already replaced.")
        }
    }

    /**
     * v2.12.0-18 Task 3: Apply 1-Euro Filter to primary target box (visual rendering only).
     *
     * Primary monopoly rule:
     *   - primaryTarget.rectView → smoothed via RectFilter (jitter suppression + motion tracking)
     *   - secondaryBoxes remain as-is (raw ML Kit coords, no CPU overhead)
     *
     * Resets filter when primaryStableId changes (avoids interpolation across different subjects).
     */
    private fun applySmoother(
        uiState: com.akirachang.ai.photographer.domain.model.focus.FocusUiState,
        primaryStableId: String?,
        nowMs: Long,
    ): com.akirachang.ai.photographer.domain.model.focus.FocusUiState {
        val primaryBox = uiState.overlayModel?.primaryBox
        if (uiState.visualTargetState == com.akirachang.ai.photographer.domain.model.focus.VisualTargetState.None || primaryBox == null) {
            primaryTargetSmoother.reset()
            lastSmoothedPrimaryId = null
            return uiState
        }
        // Reset when target identity changes to prevent cross-subject interpolation
        if (primaryStableId != lastSmoothedPrimaryId) {
            primaryTargetSmoother.reset()
            lastSmoothedPrimaryId = primaryStableId
        }
        val smoothedBox = primaryTargetSmoother.filter(primaryBox, nowMs)
        return uiState.copy(
            overlayModel = uiState.overlayModel.copy(primaryBox = smoothedBox)
        )
    }

    /**
     * v2.12.0-16 SB-F1: Register a tap-to-focus intent for V2 pipeline consumption.
     * v2.12.0-18: Immediately push optimistic 72dp Yellow placeholder (0ms delay, Task 1).
     *
     * Yellow here signals "tap registered / pending face confirmation", NOT AE/AF Lock.
     * The next pipeline frame (~100ms) replaces this with Cyan VisualLocked (if face found)
     * or Cyan TrackingStable (texture/tap) via the normal FocusStateMachine path.
     */
    private fun setTapToFocusIntent(x: Float, y: Float, viewWidth: Int, viewHeight: Int, density: Float = 3f) {
        pendingTapToFocusIntent = TapToFocusIntent(
            pointView = PointF(x, y),
            timestampMs = SystemClock.elapsedRealtime(),
            density = density,
        )
        // v2.12.0-18 Task 1: Optimistic UI — zero-delay Yellow 72dp placeholder at tap coords.
        val halfPx = 72f * density / 2f
        val placeholder = android.graphics.RectF(x - halfPx, y - halfPx, x + halfPx, y + halfPx)
        _state.update {
            it.copy(
                focusUiState = com.akirachang.ai.photographer.domain.model.focus.FocusUiState(
                    visualTargetState = com.akirachang.ai.photographer.domain.model.focus.VisualTargetState.TrackingStable,
                    lensFocusState = com.akirachang.ai.photographer.domain.model.focus.LensFocusState.ActiveScan,
                    exposureLockState = com.akirachang.ai.photographer.domain.model.focus.ExposureLockState.Unlocked,
                    overlayModel = com.akirachang.ai.photographer.domain.model.focus.OverlayModel(
                        primaryStableId = "tap_optimistic",
                        primaryBox = placeholder,
                        secondaryBoxes = emptyList(),
                        primaryColor = com.akirachang.ai.photographer.domain.model.focus.OverlayColor.YELLOW,
                        alpha = 1.0f,
                        showLockIcon = false,
                        animation = com.akirachang.ai.photographer.domain.model.focus.OverlayAnimation.PULSE,
                    ),
                )
            )
        }
        Log.d(TAG, "[v2.12.0-18] Optimistic tap: Yellow 72dp at ($x, $y) density=$density halfPx=$halfPx")

        // [SP-B] Active TTL cleanup: if no detection consumes this intent within TAP_INTENT_WINDOW_MS,
        // proactively clear the optimistic Yellow box so it never freezes on screen.
        // Guard: only wipe UI if the placeholder is still showing — real tracking (Cyan) must NOT be cleared.
        tapClearJob?.cancel()
        tapClearJob = viewModelScope.launch {
            delay(TAP_INTENT_WINDOW_MS)
            expireTapToFocusIntent(
                reason = "Tap intent TTL expired",
                cancelTimer = false,
            )
        }
    }

    private fun currentTapToFocusIntent(nowMs: Long = SystemClock.elapsedRealtime()): TapToFocusIntent? {
        val intent = pendingTapToFocusIntent ?: return null
        if ((nowMs - intent.timestampMs) > TAP_INTENT_WINDOW_MS) {
            expireTapToFocusIntent(
                reason = "Tap intent expired during pipeline read",
                cancelTimer = true,
            )
            return null
        }
        return intent
    }

    private fun rebuildNewPipelineSensorToViewMatrix(
        detectionResult: DetectionResult?,
        currentState: CameraState,
    ) {
        val rawImgW = detectionResult?.imageWidth ?: lastKnownImageWidth
        val rawImgH = detectionResult?.imageHeight ?: lastKnownImageHeight
        val rotation = detectionResult?.rotationDegrees ?: 0
        val effectiveImgW = if (rotation == 90 || rotation == 270) rawImgH else rawImgW
        val effectiveImgH = if (rotation == 90 || rotation == 270) rawImgW else rawImgH

        if (effectiveImgW <= 0 || effectiveImgH <= 0) {
            latestSensorToViewMatrix = null
            Log.w(TAG, "[v2.12.0-16 SB-F2] No image dimensions — sensorToViewMatrix=null")
            return
        }

        // [v2.12.0-17][SV-FIX-3R-FINAL] Delegate matrix construction to PreviewViewGeometryMapper.
        // V2 pipeline owns its own geometry logic — no dependency on presentation-layer CoordinateTransformer.
        val contentBounds = currentState.contentBounds
        val isFrontCamera = currentState.currentCamera == com.akirachang.ai.photographer.domain.model.CameraLens.FRONT
        if (!contentBounds.isEmpty) {
            latestSensorToViewMatrix = newPipelineGeometryMapper.createSensorToViewMatrix(
                contentBounds  = contentBounds,
                imageWidth     = effectiveImgW,
                imageHeight    = effectiveImgH,
                isFrontCamera  = isFrontCamera,
            )
            Log.d(TAG, "[v2.12.0-17][SV-FIX-3R-FINAL] sensorToViewMatrix from Mapper.createSensorToViewMatrix()")
            return
        }

        // contentBounds not ready: do not set matrix — PreviewViewGeometryMapper owns the FIT_CENTER fallback.
        latestSensorToViewMatrix = null
        Log.d(TAG, "[v2.12.0-17] contentBounds not ready — sensorToViewMatrix=null, Mapper will use FIT_CENTER fallback")
    }

    private fun AfState.toLegacyVisualState(): com.akirachang.ai.photographer.domain.model.focus.VisualTargetState {
        return when (this) {
            is AfState.Focusing -> com.akirachang.ai.photographer.domain.model.focus.VisualTargetState.Candidate
            is AfState.Locked -> com.akirachang.ai.photographer.domain.model.focus.VisualTargetState.VisualLocked
            else -> com.akirachang.ai.photographer.domain.model.focus.VisualTargetState.None
        }
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

    override fun onCleared() {
        super.onCleared()
        // Reset new pipeline components on ViewModel cleared
        focusTargetTracker.reset()
        newFocusTriggerThrottle.reset()
        // Clear PTF EMA state and reset alignment result on ViewModel destruction.
        ptfEmaX = null
        ptfEmaY = null
        _state.update { it.copy(ptfAlignmentResult = PtfAlignmentResult.NO_FACE) }
    }

}
