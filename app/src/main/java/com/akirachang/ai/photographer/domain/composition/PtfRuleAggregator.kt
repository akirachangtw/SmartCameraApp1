package com.akirachang.ai.photographer.domain.composition

import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * PTF mode rule aggregator — evaluates three composition rules via Strict AND-Gate.
 *
 * ## AND-Gate contract
 * All three rules (Framing, Angle, Level) must pass simultaneously for
 * [PtfAlignmentResult.isAligned] to be true. A single failure produces false
 * and a non-zero [PtfAlignmentResult.offset] pointing from face center to
 * frame center.
 *
 * ## Stateless design
 * This class holds no mutable state. EMA(α=0.3) smoothing of face coordinates
 * is performed upstream in CameraViewModel before calling [aggregate].
 *
 * ## Coordinate system
 * All spatial inputs and outputs are normalized [0.0, 1.0] (origin = top-left).
 * UI layer converts offset to px by multiplying by viewport dimensions.
 *
 * Intentionally isolated from [PriorityEngine] — do NOT merge or share state.
 */
class PtfRuleAggregator @Inject constructor() {

    /**
     * Evaluates PTF alignment for a single frame.
     *
     * @param face         Smoothed (EMA) face data, or null if ML Kit detected no face.
     * @param devicePitch  Device pitch in degrees from IMU. Pass-to-friend valid range: [-15, 25].
     * @param deviceRoll   Device roll in degrees from IMU (positive = clockwise tilt).
     * @return [PtfAlignmentResult.NO_FACE] when [face] is null;
     *         otherwise AND-Gate result across all three rules.
     */
    fun aggregate(
        face: FaceLandmarkData?,
        devicePitch: Float,
        deviceRoll: Float,
    ): PtfAlignmentResult {
        if (face == null) return PtfAlignmentResult.NO_FACE

        val failedRules = mutableListOf<PtfRuleName>()

        // Rule 1 — Framing: Euclidean distance from face bbox center to frame center < 0.12
        val faceCenterX = (face.boundingBoxNormalized.left + face.boundingBoxNormalized.right) / 2f
        val faceCenterY = (face.boundingBoxNormalized.top + face.boundingBoxNormalized.bottom) / 2f
        val dx = FRAME_CENTER - faceCenterX
        val dy = FRAME_CENTER - faceCenterY
        val euclideanDist = sqrt(dx * dx + dy * dy)
        if (euclideanDist >= FRAMING_THRESHOLD) {
            failedRules.add(PtfRuleName.FRAMING)
        }

        // Rule 2 — Angle: device pitch must be within asymmetric tolerance [-15°, 25°]
        if (devicePitch < ANGLE_MIN_PITCH_DEG || devicePitch > ANGLE_MAX_PITCH_DEG) {
            failedRules.add(PtfRuleName.ANGLE)
        }

        // Rule 3 — Level: device roll must be within tolerance ±8°
        if (kotlin.math.abs(deviceRoll) >= LEVEL_MAX_ROLL_DEG) {
            failedRules.add(PtfRuleName.LEVEL)
        }

        val isAligned = failedRules.isEmpty()
        val offset = if (isAligned) PtfOffset.ZERO else PtfOffset(dx, dy)

        return PtfAlignmentResult(
            isAligned = isAligned,
            offset = offset,
            failedRules = failedRules,
        )
    }

    companion object {
        private const val FRAME_CENTER = 0.5f

        /** Euclidean distance threshold in normalized [0,1] space for FRAMING rule. */
        const val FRAMING_THRESHOLD = 0.12f

        /** Minimum device pitch (degrees) for ANGLE rule to pass. */
        const val ANGLE_MIN_PITCH_DEG = -15f

        /** Maximum device pitch (degrees) for ANGLE rule to pass. */
        const val ANGLE_MAX_PITCH_DEG = 25f

        /** Maximum absolute device roll (degrees) for LEVEL rule to pass (exclusive). */
        const val LEVEL_MAX_ROLL_DEG = 8f
    }
}
