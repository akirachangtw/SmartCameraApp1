/**
 * ClassName: DetectionOverlay
 * Description: Canvas-based overlay for drawing ML Kit detection bounding boxes
 * Author: Claude Code
 * Date: 2025-11-18
 * Version: 1.4.2
 *
 * Features:
 * - Face bounding boxes with tracking IDs (green)
 * - Object bounding boxes with labels and confidence scores (blue)
 * - Uses official MlKitAnalyzer with VIEW_REFERENCED coordinates (no manual transformation needed!)
 * - Direct rendering without animations for immediate display
 *
 * Modifications:
 * - 2025-11-18: Initial creation
 * - 2025-11-20: Fixed coordinate transformation to handle rotation and scaling (v1.1.0)
 * - 2025-11-22: Critical fix - removed double rotation issue in transformRect() (v1.2.0)
 * - 2025-11-23: Fixed dimension swap bug - imageSize already rotated by ML Kit (v1.3.0)
 * - 2025-11-23: Improved boundary checking, removed out-of-bounds warnings for filtered detections (v1.3.4)
 * - 2025-11-23: MAJOR REFACTOR - Removed all manual coordinate transformation logic, now using
 *               official MlKitAnalyzer with COORDINATE_SYSTEM_VIEW_REFERENCED (v1.4.0)
 * - 2025-11-25: Removed animation system to fix multi-face rendering issue - all boxes now
 *               display immediately without sequential animation delays (v1.4.2)
 */
package com.akirachang.ai.photographer.presentation.camera.components.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.DetectedObject
import com.akirachang.ai.photographer.domain.model.DetectionResult

/**
 * Detection overlay composable
 *
 * Renders bounding boxes for detected faces and objects on camera preview
 *
 * IMPORTANT: With v1.4.0, coordinates from MlKitAnalyzer are already in VIEW space,
 * so we can draw them directly without any transformation!
 *
 * v1.4.2: Removed animation system to fix rendering issue where only first box displayed.
 * All detected boxes now render immediately.
 *
 * @param detectionResult ML Kit detection result (coordinates already in View space)
 * @param modifier Modifier for the canvas
 */
@Composable
fun DetectionOverlay(
    detectionResult: DetectionResult?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        detectionResult?.let { result ->
            // Draw face bounding boxes directly (no animation)
            result.faces.forEachIndexed { index, face ->
                val trackingId = face.trackingId ?: index
                val rect = face.boundingBox.toComposeRect()
                drawFaceBoundingBox(
                    rect = rect,
                    trackingId = trackingId,
                    face = face
                )
            }

            // Draw object bounding boxes directly (no animation)
            result.objects.forEachIndexed { index, obj ->
                val trackingId = obj.trackingId ?: index
                val rect = obj.boundingBox.toComposeRect()
                drawObjectBoundingBox(
                    rect = rect,
                    trackingId = trackingId,
                    detectedObject = obj
                )
            }
        }
    }
}

/**
 * Draw face bounding box with tracking ID
 *
 * Color: Green (#4CAF50)
 * Style: Stroke with rounded corners
 * Label: Tracking ID
 */
private fun DrawScope.drawFaceBoundingBox(
    rect: Rect,
    trackingId: Int,
    face: Face
) {
    val strokeWidth = 4f
    val color = Color(0xFF4CAF50) // Green

    // Draw bounding box
    drawRect(
        color = color,
        topLeft = rect.topLeft,
        size = Size(rect.width, rect.height),
        style = Stroke(width = strokeWidth)
    )

    // Draw tracking ID label
    val labelText = "ID: $trackingId"
    val textSize = 14.sp.toPx()
    val textPaint = Paint().asFrameworkPaint().apply {
        this.color = color.toArgb()
        this.textSize = textSize
        isAntiAlias = true
        isFakeBoldText = true
    }

    // Background for label
    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)
    val labelWidth = textBounds.width() + 16f
    val labelHeight = textBounds.height() + 12f

    // Draw label background
    drawRect(
        color = color,
        topLeft = Offset(rect.left, rect.top - labelHeight),
        size = Size(labelWidth, labelHeight)
    )

    // Draw label text
    drawContext.canvas.nativeCanvas.drawText(
        labelText,
        rect.left + 8f,
        rect.top - 6f,
        textPaint.apply {
            this.color = Color.White.toArgb()
        }
    )
}

/**
 * Draw object bounding box with label and confidence
 *
 * Color: Blue (#2196F3)
 * Style: Stroke with rounded corners
 * Label: Object category and confidence score
 */
private fun DrawScope.drawObjectBoundingBox(
    rect: Rect,
    trackingId: Int,
    detectedObject: DetectedObject
) {
    val strokeWidth = 4f
    val color = Color(0xFF2196F3) // Blue

    // Draw bounding box
    drawRect(
        color = color,
        topLeft = rect.topLeft,
        size = Size(rect.width, rect.height),
        style = Stroke(width = strokeWidth)
    )

    // Get object label and confidence
    val label = detectedObject.labels.firstOrNull()
    val labelText = if (label != null) {
        val confidence = (label.confidence * 100).toInt()
        "${label.text} $confidence%"
    } else {
        "物件 #$trackingId"
    }

    val textSize = 14.sp.toPx()
    val textPaint = Paint().asFrameworkPaint().apply {
        this.color = color.toArgb()
        this.textSize = textSize
        isAntiAlias = true
        isFakeBoldText = true
    }

    // Background for label
    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)
    val labelWidth = textBounds.width() + 16f
    val labelHeight = textBounds.height() + 12f

    // Draw label background
    drawRect(
        color = color,
        topLeft = Offset(rect.left, rect.top - labelHeight),
        size = Size(labelWidth, labelHeight)
    )

    // Draw label text
    drawContext.canvas.nativeCanvas.drawText(
        labelText,
        rect.left + 8f,
        rect.top - 6f,
        textPaint.apply {
            this.color = Color.White.toArgb()
        }
    )
}

/**
 * Extension function to convert android.graphics.Rect to Compose Rect
 *
 * With MlKitAnalyzer using COORDINATE_SYSTEM_VIEW_REFERENCED, the bounding boxes
 * are already in PreviewView coordinates, so we just need a simple type conversion!
 */
private fun android.graphics.Rect.toComposeRect(): Rect {
    return Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
}
