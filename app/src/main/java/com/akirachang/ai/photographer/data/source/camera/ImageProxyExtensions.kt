package com.akirachang.ai.photographer.data.source.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import org.opencv.core.CvType
import org.opencv.core.Mat

/**
 * ImageProxy Extensions for OpenCV
 *
 * v2.7.0 Phase 2 Sprint 2
 *
 * Provides conversion functions from CameraX ImageProxy to OpenCV Mat format.
 * Optimized for performance with minimal memory allocation.
 */

/**
 * Convert ImageProxy (YUV_420_888) to OpenCV Mat (grayscale)
 *
 * Extracts only the Y plane (luminance channel) for grayscale processing.
 * This is efficient for edge detection, line detection, and other operations
 * that don't require color information.
 *
 * Performance characteristics:
 * - Single buffer copy (Y plane only)
 * - No color space conversion overhead
 * - Suitable for real-time analysis at 30fps
 *
 * @return Mat in CV_8UC1 format (single-channel grayscale)
 * @throws IllegalStateException if image format is not YUV_420_888
 */
@SuppressLint("UnsafeOptInUsageError")
fun ImageProxy.toGrayMat(): Mat {
    // Verify format
    require(format == android.graphics.ImageFormat.YUV_420_888) {
        "Expected YUV_420_888 format, got $format"
    }

    // Get Y plane (luminance)
    val yPlane = planes[0]
    val yBuffer = yPlane.buffer

    // Create Mat with image dimensions
    val mat = Mat(height, width, CvType.CV_8UC1)

    // Copy Y plane data to Mat
    // Note: We need to handle row stride if it's not equal to width
    if (yPlane.rowStride == width) {
        // Direct copy (optimal case)
        val yData = ByteArray(yBuffer.remaining())
        yBuffer.get(yData)
        mat.put(0, 0, yData)
    } else {
        // Row-by-row copy (handle padding)
        val rowData = ByteArray(width)
        for (row in 0 until height) {
            yBuffer.position(row * yPlane.rowStride)
            yBuffer.get(rowData)
            mat.put(row, 0, rowData)
        }
    }

    return mat
}

/**
 * Convert ImageProxy (YUV_420_888) to OpenCV Mat (BGR color)
 *
 * Full YUV → BGR conversion for color-based analysis.
 * More expensive than toGrayMat() but provides color information.
 *
 * Note: Currently not implemented in Sprint 2 (not needed for guideline overlay).
 * Will be implemented in future phases if color analysis is required.
 *
 * @return Mat in CV_8UC3 format (BGR color)
 * @throws NotImplementedError Always (not yet implemented)
 */
@SuppressLint("UnsafeOptInUsageError")
fun ImageProxy.toBgrMat(): Mat {
    throw NotImplementedError(
        "BGR conversion not implemented in v2.7.0 Phase 2. " +
        "Use toGrayMat() for grayscale analysis."
    )
}
