package com.akirachang.ai.photographer.data.focus.tracking

import android.graphics.RectF
import android.util.Log
import com.akirachang.ai.photographer.domain.focus.tracking.TargetTracker
import com.akirachang.ai.photographer.domain.model.focus.FocusTarget
import com.akirachang.ai.photographer.domain.model.focus.TrackedTargetState

/**
 * Default target tracker with IoU-based stableId rebinding and TTL grace period.
 * v2.12.0-15 / §17.11.5
 *
 * IoU threshold: 0.5 (strict rebinding to avoid ID swap)
 * TTL grace period: 300ms — prevents zombie frames and 10s stall bug
 */
class DefaultTargetTracker(
    private val iouThreshold: Float = 0.5f,
    private val targetTtlMs: Long = 300L,
) : TargetTracker {

    companion object {
        private const val TAG = "DefaultTargetTracker"
    }

    // stableId → last seen timestamp
    private val trackedTimestamps = mutableMapOf<String, Long>()
    // stableId → last known FocusTarget
    private val lastKnownTargets = mutableMapOf<String, FocusTarget>()
    private var primaryStableId: String? = null

    override fun update(detectedTargets: List<FocusTarget>, nowMs: Long): TrackedTargetState {
        if (detectedTargets.isEmpty()) {
            return handleNoTargets(nowMs)
        }

        // Assign stableIds via IoU rebinding
        val assignedTargets = assignStableIds(detectedTargets, nowMs)

        // Update tracking state
        assignedTargets.forEach { target ->
            trackedTimestamps[target.stableId] = nowMs
            lastKnownTargets[target.stableId] = target
        }

        // Evict expired targets (TTL exceeded)
        evictExpired(nowMs)

        // v2.12.0-15 Phase 2: Primary selection with SSOT priority weights
        // Priority: Tap > Face+Eyes > Face-only > Object
        // hasEyes bonus: +2.0f to ensure face with eyes always beats face-only
        val primary = assignedTargets.maxByOrNull { target ->
            val eyeBonus = if (target.leftEyeView != null || target.rightEyeView != null) 2.0f else 0f
            val typeBonus = when (target.type) {
                com.akirachang.ai.photographer.domain.model.focus.TargetType.TAP_SUBJECT,
                com.akirachang.ai.photographer.domain.model.focus.TargetType.TAP_TEXTURE -> 10.0f
                com.akirachang.ai.photographer.domain.model.focus.TargetType.FACE -> 1.0f
                com.akirachang.ai.photographer.domain.model.focus.TargetType.OBJECT -> 0.5f
            }
            target.priorityScore + eyeBonus + typeBonus
        }
        if (primary != null) primaryStableId = primary.stableId

        val primaryType = primary?.type?.name ?: "none"
        val primaryHasEyes = primary?.let { it.leftEyeView != null || it.rightEyeView != null } ?: false
        val primaryScore = primary?.let { t ->
            val eyeBonus = if (t.leftEyeView != null || t.rightEyeView != null) 2.0f else 0f
            val typeBonus = when (t.type) {
                com.akirachang.ai.photographer.domain.model.focus.TargetType.TAP_SUBJECT,
                com.akirachang.ai.photographer.domain.model.focus.TargetType.TAP_TEXTURE -> 10.0f
                com.akirachang.ai.photographer.domain.model.focus.TargetType.FACE -> 1.0f
                com.akirachang.ai.photographer.domain.model.focus.TargetType.OBJECT -> 0.5f
            }
            t.priorityScore + eyeBonus + typeBonus
        } ?: 0f
        // SSOT Priority: Tap(+10) > Face+Eyes(+1+2) > Face-only(+1) > Object(+0.5)
        Log.d(TAG, "[v2.12.0-15 T4-F1] update: ${assignedTargets.size} targets, primary=$primaryStableId type=$primaryType hasEyes=$primaryHasEyes effectiveScore=$primaryScore")

        return TrackedTargetState(
            allTargets = assignedTargets,
            primaryTarget = primary,
            recentlyLostTarget = null,
        )
    }

    override fun reset() {
        trackedTimestamps.clear()
        lastKnownTargets.clear()
        primaryStableId = null
        Log.d(TAG, "[v2.12.0-15] TargetTracker reset")
    }

    private fun handleNoTargets(nowMs: Long): TrackedTargetState {
        val primaryId = primaryStableId
        val lastSeen = if (primaryId != null) trackedTimestamps[primaryId] else null

        // Check if primary target is within TTL grace period
        val recentlyLost = if (primaryId != null && lastSeen != null &&
            nowMs - lastSeen <= targetTtlMs) {
            lastKnownTargets[primaryId]
        } else null

        evictExpired(nowMs)

        if (recentlyLost == null) {
            Log.d(TAG, "[v2.12.0-15] No targets, TTL expired → None")
        } else {
            Log.d(TAG, "[v2.12.0-15] No targets, within TTL (${nowMs - (lastSeen ?: 0)}ms) recentlyLost=${recentlyLost.stableId}")
        }

        return TrackedTargetState(
            allTargets = emptyList(),
            primaryTarget = null,
            recentlyLostTarget = recentlyLost,
        )
    }

    private fun evictExpired(nowMs: Long) {
        val expiredIds = trackedTimestamps
            .filter { (_, ts) -> nowMs - ts > targetTtlMs }
            .keys.toSet()
        expiredIds.forEach { id ->
            trackedTimestamps.remove(id)
            lastKnownTargets.remove(id)
            if (primaryStableId == id) primaryStableId = null
        }
    }

    private fun assignStableIds(
        detectedTargets: List<FocusTarget>,
        nowMs: Long,
    ): List<FocusTarget> {
        val result = mutableListOf<FocusTarget>()
        val usedIds = mutableSetOf<String>()
        var newIdIndex = 0

        for (target in detectedTargets) {
            // Canonical format: ${type}_${trackingId} — underscore, no colon
            val trackingKey = if (target.trackingId != null) {
                "${target.type.name.lowercase()}_${target.trackingId}"
            } else null

            val matchedId = when {
                trackingKey != null && trackedTimestamps.containsKey(trackingKey) && trackingKey !in usedIds -> trackingKey
                else -> findBestIouMatch(target.rectView, usedIds)
            }

            val stableId = matchedId ?: generateNewStableId(target, nowMs, newIdIndex++)
            usedIds.add(stableId)
            result.add(target.copy(stableId = stableId))
        }

        return result
    }

    private fun findBestIouMatch(rect: RectF, excludeIds: Set<String>): String? {
        var bestIou = iouThreshold
        var bestId: String? = null

        for ((id, target) in lastKnownTargets) {
            if (id in excludeIds) continue
            val iou = computeIou(rect, target.rectView)
            if (iou > bestIou) {
                bestIou = iou
                bestId = id
            }
        }

        return bestId
    }

    private fun generateNewStableId(target: FocusTarget, nowMs: Long, index: Int): String =
        if (target.trackingId != null) "${target.type.name.lowercase()}_${target.trackingId}"
        else "${target.type.name.lowercase()}_${nowMs}_$index"

    private fun computeIou(a: RectF, b: RectF): Float {
        val interLeft   = maxOf(a.left, b.left)
        val interTop    = maxOf(a.top, b.top)
        val interRight  = minOf(a.right, b.right)
        val interBottom = minOf(a.bottom, b.bottom)
        if (interRight <= interLeft || interBottom <= interTop) return 0f
        val interArea = (interRight - interLeft) * (interBottom - interTop)
        val aArea = a.width() * a.height()
        val bArea = b.width() * b.height()
        val unionArea = aArea + bArea - interArea
        return if (unionArea <= 0f) 0f else interArea / unionArea
    }
}
