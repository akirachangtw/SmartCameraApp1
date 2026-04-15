package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akirachang.ai.photographer.R
import com.akirachang.ai.photographer.presentation.camera.PassToFriendMode
import androidx.compose.foundation.clickable

// D7-B1: Testability probes for CameraTabBar.
internal const val TAG_TAB_NORMAL = "TabNormal"
internal const val TAG_TAB_PTF = "TabPassToFriend"

private val PTF_ACTIVE_COLOR = Color(0xFF50C864)   // Cueshot green — SSOT §2.5
private val TAB_BG_COLOR = Color.Black.copy(alpha = 0.55f)
private val TAB_SELECTED_TEXT = Color.Black
private val TAB_UNSELECTED_TEXT = Color.White.copy(alpha = 0.75f)

/**
 * Camera mode tab bar — Cueshot D7-B1.
 *
 * Two tabs: Normal (拍照) ↔ Pass-To-Friend (遞手機).
 * Selection drives [PassToFriendMode] via [onToggle] callback.
 * 300ms color-fade animation (Rule 14: all remember/state calls are at top of composable).
 *
 * @param passToFriendMode Current PTF mode from [CameraState].
 * @param onToggle Invoked when the user taps the non-active tab.
 * @param modifier Modifier passed to the root [Row].
 */
@Composable
internal fun CameraTabBar(
    passToFriendMode: PassToFriendMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Rule 14: all animation state declared at composable top — before any conditional logic.
    val isPtfActive = passToFriendMode is PassToFriendMode.Active

    val normalBg by animateColorAsState(
        targetValue = if (!isPtfActive) PTF_ACTIVE_COLOR else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "normal_tab_bg",
    )
    val ptfBg by animateColorAsState(
        targetValue = if (isPtfActive) PTF_ACTIVE_COLOR else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "ptf_tab_bg",
    )
    val normalTextColor by animateColorAsState(
        targetValue = if (!isPtfActive) TAB_SELECTED_TEXT else TAB_UNSELECTED_TEXT,
        animationSpec = tween(durationMillis = 300),
        label = "normal_tab_text",
    )
    val ptfTextColor by animateColorAsState(
        targetValue = if (isPtfActive) TAB_SELECTED_TEXT else TAB_UNSELECTED_TEXT,
        animationSpec = tween(durationMillis = 300),
        label = "ptf_tab_text",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(TAB_BG_COLOR)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Normal tab
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(normalBg)
                .clickable(
                    enabled = isPtfActive,  // only tappable when not already selected
                    onClick = onToggle,
                )
                .semantics { role = Role.Tab }
                .testTag(TAG_TAB_NORMAL),
        ) {
            Text(
                text = stringResource(R.string.tab_normal_mode),
                color = normalTextColor,
                fontSize = 14.sp,
                fontWeight = if (!isPtfActive) FontWeight.Bold else FontWeight.Normal,
            )
        }

        // Pass-To-Friend tab
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ptfBg)
                .clickable(
                    enabled = !isPtfActive,  // only tappable when not already selected
                    onClick = onToggle,
                )
                .semantics { role = Role.Tab }
                .testTag(TAG_TAB_PTF),
        ) {
            Text(
                text = stringResource(R.string.tab_pass_to_friend),
                color = ptfTextColor,
                fontSize = 14.sp,
                fontWeight = if (isPtfActive) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
