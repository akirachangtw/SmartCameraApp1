package com.akirachang.ai.photographer.data.focus.policy

import android.util.Log
import com.akirachang.ai.photographer.domain.focus.policy.EyeAfPolicyResolver
import com.akirachang.ai.photographer.domain.model.focus.EyeAfDecision
import com.akirachang.ai.photographer.domain.model.focus.EyeAfPolicyInput
import com.akirachang.ai.photographer.domain.model.focus.MeteringAnchor
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState

/**
 * Default Eye-AF policy resolver.
 * v2.12.0-15 / §17.10 決策表
 *
 * Decision table:
 * ┌─────────────────────────────────────────┬───────────────────────────────┐
 * │ Condition                               │ Action                        │
 * ├─────────────────────────────────────────┼───────────────────────────────┤
 * │ isFrontCamera && manual tap requested   │ REJECT                        │
 * │ isFixedFocus                            │ SYNTHETIC_LOCK (bypass HAL)   │
 * │ within post-lock cooldown window        │ THROTTLE                      │
 * │ otherwise                               │ ALLOW (normal HAL path)       │
 * └─────────────────────────────────────────┴───────────────────────────────┘
 *
 * Field mapping from EyeAfPolicyInput (Step 1 SSOT):
 *   isFrontCamera  ← input.lensCapability.isFrontCamera
 *   isManualTap    ← input.userIntent.isTapToFocusActive
 *   isFixedFocus   ← input.lensCapability.isFixedFocus
 *   isInCooldown   ← (nowMs - lastFocusTriggerMs) < COOLDOWN_MS
 */
class DefaultEyeAfPolicyResolver : EyeAfPolicyResolver {

    companion object {
        private const val TAG = "DefaultEyeAfPolicyResolver"

        /** Post-lock cooldown window in milliseconds. */
        private const val COOLDOWN_MS = 500L
    }

    override fun resolve(input: EyeAfPolicyInput): EyeAfDecision {
        val isFront = input.lensCapability.isFrontCamera
        val isFixed = input.lensCapability.isFixedFocus
        val isManualTap = input.userIntent.isTapToFocusActive
        val hasTarget = input.primaryTarget != null
        val hasEyes = input.primaryTarget?.leftEyeView != null || input.primaryTarget?.rightEyeView != null

        // Rule 1: Front camera manual tap — Reject
        if (isFront && isManualTap) {
            Log.i(TAG, "[v2.12.0-15] REJECT: front camera manual tap not allowed")
            return EyeAfDecision(
                anchor = MeteringAnchor.NONE,
                visualState = VisualTargetState.None,
                shouldTriggerFocus = false,
                shouldTriggerMetering = false,
                allowOpticalState = false,
            )
        }

        // Rule 2: Fixed-focus lens — SyntheticLock (bypass HAL)
        if (isFixed) {
            val anchor = when {
                hasEyes   -> MeteringAnchor.BOTH_EYES_CENTER
                hasTarget -> MeteringAnchor.UPPER_FACE
                else      -> MeteringAnchor.NONE
            }
            val visual = when {
                hasEyes   -> VisualTargetState.VisualLocked   // both eyes stable → visual lock
                hasTarget -> VisualTargetState.TrackingStable  // face box only → tracking (degraded)
                else      -> VisualTargetState.None
            }
            Log.i(TAG, "[v2.12.0-15] SYNTHETIC_LOCK: fixed-focus, anchor=$anchor, visual=$visual")
            return EyeAfDecision(
                anchor = anchor,
                visualState = visual,
                shouldTriggerFocus = false,   // bypass HAL
                shouldTriggerMetering = hasTarget,
                allowOpticalState = false,
            )
        }

        // Rule 3: Within post-lock cooldown — Throttle (skip trigger)
        val lastTrigger = input.lastFocusTriggerMs
        if (lastTrigger != null && (input.nowMs - lastTrigger) < COOLDOWN_MS) {
            Log.d(TAG, "[v2.12.0-15] THROTTLE: within post-lock cooldown")
            return EyeAfDecision(
                anchor = if (hasEyes) MeteringAnchor.BOTH_EYES_CENTER else MeteringAnchor.FACE_CENTER,
                visualState = if (hasTarget) VisualTargetState.TrackingStable else VisualTargetState.None,
                shouldTriggerFocus = false,
                shouldTriggerMetering = false,
                allowOpticalState = true,
            )
        }

        // Rule 4: Normal HAL path — Allow
        val anchor = when {
            hasEyes     -> MeteringAnchor.BOTH_EYES_CENTER
            hasTarget   -> MeteringAnchor.UPPER_FACE
            isManualTap -> MeteringAnchor.TAP_POINT
            else        -> MeteringAnchor.NONE
        }
        val visual = when {
            hasEyes   -> VisualTargetState.VisualLocked
            hasTarget -> VisualTargetState.TrackingStable
            else      -> VisualTargetState.None
        }
        Log.d(TAG, "[v2.12.0-15] ALLOW: normal HAL, anchor=$anchor, visual=$visual")
        return EyeAfDecision(
            anchor = anchor,
            visualState = visual,
            shouldTriggerFocus = hasTarget || isManualTap,
            shouldTriggerMetering = hasTarget || isManualTap,
            allowOpticalState = !isFront,
        )
    }
}