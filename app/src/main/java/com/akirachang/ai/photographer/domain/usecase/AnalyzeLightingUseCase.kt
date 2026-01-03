/**
 * ClassName: AnalyzeLightingUseCase
 * Description: Domain layer use case for analyzing scene lighting and exposure
 * Author: Claude Code
 * Date: 2025-12-20
 * Version: 2.6.0
 *
 * Features:
 * - Histogram-based exposure analysis
 * - Backlight detection with subject context (separated state in v2.6.0)
 * - HDR recommendation logic
 * - Lighting state classification for UI
 * - Clean Architecture compliance (framework-independent)
 * - Priority-based warning system
 *
 * Analysis Pipeline:
 * 1. Calculate histogram from image (full-frame or subject ROI)
 * 2. Detect exposure issues (backlight, high contrast, overexposure, underexposure)
 * 3. Classify lighting state with priority order
 * 4. Generate Traditional Chinese warning messages with recommendations
 *
 * v2.6.0 Changes:
 * - Separated Backlight from HighContrast state (now distinct conditions)
 * - Renamed LowLight to Underexposed for clarity
 * - Added detailed metrics (contrastRatio, luminanceDifference) to each state
 * - Enhanced recommendation messages with specific guidance
 * - Improved state classification logic with better priority handling
 */
package com.akirachang.ai.photographer.domain.usecase

import android.graphics.Bitmap
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
     * v2.6.0: Updated to use new LightingState structure with separated Backlight state
     *
     * Priority order (highest to lowest):
     * 1. Backlight (most critical - affects subject visibility)
     * 2. Overexposure (highlight clipping - specific exposure issue)
     * 3. High contrast (needs HDR - general scene characteristic)
     * 4. Underexposure (low light)
     * 5. Normal
     *
     * @param analysis ExposureAnalysis from detector
     * @return LightingState for UI
     */
    private fun classifyLightingState(
        analysis: com.akirachang.ai.photographer.domain.model.ExposureAnalysis
    ): LightingState {
        return when {
            // Backlight - highest priority (separated from HighContrast in v2.6.0)
            analysis.isBacklit -> {
                // Calculate luminance difference for display
                val subjectLuminance = analysis.avgLuminance * 0.7f // Estimate (subject darker)
                val backgroundLuminance = analysis.avgLuminance * 1.3f // Estimate (background brighter)
                val luminanceDiff = kotlin.math.abs(backgroundLuminance - subjectLuminance)

                LightingState.Backlight(
                    subjectLuminance = subjectLuminance,
                    backgroundLuminance = backgroundLuminance,
                    luminanceDifference = luminanceDiff
                )
            }

            // Overexposure - priority 2 (more specific than high contrast)
            analysis.isOverexposed -> {
                LightingState.Overexposed(
                    highlightRatio = analysis.highlightRatio,
                    avgLuminance = analysis.avgLuminance
                )
            }

            // High contrast (not backlit, not overexposed, but needs HDR)
            analysis.isHighContrast -> {
                // Calculate contrast ratio from dynamic range
                val minLevel = 127.5f - (analysis.dynamicRange / 2f)
                val maxLevel = 127.5f + (analysis.dynamicRange / 2f)
                val contrastRatio = if (minLevel > 0) maxLevel / minLevel else 10f

                LightingState.HighContrast(
                    contrastRatio = contrastRatio,
                    dynamicRange = analysis.dynamicRange,
                    highlightRatio = analysis.highlightRatio,
                    shadowRatio = analysis.shadowRatio
                )
            }

            // Low light (renamed to Underexposed in v2.6.0)
            analysis.isLowLight -> {
                LightingState.Underexposed(
                    avgLuminance = analysis.avgLuminance,
                    shadowRatio = analysis.shadowRatio
                )
            }

            // Check exposure score for subtle issues
            analysis.exposureScore < EXPOSURE_SCORE_THRESHOLD -> {
                // Moderate exposure issue - use HighContrast as catch-all
                val minLevel = 127.5f - (analysis.dynamicRange / 2f)
                val maxLevel = 127.5f + (analysis.dynamicRange / 2f)
                val contrastRatio = if (minLevel > 0) maxLevel / minLevel else 5f

                LightingState.HighContrast(
                    contrastRatio = contrastRatio,
                    dynamicRange = analysis.dynamicRange,
                    highlightRatio = analysis.highlightRatio,
                    shadowRatio = analysis.shadowRatio
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

    /**
     * Analyze lighting from Bitmap without subject context (v2.5.1 Phase 7)
     *
     * Used when no faces/objects detected, provides basic exposure analysis
     *
     * @param bitmap Bitmap from unified camera preview stream
     * @return LightingState for UI presentation
     */
    operator fun invoke(bitmap: Bitmap): LightingState {
        try {
            // Calculate histogram
            val histogram = histogramCalculator.calculate(bitmap)

            // Analyze exposure
            val analysis = exposureDetector.analyze(histogram)

            Log.d(TAG, "Lighting analysis from Bitmap: score=${analysis.exposureScore}, " +
                    "backlit=${analysis.isBacklit}, overexp=${analysis.isOverexposed}, " +
                    "lowlight=${analysis.isLowLight}")

            // Classify state
            return classifyLightingState(analysis)

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing lighting from Bitmap", e)
            return LightingState.Normal
        }
    }

    /**
     * Analyze lighting with subject context for backlight detection (v2.5.1 Phase 7)
     *
     * Used when face/object detected, enables accurate backlight detection
     *
     * @param bitmap Bitmap from unified camera preview stream
     * @param detectionResult Detection result with subject bounding boxes
     * @return LightingState with backlight detection
     */
    fun analyzeWithSubject(
        bitmap: Bitmap,
        detectionResult: DetectionResult
    ): LightingState {
        try {
            // Calculate full-frame histogram
            val histogram = histogramCalculator.calculate(bitmap)

            // Get primary subject bounding box
            val subjectBox = getPrimarySubjectBox(detectionResult) ?: run {
                // No valid subject, fall back to basic analysis
                return invoke(bitmap)
            }

            // Analyze with subject context
            val analysis = exposureDetector.analyzeWithSubject(
                bitmap = bitmap,
                histogram = histogram,
                subjectBox = subjectBox
            )

            Log.d(TAG, "Lighting with subject (Bitmap): score=${analysis.exposureScore}, " +
                    "backlit=${analysis.isBacklit}, HDR=${analysis.shouldUseHDR}")

            return classifyLightingState(analysis)

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing lighting with subject (Bitmap)", e)
            return LightingState.Normal
        }
    }
}
