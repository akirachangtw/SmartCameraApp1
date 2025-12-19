package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged

/**
 * Camera Preview composable
 *
 * v2.5.1: Refactored to support unified camera input abstraction
 * - Accepts Bitmap for both test images (emulator) and live camera (device)
 * - Removes direct dependency on CameraX PreviewView
 * - Enables proper test image display in emulator builds
 *
 * v2.3.1: Fixed size callback - use ViewTreeObserver for reliable size detection
 * v2.0 Phase 2: Added onSizeChanged callback for composition analysis accuracy
 */
@Composable
fun CameraPreview(
    previewBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    onSizeChanged: ((Int, Int) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    onSizeChanged?.invoke(size.width, size.height)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Camera preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Loading state - waiting for first frame
            CircularProgressIndicator()
        }
    }
}

