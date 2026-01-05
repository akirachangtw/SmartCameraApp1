package com.akirachang.ai.photographer.domain.model

import androidx.compose.ui.geometry.Offset

/**
 * Guideline Analysis Result
 *
 * v2.7.0 Phase 2 Sprint 3
 *
 * Contains detected composition guideline lines from OpenCV Hough Line Transform.
 * These lines can be used to assist photographers with composition techniques like:
 * - Leading lines (roads, rails, buildings)
 * - Symmetry detection (horizontal/vertical axes)
 * - Diagonal composition
 *
 * Coordinates are in normalized space [0.0, 1.0] for both x and y,
 * allowing easy scaling to any view size.
 */
data class GuidelineAnalysisResult(
    /**
     * Detected guideline lines in normalized coordinates
     *
     * Each Line represents a detected edge/line in the frame.
     * Coordinates are normalized to [0.0, 1.0] range.
     */
    val lines: List<Line> = emptyList(),

    /**
     * Processing time in milliseconds
     *
     * Used for performance monitoring and optimization.
     */
    val processingTimeMs: Long = 0,

    /**
     * Frame dimensions (width x height)
     *
     * Original dimensions of the analyzed frame, used for debugging.
     */
    val frameDimensions: Pair<Int, Int> = Pair(0, 0)
) {
    /**
     * Represents a detected line segment
     *
     * @param start Start point in normalized coordinates [0.0, 1.0]
     * @param end End point in normalized coordinates [0.0, 1.0]
     * @param strength Line strength/confidence (0.0 - 1.0), higher is stronger
     * @param type Line type classification
     */
    data class Line(
        val start: Offset,
        val end: Offset,
        val strength: Float = 1.0f,
        val type: LineType = LineType.GENERAL
    ) {
        /**
         * Calculate line angle in degrees (0-180)
         *
         * 0° = horizontal right
         * 90° = vertical up
         * 180° = horizontal left
         */
        val angle: Float
            get() {
                val dx = end.x - start.x
                val dy = end.y - start.y
                val radians = kotlin.math.atan2(dy.toDouble(), dx.toDouble())
                val degrees = Math.toDegrees(radians).toFloat()
                return (degrees + 360f) % 180f  // Normalize to 0-180
            }

        /**
         * Check if line is approximately horizontal
         * (angle within ±15° of 0° or 180°)
         */
        val isHorizontal: Boolean
            get() = angle < 15f || angle > 165f

        /**
         * Check if line is approximately vertical
         * (angle within ±15° of 90°)
         */
        val isVertical: Boolean
            get() = angle in 75f..105f

        /**
         * Check if line is diagonal
         * (not horizontal or vertical)
         */
        val isDiagonal: Boolean
            get() = !isHorizontal && !isVertical
    }

    /**
     * Line type classification
     *
     * Used to categorize detected lines for different composition techniques.
     */
    enum class LineType {
        /**
         * General line (default)
         */
        GENERAL,

        /**
         * Leading line (roads, rails, perspective lines)
         * - Typically diagonal, converging towards vanishing point
         */
        LEADING,

        /**
         * Symmetry axis (vertical or horizontal centerline)
         * - Used for symmetry composition detection
         */
        SYMMETRY,

        /**
         * Horizon line (landscape photography)
         * - Strong horizontal line, typically in upper or lower third
         */
        HORIZON,

        /**
         * Architectural line (buildings, structures)
         * - Strong vertical or diagonal lines from man-made structures
         */
        ARCHITECTURAL
    }

    /**
     * Filter lines by minimum strength threshold
     *
     * @param minStrength Minimum strength (0.0 - 1.0), default 0.5
     * @return Filtered result with only strong lines
     */
    fun filterByStrength(minStrength: Float = 0.5f): GuidelineAnalysisResult {
        return copy(lines = lines.filter { it.strength >= minStrength })
    }

    /**
     * Get top N strongest lines
     *
     * @param count Maximum number of lines to return
     * @return Filtered result with top N lines sorted by strength
     */
    fun topLines(count: Int): GuidelineAnalysisResult {
        return copy(lines = lines.sortedByDescending { it.strength }.take(count))
    }

    /**
     * Filter lines by type
     *
     * @param type Line type to filter by
     * @return Filtered result with only lines of specified type
     */
    fun filterByType(type: LineType): GuidelineAnalysisResult {
        return copy(lines = lines.filter { it.type == type })
    }

    /**
     * Get horizontal lines only
     */
    fun horizontalLines(): GuidelineAnalysisResult {
        return copy(lines = lines.filter { it.isHorizontal })
    }

    /**
     * Get vertical lines only
     */
    fun verticalLines(): GuidelineAnalysisResult {
        return copy(lines = lines.filter { it.isVertical })
    }

    /**
     * Get diagonal lines only
     */
    fun diagonalLines(): GuidelineAnalysisResult {
        return copy(lines = lines.filter { it.isDiagonal })
    }

    companion object {
        /**
         * Empty result (no lines detected)
         */
        val EMPTY = GuidelineAnalysisResult()

        /**
         * Create result with denormalized line coordinates
         *
         * @param lines Lines in pixel coordinates
         * @param frameWidth Frame width in pixels
         * @param frameHeight Frame height in pixels
         * @param processingTimeMs Processing time
         * @return Result with normalized coordinates
         */
        fun fromPixelCoordinates(
            lines: List<Pair<Pair<Float, Float>, Pair<Float, Float>>>,
            frameWidth: Int,
            frameHeight: Int,
            processingTimeMs: Long = 0
        ): GuidelineAnalysisResult {
            val normalizedLines = lines.map { (start, end) ->
                Line(
                    start = Offset(
                        x = start.first / frameWidth,
                        y = start.second / frameHeight
                    ),
                    end = Offset(
                        x = end.first / frameWidth,
                        y = end.second / frameHeight
                    ),
                    strength = 1.0f  // Will be calculated by analyzer
                )
            }

            return GuidelineAnalysisResult(
                lines = normalizedLines,
                processingTimeMs = processingTimeMs,
                frameDimensions = Pair(frameWidth, frameHeight)
            )
        }
    }
}
