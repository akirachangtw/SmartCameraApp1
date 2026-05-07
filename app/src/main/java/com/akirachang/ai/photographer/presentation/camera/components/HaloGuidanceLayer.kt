package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.composition.PtfAlignmentResult
import com.akirachang.ai.photographer.domain.composition.rules.EyeLineRule
import com.akirachang.ai.photographer.domain.config.AmbientConstants
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.CompositionResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.presentation.utils.NormalizedPoint
import com.akirachang.ai.photographer.presentation.utils.NormalizedRect
import com.akirachang.ai.photographer.presentation.utils.NormalizedToPixelTransformer
import com.akirachang.ai.photographer.presentation.utils.PixelRect
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// ── Test-tag constants (consumed by HaloGuidanceLayerTest) ─────────────────────
internal const val TAG_HALO_AMBER = "HaloGuidanceLayer_Amber"
// Alignment-state rendering probe — present when isAllAligned==true. Canvas is intentionally
// empty (no visual output); EdgeWarmthLayer + ShutterRing own the green feedback in this state.
internal const val TAG_HALO_GREEN = "HaloGuidanceLayer_Green"
// Null-result suppression probe — present when compositionResult==null (Constitution §6).
internal const val TAG_HALO_HIDDEN = "HaloGuidanceLayer_Hidden"
// PTF mode test tags
internal const val TAG_HALO_PTF_UNALIGNED = "HaloGuidanceLayer_PtfUnaligned"
internal const val TAG_HALO_PTF_ALIGNED = "HaloGuidanceLayer_PtfAligned"
internal const val TAG_HALO_PTF_NO_FACE = "HaloGuidanceLayer_PtfNoFace"

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

/** PTF Halo alpha values per SSOT補丁 §3.5. */
private const val PTF_HALO_UNALIGNED_ALPHA = 0.25f
private const val PTF_HALO_ALIGNED_ALPHA = 0.35f

/** PTF breathing animation spec per Strategic Brief Part D.3. */
private const val PTF_BREATH_ALPHA_MIN = 0.10f
private const val PTF_BREATH_ALPHA_MAX = 0.18f
private val PTF_BREATH_DURATION_MS = AmbientConstants.BREATHE_CYCLE_MS.toInt()

/** PTF gold→green color transition duration per SSOT補丁 §5. */
private val PTF_COLOR_TRANSITION_MS = AmbientConstants.TRANSITION_FAST_MS.toInt()

/** Bright arc offset in dp (gravitational field indicator). */
private const val PTF_BRIGHT_ARC_OFFSET_DP = 3f

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
 * PTF mode (isPtfMode == true): renders three-state PTF Halo driven by [ptfAlignmentResult]:
 * - NO_FACE (failedRules empty, isAligned false) → hidden
 * - Unaligned (failedRules non-empty) → gold ring + gravitational bright arc
 * - Aligned (isAligned true) → green ring + breathing rhythm
 *
 * Suppression rules (highest-priority, Cueshot Constitution §6):
 * - `compositionResult == null` (Normal mode) → fully transparent, nothing rendered.
 * - `compositionResult.isAllAligned == true` (Normal mode) → fully hidden.
 *
 * Update rate: 10fps throttle ([HALO_FRAME_INTERVAL_MS] = 100ms), decoupled from the
 * 60fps Compose recomposition cycle and CameraX hardware calls (SSOT §硬體頻率脫鉤).
 *
 * @param faces                Face landmark data from the latest ML Kit detection frame.
 * @param compositionResult    Aggregated Normal mode rule engine output. Null until first frame.
 * @param isPtfMode            True when camera is in PassToFriend mode.
 * @param ptfAlignmentResult   PTF rule engine output. Null ignored (defaults to NO_FACE behavior).
 * @param modifier             Should be [fillMaxSize] to cover the full preview.
 */
@Composable
fun HaloGuidanceLayer(
    faces: List<FaceLandmarkData>,
    compositionResult: CompositionResult?,
    isPtfMode: Boolean = false,
    ptfAlignmentResult: PtfAlignmentResult? = null,
    modifier: Modifier = Modifier,
) {
    // ── 10fps throttle state — declared unconditionally before any early return
    //    to satisfy Compose slot-table stability (Rule 14: remember + LaunchedEffect must
    //    be called on every recomposition regardless of branch outcome).
    var throttledFaces by remember { mutableStateOf<List<FaceLandmarkData>>(emptyList()) }
    var throttledResult by remember { mutableStateOf<CompositionResult?>(null) }
    var lastFrameMs by remember { mutableLongStateOf(0L) }
    var throttledPtfResult by remember { mutableStateOf<PtfAlignmentResult?>(null) }

    // ── PTF animation state — declared unconditionally per Rule 14 ──────────
    val effectivePtfResult = throttledPtfResult ?: ptfAlignmentResult
    val isPtfAligned = effectivePtfResult?.isAligned == true
    val ptfHaloColor by animateColorAsState(
        targetValue = if (isPtfAligned) HaloGreen else HaloAmber,
        animationSpec = tween(durationMillis = PTF_COLOR_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "ptfHaloColor",
    )

    // Breathing alpha — only animates when PTF aligned; otherwise held at unaligned alpha.
    val ptfInfiniteTransition = rememberInfiniteTransition(label = "ptfBreathing")
    val ptfBreathAlpha by ptfInfiniteTransition.animateFloat(
        initialValue = PTF_BREATH_ALPHA_MIN,
        targetValue = PTF_BREATH_ALPHA_MAX,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = PTF_BREATH_DURATION_MS, easing = FastOutSlowInEasing),
        ),
        label = "ptfBreathAlpha",
    )

    val density = LocalDensity.current
    val brightArcOffsetPx = with(density) { PTF_BRIGHT_ARC_OFFSET_DP.dp.toPx() }

    // ── Propagate face snapshot at 10fps cadence (hoisted above all early returns)
    LaunchedEffect(faces, compositionResult, ptfAlignmentResult) {
        val now = System.currentTimeMillis()
        if (now - lastFrameMs >= HALO_FRAME_INTERVAL_MS) {
            throttledFaces = faces
            throttledResult = compositionResult
            throttledPtfResult = ptfAlignmentResult
            lastFrameMs = now
        } else {
            delay(HALO_FRAME_INTERVAL_MS - (now - lastFrameMs))
            throttledFaces = faces
            throttledResult = compositionResult
            throttledPtfResult = ptfAlignmentResult
            lastFrameMs = System.currentTimeMillis()
        }
    }

    // ── PTF mode branch — entirely separate rendering path ───────────────────
    if (isPtfMode) {
        val ptfResult = effectivePtfResult

        // NO_FACE sentinel: failedRules empty AND not aligned → hidden
        if (ptfResult == null || (!ptfResult.isAligned && ptfResult.failedRules.isEmpty())) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().testTag(TAG_HALO_PTF_NO_FACE)
            )
            return
        }

        if (ptfResult.isAligned) {
            // State C: aligned — green breathing ring
            val breathAlpha = ptfBreathAlpha
            Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_PTF_ALIGNED)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension * 0.38f
                val strokePx = size.minDimension * HALO_STROKE_FRACTION
                drawCircle(
                    color = ptfHaloColor.copy(alpha = PTF_HALO_ALIGNED_ALPHA + breathAlpha * 0.1f),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokePx),
                )
            }
            return
        }

        // State B: unaligned with face — gold ring + gravitational bright arc
        Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_PTF_UNALIGNED)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.38f
            val strokePx = size.minDimension * HALO_STROKE_FRACTION

            // Base ring
            drawCircle(
                color = ptfHaloColor.copy(alpha = PTF_HALO_UNALIGNED_ALPHA),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = strokePx),
            )

            // Gravitational bright arc: offset direction indicates where subject should move
            val pxDx = ptfResult.offset.dx * size.width
            val pxDy = ptfResult.offset.dy * size.height
            if (pxDx != 0f || pxDy != 0f) {
                val angle = atan2(pxDy, pxDx)
                val brightCx = cx - cos(angle) * brightArcOffsetPx
                val brightCy = cy - sin(angle) * brightArcOffsetPx
                drawCircle(
                    color = ptfHaloColor.copy(alpha = PTF_HALO_UNALIGNED_ALPHA + 0.30f),
                    radius = radius * 0.25f,
                    center = Offset(brightCx, brightCy),
                )
            }
        }
        return
    }

    // ── Normal mode path below — zero changes from original ─────────────────

    // ── Constitution §6: null result → completely invisible ─────────────────
    if (compositionResult == null) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TAG_HALO_HIDDEN)
        )
        return
    }

    // ── State-mutex: isAllAligned → hide to avoid collision with breathing UI ─
    if (compositionResult.isAllAligned) {
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
 * Skipped silently if either eye landmark is absent.
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
