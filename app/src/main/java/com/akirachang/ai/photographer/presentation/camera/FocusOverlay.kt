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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.domain.model.focus.OverlayColor
import com.akirachang.ai.photographer.domain.model.focus.OverlayModel
import com.akirachang.ai.photographer.domain.model.focus.VisualTargetState
import kotlinx.coroutines.delay

/**
 * New focus overlay composable driven by FocusUiState / OverlayModel.
 * v2.12.0-15 Phase 2
 * v2.12.0-18: Color animation (Yellow→Cyan transition) + secondary bracket rendering.
 *
 * Rules:
 * - If overlayModel is null OR visualTargetState is None → render NOTHING (fully transparent).
 * - No hardcoded 36dp fallback box is ever permitted.
 * - Primary box: Cyan (Eye-AF) or Yellow (AE/AF Lock or optimistic tap placeholder).
 *   Yellow→Cyan transition animates over 200ms when target is confirmed.
 * - Secondary boxes: rendered as corner [ ] brackets at fixed 0.3 alpha (primary monopoly rule).
 *   Secondary targets use raw ML Kit coords — no 1-Euro smoothing (CPU saving).
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

    val primaryColorTarget = when (overlayModel.primaryColor) {
        OverlayColor.CYAN   -> Color(0xFF00F2FF)
        OverlayColor.YELLOW -> Color(0xFFFFD700)
    }

    // v2.12.0-18 Task 1: Animate color transition (Yellow → Cyan) over 200ms.
    // Handles optimistic tap placeholder (Yellow) fading to confirmed Eye-AF (Cyan).
    val animatedPrimaryColor by animateColorAsState(
        targetValue = primaryColorTarget,
        animationSpec = tween(durationMillis = 200),
        label = "focusPrimaryColor",
    )

    // ── Compose 動畫狀態機（SSOT 視覺生命週期）────────────────────────────────
    // 追蹤上一個 visualState，用於偵測 VisualLocked 進入事件
    var prevVisualState by remember { mutableStateOf<VisualTargetState>(VisualTargetState.None) }

    // 動畫 alpha 目標值（由生命週期狀態機控制）
    var alphaTarget by remember { mutableFloatStateOf(overlayModel.alpha) }

    // 平滑動畫 alpha（Compose 驅動，非 Thread.sleep）
    val animatedAlpha by animateFloatAsState(
        targetValue = alphaTarget,
        animationSpec = tween(durationMillis = 400),
        label = "focusOverlayAlpha",
    )

    // 夾爪動畫觸發狀態
    var showClawAnimation by remember { mutableStateOf(false) }

    // 視覺生命週期狀態機：VisualLocked 進入時觸發夾爪 + 1s 後漸變至 0.4
    LaunchedEffect(visualTargetState, overlayModel.primaryStableId) {
        when (visualTargetState) {
            VisualTargetState.VisualLocked -> {
                if (prevVisualState != VisualTargetState.VisualLocked) {
                    // 剛進入 VisualLocked：觸發夾爪動畫，alpha = 1.0
                    showClawAnimation = true
                    alphaTarget = 1.0f
                    // 維持 1000ms
                    delay(1000L)
                    // 若仍在 VisualLocked（未被切換），漸變至 0.4
                    if (visualTargetState == VisualTargetState.VisualLocked) {
                        alphaTarget = 0.4f
                        showClawAnimation = false
                    }
                } else {
                    // 持續 VisualLocked：維持當前 alpha（不重置）
                    // alphaTarget 保持不變
                }
            }
            VisualTargetState.TrackingStable -> {
                showClawAnimation = false
                alphaTarget = 1.0f
            }
            VisualTargetState.TrackingFade -> {
                showClawAnimation = false
                alphaTarget = 0.4f
            }
            VisualTargetState.Candidate -> {
                showClawAnimation = false
                alphaTarget = 0.6f
            }
            VisualTargetState.Lost -> {
                showClawAnimation = false
                alphaTarget = 0.2f
            }
            VisualTargetState.None -> {
                showClawAnimation = false
                alphaTarget = 0.0f
            }
        }
        prevVisualState = visualTargetState
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidthPx = 2.5.dp.toPx()

        // ── Primary box ───────────────────────────────────────────────────────
        overlayModel.primaryBox?.let { rect ->
            drawRect(
                color = animatedPrimaryColor.copy(alpha = animatedAlpha),
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width(), rect.height()),
                style = Stroke(width = strokeWidthPx),
            )
        }

        // ── 夾爪動畫（VisualLocked 進入時的機械夾爪效果）──────────────────────
        if (showClawAnimation) {
            overlayModel.primaryBox?.let { rect ->
                val clawSize = 12.dp.toPx()
                val clawStrokeWidth = strokeWidthPx * 1.5f
                val clawColor = animatedPrimaryColor.copy(alpha = animatedAlpha)
                // 四角夾爪：左上
                drawLine(clawColor, Offset(rect.left, rect.top + clawSize), Offset(rect.left, rect.top), clawStrokeWidth)
                drawLine(clawColor, Offset(rect.left, rect.top), Offset(rect.left + clawSize, rect.top), clawStrokeWidth)
                // 右上
                drawLine(clawColor, Offset(rect.right - clawSize, rect.top), Offset(rect.right, rect.top), clawStrokeWidth)
                drawLine(clawColor, Offset(rect.right, rect.top), Offset(rect.right, rect.top + clawSize), clawStrokeWidth)
                // 左下
                drawLine(clawColor, Offset(rect.left, rect.bottom - clawSize), Offset(rect.left, rect.bottom), clawStrokeWidth)
                drawLine(clawColor, Offset(rect.left, rect.bottom), Offset(rect.left + clawSize, rect.bottom), clawStrokeWidth)
                // 右下
                drawLine(clawColor, Offset(rect.right - clawSize, rect.bottom), Offset(rect.right, rect.bottom), clawStrokeWidth)
                drawLine(clawColor, Offset(rect.right, rect.bottom - clawSize), Offset(rect.right, rect.bottom), clawStrokeWidth)
            }
        }

        // ── Secondary boxes (non-primary faces): corner [ ] brackets only ─────
        // v2.12.0-18 Task 3: Primary monopoly rule.
        // - Fixed alpha 0.3 (never animated — secondary targets are not primary)
        // - Always Cyan corner brackets (candidates waiting for primary selection)
        // - Raw ML Kit coordinates (no 1-Euro smoothing — CPU saving)
        val secondaryColor = Color(0xFF00F2FF).copy(alpha = 0.3f)
        val bracketSize = 10.dp.toPx()
        val bracketStroke = strokeWidthPx * 0.7f
        overlayModel.secondaryBoxes.forEach { rect ->
            // Top-left corner
            drawLine(secondaryColor, Offset(rect.left, rect.top + bracketSize), Offset(rect.left, rect.top), bracketStroke)
            drawLine(secondaryColor, Offset(rect.left, rect.top), Offset(rect.left + bracketSize, rect.top), bracketStroke)
            // Top-right corner
            drawLine(secondaryColor, Offset(rect.right - bracketSize, rect.top), Offset(rect.right, rect.top), bracketStroke)
            drawLine(secondaryColor, Offset(rect.right, rect.top), Offset(rect.right, rect.top + bracketSize), bracketStroke)
            // Bottom-left corner
            drawLine(secondaryColor, Offset(rect.left, rect.bottom - bracketSize), Offset(rect.left, rect.bottom), bracketStroke)
            drawLine(secondaryColor, Offset(rect.left, rect.bottom), Offset(rect.left + bracketSize, rect.bottom), bracketStroke)
            // Bottom-right corner
            drawLine(secondaryColor, Offset(rect.right - bracketSize, rect.bottom), Offset(rect.right, rect.bottom), bracketStroke)
            drawLine(secondaryColor, Offset(rect.right, rect.bottom - bracketSize), Offset(rect.right, rect.bottom), bracketStroke)
        }

        // ── Lock icon (AE/AF locked) ──────────────────────────────────────────
        if (overlayModel.showLockIcon) {
            overlayModel.primaryBox?.let { rect ->
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = animatedAlpha),
                    radius = 6.dp.toPx(),
                    center = Offset(rect.centerX(), rect.top - 10.dp.toPx()),
                )
            }
        }
    }
}
