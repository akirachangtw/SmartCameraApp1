package com.akirachang.ai.photographer.data.source.camera

import android.graphics.RectF
import kotlin.math.abs

// ── 1 Euro Filter (L2 §1.1 / Focus_V1 §5.1) ───────────────────────────────
//
// Low-pass filter that adapts its cutoff frequency based on signal speed:
//   - Slow movement → low cutoff → aggressive smoothing (removes jitter)
//   - Fast movement → high cutoff → minimal lag (tracks motion)
//
// Filters a single float channel. Instantiate four for bbox L/T/R/B.
//
// Parameters:
//   minCutoff (default 1.0): base cutoff Hz — lower = smoother but more lag
//   beta      (default 0.007): speed coefficient — higher = less lag during motion
//   dCutoff   (default 1.0): derivative cutoff Hz

class OneEuroFilter(
    private val minCutoff: Float = 1.0f,
    private val beta: Float = 0.007f,
    private val dCutoff: Float = 1.0f
) {
    private var firstTime = true
    private var lastTime = 0L
    private var xPrev = 0f
    private var dxPrev = 0f

    private fun alpha(cutoff: Float, dt: Float): Float {
        val tau = 1.0f / (2.0f * Math.PI.toFloat() * cutoff)
        return 1.0f / (1.0f + tau / dt)
    }

    /**
     * @param value     Raw input value (e.g., bbox.left in screen px)
     * @param timestamp Monotonic timestamp in milliseconds (System.currentTimeMillis())
     * @return          Filtered value
     */
    fun filter(value: Float, timestamp: Long): Float {
        if (firstTime) {
            firstTime = false
            lastTime = timestamp
            xPrev = value
            dxPrev = 0f
            return value
        }

        val dt = (timestamp - lastTime) / 1000f  // ms → seconds
        if (dt <= 0f) return xPrev

        // 1. Raw derivative (speed)
        val dx = (value - xPrev) / dt
        // 2. Filtered derivative
        val alphaD = alpha(dCutoff, dt)
        val dxHat = dxPrev + alphaD * (dx - dxPrev)
        // 3. Adaptive cutoff = base + speed * beta
        val cutoff = minCutoff + beta * abs(dxHat)
        // 4. Filtered value
        val a = alpha(cutoff, dt)
        val xHat = xPrev + a * (value - xPrev)

        lastTime = timestamp
        xPrev = xHat
        dxPrev = dxHat
        return xHat
    }

    fun reset() {
        firstTime = true
        lastTime = 0L
        xPrev = 0f
        dxPrev = 0f
    }
}

// ── RectFilter ──────────────────────────────────────────────────────────────
//
// Wraps four OneEuroFilter instances (left, top, right, bottom) to smooth an
// entire bounding box while preserving its shape.
//
// Usage:
//   val smoother = RectFilter()
//   // In ML Kit callback:
//   val smoothed = smoother.filter(rawScreenBox, System.currentTimeMillis())
//   // Use smoothed.centerX() / smoothed.centerY() for focus indicator position
//   // Use smoothed.width() / smoothed.height() for focus indicator size

class RectFilter(
    minCutoff: Float = 1.0f,
    beta: Float = 0.007f,
    dCutoff: Float = 1.0f
) {
    private val leftFilter = OneEuroFilter(minCutoff, beta, dCutoff)
    private val topFilter = OneEuroFilter(minCutoff, beta, dCutoff)
    private val rightFilter = OneEuroFilter(minCutoff, beta, dCutoff)
    private val bottomFilter = OneEuroFilter(minCutoff, beta, dCutoff)

    /**
     * @param raw       Unfiltered bounding box in screen-pixel coordinates
     * @param timestamp Monotonic timestamp in milliseconds
     * @return          Smoothed bounding box
     */
    fun filter(raw: RectF, timestamp: Long): RectF {
        return RectF(
            leftFilter.filter(raw.left, timestamp),
            topFilter.filter(raw.top, timestamp),
            rightFilter.filter(raw.right, timestamp),
            bottomFilter.filter(raw.bottom, timestamp)
        )
    }

    fun reset() {
        leftFilter.reset()
        topFilter.reset()
        rightFilter.reset()
        bottomFilter.reset()
    }
}

// ── FocusUiSmoother ─────────────────────────────────────────────────────────
//
// High-level API combining RectFilter + a center-point cache.
// Compose UI observes `smoothedTarget` StateFlow at ≤60 FPS via
// StateFlow.sample(16L) (L2 §1.2).
//
// ```kotlin
// val smoother = FocusUiSmoother()
//
// // In ML Kit pipeline callback (any thread):
// smoother.onNewTarget(trackedTarget)
//
// // In Compose:
// val target by smoother.smoothedTarget.collectAsState()
// target?.let { FocusIndicator(x = it.centerX, y = it.centerY, ...) }
// ```

class FocusUiSmoother(
    minCutoff: Float = 1.0f,
    beta: Float = 0.007f
) {
    private val rectFilter = RectFilter(minCutoff, beta)

    private val _smoothedTarget = kotlinx.coroutines.flow.MutableStateFlow<TrackedTarget?>(null)
    val smoothedTarget: kotlinx.coroutines.flow.StateFlow<TrackedTarget?>
        get() = _smoothedTarget

    /**
     * Feed a new ML Kit detection result. Thread-safe.
     * Pass null when target is lost (after TargetStateStore TTL expires).
     */
    fun onNewTarget(target: TrackedTarget?) {
        if (target == null) {
            _smoothedTarget.value = null
            rectFilter.reset()
            return
        }

        val now = System.currentTimeMillis()
        val smoothBox = rectFilter.filter(target.screenBox, now)

        _smoothedTarget.value = target.copy(
            screenBox = smoothBox,
            centerX = smoothBox.centerX(),
            centerY = smoothBox.centerY()
        )
    }

    fun reset() {
        _smoothedTarget.value = null
        rectFilter.reset()
    }
}
