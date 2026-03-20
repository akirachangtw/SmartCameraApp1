package com.akirachang.ai.photographer.domain.focus.control

import com.akirachang.ai.photographer.domain.model.focus.EyeAfDecision
import com.akirachang.ai.photographer.domain.model.focus.FocusControllerResult
import com.akirachang.ai.photographer.domain.model.focus.FocusTarget
import com.akirachang.ai.photographer.domain.model.focus.GeometryContext

/**
 * Domain interface for hardware focus execution.
 * v2.12.0-15 / §17.11.8
 *
 * Rules:
 * - This is the ONLY legal place to call cameraControl.startFocusAndMetering().
 * - Must NOT make product-level decisions (those belong to EyeAfPolicyResolver).
 * - Must translate MeteringAnchor coordinates to CameraX MeteringAction.
 * - Must be called from a coroutine context (suspend function).
 */
interface FocusController {
    /**
     * Apply the given [decision] to hardware.
     * @param decision Output of EyeAfPolicyResolver.
     * @param primaryTarget The primary tracked target (for coordinate extraction).
     * @param geometryContext Current geometry mapping.
     * @return [FocusControllerResult] describing what was sent to hardware.
     */
    suspend fun apply(
        decision: EyeAfDecision,
        primaryTarget: FocusTarget?,
        geometryContext: GeometryContext,
    ): FocusControllerResult
}
