package com.akirachang.ai.photographer.presentation.camera

import com.akirachang.ai.photographer.domain.camera.CameraMode
import com.akirachang.ai.photographer.domain.config.GuidanceStateConstants
import com.akirachang.ai.photographer.domain.model.guidance.GuidanceState
import com.akirachang.ai.photographer.domain.model.guidance.SuppressionReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D3-T4 + D7-B2-T2 Negative tests:
 *   - Boundary defence for focal length computation and device roll state (D3-T4, KAIROS Rule 10).
 *   - Rule 16 PTF shutter cooldown validation (D7-B2-T2, KAIROS Rule 16).
 *
 * Scope: CameraViewModel companion object pure functions — no DI required.
 *        CameraState pure data class — no DI required.
 */
class CameraViewModelTest {

    // -----------------------------------------------------------------------------------------
    // computeFocalLengthPixels — focal length boundary tests
    // -----------------------------------------------------------------------------------------

    @Test
    fun `computeFocalLengthPixels returns 0f when previewWidth is 0 and factor is null`() {
        // Sentinel contract: 0f tells ImuCompensator "not ready" → returns (0,0) silently.
        val result = CameraViewModel.computeFocalLengthPixels(factor = null, previewWidth = 0)
        assertEquals(0f, result, 0f)
    }

    @Test
    fun `computeFocalLengthPixels returns 0f when previewWidth is 0 and factor is provided`() {
        // Even if hardware factor is available, width=0 means layout not measured yet.
        // fail-safe: 0 / 2f = 0f (hardware path skipped because width <= 0).
        val result = CameraViewModel.computeFocalLengthPixels(factor = 0.8f, previewWidth = 0)
        assertEquals(0f, result, 0f)
    }

    @Test
    fun `computeFocalLengthPixels uses fail-safe (width div 2f) when factor is null`() {
        // Fail-safe path: camera unbound, no factor available.
        // Expected: previewWidth / 2f = 1920 / 2f = 960f
        val result = CameraViewModel.computeFocalLengthPixels(factor = null, previewWidth = 1920)
        assertEquals(960f, result, 0f)
    }

    @Test
    fun `computeFocalLengthPixels uses hardware-first path when factor is available`() {
        // Hardware-first path: factor * previewWidth
        // factor = 0.8f, previewWidth = 1920 → 1920 * 0.8f = 1536f
        val result = CameraViewModel.computeFocalLengthPixels(factor = 0.8f, previewWidth = 1920)
        assertEquals(1536f, result, 0.001f)
    }

    @Test
    fun `computeFocalLengthPixels result is never NaN`() {
        // NaN guard: all boundary inputs must produce finite results.
        val inputs = listOf(
            Pair(null, 0),
            Pair(null, 1920),
            Pair(0.0f, 0),
            Pair(0.0f, 1920),
            Pair(0.8f, 0),
            Pair(0.8f, 1920),
        )
        for ((factor, width) in inputs) {
            val result = CameraViewModel.computeFocalLengthPixels(factor, width)
            assertTrue(
                "Expected finite result for factor=$factor width=$width, got $result",
                result.isFinite()
            )
        }
    }

    @Test
    fun `computeFocalLengthPixels with very small factor does not underflow to NaN`() {
        // Extreme hardware factor (physically implausible but must not crash).
        val result = CameraViewModel.computeFocalLengthPixels(factor = Float.MIN_VALUE, previewWidth = 1920)
        assertTrue("Result must be finite", result.isFinite())
        assertTrue("Result must be non-negative", result >= 0f)
    }

    // -----------------------------------------------------------------------------------------
    // CameraState.deviceRoll — roll angle boundary tests
    // -----------------------------------------------------------------------------------------

    @Test
    fun `CameraState deviceRoll accepts extreme positive roll without exception`() {
        // +180 degrees: device fully inverted — must not throw, must store exact value.
        val state = CameraState(deviceRoll = 180f)
        assertEquals(180f, state.deviceRoll, 0f)
    }

    @Test
    fun `CameraState deviceRoll accepts extreme negative roll without exception`() {
        // -180 degrees: device fully inverted in opposite direction.
        val state = CameraState(deviceRoll = -180f)
        assertEquals(-180f, state.deviceRoll, 0f)
    }

    @Test
    fun `CameraState deviceRoll defaults to 0f`() {
        // Default state must be level (0 degrees roll).
        val state = CameraState()
        assertEquals(0f, state.deviceRoll, 0f)
    }

    @Test
    fun `CameraState deviceRoll copy preserves other fields`() {
        // Updating roll via copy() must not contaminate unrelated fields.
        val original = CameraState(isCapturing = true, deviceRoll = 0f)
        val updated = original.copy(deviceRoll = 90f)
        assertEquals(90f, updated.deviceRoll, 0f)
        assertTrue("isCapturing must be preserved", updated.isCapturing)
    }

    // -----------------------------------------------------------------------------------------
    // Rule 16 — PTF shutter cooldown (D7-B2-T2)
    // Validates CameraViewModel.isPtfCooldownActive() pure function.
    // Three-step contract: sentinel allows first shot → mid-window blocks → post-window allows.
    // -----------------------------------------------------------------------------------------

    @Test
    fun `isPtfCooldownActive returns false for sentinel initial value (first capture must pass)`() {
        // Rule 16 sentinel contract: initial lastCaptureTimeMs = Long.MIN_VALUE / 2.
        // Any realistic "now" value minus the sentinel must be >> 1000ms, so cooldown is NOT active.
        val sentinel = Long.MIN_VALUE / 2
        val now = System.currentTimeMillis()
        val result = CameraViewModel.isPtfCooldownActive(
            now = now,
            lastCaptureTimeMs = sentinel
        )
        assertFalse(
            "First capture after PTF entry must not be blocked — sentinel $sentinel, now $now",
            result
        )
    }

    @Test
    fun `isPtfCooldownActive_inPtfMode_enforces1000msCooldown`() {
        // Simulates the three-step Rule 16 cooldown sequence using a virtual clock.
        // Step 1: First capture at t=0 — must pass (sentinel allows it).
        // Step 2: Second capture at t=500ms — must be blocked (cooldown not elapsed).
        // Step 3: Third capture at t=1001ms — must pass (cooldown elapsed).

        val sentinel = Long.MIN_VALUE / 2
        val t0 = 1_000_000L  // Arbitrary epoch base; avoids sentinel-proximity edge case.

        // Step 1 — first shot uses sentinel, always passes.
        val firstBlocked = CameraViewModel.isPtfCooldownActive(
            now = t0,
            lastCaptureTimeMs = sentinel
        )
        assertFalse("Step 1: first capture must not be blocked by sentinel", firstBlocked)

        // Record capture time (mirrors ViewModel: lastCaptureTimeMs = now).
        val lastCapture = t0

        // Step 2 — 500ms later, cooldown (1000ms) has NOT elapsed → must be blocked.
        val midWindowBlocked = CameraViewModel.isPtfCooldownActive(
            now = t0 + 500L,
            lastCaptureTimeMs = lastCapture
        )
        assertTrue("Step 2: capture at 500ms must be blocked (cooldown = 1000ms)", midWindowBlocked)

        // Step 3 — 1001ms later, cooldown has elapsed → must pass.
        val postWindowBlocked = CameraViewModel.isPtfCooldownActive(
            now = t0 + 1001L,
            lastCaptureTimeMs = lastCapture
        )
        assertFalse("Step 3: capture at 1001ms must not be blocked", postWindowBlocked)
    }

    @Test
    fun `isPtfCooldownActive returns false exactly at cooldown boundary (1000ms elapsed)`() {
        // Boundary: exactly 1000ms elapsed → now - last = 1000, which is NOT < 1000 → not active.
        val lastCapture = 5_000_000L
        val result = CameraViewModel.isPtfCooldownActive(
            now = lastCapture + 1000L,
            lastCaptureTimeMs = lastCapture
        )
        assertFalse("Capture exactly at 1000ms boundary must not be blocked", result)
    }

    @Test
    fun `isPtfCooldownActive returns true one millisecond before boundary`() {
        // One millisecond before: now - last = 999 < 1000 → cooldown still active.
        val lastCapture = 5_000_000L
        val result = CameraViewModel.isPtfCooldownActive(
            now = lastCapture + 999L,
            lastCaptureTimeMs = lastCapture
        )
        assertTrue("Capture at 999ms must still be blocked", result)
    }
    // ── STATE-CONTRACT-01-FIX-B: GuidanceState reducer tests ─────────────────
    private fun reduce(
        raw: Int = 1, eligible: Int = 1, score: Float = 0f,
        mode: CameraMode = CameraMode.Normal, af: Boolean = false,
        prev: GuidanceState = GuidanceState.GuidanceActive,
        cnt: CameraViewModel.GuidanceCounters = CameraViewModel.GuidanceCounters(),
    ) = CameraViewModel.computeGuidanceStatePure(raw, eligible, score, mode, af, prev, cnt).state

    @Test fun `AC-3 eligible0 returns FacesDetectedNoneEligible`() {
        assertEquals(GuidanceState.FacesDetectedNoneEligible, reduce(raw = 2, eligible = 0))
    }
    @Test fun `AC-4 SubjectStabilizing Aligned GuidanceActive transitions`() {
        assertEquals(GuidanceState.SubjectStabilizing, reduce(prev = GuidanceState.NoFace))
        assertEquals(GuidanceState.Aligned, reduce(score = GuidanceStateConstants.ALIGNED_ENTER_SCORE))
        assertEquals(GuidanceState.GuidanceActive, reduce(score = 60f, prev = GuidanceState.Aligned,
            cnt = CameraViewModel.GuidanceCounters(alignedDwell = GuidanceStateConstants.ALIGNED_DWELL_FRAMES - 1)))
    }
    @Test fun `AC-5 all three suppress reasons`() {
        assertEquals(GuidanceState.Suppressed(SuppressionReason.AF_IN_PROGRESS), reduce(af = true))
        assertEquals(GuidanceState.Suppressed(SuppressionReason.PTF_GAZE_AF_DISABLED), reduce(mode = CameraMode.PassToFriend))
        assertEquals(GuidanceState.Suppressed(SuppressionReason.ALIGNMENT_BOUNDARY_OSCILLATION),
            reduce(score = 70f, cnt = CameraViewModel.GuidanceCounters(hysteresisFrame = GuidanceStateConstants.OSCILLATION_THRESHOLD - 1)))
    }
}
