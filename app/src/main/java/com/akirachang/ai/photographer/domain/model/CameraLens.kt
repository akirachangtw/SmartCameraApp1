package com.akirachang.ai.photographer.domain.model

/**
 * Camera Lens Selection
 *
 * Platform-agnostic representation of camera lens (front/back).
 * Replaces CameraX CameraSelector to avoid leaking implementation details.
 *
 * v2.5.1: Camera Input Abstraction - Priority 2
 */
enum class CameraLens {
    /**
     * Back-facing camera (primary camera)
     */
    BACK,

    /**
     * Front-facing camera (selfie camera)
     */
    FRONT
}
