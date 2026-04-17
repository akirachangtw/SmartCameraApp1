package com.akirachang.ai.photographer.domain.composition

import android.graphics.RectF
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PtfRuleAggregatorTest {

    private lateinit var aggregator: PtfRuleAggregator

    @Before
    fun setUp() {
        aggregator = PtfRuleAggregator()
    }

    private fun centeredFace(cx: Float = 0.5f, cy: Float = 0.5f, halfSize: Float = 0.1f) =
        FaceLandmarkData(
            boundingBoxNormalized = RectF(cx - halfSize, cy - halfSize, cx + halfSize, cy + halfSize),
            leftEyeNormalized = null,
            rightEyeNormalized = null,
            headEulerAngleY = 0f,
        )

    // ── AND-Gate: all pass ────────────────────────────────────────────────────

    @Test
    fun `all three rules pass - isAligned true and failedRules empty`() {
        val result = aggregator.aggregate(
            face = centeredFace(),
            devicePitch = 0f,
            deviceRoll = 0f,
        )
        assertTrue(result.isAligned)
        assertTrue(result.failedRules.isEmpty())
        assertEquals(PtfOffset.ZERO, result.offset)
    }

    // ── AND-Gate: null face ───────────────────────────────────────────────────

    @Test
    fun `null face returns NO_FACE sentinel`() {
        val result = aggregator.aggregate(face = null, devicePitch = 0f, deviceRoll = 0f)
        assertEquals(PtfAlignmentResult.NO_FACE, result)
        assertFalse(result.isAligned)
        assertTrue(result.failedRules.isEmpty())
        assertEquals(PtfOffset.ZERO, result.offset)
    }

    // ── AND-Gate: single rule fails ───────────────────────────────────────────

    @Test
    fun `framing fails alone - isAligned false and failedRules contains FRAMING`() {
        // face far from center: distance ≈ 0.283 > 0.12
        val result = aggregator.aggregate(
            face = centeredFace(cx = 0.7f, cy = 0.7f),
            devicePitch = 0f,
            deviceRoll = 0f,
        )
        assertFalse(result.isAligned)
        assertTrue(PtfRuleName.FRAMING in result.failedRules)
        assertFalse(PtfRuleName.ANGLE in result.failedRules)
        assertFalse(PtfRuleName.LEVEL in result.failedRules)
    }

    @Test
    fun `angle fails alone - isAligned false and failedRules contains ANGLE`() {
        val result = aggregator.aggregate(
            face = centeredFace(),
            devicePitch = -15.1f,
            deviceRoll = 0f,
        )
        assertFalse(result.isAligned)
        assertTrue(PtfRuleName.ANGLE in result.failedRules)
        assertFalse(PtfRuleName.FRAMING in result.failedRules)
        assertFalse(PtfRuleName.LEVEL in result.failedRules)
    }

    @Test
    fun `level fails alone - isAligned false and failedRules contains LEVEL`() {
        val result = aggregator.aggregate(
            face = centeredFace(),
            devicePitch = 0f,
            deviceRoll = 8f,
        )
        assertFalse(result.isAligned)
        assertTrue(PtfRuleName.LEVEL in result.failedRules)
        assertFalse(PtfRuleName.FRAMING in result.failedRules)
        assertFalse(PtfRuleName.ANGLE in result.failedRules)
    }

    // ── AND-Gate: multiple rules fail ─────────────────────────────────────────

    @Test
    fun `all three rules fail - failedRules contains all three names`() {
        val result = aggregator.aggregate(
            face = centeredFace(cx = 0.9f, cy = 0.9f),
            devicePitch = 30f,
            deviceRoll = 45f,
        )
        assertFalse(result.isAligned)
        assertEquals(3, result.failedRules.size)
        assertTrue(PtfRuleName.FRAMING in result.failedRules)
        assertTrue(PtfRuleName.ANGLE in result.failedRules)
        assertTrue(PtfRuleName.LEVEL in result.failedRules)
    }

    // ── Offset direction ─────────────────────────────────────────────────────

    @Test
    fun `face left of center - offset dx is positive (push right)`() {
        // face center at (0.3, 0.5): dx = 0.5 - 0.3 = 0.2 > 0
        val result = aggregator.aggregate(
            face = centeredFace(cx = 0.3f, cy = 0.5f),
            devicePitch = 0f,
            deviceRoll = 0f,
        )
        assertFalse(result.isAligned)
        assertTrue(result.offset.dx > 0f)
    }

    @Test
    fun `aligned result has zero offset`() {
        val result = aggregator.aggregate(
            face = centeredFace(),
            devicePitch = 0f,
            deviceRoll = 0f,
        )
        assertTrue(result.isAligned)
        assertEquals(0f, result.offset.dx, 0.0001f)
        assertEquals(0f, result.offset.dy, 0.0001f)
    }

    // ── Boundary: pitch asymmetric range ─────────────────────────────────────

    @Test
    fun `pitch at lower boundary -15 passes angle rule`() {
        val result = aggregator.aggregate(
            face = centeredFace(),
            devicePitch = -15f,
            deviceRoll = 0f,
        )
        assertTrue(result.isAligned)
    }

    @Test
    fun `pitch just outside lower boundary -15_1 fails angle rule`() {
        val result = aggregator.aggregate(
            face = centeredFace(),
            devicePitch = -15.1f,
            deviceRoll = 0f,
        )
        assertFalse(result.isAligned)
        assertTrue(PtfRuleName.ANGLE in result.failedRules)
    }
}
