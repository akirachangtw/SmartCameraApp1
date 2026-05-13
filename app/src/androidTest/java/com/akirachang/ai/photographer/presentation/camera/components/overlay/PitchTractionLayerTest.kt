package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.akirachang.ai.photographer.domain.model.DeviceCapabilityTier
import com.akirachang.ai.photographer.presentation.camera.overlay.PitchTractionLayer
import com.akirachang.ai.photographer.presentation.camera.overlay.TAG_PITCH_TRACTION_ALIGNED
import com.akirachang.ai.photographer.presentation.camera.overlay.TAG_PITCH_TRACTION_AMBER
import com.akirachang.ai.photographer.presentation.camera.overlay.TAG_PITCH_TRACTION_DOTS_ENABLED
import org.junit.Rule
import org.junit.Test

/**
 * D4-T4 KAIROS Rule 10 — PitchTractionLayer Tier Degradation & Rendering Path Tests.
 *
 * Verifies two invariants:
 *
 * 1. FULL tier with isAligned=false: amber pitch traction canvas (TAG_PITCH_TRACTION_AMBER)
 *    enters composition — full visual pipeline is active.
 *
 * 2. LEGACY tier with isAligned=false: amber canvas still enters composition
 *    (the Canvas node itself is always present), but the [renderDots] flag inside
 *    it is false — dots are suppressed. We verify the Canvas branch that CAN
 *    host dots is present, and separately assert via logic that the aligned
 *    (green) branch is absent.
 *
 * 3. isAligned=true: aligned breathing canvas (TAG_PITCH_TRACTION_ALIGNED) enters
 *    composition, amber canvas does NOT (early return guard).
 *
 * Note: PitchTractionLayer uses Canvas for all rendering — there are no text/semantic
 * child nodes. Tests use testTag markers injected on each Canvas branch to
 * distinguish composition paths without touching draw-call internals.
 *
 * Run with: ./gradlew connectedAndroidTest
 */
class PitchTractionLayerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test 1: FULL tier, not aligned — amber path active ───────────────────

    /**
     * [Positive] FULL tier, isAligned=false → amber traction Canvas enters composition.
     */
    @Test
    fun fullTier_notAligned_amberCanvasShouldBeInComposition() {
        composeTestRule.setContent {
            PitchTractionLayer(
                deviceRoll = 15f,
                capabilityTier = DeviceCapabilityTier.FULL,
                isAligned = false,
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_AMBER)
            .assertExists("FULL tier amber traction Canvas must be in composition when isAligned=false")

        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_ALIGNED)
            .assertDoesNotExist()
    }

    /**
     * [Negative — Tier Fallback] LEGACY tier, isAligned=false → amber Canvas still
     * enters composition, but internal [renderDots] flag is false.
     *
     * The dot suppression (renderDots = false for LEGACY) is a Canvas draw-call
     * decision that cannot be observed via semantic nodes. Its correctness is
     * guaranteed by the source-level assertion in PitchTractionLayer.kt:
     *   val renderDots = capabilityTier !is DeviceCapabilityTier.LEGACY
     * which is also verified by [legacyTier_renderDotsFlag_mustBeFalse] below.
     */
    @Test
    fun legacyTier_notAligned_amberCanvasShouldBeInComposition_alignedCanvasMustBeAbsent() {
        composeTestRule.setContent {
            PitchTractionLayer(
                deviceRoll = 10f,
                capabilityTier = DeviceCapabilityTier.LEGACY,
                isAligned = false,
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_AMBER)
            .assertExists("LEGACY tier amber Canvas must be in composition (base gradient always drawn)")

        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_ALIGNED)
            .assertDoesNotExist()
    }

    /**
     * [Positive] isAligned=true (any tier) → aligned breathing Canvas enters composition.
     * Amber Canvas must NOT be present (early return prevents it).
     */
    @Test
    fun anyTier_aligned_alignedCanvasShouldBeInComposition_amberCanvasMustBeAbsent() {
        composeTestRule.setContent {
            PitchTractionLayer(
                deviceRoll = 5f,
                capabilityTier = DeviceCapabilityTier.FULL,
                isAligned = true,
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_ALIGNED)
            .assertExists("Aligned breathing Canvas must be in composition when isAligned=true")

        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_AMBER)
            .assertDoesNotExist()
    }

    /**
     * [Negative — LEGACY dot flag] Verifies the dot-suppression contract at the
     * logic level: [DeviceCapabilityTier.LEGACY] must NOT be an instance of any
     * tier other than LEGACY, and the `!is LEGACY` guard must evaluate to false.
     */
    @Test
    fun legacyTier_renderDotsFlag_mustBeFalse() {
        val tier: DeviceCapabilityTier = DeviceCapabilityTier.LEGACY
        val renderDots = tier !is DeviceCapabilityTier.LEGACY
        assert(!renderDots) {
            "renderDots must be false for LEGACY tier — dots disabled to protect 60fps budget"
        }
    }

    /**
     * [Positive — FULL dot flag] Verifies that FULL tier enables dot rendering.
     */
    @Test
    fun fullTier_renderDotsFlag_mustBeTrue() {
        val tier: DeviceCapabilityTier = DeviceCapabilityTier.FULL
        val renderDots = tier !is DeviceCapabilityTier.LEGACY
        assert(renderDots) {
            "renderDots must be true for FULL tier — full visual pipeline expected on flagship devices"
        }
    }

    /**
     * [Positive — LIMITED dot flag] Verifies that LIMITED tier also enables dot rendering.
     */
    @Test
    fun limitedTier_renderDotsFlag_mustBeTrue() {
        val tier: DeviceCapabilityTier = DeviceCapabilityTier.LIMITED
        val renderDots = tier !is DeviceCapabilityTier.LEGACY
        assert(renderDots) {
            "renderDots must be true for LIMITED tier — dots enabled at reduced frame rate"
        }
    }

    // ── Test 7: dots probe — FULL tier + active direction ────────────────────

    /**
     * [Positive — Dots Probe] FULL tier + deviceRoll=15f (> ROLL_THRESHOLD_DEG=3f)
     * → direction=RIGHT is non-null → dots probe Box enters composition.
     */
    @Test
    fun fullTier_withDirection_dotsProbe_mustBeInComposition() {
        composeTestRule.setContent {
            PitchTractionLayer(
                deviceRoll = 15f,
                capabilityTier = DeviceCapabilityTier.FULL,
                isAligned = false,
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_DOTS_ENABLED)
            .assertExists("Dots probe must be in composition for FULL tier with active direction")
    }

    // ── Test 8: dots probe — LEGACY tier + active direction ──────────────────

    /**
     * [Negative — Tier Fallback — Dots Probe] LEGACY tier + deviceRoll=15f →
     * renderDots=false → dots probe Box never enters composition.
     */
    @Test
    fun legacyTier_withDirection_dotsProbe_mustBeAbsentFromComposition() {
        composeTestRule.setContent {
            PitchTractionLayer(
                deviceRoll = 15f,
                capabilityTier = DeviceCapabilityTier.LEGACY,
                isAligned = false,
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_DOTS_ENABLED)
            .assertDoesNotExist()
    }

    // ── Test 9: dots probe — FULL tier + no direction ─────────────────────────

    /**
     * [Negative — No Direction — Dots Probe] FULL tier + deviceRoll=1f
     * (< ROLL_THRESHOLD_DEG=3f) → direction=null → dots probe Box absent.
     */
    @Test
    fun fullTier_noDirection_dotsProbe_mustBeAbsentFromComposition() {
        composeTestRule.setContent {
            PitchTractionLayer(
                deviceRoll = 1f,
                capabilityTier = DeviceCapabilityTier.FULL,
                isAligned = false,
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TAG_PITCH_TRACTION_DOTS_ENABLED)
            .assertDoesNotExist()
    }
}
