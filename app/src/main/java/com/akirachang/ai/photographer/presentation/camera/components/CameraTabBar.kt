package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.R
import com.akirachang.ai.photographer.domain.camera.CameraMode

internal const val TAG_TAB_NORMAL = "TabNormal"
internal const val TAG_TAB_PTF = "TabPassToFriend"

// Stage0 Pill Toggle — Layout Spec v2.1 §5 / SSOT §3
private val PILL_WIDTH = 128.dp
private val PILL_HEIGHT = 28.dp
private val PILL_CORNER = 14.dp
private val INDICATOR_WIDTH = 60.dp
private val INDICATOR_HEIGHT = 24.dp
private val INDICATOR_CORNER = 12.dp
private val INDICATOR_MARGIN = 2.dp
private val TOUCH_HEIGHT = 48.dp
private val ICON_SIZE = 20.dp
private val STROKE_COLOR = Color(0xFF555555)
private val INDICATOR_BG = Color(0xFF4A4A4A)
private val SELECTED_ICON = Color(0xFFCCCCCC)
private val UNSELECTED_ICON = Color(0xFF666666)

// Indicator slides: 2dp from left edge (Photo) ↔ 66dp from left edge (PTF)
private val INDICATOR_X_NORMAL = INDICATOR_MARGIN
private val INDICATOR_X_PTF = PILL_WIDTH - INDICATOR_WIDTH - INDICATOR_MARGIN

/**
 * Stage0 Pill Toggle — Photo ↔ PTF mode selector.
 *
 * Visual: 128dp × 28dp pill with 1px #555555 stroke and sliding #4A4A4A indicator.
 * Touch target: 128dp × 48dp (per Layout Spec v2.1 §5).
 * Zero hardware transitions — dispatches [CameraUiEvent.SetCameraMode] only.
 */
@Composable
internal fun CameraTabBar(
    cameraMode: CameraMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Rule 14: all state calls at composable top, before any conditionals.
    val isPtfActive = cameraMode is CameraMode.PassToFriend

    val indicatorX by animateDpAsState(
        targetValue = if (isPtfActive) INDICATOR_X_PTF else INDICATOR_X_NORMAL,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pill_indicator_x",
    )
    val normalIconColor by animateColorAsState(
        targetValue = if (!isPtfActive) SELECTED_ICON else UNSELECTED_ICON,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "normal_icon_color",
    )
    val ptfIconColor by animateColorAsState(
        targetValue = if (isPtfActive) SELECTED_ICON else UNSELECTED_ICON,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ptf_icon_color",
    )

    Box(
        modifier = modifier
            .width(PILL_WIDTH)
            .height(TOUCH_HEIGHT),
    ) {
        // Visual pill: 128dp × 28dp, vertically centered within the 48dp touch target.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(PILL_WIDTH)
                .height(PILL_HEIGHT)
                .border(1.dp, STROKE_COLOR, RoundedCornerShape(PILL_CORNER))
                .clip(RoundedCornerShape(PILL_CORNER)),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorX, y = INDICATOR_MARGIN)
                    .size(INDICATOR_WIDTH, INDICATOR_HEIGHT)
                    .clip(RoundedCornerShape(INDICATOR_CORNER))
                    .background(INDICATOR_BG),
            )
        }

        // Click targets span the full 48dp touch-target height; icons are centered.
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = isPtfActive, onClick = onToggle)
                    .semantics { role = Role.Tab }
                    .testTag(TAG_TAB_NORMAL),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.tab_normal_mode_cd),
                    tint = normalIconColor,
                    modifier = Modifier.size(ICON_SIZE),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = !isPtfActive, onClick = onToggle)
                    .semantics { role = Role.Tab }
                    .testTag(TAG_TAB_PTF),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = stringResource(R.string.tab_pass_to_friend_cd),
                    tint = ptfIconColor,
                    modifier = Modifier.size(ICON_SIZE),
                )
            }
        }
    }
}
