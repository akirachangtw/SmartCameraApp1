package com.akirachang.ai.photographer.presentation.camera.focus

import android.util.Log
import com.akirachang.ai.photographer.domain.model.focus.*

/**
 * Default implementation of FocusStateMachine.
 * v2.12.0-15 / §17.11.9
 *
 * Three orthogonal state axes (§6.2):
 *   1. VisualTargetState  — driven by TrackedTargetState + EyeAfDecision.visualState
 *   2. LensFocusState     — driven by FocusControllerResult.opticalResult
 *   3. ExposureLockState  — driven by UserIntentState.isAeAfLockRequested
 *
 * SystemEvent handling:
 *   LENS_SWITCHED / LIFECYCLE_STOPPED → always return FocusUiState.EMPTY
 */
class DefaultFocusStateMachine : FocusStateMachine {

    companion object {
        private const val TAG = "DefaultFocusStateMachine"
    }

    override fun reduce(input: FocusStateInput): FocusUiState {
        // ── System interrupt events — always reset to EMPTY ──────────────────
        if (input.systemEvent == SystemEvent.LENS_SWITCHED ||
            input.systemEvent == SystemEvent.LIFECYCLE_STOPPED) {
            Log.i(TAG, "[v2.12.0-15] SystemEvent=${input.systemEvent} → EMPTY reset")
            return FocusUiState.EMPTY
        }

        // ── Axis 1: VisualTargetState ─────────────────────────────────────────
        val visualState = reduceVisualState(input)

        // ── Axis 2: LensFocusState ────────────────────────────────────────────
        val lensState = reduceLensState(input)

        // ── Axis 3: ExposureLockState ─────────────────────────────────────────
        val exposureState = reduceExposureState(input)

        // ── OverlayModel: null when visual is None ────────────────────────────
        val overlay = if (visualState == VisualTargetState.None) {
            null
        } else {
            buildOverlayModel(input, visualState, exposureState)
        }

        return FocusUiState(
            visualTargetState = visualState,
            lensFocusState = lensState,
            exposureLockState = exposureState,
            overlayModel = overlay,
        )
    }

    // ── Axis 1: VisualTargetState reducer ─────────────────────────────────────
    private fun reduceVisualState(input: FocusStateInput): VisualTargetState {
        val primary = input.trackedTargetState.primaryTarget
        val decisionVisual = input.eyeAfDecision.visualState

        // No primary target → None (消滅殭屍框)
        if (primary == null) {
            val lost = input.trackedTargetState.recentlyLostTarget
            return if (lost != null) VisualTargetState.TrackingFade else VisualTargetState.None
        }

        // Delegate to EyeAfDecision visual state (policy layer owns this)
        return decisionVisual
    }

    // ── Axis 2: LensFocusState reducer ───────────────────────────────────────
    private fun reduceLensState(input: FocusStateInput): LensFocusState {
        val optical = input.focusControllerResult?.opticalResult
            ?: return LensFocusState.NotRequested

        return when (optical) {
            OpticalFocusState.FOCUSED            -> LensFocusState.Focused
            OpticalFocusState.NOT_FOCUSED        -> LensFocusState.NotFocused
            OpticalFocusState.PASSIVE_CONTINUOUS -> LensFocusState.PassiveContinuous
            OpticalFocusState.UNSUPPORTED        -> LensFocusState.UnsupportedFixedFocus
            OpticalFocusState.UNKNOWN            -> LensFocusState.NotRequested
        }
    }

    // ── Axis 3: ExposureLockState reducer ─────────────────────────────────────
    private fun reduceExposureState(input: FocusStateInput): ExposureLockState {
        // v2.12.0-15 Phase 2 T3-F2: Front camera NEVER allows Yellow lock (SSOT §3.2)
        // Only Cyan Eye-AF is permitted on front camera.
        if (input.isFrontCamera) {
            if (input.userIntent.isAeAfLockRequested) {
                Log.w(TAG, "[v2.12.0-15 T3-F2] Front camera AE/AF lock request rejected — forced Unlocked")
            }
            return ExposureLockState.Unlocked
        }
        return if (input.userIntent.isAeAfLockRequested) {
            ExposureLockState.AEAFLocked
        } else {
            ExposureLockState.Unlocked
        }
    }

    // ── OverlayModel builder ──────────────────────────────────────────────────
    private fun buildOverlayModel(
        input: FocusStateInput,
        visualState: VisualTargetState,
        exposureState: ExposureLockState,
    ): OverlayModel {
        val primary = input.trackedTargetState.primaryTarget
        val primaryBox = primary?.rectView

        val color = if (exposureState == ExposureLockState.AEAFLocked) {
            OverlayColor.YELLOW
        } else {
            OverlayColor.CYAN
        }

        val alpha = when (visualState) {
            VisualTargetState.VisualLocked   -> 1.0f
            VisualTargetState.TrackingStable -> 1.0f
            VisualTargetState.Candidate      -> 0.6f
            VisualTargetState.TrackingFade   -> 0.4f
            VisualTargetState.Lost           -> 0.2f
            VisualTargetState.None           -> 0.0f
        }

        val animation = when (visualState) {
            VisualTargetState.VisualLocked   -> OverlayAnimation.NONE
            VisualTargetState.TrackingFade   -> OverlayAnimation.FADE
            VisualTargetState.Candidate      -> OverlayAnimation.PULSE
            else                             -> OverlayAnimation.NONE
        }

        val showLockIcon = exposureState == ExposureLockState.AEAFLocked

        val secondaryBoxes = input.trackedTargetState.allTargets
            .filter { it != primary }
            .map { it.rectView }

        return OverlayModel(
            primaryStableId = primary?.stableId,
            primaryBox = primaryBox,
            secondaryBoxes = secondaryBoxes,
            primaryColor = color,
            alpha = alpha,
            showLockIcon = showLockIcon,
            animation = animation,
        )
    }
}
