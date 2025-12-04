package local.mylab.smartcameraapp1.presentation.camera.components

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Camera Preview composable
 *
 * v1.4.0: Simplified - no longer needs size tracking callback since
 * MlKitAnalyzer with VIEW_REFERENCED handles coordinate transformation automatically
 *
 * v2.0 Phase 2: Added onSizeChanged callback for composition analysis accuracy
 */
@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier,
    onSizeChanged: ((Int, Int) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                // v2.2.1: Changed to FILL_CENTER to fill screen without black bars
                // This prevents overlays from extending beyond preview area
                // VIEW_REFERENCED coordinates still work correctly with this scale type
                scaleType = PreviewView.ScaleType.FILL_CENTER

                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier,
        update = { previewView ->
            // Phase 2: Report size changes for accurate composition analysis
            onSizeChanged?.invoke(previewView.width, previewView.height)
        }
    )
}
