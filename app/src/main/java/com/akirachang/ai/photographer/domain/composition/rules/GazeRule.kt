package com.akirachang.ai.photographer.domain.composition.rules

import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.RuleResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import javax.inject.Inject
import kotlin.math.abs

/**
 * Gaze Direction composition rule.
 *
 * Evaluates whether the subject is facing (approximately) toward the camera.
 * Uses [FaceLandmarkData.headEulerAngleY] from ML Kit — the yaw angle of the
 * head in degrees (positive = turning right, negative = turning left).
 *
 * A yaw close to 0° means the subject faces forward. Score degrades linearly
 * to 0 at [MAX_YAW_DEG]. Subjects facing strongly off-camera (yaw >= MAX_YAW_DEG)
 * indicate a "looking away" composition that warrants low priority.
 *
 * Falls back gracefully to score=0/isAligned=false if no faces are present.
 * Bounding-box validation is applied first; faces with out-of-range boxes are
 * rejected to honour the normalized-coordinate contract.
 */
class GazeRule @Inject constructor() : CompositionRule {

    override val name: String = "gaze"
    // SSOT §4.2 構圖類矩陣：注視方向
    override val weight: Float = 0.15f
    // headEulerAngleY is a raw ML Kit output — moderate confidence
    override val confidenceFactor: Float = 0.70f

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

        val absYaw = abs(primary.headEulerAngleY)
        val score = (100f * (1f - absYaw / MAX_YAW_DEG)).coerceIn(0f, 100f)
        val isAligned = score >= ALIGNED_THRESHOLD

        return RuleResult(score = score, isAligned = isAligned)
    }

    companion object {
        /** Yaw angle (degrees) at which score reaches 0 — subject fully side-on. */
        private const val MAX_YAW_DEG = 45f

        /** Score threshold above which gaze is considered "forward-facing". */
        private const val ALIGNED_THRESHOLD = 85f
    }
}
