/**
 * ClassName: AnalyzeCompositionUseCase
 * Description: Domain layer use case for analyzing photo composition using photography rules
 * Author: Claude Code
 * Date: 2025-11-27
 * Version: 2.0.0-alpha03
 *
 * Features:
 * - Rule of Thirds analysis (30% weight)
 * - Headroom checking (25% weight)
 * - Edge distance validation (25% weight)
 * - Multi-subject support (20% weight)
 * - Weighted scoring system
 * - Directional composition suggestions
 *
 * Modifications:
 * - 2025-11-27: Initial creation for v2.0 Clean Architecture refactor (Phase 1)
 * - 2025-11-27: Added accurate dimension calculation (Phase 2)
 * - 2025-11-27: Implemented complete composition rules with scoring (Phase 3)
 */
package local.mylab.smartcameraapp1.domain.usecase

import android.util.Log
import local.mylab.smartcameraapp1.domain.model.DetectionResult
import local.mylab.smartcameraapp1.presentation.camera.model.CompositionState
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use case for analyzing camera composition based on photography rules
 *
 * This use case implements Clean Architecture principles by:
 * - Being framework-independent (pure Kotlin, except Log)
 * - Having a single responsibility (composition analysis)
 * - Being testable (minimal Android dependencies)
 *
 * Phase 3 implements complete composition rule set:
 * - Rule of Thirds (30% weight): Subject alignment with intersection points
 * - Headroom Analysis (25% weight): Space above subject's head
 * - Edge Distance (25% weight): Margins from frame edges
 * - Subject Detection (20% weight): Single vs multi-subject handling
 *
 * Scoring system:
 * - Each rule produces a score (0.0 - 1.0)
 * - Weighted average determines overall composition quality
 * - Threshold 0.7 = Good composition
 */
class AnalyzeCompositionUseCase @Inject constructor() {

    companion object {
        private const val TAG = "AnalyzeCompositionUseCase"

        // Composition thresholds
        private const val GOOD_COMPOSITION_THRESHOLD = 0.7f

        // Rule weights (must sum to 1.0)
        private const val WEIGHT_RULE_OF_THIRDS = 0.30f
        private const val WEIGHT_HEADROOM = 0.25f
        private const val WEIGHT_EDGE_DISTANCE = 0.25f
        private const val WEIGHT_SUBJECT_DETECTION = 0.20f

        // Headroom configuration (percentage of image height)
        private const val IDEAL_HEADROOM_MIN = 0.05f  // 5%
        private const val IDEAL_HEADROOM_MAX = 0.15f  // 15%

        // Edge distance configuration (percentage of image width)
        private const val MIN_EDGE_DISTANCE = 0.05f  // 5%
    }

    /**
     * Analyzes composition of detected subjects in the frame
     *
     * Phase 3: Complete multi-rule analysis with weighted scoring system.
     * Analyzes Rule of Thirds, headroom, edge distance, and subject count.
     *
     * @param detectionResult Current ML Kit detection result
     * @param previewWidth Width of the preview view in pixels
     * @param previewHeight Height of the preview view in pixels
     * @return CompositionState indicating composition quality and suggestions
     */
    operator fun invoke(
        detectionResult: DetectionResult,
        previewWidth: Int? = null,
        previewHeight: Int? = null
    ): CompositionState {
        val faces = detectionResult.faces

        // No subject detected
        if (faces.isEmpty()) {
            return CompositionState.NoSubject
        }

        // Phase 3: Select primary subject (largest face if multiple detected)
        val primaryFace = selectPrimarySubject(faces)
        val boundingBox = primaryFace.boundingBox

        // Calculate subject metrics
        val faceCenterX = boundingBox.centerX().toFloat()
        val faceCenterY = boundingBox.centerY().toFloat()
        val faceTop = boundingBox.top.toFloat()
        val faceLeft = boundingBox.left.toFloat()
        val faceRight = boundingBox.right.toFloat()
        val faceBottom = boundingBox.bottom.toFloat()

        // Get image dimensions
        val imageWidth: Float
        val imageHeight: Float

        if (previewWidth != null && previewHeight != null) {
            imageWidth = previewWidth.toFloat()
            imageHeight = previewHeight.toFloat()
        } else {
            imageWidth = boundingBox.right * 1.5f
            imageHeight = boundingBox.bottom * 1.5f
            Log.w(TAG, "Preview dimensions not provided, using estimation (less accurate)")
        }

        // Phase 3: Perform multi-rule analysis
        val analysis = performCompositionAnalysis(
            faceCenterX = faceCenterX,
            faceCenterY = faceCenterY,
            faceTop = faceTop,
            faceLeft = faceLeft,
            faceRight = faceRight,
            faceBottom = faceBottom,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            faceCount = faces.size
        )

        // Determine composition state based on overall score
        return if (analysis.overallScore >= GOOD_COMPOSITION_THRESHOLD) {
            CompositionState.Good
        } else {
            val suggestion = generateDetailedSuggestion(analysis, faceCenterX, faceCenterY, faceTop, imageWidth, imageHeight)
            CompositionState.NeedsAdjustment(suggestion)
        }
    }

    /**
     * Selects the primary subject from multiple detected faces
     *
     * Strategy: Choose the largest face (by bounding box area)
     * Assumption: Larger face = closer to camera = main subject
     */
    private fun selectPrimarySubject(faces: List<com.google.mlkit.vision.face.Face>): com.google.mlkit.vision.face.Face {
        return faces.maxByOrNull { face ->
            val box = face.boundingBox
            box.width() * box.height()
        } ?: faces.first()
    }

    /**
     * Performs complete composition analysis using all rules
     *
     * Phase 3: Multi-rule scoring system
     */
    private fun performCompositionAnalysis(
        faceCenterX: Float,
        faceCenterY: Float,
        faceTop: Float,
        faceLeft: Float,
        faceRight: Float,
        faceBottom: Float,
        imageWidth: Float,
        imageHeight: Float,
        faceCount: Int
    ): CompositionAnalysis {
        // Rule 1: Rule of Thirds (30% weight)
        val ruleOfThirdsScore = analyzeRuleOfThirdsScore(
            faceCenterX, faceCenterY, imageWidth, imageHeight
        )

        // Rule 2: Headroom Analysis (25% weight)
        val headroomScore = analyzeHeadroomScore(
            faceTop, imageHeight
        )

        // Rule 3: Edge Distance (25% weight)
        val edgeDistanceScore = analyzeEdgeDistanceScore(
            faceLeft, faceRight, faceTop, faceBottom, imageWidth, imageHeight
        )

        // Rule 4: Subject Detection (20% weight)
        val subjectDetectionScore = analyzeSubjectDetectionScore(faceCount)

        // Calculate weighted overall score
        val overallScore =
            ruleOfThirdsScore * WEIGHT_RULE_OF_THIRDS +
            headroomScore * WEIGHT_HEADROOM +
            edgeDistanceScore * WEIGHT_EDGE_DISTANCE +
            subjectDetectionScore * WEIGHT_SUBJECT_DETECTION

        return CompositionAnalysis(
            ruleOfThirdsScore = ruleOfThirdsScore,
            headroomScore = headroomScore,
            edgeDistanceScore = edgeDistanceScore,
            subjectDetectionScore = subjectDetectionScore,
            overallScore = overallScore
        )
    }

    /**
     * Analyzes Rule of Thirds and returns a score (0.0 - 1.0)
     */
    private fun analyzeRuleOfThirdsScore(
        faceCenterX: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Float {
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f
        val tolerance = imageWidth * 0.1f

        // Calculate distances to nearest third lines
        val horizontalDistance = minOf(
            abs(faceCenterX - leftThird),
            abs(faceCenterX - rightThird)
        )
        val verticalDistance = minOf(
            abs(faceCenterY - topThird),
            abs(faceCenterY - bottomThird)
        )

        // Score based on proximity to intersection points
        // Perfect alignment = 1.0, far from lines = 0.0
        val horizontalScore = (1f - (horizontalDistance / tolerance).coerceIn(0f, 1f))
        val verticalScore = (1f - (verticalDistance / tolerance).coerceIn(0f, 1f))

        // Both dimensions must align well for good score
        return (horizontalScore + verticalScore) / 2f
    }

    /**
     * Analyzes headroom (space above subject) and returns a score (0.0 - 1.0)
     *
     * Ideal headroom: 5-15% of image height
     * Too little: cramped feeling
     * Too much: subject too low in frame
     */
    private fun analyzeHeadroomScore(
        faceTop: Float,
        imageHeight: Float
    ): Float {
        val headroomRatio = faceTop / imageHeight

        return when {
            headroomRatio < IDEAL_HEADROOM_MIN -> {
                // Too little headroom - score drops as it gets closer to 0
                (headroomRatio / IDEAL_HEADROOM_MIN).coerceIn(0f, 1f)
            }
            headroomRatio > IDEAL_HEADROOM_MAX -> {
                // Too much headroom - score drops as it increases
                val excess = headroomRatio - IDEAL_HEADROOM_MAX
                (1f - excess).coerceIn(0f, 1f)
            }
            else -> {
                // Within ideal range - perfect score
                1.0f
            }
        }
    }

    /**
     * Analyzes edge distance and returns a score (0.0 - 1.0)
     *
     * Subject should not be too close to frame edges
     * Minimum safe distance: 5% of image width
     */
    private fun analyzeEdgeDistanceScore(
        faceLeft: Float,
        faceRight: Float,
        faceTop: Float,
        faceBottom: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Float {
        val minMarginWidth = imageWidth * MIN_EDGE_DISTANCE
        val minMarginHeight = imageHeight * MIN_EDGE_DISTANCE

        // Calculate distances from all edges
        val leftMargin = faceLeft
        val rightMargin = imageWidth - faceRight
        val topMargin = faceTop
        val bottomMargin = imageHeight - faceBottom

        // Find minimum margin
        val minMargin = minOf(leftMargin, rightMargin, topMargin, bottomMargin)
        val minRequired = minOf(minMarginWidth, minMarginHeight)

        // Score based on minimum margin
        return (minMargin / minRequired).coerceIn(0f, 1f)
    }

    /**
     * Analyzes subject detection quality and returns a score (0.0 - 1.0)
     *
     * Single subject: Perfect (1.0)
     * Multiple subjects: Lower score (indicates complexity)
     */
    private fun analyzeSubjectDetectionScore(faceCount: Int): Float {
        return when (faceCount) {
            1 -> 1.0f  // Perfect - single clear subject
            2 -> 0.8f  // Good - duo composition
            3 -> 0.6f  // Acceptable - group of three
            else -> 0.4f  // Complex - many subjects
        }
    }

    /**
     * Generates detailed suggestion based on analysis results
     */
    private fun generateDetailedSuggestion(
        analysis: CompositionAnalysis,
        faceCenterX: Float,
        faceCenterY: Float,
        faceTop: Float,
        imageWidth: Float,
        imageHeight: Float
    ): String {
        // Find the weakest rule (lowest score)
        val scores = listOf(
            "Rule of Thirds" to analysis.ruleOfThirdsScore,
            "Headroom" to analysis.headroomScore,
            "Edge Distance" to analysis.edgeDistanceScore
        )
        val weakestRule = scores.minByOrNull { it.second }

        return when (weakestRule?.first) {
            "Rule of Thirds" -> {
                // Provide directional guidance
                val leftThird = imageWidth / 3f
                val rightThird = imageWidth * 2f / 3f
                val topThird = imageHeight / 3f
                val bottomThird = imageHeight * 2f / 3f

                val horizontal = when {
                    faceCenterX < leftThird -> "Move Right"
                    faceCenterX > rightThird -> "Move Left"
                    else -> null
                }
                val vertical = when {
                    faceCenterY < topThird -> "Move Down"
                    faceCenterY > bottomThird -> "Move Up"
                    else -> null
                }

                when {
                    horizontal != null && vertical != null -> "$vertical and $horizontal"
                    horizontal != null -> horizontal
                    vertical != null -> vertical
                    else -> "Center Subject"
                }
            }
            "Headroom" -> {
                val headroomRatio = faceTop / imageHeight
                when {
                    headroomRatio < IDEAL_HEADROOM_MIN -> "Move Down (More Headroom)"
                    headroomRatio > IDEAL_HEADROOM_MAX -> "Move Up (Less Headroom)"
                    else -> "Adjust Headroom"
                }
            }
            "Edge Distance" -> {
                "Move Away From Edge"
            }
            else -> "Adjust Position"
        }
    }

    /**
     * Data class holding composition analysis results
     */
    private data class CompositionAnalysis(
        val ruleOfThirdsScore: Float,
        val headroomScore: Float,
        val edgeDistanceScore: Float,
        val subjectDetectionScore: Float,
        val overallScore: Float
    )

    /**
     * Analyzes composition based on Rule of Thirds (Legacy method - kept for compatibility)
     *
     * The Rule of Thirds divides the frame into a 3x3 grid and suggests placing
     * the main subject at one of the four intersection points.
     *
     * @param faceCenterX X coordinate of face center
     * @param faceCenterY Y coordinate of face center
     * @param imageWidth Width of the image/preview
     * @param imageHeight Height of the image/preview
     * @return Analysis result with composition quality
     */
    private fun analyzeRuleOfThirds(
        faceCenterX: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): RuleOfThirdsResult {
        // Calculate third positions
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f

        // Tolerance: 10% of image width
        val tolerance = imageWidth * 0.1f

        // Check horizontal alignment (left or right third line)
        val isHorizontallyAligned =
            abs(faceCenterX - leftThird) < tolerance ||
            abs(faceCenterX - rightThird) < tolerance

        // Check vertical alignment (top or bottom third line)
        val isVerticallyAligned =
            abs(faceCenterY - topThird) < tolerance ||
            abs(faceCenterY - bottomThird) < tolerance

        // Good composition if aligned both horizontally and vertically
        val isGoodComposition = isHorizontallyAligned && isVerticallyAligned

        return RuleOfThirdsResult(
            isGoodComposition = isGoodComposition,
            horizontalAlignment = calculateHorizontalAlignment(faceCenterX, leftThird, rightThird, tolerance),
            verticalAlignment = calculateVerticalAlignment(faceCenterY, topThird, bottomThird, tolerance)
        )
    }

    /**
     * Calculates horizontal alignment relative to Rule of Thirds lines
     */
    private fun calculateHorizontalAlignment(
        x: Float,
        leftThird: Float,
        rightThird: Float,
        tolerance: Float
    ): HorizontalAlignment {
        return when {
            abs(x - leftThird) < tolerance -> HorizontalAlignment.LEFT_THIRD
            abs(x - rightThird) < tolerance -> HorizontalAlignment.RIGHT_THIRD
            x < leftThird -> HorizontalAlignment.TOO_LEFT
            x > rightThird -> HorizontalAlignment.TOO_RIGHT
            else -> HorizontalAlignment.CENTER
        }
    }

    /**
     * Calculates vertical alignment relative to Rule of Thirds lines
     */
    private fun calculateVerticalAlignment(
        y: Float,
        topThird: Float,
        bottomThird: Float,
        tolerance: Float
    ): VerticalAlignment {
        return when {
            abs(y - topThird) < tolerance -> VerticalAlignment.TOP_THIRD
            abs(y - bottomThird) < tolerance -> VerticalAlignment.BOTTOM_THIRD
            y < topThird -> VerticalAlignment.TOO_HIGH
            y > bottomThird -> VerticalAlignment.TOO_LOW
            else -> VerticalAlignment.CENTER
        }
    }

    /**
     * Generates composition suggestion based on subject position
     *
     * @param faceCenterX X coordinate of face center
     * @param faceCenterY Y coordinate of face center
     * @param imageWidth Width of the image/preview
     * @param imageHeight Height of the image/preview
     * @return Suggestion message for user
     */
    private fun generateCompositionSuggestion(
        faceCenterX: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): String {
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f

        // Determine horizontal suggestion
        val horizontalSuggestion = when {
            faceCenterX < leftThird -> "Move Right"
            faceCenterX > rightThird -> "Move Left"
            else -> null
        }

        // Determine vertical suggestion
        val verticalSuggestion = when {
            faceCenterY < topThird -> "Move Down"
            faceCenterY > bottomThird -> "Move Up"
            else -> null
        }

        // Combine suggestions
        return when {
            horizontalSuggestion != null && verticalSuggestion != null ->
                "$verticalSuggestion and $horizontalSuggestion"
            horizontalSuggestion != null -> horizontalSuggestion
            verticalSuggestion != null -> verticalSuggestion
            else -> "Adjust Position" // Fallback
        }
    }

    /**
     * Result of Rule of Thirds analysis
     */
    private data class RuleOfThirdsResult(
        val isGoodComposition: Boolean,
        val horizontalAlignment: HorizontalAlignment,
        val verticalAlignment: VerticalAlignment
    )

    /**
     * Horizontal alignment classification
     */
    private enum class HorizontalAlignment {
        TOO_LEFT,
        LEFT_THIRD,
        CENTER,
        RIGHT_THIRD,
        TOO_RIGHT
    }

    /**
     * Vertical alignment classification
     */
    private enum class VerticalAlignment {
        TOO_HIGH,
        TOP_THIRD,
        CENTER,
        BOTTOM_THIRD,
        TOO_LOW
    }
}
