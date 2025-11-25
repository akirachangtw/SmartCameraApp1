/**
 * ClassName: CompositionState
 * Description: Represents the current composition analysis state for camera preview
 * Author: Claude Code
 * Date: 2025-11-25
 * Version: 1.5.0
 *
 * Features:
 * - Rule of Thirds analysis state
 * - Simple Good/Bad composition feedback
 * - Subject detection state
 *
 * Modifications:
 * - 2025-11-25: Initial creation for v1.5.0 prototype
 */
package local.mylab.smartcameraapp1.presentation.camera.model

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
    object Good : CompositionState()

    /**
     * Composition needs adjustment
     *
     * @param message Suggestion message for user (e.g., "Move subject to the right")
     */
    data class NeedsAdjustment(val message: String) : CompositionState()

    /**
     * No subject detected in frame
     *
     * Triggered when:
     * - No faces detected by ML Kit
     * - No primary objects detected
     */
    object NoSubject : CompositionState()
}
