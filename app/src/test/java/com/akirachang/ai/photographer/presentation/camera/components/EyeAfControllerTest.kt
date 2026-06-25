package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.PointF
import android.graphics.RectF
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.RuleScore
import com.akirachang.ai.photographer.domain.model.guidance.GuidanceState
import com.akirachang.ai.photographer.presentation.camera.AfState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])

/**
 * Unit tests for [EyeAfController] — D6-B3.
 *
 * Coverage:
 *   Test 1 — Front camera: process() always returns null (Constitution §7).
 *   Test 2 — Gaze aligned: process() suppresses trigger.
 *   Test 3 — Gaze unaligned, back camera: trigger is emitted with correct coordinates.
 *   Test 4 — Throttle: second call within cooldown window suppressed; fires after cooldown.
 *   Test 5 — Empty faces: trigger suppressed even when gaze is unaligned.
 *   Test 6 — Null compositionResult: trigger suppressed.
 *   Test 7 — Missing eye landmarks: falls back to bounding-box center.
 *   Test 8 — reset() clears throttle; trigger fires immediately after reset.
 */
class EyeAfControllerTest {

    private lateinit var controller: EyeAfController

    private val previewWidth = 1080
    private val previewHeight = 1920

    // A face with both eye landmarks available
    private val faceWithEyes = FaceLandmarkData(
        boundingBoxNormalized = RectF(0.3f, 0.2f, 0.7f, 0.6f),
        leftEyeNormalized = PointF(0.4f, 0.35f),
        rightEyeNormalized = PointF(0.6f, 0.35f),
        headEulerAngleY = 20f,
    )

    // A face with no eye landmarks (ML Kit classification failure)
    private val faceWithoutEyes = FaceLandmarkData(
        boundingBoxNormalized = RectF(0.3f, 0.2f, 0.7f, 0.6f),
        leftEyeNormalized = null,
        rightEyeNormalized = null,
        headEulerAngleY = 20f,
    )

    private fun unalignedGazeResult(): CompositionResult = CompositionResult(
        ruleScores = mapOf(GazeRule.NAME to RuleScore(name = GazeRule.NAME, score = 30f, isAligned = false, weight = 1f)),
        topInstruction = null,
        isAllAligned = false,
    )

    private fun alignedGazeResult(): CompositionResult = CompositionResult(
        ruleScores = mapOf(GazeRule.NAME to RuleScore(name = GazeRule.NAME, score = 100f, isAligned = true, weight = 1f)),
        topInstruction = null,
        isAllAligned = true,
    )

    @Before
    fun setUp() {
        controller = EyeAfController()
    }

    // ── Test 1: Constitution §7 — front camera never triggers AF ─────────────

    @Test
    fun `front camera always suppresses trigger regardless of gaze alignment`() {
        val trigger = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.FRONT,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("Front camera must never produce an EyeAfTrigger", trigger)
    }

    // ── Test 2: Gaze aligned — no trigger ────────────────────────────────────

    @Test
    fun `gaze aligned suppresses trigger`() {
        val trigger = controller.process(
            compositionResult = alignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("Aligned gaze must not trigger AF", trigger)
    }

    // ── Test 3: Gaze unaligned, back camera — trigger with correct coords ────

    @Test
    fun `gaze unaligned on back camera produces trigger with correct eye midpoint`() {
        val trigger = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNotNull("Unaligned gaze on back camera must produce a trigger", trigger)

        // Eye midpoint: leftEye=(0.4, 0.35), rightEye=(0.6, 0.35) → avg=(0.5, 0.35)
        // In pixels: x = 0.5 * 1080 = 540, y = 0.35 * 1920 = 672
        val expectedX = (0.4f + 0.6f) / 2f * previewWidth   // 540f
        val expectedY = 0.35f * previewHeight                 // 672f
        assertEquals("Eye midpoint X should be ${expectedX}", expectedX, trigger!!.viewPoint.x, 0.1f)
        assertEquals("Eye midpoint Y should be ${expectedY}", expectedY, trigger.viewPoint.y, 0.1f)
    }

    // ── Test 4: Throttle — suppress within cooldown, allow after ─────────────

    @Test
    fun `second call within cooldown window is suppressed`() {
        val firstTrigger = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNotNull("First call must fire", firstTrigger)

        val secondTrigger = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS - 1L,
        )
        assertNull("Call within cooldown must be suppressed", secondTrigger)
    }

    @Test
    fun `call after cooldown window fires again`() {
        controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )

        val afterCooldown = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNotNull("Call after cooldown must fire", afterCooldown)
    }

    // ── Test 4b: Guard 7 boundary — drift=0 preserves stableId ──────────────

    @Test
    fun `guard 7 boundary drift zero preserves stableId`() {
        val first = controller.process(
            compositionResult = unalignedGazeResult(), faces = listOf(faceWithEyes),
            facing = CameraLens.BACK, previewWidth = previewWidth, previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNotNull("First trigger must fire", first)

        val second = controller.process(
            compositionResult = unalignedGazeResult(), faces = listOf(faceWithEyes),
            facing = CameraLens.BACK, previewWidth = previewWidth, previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNotNull("Drift=0 after cooldown must still fire", second)
        assertEquals("Drift=0: stableId must be preserved", first!!.stableId, second!!.stableId)
    }

    // ── Test 4c: Guard 7 boundary — drift just below threshold preserves stableId

    @Test
    fun `guard 7 boundary drift just below threshold preserves stableId`() {
        val first = controller.process(
            compositionResult = unalignedGazeResult(), faces = listOf(faceWithEyes),
            facing = CameraLens.BACK, previewWidth = previewWidth, previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNotNull(first)

        // drift = 0.04 < DRIFT_THRESHOLD_NORMALIZED (0.05) → preserve stableId
        val faceBelowThreshold = faceWithEyes.copy(
            leftEyeNormalized = PointF(0.4f + 0.028f, 0.35f + 0.028f),  // sqrt(0.028²+0.028²) ≈ 0.040
            rightEyeNormalized = PointF(0.6f + 0.028f, 0.35f + 0.028f),
        )
        val second = controller.process(
            compositionResult = unalignedGazeResult(), faces = listOf(faceBelowThreshold),
            facing = CameraLens.BACK, previewWidth = previewWidth, previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNotNull("Drift below threshold must still fire", second)
        assertEquals("Drift < threshold: stableId must be preserved", first!!.stableId, second!!.stableId)
    }

    // ── Test 4d: Guard 7 boundary — drift above threshold generates new stableId

    @Test
    fun `guard 7 boundary drift above threshold generates new stableId`() {
        val first = controller.process(
            compositionResult = unalignedGazeResult(), faces = listOf(faceWithEyes),
            facing = CameraLens.BACK, previewWidth = previewWidth, previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNotNull(first)

        // drift = 0.1 > DRIFT_THRESHOLD_NORMALIZED (0.05) → new stableId
        val faceAboveThreshold = faceWithEyes.copy(
            leftEyeNormalized = PointF(0.4f, 0.35f + 0.1f),
            rightEyeNormalized = PointF(0.6f, 0.35f + 0.1f),
        )
        val second = controller.process(
            compositionResult = unalignedGazeResult(), faces = listOf(faceAboveThreshold),
            facing = CameraLens.BACK, previewWidth = previewWidth, previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNotNull("Drift above threshold must fire", second)
        assertNotEquals("Drift > threshold: stableId must be new", first!!.stableId, second!!.stableId)
    }

    // ── Test 5: Empty faces suppresses trigger ────────────────────────────────

    @Test
    fun `empty faces list suppresses trigger`() {
        val trigger = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = emptyList(),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("Empty faces must not trigger AF", trigger)
    }

    // ── Test 6: Null compositionResult suppresses trigger ─────────────────────

    @Test
    fun `null compositionResult suppresses trigger`() {
        val trigger = controller.process(
            compositionResult = null,
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("Null compositionResult must not trigger AF", trigger)
    }

    // ── Test 7: Missing eye landmarks falls back to bounding-box center ───────

    @Test
    fun `missing eye landmarks falls back to bounding-box center`() {
        val midpoint = controller.resolveEyeMidpointNormalized(faceWithoutEyes)

        // Bounding box: left=0.3, top=0.2, right=0.7, bottom=0.6 → center=(0.5, 0.4)
        assertEquals("Fallback X should be bbox center", 0.5f, midpoint.x, 0.001f)
        assertEquals("Fallback Y should be bbox center", 0.4f, midpoint.y, 0.001f)
    }

    // ── Test 8: reset() clears throttle ──────────────────────────────────────

    @Test
    fun `reset clears throttle so next call fires immediately`() {
        controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        val beforeReset = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1100L,
        )
        assertNull("Within cooldown must be suppressed", beforeReset)
        controller.reset()
        val afterReset = controller.process(
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1100L,
        )
        assertNotNull("After reset, trigger must fire regardless of elapsed time", afterReset)
    }

    // ── STATE-CONTRACT-01 AI-5: Guard 0 — guidanceState gate ─────────────────

    @Test
    fun `guard 0 NoFace suppresses trigger`() {
        val trigger = controller.process(
            guidanceState = GuidanceState.NoFace,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("NoFace must suppress AF trigger", trigger)
    }

    @Test
    fun `guard 0 FacesDetectedNoneEligible suppresses trigger`() {
        val trigger = controller.process(
            guidanceState = GuidanceState.FacesDetectedNoneEligible,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("FacesDetectedNoneEligible must suppress AF trigger", trigger)
    }

    @Test
    fun `guard 0 Aligned suppresses trigger`() {
        val trigger = controller.process(
            guidanceState = GuidanceState.Aligned,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("Aligned must suppress AF trigger", trigger)
    }

    @Test
    fun `guard 0 Suppressed suppresses trigger`() {
        val trigger = controller.process(
            guidanceState = GuidanceState.Suppressed(com.akirachang.ai.photographer.domain.model.guidance.SuppressionReason.AF_IN_PROGRESS),
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNull("Suppressed must suppress AF trigger", trigger)
    }

    @Test
    fun `guard 0 GuidanceActive allows trigger`() {
        val trigger = controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        assertNotNull("GuidanceActive must allow AF trigger", trigger)
    }

    // ── STATE-CONTRACT-01 AI-5: Guard 7a — skip-when-focused ─────────────────

    @Test
    fun `guard 7a locked plus stable drift skips re-trigger`() {
        // First call arms the trigger point
        controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            afState = AfState.Idle,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        // Same face, afState=Locked, same position → Guard 7a must skip
        val trigger = controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            afState = AfState.Locked,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNull("Locked + stable drift must skip re-trigger (Guard 7a)", trigger)
    }

    @Test
    fun `guard 7a locked plus large drift allows re-trigger`() {
        controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            afState = AfState.Idle,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        // drift = 0.1 > DRIFT_THRESHOLD_NORMALIZED (0.05) — subject moved, trigger allowed
        val movedFace = faceWithEyes.copy(
            leftEyeNormalized = PointF(0.4f, 0.35f + 0.1f),
            rightEyeNormalized = PointF(0.6f, 0.35f + 0.1f),
        )
        val trigger = controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(movedFace),
            facing = CameraLens.BACK,
            afState = AfState.Locked,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNotNull("Locked + large drift must allow re-trigger (Guard 7a)", trigger)
    }

    @Test
    fun `guard 7a idle state does not skip trigger even with stable drift`() {
        controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            afState = AfState.Idle,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L,
        )
        // Same position but afState=Idle — Guard 7a must NOT apply
        val trigger = controller.process(
            guidanceState = GuidanceState.GuidanceActive,
            compositionResult = unalignedGazeResult(),
            faces = listOf(faceWithEyes),
            facing = CameraLens.BACK,
            afState = AfState.Idle,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            nowMs = 1000L + EyeAfController.GAZE_AF_COOLDOWN_MS,
        )
        assertNotNull("Idle + stable drift must still allow trigger (Guard 7a inactive)", trigger)
    }
}
