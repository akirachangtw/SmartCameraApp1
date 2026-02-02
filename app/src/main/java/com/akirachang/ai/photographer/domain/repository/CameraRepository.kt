package com.akirachang.ai.photographer.domain.repository

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.model.GuidelineAnalysisResult
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.akirachang.ai.photographer.data.opencv.TrackingResult

interface CameraRepository {
    /**
     * 獲取相機預覽流（統一介面）
     * v2.5.1: Camera Input 抽象層整合
     *
     * 提供統一的 Bitmap 預覽流，無論來源是實機 CameraX 或模擬器測試圖片
     */
    fun getPreviewStream(): Flow<Bitmap>

    /**
     * 獲取 FPS 效能指標流（v2.8.4 Quick Win）
     *
     * 提供即時 FPS 監控用於效能驗證
     * Null 當相機未啟動或指標不可用
     */
    fun getFpsStream(): StateFlow<Float?>

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
     * Guideline analysis results (OpenCV Hough Line Transform)
     * v2.7.0 Phase 2 Sprint 4
     *
     * Provides real-time composition guideline detection results.
     * Null when no analysis is running or feature flag is disabled.
     */
    val guidelineResults: StateFlow<GuidelineAnalysisResult?>

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
     * Check if native preview is supported
     *
     * v2.8.6 Phase 2: PreviewView migration
     *
     * @return true for LiveCameraSource (supports PreviewView), false for StaticImageSource (bitmap only)
     */
    fun supportsNativePreview(): Boolean

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
     * Set optical flow tracking result callback
     * v2.x.x Phase 4: Optical flow point tracking
     *
     * @param callback Callback invoked when tracking result is available
     */
    fun setOpticalFlowCallback(callback: (TrackingResult) -> Unit)

    /**
     * Set preview size for coordinate mapping
     * v2.x.x Phase 4: Required for PreviewView ↔ ImageAnalysis coordinate conversion
     *
     * @param width Preview width in pixels
     * @param height Preview height in pixels
     */
    fun setPreviewSize(width: Int, height: Int)

    /**
     * Start optical flow tracking at a specific point
     * v2.x.x Phase 4: Tap-to-track point feature
     *
     * @param x X coordinate in PreviewView (0.0 to previewWidth)
     * @param y Y coordinate in PreviewView (0.0 to previewHeight)
     */
    fun startOpticalFlowTracking(x: Float, y: Float)

    /**
     * Stop optical flow tracking
     * v2.x.x Phase 4: Stop tracking and release resources
     */
    fun stopOpticalFlowTracking()

    /**
     * Check if optical flow tracking is active
     * v2.x.x Phase 4: Query tracking state
     *
     * @return True if tracking is active, false otherwise
     */
    fun isOpticalFlowTracking(): Boolean
}
