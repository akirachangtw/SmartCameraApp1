package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.PointF
import android.util.Log
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.presentation.utils.NormalizedPoint
import com.akirachang.ai.photographer.presentation.utils.NormalizedToPixelTransformer

/**
 * Eye-AF trigger output — consumed by CameraViewModel to call cameraRepository.focusOnPoint().
 *
 * @param viewPoint   Metering point in view-space pixels (ready for MeteringPointFactory).
 * @param stableId    Stable identifier for logging and throttle correlation.
 */
data class EyeAfTrigger(
    val viewPoint: PointF,
    val stableId: String,
)

/**
 * EyeAfController — Cueshot D6-B3.
 *
 * Converts composition rule engine output into Eye-AF hardware trigger commands.
 *
 * Trigger contract:
 *   - `ruleScores["gaze"]?.isAligned == false` AND `facing == BACK` AND faces non-empty
 *     → derive eye midpoint (left + right average; falls back to single eye or bounding-box
 *       center if landmarks are absent) → emit [EyeAfTrigger].
 *   - `ruleScores["gaze"]?.isAligned == true` OR `facing == FRONT` OR faces empty
 *     → return null (no hardware trigger).
 *
 * Constitution §7 guard: front camera never triggers AF. This class is the first line of
 * defense; LiveCameraSource.focusOnPoint() acts as the second gate: if isFixedFocus is true
 * (as detected by FixedFocusDetector at camera bind time), it issues AE/AWB-only metering
 * and suppresses FLAG_AF.
 *
 * Throttle: minimum [GAZE_AF_COOLDOWN_MS] between successive triggers to protect the AF
 * motor from oscillation. Decoupled from the 10fps Halo rendering throttle.
 *
 * Usage: call [process] once per composition result update (inside observeCompositionAdvice).
 * All calls must be made on the same thread (Main / viewModelScope).
 */
class EyeAfController {

    companion object {
        private const val TAG = "EyeAfController"

        /** Minimum milliseconds between successive AF triggers (motor protection). */
        internal const val GAZE_AF_COOLDOWN_MS = 2_000L

        /** Rule key for gaze alignment — references GazeRule.NAME directly. */
        private val GAZE_RULE_KEY = GazeRule.NAME
    }

    // Sentinel: Long.MIN_VALUE / 2 ensures the first call always passes the throttle gate
    // regardless of nowMs magnitude (avoids Long overflow in subtraction).
    private var lastTriggerMs: Long = Long.MIN_VALUE / 2

    /**
     * Evaluates the current composition state and produces an [EyeAfTrigger] when all
     * conditions are met, or null when the trigger must be suppressed.
     *
     * @param compositionResult  Latest rule engine output; null → suppress.
     * @param faces              Detected face landmark list from the same frame.
     * @param facing             Current camera lens; [CameraLens.FRONT] → always suppress.
     * @param previewWidth       View width in pixels — used for normalized→pixel transform.
     * @param previewHeight      View height in pixels.
     * @param nowMs              Current epoch milliseconds (injectable for testing).
     * @return [EyeAfTrigger] if AF should fire, null otherwise.
     */
    fun process(
        compositionResult: CompositionResult?,
        faces: List<FaceLandmarkData>,
        facing: CameraLens,
        previewWidth: Int,
        previewHeight: Int,
        nowMs: Long = System.currentTimeMillis(),
    ): EyeAfTrigger? {

        // ── Guard 1: Constitution §7 — front camera never triggers manual AF ──────
        if (facing == CameraLens.FRONT) {
            Log.v(TAG, "[D6-B3] Front camera — AF suppressed")
            return null
        }

        // ── Guard 2: No composition data yet ─────────────────────────────────────
        if (compositionResult == null) return null

        // ── Guard 3: No faces detected ───────────────────────────────────────────
        if (faces.isEmpty()) return null

        // ── Guard 4: Gaze is aligned — no need to drive AF ───────────────────────
        val gazeScore = compositionResult.ruleScores[GAZE_RULE_KEY]
        if (gazeScore == null || gazeScore.isAligned) {
            Log.v(TAG, "[D6-B3] Gaze aligned or absent — AF suppressed")
            return null
        }

        // ── Guard 5: Preview dimensions must be valid ─────────────────────────────
        if (previewWidth <= 0 || previewHeight <= 0) {
            Log.w(TAG, "[D6-B3] Invalid preview size ${previewWidth}x${previewHeight} — AF suppressed")
            return null
        }

        // ── Guard 6: Throttle — protect AF motor from oscillation ─────────────────
        if (nowMs - lastTriggerMs < GAZE_AF_COOLDOWN_MS) {
            Log.v(TAG, "[D6-B3] Throttle gate — ${nowMs - lastTriggerMs}ms < ${GAZE_AF_COOLDOWN_MS}ms")
            return null
        }

        // ── Select primary face (index 0 — highest confidence per ML Kit contract) ─
        val primaryFace = faces[0]

        // ── Resolve eye midpoint in normalized [0,1] space ────────────────────────
        val eyeMidpointNorm = resolveEyeMidpointNormalized(primaryFace)

        // ── Transform to view-space pixels ────────────────────────────────────────
        val transformer = NormalizedToPixelTransformer(
            screenWidth = previewWidth.toFloat(),
            screenHeight = previewHeight.toFloat(),
        )
        val eyePixel = transformer.transformPoint(eyeMidpointNorm)

        // ── Arm trigger ───────────────────────────────────────────────────────────
        lastTriggerMs = nowMs
        val stableId = "gaze_af:${nowMs}"
        Log.i(TAG, "[D6-B3] EyeAfTrigger: viewPoint=(${eyePixel.x}, ${eyePixel.y}) stableId=$stableId gazeScore=${gazeScore.score}")

        return EyeAfTrigger(
            viewPoint = PointF(eyePixel.x, eyePixel.y),
            stableId = stableId,
        )
    }

    /**
     * Resets throttle state — call on camera switch or focus pipeline reset.
     */
    fun reset() {
        lastTriggerMs = Long.MIN_VALUE / 2
        Log.d(TAG, "[D6-B3] EyeAfController reset")
    }

    /**
     * Derives a normalized eye midpoint from face landmarks.
     *
     * Priority:
     *   1. Both eyes present → average
     *   2. Left eye only → left eye
     *   3. Right eye only → right eye
     *   4. No landmarks → bounding-box center (safe fallback, no crash)
     */
    internal fun resolveEyeMidpointNormalized(face: FaceLandmarkData): NormalizedPoint {
        val left = face.leftEyeNormalized
        val right = face.rightEyeNormalized

        return when {
            left != null && right != null ->
                NormalizedPoint((left.x + right.x) / 2f, (left.y + right.y) / 2f)
            left != null ->
                NormalizedPoint(left.x, left.y)
            right != null ->
                NormalizedPoint(right.x, right.y)
            else -> {
                // Bounding-box center fallback — landmarks not classified by ML Kit
                val box = face.boundingBoxNormalized
                NormalizedPoint(
                    (box.left + box.right) / 2f,
                    (box.top + box.bottom) / 2f,
                )
            }
        }
    }
}
