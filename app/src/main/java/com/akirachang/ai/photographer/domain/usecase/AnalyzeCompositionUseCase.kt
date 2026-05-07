package com.akirachang.ai.photographer.domain.usecase

import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.PriorityEngine
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.RuleScore
import javax.inject.Inject

/**
 * AnalyzeCompositionUseCase — Cueshot v3.3 Rule Engine.
 *
 * Aggregates per-rule scores into a [CompositionResult] for a single frame.
 * No debounce or temporal smoothing — single-frame pure aggregation only.
 *
 * Priority selection delegates to [PriorityEngine] using SSOT §4.1 formula:
 *   priority = rule.weight × ((100 − score) / 100) × rule.confidenceFactor
 *
 * Alignment aggregation uses weighted average score:
 *   weightedAvg = Σ(weight_i × score_i) / Σ(weight_i)
 *   isAllAligned = weightedAvg >= ALIGNED_WEIGHTED_THRESHOLD (70.0)
 *
 * This decouples isAllAligned from topInstruction — a frame can be "well-composed"
 * even when one rule suggests a minor tweak (topInstruction != null).
 *
 * Coordinate contract: all spatial fields in [FaceLandmarkData] must be pre-normalized
 * to [0.0, 1.0]. The caller (ViewModel mapping layer) is responsible for normalization.
 */
class AnalyzeCompositionUseCase @Inject constructor(
    private val rules: Set<@JvmSuppressWildcards CompositionRule>,
    private val priorityEngine: PriorityEngine,
) {

    operator fun invoke(
        faces: List<FaceLandmarkData>,
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

        val ruleScoresMap: Map<CompositionRule, RuleScore> = rules.associateWith { rule ->
            val result = rule.evaluate(faces, imuPitch, imuRoll)
            RuleScore(
                name = rule.name,
                score = result.score,
                isAligned = result.isAligned,
                weight = rule.weight,
            )
        }

        val topInstruction: RuleScore? = priorityEngine.calculateTopInstruction(ruleScoresMap)
        val isAllAligned = computeWeightedAlignment(ruleScoresMap.values)

        return CompositionResult(
            topInstruction = topInstruction,
            ruleScores = ruleScoresMap.mapKeys { it.key.name },
            isAllAligned = isAllAligned,
        )
    }

    private fun computeWeightedAlignment(scores: Collection<RuleScore>): Boolean {
        val totalWeight = scores.sumOf { it.weight.toDouble() }
        if (totalWeight <= 0.0) return false
        val weightedAvg = scores.sumOf { it.weight.toDouble() * it.score } / totalWeight
        return weightedAvg >= ALIGNED_WEIGHTED_THRESHOLD
    }

    companion object {
        const val ALIGNED_WEIGHTED_THRESHOLD = 70.0
    }
}
