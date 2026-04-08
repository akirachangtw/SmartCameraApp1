package com.akirachang.ai.photographer.data.source.ml

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.akirachang.ai.photographer.domain.model.DetectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ImageAnalyzer — Cueshot D0 (OpticalFlow removed, pure ML Kit path)
 *
 * Legacy emulator path (Device path uses SmartCameraAnalyzer via LiveCameraSource).
 * OpticalFlow / OpenCV integration removed per 焦土政策.
 */
class ImageAnalyzer @Inject constructor(
    private val mlKitDetector: MLKitDetector,
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "ImageAnalyzer"
        private const val FRAME_INTERVAL_MS = 100L // 10 FPS
    }

    private val analysisScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastAnalysisTimestamp = 0L
    private var onDetectionResult: ((DetectionResult) -> Unit)? = null

    @Volatile
    private var isAnalyzing = false

    fun setDetectionCallback(callback: (DetectionResult) -> Unit) {
        onDetectionResult = callback
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTimestamp < FRAME_INTERVAL_MS || isAnalyzing || imageProxy.image == null) {
            imageProxy.close()
            return
        }
        lastAnalysisTimestamp = currentTime
        isAnalyzing = true

        analysisScope.launch {
            try {
                val result = mlKitDetector.detect(imageProxy)
                if (result != null && result.hasDetections) {
                    onDetectionResult?.invoke(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
            } finally {
                imageProxy.close()
                isAnalyzing = false
            }
        }
    }

    fun release() {
        analysisScope.cancel()
        onDetectionResult = null
    }
}
