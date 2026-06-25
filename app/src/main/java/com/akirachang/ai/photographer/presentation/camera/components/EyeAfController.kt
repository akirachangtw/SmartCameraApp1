package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.PointF
import android.util.Log
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.guidance.GuidanceState
import com.akirachang.ai.photographer.presentation.camera.AfState
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

        /**
         * Guard 7: Minimum normalized drift to allow stableId regeneration (≈5% of frame width).
         * Derived from SSOT v4.0 §7 Eye-AF contract: AF motor protection requires stable target
         * tracking; retrigger stableId churn must be suppressed when subject hasn't moved.
         * 0.05 ≈ 54px on 1080p — sufficient to distinguish deliberate subject movement from jitter.
         */
        internal const val DRIFT_THRESHOLD_NORMALIZED = 0.05f
    }

    // Sentinel: Long.MIN_VALUE / 2 ensures the first call always passes the throttle gate
    // regardless of nowMs magnitude (avoids Long overflow in subtraction).
    private var lastTriggerMs: Long = Long.MIN_VALUE / 2

    // Guard 7: tracks last trigger point + stableId to preserve LaunchedEffect key stability.
    private var lastTriggerPoint: com.akirachang.ai.photographer.presentation.utils.NormalizedPoint? = null
    private var lastStableId: String? = null

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
        guidanceState: GuidanceState = GuidanceState.GuidanceActive,
        compositionResult: CompositionResult?,
        faces: List<FaceLandmarkData>,
        facing: CameraLens,
        afState: AfState = AfState.Idle,
        previewWidth: Int,
        previewHeight: Int,
        nowMs: Long = System.currentTimeMillis(),
    ): EyeAfTrigger? {

        // ── Guard 0: STATE-CONTRACT-01 AI-5 — only GuidanceActive triggers AF ─────
        if (guidanceState != GuidanceState.GuidanceActive) {
            Log.v(TAG, "[Guard0] guidanceState=$guidanceState — AF suppressed")
            return null
        }

        // ── Guard 1: Constitution §7 — front camera never triggers manual AF ──────
        if (facing == CameraLens.FRONT) {
            Log.v(TAG, "[D6-B3] Front camera — AF suppressed")
            return null
        }

        // ── Guard 2: No composition data yet ─────────────────────────────────────
        if (compositionResult == null) return null

        // ── Guard 3: No faces detected ───────────────────────────────────────────
        if (faces.isEmpty()) return null

        // ── Guard 3.5 [P05-R1]: Filter Ghost Face — bbox must be fully within [0,1] ──
        val validFaces = faces.filter { face ->
            val box = face.boundingBoxNormalized
            box.left >= 0f && box.top >= 0f && box.right <= 1f && box.bottom <= 1f
        }
        if (validFaces.isEmpty()) {
            Log.v(TAG, "[P05-R1] All faces filtered — bbox out of [0,1] bounds, AF suppressed")
            return null
        }

        // ── Guard 4: Gaze is aligned or score too low — no need to drive AF ─────
        val gazeScore = compositionResult.ruleScores[GAZE_RULE_KEY]
        if (gazeScore == null || gazeScore.isAligned || gazeScore.score <= 0.1f) {
            Log.v(TAG, "[D6-B3] Gaze aligned/absent/near-zero(${gazeScore?.score}) — AF suppressed")
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
        val primaryFace = validFaces[0]

        // ── Resolve eye midpoint in normalized [0,1] space ────────────────────────
        val eyeMidpointNorm = resolveEyeMidpointNormalized(primaryFace)

        // ── Compute drift once for Guard 7a and Guard 7b ─────────────────────────
        val prev = lastTriggerPoint
        val drift = if (prev != null) {
            val dx = eyeMidpointNorm.x - prev.x
            val dy = eyeMidpointNorm.y - prev.y
            kotlin.math.sqrt(dx * dx + dy * dy)
        } else Float.MAX_VALUE

        // ── Guard 7a: Skip re-trigger when lens is already focused and stable ─────
        // Prevents AF motor oscillation when afState==Locked and the subject hasn't moved.
        if (afState == AfState.Locked && drift < DRIFT_THRESHOLD_NORMALIZED) {
            Log.v(TAG, "[Guard7a] Locked+stable drift=${"%.3f".format(drift)} — skip re-trigger")
            return null
        }

        // ── Guard 7b: Preserve stableId when target hasn't drifted past threshold ─
        // Prevents 2s-cycle stableId churn from re-keying LaunchedEffect and resetting
        // the dwell-fade animation. Trigger still fires; stableId is reused.
        val preserveStableId = prev != null && drift < DRIFT_THRESHOLD_NORMALIZED
        if (preserveStableId) {
            Log.v(TAG, "[Guard7b] drift=${"%.3f".format(drift)} < $DRIFT_THRESHOLD_NORMALIZED — reusing stableId to stabilize LaunchedEffect")
        }

        // ── Transform to view-space pixels ────────────────────────────────────────
        val transformer = NormalizedToPixelTransformer(
            screenWidth = previewWidth.toFloat(),
            screenHeight = previewHeight.toFloat(),
        )
        val eyePixel = transformer.transformPoint(eyeMidpointNorm)

        // ── Arm trigger ───────────────────────────────────────────────────────────
        lastTriggerMs = nowMs
        lastTriggerPoint = eyeMidpointNorm
        val stableId = if (preserveStableId && lastStableId != null) lastStableId!! else "gaze_af:${nowMs}"
        lastStableId = stableId
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
        lastTriggerPoint = null
        lastStableId = null
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
