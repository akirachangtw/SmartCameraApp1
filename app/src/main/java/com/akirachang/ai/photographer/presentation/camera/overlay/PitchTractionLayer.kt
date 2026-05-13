package com.akirachang.ai.photographer.presentation.camera.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import com.akirachang.ai.photographer.domain.config.AmbientConstants
import com.akirachang.ai.photographer.domain.model.DeviceCapabilityTier
import kotlin.math.abs

// Test-tag constants — consumed by PitchTractionLayerTest (D4-T4 defence matrix).
internal const val TAG_PITCH_TRACTION_ALIGNED      = "PitchTractionLayer_Aligned"
internal const val TAG_PITCH_TRACTION_AMBER        = "PitchTractionLayer_Amber"
// Semantic probe: present in composition when dots are enabled (FULL/LIMITED tier + direction active).
// Zero-size Box — no visual impact. Consumed by PitchTractionLayerTest.
internal const val TAG_PITCH_TRACTION_DOTS_ENABLED = "PitchTractionLayer_DotsEnabled"

/** Amber guide color per SSOT v4.0 */
private val WarmAmber = Color(0xFFD4A030)

/** Aligned completion color per SSOT v4.0 §2.5 */
private val AlignGreen = Color(0xFF50C864)

/**
 * Minimum roll deviation (degrees) to trigger directional pitch traction.
 * Below this threshold the device is considered level — no directional glow.
 */
private const val ROLL_THRESHOLD_DEG = 3f

/** Pitch traction gradient covers the outermost 15% of the viewport. */
private const val EDGE_BAND_FRACTION = 0.15f

/**
 * Floating light dot configuration.
 * Each dot has an independent animation period to avoid lockstep patterns.
 */
private data class DotSpec(
    /** Horizontal position as fraction of edge-band width [0, 1]. */
    val normalizedX: Float,
    /** Vertical position as fraction of full height [0, 1]. */
    val normalizedY: Float,
    /** Radius in pixels (resolved at draw time from dp-equivalent fraction). */
    val radiusFraction: Float,
    /** Animation cycle duration in milliseconds. */
    val durationMs: Int,
    /** Phase offset so dots start at different points in their cycle. */
    val initialValue: Float,
)

/**
 * 5 dots distributed across LEFT/RIGHT/TOP/BOTTOM bands.
 * Positions and sizes are fixed seeds; alpha animates independently per dot.
 * Dot alpha range: 0.10 – 0.15 per SSOT D4 spec.
 */
private val DOT_SPECS = listOf(
    DotSpec(0.40f, 0.20f, 0.012f, durationMs = 2200, initialValue = 0f),
    DotSpec(0.70f, 0.75f, 0.010f, durationMs = 3100, initialValue = 0.3f),
    DotSpec(0.20f, 0.55f, 0.014f, durationMs = 2700, initialValue = 0.6f),
    DotSpec(0.55f, 0.35f, 0.009f, durationMs = 3400, initialValue = 0.15f),
    DotSpec(0.30f, 0.88f, 0.011f, durationMs = 2500, initialValue = 0.8f),
)

/** Direction derived from roll/pitch to drive pitch traction placement. */
private enum class TractionDirection { LEFT, RIGHT, UP, DOWN }

/**
 * Shared animation spec for breathing rhythm per SSOT v4.0 §2.5.
 * [AmbientConstants.BREATHE_CYCLE_MS] single pass, double full cycle (reverse). FastOutSlowIn easing.
 */
private val BREATHING_ANIM_SPEC: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
    animation = tween(durationMillis = AmbientConstants.BREATHE_CYCLE_MS.toInt(), easing = FastOutSlowInEasing),
    repeatMode = RepeatMode.Reverse,
)

/**
 * PitchTractionLayer — Cueshot v4.0 Ambient UI.
 *
 * Renders:
 * 1. **Gradient base layer** — directional pitch traction (#D4A030 amber) driven by [deviceRoll].
 *    Max 20% alpha at full deviation.
 * 2. **Floating light dots** — 5 independent breathing dots overlaid on the traction edge band.
 *    Alpha cycles 0.10–0.15, period 2–4s per dot. Disabled on [DeviceCapabilityTier.LEGACY].
 * 3. **Aligned breathing mode** — when [isAligned] is true, overrides amber mode with
 *    4-edge uniform green (#50C864) breathing alpha 0.10→0.18 per SSOT §2.5.
 *
 * @param modifier Modifier — should be [fillMaxSize] to cover the full preview.
 * @param deviceRoll Roll angle in degrees from [CameraState.deviceRoll].
 *   Positive = device tilted right, negative = device tilted left.
 * @param capabilityTier Device tier. LEGACY suppresses dot rendering.
 * @param isAligned True when compositionResult.isAllAligned == true. Activates breathing mode.
 */
@Composable
fun PitchTractionLayer(
    modifier: Modifier = Modifier,
    deviceRoll: Float = 0f,
    capabilityTier: DeviceCapabilityTier = DeviceCapabilityTier.FULL,
    isAligned: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PitchTractionLayer")

    // ── Aligned breathing mode (SSOT §2.5) ──────────────────────────────────
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.18f,
        animationSpec = BREATHING_ANIM_SPEC,
        label = "breathe_alpha",
    )

    if (isAligned) {
        Canvas(modifier = modifier.fillMaxSize().testTag(TAG_PITCH_TRACTION_ALIGNED)) {
            drawAlignedBreathingGradient(breatheAlpha)
        }
        return
    }

    // ── Normal state: directional amber traction + floating dots ────────────
    val renderDots = capabilityTier !is DeviceCapabilityTier.LEGACY

    val absRoll = abs(deviceRoll)
    val direction: TractionDirection? = when {
        absRoll < ROLL_THRESHOLD_DEG -> null
        deviceRoll < 0f -> TractionDirection.LEFT
        else -> TractionDirection.RIGHT
    }
    val intensity = (absRoll / 45f).coerceIn(0f, 1f)
    val gradientAlpha = intensity * 0.20f

    val dotAlphas: List<Float> = if (renderDots) {
        DOT_SPECS.map { spec ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = spec.initialValue,
                targetValue = 1f - spec.initialValue,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = spec.durationMs,
                        easing = FastOutSlowInEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_alpha_${spec.durationMs}",
            )
            0.10f + alpha * 0.05f
        }
    } else {
        emptyList()
    }

    val dotPaint = remember {
        Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(8f, 0f, 0f, WarmAmber.copy(alpha = 0.6f).toArgb())
            }
        }
    }

    if (renderDots && direction != null) {
        Box(modifier = Modifier.size(0.dp).testTag(TAG_PITCH_TRACTION_DOTS_ENABLED))
    }

    Canvas(modifier = modifier.fillMaxSize().testTag(TAG_PITCH_TRACTION_AMBER)) {
        drawPitchTractionGradient(direction, gradientAlpha)
        if (renderDots && direction != null) {
            drawFloatingDots(direction, dotAlphas, dotPaint)
        }
    }
}

/**
 * Draws the aligned breathing gradient per SSOT v4.0 §2.5.
 * All 4 edges glow uniformly in #50C864 green at the animated [alpha].
 */
private fun DrawScope.drawAlignedBreathingGradient(alpha: Float) {
    val edgePx = size.height * EDGE_BAND_FRACTION
    val edgePxW = size.width * EDGE_BAND_FRACTION
    val green = AlignGreen.copy(alpha = alpha)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(green, Color.Transparent),
            startY = 0f,
            endY = edgePx,
        ),
        size = size,
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, green),
            startY = size.height - edgePx,
            endY = size.height,
        ),
        size = size,
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(green, Color.Transparent),
            startX = 0f,
            endX = edgePxW,
        ),
        size = size,
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, green),
            startX = size.width - edgePxW,
            endX = size.width,
        ),
        size = size,
    )
}

/**
 * Draws the directional pitch traction gradient per SSOT v4.0 §2.3.
 * When [direction] is null (device level), renders a subtle radial vignette.
 */
private fun DrawScope.drawPitchTractionGradient(
    direction: TractionDirection?,
    alpha: Float,
) {
    if (alpha <= 0f) return
    val edgePx = size.height * EDGE_BAND_FRACTION

    when (direction) {
        TractionDirection.UP -> drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(WarmAmber.copy(alpha = alpha), Color.Transparent),
                startY = 0f,
                endY = edgePx,
            ),
            size = size,
        )
        TractionDirection.DOWN -> drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, WarmAmber.copy(alpha = alpha)),
                startY = size.height - edgePx,
                endY = size.height,
            ),
            size = size,
        )
        TractionDirection.LEFT -> drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(WarmAmber.copy(alpha = alpha), Color.Transparent),
                startX = 0f,
                endX = size.width * EDGE_BAND_FRACTION,
            ),
            size = size,
        )
        TractionDirection.RIGHT -> drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, WarmAmber.copy(alpha = alpha)),
                startX = size.width * (1f - EDGE_BAND_FRACTION),
                endX = size.width,
            ),
            size = size,
        )
        null -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, WarmAmber.copy(alpha = alpha * 0.5f)),
                    center = center,
                    radius = size.maxDimension * 0.6f,
                ),
                size = size,
            )
        }
    }
}

/**
 * Draws floating light dots within the active pitch traction edge band.
 */
private fun DrawScope.drawFloatingDots(
    direction: TractionDirection,
    alphas: List<Float>,
    paint: Paint,
) {
    val bandW = size.width * EDGE_BAND_FRACTION
    val bandH = size.height * EDGE_BAND_FRACTION

    DOT_SPECS.forEachIndexed { index, spec ->
        val dotAlpha = alphas.getOrElse(index) { 0.10f }
        val dotColor = WarmAmber.copy(alpha = dotAlpha)
        val radius = size.minDimension * spec.radiusFraction

        val (cx, cy) = when (direction) {
            TractionDirection.LEFT -> Pair(
                bandW * spec.normalizedX,
                size.height * spec.normalizedY,
            )
            TractionDirection.RIGHT -> Pair(
                size.width - bandW + bandW * spec.normalizedX,
                size.height * spec.normalizedY,
            )
            TractionDirection.UP -> Pair(
                size.width * spec.normalizedX,
                bandH * spec.normalizedY,
            )
            TractionDirection.DOWN -> Pair(
                size.width * spec.normalizedX,
                size.height - bandH + bandH * spec.normalizedY,
            )
        }

        drawIntoCanvas { canvas ->
            paint.asFrameworkPaint().color = dotColor.toArgb()
            canvas.drawCircle(
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                radius = radius,
                paint = paint,
            )
        }
        drawCircle(
            color = dotColor,
            radius = radius * 0.5f,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            blendMode = BlendMode.SrcOver,
        )
    }
}
