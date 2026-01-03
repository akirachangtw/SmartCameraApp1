package com.akirachang.ai.photographer.data.source.camera

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

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
}
