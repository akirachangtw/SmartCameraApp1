package com.akirachang.ai.photographer.domain.model.focus

import android.graphics.Matrix

/**
 * Describes a single camera frame input with necessary geometry metadata.
 * v2.12.0-15 / §17.11.2
 */
data class FramePacket(
    val frameId: Long,
    val timestampMs: Long,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotationDegrees: Int,
    val lensFacing: Int,
    val imageProxyRef: Any?,
    val sensorToBufferMatrix: Matrix?,
)
