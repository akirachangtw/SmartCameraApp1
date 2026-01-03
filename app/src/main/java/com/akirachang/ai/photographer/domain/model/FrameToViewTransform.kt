package com.akirachang.ai.photographer.domain.model

import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale

/**
 * Frame to View Transform - Coordinate mapping utility
 *
 * v2.7.0 Phase 2 Sprint 1
 *
 * Maps ImageAnalysis buffer coordinates (e.g., 640x480) to Compose View coordinates.
 * Handles rotation, mirroring, scaling, and cropping transformations.
 *
 * Reference: ML Kit MlKitAnalyzer's COORDINATE_SYSTEM_VIEW_REFERENCED implementation
 *
 * @param imageWidth Analysis buffer width (e.g., 640)
 * @param imageHeight Analysis buffer height (e.g., 480)
 * @param viewWidth Compose View width (screen pixels)
 * @param viewHeight Compose View height (screen pixels)
 * @param contentScale Compose ContentScale (Crop or Fit)
 * @param rotationDegrees Rotation angle (0, 90, 180, 270)
 * @param isFrontCamera Whether using front camera (needs mirroring)
 */
data class FrameToViewTransform(
    val imageWidth: Int,
    val imageHeight: Int,
    val viewWidth: Int,
    val viewHeight: Int,
    val contentScale: ContentScale,
    val rotationDegrees: Int,
    val isFrontCamera: Boolean
) {
    private val transformMatrix: Matrix by lazy {
        calculateTransformMatrix()
    }

    /**
     * Map a point from buffer coordinates to view coordinates
     *
     * @param bufferX X coordinate in image buffer
     * @param bufferY Y coordinate in image buffer
     * @return Offset in view coordinates
     */
    fun mapPoint(bufferX: Float, bufferY: Float): Offset {
        val point = floatArrayOf(bufferX, bufferY)
        transformMatrix.mapPoints(point)
        return Offset(point[0], point[1])
    }

    /**
     * Map a rectangle from buffer coordinates to view coordinates
     *
     * @param bufferRect Rectangle in image buffer coordinates
     * @return Rect in view coordinates
     */
    fun mapRect(bufferRect: Rect): Rect {
        // Map all four corners
        val topLeft = mapPoint(bufferRect.left, bufferRect.top)
        val topRight = mapPoint(bufferRect.right, bufferRect.top)
        val bottomLeft = mapPoint(bufferRect.left, bufferRect.bottom)
        val bottomRight = mapPoint(bufferRect.right, bufferRect.bottom)

        // Find bounding box of mapped points
        val left = minOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x)
        val top = minOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y)
        val right = maxOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x)
        val bottom = maxOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y)

        return Rect(left, top, right, bottom)
    }

    /**
     * Calculate the transformation matrix
     *
     * Transformation pipeline:
     * 1. Translate to origin (for rotation)
     * 2. Rotate by rotationDegrees
     * 3. Mirror (if front camera)
     * 4. Scale to view size (with ContentScale strategy)
     * 5. Translate to final position
     */
    private fun calculateTransformMatrix(): Matrix {
        val matrix = Matrix()

        // Step 1: Calculate rotated dimensions
        val (rotatedWidth, rotatedHeight) = when (rotationDegrees) {
            90, 270 -> imageHeight to imageWidth
            else -> imageWidth to imageHeight
        }

        // Step 2: Calculate scale factors based on ContentScale
        val (scaleX, scaleY, translateX, translateY) = when (contentScale) {
            ContentScale.Crop -> {
                // Fill view, may crop image
                val scale = maxOf(
                    viewWidth.toFloat() / rotatedWidth,
                    viewHeight.toFloat() / rotatedHeight
                )
                val tx = (viewWidth - rotatedWidth * scale) / 2f
                val ty = (viewHeight - rotatedHeight * scale) / 2f
                Quadruple(scale, scale, tx, ty)
            }
            ContentScale.Fit -> {
                // Fit view, may have letterbox/pillarbox
                val scale = minOf(
                    viewWidth.toFloat() / rotatedWidth,
                    viewHeight.toFloat() / rotatedHeight
                )
                val tx = (viewWidth - rotatedWidth * scale) / 2f
                val ty = (viewHeight - rotatedHeight * scale) / 2f
                Quadruple(scale, scale, tx, ty)
            }
            else -> {
                // Stretch to fill (not recommended for camera)
                val scaleX = viewWidth.toFloat() / rotatedWidth
                val scaleY = viewHeight.toFloat() / rotatedHeight
                Quadruple(scaleX, scaleY, 0f, 0f)
            }
        }

        // Step 3: Build transformation matrix
        // Order: Translate -> Rotate -> Mirror -> Scale -> Translate

        // 3.1: Translate to rotation center
        matrix.postTranslate(-imageWidth / 2f, -imageHeight / 2f)

        // 3.2: Rotate
        matrix.postRotate(rotationDegrees.toFloat())

        // 3.3: Mirror for front camera
        if (isFrontCamera) {
            matrix.postScale(-1f, 1f)
        }

        // 3.4: Scale
        matrix.postScale(scaleX, scaleY)

        // 3.5: Translate to final position
        matrix.postTranslate(
            viewWidth / 2f + translateX,
            viewHeight / 2f + translateY
        )

        return matrix
    }

    /**
     * Helper data class for quad values
     */
    private data class Quadruple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
