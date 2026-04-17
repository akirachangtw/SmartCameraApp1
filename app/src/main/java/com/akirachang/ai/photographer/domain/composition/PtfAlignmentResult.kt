package com.akirachang.ai.photographer.domain.composition

/**
 * Aggregate output of the PTF rule engine.
 *
 * Produced by [PtfRuleAggregator] after evaluating all three PTF rules
 * (Framing, Angle, Level) via Strict AND-Gate logic.
 *
 * @property isAligned   True only when all three rules pass simultaneously.
 *                       Drives the breathing-rhythm Halo animation in PTF mode.
 * @property offset      Normalized [0,1] displacement from face bbox center to
 *                       frame center. [PtfOffset.ZERO] when aligned or no face
 *                       detected. UI layer multiplies by viewport size for px.
 * @property failedRules Rules that did not pass, ordered by visual priority
 *                       (FRAMING > ANGLE > LEVEL). Empty when [isAligned] is true.
 */
data class PtfAlignmentResult(
    val isAligned: Boolean,
    val offset: PtfOffset,
    val failedRules: List<PtfRuleName>,
) {
    companion object {
        /** Sentinel for when no face is detected — treat as not aligned, no guidance. */
        val NO_FACE = PtfAlignmentResult(
            isAligned = false,
            offset = PtfOffset.ZERO,
            failedRules = emptyList(),
        )
    }
}
