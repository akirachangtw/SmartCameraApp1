package com.akirachang.ai.photographer.domain.usecase

import android.graphics.RectF
import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.RuleScore
import javax.inject.Inject

/**
 * AnalyzeCompositionUseCase — Cueshot v3.3 Rule Engine.
 *
 * Aggregates per-rule scores into a [CompositionResult] for a single frame.
 * No debounce or temporal smoothing — single-frame pure aggregation only.
 * Smoothing is deferred to D5.
 *
 * Coordinate contract: all [RectF] in [faces] must be pre-normalized to [0.0, 1.0].
 * The caller is responsible for coordinate normalization before invoking this use case.
 */
class AnalyzeCompositionUseCase @Inject constructor(
    private val rules: Set<@JvmSuppressWildcards CompositionRule>
) {

    operator fun invoke(
        faces: List<RectF>,
        imuPitch: Float,
        imuRoll: Float,
    ): CompositionResult {
        if (rules.isEmpty()) {
            return CompositionResult(
                topInstruction = null,
                ruleScores = emptyMap(),
                isAllAligned = false,
            )
        }

        val ruleScores: Map<String, RuleScore> = rules.associate { rule ->
            val result = rule.evaluate(faces, imuPitch, imuRoll)
            rule.name to RuleScore(
                name = rule.name,
                score = result.score,
                isAligned = result.isAligned,
                weight = rule.weight,
            )
        }

        val isAllAligned = ruleScores.values.all { it.isAligned }

        val topInstruction: RuleScore? = ruleScores.values
            .filter { !it.isAligned }
            .minByOrNull { it.score }

        return CompositionResult(
            topInstruction = topInstruction,
            ruleScores = ruleScores,
            isAllAligned = isAllAligned,
        )
    }
}
