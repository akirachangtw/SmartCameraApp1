package com.akirachang.ai.photographer.data.source.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
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
import com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode
import com.akirachang.ai.photographer.util.BitmapPool
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
import androidx.camera.core.CameraState
import androidx.camera.core.ExperimentalCameraInfo
import androidx.lifecycle.asFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector

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
@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
class LiveCameraSource @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraInputSource {

    companion object {
        private const val TAG = "LiveCameraSource"
        // v2.11.4-01: AE/AF debug logging throttle interval (1 log per second)
        private const val AEAF_DEBUG_LOG_INTERVAL_MS = 1000L
        // P08-F4-FIX-v2 AI-5: Contract §4 AI-5 — 連續 N ms 無 detection 結果觸發 PIPELINE-STALL
        private const val DETECTION_HEARTBEAT_TIMEOUT_MS = 3000L
    }

    // v2.11.4-01: Flag to enable Camera2 AE/AF state logging (toggled by lock/unlock)
    @Volatile
    private var logAeAfState = false
    private var lastAeAfLogTime = 0L

    // v2.11.4-06 Bug 3: Track latest converged focus distance for lens freeze on lock.
    // Pixel 9a resets LENS_FOCUS_DISTANCE to 0 (infinity) when AF_MODE=OFF is applied,
    // causing visible blur. We capture the most recent value from every CaptureResult frame
    // and pin the lens to this position when locking AE/AF.
    @Volatile
    private var lastFocusDistance: Float = 0f

    // v2.11.4-08 (L2 State Machine): Full AeAfLockState enum replaces the boolean flag.
    // Tracks precise lifecycle of each lock operation:
    //   UNLOCKED → LOCK_REQUESTED → LOCKED → UNLOCK_REQUESTED → UNLOCKED
    // CameraState.OPEN observer only clears Camera2 options when state is UNLOCKED,
    // preventing spurious clears during active lock/unlock transitions.
    private enum class AeAfLockState { UNLOCKED, LOCK_REQUESTED, LOCKED, UNLOCK_REQUESTED }

    @Volatile
    private var aeAfLockState: AeAfLockState = AeAfLockState.UNLOCKED

    // v2.12.0-07: Fixed-focus lens detection (set after bindToLifecycle).
    // Front cameras on many devices (incl. Pixel 9a) have no VCM — startFocusAndMetering()
    // always returns isFocusSuccessful=false, causing endless FAILED→backoff loops.
    @Volatile
    private var isFixedFocus = false

    // v2.12.0-11: StateFlow path for ViewModel to observe fixed-focus capability.
    private val _isFixedFocusLens = MutableStateFlow(false)
    val isFixedFocusLens: StateFlow<Boolean> = _isFixedFocusLens.asStateFlow()

    // v2.11.4-08: Increments on every bindToLifecycle call.
    // Future callbacks capture this value at submission time; if it diverges (camera switched),
    // the stale callback is discarded, preventing LOCKED state from a recycled session.
    @Volatile
    private var sessionId: Int = 0

    // v2.11.4-08: Coroutine scope for the CameraState.OPEN Flow observer.
    // Survives screen lock/unlock and background transitions within the same session.
    private val sourceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    // D3-T3: Hardware focal length factor = focalLengthMm / sensorWidthMm.
    // Multiply by previewWidth to get focalLengthPixels for ImuCompensator.
    // Populated after camera binding; null until then (safe to read any time).
    @Volatile
    private var focalLengthFactor: Float? = null

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

    // v2.8.3 Phase D: Reusable Paint object to avoid per-frame allocation
    private val transformPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    // v2.9.6 Phase 1: Orientation tracking for dynamic rotation
    private var orientationListener: OrientationEventListener? = null
    private var currentRotation: Int = Surface.ROTATION_0
    private var orientationListenerTriggered = false  // v2.9.6 Phase 1 Fix: Track if listener is working

    // v2.9.6 Phase 1 Fix: Store rotation from ImageProxy for EXIF
    private var lastCaptureRotationDegrees: Int = 0

    // P08-F4-FIX-v2 AI-1: Real-device face detection callback path (Contract C2)
    // Set by CameraRepositoryImpl.startImageAnalysis() when isLiveCameraPath=true.
    // Nulled by CameraRepositoryImpl.stopImageAnalysis() and unbind() for lifecycle safety.
    @Volatile var liveDetector: FaceDetector? = null
    var onDetectionResult: ((DetectionResult) -> Unit)? = null
    @Volatile private var lastDetectionTimeMs: Long = 0L
    @Volatile private var lastFaceCount: Int = -1

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
                val yuvMode = "CPU"
                yuvConverter.convertToBitmapBuffer(imageProxy, rawBuffer)
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

                // Rotate and mirror bitmap if needed to match display orientation
                val startTransform = System.nanoTime()
                val outputBuffer = if (compensatedRotation != 0 || isFrontCamera) {
                    transformBitmap(rawBuffer, compensatedRotation, isFrontCamera)
                } else {
                    rawBuffer
                }
                val transformTime = (System.nanoTime() - startTransform) / 1_000_000.0

                // P08-F4-FIX-v2 AI-2: Async face detection on portrait bitmap (Contract C2)
                // fromBitmap() does not hold ImageProxy — imageProxy.close() in finally is safe
                val detectionBitmap = outputBuffer.bitmap
                liveDetector?.let { detector ->
                    val inputImage = InputImage.fromBitmap(detectionBitmap, 0)
                    detector.process(inputImage)
                        .addOnSuccessListener { faces ->
                            lastFaceCount = faces.size
                            lastDetectionTimeMs = System.currentTimeMillis()
                            onDetectionResult?.invoke(
                                DetectionResult(
                                    faces = faces,
                                    objects = emptyList(),
                                    imageWidth = detectionBitmap.width,
                                    imageHeight = detectionBitmap.height,
                                    rotationDegrees = 0
                                )
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "[F4v2] Face detection failed on frame $frameCount", e)
                        }
                }

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
                    // AI-5: Heartbeat protection — warn if detection pipeline stalled > 3s
                    val nowMs = System.currentTimeMillis()
                    if (liveDetector != null && lastDetectionTimeMs > 0L && (nowMs - lastDetectionTimeMs) > DETECTION_HEARTBEAT_TIMEOUT_MS) {
                        Log.w(TAG, "[PIPELINE-STALL] Detection heartbeat timeout: ${nowMs - lastDetectionTimeMs}ms since last result")
                    }
                    // AI-7: F4v2-Diag 7-field diagnostic log (Contract C2 geometry verification)
                    Log.d(TAG, "[F4v2-Diag] currentRotation=$currentRotation rotationDegrees=$rotationDegrees compensatedRotation=$compensatedRotation imageWidth=${detectionBitmap.width} imageHeight=${detectionBitmap.height} faceCount=$lastFaceCount isFrontCamera=$isFrontCamera")
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

            // [P05-R1] Clamp coordinates to valid view bounds — prevents IllegalArgumentException
            // from MeteringPointFactory when Eye-AF produces out-of-bounds coordinates
            // (Ghost Face bbox overflow or IMU-compensated drift beyond preview edges).
            val clampedX = x.coerceIn(0f, viewWidth.toFloat())
            val clampedY = y.coerceIn(0f, viewHeight.toFloat())

            // v2.11.1 P0-1: Use PreviewView.meteringPointFactory if available
            // This provides accurate coordinate mapping that respects FIT_CENTER letterboxing
            val focusPoint = if (previewView != null) {
                // ✅ FIX Issue #1: Check if tap is within content bounds (exclude letterbox)
                // PreviewView.meteringPointFactory automatically handles this validation
                val factory = previewView!!.meteringPointFactory
                factory.createPoint(clampedX, clampedY)
            } else {
                // Fallback to SurfaceOrientedMeteringPointFactory (legacy behavior)
                // ⚠️ WARNING: This does NOT account for FIT_CENTER letterboxing
                Log.e(TAG, "[v2.12.0-14] CRITICAL: PreviewView not set — metering point uses legacy SurfaceOrientedFactory (letterbox NOT handled). Focus accuracy degraded.")
                val factory = androidx.camera.core.SurfaceOrientedMeteringPointFactory(
                    viewWidth.toFloat(),
                    viewHeight.toFloat()
                )
                factory.createPoint(clampedX, clampedY)
            }

            // v2.12.0-07: Fixed-focus bypass — skip FLAG_AF entirely.
            // Fixed-focus lenses (e.g. Pixel 9a front camera) have no VCM motor;
            // startFocusAndMetering(FLAG_AF) always returns isFocusSuccessful=false,
            // causing AfState to enter endless FAILED→backoff loops.
            // Instead: trigger AE+AWB only (exposure/white-balance still benefit from
            // metering point) and return success immediately so ViewModel sets AfState.Locked.
            if (isFixedFocus) {
                Log.i(TAG, "[v2.12.0-07] Fixed-focus lens — AE/AWB metering only at ($clampedX, $clampedY)")
                val aeOnlyAction = androidx.camera.core.FocusMeteringAction.Builder(
                    focusPoint,
                    androidx.camera.core.FocusMeteringAction.FLAG_AE or
                            androidx.camera.core.FocusMeteringAction.FLAG_AWB
                ).apply {
                    setAutoCancelDuration(4, java.util.concurrent.TimeUnit.SECONDS)
                }.build()
                cameraControl.startFocusAndMetering(aeOnlyAction)
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            // Build focus metering action (autofocus-capable lenses)
            // v2.12.0-06 H2.1: Add FLAG_AE so rear-camera HAL can gate exposure before locking AF.
            // v2.12.0-06 H2.2: autoCancelDuration 5s→4s — CameraX must deliver result BEFORE
            //   ViewModel's withTimeout(4500ms) fires, preventing premature TIMEOUT state + backoff.
            val action = androidx.camera.core.FocusMeteringAction.Builder(
                focusPoint,
                androidx.camera.core.FocusMeteringAction.FLAG_AF or
                        androidx.camera.core.FocusMeteringAction.FLAG_AE
            ).apply {
                setAutoCancelDuration(4, java.util.concurrent.TimeUnit.SECONDS)
            }.build()

            // Start focus and metering
            val future = cameraControl.startFocusAndMetering(action)

            // v2.9.10: Use addListener for non-blocking coroutine approach (fixes UI freeze)
            future.addListener({
                try {
                    val result = future.get()  // Called in Executor thread, doesn't block main thread
                    if (result.isFocusSuccessful) {
                        Log.d(TAG, "[v2.11.1] Focus successful at ($clampedX, $clampedY) - factory: ${if (previewView != null) "PreviewView" else "SurfaceOriented"}")
                        Timber.d("LiveCameraSource: Focus successful at ($clampedX, $clampedY)")
                        continuation.resume(Result.success(Unit))
                    } else {
                        Log.w(TAG, "[v2.11.1] Focus failed at ($clampedX, $clampedY) - factory: ${if (previewView != null) "PreviewView" else "SurfaceOriented"}")
                        Timber.w("LiveCameraSource: Focus failed at ($clampedX, $clampedY)")
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

            // v2.11.4-08: Capture session ID now — listener compares against current sessionId
            // to detect if the camera was rebound (e.g. switchCamera) before the lock completes.
            val capturedSession = sessionId
            aeAfLockState = AeAfLockState.LOCK_REQUESTED
            Log.d(TAG, "[v2.11.4-08] Lock requested — session #$capturedSession, state=LOCK_REQUESTED")

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

            // v2.11.4-07 Bug 4 fix: Pre-clear Camera2 interop options BEFORE startFocusAndMetering().
            // clearCaptureRequestOptions() and startFocusAndMetering() share the same camera executor
            // (FIFO queue). By submitting the clear first, the executor processes:
            //   1. clearCaptureRequestOptions() → removes AF_MODE=OFF residual
            //   2. startFocusAndMetering() → CameraX native AF (CONTINUOUS_PICTURE first)
            // Without this, a residual AF_MODE=OFF from a previous lock/cancel cycle blocks
            // CameraX's AF scan → 5s timeout on second lock attempt after any unlock.
            try {
Camera2CameraControl.from(cameraControl).clearCaptureRequestOptions()
                Log.i(TAG, "[v2.11.4-07] Pre-clear Camera2 options before lock attempt")
            } catch (e: Exception) {
                Log.w(TAG, "[v2.11.4-07] Pre-clear failed (non-fatal): ${e.message}")
            }

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
                        // v2.11.4-08: Stale session guard — if camera was rebound between lock request
                        // and this callback, discard the result to prevent LOCKED state from a dead session.
                        if (capturedSession != sessionId) {
                            Log.w(TAG, "[v2.11.4-08] Stale lock callback discarded (session $capturedSession != $sessionId)")
                            continuation.cancel()
                            return@addListener
                        }
                        aeAfLockState = AeAfLockState.LOCKED
                        // v2.11.4-06 Bug 3: Capture lastFocusDistance at this exact moment.
                        // At success callback time (1-2s after tap), the value reflects the
                        // converged lens position. Pinning LENS_FOCUS_DISTANCE prevents Pixel 9a
                        // from drifting to infinity (0 diopters) when AF_MODE=OFF takes effect.
                        val frozenFocusDist = lastFocusDistance
                Camera2CameraControl.from(cameraControl)
                            .addCaptureRequestOptions(
                                CaptureRequestOptions.Builder()
                                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, true)
                                    .setCaptureRequestOption(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_OFF
                                    )
                                    .setCaptureRequestOption(
                                        CaptureRequest.LENS_FOCUS_DISTANCE,
                                        frozenFocusDist
                                    )
                                    .build()
                            )
                        Log.i(TAG, "[v2.11.4-06] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF + CONTROL_LENS_FOCUS_DISTANCE=$frozenFocusDist applied")
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
                    aeAfLockState = AeAfLockState.UNLOCKED
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

            // v2.11.4-10: Clear Camera2 Interop options BEFORE cancelFocusAndMetering().
            // Root cause of prior deadlock: AF_MODE=OFF (set in lockAEAF via Camera2 Interop)
            // causes CameraX's cancelFocusAndMetering() future to hang indefinitely.
            // CameraX waits for an AF state change to confirm cancellation, but AF_MODE=OFF
            // keeps AF_STATE=INACTIVE permanently — nothing to wait for, future never completes.
            // Since cleanup (clearCaptureRequestOptions, logAeAfState=false) was inside
            // future.addListener(), it NEVER ran → AE_LOCK=true remained in hardware.
            // Fix: Clear Camera2 options synchronously first so AF_MODE reverts to CameraX
            // default (CONTINUOUS_PICTURE), unblocking the cancel future.
            aeAfLockState = AeAfLockState.UNLOCK_REQUESTED
            Log.d(TAG, "[v2.11.4-10] Unlock requested — clearing Camera2 options before cancel")
            try {
Camera2CameraControl.from(cameraControl).clearCaptureRequestOptions()
                Log.i(TAG, "[v2.11.4-10] Camera2 options pre-cleared (AE_LOCK + AF_MODE=OFF removed)")
            } catch (ex: Exception) {
                Log.w(TAG, "[v2.11.4-10] Pre-cancel clearCaptureRequestOptions failed (non-fatal): ${ex.message}")
            }

            // Cancel any active focus metering action (now unblocked since AF_MODE=OFF removed)
            val future = cameraControl.cancelFocusAndMetering()

            // Non-blocking coroutine approach
            future.addListener({
                aeAfLockState = AeAfLockState.UNLOCKED
                Log.d(TAG, "[v2.11.4-10] State → UNLOCKED after cancel")
                try {
                    future.get()
                    // v2.11.4-01: Stop Camera2 AE/AF state logging
                    if (BuildConfig.DEBUG) {
                        logAeAfState = false
                        Log.i(TAG, "[AE/AF Debug] Logging DISABLED — returned to auto mode")
                    }
                    Log.i(TAG, "Focus and metering cancelled - returned to auto mode")
                    Timber.i("LiveCameraSource: Focus/metering cancelled")
                    continuation.resume(Result.success(Unit))
                } catch (e: Exception) {
                    // OperationCanceledException = superseded by another cancelFocusAndMetering()
                    // Camera2 pre-cleanup already done above; treat as non-fatal
                    val isSuperseded = e.cause?.message?.contains("Cancelled by another") == true
                    if (isSuperseded) {
                        Log.w(TAG, "[v2.11.4-10] cancelFocusAndMetering superseded — cleanup already applied")
                        if (BuildConfig.DEBUG) logAeAfState = false
                        continuation.resume(Result.success(Unit))
                    } else {
                        Log.e(TAG, "Cancel focus/metering error", e)
                        Timber.e(e, "LiveCameraSource: Cancel focus/metering error")
                        continuation.resume(Result.failure(e))
                    }
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
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)
        } else {
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay
        }
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
    @androidx.annotation.OptIn(
        ExperimentalCameraInfo::class,
        ExperimentalCamera2Interop::class
    )
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
                    // v2.12.0: Simplified to 4:3 / 16:9 only — removed 1:1 digital crop mode
                    //   4:3 = sensor native, 16:9 = display fill; no square ResolutionFilter needed
                    val targetAspectRatio = when (currentAspectRatioMode) {
                        AspectRatioMode.RATIO_4_3 -> androidx.camera.core.AspectRatio.RATIO_4_3
                        AspectRatioMode.RATIO_16_9 -> androidx.camera.core.AspectRatio.RATIO_16_9
                    }
                    val commonSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                        .setAspectRatioStrategy(
                            androidx.camera.core.resolutionselector.AspectRatioStrategy(
                                targetAspectRatio,
                                androidx.camera.core.resolutionselector.AspectRatioStrategy.FALLBACK_RULE_AUTO
                            )
                        )
                        .build()
                        .also {
                            Log.i(TAG, "[v2.12.0] ResolutionSelector: ${currentAspectRatioMode.displayName} → RATIO_$targetAspectRatio")
                        }

                    // v2.12.0-02: Dedicated analysisSelector for ImageAnalysis — capped at ≤640×480
                    // Rationale: commonSelector lets HAL pick any resolution (e.g. Sony selects 1920×1080
                    //   for 16:9 → FPS drops below 20). ML Kit analysis does not need high resolution.
                    //   Separating selectors: Preview+Capture keep full quality; Analysis stays small.
                    // v2.12.0-02 fix: Remove AspectRatioStrategy from analysisSelector.
                    //   Root cause: Sony XQ-BT52 HAL has no 16:9 YUV_420_888 resolution < 1280×720.
                    //   AspectRatioStrategy(16:9) filtered to 16:9-only resolutions → Sony minimum =
                    //   1280×720 → ResolutionStrategy cap(640×480) failed (no lower 16:9 found).
                    //   Fix: AR-free selector → HAL picks 640×480 (4:3 physical) for both 4:3 and 16:9
                    //   modes. ViewPort cropRect handles the 16:9 visible region.
                    //   Impact: ML Kit analyzes full 640×480 in 16:9 mode (slight letterbox FOV).
                    //   imageProxy.width=640, imageProxy.height=480 for all modes → coordinate mapping OK.
                    val analysisSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            androidx.camera.core.resolutionselector.ResolutionStrategy(
                                android.util.Size(640, 480),
                                androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                            )
                        )
                        .build()
                        .also {
                            Log.i(TAG, "[v2.12.0-02] analysisSelector: AR-free, capped ≤640×480 (both modes)")
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
                Camera2Interop.Extender(previewBuilder).setSessionCaptureCallback(
                            object : CameraCaptureSession.CaptureCallback() {
                                override fun onCaptureCompleted(
                                    session: CameraCaptureSession,
                                    request: CaptureRequest,
                                    result: TotalCaptureResult
                                ) {
                                    // v2.11.4-06 Bug 3: Always track focus distance regardless of debug logging state.
                                    // This value is used by lockAEAF() to freeze the lens at the converged position
                                    // when AF_MODE=OFF is applied (Pixel 9a resets to 0/infinity otherwise).
                                    val fd = result.get(CaptureResult.LENS_FOCUS_DISTANCE)
                                    if (fd != null) lastFocusDistance = fd

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
                    // v2.12.0: Use analysisSelector (≤640×480 cap) — prevents HAL-dependent FPS drop
                    imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(analysisSelector)
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
                        cameraProvider.unbindAll()
                        Log.d(TAG, "Previous UseCases unbound successfully")
                        focalLengthFactor = null
                        Log.d(TAG, "[D3-T3] focalLengthFactor reset before rebind")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to unbind previous UseCases: ${e.message}", e)
                        // 繼續執行 bind，可能是首次綁定
                    }

                    // 5. Create ViewPort for WYSIWYG guarantee (v2.10.0: Gemini approach)
                    // ViewPort ensures Preview, ImageAnalysis, and ImageCapture see the same cropped region
                    // v2.10.0-fix: Use FIT ScaleType to prevent double-cropping (ViewPort FILL + PreviewView FILL)
                    // v2.12.0: Only 4:3 and 16:9 supported — ViewPort uses Rational(4,3) or Rational(16,9)
                    val viewPort = ViewPort.Builder(
                        currentAspectRatioMode.toRational(),  // Rational(4,3) / Rational(16,9)
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

                    // v2.12.0-07: Detect fixed-focus lens and store for focusOnPoint() bypass
                    isFixedFocus = FixedFocusDetector.isFixedFocus(camera.cameraInfo)
                    val afModes = FixedFocusDetector.getAfModes(camera.cameraInfo)
                    val hwLevel = FixedFocusDetector.getHardwareLevel(camera.cameraInfo)
                    Log.i(TAG, "[v2.12.0-07] isFixedFocus=$isFixedFocus, afModes=[${afModes.joinToString()}], hwLevel=$hwLevel")

                    // v2.12.0-11: Emit to StateFlow so ViewModel can observe fixed-focus capability
                    _isFixedFocusLens.value = isFixedFocus
                    Log.i(TAG, "[v2.12.0-11] isFixedFocusLens updated: $isFixedFocus → StateFlow emitted")

                    // D3-T3: Read hardware focal length factor from CameraCharacteristics.
                    // Guard: only runs after bindToLifecycle() succeeds — CameraInfo is valid here.
                    focalLengthFactor = runCatching {
                        val camera2Info = androidx.camera.camera2.interop.Camera2CameraInfo.from(camera.cameraInfo)
                        val focalLengths = camera2Info.getCameraCharacteristic(
                            android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
                        )
                        val sensorSize = camera2Info.getCameraCharacteristic(
                            android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
                        )
                        val focalMm = focalLengths?.firstOrNull()
                        val sensorWidthMm = sensorSize?.width
                        if (focalMm != null && sensorWidthMm != null && sensorWidthMm > 0f) {
                            (focalMm / sensorWidthMm).also {
                                Log.i(TAG, "[D3-T3] focalLengthFactor=$it (focalMm=$focalMm sensorWidthMm=$sensorWidthMm)")
                            }
                        } else {
                            Log.w(TAG, "[D3-T3] Cannot read focal length from CameraCharacteristics, factor=null")
                            null
                        }
                    }.getOrElse { e ->
                        Log.e(TAG, "[D3-T3] Failed to read focal length characteristics: ${e.message}", e)
                        null
                    }

                    // v2.11.4-06 Bug 2: Clear any lingering Camera2 interop options from previous session.
                    // When screen locks while AE_LOCK=true + AF_MODE=OFF are active, the
                    // clearCaptureRequestOptions() call in ON_STOP fails silently (camera inactive,
                    // HAL ignores the request). CameraX re-applies the stale options on re-bind,
                    // freezing AF after screen unlock. Clearing unconditionally on every bind
                    // guarantees a clean AE/AF state regardless of how the previous session ended.
                    try {
                Camera2CameraControl.from(camera.cameraControl).clearCaptureRequestOptions()
                        Log.i(TAG, "[v2.11.4-06] Camera2 interop options cleared on bind (clean state)")
                    } catch (e: Exception) {
                        Log.w(TAG, "[v2.11.4-06] Failed to clear Camera2 options on bind: ${e.message}")
                    }

                    // v2.11.4-09 Bug 2 fix (race condition fix): Broaden CameraState.OPEN guard.
                    // v2.11.4-08 guard (UNLOCKED only) missed a race condition:
                    //   ON_STOP fires → cancelFocusAndMetering() starts, but camera goes inactive
                    //   before the Future listener runs → aeAfLockState stays LOCKED → guard blocks clear.
                    // Symptom: yellow box gone (ViewModel isAEAFLocked=false) but AE_LOCK=true persists
                    //   in hardware → overexposed preview after screen unlock.
                    // Fix: allow clear whenever state is NOT LOCK_REQUESTED (i.e. not mid-lock-operation).
                    //   If LOCKED at OPEN time → stale lock from closed session → auto-invalidate.
                    //   UNLOCK_REQUESTED → cleanup already in flight, redundant clear is safe.
                    val currentSession = ++sessionId
                    camera.cameraInfo.cameraState
                        .asFlow()
                        .filter { it.type == CameraState.Type.OPEN }
                        .onEach {
                            if (currentSession == sessionId && aeAfLockState != AeAfLockState.LOCK_REQUESTED) {
                                // v2.11.4-09: If LOCKED when camera reopens, the lock is from a closed
                                // session (ON_STOP cancel raced with camera shutdown). Auto-invalidate.
                                val autoInvalidated = aeAfLockState == AeAfLockState.LOCKED
                                if (autoInvalidated) {
                                    aeAfLockState = AeAfLockState.UNLOCKED
                                    Log.i(TAG, "[v2.11.4-09] Stale lock auto-invalidated on camera reopen (session #$currentSession)")
                                }
                                try {
                                        Camera2CameraControl.from(camera.cameraControl)
                                        .clearCaptureRequestOptions()
                                    Log.i(TAG, "[v2.11.4-09] Camera2 options cleared on CameraState.OPEN (session #$currentSession, autoInvalidated=$autoInvalidated)")
                                } catch (e: Exception) {
                                    Log.w(TAG, "[v2.11.4-09] clearCaptureRequestOptions on OPEN failed (non-fatal): ${e.message}")
                                }
                            } else {
                                Log.d(TAG, "[v2.11.4-09] CameraState.OPEN — skip clear (lock in progress): session=${currentSession}/$sessionId, state=$aeAfLockState")
                            }
                        }
                        .launchIn(sourceScope)

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
                focalLengthFactor = null
                Log.i(TAG, "[D3-T3] focalLengthFactor reset to null on unbind")
            }
            currentLifecycleOwner = null

            // P08-F4-FIX-v2 AI-4: Clear detection path on camera session end (SSOT §4.4.2)
            liveDetector = null
            onDetectionResult = null

            // Release SurfaceTexture resources
            previewSurface?.release()
            previewSurfaceTexture?.release()
            previewSurface = null
            previewSurfaceTexture = null
            Log.d(TAG, "Preview Surface resources released")

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
    /**
     * v2.12.0-15 SV-S5a-R2: Expose CameraControl for CameraXFocusController.
     * Lifecycle-safe: returns null if camera is not yet initialized.
     */
    fun getCameraControl(): androidx.camera.core.CameraControl? {
        return if (::camera.isInitialized) camera.cameraControl else null
    }

    /**
     * v2.12.0-15 SV-S5-Final: Expose MeteringPointFactory for CameraXFocusController DI binding.
     * Lifecycle-safe: returns null if PreviewView is not yet set.
     */
    fun getMeteringPointFactory(): androidx.camera.core.MeteringPointFactory? {
        return previewView?.meteringPointFactory
    }

    /**
     * D3-T3: Return hardware focal length factor (focalLengthMm / sensorWidthMm).
     * Multiply by previewWidth to obtain focalLengthPixels for ImuCompensator.
     * Returns null if camera is not yet bound or CameraCharacteristics are unavailable.
     */
    fun getFocalLengthFactor(): Float? = focalLengthFactor

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
