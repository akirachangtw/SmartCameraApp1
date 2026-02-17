package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.RectF
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import android.util.Log
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode

/**
 * PreviewViewportContainer - Unified coordinate system for camera preview
 *
 * v2.11.1 P0-2: Coordinate system unification
 *
 * This container calculates the actual content bounds (contentRect) that excludes
 * FIT_CENTER letterbox (black bars) and provides accurate coordinate transformation
 * between different coordinate spaces.
 *
 * **Problem Solved**:
 * - Grid lines extending beyond preview boundaries (Issue #2)
 * - Out-of-preview area triggering tap focus (Issue #1)
 * - Eye-AF focus points mapping to wrong positions (Issue #3)
 *
 * **Coordinate Spaces**:
 * 1. Screen Space: Full screen dimensions (includes letterbox)
 * 2. Content Space: Actual preview content (excludes letterbox)
 * 3. Image Space: ML Kit / ImageAnalysis bitmap coordinates
 *
 * @param targetAspectRatio Target aspect ratio mode (4:3, 16:9, 1:1)
 * @param onContentBoundsChanged Callback when content bounds are calculated
 * @param modifier Modifier for the container
 * @param content Composable content (PreviewView, overlays, etc.) — receives BoxScope for align() support
 */
@Composable
fun PreviewViewportContainer(
    targetAspectRatio: AspectRatioMode,
    onContentBoundsChanged: (RectF) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(RectF) -> Unit
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var contentBounds by remember { mutableStateOf(RectF(0f, 0f, 0f, 0f)) }

    // Calculate content bounds when screen size or aspect ratio changes
    LaunchedEffect(screenSize, targetAspectRatio) {
        if (screenSize.width > 0 && screenSize.height > 0) {
            val bounds = calculateContentBounds(
                screenWidth = screenSize.width,
                screenHeight = screenSize.height,
                targetAspectRatio = targetAspectRatio
            )
            contentBounds = bounds
            onContentBoundsChanged(bounds)

            Log.d(TAG, "[v2.11.1 P0-2] Content bounds calculated: " +
                    "left=${bounds.left}, top=${bounds.top}, " +
                    "right=${bounds.right}, bottom=${bounds.bottom}, " +
                    "width=${bounds.width()}, height=${bounds.height()}, " +
                    "aspectRatio=${targetAspectRatio.displayName}")
        }
    }

    Box(
        modifier = modifier.onSizeChanged { size ->
            screenSize = size
            Log.d(TAG, "[v2.11.1 P0-2] Screen size changed: ${size.width}x${size.height}")
        }
    ) {
        content(contentBounds)
    }
}

/**
 * Calculate content bounds for FIT_CENTER layout
 *
 * FIT_CENTER strategy:
 * - Scales preview to fit within screen while maintaining aspect ratio
 * - Adds letterbox (black bars) on sides or top/bottom
 * - Content is centered within screen
 *
 * @param screenWidth Screen width in pixels
 * @param screenHeight Screen height in pixels
 * @param targetAspectRatio Target aspect ratio mode
 * @return RectF representing content bounds (excludes letterbox)
 */
private fun calculateContentBounds(
    screenWidth: Int,
    screenHeight: Int,
    targetAspectRatio: AspectRatioMode
): RectF {
    val screenAspect = screenWidth.toFloat() / screenHeight.toFloat()
    val rational = targetAspectRatio.toRational()

    // v2.11.1 Phase 1 FIX: Handle portrait mode (height > width)
    // AspectRatioMode defines landscape ratios (e.g., 16:9 = width:height)
    // For portrait screens, we need to invert the ratio
    val isPortrait = screenHeight > screenWidth
    val targetAspect = if (isPortrait) {
        // Invert ratio for portrait: 16:9 becomes 9:16
        rational.denominator.toFloat() / rational.numerator.toFloat()
    } else {
        rational.numerator.toFloat() / rational.denominator.toFloat()
    }

    val (contentWidth, contentHeight) = if (screenAspect > targetAspect) {
        // Screen is wider than target → letterbox on left/right
        val width = screenHeight * targetAspect
        val height = screenHeight.toFloat()
        Pair(width, height)
    } else {
        // Screen is taller than target → letterbox on top/bottom
        val width = screenWidth.toFloat()
        val height = screenWidth / targetAspect
        Pair(width, height)
    }

    // Center content within screen
    val left = (screenWidth - contentWidth) / 2f
    val top = (screenHeight - contentHeight) / 2f
    val right = left + contentWidth
    val bottom = top + contentHeight

    return RectF(left, top, right, bottom)
}

/**
 * Coordinate transformation helper for PreviewViewportContainer
 *
 * Provides bidirectional coordinate mapping between:
 * - Screen Space (includes letterbox)
 * - Content Space (excludes letterbox)
 * - Image Space (ML Kit bitmap coordinates)
 */
class CoordinateTransformer(
    private val contentBounds: RectF,
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val isFrontCamera: Boolean = false
) {
    /**
     * Check if point is within content bounds
     *
     * @param x X coordinate in screen space
     * @param y Y coordinate in screen space
     * @return true if point is inside content area (not in letterbox)
     */
    fun isPointInContent(x: Float, y: Float): Boolean {
        return contentBounds.contains(x, y)
    }

    /**
     * Transform screen space coordinates to content space
     *
     * Converts full-screen coordinates (includes letterbox) to content-relative
     * coordinates (0,0 = content top-left).
     *
     * @param x X coordinate in screen space
     * @param y Y coordinate in screen space
     * @return Pair of (contentX, contentY), or null if outside content bounds
     */
    fun screenToContent(x: Float, y: Float): Pair<Float, Float>? {
        if (!isPointInContent(x, y)) {
            Log.w(TAG, "[CoordinateTransformer] Point ($x, $y) outside content bounds")
            return null
        }

        val contentX = x - contentBounds.left
        val contentY = y - contentBounds.top
        return contentX to contentY
    }

    /**
     * Transform content space coordinates to image space
     *
     * Converts content-relative coordinates to ML Kit bitmap coordinates.
     * Handles front camera mirroring if needed.
     *
     * @param contentX X coordinate in content space
     * @param contentY Y coordinate in content space
     * @return Pair of (imageX, imageY)
     */
    fun contentToImage(contentX: Float, contentY: Float): Pair<Float, Float> {
        val contentWidth = contentBounds.width()
        val contentHeight = contentBounds.height()

        // Scale to image coordinates
        var imageX = (contentX / contentWidth) * imageWidth
        val imageY = (contentY / contentHeight) * imageHeight

        // Handle front camera mirroring
        if (isFrontCamera) {
            imageX = imageWidth - imageX
        }

        return imageX to imageY
    }

    /**
     * Transform image space coordinates to content space
     *
     * Converts ML Kit bitmap coordinates to content-relative coordinates.
     * Handles front camera mirroring if needed.
     *
     * @param imageX X coordinate in image space
     * @param imageY Y coordinate in image space
     * @return Pair of (contentX, contentY)
     */
    fun imageToContent(imageX: Float, imageY: Float): Pair<Float, Float> {
        val contentWidth = contentBounds.width()
        val contentHeight = contentBounds.height()

        // Handle front camera mirroring
        var adjustedImageX = imageX
        if (isFrontCamera) {
            adjustedImageX = imageWidth - imageX
        }

        // Scale to content coordinates
        val contentX = (adjustedImageX / imageWidth) * contentWidth
        val contentY = (imageY / imageHeight) * contentHeight

        return contentX to contentY
    }

    /**
     * Transform image space coordinates to screen space
     *
     * One-step transformation from ML Kit bitmap to full-screen coordinates.
     *
     * @param imageX X coordinate in image space
     * @param imageY Y coordinate in image space
     * @return Pair of (screenX, screenY)
     */
    fun imageToScreen(imageX: Float, imageY: Float): Pair<Float, Float> {
        val (contentX, contentY) = imageToContent(imageX, imageY)
        val screenX = contentX + contentBounds.left
        val screenY = contentY + contentBounds.top
        return screenX to screenY
    }

    /**
     * Transform screen space coordinates to image space
     *
     * One-step transformation from full-screen coordinates to ML Kit bitmap.
     *
     * @param x X coordinate in screen space
     * @param y Y coordinate in screen space
     * @return Pair of (imageX, imageY), or null if outside content bounds
     */
    fun screenToImage(x: Float, y: Float): Pair<Float, Float>? {
        val contentCoords = screenToContent(x, y) ?: return null
        return contentToImage(contentCoords.first, contentCoords.second)
    }
}

private const val TAG = "PreviewViewportContainer"
