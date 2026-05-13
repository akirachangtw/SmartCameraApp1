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

internal const val TAG_PILL_PHOTO = "PillPhoto"
internal const val TAG_PILL_PTF = "PillPTF"

private val PILL_TRACK_STROKE = Color(0xFF555555)
private val PILL_INDICATOR_BG = Color(0xFF4A4A4A)
private val PILL_SELECTED_ICON = Color(0xFFCCCCCC)
private val PILL_UNSELECTED_ICON = Color(0xFF666666)

/**
 * Photo/PTF Pill Toggle — Layout Spec v2.2 §5.
 *
 * 128×28dp track, 60×24dp sliding indicator, 300ms FastOutSlowInEasing.
 * Left half = Photo mode, right half = PTF mode.
 */
@Composable
internal fun PillToggle(
    cameraMode: CameraMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Rule 14: all animation state at composable top before any conditional logic.
    val isPtfActive = cameraMode is CameraMode.PassToFriend

    val indicatorOffset by animateDpAsState(
        targetValue = if (isPtfActive) 66.dp else 2.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pill_indicator_offset",
    )
    val photoIconColor by animateColorAsState(
        targetValue = if (!isPtfActive) PILL_SELECTED_ICON else PILL_UNSELECTED_ICON,
        animationSpec = tween(durationMillis = 300),
        label = "pill_photo_icon",
    )
    val ptfIconColor by animateColorAsState(
        targetValue = if (isPtfActive) PILL_SELECTED_ICON else PILL_UNSELECTED_ICON,
        animationSpec = tween(durationMillis = 300),
        label = "pill_ptf_icon",
    )

    Box(
        modifier = modifier
            .width(128.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(width = 1.dp, color = PILL_TRACK_STROKE, shape = RoundedCornerShape(14.dp)),
    ) {
        // Sliding indicator — offset from CenterStart
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = indicatorOffset)
                .size(width = 60.dp, height = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PILL_INDICATOR_BG),
        )

        // Two tap zones drawn over the indicator
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = isPtfActive, onClick = onToggle)
                    .semantics { role = Role.Tab }
                    .testTag(TAG_PILL_PHOTO),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.tab_normal_mode_cd),
                    tint = photoIconColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = !isPtfActive, onClick = onToggle)
                    .semantics { role = Role.Tab }
                    .testTag(TAG_PILL_PTF),
            ) {
                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = stringResource(R.string.tab_pass_to_friend_cd),
                    tint = ptfIconColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
