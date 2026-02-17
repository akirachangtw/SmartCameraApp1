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
     * Set PreviewView reference for coordinate transformation
     *
     * v2.11.1 P0-1: Coordinate system unification (Bug fix for out-of-preview tap focus)
     * PreviewView.meteringPointFactory provides accurate coordinate mapping that respects
     * FIT_CENTER letterboxing and ViewPort cropping.
     *
     * For LiveCameraSource: Stores PreviewView reference for accurate metering point creation
     * For StaticImageSource: No-op (test images don't support autofocus)
     *
     * @param previewView PreviewView instance, or null to clear
     */
    fun setPreviewView(previewView: androidx.camera.view.PreviewView?) {
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

    /**
     * Get current device rotation for EXIF Orientation
     * v2.9.6 Phase 1: EXIF Orientation support
     * @return Surface rotation constant (ROTATION_0/90/180/270)
     */
    fun getCurrentRotation(): Int = android.view.Surface.ROTATION_0

    /**
     * Focus on a specific point in the preview
     * v2.9.9: Tap-to-Focus support
     *
     * For LiveCameraSource: Uses CameraX FocusMeteringAction to focus on the tapped point
     * For StaticImageSource: No-op (test images don't support autofocus)
     *
     * @param x X coordinate in the preview view (0.0 to viewWidth)
     * @param y Y coordinate in the preview view (0.0 to viewHeight)
     * @param viewWidth Width of the preview view in pixels
     * @param viewHeight Height of the preview view in pixels
     * @return Result indicating success or failure
     */
    suspend fun focusOnPoint(x: Float, y: Float, viewWidth: Int, viewHeight: Int): Result<Unit> {
        // Default no-op for StaticImageSource
        return Result.failure(UnsupportedOperationException("Focus not supported"))
    }

    /**
     * Lock AE/AF at a specific point (long press)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * For LiveCameraSource: Uses CameraX FocusMeteringAction with FLAG_AF | FLAG_AE, no auto-cancel
     * For StaticImageSource: No-op (test images don't support AE/AF lock)
     *
     * @param x X coordinate in the preview view (0.0 to viewWidth)
     * @param y Y coordinate in the preview view (0.0 to viewHeight)
     * @param viewWidth Width of the preview view in pixels
     * @param viewHeight Height of the preview view in pixels
     * @return Result indicating success or failure
     */
    suspend fun lockAEAF(x: Float, y: Float, viewWidth: Int, viewHeight: Int): Result<Unit> {
        // Default no-op for StaticImageSource
        return Result.failure(UnsupportedOperationException("AE/AF lock not supported"))
    }

    /**
     * Cancel focus and metering (unlock AE/AF)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * For LiveCameraSource: Calls CameraControl.cancelFocusAndMetering() to return to auto mode
     * For StaticImageSource: No-op (test images don't support AE/AF lock)
     *
     * @return Result indicating success or failure
     */
    suspend fun cancelFocusAndMetering(): Result<Unit> {
        // Default no-op for StaticImageSource
        return Result.failure(UnsupportedOperationException("Cancel focus/metering not supported"))
    }
}
