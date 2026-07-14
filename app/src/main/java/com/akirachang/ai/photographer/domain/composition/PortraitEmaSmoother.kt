package com.akirachang.ai.photographer.domain.composition

import android.graphics.RectF
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.akirachang.ai.photographer.domain.config.GuidanceStateConstants
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import javax.inject.Inject

/**
 * GAP-11: SSOT §2.5.1 Portrait EMA — `smoothed = last * 0.7 + new * 0.3` on the normalized bbox
 * edges, killing the ±4.79% ML Kit quantisation jitter that pulses the Halo radius.
 *
 * The pipeline's single guidance smoothing point (Hard Constraint 2). Unscoped, so each
 * CameraViewModel owns its own EMA history; the AF branch keeps the raw list (Hard Constraint 1).
 */
class PortraitEmaSmoother @Inject constructor() {

    // Keyed by ML Kit trackingId (enableTracking() is on). A key absent from the current frame is
    // dropped, so a lost or swapped face starts clean instead of trailing a ghost bbox.
    private val emaBoxes = mutableMapOf<Int, RectF>()

    /**
     * @param trackingIds ML Kit trackingId per face, positionally aligned with [faces]. A null id
     *                    passes the face through unsmoothed — there is no identity to accumulate against.
     */
    fun smooth(trackingIds: List<Int?>, faces: List<FaceLandmarkData>): List<FaceLandmarkData> {
        val nextState = mutableMapOf<Int, RectF>()
        val smoothed = faces.mapIndexed { idx, face ->
            val id = trackingIds.getOrNull(idx) ?: return@mapIndexed face
            val raw = face.boundingBoxNormalized
            val last = emaBoxes[id]
            val box = RectF(
                portraitEmaStep(last?.left, raw.left),
                portraitEmaStep(last?.top, raw.top),
                portraitEmaStep(last?.right, raw.right),
                portraitEmaStep(last?.bottom, raw.bottom),
            )
            nextState[id] = box
            Log.d(TAG, "[GAP-11] face[$id] emaBbox w=${box.width()} h=${box.height()} (raw w=${raw.width()} h=${raw.height()})")
            face.copy(boundingBoxNormalized = box)
        }
        emaBoxes.clear()
        emaBoxes.putAll(nextState)
        return smoothed
    }

    /** Drops all EMA history — the frame carried no detection, so no identity survives. */
    fun reset() = emaBoxes.clear()

    companion object {
        private const val TAG = "PortraitEmaSmoother"

        /**
         * SSOT §2.5.1 (:536-541) recurrence — sole truth source: [smooth] delegates every bbox edge
         * here, so no inline copy of the formula exists.
         *
         * @param last previously smoothed value, or null on a tracked identity's first sample —
         *             seeds the EMA with the raw value rather than pulling the bbox toward zero.
         */
        @VisibleForTesting
        internal fun portraitEmaStep(last: Float?, new: Float): Float =
            if (last == null) new
            else last * GuidanceStateConstants.PORTRAIT_EMA_HISTORY +
                new * GuidanceStateConstants.PORTRAIT_EMA_NEW
    }
}
