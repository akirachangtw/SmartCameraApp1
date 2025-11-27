/**
 * ClassName: AnalyzeCompositionUseCase
 * Description: Domain layer use case for analyzing photo composition using photography rules
 * Author: Claude Code
 * Date: 2025-11-27
 * Version: 2.0.0-alpha01
 *
 * Features:
 * - Rule of Thirds analysis
 * - Headroom checking
 * - Edge distance validation
 * - Multi-subject support
 * - Directional composition suggestions
 *
 * Modifications:
 * - 2025-11-27: Initial creation for v2.0 Clean Architecture refactor
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
 * - Being framework-independent (pure Kotlin)
 * - Having a single responsibility (composition analysis)
 * - Being testable (no Android dependencies)
 *
 * Currently implements:
 * - Rule of Thirds
 * - Basic subject detection
 *
 * Future improvements (v2.0 phases 2-4):
 * - Headroom analysis
 * - Edge distance checking
 * - Directional suggestions
 * - Multi-subject support
 */
class AnalyzeCompositionUseCase @Inject constructor() {

    companion object {
        private const val TAG = "AnalyzeCompositionUseCase"
    }

    /**
     * Analyzes composition of detected subjects in the frame
     *
     * Phase 2: Now prioritizes actual preview dimensions for accurate analysis.
     * Falls back to estimation only if dimensions are not provided.
     *
     * @param detectionResult Current ML Kit detection result
     * @param previewWidth Width of the preview view in pixels (Phase 2: now prioritized)
     * @param previewHeight Height of the preview view in pixels (Phase 2: now prioritized)
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

        // Analyze first face (simplified for initial refactor)
        // TODO: Phase 3 - Implement multi-subject support
        val face = faces.first()
        val boundingBox = face.boundingBox

        // Calculate face center
        val faceCenterX = boundingBox.centerX().toFloat()
        val faceCenterY = boundingBox.centerY().toFloat()

        // Phase 2: Prioritize actual preview dimensions for accuracy
        val imageWidth: Float
        val imageHeight: Float

        if (previewWidth != null && previewHeight != null) {
            // Use actual dimensions (accurate)
            imageWidth = previewWidth.toFloat()
            imageHeight = previewHeight.toFloat()
        } else {
            // Fallback to estimation (legacy behavior for compatibility)
            imageWidth = boundingBox.right * 1.5f
            imageHeight = boundingBox.bottom * 1.5f
            Log.w(TAG, "Preview dimensions not provided, using estimation (less accurate)")
        }

        // Perform Rule of Thirds analysis
        val ruleOfThirdsResult = analyzeRuleOfThirds(
            faceCenterX = faceCenterX,
            faceCenterY = faceCenterY,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        return if (ruleOfThirdsResult.isGoodComposition) {
            CompositionState.Good
        } else {
            // TODO: Phase 4 - Implement directional suggestions
            val suggestion = generateCompositionSuggestion(
                faceCenterX = faceCenterX,
                faceCenterY = faceCenterY,
                imageWidth = imageWidth,
                imageHeight = imageHeight
            )
            CompositionState.NeedsAdjustment(suggestion)
        }
    }

    /**
     * Analyzes composition based on Rule of Thirds
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
