/**
 * ClassName: CompositionState
 * Description: Represents the current composition analysis state for camera preview
 * Author: Claude Code
 * Date: 2025-11-25
 * Version: 2.4.0
 *
 * Features:
 * - Rule of Thirds analysis state
 * - Simple Good/Bad composition feedback
 * - Subject detection state
 * - Direction guidance vectors (v2.1.1)
 * - Visual composition guide data (v2.4.0)
 *
 * Modifications:
 * - 2025-11-25: Initial creation for v1.5.0 prototype
 * - 2025-12-02: Add direction vectors for guideline rendering (v2.1.1)
 * - 2025-12-08: Add CompositionGuideData for visual guide overlay (v2.4.0)
 */
package com.akirachang.ai.photographer.presentation.camera.model

/**
 * Sealed class representing composition analysis states
 *
 * This is a simplified prototype implementation focusing on Rule of Thirds.
 * Future versions will include more sophisticated composition rules.
 */
sealed class CompositionState {

    /**
     * Good composition detected
     *
     * Triggered when:
     * - Main subject (face) is positioned near Rule of Thirds intersection points
     * - Subject is well-positioned within the frame
     */
    data class Good(
        val guideData: CompositionGuideData? = null
    ) : CompositionState()

    /**
     * Composition needs adjustment
     *
     * @param message Professional suggestion message for user (e.g., "請上移，使眼睛位於上三分線")
     * @param directionX Direction vector X component (-1.0 to 1.0) for guideline rendering
     * @param directionY Direction vector Y component (-1.0 to 1.0) for guideline rendering
     * @param guideData Visual composition guide data for overlay rendering (v2.4.0)
     *
     * Direction vector interpretation:
     * - directionX: -1.0 = left, 0.0 = centered, 1.0 = right
     * - directionY: -1.0 = up, 0.0 = centered, 1.0 = down
     */
    data class NeedsAdjustment(
        val message: String,
        val directionX: Float = 0f,
        val directionY: Float = 0f,
        val guideData: CompositionGuideData? = null
    ) : CompositionState()

    /**
     * No subject detected in frame
     *
     * Triggered when:
     * - No faces detected by ML Kit
     * - No primary objects detected
     */
    object NoSubject : CompositionState()
}
