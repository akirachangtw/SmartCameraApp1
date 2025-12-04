/**
 * ClassName: HDRWarningOverlay
 * Description: Visual overlay for HDR and lighting warnings
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - HDR recommendation badge
 * - Backlight warning
 * - Overexposure alert
 * - Low light indicator
 * - Color-coded by severity
 * - Animated entrance
 */
package local.mylab.smartcameraapp1.presentation.camera.components.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import local.mylab.smartcameraapp1.presentation.camera.model.LightingState

/**
 * Overlay displaying HDR and lighting warnings
 *
 * Shows color-coded warnings for:
 * - High contrast/backlight (Orange) → HDR recommended
 * - Overexposure (Red) → Reduce exposure
 * - Low light (Yellow) → Increase exposure/light
 *
 * @param lightingState Current lighting analysis state
 * @param modifier Modifier for layout customization
 */
@Composable
fun HDRWarningOverlay(
    lightingState: LightingState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = lightingState != LightingState.Normal,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (lightingState) {
                is LightingState.HighContrast -> {
                    WarningBadge(
                        icon = Icons.Default.WbSunny,
                        message = lightingState.message,
                        backgroundColor = Color(0xFFFF9800), // Orange
                        iconTint = Color.White
                    )
                }

                is LightingState.Overexposed -> {
                    WarningBadge(
                        icon = Icons.Default.Warning,
                        message = lightingState.message,
                        backgroundColor = Color(0xFFF44336), // Red
                        iconTint = Color.White
                    )
                }

                is LightingState.LowLight -> {
                    WarningBadge(
                        icon = Icons.Default.Lightbulb,
                        message = lightingState.message,
                        backgroundColor = Color(0xFFFFC107), // Amber
                        iconTint = Color.White
                    )
                }

                LightingState.Normal -> {
                    // No warning
                }
            }
        }
    }
}

/**
 * Warning badge component
 *
 * @param icon Icon to display
 * @param message Warning message in Chinese
 * @param backgroundColor Badge background color
 * @param iconTint Icon tint color
 */
@Composable
private fun WarningBadge(
    icon: ImageVector,
    message: String,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )

        Text(
            text = message,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
