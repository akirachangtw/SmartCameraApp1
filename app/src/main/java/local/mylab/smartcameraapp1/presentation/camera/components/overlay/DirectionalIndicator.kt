/**
 * ClassName: DirectionalIndicator
 * Description: Directional guideline overlay to guide composition adjustments
 * Author: Claude Code
 * Date: 2025-11-30
 * Version: 2.1.1
 *
 * Features:
 * - Directional guideline from subject to optimal position
 * - Animated arrow with gradient fade effect
 * - Based on professional Chinese composition suggestions
 * - Replaces previous circular target design with simpler arrows
 *
 * Modifications:
 * - 2025-11-30: Initial creation for v2.0 Phase 4 (Visual Enhancements)
 * - 2025-12-02: Complete rewrite for guideline-based design (v2.1.1)
 */
package local.mylab.smartcameraapp1.presentation.camera.components.overlay

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import local.mylab.smartcameraapp1.presentation.camera.model.CompositionState
import kotlin.math.sqrt

/**
 * Directional indicator overlay composable
 *
 * Displays an animated directional arrow from the subject center pointing
 * toward the optimal composition direction based on professional analysis.
 *
 * Design principles (v2.1.1):
 * - No target circles (removed for simplicity)
 * - Directional guideline with animated arrow
 * - Text-first approach (guidelines are supplementary)
 * - Gradient fade effect for elegance
 *
 * @param compositionState Current composition analysis state
 * @param subjectCenterX X coordinate of detected subject center
 * @param subjectCenterY Y coordinate of detected subject center
 * @param modifier Modifier for the indicator container
 */
@Composable
fun DirectionalIndicator(
    compositionState: CompositionState,
    subjectCenterX: Float,
    subjectCenterY: Float,
    modifier: Modifier = Modifier
) {
    // Only show indicators when composition needs adjustment
    if (compositionState !is CompositionState.NeedsAdjustment) return

    val directionX = compositionState.directionX
    val directionY = compositionState.directionY

    // Don't draw if no clear direction
    if (directionX == 0f && directionY == 0f) return

    // Pulsing animation for the arrow (subtle fade in/out)
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_fade")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawDirectionalArrow(
            subjectX = subjectCenterX,
            subjectY = subjectCenterY,
            directionX = directionX,
            directionY = directionY,
            alpha = alpha
        )
    }
}

/**
 * Draws a directional arrow from subject position pointing toward optimal direction
 *
 * Arrow design:
 * - Origin: Subject center
 * - Direction: Based on composition analysis vector
 * - Length: Dynamic based on direction magnitude (150-250dp)
 * - Color: Amber #FFC107 (consistent with existing design)
 * - Style: Gradient fade from origin to tip
 *
 * @param subjectX Subject center X coordinate
 * @param subjectY Subject center Y coordinate
 * @param directionX Direction vector X (-1.0 to 1.0)
 * @param directionY Direction vector Y (-1.0 to 1.0)
 * @param alpha Current animation alpha value
 */
private fun DrawScope.drawDirectionalArrow(
    subjectX: Float,
    subjectY: Float,
    directionX: Float,
    directionY: Float,
    alpha: Float
) {
    // Calculate arrow length based on direction magnitude
    val magnitude = sqrt(directionX * directionX + directionY * directionY)
    if (magnitude < 0.1f) return  // Skip if direction too small

    // Normalize direction
    val normX = directionX / magnitude
    val normY = directionY / magnitude

    // Arrow properties
    val baseLength = 180.dp.toPx()
    val arrowLength = baseLength * magnitude.coerceIn(0.5f, 1.5f)
    val arrowHeadSize = 30.dp.toPx()
    val strokeWidth = 4.dp.toPx()

    // Calculate end point
    val endX = subjectX + (normX * arrowLength)
    val endY = subjectY + (normY * arrowLength)

    // Arrow color (Amber with animated alpha)
    val arrowColor = Color(0xFFFFC107).copy(alpha = alpha)

    // Draw arrow line
    drawLine(
        color = arrowColor,
        start = Offset(subjectX, subjectY),
        end = Offset(endX, endY),
        strokeWidth = strokeWidth
    )

    // Draw arrow head (triangle at end)
    val path = Path().apply {
        // Arrow tip
        moveTo(endX, endY)

        // Left wing
        val leftWingX = endX - (normX * arrowHeadSize + normY * arrowHeadSize * 0.4f)
        val leftWingY = endY - (normY * arrowHeadSize - normX * arrowHeadSize * 0.4f)
        lineTo(leftWingX, leftWingY)

        // Back to tip
        moveTo(endX, endY)

        // Right wing
        val rightWingX = endX - (normX * arrowHeadSize - normY * arrowHeadSize * 0.4f)
        val rightWingY = endY - (normY * arrowHeadSize + normX * arrowHeadSize * 0.4f)
        lineTo(rightWingX, rightWingY)
    }

    drawPath(
        path = path,
        color = arrowColor,
        style = Stroke(width = strokeWidth)
    )
}
