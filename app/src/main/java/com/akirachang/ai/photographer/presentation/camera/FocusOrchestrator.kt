package com.akirachang.ai.photographer.presentation.camera

import com.akirachang.ai.photographer.data.focus.policy.DefaultEyeAfPolicyResolver
import com.akirachang.ai.photographer.domain.focus.policy.EyeAfPolicyResolver
import com.akirachang.ai.photographer.domain.model.focus.EyeAfPolicyInput
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState

/**
 * Thin Adapter: translates FocusOrchestrator calls into EyeAfPolicyResolver decisions.
 * v2.12.0-15 / §17.11.7
 *
 * All decision logic lives in DefaultEyeAfPolicyResolver.
 * FocusOrchestrator is the only entry point for focus policy in the presentation layer.
 *
 * FocusDecision values:
 *   Allow         — proceed with HAL focus (normal path)
 *   SyntheticLock — fixed-focus bypass (no HAL call, apply visual lock directly)
 *   Reject        — policy forbids this focus attempt
 *
 * Note: THROTTLE from the policy layer maps to Allow here because cooldown enforcement
 * is already handled by eyeAfThrottle in CameraViewModel. Mapping to Allow lets the
 * existing throttle mechanism continue to operate without change.
 */
object FocusOrchestrator {

    sealed class FocusDecision {
        data object Allow : FocusDecision()
        data object SyntheticLock : FocusDecision()
        data object Reject : FocusDecision()
    }

    private val resolver: EyeAfPolicyResolver = DefaultEyeAfPolicyResolver()

    /**
     * Decide the focus action for a given [input].
     *
     * @param input Assembled from current CameraState / UserIntentState / LensCapability.
     * @return [FocusDecision] for the caller to act upon.
     */
    fun decide(input: EyeAfPolicyInput): FocusDecision {
        val decision = resolver.resolve(input)
        return when {
            // Reject: no trigger, no visual
            decision.visualState == VisualTargetState.None && !decision.shouldTriggerFocus
                && !decision.shouldTriggerMetering -> FocusDecision.Reject
            // SyntheticLock: bypass HAL (fixed-focus path)
            !decision.shouldTriggerFocus && decision.shouldTriggerMetering -> FocusDecision.SyntheticLock
            // Allow: normal HAL path
            else -> FocusDecision.Allow
        }
    }
}
