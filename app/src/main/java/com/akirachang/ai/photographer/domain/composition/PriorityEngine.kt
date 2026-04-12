package com.akirachang.ai.photographer.domain.composition

import javax.inject.Inject

/**
 * D5-T2 Priority Engine — selects the highest-priority misaligned rule.
 *
 * Algorithm (SSOT §4.1):
 *   priority = rule.weight * ((100 - result.score) / 100) * rule.confidenceFactor
 *
 * A lower score means the composition is further from ideal, producing a higher
 * priority value. The rule with the highest priority becomes the top instruction
 * the user should act on first.
 *
 * This class has no external dependencies — pure domain logic only.
 * Wired into [AnalyzeCompositionUseCase] in D5-T3.
 */
class PriorityEngine @Inject constructor() {

    /**
     * Calculates which misaligned rule deserves the user's attention most urgently.
     *
     * @param results Map of each [CompositionRule] to its [RuleResult] from the current frame.
     * @return The [RuleResult] of the highest-priority misaligned rule, or `null` if all
     *         rules are aligned (triggers breathing-rhythm UI).
     */
    fun calculateTopInstruction(results: Map<CompositionRule, RuleResult>): RuleResult? {
        val misaligned = results.entries.filter { (_, result) -> !result.isAligned }

        if (misaligned.isEmpty()) return null

        return misaligned
            .maxByOrNull { (rule, result) ->
                rule.weight * ((100f - result.score) / 100f) * rule.confidenceFactor
            }
            ?.value
    }
}
