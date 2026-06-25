package com.akirachang.ai.photographer.domain.composition.rules

import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.RuleResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Eye Line (horizon level) composition rule.
 *
 * Evaluates whether the subject's eye line is horizontal — i.e. both eyes
 * are at approximately the same vertical (Y) position in normalized space.
 *
 * Roll tilt is computed as the angle of the vector from right eye to left eye
 * relative to the horizontal axis. A perfect score (100) is given when the
 * tilt is 0°. Score degrades linearly to 0 at [MAX_TILT_DEG].
 *
 * Falls back to the bounding-box top edge as a proxy if either eye landmark
 * is unavailable (LANDMARK_NONE), giving a partial score only (capped at [FALLBACK_SCORE_CAP]).
 * Under LANDMARK_NONE, EyeLine visual overlay may be unavailable/degraded, but semantic
 * fallback scoring still participates.
 * If no faces are present the rule returns score=0, isAligned=false.
 *
 * Face coordinates MUST be pre-normalized to [0..1].
 */
class EyeLineRule @Inject constructor() : CompositionRule {

    override val name: String = "eye_line"
    // SSOT §4.2 構圖類矩陣：眼線水平度
    override val weight: Float = 0.15f
    // Landmark-based, ML Kit eye accuracy is reasonably high
    override val confidenceFactor: Float = 0.75f

    override fun evaluate(faces: List<FaceLandmarkData>, imuPitch: Float, imuRoll: Float): RuleResult {
        if (faces.isEmpty()) return RuleResult(score = 0f, isAligned = false)

        // Pick the largest face by bounding-box area.
        val primary = faces.maxByOrNull {
            val box = it.boundingBoxNormalized
            box.width() * box.height()
        } ?: return RuleResult(score = 0f, isAligned = false)

        // Validate bounding box coordinates.
        val box = primary.boundingBoxNormalized
        if (box.left < 0f || box.top < 0f || box.right > 1f || box.bottom > 1f ||
            box.left > box.right || box.top > box.bottom
        ) {
            return RuleResult(score = 0f, isAligned = false)
        }

        val leftEye = primary.leftEyeNormalized
        val rightEye = primary.rightEyeNormalized

        val tiltDeg: Float
        val isFallback: Boolean

        if (leftEye != null && rightEye != null) {
            // Full landmark path: compute roll angle from left→right eye vector.
            // atan2 gives angle in radians; positive Y axis points downward in image space.
            // Using left→right direction so dx > 0 when horizontal (angle = 0°).
            val dx = rightEye.x - leftEye.x
            val dy = rightEye.y - leftEye.y
            tiltDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat().let { abs(it) }
            isFallback = false
        } else {
            // Fallback path: no eye landmarks available. Return minimal fallback score.
            return RuleResult(score = FALLBACK_SCORE_CAP, isAligned = false)
        }

        val rawScore = (100f * (1f - tiltDeg / MAX_TILT_DEG)).coerceIn(0f, 100f)
        val score = if (isFallback) rawScore.coerceAtMost(FALLBACK_SCORE_CAP) else rawScore
        val isAligned = score >= ALIGNED_THRESHOLD

        return RuleResult(score = score, isAligned = isAligned)
    }

    companion object {
        /** Rule key — referenced by HaloGuidanceLayer. */
        const val NAME = "eye_line"

        /** Tilt angle (degrees) at which score reaches 0. */
        private const val MAX_TILT_DEG = 15f

        /** Score threshold above which the eye line is considered "aligned". */
        private const val ALIGNED_THRESHOLD = 85f

        /** Maximum score returned when eye landmarks are unavailable (fallback mode). */
        private const val FALLBACK_SCORE_CAP = 30f
    }
}
