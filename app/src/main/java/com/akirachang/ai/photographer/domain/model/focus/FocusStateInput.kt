package com.akirachang.ai.photographer.domain.model.focus

/**
 * Input to FocusStateMachine.reduce().
 * v2.12.0-15 / §17.11.9
 */
data class FocusStateInput(
    val trackedTargetState: TrackedTargetState,
    val eyeAfDecision: EyeAfDecision,
    val focusControllerResult: FocusControllerResult?,
    val userIntent: UserIntentState,
    val systemEvent: SystemEvent,
    val nowMs: Long,
)

enum class SystemEvent {
    NORMAL_UPDATE,
    LENS_SWITCHED,
    LIFECYCLE_STOPPED,
}
