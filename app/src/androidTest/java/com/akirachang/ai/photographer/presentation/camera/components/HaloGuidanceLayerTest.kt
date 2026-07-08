package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akirachang.ai.photographer.domain.composition.PtfAlignmentResult
import com.akirachang.ai.photographer.domain.composition.PtfOffset
import com.akirachang.ai.photographer.domain.composition.PtfRuleName
import com.akirachang.ai.photographer.domain.config.AmbientConstants
import com.akirachang.ai.photographer.domain.config.GuidanceStateConstants
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.guidance.GuidanceState
import com.akirachang.ai.photographer.domain.model.guidance.SuppressionReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

// STATE-CONTRACT-01-FIX-A — GuidanceState-driven TAG_HALO_* visibility invariants.
@RunWith(AndroidJUnit4::class)
class HaloGuidanceLayerTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val fakeFace = FaceLandmarkData(
        boundingBoxNormalized = RectF(0.35f, 0.1f, 0.65f, 0.5f),
        leftEyeNormalized = PointF(0.4f, 0.25f), rightEyeNormalized = PointF(0.6f, 0.25f), headEulerAngleY = 0f,
    )
    private val ptfFaceA = FaceLandmarkData(RectF(0.20f, 0.20f, 0.60f, 0.40f), null, null, 0f)
    private val ptfFaceB = FaceLandmarkData(RectF(0.60f, 0.40f, 0.90f, 0.90f), null, null, 0f)

    private fun setAndAdvance(state: GuidanceState, faces: List<FaceLandmarkData> = listOf(fakeFace)) {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent { HaloGuidanceLayer(guidanceState = state, faces = faces) }
        composeTestRule.mainClock.advanceTimeBy(150L)
    }

    @Test fun test_noFace_showsHidden() {
        setAndAdvance(GuidanceState.NoFace, emptyList())
        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertDoesNotExist()
    }

    @Test fun test_facesDetectedNoneEligible_showsHidden() {
        setAndAdvance(GuidanceState.FacesDetectedNoneEligible)
        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertDoesNotExist()
    }

    @Test fun test_guidanceActive_showsAmber() {
        setAndAdvance(GuidanceState.GuidanceActive)
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN).assertDoesNotExist()
    }

    @Test fun test_subjectStabilizing_showsAmber() {
        setAndAdvance(GuidanceState.SubjectStabilizing)
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertDoesNotExist()
    }

    @Test fun test_suppressed_showsAmber() {
        setAndAdvance(GuidanceState.Suppressed(SuppressionReason.AF_IN_PROGRESS))
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertDoesNotExist()
    }

    @Test fun test_aligned_showsGreen() {
        setAndAdvance(GuidanceState.Aligned)
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN).assertDoesNotExist()
    }

    @Test fun test_transition_guidanceActiveToAligned() {
        composeTestRule.mainClock.autoAdvance = false
        var state by mutableStateOf<GuidanceState>(GuidanceState.GuidanceActive)
        composeTestRule.setContent { HaloGuidanceLayer(guidanceState = state, faces = listOf(fakeFace)) }
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertExists()
        state = GuidanceState.Aligned
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER).assertDoesNotExist()
    }

    @Test fun test_ptfMode_noFace_showsNoFaceProbe() {
        composeTestRule.setContent {
            HaloGuidanceLayer(guidanceState = GuidanceState.NoFace, faces = emptyList(),
                isPtfMode = true, ptfAlignmentResult = PtfAlignmentResult.NO_FACE)
        }
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_NO_FACE).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_ALIGNED).assertDoesNotExist()
    }

    @Test fun test_ptfMode_unaligned_showsUnalignedProbe() {
        composeTestRule.mainClock.autoAdvance = false
        val ptf = PtfAlignmentResult(false, PtfOffset(0.15f, 0.10f), listOf(PtfRuleName.FRAMING))
        composeTestRule.setContent {
            HaloGuidanceLayer(guidanceState = GuidanceState.NoFace, faces = listOf(fakeFace), isPtfMode = true, ptfAlignmentResult = ptf)
        }
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_NO_FACE).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_ALIGNED).assertDoesNotExist()
    }

    @Test fun test_ptfMode_aligned_showsAlignedProbe() {
        composeTestRule.mainClock.autoAdvance = false
        val ptf = PtfAlignmentResult(true, PtfOffset.ZERO, emptyList())
        composeTestRule.setContent {
            HaloGuidanceLayer(guidanceState = GuidanceState.NoFace, faces = listOf(fakeFace), isPtfMode = true, ptfAlignmentResult = ptf)
        }
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_ALIGNED).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_NO_FACE).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED).assertDoesNotExist()
    }

    @Test fun test_ptfMode_unaligned_exposesBboxDerivedGeometry() {
        composeTestRule.mainClock.autoAdvance = false
        val ptf = PtfAlignmentResult(false, PtfOffset(0.15f, 0.10f), listOf(PtfRuleName.FRAMING))
        composeTestRule.setContent {
            HaloGuidanceLayer(guidanceState = GuidanceState.NoFace, faces = listOf(ptfFaceA), isPtfMode = true, ptfAlignmentResult = ptf)
        }
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED).assert(hasPtfGeometry(0.40f, 0.30f, 0.24f))
    }

    @Test fun test_ptfMode_bboxChange_updatesGeometry() {
        composeTestRule.mainClock.autoAdvance = false
        val ptf = PtfAlignmentResult(false, PtfOffset(0.15f, 0.10f), listOf(PtfRuleName.FRAMING))
        var faces by mutableStateOf(listOf(ptfFaceA))
        composeTestRule.setContent {
            HaloGuidanceLayer(guidanceState = GuidanceState.NoFace, faces = faces, isPtfMode = true, ptfAlignmentResult = ptf)
        }
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED).assert(hasPtfGeometry(0.40f, 0.30f, 0.24f))
        faces = listOf(ptfFaceB)
        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED).assert(hasPtfGeometry(0.75f, 0.65f, 0.30f))
    }

    private fun hasPtfGeometry(cx: Float, cy: Float, r: Float): SemanticsMatcher =
        SemanticsMatcher("PTF Halo geometry center=($cx,$cy) radius=$r") { node ->
            val ax = node.config.getOrNull(PtfHaloCenterXKey) ?: return@SemanticsMatcher false
            val ay = node.config.getOrNull(PtfHaloCenterYKey) ?: return@SemanticsMatcher false
            val ar = node.config.getOrNull(PtfHaloRadiusKey) ?: return@SemanticsMatcher false
            abs(ax - cx) < 0.0001f && abs(ay - cy) < 0.0001f && abs(ar - r) < 0.0001f
        }

    // GAP-06-RENDER-DOT-01 AI-3 — computeTractionDot 純幾何 + 三段 V-curve 語意（SSOT v4.0.7 §2.5 L439-479）
    private val haloAmber = Color(0xFFD4A030)
    private val haloGreen = Color(0xFF50C864)
    private fun dot(dist: Float) = computeTractionDot(0f, 0f, 1000f, 36f, Offset(dist, 0f))

    @Test fun test_dot_vCurveAmberSegment() {
        assertEquals(0.85f, dot(100f).alpha, 1e-4f)   // dist=max → 琥珀峰值
        assertEquals(0.60f, dot(75f).alpha, 1e-4f)
        assertEquals(haloAmber, dot(75f).color)
    }

    @Test fun test_dot_vCurveGreenSegment() {
        assertEquals(0.35f, dot(50f).alpha, 1e-4f)    // mid 邊界谷底（> 而非 >=）
        assertEquals(0.60f, dot(25f).alpha, 1e-4f)
        assertEquals(haloGreen, dot(25f).color)
    }

    @Test fun test_dot_nullTargetHomesGreenPeakScaled() {
        val home = computeTractionDot(0f, 0f, 1000f, 36f, null)   // Rev C null→歸心 dist=0
        assertEquals(0f, home.dist, 1e-4f)
        assertEquals(haloGreen, home.color)
        assertEquals(AmbientConstants.HALO_DOT_ALPHA_PEAK, home.alpha, 1e-4f)          // R-T13 V-3：spec.alpha 與 breathe 解耦
        assertEquals(AmbientConstants.HALO_DOT_ALIGNED_SCALE, home.homeScale, 1e-4f)
    }

    @Test fun test_dot_injectedOffsetIsDirectional() {   // Rev C 注入合成 Offset（僅 androidTest）
        assertTrue(dot(100f).centerX > 0f)               // atan2=0 → +x
        assertTrue(computeTractionDot(0f, 0f, 1000f, 36f, Offset(0f, 80f)).centerY > 0f)   // atan2=π/2 → +y
    }

    @Test fun test_dot_clampBoundaryDotEqualsBbox() {
        val s = computeTractionDot(0f, 0f, 36f, 36f, Offset(100f, 0f))   // maxOffset=0 → 圓點不出環
        assertEquals(0f, s.centerX, 1e-4f)
        assertEquals(0f, s.centerY, 1e-4f)
    }

    // GAP-06-RENDER-DOT-01-FIX-A AI-2 — 四象限補齊 + Aligned dot persistence + dot/ring 解耦回歸
    @Test fun test_dot_injectedOffsetNegativeQuadrants() {   // T-1/T-2 補 −x/−y → 四象限 4/4
        assertTrue(dot(-80f).centerX < 0f)                                                   // atan2=π → −x
        assertTrue(computeTractionDot(0f, 0f, 1000f, 36f, Offset(0f, -80f)).centerY < 0f)    // atan2=−π/2 → −y
    }

    @Test fun test_aligned_dotPersistsWithFace() {   // T-3 SSOT §2.5「對齊強存在」：dot 節點存在
        setAndAdvance(GuidanceState.Aligned)
        composeTestRule.onNodeWithTag(TAG_HALO_DOT).assertExists()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertExists()
    }

    @Test fun test_aligned_noFace_dotAbsent() {   // T-3 負向：無臉 → 不畫點，空環 Canvas 仍在
        setAndAdvance(GuidanceState.Aligned, emptyList())
        composeTestRule.onNodeWithTag(TAG_HALO_DOT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN).assertExists()
    }

    @Test fun test_dot_alphaDecoupledFromRingBreathe() {   // T-4 R-T13 V-3：dot alpha 不受 ring breathe 影響
        // 環域 aligned 呼吸帶 [MIN, MAX]；dot alpha 族 [valley, peak]。computeTractionDot 簽名不含 ring alpha，
        // 且 dot 最低 alpha（V 谷底）仍高於環呼吸上限 → 兩族完全不相交，結構解耦。誤接 normalBreathAlpha 即失敗。
        val ringMax = GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX
        val home = computeTractionDot(0f, 0f, 1000f, 36f, null).alpha   // 歸心峰值 0.85
        val valley = dot(50f).alpha                                     // V 谷底 0.35
        assertTrue(home > ringMax)     // 0.85 > 0.18
        assertTrue(valley > ringMax)   // 0.35 > 0.18 → dot 最低仍高於環呼吸帶
    }
}
