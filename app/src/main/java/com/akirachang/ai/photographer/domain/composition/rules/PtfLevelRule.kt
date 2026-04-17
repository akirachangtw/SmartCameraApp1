package com.akirachang.ai.photographer.domain.composition.rules

import com.akirachang.ai.photographer.domain.composition.PtfRuleName
import kotlin.math.abs

/**
 * PTF Level Rule — device roll must be within ±[MAX_ROLL_DEG] of horizontal.
 *
 * Passes when: abs(deviceRoll) < 8.0° (exclusive)
 */
object PtfLevelRule {

    const val MAX_ROLL_DEG = 8f
    val RULE_NAME = PtfRuleName.LEVEL

    fun evaluate(deviceRoll: Float): Boolean = abs(deviceRoll) < MAX_ROLL_DEG
}
