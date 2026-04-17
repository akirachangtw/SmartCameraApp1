package com.akirachang.ai.photographer.domain.composition.rules

import com.akirachang.ai.photographer.domain.composition.PtfRuleName

/**
 * PTF Angle Rule — device pitch must be within the asymmetric range [-15°, 25°].
 *
 * A pitch below -15° (phone tilted too far back) or above 25° (too far forward)
 * means the friend holding the phone is at an uncomfortable angle for the subject.
 *
 * Passes when: pitch ∈ [-15.0, 25.0] (inclusive)
 */
object PtfAngleRule {

    const val MIN_PITCH_DEG = -15f
    const val MAX_PITCH_DEG = 25f
    val RULE_NAME = PtfRuleName.ANGLE

    fun evaluate(devicePitch: Float): Boolean =
        devicePitch >= MIN_PITCH_DEG && devicePitch <= MAX_PITCH_DEG
}
