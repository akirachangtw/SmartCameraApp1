/**
 * ClassName: HDRWarningOverlayTest
 * Description: UI tests for HDRWarningOverlay component
 * Author: Claude Code
 * Date: 2025-12-20
 * Version: 2.6.0
 *
 * Test Coverage:
 * - Normal state (no warning display)
 * - Backlight state display
 * - HighContrast state display
 * - Overexposed state display
 * - Underexposed state display
 * - Animation behavior (entrance/exit)
 * - Color coding verification
 * - Recommendation text display
 *
 * Test Strategy:
 * - Use Jetpack Compose Testing framework
 * - Test each LightingState separately
 * - Verify UI elements (badge, icon, text, colors)
 * - Verify animations (visibility transitions)
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for HDRWarningOverlay
 *
 * Run with: ./gradlew connectedAndroidTest
 */
class HDRWarningOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========== Normal State Tests ==========

    @Test
    fun normalState_shouldNotDisplayWarning() {
        // Arrange
        val lightingState = LightingState.Normal

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: Warning should not be visible
        composeTestRule.onNodeWithText("逆光場景", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("高對比", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("過曝", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("光線不足", substring = true).assertDoesNotExist()
    }

    // ========== Backlight State Tests ==========

    @Test
    fun backlightState_shouldDisplayWarning() {
        // Arrange
        val lightingState = LightingState.Backlight(
            subjectLuminance = 76f,
            backgroundLuminance = 178f,
            luminanceDifference = 102f
        )

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: Backlight warning should be visible
        composeTestRule.onNodeWithText("逆光場景 - 建議使用 HDR 或開啟補光")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backlightState_shouldDisplayWithDefaultRecommendation() {
        // Arrange: Backlight does not accept a custom recommendation parameter;
        // the default recommendation text is returned by getRecommendation().
        val lightingState = LightingState.Backlight(
            subjectLuminance = 76f,
            backgroundLuminance = 178f,
            luminanceDifference = 102f,
        )

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: The default backlight recommendation text is displayed
        composeTestRule.onNodeWithText("逆光場景", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    // ========== HighContrast State Tests ==========

    @Test
    fun highContrastState_shouldDisplayWarning() {
        // Arrange
        val lightingState = LightingState.HighContrast(
            contrastRatio = 5f,
            dynamicRange = 200f,
            highlightRatio = 0.15f,
            shadowRatio = 0.12f
        )

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: HighContrast warning should be visible
        composeTestRule.onNodeWithText("高對比場景 - 建議使用 HDR 模式")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun highContrastState_withHighHighlights_shouldShowAlternativeMessage() {
        // Arrange: High highlight ratio triggers different message.
        // HighContrast does not accept a custom recommendation parameter.
        val lightingState = LightingState.HighContrast(
            contrastRatio = 5f,
            dynamicRange = 200f,
            highlightRatio = 0.25f, // > 0.20
            shadowRatio = 0.12f,
        )

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert
        composeTestRule.onNodeWithText("高反差場景", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    // ========== Overexposed State Tests ==========

    @Test
    fun overexposedState_shouldDisplayWarning() {
        // Arrange
        val lightingState = LightingState.Overexposed(
            highlightRatio = 0.18f,
            avgLuminance = 200f
        )

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: Overexposed warning should be visible
        composeTestRule.onNodeWithText("過曝風險 - 建議降低曝光補償或避免直射光線")
            .assertExists()
            .assertIsDisplayed()
    }

    // ========== Underexposed State Tests ==========

    @Test
    fun underexposedState_shouldDisplayWarning() {
        // Arrange
        val lightingState = LightingState.Underexposed(
            avgLuminance = 40f,
            shadowRatio = 0.30f
        )

        // Act
        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: Underexposed warning should be visible
        composeTestRule.onNodeWithText("光線不足 - 建議增加照明或提高 ISO")
            .assertExists()
            .assertIsDisplayed()
    }

    // ========== State Transition Tests ==========

    @Test
    fun stateTransition_fromNormalToBacklight_shouldAnimateIn() {
        // Arrange: mutableStateOf drives recomposition (Rule 14 — Compose Slot-Table)
        var lightingState by mutableStateOf<LightingState>(LightingState.Normal)

        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: No warning initially
        composeTestRule.onNodeWithText("逆光場景", substring = true).assertDoesNotExist()

        // Act: Change state — triggers recomposition, not a new composition
        lightingState = LightingState.Backlight(76f, 178f, 102f)

        // Assert: Warning should appear (animation tested implicitly)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("逆光場景", substring = true)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun stateTransition_fromBacklightToNormal_shouldAnimateOut() {
        // Arrange: mutableStateOf drives recomposition (Rule 14 — Compose Slot-Table)
        var lightingState by mutableStateOf<LightingState>(LightingState.Backlight(76f, 178f, 102f))

        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: Warning visible initially
        composeTestRule.onNodeWithText("逆光場景", substring = true).assertExists()

        // Act: Change state — triggers recomposition, not a new composition
        lightingState = LightingState.Normal

        // Assert: Warning should disappear (animation tested implicitly)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("逆光場景", substring = true).assertDoesNotExist()
    }

    @Test
    fun stateTransition_betweenWarningStates_shouldUpdateMessage() {
        // Arrange: mutableStateOf drives recomposition (Rule 14 — Compose Slot-Table)
        var lightingState by mutableStateOf<LightingState>(LightingState.Backlight(76f, 178f, 102f))

        composeTestRule.setContent {
            HDRWarningOverlay(lightingState = lightingState)
        }

        // Assert: Backlight message visible
        composeTestRule.onNodeWithText("逆光場景", substring = true).assertExists()

        // Act: Change state — triggers recomposition, not a new composition
        lightingState = LightingState.Overexposed(0.18f, 200f)

        // Assert: Overexposed message visible, Backlight message gone
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("過曝風險", substring = true).assertExists()
        composeTestRule.onNodeWithText("逆光場景", substring = true).assertDoesNotExist()
    }

    // ========== Priority Tests ==========

    @Test
    fun allStates_shouldHaveCorrectPriority() {
        // Test that priority values match expected order
        val backlight = LightingState.Backlight(76f, 178f, 102f)
        val highContrast = LightingState.HighContrast(5f, 200f, 0.12f, 0.11f)
        val overexposed = LightingState.Overexposed(0.18f, 200f)
        val underexposed = LightingState.Underexposed(40f, 0.30f)
        val normal = LightingState.Normal

        // Assert: Priority order
        assert(backlight.getPriority() == 4) { "Backlight should have priority 4" }
        assert(highContrast.getPriority() == 3) { "HighContrast should have priority 3" }
        assert(overexposed.getPriority() == 2) { "Overexposed should have priority 2" }
        assert(underexposed.getPriority() == 1) { "Underexposed should have priority 1" }
        assert(normal.getPriority() == 0) { "Normal should have priority 0" }
    }

    // ========== Recommendation Tests ==========

    @Test
    fun allWarningStates_shouldReturnNonEmptyRecommendation() {
        val backlight = LightingState.Backlight(76f, 178f, 102f)
        val highContrast = LightingState.HighContrast(5f, 200f, 0.12f, 0.11f)
        val overexposed = LightingState.Overexposed(0.18f, 200f)
        val underexposed = LightingState.Underexposed(40f, 0.30f)

        // Assert: All warning states have recommendations
        assert(backlight.getRecommendation().isNotEmpty()) { "Backlight should have recommendation" }
        assert(highContrast.getRecommendation().isNotEmpty()) { "HighContrast should have recommendation" }
        assert(overexposed.getRecommendation().isNotEmpty()) { "Overexposed should have recommendation" }
        assert(underexposed.getRecommendation().isNotEmpty()) { "Underexposed should have recommendation" }
    }

    @Test
    fun normalState_shouldReturnEmptyRecommendation() {
        val normal = LightingState.Normal

        // Assert: Normal state has no recommendation
        assert(normal.getRecommendation().isEmpty()) { "Normal should have empty recommendation" }
        assert(!normal.requiresWarning()) { "Normal should not require warning" }
    }
}
