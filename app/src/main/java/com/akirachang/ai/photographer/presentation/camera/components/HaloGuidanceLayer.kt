package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import com.akirachang.ai.photographer.domain.composition.rules.EyeLineRule
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.presentation.utils.NormalizedPoint
import com.akirachang.ai.photographer.presentation.utils.NormalizedRect
import com.akirachang.ai.photographer.presentation.utils.NormalizedToPixelTransformer
import com.akirachang.ai.photographer.presentation.utils.PixelRect
import kotlinx.coroutines.delay

// ── Test-tag constants (consumed by HaloGuidanceLayerTest) ─────────────────────
internal const val TAG_HALO_AMBER = "HaloGuidanceLayer_Amber"
// Alignment-state rendering probe — present when isAllAligned==true. Canvas is intentionally
// empty (no visual output); EdgeWarmthLayer + ShutterRing own the green feedback in this state.
internal const val TAG_HALO_GREEN = "HaloGuidanceLayer_Green"
// Null-result suppression probe — present when compositionResult==null (Constitution §6).
internal const val TAG_HALO_HIDDEN = "HaloGuidanceLayer_Hidden"

/** Amber guide color per SSOT v3.3 — face Halo when not aligned. */
private val HaloAmber = Color(0xFFD4A030)

/** Aligned completion color per SSOT v3.3 §2.5 — redundant (handled by EdgeWarmthLayer / ShutterRing). */
private val HaloGreen = Color(0xFF50C864)

/** Halo ring stroke width as a fraction of the smaller canvas dimension. */
private const val HALO_STROKE_FRACTION = 0.006f

/** Gaze Halo outer padding beyond the face bounding box (normalized units). */
private const val GAZE_HALO_PADDING_NORM = 0.025f

/** EyeLine guide dash interval in pixels. */
private const val EYE_LINE_DASH_ON_PX = 12f
private const val EYE_LINE_DASH_OFF_PX = 8f

/** 10fps throttle interval in milliseconds (SSOT §硬體頻率脫鉤). */
private const val HALO_FRAME_INTERVAL_MS = 100L

/**
 * HaloGuidanceLayer — Cueshot v3.3 face-guidance Halo overlay.
 *
 * Renders two visual guidance signals over detected faces:
 *
 * 1. **GazeRule Halo** — amber ring (#D4A030) surrounding the face bounding box when
 *    `ruleScores["gaze"]?.isAligned == false`. Prompts the user to direct the subject's
 *    gaze toward the camera.
 *
 * 2. **EyeLineRule guide** — dashed amber line connecting left and right eye landmarks
 *    when `ruleScores["eye_line"]?.isAligned == false`. Visualises horizontal eye alignment
 *    so the user can tilt the phone to level the eye line.
 *
 * Suppression rules (highest-priority, Cueshot Constitution §6):
 * - `compositionResult == null` → fully transparent, nothing rendered.
 * - `compositionResult.isAllAligned == true` → fully hidden; avoids collision with the
 *   EdgeWarmthLayer breathing ring and ShutterButtonSection breathing circle.
 *
 * Update rate: 10fps throttle ([HALO_FRAME_INTERVAL_MS] = 100ms), decoupled from the
 * 60fps Compose recomposition cycle and CameraX hardware calls (SSOT §硬體頻率脫鉤).
 *
 * @param faces             Face landmark data from the latest ML Kit detection frame.
 *                          Must be pre-normalized to [0, 1] image space.
 * @param compositionResult Aggregated rule engine output. Null until first detection frame.
 * @param modifier          Should be [fillMaxSize] to cover the full preview.
 */
@Composable
fun HaloGuidanceLayer(
    faces: List<FaceLandmarkData>,
    compositionResult: CompositionResult?,
    modifier: Modifier = Modifier,
) {
    // ── 10fps throttle state — declared unconditionally before any early return
    //    to satisfy Compose slot-table stability (remember + LaunchedEffect must
    //    be called on every recomposition regardless of branch outcome).
    var throttledFaces by remember { mutableStateOf<List<FaceLandmarkData>>(emptyList()) }
    var throttledResult by remember { mutableStateOf<CompositionResult?>(null) }
    var lastFrameMs by remember { mutableLongStateOf(0L) }

    // ── Propagate face snapshot at 10fps cadence (hoisted above all early returns)
    //    Keeps the throttled snapshot current even when rendering is suppressed by
    //    isAllAligned or null guards — eliminates the 100ms stale-frame on state transition.
    LaunchedEffect(faces, compositionResult) {
        val now = System.currentTimeMillis()
        if (now - lastFrameMs >= HALO_FRAME_INTERVAL_MS) {
            throttledFaces = faces
            throttledResult = compositionResult
            lastFrameMs = now
        } else {
            delay(HALO_FRAME_INTERVAL_MS - (now - lastFrameMs))
            throttledFaces = faces
            throttledResult = compositionResult
            lastFrameMs = System.currentTimeMillis()
        }
    }

    // ── Constitution §6: null result → completely invisible ─────────────────
    if (compositionResult == null) {
        // Zero-size semantic probe for instrumented tests.
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TAG_HALO_HIDDEN)
        )
        return
    }

    // ── State-mutex: isAllAligned → hide to avoid collision with breathing UI ─
    if (compositionResult.isAllAligned) {
        // Green-aligned state: EdgeWarmthLayer + ShutterRing already provide feedback.
        // HaloGuidanceLayer must not render to prevent visual collision.
        Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_GREEN)) {
            // Intentionally empty — green alignment state is owned by EdgeWarmthLayer.
        }
        return
    }

    val activeResult = throttledResult ?: return
    val activeFaces = throttledFaces

    // ── Derive rule alignment flags ──────────────────────────────────────────
    val gazeNotAligned = activeResult.ruleScores[GazeRule.NAME]?.isAligned == false
    val eyeLineNotAligned = activeResult.ruleScores[EyeLineRule.NAME]?.isAligned == false

    // Nothing to draw if both rules are aligned or absent.
    if (!gazeNotAligned && !eyeLineNotAligned) return

    Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_AMBER)) {
        val transformer = NormalizedToPixelTransformer(
            screenWidth = size.width,
            screenHeight = size.height,
        )
        val strokePx = size.minDimension * HALO_STROKE_FRACTION

        activeFaces.forEach { face ->
            if (gazeNotAligned) {
                drawGazeHalo(face, transformer, strokePx)
            }
            if (eyeLineNotAligned) {
                drawEyeLineGuide(face, transformer, strokePx)
            }
        }
    }
}

/**
 * Draws an amber ring around the face bounding box with a small outward padding.
 * Signals to the user that the subject's gaze needs adjustment.
 */
private fun DrawScope.drawGazeHalo(
    face: FaceLandmarkData,
    transformer: NormalizedToPixelTransformer,
    strokePx: Float,
) {
    val box = face.boundingBoxNormalized
    // Expand bounding box by padding to draw halo ring slightly outside the face.
    val paddedRect = NormalizedRect(
        left = box.left - GAZE_HALO_PADDING_NORM,
        top = box.top - GAZE_HALO_PADDING_NORM,
        right = box.right + GAZE_HALO_PADDING_NORM,
        bottom = box.bottom + GAZE_HALO_PADDING_NORM,
    )
    val pixelRect: PixelRect = transformer.transformRect(paddedRect)

    val rectWidth = pixelRect.right - pixelRect.left
    val rectHeight = pixelRect.bottom - pixelRect.top
    if (rectWidth <= 0f || rectHeight <= 0f) return

    val centerX = pixelRect.left + rectWidth / 2f
    val centerY = pixelRect.top + rectHeight / 2f
    // Use the shorter axis as the halo radius so the circle fits within the bounding region.
    val radius = minOf(rectWidth, rectHeight) / 2f

    drawCircle(
        color = HaloAmber,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = strokePx),
    )
}

/**
 * Draws a dashed amber horizontal guide line through both eye landmarks.
 * Visualises the eye-line tilt so the user can level the phone.
 * Skipped silently if either eye landmark is absent (ML Kit classification failed).
 */
private fun DrawScope.drawEyeLineGuide(
    face: FaceLandmarkData,
    transformer: NormalizedToPixelTransformer,
    strokePx: Float,
) {
    val leftEyeRaw = face.leftEyeNormalized ?: return
    val rightEyeRaw = face.rightEyeNormalized ?: return

    val leftPixel = transformer.transformPoint(NormalizedPoint(leftEyeRaw.x, leftEyeRaw.y))
    val rightPixel = transformer.transformPoint(NormalizedPoint(rightEyeRaw.x, rightEyeRaw.y))

    drawLine(
        color = HaloAmber.copy(alpha = 0.75f),
        start = Offset(leftPixel.x, leftPixel.y),
        end = Offset(rightPixel.x, rightPixel.y),
        strokeWidth = strokePx,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(EYE_LINE_DASH_ON_PX, EYE_LINE_DASH_OFF_PX),
            phase = 0f,
        ),
    )
}
