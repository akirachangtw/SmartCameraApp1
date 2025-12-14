/**
 * ClassName: Histogram
 * Description: Data model for image luminance histogram
 * Author: Claude Code
 * Date: 2025-12-02
 * Version: 2.2.0
 *
 * Features:
 * - 256-bin luminance histogram (0-255)
 * - Statistical analysis helpers
 * - Memory-efficient representation
 */
package com.akirachang.ai.photographer.domain.model

/**
 * Represents a luminance histogram for image analysis
 *
 * @property data Array of 256 bins (indices 0-255)
 * @property totalPixels Total number of sampled pixels
 */
data class Histogram(
    val data: IntArray,
    val totalPixels: Int
) {
    init {
        require(data.size == 256) { "Histogram must have exactly 256 bins" }
        require(totalPixels > 0) { "Total pixels must be positive" }
    }

    /**
     * Calculate average luminance value (0-255)
     */
    val avgLuminance: Float by lazy {
        data.indices.sumOf { index ->
            index * data[index]
        }.toFloat() / totalPixels
    }

    /**
     * Calculate highlight ratio (pixels in range 240-255)
     */
    val highlightRatio: Float by lazy {
        data.sliceArray(240..255).sum().toFloat() / totalPixels
    }

    /**
     * Calculate shadow ratio (pixels in range 0-30)
     */
    val shadowRatio: Float by lazy {
        data.sliceArray(0..30).sum().toFloat() / totalPixels
    }

    /**
     * Calculate midtone ratio (pixels in range 85-170)
     */
    val midtoneRatio: Float by lazy {
        data.sliceArray(85..170).sum().toFloat() / totalPixels
    }

    /**
     * Check if histogram is properly normalized
     */
    fun isValid(): Boolean {
        val sum = data.sum()
        return sum == totalPixels && totalPixels > 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Histogram

        if (!data.contentEquals(other.data)) return false
        if (totalPixels != other.totalPixels) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + totalPixels
        return result
    }
}
