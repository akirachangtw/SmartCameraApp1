package com.akirachang.ai.photographer.domain.focus.policy

import com.akirachang.ai.photographer.domain.model.focus.EyeAfDecision
import com.akirachang.ai.photographer.domain.model.focus.EyeAfPolicyInput

/**
 * Domain interface for Eye-AF / focus policy decision.
 * v2.12.0-15 / §17.11.7
 *
 * Rules:
 * - Stateless: must not hold mutable state.
 * - Pure decision: must not call any hardware API (no startFocusAndMetering).
 * - All callers must go through this interface; FocusOrchestrator is the only adapter.
 */
interface EyeAfPolicyResolver {
    /**
     * Given the current [input], return the recommended focus action.
     */
    fun resolve(input: EyeAfPolicyInput): EyeAfDecision
}
