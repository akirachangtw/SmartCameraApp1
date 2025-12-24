/**
 * ClassName: AnalyzeLightingUseCaseTest
 * Description: Unit tests for AnalyzeLightingUseCase
 * Author: Claude Code
 * Date: 2025-12-20
 * Version: 2.6.0
 *
 * Test Coverage:
 * - Normal lighting conditions
 * - HDR scenarios (High Contrast)
 * - Backlight scenarios
 * - Overexposure scenarios
 * - Underexposure (Low Light) scenarios
 * - Priority-based state classification
 * - Boundary conditions and edge cases
 *
 * v2.6.0 Tests:
 * - Separated Backlight from HighContrast state
 * - Underexposed (renamed from LowLight)
 * - State priority system
 * - Detailed metrics validation
 *
 * Test Strategy:
 * - Use Mockito to mock HistogramCalculator and ExposureDetector dependencies
 * - Use Robolectric for Android framework classes (Rect, Bitmap, ImageProxy)
 * - Follow AAA pattern (Arrange-Act-Assert)
 * - Test both ImageProxy and Bitmap overloads
 */
package com.akirachang.ai.photographer.domain.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.akirachang.ai.photographer.data.source.analysis.ExposureDetector
import com.akirachang.ai.photographer.data.source.analysis.HistogramCalculator
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.model.ExposureAnalysis
import com.akirachang.ai.photographer.domain.model.Histogram
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.google.mlkit.vision.face.Face
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for AnalyzeLightingUseCase
 *
 * Uses Robolectric to provide Android framework classes
 * Uses Mockito to mock dependencies
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnalyzeLightingUseCaseTest {

    @Mock
    private lateinit var histogramCalculator: HistogramCalculator

    @Mock
    private lateinit var exposureDetector: ExposureDetector

    @Mock
    private lateinit var imageProxy: ImageProxy

    @Mock
    private lateinit var bitmap: Bitmap

    @Mock
    private lateinit var face: Face

    private lateinit var useCase: AnalyzeLightingUseCase

    // Test constants
    private val testWidth = 1080
    private val testHeight = 1920

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = AnalyzeLightingUseCase(histogramCalculator, exposureDetector)
    }

    // ========== Helper Functions ==========

    private fun createTestHistogram(): Histogram {
        val data = IntArray(256) { 10 }
        return Histogram(data, data.sum())
    }

    private fun createTestExposureAnalysis(
        isBacklit: Boolean = false,
        isOverexposed: Boolean = false,
        isLowLight: Boolean = false,
        highlightRatio: Float = 0.05f,
        shadowRatio: Float = 0.05f,
        avgLuminance: Float = 127.5f,
        dynamicRange: Float = 100f
    ): ExposureAnalysis {
        return ExposureAnalysis(
            isBacklit = isBacklit,
            isOverexposed = isOverexposed,
            isLowLight = isLowLight,
            highlightRatio = highlightRatio,
            shadowRatio = shadowRatio,
            avgLuminance = avgLuminance,
            dynamicRange = dynamicRange
        )
    }

    private fun createTestDetectionResult(hasFace: Boolean = false): DetectionResult {
        val faces = if (hasFace) {
            val boundingBox = Rect(300, 500, 700, 1100)
            `when`(face.boundingBox).thenReturn(boundingBox)
            listOf(face)
        } else {
            emptyList()
        }

        return DetectionResult(
            faces = faces,
            objects = emptyList(),
            imageWidth = testWidth,
            imageHeight = testHeight,
            rotationDegrees = 0
        )
    }

    // ========== Normal Lighting Tests ==========

    @Test
    fun `invoke should return Normal for good exposure`() {
        // Arrange
        val histogram = createTestHistogram()
        val analysis = createTestExposureAnalysis()

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase(imageProxy)

        // Assert
        assertTrue("Should return Normal state", result is LightingState.Normal)
    }

    // ========== Backlight Tests ==========

    @Test
    fun `invoke should return Backlight when subject is backlit`() {
        // Arrange
        val histogram = createTestHistogram()
        val analysis = createTestExposureAnalysis(
            isBacklit = true,
            avgLuminance = 150f,
            dynamicRange = 180f
        )

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase(imageProxy)

        // Assert
        assertTrue("Should return Backlight state", result is LightingState.Backlight)

        val backlightState = result as LightingState.Backlight
        assertTrue("Subject luminance should be lower",
            backlightState.subjectLuminance < backlightState.backgroundLuminance)
        assertEquals("Recommendation should mention HDR",
            "逆光場景 - 建議使用 HDR 或開啟補光",
            backlightState.getRecommendation())
    }

    @Test
    fun `Backlight should have highest priority`() {
        val backlightState = LightingState.Backlight(76f, 178f, 102f)
        assertEquals("Backlight priority should be 4", 4, backlightState.getPriority())
    }

    // ========== High Contrast Tests ==========

    @Test
    fun `invoke should return HighContrast for HDR scenes`() {
        // Arrange: High contrast (both highlights and shadows significant)
        val histogram = createTestHistogram()
        val analysis = createTestExposureAnalysis(
            isBacklit = false,
            highlightRatio = 0.15f,
            shadowRatio = 0.12f,
            avgLuminance = 127.5f,
            dynamicRange = 200f
        )

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase(imageProxy)

        // Assert
        assertTrue("Should return HighContrast state", result is LightingState.HighContrast)

        val highContrastState = result as LightingState.HighContrast
        assertEquals("Dynamic range should be 200", 200f, highContrastState.dynamicRange, 0.1f)
        assertEquals("Highlight ratio should be 0.15", 0.15f, highContrastState.highlightRatio, 0.01f)
        assertEquals("Shadow ratio should be 0.12", 0.12f, highContrastState.shadowRatio, 0.01f)
        assertTrue("Recommendation should mention HDR",
            highContrastState.getRecommendation().contains("HDR"))
    }

    @Test
    fun `HighContrast should have priority 3`() {
        val highContrastState = LightingState.HighContrast(5f, 200f, 0.12f, 0.11f)
        assertEquals("HighContrast priority should be 3", 3, highContrastState.getPriority())
    }

    // ========== Overexposure Tests ==========

    @Test
    fun `invoke should return Overexposed for excessive highlights`() {
        // Arrange: Overexposure (highlights > 15%)
        val histogram = createTestHistogram()
        val analysis = createTestExposureAnalysis(
            isBacklit = false,
            isOverexposed = true,
            highlightRatio = 0.18f,
            avgLuminance = 200f
        )

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase(imageProxy)

        // Assert
        assertTrue("Should return Overexposed state", result is LightingState.Overexposed)

        val overexposedState = result as LightingState.Overexposed
        assertEquals("Highlight ratio should be 0.18", 0.18f, overexposedState.highlightRatio, 0.01f)
        assertEquals("Average luminance should be 200", 200f, overexposedState.avgLuminance, 0.1f)
        assertTrue("Recommendation should mention exposure",
            overexposedState.getRecommendation().contains("曝光"))
    }

    @Test
    fun `Overexposed should have priority 2`() {
        val overexposedState = LightingState.Overexposed(0.18f, 200f)
        assertEquals("Overexposed priority should be 2", 2, overexposedState.getPriority())
    }

    // ========== Underexposure Tests ==========

    @Test
    fun `invoke should return Underexposed for low light`() {
        // Arrange: Low light (avg < 51)
        val histogram = createTestHistogram()
        val analysis = createTestExposureAnalysis(
            isBacklit = false,
            isLowLight = true,
            avgLuminance = 40f,
            shadowRatio = 0.30f
        )

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase(imageProxy)

        // Assert
        assertTrue("Should return Underexposed state", result is LightingState.Underexposed)

        val underexposedState = result as LightingState.Underexposed
        assertEquals("Average luminance should be 40", 40f, underexposedState.avgLuminance, 0.1f)
        assertEquals("Shadow ratio should be 0.30", 0.30f, underexposedState.shadowRatio, 0.01f)
    }

    @Test
    fun `Underexposed should have priority 1`() {
        val underexposedState = LightingState.Underexposed(40f, 0.30f)
        assertEquals("Underexposed priority should be 1", 1, underexposedState.getPriority())
    }

    // ========== Priority System Tests ==========

    @Test
    fun `priority order should be correct`() {
        // Backlight: 4 > HighContrast: 3 > Overexposed: 2 > Underexposed: 1 > Normal: 0
        val backlightState = LightingState.Backlight(76f, 178f, 102f)
        val highContrastState = LightingState.HighContrast(5f, 200f, 0.12f, 0.11f)
        val overexposedState = LightingState.Overexposed(0.18f, 200f)
        val underexposedState = LightingState.Underexposed(40f, 0.30f)
        val normalState = LightingState.Normal

        assertTrue(backlightState.getPriority() > highContrastState.getPriority())
        assertTrue(highContrastState.getPriority() > overexposedState.getPriority())
        assertTrue(overexposedState.getPriority() > underexposedState.getPriority())
        assertTrue(underexposedState.getPriority() > normalState.getPriority())
    }

    // ========== Helper Method Tests ==========

    @Test
    fun `requiresWarning should return false for Normal`() {
        val normalState = LightingState.Normal
        assertFalse("Normal should not require warning", normalState.requiresWarning())
    }

    @Test
    fun `requiresWarning should return true for warning states`() {
        assertTrue(LightingState.Backlight(76f, 178f, 102f).requiresWarning())
        assertTrue(LightingState.HighContrast(5f, 200f, 0.12f, 0.11f).requiresWarning())
        assertTrue(LightingState.Overexposed(0.18f, 200f).requiresWarning())
        assertTrue(LightingState.Underexposed(40f, 0.30f).requiresWarning())
    }

    @Test
    fun `getRecommendation should return empty for Normal`() {
        assertEquals("", LightingState.Normal.getRecommendation())
    }

    @Test
    fun `getRecommendation should return non-empty for warnings`() {
        assertTrue(LightingState.Backlight(76f, 178f, 102f).getRecommendation().isNotEmpty())
        assertTrue(LightingState.HighContrast(5f, 200f, 0.12f, 0.11f).getRecommendation().isNotEmpty())
        assertTrue(LightingState.Overexposed(0.18f, 200f).getRecommendation().isNotEmpty())
        assertTrue(LightingState.Underexposed(40f, 0.30f).getRecommendation().isNotEmpty())
    }

    // ========== Bitmap Overload Tests ==========

    @Test
    fun `invoke with Bitmap should work correctly`() {
        // Arrange
        val histogram = createTestHistogram()
        val analysis = createTestExposureAnalysis()

        `when`(histogramCalculator.calculate(bitmap)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase(bitmap)

        // Assert
        assertTrue("Should return Normal state", result is LightingState.Normal)
    }

    @Test
    fun `analyzeWithSubject with Bitmap should work correctly`() {
        // Arrange
        val histogram = createTestHistogram()
        val detectionResult = createTestDetectionResult(hasFace = true)
        val subjectBox = detectionResult.faces.first().boundingBox
        val analysis = createTestExposureAnalysis()

        `when`(histogramCalculator.calculate(bitmap)).thenReturn(histogram)
        `when`(exposureDetector.analyzeWithSubject(bitmap, histogram, subjectBox))
            .thenReturn(analysis)

        // Act
        val result = useCase.analyzeWithSubject(bitmap, detectionResult)

        // Assert
        assertTrue("Should return Normal state", result is LightingState.Normal)
    }

    // ========== Edge Cases ==========

    @Test
    fun `analyzeWithSubject should fallback when no subject`() {
        // Arrange: No face or object
        val histogram = createTestHistogram()
        val detectionResult = createTestDetectionResult(hasFace = false)
        val analysis = createTestExposureAnalysis()

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyze(histogram)).thenReturn(analysis)

        // Act
        val result = useCase.analyzeWithSubject(imageProxy, detectionResult)

        // Assert
        assertTrue("Should fallback to Normal state", result is LightingState.Normal)
    }

    @Test
    fun `invoke should return Normal on exception`() {
        // Arrange: Simulate exception
        `when`(histogramCalculator.calculate(imageProxy)).thenThrow(RuntimeException("Test exception"))

        // Act
        val result = useCase(imageProxy)

        // Assert
        assertTrue("Should return Normal on exception", result is LightingState.Normal)
    }

    @Test
    fun `analyzeWithSubject should return Normal on exception`() {
        // Arrange
        val detectionResult = createTestDetectionResult(hasFace = true)
        `when`(histogramCalculator.calculate(imageProxy)).thenThrow(RuntimeException("Test"))

        // Act
        val result = useCase.analyzeWithSubject(imageProxy, detectionResult)

        // Assert
        assertTrue("Should return Normal on exception", result is LightingState.Normal)
    }

    @Test
    fun `analyzeWithSubject with ImageProxy should detect backlight`() {
        // Arrange
        val histogram = createTestHistogram()
        val detectionResult = createTestDetectionResult(hasFace = true)
        val subjectBox = detectionResult.faces.first().boundingBox
        val analysis = createTestExposureAnalysis(isBacklit = true, avgLuminance = 180f)

        `when`(histogramCalculator.calculate(imageProxy)).thenReturn(histogram)
        `when`(exposureDetector.analyzeWithSubject(imageProxy, histogram, subjectBox))
            .thenReturn(analysis)

        // Act
        val result = useCase.analyzeWithSubject(imageProxy, detectionResult)

        // Assert
        assertTrue("Should return Backlight state", result is LightingState.Backlight)
        assertEquals("Priority should be highest", 4, result.getPriority())
    }
}
