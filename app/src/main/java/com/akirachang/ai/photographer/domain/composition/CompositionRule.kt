package com.akirachang.ai.photographer.domain.composition

import android.graphics.RectF

/**
 * Result of a single composition rule evaluation.
 *
 * @param score     Normalized score [0.0, 100.0]. Higher = better composition.
 * @param isAligned True when the subject satisfies this rule's alignment criterion.
 */
data class RuleResult(val score: Float, val isAligned: Boolean)

/**
 * Contract for all Cueshot composition rules.
 *
 * Rules are stateless, thread-safe, and UI-free — pure domain logic only.
 * Each rule receives pre-computed geometry and returns a [RuleResult].
 *
 * Implementations (ThirdsRule, HeadroomRule, EyeLineRule, GazeRule) are
 * added in D2+. This interface is the stable contract they must satisfy.
 *
 * Wiring: rules are injected as a [Set] into [AnalyzeCompositionUseCase]
 * via Hilt @Multibinds (configured in D2 DI module).
 */
interface CompositionRule {
    /** Weight for weighted-average total score. Must be > 0. */
    val weight: Float

    /**
     * Evaluate this rule against the current frame geometry.
     *
     * @param faces    Face bounding boxes in viewport coordinates (post-matrix transform).
     * @param imuPitch Device pitch angle in degrees (positive = tilting up).
     * @param imuRoll  Device roll angle in degrees (positive = rolling right).
     * @return [RuleResult] with score and alignment flag. Never throws.
     */
    fun evaluate(faces: List<RectF>, imuPitch: Float, imuRoll: Float): RuleResult
}
