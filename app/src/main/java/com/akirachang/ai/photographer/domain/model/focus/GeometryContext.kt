package com.akirachang.ai.photographer.domain.model.focus

import android.graphics.Matrix

/**
 * All geometry information required for image→view coordinate mapping.
 * v2.12.0-15 / §17.11.4
 *
 * Rule: If sensorToViewMatrix is null, fallback must be explicit — never silently use wrong ratio.
 */
data class GeometryContext(
    val imageWidth: Int,
    val imageHeight: Int,
    val previewWidth: Int,
    val previewHeight: Int,
    val sensorToBufferMatrix: Matrix?,
    val sensorToViewMatrix: Matrix?,
    val isFrontCamera: Boolean,
    val isMirroredPreview: Boolean,
)
