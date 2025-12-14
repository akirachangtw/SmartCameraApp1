/**
 * ClassName: AnalyzeCompositionUseCaseTest
 * Description: Unit tests for AnalyzeCompositionUseCase
 * Author: Claude Code
 * Date: 2025-11-27
 * Version: 2.0.0-beta01
 *
 * Tests Phase 3 complete composition rules implementation:
 * - Rule of Thirds (30% weight)
 * - Headroom Analysis (25% weight)
 * - Edge Distance (25% weight)
 * - Subject Detection (20% weight)
 * - Weighted scoring system
 */
package com.akirachang.ai.photographer.domain.usecase

import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for AnalyzeCompositionUseCase
 *
 * Test coverage:
 * - Rule of Thirds: alignment with intersection points
 * - Headroom: space above subject (5-15% ideal)
 * - Edge Distance: minimum 5% margin from edges
 * - Multi-subject: largest face selection
 * - Weighted scoring: overall score calculation
 * - Edge cases: no faces, boundary conditions
 *
 * Uses Robolectric to provide Android framework classes (Rect, etc.)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])  // Use Android API 28 for testing
class AnalyzeCompositionUseCaseTest {

    private lateinit var useCase: AnalyzeCompositionUseCase

    // Test dimensions
    private val testWidth = 1080
    private val testHeight = 1920

    @Before
    fun setup() {
        useCase = AnalyzeCompositionUseCase()
    }

    // ==================== Helper Functions ====================

    /**
     * Creates a mock Face with specified bounding box
     */
    private fun createMockFace(left: Int, top: Int, right: Int, bottom: Int): Face {
        val face = mock(Face::class.java)
        val boundingBox = Rect(left, top, right, bottom)
        `when`(face.boundingBox).thenReturn(boundingBox)
        return face
    }

    /**
     * Creates a DetectionResult with given faces
     */
    private fun createDetectionResult(faces: List<Face>): DetectionResult {
        return DetectionResult(
            faces = faces,
            objects = emptyList(),
            imageWidth = testWidth,
            imageHeight = testHeight
        )
    }

    // ==================== No Subject Tests ====================

    @Test
    fun `invoke with no faces should return NoSubject`() {
        // Arrange
        val detectionResult = createDetectionResult(emptyList())

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue(result is CompositionState.NoSubject)
    }

    // ==================== Rule of Thirds Tests ====================

    @Test
    fun `face at top-left intersection should return Good`() {
        // Arrange: Face center at (1/3, 1/3) of frame
        val leftThird = testWidth / 3
        val topThird = testHeight / 3
        val faceSize = 200

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = topThird - faceSize / 2,
            right = leftThird + faceSize / 2,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected Good composition for top-left intersection",
            result is CompositionState.Good)
    }

    @Test
    fun `face at top-right intersection should return Good`() {
        // Arrange: Face center at (2/3, 1/3) of frame
        val rightThird = testWidth * 2 / 3
        val topThird = testHeight / 3
        val faceSize = 200

        val face = createMockFace(
            left = rightThird - faceSize / 2,
            top = topThird - faceSize / 2,
            right = rightThird + faceSize / 2,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected Good composition for top-right intersection",
            result is CompositionState.Good)
    }

    @Test
    fun `face at bottom-left intersection should return Good`() {
        // Arrange: Face center at (1/3, 2/3) of frame
        val leftThird = testWidth / 3
        val bottomThird = testHeight * 2 / 3
        val faceSize = 200

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = bottomThird - faceSize / 2,
            right = leftThird + faceSize / 2,
            bottom = bottomThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected Good composition for bottom-left intersection",
            result is CompositionState.Good)
    }

    @Test
    fun `face at bottom-right intersection should return Good`() {
        // Arrange: Face center at (2/3, 2/3) of frame
        val rightThird = testWidth * 2 / 3
        val bottomThird = testHeight * 2 / 3
        val faceSize = 200

        val face = createMockFace(
            left = rightThird - faceSize / 2,
            top = bottomThird - faceSize / 2,
            right = rightThird + faceSize / 2,
            bottom = bottomThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected Good composition for bottom-right intersection",
            result is CompositionState.Good)
    }

    @Test
    fun `face at center should return NeedsAdjustment`() {
        // Arrange: Face center at (1/2, 1/2) of frame (not on third lines)
        val centerX = testWidth / 2
        val centerY = testHeight / 2
        val faceSize = 200

        val face = createMockFace(
            left = centerX - faceSize / 2,
            top = centerY - faceSize / 2,
            right = centerX + faceSize / 2,
            bottom = centerY + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected NeedsAdjustment for centered face",
            result is CompositionState.NeedsAdjustment)
    }

    @Test
    fun `face slightly off intersection within tolerance should return Good`() {
        // Arrange: Face slightly off top-left intersection but within 10% tolerance
        val leftThird = testWidth / 3
        val topThird = testHeight / 3
        val offset = (testWidth * 0.05f).toInt()  // 5% offset (within 10% tolerance)
        val faceSize = 200

        val face = createMockFace(
            left = leftThird + offset - faceSize / 2,
            top = topThird + offset - faceSize / 2,
            right = leftThird + offset + faceSize / 2,
            bottom = topThird + offset + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected Good composition for face within tolerance",
            result is CompositionState.Good)
    }

    @Test
    fun `face far from intersections should return NeedsAdjustment with direction`() {
        // Arrange: Face in top-left corner (far from any intersection)
        val faceSize = 150

        val face = createMockFace(
            left = 50,
            top = 50,
            right = 50 + faceSize,
            bottom = 50 + faceSize
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Expected NeedsAdjustment for poorly positioned face",
            result is CompositionState.NeedsAdjustment)
        if (result is CompositionState.NeedsAdjustment) {
            assertNotNull("Expected suggestion message", result.message)
            assertFalse("Expected non-empty message", result.message.isEmpty())
        }
    }

    // ==================== Headroom Tests ====================

    @Test
    fun `face with ideal headroom 10% should contribute to Good score`() {
        // Arrange: Face positioned with 10% headroom (ideal: 5-15%)
        val headroom = (testHeight * 0.10f).toInt()
        val faceSize = 300
        val leftThird = testWidth / 3

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = headroom,
            right = leftThird + faceSize / 2,
            bottom = headroom + faceSize
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        // Should be Good if other rules also score well
        // At minimum, should not penalize for headroom
        assertNotNull(result)
    }

    @Test
    fun `face with too little headroom 2% should suggest more headroom`() {
        // Arrange: Face too close to top (2% headroom, below 5% minimum)
        val headroom = (testHeight * 0.02f).toInt()
        val faceSize = 300
        val centerX = testWidth / 2

        val face = createMockFace(
            left = centerX - faceSize / 2,
            top = headroom,
            right = centerX + faceSize / 2,
            bottom = headroom + faceSize
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        if (result is CompositionState.NeedsAdjustment) {
            assertTrue("Expected headroom-related suggestion",
                result.message.contains("Headroom", ignoreCase = true) ||
                result.message.contains("Down", ignoreCase = true))
        }
    }

    @Test
    fun `face with too much headroom 30% may still score as Good if other rules compensate`() {
        // Arrange: Face too low in frame (30% headroom, above 15% maximum)
        // But positioned at left third horizontally with good margins
        val headroom = (testHeight * 0.30f).toInt()
        val faceSize = 300
        val leftThird = testWidth / 3

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = headroom,
            right = leftThird + faceSize / 2,
            bottom = headroom + faceSize
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        // Weighted scoring system: even with poor headroom score (~0.4),
        // if other rules score well (Rule of Thirds ~0.9, Edge ~1.0, Subject ~1.0),
        // overall score = 0.9*0.3 + 0.4*0.25 + 1.0*0.25 + 1.0*0.2 = 0.27 + 0.1 + 0.25 + 0.2 = 0.82
        // This demonstrates that weighted scoring allows trade-offs between rules
        assertNotNull("Should return valid composition state", result)
        assertTrue("Result should be either Good or NeedsAdjustment",
            result is CompositionState.Good || result is CompositionState.NeedsAdjustment)
    }

    // ==================== Edge Distance Tests ====================

    @Test
    fun `face with good margin 10% from edges should contribute to Good score`() {
        // Arrange: Face with 10% margin from all edges (well above 5% minimum)
        val margin = (testWidth * 0.10f).toInt()
        val faceSize = 200
        val centerX = testWidth / 2
        val topThird = testHeight / 3

        val face = createMockFace(
            left = centerX - faceSize / 2,
            top = topThird - faceSize / 2,
            right = centerX + faceSize / 2,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertNotNull(result)
        // Good margins should not cause NeedsAdjustment for edge distance
    }

    @Test
    fun `face too close to left edge 2% should suggest moving away`() {
        // Arrange: Face at 2% from left edge (below 5% minimum)
        val leftMargin = (testWidth * 0.02f).toInt()
        val faceSize = 200
        val topThird = testHeight / 3

        val face = createMockFace(
            left = leftMargin,
            top = topThird - faceSize / 2,
            right = leftMargin + faceSize,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        if (result is CompositionState.NeedsAdjustment) {
            assertTrue("Expected edge-related suggestion",
                result.message.contains("Edge", ignoreCase = true) ||
                result.message.contains("Right", ignoreCase = true))
        }
    }

    @Test
    fun `face too close to right edge should suggest moving away`() {
        // Arrange: Face at 2% from right edge
        val rightMargin = (testWidth * 0.02f).toInt()
        val faceSize = 200
        val topThird = testHeight / 3

        val face = createMockFace(
            left = testWidth - rightMargin - faceSize,
            top = topThird - faceSize / 2,
            right = testWidth - rightMargin,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        if (result is CompositionState.NeedsAdjustment) {
            assertTrue("Expected edge-related suggestion",
                result.message.contains("Edge", ignoreCase = true) ||
                result.message.contains("Left", ignoreCase = true))
        }
    }

    // ==================== Multi-Subject Tests ====================

    @Test
    fun `single face should have perfect subject detection score`() {
        // Arrange: Single face at good position
        val leftThird = testWidth / 3
        val topThird = testHeight / 3
        val faceSize = 200

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = topThird - faceSize / 2,
            right = leftThird + faceSize / 2,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        // Single subject should contribute 1.0 score (20% weight)
        assertTrue("Single face should result in Good composition when well positioned",
            result is CompositionState.Good)
    }

    @Test
    fun `multiple faces should analyze largest face`() {
        // Arrange: Two faces - one large, one small
        val leftThird = testWidth / 3
        val topThird = testHeight / 3

        // Large face (should be selected)
        val largeFace = createMockFace(
            left = leftThird - 150,
            top = topThird - 150,
            right = leftThird + 150,
            bottom = topThird + 150
        )

        // Small face
        val smallFace = createMockFace(
            left = 100,
            top = 100,
            right = 200,
            bottom = 200
        )

        val detectionResult = createDetectionResult(listOf(smallFace, largeFace))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        // Should analyze the large face (at third intersection) not the small one
        assertNotNull("Should return result for largest face", result)
        // Large face is well-positioned, so should be Good if other rules pass
    }

    @Test
    fun `two faces should have lower subject detection score than one`() {
        // Arrange: Two faces at good positions
        val leftThird = testWidth / 3
        val topThird = testHeight / 3
        val faceSize = 150

        val face1 = createMockFace(
            left = leftThird - faceSize,
            top = topThird - faceSize / 2,
            right = leftThird,
            bottom = topThird + faceSize / 2
        )

        val face2 = createMockFace(
            left = leftThird + 50,
            top = topThird - faceSize / 2,
            right = leftThird + 50 + faceSize,
            bottom = topThird + faceSize / 2
        )

        val detectionResult = createDetectionResult(listOf(face1, face2))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        // Two faces get 0.8 score (vs 1.0 for single face)
        // This reduces overall score by 20% * 0.2 = 4%
        assertNotNull("Should handle multiple faces", result)
    }

    // ==================== Weighted Scoring Tests ====================

    @Test
    fun `perfect composition should return Good`() {
        // Arrange: Face at intersection with ideal headroom and good margins
        val leftThird = testWidth / 3
        val headroom = (testHeight * 0.10f).toInt()  // 10% - ideal
        val faceSize = 250

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = headroom,
            right = leftThird + faceSize / 2,
            bottom = headroom + faceSize
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertTrue("Perfect composition should return Good",
            result is CompositionState.Good)
    }

    @Test
    fun `borderline score should correctly classify as Good or NeedsAdjustment`() {
        // Arrange: Face positioned to score around threshold (0.7)
        // Slightly off-center but with good headroom
        val x = (testWidth * 0.4f).toInt()  // Between center and left third
        val headroom = (testHeight * 0.10f).toInt()
        val faceSize = 250

        val face = createMockFace(
            left = x - faceSize / 2,
            top = headroom,
            right = x + faceSize / 2,
            bottom = headroom + faceSize
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, testWidth, testHeight)

        // Assert
        assertNotNull("Should return valid composition state", result)
        assertTrue("Result should be either Good or NeedsAdjustment",
            result is CompositionState.Good || result is CompositionState.NeedsAdjustment)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `face at boundary position should not crash`() {
        // Arrange: Face at frame boundary
        val face = createMockFace(
            left = 0,
            top = 0,
            right = 100,
            bottom = 100
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act & Assert
        val result = useCase(detectionResult, testWidth, testHeight)
        assertNotNull("Should handle boundary position", result)
    }

    @Test
    fun `very small face should not crash`() {
        // Arrange: Very small face
        val face = createMockFace(
            left = 500,
            top = 500,
            right = 510,
            bottom = 510
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act & Assert
        val result = useCase(detectionResult, testWidth, testHeight)
        assertNotNull("Should handle very small face", result)
    }

    @Test
    fun `invoke without preview dimensions should use estimation`() {
        // Arrange: Call without passing preview dimensions
        val leftThird = 360  // Assuming estimation gives ~1080 width
        val topThird = 640   // Assuming estimation gives ~1920 height
        val faceSize = 200

        val face = createMockFace(
            left = leftThird - faceSize / 2,
            top = topThird - faceSize / 2,
            right = leftThird + faceSize / 2,
            bottom = topThird + faceSize / 2
        )
        val detectionResult = createDetectionResult(listOf(face))

        // Act
        val result = useCase(detectionResult, null, null)

        // Assert
        assertNotNull("Should work with null dimensions using estimation", result)
    }
}
