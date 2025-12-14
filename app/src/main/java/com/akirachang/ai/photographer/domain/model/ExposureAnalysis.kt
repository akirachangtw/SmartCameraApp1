/**
 * ClassName: ExposureAnalysis
 * Description: Data model for exposure and lighting analysis results
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - Backlight detection (subject darker than background)
 * - Overexposure detection (excessive highlights)
 * - Low light detection (insufficient illumination)
 * - Exposure statistics
 */
package com.akirachang.ai.photographer.domain.model

/**
 * Represents the result of exposure analysis on a camera frame
 *
 * @property isBacklit True if subject is backlit (subject dark, background bright)
 * @property isOverexposed True if significant highlight clipping (>15% pixels at 240-255)
 * @property isLowLight True if scene is underlit (avg luminance < 20%)
 * @property highlightRatio Proportion of pixels in highlight range (240-255)
 * @property shadowRatio Proportion of pixels in shadow range (0-30)
 * @property avgLuminance Average luminance value (0-255)
 * @property dynamicRange Estimated dynamic range (max - min luminance levels with >1% pixels)
 */
data class ExposureAnalysis(
    val isBacklit: Boolean,
    val isOverexposed: Boolean,
    val isLowLight: Boolean,
    val highlightRatio: Float,
    val shadowRatio: Float,
    val avgLuminance: Float,
    val dynamicRange: Float = 0f
) {
    /**
     * Check if this is a high contrast scene (requires HDR)
     * High contrast = significant both highlights AND shadows
     */
    val isHighContrast: Boolean
        get() = (highlightRatio > 0.10f && shadowRatio > 0.10f) || dynamicRange > 180f

    /**
     * Get overall exposure quality score (0.0 - 1.0)
     * 1.0 = perfect exposure, 0.0 = severe issues
     */
    val exposureScore: Float
        get() {
            var score = 1.0f

            // Penalize overexposure
            if (isOverexposed) score -= 0.3f

            // Penalize low light
            if (isLowLight) score -= 0.2f

            // Penalize backlight (most severe)
            if (isBacklit) score -= 0.4f

            // Penalize high contrast
            if (isHighContrast) score -= 0.2f

            return score.coerceAtLeast(0f)
        }

    /**
     * Get suggested camera adjustment in Chinese
     */
    fun getSuggestion(): String {
        return when {
            isBacklit -> "逆光場景 - 建議使用 HDR 或補光"
            isOverexposed -> "過曝風險 - 降低曝光補償"
            isLowLight -> "光線不足 - 提高 ISO 或使用補光"
            isHighContrast -> "高對比場景 - 建議使用 HDR"
            else -> "曝光正常"
        }
    }

    /**
     * Check if HDR mode is recommended for this scene
     */
    val shouldUseHDR: Boolean
        get() = isBacklit || isHighContrast
}
