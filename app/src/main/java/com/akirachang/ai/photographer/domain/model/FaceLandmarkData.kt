package com.akirachang.ai.photographer.domain.model

import android.graphics.PointF
import android.graphics.RectF

/**
 * Domain DTO carrying face geometry and landmark data for a single detected face.
 *
 * All spatial fields are normalized to [0.0, 1.0] relative to the image dimensions.
 * Coordinate origin is top-left (0, 0); bottom-right is (1, 1).
 *
 * Caller (ViewModel / mapping layer) is responsible for normalization before construction.
 * CompositionRule implementations must not apply any additional coordinate transformation.
 *
 * @param boundingBoxNormalized Face bounding box in normalized [0..1] image space.
 * @param leftEyeNormalized     Left eye landmark position, normalized [0..1], or null if
 *                              ML Kit did not detect / classify this landmark.
 * @param rightEyeNormalized    Right eye landmark position, normalized [0..1], or null if
 *                              ML Kit did not detect / classify this landmark.
 * @param headEulerAngleY       Yaw angle of the head (degrees). Positive = face turned right.
 *                              0.0 when unavailable.
 */
data class FaceLandmarkData(
    val boundingBoxNormalized: RectF,
    val leftEyeNormalized: PointF?,
    val rightEyeNormalized: PointF?,
    val headEulerAngleY: Float,
)
