/**
 * ClassName: CompositionGuideData
 * Description: Data class for composition guide frame visualization
 * Author: Claude Code
 * Date: 2025-12-08
 * Version: 2.4.0
 *
 * Features:
 * - Target frame position (where face should move to)
 * - Current face position
 * - Alignment progress tracking
 * - Visual state management
 *
 * This data class supports the visual composition guide overlay that helps
 * users quickly find the ideal composition position by showing a target frame.
 */
package com.akirachang.ai.photographer.presentation.camera.model

import androidx.compose.ui.geometry.Rect

/**
 * Data class representing composition guide frame information
 *
 * The guide frame shows users where to move their face for ideal composition.
 * When the face aligns with the target frame, good composition is achieved.
 *
 * @param targetRect The target frame position (where face should be)
 * @param currentRect The current face bounding box position
 * @param isAligned Whether the face is currently aligned with target
 * @param alignmentProgress Progress from 0.0 (far) to 1.0 (perfectly aligned)
 */
data class CompositionGuideData(
    val targetRect: Rect,
    val currentRect: Rect,
    val isAligned: Boolean,
    val alignmentProgress: Float
) {
    companion object {
        /**
         * Creates an empty/invalid guide data
         * Used when guide should not be displayed
         */
        val EMPTY = CompositionGuideData(
            targetRect = Rect.Zero,
            currentRect = Rect.Zero,
            isAligned = false,
            alignmentProgress = 0f
        )
    }
}
