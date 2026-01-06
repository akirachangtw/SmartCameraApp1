package com.akirachang.ai.photographer.data.source.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.akirachang.ai.photographer.data.source.analysis.GuidelineAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Live Camera Source for Real Device
 *
 * v2.6.1: Migrated from LifecycleCameraController to ProcessCameraProvider
 *
 * Uses ProcessCameraProvider with Preview + ImageAnalysis + ImageCapture UseCases.
 * Preview UseCase is REQUIRED for camera to start on real devices.
 * Converts ImageProxy from ImageAnalysis to Bitmap flow for unified interface.
 */
@Singleton
class LiveCameraSource @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraInputSource {

    companion object {
        private const val TAG = "LiveCameraSource"
    }

    // v2.6.1: Custom SurfaceProvider for Preview UseCase
    // Preview UseCase is mandatory - without it, camera won't start on real devices
    // Reference: chat_logs/CameraX_StaticImage_TestMode_Fix.md lines 37-54
    //
    // Using SurfaceTexture approach instead of PreviewView to avoid WindowManager token issues
    private var previewSurfaceTexture: SurfaceTexture? = null
    private var previewSurface: Surface? = null

    private val customSurfaceProvider = Preview.SurfaceProvider { request ->
        Log.d(TAG, "SurfaceProvider requested: ${request.resolution.width}x${request.resolution.height}")

        // Create SurfaceTexture with requested resolution
        val surfaceTexture = SurfaceTexture(0).apply {
            setDefaultBufferSize(
                request.resolution.width,
                request.resolution.height
            )
        }
        previewSurfaceTexture = surfaceTexture

        // Create Surface from SurfaceTexture
        val surface = Surface(surfaceTexture)
        previewSurface = surface

        // Provide the surface to CameraX
        request.provideSurface(surface, ContextCompat.getMainExecutor(context)) {
            // Cleanup when surface is no longer needed
            Log.d(TAG, "Surface no longer needed, cleaning up")
            surface.release()
            surfaceTexture.release()
            previewSurface = null
            previewSurfaceTexture = null
        }

        Log.i(TAG, "✅ Custom Surface provided to Preview UseCase")
    }

    // ProcessCameraProvider and UseCases
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var imageCapture: ImageCapture

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var currentLifecycleOwner: LifecycleOwner? = null
    private var isPreviewActive = false

    // v2.6.1: Persistent analyzer for preview stream
    // Must be reattached after every camera rebind (e.g., switchCamera)
    private var previewStreamAnalyzer: ImageAnalysis.Analyzer? = null

    // v2.6.2: Pending exposure index for HDR control
    // Must be reapplied after every camera rebind (e.g., switchCamera)
    private var pendingExposureIndex: Int? = null

    // v2.8.0 Phase 0: Performance monitoring (FPS tracking)
    private var frameCount = 0L
    private var lastFpsReportTime = System.currentTimeMillis()
    private var droppedFrameCount = 0L
    private val fpsReportInterval = 30 // Report every 30 frames

    // v2.8.0 Phase 1: Mutex to prevent race conditions during camera switching
    private val bindMutex = Mutex()

    /**
     * Get live camera preview stream as Bitmap flow
     *
     * Uses ImageAnalysis to convert ImageProxy to Bitmap at runtime.
     * Preview continues until stopPreview() is called.
     */
    override fun getPreviewStream(): Flow<Bitmap> = callbackFlow {
        Log.d(TAG, "getPreviewStream: Creating ImageAnalysis analyzer")
        Timber.d("LiveCameraSource: Starting live camera preview stream")

        val analyzer = ImageAnalysis.Analyzer { imageProxy ->
            try {
                // v2.8.0 Phase 0: FPS tracking
                frameCount++

                // Convert ImageProxy to Bitmap
                val rawBitmap = imageProxy.toBitmap()

                // Get rotation from ImageProxy metadata
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                // Check if using front camera (needs mirroring for selfie view)
                val isFrontCamera = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                // Rotate and mirror bitmap if needed to match display orientation
                val bitmap = if (rotationDegrees != 0 || isFrontCamera) {
                    rotateBitmap(rawBitmap, rotationDegrees, isFrontCamera)
                } else {
                    rawBitmap
                }

                val sendResult = trySend(bitmap)
                if (sendResult.isSuccess) {
                    Log.v(TAG, "Preview frame sent: ${bitmap.width}x${bitmap.height}, rotation: $rotationDegrees°")
                } else {
                    // v2.8.0 Phase 0: Count dropped frames
                    droppedFrameCount++
                    Log.w(TAG, "Failed to send preview frame - channel might be closed")
                }

                // v2.8.0 Phase 0: Report FPS + Memory every N frames
                if (frameCount % fpsReportInterval == 0L) {
                    val currentTime = System.currentTimeMillis()
                    val elapsed = currentTime - lastFpsReportTime
                    if (elapsed > 0) {
                        val fps = (fpsReportInterval * 1000f) / elapsed
                        val dropRate = (droppedFrameCount * 100f) / frameCount

                        // Memory monitoring
                        val memoryInfo = android.os.Debug.MemoryInfo()
                        android.os.Debug.getMemoryInfo(memoryInfo)
                        val nativeMB = memoryInfo.nativePss / 1024  // KB to MB
                        val javaMB = memoryInfo.dalvikPss / 1024    // KB to MB
                        val totalMB = memoryInfo.totalPss / 1024    // KB to MB

                        Log.i(TAG, "Performance: FPS=${"%.1f".format(fps)}, " +
                                   "Dropped=${droppedFrameCount}/$frameCount (${"%.1f".format(dropRate)}%), " +
                                   "Memory: Native=${nativeMB}MB, Java=${javaMB}MB, Total=${totalMB}MB")
                    }
                    lastFpsReportTime = currentTime
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to convert ImageProxy to Bitmap", e)
                Timber.e(e, "LiveCameraSource: Failed to process preview frame")
            } finally {
                imageProxy.close()
            }
        }

        // v2.6.1: Store analyzer persistently for rebind scenarios
        previewStreamAnalyzer = analyzer

        // If camera is already initialized, set analyzer immediately
        if (::imageAnalysis.isInitialized) {
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                analyzer
            )
            Log.i(TAG, "✅ Preview stream analyzer set immediately - waiting for frames")
        } else {
            Log.i(TAG, "Preview stream analyzer stored - will be set after camera binds")
        }

        isPreviewActive = true
        Timber.i("LiveCameraSource: Preview stream active")

        awaitClose {
            Log.d(TAG, "Preview stream closing")
            Timber.d("LiveCameraSource: Preview stream closed")
            // v2.8.0 Phase 1: Stop GuidelineAnalyzer before clearing
            (previewStreamAnalyzer as? GuidelineAnalyzer)?.stop()
            if (::imageAnalysis.isInitialized) {
                imageAnalysis.clearAnalyzer()
            }
            previewStreamAnalyzer = null  // v2.6.1: Clear persistent analyzer
            isPreviewActive = false
        }
    }

    /**
     * Capture a still image from the camera
     *
     * Uses CameraX ImageCapture to capture a high-quality still image
     * and converts it to Bitmap for the unified interface.
     *
     * @return Result containing the captured Bitmap or error
     */
    override suspend fun takePicture(): Result<Bitmap> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "takePicture: Capturing image with ImageCapture")
        Timber.d("LiveCameraSource: Taking picture with real camera")

        // Check if camera is initialized
        if (!::imageCapture.isInitialized) {
            Log.e(TAG, "ImageCapture not initialized - camera not ready")
            continuation.resume(Result.failure(IllegalStateException("Camera not initialized")))
            return@suspendCancellableCoroutine
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    Log.i(TAG, "Image captured successfully (${image.width}x${image.height})")
                    Timber.d("LiveCameraSource: Image captured successfully")
                    try {
                        val bitmap = image.toBitmap()
                        Log.i(TAG, "Bitmap created from captured image: ${bitmap.width}x${bitmap.height}")
                        Timber.i("LiveCameraSource: Bitmap created: ${bitmap.width}x${bitmap.height}")
                        continuation.resume(Result.success(bitmap))
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert captured ImageProxy to Bitmap", e)
                        Timber.e(e, "LiveCameraSource: Failed to convert image")
                        continuation.resume(Result.failure(e))
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed", exception)
                    Timber.e(exception, "LiveCameraSource: Failed to capture image")
                    continuation.resume(
                        Result.failure(
                            Exception("Failed to capture image: ${exception.message}", exception)
                        )
                    )
                }
            }
        )

        continuation.invokeOnCancellation {
            Log.w(TAG, "takePicture() was cancelled")
            Timber.w("LiveCameraSource: takePicture() was cancelled")
        }
    }

    override fun stopPreview() {
        Timber.d("LiveCameraSource: Stopping live preview")
        if (isPreviewActive) {
            // v2.8.0 Phase 1: Stop GuidelineAnalyzer before clearing
            (previewStreamAnalyzer as? GuidelineAnalyzer)?.stop()
            if (::imageAnalysis.isInitialized) {
                imageAnalysis.clearAnalyzer()
            }
            previewStreamAnalyzer = null  // v2.6.1: Clear persistent analyzer
            isPreviewActive = false
            Timber.i("LiveCameraSource: Preview stopped")
        }
    }

    override suspend fun setZoomRatio(ratio: Float): Result<Unit> {
        return try {
            if (!::camera.isInitialized) {
                Log.w(TAG, "Camera not initialized - cannot set zoom")
                return Result.failure(IllegalStateException("Camera not initialized"))
            }

            val cameraInfo = camera.cameraInfo
            val cameraControl = camera.cameraControl

            val zoomState = cameraInfo.zoomState.value
            if (zoomState != null) {
                val clampedRatio = ratio.coerceIn(
                    zoomState.minZoomRatio,
                    zoomState.maxZoomRatio
                )
                cameraControl.setZoomRatio(clampedRatio)
                Log.d(TAG, "Zoom ratio set to $clampedRatio")
                Timber.d("LiveCameraSource: Zoom ratio set to $clampedRatio")
                Result.success(Unit)
            } else {
                Log.w(TAG, "Zoom state not available")
                Timber.w("LiveCameraSource: Zoom state not available")
                Result.failure(Exception("Zoom state not available"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set zoom ratio", e)
            Timber.e(e, "LiveCameraSource: Failed to set zoom ratio")
            Result.failure(e)
        }
    }

    /**
     * Bind camera to lifecycle using ProcessCameraProvider
     *
     * v2.6.1: Properly binds Preview + ImageAnalysis + ImageCapture UseCases
     * Preview UseCase is MANDATORY for camera to start on real devices
     * Reference: chat_logs/CameraX_StaticImage_TestMode_Fix.md
     */
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        try {
            currentLifecycleOwner = lifecycleOwner
            Log.i(TAG, "bindToLifecycle: Initializing ProcessCameraProvider")

            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                    Log.d(TAG, "ProcessCameraProvider obtained successfully")

                    // 1. Build Preview UseCase with SurfaceProvider
                    preview = Preview.Builder()
                        .setTargetResolution(Size(640, 480))
                        .build()
                        .apply {
                            // ✅ CRITICAL: Attach custom SurfaceProvider
                            // This gives CameraX a valid Surface to render preview
                            setSurfaceProvider(customSurfaceProvider)
                            Log.i(TAG, "✅ Preview UseCase created with custom SurfaceProvider")
                        }

                    // 2. Build ImageAnalysis UseCase (for Bitmap stream to UI)
                    imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(640, 480))
                        .setTargetRotation(Surface.ROTATION_0)  // Set target rotation for proper orientation
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            Log.i(TAG, "✅ ImageAnalysis UseCase created with ROTATION_0")
                        }

                    // 3. Build ImageCapture UseCase (for photo capture)
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(Surface.ROTATION_0)
                        .build()
                        .apply {
                            Log.i(TAG, "✅ ImageCapture UseCase created")
                        }

                    // 4. Unbind all previous UseCases before rebinding
                    Log.d(TAG, "Rebinding camera - selector: ${if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) "BACK" else "FRONT"}")
                    try {
                        // v2.8.0 Phase 1: Stop GuidelineAnalyzer before unbinding
                        (previewStreamAnalyzer as? GuidelineAnalyzer)?.stop()
                        cameraProvider.unbindAll()
                        Log.d(TAG, "Previous UseCases unbound successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to unbind previous UseCases: ${e.message}", e)
                        // 繼續執行 bind，可能是首次綁定
                    }

                    // 5. ✅ CRITICAL: Bind all three UseCases together
                    // Preview MUST be included or camera won't start on real devices
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,        // ✅ REQUIRED: Provides Surface for camera
                        imageAnalysis,  // ✅ Provides Bitmap stream for UI
                        imageCapture    // ✅ Provides photo capture capability
                    )

                    Log.i(TAG, "✅ Camera bound successfully: Preview + ImageAnalysis + ImageCapture")
                    Log.i(TAG, "Camera should now start on real device")
                    Timber.i("LiveCameraSource: Camera bound to lifecycle")

                    // v2.6.1 FIX: Reattach analyzer after rebind (critical for switchCamera)
                    previewStreamAnalyzer?.let { analyzer ->
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            analyzer
                        )
                        Log.i(TAG, "✅ Preview stream analyzer reattached after rebind - frames should flow")
                    } ?: Log.w(TAG, "⚠️ No previewStreamAnalyzer to attach")

                    // v2.6.2 FIX: Reapply pending exposure compensation after rebind (critical for HDR persistence)
                    pendingExposureIndex?.let { exposureValue ->
                        camera.cameraControl.setExposureCompensationIndex(exposureValue)
                        Log.i(TAG, "✅ Pending HDR exposure compensation reapplied: $exposureValue")
                    } ?: Log.d(TAG, "No pending HDR exposure compensation to apply")

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to bind camera UseCases", e)
                    Timber.e(e, "LiveCameraSource: Failed to bind camera to lifecycle")
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get ProcessCameraProvider", e)
            Timber.e(e, "LiveCameraSource: Failed to initialize camera")
        }
    }

    /**
     * Unbind camera from lifecycle and release resources
     */
    fun unbind() {
        try {
            if (::cameraProvider.isInitialized) {
                cameraProvider.unbindAll()
                Log.i(TAG, "Camera unbound from lifecycle")
                Timber.i("LiveCameraSource: Camera unbound from lifecycle")
            }
            currentLifecycleOwner = null

            // Release SurfaceTexture resources
            previewSurface?.release()
            previewSurfaceTexture?.release()
            previewSurface = null
            previewSurfaceTexture = null
            Log.d(TAG, "Preview Surface resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind camera", e)
            Timber.e(e, "LiveCameraSource: Failed to unbind camera")
        }
    }

    /**
     * Switch between front and back camera
     *
     * v2.6.1: Changed to suspend function and executes on Main thread
     * to prevent "Not in application's main thread" error
     *
     * v2.8.0 Phase 1: Added Mutex to prevent race conditions during fast consecutive calls
     */
    override suspend fun switchCamera() {
        bindMutex.withLock {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            Log.i(TAG, "Camera selector switched to: ${if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) "BACK" else "FRONT"}")

            // Re-bind camera with new selector on Main thread
            // CameraX operations MUST run on main thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                currentLifecycleOwner?.let { bindToLifecycle(it) }
            }
        }
    }

    /**
     * Get current camera selector
     */
    fun getCurrentCameraSelector(): CameraSelector = cameraSelector

    /**
     * Set camera selector (front or back)
     */
    fun setCameraSelector(selector: CameraSelector) {
        if (cameraSelector != selector) {
            cameraSelector = selector
            Log.i(TAG, "Camera selector changed - rebinding camera")
            currentLifecycleOwner?.let { bindToLifecycle(it) }
        }
    }

    /**
     * Set exposure compensation (for HDR mode simulation)
     */
    fun setExposureCompensation(value: Int) {
        if (::camera.isInitialized) {
            try {
                camera.cameraControl.setExposureCompensationIndex(value)
                Log.i(TAG, "Exposure compensation set to $value")
                Timber.d("LiveCameraSource: Exposure compensation set to $value")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set exposure compensation", e)
                Timber.e(e, "LiveCameraSource: Failed to set exposure")
            }
        } else {
            Log.w(TAG, "Camera not initialized - cannot set exposure compensation")
        }
    }

    /**
     * Set flash mode for image capture
     */
    fun setFlashMode(@ImageCapture.FlashMode flashMode: Int) {
        if (::imageCapture.isInitialized) {
            imageCapture.flashMode = flashMode
            Log.i(TAG, "Flash mode set to $flashMode")
            Timber.d("LiveCameraSource: Flash mode set to $flashMode")
        } else {
            Log.w(TAG, "ImageCapture not initialized - cannot set flash mode")
        }
    }

    /**
     * Get current flash mode
     */
    fun getFlashMode(): Int {
        return if (::imageCapture.isInitialized) {
            imageCapture.flashMode
        } else {
            ImageCapture.FLASH_MODE_AUTO
        }
    }

    /**
     * Rotate and optionally mirror a bitmap
     *
     * v2.6.1: Added mirror parameter for front camera selfie view
     *
     * @param source Source bitmap to transform
     * @param degrees Rotation angle (0, 90, 180, 270)
     * @param mirror Whether to mirror horizontally (for front camera)
     * @return Transformed bitmap
     */
    private fun rotateBitmap(source: Bitmap, degrees: Int, mirror: Boolean = false): Bitmap {
        if (degrees == 0 && !mirror) return source

        val matrix = android.graphics.Matrix()

        // For front camera: rotate first, then mirror
        // This prevents upside-down issue
        if (degrees != 0) {
            matrix.postRotate(degrees.toFloat())
        }

        // Apply mirroring after rotation (for front camera)
        if (mirror) {
            matrix.postScale(-1f, 1f)
        }

        val transformed = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )

        // Recycle the source bitmap if it's different from the transformed one
        if (transformed != source) {
            source.recycle()
        }

        val operation = when {
            mirror && degrees != 0 -> "rotated $degrees° and mirrored"
            mirror -> "mirrored"
            else -> "rotated $degrees°"
        }
        Log.d(TAG, "Transformed bitmap from ${source.width}x${source.height} ($operation) to ${transformed.width}x${transformed.height}")
        return transformed
    }

    /**
     * Enable HDR mode via exposure compensation
     *
     * v2.6.2: Implements CameraInputSource.enableHDR()
     *
     * Uses persistent queueing mechanism similar to previewStreamAnalyzer:
     * - If camera is initialized: apply immediately and store for rebind
     * - If camera not initialized: queue for later application
     * - Automatically reapplied after camera rebind (e.g., switchCamera)
     *
     * @return Result indicating success or failure
     */
    override fun enableHDR(): Result<Unit> {
        return try {
            if (::camera.isInitialized) {
                // Camera is ready: apply immediately
                val exposureState = camera.cameraInfo.exposureState
                val range = exposureState.exposureCompensationRange
                val targetValue = 1.coerceIn(range.lower, range.upper)

                camera.cameraControl.setExposureCompensationIndex(targetValue)
                pendingExposureIndex = targetValue  // Store for rebind scenarios

                Log.i(TAG, "HDR enabled: exposure compensation set to $targetValue (range: ${range.lower}..${range.upper})")
                Result.success(Unit)
            } else {
                // Camera not ready: queue for later application
                pendingExposureIndex = 1
                Log.i(TAG, "HDR enable queued: will be applied after camera binds")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable HDR", e)
            Result.failure(e)
        }
    }

    /**
     * Disable HDR mode by resetting exposure compensation
     *
     * v2.6.2: Implements CameraInputSource.disableHDR()
     *
     * Uses persistent queueing mechanism similar to previewStreamAnalyzer:
     * - If camera is initialized: apply immediately and store for rebind
     * - If camera not initialized: queue for later application
     * - Automatically reapplied after camera rebind (e.g., switchCamera)
     *
     * @return Result indicating success or failure
     */
    override fun disableHDR(): Result<Unit> {
        return try {
            if (::camera.isInitialized) {
                // Camera is ready: apply immediately
                camera.cameraControl.setExposureCompensationIndex(0)
                pendingExposureIndex = 0  // Store for rebind scenarios

                Log.i(TAG, "HDR disabled: exposure compensation reset to 0")
                Result.success(Unit)
            } else {
                // Camera not ready: queue for later application
                pendingExposureIndex = 0
                Log.i(TAG, "HDR disable queued: will be applied after camera binds")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable HDR", e)
            Result.failure(e)
        }
    }

    // v2.5.2 Phase 7: getCameraController() removed - all camera operations now abstracted
}

/**
 * Extension function to convert ImageProxy to Bitmap
 *
 * Optimized direct YUV → RGB conversion without JPEG intermediate step.
 *
 * Performance improvements (v2.5.1 Priority 3):
 * - Before: YUV → NV21 → JPEG → ByteArray → Bitmap (~180 MB/s at 30 FPS)
 * - After: YUV → RGB pixels → Bitmap (~60 MB/s at 30 FPS, 3x reduction)
 * - Memory savings: ~50% less temporary allocations
 * - Speed improvement: ~2-3x faster
 *
 * Uses ITU-R BT.601 conversion formula:
 * - R = Y + 1.402 * (V - 128)
 * - G = Y - 0.344 * (U - 128) - 0.714 * (V - 128)
 * - B = Y + 1.772 * (U - 128)
 */
@SuppressLint("UnsafeOptInUsageError")
private fun ImageProxy.toBitmap(): Bitmap {
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    // Reset buffer positions
    yBuffer.rewind()
    uBuffer.rewind()
    vBuffer.rewind()

    val yRowStride = yPlane.rowStride
    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(width * height)

    var pixelIndex = 0
    for (y in 0 until height) {
        val yRowOffset = y * yRowStride
        val uvRowOffset = (y / 2) * uvRowStride

        for (x in 0 until width) {
            // Get Y value
            val yValue = (yBuffer.get(yRowOffset + x).toInt() and 0xFF)

            // Get UV values (subsampled 2x2)
            val uvPixelIndex = uvRowOffset + (x / 2) * uvPixelStride
            val uValue = (uBuffer.get(uvPixelIndex).toInt() and 0xFF) - 128
            val vValue = (vBuffer.get(uvPixelIndex).toInt() and 0xFF) - 128

            // YUV → RGB conversion (ITU-R BT.601)
            val r = (yValue + 1.402f * vValue).toInt().coerceIn(0, 255)
            val g = (yValue - 0.344f * uValue - 0.714f * vValue).toInt().coerceIn(0, 255)
            val b = (yValue + 1.772f * uValue).toInt().coerceIn(0, 255)

            // Pack ARGB into int (0xAARRGGBB)
            pixels[pixelIndex++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}

/**
 * Legacy JPEG-based conversion (kept for reference)
 *
 * @deprecated Use direct YUV → RGB conversion above for better performance
 */
@Suppress("unused")
@SuppressLint("UnsafeOptInUsageError")
private fun ImageProxy.toBitmapLegacy(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = android.graphics.YuvImage(
        nv21,
        android.graphics.ImageFormat.NV21,
        width,
        height,
        null
    )

    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(
        android.graphics.Rect(0, 0, width, height),
        100,
        out
    )
    val imageBytes = out.toByteArray()

    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        ?: throw IllegalStateException("Failed to decode bitmap from ImageProxy")
}
