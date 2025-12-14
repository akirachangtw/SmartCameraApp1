/**
 * ClassName: AnalyzeLightingUseCase
 * Description: Domain layer use case for analyzing scene lighting and exposure
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - Histogram-based exposure analysis
 * - Backlight detection with subject context
 * - HDR recommendation logic
 * - Lighting state classification for UI
 * - Clean Architecture compliance (framework-independent)
 *
 * Analysis Pipeline:
 * 1. Calculate histogram from image
 * 2. Detect exposure issues (overexposure, low light, backlight)
 * 3. Classify lighting state for UI presentation
 * 4. Generate Chinese warning messages
 */
package com.akirachang.ai.photographer.domain.usecase

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.akirachang.ai.photographer.data.source.analysis.ExposureDetector
import com.akirachang.ai.photographer.data.source.analysis.HistogramCalculator
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import javax.inject.Inject

/**
 * Use case for analyzing scene lighting conditions
 *
 * This use case implements Clean Architecture principles by:
 * - Being the single source of truth for lighting analysis
 * - Coordinating between histogram calculation and exposure detection
 * - Transforming domain models (ExposureAnalysis) to presentation models (LightingState)
 * - Providing business logic for HDR recommendations
 */
class AnalyzeLightingUseCase @Inject constructor(
    private val histogramCalculator: HistogramCalculator,
    private val exposureDetector: ExposureDetector
) {

    companion object {
        private const val TAG = "AnalyzeLightingUseCase"

        // Thresholds for state classification
        private const val EXPOSURE_SCORE_THRESHOLD = 0.7f // Below this = needs warning
    }

    /**
     * Analyze lighting without subject context
     *
     * Used when no faces/objects detected, provides basic exposure analysis
     *
     * @param image ImageProxy from CameraX analysis pipeline
     * @return LightingState for UI presentation
     */
    operator fun invoke(image: ImageProxy): LightingState {
        try {
            // Calculate histogram
            val histogram = histogramCalculator.calculate(image)

            // Analyze exposure
            val analysis = exposureDetector.analyze(histogram)

            Log.d(TAG, "Lighting analysis: score=${analysis.exposureScore}, " +
                    "backlit=${analysis.isBacklit}, overexp=${analysis.isOverexposed}, " +
                    "lowlight=${analysis.isLowLight}")

            // Classify state
            return classifyLightingState(analysis)

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing lighting", e)
            return LightingState.Normal
        }
    }

    /**
     * Analyze lighting with subject context for backlight detection
     *
     * Used when face/object detected, enables accurate backlight detection
     *
     * @param image ImageProxy from CameraX
     * @param detectionResult Detection result with subject bounding boxes
     * @return LightingState with backlight detection
     */
    fun analyzeWithSubject(
        image: ImageProxy,
        detectionResult: DetectionResult
    ): LightingState {
        try {
            // Calculate full-frame histogram
            val histogram = histogramCalculator.calculate(image)

            // Get primary subject bounding box
            val subjectBox = getPrimarySubjectBox(detectionResult) ?: run {
                // No valid subject, fall back to basic analysis
                return invoke(image)
            }

            // Analyze with subject context
            val analysis = exposureDetector.analyzeWithSubject(
                image = image,
                histogram = histogram,
                subjectBox = subjectBox
            )

            Log.d(TAG, "Lighting with subject: score=${analysis.exposureScore}, " +
                    "backlit=${analysis.isBacklit}, HDR=${analysis.shouldUseHDR}")

            return classifyLightingState(analysis)

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing lighting with subject", e)
            return LightingState.Normal
        }
    }

    /**
     * Classify exposure analysis into presentation state
     *
     * Priority order (highest to lowest):
     * 1. Backlight (most critical)
     * 2. High contrast (needs HDR)
     * 3. Overexposure
     * 4. Low light
     * 5. Normal
     *
     * @param analysis ExposureAnalysis from detector
     * @return LightingState for UI
     */
    private fun classifyLightingState(
        analysis: com.akirachang.ai.photographer.domain.model.ExposureAnalysis
    ): LightingState {
        return when {
            // Backlight - highest priority
            analysis.isBacklit -> {
                LightingState.HighContrast(
                    analysis = analysis,
                    message = "逆光場景 - 建議使用 HDR"
                )
            }

            // High contrast (not backlit but needs HDR)
            analysis.isHighContrast -> {
                LightingState.HighContrast(
                    analysis = analysis,
                    message = when {
                        analysis.highlightRatio > 0.20f -> "高反差場景 - 注意高光細節"
                        else -> "場景對比度高 - 建議使用 HDR"
                    }
                )
            }

            // Overexposure
            analysis.isOverexposed -> {
                LightingState.Overexposed(
                    highlightRatio = analysis.highlightRatio,
                    message = "過曝風險 - 降低曝光補償"
                )
            }

            // Low light
            analysis.isLowLight -> {
                LightingState.LowLight(
                    avgLuminance = analysis.avgLuminance,
                    message = "光線不足 - 建議補光或提高 ISO"
                )
            }

            // Check exposure score for subtle issues
            analysis.exposureScore < EXPOSURE_SCORE_THRESHOLD -> {
                LightingState.HighContrast(
                    analysis = analysis,
                    message = "曝光需調整"
                )
            }

            // Normal exposure
            else -> LightingState.Normal
        }
    }

    /**
     * Get primary subject bounding box from detection result
     *
     * Priority: Largest face > First object
     *
     * @param detectionResult Detection result from ML Kit
     * @return Bounding box Rect or null if no subjects
     */
    private fun getPrimarySubjectBox(detectionResult: DetectionResult): Rect? {
        // Priority 1: Largest face
        if (detectionResult.faces.isNotEmpty()) {
            val largestFace = detectionResult.faces.maxByOrNull {
                it.boundingBox.width() * it.boundingBox.height()
            }
            return largestFace?.boundingBox
        }

        // Priority 2: First object
        if (detectionResult.objects.isNotEmpty()) {
            return detectionResult.objects.first().boundingBox
        }

        return null
    }
}
