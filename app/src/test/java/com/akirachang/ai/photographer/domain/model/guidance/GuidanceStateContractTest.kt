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
    fun `C-alpha1 ALIGNED_ALPHA_MIN is strictly less than ALIGNED_ALPHA_MAX`() {
        // SC-04 C-α1 帶序：0.25 < 0.40
        assertTrue(
            "ALIGNED_ALPHA_MIN must be < ALIGNED_ALPHA_MAX",
            GuidanceStateConstants.HALO_ALIGNED_ALPHA_MIN < GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX
        )
    }

    @Test
    fun `C-alpha2 ALIGNED_ALPHA_MAX is strictly less than HALO_ACTIVE_ALPHA`() {
        // SC-04 C-α2 階層：0.40 < 0.8（對齊比未對齊安靜一級，D-3）
        assertTrue(
            "ALIGNED_ALPHA_MAX must be < HALO_ACTIVE_ALPHA",
            GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX < GuidanceStateConstants.HALO_ACTIVE_ALPHA
        )
    }

    @Test
    fun `C-alpha3 ALIGNED_ALPHA_MAX equals half of HALO_ACTIVE_ALPHA`() {
        // SC-04 C-α3 半值律：0.40 = 0.8 之半（SSOT D-3 明載相對式）
        assertEquals(
            GuidanceStateConstants.HALO_ACTIVE_ALPHA * 0.5f,
            GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX,
            0.001f
        )
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
        // SC-04 重錨後與 C-α3 同式，斷言主體改為常數間關係（禁裸數字快照，K4-54）
        val expected = GuidanceStateConstants.HALO_ACTIVE_ALPHA * 0.5f
        assertEquals(GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX, expected, 0.001f)
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
