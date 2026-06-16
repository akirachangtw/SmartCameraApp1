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
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.RuleScore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * D6-B2-T3 — HaloGuidanceLayer state-mutex & rendering pipeline tests.
 *
 * Verifies three rendering invariants:
 *
 * 1. NULL SUPPRESSION (Constitution §6): compositionResult=null → TAG_HALO_HIDDEN Box
 *    enters composition; AMBER and GREEN must be absent.
 *
 * 2. ALIGNED SUPPRESSION: isAllAligned=true → TAG_HALO_GREEN Canvas enters composition;
 *    AMBER and HIDDEN must be absent.
 *
 * 3. AMBER GUIDANCE: isAllAligned=false + unaligned gaze rule → TAG_HALO_AMBER Canvas
 *    enters composition; GREEN and HIDDEN must be absent.
 *
 * 4. STATE TRANSITION: Aligned → Unaligned transition propagates correctly after the
 *    10fps throttle interval (100ms) elapses.
 *
 * Note: All tests use testTag semantic probes injected in HaloGuidanceLayer.kt.
 * No production code is modified (Scope Ban — D6-B2-T3 §3).
 *
 * Run with:
 *   ./gradlew connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=\
 *     com.akirachang.ai.photographer.presentation.camera.components.HaloGuidanceLayerTest
 */
@RunWith(AndroidJUnit4::class)
class HaloGuidanceLayerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Shared fake data ─────────────────────────────────────────────────────

    /** A valid face positioned in the upper-center of the frame. */
    private val fakeFace = FaceLandmarkData(
        boundingBoxNormalized = RectF(0.35f, 0.1f, 0.65f, 0.5f),
        leftEyeNormalized = PointF(0.4f, 0.25f),
        rightEyeNormalized = PointF(0.6f, 0.25f),
        headEulerAngleY = 0f,
    )

    /** PTF face with asymmetric bbox, used to verify center/radius semantics. */
    private val ptfFaceA = FaceLandmarkData(
        boundingBoxNormalized = RectF(0.20f, 0.20f, 0.60f, 0.40f),
        leftEyeNormalized = null,
        rightEyeNormalized = null,
        headEulerAngleY = 0f,
    )

    /** Same PTF state, different bbox center and radius. */
    private val ptfFaceB = FaceLandmarkData(
        boundingBoxNormalized = RectF(0.60f, 0.40f, 0.90f, 0.90f),
        leftEyeNormalized = null,
        rightEyeNormalized = null,
        headEulerAngleY = 0f,
    )

    /** CompositionResult where all rules are satisfied (isAllAligned = true). */
    private val alignedResult = CompositionResult(
        topInstruction = null,
        ruleScores = mapOf(
            "gaze" to RuleScore(name = "gaze", score = 90f, isAligned = true, weight = 0.30f),
            "eye_line" to RuleScore(name = "eye_line", score = 88f, isAligned = true, weight = 0.20f),
        ),
        isAllAligned = true,
    )

    /**
     * CompositionResult where gaze is NOT aligned.
     * Satisfies the HaloGuidanceLayer amber path guard:
     *   ruleScores["gaze"]?.isAligned == false → gazeNotAligned = true → AMBER Canvas rendered.
     */
    private val unalignedResult = CompositionResult(
        topInstruction = RuleScore(name = "gaze", score = 30f, isAligned = false, weight = 0.30f),
        ruleScores = mapOf(
            "gaze" to RuleScore(name = "gaze", score = 30f, isAligned = false, weight = 0.30f),
            "eye_line" to RuleScore(name = "eye_line", score = 85f, isAligned = true, weight = 0.20f),
        ),
        isAllAligned = false,
    )

    // ── Test 1: NULL suppression ─────────────────────────────────────────────

    /**
     * [Positive — NULL guard] compositionResult=null → TAG_HALO_HIDDEN Box enters
     * composition (Constitution §6 — fully invisible, zero draw calls).
     */
    @Test
    fun test_whenResultIsNull_showsHiddenProbe() {
        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(fakeFace),
                compositionResult = null,
            )
        }

        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN)
            .assertExists("TAG_HALO_HIDDEN must be in composition when compositionResult=null")

        composeTestRule.onNodeWithTag(TAG_HALO_AMBER)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_GREEN)
            .assertDoesNotExist()
    }

    // ── Test 2: ALIGNED suppression ──────────────────────────────────────────

    /**
     * [Positive — Aligned guard] isAllAligned=true → TAG_HALO_GREEN Canvas enters
     * composition. AMBER and HIDDEN must be absent (visual collision prevention).
     */
    @Test
    fun test_whenIsAllAligned_showsGreenProbe() {
        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(fakeFace),
                compositionResult = alignedResult,
            )
        }

        composeTestRule.onNodeWithTag(TAG_HALO_GREEN)
            .assertExists("TAG_HALO_GREEN must be in composition when isAllAligned=true")

        composeTestRule.onNodeWithTag(TAG_HALO_AMBER)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN)
            .assertDoesNotExist()
    }

    // ── Test 3: AMBER guidance ───────────────────────────────────────────────

    /**
     * [Positive — Amber path] isAllAligned=false + gaze rule not aligned →
     * TAG_HALO_AMBER Canvas enters composition. GREEN and HIDDEN must be absent.
     *
     * Note: The LaunchedEffect throttle (100ms) must fire before throttledResult is
     * propagated to the amber Canvas path. We advance the main clock by 150ms to
     * ensure the first throttle frame completes.
     */
    @Test
    fun test_whenNotAligned_showsAmberHalo() {
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(fakeFace),
                compositionResult = unalignedResult,
            )
        }

        // Advance past the 100ms throttle interval to let LaunchedEffect propagate
        // throttledResult and throttledFaces before the amber Canvas is drawn.
        composeTestRule.mainClock.advanceTimeBy(150L)

        composeTestRule.onNodeWithTag(TAG_HALO_AMBER)
            .assertExists("TAG_HALO_AMBER must be in composition when isAllAligned=false and gaze is unaligned")

        composeTestRule.onNodeWithTag(TAG_HALO_GREEN)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN)
            .assertDoesNotExist()
    }

    // ── Test 4: State transition — aligned → unaligned ───────────────────────

    /**
     * [State Transition] Initial state: aligned (GREEN probe). After switching to
     * unaligned, advance virtual clock by 150ms to flush the 10fps throttle
     * (LaunchedEffect delay), then assert AMBER probe is present.
     *
     * Validates that the T2.1 fix (LaunchedEffect hoisted above isAllAligned early
     * return) correctly propagates throttledResult even during the suppressed phase,
     * eliminating 100ms stale-frame on state transition.
     */
    @Test
    fun test_stateTransition_fromAlignedToUnaligned_updatesCorrectly() {
        composeTestRule.mainClock.autoAdvance = false

        // Use Compose-observable state so recomposition is triggered on assignment.
        var currentResult by mutableStateOf<CompositionResult?>(alignedResult)

        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(fakeFace),
                compositionResult = currentResult,
            )
        }

        // Advance to let initial aligned state settle
        composeTestRule.mainClock.advanceTimeBy(150L)

        // Verify initial aligned state
        composeTestRule.onNodeWithTag(TAG_HALO_GREEN)
            .assertExists("Initial aligned state must show GREEN probe")
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER)
            .assertDoesNotExist()

        // Transition to unaligned — mutableStateOf triggers recomposition
        currentResult = unalignedResult

        // Advance virtual clock past the 10fps throttle (100ms) to flush LaunchedEffect
        composeTestRule.mainClock.advanceTimeBy(150L)

        // After throttle flush, AMBER must now be in composition
        composeTestRule.onNodeWithTag(TAG_HALO_AMBER)
            .assertExists("TAG_HALO_AMBER must appear after throttle flush when transitioning to unaligned")

        composeTestRule.onNodeWithTag(TAG_HALO_GREEN)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_HIDDEN)
            .assertDoesNotExist()
    }

    // ── PTF mode tests (D8-B2-T3) ───────────────────────────────────────────

    /**
     * [PTF — NO_FACE] isPtfMode=true + PtfAlignmentResult.NO_FACE →
     * TAG_HALO_PTF_NO_FACE Box in composition; UNALIGNED and ALIGNED absent.
     */
    @Test
    fun test_ptfMode_noFace_showsNoFaceProbe() {
        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = emptyList(),
                compositionResult = null,
                isPtfMode = true,
                ptfAlignmentResult = PtfAlignmentResult.NO_FACE,
            )
        }

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_NO_FACE)
            .assertExists("TAG_HALO_PTF_NO_FACE must be in composition when NO_FACE sentinel")

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_ALIGNED)
            .assertDoesNotExist()
    }

    /**
     * [PTF — Unaligned] isPtfMode=true + failedRules non-empty →
     * TAG_HALO_PTF_UNALIGNED Canvas in composition; NO_FACE and ALIGNED absent.
     */
    @Test
    fun test_ptfMode_unaligned_showsUnalignedProbe() {
        composeTestRule.mainClock.autoAdvance = false

        val unalignedPtfResult = PtfAlignmentResult(
            isAligned = false,
            offset = PtfOffset(dx = 0.15f, dy = 0.10f),
            failedRules = listOf(PtfRuleName.FRAMING),
        )

        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(fakeFace),
                compositionResult = null,
                isPtfMode = true,
                ptfAlignmentResult = unalignedPtfResult,
            )
        }

        composeTestRule.mainClock.advanceTimeBy(150L)

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED)
            .assertExists("TAG_HALO_PTF_UNALIGNED must be in composition when failedRules non-empty")

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_NO_FACE)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_ALIGNED)
            .assertDoesNotExist()
    }

    /**
     * [PTF — Aligned] isPtfMode=true + isAligned=true →
     * TAG_HALO_PTF_ALIGNED Canvas in composition; NO_FACE and UNALIGNED absent.
     */
    @Test
    fun test_ptfMode_aligned_showsAlignedProbe() {
        composeTestRule.mainClock.autoAdvance = false

        val alignedPtfResult = PtfAlignmentResult(
            isAligned = true,
            offset = PtfOffset.ZERO,
            failedRules = emptyList(),
        )

        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(fakeFace),
                compositionResult = null,
                isPtfMode = true,
                ptfAlignmentResult = alignedPtfResult,
            )
        }

        composeTestRule.mainClock.advanceTimeBy(150L)

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_ALIGNED)
            .assertExists("TAG_HALO_PTF_ALIGNED must be in composition when isAligned=true")

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_NO_FACE)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED)
            .assertDoesNotExist()
    }

    /**
     * [PTF — Geometry] PTF Halo center/radius must be derived from the primary face bbox,
     * not the fixed canvas center or screen minDimension.
     */
    @Test
    fun test_ptfMode_unaligned_exposesBboxDerivedGeometry() {
        composeTestRule.mainClock.autoAdvance = false

        val unalignedPtfResult = PtfAlignmentResult(
            isAligned = false,
            offset = PtfOffset(dx = 0.15f, dy = 0.10f),
            failedRules = listOf(PtfRuleName.FRAMING),
        )

        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = listOf(ptfFaceA),
                compositionResult = null,
                isPtfMode = true,
                ptfAlignmentResult = unalignedPtfResult,
            )
        }

        composeTestRule.mainClock.advanceTimeBy(150L)

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED)
            .assert(hasPtfGeometry(centerX = 0.40f, centerY = 0.30f, radius = 0.24f))
    }

    /**
     * [PTF — Geometry transition] When the face bbox changes, the PTF Halo semantic
     * geometry changes with it. This guards against regressing to fixed-center rendering.
     */
    @Test
    fun test_ptfMode_bboxChange_updatesGeometry() {
        composeTestRule.mainClock.autoAdvance = false

        val unalignedPtfResult = PtfAlignmentResult(
            isAligned = false,
            offset = PtfOffset(dx = 0.15f, dy = 0.10f),
            failedRules = listOf(PtfRuleName.FRAMING),
        )
        var currentFaces by mutableStateOf(listOf(ptfFaceA))

        composeTestRule.setContent {
            HaloGuidanceLayer(
                faces = currentFaces,
                compositionResult = null,
                isPtfMode = true,
                ptfAlignmentResult = unalignedPtfResult,
            )
        }

        composeTestRule.mainClock.advanceTimeBy(150L)
        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED)
            .assert(hasPtfGeometry(centerX = 0.40f, centerY = 0.30f, radius = 0.24f))

        currentFaces = listOf(ptfFaceB)
        composeTestRule.mainClock.advanceTimeBy(150L)

        composeTestRule.onNodeWithTag(TAG_HALO_PTF_UNALIGNED)
            .assert(hasPtfGeometry(centerX = 0.75f, centerY = 0.65f, radius = 0.30f))
    }

    private fun hasPtfGeometry(centerX: Float, centerY: Float, radius: Float): SemanticsMatcher =
        SemanticsMatcher("PTF Halo geometry center=($centerX,$centerY) radius=$radius") { node ->
            val actualCenterX = node.config.getOrNull(PtfHaloCenterXKey) ?: return@SemanticsMatcher false
            val actualCenterY = node.config.getOrNull(PtfHaloCenterYKey) ?: return@SemanticsMatcher false
            val actualRadius = node.config.getOrNull(PtfHaloRadiusKey) ?: return@SemanticsMatcher false
            abs(actualCenterX - centerX) < 0.0001f &&
                abs(actualCenterY - centerY) < 0.0001f &&
                abs(actualRadius - radius) < 0.0001f
        }
}
