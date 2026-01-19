package com.akirachang.ai.photographer.data.source.camera

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Camera Input Source Interface
 *
 * Abstraction for different camera input sources:
 * - LiveCameraSource: Real camera using CameraX
 * - StaticImageSource: Test images for emulator/testing
 *
 * UI and composition engine depend on this abstraction, not directly on CameraX.
 */
interface CameraInputSource {

    /**
     * Get camera preview stream as Flow<Bitmap>
     *
     * @return Flow that emits preview frames as Bitmap
     */
    fun getPreviewStream(): Flow<Bitmap>

    /**
     * Get FPS performance metrics stream
     *
     * v2.8.4: Performance monitoring for Quick Win validation
     * @return StateFlow that emits current FPS, or null if not available
     */
    fun getFpsStream(): StateFlow<Float?> = kotlinx.coroutines.flow.MutableStateFlow(null)

    /**
     * Capture a photo
     *
     * @return Result containing captured Bitmap, or error
     */
    suspend fun takePicture(): Result<Bitmap>

    /**
     * Stop the preview stream
     */
    fun stopPreview()

    /**
     * Set camera zoom ratio
     *
     * @param ratio Zoom ratio (1.0 = no zoom)
     * @return Result indicating success or failure
     */
    suspend fun setZoomRatio(ratio: Float): Result<Unit>

    /**
     * Switch between front and back camera
     *
     * For LiveCameraSource: Actually switches the physical camera
     * For StaticImageSource: No-op (test images don't have front/back distinction)
     *
     * v2.6.1: Changed to suspend function to allow async camera rebinding
     */
    suspend fun switchCamera()

    /**
     * Enable HDR mode (via exposure compensation)
     *
     * v2.6.2: Camera Input abstraction for HDR control
     * For LiveCameraSource: Sets exposure compensation to +1
     * For StaticImageSource: No-op (test images don't have HDR control)
     *
     * @return Result indicating success or failure
     */
    fun enableHDR(): Result<Unit>

    /**
     * Disable HDR mode (reset exposure compensation)
     *
     * v2.6.2: Camera Input abstraction for HDR control
     * For LiveCameraSource: Resets exposure compensation to 0
     * For StaticImageSource: No-op (test images don't have HDR control)
     *
     * @return Result indicating success or failure
     */
    fun disableHDR(): Result<Unit>

    /**
     * Set PreviewView surface provider for native preview rendering
     *
     * v2.8.6 Phase 2: PreviewView migration for Graphics memory fix
     *
     * For LiveCameraSource: Connects CameraX Preview UseCase to PreviewView
     * For StaticImageSource: No-op (uses bitmap preview)
     *
     * @param surfaceProvider PreviewView's surface provider, or null to clear
     */
    fun setPreviewSurfaceProvider(surfaceProvider: androidx.camera.core.Preview.SurfaceProvider?) {
        // Default no-op for StaticImageSource
    }

    /**
     * Check if native preview is supported
     *
     * v2.8.6 Phase 2: PreviewView migration
     *
     * @return true for LiveCameraSource (supports PreviewView), false for StaticImageSource (bitmap only)
     */
    fun supportsNativePreview(): Boolean = false
}
