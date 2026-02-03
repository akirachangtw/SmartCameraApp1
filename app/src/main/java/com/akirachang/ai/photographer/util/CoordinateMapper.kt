package com.akirachang.ai.photographer.util

import android.util.Log
import org.opencv.core.Point

/**
 * Coordinate Mapping Utility
 *
 * v2.x.x Phase 3: PreviewView ↔ ImageAnalysis coordinate conversion
 * v2.x.x Phase 7: CENTER_CROP-aware mapping (replaces direct scaling)
 *
 * Handles coordinate transformations between different coordinate systems:
 * - PreviewView coordinates: User tap coordinates on the screen
 * - ImageAnalysis coordinates: Coordinates in the camera frame (bitmap)
 *
 * CameraX PreviewView defaults to CENTER_CROP: the image is scaled uniformly
 * so it fills the view, then the excess is cropped at center.  Simple X/Y
 * scaling is only correct when the aspect ratios match; otherwise the crop
 * offsets must be accounted for.
 */
object CoordinateMapper {
    private const val TAG = "CoordinateMapper"

    /**
     * Convert PreviewView coordinates to ImageAnalysis (bitmap) coordinates.
     *
     * Uses CENTER_CROP geometry:
     *   uniformScale = max(previewW/imageW, previewH/imageH)
     *   cropOffsetX  = (imageW  − previewW / uniformScale) / 2
     *   cropOffsetY  = (imageH  − previewH / uniformScale) / 2
     *   imageX       = x / uniformScale + cropOffsetX
     *   imageY       = y / uniformScale + cropOffsetY
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
        val uniformScale = maxOf(
            previewWidth.toFloat() / imageWidth,
            previewHeight.toFloat() / imageHeight
        )
        val cropOffsetX = (imageWidth - previewWidth / uniformScale) / 2f
        val cropOffsetY = (imageHeight - previewHeight / uniformScale) / 2f

        val imageX = (x / uniformScale + cropOffsetX).coerceIn(0f, imageWidth.toFloat() - 1)
        val imageY = (y / uniformScale + cropOffsetY).coerceIn(0f, imageHeight.toFloat() - 1)

        Log.v(TAG, "previewToImage: ($x, $y) preview -> ($imageX, $imageY) image " +
              "(uniformScale=$uniformScale, cropOff=($cropOffsetX, $cropOffsetY))")

        return Point(imageX.toDouble(), imageY.toDouble())
    }

    /**
     * Convert ImageAnalysis (bitmap) coordinates to PreviewView coordinates.
     *
     * Inverse of previewToImage (CENTER_CROP geometry).
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
        val uniformScale = maxOf(
            previewWidth.toFloat() / imageWidth,
            previewHeight.toFloat() / imageHeight
        )
        val cropOffsetX = (imageWidth - previewWidth / uniformScale) / 2f
        val cropOffsetY = (imageHeight - previewHeight / uniformScale) / 2f

        val previewX = ((x - cropOffsetX) * uniformScale).coerceIn(0f, previewWidth.toFloat() - 1)
        val previewY = ((y - cropOffsetY) * uniformScale).coerceIn(0f, previewHeight.toFloat() - 1)

        Log.v(TAG, "imageToPreview: ($x, $y) image -> ($previewX, $previewY) preview " +
              "(uniformScale=$uniformScale, cropOff=($cropOffsetX, $cropOffsetY))")

        return Pair(previewX, previewY)
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
