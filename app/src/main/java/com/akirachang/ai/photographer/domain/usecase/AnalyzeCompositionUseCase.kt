/**
 * ClassName: AnalyzeCompositionUseCase
 * Description: Domain layer use case for analyzing photo composition using photography rules
 * Author: Claude Code
 * Date: 2025-11-27
 * Version: 2.4.0
 *
 * Features:
 * - Rule of Thirds analysis (25% weight)
 * - Headroom checking (20% weight)
 * - Edge distance validation (20% weight)
 * - Multi-subject support (10% weight)
 * - Eye position analysis (25% weight) - v2.3.0
 * - Weighted scoring system
 * - Professional Chinese suggestions (v2.1.1)
 * - Front camera mirror coordinate handling (v2.3.1)
 * - Visual composition guide frame (v2.4.0)
 *
 * Modifications:
 * - 2025-11-27: Initial creation for v2.0 Clean Architecture refactor (Phase 1)
 * - 2025-11-27: Added accurate dimension calculation (Phase 2)
 * - 2025-11-27: Implemented complete composition rules with scoring (Phase 3)
 * - 2025-12-02: Add Chinese professional suggestions and direction vectors (v2.1.1)
 * - 2025-12-04: Add eye position analysis for portrait composition (v2.3.0)
 * - 2025-12-05: Fix front camera mirror coordinate issue - X coordinates now properly mirrored (v2.3.1)
 * - 2025-12-08: Add visual composition guide frame for easier alignment (v2.4.0)
 */
package com.akirachang.ai.photographer.domain.usecase

import android.graphics.PointF
import android.util.Log
import androidx.compose.ui.geometry.Rect
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.presentation.camera.model.CompositionGuideData
import com.akirachang.ai.photographer.presentation.camera.model.CompositionState
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max

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
        // v2.3.0: Added eye position weight, adjusted other weights
        // v2.4.6: 降低眼睛位置權重，避免過度影響整體評分
        // v2.6.0: Split edge distance into edge + fill-the-frame
        private const val WEIGHT_EYE_POSITION = 0.18f      // 從 0.25f 降為 0.18f
        private const val WEIGHT_RULE_OF_THIRDS = 0.25f    // 保持不變
        private const val WEIGHT_HEADROOM = 0.22f          // 從 0.20f 增為 0.22f（補眼睛減少的部分）
        private const val WEIGHT_EDGE_DISTANCE = 0.25f     // 保持不變 (內部拆分為 edge 60% + fill 40%)
        private const val WEIGHT_SUBJECT_DETECTION = 0.10f // 保持不變
        // 總和 = 0.18 + 0.25 + 0.22 + 0.25 + 0.10 = 1.00

        // Edge distance sub-weights (v2.6.0 - must sum to 1.0)
        private const val SUB_WEIGHT_EDGE = 0.60f          // Edge distance: 60% of WEIGHT_EDGE_DISTANCE
        private const val SUB_WEIGHT_FILL_FRAME = 0.40f    // Fill-the-frame: 40% of WEIGHT_EDGE_DISTANCE

        // Headroom configuration (percentage of image height)
        private const val IDEAL_HEADROOM_MIN = 0.05f  // 5%
        private const val IDEAL_HEADROOM_MAX = 0.15f  // 15%

        // Edge distance configuration (percentage of image width)
        private const val MIN_EDGE_DISTANCE = 0.05f  // 5%

        // Fill-the-frame configuration (v2.6.0 - percentage of image area)
        private const val FILL_FRAME_MIN = 0.30f  // 30% - minimum subject area
        private const val FILL_FRAME_IDEAL_MIN = 0.40f  // 40% - ideal minimum
        private const val FILL_FRAME_IDEAL_MAX = 0.70f  // 70% - ideal maximum
        private const val FILL_FRAME_MAX = 0.85f  // 85% - maximum before too tight
    }

    /**
     * Safely perform division with NaN/Infinity guards (v2.5.3)
     *
     * Prevents NaN propagation in composition calculations by validating:
     * - Denominator is non-zero
     * - Neither numerator nor denominator is NaN
     * - Result is finite (not Infinity)
     *
     * @param numerator Dividend value
     * @param denominator Divisor value
     * @param defaultValue Fallback value if division is invalid (default: 0f)
     * @return Result of division or defaultValue if invalid
     */
    private fun safeDivide(
        numerator: Float,
        denominator: Float,
        defaultValue: Float = 0f
    ): Float {
        // Check for zero denominator
        if (denominator == 0f) {
            Log.w(TAG, "safeDivide: Zero denominator ($numerator / 0), returning $defaultValue")
            return defaultValue
        }

        // Check for NaN inputs
        if (numerator.isNaN() || denominator.isNaN()) {
            Log.w(TAG, "safeDivide: NaN input detected (num=$numerator, den=$denominator), returning $defaultValue")
            return defaultValue
        }

        // Perform division
        val result = numerator / denominator

        // Check result validity
        return if (result.isNaN() || result.isInfinite()) {
            Log.w(TAG, "safeDivide: Invalid result (NaN/Infinite) from ($numerator / $denominator), returning $defaultValue")
            defaultValue
        } else {
            result
        }
    }

    /**
     * Transforms ML Kit image-space coordinates into PreviewView/Compose view-space coordinates.
     *
     * CameraPreview uses PreviewView.ScaleType.FILL_CENTER (center-crop), so we must apply:
     * - scale = max(viewW/imgW, viewH/imgH)
     * - offset to center the scaled image
     * - optional mirror for front camera (preview is mirrored for user)
     */
    private data class ViewTransform(
        val viewWidth: Float,
        val viewHeight: Float,
        val imageWidth: Float,
        val imageHeight: Float,
        val mirrorX: Boolean
    ) {
        private val scale: Float = max(viewWidth / imageWidth, viewHeight / imageHeight)
        private val offsetX: Float = (viewWidth - (imageWidth * scale)) / 2f
        private val offsetY: Float = (viewHeight - (imageHeight * scale)) / 2f

        fun mapPoint(point: PointF): PointF {
            val x = (point.x * scale) + offsetX
            val y = (point.y * scale) + offsetY
            val mappedX = if (mirrorX) viewWidth - x else x
            return PointF(mappedX, y)
        }

        fun mapRect(rect: android.graphics.Rect): Rect {
            val topLeft = mapPoint(PointF(rect.left.toFloat(), rect.top.toFloat()))
            val bottomRight = mapPoint(PointF(rect.right.toFloat(), rect.bottom.toFloat()))

            val left = minOf(topLeft.x, bottomRight.x)
            val right = maxOf(topLeft.x, bottomRight.x)
            val top = minOf(topLeft.y, bottomRight.y)
            val bottom = maxOf(topLeft.y, bottomRight.y)

            return Rect(left = left, top = top, right = right, bottom = bottom)
        }
    }

    /**
     * Analyzes composition of detected subjects in the frame
     *
     * Phase 3: Complete multi-rule analysis with weighted scoring system.
     * Analyzes Rule of Thirds, headroom, edge distance, and subject count.
     *
     * v2.3.1: Added isFrontCamera parameter for proper mirror coordinate handling.
     * Front camera preview is mirrored, but ML Kit returns original (non-mirrored) coordinates.
     * This causes left/right direction guidance to be inverted without correction.
     *
     * @param detectionResult Current ML Kit detection result
     * @param previewWidth Width of the preview view in pixels
     * @param previewHeight Height of the preview view in pixels
     * @param isFrontCamera True if using front camera (requires X coordinate mirroring)
     * @return CompositionState indicating composition quality and suggestions
     */
    operator fun invoke(
        detectionResult: DetectionResult,
        previewWidth: Int? = null,
        previewHeight: Int? = null,
        isFrontCamera: Boolean = false
    ): CompositionState {
        val faces = detectionResult.faces
        val objects = detectionResult.objects

        // v2.5.4: Priority-based composition analysis
        // Priority 1: Face composition (existing logic for portraits)
        if (faces.isNotEmpty()) {
            return analyzeFaceComposition(
                faces = faces,
                detectionResult = detectionResult,
                previewWidth = previewWidth,
                previewHeight = previewHeight,
                isFrontCamera = isFrontCamera
            )
        }

        // Priority 2: Object composition (new - for landscape images)
        if (objects.isNotEmpty()) {
            return analyzeObjectComposition(
                objects = objects,
                detectionResult = detectionResult,
                previewWidth = previewWidth,
                previewHeight = previewHeight
            )
        }

        // No subject detected
        return CompositionState.NoSubject
    }

    /**
     * Analyze composition using face detection (for portrait images)
     * Maintains all v2.5.3 logic without changes
     *
     * v2.5.4: Extracted from invoke() to support object-based composition
     */
    private fun analyzeFaceComposition(
        faces: List<com.google.mlkit.vision.face.Face>,
        detectionResult: DetectionResult,
        previewWidth: Int?,
        previewHeight: Int?,
        isFrontCamera: Boolean
    ): CompositionState {
        // Phase 3: Select primary subject (largest face if multiple detected)
        val primaryFace = selectPrimarySubject(faces)
        val boundingBox = primaryFace.boundingBox

        // v2.5.3: Validate bounding box before proceeding
        if (boundingBox.isEmpty || boundingBox.width() <= 0 || boundingBox.height() <= 0) {
            Log.e(TAG, "Invalid bounding box: $boundingBox (width=${boundingBox.width()}, height=${boundingBox.height()})")
            return CompositionState.NoSubject
        }
        Log.d(TAG, "Valid bounding box: $boundingBox (${boundingBox.width()}x${boundingBox.height()})")

        // Determine ML Kit coordinate space dimensions.
        // InputImage is created with rotationDegrees, so ML Kit outputs coordinates in the rotated (upright) space.
        val rotatedImageWidth: Float
        val rotatedImageHeight: Float
        if (detectionResult.rotationDegrees % 180 == 0) {
            rotatedImageWidth = detectionResult.imageWidth.toFloat()
            rotatedImageHeight = detectionResult.imageHeight.toFloat()
        } else {
            rotatedImageWidth = detectionResult.imageHeight.toFloat()
            rotatedImageHeight = detectionResult.imageWidth.toFloat()
        }

        // v2.5.3: Validate image dimensions are non-zero
        if (rotatedImageWidth <= 0f || rotatedImageHeight <= 0f) {
            Log.e(TAG, "Invalid image dimensions: ${rotatedImageWidth}x${rotatedImageHeight}")
            return CompositionState.NoSubject
        }

        // Compose/Preview view dimensions.
        // If not available, fall back to rotated image dimensions (less accurate for UI overlays).
        val viewWidth = previewWidth?.toFloat() ?: rotatedImageWidth
        val viewHeight = previewHeight?.toFloat() ?: rotatedImageHeight
        if (previewWidth == null || previewHeight == null) {
            Log.w(TAG, "Preview dimensions not provided, falling back to image size (overlays may be inaccurate)")
        }

        val viewTransform = ViewTransform(
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            imageWidth = rotatedImageWidth,
            imageHeight = rotatedImageHeight,
            mirrorX = isFrontCamera
        )

        val faceRect = viewTransform.mapRect(boundingBox)
        val eyeAnalysis = EyePositionAnalysis.create(
            face = primaryFace,
            imageHeight = viewHeight,
            mapPoint = viewTransform::mapPoint
        )

        // Calculate subject metrics
        val faceCenterX = (faceRect.left + faceRect.right) / 2f
        val faceLeft = faceRect.left
        val faceRight = faceRect.right

        // v2.5.3: Validate calculated center is valid
        if (faceCenterX.isNaN() || faceCenterX.isInfinite()) {
            Log.e(TAG, "Invalid faceCenterX: left=${faceRect.left}, right=${faceRect.right}")
            return CompositionState.NoSubject
        }

        val faceCenterY = (faceRect.top + faceRect.bottom) / 2f
        val faceTop = faceRect.top
        val faceBottom = faceRect.bottom

        if (faceCenterY.isNaN() || faceCenterY.isInfinite()) {
            Log.e(TAG, "Invalid faceCenterY: top=${faceRect.top}, bottom=${faceRect.bottom}")
            return CompositionState.NoSubject
        }

        Log.d(TAG, "Face center: (${faceCenterX.toInt()}, ${faceCenterY.toInt()})")

        // Phase 3: Perform multi-rule analysis
        val analysis = performCompositionAnalysis(
            face = primaryFace,
            faceCenterX = faceCenterX,
            faceCenterY = faceCenterY,
            faceTop = faceTop,
            faceLeft = faceLeft,
            faceRight = faceRight,
            faceBottom = faceBottom,
            imageWidth = viewWidth,
            imageHeight = viewHeight,
            faceCount = faces.size,
            eyeAnalysis = eyeAnalysis
        )

        // v2.4.0: Calculate visual composition guide frame in view space.
        val guideData = calculateGuideData(
            faceBoundingBox = faceRect,
            eyeAnalysis = analysis.eyeAnalysis,
            imageWidth = viewWidth,
            imageHeight = viewHeight
        )

        // v2.6.0: Generate fill-the-frame suggestion
        val (fillFrameIssue, fillFrameSuggestion) = generateFillFrameSuggestion(
            subjectWidth = faceRect.width,
            subjectHeight = faceRect.height,
            imageWidth = viewWidth,
            imageHeight = viewHeight
        )

        // Determine composition state based on overall score
        return if (analysis.overallScore >= GOOD_COMPOSITION_THRESHOLD) {
            CompositionState.Good(guideData = guideData)
        } else {
            val (message, dirX, dirY) = generateDetailedSuggestion(
                analysis, faceCenterX, faceCenterY, faceTop, faceLeft, faceRight, viewWidth, viewHeight
            )

            CompositionState.NeedsAdjustment(
                message = message,
                directionX = dirX,
                directionY = dirY,
                guideData = guideData,
                fillFrameIssue = fillFrameIssue,
                fillFrameSuggestion = fillFrameSuggestion
            )
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
        face: com.google.mlkit.vision.face.Face,
        faceCenterX: Float,
        faceCenterY: Float,
        faceTop: Float,
        faceLeft: Float,
        faceRight: Float,
        faceBottom: Float,
        imageWidth: Float,
        imageHeight: Float,
        faceCount: Int,
        eyeAnalysis: EyePositionAnalysis?
    ): CompositionAnalysis {
        // Rule 0: Eye Position Analysis (v2.3.0) - 人像攝影最高優先級
        val eyePositionScore = if (eyeAnalysis?.hasEyeLandmarks == true && eyeAnalysis.needsAdjustment) {
            // 根據偏差計算分數
            val normalizedDeviation = kotlin.math.abs(eyeAnalysis.deviationFromIdeal) / imageHeight
            (1f - normalizedDeviation * 3f).coerceIn(0f, 1f)
        } else {
            1.0f  // 沒有 landmark 或已對齊，給予滿分
        }

        // Rule 1: Rule of Thirds (25% weight)
        val ruleOfThirdsScore = analyzeRuleOfThirdsScore(
            faceCenterX, faceCenterY, imageWidth, imageHeight
        )

        // Rule 2: Headroom Analysis (25% weight)
        val headroomScore = analyzeHeadroomScore(
            faceTop, imageHeight
        )

        // Rule 3: Edge Distance & Fill-the-frame (25% weight total)
        // v2.6.0: Split into edge (60%) and fill-the-frame (40%) sub-scores
        val edgeDistanceScore = analyzeEdgeDistanceScore(
            faceLeft, faceRight, faceTop, faceBottom, imageWidth, imageHeight
        )

        // Calculate face dimensions for fill-the-frame analysis
        val faceWidth = faceRight - faceLeft
        val faceHeight = faceBottom - faceTop

        val fillFrameScore = analyzeFillFrameScore(
            subjectWidth = faceWidth,
            subjectHeight = faceHeight,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        // Combined edge score: edge (60%) + fill (40%)
        val combinedEdgeScore = edgeDistanceScore * SUB_WEIGHT_EDGE + fillFrameScore * SUB_WEIGHT_FILL_FRAME

        // Rule 4: Subject Detection (10% weight)
        val subjectDetectionScore = analyzeSubjectDetectionScore(faceCount)

        // Calculate weighted overall score (v2.3.0: includes eye position, v2.6.0: includes fill-the-frame)
        val overallScore =
            eyePositionScore * WEIGHT_EYE_POSITION +
            ruleOfThirdsScore * WEIGHT_RULE_OF_THIRDS +
            headroomScore * WEIGHT_HEADROOM +
            combinedEdgeScore * WEIGHT_EDGE_DISTANCE +
            subjectDetectionScore * WEIGHT_SUBJECT_DETECTION

        // v2.6.0: Validate all scores for NaN/Infinity (including fill-frame)
        val hasNaN = listOf(
            eyePositionScore, ruleOfThirdsScore, headroomScore,
            edgeDistanceScore, fillFrameScore, combinedEdgeScore,
            subjectDetectionScore, overallScore
        ).any { it.isNaN() || it.isInfinite() }

        if (hasNaN) {
            Log.e("ScoreValidation", "❌ NaN/Infinite detected!")
            Log.e("ScoreValidation", "  Eye: $eyePositionScore")
            Log.e("ScoreValidation", "  Thirds: $ruleOfThirdsScore")
            Log.e("ScoreValidation", "  Headroom: $headroomScore")
            Log.e("ScoreValidation", "  Edge: $edgeDistanceScore")
            Log.e("ScoreValidation", "  Fill: $fillFrameScore")
            Log.e("ScoreValidation", "  Combined Edge: $combinedEdgeScore")
            Log.e("ScoreValidation", "  Subject: $subjectDetectionScore")
            Log.e("ScoreValidation", "  Overall: $overallScore")
        } else {
            Log.i("ScoreValidation", "✓ All scores valid (Overall: ${"%.2f".format(overallScore)}, Fill: ${"%.2f".format(fillFrameScore)})")
        }

        return CompositionAnalysis(
            ruleOfThirdsScore = ruleOfThirdsScore,
            headroomScore = headroomScore,
            edgeDistanceScore = edgeDistanceScore,
            subjectDetectionScore = subjectDetectionScore,
            eyePositionScore = eyePositionScore,
            overallScore = overallScore,
            eyeAnalysis = eyeAnalysis
        )
    }

    /**
     * Analyzes Rule of Thirds and returns a score (0.0 - 1.0)
     * v2.5.3: Added dimension validation and safe division to prevent NaN
     */
    private fun analyzeRuleOfThirdsScore(
        faceCenterX: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Float {
        // v2.5.3: Validate inputs
        if (imageWidth <= 0f || imageHeight <= 0f) {
            Log.w(TAG, "Invalid dimensions for Rule of Thirds: ${imageWidth}x${imageHeight}")
            return 0f
        }

        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f
        val tolerance = imageWidth * 0.1f

        // v2.5.3: Guard against zero tolerance
        if (tolerance == 0f) {
            Log.w(TAG, "Tolerance is zero, cannot calculate Rule of Thirds")
            return 0f
        }

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
        // v2.5.3: Use safe division
        val horizontalScore = 1f - safeDivide(horizontalDistance, tolerance, 1f).coerceIn(0f, 1f)
        val verticalScore = 1f - safeDivide(verticalDistance, tolerance, 1f).coerceIn(0f, 1f)

        val finalScore = (horizontalScore + verticalScore) / 2f

        Log.d(TAG, "Rule of Thirds: H=${"%.2f".format(horizontalScore)}, V=${"%.2f".format(verticalScore)}, Final=${"%.2f".format(finalScore)}")
        return finalScore
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
     * Analyzes fill-the-frame composition and returns a score (0.0 - 1.0)
     *
     * v2.6.0: Fill-the-frame composition analysis
     *
     * Evaluates how much of the frame the subject occupies:
     * - Too little (< 30%): Subject too small, wasted space
     * - Ideal (40-70%): Perfect fill, balanced composition
     * - Too much (> 85%): Subject too large, cramped feeling
     *
     * Scoring:
     * - 1.0: Subject area in ideal range (40-70%)
     * - 0.7-1.0: Acceptable range (30-85%)
     * - < 0.7: Poor fill (too small or too large)
     *
     * @param subjectWidth Width of subject bounding box
     * @param subjectHeight Height of subject bounding box
     * @param imageWidth Total image width
     * @param imageHeight Total image height
     * @return Fill-the-frame score (0.0-1.0)
     */
    private fun analyzeFillFrameScore(
        subjectWidth: Float,
        subjectHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Float {
        // Calculate subject area ratio (subject area / image area)
        val subjectArea = subjectWidth * subjectHeight
        val imageArea = imageWidth * imageHeight
        val areaRatio = safeDivide(subjectArea, imageArea, 0f)

        // Validate ratio
        if (areaRatio <= 0f || areaRatio > 1f) {
            Log.w(TAG, "Invalid area ratio: $areaRatio (subject: $subjectArea, image: $imageArea)")
            return 0.5f // Neutral score for invalid data
        }

        // Score based on fill ratio
        val score = when {
            // Ideal range: 40-70% (perfect fill)
            areaRatio >= FILL_FRAME_IDEAL_MIN && areaRatio <= FILL_FRAME_IDEAL_MAX -> {
                1.0f // Perfect score
            }

            // Acceptable low range: 30-40% (subject a bit small)
            areaRatio >= FILL_FRAME_MIN && areaRatio < FILL_FRAME_IDEAL_MIN -> {
                // Linear interpolation: 0.7 at 30%, 1.0 at 40%
                0.7f + (areaRatio - FILL_FRAME_MIN) / (FILL_FRAME_IDEAL_MIN - FILL_FRAME_MIN) * 0.3f
            }

            // Acceptable high range: 70-85% (subject a bit large)
            areaRatio > FILL_FRAME_IDEAL_MAX && areaRatio <= FILL_FRAME_MAX -> {
                // Linear interpolation: 1.0 at 70%, 0.7 at 85%
                1.0f - (areaRatio - FILL_FRAME_IDEAL_MAX) / (FILL_FRAME_MAX - FILL_FRAME_IDEAL_MAX) * 0.3f
            }

            // Too small: < 30% (subject too far/small)
            areaRatio < FILL_FRAME_MIN -> {
                // Linear decrease: 0.7 at 30%, 0.0 at 0%
                (areaRatio / FILL_FRAME_MIN) * 0.7f
            }

            // Too large: > 85% (subject too close/large)
            areaRatio > FILL_FRAME_MAX -> {
                // Linear decrease: 0.7 at 85%, 0.0 at 100%
                0.7f - ((areaRatio - FILL_FRAME_MAX) / (1.0f - FILL_FRAME_MAX)) * 0.7f
            }

            else -> 0.5f // Fallback (should not reach here)
        }

        Log.d(TAG, "Fill-the-frame: ratio=${"%.2f".format(areaRatio * 100)}%, score=${"%.2f".format(score)}")
        return score.coerceIn(0f, 1f)
    }

    /**
     * Generate fill-the-frame suggestion based on subject area ratio
     *
     * v2.6.0: Provides Traditional Chinese suggestions for fill-frame adjustment
     *
     * @param subjectWidth Width of subject bounding box
     * @param subjectHeight Height of subject bounding box
     * @param imageWidth Image width
     * @param imageHeight Image height
     * @return Pair of (hasIssue: Boolean, suggestion: String)
     */
    private fun generateFillFrameSuggestion(
        subjectWidth: Float,
        subjectHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Pair<Boolean, String> {
        val subjectArea = subjectWidth * subjectHeight
        val imageArea = imageWidth * imageHeight
        val areaRatio = safeDivide(subjectArea, imageArea, 0f)

        return when {
            // Too small: < 30% → move closer
            areaRatio < FILL_FRAME_MIN -> {
                true to "主體過小，建議靠近拍攝"
            }

            // Acceptable low: 30-40% → slightly small but ok
            areaRatio >= FILL_FRAME_MIN && areaRatio < FILL_FRAME_IDEAL_MIN -> {
                true to "主體稍小，可以再靠近一點"
            }

            // Ideal: 40-70% → perfect
            areaRatio >= FILL_FRAME_IDEAL_MIN && areaRatio <= FILL_FRAME_IDEAL_MAX -> {
                false to ""
            }

            // Acceptable high: 70-85% → slightly large but ok
            areaRatio > FILL_FRAME_IDEAL_MAX && areaRatio <= FILL_FRAME_MAX -> {
                true to "主體稍大，可以退後一點"
            }

            // Too large: > 85% → move back
            areaRatio > FILL_FRAME_MAX -> {
                true to "主體過大，建議退後拍攝"
            }

            else -> false to ""
        }
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
     * 生成專業中文建議
     *
     * v2.1.1: 改為中文專業建議
     * v2.2.2: 移除方向箭頭，directionX/Y 保持為 0
     * v2.3.0: 優先級系統、眼睛位置引導、改進邊界訊息
     * v2.3.1: 全面重寫提示詞系統，更精細的構圖引導
     *
     * 優先級順序（高到低）：
     * 1. 邊界警告（防止裁切）
     * 2. 眼睛位置（人像攝影關鍵）
     * 3. 頭部空間（構圖平衡）
     * 4. 三分法則（美學建議）
     */
    private fun generateDetailedSuggestion(
        analysis: CompositionAnalysis,
        faceCenterX: Float,
        faceCenterY: Float,
        faceTop: Float,
        faceLeft: Float,
        faceRight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Triple<String, Float, Float> {
        val issues = mutableListOf<CompositionIssue>()

        // 計算關鍵位置參數
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f
        val centerX = imageWidth / 2f
        val headroomRatio = faceTop / imageHeight

        // Debug log 所有分數
        Log.d(TAG, "Scores - edge:${analysis.edgeDistanceScore} eye:${analysis.eyePositionScore} " +
              "headroom:${analysis.headroomScore} thirds:${analysis.ruleOfThirdsScore} overall:${analysis.overallScore}")

        // 問題 1: 邊界警告（最高優先級 - 防止裁切）
        // v2.3.2: 降低閾值到 0.5f，只在真正接近邊界時才警告
        if (analysis.edgeDistanceScore < 0.5f) {
            val edgeMessage = generateEdgeBoundaryMessage(
                faceLeft, faceRight, faceTop, faceCenterY, imageWidth, imageHeight
            )
            issues.add(CompositionIssue(
                priority = CompositionIssuePriority.EDGE_BOUNDARY,
                score = analysis.edgeDistanceScore,
                message = edgeMessage
            ))
        }

        // 問題 2: 眼睛位置（高優先級 - 人像攝影關鍵）
        analysis.eyeAnalysis?.let { eyeAnalysis ->
            if (eyeAnalysis.hasEyeLandmarks && eyeAnalysis.needsAdjustment) {
                val eyeMessage = generateEyePositionMessage(eyeAnalysis)
                issues.add(CompositionIssue(
                    priority = CompositionIssuePriority.EYE_POSITION,
                    score = analysis.eyePositionScore,
                    message = eyeMessage
                ))
            }
        }

        // 問題 3: 頭部空間（中優先級 - 構圖平衡）
        // v2.3.2: 降低閾值到 0.6f
        if (analysis.headroomScore < 0.6f) {
            val headroomMessage = generateHeadroomMessage(headroomRatio)
            issues.add(CompositionIssue(
                priority = CompositionIssuePriority.HEADROOM,
                score = analysis.headroomScore,
                message = headroomMessage
            ))
        }

        // 問題 4: 水平位置 - 臉部偏離三分交點（低優先級 - 美學建議）
        // v2.3.2: 降低閾值到 0.6f
        if (analysis.ruleOfThirdsScore < 0.6f) {
            val horizontalMessage = generateHorizontalPositionMessage(
                faceCenterX, centerX, leftThird, rightThird
            )
            if (horizontalMessage != null) {
                issues.add(CompositionIssue(
                    priority = CompositionIssuePriority.RULE_OF_THIRDS,
                    score = analysis.ruleOfThirdsScore,
                    message = horizontalMessage
                ))
            }
        }

        // 問題 5: 垂直位置 - 主體過高或過低（低優先級）
        val verticalMessage = generateVerticalPositionMessage(
            faceCenterY, topThird, bottomThird, imageHeight
        )
        if (verticalMessage != null && analysis.ruleOfThirdsScore < 0.6f) {
            issues.add(CompositionIssue(
                priority = CompositionIssuePriority.RULE_OF_THIRDS,
                score = analysis.ruleOfThirdsScore,
                message = verticalMessage
            ))
        }

        // 返回最高優先級問題，或「構圖良好」
        val message = issues.maxByOrNull { it.priority.priority }?.message ?: "構圖良好"

        Log.d(TAG, "Final message: $message (${issues.size} issues found)")

        // v2.2.2: 方向箭頭已移除，保持為 0
        return Triple(message, 0f, 0f)
    }

    /**
     * 生成眼睛位置提示訊息
     * v2.3.1: 更精細的眼睛位置引導
     * v2.4.2: 配合灰色引導框，精準引導眼睛對準
     * v2.4.3: 統一訊息，灰色框本身就是視覺引導
     */
    private fun generateEyePositionMessage(eyeAnalysis: EyePositionAnalysis): String {
        return "請將眼睛對準灰色框中央"
    }

    /**
     * 生成頭部空間提示訊息
     * v2.3.1: 更精細的頭部空間引導
     */
    private fun generateHeadroomMessage(headroomRatio: Float): String {
        return when {
            // 頭部空間過少（可能被裁切）
            headroomRatio < 0.02f -> "頭部略被切到，請提高相機"
            headroomRatio < IDEAL_HEADROOM_MIN -> "調整角度避免裁切額頭"
            // 頭部空間過多
            headroomRatio > 0.25f -> "頭部空間偏多，請稍微下壓相機"
            headroomRatio > IDEAL_HEADROOM_MAX -> "適度減少上方空間會使人像更平衡"
            else -> "調整頭部空間"
        }
    }

    /**
     * 生成水平位置提示訊息
     * v2.3.1: 針對三分交點的水平引導
     */
    private fun generateHorizontalPositionMessage(
        faceCenterX: Float,
        centerX: Float,
        leftThird: Float,
        rightThird: Float
    ): String? {
        // 計算容差範圍（中央區域）
        val centerTolerance = (rightThird - leftThird) * 0.3f
        val isNearCenter = faceCenterX > (centerX - centerTolerance) &&
                           faceCenterX < (centerX + centerTolerance)

        return when {
            // 臉部在中央，建議移向三分交點
            isNearCenter -> "人物略偏中央，可向左或右調整以強化視覺焦點"
            // 臉部偏左太多（超出左三分線）
            faceCenterX < leftThird * 0.7f -> "將人物向右輕移，使臉位於三分交點"
            // 臉部偏右太多（超出右三分線）
            faceCenterX > rightThird * 1.3f -> "將人物向左輕移，使臉位於三分交點"
            else -> null  // 已在三分線附近，無需調整
        }
    }

    /**
     * 生成垂直位置提示訊息
     * v2.3.1: 針對主體過高或過低的引導
     */
    private fun generateVerticalPositionMessage(
        faceCenterY: Float,
        topThird: Float,
        bottomThird: Float,
        imageHeight: Float
    ): String? {
        return when {
            // 主體靠太高（接近畫面頂端）
            faceCenterY < topThird * 0.6f -> "人物太高，略微調低相機"
            // 主體靠太低（位於畫面下方）
            faceCenterY > bottomThird * 1.2f -> "人物位置偏低，可抬高相機"
            faceCenterY > imageHeight * 0.7f -> "將主體移向畫面上半部更佳"
            else -> null
        }
    }

    /**
     * 生成邊界特定訊息
     * v2.3.1: 更友善的邊界警告
     * v2.3.2: 添加 debug log，修正邊界判斷邏輯
     */
    private fun generateEdgeBoundaryMessage(
        faceLeft: Float,
        faceRight: Float,
        faceTop: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): String {
        val leftDist = faceLeft
        val rightDist = imageWidth - faceRight
        val topDist = faceTop
        val bottomDist = imageHeight - faceCenterY

        // Debug log 座標資訊
        Log.d(TAG, "Edge distances - L:$leftDist R:$rightDist T:$topDist B:$bottomDist | " +
              "faceLeft:$faceLeft faceRight:$faceRight imageWidth:$imageWidth")

        val minDist = minOf(leftDist, rightDist, topDist, bottomDist)
        val minEdge = imageWidth * MIN_EDGE_DISTANCE

        // 判斷哪個邊最接近，並根據嚴重程度給出不同訊息
        return when {
            minDist == leftDist -> {
                if (leftDist < minEdge * 0.5f) "臉部快超出左邊界，請向右移動"
                else "人物略靠左側，可向右微調"
            }
            minDist == rightDist -> {
                if (rightDist < minEdge * 0.5f) "臉部快超出右邊界，請向左移動"
                else "人物略靠右側，可向左微調"
            }
            minDist == topDist -> {
                if (topDist < minEdge * 0.5f) "頭部快超出上邊界，請下移相機"
                else "人物略靠上方，請稍微下移"
            }
            minDist == bottomDist -> {
                if (bottomDist < minEdge * 0.5f) "主體快超出下邊界，請上移相機"
                else "人物略靠下方，請稍微上移"
            }
            else -> "請調整位置避免主體被裁切"
        }
    }

    /**
     * Analyzes fill-the-frame composition and returns ratio
     *
     * v2.2.0: Added for Fill the Frame composition guidance
     *
     * @param faceWidth Width of face bounding box
     * @param faceHeight Height of face bounding box
     * @param imageWidth Image width
     * @param imageHeight Image height
     * @return Subject fill ratio (0.0 - 1.0)
     */
    fun analyzeSubjectFillRatio(
        faceWidth: Float,
        faceHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Float {
        val subjectArea = faceWidth * faceHeight
        val imageArea = imageWidth * imageHeight
        return (subjectArea / imageArea).coerceIn(0f, 1f)
    }

    /**
     * Generate fill-the-frame suggestion based on subject size
     *
     * v2.2.0: Chinese suggestions for closeup/portrait composition
     *
     * @param fillRatio Subject fill ratio (0.0 - 1.0)
     * @return Chinese suggestion or null if composition is good
     */
    fun getFillFrameSuggestion(fillRatio: Float): String? {
        return when {
            // Too small - suggest closer framing (closeup/food/portrait)
            fillRatio < 0.08f -> "靠近一點，讓主體填滿畫面更具吸引力"

            // Small but acceptable for environmental portrait
            fillRatio < 0.15f -> "可再靠近些以突出主體細節"

            // Optimal range for portraits/closeups (15-40%)
            fillRatio in 0.15f..0.40f -> null // Good composition

            // Large but still acceptable
            fillRatio in 0.40f..0.60f -> null // Good for tight framing

            // Too large - warn about cropping
            fillRatio > 0.60f -> "主體過大，建議保留適當空間"

            else -> null
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
        val eyePositionScore: Float,
        val overallScore: Float,
        val eyeAnalysis: EyePositionAnalysis?
    )

    /**
     * 眼睛位置分析結果
     * v2.3.0: 專業人像構圖 - 眼睛應位於上三分線
     */
    private data class EyePositionAnalysis(
        val hasEyeLandmarks: Boolean,        // 是否成功取得眼睛座標
        val averageEyeY: Float,              // 左右眼平均 Y 座標
        val upperThirdLine: Float,           // 上三分線位置
        val deviationFromIdeal: Float,       // 與理想位置的偏差
        val needsAdjustment: Boolean,        // 是否需要調整
        val isAboveIdeal: Boolean            // 是否高於理想位置
    ) {
         companion object {
             private const val EYE_POSITION_TOLERANCE_RATIO = 0.08f  // 容差 8%

             fun create(
                 face: com.google.mlkit.vision.face.Face,
                 imageHeight: Float,
                 mapPoint: (PointF) -> PointF = { it }
             ): EyePositionAnalysis {
                 val leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)
                 val rightEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)

                // 檢查是否有眼睛座標
                if (leftEye == null || rightEye == null) {
                    return EyePositionAnalysis(
                        hasEyeLandmarks = false,
                        averageEyeY = 0f,
                        upperThirdLine = imageHeight / 3f,
                        deviationFromIdeal = 0f,
                        needsAdjustment = false,
                        isAboveIdeal = false
                    )
                 }

                 // 計算左右眼平均 Y 座標
                 val leftPos = mapPoint(leftEye.position)
                 val rightPos = mapPoint(rightEye.position)
                 val avgEyeY = (leftPos.y + rightPos.y) / 2f

                // 上三分線（理想眼睛位置）
                val upperThird = imageHeight / 3f

                // 容差（8% 畫面高度）
                val tolerance = imageHeight * EYE_POSITION_TOLERANCE_RATIO

                // 計算偏差
                val deviation = avgEyeY - upperThird

                return EyePositionAnalysis(
                    hasEyeLandmarks = true,
                    averageEyeY = avgEyeY,
                    upperThirdLine = upperThird,
                    deviationFromIdeal = deviation,
                    needsAdjustment = kotlin.math.abs(deviation) > tolerance,
                    isAboveIdeal = avgEyeY < upperThird
                )
             }
         }
     }

    /**
     * 構圖問題優先級
     * 數字越大 = 優先級越高
     *
     * v2.3.0: 多問題場景下的訊息優先級系統
     */
    private enum class CompositionIssuePriority(val priority: Int) {
        RULE_OF_THIRDS(1),      // 最低 - 美學建議
        HEADROOM(2),            // 低 - 次要調整
        EYE_POSITION(3),        // 高 - 人像攝影關鍵
        EDGE_BOUNDARY(4)        // 最高 - 防止裁切
    }

    /**
     * 構圖問題資料
     */
    private data class CompositionIssue(
        val priority: CompositionIssuePriority,
        val score: Float,
        val message: String
    )

    /**
     * 計算構圖引導框框資料
     * v2.4.0: 視覺引導功能 - 顯示人臉應該移動到的目標位置
     *
     * @param faceBoundingBox 當前人臉邊界框
     * @param eyeAnalysis 眼睛位置分析結果
     * @param imageWidth 畫面寬度
     * @param imageHeight 畫面高度
     * @param isFrontCamera 是否為前置鏡頭
     * @return CompositionGuideData 或 null（如果不需要顯示引導框）
     */
    private fun calculateGuideData(
        faceBoundingBox: Rect,
        eyeAnalysis: EyePositionAnalysis?,
        imageWidth: Float,
        imageHeight: Float,
    ): CompositionGuideData? {
        // 如果沒有眼睛 landmark，使用臉部框估算眼睛位置
        val currentEyeY: Float
        val upperThirdLine = imageHeight / 3f
        // v2.4.6: 放寬容差以改善用戶體驗 - 實際測試中偏差常達500-600px
        val tolerance = imageHeight * 0.15f  // 從 0.08f 增為 0.15f (~360px for 2405px)

        if (eyeAnalysis?.hasEyeLandmarks == true) {
            currentEyeY = eyeAnalysis.averageEyeY
        } else {
            // 估算眼睛位置 ≈ 臉部頂端 + 35% 臉部高度
            currentEyeY = faceBoundingBox.top + faceBoundingBox.height * 0.35f
        }

        // 計算眼睛需要移動的距離
        val eyeOffsetY = upperThirdLine - currentEyeY

        Log.d(TAG, "GuideData calc - eyeY:$currentEyeY upperThird:$upperThirdLine offset:$eyeOffsetY tolerance:$tolerance")

        // 檢查人臉大小是否適合顯示引導框
        val faceArea = faceBoundingBox.width * faceBoundingBox.height
        val imageArea = imageWidth * imageHeight
        val faceRatio = faceArea / imageArea

        Log.d(TAG, "GuideData: faceRatio=$faceRatio (area=$faceArea, imageArea=$imageArea)")

        // 人臉過小 (<0.1%) 或過大 (>60%) 不顯示引導框
        // v2.4.1: 降低最小閾值從 5% 到 0.1%，以適應遠距離人像場景
        if (faceRatio < 0.001f || faceRatio > 0.60f) {
            Log.d(TAG, "GuideData: returning null - face ratio out of range (faceRatio=$faceRatio)")
            return null
        }

        // 計算目標框框位置（將當前框框向上/下偏移）
        val targetTop = (faceBoundingBox.top + eyeOffsetY).coerceIn(0f, imageHeight - faceBoundingBox.height)
        val targetBottom = targetTop + faceBoundingBox.height

        // v2.4.3: 計算水平目標位置（左三分線或右三分線）
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        // v2.4.6: 與垂直容差保持一致性
        val horizontalTolerance = imageWidth * 0.15f  // 從 0.08f 增為 0.15f

        val currentLeft = faceBoundingBox.left
        val currentRight = faceBoundingBox.right

        // 計算當前臉部中心 X 座標
        val faceCenterX = (currentLeft + currentRight) / 2f

        // v2.4.6: 修正水平引導邏輯倒反 - 人臉偏左應引導向右三分線，反之亦然
        // 攝影美學：偏離中心的人臉應引導回構圖平衡點
        val targetCenterX = if (faceCenterX < imageWidth / 2f) {
            rightThird  // 臉部在左半邊，目標是右三分線（引導向中心）
        } else {
            leftThird   // 臉部在右半邊，目標是左三分線（引導向中心）
        }

        // 計算水平偏移
        val horizontalOffset = targetCenterX - faceCenterX
        val faceWidth = currentRight - currentLeft

        Log.d(TAG, "GuideData horizontal - faceCenterX:$faceCenterX targetCenterX:$targetCenterX offsetX:$horizontalOffset tolerance:$horizontalTolerance")

        // v2.4.3+: 同時檢查垂直和水平對齊，都在容差內視為對齊（顯示綠框）
        val verticalAligned = abs(eyeOffsetY) <= tolerance
        val horizontalAligned = abs(horizontalOffset) <= horizontalTolerance

        // 目標框框水平位置
        // v2.9.5: Fix coerce range error when faceWidth > imageWidth (view space mismatch)
        val maxLeft = (imageWidth - faceWidth).coerceAtLeast(0f)
        val targetLeft = (targetCenterX - faceWidth / 2f).coerceIn(0f, maxLeft)
        val targetRight = targetLeft + faceWidth

        val targetRect = Rect(
            left = targetLeft,
            top = targetTop,
            right = targetRight,
            bottom = targetBottom
        )

        val currentRect = faceBoundingBox

        // 計算垂直對齊進度
        val verticalAlignmentProgress = calculateAlignmentProgress(
            current = currentEyeY,
            target = upperThirdLine,
            tolerance = tolerance
        )

        // v2.4.3: 計算水平對齊進度
        val horizontalAlignmentProgress = calculateAlignmentProgress(
            current = faceCenterX,
            target = targetCenterX,
            tolerance = horizontalTolerance
        )

        // 綜合對齊進度（垂直和水平的平均）
        val isFullyAligned = verticalAligned && horizontalAligned
        val overallAlignmentProgress = if (isFullyAligned) {
            1f
        } else {
            (verticalAlignmentProgress + horizontalAlignmentProgress) / 2f
        }

        Log.d(TAG, "GuideData: targetRect=$targetRect, vProgress=$verticalAlignmentProgress, hProgress=$horizontalAlignmentProgress, overall=$overallAlignmentProgress")

        return CompositionGuideData(
            targetRect = targetRect,
            currentRect = currentRect,
            isAligned = isFullyAligned,
            alignmentProgress = overallAlignmentProgress
        )
    }

    /**
     * 計算對齊進度
     * v2.4.0: 用於視覺引導框顏色漸變
     * v2.5.3: Added tolerance validation and safe division to prevent NaN
     *
     * @param current 當前座標值
     * @param target 目標座標值
     * @param tolerance 容差範圍
     * @return 0.0（遠離）到 1.0（完美對齊）
     */
    private fun calculateAlignmentProgress(
        current: Float,
        target: Float,
        tolerance: Float
    ): Float {
        // v2.5.3: Validate inputs
        if (tolerance <= 0f) {
            Log.w(TAG, "calculateAlignmentProgress: Invalid tolerance ($tolerance)")
            return 0f
        }

        if (current.isNaN() || target.isNaN()) {
            Log.w(TAG, "calculateAlignmentProgress: NaN detected (current=$current, target=$target)")
            return 0f
        }

        val distance = abs(current - target)
        val maxDistance = tolerance * 2f

        // v2.5.3: Use safe division
        val progress = safeDivide(distance, maxDistance, 1f)
        val finalProgress = (1f - progress).coerceIn(0f, 1f)

        Log.d(TAG, "Alignment progress: distance=${"%.1f".format(distance)}, progress=${"%.2f".format(finalProgress)}")
        return finalProgress
    }

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

    // ==================== v2.5.4: Object-Based Composition Analysis ====================

    /**
     * Analyze composition using object detection (for landscape images without faces)
     *
     * v2.5.4: New functionality for landscape photography
     * Uses ML Kit Object Detection to analyze composition of non-portrait scenes
     *
     * Scoring algorithm:
     * - Rule of Thirds: 40% weight (vs 30% for faces)
     * - Edge Distance: 35% weight (vs 25% for faces)
     * - Symmetry: 25% weight (new - landscape-specific)
     * - Overall score multiplied by 0.85 (object bbox less accurate than face landmarks)
     *
     * @param objects List of detected objects (already filtered by confidence >= 0.5)
     * @param detectionResult Full detection result with image dimensions
     * @param previewWidth Width of the preview view in pixels
     * @param previewHeight Height of the preview view in pixels
     * @return CompositionState indicating composition quality and suggestions
     */
    private fun analyzeObjectComposition(
        objects: List<com.google.mlkit.vision.objects.DetectedObject>,
        detectionResult: DetectionResult,
        previewWidth: Int?,
        previewHeight: Int?
    ): CompositionState {
        // 1. Select primary object (largest by area)
        val primaryObject = objects.maxByOrNull {
            it.boundingBox.width() * it.boundingBox.height()
        } ?: return CompositionState.NoSubject

        val boundingBox = primaryObject.boundingBox

        // 2. Validate bounding box
        if (boundingBox.isEmpty || boundingBox.width() <= 0 || boundingBox.height() <= 0) {
            Log.e(TAG, "analyzeObjectComposition: Invalid object bounding box: $boundingBox")
            return CompositionState.NoSubject
        }

        Log.d(TAG, "analyzeObjectComposition: Primary object bbox=$boundingBox (${boundingBox.width()}x${boundingBox.height()})")

        // 3. Determine image dimensions (same logic as face analysis)
        val rotatedImageWidth: Float
        val rotatedImageHeight: Float

        if (detectionResult.rotationDegrees == 90 || detectionResult.rotationDegrees == 270) {
            // Rotation is 90° or 270°: dimensions are swapped
            rotatedImageWidth = detectionResult.imageHeight.toFloat()
            rotatedImageHeight = detectionResult.imageWidth.toFloat()
        } else {
            // Rotation is 0° or 180°: dimensions remain same
            rotatedImageWidth = detectionResult.imageWidth.toFloat()
            rotatedImageHeight = detectionResult.imageHeight.toFloat()
        }

        // v2.5.3: Validate image dimensions
        if (rotatedImageWidth <= 0f || rotatedImageHeight <= 0f) {
            Log.e(TAG, "analyzeObjectComposition: Invalid image dimensions: ${rotatedImageWidth}x${rotatedImageHeight}")
            return CompositionState.NoSubject
        }

        Log.d(TAG, "analyzeObjectComposition: Image dimensions ${rotatedImageWidth}x${rotatedImageHeight} (rotation=${detectionResult.rotationDegrees}°)")

        // 4. Calculate object center coordinates
        val objectCenterX = boundingBox.exactCenterX()
        val objectCenterY = boundingBox.exactCenterY()

        // v2.5.3: NaN/Infinity validation
        if (objectCenterX.isNaN() || objectCenterX.isInfinite() ||
            objectCenterY.isNaN() || objectCenterY.isInfinite()) {
            Log.e(TAG, "analyzeObjectComposition: Invalid object center: ($objectCenterX, $objectCenterY)")
            return CompositionState.NoSubject
        }

        Log.d(TAG, "analyzeObjectComposition: Object center at (%.1f, %.1f)".format(objectCenterX, objectCenterY))

        // 5. Rule of Thirds analysis (reuse existing function)
        val ruleOfThirdsScore = analyzeRuleOfThirdsScore(
            faceCenterX = objectCenterX,
            faceCenterY = objectCenterY,
            imageWidth = rotatedImageWidth,
            imageHeight = rotatedImageHeight
        )

        // 6. Edge Distance analysis (reuse existing function)
        val edgeDistanceScore = analyzeEdgeDistanceScore(
            faceLeft = boundingBox.left.toFloat(),
            faceRight = boundingBox.right.toFloat(),
            faceTop = boundingBox.top.toFloat(),
            faceBottom = boundingBox.bottom.toFloat(),
            imageWidth = rotatedImageWidth,
            imageHeight = rotatedImageHeight
        )

        // 6.5. Fill-the-frame analysis (v2.6.0)
        val fillFrameScore = analyzeFillFrameScore(
            subjectWidth = boundingBox.width().toFloat(),
            subjectHeight = boundingBox.height().toFloat(),
            imageWidth = rotatedImageWidth,
            imageHeight = rotatedImageHeight
        )

        // Combined edge score: edge (60%) + fill (40%)
        val combinedEdgeScore = edgeDistanceScore * SUB_WEIGHT_EDGE + fillFrameScore * SUB_WEIGHT_FILL_FRAME

        // 7. Symmetry analysis (new - landscape-specific)
        val symmetryScore = calculateSymmetryScore(
            centerX = objectCenterX,
            imageWidth = rotatedImageWidth
        )

        // 8. Calculate weighted overall score (v2.6.0: uses combinedEdgeScore)
        val rawScore = ruleOfThirdsScore * 0.40f + combinedEdgeScore * 0.35f + symmetryScore * 0.25f

        // Apply confidence discount for object detection (less accurate than face landmarks)
        val overallScore = rawScore * 0.85f

        // 9. Validate scores (prevent NaN propagation) - v2.6.0: includes fill-frame
        val scores = listOf(ruleOfThirdsScore, edgeDistanceScore, fillFrameScore, combinedEdgeScore, symmetryScore, overallScore)
        if (scores.any { it.isNaN() || it.isInfinite() }) {
            Log.e(TAG, "analyzeObjectComposition: NaN/Infinite detected in scores - thirds:$ruleOfThirdsScore, edge:$edgeDistanceScore, fill:$fillFrameScore, combined:$combinedEdgeScore, symmetry:$symmetryScore, overall:$overallScore")
            return CompositionState.NoSubject
        }

        Log.i(TAG, "Object Scores - thirds:%.2f edge:%.2f fill:%.2f combined:%.2f symmetry:%.2f overall:%.2f".format(
            ruleOfThirdsScore, edgeDistanceScore, fillFrameScore, combinedEdgeScore, symmetryScore, overallScore
        ))

        // 9.5. Generate fill-the-frame suggestion (v2.6.0)
        val (fillFrameIssue, fillFrameSuggestion) = generateFillFrameSuggestion(
            subjectWidth = boundingBox.width().toFloat(),
            subjectHeight = boundingBox.height().toFloat(),
            imageWidth = rotatedImageWidth,
            imageHeight = rotatedImageHeight
        )

        // 10. Determine composition state (more lenient thresholds for objects)
        return when {
            overallScore >= 0.65f -> {
                Log.i(TAG, "✓ All scores valid (Overall: %.2f)".format(overallScore))
                CompositionState.Good(
                    guideData = null  // v2.5.4: Guide data not yet implemented for objects
                )
            }
            overallScore >= 0.40f -> {
                val (directionX, directionY) = calculateObjectAdjustmentDirection(
                    currentX = objectCenterX,
                    currentY = objectCenterY,
                    imageWidth = rotatedImageWidth,
                    imageHeight = rotatedImageHeight
                )
                CompositionState.NeedsAdjustment(
                    message = "Adjust object position for better composition",
                    directionX = directionX,
                    directionY = directionY,
                    guideData = null,
                    fillFrameIssue = fillFrameIssue,
                    fillFrameSuggestion = fillFrameSuggestion
                )
            }
            else -> CompositionState.NoSubject
        }
    }

    /**
     * Calculate symmetry score for landscape composition
     *
     * v2.5.4: New scoring function for landscape photography
     * Checks if the object is positioned at the horizontal center of the frame
     * Higher score when object is centered (good for symmetrical compositions)
     *
     * @param centerX Object center X coordinate
     * @param imageWidth Image width
     * @return Symmetry score (0.0 to 1.0)
     */
    private fun calculateSymmetryScore(
        centerX: Float,
        imageWidth: Float
    ): Float {
        // Calculate image center using safe division
        val imageCenterX = safeDivide(imageWidth, 2f, imageWidth / 2f)

        // Calculate deviation from center
        val deviationFromCenter = abs(centerX - imageCenterX)
        val maxDeviation = safeDivide(imageWidth, 2f, imageWidth / 2f)

        // Normalize deviation (0 = centered, 1 = at edge)
        val normalizedDeviation = safeDivide(deviationFromCenter, maxDeviation, 1f)

        // Convert to score (1 = centered, 0 = at edge)
        val score = (1f - normalizedDeviation).coerceIn(0f, 1f)

        Log.d(TAG, "Symmetry: deviation=%.1f, score=%.2f".format(deviationFromCenter, score))
        return score
    }

    /**
     * Calculate adjustment direction vector for object composition
     *
     * v2.5.4: Guides user to move camera for better object composition
     * Calculates direction to nearest Rule of Thirds intersection point
     *
     * @param currentX Current object center X
     * @param currentY Current object center Y
     * @param imageWidth Image width
     * @param imageHeight Image height
     * @return Pair of (directionX, directionY) normalized to [-1, 1]
     */
    private fun calculateObjectAdjustmentDirection(
        currentX: Float,
        currentY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Pair<Float, Float> {
        // Calculate third positions using safe division
        val thirdX = safeDivide(imageWidth, 3f, imageWidth / 3f)
        val thirdY = safeDivide(imageHeight, 3f, imageHeight / 3f)

        val leftThird = thirdX
        val rightThird = imageWidth - thirdX
        val topThird = thirdY
        val bottomThird = imageHeight - thirdY

        // Find nearest third intersection point (horizontal)
        val targetX = when {
            abs(currentX - leftThird) < abs(currentX - rightThird) -> leftThird
            else -> rightThird
        }

        // Find nearest third intersection point (vertical)
        val targetY = when {
            abs(currentY - topThird) < abs(currentY - bottomThird) -> topThird
            else -> bottomThird
        }

        // Calculate direction vector (normalized)
        val directionX = safeDivide(targetX - currentX, imageWidth, 0f).coerceIn(-1f, 1f)
        val directionY = safeDivide(targetY - currentY, imageHeight, 0f).coerceIn(-1f, 1f)

        Log.d(TAG, "Object adjustment direction: (%.2f, %.2f) -> target (%.0f, %.0f)".format(
            directionX, directionY, targetX, targetY
        ))
        return Pair(directionX, directionY)
    }
}
