/**
 * ClassName: LevelIndicator
 * Description: Visual level indicator overlay showing device tilt angle
 * Author: Claude Code
 * Date: 2025-12-01
 * Version: 2.1.0
 *
 * Modifications:
 * - 2025-12-01: Initial creation for V1.0 completion (LevelIndicator feature)
 */
package local.mylab.smartcameraapp1.presentation.camera.components.overlay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Level indicator overlay component
 *
 * Displays a visual horizon line that rotates based on device tilt.
 * - Green line when device is level (±2°)
 * - Amber line when device needs adjustment
 * - White dashed reference line showing true horizon
 * - Angle text display showing current tilt
 *
 * @param roll Device roll angle in degrees (-180 to 180)
 *             Positive: tilted right, Negative: tilted left, 0: level
 * @param visible Whether to show the indicator
 * @param modifier Modifier for this composable
 */
@Composable
fun LevelIndicator(
    roll: Float,
    visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    // Smooth animation for roll angle changes
    val animatedRoll by animateFloatAsState(
        targetValue = roll,
        animationSpec = tween(durationMillis = 100),
        label = "roll_animation"
    )

    // Determine if device is level (within ±2°)
    val isLevel = abs(animatedRoll) <= 2.0f

    // Color based on level status
    val lineColor = if (isLevel) {
        Color(0xFF4CAF50) // Green when level
    } else {
        Color(0xFFFFA726) // Amber when tilted
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Canvas for drawing horizon lines
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2

            // Reference line (dashed white - true horizon)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 10f),
                    phase = 0f
                )
            )

            // Calculate tilt offset
            // For every degree of tilt, move the line proportionally
            val tiltOffset = (animatedRoll / 90f) * (canvasHeight / 2)

            // Horizon line (colored based on level status)
            drawLine(
                color = lineColor,
                start = Offset(0f, centerY + tiltOffset),
                end = Offset(canvasWidth, centerY + tiltOffset),
                strokeWidth = 4f
            )

            // Center indicator circle (reference point)
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(canvasWidth / 2, centerY),
                style = Stroke(width = 2f)
            )
        }

        // Angle text display
        Text(
            text = "${animatedRoll.toInt()}°",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 8.dp),
            color = lineColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
