package com.akirachang.ai.photographer.domain.model.focus

/**
 * Output of FocusStateMachine.reduce().
 * v2.12.0-15 / §17.11.9
 *
 * Rule: when visualTargetState is None, the UI MUST be fully hidden.
 * No fallback 36dp box is ever permitted.
 */
data class FocusUiState(
    val visualTargetState: VisualTargetState,
    val lensFocusState: LensFocusState,
    val exposureLockState: ExposureLockState,
    val overlayModel: OverlayModel?,  // null when visualTargetState is None
) {
    companion object {
        val EMPTY = FocusUiState(
            visualTargetState = VisualTargetState.None,
            lensFocusState = LensFocusState.NotRequested,
            exposureLockState = ExposureLockState.Unlocked,
            overlayModel = null,
        )
    }
}