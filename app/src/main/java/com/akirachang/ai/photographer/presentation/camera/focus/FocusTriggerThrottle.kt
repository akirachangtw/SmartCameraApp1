package com.akirachang.ai.photographer.presentation.camera.focus

import android.util.Log
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Focus trigger throttle controller.
 * v2.12.0-15 / §17.12.2
 *
 * Extracted from AfTriggerThrottleController with domain-layer decoupling:
 * - Accepts VisualTargetState instead of AfState
 * - Adds Target Lost Grace Period (500ms) to prevent 10s stall bug
 * - Removes direct AfState dependency
 *
 * NOTE: AfTriggerThrottleController in AfStateMachine.kt is kept for backward
 * compatibility with CameraViewModel. This class is for Step 5+ usage.
 */
class FocusTriggerThrottle(
    private val preLockThrottleMs:   Long  = 1_500L,
    private val postLockCooldownMs:  Long  = 5_000L,
    private val movingCooldownMs:    Long  = 2_000L,
    private val backoffBaseMs:       Long  = 2_000L,
    private val targetLostGraceMs:   Long  = 500L,    // v2.12.0-15: Grace period before forcing None
    var rescanThresholdPx:           Float = 54f,
) {
    companion object {
        private const val TAG = "FocusTriggerThrottle"
        private const val MAX_FAIL_COUNT = 3
    }

    private var lastTriggerTimeMs   = 0L
    private var nextAllowedTimeMs   = 0L
    private var isArmed             = false
    private var lockedTimestamp     = 0L
    private var consecutiveFailCount = 0
    private var targetLostTimeMs    = 0L   // v2.12.0-15: when target was last seen

    private val stabilityTracker = InternalStabilityTracker()

    fun setScreenWidth(widthPx: Int) {
        rescanThresholdPx = widthPx * 0.05f
    }

    fun onSuccess() {
        isArmed              = true
        lockedTimestamp      = now()
        nextAllowedTimeMs    = now() + postLockCooldownMs
        consecutiveFailCount = 0
        targetLostTimeMs     = 0L
        Log.d(TAG, "[v2.12.0-15] onSuccess: armed, cooldown=${postLockCooldownMs}ms")
    }

    fun onFailure(failCount: Int) {
        val backoff = min(backoffBaseMs * (1L shl (failCount - 1).coerceIn(0, 2)), 8_000L)
        nextAllowedTimeMs = now() + backoff
        consecutiveFailCount++
        if (consecutiveFailCount >= MAX_FAIL_COUNT) {
            Log.i(TAG, "[v2.12.0-15] Auto-retry suspended (failCount=$consecutiveFailCount)")
        }
    }

    /**
     * v2.12.0-15: Target Lost Grace Period.
     * Call when primary target is lost. After [targetLostGraceMs], shouldTrigger() returns false.
     */
    fun onTargetLost() {
        if (targetLostTimeMs == 0L) {
            targetLostTimeMs = now()
            Log.d(TAG, "[v2.12.0-15] Target lost, grace period started (${targetLostGraceMs}ms)")
        }
    }

    /**
     * Call when target is reacquired.
     */
    fun onTargetReacquired() {
        targetLostTimeMs = 0L
    }

    fun shouldTrigger(
        eyeScreenX: Float,
        eyeScreenY: Float,
        currentVisualState: VisualTargetState,
    ): Boolean {
        val n = now()

        // v2.12.0-15: Target Lost Grace Period — force stop after grace period
        if (targetLostTimeMs > 0L && (n - targetLostTimeMs) > targetLostGraceMs) {
            Log.d(TAG, "[v2.12.0-15] Grace period expired → shouldTrigger=false")
            return false
        }

        // Suspended mode
        if (consecutiveFailCount >= MAX_FAIL_COUNT) return false

        // Don't interrupt in-flight FOCUSING equivalent
        if (currentVisualState == VisualTargetState.Candidate) return false

        val effectiveThreshold = if (isArmed && currentVisualState == VisualTargetState.VisualLocked)
            rescanThresholdPx * 2.5f else rescanThresholdPx

        val deltaPx  = stabilityTracker.getDeltaPx(eyeScreenX, eyeScreenY)
        val isMoving = deltaPx > effectiveThreshold

        if (isArmed && isMoving && currentVisualState == VisualTargetState.VisualLocked) {
            val potentialMoveTime = lockedTimestamp + movingCooldownMs
            nextAllowedTimeMs = min(nextAllowedTimeMs, max(n, potentialMoveTime))
        }

        if (n < nextAllowedTimeMs) return false

        if (!isArmed && currentVisualState == VisualTargetState.None) {
            if (n - lastTriggerTimeMs < preLockThrottleMs) return false
        }

        lastTriggerTimeMs = n
        return true
    }

    fun reset() {
        lastTriggerTimeMs    = 0L
        nextAllowedTimeMs    = 0L
        isArmed              = false
        lockedTimestamp      = 0L
        consecutiveFailCount = 0
        targetLostTimeMs     = 0L
        stabilityTracker.reset()
        Log.d(TAG, "[v2.12.0-15] FocusTriggerThrottle reset")
    }

    private fun now() = System.currentTimeMillis()

    // Internal stability tracker (domain-decoupled copy of StabilityTracker)
    private class InternalStabilityTracker {
        private var lastX = Float.NaN
        private var lastY = Float.NaN

        fun getDeltaPx(x: Float, y: Float): Float {
            if (lastX.isNaN()) { lastX = x; lastY = y; return 0f }
            val dx = x - lastX; val dy = y - lastY
            lastX = x; lastY = y
            return sqrt(dx * dx + dy * dy)
        }

        fun reset() { lastX = Float.NaN; lastY = Float.NaN }
    }
}
