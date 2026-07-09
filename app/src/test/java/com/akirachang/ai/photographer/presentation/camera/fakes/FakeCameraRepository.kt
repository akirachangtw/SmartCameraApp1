package com.akirachang.ai.photographer.presentation.camera.fakes

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraControl
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.model.ExifInfo
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.DetectedObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * STATE-CONTRACT-03 AI-2 — hand-written CameraRepository fake driving the real
 * CameraViewModel end-to-end (Option A integration harness, CSO-approved).
 *
 * Two seams are controllable; everything else is an inert default so the
 * ViewModel init subscriptions register without crashing:
 *   - [detectionResultsFlow]: push ML Kit detection frames through Path A.
 *   - [focusOnPoint]: resolve/throw on demand and count dispatches, so tests
 *     observe the GAP-07 producer wiring purely through the pipeline (never by
 *     injecting afState — Hard Constraint 2, 鐵律 9).
 */
class FakeCameraRepository : CameraRepository {

    /** Controllable ML Kit detection source (Path A single source of truth). */
    val detectionResultsFlow = MutableStateFlow<DetectionResult?>(null)

    /** Number of times [focusOnPoint] has been dispatched (producer-wiring probe). */
    var focusOnPointCallCount = 0
        private set

    /** When true, [focusOnPoint] throws to exercise the AI-1 catch → Failed branch. */
    var focusShouldThrow = false

    private val fixedFocusFlow = MutableStateFlow(false)

    override suspend fun focusOnPoint(x: Float, y: Float, viewWidth: Int, viewHeight: Int): Result<Unit> {
        focusOnPointCallCount++
        if (focusShouldThrow) {
            throw IllegalStateException("FakeCameraRepository: forced focus failure")
        }
        return Result.success(Unit)
    }

    override val detectionResults: StateFlow<DetectionResult?> get() = detectionResultsFlow
    override val isFixedFocusLens: StateFlow<Boolean> get() = fixedFocusFlow

    override fun getPreviewStream(): Flow<Bitmap> = emptyFlow()
    override suspend fun takePicture(): CaptureResult =
        CaptureResult.Error(UnsupportedOperationException("fake"))
    override suspend fun switchCamera() {}
    override fun getCurrentCamera(): CameraLens = CameraLens.BACK
    override fun selectCamera(lens: CameraLens) {}
    override fun setFlashMode(flashMode: Int) {}
    override fun getFlashMode(): Int = 0
    override fun startImageAnalysis() {}
    override fun stopImageAnalysis() {}
    override fun enableHDRMode() {}
    override fun disableHDRMode() {}
    override fun shouldEnableAutoHDR(lightingState: LightingState): Boolean = false
    override suspend fun detectFaces(image: InputImage): List<Face> = emptyList()
    override suspend fun detectObjects(image: InputImage): List<DetectedObject> = emptyList()
    override fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {}
    override fun setPreviewSurfaceProvider(surfaceProvider: Preview.SurfaceProvider?) {}
    override fun setPreviewView(previewView: PreviewView?) {}
    override fun supportsNativePreview(): Boolean = false
    override suspend fun setAspectRatioMode(mode: AspectRatioMode) {}
    override suspend fun lockAEAF(x: Float, y: Float, viewWidth: Int, viewHeight: Int): Result<Unit> = Result.success(Unit)
    override suspend fun cancelFocusAndMetering(): Result<Unit> = Result.success(Unit)
    override fun setPreviewSize(width: Int, height: Int) {}
    override suspend fun readExifInfo(uri: Uri): ExifInfo? = null
    override fun getCameraControl(): CameraControl? = null
    override fun getMeteringPointFactory(): MeteringPointFactory? = null
    override fun getFocalLengthFactor(): Float? = null
}
