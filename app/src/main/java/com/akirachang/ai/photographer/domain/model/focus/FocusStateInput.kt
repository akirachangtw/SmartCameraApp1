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
    // v2.12.0-15 Phase 2 T3-F2: Front camera guard for ExposureLockState
    val isFrontCamera: Boolean = false,
)

enum class SystemEvent {
    NORMAL_UPDATE,
    LENS_SWITCHED,
    LIFECYCLE_STOPPED,
}
