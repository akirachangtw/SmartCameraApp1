/**
 * ClassName: DetectionOverlay
 * Description: Canvas-based overlay for drawing ML Kit detection bounding boxes
 * Author: Claude Code
 * Date: 2025-11-18
 * Version: 1.4.0
 *
 * Features:
 * - Face bounding boxes with tracking IDs (green)
 * - Object bounding boxes with labels and confidence scores (blue)
 * - Uses official MlKitAnalyzer with VIEW_REFERENCED coordinates (no manual transformation needed!)
 * - Smooth animations for box transitions
 *
 * Modifications:
 * - 2025-11-18: Initial creation
 * - 2025-11-20: Fixed coordinate transformation to handle rotation and scaling (v1.1.0)
 * - 2025-11-22: Critical fix - removed double rotation issue in transformRect() (v1.2.0)
 * - 2025-11-23: Fixed dimension swap bug - imageSize already rotated by ML Kit (v1.3.0)
 * - 2025-11-23: Improved boundary checking, removed out-of-bounds warnings for filtered detections (v1.3.4)
 * - 2025-11-23: MAJOR REFACTOR - Removed all manual coordinate transformation logic, now using
 *               official MlKitAnalyzer with COORDINATE_SYSTEM_VIEW_REFERENCED (v1.4.0)
 */
package local.mylab.smartcameraapp1.presentation.camera.components.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import local.mylab.smartcameraapp1.domain.model.DetectionResult

/**
 * Detection overlay composable
 *
 * Renders bounding boxes for detected faces and objects on camera preview
 *
 * IMPORTANT: With v1.4.0, coordinates from MlKitAnalyzer are already in VIEW space,
 * so we can draw them directly without any transformation!
 *
 * @param detectionResult ML Kit detection result (coordinates already in View space)
 * @param modifier Modifier for the canvas
 */
@Composable
fun DetectionOverlay(
    detectionResult: DetectionResult?,
    modifier: Modifier = Modifier
) {
    // Animated bounding boxes for smooth transitions
    val animatedFaceBoxes = remember {
        mutableMapOf<Int, Animatable<Rect, AnimationVector4D>>()
    }
    val animatedObjectBoxes = remember {
        mutableMapOf<Int, Animatable<Rect, AnimationVector4D>>()
    }

    // Update animations when detection results change
    LaunchedEffect(detectionResult) {
        detectionResult?.let { result ->
            // Animate face boxes
            // IMPORTANT: Coordinates are already in View space, no transformation needed!
            result.faces.forEachIndexed { index, face ->
                val trackingId = face.trackingId ?: index
                val screenRect = face.boundingBox.toComposeRect()

                val animatable = animatedFaceBoxes.getOrPut(trackingId) {
                    Animatable(screenRect, Rect.VectorConverter)
                }

                animatable.animateTo(
                    targetValue = screenRect,
                    animationSpec = tween(durationMillis = 150)
                )
            }

            // Animate object boxes
            // IMPORTANT: Coordinates are already in View space, no transformation needed!
            result.objects.forEachIndexed { index, obj ->
                val trackingId = obj.trackingId ?: index
                val screenRect = obj.boundingBox.toComposeRect()

                val animatable = animatedObjectBoxes.getOrPut(trackingId) {
                    Animatable(screenRect, Rect.VectorConverter)
                }

                animatable.animateTo(
                    targetValue = screenRect,
                    animationSpec = tween(durationMillis = 150)
                )
            }

            // Remove stale animations
            val currentFaceIds = result.faces.mapIndexed { index, face ->
                face.trackingId ?: index
            }.toSet()
            animatedFaceBoxes.keys.retainAll(currentFaceIds)

            val currentObjectIds = result.objects.mapIndexed { index, obj ->
                obj.trackingId ?: index
            }.toSet()
            animatedObjectBoxes.keys.retainAll(currentObjectIds)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        detectionResult?.let { result ->
            // Draw face bounding boxes
            result.faces.forEachIndexed { index, face ->
                val trackingId = face.trackingId ?: index
                animatedFaceBoxes[trackingId]?.value?.let { rect ->
                    drawFaceBoundingBox(
                        rect = rect,
                        trackingId = trackingId,
                        face = face
                    )
                }
            }

            // Draw object bounding boxes
            result.objects.forEachIndexed { index, obj ->
                val trackingId = obj.trackingId ?: index
                animatedObjectBoxes[trackingId]?.value?.let { rect ->
                    drawObjectBoundingBox(
                        rect = rect,
                        trackingId = trackingId,
                        detectedObject = obj
                    )
                }
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
