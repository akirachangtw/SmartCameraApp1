package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akirachang.ai.photographer.domain.composition.PtfAlignmentResult
import com.akirachang.ai.photographer.domain.composition.PtfOffset
import com.akirachang.ai.photographer.domain.composition.PtfRuleName
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.guidance.GuidanceState
import com.akirachang.ai.photographer.domain.model.guidance.SuppressionReason
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
}
