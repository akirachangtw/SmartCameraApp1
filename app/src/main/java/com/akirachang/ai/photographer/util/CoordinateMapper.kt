package com.akirachang.ai.photographer.util

import android.util.Log

/**
 * CoordinateMapper — CENTER_CROP aware PreviewView ↔ ImageAnalysis coordinate conversion.
 *
 * NOTE: This utility is now legacy — FIT_CENTER path uses CoordinateTransformer.
 * Kept for backwards-compat with emulator path (StaticImageSource).
 */
object CoordinateMapper {
    private const val TAG = "CoordinateMapper"

    fun previewToImage(
        x: Float,
        y: Float,
        previewWidth: Int,
        previewHeight: Int,
        imageWidth: Int,
        imageHeight: Int,
        rotation: Int = 0,
        isFrontCamera: Boolean = false
    ): Pair<Float, Float> {
        val uniformScale = maxOf(
            previewWidth.toFloat() / imageWidth,
            previewHeight.toFloat() / imageHeight
        )
        val cropOffsetX = (imageWidth - previewWidth / uniformScale) / 2f
        val cropOffsetY = (imageHeight - previewHeight / uniformScale) / 2f

        val imageX = (x / uniformScale + cropOffsetX).coerceIn(0f, imageWidth.toFloat() - 1)
        val imageY = (y / uniformScale + cropOffsetY).coerceIn(0f, imageHeight.toFloat() - 1)

        Log.v(TAG, "previewToImage: ($x, $y) -> ($imageX, $imageY)")
        return Pair(imageX, imageY)
    }

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

        Log.v(TAG, "imageToPreview: ($x, $y) -> ($previewX, $previewY)")
        return Pair(previewX, previewY)
    }
}
