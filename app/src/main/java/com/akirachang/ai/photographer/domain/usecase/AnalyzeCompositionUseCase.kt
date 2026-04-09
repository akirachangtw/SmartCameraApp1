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
 * D1: [CompositionRule] interface and [CompositionScore] model established.
 *     Constructor will be migrated to Set<CompositionRule> injection in D2
 *     alongside Hilt @Multibinds DI module (avoids premature DI restructuring).
 *
 * TODO (Cueshot Ticket D2): Migrate to Rule Engine Architecture
 *   - Change constructor to: Set<@JvmSuppressWildcards CompositionRule>
 *   - Add Hilt @Multibinds binding in di/CompositionModule.kt
 *   - Implement weighted-average scoring over injected rules
 *   - Return CompositionScore instead of CompositionAdvice
 *   - Connect rules: ThirdsRule, HeadroomRule (D2), EyeLineRule, GazeRule (D6)
 */
class AnalyzeCompositionUseCase @Inject constructor() {

    operator fun invoke(
        detectionResult: DetectionResult?,
        previewWidth: Int,
        previewHeight: Int,
    ): CompositionAdvice = CompositionAdvice.None
}
