package com.akirachang.ai.photographer.domain.composition.rules

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
class ThirdsRuleTest {

    private lateinit var rule: ThirdsRule

    @Before
    fun setUp() {
        rule = ThirdsRule()
    }

    /** Wraps a normalized [RectF] into a [FaceLandmarkData] for test convenience. */
    private fun RectF.asLandmark() = FaceLandmarkData(
        boundingBoxNormalized = this,
        leftEyeNormalized = null,
        rightEyeNormalized = null,
        headEulerAngleY = 0f,
    )

    // ── Boundary ────────────────────────────────────────────────────────────

    @Test
    fun `no faces returns zero score and not aligned`() {
        val result = rule.evaluate(emptyList(), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Perfect alignment ────────────────────────────────────────────────────

    @Test
    fun `face center at top-left intersection scores 100 and is aligned`() {
        // Build RectF so center is exactly 1/3, 1/3
        val cx = 1f / 3f
        val cy = 1f / 3f
        val half = 0.05f
        val face = RectF(cx - half, cy - half, cx + half, cy + half)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `face center at top-right intersection scores 100 and is aligned`() {
        val cx = 2f / 3f
        val cy = 1f / 3f
        val half = 0.05f
        val face = RectF(cx - half, cy - half, cx + half, cy + half)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `face center at bottom-left intersection scores 100 and is aligned`() {
        val cx = 1f / 3f
        val cy = 2f / 3f
        val half = 0.05f
        val face = RectF(cx - half, cy - half, cx + half, cy + half)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `face center at bottom-right intersection scores 100 and is aligned`() {
        val cx = 2f / 3f
        val cy = 2f / 3f
        val half = 0.05f
        val face = RectF(cx - half, cy - half, cx + half, cy + half)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Slight offset — still aligned ────────────────────────────────────────

    @Test
    fun `face center slightly offset from intersection scores 85-99 and is aligned`() {
        // Offset ~0.04 from (0.333, 0.333) — well within MAX_TOLERANCE
        val offsetX = 1f / 3f + 0.04f
        val offsetY = 1f / 3f
        val half = 0.05f
        val face = RectF(offsetX - half, offsetY - half, offsetX + half, offsetY + half)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertTrue("Score should be >= 85, was ${result.score}", result.score >= 85f)
        assertTrue("Score should be < 100, was ${result.score}", result.score < 100f)
        assertTrue(result.isAligned)
    }

    // ── Far away — not aligned ───────────────────────────────────────────────

    @Test
    fun `face center at image center scores below 85 and is not aligned`() {
        // Dead center (0.5, 0.5) — nearest thirds point is ~0.236 away
        val half = 0.05f
        val face = RectF(0.5f - half, 0.5f - half, 0.5f + half, 0.5f + half)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertTrue("Score should be < 85 for dead-center, was ${result.score}", result.score < 85f)
        assertFalse(result.isAligned)
    }

    // ── Invariant: grid must NOT drift with face position ────────────────────

    @Test
    fun `score depends on center position not on face size - small face aligned`() {
        // Small face, center exactly at top-left thirds point
        val cx = 1f / 3f
        val cy = 1f / 3f
        val half = 0.01f
        val smallFace = RectF(cx - half, cy - half, cx + half, cy + half)
        val result = rule.evaluate(listOf(smallFace.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
    }

    @Test
    fun `score depends on center position not on face size - large face aligned`() {
        // Large face, center exactly at top-left thirds point
        val cx = 1f / 3f
        val cy = 1f / 3f
        val half = 0.25f
        val largeFace = RectF(cx - half, cy - half, cx + half, cy + half)
        val result = rule.evaluate(listOf(largeFace.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
    }

    @Test
    fun `grid does not drift - aligned face scores same with or without a smaller decoy`() {
        // Primary (larger) face aligned at (1/3, 1/3)
        val cx = 1f / 3f
        val cy = 1f / 3f
        val bigHalf = 0.15f
        val alignedFace = RectF(cx - bigHalf, cy - bigHalf, cx + bigHalf, cy + bigHalf)

        // Alone
        val resultAlone = rule.evaluate(listOf(alignedFace.asLandmark()), 0f, 0f)

        // With a smaller decoy at center (area < alignedFace so rule still picks alignedFace)
        val decoyHalf = 0.05f
        val decoyFace = RectF(0.5f - decoyHalf, 0.5f - decoyHalf, 0.5f + decoyHalf, 0.5f + decoyHalf)
        val resultWithDecoy = rule.evaluate(listOf(alignedFace.asLandmark(), decoyFace.asLandmark()), 0f, 0f)

        assertEquals(100f, resultAlone.score, 0.001f)
        assertEquals(100f, resultWithDecoy.score, 0.001f)
    }

    // ── Multi-face: largest face is picked ───────────────────────────────────

    @Test
    fun `largest face is picked when multiple faces present`() {
        // Small face at center (not aligned), large face at intersection (aligned)
        val smallFace = RectF(0.45f, 0.45f, 0.55f, 0.55f)
        val largeFace = RectF(
            1f / 3f - 0.1f, 1f / 3f - 0.1f,
            1f / 3f + 0.1f, 1f / 3f + 0.1f
        )
        val result = rule.evaluate(listOf(smallFace.asLandmark(), largeFace.asLandmark()), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Boundary defense: invalid coordinates rejected silently ─────────────

    @Test
    fun `pixel coordinates out of range return zero score and not aligned`() {
        // Simulates caller accidentally passing raw pixel coordinates
        val face = RectF(0f, 0f, 1080f, 1920f)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `negative coordinate returns zero score and not aligned`() {
        val face = RectF(-0.1f, 0.2f, 0.5f, 0.5f)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `malformed rect with left greater than right returns zero score and not aligned`() {
        val face = RectF(0.7f, 0.2f, 0.3f, 0.5f) // left > right
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Invariant: same center → same score regardless of face size ──────────

    @Test
    fun `same center point yields identical score for different face sizes`() {
        val cx = 1f / 3f
        val cy = 1f / 3f
        val smallFace = RectF(cx - 0.01f, cy - 0.01f, cx + 0.01f, cy + 0.01f)
        val largeFace = RectF(cx - 0.25f, cy - 0.25f, cx + 0.25f, cy + 0.25f)
        val resultSmall = rule.evaluate(listOf(smallFace.asLandmark()), 0f, 0f)
        val resultLarge = rule.evaluate(listOf(largeFace.asLandmark()), 0f, 0f)
        assertEquals(resultSmall.score, resultLarge.score, 0.001f)
        assertEquals(resultSmall.isAligned, resultLarge.isAligned)
    }

    // ── IMU independence ─────────────────────────────────────────────────────

    @Test
    fun `imu parameters do not affect rule output`() {
        val half = 0.05f
        val face = RectF(1f / 3f - half, 1f / 3f - half, 1f / 3f + half, 1f / 3f + half)
        val result0 = rule.evaluate(listOf(face.asLandmark()), imuPitch = 0f, imuRoll = 0f)
        val result45 = rule.evaluate(listOf(face.asLandmark()), imuPitch = 45f, imuRoll = 30f)
        assertEquals(result0.score, result45.score, 0.001f)
        assertEquals(result0.isAligned, result45.isAligned)
    }

    // ── Score bounds ─────────────────────────────────────────────────────────

    @Test
    fun `score is always within 0 to 100`() {
        val face = RectF(0f, 0f, 0.1f, 0.1f)
        val result = rule.evaluate(listOf(face.asLandmark()), 0f, 0f)
        assertTrue(result.score >= 0f)
        assertTrue(result.score <= 100f)
    }
}
