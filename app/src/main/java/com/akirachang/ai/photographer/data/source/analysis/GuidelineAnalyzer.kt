package com.akirachang.ai.photographer.data.source.analysis

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import com.akirachang.ai.photographer.data.source.camera.toGrayMat
import com.akirachang.ai.photographer.domain.model.GuidelineAnalysisResult
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.min

/**
 * Guideline Analyzer - OpenCV-based composition guideline analyzer
 *
 * v2.7.0 Phase 2 Sprint 3: OpenCV Hough Line Transform implementation
 *
 * Detects composition guideline lines using OpenCV:
 * - Canny edge detection
 * - Hough Line Transform (probabilistic)
 * - Line filtering and classification
 * - Normalized coordinate output
 *
 * Performance target: < 15ms average, < 30ms P95
 */
class GuidelineAnalyzer(
    /**
     * Callback for analysis results
     *
     * Called on background thread (ImageAnalysis executor).
     * Use Flow/StateFlow to propagate to UI thread.
     */
    private val onAnalysisResult: (GuidelineAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "GuidelineAnalyzer"
        private const val STATS_INTERVAL = 30L // Report every 30 frames

        // Canny edge detection thresholds
        private const val CANNY_THRESHOLD_1 = 50.0
        private const val CANNY_THRESHOLD_2 = 150.0

        // Hough Line Transform parameters
        private const val HOUGH_RHO = 1.0              // Distance resolution (pixels)
        private const val HOUGH_THETA = Math.PI / 180  // Angle resolution (1 degree)
        private const val HOUGH_THRESHOLD = 70         // Minimum votes to consider a line (v2.7.0 Sprint 5: 50→70, +40%)
        private const val HOUGH_MIN_LINE_LENGTH = 80.0 // Minimum line length (pixels) (v2.7.0 Sprint 5: 50.0→80.0, +60%)
        private const val HOUGH_MAX_LINE_GAP = 10.0    // Maximum gap between line segments

        // Line filtering
        private const val MAX_LINES = 6                // Maximum lines to return (v2.7.0 Sprint 5: 10→6, -40%)
        private const val MIN_LINE_STRENGTH = 0.5f     // Minimum strength threshold (v2.7.0 Sprint 5: 0.3→0.5, +67%)
    }

    private var frameCount = 0L
    private val performanceStats = mutableListOf<Long>()

    override fun analyze(image: ImageProxy) {
        val startTime = System.nanoTime()

        try {
            frameCount++

            // Convert ImageProxy to OpenCV Mat (grayscale)
            val grayMat = image.toGrayMat()

            // Detect guideline lines
            val result = detectGuidelineLines(
                grayMat = grayMat,
                frameWidth = image.width,
                frameHeight = image.height
            )

            // Release Mat to avoid memory leak
            grayMat.release()

            // Calculate processing time
            val elapsedMillis = (System.nanoTime() - startTime) / 1_000_000

            // Add processing time to result
            val finalResult = result.copy(processingTimeMs = elapsedMillis)

            // Log result
            Log.v(TAG, "Frame #$frameCount: Detected ${finalResult.lines.size} lines in ${elapsedMillis}ms")

            // Callback with result
            onAnalysisResult(finalResult)

            // Performance tracking
            performanceStats.add(elapsedMillis)

            // Report statistics every STATS_INTERVAL frames
            if (frameCount % STATS_INTERVAL == 0L) {
                reportPerformanceStats()
                performanceStats.clear()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed at frame #$frameCount", e)
            // Callback with empty result on error
            onAnalysisResult(GuidelineAnalysisResult.EMPTY)
        } finally {
            // Always close ImageProxy
            image.close()
        }
    }

    /**
     * Detect guideline lines using OpenCV
     *
     * Pipeline:
     * 1. Canny edge detection
     * 2. Hough Line Transform (probabilistic)
     * 3. Line filtering and normalization
     * 4. Strength calculation
     *
     * @param grayMat Input grayscale Mat
     * @param frameWidth Frame width (for normalization)
     * @param frameHeight Frame height (for normalization)
     * @return Guideline analysis result with detected lines
     */
    private fun detectGuidelineLines(
        grayMat: Mat,
        frameWidth: Int,
        frameHeight: Int
    ): GuidelineAnalysisResult {
        // Step 1: Canny edge detection
        val edges = Mat()
        Imgproc.Canny(grayMat, edges, CANNY_THRESHOLD_1, CANNY_THRESHOLD_2)

        // Step 2: Hough Line Transform (probabilistic)
        val lines = Mat()
        Imgproc.HoughLinesP(
            edges,
            lines,
            HOUGH_RHO,
            HOUGH_THETA,
            HOUGH_THRESHOLD,
            HOUGH_MIN_LINE_LENGTH,
            HOUGH_MAX_LINE_GAP
        )

        // Step 3: Extract and normalize lines
        val detectedLines = extractLines(lines, frameWidth, frameHeight)

        // Step 4: Filter and sort by strength
        val filteredLines = detectedLines
            .sortedByDescending { it.strength }
            .take(MAX_LINES)
            .filter { it.strength >= MIN_LINE_STRENGTH }

        // Release intermediate Mats
        edges.release()
        lines.release()

        return GuidelineAnalysisResult(
            lines = filteredLines,
            processingTimeMs = 0,  // Will be set by caller
            frameDimensions = Pair(frameWidth, frameHeight)
        )
    }

    /**
     * Extract lines from OpenCV HoughLinesP result
     *
     * Converts from pixel coordinates to normalized coordinates [0.0, 1.0].
     * Calculates line strength based on length.
     *
     * @param linesMat OpenCV Mat containing detected lines (Nx4)
     * @param frameWidth Frame width for normalization
     * @param frameHeight Frame height for normalization
     * @return List of normalized lines with strength
     */
    private fun extractLines(
        linesMat: Mat,
        frameWidth: Int,
        frameHeight: Int
    ): List<GuidelineAnalysisResult.Line> {
        if (linesMat.empty() || linesMat.rows() == 0) {
            return emptyList()
        }

        val maxDiagonal = kotlin.math.sqrt(
            (frameWidth * frameWidth + frameHeight * frameHeight).toDouble()
        ).toFloat()

        return (0 until linesMat.rows()).map { i ->
            val line = linesMat.get(i, 0)

            // Extract pixel coordinates
            val x1 = line[0].toFloat()
            val y1 = line[1].toFloat()
            val x2 = line[2].toFloat()
            val y2 = line[3].toFloat()

            // Normalize to [0.0, 1.0]
            val start = Offset(
                x = x1 / frameWidth,
                y = y1 / frameHeight
            )
            val end = Offset(
                x = x2 / frameWidth,
                y = y2 / frameHeight
            )

            // Calculate line strength (based on length relative to diagonal)
            val dx = x2 - x1
            val dy = y2 - y1
            val length = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            val strength = min(1.0f, length / (maxDiagonal * 0.3f))  // Normalize to [0, 1]

            GuidelineAnalysisResult.Line(
                start = start,
                end = end,
                strength = strength,
                type = GuidelineAnalysisResult.LineType.GENERAL
            )
        }
    }

    /**
     * Analyze Bitmap for guideline lines (ViewModel integration)
     *
     * v2.7.0 Phase 2 Sprint 4
     *
     * Provides synchronous Bitmap analysis for ViewModel-layer integration.
     * Used when previewBitmap StateFlow is available instead of ImageProxy.
     *
     * Note: This method is synchronous and should be called on a background thread
     * (e.g., Dispatchers.Default). The caller is responsible for thread management.
     *
     * @param bitmap Input bitmap (typically from previewBitmap StateFlow)
     * @return GuidelineAnalysisResult with detected lines
     */
    fun analyzeBitmap(bitmap: Bitmap): GuidelineAnalysisResult {
        val startTime = System.nanoTime()

        return try {
            // Convert Bitmap to OpenCV Mat (grayscale)
            val grayMat = bitmap.toGrayMat()

            // Detect guideline lines
            val result = detectGuidelineLines(
                grayMat = grayMat,
                frameWidth = bitmap.width,
                frameHeight = bitmap.height
            )

            // Release Mat to avoid memory leak
            grayMat.release()

            // Calculate processing time
            val elapsedMillis = (System.nanoTime() - startTime) / 1_000_000

            // Add processing time to result
            result.copy(processingTimeMs = elapsedMillis)

        } catch (e: Exception) {
            Log.e(TAG, "Bitmap analysis failed", e)
            // Return empty result on error
            GuidelineAnalysisResult.EMPTY
        }
    }

    /**
     * Report performance statistics
     *
     * v2.7.0 Sprint 5: Enhanced logging with device/android/params info
     *
     * Logs:
     * - Average processing time
     * - P95 processing time (95th percentile)
     * - Min/Max processing time
     * - Device model, Android version, parameters preset
     */
    private fun reportPerformanceStats() {
        if (performanceStats.isEmpty()) return

        val sorted = performanceStats.sorted()
        val avg = performanceStats.average()
        val p95Index = (performanceStats.size * 0.95).toInt()
        val p95 = sorted.getOrNull(p95Index) ?: sorted.last()
        val min = sorted.first()
        val max = sorted.last()

        // v2.7.0 Sprint 5: Include device/android/params in log
        Log.i(
            TAG,
            "Performance ($STATS_INTERVAL frames): " +
            "avg=${avg.toInt()}ms, " +
            "P95=${p95}ms, " +
            "min=${min}ms, " +
            "max=${max}ms | " +
            "device=${android.os.Build.MODEL}, " +
            "android=API${android.os.Build.VERSION.SDK_INT}, " +
            "params=sprint5_optimized"
        )

        // Warning if performance is below target
        if (avg > 15.0) {
            Log.w(TAG, "⚠️ Average processing time (${avg.toInt()}ms) exceeds target (15ms)")
        }
        if (p95 > 30) {
            Log.w(TAG, "⚠️ P95 processing time (${p95}ms) exceeds target (30ms)")
        }
    }
}
