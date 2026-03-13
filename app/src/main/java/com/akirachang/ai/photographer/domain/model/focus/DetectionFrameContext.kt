package com.akirachang.ai.photographer.domain.model.focus

/**
 * Combines a FramePacket with preview-layer geometry needed for coordinate mapping.
 * v2.12.0-15 / §17.11.2
 */
data class DetectionFrameContext(
    val frame: FramePacket,
    val previewWidth: Int,
    val previewHeight: Int,
    val isMirroredPreview: Boolean,
)
