package com.akirachang.ai.photographer.domain.model.focus

/**
 * Input/output models for EyeAfPolicyResolver.
 * v2.12.0-15 / §17.11.7
 *
 * Policy layer owns all front/back camera differences, low-light degradation,
 * and occlusion fallback — nothing in Controller or StateMachine should re-derive these.
 */
data class EyeAfPolicyInput(
    val primaryTarget: FocusTarget?,
    val lensCapability: LensCapability,
    val userIntent: UserIntentState,
    val nowMs: Long,
    val lastFocusTriggerMs: Long?,
)

data class EyeAfDecision(
    val anchor: MeteringAnchor,
    val visualState: VisualTargetState,
    val shouldTriggerFocus: Boolean,
    val shouldTriggerMetering: Boolean,
    val allowOpticalState: Boolean,
)

enum class MeteringAnchor {
    BOTH_EYES_CENTER,
    SINGLE_EYE,
    UPPER_FACE,
    FACE_CENTER,
    TRACKED_REGION,
    TAP_POINT,
    NONE,
}
