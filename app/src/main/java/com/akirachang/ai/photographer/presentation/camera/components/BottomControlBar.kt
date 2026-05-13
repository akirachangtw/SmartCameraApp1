package com.akirachang.ai.photographer.presentation.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akirachang.ai.photographer.R
import com.akirachang.ai.photographer.domain.camera.CameraMode
import com.akirachang.ai.photographer.presentation.camera.ShutterButtonSection

/**
 * Bottom control bar — Layout Spec v2.2 §4.
 *
 * Structure (top → bottom):
 *   4.2a Pill Toggle zone (40dp)
 *   4.2b Shutter zone (80dp) — Gallery/PTF-Exit left, Shutter center, Flip right
 *   4.2c Navigation Safe Area (weight remainder)
 *
 * Background animates #0D0D0D ↔ #1A1714 on PTF mode switch (300ms FastOutSlowInEasing).
 */
@Composable
internal fun BottomControlBar(
    cameraMode: CameraMode,
    isAligned: Boolean,
    isCapturing: Boolean,
    onTogglePill: () -> Unit,
    onCapture: () -> Unit,
    onFlipCamera: () -> Unit,
    onExitPtf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Rule 14: all animation state at composable top before any conditional logic.
    val isPtfActive = cameraMode is CameraMode.PassToFriend

    val barBackground by animateColorAsState(
        targetValue = if (isPtfActive) Color(0xFF1A1714) else Color(0xFF0D0D0D),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bottomBarWarmShift",
    )
    val galleryAlpha by animateFloatAsState(
        targetValue = if (!isPtfActive) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "gallery_alpha",
    )
    val ptfExitAlpha by animateFloatAsState(
        targetValue = if (isPtfActive) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "ptf_exit_alpha",
    )
    val flipAlpha by animateFloatAsState(
        targetValue = if (!isPtfActive) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "flip_alpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(barBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 4.2a: Pill Toggle zone (40dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            PillToggle(
                cameraMode = cameraMode,
                onToggle = onTogglePill,
            )
        }

        // 4.2b: Shutter zone (80dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Left: Gallery (normal) cross-fades to PTF Exit (PTF)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.alpha(galleryAlpha),
                    enabled = !isPtfActive,
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = stringResource(R.string.gallery_cd),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                IconButton(
                    onClick = onExitPtf,
                    modifier = Modifier.alpha(ptfExitAlpha),
                    enabled = isPtfActive,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.ptf_exit_cd),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            // Center: Shutter button
            ShutterButtonSection(
                isAligned = isAligned,
                isCapturing = isCapturing,
                cameraMode = cameraMode,
                onCapture = onCapture,
            )

            // Right: Flip (normal) / hidden (PTF)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onFlipCamera,
                    modifier = Modifier.alpha(flipAlpha),
                    enabled = !isPtfActive,
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = stringResource(R.string.flip_camera_cd),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        // 4.2c: Navigation Safe Area — fills remainder of weight(1f) allocation
        Spacer(modifier = Modifier.weight(1f))
    }
}
