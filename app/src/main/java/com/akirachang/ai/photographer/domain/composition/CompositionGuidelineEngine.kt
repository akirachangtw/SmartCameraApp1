package com.akirachang.ai.photographer.domain.composition

import android.graphics.RectF
import android.util.Log
import com.akirachang.ai.photographer.domain.model.CompositionAdvice
import kotlin.math.sqrt

/**
 * Pure domain-layer engine for Rule-of-Thirds composition analysis.
 *
 * v2.13.0: Reuses the viewport-coordinate RectF already produced by the
 * v2.12.0 CoordinateTransformer pipeline — no additional matrix work needed.
 *
 * Contract:
 * - Stateless: every call is independent, no side effects.
 * - Thread-safe: no shared mutable state.
 * - ViewModel/UI free: zero Android UI imports.
 * - Invalid geometry → returns [CompositionAdvice.None] (never throws).
 */
class CompositionGuidelineEngine {

    // Fraction of max(viewportW, viewportH) within which a centre is "aligned".
    private val ALIGNMENT_TOLERANCE = 0.05f

    /**
     * Analyse where [targetRect] sits relative to the Rule-of-Thirds grid.
     *
     * @param targetRect  Subject bounding box in viewport coordinates (post-Matrix transform).
     * @param viewportWidth  Viewport width in pixels (must be > 0).
     * @param viewportHeight Viewport height in pixels (must be > 0).
     * @return [CompositionAdvice.None] on invalid input,
     *         [CompositionAdvice.Aligned] when the centre is on an intersection,
     *         [CompositionAdvice.Adjust] otherwise.
     */
    fun analyzeRuleOfThirds(
        targetRect: RectF,
        viewportWidth: Float,
        viewportHeight: Float,
    ): CompositionAdvice {
        // ── Guard: invalid geometry ──────────────────────────────────────────────
        if (viewportWidth <= 0f || viewportHeight <= 0f) {
            Log.w(TAG, "[RoT] Invalid viewport ${viewportWidth}x${viewportHeight} → None")
            return CompositionAdvice.None
        }
        if (targetRect.width() <= 0f || targetRect.height() <= 0f) {
            Log.w(TAG, "[RoT] Invalid targetRect ${targetRect} → None")
            return CompositionAdvice.None
        }

        // ── Step 1: Four Rule-of-Thirds golden intersections ────────────────────
        val w = viewportWidth
        val h = viewportHeight
        val intersections = listOf(
            Pair(w / 3f, h / 3f),
            Pair(2f * w / 3f, h / 3f),
            Pair(w / 3f, 2f * h / 3f),
            Pair(2f * w / 3f, 2f * h / 3f),
        )

        // ── Step 2: Subject centre ───────────────────────────────────────────────
        val centerX = targetRect.centerX()
        val centerY = targetRect.centerY()

        // ── Step 3: Nearest intersection ────────────────────────────────────────
        val nearest = intersections.minByOrNull { (ix, iy) ->
            euclideanDistance(centerX, centerY, ix, iy)
        }!! // list is never empty

        val (nearestX, nearestY) = nearest
        val distance = euclideanDistance(centerX, centerY, nearestX, nearestY)

        // ── Step 4: Tolerance check ──────────────────────────────────────────────
        val tolerancePx = ALIGNMENT_TOLERANCE * maxOf(w, h)
        if (distance <= tolerancePx) {
            Log.d(TAG, "[RoT] Aligned (dist=${distance}px ≤ tol=${tolerancePx}px)")
            return CompositionAdvice.Aligned
        }

        // ── Step 5: Build Adjust advice ──────────────────────────────────────────
        // delta = direction camera/frame must move so subject reaches intersection
        val deltaX = nearestX - centerX
        val deltaY = nearestY - centerY

        Log.d(TAG, "[RoT] Adjust dx=$deltaX dy=$deltaY")
        return CompositionAdvice.Adjust(deltaX = deltaX, deltaY = deltaY)
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private fun euclideanDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    companion object {
        private const val TAG = "CompositionGuidelineEngine"
    }
}
