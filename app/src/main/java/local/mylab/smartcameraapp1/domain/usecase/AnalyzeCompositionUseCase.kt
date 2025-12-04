/**
 * ClassName: AnalyzeCompositionUseCase
 * Description: Domain layer use case for analyzing photo composition using photography rules
 * Author: Claude Code
 * Date: 2025-11-27
 * Version: 2.1.1
 *
 * Features:
 * - Rule of Thirds analysis (30% weight)
 * - Headroom checking (25% weight)
 * - Edge distance validation (25% weight)
 * - Multi-subject support (20% weight)
 * - Weighted scoring system
 * - Professional Chinese suggestions (v2.1.1)
 * - Direction vectors for guideline rendering (v2.1.1)
 *
 * Modifications:
 * - 2025-11-27: Initial creation for v2.0 Clean Architecture refactor (Phase 1)
 * - 2025-11-27: Added accurate dimension calculation (Phase 2)
 * - 2025-11-27: Implemented complete composition rules with scoring (Phase 3)
 * - 2025-12-02: Add Chinese professional suggestions and direction vectors (v2.1.1)
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
        // v2.3.0: Added eye position weight, adjusted other weights
        private const val WEIGHT_EYE_POSITION = 0.25f      // 新增：眼睛位置
        private const val WEIGHT_RULE_OF_THIRDS = 0.25f    // 從 0.30 降為 0.25
        private const val WEIGHT_HEADROOM = 0.20f          // 從 0.25 降為 0.20
        private const val WEIGHT_EDGE_DISTANCE = 0.20f     // 從 0.25 降為 0.20
        private const val WEIGHT_SUBJECT_DETECTION = 0.10f // 從 0.20 降為 0.10

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
            face = primaryFace,
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
            val (message, dirX, dirY) = generateDetailedSuggestion(
                analysis, faceCenterX, faceCenterY, faceTop, faceLeft, faceRight, imageWidth, imageHeight
            )
            CompositionState.NeedsAdjustment(
                message = message,
                directionX = dirX,
                directionY = dirY
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
        faceCount: Int
    ): CompositionAnalysis {
        // Rule 0: Eye Position Analysis (v2.3.0) - 人像攝影最高優先級
        val eyeAnalysis = if (faceCount > 0) {
            EyePositionAnalysis.create(face, imageHeight)
        } else {
            null
        }

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

        // Rule 3: Edge Distance (25% weight)
        val edgeDistanceScore = analyzeEdgeDistanceScore(
            faceLeft, faceRight, faceTop, faceBottom, imageWidth, imageHeight
        )

        // Rule 4: Subject Detection (10% weight)
        val subjectDetectionScore = analyzeSubjectDetectionScore(faceCount)

        // Calculate weighted overall score (v2.3.0: includes eye position)
        val overallScore =
            eyePositionScore * WEIGHT_EYE_POSITION +
            ruleOfThirdsScore * WEIGHT_RULE_OF_THIRDS +
            headroomScore * WEIGHT_HEADROOM +
            edgeDistanceScore * WEIGHT_EDGE_DISTANCE +
            subjectDetectionScore * WEIGHT_SUBJECT_DETECTION

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
     * Generates detailed suggestion with professional Chinese text and direction vectors
     *
     * Returns: Triple(message, directionX, directionY)
     * - message: Professional Chinese composition suggestion
     * - directionX: -1.0 (left) to 1.0 (right), 0.0 = centered
     * - directionY: -1.0 (up) to 1.0 (down), 0.0 = centered
     *
     * v2.1.1: Changed to Chinese professional suggestions based on scene composition matrix
     */
    /**
     * 生成專業中文建議
     *
     * v2.1.1: 改為中文專業建議
     * v2.2.2: 移除方向箭頭，directionX/Y 保持為 0
     * v2.3.0: 優先級系統、眼睛位置引導、改進邊界訊息
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

        // 問題 1: 邊界警告（最高優先級）
        if (analysis.edgeDistanceScore < 0.7f) {
            val edgeMessage = generateEdgeBoundaryMessage(
                faceLeft, faceRight, faceTop, faceCenterY, imageWidth, imageHeight
            )
            issues.add(CompositionIssue(
                priority = CompositionIssuePriority.EDGE_BOUNDARY,
                score = analysis.edgeDistanceScore,
                message = edgeMessage
            ))
        }

        // 問題 2: 眼睛位置（高優先級）- v2.3.0
        analysis.eyeAnalysis?.let { eyeAnalysis ->
            if (eyeAnalysis.hasEyeLandmarks && eyeAnalysis.needsAdjustment) {
                val eyeMessage = when {
                    eyeAnalysis.isAboveIdeal -> "向下降低鏡頭，使眼睛對齊上三分線"
                    else -> "向上抬高鏡頭，使眼睛位於上三分線"
                }
                issues.add(CompositionIssue(
                    priority = CompositionIssuePriority.EYE_POSITION,
                    score = analysis.eyePositionScore,
                    message = eyeMessage
                ))
            }
        }

        // 問題 3: 頭部空間（中優先級）
        if (analysis.headroomScore < 0.7f) {
            val headroomRatio = faceTop / imageHeight
            val headroomMessage = when {
                headroomRatio < IDEAL_HEADROOM_MIN -> "頭部空間略少，可微微下移手機"
                headroomRatio > IDEAL_HEADROOM_MAX -> "頭部空間過多，可微微上提手機"
                else -> "調整頭部空間"
            }
            issues.add(CompositionIssue(
                priority = CompositionIssuePriority.HEADROOM,
                score = analysis.headroomScore,
                message = headroomMessage
            ))
        }

        // 問題 4: 三分法則（最低優先級）
        if (analysis.ruleOfThirdsScore < 0.7f) {
            val ruleOfThirdsMessage = generateRuleOfThirdsMessage(
                faceCenterX, faceCenterY, imageWidth, imageHeight
            )
            issues.add(CompositionIssue(
                priority = CompositionIssuePriority.RULE_OF_THIRDS,
                score = analysis.ruleOfThirdsScore,
                message = ruleOfThirdsMessage
            ))
        }

        // 返回最高優先級問題，或 fallback 訊息
        val message = issues.maxByOrNull { it.priority.priority }?.message ?: "調整構圖位置"

        // v2.2.2: 方向箭頭已移除，保持為 0
        return Triple(message, 0f, 0f)
    }

    /**
     * 生成邊界特定訊息
     * v2.3.0: 方向性邊界警告
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

        val minDist = minOf(leftDist, rightDist, topDist, bottomDist)

        return when (minDist) {
            leftDist -> "人物太靠左側，請將人物向右移動"
            rightDist -> "人物太靠右側，請將人物向左移動"
            topDist -> "人物太靠上方，請向下調整"
            bottomDist -> "人物太靠下方，請向上調整"
            else -> "主體太靠近邊緣，請調整位置"
        }
    }

    /**
     * 生成三分法則訊息
     * v2.3.0: 簡化方向引導
     */
    private fun generateRuleOfThirdsMessage(
        faceCenterX: Float,
        faceCenterY: Float,
        imageWidth: Float,
        imageHeight: Float
    ): String {
        val leftThird = imageWidth / 3f
        val rightThird = imageWidth * 2f / 3f
        val topThird = imageHeight / 3f
        val bottomThird = imageHeight * 2f / 3f

        val horizontalSuggestion = when {
            faceCenterX < leftThird -> "請右移"
            faceCenterX > rightThird -> "請左移"
            else -> null
        }

        val verticalSuggestion = when {
            faceCenterY < topThird -> "請下移"
            faceCenterY > bottomThird -> "請上移"
            else -> null
        }

        return when {
            horizontalSuggestion != null && verticalSuggestion != null ->
                "${verticalSuggestion}並${horizontalSuggestion}，對齊三分交點"
            horizontalSuggestion != null -> "${horizontalSuggestion}，將主體移至三分線"
            verticalSuggestion != null -> "${verticalSuggestion}，使主體位於三分線"
            else -> "將主體移至三分交點更吸睛"
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
                imageHeight: Float
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
                val avgEyeY = (leftEye.position.y + rightEye.position.y) / 2f

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
