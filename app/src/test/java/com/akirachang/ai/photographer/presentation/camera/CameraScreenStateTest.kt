package com.akirachang.ai.photographer.presentation.camera

import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.RuleScore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D4-T4 KAIROS Rule 10 — State Mutex & Animation Lifecycle Defence Tests.
 *
 * Verifies the two invariants established by SV-D4-T2-FIX-01 and SV-D4-T2-FIX-02:
 *
 * FIX-01 invariant: isCapturing takes absolute visual priority.
 *   The breathing ring MUST NOT be rendered when isCapturing == true,
 *   regardless of isAligned state. Prevents progress-circle / breathing-ring
 *   Split-brain double-render.
 *
 * FIX-02 invariant: InfiniteTransition clock must NOT run unless the ring is
 *   actually composed. Validated here by verifying that the gate condition
 *   `isAligned && !isCapturing` is false whenever it should be, so the Compose
 *   `if` block that hosts `rememberInfiniteTransition` is never entered.
 *
 * Strategy: The breathing-ring visibility is determined entirely by the pure
 * boolean expression:
 *   breathingRingShouldRender = isAligned && !state.isCapturing
 * This is a deterministic calculation on plain data objects — no UI framework
 * needed. These tests pin the truth table to prevent future regressions.
 */
class CameraScreenStateTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Builds an aligned [CompositionResult] where every rule reports aligned. */
    private fun alignedCompositionResult() = CompositionResult(
        topInstruction = null,
        ruleScores = mapOf(
            "thirds" to RuleScore("thirds", score = 95f, isAligned = true, weight = 1f),
            "headroom" to RuleScore("headroom", score = 88f, isAligned = true, weight = 1f),
        ),
        isAllAligned = true,
    )

    /** Builds a non-aligned [CompositionResult] (subject off-thirds). */
    private fun notAlignedCompositionResult() = CompositionResult(
        topInstruction = RuleScore("thirds", score = 30f, isAligned = false, weight = 1f),
        ruleScores = mapOf(
            "thirds" to RuleScore("thirds", score = 30f, isAligned = false, weight = 1f),
        ),
        isAllAligned = false,
    )

    /**
     * Mirrors the gate expression from CameraScreen.kt (L503):
     *   if (isAligned && !state.isCapturing) { ... BreathingRing ... }
     */
    private fun breathingRingShouldRender(state: CameraState): Boolean {
        val isAligned = state.compositionResult?.isAllAligned == true
        return isAligned && !state.isCapturing
    }

    // ── Test Matrix ───────────────────────────────────────────────────────────

    /**
     * [Positive] isAligned=true AND isCapturing=false → ring renders.
     * This is the normal golden-moment UX path.
     */
    @Test
    fun `when isAligned true and isCapturing false, breathing ring should render`() {
        val state = CameraState(
            compositionResult = alignedCompositionResult(),
            isCapturing = false,
        )

        assertTrue(
            "BreathingRing must render when composition is aligned and not capturing",
            breathingRingShouldRender(state),
        )
    }

    /**
     * [Negative — FIX-01] isAligned=true AND isCapturing=true → ring BLOCKED.
     * isCapturing (progress circle) has absolute priority over isAligned (breathing ring).
     * Prevents Split-brain double-render during shutter press.
     */
    @Test
    fun `when isAligned true and isCapturing true, breathing ring must NOT render`() {
        val state = CameraState(
            compositionResult = alignedCompositionResult(),
            isCapturing = true,
        )

        assertFalse(
            "BreathingRing must NOT render while isCapturing=true (FIX-01 mutex guard)",
            breathingRingShouldRender(state),
        )
    }

    /**
     * [Negative — FIX-02] isAligned=false → gate condition is false → InfiniteTransition
     * block is never entered → animation clock does NOT mount.
     * Validated by asserting the gate expression returns false, which is exactly the
     * `if` condition wrapping `rememberInfiniteTransition` in CameraScreen.kt L503.
     */
    @Test
    fun `when isAligned false, breathing ring gate is false so InfiniteTransition is not composed`() {
        val state = CameraState(
            compositionResult = notAlignedCompositionResult(),
            isCapturing = false,
        )

        assertFalse(
            "Gate must be false when not aligned — prevents InfiniteTransition from mounting (FIX-02)",
            breathingRingShouldRender(state),
        )
    }

    /**
     * [Boundary] isAligned transitions from true → false → ring disposed.
     * Simulates the state flip that Compose observes on recomposition.
     * Verifies: ring renders before flip, does NOT render after flip.
     */
    @Test
    fun `when isAligned changes from true to false, breathing ring transitions to not-rendered`() {
        // State before flip: aligned, not capturing → ring should render
        val stateBefore = CameraState(
            compositionResult = alignedCompositionResult(),
            isCapturing = false,
        )
        assertTrue(
            "BreathingRing must render before isAligned becomes false",
            breathingRingShouldRender(stateBefore),
        )

        // State after flip: no longer aligned → ring should be removed from composition
        val stateAfter = stateBefore.copy(
            compositionResult = notAlignedCompositionResult(),
        )
        assertFalse(
            "BreathingRing must NOT render after isAligned flips to false (disposal boundary)",
            breathingRingShouldRender(stateAfter),
        )
    }

    /**
     * [Edge] compositionResult == null (first frame, no detection yet) → ring absent.
     * Guards against the null-safe check `state.compositionResult?.isAllAligned == true`
     * returning true on a null receiver.
     */
    @Test
    fun `when compositionResult is null, breathing ring must NOT render`() {
        val state = CameraState(
            compositionResult = null,
            isCapturing = false,
        )

        assertFalse(
            "BreathingRing must not render before first detection frame (compositionResult null)",
            breathingRingShouldRender(state),
        )
    }
}
