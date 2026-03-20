package com.akirachang.ai.photographer.presentation.camera

import com.akirachang.ai.photographer.BuildConfig
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sqrt

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class FailReason { TIMEOUT, HAL_FAILED, EXCEPTION }

// ── 4-way AF State  (v2.12.0-05: Step 3+4)  ──────────────────────────────────
//
// Replaces the two-boolean (isFocusing + focusSuccess) pattern with a proper
// sealed hierarchy.
//
// State transitions (per L2 §2.1):
//   IDLE      → FOCUSING   on valid Eye-AF / User-Tap trigger (throttle gate passed)
//   FOCUSING  → LOCKED     on HAL success
//   FOCUSING  → FAILED     on HAL failure, TIMEOUT, or EXCEPTION
//   LOCKED    → FOCUSING   on significant subject movement + cooldown expired
//   LOCKED    → IDLE       on subject lost / AE-AF unlock
//   FAILED    → IDLE       after backoff delay (next trigger resets state)
//   any       → IDLE       on manual reset (tracking lost, camera switch, …)

sealed class AfState {
    object Idle     : AfState()
    object Focusing : AfState()
    object Locked   : AfState()
    data class Failed(val reason: FailReason, val failCount: Int) : AfState()
}

// ── StabilityTracker ──────────────────────────────────────────────────────────
//
// Tracks screen-space Eye-AF position delta between successive ML Kit frames.
// Used by AfTriggerThrottleController to distinguish stable vs. moving subjects.

internal class StabilityTracker {
    private var lastX = Float.NaN
    private var lastY = Float.NaN

    /** Returns Euclidean distance (px) since the previous call. */
    fun getDeltaPx(x: Float, y: Float): Float {
        if (lastX.isNaN()) { lastX = x; lastY = y; return 0f }
        val dx = x - lastX
        val dy = y - lastY
        lastX = x
        lastY = y
        return sqrt(dx * dx + dy * dy)
    }

    fun reset() { lastX = Float.NaN; lastY = Float.NaN }
}

// ── AfTriggerThrottleController  (L2 §3.2) ───────────────────────────────────
//
// Gates Eye-AF startFocusAndMetering() frequency.
// User Tap bypasses all throttle — call focusOnPoint() directly.

/**
 * @deprecated Use FocusTriggerThrottle (presentation/camera/focus/) instead.
 * Kept for backward compatibility with CameraViewModel until Step 5 migration.
 * v2.12.0-15
 */
@Deprecated("Use FocusTriggerThrottle instead", level = DeprecationLevel.WARNING)
internal class AfTriggerThrottleController(
    private val stabilityTracker:    StabilityTracker,
    private val preLockThrottleMs:   Long  = 1_500L,  // unarmed: min ms between attempts
    private val postLockCooldownMs:  Long  = 5_000L,  // armed+stable: hard cooldown
    private val movingCooldownMs:    Long  = 2_000L,  // armed+moving: shortened cooldown
    private val backoffBaseMs:       Long  = 2_000L,  // failure backoff base (doubles per fail)
    var rescanThresholdPx:           Float = 54f       // 5 % of screen width (~1080px)
) {
    private var lastTriggerTimeMs  = 0L
    private var nextAllowedTimeMs  = 0L
    private var isArmed            = false             // true after first LOCKED success
    private var lockedTimestamp    = 0L
    private val maxFailCount: Int = 3              // v2.12.0-11: max consecutive failures before suspend
    private var consecutiveFailCount: Int = 0      // v2.12.0-11: consecutive failure counter

    /** Call when preview width is known (SetPreviewSize event). */
    fun setScreenWidth(widthPx: Int) {
        rescanThresholdPx = widthPx * 0.05f
    }

    /** Call from CameraViewModel when focus succeeded (LOCKED state). */
    fun onSuccess() {
        isArmed           = true
        lockedTimestamp   = now()
        nextAllowedTimeMs = now() + postLockCooldownMs
        consecutiveFailCount = 0  // v2.12.0-11: reset on success
    }

    /**
     * Call when focus failed (HAL_FAILED / TIMEOUT / EXCEPTION).
     * Applies exponential backoff capped at 8 s.
     */
    fun onFailure(failCount: Int) {
        val backoff = min(backoffBaseMs * (1L shl (failCount - 1).coerceIn(0, 2)), 8_000L)
        nextAllowedTimeMs = now() + backoff
        consecutiveFailCount++
        if (consecutiveFailCount >= maxFailCount) {
            android.util.Log.i("AfStateMachine",
                "[v2.12.0-11] Auto-retry suspended (failCount=$consecutiveFailCount) — awaiting UserTap")
        }
    }

    /**
     * Returns true when Eye-AF is allowed to call startFocusAndMetering().
     * Must be called on the Main thread (same as CameraViewModel coroutine dispatcher).
     *
     * @param eyeScreenX  Nearest-eye X in screen px (from CoordinateTransformer)
     * @param eyeScreenY  Nearest-eye Y in screen px
     * @param currentAfState  Current AfState from CameraState
     */
    fun shouldTrigger(eyeScreenX: Float, eyeScreenY: Float, currentAfState: AfState): Boolean {
        val n = now()

        // v2.12.0-11: Suspended mode — too many consecutive failures, await UserTap or camera switch
        if (consecutiveFailCount >= maxFailCount) return false

        // Rule 1 — Don't interrupt in-flight FOCUSING
        if (currentAfState is AfState.Focusing) return false

        // v2.12.0-11: Expand hysteresis 2.5x when Locked to suppress ML Kit bbox micro-jitter
        val effectiveThreshold = if (isArmed && currentAfState is AfState.Locked)
            rescanThresholdPx * 2.5f else rescanThresholdPx

        // Rule 2 — Stability / displacement check
        val deltaPx  = stabilityTracker.getDeltaPx(eyeScreenX, eyeScreenY)
        val isMoving = deltaPx > effectiveThreshold

        // Rule 3 — Dynamic cooldown shortening for moving LOCKED target
        if (isArmed && isMoving && currentAfState is AfState.Locked) {
            val potentialMoveTime = lockedTimestamp + movingCooldownMs
            nextAllowedTimeMs = min(nextAllowedTimeMs, max(n, potentialMoveTime))
        }

        // Rule 4 — Time gate
        if (BuildConfig.DEBUG && isArmed && currentAfState is AfState.Locked && !isMoving) {
            android.util.Log.d("AfStateMachine",
                "[v2.12.0-11] Hysteresis gate (Locked): delta=${deltaPx}px < ${effectiveThreshold}px — skip")
        }
        if (n < nextAllowedTimeMs) return false

        // Rule 5 — Pre-lock base throttle (unarmed + IDLE first attempt)
        if (!isArmed && currentAfState is AfState.Idle) {
            if (n - lastTriggerTimeMs < preLockThrottleMs) return false
        }

        lastTriggerTimeMs = n
        return true
    }

    fun reset() {
        lastTriggerTimeMs = 0L
        nextAllowedTimeMs = 0L
        isArmed           = false
        lockedTimestamp   = 0L
        stabilityTracker.reset()
        consecutiveFailCount = 0  // v2.12.0-11: reset on UserTap / camera switch
    }

    private fun now() = System.currentTimeMillis()
}
