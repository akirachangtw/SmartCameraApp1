package com.akirachang.ai.photographer.presentation.camera

import android.graphics.Rect
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import com.akirachang.ai.photographer.data.preferences.PtfPreferences
import com.akirachang.ai.photographer.data.source.camera.CameraCapabilityDetector
import com.akirachang.ai.photographer.domain.camera.ImuCompensator
import com.akirachang.ai.photographer.domain.composition.PtfRuleAggregator
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.model.DeviceCapabilityTier
import com.akirachang.ai.photographer.domain.model.RuleScore
import com.akirachang.ai.photographer.domain.sensor.DeviceSensorManager
import com.akirachang.ai.photographer.domain.sensor.OrientationSensorManager
import com.akirachang.ai.photographer.domain.usecase.AnalyzeCompositionUseCase
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.components.EyeAfController
import com.akirachang.ai.photographer.presentation.camera.fakes.FakeCameraRepository
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * STATE-CONTRACT-03 AI-2 — GAP-07 afState producer-wiring integration tests.
 *
 * Drives the REAL [CameraViewModel] end-to-end through a controllable ML Kit
 * detection flow (Path A) → composition pipeline → [EyeAfController] → the
 * AI-1 focusOnPoint dispatch site, and asserts the resulting [AfState]
 * transitions. Per CSO Option A ruling, afState is NEVER injected by the test:
 * every assertion is reached only via the production wiring path (Hard
 * Constraint 2 / 鐵律 9 — the test stays green ONLY if the producer is wired).
 *
 * ## Why no `runTest { }`
 * The real ViewModel's `observePreviewForAnalysis()` starts a `Flow.sample()`
 * whose `fixedPeriodTicker` reschedules itself forever on the test dispatcher's
 * scheduler. `runTest`'s terminal `advanceUntilIdle()` (and any explicit one)
 * spins the virtual clock into that ticker's infinite future → livelock (100%
 * CPU, never returns; confirmed by thread-dump forensics). This suite therefore
 * drives the pipeline with [TestCoroutineScheduler.runCurrent] only, which
 * executes tasks at the CURRENT virtual time and never advances into the
 * ticker's next tick. The whole AF critical path (detection → composition →
 * focusOnPoint → afState) is zero-delay dispatch, so `runCurrent()` fully drains
 * it. Guard 6's throttle is driven by the injected [fakeClock] (real millis via
 * the Option-D `timeProvider` seam), which is independent of virtual time — so no
 * virtual-time advancement is ever needed.
 *
 * Test 2 vs Test 3 form a causal differential pair on the same [fakeClock]
 * timeline (second frame always lands past the throttle window): the ONLY
 * difference is afState (Locked vs Failed), which alone decides whether Guard 7a
 * suppresses the re-trigger — proving a Locked afState (GAP-07 fix) resurrects it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CameraViewModelTestGazeAfWiring {

    private val dispatcher = StandardTestDispatcher()
    private val scheduler = dispatcher.scheduler

    private lateinit var repo: FakeCameraRepository
    private lateinit var analyzeComposition: AnalyzeCompositionUseCase
    private lateinit var analyzeLighting: AnalyzeLightingUseCase
    private lateinit var deviceSensorManager: DeviceSensorManager
    private lateinit var orientationSensorManager: OrientationSensorManager
    private lateinit var overlayPreferences: OverlayPreferences
    private lateinit var ptfPreferences: PtfPreferences
    private lateinit var capabilityDetector: CameraCapabilityDetector
    private lateinit var imuCompensator: ImuCompensator
    private lateinit var ptfRuleAggregator: PtfRuleAggregator

    private lateinit var viewModel: CameraViewModel

    /** Injected throttle clock (drives EyeAfController Guard 6 via timeProvider seam). */
    private var fakeClock = BASE_CLOCK

    /** Single reused face → identical eye midpoint across frames → drift ≈ 0 (< 0.05). */
    private lateinit var face: Face

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        ShadowLog.clear()

        repo = FakeCameraRepository()
        analyzeComposition = Mockito.mock(AnalyzeCompositionUseCase::class.java)
        analyzeLighting = Mockito.mock(AnalyzeLightingUseCase::class.java)
        deviceSensorManager = Mockito.mock(DeviceSensorManager::class.java)
        orientationSensorManager = Mockito.mock(OrientationSensorManager::class.java)
        overlayPreferences = Mockito.mock(OverlayPreferences::class.java)
        ptfPreferences = Mockito.mock(PtfPreferences::class.java)
        capabilityDetector = Mockito.mock(CameraCapabilityDetector::class.java)
        imuCompensator = Mockito.mock(ImuCompensator::class.java)
        ptfRuleAggregator = Mockito.mock(PtfRuleAggregator::class.java)

        // init-block subscriptions must register without crashing.
        Mockito.`when`(ptfPreferences.isPtfOnboarded).thenReturn(flowOf(false))
        Mockito.`when`(overlayPreferences.aspectRatioMode).thenReturn(flowOf("RATIO_16_9"))
        Mockito.`when`(overlayPreferences.isAutoHDREnabled).thenReturn(flowOf(false))
        Mockito.`when`(orientationSensorManager.areSensorsAvailable()).thenReturn(false)
        Mockito.`when`(deviceSensorManager.startGyroscopeTracking()).thenReturn(emptyFlow())

        // Composition = one non-aligned gaze rule: score > 0.1 (passes Guard 4) and
        // weighted score 50 < ALIGNED_EXIT_SCORE (65) so guidance settles on GuidanceActive.
        val gazeScore = RuleScore(name = GazeRule.NAME, score = 50f, isAligned = false, weight = 1f)
        val composition = CompositionResult(
            topInstruction = gazeScore,
            ruleScores = mapOf(GazeRule.NAME to gazeScore),
            isAllAligned = false,
        )
        Mockito.`when`(
            analyzeComposition.invoke(
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
            ),
        ).thenReturn(composition)

        face = Mockito.mock(Face::class.java).also { f ->
            Mockito.`when`(f.boundingBox).thenReturn(Rect(100, 100, 300, 300))
            Mockito.`when`(f.rightEyeOpenProbability).thenReturn(0.95f)
            Mockito.`when`(f.leftEyeOpenProbability).thenReturn(0.95f)
            // getLandmark left unstubbed → null → EyeAfController falls back to bbox center.
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Test 1 (behavioural · 假過疫苗) ─────────────────────────────────────────
    // Gaze AF fires through the real pipeline → focusOnPoint succeeds → Locked afState.
    @Test
    fun `gaze AF success drives afState to Locked via production wiring`() {
        driveToFirstArm()

        assertEquals("focusOnPoint must dispatch exactly once", 1, repo.focusOnPointCallCount)
        assertEquals(
            "afState must reach Locked purely through AI-1 wiring (no injection)",
            AfState.Locked,
            viewModel.state.value.afState,
        )
    }

    // ── Test 2 (behavioural · Guard 7a 復活證明) ────────────────────────────────
    // After Locked, a stable second frame past the throttle window must NOT re-dispatch,
    // and Guard 7a must be the gate that suppresses it (positive ShadowLog assertion).
    @Test
    fun `locked plus stable subject suppresses re-trigger via Guard 7a`() {
        driveToFirstArm()
        assertEquals(AfState.Locked, viewModel.state.value.afState)

        ShadowLog.clear()
        // Advance throttle clock past Guard 6 window so Guard 7a becomes the deciding gate.
        fakeClock = BASE_CLOCK + EyeAfController.GAZE_AF_COOLDOWN_MS + 1L
        repo.detectionResultsFlow.value = frame(ts = 5L)
        scheduler.runCurrent()

        assertEquals(
            "Locked + stable subject must not re-dispatch focusOnPoint",
            1,
            repo.focusOnPointCallCount,
        )
        val guard7aHit = ShadowLog.getLogs().any { log ->
            log.tag == "EyeAfController" && log.msg.contains("[Guard7a]")
        }
        assertTrue(
            "Guard 7a must be the suppressing gate (proves the Locked afState reached it)",
            guard7aHit,
        )
        assertEquals(AfState.Locked, viewModel.state.value.afState)
    }

    // ── Test 3 (negative · Failed 不卡死) ───────────────────────────────────────
    // Same throttle timeline as Test 2; only afState differs (Failed, not Locked).
    // Guard 7a must NOT apply → re-trigger fires → afState remains re-triggerable.
    @Test
    fun `failed focus is re-triggerable on next frame past throttle`() {
        repo.focusShouldThrow = true
        driveToFirstArm()
        assertEquals(1, repo.focusOnPointCallCount)
        assertTrue(
            "focus exception must drive afState to Failed",
            viewModel.state.value.afState is AfState.Failed,
        )

        // Same throttle-window offset as Test 2; only variable vs Test 2 is afState.
        fakeClock = BASE_CLOCK + EyeAfController.GAZE_AF_COOLDOWN_MS + 1L
        repo.detectionResultsFlow.value = frame(ts = 5L)
        scheduler.runCurrent()

        assertEquals(
            "Failed state must not stick — next eligible frame re-triggers focus",
            2,
            repo.focusOnPointCallCount,
        )
        assertTrue(viewModel.state.value.afState is AfState.Failed)
    }

    // ── Harness ────────────────────────────────────────────────────────────────

    /**
     * Builds the real ViewModel and drives four distinct detection frames — enough
     * for the guidance reducer to walk NoFace → SubjectStabilizing×3 → GuidanceActive,
     * at which point Guard 0 opens and the first AF trigger arms at [BASE_CLOCK].
     *
     * Uses [scheduler].runCurrent() only (never advanceUntilIdle) — see class KDoc.
     */
    private fun driveToFirstArm() {
        viewModel = buildViewModel()
        scheduler.runCurrent() // let init subscriptions register (collectors suspend)
        viewModel.updatePreviewSize(1080, 1920) // Guard 5: preview dims > 0
        viewModel.timeProvider = { fakeClock } // Guard 6 throttle clock seam
        fakeClock = BASE_CLOCK

        for (ts in 1L..4L) {
            repo.detectionResultsFlow.value = frame(ts)
            scheduler.runCurrent()
        }
    }

    private fun buildViewModel(): CameraViewModel = CameraViewModel(
        cameraRepository = repo,
        analyzeCompositionUseCase = analyzeComposition,
        analyzeLightingUseCase = analyzeLighting,
        deviceSensorManager = deviceSensorManager,
        orientationSensorManager = orientationSensorManager,
        overlayPreferences = overlayPreferences,
        ptfPreferences = ptfPreferences,
        capabilityDetector = capabilityDetector,
        imuCompensator = imuCompensator,
        deviceCapabilityTier = DeviceCapabilityTier.FULL,
        ptfRuleAggregator = ptfRuleAggregator,
    )

    /** Distinct timestamp → StateFlow emits & distinctUntilChanged passes; face reused → stable position. */
    private fun frame(ts: Long) = DetectionResult(
        faces = listOf(face),
        imageWidth = 1000,
        imageHeight = 1000,
        rotationDegrees = 0,
        timestamp = ts,
    )

    companion object {
        private const val BASE_CLOCK = 100_000L
    }
}
