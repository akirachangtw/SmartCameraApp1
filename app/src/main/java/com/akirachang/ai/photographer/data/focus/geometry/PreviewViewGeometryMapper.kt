package com.akirachang.ai.photographer.data.focus.geometry

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import com.akirachang.ai.photographer.domain.focus.geometry.GeometryMapper
import com.akirachang.ai.photographer.domain.model.focus.GeometryContext

/**
 * GeometryMapper implementation for PreviewView (FIT_CENTER) geometry.
 * v2.12.0-15 / §17.11.4, §8.2, §8.3, §8.4
 *
 * Mapping priority:
 *   1. sensorToViewMatrix (provided by CameraX CoordinateTransformer) — preferred
 *   2. FIT_CENTER scale + letterbox offset fallback
 *
 * Front-camera mirroring:
 *   Applied only when [GeometryContext.isFrontCamera] && [GeometryContext.isMirroredPreview]
 *   and the underlying matrix has not already handled it.
 *
 * Bounding-box expansion (§8.3):
 *   w_draw = (1 + EXPAND_ALPHA) * w
 *   h_draw = (1 + EXPAND_BETA)  * h
 */
class PreviewViewGeometryMapper : GeometryMapper {

    companion object {
        private const val TAG = "PreviewViewGeometryMapper"

        /** §8.3 horizontal expansion coefficient */
        private const val EXPAND_ALPHA = 0.08f

        /** §8.3 vertical expansion coefficient */
        private const val EXPAND_BETA = 0.08f
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Map [rect] (image space) → view space.
     * Steps: transform 4 corners → outer bounding box → expansion.
     */
    override fun mapRectImageToView(rect: RectF, context: GeometryContext): RectF {
        val corners = floatArrayOf(
            rect.left,  rect.top,
            rect.right, rect.top,
            rect.right, rect.bottom,
            rect.left,  rect.bottom,
        )

        transformPoints(corners, context)

        // §8.2: outer bounding box
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        for (i in corners.indices step 2) {
            if (corners[i]     < minX) minX = corners[i]
            if (corners[i]     > maxX) maxX = corners[i]
            if (corners[i + 1] < minY) minY = corners[i + 1]
            if (corners[i + 1] > maxY) maxY = corners[i + 1]
        }

        // §8.3: expansion
        val w = maxX - minX
        val h = maxY - minY
        val expandX = w * EXPAND_ALPHA / 2f
        val expandY = h * EXPAND_BETA  / 2f

        // v2.12.0-15 Phase 2: AR-preserving clamp — prevent box from exceeding preview bounds
        val clampedLeft   = (minX - expandX).coerceAtLeast(0f)
        val clampedTop    = (minY - expandY).coerceAtLeast(0f)
        val clampedRight  = (maxX + expandX).coerceAtMost(context.previewWidth.toFloat())
        val clampedBottom = (maxY + expandY).coerceAtMost(context.previewHeight.toFloat())
        return RectF(clampedLeft, clampedTop, clampedRight, clampedBottom)
    }

    /**
     * Map [point] (image space) → view space.
     */
    override fun mapPointImageToView(point: PointF, context: GeometryContext): PointF {
        val pts = floatArrayOf(point.x, point.y)
        transformPoints(pts, context)
        return PointF(pts[0], pts[1])
    }

    // -------------------------------------------------------------------------
    // Matrix factory
    // -------------------------------------------------------------------------

    /**
     * Build the affine sensor→view matrix directly from geometry inputs.
     * v2.12.0-17 / SV-FIX-3R-FINAL
     *
     * Eliminates dependency on presentation-layer CoordinateTransformer for matrix construction.
     * Math is identical to CoordinateTransformer.toMatrix() but lives in the data/domain layer.
     *
     * Row-major layout (android.graphics.Matrix.setValues):
     *   | MSCALE_X  MSKEW_X   MTRANS_X |
     *   | MSKEW_Y   MSCALE_Y  MTRANS_Y |
     *   | MPERSP_0  MPERSP_1  MPERSP_2 |
     *
     * Front camera: scaleX = -cW/imgW, translateX = contentBounds.right (mirror)
     * Rear camera:  scaleX =  cW/imgW, translateX = contentBounds.left
     */
    fun createSensorToViewMatrix(
        contentBounds: RectF,
        imageWidth: Int,
        imageHeight: Int,
        isFrontCamera: Boolean,
    ): Matrix {
        val scaleX = contentBounds.width() / imageWidth
        val scaleY = contentBounds.height() / imageHeight
        return Matrix().apply {
            if (isFrontCamera) {
                setValues(floatArrayOf(
                    -scaleX, 0f, contentBounds.right,
                    0f, scaleY, contentBounds.top,
                    0f, 0f, 1f,
                ))
            } else {
                setValues(floatArrayOf(
                    scaleX, 0f, contentBounds.left,
                    0f, scaleY, contentBounds.top,
                    0f, 0f, 1f,
                ))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * In-place transform of a flat [pts] array (x0,y0, x1,y1, …) from image space to view space.
     * Uses sensorToViewMatrix when available, otherwise FIT_CENTER fallback.
     * Applies front-camera mirror if required.
     */
    private fun transformPoints(pts: FloatArray, context: GeometryContext) {
        val matrix = context.sensorToViewMatrix
        if (matrix != null) {
            matrix.mapPoints(pts)
        } else {
            applyFitCenterFallback(pts, context)
        }

        // §8.4: front-camera mirror — matrix-aware strategy
        // v2.12.0-15 SV-BatchA Fix B: Only apply manual mirror when matrix == null (FIT_CENTER fallback).
        // When matrix != null, sensorToViewMatrix already includes mirror — applying again causes double-mirror.
        if (context.isFrontCamera && context.isMirroredPreview && matrix == null) {
            val previewW = context.previewWidth.toFloat()
            for (i in pts.indices step 2) {
                pts[i] = previewW - pts[i]
            }
            Log.v(TAG, "Front-camera mirror applied (FIT_CENTER fallback path, matrix=null)")
        }
    }

    /**
     * FIT_CENTER scale: uniformly scale the image to fit within the preview,
     * then offset for letterbox centering.
     *
     *   scale   = min(previewW / imageW, previewH / imageH)
     *   offsetX = (previewW - imageW * scale) / 2
     *   offsetY = (previewH - imageH * scale) / 2
     *   viewX   = imageX * scale + offsetX
     *   viewY   = imageY * scale + offsetY
     */
    private fun applyFitCenterFallback(pts: FloatArray, context: GeometryContext) {
        if (context.imageWidth <= 0 || context.imageHeight <= 0) {
            Log.e(TAG, "applyFitCenterFallback: invalid image dimensions — skipping transform")
            return
        }
        val scale = minOf(
            context.previewWidth.toFloat()  / context.imageWidth,
            context.previewHeight.toFloat() / context.imageHeight,
        )
        val offsetX = (context.previewWidth  - context.imageWidth  * scale) / 2f
        val offsetY = (context.previewHeight - context.imageHeight * scale) / 2f

        for (i in pts.indices step 2) {
            pts[i]     = pts[i]     * scale + offsetX
            pts[i + 1] = pts[i + 1] * scale + offsetY
        }
    }
}
