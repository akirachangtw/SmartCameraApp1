/**
 * ClassName: SmartCameraAnalyzer
 * Description: Custom ImageAnalysis.Analyzer — ML Kit detection + lighting analysis
 * Author: Claude Code
 * Version: Cueshot D0 (OpenCV / OpticalFlow removed; pure ML Kit pipeline)
 *
 * Architecture: Dumb Pipe (v2.12.0-15 §17.12.5)
 *   1. Receive CameraX frame
 *   2. Feed to ML Kit
 *   3. Output raw DetectionResult (image-space coords) + LightingState via callbacks
 * All coordinate mapping, target ranking, and policy decisions handled upstream (CameraViewModel).
 */
package com.akirachang.ai.photographer.data.source.camera

import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.objects.ObjectDetector
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}

class SmartCameraAnalyzer(
    private val faceDetector: FaceDetector,
    private val objectDetector: ObjectDetector,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val onDetectionResult: (DetectionResult) -> Unit,
    private val onLightingResult: (LightingState) -> Unit,
    private val minObjectConfidence: Float = 0.5f,
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "SmartCameraAnalyzer"
        private const val DEFAULT_THROTTLE_MS    = 200L
        private const val MIN_THROTTLE_MS        = 100L
        private const val MAX_THROTTLE_MS        = 500L
        private const val WARMUP_FRAMES          = 3
        private const val EMA_ALPHA              = 0.2
        private const val SAFETY_FACTOR          = 1.25
        private const val DEADBAND_RATIO         = 0.10
        private const val MAX_EXPAND_PER_FRAME   = 50L
        private const val MAX_CONTRACT_PER_FRAME = 20L
        private const val PANIC_RATIO            = 1.5
        private const val PANIC_SCALE            = 1.2
        private const val LOG_EVERY_N_FRAMES     = 30
    }

    private val analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val isProcessing = AtomicBoolean(false)
    private var lastAnalysisTime = 0L
    private var frameCount = 0
    private val analysisInterval = 3
    private val adaptiveThrottleMs = AtomicLong(DEFAULT_THROTTLE_MS)
    private var emaMs = 0.0
    private var warmupCount = 0
    private var processedFrames = 0L
    private var cachedDetectionResult: DetectionResult? = null

    /** Set preview size for coordinate mapping (interface compliance) */
    var previewWidth: Int = 0
    var previewHeight: Int = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        frameCount++
        if (frameCount % analysisInterval != 0) {
            imageProxy.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < adaptiveThrottleMs.get()) {
            imageProxy.close()
            return
        }

        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        lastAnalysisTime = currentTime

        analysisScope.launch {
            val startNs = SystemClock.elapsedRealtimeNanos()
            try {
                val mediaImage = imageProxy.image ?: run {
                    Log.w(TAG, "mediaImage null — aborting")
                    return@launch
                }
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

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

                val filteredObjects = objects.filter { obj ->
                    (obj.labels.firstOrNull()?.confidence ?: 0f) >= minObjectConfidence
                }

                val detectionResult = DetectionResult(
                    faces = faces,
                    objects = filteredObjects,
                    imageWidth = imageProxy.width,
                    imageHeight = imageProxy.height,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                    timestamp = currentTime
                )
                cachedDetectionResult = detectionResult
                onDetectionResult(detectionResult)

                val lightingState = if (faces.isNotEmpty()) {
                    analyzeLightingUseCase.analyzeWithSubject(imageProxy, detectionResult)
                } else {
                    analyzeLightingUseCase(imageProxy)
                }
                onLightingResult(lightingState)

                Log.d(TAG, "Analysis: ${faces.size} faces, ${filteredObjects.size} objects, lighting=${lightingState.javaClass.simpleName}")

            } catch (e: Exception) {
                Log.e(TAG, "Analysis error", e)
            } finally {
                updateAdaptiveThrottle(SystemClock.elapsedRealtimeNanos() - startNs)
                imageProxy.close()
                isProcessing.set(false)
            }
        }
    }

    private fun updateAdaptiveThrottle(measuredNs: Long) {
        val measuredMs = measuredNs / 1_000_000.0
        if (warmupCount < WARMUP_FRAMES) { warmupCount++; return }
        emaMs = if (emaMs == 0.0) measuredMs else EMA_ALPHA * measuredMs + (1.0 - EMA_ALPHA) * emaMs
        val currentMs = adaptiveThrottleMs.get().toDouble()
        processedFrames++
        if (measuredMs > currentMs * PANIC_RATIO) {
            val panicMs = (currentMs * PANIC_SCALE).toLong().coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
            adaptiveThrottleMs.set(panicMs)
            return
        }
        val targetMs = (emaMs * SAFETY_FACTOR).toLong().coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
        if (Math.abs(targetMs - currentMs) < currentMs * DEADBAND_RATIO) return
        val newMs = when {
            targetMs > currentMs -> (currentMs + MAX_EXPAND_PER_FRAME).toLong().coerceAtMost(targetMs).coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
            else                 -> (currentMs - MAX_CONTRACT_PER_FRAME).toLong().coerceAtLeast(targetMs).coerceIn(MIN_THROTTLE_MS, MAX_THROTTLE_MS)
        }
        adaptiveThrottleMs.set(newMs)
        if (processedFrames % LOG_EVERY_N_FRAMES == 0L) {
            Log.i(TAG, "[v2.12.0-03] throttle=${newMs}ms EMA=${emaMs.toInt()}ms frames=$processedFrames")
        }
    }

    fun cleanup() {
        analysisScope.cancel()
        Log.d(TAG, "Analyzer cleaned up")
    }
}
