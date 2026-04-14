package com.akirachang.ai.photographer.presentation.utils

/**
 * Lightweight normalized point in domain space [0, 1].
 */
data class NormalizedPoint(val x: Float, val y: Float)

/**
 * Lightweight normalized rect in domain space [0, 1].
 */
data class NormalizedRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

/**
 * Pixel-space point after coordinate transformation.
 */
data class PixelPoint(val x: Float, val y: Float)

/**
 * Pixel-space rect after coordinate transformation.
 */
data class PixelRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

/**
 * Transforms normalized domain coordinates [0, 1] to physical canvas pixel coordinates.
 *
 * Single responsibility: pure coordinate multiplication and boundary mapping.
 * No Compose UI state, no Android SDK dependency, no business logic.
 *
 * Values outside [0, 1] are mapped to coordinates beyond the screen boundary —
 * this is intentional; the caller's Canvas clip region handles visibility.
 *
 * @param screenWidth  Physical canvas width in pixels.
 * @param screenHeight Physical canvas height in pixels.
 */
class NormalizedToPixelTransformer(
    val screenWidth: Float,
    val screenHeight: Float
) {

    /**
     * Maps a normalized x coordinate to a pixel x coordinate.
     */
    fun transformX(normalizedX: Float): Float = normalizedX * screenWidth

    /**
     * Maps a normalized y coordinate to a pixel y coordinate.
     */
    fun transformY(normalizedY: Float): Float = normalizedY * screenHeight

    /**
     * Maps a normalized point to a pixel-space point.
     */
    fun transformPoint(normalizedPoint: NormalizedPoint): PixelPoint =
        PixelPoint(transformX(normalizedPoint.x), transformY(normalizedPoint.y))

    /**
     * Maps a normalized rect to a pixel-space rect.
     */
    fun transformRect(normalizedRect: NormalizedRect): PixelRect = PixelRect(
        left = transformX(normalizedRect.left),
        top = transformY(normalizedRect.top),
        right = transformX(normalizedRect.right),
        bottom = transformY(normalizedRect.bottom)
    )
}
