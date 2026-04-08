package com.akirachang.ai.photographer.presentation.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akirachang.ai.photographer.domain.model.focus.FocusUiState
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.model.focus.OverlayColor
import com.akirachang.ai.photographer.domain.model.focus.OverlayModel
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState
import kotlinx.coroutines.delay

// ── SSOT: All timing, sizes, and colors MUST reference FocusTokens ──────────
// Violation of this contract = architecture pollution (see devspec/feature_record_Backlog.md §M/§N/§O).
internal object FocusTokens {
    // Cueshot D0: Legacy blue palette retired. Ambient UI palette: Amber / Golden / Green.
    val CueshotAmber = Color(0xFFD4A030)   // #D4A030 — guiding / unaligned
    val CueshotGreen = Color(0xFF50C864)   // #50C864 — aligned / confirmed
    val GoldenFocus  = Color(0xFFFFD700)   // lock indicator (unchanged)

    // Sizes (dp)
    val SearchingSize = 180.dp
    val TrackingSize = 160.dp
    val LockedSize = 150.dp

    // Animation timing (ms)
    const val CLAMP_DURATION = 350
    const val DWELL_DELAY = 1000L
    const val DWELL_FADE_DURATION = 500
    const val COLOR_TRANSITION_MS = 200   // Yellow → Cyan confirmed-target transition (v2.12.0-18)

    // Stroke widths (dp) — dwell active vs faded
    val OUTER_STROKE_ACTIVE = 4.dp
    val OUTER_STROKE_FADED  = 1.5.dp
    val INNER_STROKE_ACTIVE = 2.dp
    val INNER_STROKE_FADED  = 1.dp
}

/**
 * FocusIndicatorOverlay — Professional focus visual layer.
 *
 * Implements the three canonical focus states defined in FOCUS_INDICATOR_COMPOSE_SPEC.md:
 *   1. Searching  — 3-layer static concentric rings (Alpha 0.2), no rotation on base rings.
 *   2. Tracking   — Mechanical clamp tween (3-phase, 350ms) + dwell fade to 0.4 after 1000ms.
 *   3. Locked     — Static golden frame (GoldenFocus, 6dp corners, 15% fill). Zero animation.
 *
 * SSOT Route: This composable is the ACTIVE CameraScreen focus path (v2.12.0-19 wiring complete).
 *   FocusOverlay.kt has been deleted (焦土政策 — zero call sites after wire-over).
 *   This file is now the single focus rendering truth — do not re-introduce FocusOverlay.
 *
 * Performance rule: all animated State reads are deferred into the Canvas draw lambda
 * (draw phase) via Animatable.value — no Composition-phase state reads on hot path.
 */
/**
 * Entry point for CameraScreen wiring.
 *
 * Accepts a [StateFlow<FocusUiState>] and collects it **internally** via
 * [collectAsStateWithLifecycle]. This isolates all focus coordinate recompositions
 * inside this composable — CameraScreen is never recomposed by focus updates.
 *
 * v2.12.0-19: Local recomposition defense — do NOT change this signature back to
 * receiving raw (overlayModel, visualTargetState) at the call site.
 */
@Composable
fun FocusIndicatorOverlay(
    focusUiStateFlow: StateFlow<FocusUiState>,
    modifier: Modifier = Modifier,
) {
    val focusUiState by focusUiStateFlow.collectAsStateWithLifecycle()
    val overlayModel = focusUiState.overlayModel
    val visualTargetState = focusUiState.visualTargetState

    // Guard: render nothing when model absent or state is None.
    if (overlayModel == null) return
    if (visualTargetState == VisualTargetState.None) return

    // ── State classification ─────────────────────────────────────────────────
    val isLocked = overlayModel.showLockIcon || overlayModel.primaryColor == OverlayColor.YELLOW
    val hasPrimaryBox = overlayModel.primaryBox != null
    val isSearching = !hasPrimaryBox || visualTargetState == VisualTargetState.Candidate

    // ── Color: Yellow → Cyan transition 200ms ────────────────────────────────
    val colorTarget = when (overlayModel.primaryColor) {
        OverlayColor.CYAN   -> FocusTokens.CueshotAmber
        OverlayColor.YELLOW -> FocusTokens.GoldenFocus
    }
    val animatedColor = animateColorAsState(
        targetValue = colorTarget,
        animationSpec = tween(FocusTokens.COLOR_TRANSITION_MS),
        label = "indicatorColor",
    )

    // ── Alpha lifecycle (dwell fade from 1.0 → 0.4 after DWELL_DELAY) ────────
    var alphaTarget by remember { mutableFloatStateOf(1f) }
    val animatedAlpha = animateFloatAsState(
        targetValue = alphaTarget,
        animationSpec = tween(FocusTokens.DWELL_FADE_DURATION),
        label = "indicatorAlpha",
    )

    // ── Dwell stroke thinning (outer 4dp → 1.5dp, inner 2dp → 1dp) ──────────
    var isDwell by remember { mutableStateOf(false) }
    val outerStrokeDp = animateDpAsState(
        targetValue = if (isDwell) FocusTokens.OUTER_STROKE_FADED else FocusTokens.OUTER_STROKE_ACTIVE,
        animationSpec = tween(FocusTokens.DWELL_FADE_DURATION),
        label = "outerStroke",
    )
    val innerStrokeDp = animateDpAsState(
        targetValue = if (isDwell) FocusTokens.INNER_STROKE_FADED else FocusTokens.INNER_STROKE_ACTIVE,
        animationSpec = tween(FocusTokens.DWELL_FADE_DURATION),
        label = "innerStroke",
    )

    // ── Mechanical clamp animation (Animatable — re-triggerable keyframes) ───
    val clampScale = remember { Animatable(1f) }

    // ── Visual lifecycle state machine ────────────────────────────────────────
    LaunchedEffect(visualTargetState, overlayModel.primaryStableId) {
        when (visualTargetState) {
            VisualTargetState.VisualLocked -> {
                isDwell = false
                alphaTarget = 1f
                // Phase 1-3: Mechanical clamp 350ms keyframes
                clampScale.snapTo(1f)
                clampScale.animateTo(
                    targetValue = 1f,
                    animationSpec = keyframes {
                        durationMillis = FocusTokens.CLAMP_DURATION
                        // Phase 1: Snap clamp (0 → 150ms)
                        1.00f at 0   using FastOutLinearInEasing
                        // Phase 2: Rebound overshoot (150 → 270ms)
                        0.75f at 150 using FastOutSlowInEasing
                        // Phase 3: Settle (270 → 350ms)
                        1.20f at 270 using LinearOutSlowInEasing
                        1.00f at FocusTokens.CLAMP_DURATION
                    },
                )
                // Dwell: sustain 1000ms then fade to 0.4 (LaunchedEffect auto-cancels on key change)
                delay(FocusTokens.DWELL_DELAY)
                isDwell = true
                alphaTarget = 0.4f
            }
            VisualTargetState.TrackingStable -> {
                isDwell = false
                alphaTarget = 1f
            }
            VisualTargetState.TrackingFade -> {
                isDwell = true
                alphaTarget = 0.4f
            }
            VisualTargetState.Candidate -> {
                isDwell = false
                alphaTarget = 0.6f
            }
            VisualTargetState.Lost -> {
                isDwell = false
                alphaTarget = 0.2f
            }
            VisualTargetState.None -> { /* guarded at top */ }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Defer all animated reads to draw phase — prevents Composition-phase invalidation.
        val alpha      = animatedAlpha.value
        val color      = animatedColor.value
        val scale      = clampScale.value
        val outerStrokePx = outerStrokeDp.value.toPx()
        val innerStrokePx = innerStrokeDp.value.toPx()

        when {
            // ── State 3: AE/AF Locked — absolute static, no animation ────────
            isLocked && hasPrimaryBox -> {
                overlayModel.primaryBox?.let { rect ->
                    drawLockedFrame(
                        cx = rect.centerX(), cy = rect.centerY(),
                        w = rect.width(), h = rect.height(),
                        alpha = alpha,
                    )
                }
            }

            // ── State 1: Searching — 3-layer static concentric rings ──────────
            isSearching -> {
                val centerOffset = overlayModel.primaryBox?.let { box ->
                    Offset(box.centerX(), box.centerY())
                } ?: Offset(size.width / 2f, size.height / 2f)
                drawSearchingBase(
                    centerOffset = centerOffset,
                    radius = FocusTokens.SearchingSize.toPx() / 2f,
                    alpha = alpha,
                    color = color,
                )
            }

            // ── State 2: Tracking / Success — mechanical clamp + dwell ────────
            else -> {
                overlayModel.primaryBox?.let { rect ->
                    drawTrackingFrame(
                        cx = rect.centerX(), cy = rect.centerY(),
                        w = rect.width() * scale, h = rect.height() * scale,
                        alpha = alpha, color = color,
                        outerStroke = outerStrokePx,
                        innerStroke = innerStrokePx,
                    )
                }
            }
        }

        // ── Secondary boxes: corner [ ] brackets, fixed 0.3 alpha ────────────
        // Primary monopoly rule (§O): secondaries never animate, always Cyan at 0.3.
        val secondaryColor = FocusTokens.CueshotAmber.copy(alpha = 0.3f)
        val bracketSize    = 10.dp.toPx()
        val bracketStroke  = 1.5.dp.toPx()
        overlayModel.secondaryBoxes.forEach { rect ->
            drawLine(secondaryColor, Offset(rect.left, rect.top + bracketSize),  Offset(rect.left,  rect.top),              bracketStroke)
            drawLine(secondaryColor, Offset(rect.left, rect.top),                Offset(rect.left + bracketSize, rect.top),  bracketStroke)
            drawLine(secondaryColor, Offset(rect.right - bracketSize, rect.top), Offset(rect.right, rect.top),               bracketStroke)
            drawLine(secondaryColor, Offset(rect.right, rect.top),               Offset(rect.right, rect.top + bracketSize), bracketStroke)
            drawLine(secondaryColor, Offset(rect.left,  rect.bottom - bracketSize), Offset(rect.left,  rect.bottom),              bracketStroke)
            drawLine(secondaryColor, Offset(rect.left,  rect.bottom),               Offset(rect.left + bracketSize, rect.bottom), bracketStroke)
            drawLine(secondaryColor, Offset(rect.right - bracketSize, rect.bottom), Offset(rect.right, rect.bottom),              bracketStroke)
            drawLine(secondaryColor, Offset(rect.right, rect.bottom - bracketSize), Offset(rect.right, rect.bottom),              bracketStroke)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DrawScope extensions — strictly internal, SSOT values only, no hardcoded magic
// ─────────────────────────────────────────────────────────────────────────────

/**
 * State 1: Searching base — 3-layer static concentric rings.
 * Outer / inner rings: thin (1.5dp). Middle ring: chunky dash (14dp, 8dp/6dp intervals).
 * Middle ring rotated −90° so first dash starts at 12 o'clock.
 * All rings rendered at alpha × 0.2 (ambient presence, not dominant).
 */
private fun DrawScope.drawSearchingBase(
    centerOffset: Offset,
    radius: Float,
    alpha: Float,
    color: Color,
) {
    val ringColor = color.copy(alpha = alpha * 0.2f)
    val thinStroke = 1.5.dp.toPx()

    // 1. Outer thin ring — radius = SearchingSize/2 × 0.92 (= size.width × 0.46 in 180dp canvas)
    drawCircle(
        color = ringColor,
        radius = radius * 0.92f,
        center = centerOffset,
        style = Stroke(width = thinStroke),
    )

    // 2. Middle chunky dash ring — rotated −90° for 12 o'clock start (absolute, no spin)
    rotate(degrees = -90f, pivot = centerOffset) {
        drawCircle(
            color = ringColor,
            radius = radius * 0.78f,
            center = centerOffset,
            style = Stroke(
                width = 14.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                    phase = 0f,
                ),
            ),
        )
    }

    // 3. Inner thin ring
    drawCircle(
        color = ringColor,
        radius = radius * 0.64f,
        center = centerOffset,
        style = Stroke(width = thinStroke),
    )
}

/**
 * State 2: Tracking frame — inner fine rect + outer 4-corner L-brackets.
 * Scale is applied by caller (clamp animation × center-preserving).
 * Dwell: outerStroke 4dp → 1.5dp, innerStroke 2dp → 1dp, alpha 1.0 → 0.4.
 */
private fun DrawScope.drawTrackingFrame(
    cx: Float, cy: Float,
    w: Float, h: Float,
    alpha: Float,
    color: Color,
    outerStroke: Float,
    innerStroke: Float,
) {
    val left   = cx - w / 2f
    val top    = cy - h / 2f
    val right  = cx + w / 2f
    val bottom = cy + h / 2f
    val drawColor = color.copy(alpha = alpha)
    val innerPad = 16.dp.toPx()

    // 1. Inner fine rectangle (Focusing.png equivalent)
    drawRect(
        color = drawColor,
        topLeft = Offset(left + innerPad, top + innerPad),
        size = Size(w - innerPad * 2f, h - innerPad * 2f),
        style = Stroke(width = innerStroke),
    )

    // 2. Outer four-corner L-brackets (Four-corner-frame.png equivalent)
    val cornerLen = 32.dp.toPx()
    val path = Path().apply {
        // Top-left
        moveTo(left, top + cornerLen); lineTo(left, top); lineTo(left + cornerLen, top)
        // Top-right
        moveTo(right - cornerLen, top); lineTo(right, top); lineTo(right, top + cornerLen)
        // Bottom-right
        moveTo(right, bottom - cornerLen); lineTo(right, bottom); lineTo(right - cornerLen, bottom)
        // Bottom-left
        moveTo(left + cornerLen, bottom); lineTo(left, bottom); lineTo(left, bottom - cornerLen)
    }
    drawPath(path = path, color = drawColor, style = Stroke(width = outerStroke, join = StrokeJoin.Miter))
}

/**
 * State 3: AE/AF Locked frame — absolute static, golden, no animation.
 * Inner rect: 15% GoldenFocus fill + 2dp border.
 * Outer corners: 6dp thick L-brackets (maximum authority cue).
 */
private fun DrawScope.drawLockedFrame(
    cx: Float, cy: Float,
    w: Float, h: Float,
    alpha: Float,
) {
    val color  = FocusTokens.GoldenFocus.copy(alpha = alpha)
    val left   = cx - w / 2f
    val top    = cy - h / 2f
    val right  = cx + w / 2f
    val bottom = cy + h / 2f
    val innerPad = 12.dp.toPx()
    val innerLeft  = left + innerPad
    val innerTop   = top + innerPad
    val innerSize  = Size(w - innerPad * 2f, h - innerPad * 2f)

    // 1. 15% semi-transparent inner fill (confirms hard-locked target)
    drawRect(
        color = FocusTokens.GoldenFocus.copy(alpha = 0.15f),
        topLeft = Offset(innerLeft, innerTop),
        size = innerSize,
    )

    // 2. Inner 2dp golden border
    drawRect(
        color = color,
        topLeft = Offset(innerLeft, innerTop),
        size = innerSize,
        style = Stroke(width = 2.dp.toPx()),
    )

    // 3. Outer 6dp thick L-brackets (maximum visual authority — AEAF-locked.png equivalent)
    val cornerLen = 32.dp.toPx()
    val path = Path().apply {
        moveTo(left, top + cornerLen); lineTo(left, top); lineTo(left + cornerLen, top)
        moveTo(right - cornerLen, top); lineTo(right, top); lineTo(right, top + cornerLen)
        moveTo(right, bottom - cornerLen); lineTo(right, bottom); lineTo(right - cornerLen, bottom)
        moveTo(left + cornerLen, bottom); lineTo(left, bottom); lineTo(left, bottom - cornerLen)
    }
    drawPath(path = path, color = color, style = Stroke(width = 6.dp.toPx(), join = StrokeJoin.Miter))
}
