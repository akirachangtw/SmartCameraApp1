package com.akirachang.ai.photographer.composition

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.akirachang.ai.photographer.domain.composition.rules.EyeLineRule
import com.akirachang.ai.photographer.domain.composition.rules.GazeRule
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * D6-B1-T1.5 Physical Landmark Probe — Instrumented Integration Test
 *
 * Reads real JPEG portrait images from debug assets, runs them through ML Kit
 * FaceDetector (LANDMARK_MODE_ALL + eulerAngles), converts results to
 * [FaceLandmarkData], then feeds them into [EyeLineRule] and [GazeRule] to
 * verify physical plausibility of the rule scoring logic.
 *
 * Note: This test uses LANDMARK_MODE_ALL intentionally (unlike production
 * which uses LANDMARK_MODE_NONE for crash-safety). The probe detector is
 * isolated to this test class and does not affect production code.
 *
 * Asset location: app/src/debug/assets/test_images/
 */
@RunWith(AndroidJUnit4::class)
class LandmarkProbeTest {

    private val tag = "D6_PROBE"

    private lateinit var gazeRule: GazeRule
    private lateinit var eyeLineRule: EyeLineRule
    private lateinit var detector: com.google.mlkit.vision.face.FaceDetector

    @Before
    fun setUp() {
        gazeRule = GazeRule()
        eyeLineRule = EyeLineRule()

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .enableTracking()
            .build()
        detector = FaceDetection.getClient(options)
    }

    @After
    fun tearDown() {
        detector.close()
    }

    // ── Helper: load asset bitmap ────────────────────────────────────────────

    private fun loadAssetBitmap(fileName: String): Bitmap {
        // Use targetContext (app APK) — debug assets live in the app APK, not the test APK.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.assets.open("test_images/$fileName").use { stream ->
            BitmapFactory.decodeStream(stream)
                ?: error("Failed to decode bitmap from asset: $fileName")
        }
    }

    // ── Helper: run ML Kit synchronously ────────────────────────────────────

    private fun detectFaces(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return Tasks.await(detector.process(inputImage))
    }

    // ── Helper: map ML Kit Face → FaceLandmarkData ──────────────────────────

    private fun Face.toFaceLandmarkData(imgW: Int, imgH: Int): FaceLandmarkData {
        val box = boundingBox
        // ML Kit may return bounding boxes slightly outside image bounds.
        // Clamp to [0, 1] to satisfy CompositionRule normalized-coordinate contract.
        val boundingBoxNormalized = RectF(
            (box.left.toFloat() / imgW).coerceIn(0f, 1f),
            (box.top.toFloat() / imgH).coerceIn(0f, 1f),
            (box.right.toFloat() / imgW).coerceIn(0f, 1f),
            (box.bottom.toFloat() / imgH).coerceIn(0f, 1f),
        )

        Log.d("D6_PROBE", "boundingBoxNormalized=$boundingBoxNormalized (raw: ${box.left},${box.top},${box.right},${box.bottom} imgW=$imgW imgH=$imgH)")

        val leftEyeNormalized = getLandmark(FaceLandmark.LEFT_EYE)
            ?.position
            ?.let { pt -> PointF(pt.x / imgW, pt.y / imgH) }
        val rightEyeNormalized = getLandmark(FaceLandmark.RIGHT_EYE)
            ?.position
            ?.let { pt -> PointF(pt.x / imgW, pt.y / imgH) }

        return FaceLandmarkData(
            boundingBoxNormalized = boundingBoxNormalized,
            leftEyeNormalized = leftEyeNormalized,
            rightEyeNormalized = rightEyeNormalized,
            headEulerAngleY = headEulerAngleY,
        )
    }

    // ── Test 1: Front face (0°) ──────────────────────────────────────────────

    @Test
    fun testFrontFace_0deg() {
        val bitmap = loadAssetBitmap("portrait_front_0deg.jpg")
        val faces = detectFaces(bitmap)

        assertTrue("ML Kit must detect at least one face in portrait_front_0deg.jpg", faces.isNotEmpty())

        val primary = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
        val data = primary.toFaceLandmarkData(bitmap.width, bitmap.height)

        Log.d(tag, "Front Face (0°) — " +
            "LeftEye=${data.leftEyeNormalized} " +
            "RightEye=${data.rightEyeNormalized} " +
            "EulerY=${data.headEulerAngleY}")

        // GazeRule: front-facing subject should score close to 100
        val gazeResult = gazeRule.evaluate(listOf(data), 0f, 0f)
        Log.d(tag, "Front Face GazeRule — score=${gazeResult.score} isAligned=${gazeResult.isAligned}")

        assertTrue(
            "Front face GazeRule score should be >= 85 (eulerY≈0°), was ${gazeResult.score}",
            gazeResult.score >= 85f,
        )
        assertTrue(
            "Front face GazeRule should be aligned",
            gazeResult.isAligned,
        )

        // EyeLineRule: horizontal eyes should score well
        val eyeResult = eyeLineRule.evaluate(listOf(data), 0f, 0f)
        Log.d(tag, "Front Face EyeLineRule — score=${eyeResult.score} isAligned=${eyeResult.isAligned}")

        assertTrue(
            "Front face EyeLineRule score should be > 0, was ${eyeResult.score}",
            eyeResult.score > 0f,
        )
    }

    // ── Test 2: Side face (30°) ──────────────────────────────────────────────

    @Test
    fun testSideFace_30deg() {
        val bitmapFront = loadAssetBitmap("portrait_front_0deg.jpg")
        val facesFront = detectFaces(bitmapFront)
        assertTrue("Must detect face in portrait_front_0deg.jpg", facesFront.isNotEmpty())
        val frontData = facesFront
            .maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
            .toFaceLandmarkData(bitmapFront.width, bitmapFront.height)
        val frontScore = gazeRule.evaluate(listOf(frontData), 0f, 0f).score

        val bitmap = loadAssetBitmap("portrait_side_30deg.jpg")
        val faces = detectFaces(bitmap)
        assertTrue("ML Kit must detect at least one face in portrait_side_30deg.jpg", faces.isNotEmpty())

        val primary = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
        val data = primary.toFaceLandmarkData(bitmap.width, bitmap.height)

        Log.d(tag, "30° Side Face — " +
            "LeftEye=${data.leftEyeNormalized} " +
            "RightEye=${data.rightEyeNormalized} " +
            "EulerY=${data.headEulerAngleY}")

        val gazeResult = gazeRule.evaluate(listOf(data), 0f, 0f)
        Log.d(tag, "30° Side Face GazeRule — score=${gazeResult.score} isAligned=${gazeResult.isAligned} (front was $frontScore)")

        assertTrue(
            "30° side face GazeRule score should be lower than front face ($frontScore), was ${gazeResult.score}",
            gazeResult.score < frontScore,
        )
    }

    // ── Test 3: Extreme side face (60°) — Fail-safe ─────────────────────────

    @Test
    fun testExtremeSideFace_60deg() {
        val bitmap = loadAssetBitmap("portrait_extreme_side_60deg.jpg")
        val faces = detectFaces(bitmap)
        assertTrue("ML Kit must detect at least one face in portrait_extreme_side_60deg.jpg", faces.isNotEmpty())

        val primary = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
        val data = primary.toFaceLandmarkData(bitmap.width, bitmap.height)

        Log.d(tag, "60° Extreme Side Face — " +
            "LeftEye=${data.leftEyeNormalized} " +
            "RightEye=${data.rightEyeNormalized} " +
            "EulerY=${data.headEulerAngleY}")

        val gazeResult = gazeRule.evaluate(listOf(data), 0f, 0f)
        Log.d(tag, "60° Extreme Side Face GazeRule — score=${gazeResult.score} isAligned=${gazeResult.isAligned}")

        // Fail-safe: abs(60°) > EXTREME_YAW_DEG(45°) → subject intentionally side-on.
        // Rule must return isAligned=true so PriorityEngine skips it (not a low-score penalty).
        assertEquals(
            "Extreme 60° yaw should trigger fail-safe: score must be 100f, was ${gazeResult.score}",
            100f,
            gazeResult.score,
            0.001f,
        )
        assertTrue(
            "Extreme 60° yaw GazeRule should be aligned (fail-safe — skip signal)",
            gazeResult.isAligned,
        )

        // EyeLineRule: landmark detection may be unreliable at extreme angles
        val eyeResult = eyeLineRule.evaluate(listOf(data), 0f, 0f)
        Log.d(tag, "60° Extreme Side Face EyeLineRule — score=${eyeResult.score} isAligned=${eyeResult.isAligned}")
        // Score should be within valid range regardless of landmark availability
        assertTrue("EyeLineRule score must be >= 0", eyeResult.score >= 0f)
        assertTrue("EyeLineRule score must be <= 100", eyeResult.score <= 100f)
    }
}
