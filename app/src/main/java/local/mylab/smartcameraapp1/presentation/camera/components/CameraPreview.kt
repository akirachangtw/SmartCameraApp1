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
                // Set scale type to FIT_CENTER to maintain aspect ratio
                // This ensures MlKitAnalyzer's VIEW_REFERENCED coordinates work correctly
                scaleType = PreviewView.ScaleType.FIT_CENTER

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
