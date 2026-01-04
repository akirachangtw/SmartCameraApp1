package com.akirachang.ai.photographer.data.source.analysis

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.akirachang.ai.photographer.data.source.camera.toGrayMat

/**
 * Guideline Analyzer - OpenCV-based composition guideline analyzer
 *
 * v2.7.0 Phase 2 Sprint 2: Performance measurement version
 *
 * This is the initial version that focuses on performance validation:
 * - Converts ImageProxy → Mat
 * - Measures conversion time
 * - Validates memory management
 * - Does NOT perform actual OpenCV analysis yet (Sprint 3)
 *
 * Purpose: Establish performance baseline before implementing full analysis
 */
class GuidelineAnalyzer : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "GuidelineAnalyzer"
        private const val STATS_INTERVAL = 30L // Report every 30 frames
    }

    private var frameCount = 0L
    private val performanceStats = mutableListOf<Long>()

    override fun analyze(image: ImageProxy) {
        val startTime = System.nanoTime()

        try {
            frameCount++

            // Convert ImageProxy to OpenCV Mat (grayscale)
            val mat = image.toGrayMat()

            // Log mat info for verification
            Log.v(TAG, "Frame #$frameCount: Mat ${mat.cols()}x${mat.rows()}, type=${mat.type()}")

            // Release Mat to avoid memory leak
            mat.release()

        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed at frame #$frameCount", e)
        } finally {
            // Always close ImageProxy
            image.close()
        }

        // Performance measurement
        val elapsedNanos = System.nanoTime() - startTime
        val elapsedMillis = elapsedNanos / 1_000_000  // Convert to ms

        performanceStats.add(elapsedMillis)

        // Report statistics every STATS_INTERVAL frames
        if (frameCount % STATS_INTERVAL == 0L) {
            reportPerformanceStats()
            performanceStats.clear()
        }
    }

    /**
     * Report performance statistics
     *
     * Logs:
     * - Average processing time
     * - P95 processing time (95th percentile)
     * - Min/Max processing time
     */
    private fun reportPerformanceStats() {
        if (performanceStats.isEmpty()) return

        val sorted = performanceStats.sorted()
        val avg = performanceStats.average()
        val p95Index = (performanceStats.size * 0.95).toInt()
        val p95 = sorted.getOrNull(p95Index) ?: sorted.last()
        val min = sorted.first()
        val max = sorted.last()

        Log.i(
            TAG,
            "Performance ($STATS_INTERVAL frames): " +
            "avg=${avg.toInt()}ms, " +
            "P95=${p95}ms, " +
            "min=${min}ms, " +
            "max=${max}ms"
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
