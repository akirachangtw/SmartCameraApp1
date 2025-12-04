/**
 * ClassName: ExposureDetector
 * Description: Detects exposure issues (backlight, overexposure, low light) from histogram
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - Backlight detection (subject darker than background)
 * - Overexposure detection (highlight clipping)
 * - Low light detection (insufficient illumination)
 * - Dynamic range estimation
 * - Configurable thresholds
 *
 * Detection Logic:
 * - Backlight: Subject ROI luminance < 30%, background > 70%
 * - Overexposure: Highlights (240-255) > 15% of pixels
 * - Low Light: Average luminance < 51 (20% of 255)
 * - High Contrast: Significant both highlights AND shadows present
 */
package local.mylab.smartcameraapp1.data.source.analysis

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import local.mylab.smartcameraapp1.domain.model.ExposureAnalysis
import local.mylab.smartcameraapp1.domain.model.Histogram
import javax.inject.Inject
import kotlin.math.abs

/**
 * Detector for exposure and lighting issues
 */
class ExposureDetector @Inject constructor(
    private val histogramCalculator: HistogramCalculator
) {

    companion object {
        private const val TAG = "ExposureDetector"

        // Thresholds
        private const val OVEREXPOSURE_THRESHOLD = 0.15f // 15% highlights = overexposed
        private const val LOW_LIGHT_THRESHOLD = 51f // 20% of 255 = low light
        private const val SHADOW_THRESHOLD = 0.10f // 10% shadows significant
        private const val HIGHLIGHT_THRESHOLD = 0.10f // 10% highlights significant

        // Backlight detection thresholds
        private const val SUBJECT_DARK_THRESHOLD = 76f // 30% of 255
        private const val BACKGROUND_BRIGHT_THRESHOLD = 178f // 70% of 255
        private const val LUMINANCE_DIFF_THRESHOLD = 80f // Significant difference

        // Dynamic range thresholds
        private const val HIGH_DYNAMIC_RANGE_THRESHOLD = 180f // Out of 255
    }

    /**
     * Analyze exposure from full-frame histogram
     *
     * @param histogram Full-frame luminance histogram
     * @param subjectBox Optional subject bounding box for backlight detection
     * @return ExposureAnalysis with detected issues and metrics
     */
    fun analyze(histogram: Histogram, subjectBox: Rect? = null): ExposureAnalysis {
        // Basic metrics
        val avgLuminance = histogram.avgLuminance
        val highlightRatio = histogram.highlightRatio
        val shadowRatio = histogram.shadowRatio

        // Dynamic range estimation
        val dynamicRange = estimateDynamicRange(histogram)

        // Detection flags
        val isOverexposed = highlightRatio > OVEREXPOSURE_THRESHOLD
        val isLowLight = avgLuminance < LOW_LIGHT_THRESHOLD
        val isHighContrast = (highlightRatio > HIGHLIGHT_THRESHOLD &&
                             shadowRatio > SHADOW_THRESHOLD) ||
                             dynamicRange > HIGH_DYNAMIC_RANGE_THRESHOLD

        Log.d(TAG, "Exposure: avg=$avgLuminance, HL=$highlightRatio, SH=$shadowRatio, DR=$dynamicRange")

        return ExposureAnalysis(
            isBacklit = false, // Will be set by analyzeWithSubject if subject available
            isOverexposed = isOverexposed,
            isLowLight = isLowLight,
            highlightRatio = highlightRatio,
            shadowRatio = shadowRatio,
            avgLuminance = avgLuminance,
            dynamicRange = dynamicRange
        )
    }

    /**
     * Analyze exposure with subject bounding box for backlight detection
     *
     * @param image Full ImageProxy
     * @param histogram Full-frame histogram
     * @param subjectBox Subject bounding box from face/object detection
     * @return ExposureAnalysis with backlight detection
     */
    fun analyzeWithSubject(
        image: ImageProxy,
        histogram: Histogram,
        subjectBox: Rect
    ): ExposureAnalysis {
        // Get base analysis
        val baseAnalysis = analyze(histogram, subjectBox)

        // Calculate subject region histogram for backlight detection
        val subjectHistogram = histogramCalculator.calculateROI(
            image,
            roiX = subjectBox.left,
            roiY = subjectBox.top,
            roiWidth = subjectBox.width(),
            roiHeight = subjectBox.height()
        )

        val subjectAvgLuminance = subjectHistogram.avgLuminance
        val backgroundAvgLuminance = histogram.avgLuminance // Full frame includes background

        // Backlight detection: Subject dark AND (background bright OR high contrast)
        val isBacklit = detectBacklight(
            subjectLuminance = subjectAvgLuminance,
            backgroundLuminance = backgroundAvgLuminance,
            overallContrast = baseAnalysis.dynamicRange
        )

        Log.d(TAG, "Backlight check: subject=$subjectAvgLuminance, bg=$backgroundAvgLuminance, backlit=$isBacklit")

        return baseAnalysis.copy(isBacklit = isBacklit)
    }

    /**
     * Detect backlight condition
     *
     * Backlight occurs when:
     * 1. Subject is significantly darker than background
     * 2. Large luminance difference exists (> 80)
     * 3. Subject is in shadow range (< 30%)
     *
     * @param subjectLuminance Average luminance of subject region
     * @param backgroundLuminance Average luminance of full frame
     * @param overallContrast Dynamic range of entire scene
     * @return True if backlight condition detected
     */
    private fun detectBacklight(
        subjectLuminance: Float,
        backgroundLuminance: Float,
        overallContrast: Float
    ): Boolean {
        val luminanceDiff = abs(backgroundLuminance - subjectLuminance)

        // Strict backlight: subject very dark, background very bright
        val strictBacklight = subjectLuminance < SUBJECT_DARK_THRESHOLD &&
                             backgroundLuminance > BACKGROUND_BRIGHT_THRESHOLD

        // Moderate backlight: significant difference + subject in shadow
        val moderateBacklight = luminanceDiff > LUMINANCE_DIFF_THRESHOLD &&
                               subjectLuminance < (SUBJECT_DARK_THRESHOLD * 1.5f) &&
                               overallContrast > 120f

        return strictBacklight || moderateBacklight
    }

    /**
     * Estimate dynamic range from histogram
     *
     * Finds the range between darkest and brightest luminance levels
     * that each contain at least 1% of pixels (to ignore noise)
     *
     * @param histogram Luminance histogram
     * @return Estimated dynamic range (0-255)
     */
    private fun estimateDynamicRange(histogram: Histogram): Float {
        val threshold = (histogram.totalPixels * 0.01f).toInt() // 1% threshold

        // Find darkest level with significant pixels
        var minLevel = 0
        for (i in 0 until 256) {
            if (histogram.data[i] >= threshold) {
                minLevel = i
                break
            }
        }

        // Find brightest level with significant pixels
        var maxLevel = 255
        for (i in 255 downTo 0) {
            if (histogram.data[i] >= threshold) {
                maxLevel = i
                break
            }
        }

        val range = (maxLevel - minLevel).toFloat()
        Log.d(TAG, "Dynamic range: $minLevel-$maxLevel = $range")

        return range
    }
}
