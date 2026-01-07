package com.akirachang.ai.photographer.util

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * YUV to RGB Converter with Integer Math Optimization
 *
 * v2.8.1 Phase B-1: Replaces floating-point YUV conversion with integer arithmetic
 * and lookup tables for 2-3x performance improvement on mid-range devices.
 *
 * Performance:
 * - Before (float): ~44ms per frame (640x480)
 * - After (integer + LUT): ~15-20ms per frame (target)
 *
 * Color conversion formula (ITU-R BT.601):
 * R = Y + 1.402 * (V - 128)   ≈ Y + ((359 * (V - 128)) >> 8)
 * G = Y - 0.344 * (U - 128) - 0.714 * (V - 128)
 * B = Y + 1.772 * (U - 128)   ≈ Y + ((454 * (U - 128)) >> 8)
 */
class YuvConverter {

    // Lookup tables for YUV to RGB conversion (precomputed at initialization)
    // These avoid repeated multiplication and division operations
    private val rLut = IntArray(256) { v ->
        // R component: 1.402 * (V - 128) ≈ (359 * (V - 128)) >> 8
        (359 * (v - 128)) shr 8
    }

    private val gLutU = IntArray(256) { u ->
        // G component (U part): -0.344 * (U - 128) ≈ -(88 * (U - 128)) >> 8
        -(88 * (u - 128)) shr 8
    }

    private val gLutV = IntArray(256) { v ->
        // G component (V part): -0.714 * (V - 128) ≈ -(182 * (V - 128)) >> 8
        -(182 * (v - 128)) shr 8
    }

    private val bLut = IntArray(256) { u ->
        // B component: 1.772 * (U - 128) ≈ (454 * (U - 128)) >> 8
        (454 * (u - 128)) shr 8
    }

    // Reusable pixel buffer to avoid allocation
    private var pixels: IntArray? = null
    private var currentSize = 0

    /**
     * Convert ImageProxy YUV_420_888 to RGB pixel array
     *
     * @param image ImageProxy from CameraX (YUV_420_888 format)
     * @return IntArray of ARGB pixels (format: 0xAARRGGBB)
     */
    fun convertToPixels(image: ImageProxy): IntArray {
        val width = image.width
        val height = image.height
        val requiredSize = width * height

        // Reuse pixel buffer if size matches, otherwise allocate new
        if (pixels == null || currentSize != requiredSize) {
            pixels = IntArray(requiredSize)
            currentSize = requiredSize
        }

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        // Rewind buffers to start
        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        var pixelIndex = 0

        // Convert YUV to RGB using integer math and lookup tables
        for (y in 0 until height) {
            val yRowOffset = y * yRowStride
            val uvRowOffset = (y / 2) * uvRowStride

            for (x in 0 until width) {
                // Get Y component (luminance)
                val yValue = yBuffer.get(yRowOffset + x).toInt() and 0xFF

                // Get U and V components (chrominance) - subsampled 2x2
                val uvPixelIndex = uvRowOffset + (x / 2) * uvPixelStride
                val uValue = uBuffer.get(uvPixelIndex).toInt() and 0xFF
                val vValue = vBuffer.get(uvPixelIndex).toInt() and 0xFF

                // Convert to RGB using precomputed lookup tables
                // All operations are integer math - no floating point!
                val r = (yValue + rLut[vValue]).coerceIn(0, 255)
                val g = (yValue + gLutU[uValue] + gLutV[vValue]).coerceIn(0, 255)
                val b = (yValue + bLut[uValue]).coerceIn(0, 255)

                // Pack into ARGB format (0xAARRGGBB)
                pixels!![pixelIndex++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        return pixels!!
    }

    /**
     * Convert ImageProxy YUV to Bitmap directly
     *
     * @param image ImageProxy from CameraX
     * @param reuseBitmap Optional existing bitmap to reuse (must match dimensions)
     * @return Bitmap with RGB data
     */
    fun convertToBitmap(image: ImageProxy, reuseBitmap: Bitmap? = null): Bitmap {
        val width = image.width
        val height = image.height

        val pixelData = convertToPixels(image)

        // Reuse bitmap if provided and dimensions match
        val bitmap = if (reuseBitmap != null &&
                         reuseBitmap.width == width &&
                         reuseBitmap.height == height &&
                         !reuseBitmap.isRecycled) {
            reuseBitmap
        } else {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }

        bitmap.setPixels(pixelData, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * Convert ImageProxy YUV to BitmapPool.BitmapBuffer
     *
     * Optimized version that works with BitmapPool for zero-allocation conversion
     *
     * @param image ImageProxy from CameraX
     * @param buffer BitmapPool.BitmapBuffer to write into
     */
    fun convertToBitmapBuffer(image: ImageProxy, buffer: BitmapPool.BitmapBuffer) {
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        val pixels = buffer.pixels
        var pixelIndex = 0

        // Convert YUV to RGB - same logic as convertToPixels but writes directly to buffer
        for (y in 0 until height) {
            val yRowOffset = y * yRowStride
            val uvRowOffset = (y / 2) * uvRowStride

            for (x in 0 until width) {
                val yValue = yBuffer.get(yRowOffset + x).toInt() and 0xFF

                val uvPixelIndex = uvRowOffset + (x / 2) * uvPixelStride
                val uValue = uBuffer.get(uvPixelIndex).toInt() and 0xFF
                val vValue = vBuffer.get(uvPixelIndex).toInt() and 0xFF

                // Integer math + LUT lookups
                val r = (yValue + rLut[vValue]).coerceIn(0, 255)
                val g = (yValue + gLutU[uValue] + gLutV[vValue]).coerceIn(0, 255)
                val b = (yValue + bLut[uValue]).coerceIn(0, 255)

                pixels[pixelIndex++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Write pixels to bitmap
        buffer.bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}
