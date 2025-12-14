/**
 * ClassName: DetectionResult
 * Description: Domain model for ML Kit detection results
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 1.3.0
 *
 * Modifications:
 * - 2025-11-17: Initial creation for ML Kit integration
 * - 2025-11-18: Added imageWidth and imageHeight for coordinate transformation
 * - 2025-11-20: Added rotationDegrees for proper coordinate transformation
 */
package com.akirachang.ai.photographer.domain.model

import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.DetectedObject

/**
 * Detection result data class
 *
 * Encapsulates ML Kit face and object detection results
 *
 * @property faces List of detected faces
 * @property objects List of detected objects
 * @property imageWidth Width of the analyzed image (for coordinate transformation)
 * @property imageHeight Height of the analyzed image (for coordinate transformation)
 * @property rotationDegrees Image rotation in degrees (0, 90, 180, 270)
 * @property timestamp Detection timestamp in milliseconds
 */
data class DetectionResult(
    val faces: List<Face> = emptyList(),
    val objects: List<DetectedObject> = emptyList(),
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
