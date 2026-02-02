package com.akirachang.ai.photographer.util

import android.util.Log
import org.opencv.core.Point

/**
 * Coordinate Mapping Utility
 *
 * v2.x.x Phase 3: PreviewView ↔ ImageAnalysis coordinate conversion
 *
 * Handles coordinate transformations between different coordinate systems:
 * - PreviewView coordinates: User tap coordinates on the screen
 * - ImageAnalysis coordinates: Coordinates in the camera frame
 *
 * Considerations:
 * - Image rotation (0°, 90°, 180°, 270°)
 * - Front camera mirroring
 * - Different aspect ratios (PreviewView vs ImageAnalysis)
 * - ScaleType (FIT_CENTER, FILL_CENTER, etc.)
 *
 * Note: This is a simplified implementation for v2.x.x Phase 3.
 * Assumes ImageAnalysis and PreviewView have the same aspect ratio and no rotation.
 * Full rotation/mirroring support will be added in Phase 4 if needed.
 */
object CoordinateMapper {
    private const val TAG = "CoordinateMapper"

    /**
     * Convert PreviewView coordinates to ImageAnalysis coordinates
     *
     * Simplified version: Assumes no rotation and direct scaling
     *
     * @param x X coordinate in PreviewView (0.0 to previewWidth)
     * @param y Y coordinate in PreviewView (0.0 to previewHeight)
     * @param previewWidth Width of PreviewView in pixels
     * @param previewHeight Height of PreviewView in pixels
     * @param imageWidth Width of ImageAnalysis frame in pixels
     * @param imageHeight Height of ImageAnalysis frame in pixels
     * @param rotation Image rotation in degrees (0, 90, 180, 270) - not yet supported
     * @param isFrontCamera Whether the camera is front-facing (for mirroring) - not yet supported
     * @return Point in ImageAnalysis coordinates
     */
    fun previewToImage(
        x: Float,
        y: Float,
        previewWidth: Int,
        previewHeight: Int,
        imageWidth: Int,
        imageHeight: Int,
        rotation: Int = 0,
        isFrontCamera: Boolean = false
    ): Point {
        // Phase 3: Simplified mapping (direct scaling)
        // Assumes PreviewView and ImageAnalysis have the same orientation

        // Calculate scale factors
        val scaleX = imageWidth.toFloat() / previewWidth
        val scaleY = imageHeight.toFloat() / previewHeight

        // Apply scaling
        val imageX = x * scaleX
        val imageY = y * scaleY

        // Clamp to image bounds
        val clampedX = imageX.coerceIn(0f, imageWidth.toFloat() - 1)
        val clampedY = imageY.coerceIn(0f, imageHeight.toFloat() - 1)

        Log.v(TAG, "previewToImage: ($x, $y) preview -> ($clampedX, $clampedY) image (scale: $scaleX, $scaleY)")

        return Point(clampedX.toDouble(), clampedY.toDouble())
    }

    /**
     * Convert ImageAnalysis coordinates to PreviewView coordinates
     *
     * Simplified version: Assumes no rotation and direct scaling
     *
     * @param x X coordinate in ImageAnalysis frame (0.0 to imageWidth)
     * @param y Y coordinate in ImageAnalysis frame (0.0 to imageHeight)
     * @param imageWidth Width of ImageAnalysis frame in pixels
     * @param imageHeight Height of ImageAnalysis frame in pixels
     * @param previewWidth Width of PreviewView in pixels
     * @param previewHeight Height of PreviewView in pixels
     * @param rotation Image rotation in degrees (0, 90, 180, 270) - not yet supported
     * @param isFrontCamera Whether the camera is front-facing (for mirroring) - not yet supported
     * @return Pair of (x, y) in PreviewView coordinates
     */
    fun imageToPreview(
        x: Float,
        y: Float,
        imageWidth: Int,
        imageHeight: Int,
        previewWidth: Int,
        previewHeight: Int,
        rotation: Int = 0,
        isFrontCamera: Boolean = false
    ): Pair<Float, Float> {
        // Phase 3: Simplified mapping (direct scaling)
        // Assumes PreviewView and ImageAnalysis have the same orientation

        // Calculate scale factors
        val scaleX = previewWidth.toFloat() / imageWidth
        val scaleY = previewHeight.toFloat() / imageHeight

        // Apply scaling
        val previewX = x * scaleX
        val previewY = y * scaleY

        // Clamp to preview bounds
        val clampedX = previewX.coerceIn(0f, previewWidth.toFloat() - 1)
        val clampedY = previewY.coerceIn(0f, previewHeight.toFloat() - 1)

        Log.v(TAG, "imageToPreview: ($x, $y) image -> ($clampedX, $clampedY) preview (scale: $scaleX, $scaleY)")

        return Pair(clampedX, clampedY)
    }

    /**
     * Convert OpenCV Point to Pair<Float, Float>
     */
    fun pointToPair(point: Point): Pair<Float, Float> {
        return Pair(point.x.toFloat(), point.y.toFloat())
    }

    /**
     * Convert Pair<Float, Float> to OpenCV Point
     */
    fun pairToPoint(pair: Pair<Float, Float>): Point {
        return Point(pair.first.toDouble(), pair.second.toDouble())
    }
}
