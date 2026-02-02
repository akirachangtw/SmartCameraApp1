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
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Grid overlay composable for Rule of Thirds guidance
 *
 * Draws a 3x3 grid that divides the screen into thirds both horizontally
 * and vertically, highlighting the four intersection points which are
 * considered optimal positions for subject placement in photography.
 *
 * @param modifier Modifier for the canvas
 * @param visible Whether to show the grid (default: true)
 * @param color Color of the grid lines (default: semi-transparent white)
 * @param strokeWidth Width of the grid lines in pixels (default: 2f)
 */
@Composable
fun GridOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    color: Color = Color.White.copy(alpha = 0.4f),
    strokeWidth: Float = 2f
) {
    if (!visible) return

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRuleOfThirdsGrid(
            color = color,
            strokeWidth = strokeWidth
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
 */
private fun DrawScope.drawRuleOfThirdsGrid(
    color: Color,
    strokeWidth: Float
) {
    val width = size.width
    val height = size.height

    // Calculate third positions
    val leftThird = width / 3f
    val rightThird = width * 2f / 3f
    val topThird = height / 3f
    val bottomThird = height * 2f / 3f

    // Optional: Dashed line effect for less intrusive appearance
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)

    // Draw vertical lines
    drawLine(
        color = color,
        start = Offset(leftThird, 0f),
        end = Offset(leftThird, height),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = pathEffect
    )

    drawLine(
        color = color,
        start = Offset(rightThird, 0f),
        end = Offset(rightThird, height),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = pathEffect
    )

    // Draw horizontal lines
    drawLine(
        color = color,
        start = Offset(0f, topThird),
        end = Offset(width, topThird),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = pathEffect
    )

    drawLine(
        color = color,
        start = Offset(0f, bottomThird),
        end = Offset(width, bottomThird),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = pathEffect
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
