package com.akirachang.ai.photographer.domain.composition.rules

import android.graphics.RectF
import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.RuleResult
import javax.inject.Inject

/**
 * Headroom composition rule.
 *
 * Evaluates whether the primary subject (largest face) has appropriate headroom —
 * the vertical space between the top of the face and the top of the frame.
 *
 * Headroom ratio = yDistance / faceHeight, where:
 *   - yDistance = face.top (distance from frame top to face top in normalized space)
 *   - faceHeight = face.bottom - face.top
 *
 * Ideal range: [0.1, 0.2]. Score = 100 when ratio is in range; decreases linearly outside.
 *
 * Face bounding boxes MUST be pre-normalized to [0..1] x [0..1] before calling.
 * ⚠️ Coordinate contract: see CompositionRule.evaluate() KDoc.
 */
class HeadroomRule @Inject constructor() : CompositionRule {

    override val name: String = "headroom"
    // SSOT §4.2 構圖類矩陣：淨空基礎權重
    override val weight: Float = 0.20f
    // Face detection via ML Kit — high but not absolute confidence
    override val confidenceFactor: Float = 0.80f

    override fun evaluate(faces: List<RectF>, imuPitch: Float, imuRoll: Float): RuleResult {
        if (faces.isEmpty()) return RuleResult(score = 0f, isAligned = false)

        // Reject any face with out-of-range or malformed coordinates (graceful degradation).
        val hasInvalidCoordinate = faces.any { face ->
            face.left < 0f || face.top < 0f ||
            face.right > 1f || face.bottom > 1f ||
            face.left > face.right || face.top > face.bottom
        }
        if (hasInvalidCoordinate) return RuleResult(score = 0f, isAligned = false)

        // Pick the largest face by bounding-box area.
        val primary = faces.maxByOrNull { it.width() * it.height() }
            ?: return RuleResult(score = 0f, isAligned = false)

        val faceHeight = primary.bottom - primary.top
        if (faceHeight <= 0f) return RuleResult(score = 0f, isAligned = false)

        val yDistance = primary.top
        val ratio = yDistance / faceHeight

        val isAligned = ratio >= IDEAL_MIN && ratio <= IDEAL_MAX

        val score = if (isAligned) {
            100f
        } else {
            // Linear decay from ideal boundary toward zero.
            val distFromIdeal = if (ratio < IDEAL_MIN) {
                IDEAL_MIN - ratio
            } else {
                ratio - IDEAL_MAX
            }
            (100f * (1f - distFromIdeal / MAX_DEVIATION)).coerceIn(0f, 99f)
        }

        return RuleResult(score = score, isAligned = isAligned)
    }

    companion object {
        /** Ideal headroom ratio lower bound. */
        private const val IDEAL_MIN = 0.1f

        /** Ideal headroom ratio upper bound. */
        private const val IDEAL_MAX = 0.2f

        /** Maximum ratio deviation for any score > 0. */
        private const val MAX_DEVIATION = 0.5f
    }
}
