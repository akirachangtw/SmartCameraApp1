/**
 * ClassName: SmartCameraAnalyzer
 * Description: Custom ImageAnalysis.Analyzer that integrates ML Kit detection with lighting analysis
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - ML Kit face and object detection
 * - Lighting analysis with histogram calculation
 * - Exposure detection (backlight, overexposure, low light)
 * - Throttled analysis for performance (10 FPS)
 * - Proper resource management
 *
 * Architecture:
 * - Replaces MlKitAnalyzer to gain ImageProxy access
 * - Provides both DetectionResult and LightingState
 * - Uses ViewModel callbacks for state updates
 */
package com.akirachang.ai.photographer.data.source.camera

import android.os.Build
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extension function to convert Google Tasks to Kotlin Coroutines
 */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

/**
 * Custom analyzer that performs both ML Kit detection and lighting analysis
 *
 * @param faceDetector ML Kit face detector
 * @param objectDetector ML Kit object detector
 * @param analyzeLightingUseCase Lighting analysis use case
 * @param onDetectionResult Callback for detection results
 * @param onLightingResult Callback for lighting analysis results
 * @param minObjectConfidence Minimum confidence threshold for objects (default 0.5)
 */
class SmartCameraAnalyzer(
    private val faceDetector: FaceDetector,
    private val objectDetector: ObjectDetector,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val onDetectionResult: (DetectionResult) -> Unit,
    private val onLightingResult: (LightingState) -> Unit,
    private val minObjectConfidence: Float = 0.5f
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "SmartCameraAnalyzer"

        // Pixel 9a: Increase throttle to 500ms (2 FPS) to reduce ML Kit TPU memory usage
        // Other devices: Keep at 100ms (10 FPS) for responsive AI analysis
        private val THROTTLE_INTERVAL_MS = if (Build.MODEL == "Pixel 9a") 500L else 100L
    }

    private val analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val isProcessing = AtomicBoolean(false)
    private var lastAnalysisTime = 0L

    // Cached detection results for lighting analysis
    private var cachedDetectionResult: DetectionResult? = null

    override fun analyze(imageProxy: ImageProxy) {
        // Throttle analysis to 10 FPS
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < THROTTLE_INTERVAL_MS) {
            imageProxy.close()
            return
        }

        // Skip if already processing
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        lastAnalysisTime = currentTime

        analysisScope.launch {
            try {
                // Create InputImage for ML Kit
                val inputImage = InputImage.fromMediaImage(
                    imageProxy.image!!,
                    imageProxy.imageInfo.rotationDegrees
                )

                // Parallel ML Kit detection with proper Task handling
                val faces = try {
                    faceDetector.process(inputImage).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Face detection failed", e)
                    emptyList()
                }

                val objects = try {
                    objectDetector.process(inputImage).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Object detection failed", e)
                    emptyList()
                }

                // Filter low confidence objects
                val filteredObjects = objects.filter { obj ->
                    val confidence = obj.labels.firstOrNull()?.confidence ?: 0f
                    confidence >= minObjectConfidence
                }

                // Create detection result with VIEW_REFERENCED coordinates
                // Note: ML Kit returns coordinates in image space, but we're using
                // CameraX's coordinate system which handles the transformation
                val detectionResult = DetectionResult(
                    faces = faces,
                    objects = filteredObjects,
                    imageWidth = imageProxy.width,
                    imageHeight = imageProxy.height,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                    timestamp = currentTime
                )

                // Cache for lighting analysis
                cachedDetectionResult = detectionResult

                // Notify detection result on main thread
                onDetectionResult(detectionResult)

                // Perform lighting analysis
                // Use analyzeWithSubject if faces detected, otherwise basic analysis
                val lightingState = if (faces.isNotEmpty()) {
                    analyzeLightingUseCase.analyzeWithSubject(imageProxy, detectionResult)
                } else {
                    analyzeLightingUseCase(imageProxy)
                }

                // Notify lighting result on main thread
                onLightingResult(lightingState)

                Log.d(
                    TAG,
                    "Analysis complete: ${faces.size} faces, ${filteredObjects.size} objects, " +
                            "lighting: ${lightingState.javaClass.simpleName}"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Analysis error", e)
            } finally {
                imageProxy.close()
                isProcessing.set(false)
            }
        }
    }

    /**
     * Clean up resources
     * Call this when stopping analysis
     */
    fun cleanup() {
        analysisScope.cancel()
        Log.d(TAG, "Analyzer cleaned up")
    }
}
