/**
 * ClassName: ImageAnalyzer
 * Description: Image analysis implementation for CameraX with ML Kit detection
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 1.1.0
 *
 * Modifications:
 * - 2025-11-17: Initial creation with 10 FPS frame throttling
 * - v2.x.x Phase 3: Added optical flow point tracking integration
 */
package com.akirachang.ai.photographer.data.source.ml

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.data.opencv.OpticalFlowTracker
import com.akirachang.ai.photographer.data.opencv.TrackingResult
import com.akirachang.ai.photographer.data.source.camera.toGrayMat
import com.akirachang.ai.photographer.util.CoordinateMapper
import org.opencv.core.Point
import javax.inject.Inject

/**
 * 影像分析器
 *
 * 實作 CameraX 的 ImageAnalysis.Analyzer 介面
 * 負責接收相機幀並執行 ML Kit 偵測
 *
 * 特性：
 * - 幀率節流（預設每秒 10 幀）
 * - 背景執行緒處理
 * - 自動記憶體管理
 * - v2.x.x Phase 3: Optical flow point tracking
 */
class ImageAnalyzer @Inject constructor(
    private val mlKitDetector: MLKitDetector
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "ImageAnalyzer"
        private const val TARGET_FPS = 10 // 目標分析幀率
        private const val FRAME_INTERVAL_MS = 1000L / TARGET_FPS // 100ms
    }

    // 分析協程作用域（使用 SupervisorJob 避免單一失敗影響全域）
    private val analysisScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 上次分析時間戳
    private var lastAnalysisTimestamp = 0L

    // 偵測結果回調
    private var onDetectionResult: ((DetectionResult) -> Unit)? = null

    // 是否正在分析中
    @Volatile
    private var isAnalyzing = false

    // v2.x.x Phase 3: Optical flow tracking
    private var opticalFlowTracker: OpticalFlowTracker? = null
    private var onOpticalFlowResult: ((TrackingResult) -> Unit)? = null
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    /**
     * 設置偵測結果回調
     *
     * @param callback 當偵測完成時會被呼叫的回調函數
     */
    fun setDetectionCallback(callback: (DetectionResult) -> Unit) {
        onDetectionResult = callback
    }

    /**
     * Set optical flow tracking result callback
     *
     * v2.x.x Phase 3: Optical flow point tracking
     *
     * @param callback Callback invoked when tracking result is available
     */
    fun setOpticalFlowCallback(callback: (TrackingResult) -> Unit) {
        onOpticalFlowResult = callback
    }

    /**
     * Set preview size for coordinate mapping
     *
     * v2.x.x Phase 3: Required for PreviewView ↔ ImageAnalysis coordinate conversion
     *
     * @param width Preview width in pixels
     * @param height Preview height in pixels
     */
    fun setPreviewSize(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
        Log.d(TAG, "Preview size set: ${width}x${height}")
    }

    /**
     * Start optical flow tracking at a specific point
     *
     * v2.x.x Phase 3: Tap-to-track point feature
     *
     * @param x X coordinate in PreviewView (0.0 to previewWidth)
     * @param y Y coordinate in PreviewView (0.0 to previewHeight)
     */
    fun startOpticalFlowTracking(x: Float, y: Float) {
        if (previewWidth == 0 || previewHeight == 0) {
            Log.w(TAG, "Cannot start tracking: preview size not set")
            return
        }

        if (imageWidth == 0 || imageHeight == 0) {
            Log.w(TAG, "Cannot start tracking: image size not yet known (wait for first frame)")
            return
        }

        // Convert PreviewView coordinates to ImageAnalysis coordinates
        val imagePoint = CoordinateMapper.previewToImage(
            x, y,
            previewWidth, previewHeight,
            imageWidth, imageHeight
        )

        // Create tracker if needed
        if (opticalFlowTracker == null) {
            opticalFlowTracker = OpticalFlowTracker()
        }

        // Start tracking
        opticalFlowTracker?.startTracking(imagePoint, imageWidth, imageHeight)
        Log.i(TAG, "Optical flow tracking started at preview ($x, $y) -> image (${imagePoint.x}, ${imagePoint.y})")
    }

    /**
     * Stop optical flow tracking
     *
     * v2.x.x Phase 3: Stop tracking and release resources
     */
    fun stopOpticalFlowTracking() {
        opticalFlowTracker?.stopTracking()
        Log.i(TAG, "Optical flow tracking stopped")
    }

    /**
     * Check if optical flow tracking is active
     *
     * v2.x.x Phase 3: Query tracking state
     *
     * @return True if tracking is active, false otherwise
     */
    fun isOpticalFlowTracking(): Boolean {
        return opticalFlowTracker?.isTracking() ?: false
    }

    /**
     * 分析影像幀
     *
     * 此方法會被 CameraX 在相機幀可用時呼叫
     *
     * @param imageProxy 影像代理（包含影像數據與元資訊）
     */
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // 幀率節流：檢查是否達到分析間隔
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTimestamp < FRAME_INTERVAL_MS) {
            imageProxy.close() // 重要：必須關閉跳過的幀
            return
        }

        // 跳過正在分析的幀（避免積壓）
        if (isAnalyzing) {
            imageProxy.close()
            return
        }

        // 檢查影像是否可用
        if (imageProxy.image == null) {
            Log.w(TAG, "ImageProxy has null image")
            imageProxy.close()
            return
        }

        // 更新時間戳與狀態
        lastAnalysisTimestamp = currentTime
        isAnalyzing = true

        Log.i(TAG, "Frame analysis started at timestamp: $currentTime")

        // v2.x.x Phase 3: Store image dimensions for coordinate mapping
        if (imageWidth == 0 || imageHeight == 0) {
            imageWidth = imageProxy.width
            imageHeight = imageProxy.height
            Log.d(TAG, "Image size captured: ${imageWidth}x${imageHeight}")
        }

        // 在背景協程中執行偵測
        analysisScope.launch {
            try {
                // 1. ML Kit detection
                val result = mlKitDetector.detect(imageProxy)

                if (result != null && result.hasDetections) {
                    // 在主執行緒上發送結果
                    onDetectionResult?.invoke(result)
                }

                // 2. v2.x.x Phase 3: Optical flow tracking (if active)
                if (isOpticalFlowTracking()) {
                    try {
                        // Convert ImageProxy to grayscale Mat
                        val grayMat = imageProxy.toGrayMat()

                        try {
                            // Track point
                            val trackingResult = opticalFlowTracker?.track(grayMat)

                            if (trackingResult != null) {
                                // Convert tracking result coordinates back to PreviewView coordinates
                                val (previewX, previewY) = if (trackingResult.isValid) {
                                    CoordinateMapper.imageToPreview(
                                        trackingResult.point.x.toFloat(),
                                        trackingResult.point.y.toFloat(),
                                        imageWidth, imageHeight,
                                        previewWidth, previewHeight
                                    )
                                } else {
                                    // Tracking lost - keep previous coordinates or use (0, 0)
                                    Pair(0f, 0f)
                                }

                                // Create result with PreviewView coordinates
                                val previewTrackingResult = TrackingResult(
                                    point = Point(previewX.toDouble(), previewY.toDouble()),
                                    quality = trackingResult.quality,
                                    distance = trackingResult.distance,
                                    isValid = trackingResult.isValid
                                )

                                // Invoke callback
                                onOpticalFlowResult?.invoke(previewTrackingResult)
                            }
                        } finally {
                            // Release Mat to prevent memory leak
                            grayMat.release()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Optical flow tracking failed", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
            } finally {
                // 必須關閉 ImageProxy 以釋放資源
                imageProxy.close()
                isAnalyzing = false
            }
        }
    }

    /**
     * 清理資源
     *
     * 應在不再需要分析時呼叫
     */
    fun release() {
        try {
            analysisScope.cancel()
            onDetectionResult = null

            // v2.x.x Phase 3: Release optical flow tracker
            stopOpticalFlowTracking()
            opticalFlowTracker = null
            onOpticalFlowResult = null

            Log.i(TAG, "ImageAnalyzer released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release ImageAnalyzer", e)
        }
    }

    /**
     * 取得當前分析統計資訊
     */
    fun getAnalysisStats(): AnalysisStats {
        return AnalysisStats(
            targetFps = TARGET_FPS,
            frameIntervalMs = FRAME_INTERVAL_MS,
            isAnalyzing = isAnalyzing,
            lastAnalysisTimestamp = lastAnalysisTimestamp
        )
    }

    /**
     * 分析統計資訊資料類別
     */
    data class AnalysisStats(
        val targetFps: Int,
        val frameIntervalMs: Long,
        val isAnalyzing: Boolean,
        val lastAnalysisTimestamp: Long
    )
}
