package com.akirachang.ai.photographer.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraSelector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.model.LightingState

interface CameraRepository {
    /**
     * 獲取相機預覽流（統一介面）
     * v2.5.1: Camera Input 抽象層整合
     *
     * 提供統一的 Bitmap 預覽流，無論來源是實機 CameraX 或模擬器測試圖片
     */
    fun getPreviewStream(): Flow<Bitmap>

    /**
     * 拍攝照片
     */
    suspend fun takePicture(): CaptureResult

    /**
     * 切換前後鏡頭
     *
     * v2.6.1: Changed to suspend function to allow async camera rebinding
     */
    suspend fun switchCamera()

    /**
     * 獲取當前鏡頭
     *
     * v2.5.1: Returns platform-agnostic CameraLens instead of CameraSelector
     */
    fun getCurrentCamera(): CameraLens

    /**
     * 選擇相機鏡頭（前置或後置）
     *
     * v2.5.1: Uses platform-agnostic CameraLens
     */
    fun selectCamera(lens: CameraLens)


    /**
     * 設置閃光燈模式
     */
    fun setFlashMode(flashMode: Int)

    /**
     * 獲取當前閃光燈模式
     */
    fun getFlashMode(): Int

    /**
     * 開始影像分析（ML Kit 偵測）
     */
    fun startImageAnalysis()

    /**
     * 停止影像分析
     */
    fun stopImageAnalysis()

    /**
     * 偵測結果的狀態流
     */
    val detectionResults: StateFlow<DetectionResult?>

    /**
     * Whether the current lens is fixed-focus (no VCM / AF hardware)
     * v2.12.0-11: StateFlow path for ViewModel to observe fixed-focus capability
     *
     * Emits true after bindToLifecycle() when FixedFocusDetector confirms the lens
     * cannot perform autofocus (e.g. Pixel 9a front camera).
     * Emits false for emulator / StaticImageSource.
     */
    val isFixedFocusLens: StateFlow<Boolean>

    /**
     * 啟用 HDR 模式（透過曝光補償）
     * v2.3.0: 逆光場景自動 HDR
     */
    fun enableHDRMode()

    /**
     * 停用 HDR 模式（重設曝光補償）
     * v2.3.0: 逆光場景自動 HDR
     */
    fun disableHDRMode()

    /**
     * 檢查是否應啟用自動 HDR
     * v2.3.0: 逆光場景自動 HDR
     */
    fun shouldEnableAutoHDR(lightingState: LightingState): Boolean

    /**
     * 使用 ML Kit 進行人臉偵測
     * v2.5.1 Phase 7: Bitmap-based analysis
     */
    suspend fun detectFaces(image: com.google.mlkit.vision.common.InputImage): List<com.google.mlkit.vision.face.Face>

    /**
     * 使用 ML Kit 進行物件偵測
     * v2.5.1 Phase 7: Bitmap-based analysis
     */
    suspend fun detectObjects(image: com.google.mlkit.vision.common.InputImage): List<com.google.mlkit.vision.objects.DetectedObject>

    /**
     * 綁定相機到生命週期
     * v2.6.1: Required for LiveCameraSource to start camera
     */
    fun bindToLifecycle(lifecycleOwner: androidx.lifecycle.LifecycleOwner)

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
    fun setPreviewSurfaceProvider(surfaceProvider: androidx.camera.core.Preview.SurfaceProvider?)

    /**
     * Set PreviewView reference for accurate coordinate transformation
     *
     * v2.11.1 P0-1: Coordinate system unification (Bug fix for out-of-preview tap focus)
     * PreviewView.meteringPointFactory provides accurate mapping that accounts for:
     * - FIT_CENTER letterboxing (black bars)
     * - ViewPort cropping (aspect ratio modes)
     * - Display rotation
     *
     * For LiveCameraSource: Stores PreviewView reference for accurate metering point creation
     * For StaticImageSource: No-op (test images don't support autofocus)
     *
     * @param previewView PreviewView instance, or null to clear
     */
    fun setPreviewView(previewView: androidx.camera.view.PreviewView?)

    /**
     * Check if native preview is supported
     *
     * v2.8.6 Phase 2: PreviewView migration
     *
     * @return true for LiveCameraSource (supports PreviewView), false for StaticImageSource (bitmap only)
     */
    fun supportsNativePreview(): Boolean

    /**
     * Set aspect ratio mode and rebind camera
     * v2.10.0: UseCaseGroup + ViewPort architecture
     *
     * @param mode Target aspect ratio mode (4:3, 16:9, 1:1)
     */
    suspend fun setAspectRatioMode(mode: com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode)

    /**
     * Focus on a specific point in the preview
     * v2.9.9: Tap-to-Focus support
     *
     * @param x X coordinate in the preview view (0.0 to viewWidth)
     * @param y Y coordinate in the preview view (0.0 to viewHeight)
     * @param viewWidth Width of the preview view in pixels
     * @param viewHeight Height of the preview view in pixels
     * @return Result indicating success or failure
     */
    suspend fun focusOnPoint(x: Float, y: Float, viewWidth: Int, viewHeight: Int): Result<Unit>

    /**
     * Lock AE/AF at a specific point (long press)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * Locks auto exposure and auto focus at the specified point.
     * Creates a persistent focus metering action without auto-cancel duration.
     * Use cancelFocusAndMetering() to unlock and return to auto mode.
     *
     * @param x X coordinate in the preview view (0.0 to viewWidth)
     * @param y Y coordinate in the preview view (0.0 to viewHeight)
     * @param viewWidth Width of the preview view in pixels
     * @param viewHeight Height of the preview view in pixels
     * @return Result indicating success or failure
     */
    suspend fun lockAEAF(x: Float, y: Float, viewWidth: Int, viewHeight: Int): Result<Unit>

    /**
     * Cancel focus and metering (unlock AE/AF)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * Cancels any active focus metering action and returns to auto mode.
     * Used to unlock AE/AF after lockAEAF() or clear focus after focusOnPoint().
     *
     * @return Result indicating success or failure
     */
    suspend fun cancelFocusAndMetering(): Result<Unit>

    /**
     * Set preview size for coordinate mapping
     */
    fun setPreviewSize(width: Int, height: Int)

    /**
     * Read EXIF metadata from captured photo
     * v2.11.0 Phase 3: EXIF information display feature
     *
     * Extracts comprehensive EXIF metadata including file info, GPS data,
     * camera information, and capture parameters.
     *
     * @param uri Content URI of the captured photo
     * @return ExifInfo object with all metadata, or null if reading fails
     */
    suspend fun readExifInfo(uri: Uri): com.akirachang.ai.photographer.domain.model.ExifInfo?

    /**
     * Get CameraControl for direct hardware focus/metering operations.
     * v2.12.0-15 SV-S5a-R2: Exposed for CameraXFocusController in new focus pipeline.
     *
     * Returns null if camera is not yet bound to lifecycle (lifecycle-safe).
     * For StaticImageSource: always returns null (no hardware camera).
     */
    fun getCameraControl(): androidx.camera.core.CameraControl?

    /**
     * Get MeteringPointFactory from PreviewView for accurate focus point mapping.
     * v2.12.0-15 SV-S5-Final: Exposed for CameraXFocusController in FocusModule DI binding.
     *
     * Returns null if PreviewView is not set or camera is not yet bound.
     * For StaticImageSource: always returns null (no hardware camera).
     */
    fun getMeteringPointFactory(): androidx.camera.core.MeteringPointFactory?

    /**
     * D3-T3: Return hardware focal length factor (focalLengthMm / sensorWidthMm).
     * Multiply by previewWidth (px) to obtain focalLengthPixels for ImuCompensator.
     * Returns null in two distinct cases:
     *   1. Temporary: camera not yet bound (unbind state); will be updated after next bind.
     *   2. Permanent: hardware does not expose LENS_INFO_AVAILABLE_FOCAL_LENGTHS;
     *      caller must apply fail-safe degradation (previewWidth / 2f).
     * For StaticImageSource: always returns null (permanent).
     */
    fun getFocalLengthFactor(): Float?
}
