package com.akirachang.ai.photographer.presentation.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * SV-D4-T4-FIX-01/02 — ShutterButtonSection UI Instrumented Tests.
 *
 * Verifies breathing-ring / progress-circle composition state using the extracted
 * [ShutterButtonSection] stateless composable.
 *
 * Why stateless extraction matters:
 *   - CameraScreen depends on Hilt + CameraX — cannot be composed directly in tests.
 *   - ShutterButtonSection has zero DI dependencies; it accepts plain Boolean params.
 *   - mutableStateOf drives recomposition without repeated setContent calls.
 *
 * State-machine under test (CameraScreen.kt ShutterButtonSection):
 *   BreathingRing rendered  ← isAligned=true  AND isCapturing=false  (TAG_SHUTTER_BREATHING_RING)
 *   BreathingRing absent    ← isAligned=false OR  isCapturing=true
 *   ProgressCircle rendered ← isCapturing=true                       (TAG_SHUTTER_PROGRESS_CIRCLE)
 *
 * Run with: ./gradlew connectedAndroidTest
 */
class CameraScreenBreathingRingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test 1: Positive path ─────────────────────────────────────────────────

    /**
     * [Positive] isAligned=true AND isCapturing=false →
     *   BreathingRing IN composition, ProgressCircle NOT in composition.
     */
    @Test
    fun aligned_notCapturing_breathingRingExistsAndProgressCircleAbsent() {
        composeTestRule.setContent {
            ShutterButtonSection(
                isAligned = true,
                isCapturing = false,
                onCapture = {},
            )
        }

        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertExists("BreathingRing must be composed when isAligned=true and not capturing")

        composeTestRule.onNodeWithTag(TAG_SHUTTER_PROGRESS_CIRCLE)
            .assertDoesNotExist()
    }

    // ── Test 2: FIX-01 mutex (isCapturing wins) ───────────────────────────────

    /**
     * [Negative — FIX-01] isAligned=true AND isCapturing=true →
     *   BreathingRing ABSENT (suppressed by isCapturing mutex),
     *   ProgressCircle IN composition.
     */
    @Test
    fun aligned_capturing_breathingRingAbsentAndProgressCircleExists() {
        composeTestRule.setContent {
            ShutterButtonSection(
                isAligned = true,
                isCapturing = true,
                onCapture = {},
            )
        }

        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_SHUTTER_PROGRESS_CIRCLE)
            .assertExists("ProgressCircle must be composed when isCapturing=true")
    }

    // ── Test 3: Not-aligned baseline ─────────────────────────────────────────

    /**
     * [Negative] isAligned=false AND isCapturing=false →
     *   BreathingRing ABSENT, ProgressCircle ABSENT.
     *   Confirms FIX-02: InfiniteTransition clock not mounted when not aligned.
     */
    @Test
    fun notAligned_notCapturing_bothAbsent() {
        composeTestRule.setContent {
            ShutterButtonSection(
                isAligned = false,
                isCapturing = false,
                onCapture = {},
            )
        }

        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag(TAG_SHUTTER_PROGRESS_CIRCLE)
            .assertDoesNotExist()
    }

    // ── Test 4: State transition (recomposition) ──────────────────────────────

    /**
     * [Boundary] isAligned transitions false → true →
     *   BreathingRing appears after recomposition.
     *
     * Uses mutableStateOf to drive recomposition without calling setContent twice
     * (avoids the IllegalStateException "has already set content" design bug).
     */
    @Test
    fun stateTransition_notAlignedToAligned_breathingRingAppears() {
        var isAligned by mutableStateOf(false)

        composeTestRule.setContent {
            ShutterButtonSection(
                isAligned = isAligned,
                isCapturing = false,
                onCapture = {},
            )
        }

        // Initial state: not aligned — ring absent
        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertDoesNotExist()

        // Trigger recomposition: flip to aligned
        isAligned = true
        composeTestRule.waitForIdle()

        // After transition: ring must appear
        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertExists("BreathingRing must appear after isAligned transitions from false to true")
    }

    // ── Test 5: State transition (dispose) ───────────────────────────────────

    /**
     * [Boundary — FIX-05] isAligned transitions true → false →
     * BreathingRing is disposed from composition.
     *
     * This verifies that when alignment is lost, the InfiniteTransition
     * clock is unmounted (FIX-02 lifecycle contract).
     */
    @Test
    fun stateTransition_alignedToNotAligned_breathingRingDisposed() {
        var isAligned by mutableStateOf(true)

        composeTestRule.setContent {
            ShutterButtonSection(
                isAligned = isAligned,
                isCapturing = false,
                onCapture = {},
            )
        }

        // Initial state: aligned — ring must be present
        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertExists("BreathingRing must be in composition when isAligned=true")

        // Trigger recomposition: lose alignment
        isAligned = false
        composeTestRule.waitForIdle()

        // After transition: ring must be disposed
        composeTestRule.onNodeWithTag(TAG_SHUTTER_BREATHING_RING)
            .assertDoesNotExist()
    }
}
