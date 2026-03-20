package com.akirachang.ai.photographer.domain.focus.geometry

import android.graphics.PointF
import android.graphics.RectF
import com.akirachang.ai.photographer.domain.model.focus.GeometryContext

/**
 * Domain interface for image-space → view-space coordinate mapping.
 * v2.12.0-15 / §17.11.4
 *
 * Rules:
 * - All rectView / pointView coordinates must be derived through this interface.
 * - UI, ViewModel, and StateMachine are forbidden from hand-computing scale ratios.
 * - If sensorToViewMatrix is absent, implementations must provide an explicit fallback
 *   (never silently adopt an incorrect geometry).
 */
interface GeometryMapper {

    /**
     * Map a rectangle from image space to view space.
     *
     * Implementation must:
     * 1. Transform all 4 corners of [rect].
     * 2. Take the outer bounding box of the transformed corners (§8.2).
     * 3. Apply expansion coefficients α/β (§8.3).
     */
    fun mapRectImageToView(rect: RectF, context: GeometryContext): RectF

    /**
     * Map a single point from image space to view space.
     */
    fun mapPointImageToView(point: PointF, context: GeometryContext): PointF
}
