/**
 * ClassName: LightingState
 * Description: UI state model for lighting analysis and HDR warning overlay
 * Author: Claude Code
 * Date: 2025-12-20
 * Version: 2.6.0
 *
 * Features:
 * - Sealed class hierarchy for type-safe lighting conditions
 * - HDR warning states (High Contrast, Backlight)
 * - Exposure warning states (Overexposed, Underexposed)
 * - User-facing recommendations in Traditional Chinese
 * - Lighting quality scoring
 *
 * Usage:
 * This state is used by LightingWarningOverlay to display appropriate
 * warnings and recommendations to the user during camera preview.
 *
 * v2.6.0 Changes:
 * - Added Backlight state (separated from HighContrast)
 * - Renamed LowLight to Underexposed for clarity
 * - Added detailed metrics for each state
 * - Added priority system for multiple warnings
 */
package com.akirachang.ai.photographer.presentation.camera.model

/**
 * Represents the lighting condition of the current camera scene
 *
 * Used by the UI layer to display appropriate warnings and recommendations
 * when exposure or lighting issues are detected.
 */
sealed class LightingState {

    /**
     * Normal lighting condition - no warnings needed
     *
     * Criteria:
     * - No backlight
     * - No overexposure (< 15% highlights)
     * - Sufficient light (avg luminance ≥ 20%)
     * - Dynamic range within normal bounds (< 180)
     */
    data object Normal : LightingState()

    /**
     * High contrast scene requiring HDR
     *
     * Occurs when:
     * - Significant highlights (> 10%) AND shadows (> 10%) present
     * - OR dynamic range > 180 (out of 255)
     *
     * Common scenarios: Window scenes, sunset/sunrise, indoor-outdoor mix
     *
     * @property contrastRatio Ratio between brightest and darkest regions
     * @property dynamicRange Estimated dynamic range (0-255)
     * @property highlightRatio Proportion of highlight pixels (0-1)
     * @property shadowRatio Proportion of shadow pixels (0-1)
     * @property recommendation User-facing suggestion in Traditional Chinese
     */
    data class HighContrast(
        val contrastRatio: Float,
        val dynamicRange: Float,
        val highlightRatio: Float,
        val shadowRatio: Float
    ) : LightingState()

    /**
     * Backlight condition - subject darker than background
     *
     * Occurs when:
     * - Subject luminance < 30% (76/255)
     * - Background luminance > 70% (178/255)
     * - OR significant luminance difference (> 80) with subject in shadow
     *
     * Common scenarios: Person against window, sunset portraits, stage lighting
     *
     * @property subjectLuminance Average luminance of subject region (0-255)
     * @property backgroundLuminance Average luminance of background (0-255)
     * @property luminanceDifference Absolute difference between subject and background
     * @property recommendation User-facing suggestion in Traditional Chinese
     */
    data class Backlight(
        val subjectLuminance: Float,
        val backgroundLuminance: Float,
        val luminanceDifference: Float
    ) : LightingState()

    /**
     * Overexposure condition - excessive highlights
     *
     * Occurs when:
     * - Highlight ratio (240-255) > 15% of pixels
     *
     * Common scenarios: Bright outdoor scenes, direct sunlight, white backgrounds
     *
     * @property highlightRatio Proportion of highlight pixels (0-1)
     * @property avgLuminance Average scene luminance (0-255)
     * @property recommendation User-facing suggestion in Traditional Chinese
     */
    data class Overexposed(
        val highlightRatio: Float,
        val avgLuminance: Float
    ) : LightingState()

    /**
     * Underexposure condition - insufficient light
     *
     * Occurs when:
     * - Average luminance < 20% (51/255)
     *
     * Common scenarios: Indoor low-light, night scenes, dark environments
     *
     * @property avgLuminance Average scene luminance (0-255)
     * @property shadowRatio Proportion of shadow pixels (0-1)
     * @property recommendation User-facing suggestion in Traditional Chinese
     */
    data class Underexposed(
        val avgLuminance: Float,
        val shadowRatio: Float
    ) : LightingState()

    /**
     * Get display priority for this lighting state
     *
     * Higher priority states should be displayed when multiple issues exist.
     * Priority order:
     * 1. Backlight (most critical - affects subject visibility)
     * 2. High Contrast (requires HDR mode)
     * 3. Overexposed (clipping loss)
     * 4. Underexposed (noise/detail loss)
     * 5. Normal (no warning)
     *
     * @return Priority value (higher = more critical)
     */
    fun getPriority(): Int = when (this) {
        is Backlight -> 4
        is HighContrast -> 3
        is Overexposed -> 2
        is Underexposed -> 1
        is Normal -> 0
    }

    /**
     * Check if this state requires user warning
     */
    fun requiresWarning(): Boolean = this !is Normal

    /**
     * Get recommendation text for display
     */
    fun getRecommendation(): String = when (this) {
        is Backlight -> "逆光場景 - 建議使用 HDR 或開啟補光"
        is HighContrast -> {
            when {
                highlightRatio > 0.20f -> "高反差場景 - 注意高光細節，建議使用 HDR"
                else -> "高對比場景 - 建議使用 HDR 模式"
            }
        }
        is Overexposed -> "過曝風險 - 建議降低曝光補償或避免直射光線"
        is Underexposed -> "光線不足 - 建議增加照明或提高 ISO"
        is Normal -> ""
    }
}
