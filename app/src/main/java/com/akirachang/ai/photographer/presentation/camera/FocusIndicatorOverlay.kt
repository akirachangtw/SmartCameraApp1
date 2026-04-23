package com.akirachang.ai.photographer.presentation.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akirachang.ai.photographer.domain.model.focus.FocusUiState
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.model.focus.OverlayColor
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState
import kotlinx.coroutines.delay

// ── SSOT: All timing, sizes, and colors MUST reference FocusTokens ──────────
// Violation of this contract = architecture pollution (see devspec/feature_record_Backlog.md §M/§N/§O).
internal object FocusTokens {
    // Cueshot D0: Legacy blue palette retired. Ambient UI palette: Amber / Golden / Green.
    val CueshotAmber = Color(0xFFD4A030)   // #D4A030 — guiding / unaligned
    val CueshotGreen = Color(0xFF50C864)   // #50C864 — aligned / confirmed

    // Sizes (dp)
    val SearchingSize = 180.dp

    // Animation timing (ms)
    const val DWELL_DELAY = 1000L
    const val DWELL_FADE_DURATION = 500
    const val COLOR_TRANSITION_MS = 200   // Yellow → Cyan confirmed-target transition (v2.12.0-18)
}

/**
 * FocusIndicatorOverlay — Ambient UI focus visual layer.
 *
 * All focus states render the concentric-ring searching base (SSOT v3.3 §2.4 compliance).
 * Rectangles and L-brackets removed (D8-STABILIZE-FIX I2): circles only.
 * Alpha lifecycle driven by [VisualTargetState]: dwell fade 1.0 → 0.4 after DWELL_DELAY.
 *
 * SSOT Route: This composable is the ACTIVE CameraScreen focus path (v2.12.0-19 wiring complete).
 *   FocusOverlay.kt has been deleted (焦土政策 — zero call sites after wire-over).
 *   This file is now the single focus rendering truth — do not re-introduce FocusOverlay.
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

    // ── Color: Focusing→Amber, Focused→Green transition 200ms ───────────────
    // SSOT v3.3 §2.4: Halo金色→綠色 (CYAN=tracking/guiding, YELLOW=AE/AF locked)
    val colorTarget = when (overlayModel.primaryColor) {
        OverlayColor.CYAN   -> FocusTokens.CueshotAmber  // Focusing: amber (#D4A030)
        OverlayColor.YELLOW -> FocusTokens.CueshotGreen  // Focused:  green (#50C864)
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

    // ── Visual lifecycle state machine ────────────────────────────────────────
    LaunchedEffect(visualTargetState, overlayModel.primaryStableId) {
        when (visualTargetState) {
            VisualTargetState.VisualLocked -> {
                alphaTarget = 1f
                delay(FocusTokens.DWELL_DELAY)
                alphaTarget = 0.4f
            }
            VisualTargetState.TrackingStable -> {
                alphaTarget = 1f
            }
            VisualTargetState.TrackingFade -> {
                alphaTarget = 0.4f
            }
            VisualTargetState.Candidate -> {
                alphaTarget = 0.6f
            }
            VisualTargetState.Lost -> {
                alphaTarget = 0.2f
            }
            VisualTargetState.None -> { /* guarded at top */ }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Defer all animated reads to draw phase — prevents Composition-phase invalidation.
        val alpha = animatedAlpha.value
        val color = animatedColor.value

        // All states render the concentric-ring Searching base (Ambient UI: circles only).
        // Rectangles, L-brackets, and secondary boxes are removed (SSOT v3.3 §2.4 compliance).
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

