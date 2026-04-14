package com.akirachang.ai.photographer.domain.composition.rules

import android.graphics.PointF
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
class EyeLineRuleTest {

    private lateinit var rule: EyeLineRule

    @Before
    fun setUp() {
        rule = EyeLineRule()
    }

    /**
     * Build a [FaceLandmarkData] with both eye landmarks at the same Y (perfectly horizontal).
     * eyeY is the shared normalized Y position; leftEyeX < rightEyeX.
     */
    private fun landmarkWithHorizontalEyes(
        box: RectF = RectF(0.2f, 0.2f, 0.8f, 0.8f),
        eyeY: Float = 0.4f,
        leftEyeX: Float = 0.35f,
        rightEyeX: Float = 0.65f,
    ) = FaceLandmarkData(
        boundingBoxNormalized = box,
        leftEyeNormalized = PointF(leftEyeX, eyeY),
        rightEyeNormalized = PointF(rightEyeX, eyeY),
        headEulerAngleY = 0f,
    )

    /**
     * Build a [FaceLandmarkData] with eye landmarks tilted by [tiltDeg] degrees clockwise.
     * Uses a symmetric eye pair centred on the face box.
     */
    private fun landmarkWithTiltedEyes(
        box: RectF = RectF(0.2f, 0.2f, 0.8f, 0.8f),
        tiltDeg: Float,
    ): FaceLandmarkData {
        val cx = box.centerX()
        val cy = box.centerY()
        val halfSpan = 0.12f
        val tiltRad = Math.toRadians(tiltDeg.toDouble()).toFloat()
        val leftEye = PointF(cx - halfSpan * Math.cos(tiltRad.toDouble()).toFloat(),
                             cy - halfSpan * Math.sin(tiltRad.toDouble()).toFloat())
        val rightEye = PointF(cx + halfSpan * Math.cos(tiltRad.toDouble()).toFloat(),
                              cy + halfSpan * Math.sin(tiltRad.toDouble()).toFloat())
        return FaceLandmarkData(
            boundingBoxNormalized = box,
            leftEyeNormalized = leftEye,
            rightEyeNormalized = rightEye,
            headEulerAngleY = 0f,
        )
    }

    private fun landmarkNoEyes(box: RectF = RectF(0.2f, 0.2f, 0.8f, 0.8f)) =
        FaceLandmarkData(
            boundingBoxNormalized = box,
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

    // ── Negative: ADR contract — pixel coords rejected ───────────────────────

    @Test
    fun `pixel coordinates out of range return zero score and not aligned`() {
        val face = FaceLandmarkData(
            boundingBoxNormalized = RectF(0f, 0f, 1080f, 1920f),
            leftEyeNormalized = PointF(300f, 400f),
            rightEyeNormalized = PointF(700f, 400f),
            headEulerAngleY = 0f,
        )
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `negative bounding box coordinate returns zero score and not aligned`() {
        val face = FaceLandmarkData(
            boundingBoxNormalized = RectF(-0.1f, 0.2f, 0.5f, 0.8f),
            leftEyeNormalized = PointF(0.3f, 0.4f),
            rightEyeNormalized = PointF(0.5f, 0.4f),
            headEulerAngleY = 0f,
        )
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `malformed bounding box left greater than right returns zero score and not aligned`() {
        val face = FaceLandmarkData(
            boundingBoxNormalized = RectF(0.8f, 0.2f, 0.3f, 0.7f),
            leftEyeNormalized = PointF(0.4f, 0.4f),
            rightEyeNormalized = PointF(0.6f, 0.4f),
            headEulerAngleY = 0f,
        )
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── No landmarks fallback ────────────────────────────────────────────────

    @Test
    fun `no eye landmarks returns fallback score 30 and not aligned`() {
        val face = landmarkNoEyes()
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(30f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `only left eye available returns fallback score 30 and not aligned`() {
        val face = FaceLandmarkData(
            boundingBoxNormalized = RectF(0.2f, 0.2f, 0.8f, 0.8f),
            leftEyeNormalized = PointF(0.35f, 0.4f),
            rightEyeNormalized = null,
            headEulerAngleY = 0f,
        )
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(30f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Perfect alignment ────────────────────────────────────────────────────

    @Test
    fun `perfectly horizontal eye line scores 100 and is aligned`() {
        val face = landmarkWithHorizontalEyes()
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── Slight tilt — still aligned ───────────────────────────────────────────

    @Test
    fun `small tilt 3 degrees scores above 75`() {
        val face = landmarkWithTiltedEyes(tiltDeg = 3f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        // Linear formula: 100 * (1 - 3/15) = 80f; threshold is ALIGNED_THRESHOLD=85f so isAligned=false here.
        // 3° is a minor tilt; score should be well above 75.
        assertTrue("Score should be >= 75 for 3° tilt, was ${result.score}", result.score >= 75f)
    }

    // ── Heavy tilt — not aligned ──────────────────────────────────────────────

    @Test
    fun `tilt 15 degrees or more scores 0 and is not aligned`() {
        val face = landmarkWithTiltedEyes(tiltDeg = 15f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    @Test
    fun `tilt 30 degrees scores 0 and is not aligned`() {
        val face = landmarkWithTiltedEyes(tiltDeg = 30f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertEquals(0f, result.score, 0.001f)
        assertFalse(result.isAligned)
    }

    // ── Score bounds ─────────────────────────────────────────────────────────

    @Test
    fun `score is always within 0 to 100`() {
        val face = landmarkWithTiltedEyes(tiltDeg = 45f)
        val result = rule.evaluate(listOf(face), 0f, 0f)
        assertTrue(result.score >= 0f)
        assertTrue(result.score <= 100f)
    }

    // ── Multi-face: largest face is picked ───────────────────────────────────

    @Test
    fun `largest face is picked when multiple faces present`() {
        // Large face with horizontal eyes → aligned
        val largeFace = landmarkWithHorizontalEyes(box = RectF(0.1f, 0.1f, 0.9f, 0.9f))
        // Small face with heavy tilt → not aligned if picked
        val smallFace = landmarkWithTiltedEyes(
            box = RectF(0.4f, 0.4f, 0.5f, 0.5f),
            tiltDeg = 20f,
        )
        val result = rule.evaluate(listOf(smallFace, largeFace), 0f, 0f)
        assertEquals(100f, result.score, 0.001f)
        assertTrue(result.isAligned)
    }

    // ── IMU independence ──────────────────────────────────────────────────────

    @Test
    fun `imu parameters do not affect rule output`() {
        val face = landmarkWithHorizontalEyes()
        val result0 = rule.evaluate(listOf(face), imuPitch = 0f, imuRoll = 0f)
        val result45 = rule.evaluate(listOf(face), imuPitch = 45f, imuRoll = 30f)
        assertEquals(result0.score, result45.score, 0.001f)
        assertEquals(result0.isAligned, result45.isAligned)
    }
}
