package com.akirachang.ai.photographer.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NormalizedToPixelTransformerTest {

    private lateinit var transformer: NormalizedToPixelTransformer

    companion object {
        private const val SCREEN_WIDTH = 1080f
        private const val SCREEN_HEIGHT = 1920f
        private const val DELTA = 0.001f
    }

    @Before
    fun setUp() {
        transformer = NormalizedToPixelTransformer(SCREEN_WIDTH, SCREEN_HEIGHT)
    }

    // --- transformX ---

    @Test
    fun transformX_zero_mapsToZero() {
        assertEquals(0f, transformer.transformX(0f), DELTA)
    }

    @Test
    fun transformX_half_mapsToCenterX() {
        assertEquals(540f, transformer.transformX(0.5f), DELTA)
    }

    @Test
    fun transformX_one_mapsToFullWidth() {
        assertEquals(1080f, transformer.transformX(1f), DELTA)
    }

    @Test
    fun transformX_negative_mapsToNegativePixel() {
        assertEquals(-108f, transformer.transformX(-0.1f), DELTA)
    }

    @Test
    fun transformX_greaterThanOne_mapsToOffScreenPixel() {
        assertEquals(1188f, transformer.transformX(1.1f), DELTA)
    }

    // --- transformY ---

    @Test
    fun transformY_zero_mapsToZero() {
        assertEquals(0f, transformer.transformY(0f), DELTA)
    }

    @Test
    fun transformY_half_mapsToCenterY() {
        assertEquals(960f, transformer.transformY(0.5f), DELTA)
    }

    @Test
    fun transformY_one_mapsToFullHeight() {
        assertEquals(1920f, transformer.transformY(1f), DELTA)
    }

    @Test
    fun transformY_negative_mapsToNegativePixel() {
        assertEquals(-192f, transformer.transformY(-0.1f), DELTA)
    }

    @Test
    fun transformY_greaterThanOne_mapsToOffScreenPixel() {
        assertEquals(2112f, transformer.transformY(1.1f), DELTA)
    }

    // --- transformPoint ---

    @Test
    fun transformPoint_center_mapsToScreenCenter() {
        val result = transformer.transformPoint(NormalizedPoint(0.5f, 0.5f))
        assertEquals(540f, result.x, DELTA)
        assertEquals(960f, result.y, DELTA)
    }

    @Test
    fun transformPoint_origin_mapsToOrigin() {
        val result = transformer.transformPoint(NormalizedPoint(0f, 0f))
        assertEquals(0f, result.x, DELTA)
        assertEquals(0f, result.y, DELTA)
    }

    @Test
    fun transformPoint_fullCorner_mapsToBottomRight() {
        val result = transformer.transformPoint(NormalizedPoint(1f, 1f))
        assertEquals(1080f, result.x, DELTA)
        assertEquals(1920f, result.y, DELTA)
    }

    // --- transformRect ---

    @Test
    fun transformRect_normalizedFullScreen_mapsToFullScreenRect() {
        val result = transformer.transformRect(NormalizedRect(0f, 0f, 1f, 1f))
        assertEquals(0f, result.left, DELTA)
        assertEquals(0f, result.top, DELTA)
        assertEquals(1080f, result.right, DELTA)
        assertEquals(1920f, result.bottom, DELTA)
    }

    @Test
    fun transformRect_centerQuarter_mapsToPixelRect() {
        val result = transformer.transformRect(NormalizedRect(0.25f, 0.25f, 0.75f, 0.75f))
        assertEquals(270f, result.left, DELTA)
        assertEquals(480f, result.top, DELTA)
        assertEquals(810f, result.right, DELTA)
        assertEquals(1440f, result.bottom, DELTA)
    }

    @Test
    fun transformRect_offScreenRect_doesNotCrash() {
        val result = transformer.transformRect(NormalizedRect(-0.1f, -0.1f, 1.1f, 1.1f))
        assertEquals(-108f, result.left, DELTA)
        assertEquals(-192f, result.top, DELTA)
        assertEquals(1188f, result.right, DELTA)
        assertEquals(2112f, result.bottom, DELTA)
    }
}
