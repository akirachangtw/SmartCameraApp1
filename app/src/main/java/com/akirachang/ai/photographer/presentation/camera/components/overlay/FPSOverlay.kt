/**
 * ClassName: FPSOverlay
 * Description: Real-time FPS performance monitor overlay
 * Author: Claude Code
 * Date: 2026-01-08
 * Version: 2.8.4
 *
 * Features:
 * - Real-time FPS display for performance validation
 * - Color-coded feedback (green >= 26 FPS, amber 20-25 FPS, red < 20 FPS)
 * - Compact design for minimal visual intrusion
 * - Toggle visibility for production builds
 *
 * Modifications:
 * - 2026-01-08: Initial creation for v2.8.4 Quick Win validation
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FPS overlay composable
 *
 * Displays real-time frame rate for performance monitoring during
 * Quick Win validation and optimization work.
 *
 * Color coding:
 * - Green: >= 26 FPS (target achieved)
 * - Amber: 20-25 FPS (close to target)
 * - Red: < 20 FPS (below baseline)
 *
 * @param fps Current frames per second, null if not available
 * @param visible Whether to show the overlay
 * @param modifier Modifier for the overlay container
 */
@Composable
fun FPSOverlay(
    fps: Float?,
    visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!visible || fps == null) return

    val (backgroundColor, textColor) = when {
        fps >= 26f -> Color(0xFF4CAF50).copy(alpha = 0.85f) to Color.White  // Green - target achieved
        fps >= 20f -> Color(0xFFFFC107).copy(alpha = 0.85f) to Color.Black  // Amber - close
        else -> Color(0xFFF44336).copy(alpha = 0.85f) to Color.White        // Red - below baseline
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = String.format("%.1f FPS", fps),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,  // Monospace for stable width
            style = MaterialTheme.typography.bodySmall
        )
    }
}
