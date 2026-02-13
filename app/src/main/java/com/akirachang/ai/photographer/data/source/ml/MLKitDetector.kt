/**
 * ClassName: MLKitDetector
 * Description: ML Kit detector wrapper for face and object detection
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 1.3.5
 *
 * Modifications:
 * - 2025-11-17: Initial creation for ML Kit integration
 * - 2025-11-18: Added image size capture for coordinate transformation
 * - 2025-11-20: Added rotation capture and enhanced debug logging
 * - 2025-11-23: Added boundary and confidence filtering (v1.3.4)
 * - 2025-11-23: Fixed filtering to use mediaImage dimensions (v1.3.5)
 */
package com.akirachang.ai.photographer.data.source.ml

import android.graphics.PointF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.tasks.await
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.model.EyePosition
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit 偵測器
 *
 * 封裝 ML Kit 的人臉與物件偵測 API
 * 提供統一的偵測介面
 */
@Singleton
class MLKitDetector @Inject constructor() {

    companion object {
        private const val TAG = "MLKitDetector"

        // Filtering thresholds
        private const val MIN_OBJECT_CONFIDENCE = 0.5f // 最小物件信心度（50%）
        private const val MIN_BOX_SIZE = 20 // 最小邊界框尺寸（像素）
        private const val MAX_BOX_SIZE_RATIO = 0.9f // 最大邊界框尺寸比例（佔影像90%）
    }

    // 人臉偵測器（延遲初始化）
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) // v2.11.0: Enable eye landmarks for Eye-AF
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f) // 最小人臉大小（佔畫面比例）
            .enableTracking() // 啟用人臉追蹤
            .build()

        FaceDetection.getClient(options).also {
            Log.i(TAG, "Face detector initialized")
        }
    }

    // 物件偵測器（延遲初始化）
    private val objectDetector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE) // 串流模式（影片）
            .enableMultipleObjects() // 啟用多物件偵測
            .enableClassification() // 啟用分類
            .build()

        ObjectDetection.getClient(options).also {
            Log.i(TAG, "Object detector initialized")
        }
    }

    /**
     * 同時執行人臉與物件偵測
     *
     * @param imageProxy CameraX 的影像代理
     * @return DetectionResult 偵測結果，如果失敗則返回 null
     */
    @OptIn(ExperimentalGetImage::class)
    suspend fun detect(imageProxy: ImageProxy): DetectionResult? {
        return try {
            val mediaImage = imageProxy.image!!
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            // Log image info for debugging
            Log.d(
                TAG,
                "MediaImage size: ${mediaImage.width}x${mediaImage.height}, " +
                        "InputImage size: ${inputImage.width}x${inputImage.height}, " +
                        "rotation: ${imageProxy.imageInfo.rotationDegrees}°"
            )

            // 並行執行人臉與物件偵測
            val rawFaces = detectFaces(inputImage)
            val rawObjects = detectObjects(inputImage)

            // IMPORTANT: Filter using MediaImage dimensions (ML Kit coordinate space)
            // ML Kit returns coordinates in the original image space (mediaImage),
            // NOT in the rotated space (inputImage)
            val filteredFaces = filterFaces(rawFaces, mediaImage.width, mediaImage.height)
            val filteredObjects = filterObjects(rawObjects, mediaImage.width, mediaImage.height)

            DetectionResult(
                faces = filteredFaces,
                objects = filteredObjects,
                eyePositions = extractEyePositions(filteredFaces),  // v2.11.0: Eye-AF
                imageWidth = inputImage.width,
                imageHeight = inputImage.height,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                timestamp = System.currentTimeMillis()
            ).also {
                // Log filtering summary
                val facesFiltered = rawFaces.size - filteredFaces.size
                val objectsFiltered = rawObjects.size - filteredObjects.size

                Log.i(
                    TAG,
                    "Detection: ${filteredFaces.size} faces (${facesFiltered} filtered), ${filteredObjects.size} objects (${objectsFiltered} filtered) | Image: ${inputImage.width}x${inputImage.height}, Rotation: ${imageProxy.imageInfo.rotationDegrees}°"
                )

                // Log individual face bounding boxes for debugging
                filteredFaces.forEachIndexed { index, face ->
                    val box = face.boundingBox
                    Log.d(TAG, "Face $index (ID: ${face.trackingId}): Rect[${box.left}, ${box.top}, ${box.right}, ${box.bottom}]")
                }

                // Log individual object bounding boxes for debugging
                filteredObjects.forEachIndexed { index, obj ->
                    val box = obj.boundingBox
                    val label = obj.labels.firstOrNull()
                    Log.d(TAG, "Object $index (ID: ${obj.trackingId}): ${label?.text ?: "Unknown"} ${label?.let { "(${(it.confidence * 100).toInt()}%)" } ?: ""} Rect[${box.left}, ${box.top}, ${box.right}, ${box.bottom}]")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Detection failed", e)
            null
        }
    }

    /**
     * Extract eye positions from detected faces (v2.11.0 Eye-AF)
     *
     * @param faces List of detected faces
     * @return List of eye positions with nearest eye calculation
     */
    private fun extractEyePositions(faces: List<Face>): List<EyePosition> {
        return faces.mapNotNull { face ->
            val leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE)
            val rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE)

            val leftEye = leftEyeLandmark?.position
            val rightEye = rightEyeLandmark?.position

            val nearestEye = calculateNearestEye(leftEye, rightEye, face.headEulerAngleY)

            if (leftEye != null || rightEye != null) {
                EyePosition(
                    leftEye = leftEye,
                    rightEye = rightEye,
                    nearestEye = nearestEye,
                    faceId = face.trackingId ?: -1
                ).also {
                    Log.d(TAG, "Eye-AF: Face ${face.trackingId} - Left: $leftEye, Right: $rightEye, Nearest: $nearestEye (Yaw: ${face.headEulerAngleY}°)")
                }
            } else {
                null
            }
        }
    }

    /**
     * Calculate nearest eye to camera based on head pose (v2.11.0 Eye-AF)
     *
     * Uses head yaw angle to determine which eye is closer:
     * - Face turned left (positive yaw > 10°): right eye is closer
     * - Face turned right (negative yaw < -10°): left eye is closer
     * - Face front (yaw between -10° and 10°): choose eye with lower Y coordinate
     *
     * @param leftEye Left eye position
     * @param rightEye Right eye position
     * @param headEulerAngleY Head yaw angle in degrees
     * @return The eye closest to camera, or null if both eyes are null
     */
    private fun calculateNearestEye(
        leftEye: PointF?,
        rightEye: PointF?,
        headEulerAngleY: Float
    ): PointF? {
        return when {
            headEulerAngleY > 10f -> rightEye  // Face left, right eye closer
            headEulerAngleY < -10f -> leftEye  // Face right, left eye closer
            else -> {
                // Front face: choose eye with lower Y (higher in image, closer to camera)
                if (leftEye != null && rightEye != null) {
                    if (leftEye.y < rightEye.y) leftEye else rightEye
                } else {
                    leftEye ?: rightEye
                }
            }
        }
    }

    /**
     * 執行人臉偵測
     *
     * @param inputImage ML Kit 輸入影像
     * @return List<Face> 偵測到的人臉列表
     */
    private suspend fun detectFaces(inputImage: InputImage): List<Face> {
        return try {
            faceDetector.process(inputImage).await()
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            emptyList()
        }
    }

    /**
     * 執行物件偵測
     *
     * @param inputImage ML Kit 輸入影像
     * @return List<DetectedObject> 偵測到的物件列表
     */
    private suspend fun detectObjects(inputImage: InputImage): List<DetectedObject> {
        return try {
            objectDetector.process(inputImage).await()
        } catch (e: Exception) {
            Log.e(TAG, "Object detection failed", e)
            emptyList()
        }
    }

    /**
     * 過濾人臉偵測結果
     *
     * 移除邊界外或異常的人臉框
     *
     * @param faces 原始人臉列表
     * @param imageWidth 影像寬度
     * @param imageHeight 影像高度
     * @return 過濾後的人臉列表
     */
    private fun filterFaces(faces: List<Face>, imageWidth: Int, imageHeight: Int): List<Face> {
        return faces.filter { face ->
            val box = face.boundingBox
            val reason = getFilterReason(box, imageWidth, imageHeight, null)

            if (reason != null) {
                Log.d(TAG, "❌ Filtered Face (ID: ${face.trackingId}): $reason - Rect[${box.left}, ${box.top}, ${box.right}, ${box.bottom}]")
                false
            } else {
                true
            }
        }
    }

    /**
     * 過濾物件偵測結果
     *
     * 移除低信心度、邊界外或異常的物件框
     *
     * @param objects 原始物件列表
     * @param imageWidth 影像寬度
     * @param imageHeight 影像高度
     * @return 過濾後的物件列表
     */
    private fun filterObjects(objects: List<DetectedObject>, imageWidth: Int, imageHeight: Int): List<DetectedObject> {
        return objects.filter { obj ->
            val box = obj.boundingBox
            val confidence = obj.labels.firstOrNull()?.confidence

            val reason = getFilterReason(box, imageWidth, imageHeight, confidence)

            if (reason != null) {
                val label = obj.labels.firstOrNull()
                Log.d(TAG, "❌ Filtered Object (ID: ${obj.trackingId}): $reason - ${label?.text ?: "Unknown"} ${confidence?.let { "(${(it * 100).toInt()}%)" } ?: ""} Rect[${box.left}, ${box.top}, ${box.right}, ${box.bottom}]")
                false
            } else {
                true
            }
        }
    }

    /**
     * 檢查邊界框是否應該被過濾，並返回過濾原因
     *
     * @param box 邊界框
     * @param imageWidth 影像寬度
     * @param imageHeight 影像高度
     * @param confidence 信心度（可選，用於物件）
     * @return 過濾原因，null 表示通過檢查
     */
    private fun getFilterReason(
        box: android.graphics.Rect,
        imageWidth: Int,
        imageHeight: Int,
        confidence: Float?
    ): String? {
        // 1. 檢查信心度（僅物件）
        if (confidence != null && confidence < MIN_OBJECT_CONFIDENCE) {
            return "Low confidence (${(confidence * 100).toInt()}% < ${(MIN_OBJECT_CONFIDENCE * 100).toInt()}%)"
        }

        // 2. 檢查負座標（邊界外）
        if (box.left < 0 || box.top < 0) {
            return "Negative coordinates (left=${box.left}, top=${box.top})"
        }

        // 3. 檢查超出影像邊界
        if (box.right > imageWidth || box.bottom > imageHeight) {
            return "Out of image bounds (right=${box.right}>${imageWidth} or bottom=${box.bottom}>${imageHeight})"
        }

        // 4. 檢查過小的框
        val width = box.width()
        val height = box.height()
        if (width < MIN_BOX_SIZE || height < MIN_BOX_SIZE) {
            return "Box too small (${width}x${height} < ${MIN_BOX_SIZE}px)"
        }

        // 5. 檢查過大的框（可能是誤報）
        val widthRatio = width.toFloat() / imageWidth
        val heightRatio = height.toFloat() / imageHeight
        if (widthRatio > MAX_BOX_SIZE_RATIO || heightRatio > MAX_BOX_SIZE_RATIO) {
            return "Box too large (${(widthRatio * 100).toInt()}%x${(heightRatio * 100).toInt()}% > ${(MAX_BOX_SIZE_RATIO * 100).toInt()}%)"
        }

        return null // 通過所有檢查
    }

    /**
     * 釋放偵測器資源
     *
     * 應在不再需要偵測時呼叫
     */
    fun release() {
        try {
            faceDetector.close()
            objectDetector.close()
            Log.d(TAG, "Detectors released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release detectors", e)
        }
    }
}
