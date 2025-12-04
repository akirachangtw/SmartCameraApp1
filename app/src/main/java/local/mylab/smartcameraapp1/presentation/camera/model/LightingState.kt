/**
 * ClassName: LightingState
 * Description: UI state for lighting analysis and HDR warnings
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - Sealed class for type-safe state management
 * - Different states for various lighting conditions
 * - Chinese text suggestions for UI display
 */
package local.mylab.smartcameraapp1.presentation.camera.model

import local.mylab.smartcameraapp1.domain.model.ExposureAnalysis

/**
 * Represents the lighting state of the current camera scene
 */
sealed class LightingState {
    /**
     * Normal exposure - no warnings needed
     */
    data object Normal : LightingState()

    /**
     * High contrast scene - HDR recommended
     *
     * @property analysis Full exposure analysis data
     * @property message Chinese warning message
     */
    data class HighContrast(
        val analysis: ExposureAnalysis,
        val message: String
    ) : LightingState() {
        val isBacklit: Boolean get() = analysis.isBacklit
        val isOverexposed: Boolean get() = analysis.isOverexposed
        val shouldShowHDRWarning: Boolean get() = analysis.shouldUseHDR
    }

    /**
     * Low light scene - needs increased exposure or light source
     *
     * @property avgLuminance Average scene luminance (0-255)
     * @property message Chinese warning message
     */
    data class LowLight(
        val avgLuminance: Float,
        val message: String
    ) : LightingState()

    /**
     * Overexposed scene - reduce exposure
     *
     * @property highlightRatio Proportion of clipped highlights
     * @property message Chinese warning message
     */
    data class Overexposed(
        val highlightRatio: Float,
        val message: String
    ) : LightingState()
}
