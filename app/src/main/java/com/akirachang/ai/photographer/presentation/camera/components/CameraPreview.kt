package com.akirachang.ai.photographer.presentation.camera.components

import android.view.ViewTreeObserver
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * v2.3.1: Fixed size callback - use ViewTreeObserver for reliable size detection
 */
@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier,
    onSizeChanged: ((Int, Int) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // v2.3.1: Use DisposableEffect to properly track size changes
    DisposableEffect(previewViewRef) {
        val view = previewViewRef ?: return@DisposableEffect onDispose { }

        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            if (view.width > 0 && view.height > 0) {
                onSizeChanged?.invoke(view.width, view.height)
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                // v2.2.1: Changed to FILL_CENTER to fill screen without black bars
                // This prevents overlays from extending beyond preview area
                // VIEW_REFERENCED coordinates still work correctly with this scale type
                scaleType = PreviewView.ScaleType.FILL_CENTER

                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)

                // v2.3.1: Store reference for size tracking
                previewViewRef = this
            }
        },
        modifier = modifier
    )
}
