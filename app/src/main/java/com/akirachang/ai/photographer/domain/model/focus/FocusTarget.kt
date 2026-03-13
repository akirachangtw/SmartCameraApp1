package com.akirachang.ai.photographer.domain.model.focus

import android.graphics.PointF
import android.graphics.RectF

/**
 * Unified representation of a focus target (face / object / tap).
 * v2.12.0-15 / §17.11.5
 *
 * stableId format: "face:123" / "object:7" / "tap:manual:{sessionId}"
 */
data class FocusTarget(
    val stableId: String,
    val type: TargetType,
    val rectImage: RectF,
    val rectView: RectF,
    val leftEyeView: PointF?,
    val rightEyeView: PointF?,
    val confidence: Float,
    val priorityScore: Float,
    val trackingId: Int?,
    val firstSeenMs: Long,
    val lastSeenMs: Long,
    val source: TargetSource,
)

enum class TargetType {
    FACE,
    OBJECT,
    TAP_SUBJECT,
    TAP_TEXTURE,
}

enum class TargetSource {
    ML_KIT_FACE,
    ML_KIT_OBJECT,
    OPTICAL_FLOW,
    USER_TAP,
}
