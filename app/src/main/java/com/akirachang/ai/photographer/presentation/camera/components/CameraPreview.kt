package com.akirachang.ai.photographer.presentation.camera.components

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.akirachang.ai.photographer.BuildConfig

/**
 * Camera Preview composable with dual-path rendering
 *
 * v2.8.6 Phase 2: PreviewView migration for Graphics memory fix
 * - PRIMARY PATH: PreviewView (native GPU rendering) for real devices
 * - FALLBACK PATH: Image(bitmap) for emulator or when native preview unavailable
 * - Graphics memory leak eliminated by bypassing Compose bitmap→texture upload
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
    supportsNativePreview: Boolean,
    onPreviewViewCreated: ((PreviewView) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onSizeChanged: ((Int, Int) -> Unit)? = null
) {
    // v2.8.6 Phase 2: Determine rendering mode based on feature flag and device capability
    val useNativePreview = BuildConfig.USE_PREVIEWVIEW_DISPLAY && supportsNativePreview

    if (useNativePreview) {
        // PRIMARY PATH: Native PreviewView rendering (Graphics memory fix)
        NativePreviewPath(
            onPreviewViewCreated = onPreviewViewCreated,
            modifier = modifier,
            onSizeChanged = onSizeChanged
        )
    } else {
        // FALLBACK PATH: Bitmap rendering (for emulator or when feature disabled)
        BitmapPreviewPath(
            previewBitmap = previewBitmap,
            modifier = modifier,
            onSizeChanged = onSizeChanged,
            showDebugInfo = !supportsNativePreview  // Show mode indicator when fallback is used
        )
    }
}

/**
 * Native PreviewView rendering path
 *
 * v2.8.6 Phase 2: Direct GPU rendering via CameraX Preview UseCase
 * - No CPU bitmap conversion
 * - No Compose texture upload
 * - Graphics memory stable at ~90-100 MB
 */
@Composable
private fun NativePreviewPath(
    onPreviewViewCreated: ((PreviewView) -> Unit)?,
    modifier: Modifier,
    onSizeChanged: ((Int, Int) -> Unit)?
) {
    val context = LocalContext.current

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
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    scaleType = PreviewView.ScaleType.FILL_CENTER

                    // Notify parent to connect CameraX Preview UseCase
                    onPreviewViewCreated?.invoke(this)

                    Log.i("CameraPreview", "[Phase 2] PreviewView created (native GPU rendering)")
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                // PreviewView updates handled by CameraX lifecycle
            }
        )

        // Debug indicator (only visible in debug builds)
        if (BuildConfig.DEBUG) {
            Text(
                text = "PreviewView Mode\n(Native GPU Rendering)",
                color = Color.Green,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp)
            )
        }
    }
}

/**
 * Bitmap rendering fallback path
 *
 * v2.8.6 Phase 2: Legacy Compose Image rendering
 * - Used for emulator (StaticImageSource)
 * - Used when USE_PREVIEWVIEW_DISPLAY = false
 * - Graphics memory: 88→215 MB (known leak, but required for emulator compatibility)
 */
@Composable
private fun BitmapPreviewPath(
    previewBitmap: Bitmap?,
    modifier: Modifier,
    onSizeChanged: ((Int, Int) -> Unit)?,
    showDebugInfo: Boolean
) {
    // v2.8.6 Phase 0: Bitmap churn tracking (only active in fallback path)
    var bitmapIdentities by remember { mutableStateOf(mutableSetOf<Int>()) }
    var frameCount by remember { mutableStateOf(0) }

    LaunchedEffect(previewBitmap) {
        if (BuildConfig.DEBUG && previewBitmap != null) {
            val identityHash = System.identityHashCode(previewBitmap)
            bitmapIdentities = (bitmapIdentities + identityHash).toMutableSet()
            frameCount++

            // Log every 60 frames (~2 seconds at 30 FPS)
            if (frameCount % 60 == 0) {
                Log.d(
                    "CameraPreview_BitmapChurn",
                    "Frames: $frameCount | Unique Bitmaps: ${bitmapIdentities.size} | " +
                    "Churn Rate: ${(bitmapIdentities.size.toFloat() / frameCount * 100).toInt()}%"
                )
            }
        }
    }

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

        // Debug indicator (only when fallback mode is active)
        if (BuildConfig.DEBUG && showDebugInfo) {
            Text(
                text = "Bitmap Mode\n(Emulator Fallback)",
                color = Color.Yellow,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp)
            )
        }
    }
}
