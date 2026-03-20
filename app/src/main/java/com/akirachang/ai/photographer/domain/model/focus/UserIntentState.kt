package com.akirachang.ai.photographer.domain.model.focus

import android.graphics.PointF

/**
 * Captures explicit user intentions that influence target ranking and AF behaviour.
 * v2.12.0-15 / §17.11.6
 *
 * Tap events must feed into target priority scoring, not just trigger a one-shot AF request.
 */
data class UserIntentState(
    val tapPointView: PointF?,
    val tapTimestampMs: Long?,
    val isTapToFocusActive: Boolean,
    val isAeAfLockRequested: Boolean,
)
