package com.akirachang.ai.photographer.domain.repository

import android.graphics.Bitmap
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
}
