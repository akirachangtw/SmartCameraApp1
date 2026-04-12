package com.akirachang.ai.photographer.domain.composition.rules

import android.graphics.RectF
import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.RuleResult
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Rule of Thirds composition rule.
 *
 * Evaluates whether the primary subject (largest face) is aligned with one of
 * the four rule-of-thirds intersection points in normalized [0..1] viewport space.
 *
 * Coordinate invariant: the grid is always anchored at the fixed normalized
 * coordinates (1/3, 1/3), (2/3, 1/3), (1/3, 2/3), (2/3, 2/3). It never
 * drifts with face positions.
 *
 * Face bounding boxes MUST be pre-normalized to [0..1] x [0..1] before calling.
 */
class ThirdsRule @Inject constructor() : CompositionRule {

    override val name: String = "thirds"
    // SSOT §4.2 構圖類矩陣：三分法基礎權重
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

        val cx = primary.centerX()
        val cy = primary.centerY()

        // Closest distance to any fixed thirds intersection point.
        val minDist = THIRDS_POINTS.minOf { (ix, iy) ->
            val dx = cx - ix
            val dy = cy - iy
            sqrt(dx * dx + dy * dy)
        }

        val score = (100f * (1f - minDist / MAX_TOLERANCE)).coerceIn(0f, 100f)
        val isAligned = score >= ALIGNED_THRESHOLD

        return RuleResult(score = score, isAligned = isAligned)
    }

    companion object {
        /** Maximum distance from an intersection for any score > 0. */
        private const val MAX_TOLERANCE = 0.3f

        /** Score threshold above which the face is considered "aligned". */
        private const val ALIGNED_THRESHOLD = 85f

        /** Four rule-of-thirds intersection points in normalized [0..1] space. */
        private val THIRDS_POINTS = listOf(
            Pair(1f / 3f, 1f / 3f),
            Pair(2f / 3f, 1f / 3f),
            Pair(1f / 3f, 2f / 3f),
            Pair(2f / 3f, 2f / 3f)
        )
    }
}
