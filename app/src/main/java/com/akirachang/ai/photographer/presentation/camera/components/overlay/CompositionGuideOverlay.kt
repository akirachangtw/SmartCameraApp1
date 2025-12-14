/**
 * ClassName: CompositionGuideOverlay
 * Description: Visual composition guide frame overlay for camera preview
 * Author: Claude Code
 * Date: 2025-12-08
 * Version: 2.4.0
 *
 * Features:
 * - Displays target frame showing ideal face position
 * - Color-coded alignment feedback (gray → green)
 * - Corner markers for visual emphasis
 * - Smooth animation transitions
 *
 * This overlay helps users quickly find the ideal composition position
 * by showing a visual guide frame where the face should be positioned.
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.presentation.camera.model.CompositionGuideData

/**
 * Composition guide overlay that displays a target frame for ideal positioning
 *
 * The overlay shows:
 * - A dashed rectangle indicating where the face should move to
 * - Corner markers for visual emphasis
 * - Color transitions based on alignment progress
 *
 * @param guideData Guide frame data containing target and current positions
 * @param modifier Modifier for customizing the overlay
 */
@Composable
fun CompositionGuideOverlay(
    guideData: CompositionGuideData?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = guideData != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        guideData?.let { data ->
            Canvas(modifier = modifier.fillMaxSize()) {
                drawGuideFrame(data)
            }
        }
    }
}

/**
 * Draws the composition guide frame with all visual elements
 */
private fun DrawScope.drawGuideFrame(guideData: CompositionGuideData) {
    val targetRect = guideData.targetRect
    val progress = guideData.alignmentProgress

    // Determine color based on alignment progress
    // Gray (#9E9E9E) → Light Green (#81C784) → Green (#4CAF50)
    val frameColor = when {
        guideData.isAligned -> Color(0xFF4CAF50) // Green - aligned
        progress > 0.7f -> Color(0xFF81C784)     // Light green - close
        progress > 0.4f -> Color(0xFFAED581)     // Lime - getting closer
        else -> Color(0xFF9E9E9E)                 // Gray - not aligned
    }

    // Calculate fill alpha based on progress
    val fillAlpha = when {
        guideData.isAligned -> 0.25f
        progress > 0.7f -> 0.20f
        else -> 0.15f
    }

    // Draw semi-transparent fill
    drawRect(
        color = frameColor.copy(alpha = fillAlpha),
        topLeft = Offset(targetRect.left, targetRect.top),
        size = Size(targetRect.width, targetRect.height)
    )

    // Draw dashed border
    val dashPathEffect = PathEffect.dashPathEffect(
        floatArrayOf(15f, 10f), 0f
    )
    val strokeWidth = 3.dp.toPx()

    // Use solid line when aligned, dashed when not
    val pathEffect = if (guideData.isAligned) null else dashPathEffect

    drawRect(
        color = frameColor.copy(alpha = 0.8f),
        topLeft = Offset(targetRect.left, targetRect.top),
        size = Size(targetRect.width, targetRect.height),
        style = Stroke(
            width = strokeWidth,
            pathEffect = pathEffect
        )
    )

    // Draw corner markers for emphasis
    drawCornerMarkers(targetRect, frameColor)
}

/**
 * Draws L-shaped corner markers at each corner of the target frame
 */
private fun DrawScope.drawCornerMarkers(
    rect: Rect,
    color: Color
) {
    val cornerLength = 25.dp.toPx()
    val strokeWidth = 4.dp.toPx()
    val markerColor = color.copy(alpha = 0.9f)

    // Top-left corner
    drawLine(
        color = markerColor,
        start = Offset(rect.left, rect.top),
        end = Offset(rect.left + cornerLength, rect.top),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = markerColor,
        start = Offset(rect.left, rect.top),
        end = Offset(rect.left, rect.top + cornerLength),
        strokeWidth = strokeWidth
    )

    // Top-right corner
    drawLine(
        color = markerColor,
        start = Offset(rect.right, rect.top),
        end = Offset(rect.right - cornerLength, rect.top),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = markerColor,
        start = Offset(rect.right, rect.top),
        end = Offset(rect.right, rect.top + cornerLength),
        strokeWidth = strokeWidth
    )

    // Bottom-left corner
    drawLine(
        color = markerColor,
        start = Offset(rect.left, rect.bottom),
        end = Offset(rect.left + cornerLength, rect.bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = markerColor,
        start = Offset(rect.left, rect.bottom),
        end = Offset(rect.left, rect.bottom - cornerLength),
        strokeWidth = strokeWidth
    )

    // Bottom-right corner
    drawLine(
        color = markerColor,
        start = Offset(rect.right, rect.bottom),
        end = Offset(rect.right - cornerLength, rect.bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = markerColor,
        start = Offset(rect.right, rect.bottom),
        end = Offset(rect.right, rect.bottom - cornerLength),
        strokeWidth = strokeWidth
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CompositionGuideOverlayPreview() {
    val mockGuideData = CompositionGuideData(
        targetRect = Rect(100f, 150f, 300f, 400f),
        currentRect = Rect(100f, 300f, 300f, 550f),
        isAligned = false,
        alignmentProgress = 0.5f
    )
    CompositionGuideOverlay(
        guideData = mockGuideData,
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CompositionGuideOverlayAlignedPreview() {
    val mockGuideData = CompositionGuideData(
        targetRect = Rect(100f, 150f, 300f, 400f),
        currentRect = Rect(100f, 160f, 300f, 410f),
        isAligned = true,
        alignmentProgress = 0.95f
    )
    CompositionGuideOverlay(
        guideData = mockGuideData,
        modifier = Modifier.fillMaxSize()
    )
}
