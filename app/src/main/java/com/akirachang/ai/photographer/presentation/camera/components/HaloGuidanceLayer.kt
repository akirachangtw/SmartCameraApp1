package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.composition.PtfAlignmentResult
import com.akirachang.ai.photographer.domain.composition.rules.EyeLineRule
import com.akirachang.ai.photographer.domain.config.AmbientConstants
import com.akirachang.ai.photographer.domain.config.GuidanceStateConstants
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.akirachang.ai.photographer.domain.model.guidance.GuidanceState
import com.akirachang.ai.photographer.presentation.utils.NormalizedPoint
import com.akirachang.ai.photographer.presentation.utils.NormalizedRect
import com.akirachang.ai.photographer.presentation.utils.NormalizedToPixelTransformer
import com.akirachang.ai.photographer.presentation.utils.PixelRect
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

// ── Test-tag constants (consumed by HaloGuidanceLayerTest) ─────────────────────
internal const val TAG_HALO_AMBER = "HaloGuidanceLayer_Amber"
// Alignment-state rendering probe — present when isAllAligned==true. Canvas is intentionally
// empty (no visual output); PitchTractionLayer + ShutterRing own the green feedback in this state.
internal const val TAG_HALO_GREEN = "HaloGuidanceLayer_Green"
// Null-result suppression probe — present when compositionResult==null (Constitution §6).
internal const val TAG_HALO_HIDDEN = "HaloGuidanceLayer_Hidden"
// PTF mode test tags
internal const val TAG_HALO_PTF_UNALIGNED = "HaloGuidanceLayer_PtfUnaligned"
internal const val TAG_HALO_PTF_ALIGNED = "HaloGuidanceLayer_PtfAligned"
internal const val TAG_HALO_PTF_NO_FACE = "HaloGuidanceLayer_PtfNoFace"

internal val PtfHaloCenterXKey = SemanticsPropertyKey<Float>("PtfHaloCenterX")
internal val PtfHaloCenterYKey = SemanticsPropertyKey<Float>("PtfHaloCenterY")
internal val PtfHaloRadiusKey = SemanticsPropertyKey<Float>("PtfHaloRadius")

internal var SemanticsPropertyReceiver.ptfHaloCenterX by PtfHaloCenterXKey
internal var SemanticsPropertyReceiver.ptfHaloCenterY by PtfHaloCenterYKey
internal var SemanticsPropertyReceiver.ptfHaloRadius by PtfHaloRadiusKey

/** Amber guide color per SSOT v3.3 — face Halo when not aligned. */
private val HaloAmber = Color(0xFFD4A030)

/** Aligned completion color per SSOT v4.0 §2.5 — redundant (handled by PitchTractionLayer / ShutterRing). */
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

/** PTF Halo alpha values per SSOT §2.5. */
private const val PTF_HALO_UNALIGNED_ALPHA = 0.25f

/** PTF breathing animation spec per Strategic Brief Part D.3. */
private const val PTF_BREATH_ALPHA_MIN = 0.10f
private const val PTF_BREATH_ALPHA_MAX = 0.18f
private val PTF_BREATH_DURATION_MS = AmbientConstants.BREATHE_CYCLE_MS.toInt()

/** PTF gold→green color transition duration per SSOT補丁 §5. */
private val PTF_COLOR_TRANSITION_MS = AmbientConstants.TRANSITION_FAST_MS.toInt()

/** Bright arc offset in dp (gravitational field indicator). */
private const val PTF_BRIGHT_ARC_OFFSET_DP = 3f

/**
 * Traction dot breathing (breathe-dot) — SSOT v4.0.7 §2.5 L493. This is the dot's OWN
 * scale/opacity envelope; the aligned RING breathe ([normalBreathAlpha]) never feeds the
 * dot alpha path (R-T13 V-3 decoupling).
 */
private const val BREATHE_DOT_SCALE_MIN = 0.9f
private const val BREATHE_DOT_SCALE_MAX = 1.1f
private const val BREATHE_DOT_OPACITY_MIN = 0.7f
private const val BREATHE_DOT_OPACITY_MAX = 1.0f
private const val BREATHE_DOT_CYCLE_MS = 1800

/** Traction dot V-curve dist domain (px): midpoint splits amber↑ / green↓, max clamps offset. SSOT §2.5 L461. */
private const val HALO_DOT_DIST_MID = 50f
private const val HALO_DOT_DIST_MAX = 100f

/** Traction dot offset boundary inset (px) — keeps the dot inside the halo ring. SSOT §2.5 L455. */
private const val HALO_DOT_BOUNDARY_INSET_PX = 2f

/**
 * HaloGuidanceLayer — STATE-CONTRACT-01 six-state guidance Halo overlay.
 *
 * Normal mode rendering is driven exclusively by [guidanceState] (CameraViewModel authority):
 * - NoFace / FacesDetectedNoneEligible → hidden
 * - SubjectStabilizing → amber halo at 50% alpha, no EyeLine guide
 * - GuidanceActive → amber halo at [GuidanceStateConstants.HALO_ACTIVE_ALPHA] + EyeLine guide
 * - Aligned → green breathing halo ([GuidanceStateConstants.HALO_ALIGNED_ALPHA_MIN]→[MAX])
 * - Suppressed → muted amber halo at 50% alpha (frozen guidance)
 *
 * PTF mode (isPtfMode == true): unchanged three-state PTF branch driven by [ptfAlignmentResult].
 *
 * Update rate: 10fps throttle ([HALO_FRAME_INTERVAL_MS] = 100ms), decoupled from 60fps Compose.
 *
 * @param guidanceState        Six-state contract from CameraViewModel — sole Normal mode driver.
 * @param faces                Face landmark data from the latest ML Kit detection frame.
 * @param isPtfMode            True when camera is in PassToFriend mode.
 * @param ptfAlignmentResult   PTF rule engine output. Null ignored (defaults to NO_FACE behavior).
 * @param targetPosition       Traction dot goal in canvas px (GuidanceActive). Null = no producer wired
 *                             yet (GAP-06-TARGET) → dot homes at dist=0 (SSOT §2.5 L440-441). Permanent
 *                             nullable contract; a future producer ticket only supplies the value.
 * @param modifier             Should be [fillMaxSize] to cover the full preview.
 */
@Composable
fun HaloGuidanceLayer(
    guidanceState: GuidanceState,
    faces: List<FaceLandmarkData>,
    isPtfMode: Boolean = false,
    ptfAlignmentResult: PtfAlignmentResult? = null,
    targetPosition: Offset? = null,
    modifier: Modifier = Modifier,
) {
    // ── All remember/LaunchedEffect/animation state MUST be declared unconditionally
    //    before any early return — Compose slot-table stability Rule 14.

    // 10fps throttle state
    var throttledFaces by remember { mutableStateOf<List<FaceLandmarkData>>(emptyList()) }
    var throttledGuidanceState by remember { mutableStateOf<GuidanceState>(GuidanceState.NoFace) }
    var lastFrameMs by remember { mutableLongStateOf(0L) }
    var throttledPtfResult by remember { mutableStateOf<PtfAlignmentResult?>(null) }

    // PTF animation state
    val effectivePtfResult = throttledPtfResult ?: ptfAlignmentResult
    val isPtfAligned = effectivePtfResult?.isAligned == true
    val ptfHaloColor by animateColorAsState(
        targetValue = if (isPtfAligned) HaloGreen else HaloAmber,
        animationSpec = tween(durationMillis = PTF_COLOR_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "ptfHaloColor",
    )
    val ptfInfiniteTransition = rememberInfiniteTransition(label = "ptfBreathing")
    val ptfBreathAlpha by ptfInfiniteTransition.animateFloat(
        initialValue = PTF_BREATH_ALPHA_MIN,
        targetValue = PTF_BREATH_ALPHA_MAX,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = PTF_BREATH_DURATION_MS, easing = FastOutSlowInEasing),
        ),
        label = "ptfBreathAlpha",
    )

    // Normal Aligned breathing animation
    val normalInfiniteTransition = rememberInfiniteTransition(label = "normalAlignedBreathing")
    val normalBreathAlpha by normalInfiniteTransition.animateFloat(
        initialValue = GuidanceStateConstants.HALO_ALIGNED_ALPHA_MIN,
        targetValue = GuidanceStateConstants.HALO_ALIGNED_ALPHA_MAX,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = PTF_BREATH_DURATION_MS, easing = FastOutSlowInEasing),
        ),
        label = "normalAlignedBreathAlpha",
    )

    // Traction dot breathe (breathe-dot) — own envelope, decoupled from ring breathe (R-T13 V-3).
    val dotBreatheTransition = rememberInfiniteTransition(label = "breatheDot")
    val dotScale by dotBreatheTransition.animateFloat(
        initialValue = BREATHE_DOT_SCALE_MIN,
        targetValue = BREATHE_DOT_SCALE_MAX,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = BREATHE_DOT_CYCLE_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotScale",
    )
    val dotOpacity by dotBreatheTransition.animateFloat(
        initialValue = BREATHE_DOT_OPACITY_MIN,
        targetValue = BREATHE_DOT_OPACITY_MAX,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = BREATHE_DOT_CYCLE_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotOpacity",
    )

    val density = LocalDensity.current
    val brightArcOffsetPx = with(density) { PTF_BRIGHT_ARC_OFFSET_DP.dp.toPx() }
    val dotDiameterPx = with(density) { AmbientConstants.HALO_DOT_SIZE_DP.dp.toPx() }

    // Propagate snapshot at 10fps cadence (hoisted above all early returns)
    LaunchedEffect(faces, guidanceState, ptfAlignmentResult) {
        val now = System.currentTimeMillis()
        if (now - lastFrameMs >= HALO_FRAME_INTERVAL_MS) {
            throttledFaces = faces
            throttledGuidanceState = guidanceState
            throttledPtfResult = ptfAlignmentResult
            lastFrameMs = now
        } else {
            delay(HALO_FRAME_INTERVAL_MS - (now - lastFrameMs))
            throttledFaces = faces
            throttledGuidanceState = guidanceState
            throttledPtfResult = ptfAlignmentResult
            lastFrameMs = System.currentTimeMillis()
        }
    }

    // ── PTF mode branch — entirely separate rendering path, unchanged ────────
    if (isPtfMode) {
        val ptfResult = effectivePtfResult
        val ptfFaces = if (throttledFaces.isNotEmpty()) throttledFaces else faces
        val primaryPtfFace = ptfFaces.maxByOrNull { face ->
            val box = face.boundingBoxNormalized
            box.width() * box.height()
        }

        if (ptfResult == null || primaryPtfFace == null ||
            (!ptfResult.isAligned && ptfResult.failedRules.isEmpty())) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().testTag(TAG_HALO_PTF_NO_FACE)
            )
            return
        }

        val ptfBbox = primaryPtfFace.boundingBoxNormalized
        if (ptfBbox.left < 0f || ptfBbox.top < 0f ||
            ptfBbox.right > 1f || ptfBbox.bottom > 1f ||
            ptfBbox.width() <= 0f || ptfBbox.height() <= 0f) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().testTag(TAG_HALO_PTF_NO_FACE)
            )
            return
        }
        val ptfCenterXNorm = (ptfBbox.left + ptfBbox.right) / 2f
        val ptfCenterYNorm = (ptfBbox.top + ptfBbox.bottom) / 2f
        val ptfRadiusNorm = max(ptfBbox.width(), ptfBbox.height()) * 0.6f
        val ptfSemantics = Modifier.semantics {
            ptfHaloCenterX = ptfCenterXNorm
            ptfHaloCenterY = ptfCenterYNorm
            ptfHaloRadius = ptfRadiusNorm
        }

        if (ptfResult.isAligned) {
            val breathAlpha = ptfBreathAlpha
            Canvas(modifier = modifier.fillMaxSize().then(ptfSemantics).testTag(TAG_HALO_PTF_ALIGNED)) {
                val cx = ptfCenterXNorm * size.width
                val cy = ptfCenterYNorm * size.height
                val radius = max(ptfBbox.width() * size.width, ptfBbox.height() * size.height) * 0.6f
                val strokePx = size.minDimension * HALO_STROKE_FRACTION
                drawCircle(
                    color = ptfHaloColor.copy(alpha = breathAlpha),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokePx),
                )
            }
            return
        }

        Canvas(modifier = modifier.fillMaxSize().then(ptfSemantics).testTag(TAG_HALO_PTF_UNALIGNED)) {
            val cx = ptfCenterXNorm * size.width
            val cy = ptfCenterYNorm * size.height
            val radius = max(ptfBbox.width() * size.width, ptfBbox.height() * size.height) * 0.6f
            val strokePx = size.minDimension * HALO_STROKE_FRACTION
            drawCircle(
                color = ptfHaloColor.copy(alpha = PTF_HALO_UNALIGNED_ALPHA),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = strokePx),
            )
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

    // ── Normal mode — six-state GuidanceState rendering ──────────────────────
    val activeFaces = throttledFaces
    val activeGuidanceState = throttledGuidanceState

    when (activeGuidanceState) {
        GuidanceState.NoFace,
        GuidanceState.FacesDetectedNoneEligible -> {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().testTag(TAG_HALO_HIDDEN)
            )
        }
        GuidanceState.SubjectStabilizing,
        is GuidanceState.Suppressed -> {
            // Muted face-following halo — no directional arc
            if (activeFaces.isNotEmpty()) {
                Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_AMBER)) {
                    val strokePx = size.minDimension * HALO_STROKE_FRACTION
                    activeFaces.forEach { face ->
                        drawGuidanceHalo(face, strokePx, HaloAmber.copy(alpha = GuidanceStateConstants.HALO_ACTIVE_ALPHA * 0.5f))
                    }
                }
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize().testTag(TAG_HALO_HIDDEN)
                )
            }
        }
        GuidanceState.GuidanceActive -> {
            Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_AMBER)) {
                val transformer = NormalizedToPixelTransformer(size.width, size.height)
                val strokePx = size.minDimension * HALO_STROKE_FRACTION
                activeFaces.forEach { face ->
                    drawGuidanceHalo(face, strokePx, HaloAmber.copy(alpha = GuidanceStateConstants.HALO_ACTIVE_ALPHA))
                    drawEyeLineGuide(face, transformer, strokePx)
                }
            }
            // Single-subject traction dot on the primary (largest bbox) face — drawn in a dedicated
            // Canvas so the ring path above stays untouched. Multi-face group rendering is §2.5.2
            // territory (out of scope). SSOT §2.5 L439-479.
            val primaryFace = activeFaces.maxByOrNull { face ->
                val box = face.boundingBoxNormalized
                box.width() * box.height()
            }
            if (primaryFace != null) {
                Canvas(modifier = modifier.fillMaxSize()) {
                    drawTractionDot(primaryFace, targetPosition, dotDiameterPx, dotScale, dotOpacity)
                }
            }
        }
        GuidanceState.Aligned -> {
            val breathAlpha = normalBreathAlpha
            if (activeFaces.isNotEmpty()) {
                Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_GREEN)) {
                    val strokePx = size.minDimension * HALO_STROKE_FRACTION
                    activeFaces.forEach { face ->
                        drawGuidanceHalo(face, strokePx, HaloGreen.copy(alpha = breathAlpha))
                    }
                }
            } else {
                Canvas(modifier = modifier.fillMaxSize().testTag(TAG_HALO_GREEN)) {}
            }
        }
    }
}

/**
 * Draws a guidance ring around the face bounding box.
 * radius = max(bboxW, bboxH) * [GuidanceStateConstants.HALO_RADIUS_SCALE] per STATE-CONTRACT-01 AI-4.
 */
private fun DrawScope.drawGuidanceHalo(
    face: FaceLandmarkData,
    strokePx: Float,
    color: Color,
) {
    val box = face.boundingBoxNormalized
    if (box.left < 0f || box.top < 0f || box.right > 1f || box.bottom > 1f) return
    val bboxWPx = box.width() * size.width
    val bboxHPx = box.height() * size.height
    if (bboxWPx <= 0f || bboxHPx <= 0f) return
    val centerX = (box.left + box.right) / 2f * size.width
    val centerY = (box.top + box.bottom) / 2f * size.height
    val radius = max(bboxWPx, bboxHPx) * GuidanceStateConstants.HALO_RADIUS_SCALE
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = strokePx),
    )
}

/**
 * Draws a dashed amber horizontal guide line through both eye landmarks.
 * Skipped silently if either eye landmark is absent (LANDMARK_NONE).
 * Under LANDMARK_NONE, EyeLine visual overlay may be unavailable/degraded, but semantic
 * fallback scoring still participates.
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

/**
 * Traction dot render spec — pure geometry + V-curve output of [computeTractionDot].
 * Breathe ([dotScale]/[dotOpacity]) is applied at draw time, NOT here (R-T13 V-3 decoupling).
 */
internal data class TractionDotSpec(
    val centerX: Float,
    val centerY: Float,
    val color: Color,
    val alpha: Float,
    val homeScale: Float,
    val dist: Float,
    val angle: Float,
)

/**
 * Computes the traction dot geometry and three-segment V-curve color per SSOT v4.0.7 §2.5 L439-466.
 *
 * [targetPosition] null → dx=dy=0 → dist=0 → home: green at peak alpha, [AmbientConstants.HALO_DOT_ALIGNED_SCALE].
 * Direction = atan2(dy, dx); offset = clamped maxOffset scaled by dist/max, keeping the dot inside the ring.
 * V-curve: dist>mid → amber rising to peak; dist∈(0,mid] → green rising to peak toward home; dist=0 → green peak.
 */
internal fun computeTractionDot(
    haloCx: Float,
    haloCy: Float,
    bboxSizePx: Float,
    dotDiameterPx: Float,
    targetPosition: Offset?,
): TractionDotSpec {
    val dx = (targetPosition?.x ?: haloCx) - haloCx
    val dy = (targetPosition?.y ?: haloCy) - haloCy
    val angle = atan2(dy, dx)
    val dist = hypot(dx, dy).coerceIn(0f, HALO_DOT_DIST_MAX)
    val maxOffset = max(0f, (bboxSizePx - dotDiameterPx) / 2f - HALO_DOT_BOUNDARY_INSET_PX)
    val dotOffset = maxOffset * (dist / HALO_DOT_DIST_MAX)
    val dotCx = haloCx + cos(angle) * dotOffset
    val dotCy = haloCy + sin(angle) * dotOffset
    val span = AmbientConstants.HALO_DOT_ALPHA_PEAK - AmbientConstants.HALO_DOT_ALPHA_VALLEY
    val (color, alpha, homeScale) = when {
        dist > HALO_DOT_DIST_MID ->
            Triple(HaloAmber, AmbientConstants.HALO_DOT_ALPHA_VALLEY + ((dist - HALO_DOT_DIST_MID) / HALO_DOT_DIST_MID) * span, 1f)
        dist > 0f ->
            Triple(HaloGreen, AmbientConstants.HALO_DOT_ALPHA_VALLEY + (1f - dist / HALO_DOT_DIST_MID) * span, 1f)
        else ->
            Triple(HaloGreen, AmbientConstants.HALO_DOT_ALPHA_PEAK, AmbientConstants.HALO_DOT_ALIGNED_SCALE)
    }
    return TractionDotSpec(dotCx, dotCy, color, alpha, homeScale, dist, angle)
}

/**
 * Draws the single traction dot inside the primary subject's halo (SSOT v4.0.7 §2.5 L439-479).
 * Geometry/color come from [computeTractionDot]; [dotScale]/[dotOpacity] are the breathe-dot envelope.
 * Invalid bbox geometry (out of [0,1] or non-positive size) → no draw, matching [drawGuidanceHalo].
 */
private fun DrawScope.drawTractionDot(
    face: FaceLandmarkData,
    targetPosition: Offset?,
    dotDiameterPx: Float,
    dotScale: Float,
    dotOpacity: Float,
) {
    val box = face.boundingBoxNormalized
    if (box.left < 0f || box.top < 0f || box.right > 1f || box.bottom > 1f) return
    val bboxWPx = box.width() * size.width
    val bboxHPx = box.height() * size.height
    if (bboxWPx <= 0f || bboxHPx <= 0f) return
    val haloCx = (box.left + box.right) / 2f * size.width
    val haloCy = (box.top + box.bottom) / 2f * size.height
    val spec = computeTractionDot(haloCx, haloCy, max(bboxWPx, bboxHPx), dotDiameterPx, targetPosition)
    val dotRadius = (dotDiameterPx / 2f) * dotScale * spec.homeScale
    if (dotRadius <= 0f) return
    val center = Offset(spec.centerX, spec.centerY)
    drawCircle(
        brush = Brush.radialGradient(
            AmbientConstants.HALO_DOT_GRADIENT_CORE_STOP to spec.color.copy(alpha = spec.alpha * dotOpacity),
            AmbientConstants.HALO_DOT_GRADIENT_EDGE_STOP to Color.Transparent,
            center = center,
            radius = dotRadius,
        ),
        radius = dotRadius,
        center = center,
    )
}
