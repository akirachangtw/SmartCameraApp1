package com.akirachang.ai.photographer.domain.model

/**
 * Aggregate output of the Cueshot composition rule engine.
 *
 * Produced by [AnalyzeCompositionUseCase] after weighted evaluation of all
 * active [CompositionRule]s. UI layer maps this to visual feedback (Halo color,
 * Edge Warmth intensity, golden-moment shutter hint).
 *
 * @param totalScore  Weighted average of all rule scores [0.0, 100.0].
 * @param isGolden    True when totalScore exceeds the golden-moment threshold
 *                    (defined in AnalyzeCompositionUseCase, wired in D2).
 */
data class CompositionScore(
    val totalScore: Float,
    val isGolden: Boolean,
)
