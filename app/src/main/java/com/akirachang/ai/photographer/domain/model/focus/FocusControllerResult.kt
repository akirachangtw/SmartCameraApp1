package com.akirachang.ai.photographer.domain.model.focus

import android.graphics.PointF
import android.graphics.RectF

/**
 * Result returned by FocusController after applying an EyeAfDecision.
 * v2.12.0-15 / §17.11.8
 *
 * OpticalFocusState is an auxiliary signal only — it must NOT override
 * VisualLocked or AEAFLocked semantics.
 */
data class FocusControllerResult(
    val triggerSent: Boolean,
    val meteringPointView: PointF?,
    val meteringRegionView: RectF?,
    val opticalResult: OpticalFocusState?,
)

enum class OpticalFocusState {
    UNKNOWN,
    PASSIVE_CONTINUOUS,
    FOCUSED,
    NOT_FOCUSED,
    UNSUPPORTED,
}
