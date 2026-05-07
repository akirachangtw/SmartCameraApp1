package com.akirachang.ai.photographer.domain.model

/**
 * Per-rule evaluation output, produced by [AnalyzeCompositionUseCase].
 *
 * @param name       Rule identifier (e.g. "thirds", "headroom").
 * @param score      Raw rule score [0.0, 100.0].
 * @param isAligned  True when the rule considers the subject well-positioned.
 * @param weight     Rule weight used in the aggregation pass.
 */
data class RuleScore(
    val name: String,
    val score: Float,
    val isAligned: Boolean,
    val weight: Float,
)

/**
 * Aggregate output of the Cueshot composition rule engine.
 *
 * Produced by [AnalyzeCompositionUseCase] after evaluating all active rules.
 * UI layer maps this to visual feedback (Halo color, breathing rhythm, golden-moment hint).
 *
 * @param topInstruction  The rule most urgently needing correction (lowest score among
 *                        non-aligned rules), or null when all rules are satisfied.
 * @param ruleScores      Individual rule outputs keyed by rule name.
 * @param isAllAligned    True when the weighted average score across all rules >= 70.0.
 *                        Independent of [topInstruction] — a frame can be well-composed
 *                        even if one rule still suggests a minor tweak.
 *                        Drives breathing animation and golden-moment shutter hint.
 */
data class CompositionResult(
    val topInstruction: RuleScore?,
    val ruleScores: Map<String, RuleScore>,
    val isAllAligned: Boolean,
)
