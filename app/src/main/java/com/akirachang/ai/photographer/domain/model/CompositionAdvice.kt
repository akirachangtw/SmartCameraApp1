package com.akirachang.ai.photographer.domain.model

/**
 * Represents the result of a Rule-of-Thirds composition analysis.
 *
 * v2.13.0: Domain model for CompositionGuidelineEngine output.
 *
 * Three possible outcomes:
 * - [None]    : Analysis could not run (invalid geometry input)
 * - [Aligned] : Subject centre is within tolerance of the nearest golden intersection
 * - [Adjust]  : Subject needs to move; deltaX/deltaY indicate direction (UI layer translates)
 */
sealed class CompositionAdvice {

    /** Input geometry was invalid (zero/negative width or height). */
    object None : CompositionAdvice()

    /** Subject is already on (or very close to) a Rule-of-Thirds intersection. */
    object Aligned : CompositionAdvice()

    /**
     * Subject needs reframing.
     *
     * @param deltaX Positive = move right, Negative = move left (in viewport pixels)
     * @param deltaY Positive = move down,  Negative = move up   (in viewport pixels)
     *
     * Note: No localised string is carried here (i18n rule — UI layer owns translation).
     */
    data class Adjust(
        val deltaX: Float,
        val deltaY: Float,
    ) : CompositionAdvice()
}
