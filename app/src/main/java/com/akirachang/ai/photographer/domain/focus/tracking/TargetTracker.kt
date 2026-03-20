package com.akirachang.ai.photographer.domain.focus.tracking

import com.akirachang.ai.photographer.domain.model.focus.FocusTarget
import com.akirachang.ai.photographer.domain.model.focus.TrackedTargetState

/**
 * Domain interface for target tracking.
 * v2.12.0-15 / §17.11.5
 *
 * Rules:
 * - Must maintain stableId across frames via IoU rebinding (threshold >= 0.5).
 * - Must implement TTL grace period (300ms) before emitting Lost/None.
 * - Must NOT call any hardware API.
 * - Must NOT hold UI state.
 */
interface TargetTracker {
    /**
     * Update tracker with new detection results.
     * @param detectedTargets Raw targets from DetectionEngine.
     * @param nowMs Current timestamp in milliseconds.
     * @return [TrackedTargetState] with stable IDs and primary selection.
     */
    fun update(detectedTargets: List<FocusTarget>, nowMs: Long): TrackedTargetState

    /**
     * Reset all tracking state (e.g., on camera switch or lifecycle stop).
     */
    fun reset()
}
