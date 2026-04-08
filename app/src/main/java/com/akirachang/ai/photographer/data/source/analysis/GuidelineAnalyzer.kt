package com.akirachang.ai.photographer.data.source.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.akirachang.ai.photographer.domain.model.GuidelineAnalysisResult

/**
 * GuidelineAnalyzer — Cueshot D0 stub (OpenCV Hough Line Transform removed).
 *
 * The OpenCV-based guideline pipeline has been removed per 焦土政策.
 * Cueshot v3.3 replaces this with the CompositionGuidelineEngine (pure Kotlin Math Engine).
 *
 * TODO (Cueshot Ticket D5): Wire CompositionGuidelineEngine as the replacement
 */
class GuidelineAnalyzer(
    private val onAnalysisResult: (GuidelineAnalysisResult) -> Unit = {}
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        image.close()
    }
}
