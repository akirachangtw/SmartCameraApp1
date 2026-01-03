/**
 * ClassName: GuidelineOverlay
 * Description: Composition guideline overlay for camera preview
 * Author: Claude Code
 * Date: 2026-01-03
 * Version: 2.7.0 Phase 2 Sprint 1
 *
 * Features:
 * - Rule of thirds grid (2 vertical + 2 horizontal lines)
 * - Fixed overlay (not driven by OpenCV yet)
 * - Adapts to screen rotation and size
 * - Controlled by feature flag
 *
 * Modifications:
 * - 2026-01-03: Initial creation (Sprint 1 - fixed grid)
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Guideline Overlay - Composition guide lines
 *
 * v2.7.0 Phase 2 Sprint 1: Fixed grid version
 *
 * Displays rule of thirds grid over camera preview to assist with composition.
 * The grid consists of:
 * - 2 vertical lines at 1/3 and 2/3 of width
 * - 2 horizontal lines at 1/3 and 2/3 of height
 *
 * Future versions (Sprint 3) will make this dynamic based on OpenCV analysis.
 *
 * @param enabled Whether to show the guideline overlay
 * @param modifier Modifier for the canvas
 */
@Composable
fun GuidelineOverlay(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val lineColor = Color.White.copy(alpha = 0.5f)
        val strokeWidth = 1.5.dp.toPx()

        // Vertical line at 1/3
        drawLine(
            color = lineColor,
            start = Offset(width / 3f, 0f),
            end = Offset(width / 3f, height),
            strokeWidth = strokeWidth
        )

        // Vertical line at 2/3
        drawLine(
            color = lineColor,
            start = Offset(width * 2f / 3f, 0f),
            end = Offset(width * 2f / 3f, height),
            strokeWidth = strokeWidth
        )

        // Horizontal line at 1/3
        drawLine(
            color = lineColor,
            start = Offset(0f, height / 3f),
            end = Offset(width, height / 3f),
            strokeWidth = strokeWidth
        )

        // Horizontal line at 2/3
        drawLine(
            color = lineColor,
            start = Offset(0f, height * 2f / 3f),
            end = Offset(width, height * 2f / 3f),
            strokeWidth = strokeWidth
        )
    }
}
