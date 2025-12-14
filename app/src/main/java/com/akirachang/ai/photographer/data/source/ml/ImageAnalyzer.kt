/**
 * ClassName: ImageAnalyzer
 * Description: Image analysis implementation for CameraX with ML Kit detection
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 1.1.0
 *
 * Modifications:
 * - 2025-11-17: Initial creation with 10 FPS frame throttling
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

    /**
     * 設置偵測結果回調
     *
     * @param callback 當偵測完成時會被呼叫的回調函數
     */
    fun setDetectionCallback(callback: (DetectionResult) -> Unit) {
        onDetectionResult = callback
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

        // 在背景協程中執行偵測
        analysisScope.launch {
            try {
                val result = mlKitDetector.detect(imageProxy)

                if (result != null && result.hasDetections) {
                    // 在主執行緒上發送結果
                    onDetectionResult?.invoke(result)
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
