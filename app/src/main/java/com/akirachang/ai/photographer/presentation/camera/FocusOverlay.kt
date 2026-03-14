package com.akirachang.ai.photographer.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.model.focus.OverlayColor
import com.akirachang.ai.photographer.domain.model.focus.OverlayModel
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState

/**
 * New focus overlay composable driven by FocusUiState / OverlayModel.
 * v2.12.0-15 Phase 2
 *
 * Rules:
 * - If overlayModel is null OR visualTargetState is None → render NOTHING (fully transparent).
 * - No hardcoded 36dp fallback box is ever permitted.
 * - Primary box: Cyan (Eye-AF) or Yellow (AE/AF Lock).
 * - Secondary boxes: rendered at 40% alpha as candidate brackets.
 */
@Composable
fun FocusOverlay(
    overlayModel: OverlayModel?,
    visualTargetState: VisualTargetState,
    modifier: Modifier = Modifier,
) {
    // SSOT Rule: None state OR no primary box → render NOTHING (fully transparent)
    // v2.12.0-15 SV-BatchA Fix A: Even if secondaryBoxes exist, we must not render
    // anything without a valid primaryBox — prevents ghost secondary brackets.
    if (overlayModel == null) return
    if (visualTargetState == VisualTargetState.None) return
    if (overlayModel.primaryBox == null) return

    val primaryColor = when (overlayModel.primaryColor) {
        OverlayColor.CYAN   -> Color(0xFF00F2FF)
        OverlayColor.YELLOW -> Color(0xFFFFD700)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidthPx = 2.5.dp.toPx()

        // ── Primary box ───────────────────────────────────────────────────────
        overlayModel.primaryBox?.let { rect ->
            drawRect(
                color = primaryColor.copy(alpha = overlayModel.alpha),
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width(), rect.height()),
                style = Stroke(width = strokeWidthPx),
            )
        }

        // ── Secondary boxes (multi-face candidates) ───────────────────────────
        overlayModel.secondaryBoxes.forEach { rect ->
            drawRect(
                color = primaryColor.copy(alpha = overlayModel.alpha * 0.4f),
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width(), rect.height()),
                style = Stroke(width = strokeWidthPx * 0.7f),
            )
        }

        // ── Lock icon (AE/AF locked) ──────────────────────────────────────────
        if (overlayModel.showLockIcon) {
            overlayModel.primaryBox?.let { rect ->
                // Simple lock indicator: small circle at top-center of box
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = overlayModel.alpha),
                    radius = 6.dp.toPx(),
                    center = Offset(rect.centerX(), rect.top - 10.dp.toPx()),
                )
            }
        }
    }
}
