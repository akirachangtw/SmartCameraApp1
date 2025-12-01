/**
 * ClassName: CompositionHint
 * Description: Displays composition analysis hints to guide user
 * Author: Claude Code
 * Date: 2025-11-25
 * Version: 2.1.1
 *
 * Features:
 * - Real-time composition feedback
 * - Professional Chinese text hints (v2.1.1)
 * - Color-coded feedback (green for good, amber for needs adjustment)
 * - Enhanced visibility with semi-transparent background
 *
 * Modifications:
 * - 2025-11-25: Initial creation for v1.5.0 prototype
 * - 2025-12-02: Update to Chinese professional suggestions (v2.1.1)
 */
package local.mylab.smartcameraapp1.presentation.camera.components.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import local.mylab.smartcameraapp1.presentation.camera.model.CompositionState

/**
 * Composition hint overlay composable
 *
 * Displays real-time feedback about the current composition based on
 * multi-rule weighted scoring system. Provides professional Chinese
 * suggestions aligned with the scene composition matrix.
 *
 * v2.1.1: Text-first design approach - hints are the primary guidance,
 * with directional arrows as supplementary visual aids.
 *
 * @param compositionState Current composition analysis state
 * @param modifier Modifier for the hint container
 */
@Composable
fun CompositionHint(
    compositionState: CompositionState,
    modifier: Modifier = Modifier
) {
    val (hintText, backgroundColor) = when (compositionState) {
        is CompositionState.Good -> {
            "良好構圖 ✓" to Color(0xFF4CAF50).copy(alpha = 0.9f) // Green
        }
        is CompositionState.NeedsAdjustment -> {
            compositionState.message to Color(0xFFFFC107).copy(alpha = 0.9f) // Amber
        }
        is CompositionState.NoSubject -> {
            "未偵測到主體" to Color(0xFF9E9E9E).copy(alpha = 0.8f) // Gray
        }
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = hintText,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Preview composable for CompositionHint states
 *
 * Note: This is for development preview only.
 * Uncomment the @Preview annotation to view in Android Studio preview pane.
 */
/*
@Preview(showBackground = true)
@Composable
private fun CompositionHintPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Good composition
        CompositionHint(
            compositionState = CompositionState.Good
        )

        // Needs adjustment
        CompositionHint(
            compositionState = CompositionState.NeedsAdjustment("Adjust Position")
        )

        // No subject
        CompositionHint(
            compositionState = CompositionState.NoSubject
        )
    }
}
*/
