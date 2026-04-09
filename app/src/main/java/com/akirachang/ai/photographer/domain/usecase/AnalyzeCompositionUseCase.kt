package com.akirachang.ai.photographer.domain.usecase

import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.model.CompositionScore
import javax.inject.Inject

/**
 * AnalyzeCompositionUseCase — Cueshot v3.3 Rule Engine skeleton.
 *
 * D0: Internal logic cleared. v2.4.0 composition analysis (hardcoded Chinese strings)
 * fully removed per i18n 三道防線 + 焦土政策.
 *
 * D1: Constructor migrated to Set<CompositionRule> injection via Hilt @Multibinds.
 *     invoke() returns skeleton CompositionScore(totalScore=0f, isGolden=false).
 *     Concrete rule implementations (ThirdsRule, HeadroomRule) deferred to D2.
 */
class AnalyzeCompositionUseCase @Inject constructor(
    private val rules: Set<@JvmSuppressWildcards CompositionRule>
) {

    operator fun invoke(
        faces: List<android.graphics.RectF>,
        imuPitch: Float,
        imuRoll: Float,
    ): CompositionScore {
        // TODO (Cueshot Ticket D2): 實作加權總分計算邏輯與防抖過濾器
        return CompositionScore(totalScore = 0f, isGolden = false)
    }
}
