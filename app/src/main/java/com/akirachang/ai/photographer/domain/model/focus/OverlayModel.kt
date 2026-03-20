package com.akirachang.ai.photographer.domain.model.focus

import android.graphics.RectF

/**
 * Complete description of what the focus overlay Composable should render.
 * v2.12.0-15 / §17.11.10
 *
 * Rule: when primaryBox is null, or VisualTargetState is None, the Composable MUST be
 * fully transparent/hidden. No fallback 36dp box is ever permitted.
 */
data class OverlayModel(
    val primaryStableId: String?,
    val primaryBox: RectF?,
    val secondaryBoxes: List<RectF>,
    val primaryColor: OverlayColor,
    val alpha: Float,
    val showLockIcon: Boolean,
    val animation: OverlayAnimation,
)

enum class OverlayColor {
    CYAN,
    YELLOW,
}

enum class OverlayAnimation {
    NONE,
    PULSE,
    FADE,
    REACQUIRE,
}
