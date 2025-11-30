/**
 * ClassName: DirectionalIndicator
 * Description: Visual directional arrows to guide composition adjustments
 * Author: Claude Code
 * Date: 2025-11-30
 * Version: 2.0.0-beta02
 *
 * Features:
 * - Animated directional arrows based on composition suggestions
 * - Supports 8 directions (Up, Down, Left, Right, and combinations)
 * - Smooth fade-in/fade-out animations
 * - Only shown when composition needs adjustment
 *
 * Modifications:
 * - 2025-11-30: Initial creation for v2.0 Phase 4 (Visual Enhancements)
 */
package local.mylab.smartcameraapp1.presentation.camera.components.overlay

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import local.mylab.smartcameraapp1.presentation.camera.model.CompositionState

/**
 * Directional indicator overlay composable
 *
 * Displays animated arrows pointing in the direction the user should move
 * the camera/subject to improve composition. Arrows appear only when
 * composition needs adjustment.
 *
 * @param compositionState Current composition analysis state
 * @param modifier Modifier for the indicator container
 */
@Composable
fun DirectionalIndicator(
    compositionState: CompositionState,
    modifier: Modifier = Modifier
) {
    // Only show indicators when composition needs adjustment
    if (compositionState !is CompositionState.NeedsAdjustment) return

    val direction = parseDirection(compositionState.message)
    if (direction == Direction.NONE) return

    // Pulsing animation for the arrows
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Draw arrows based on direction
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arrowColor = Color(0xFFFFC107).copy(alpha = alpha) // Amber
            val strokeWidth = 4.dp.toPx()

            when (direction) {
                Direction.UP -> drawArrowUp(arrowColor, strokeWidth)
                Direction.DOWN -> drawArrowDown(arrowColor, strokeWidth)
                Direction.LEFT -> drawArrowLeft(arrowColor, strokeWidth)
                Direction.RIGHT -> drawArrowRight(arrowColor, strokeWidth)
                Direction.UP_LEFT -> {
                    drawArrowUp(arrowColor, strokeWidth)
                    drawArrowLeft(arrowColor, strokeWidth)
                }
                Direction.UP_RIGHT -> {
                    drawArrowUp(arrowColor, strokeWidth)
                    drawArrowRight(arrowColor, strokeWidth)
                }
                Direction.DOWN_LEFT -> {
                    drawArrowDown(arrowColor, strokeWidth)
                    drawArrowLeft(arrowColor, strokeWidth)
                }
                Direction.DOWN_RIGHT -> {
                    drawArrowDown(arrowColor, strokeWidth)
                    drawArrowRight(arrowColor, strokeWidth)
                }
                Direction.NONE -> {} // No arrows
            }
        }
    }
}

/**
 * Direction enum representing possible movement directions
 */
private enum class Direction {
    NONE,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    UP_LEFT,
    UP_RIGHT,
    DOWN_LEFT,
    DOWN_RIGHT
}

/**
 * Parses composition suggestion message to determine arrow direction
 *
 * Examples:
 * - "Move Up" → UP
 * - "Move Down and Right" → DOWN_RIGHT
 * - "Move Left" → LEFT
 * - "Move Away From Edge" → NONE (no specific direction)
 */
private fun parseDirection(message: String): Direction {
    val hasUp = message.contains("Up", ignoreCase = true)
    val hasDown = message.contains("Down", ignoreCase = true)
    val hasLeft = message.contains("Left", ignoreCase = true)
    val hasRight = message.contains("Right", ignoreCase = true)

    return when {
        hasUp && hasLeft -> Direction.UP_LEFT
        hasUp && hasRight -> Direction.UP_RIGHT
        hasDown && hasLeft -> Direction.DOWN_LEFT
        hasDown && hasRight -> Direction.DOWN_RIGHT
        hasUp -> Direction.UP
        hasDown -> Direction.DOWN
        hasLeft -> Direction.LEFT
        hasRight -> Direction.RIGHT
        else -> Direction.NONE
    }
}

/**
 * Draws an upward-pointing arrow at the top center of the screen
 */
private fun DrawScope.drawArrowUp(color: Color, strokeWidth: Float) {
    val centerX = size.width / 2f
    val topY = size.height * 0.15f // 15% from top
    val arrowSize = 60.dp.toPx()

    val path = Path().apply {
        // Arrow pointing up
        moveTo(centerX, topY)
        lineTo(centerX - arrowSize / 2, topY + arrowSize)
        moveTo(centerX, topY)
        lineTo(centerX + arrowSize / 2, topY + arrowSize)
        // Arrow stem
        moveTo(centerX, topY + arrowSize * 0.3f)
        lineTo(centerX, topY + arrowSize * 1.2f)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

/**
 * Draws a downward-pointing arrow at the bottom center of the screen
 */
private fun DrawScope.drawArrowDown(color: Color, strokeWidth: Float) {
    val centerX = size.width / 2f
    val bottomY = size.height * 0.85f // 15% from bottom
    val arrowSize = 60.dp.toPx()

    val path = Path().apply {
        // Arrow pointing down
        moveTo(centerX, bottomY)
        lineTo(centerX - arrowSize / 2, bottomY - arrowSize)
        moveTo(centerX, bottomY)
        lineTo(centerX + arrowSize / 2, bottomY - arrowSize)
        // Arrow stem
        moveTo(centerX, bottomY - arrowSize * 0.3f)
        lineTo(centerX, bottomY - arrowSize * 1.2f)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

/**
 * Draws a leftward-pointing arrow at the left center of the screen
 */
private fun DrawScope.drawArrowLeft(color: Color, strokeWidth: Float) {
    val leftX = size.width * 0.15f // 15% from left
    val centerY = size.height / 2f
    val arrowSize = 60.dp.toPx()

    val path = Path().apply {
        // Arrow pointing left
        moveTo(leftX, centerY)
        lineTo(leftX + arrowSize, centerY - arrowSize / 2)
        moveTo(leftX, centerY)
        lineTo(leftX + arrowSize, centerY + arrowSize / 2)
        // Arrow stem
        moveTo(leftX + arrowSize * 0.3f, centerY)
        lineTo(leftX + arrowSize * 1.2f, centerY)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

/**
 * Draws a rightward-pointing arrow at the right center of the screen
 */
private fun DrawScope.drawArrowRight(color: Color, strokeWidth: Float) {
    val rightX = size.width * 0.85f // 15% from right
    val centerY = size.height / 2f
    val arrowSize = 60.dp.toPx()

    val path = Path().apply {
        // Arrow pointing right
        moveTo(rightX, centerY)
        lineTo(rightX - arrowSize, centerY - arrowSize / 2)
        moveTo(rightX, centerY)
        lineTo(rightX - arrowSize, centerY + arrowSize / 2)
        // Arrow stem
        moveTo(rightX - arrowSize * 0.3f, centerY)
        lineTo(rightX - arrowSize * 1.2f, centerY)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}
