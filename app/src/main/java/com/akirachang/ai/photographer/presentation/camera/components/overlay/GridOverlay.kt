/**
 * ClassName: GridOverlay
 * Description: Draws Rule of Thirds grid lines on camera preview
 * Author: Claude Code
 * Date: 2025-11-25
 * Version: 1.5.0
 *
 * Features:
 * - Static Rule of Thirds grid (3x3 grid with 2 vertical and 2 horizontal lines)
 * - Semi-transparent lines for better visibility
 * - Composable overlay that can be stacked on camera preview
 *
 * Modifications:
 * - 2025-11-25: Initial creation for v1.5.0 prototype
 * - 2026-04-11: D3-T2 Cueshot alignment — solid lines (dashes removed), alpha 0.4→0.5
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Grid overlay composable for Rule of Thirds guidance
 *
 * Draws a 3x3 grid that divides the screen into thirds both horizontally
 * and vertically, highlighting the four intersection points which are
 * considered optimal positions for subject placement in photography.
 *
 * v2.11.1 P0-2: Supports contentBounds to restrict grid to preview area only (excludes letterbox)
 *
 * @param modifier Modifier for the canvas
 * @param visible Whether to show the grid (default: true)
 * @param color Color of the grid lines (default: semi-transparent white)
 * @param strokeWidth Width of the grid lines in pixels (default: 2f)
 * @param contentBounds Actual preview content bounds (excludes FIT_CENTER letterbox). If null, uses full canvas size.
 */
@Composable
fun GridOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    color: Color = Color.White.copy(alpha = 0.08f),
    strokeWidth: Float = 0.5f,
    contentBounds: RectF? = null  // v2.11.1 P0-2: Optional content bounds
) {
    if (!visible) return

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRuleOfThirdsGrid(
            color = color,
            strokeWidth = strokeWidth,
            contentBounds = contentBounds
        )
    }
}

/**
 * Draws the Rule of Thirds grid
 *
 * The grid consists of:
 * - 2 vertical lines at 1/3 and 2/3 of the width
 * - 2 horizontal lines at 1/3 and 2/3 of the height
 *
 * The four intersection points are the ideal positions for subject placement.
 *
 * v2.11.1 P0-2: Supports contentBounds to restrict grid to preview area
 */
private fun DrawScope.drawRuleOfThirdsGrid(
    color: Color,
    strokeWidth: Float,
    contentBounds: RectF?
) {
    // v2.11.1 P0-2: Use contentBounds if provided, otherwise use full canvas size
    val bounds = contentBounds ?: RectF(0f, 0f, size.width, size.height)

    val left = bounds.left
    val top = bounds.top
    val width = bounds.width()
    val height = bounds.height()

    // Calculate third positions relative to content bounds
    val leftThird = left + width / 3f
    val rightThird = left + width * 2f / 3f
    val topThird = top + height / 3f
    val bottomThird = top + height * 2f / 3f

    // D3-T2: Solid lines (no dash) per Cueshot ambient UI spec
    // Draw vertical lines (within content bounds)
    drawLine(
        color = color,
        start = Offset(leftThird, top),
        end = Offset(leftThird, top + height),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = Offset(rightThird, top),
        end = Offset(rightThird, top + height),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Draw horizontal lines (within content bounds)
    drawLine(
        color = color,
        start = Offset(left, topThird),
        end = Offset(left + width, topThird),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = Offset(left, bottomThird),
        end = Offset(left + width, bottomThird),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Optional: Draw small circles at intersection points to highlight them
    // Uncomment if you want to emphasize the optimal subject positions
    /*
    val circleRadius = 8f
    val intersectionColor = color.copy(alpha = color.alpha * 1.5f)

    // Top-left intersection
    drawCircle(
        color = intersectionColor,
        radius = circleRadius,
        center = Offset(leftThird, topThird)
    )

    // Top-right intersection
    drawCircle(
        color = intersectionColor,
        radius = circleRadius,
        center = Offset(rightThird, topThird)
    )

    // Bottom-left intersection
    drawCircle(
        color = intersectionColor,
        radius = circleRadius,
        center = Offset(leftThird, bottomThird)
    )

    // Bottom-right intersection
    drawCircle(
        color = intersectionColor,
        radius = circleRadius,
        center = Offset(rightThird, bottomThird)
    )
    */
}
