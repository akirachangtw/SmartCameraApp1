package com.akirachang.ai.photographer.data.source.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.graphics.SurfaceTexture
import android.os.PowerManager
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import android.util.Rational
import androidx.lifecycle.LifecycleOwner
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.data.source.analysis.GuidelineAnalyzer
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
import com.akirachang.ai.photographer.util.BitmapPool
import com.akirachang.ai.photographer.util.RenderScriptYuvConverter
import com.akirachang.ai.photographer.util.YuvConverter
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import java.util.concurrent.Executors
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import kotlin.math.ceil
// v2.11.4-01: Camera2 interop for AE/AF lock parameter verification (DEBUG only)
// v2.11.4-02: Camera2CameraControl + CaptureRequestOptions for true AE lock
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop

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
    @ApplicationContext private val context: Context,
    private val renderScriptYuvConverter: RenderScriptYuvConverter
) : CameraInputSource {

    companion object {
        private const val TAG = "LiveCameraSource"
        // v2.11.4-01: AE/AF debug logging throttle interval (1 log per second)
        private const val AEAF_DEBUG_LOG_INTERVAL_MS = 1000L
    }

    // v2.11.4-01: Flag to enable Camera2 AE/AF state logging (toggled by lock/unlock)
    @Volatile
    private var logAeAfState = false
    private var lastAeAfLogTime = 0L

    // v2.9.0-alpha Phase 0: Thermal monitoring
    private val powerManager: PowerManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        } else {
            null
        }
    }

    private var lastThermalStatus: Int? = null
    private var thermalThrottlingStartTime: Long? = null

    // v2.8.6 Phase 0: SurfaceTexture lifecycle tracking for Graphics memory investigation
    private var surfaceTextureCreateCount = 0
    private var surfaceTextureReleaseCount = 0

    // v2.8.6 Phase 2: External surface provider for PreviewView integration
    private var externalSurfaceProvider: Preview.SurfaceProvider? = null

    // v2.11.1 P0-1: Store PreviewView reference for accurate metering point creation
    private var previewView: androidx.camera.view.PreviewView? = null

    // v2.9.0-beta3: Track selected resolution for diagnostics
    private var selectedResolution: Size? = null

    // v2.10.0: Track current aspect ratio mode for ViewPort configuration
    private var currentAspectRatioMode: AspectRatioMode = AspectRatioMode.RATIO_16_9

    // v2.6.1: Custom SurfaceProvider for Preview UseCase (Fallback when no PreviewView)
    // Preview UseCase is mandatory - without it, camera won't start on real devices
    // Reference: chat_logs/CameraX_StaticImage_TestMode_Fix.md lines 37-54
    //
    // Using SurfaceTexture approach instead of PreviewView to avoid WindowManager token issues
    private var previewSurfaceTexture: SurfaceTexture? = null
    private var previewSurface: Surface? = null

    private val fallbackSurfaceProvider = Preview.SurfaceProvider { request ->
        Log.d(TAG, "[Fallback] SurfaceProvider requested: ${request.resolution.width}x${request.resolution.height}")

        // v2.9.0-beta4: Log warning if Preview resolution doesn't match expected
        selectedResolution?.let { expected ->
            val withinFilter = request.resolution.width <= 800 && request.resolution.height <= 800
            if (request.resolution.width != expected.width || request.resolution.height != expected.height) {
                Log.w(TAG, "[v2.9.0-beta4] Preview resolution mismatch! " +
                          "Requested=${request.resolution.width}x${request.resolution.height}, " +
                          "Expected=${expected.width}x${expected.height}, " +
                          "WithinFilter(<= 800px): $withinFilter")
            }
        }

        // Create SurfaceTexture with requested resolution
        val surfaceTexture = SurfaceTexture(0).apply {
            setDefaultBufferSize(
                request.resolution.width,
                request.resolution.height
            )
        }
        previewSurfaceTexture = surfaceTexture

        // v2.8.6 Phase 0: Track SurfaceTexture creation
        surfaceTextureCreateCount++
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[SurfaceTexture] Created #$surfaceTextureCreateCount | " +
                    "HashCode: ${System.identityHashCode(surfaceTexture)} | " +
                    "Leaked: ${surfaceTextureCreateCount - surfaceTextureReleaseCount}")
        }

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

            // v2.8.6 Phase 0: Track SurfaceTexture release
            surfaceTextureReleaseCount++
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "[SurfaceTexture] Released #$surfaceTextureReleaseCount | " +
                        "HashCode: ${System.identityHashCode(surfaceTexture)} | " +
                        "Leaked: ${surfaceTextureCreateCount - surfaceTextureReleaseCount}")
            }
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

    // v2.8.4 Quick Win: FPS metrics for UI overlay
    private val _currentFps = MutableStateFlow<Float?>(null)
    private val currentFps: StateFlow<Float?> = _currentFps.asStateFlow()

    // v2.8.0 Phase 1: Mutex to prevent race conditions during camera switching
    private val bindMutex = Mutex()

    // v2.8.1: Move analyzer work off UI thread
    private val analysisExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "LiveCameraSourceAnalyzer").apply { isDaemon = true }
    }

    // v2.8.1: Bitmap reuse to reduce per-frame allocations
    private val bitmapPool = BitmapPool(maxEntriesPerKey = 4)
    private val inFlightPreviewBuffers = ArrayDeque<BitmapPool.BitmapBuffer>(4)

    // v2.8.1 Phase B-1: Integer-based YUV converter (2-3x faster than float math)
    private val yuvConverter = YuvConverter()

    // v2.8.2 Phase C: Prefer RenderScript, fallback to CPU on failure
    private var useRenderScript = BuildConfig.ENABLE_RENDERSCRIPT.also {
        Log.d(TAG, "[v2.8.2 Init] BuildConfig.ENABLE_RENDERSCRIPT=$it, useRenderScript=$it")
    }

    // v2.8.3 Phase D: Reusable Paint object to avoid per-frame allocation
    private val transformPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    // v2.9.6 Phase 1: Orientation tracking for dynamic rotation
    private var orientationListener: OrientationEventListener? = null
    private var currentRotation: Int = Surface.ROTATION_0
    private var orientationListenerTriggered = false  // v2.9.6 Phase 1 Fix: Track if listener is working

    // v2.9.6 Phase 1 Fix: Store rotation from ImageProxy for EXIF
    private var lastCaptureRotationDegrees: Int = 0

    /**
     * Get FPS performance metrics stream
     *
     * v2.8.4 Quick Win: Real-time FPS monitoring for performance validation
     * @return StateFlow that emits current FPS
     */
    override fun getFpsStream(): StateFlow<Float?> = currentFps

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

                // v2.9.0-beta4: Log actual ImageProxy size on first frame (resolution verification)
                if (frameCount == 1L) {
                    val expected = selectedResolution
                    val actual = Size(imageProxy.width, imageProxy.height)
                    val match = expected?.let { it.width == actual.width && it.height == actual.height } ?: false
                    val withinFilter = actual.width <= 800 && actual.height <= 800
                    Log.i(TAG, "[v2.9.0-beta4 Resolution] ImageProxy actual size: ${actual.width}x${actual.height}, " +
                              "Expected: ${expected?.width}x${expected?.height}, " +
                              "Match: $match, WithinFilter(<= 800px): $withinFilter")
                }

                // v2.8.1 Phase A1: Diagnostic logging (every 120 frames = ~4 seconds at 30fps)
                if (frameCount % 120 == 1L) {
                    val thread = Thread.currentThread()
                    Log.i(TAG, "[v2.8.1 Diagnostic] Thread=${thread.name}, Priority=${thread.priority}, " +
                               "Frame=$frameCount, PoolSize=${bitmapPool.getCurrentPoolSize()}")
                }

                // Convert ImageProxy to Bitmap
                val startYuv = System.nanoTime()
                val rawBuffer = bitmapPool.acquire(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
                var yuvMode = "CPU"
                if (frameCount % 30 == 1L) {
                    Log.d(TAG, "[v2.8.2 Debug] useRenderScript=$useRenderScript, BuildConfig=${BuildConfig.ENABLE_RENDERSCRIPT}")
                }
                try {
                    if (useRenderScript) {
                        if (frameCount % 30 == 1L) {
                            Log.d(TAG, "[v2.8.2 Debug] About to call RenderScript convert")
                        }
                        renderScriptYuvConverter.convert(imageProxy, rawBuffer.bitmap)
                        yuvMode = "RS"
                    } else {
                        if (frameCount % 30 == 1L) {
                            Log.d(TAG, "[v2.8.2 Debug] Using CPU converter instead")
                        }
                        yuvConverter.convertToBitmapBuffer(imageProxy, rawBuffer)
                    }
                } catch (e: Exception) {
                    if (useRenderScript) {
                        useRenderScript = false
                        Log.w(TAG, "[v2.8.2] RenderScript failed; fallback to CPU converter", e)
                        yuvConverter.convertToBitmapBuffer(imageProxy, rawBuffer)
                        yuvMode = "CPU"
                    } else {
                        throw e
                    }
                }
                val yuvTime = (System.nanoTime() - startYuv) / 1_000_000.0

                // Get rotation from ImageProxy metadata
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                // Check if using front camera (needs mirroring for selfie view)
                val isFrontCamera = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                // v2.9.6 Phase 2: Fix preview rotation issue
                // Problem: When ImageAnalysis targetRotation is dynamically updated (line 607),
                // rotationDegrees changes unexpectedly (90° → 0° → 180°), causing double rotation.
                // Solution: Use fixed rotation based on sensor orientation for portrait-locked UI
                // Back camera: 90° CW (sensor is landscape, rotate to portrait)
                // Front camera: 270° CW (90° CCW, sensor orientation differs from back)
                val compensatedRotation = if (isFrontCamera) 270 else 90

                // v2.9.6 Phase 2: Diagnostic log for preview rotation issue
                if (frameCount % 30 == 0L) {
                    Log.d(TAG, "[Rotation Debug] currentRotation=$currentRotation, " +
                        "rotationDegrees=$rotationDegrees, " +
                        "compensatedRotation=$compensatedRotation, " +
                        "isFront=$isFrontCamera, " +
                        "frame=${rawBuffer.bitmap.width}x${rawBuffer.bitmap.height}")
                }

                // Rotate and mirror bitmap if needed to match display orientation
                val startTransform = System.nanoTime()
                val outputBuffer = if (compensatedRotation != 0 || isFrontCamera) {
                    transformBitmap(rawBuffer, compensatedRotation, isFrontCamera)
                } else {
                    rawBuffer
                }
                val transformTime = (System.nanoTime() - startTransform) / 1_000_000.0

                // v2.8.2 Phase C: Timing log (every 30 frames = ~1 second at 30fps)
                if (frameCount % 30 == 0L) {
                    Log.d(
                        TAG,
                        "[v2.8.4 Timing] YUV=${"%.1f".format(yuvTime)}ms($yuvMode), " +
                            "Transform=${"%.1f".format(transformTime)}ms, " +
                            "InFlight=${inFlightPreviewBuffers.size}"
                    )
                    // v2.8.4 Quick Win: Enhanced pool diagnostics
                    Log.d(
                        TAG,
                        "[v2.8.4 Pool] In=${rawBuffer.bitmap.width}x${rawBuffer.bitmap.height}, " +
                            "Out=${outputBuffer.bitmap.width}x${outputBuffer.bitmap.height}, " +
                            "Rot=${rotationDegrees}°, Mirror=$isFrontCamera, " +
                            "Keys=${bitmapPool.getKeyCount()}, HitRate=${bitmapPool.getHitRate()}%"
                    )
                }

                if (outputBuffer !== rawBuffer) {
                    bitmapPool.release(rawBuffer)
                }

                val sendResult = trySend(outputBuffer.bitmap)
                if (sendResult.isSuccess) {
                    Log.v(
                        TAG,
                        "Preview frame sent: ${outputBuffer.bitmap.width}x${outputBuffer.bitmap.height}, rotation: $rotationDegrees°"
                    )
                    inFlightPreviewBuffers.addLast(outputBuffer)
                    while (inFlightPreviewBuffers.size > 3) {
                        bitmapPool.release(inFlightPreviewBuffers.removeFirst())
                    }
                } else {
                    // v2.8.0 Phase 0: Count dropped frames
                    droppedFrameCount++
                    Log.w(TAG, "Failed to send preview frame - channel might be closed")
                    bitmapPool.release(outputBuffer)
                }

                // v2.8.0 Phase 0: Report FPS + Memory every N frames
                if (frameCount % fpsReportInterval == 0L) {
                    val currentTime = System.currentTimeMillis()
                    val elapsed = currentTime - lastFpsReportTime
                    if (elapsed > 0) {
                        val fps = (fpsReportInterval * 1000f) / elapsed
                        val dropRate = (droppedFrameCount * 100f) / frameCount

                        // v2.8.4 Quick Win: Update FPS StateFlow for UI overlay
                        _currentFps.value = fps

                        // Memory monitoring
                        val memoryInfo = android.os.Debug.MemoryInfo()
                        android.os.Debug.getMemoryInfo(memoryInfo)
                        val nativeMB = memoryInfo.nativePss / 1024  // KB to MB
                        val javaMB = memoryInfo.dalvikPss / 1024    // KB to MB
                        val totalMB = memoryInfo.totalPss / 1024    // KB to MB

                        // v2.9.0-alpha Phase 0: Monitor thermal status
                        val thermalInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
                            val thermalStatus = powerManager!!.currentThermalStatus
                            val thermalStatusName = getThermalStatusName(thermalStatus)

                            // Track thermal throttling events
                            // Track thermal throttling events (MODERATE or higher = throttling)
                            val throttlingThreshold = PowerManager.THERMAL_STATUS_MODERATE
                            if (thermalStatus != lastThermalStatus) {
                                if (thermalStatus >= throttlingThreshold && (lastThermalStatus == null || lastThermalStatus!! < throttlingThreshold)) {
                                    // Throttling started
                                    thermalThrottlingStartTime = currentTime
                                    Log.w(TAG, "⚠️ Thermal throttling started: $thermalStatusName (FPS: ${"%.1f".format(fps)})")
                                } else if (lastThermalStatus != null && lastThermalStatus!! >= throttlingThreshold && thermalStatus < throttlingThreshold) {
                                    // Throttling ended
                                    val throttlingDuration = thermalThrottlingStartTime?.let { (currentTime - it) / 1000 } ?: 0
                                    Log.i(TAG, "✅ Thermal throttling ended: $thermalStatusName (Duration: ${throttlingDuration}s, FPS: ${"%.1f".format(fps)})")
                                    thermalThrottlingStartTime = null
                                }
                                lastThermalStatus = thermalStatus
                            }

                            ", Thermal=$thermalStatusName"
                        } else {
                            ""
                        }

                        Log.i(TAG, "Performance: FPS=${"%.1f".format(fps)}, " +
                                   "Dropped=${droppedFrameCount}/$frameCount (${"%.1f".format(dropRate)}%), " +
                                   "Memory: Native=${nativeMB}MB, Java=${javaMB}MB, Total=${totalMB}MB$thermalInfo")
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
                analysisExecutor,
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
            while (inFlightPreviewBuffers.isNotEmpty()) {
                bitmapPool.release(inFlightPreviewBuffers.removeFirst())
            }
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

                    // v2.9.6 Phase 1 Fix: Store ImageProxy rotation for EXIF
                    lastCaptureRotationDegrees = image.imageInfo.rotationDegrees

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
     * Focus on a specific point in the preview
     * v2.9.9: Tap-to-Focus implementation using CameraX FocusMeteringAction
     * v2.11.1 P0-1: Use PreviewView.meteringPointFactory for accurate coordinate mapping
     *
     * @param x X coordinate in the preview view (0.0 to viewWidth)
     * @param y Y coordinate in the preview view (0.0 to viewHeight)
     * @param viewWidth Width of the preview view in pixels
     * @param viewHeight Height of the preview view in pixels
     * @return Result indicating success or failure
     */
    override suspend fun focusOnPoint(
        x: Float,
        y: Float,
        viewWidth: Int,
        viewHeight: Int
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            if (!::camera.isInitialized) {
                Log.w(TAG, "Camera not initialized - cannot focus")
                continuation.resume(Result.failure(IllegalStateException("Camera not initialized")))
                return@suspendCancellableCoroutine
            }

            val cameraControl = camera.cameraControl

            // v2.11.1 P0-1: Use PreviewView.meteringPointFactory if available
            // This provides accurate coordinate mapping that respects FIT_CENTER letterboxing
            val focusPoint = if (previewView != null) {
                // ✅ FIX Issue #1: Check if tap is within content bounds (exclude letterbox)
                // PreviewView.meteringPointFactory automatically handles this validation
                val factory = previewView!!.meteringPointFactory
                factory.createPoint(x, y)
            } else {
                // Fallback to SurfaceOrientedMeteringPointFactory (legacy behavior)
                // ⚠️ WARNING: This does NOT account for FIT_CENTER letterboxing
                Log.w(TAG, "[v2.11.1 P0-1] PreviewView not set - using legacy factory (letterbox not handled)")
                val factory = androidx.camera.core.SurfaceOrientedMeteringPointFactory(
                    viewWidth.toFloat(),
                    viewHeight.toFloat()
                )
                factory.createPoint(x, y)
            }

            // Build focus metering action
            val action = androidx.camera.core.FocusMeteringAction.Builder(
                focusPoint,
                androidx.camera.core.FocusMeteringAction.FLAG_AF
            ).apply {
                // Auto-cancel after 5 seconds
                setAutoCancelDuration(5, java.util.concurrent.TimeUnit.SECONDS)
            }.build()

            // Start focus and metering
            val future = cameraControl.startFocusAndMetering(action)

            // v2.9.10: Use addListener for non-blocking coroutine approach (fixes UI freeze)
            future.addListener({
                try {
                    val result = future.get()  // Called in Executor thread, doesn't block main thread
                    if (result.isFocusSuccessful) {
                        Log.d(TAG, "[v2.11.1] Focus successful at ($x, $y) - factory: ${if (previewView != null) "PreviewView" else "SurfaceOriented"}")
                        Timber.d("LiveCameraSource: Focus successful at ($x, $y)")
                        continuation.resume(Result.success(Unit))
                    } else {
                        Log.w(TAG, "[v2.11.1] Focus failed at ($x, $y) - factory: ${if (previewView != null) "PreviewView" else "SurfaceOriented"}")
                        Timber.w("LiveCameraSource: Focus failed at ($x, $y)")
                        continuation.resume(Result.failure(Exception("Focus failed")))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Focus error", e)
                    Timber.e(e, "LiveCameraSource: Focus error")
                    continuation.resume(Result.failure(e))
                }
            }, ContextCompat.getMainExecutor(context))

            // Support coroutine cancellation
            continuation.invokeOnCancellation {
                Log.w(TAG, "Focus operation cancelled")
                // CameraX future auto-cleans, no manual cancellation needed
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to focus on point", e)
            Timber.e(e, "LiveCameraSource: Failed to focus on point")
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Lock AE/AF at a specific point (long press)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     * v2.11.1 P0-1: Use PreviewView.meteringPointFactory for accurate coordinate mapping
     *
     * Creates a persistent focus metering action with FLAG_AF | FLAG_AE without auto-cancel.
     * Use cancelFocusAndMetering() to unlock and return to auto mode.
     */
    override suspend fun lockAEAF(
        x: Float,
        y: Float,
        viewWidth: Int,
        viewHeight: Int
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            if (!::camera.isInitialized) {
                Log.w(TAG, "Camera not initialized - cannot lock AE/AF")
                continuation.resume(Result.failure(IllegalStateException("Camera not initialized")))
                return@suspendCancellableCoroutine
            }

            val cameraControl = camera.cameraControl

            // v2.11.1 P0-1: Use PreviewView.meteringPointFactory if available
            val lockPoint = if (previewView != null) {
                // ✅ FIX Issue #1: PreviewView.meteringPointFactory handles letterbox correctly
                val factory = previewView!!.meteringPointFactory
                factory.createPoint(x, y)
            } else {
                // Fallback to SurfaceOrientedMeteringPointFactory (legacy behavior)
                Log.w(TAG, "[v2.11.1 P0-1] PreviewView not set - using legacy factory for AE/AF lock")
                val factory = androidx.camera.core.SurfaceOrientedMeteringPointFactory(
                    viewWidth.toFloat(),
                    viewHeight.toFloat()
                )
                factory.createPoint(x, y)
            }

            // Build focus metering action with AE and AF lock
            val action = androidx.camera.core.FocusMeteringAction.Builder(
                lockPoint,
                androidx.camera.core.FocusMeteringAction.FLAG_AF or
                        androidx.camera.core.FocusMeteringAction.FLAG_AE
            ).apply {
                // v2.9.9 Day 1-2: NO auto-cancel - persistent lock until manually unlocked
                // (do not call setAutoCancelDuration)
            }.build()

            // Start focus and metering (persistent lock)
            val future = cameraControl.startFocusAndMetering(action)

            // Non-blocking coroutine approach (same as focusOnPoint)
            future.addListener({
                try {
                    val result = future.get()  // Called in Executor thread
                    // v2.11.2 fix Bug B: AE/AF lock is applied regardless of AF convergence quality.
                    // isFocusSuccessful indicates AF convergence, NOT whether the lock was applied.
                    // Always resume success so ViewModel triggers 3-phase animation + yellow indicator.
                    Log.i(TAG, "[v2.11.2] AE/AF lock applied at ($x, $y) - isFocusSuccessful=${result.isFocusSuccessful}, factory=${if (previewView != null) "PreviewView" else "SurfaceOriented"}")
                    Timber.i("LiveCameraSource: AE/AF lock applied at ($x, $y), isFocusSuccessful=${result.isFocusSuccessful}")
                    // v2.11.4-02: Set CONTROL_AE_LOCK=true via Camera2CameraControl.
                    // FocusMeteringAction.FLAG_AE only sets metering region, NOT AE_LOCK parameter.
                    // Must explicitly set CONTROL_AE_LOCK=true to freeze ISO + exposure time.
                    try {
                        @OptIn(ExperimentalCamera2Interop::class)
                        Camera2CameraControl.from(cameraControl)
                            .addCaptureRequestOptions(
                                CaptureRequestOptions.Builder()
                                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, true)
                                    .setCaptureRequestOption(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_OFF
                                    )
                                    .build()
                            )
                        Log.i(TAG, "[v2.11.4-03] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF applied (true AE+AF lock)")
                    } catch (e: Exception) {
                        Log.w(TAG, "[v2.11.4-03] Failed to apply AE/AF lock options: ${e.message}")
                    }
                    // v2.11.4-01: Start Camera2 AE/AF state logging (DEBUG only)
                    if (BuildConfig.DEBUG) {
                        logAeAfState = true
                        Log.i(TAG, "[AE/AF Debug] Logging ENABLED — will log AF_STATE, AE_STATE, ISO, exposure, focusDist every 1s")
                    }
                    continuation.resume(Result.success(Unit))
                } catch (e: Exception) {
                    Log.e(TAG, "AE/AF lock error", e)
                    Timber.e(e, "LiveCameraSource: AE/AF lock error")
                    continuation.resume(Result.failure(e))
                }
            }, ContextCompat.getMainExecutor(context))

            // Support coroutine cancellation
            continuation.invokeOnCancellation {
                Log.w(TAG, "AE/AF lock operation cancelled")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock AE/AF", e)
            Timber.e(e, "LiveCameraSource: Failed to lock AE/AF")
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Cancel focus and metering (unlock AE/AF)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * Cancels any active focus metering action and returns to auto mode.
     * Used to unlock AE/AF after lockAEAF().
     */
    override suspend fun cancelFocusAndMetering(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            if (!::camera.isInitialized) {
                Log.w(TAG, "Camera not initialized - cannot cancel focus/metering")
                continuation.resume(Result.failure(IllegalStateException("Camera not initialized")))
                return@suspendCancellableCoroutine
            }

            val cameraControl = camera.cameraControl

            // Cancel any active focus metering action
            val future = cameraControl.cancelFocusAndMetering()

            // Non-blocking coroutine approach
            future.addListener({
                try {
                    future.get()  // Wait for cancellation to complete
                    // v2.11.4-02: Clear CONTROL_AE_LOCK to re-enable auto exposure.
                    // clearCaptureRequestOptions() removes all Camera2 interop options (safe:
                    // confirmed no other usages in repo).
                    try {
                        @OptIn(ExperimentalCamera2Interop::class)
                        Camera2CameraControl.from(cameraControl).clearCaptureRequestOptions()
                        Log.i(TAG, "[v2.11.4-03] AE/AF lock cleared — auto exposure + focus restored")
                    } catch (e: Exception) {
                        Log.w(TAG, "[v2.11.4-02] Failed to clear CONTROL_AE_LOCK: ${e.message}")
                    }
                    // v2.11.4-01: Stop Camera2 AE/AF state logging
                    if (BuildConfig.DEBUG) {
                        logAeAfState = false
                        Log.i(TAG, "[AE/AF Debug] Logging DISABLED — returned to auto mode")
                    }
                    Log.i(TAG, "Focus and metering cancelled - returned to auto mode")
                    Timber.i("LiveCameraSource: Focus/metering cancelled")
                    continuation.resume(Result.success(Unit))
                } catch (e: Exception) {
                    Log.e(TAG, "Cancel focus/metering error", e)
                    Timber.e(e, "LiveCameraSource: Cancel focus/metering error")
                    continuation.resume(Result.failure(e))
                }
            }, ContextCompat.getMainExecutor(context))

            // Support coroutine cancellation
            continuation.invokeOnCancellation {
                Log.w(TAG, "Cancel focus/metering operation cancelled")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel focus and metering", e)
            Timber.e(e, "LiveCameraSource: Failed to cancel focus/metering")
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Initialize orientation listener for dynamic rotation tracking
     *
     * v2.9.6 Phase 1: Monitor device physical orientation and update target rotation
     */
    private fun initOrientationListener() {
        // v2.9.6 Phase 1 Fix: Read current device orientation before enabling listener
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = windowManager?.defaultDisplay
        currentRotation = display?.rotation ?: Surface.ROTATION_0
        Log.i(TAG, "Initial device rotation: $currentRotation")

        orientationListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                // v2.9.6 Phase 1 Fix: Mark listener as triggered
                if (!orientationListenerTriggered) {
                    orientationListenerTriggered = true
                    Log.i(TAG, "OrientationEventListener first trigger confirmed")
                }

                val rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270   // Landscape left
                    in 135..224 -> Surface.ROTATION_180  // Upside down
                    in 225..314 -> Surface.ROTATION_90   // Landscape right
                    else -> Surface.ROTATION_0           // Portrait
                }

                if (rotation != currentRotation) {
                    currentRotation = rotation
                    updateTargetRotation(rotation)
                    Log.d(TAG, "Orientation changed: $orientation° → rotation=$rotation")
                }
            }
        }

        if (orientationListener?.canDetectOrientation() == true) {
            orientationListener?.enable()
            Log.i(TAG, "OrientationEventListener enabled")
        } else {
            Log.w(TAG, "OrientationEventListener cannot detect orientation")
        }
    }

    /**
     * Update target rotation for all UseCases
     *
     * v2.9.6 Phase 1: Dynamically adjust rotation as device orientation changes
     */
    private fun updateTargetRotation(rotation: Int) {
        if (::preview.isInitialized) {
            preview.targetRotation = rotation
        }
        if (::imageAnalysis.isInitialized) {
            imageAnalysis.targetRotation = rotation
        }
        if (::imageCapture.isInitialized) {
            imageCapture.targetRotation = rotation
            Log.d(TAG, "ImageCapture targetRotation updated: $rotation")
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

                    // v2.9.0-beta5: Smart Resolution Selection
                    // Automatically detect hardware capabilities and select optimal resolution
                    val cameraInfo = cameraProvider.getCameraInfo(cameraSelector)
                    val supportedSizes = getSupportedResolutions(cameraInfo)

                    // Detect device capabilities and select optimal target
                    val (targetResolution, maxFilterWidth, maxFilterHeight) = selectDeviceOptimalResolution(supportedSizes)

                    Log.i(TAG, "[v2.9.0-beta5] Smart Resolution: target=${targetResolution.width}x${targetResolution.height}, " +
                              "filter≤${maxFilterWidth}x${maxFilterHeight}")

                    // v2.10.0: Unified AspectRatioStrategy for UseCaseGroup + ViewPort architecture
                    // v2.11.1: 1:1 mode uses ResolutionFilter (Traycer Plan A) to prefer true square resolutions.
                    //   Priority: exact square (width==height, nearest to 1920×1920) → fallback to nearest 1:1 aspect
                    //   ViewPort Rational(1,1) + FIT ensures WYSIWYG (Preview = Capture = 1:1 square)
                    //   See: docs/features/v2.10.0/v2.10.0_aspect-ratio-wysiwyg-fix_Traycer.md
                    val commonSelector = if (currentAspectRatioMode == AspectRatioMode.RATIO_1_1) {
                        // 1:1 mode: prefer true square resolution, fallback to nearest 1:1 aspect
                        androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                            .setResolutionFilter { supportedSizes, _ ->
                                val squareSizes = supportedSizes.filter { it.width == it.height }
                                if (squareSizes.isNotEmpty()) {
                                    // Select closest to 1920×1920 square
                                    val selected = squareSizes.minByOrNull {
                                        kotlin.math.abs(it.width - 1920)
                                    } ?: squareSizes.first()
                                    Log.i(TAG, "[v2.11.1] 1:1 ResolutionFilter: true square found → ${selected.width}x${selected.height}")
                                    listOf(selected)
                                } else {
                                    // Fallback: nearest aspect ratio to 1:1
                                    val fallback = supportedSizes.minByOrNull { size ->
                                        kotlin.math.abs(size.width.toFloat() / size.height - 1.0f)
                                    } ?: supportedSizes.first()
                                    Log.w(TAG, "[v2.11.1] 1:1 ResolutionFilter: no square found, fallback → ${fallback.width}x${fallback.height} (ratio=${fallback.width.toFloat()/fallback.height})")
                                    listOf(fallback)
                                }
                            }
                            .build()
                    } else {
                        // 4:3 / 16:9 modes: use standard AspectRatioStrategy
                        val targetAspectRatio = when (currentAspectRatioMode) {
                            AspectRatioMode.RATIO_4_3 -> androidx.camera.core.AspectRatio.RATIO_4_3
                            AspectRatioMode.RATIO_16_9 -> androidx.camera.core.AspectRatio.RATIO_16_9
                            else -> androidx.camera.core.AspectRatio.RATIO_4_3
                        }
                        androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                            .setAspectRatioStrategy(
                                androidx.camera.core.resolutionselector.AspectRatioStrategy(
                                    targetAspectRatio,
                                    androidx.camera.core.resolutionselector.AspectRatioStrategy.FALLBACK_RULE_AUTO
                                )
                            )
                            .build()
                            .also {
                                Log.i(TAG, "[v2.11.1] ResolutionSelector: ${currentAspectRatioMode.displayName} → RATIO_$targetAspectRatio")
                            }
                    }

                    // Store target for diagnostic logging
                    selectedResolution = targetResolution

                    // One-time diagnostic: log all supported resolutions
                    logSupportedResolutions(cameraInfo)

                    // 1. Build Preview UseCase with SurfaceProvider
                    // v2.10.0: Use unified AspectRatioStrategy from commonSelector
                    val previewBuilder = Preview.Builder()
                        .setResolutionSelector(commonSelector)

                    // v2.11.4-01: Camera2 CaptureResult callback for AE/AF lock parameter verification (DEBUG only)
                    if (BuildConfig.DEBUG) {
                        @OptIn(ExperimentalCamera2Interop::class)
                        Camera2Interop.Extender(previewBuilder).setSessionCaptureCallback(
                            object : CameraCaptureSession.CaptureCallback() {
                                override fun onCaptureCompleted(
                                    session: CameraCaptureSession,
                                    request: CaptureRequest,
                                    result: TotalCaptureResult
                                ) {
                                    if (!logAeAfState) return
                                    val now = System.currentTimeMillis()
                                    if (now - lastAeAfLogTime < AEAF_DEBUG_LOG_INTERVAL_MS) return
                                    lastAeAfLogTime = now

                                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                                    val exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
                                    val sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY)
                                    val focusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE)
                                    val aeLock = result.get(CaptureResult.CONTROL_AE_LOCK)

                                    Log.i(TAG, "[AE/AF Debug] " +
                                        "AF_STATE=${afStateToString(afState)}, " +
                                        "AE_STATE=${aeStateToString(aeState)}, " +
                                        "AE_LOCK=$aeLock, " +
                                        "exposure=${exposureTime?.let { "${it / 1_000_000.0}ms" } ?: "null"}, " +
                                        "ISO=$sensitivity, " +
                                        "focusDist=${focusDistance?.let { "%.3f diopters".format(it) } ?: "null"}")
                                }
                            }
                        )
                    }

                    preview = previewBuilder.build()
                        .apply {
                            // ✅ CRITICAL: Attach SurfaceProvider (external or fallback)
                            val provider = externalSurfaceProvider ?: fallbackSurfaceProvider
                            val providerType = if (externalSurfaceProvider != null) "PreviewView (native)" else "Fallback SurfaceTexture"

                            setSurfaceProvider(provider)
                            Log.i(TAG, "✅ Preview UseCase created with $providerType + AspectRatio ${currentAspectRatioMode.displayName}")
                        }

                    // 2. Build ImageAnalysis UseCase (for Bitmap stream to UI)
                    // v2.10.0: Use unified AspectRatioStrategy for consistency with ViewPort
                    imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(commonSelector)
                        .setTargetRotation(Surface.ROTATION_0)  // Set target rotation for proper orientation
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            Log.i(TAG, "✅ ImageAnalysis UseCase created with ROTATION_0 + AspectRatio ${currentAspectRatioMode.displayName}")
                        }

                    // 3. Build ImageCapture UseCase (for photo capture)
                    // v2.10.0: Use unified AspectRatioStrategy for automatic WYSIWYG via ViewPort
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setResolutionSelector(commonSelector)
                        // targetRotation will be set dynamically via updateTargetRotation()
                        .build()
                        .apply {
                            Log.i(TAG, "✅ ImageCapture UseCase created with AspectRatio ${currentAspectRatioMode.displayName} (WYSIWYG via ViewPort)")
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

                    // 5. Create ViewPort for WYSIWYG guarantee (v2.10.0: Gemini approach)
                    // ViewPort ensures Preview, ImageAnalysis, and ImageCapture see the same cropped region
                    // v2.10.0-fix: Use FIT ScaleType to prevent double-cropping (ViewPort FILL + PreviewView FILL)
                    // v2.11.1: 1:1 mode uses Rational(1,1) ViewPort — Preview displays 1:1 square (WYSIWYG)
                    //   ResolutionFilter provides true square resolution; ViewPort crops to 1:1 if fallback to 4:3
                    val viewPort = ViewPort.Builder(
                        currentAspectRatioMode.toRational(),  // Rational(4,3) / Rational(16,9) / Rational(1,1)
                        currentRotation  // Current device rotation (Surface.ROTATION_*)
                    )
                        .setScaleType(ViewPort.FIT)  // ✅ Preserve full FOV, prevent over-cropping
                        .build()

                    Log.i(TAG, "[v2.11.1 ViewPort] Created: aspect=${currentAspectRatioMode.displayName} (${currentAspectRatioMode.toRational()}), rotation=$currentRotation, scaleType=FIT")

                    // 6. Create UseCaseGroup with ViewPort (automatic WYSIWYG)
                    val useCaseGroup = UseCaseGroup.Builder()
                        .addUseCase(preview)        // ✅ REQUIRED: Provides Surface for camera
                        .addUseCase(imageAnalysis)  // ✅ Provides Bitmap stream for UI
                        .addUseCase(imageCapture)   // ✅ Provides photo capture capability
                        .setViewPort(viewPort)      // ✅ Ensures all UseCases see same cropped region (WYSIWYG)
                        .build()

                    // 7. ✅ CRITICAL: Bind UseCaseGroup to lifecycle
                    // Using UseCaseGroup + ViewPort guarantees Preview and Capture consistency
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        useCaseGroup  // ✅ Single UseCaseGroup instead of individual UseCases
                    )

                    Log.i(TAG, "✅ Camera bound successfully with UseCaseGroup (${currentAspectRatioMode.displayName}): Preview + ImageAnalysis + ImageCapture + ViewPort WYSIWYG")

                    // v2.9.6 Phase 1 Fix: Log sensor orientation for debugging
                    val sensorRotation = camera.cameraInfo.sensorRotationDegrees
                    Log.i(TAG, "Camera sensor orientation: $sensorRotation°")

                    Log.i(TAG, "Camera should now start on real device")
                    Timber.i("LiveCameraSource: Camera bound to lifecycle")

                    // v2.9.6 Phase 1: Initialize orientation listener for dynamic rotation
                    initOrientationListener()

                    // v2.9.6 Phase 1 Fix: Set initial targetRotation IMMEDIATELY after reading current rotation
                    updateTargetRotation(currentRotation)
                    Log.i(TAG, "Initial targetRotation set to: $currentRotation")

                    // v2.6.1 FIX: Reattach analyzer after rebind (critical for switchCamera)
                    previewStreamAnalyzer?.let { analyzer ->
                        imageAnalysis.setAnalyzer(
                            analysisExecutor,
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

            if (BuildConfig.ENABLE_RENDERSCRIPT) {
                renderScriptYuvConverter.destroy()
                useRenderScript = true
            }

            // v2.9.6 Phase 1: Cleanup orientation listener
            cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind camera", e)
            Timber.e(e, "LiveCameraSource: Failed to unbind camera")
        }
    }

    /**
     * Cleanup resources when camera is no longer needed
     *
     * v2.9.6 Phase 1: Release orientation listener
     */
    fun cleanup() {
        orientationListener?.disable()
        orientationListener = null
        Log.d(TAG, "OrientationEventListener disabled and cleaned up")
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
     * Get current device rotation for EXIF Orientation
     * v2.9.6 Phase 1: EXIF Orientation support
     */
    /**
     * Get current rotation for EXIF Orientation
     * v2.9.6 Phase 1 Fix: Use ImageProxy rotation instead of OrientationEventListener
     * ImageProxy.rotationDegrees already accounts for sensor orientation
     */
    override fun getCurrentRotation(): Int {
        // Convert rotationDegrees (0°, 90°, 180°, 270°) to Surface.ROTATION_* (0, 1, 2, 3)
        return when (lastCaptureRotationDegrees) {
            0 -> Surface.ROTATION_0
            90 -> Surface.ROTATION_90
            180 -> Surface.ROTATION_180
            270 -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
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
     * Query supported resolutions from CameraX
     *
     * v2.9.0-beta3: Helper to get available camera resolutions
     *
     * @param cameraInfo CameraInfo from CameraProvider
     * @return List of supported Size objects
     */
    private fun getSupportedResolutions(cameraInfo: androidx.camera.core.CameraInfo): List<Size> {
        return try {
            // Try to get supported resolutions from CameraInfo
            // Note: This may require Camera2Interop or CameraX experimental APIs
            val characteristics = androidx.camera.camera2.interop.Camera2CameraInfo.from(cameraInfo)
                .getCameraCharacteristic(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val sizes = characteristics?.getOutputSizes(android.graphics.ImageFormat.YUV_420_888)?.toList() ?: emptyList()

            if (sizes.isNotEmpty()) {
                Log.d(TAG, "[v2.9.0-beta3] Found ${sizes.size} supported YUV_420_888 resolutions")
            } else {
                Log.w(TAG, "[v2.9.0-beta3] No supported resolutions found, will use CameraX defaults")
            }

            sizes
        } catch (e: Exception) {
            Log.w(TAG, "[v2.9.0-beta3] Failed to query supported resolutions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Select optimal resolution from supported list
     *
     * v2.9.0-beta3: Smart resolution selection to avoid 1080p fallback
     *
     * Strategy:
     * 1. Filter resolutions >= target size (both width and height)
     * 2. Sort by pixel count (ascending)
     * 3. Return smallest valid resolution
     * 4. Fallback to target if none found (let CameraX handle)
     *
     * @param supportedSizes List of supported resolutions
     * @param targetSize Target resolution (default: 480x360)
     * @return Optimal resolution
     */
    private fun selectOptimalResolution(
        supportedSizes: List<Size>,
        targetSize: Size = Size(480, 360)
    ): Size {
        if (supportedSizes.isEmpty()) {
            Log.i(TAG, "[v2.9.0-beta3] No supported sizes provided, using target: ${targetSize.width}x${targetSize.height}")
            return targetSize
        }

        // Filter: width >= target.width AND height >= target.height
        val validSizes = supportedSizes.filter { size ->
            size.width >= targetSize.width && size.height >= targetSize.height
        }

        if (validSizes.isEmpty()) {
            Log.w(TAG, "[v2.9.0-beta3] No resolution >= ${targetSize.width}x${targetSize.height}, using target as fallback")
            return targetSize
        }

        // Sort by pixel count (ascending), then return first (smallest valid)
        val optimal = validSizes.minByOrNull { size ->
            size.width * size.height
        } ?: targetSize

        val pixelCount = optimal.width * optimal.height
        Log.i(TAG, "[v2.9.0-beta3] Selected resolution: ${optimal.width}x${optimal.height} " +
                "(${pixelCount} pixels, ${validSizes.size} valid options)")

        return optimal
    }

    /**
     * Log all supported resolutions for diagnostics
     *
     * v2.9.0-beta3: One-time diagnostic log
     *
     * @param cameraInfo CameraInfo from CameraProvider
     */
    private fun logSupportedResolutions(cameraInfo: androidx.camera.core.CameraInfo) {
        try {
            val characteristics = androidx.camera.camera2.interop.Camera2CameraInfo.from(cameraInfo)
                .getCameraCharacteristic(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val yuvSizes = characteristics?.getOutputSizes(android.graphics.ImageFormat.YUV_420_888)

            if (yuvSizes != null && yuvSizes.isNotEmpty()) {
                val sortedSizes = yuvSizes.sortedBy { it.width * it.height }
                val sizeList = sortedSizes.joinToString(", ") { "${it.width}x${it.height}" }

                Log.d(TAG, "[v2.9.0-beta3 Supported Resolutions] YUV_420_888 (${yuvSizes.size} total):")
                Log.d(TAG, "[v2.9.0-beta3] $sizeList")

                // Log pixel counts for reference
                sortedSizes.take(5).forEach { size ->
                    val pixels = size.width * size.height
                    val megapixels = pixels / 1_000_000.0
                    Log.d(TAG, "[v2.9.0-beta3] - ${size.width}x${size.height} = ${String.format("%.2f", megapixels)}MP")
                }
            } else {
                Log.w(TAG, "[v2.9.0-beta3] No YUV_420_888 resolutions found in characteristics")
            }
        } catch (e: Exception) {
            Log.w(TAG, "[v2.9.0-beta3] Failed to log supported resolutions: ${e.message}")
        }
    }

    /**
     * Rotate and optionally mirror a bitmap
     *
     * v2.6.1: Added mirror parameter for front camera selfie view
     * v2.8.3 Phase D: Minimal optimization - reuse Paint, skip CLEAR
     *
     * @param source Source bitmap to transform
     * @param degrees Rotation angle (0, 90, 180, 270)
     * @param mirror Whether to mirror horizontally (for front camera)
     * @return Transformed bitmap
     */
    private fun transformBitmap(
        source: BitmapPool.BitmapBuffer,
        degrees: Int,
        mirror: Boolean = false
    ): BitmapPool.BitmapBuffer {
        if (degrees == 0 && !mirror) return source

        // v2.8.4 Quick Win: Fixed output size to eliminate BitmapPool key explosion
        // Problem: ceil(mapRect()) produces floating-point errors (721, 719, etc.)
        //   causing pool keys to explode from 2 to 10+, hit rate <50%
        // Solution: Calculate exact integer dimensions based on rotation
        //   90/270: swap width/height; 0/180: keep dimensions
        //   Result: Only 2 pool keys (landscape + portrait), hit rate >90%

        val srcWidth = source.bitmap.width
        val srcHeight = source.bitmap.height

        // Calculate output dimensions based on rotation (no floating point)
        val (outWidth, outHeight) = when (degrees) {
            90, 270 -> srcHeight to srcWidth  // Swap dimensions for portrait
            else -> srcWidth to srcHeight      // Keep dimensions for 0/180
        }

        val out = bitmapPool.acquire(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out.bitmap)

        // Build transformation matrix optimized for each rotation case
        val matrix = Matrix()

        when {
            // Fast path: 90° rotation (most common for portrait mode)
            degrees == 90 && !mirror -> {
                matrix.postRotate(90f)
                matrix.postTranslate(outWidth.toFloat(), 0f)
            }
            // Fast path: 270° rotation
            degrees == 270 && !mirror -> {
                matrix.postRotate(270f)
                matrix.postTranslate(0f, outHeight.toFloat())
            }
            // Fast path: 180° rotation
            degrees == 180 && !mirror -> {
                matrix.postRotate(180f)
                matrix.postTranslate(outWidth.toFloat(), outHeight.toFloat())
            }
            // Fast path: Mirror only (0° rotation, front camera)
            degrees == 0 && mirror -> {
                matrix.postScale(-1f, 1f)
                matrix.postTranslate(outWidth.toFloat(), 0f)
            }
            // General path: Combined rotation + mirror
            else -> {
                if (degrees != 0) {
                    matrix.postRotate(degrees.toFloat())
                }
                if (mirror) {
                    matrix.postScale(-1f, 1f)
                }
                // Calculate bounds and adjust translation
                val srcRect = RectF(0f, 0f, srcWidth.toFloat(), srcHeight.toFloat())
                val dstRect = RectF(srcRect)
                matrix.mapRect(dstRect)
                matrix.postTranslate(-dstRect.left, -dstRect.top)
            }
        }

        // v2.8.3 Phase D: Keep drawColor(CLEAR) - removing causes 90% regression
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // v2.8.3 Phase D: Reuse Paint object (avoid per-frame allocation)
        canvas.drawBitmap(source.bitmap, matrix, transformPaint)

        return out
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

    /**
     * Set PreviewView surface provider for native preview
     * v2.8.6 Phase 2: PreviewView migration for Graphics memory fix
     */
    override fun setPreviewSurfaceProvider(surfaceProvider: Preview.SurfaceProvider?) {
        externalSurfaceProvider = surfaceProvider
        Log.i(TAG, "[Phase 2] External surface provider ${if (surfaceProvider != null) "set" else "cleared"}")

        // If camera already bound, rebind to apply new provider
        if (::cameraProvider.isInitialized && currentLifecycleOwner != null) {
            Log.i(TAG, "[Phase 2] Camera rebind triggered to apply new surface provider")
            // Trigger immediate rebinding to connect PreviewView
            currentLifecycleOwner?.let { owner ->
                bindToLifecycle(owner)
            }
        }
    }

    /**
     * Set PreviewView reference for accurate coordinate transformation
     * v2.11.1 P0-1: Coordinate system unification
     *
     * PreviewView.meteringPointFactory provides accurate mapping that accounts for:
     * - FIT_CENTER letterboxing (black bars)
     * - ViewPort cropping (aspect ratio modes)
     * - Display rotation
     *
     * This fixes Issue #1: Out-of-preview area can trigger tap focus
     */
    override fun setPreviewView(previewView: androidx.camera.view.PreviewView?) {
        this.previewView = previewView
        Log.i(TAG, "[v2.11.1 P0-1] PreviewView reference ${if (previewView != null) "set" else "cleared"}")
    }

    /**
     * Check if native preview is supported
     * v2.8.6 Phase 2: PreviewView migration
     */
    override fun supportsNativePreview(): Boolean = true

    /**
     * Set aspect ratio mode for ViewPort configuration
     * v2.10.0: UseCaseGroup + ViewPort architecture
     *
     * Automatically rebinds camera to apply the new ViewPort with updated aspect ratio.
     *
     * @param mode The desired aspect ratio mode (4:3, 16:9, 1:1)
     */
    fun setAspectRatioMode(mode: AspectRatioMode) {
        currentAspectRatioMode = mode
        Log.i(TAG, "[v2.10.0] Aspect ratio mode set to: ${mode.displayName}")

        // If camera already bound, rebind to apply new ViewPort with updated aspect ratio
        if (::cameraProvider.isInitialized && currentLifecycleOwner != null) {
            Log.i(TAG, "[v2.10.0] Camera rebind triggered to apply new aspect ratio via ViewPort")
            // Trigger immediate rebinding to apply ViewPort change
            currentLifecycleOwner?.let { owner ->
                bindToLifecycle(owner)
            }
        }
    }

    /**
     * Smart Resolution Selection - v2.9.0-beta5
     */
    private fun selectDeviceOptimalResolution(supportedSizes: List<Size>): Triple<Size, Int, Int> {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        Log.i(TAG, "[v2.9.0-beta5] Device: $manufacturer $model, ${supportedSizes.size} resolutions")
        val acceptableResolutions = supportedSizes.filter { s ->
            s.width <= 1280 && s.height <= 720 && s.width >= 480 && s.height >= 360
        }.sortedBy { it.width * it.height }
        if (acceptableResolutions.isNotEmpty()) {
            Log.i(TAG, "[v2.9.0-beta5] Acceptable: ${acceptableResolutions.joinToString { "${it.width}x${it.height}" }}")
        }
        if (manufacturer.equals("Google", ignoreCase = true) && model.contains("Pixel", ignoreCase = true)) {
            // v2.9.6 Phase 2.1: Upgrade to 1280x720 for better preview quality
            val has1280x720 = acceptableResolutions.any { it.width == 1280 && it.height == 720 }
            return if (has1280x720) {
                Log.i(TAG, "[v2.9.6] Pixel: 1280x720 (upgraded for better quality)")
                Triple(Size(1280, 720), 1920, 1080)
            } else {
                Log.i(TAG, "[v2.9.6] Pixel: 800x600 fallback")
                Triple(Size(800, 600), 1280, 720)
            }
        }
        if (manufacturer.equals("Sony", ignoreCase = true)) {
            Log.i(TAG, "[v2.9.0-beta5] Sony: 640x480")
            return Triple(Size(640, 480), 800, 600)
        }
        if (acceptableResolutions.isNotEmpty()) {
            val optimal = acceptableResolutions.lastOrNull { s -> s.width <= 800 && s.height <= 600 }
            if (optimal != null) {
                Log.i(TAG, "[v2.9.0-beta5] Generic: ${optimal.width}x${optimal.height}")
                return Triple(optimal, 800, 600)
            }
        }
        Log.w(TAG, "[v2.9.0-beta5] Fallback: 640x480")
        return Triple(Size(640, 480), 800, 600)
    }

    /**
     * Convert thermal status to human-readable name
     * v2.9.0-alpha Phase 0: Thermal monitoring
     */
    private fun getThermalStatusName(status: Int): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (status) {
                PowerManager.THERMAL_STATUS_NONE -> "NONE"
                PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
                PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
                PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
                else -> "UNKNOWN($status)"
            }
        } else {
            "N/A"
        }
    }

    // v2.11.4-01: Camera2 AF_STATE / AE_STATE name helpers (DEBUG only)
    private fun afStateToString(state: Int?): String = when (state) {
        CaptureResult.CONTROL_AF_STATE_INACTIVE -> "INACTIVE"
        CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN -> "PASSIVE_SCAN"
        CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED -> "PASSIVE_FOCUSED"
        CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN -> "ACTIVE_SCAN"
        CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> "FOCUSED_LOCKED"
        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> "NOT_FOCUSED_LOCKED"
        CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED -> "PASSIVE_UNFOCUSED"
        null -> "null"
        else -> "UNKNOWN($state)"
    }

    private fun aeStateToString(state: Int?): String = when (state) {
        CaptureResult.CONTROL_AE_STATE_INACTIVE -> "INACTIVE"
        CaptureResult.CONTROL_AE_STATE_SEARCHING -> "SEARCHING"
        CaptureResult.CONTROL_AE_STATE_CONVERGED -> "CONVERGED"
        CaptureResult.CONTROL_AE_STATE_LOCKED -> "LOCKED"
        CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED -> "FLASH_REQUIRED"
        CaptureResult.CONTROL_AE_STATE_PRECAPTURE -> "PRECAPTURE"
        null -> "null"
        else -> "UNKNOWN($state)"
    }
}
