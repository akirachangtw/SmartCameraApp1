package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.config.AmbientConstants

// ── State ─────────────────────────────────────────────────────────────────────

sealed class TapFocusState {
    object Idle : TapFocusState()
    data class Focusing(val center: Offset) : TapFocusState()
    data class Focused(val center: Offset) : TapFocusState()
    data class Failed(val center: Offset) : TapFocusState()
    data class Locked(val center: Offset) : TapFocusState()
}

// ── Colors ────────────────────────────────────────────────────────────────────

private val ColorFocusing = Color(0xFFFFFFFF)
private val ColorFocused  = Color(0xFF50C864)
private val ColorFailed   = Color(0xFFD4A030)
private val ColorLocked   = Color(0xFFD4A030)

private val BadgeBackground = Color(0xD90D0D0D)  // #0D0D0D α0.85
private val BadgeBorder     = Color(0xFFD4A030)   // 琥珀金

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * TapFocusRing — single-ring tap AF visual indicator.
 *
 * Canvas budget: exactly 1 drawCircle call. Locked state adds a Badge Composable overlay
 * (does not count toward Canvas budget per P05-T2 Hard Constraint 1).
 *
 * State transitions and tap wiring are T3 scope; this composable is pure rendering.
 *
 * RC-3 (CSO-B): Focused/Failed→Idle fadeout is a render-layer concern only.
 * fadeAlpha animates 1f→0f over FOCUS_FADEOUT_MS when previousState was Focused or Failed.
 * No FadingOut state machine member — SSOT 5-state set is preserved.
 */
@Composable
fun TapFocusRing(
    state: TapFocusState,
    modifier: Modifier = Modifier,
) {
    // ── remember blocks must precede all early returns (Rule 14) ──────────────
    val density = LocalDensity.current
    val ringRadiusPx = with(density) { (AmbientConstants.FOCUS_RING_SIZE_DP / 2f).dp.toPx() }
    val strokePx = with(density) { AmbientConstants.FOCUS_RING_STROKE_DP.dp.toPx() }

    val scale = remember { Animatable(1f) }
    val arcRotation = remember { Animatable(0f) }
    // Badge animation state hoisted to top per Rule 14 (must precede early return)
    val badgeScale = remember { Animatable(0.5f) }
    val badgeAlpha = remember { Animatable(0f) }

    // RC-3 (CSO-B): render-layer fadeout — tracks previous state to detect Focused→Idle transition.
    val previousState = remember { mutableStateOf<TapFocusState>(TapFocusState.Idle) }
    val fadeAlpha = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "focusPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AmbientConstants.FOCUS_PULSE_CYCLE_MS.toInt(), easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    val colorTarget = when (state) {
        is TapFocusState.Focusing -> ColorFocusing
        is TapFocusState.Focused  -> ColorFocused
        is TapFocusState.Failed   -> ColorFailed
        is TapFocusState.Locked   -> ColorLocked
        TapFocusState.Idle        -> Color.Transparent
    }
    val alphaTarget = when (state) {
        is TapFocusState.Focusing -> 0.9f
        is TapFocusState.Focused  -> 0.8f
        is TapFocusState.Failed   -> 0.6f
        is TapFocusState.Locked   -> 0.85f
        TapFocusState.Idle        -> 0f
    }
    // RC-4: 200ms color transition — SSOT has no explicit constant for color-transition duration.
    val animatedColor by animateColorAsState(
        targetValue = colorTarget.copy(alpha = alphaTarget),
        animationSpec = tween(AmbientConstants.FOCUS_COLOR_TRANSITION_MS.toInt()),
        label = "ringColor",
    )

    // ── Per-state animation launch ────────────────────────────────────────────
    LaunchedEffect(state) {
        // RC-3 (CSO-B): SSOT A.2.2.1 α→0 over FOCUS_FADEOUT_MS — render layer only.
        if ((previousState.value is TapFocusState.Focused || previousState.value is TapFocusState.Failed) && state == TapFocusState.Idle) {
            fadeAlpha.animateTo(0f, tween(AmbientConstants.FOCUS_FADEOUT_MS.toInt()))
        } else {
            fadeAlpha.snapTo(1f)
        }
        previousState.value = state

        when (state) {
            is TapFocusState.Focusing -> {
                badgeScale.snapTo(0.5f)
                badgeAlpha.snapTo(0f)
                scale.snapTo(1.3f)
                arcRotation.snapTo(0f)
                scale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
                arcRotation.animateTo(
                    targetValue = 60f,
                    animationSpec = tween(durationMillis = AmbientConstants.FOCUS_ARC_ROTATION_MS.toInt(), easing = FastOutSlowInEasing),
                )
            }
            is TapFocusState.Focused -> {
                badgeScale.snapTo(0.5f)
                badgeAlpha.snapTo(0f)
                scale.snapTo(1.0f)
                arcRotation.snapTo(0f)
            }
            is TapFocusState.Failed -> {
                badgeScale.snapTo(0.5f)
                badgeAlpha.snapTo(0f)
                scale.snapTo(1.0f)
                // RC-4: ±4px oscillation → 1.20 expansion → settle (SSOT A.2.2.2 FOCUS_FAILED_MS)
                scale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = keyframes {
                        durationMillis = AmbientConstants.FOCUS_FAILED_MS.toInt()
                        1.0f at 0
                        0.97f at AmbientConstants.FOCUS_FAILED_KEYFRAME_1_MS.toInt()
                        1.03f at AmbientConstants.FOCUS_FAILED_KEYFRAME_2_MS.toInt()
                        0.97f at AmbientConstants.FOCUS_FAILED_KEYFRAME_3_MS.toInt()
                        1.03f at AmbientConstants.FOCUS_FAILED_KEYFRAME_4_MS.toInt()
                        1.20f at AmbientConstants.FOCUS_FAILED_EXPAND_MS.toInt()
                        1.0f at AmbientConstants.FOCUS_FAILED_MS.toInt()
                    },
                )
            }
            is TapFocusState.Locked -> {
                scale.snapTo(0.85f)
                arcRotation.snapTo(0f)
                // Badge enter animation — re-triggers on every Locked re-entry (key = state)
                badgeScale.snapTo(0.5f)
                badgeAlpha.snapTo(0f)
                badgeScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = AmbientConstants.FOCUS_BADGE_ENTER_MS.toInt(), easing = FastOutSlowInEasing),
                )
                badgeAlpha.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = AmbientConstants.FOCUS_BADGE_ENTER_MS.toInt()),
                )
            }
            TapFocusState.Idle -> {
                badgeScale.snapTo(0.5f)
                badgeAlpha.snapTo(0f)
                scale.snapTo(1f)
                arcRotation.snapTo(0f)
            }
        }
    }

    // RC-3 (CSO-B): allow rendering while Focused→Idle fade is in progress (fadeAlpha > 0).
    val isFadingOut = state == TapFocusState.Idle && fadeAlpha.value > 0f
    if (state == TapFocusState.Idle && !isFadingOut) return

    val center = when {
        isFadingOut -> when (val prev = previousState.value) {
            is TapFocusState.Focused -> prev.center
            is TapFocusState.Failed  -> prev.center
            else -> Offset.Zero
        }
        state is TapFocusState.Focusing -> state.center
        state is TapFocusState.Focused  -> state.center
        state is TapFocusState.Failed   -> state.center
        state is TapFocusState.Locked   -> state.center
        else -> Offset.Zero
    }

    // RC-3 (CSO-B): during fadeout use previous state color at decaying alpha; otherwise use animatedColor.
    val drawColor = if (isFadingOut) {
        when (val prev = previousState.value) {
            is TapFocusState.Focused -> ColorFocused.copy(alpha = 0.8f * fadeAlpha.value)
            is TapFocusState.Failed  -> ColorFailed.copy(alpha = 0.6f * fadeAlpha.value)
            else -> animatedColor
        }
    } else {
        animatedColor
    }

    val effectiveScale = if (state is TapFocusState.Focused) pulseScale else scale.value

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(degrees = arcRotation.value, pivot = center) {
                drawCircle(
                    color = drawColor,
                    radius = ringRadiusPx * effectiveScale,
                    center = center,
                    style = Stroke(width = strokePx),
                )
            }
        }

        // Locked Badge — HC-1: zero Canvas calls (uses Modifier.background+border, no drawXxx)
        if (state is TapFocusState.Locked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = center.x + ringRadiusPx - AmbientConstants.FOCUS_RING_BADGE_OFFSET_DP.dp.toPx()
                        translationY = center.y - ringRadiusPx - AmbientConstants.FOCUS_RING_BADGE_OFFSET_DP.dp.toPx()
                        scaleX = badgeScale.value
                        scaleY = badgeScale.value
                        alpha = badgeAlpha.value
                    },
            ) {
                // Purely decorative badge — no click handler, no touch consumer (Layer 3.1: non-interactive)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(18.dp)
                        .background(BadgeBackground, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .border(1.dp, BadgeBorder, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .semantics(mergeDescendants = false) {},
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = BadgeBorder,
                        modifier = Modifier.size(10.dp),
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

// 150f keeps ring visible inside 300.dp preview canvas at any density (min 1x → 300px)
private val PreviewCenter = Offset(150f, 150f)

@Preview(showBackground = true, backgroundColor = 0xFF111111, name = "Idle")
@Composable
private fun PreviewIdle() {
    TapFocusRing(state = TapFocusState.Idle, modifier = Modifier.size(300.dp))
}

@Preview(showBackground = true, backgroundColor = 0xFF111111, name = "Focusing")
@Composable
private fun PreviewFocusing() {
    TapFocusRing(
        state = TapFocusState.Focusing(center = PreviewCenter),
        modifier = Modifier.size(300.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111111, name = "Focused")
@Composable
private fun PreviewFocused() {
    TapFocusRing(
        state = TapFocusState.Focused(center = PreviewCenter),
        modifier = Modifier.size(300.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111111, name = "Failed")
@Composable
private fun PreviewFailed() {
    TapFocusRing(
        state = TapFocusState.Failed(center = PreviewCenter),
        modifier = Modifier.size(300.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111111, name = "Locked")
@Composable
private fun PreviewLocked() {
    TapFocusRing(
        state = TapFocusState.Locked(center = PreviewCenter),
        modifier = Modifier.size(300.dp),
    )
}
