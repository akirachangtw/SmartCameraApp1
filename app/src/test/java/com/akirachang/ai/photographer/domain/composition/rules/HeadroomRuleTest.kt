package com.akirachang.ai.photographer.domain.composition.rules

import android.graphics.RectF
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
class HeadroomRuleTest {

    private lateinit var rule: HeadroomRule

    @Before
    fun setUp() {
        rule = HeadroomRule()
    }

    // ── Boundary ────────────────────────────────────────────────────────────

    @Test
    fun `no faces returns zero score and not aligned`() {
        val result = rule.evaluate(emptyList(), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Perfect alignment ────────────────────────────────────────────────────

    @Test
    fun `ratio 0_15 in ideal range scores 100 and is aligned`() {
        // ratio = yDistance / faceHeight = 0.15
        // faceHeight = 0.2, yDistance = face.top = 0.03 → ratio = 0.03/0.2 = 0.15
        val faceHeight = 0.2f
        val yDistance = 0.15f * faceHeight  // 0.03
        val face = RectF(0.3f, yDistance, 0.7f, yDistance + faceHeight)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `ratio at lower boundary 0_1 scores 100 and is aligned`() {
        val faceHeight = 0.4f
        val yDistance = 0.1f * faceHeight  // ratio = 0.1
        val face = RectF(0.2f, yDistance, 0.8f, yDistance + faceHeight)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `ratio at upper boundary 0_2 scores 100 and is aligned`() {
        // Construct directly: top=0.1f, height=0.5f → ratio = 0.1/0.5 = 0.2 exactly
        val top = 0.1f
        val faceHeight = 0.5f
        val face = RectF(0.2f, top, 0.8f, top + faceHeight)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Out of range — not aligned ───────────────────────────────────────────

    @Test
    fun `ratio 0_05 too close to top scores below 100 and is not aligned`() {
        // ratio = 0.05 < IDEAL_MIN(0.1)
        val faceHeight = 0.4f
        val yDistance = 0.05f * faceHeight
        val face = RectF(0.2f, yDistance, 0.8f, yDistance + faceHeight)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertTrue("Score should be < 100, was ${result.score}", result.score < 100f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `ratio 0_50 too far from top scores below 100 and is not aligned`() {
        // ratio = 0.5 > IDEAL_MAX(0.2)
        val faceHeight = 0.2f
        val yDistance = 0.5f * faceHeight
        val face = RectF(0.2f, yDistance, 0.8f, yDistance + faceHeight)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertTrue("Score should be < 100, was ${result.score}", result.score < 100f)
        assertFalse(result.isAligned)
    }

    // ── Score bounds ─────────────────────────────────────────────────────────

    @Test
    fun `score is always within 0 to 100`() {
        val face = RectF(0.1f, 0.0f, 0.9f, 0.3f) // ratio = 0 / 0.3 = 0
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertTrue(result.score >= 0f)
        assertTrue(result.score <= 100f)
    }

    // ── Multi-face: largest face is picked ───────────────────────────────────

    @Test
    fun `largest face is picked when multiple faces present`() {
        // Large face with ideal ratio → aligned
        val faceHeight = 0.4f
        val yDistance = 0.15f * faceHeight
        val largeFace = RectF(0.1f, yDistance, 0.9f, yDistance + faceHeight)
        // Small face with bad ratio
        val smallFace = RectF(0.4f, 0.0f, 0.6f, 0.05f)
        val result = rule.evaluate(listOf(smallFace, largeFace), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── IMU independence ─────────────────────────────────────────────────────

    @Test
    fun `imu parameters do not affect rule output`() {
        val faceHeight = 0.2f
        val yDistance = 0.15f * faceHeight
        val face = RectF(0.3f, yDistance, 0.7f, yDistance + faceHeight)
        val result0 = rule.evaluate(listOf(face), imuPitch = 0f, imuRoll = 0f)
        val result45 = rule.evaluate(listOf(face), imuPitch = 45f, imuRoll = 30f)
        assertEquals(result0.score, result45.score, 0.001f)
        assertEquals(result0.isAligned, result45.isAligned)
    }

    // ── Negative: ADR contract enforcement ───────────────────────────────────

    @Test
    fun `pixel coordinates out of range return zero score and not aligned`() {
        // Simulates caller accidentally passing raw pixel coordinates (top=100 >> 1.0)
        val face = RectF(0f, 100f, 1080f, 1920f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `negative coordinate returns zero score and not aligned`() {
        val face = RectF(-0.1f, 0.0f, 0.5f, 0.3f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `malformed rect with top greater than bottom returns zero score and not aligned`() {
        val face = RectF(0.2f, 0.6f, 0.8f, 0.3f) // top > bottom
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }
}
