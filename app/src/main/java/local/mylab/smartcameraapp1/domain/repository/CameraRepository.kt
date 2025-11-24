package local.mylab.smartcameraapp1.domain.repository

import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import kotlinx.coroutines.flow.StateFlow
import local.mylab.smartcameraapp1.domain.model.CaptureResult
import local.mylab.smartcameraapp1.domain.model.DetectionResult

interface CameraRepository {
    /**
     * 獲取相機控制器
     */
    fun getCameraController(): LifecycleCameraController

    /**
     * 拍攝照片
     */
    suspend fun takePicture(): CaptureResult

    /**
     * 切換前後鏡頭
     */
    fun switchCamera()

    /**
     * 獲取當前鏡頭選擇器
     */
    fun getCurrentCameraSelector(): CameraSelector

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
}
