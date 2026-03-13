package com.akirachang.ai.photographer.domain.model.focus

/**
 * Three orthogonal state dimensions for the focus system.
 * v2.12.0-15 / §6.2
 *
 * Rules:
 * - AE/AF Lock must NOT be implicitly triggered by Eye-AF success.
 * - OpticalFocusState must NOT override VisualLocked or AEAFLocked semantics.
 */

sealed interface VisualTargetState {
    data object None : VisualTargetState
    data object Candidate : VisualTargetState
    data object TrackingStable : VisualTargetState
    data object VisualLocked : VisualTargetState
    data object TrackingFade : VisualTargetState
    data object Lost : VisualTargetState
}

sealed interface LensFocusState {
    data object NotRequested : LensFocusState
    data object PassiveContinuous : LensFocusState
    data object ActiveScan : LensFocusState
    data object Focused : LensFocusState
    data object NotFocused : LensFocusState
    data object UnsupportedFixedFocus : LensFocusState
}

sealed interface ExposureLockState {
    data object Unlocked : ExposureLockState
    data object AEAFLocked : ExposureLockState
}
