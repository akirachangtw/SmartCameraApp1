/**
 * ClassName: GuidelineOverlay
 * Description: Composition guideline overlay for camera preview
 * Author: Claude Code
 * Date: 2026-01-03
 * Version: 2.7.0 Phase 2 Sprint 3
 *
 * Features:
 * - Dynamic guideline lines driven by OpenCV detection
 * - Fallback to rule of thirds grid when no detection
 * - Adapts to screen rotation and size
 * - Controlled by feature flag
 *
 * Modifications:
 * - 2026-01-03: Initial creation (Sprint 1 - fixed grid)
 * - 2026-01-05: Sprint 3 - Dynamic OpenCV-driven lines
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.model.GuidelineAnalysisResult

/**
 * Guideline Overlay - Dynamic composition guide lines
 *
 * v2.7.0 Phase 2 Sprint 3: OpenCV-driven version
 *
 * Displays detected guideline lines from OpenCV Hough Line Transform.
 * Falls back to rule of thirds grid when no lines are detected.
 *
 * Line rendering:
 * - Detected lines: Yellow/Gold color with varying opacity based on strength
 * - Fixed grid (fallback): White semi-transparent
 *
 * @param enabled Whether to show the guideline overlay
 * @param analysisResult OpenCV guideline analysis result (null = show fixed grid)
 * @param modifier Modifier for the canvas
 */
@Composable
fun GuidelineOverlay(
    enabled: Boolean,
    analysisResult: GuidelineAnalysisResult? = null,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Check if we have detected lines
        if (analysisResult != null && analysisResult.lines.isNotEmpty()) {
            // Render detected guideline lines
            analysisResult.lines.forEach { line ->
                // Convert normalized coordinates to screen pixels
                val start = Offset(
                    x = line.start.x * width,
                    y = line.start.y * height
                )
                val end = Offset(
                    x = line.end.x * width,
                    y = line.end.y * height
                )

                // Line color: Golden/Yellow for detected lines
                // Alpha varies with strength (0.3 - 1.0)
                val alpha = 0.3f + (line.strength * 0.7f)
                val lineColor = Color(0xFFFFD700).copy(alpha = alpha)  // Gold

                // Line width varies with strength (1.5dp - 3dp)
                val strokeWidth = (1.5f + (line.strength * 1.5f)).dp.toPx()

                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Fallback: Render fixed rule of thirds grid
            renderRuleOfThirdsGrid(width, height)
        }
    }
}

/**
 * Render rule of thirds grid (fallback)
 *
 * Classic 2x2 grid for composition assistance when no lines are detected.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderRuleOfThirdsGrid(
    width: Float,
    height: Float
) {
    val lineColor = Color.White.copy(alpha = 0.4f)
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
