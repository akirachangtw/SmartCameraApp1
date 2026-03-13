package com.akirachang.ai.photographer.data.focus.control

import android.graphics.PointF
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPointFactory
import com.akirachang.ai.photographer.domain.focus.control.FocusController
import com.akirachang.ai.photographer.domain.model.focus.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraX implementation of FocusController.
 * v2.12.0-15 / §17.11.8
 *
 * ⚠️ All startFocusAndMetering() calls are made here — nowhere else.
 *
 * Coordinate resolution (from FocusTarget.rectView, view-space):
 *   BOTH_EYES_CENTER → midpoint of leftEyeView + rightEyeView
 *   SINGLE_EYE       → leftEyeView ?? rightEyeView ?? rectView center
 *   UPPER_FACE       → top-center of rectView
 *   FACE_CENTER      → center of rectView
 *   TRACKED_REGION   → center of rectView
 *   TAP_POINT        → center of rectView (tap target)
 *   NONE             → no metering action
 *
 * Uses suspendCancellableCoroutine to wrap ListenableFuture
 * (no ListenableFuture.await() extension in this project).
 */
class CameraXFocusController(
    private val getCameraControl: () -> CameraControl?,
    private val getMeteringPointFactory: () -> MeteringPointFactory?,
) : FocusController {

    companion object {
        private const val TAG = "CameraXFocusController"
        private const val AF_TIMEOUT_SEC = 5L
    }

    override suspend fun apply(
        decision: EyeAfDecision,
        primaryTarget: FocusTarget?,
        geometryContext: GeometryContext,
    ): FocusControllerResult = withContext(Dispatchers.Main) {

        // No trigger needed
        if (!decision.shouldTriggerFocus && !decision.shouldTriggerMetering) {
            Log.d(TAG, "[v2.12.0-15] apply: no trigger (anchor=${decision.anchor})")
            return@withContext FocusControllerResult(
                triggerSent = false,
                meteringPointView = null,
                meteringRegionView = null,
                opticalResult = null,
            )
        }

        // Resolve metering point from anchor + primaryTarget
        val meteringPoint = resolveMeteringPoint(decision.anchor, primaryTarget)
        if (meteringPoint == null) {
            Log.w(TAG, "[v2.12.0-15] apply: cannot resolve point for anchor=${decision.anchor}")
            return@withContext FocusControllerResult(
                triggerSent = false,
                meteringPointView = null,
                meteringRegionView = null,
                opticalResult = null,
            )
        }

        val factory = getMeteringPointFactory()
        val cameraControl = getCameraControl()

        if (factory == null || cameraControl == null) {
            Log.w(TAG, "[v2.12.0-15] apply: factory or cameraControl not ready")
            return@withContext FocusControllerResult(
                triggerSent = false,
                meteringPointView = meteringPoint,
                meteringRegionView = null,
                opticalResult = null,
            )
        }

        // Build CameraX MeteringAction
        val cxPoint = factory.createPoint(meteringPoint.x, meteringPoint.y)
        val action = FocusMeteringAction.Builder(cxPoint).apply {
            if (decision.shouldTriggerFocus) {
                setAutoCancelDuration(AF_TIMEOUT_SEC, TimeUnit.SECONDS)
            }
        }.build()

        // Execute startFocusAndMetering via suspendCancellableCoroutine
        // (No ListenableFuture.await() extension in this project — use manual wrapping)
        return@withContext try {
            val focusResult = suspendCancellableCoroutine { cont ->
                val future = cameraControl.startFocusAndMetering(action)
                // Direct executor (Runnable::run) — no per-call thread/executor allocation
                future.addListener({
                    try {
                        cont.resume(future.get())
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                }, Runnable::run)

                cont.invokeOnCancellation { future.cancel(true) }
            }

            val opticalState = if (focusResult.isFocusSuccessful) {
                OpticalFocusState.FOCUSED
            } else {
                OpticalFocusState.NOT_FOCUSED
            }
            Log.i(TAG, "[v2.12.0-15] startFocusAndMetering: isFocusSuccessful=${focusResult.isFocusSuccessful}")

            FocusControllerResult(
                triggerSent = true,
                meteringPointView = meteringPoint,
                meteringRegionView = null,
                opticalResult = opticalState,
            )
        } catch (e: Exception) {
            Log.w(TAG, "[v2.12.0-15] startFocusAndMetering failed: ${e.message}")
            FocusControllerResult(
                triggerSent = false,
                meteringPointView = meteringPoint,
                meteringRegionView = null,
                opticalResult = OpticalFocusState.UNKNOWN,
            )
        }
    }

    /**
     * Resolve view-space metering point from MeteringAnchor + FocusTarget.
     * All coordinates are in view-space (rectView, leftEyeView, rightEyeView).
     */
    private fun resolveMeteringPoint(
        anchor: MeteringAnchor,
        target: FocusTarget?,
    ): PointF? {
        if (target == null && anchor != MeteringAnchor.NONE) {
            Log.w(TAG, "[v2.12.0-15] resolveMeteringPoint: no target for anchor=$anchor")
            return null
        }

        return when (anchor) {
            MeteringAnchor.BOTH_EYES_CENTER -> {
                val left = target?.leftEyeView
                val right = target?.rightEyeView
                when {
                    left != null && right != null ->
                        PointF((left.x + right.x) / 2f, (left.y + right.y) / 2f)
                    left != null -> left
                    right != null -> right
                    else -> target?.rectView?.let { PointF(it.centerX(), it.centerY()) }
                }
            }
            MeteringAnchor.SINGLE_EYE ->
                target?.leftEyeView ?: target?.rightEyeView
                    ?: target?.rectView?.let { PointF(it.centerX(), it.centerY()) }
            MeteringAnchor.UPPER_FACE ->
                target?.rectView?.let { PointF(it.centerX(), it.top + it.height() * 0.25f) }
            MeteringAnchor.FACE_CENTER,
            MeteringAnchor.TRACKED_REGION,
            MeteringAnchor.TAP_POINT ->
                target?.rectView?.let { PointF(it.centerX(), it.centerY()) }
            MeteringAnchor.NONE -> null
        }
    }
}
