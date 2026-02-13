/**
 * ClassName: DetectionResult
 * Description: Domain model for ML Kit detection results
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 2.11.0
 *
 * Modifications:
 * - 2025-11-17: Initial creation for ML Kit integration
 * - 2025-11-18: Added imageWidth and imageHeight for coordinate transformation
 * - 2025-11-20: Added rotationDegrees for proper coordinate transformation
 * - 2026-02-12: Added EyePosition for Eye-AF (v2.11.0)
 */
package com.akirachang.ai.photographer.domain.model

import android.graphics.PointF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.DetectedObject

/**
 * Eye position for Eye-AF (v2.11.0)
 *
 * Represents detected eye landmarks for intelligent portrait focusing
 *
 * @property leftEye Left eye position in image coordinates
 * @property rightEye Right eye position in image coordinates
 * @property nearestEye The eye closest to camera (for optimal portrait focus)
 * @property faceId Face tracking ID this eye position belongs to
 */
data class EyePosition(
    val leftEye: PointF?,
    val rightEye: PointF?,
    val nearestEye: PointF?,
    val faceId: Int
)

/**
 * Detection result data class
 *
 * Encapsulates ML Kit face and object detection results
 *
 * @property faces List of detected faces
 * @property objects List of detected objects
 * @property eyePositions List of detected eye positions for Eye-AF (v2.11.0)
 * @property imageWidth Width of the analyzed image (for coordinate transformation)
 * @property imageHeight Height of the analyzed image (for coordinate transformation)
 * @property rotationDegrees Image rotation in degrees (0, 90, 180, 270)
 * @property timestamp Detection timestamp in milliseconds
 */
data class DetectionResult(
    val faces: List<Face> = emptyList(),
    val objects: List<DetectedObject> = emptyList(),
    val eyePositions: List<EyePosition> = emptyList(),
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val rotationDegrees: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 是否偵測到任何內容
     */
    val hasDetections: Boolean
        get() = faces.isNotEmpty() || objects.isNotEmpty()

    /**
     * 偵測到的總數量
     */
    val totalDetections: Int
        get() = faces.size + objects.size

    /**
     * 是否偵測到人臉
     */
    val hasFaces: Boolean
        get() = faces.isNotEmpty()

    /**
     * 是否偵測到物件
     */
    val hasObjects: Boolean
        get() = objects.isNotEmpty()
}
