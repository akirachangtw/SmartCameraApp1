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
class GazeRuleTest {

    private lateinit var rule: GazeRule

    @Before
    fun setUp() {
        rule = GazeRule()
    }

    private fun landmark(
        box: RectF = RectF(0.2f, 0.2f, 0.8f, 0.8f),
        eulerY: Float = 0f,
    ) = FaceLandmarkData(
        boundingBoxNormalized = box,
        leftEyeNormalized = null,
        rightEyeNormalized = null,
        headEulerAngleY = eulerY,
    )

    // ── Boundary ────────────────────────────────────────────────────────────

    @Test
    fun `no faces returns zero score and not aligned`() {
        val result = rule.evaluate(emptyList(), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Negative: ADR contract — pixel coords rejected ───────────────────────

    @Test
    fun `pixel coordinates out of range return zero score and not aligned`() {
        val face = landmark(box = RectF(0f, 0f, 1080f, 1920f), eulerY = 0f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `negative bounding box coordinate returns zero score and not aligned`() {
        val face = landmark(box = RectF(-0.1f, 0.2f, 0.5f, 0.8f), eulerY = 0f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `malformed bounding box top greater than bottom returns zero score and not aligned`() {
        val face = landmark(box = RectF(0.2f, 0.8f, 0.8f, 0.2f), eulerY = 0f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Perfect alignment ────────────────────────────────────────────────────

    @Test
    fun `yaw 0 degrees scores 100 and is aligned`() {
        val result = rule.evaluate(listOf(landmark(eulerY = 0f)), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `negative yaw 0 degrees also scores 100 — symmetry`() {
        val result = rule.evaluate(listOf(landmark(eulerY = -0f)), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Slight yaw — still aligned ───────────────────────────────────────────

    @Test
    fun `small yaw 5 degrees scores above 85 and is aligned`() {
        val result = rule.evaluate(listOf(landmark(eulerY = 5f)), 0f, 0f)
        assertTrue("Score should be >= 85 for 5° yaw, was ${result.score}", result.score >= 85f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `small negative yaw minus 5 degrees scores above 85 and is aligned`() {
        val result = rule.evaluate(listOf(landmark(eulerY = -5f)), 0f, 0f)
        assertTrue("Score should be >= 85 for -5° yaw, was ${result.score}", result.score >= 85f)
        assertTrue(result.isAligned)
    }

    // ── Heavy yaw — not aligned ───────────────────────────────────────────────

    @Test
    fun `yaw 45 degrees scores 0 and is not aligned`() {
        val result = rule.evaluate(listOf(landmark(eulerY = 45f)), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `yaw minus 45 degrees scores 0 and is not aligned`() {
        val result = rule.evaluate(listOf(landmark(eulerY = -45f)), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `yaw 90 degrees triggers fail-safe and is aligned`() {
        // abs(90°) > EXTREME_YAW_DEG(45°): subject intentionally side-on.
        // Rule returns isAligned=true so PriorityEngine skips it.
        val result = rule.evaluate(listOf(landmark(eulerY = 90f)), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Symmetry: positive and negative yaw equal magnitude yield same score ──

    @Test
    fun `positive and negative yaw of same magnitude yield identical score`() {
        val resultPos = rule.evaluate(listOf(landmark(eulerY = 20f)), 0f, 0f)
        val resultNeg = rule.evaluate(listOf(landmark(eulerY = -20f)), 0f, 0f)
        assertEquals(resultPos.score, resultNeg.score, 0.001f)
        assertEquals(resultPos.isAligned, resultNeg.isAligned)
    }

    // ── Score bounds ─────────────────────────────────────────────────────────

    @Test
    fun `score is always within 0 to 100`() {
        // 180° > EXTREME_YAW_DEG(45°): fail-safe returns score=100f (still within bounds)
        val result = rule.evaluate(listOf(landmark(eulerY = 180f)), 0f, 0f)
        assertTrue(result.score >= 0f)
        assertTrue(result.score <= 100f)
    }

    // ── Extreme yaw fail-safe ─────────────────────────────────────────────────

    @Test
    fun `extreme yaw 60 degrees triggers fail-safe and is aligned`() {
        // Mirrors LandmarkProbeTest D6_PROBE data (portrait_extreme_side_60deg.jpg EulerY≈59.9°)
        val result = rule.evaluate(listOf(landmark(eulerY = 60f)), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    @Test
    fun `extreme negative yaw minus 60 degrees triggers fail-safe and is aligned`() {
        val result = rule.evaluate(listOf(landmark(eulerY = -60f)), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Multi-face: largest face is picked ───────────────────────────────────

    @Test
    fun `largest face is picked when multiple faces present`() {
        // Large face looking forward (yaw=0) → aligned
        val largeFace = landmark(box = RectF(0.1f, 0.1f, 0.9f, 0.9f), eulerY = 0f)
        // Small face looking away (yaw=45) → not aligned if picked
        val smallFace = landmark(box = RectF(0.4f, 0.4f, 0.5f, 0.5f), eulerY = 45f)
        val result = rule.evaluate(listOf(smallFace, largeFace), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── IMU independence ──────────────────────────────────────────────────────

    @Test
    fun `imu parameters do not affect rule output`() {
        val face = landmark(eulerY = 0f)
        val result0 = rule.evaluate(listOf(face), imuPitch = 0f, imuRoll = 0f)
        val result45 = rule.evaluate(listOf(face), imuPitch = 45f, imuRoll = 30f)
        assertEquals(result0.score, result45.score, 0.001f)
        assertEquals(result0.isAligned, result45.isAligned)
    }
}
