package com.akirachang.ai.photographer.domain.usecase

import com.akirachang.ai.photographer.domain.model.CompositionAdvice
import com.akirachang.ai.photographer.domain.model.DetectionResult
import javax.inject.Inject

/**
 * AnalyzeCompositionUseCase — Cueshot v3.3 Rule Engine skeleton.
 *
 * D0: Internal logic cleared. v2.4.0 composition analysis (hardcoded Chinese strings)
 * fully removed per i18n 三道防線 + 焦土政策.
 *
 * TODO (Cueshot Ticket D1): Implement v3.3 Rule Engine Architecture
 *   - Wire CompositionGuidelineEngine (domain/composition/)
 *   - Connect rules: ThirdsRule, HeadroomRule, EyeLineRule, GazeRule
 *   - Output: CompositionAdvice sealed class (pure semantic codes, no hardcoded strings)
 */
class AnalyzeCompositionUseCase @Inject constructor() {

    operator fun invoke(
        detectionResult: DetectionResult?,
        previewWidth: Int,
        previewHeight: Int,
    ): CompositionAdvice = CompositionAdvice.None
}
