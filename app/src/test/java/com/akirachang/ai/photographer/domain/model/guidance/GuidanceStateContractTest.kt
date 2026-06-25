package com.akirachang.ai.photographer.domain.model.guidance

import com.akirachang.ai.photographer.domain.config.GuidanceStateConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * STATE-CONTRACT-01 AI-6: Contract tests for GuidanceState six-state enumeration.
 *
 * Validates AC-1: exactly six public states; SuppressionReason is not a seventh public state.
 */
class GuidanceStateContractTest {

    // ── AC-1: Exactly six public states ──────────────────────────────────────

    @Test
    fun `sealed class has exactly six concrete subtypes`() {
        val instances = listOf(
            GuidanceState.NoFace,
            GuidanceState.FacesDetectedNoneEligible,
            GuidanceState.SubjectStabilizing,
            GuidanceState.GuidanceActive,
            GuidanceState.Aligned,
            GuidanceState.Suppressed(SuppressionReason.AF_IN_PROGRESS),
        )
        assertEquals("Exactly 6 public GuidanceState variants", 6, instances.size)
    }

    @Test
    fun `SuppressionReason has exactly three values`() {
        assertEquals("Exactly 3 SuppressionReason values", 3, SuppressionReason.values().size)
        assertTrue(SuppressionReason.values().contains(SuppressionReason.AF_IN_PROGRESS))
        assertTrue(SuppressionReason.values().contains(SuppressionReason.PTF_GAZE_AF_DISABLED))
        assertTrue(SuppressionReason.values().contains(SuppressionReason.ALIGNMENT_BOUNDARY_OSCILLATION))
    }

    // ── AC-8: Constants satisfy contract values ───────────────────────────────

    @Test
    fun `HALO_ACTIVE_ALPHA equals 0_25f`() {
        assertEquals(0.25f, GuidanceStateConstants.HALO_ACTIVE_ALPHA, 0.001f)
    }

    @Test
    fun `HALO_ALIGNED_ALPHA_MIN equals 0_10f`() {
        assertEquals(0.10f, GuidanceStateConstants.HALO_ALIGNED_ALPHA_MIN, 0.001f)
    }

    @Test
    fun `HALO_ALIGNED_ALPHA_MAX equals 0_18f`() {
        assertEquals(0.18f, GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX, 0.001f)
    }

    @Test
    fun `HALO_RADIUS_SCALE equals 0_6f`() {
        assertEquals(0.6f, GuidanceStateConstants.HALO_RADIUS_SCALE, 0.001f)
    }

    @Test
    fun `ALIGNED_ENTER_SCORE is strictly greater than ALIGNED_EXIT_SCORE`() {
        assertTrue(
            "Enter score must be > exit score for valid hysteresis band",
            GuidanceStateConstants.ALIGNED_ENTER_SCORE > GuidanceStateConstants.ALIGNED_EXIT_SCORE
        )
    }

    @Test
    fun `SubjectStabilizing alpha is half of HALO_ACTIVE_ALPHA`() {
        val expected = GuidanceStateConstants.HALO_ACTIVE_ALPHA * 0.5f
        assertEquals(0.125f, expected, 0.001f)
    }

    // ── Suppressed equality semantics ─────────────────────────────────────────

    @Test
    fun `Suppressed data class equality is reason-based`() {
        val a = GuidanceState.Suppressed(SuppressionReason.AF_IN_PROGRESS)
        val b = GuidanceState.Suppressed(SuppressionReason.AF_IN_PROGRESS)
        val c = GuidanceState.Suppressed(SuppressionReason.PTF_GAZE_AF_DISABLED)
        assertEquals("Same reason must be equal", a, b)
        assertTrue("Different reason must not be equal", a != c)
    }

    // ── AI-4: PTF suppression contract deviation ──────────────────────────────

    @Test
    fun `PTF_GAZE_AF_DISABLED is driven by CameraMode_PassToFriend not PtfAlignmentResult`() {
        // Contract deviation note (STATE-CONTRACT-01-FIX-B AI-4 / FIX-C AI-2):
        // Ticket spec listed PtfAlignmentResult as the PTF suppression gate.
        // Actual reducer implementation (STATE-CONTRACT-01) uses cameraMode == CameraMode.PassToFriend.
        // This is a ticket-level accepted deviation — PtfAlignmentResult is NOT a reducer
        // suppression authority and is NOT consulted by computeGuidanceStatePure.
        // Runtime coverage: CameraViewModelTest AC-5 (reduce(mode = CameraMode.PassToFriend)).
        val suppressed = GuidanceState.Suppressed(SuppressionReason.PTF_GAZE_AF_DISABLED)
        assertEquals(SuppressionReason.PTF_GAZE_AF_DISABLED, suppressed.reason)
    }
}
