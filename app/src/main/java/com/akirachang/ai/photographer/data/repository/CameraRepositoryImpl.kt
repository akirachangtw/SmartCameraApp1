package com.akirachang.ai.photographer.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.location.Location
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import com.akirachang.ai.photographer.data.source.camera.CameraInputSource
import com.akirachang.ai.photographer.data.source.camera.SmartCameraAnalyzer
import com.akirachang.ai.photographer.domain.camera.ImuCompensator
import com.akirachang.ai.photographer.data.source.ml.ImageAnalyzer
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.akirachang.ai.photographer.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import com.akirachang.ai.photographer.di.ApplicationScope

@Singleton
class CameraRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val cameraInputSource: CameraInputSource,
    private val overlayPreferences: OverlayPreferences,  // v2.9.7 Day 1-2: GPS settings
    @ApplicationScope private val applicationScope: CoroutineScope,  // v2.9.8: Async GPS tagging
    private val imuCompensator: ImuCompensator  // [v2.12.0-23] Shared IMU math engine
) : CameraRepository {

    companion object {
        private const val TAG = "CameraRepositoryImpl"

        // Filtering thresholds
        private const val MIN_OBJECT_CONFIDENCE = 0.5f
        private const val MIN_FACE_SIZE = 0.10f  // Google ML Kit official default value

        // v2.9.7 Day 1-2: GPS location timeout
        // v2.9.8: Reduced from 5s to 2s (prioritize cached location)
        private const val LOCATION_TIMEOUT_MS = 2000L  // 2 seconds

        // v2.9.8: Maximum age for cached location (5 minutes)
        private const val MAX_LOCATION_AGE_MS = 300_000L  // 5 minutes
    }

    // v2.9.7 Day 1-2: FusedLocationProviderClient for GPS
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 偵測結果狀態流
    private val _detectionResults = MutableStateFlow<DetectionResult?>(null)
    override val detectionResults: StateFlow<DetectionResult?> = _detectionResults.asStateFlow()

    // 光影分析結果狀態流 (v2.2.0)
    private val _lightingResults = MutableStateFlow<LightingState>(LightingState.Normal)
    val lightingResults: StateFlow<LightingState> = _lightingResults.asStateFlow()

    // v2.12.0-11: Fixed-focus lens StateFlow — delegates to LiveCameraSource when available
    override val isFixedFocusLens: StateFlow<Boolean> =
        (cameraInputSource as? com.akirachang.ai.photographer.data.source.camera.LiveCameraSource)?.isFixedFocusLens
            ?: MutableStateFlow(false)

    // 分析是否已啟動
    private var isAnalysisStarted = false

    // Custom analyzer instance (v2.2.0)
    private var smartAnalyzer: SmartCameraAnalyzer? = null

    // ML Kit 偵測器（延遲初始化）
    private val faceDetector: FaceDetector by lazy {
        // v2.12.0: LANDMARK_MODE_NONE — prevents "Unknown landmark type" crash on certain devices.
        // Eye-AF degrades gracefully to face bbox center (CameraViewModel:929 fallback).
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(MIN_FACE_SIZE)
            .enableTracking()
            .build()

        FaceDetection.getClient(options).also {
            Log.i(TAG, "[v2.12.0] Face detector initialized: LANDMARK_MODE_NONE (crash-safe)")
        }
    }

    private val objectDetector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        ObjectDetection.getClient(options).also {
            Log.i(TAG, "[v2.12.0] Object detector initialized: STREAM_MODE + multipleObjects + classification")
        }
    }

    private val _cameraController: LifecycleCameraController by lazy {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or
                LifecycleCameraController.IMAGE_ANALYSIS
            )
            imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
        }
    }

    /**
     * 獲取相機預覽流（統一介面）
     * v2.5.1: Camera Input 抽象層整合
     *
     * 委派給 CameraInputSource，自動根據環境提供測試圖片或實機相機預覽
     */
    override fun getPreviewStream(): Flow<Bitmap> {
        Log.d(TAG, "getPreviewStream: Delegating to CameraInputSource")
        return cameraInputSource.getPreviewStream()
    }

    /**
     * 拍攝照片並儲存到 MediaStore
     *
     * v2.6.1 修復: 委派給 CameraInputSource 以支援 StaticImageSource 和 LiveCameraSource
     *
     * 注意：使用 MediaStore API 儲存照片不需要 READ_MEDIA_* 或 WRITE_EXTERNAL_STORAGE 權限
     * - Android 10+ (API 29+) 使用 Scoped Storage，應用可以直接寫入 MediaStore
     * - 只需要 CAMERA 權限即可拍照和儲存
     * - READ_MEDIA_* 權限只在需要讀取其他應用的照片時才需要（例如：顯示圖庫）
     */
    override suspend fun takePicture(): CaptureResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting photo capture via CameraInputSource...")

            // Step 1: 委派給 CameraInputSource 獲取 Bitmap
            val bitmapResult = cameraInputSource.takePicture()

            if (bitmapResult.isFailure) {
                val error = bitmapResult.exceptionOrNull() ?: Exception("Unknown error")
                Log.e(TAG, "Failed to capture bitmap: ${error.message}", error)
                return@withContext CaptureResult.Error(
                    Exception("拍照失敗：${error.message}", error)
                )
            }

            val bitmap = bitmapResult.getOrNull()
            if (bitmap == null) {
                Log.e(TAG, "Bitmap is null despite successful result")
                return@withContext CaptureResult.Error(
                    Exception("拍照失敗：無法獲取圖片數據")
                )
            }

            Log.i(TAG, "Bitmap captured successfully: ${bitmap.width}x${bitmap.height}")

            // v2.9.6 Phase 1: Get rotation for EXIF Orientation
            val rotation = cameraInputSource.getCurrentRotation()
            Log.d(TAG, "Current rotation for EXIF: $rotation")

            // Step 2: 保存 Bitmap 到 MediaStore
            return@withContext saveBitmapToMediaStore(bitmap, rotation)

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in takePicture", e)
            CaptureResult.Error(Exception("拍照過程發生錯誤：${e.message}", e))
        }
    }

    /**
     * 保存 Bitmap 到 MediaStore
     *
     * @param bitmap The bitmap to save
     * @param rotation Device rotation for EXIF Orientation
     * @return CaptureResult with saved URI or error
     */
    private suspend fun saveBitmapToMediaStore(bitmap: Bitmap, rotation: Int): CaptureResult = withContext(Dispatchers.IO) {
        try {
            val name = SimpleDateFormat(Constants.FILENAME_FORMAT, Locale.getDefault())
                .format(System.currentTimeMillis())
            Log.d(TAG, "Saving bitmap to MediaStore with filename: $name")

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${Constants.PHOTOS_DIRECTORY}")
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (uri == null) {
                Log.e(TAG, "Failed to create MediaStore entry")
                return@withContext CaptureResult.Error(
                    Exception("照片保存失敗：無法創建儲存位置")
                )
            }

            Log.d(TAG, "MediaStore URI created: $uri")

            // Write bitmap to MediaStore URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                if (!success) {
                    Log.e(TAG, "Failed to compress bitmap to JPEG")
                    return@withContext CaptureResult.Error(
                        Exception("照片保存失敗：壓縮圖片時發生錯誤")
                    )
                }
                outputStream.flush()
                Log.i(TAG, "Photo saved successfully to: $uri")

                // v2.9.8: Immediately return success result (don't wait for GPS)
                val successResult = CaptureResult.Success(uri)

                // v2.9.8: Asynchronously handle GPS tagging in background
                // This prevents GPS location acquisition (up to 5s) from blocking UI feedback
                applicationScope.launch(Dispatchers.IO) {
                    try {
                        // v2.9.7 Day 1-2: Get GPS location for EXIF
                        val location = getLastLocation()

                        // v2.9.6 Phase 1: Write EXIF Orientation
                        // v2.9.7 Day 1-2: Write EXIF Metadata (Orientation + GPS)
                        writeExifMetadata(uri, rotation, location)
                        Log.d(TAG, "Async GPS EXIF tagging completed for: $uri")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to write EXIF metadata asynchronously (non-fatal)", e)
                    }
                }

                return@withContext successResult
            } ?: run {
                Log.e(TAG, "Failed to open output stream for URI: $uri")
                return@withContext CaptureResult.Error(
                    Exception("照片保存失敗：無法寫入儲存位置")
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to MediaStore", e)
            return@withContext CaptureResult.Error(
                Exception("照片保存過程發生錯誤：${e.message}", e)
            )
        }
    }

    /**
     * Get last known location with timeout
     * v2.9.7 Day 1-2: GPS location for EXIF tagging
     *
     * @return Location if available within timeout, null otherwise
     */
    private suspend fun getLastLocation(): Location? = withContext(Dispatchers.IO) {
        try {
            // Check if GPS tagging is enabled
            val isGpsEnabled = overlayPreferences.isGpsTaggingEnabled.first()
            if (!isGpsEnabled) {
                Log.d(TAG, "GPS tagging disabled in settings")
                return@withContext null
            }

            // Check location permission
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Location permission not granted")
                return@withContext null
            }

            // v2.9.8: Prioritize cached location to reduce latency
            // Try to use last known location if it's recent enough (< 5 minutes old)
            try {
                val lastKnownLocation = fusedLocationClient.lastLocation.await()
                if (lastKnownLocation != null && isLocationRecent(lastKnownLocation)) {
                    Log.i(TAG, "Using cached location (age: ${System.currentTimeMillis() - lastKnownLocation.time}ms): " +
                            "lat=${lastKnownLocation.latitude}, lon=${lastKnownLocation.longitude}")
                    return@withContext lastKnownLocation
                } else if (lastKnownLocation != null) {
                    Log.d(TAG, "Cached location too old (age: ${System.currentTimeMillis() - lastKnownLocation.time}ms), requesting fresh location...")
                } else {
                    Log.d(TAG, "No cached location available, requesting current location...")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get cached location, falling back to current location", e)
            }

            // Cached location not available or too old, request current location with timeout
            Log.d(TAG, "Requesting current location with ${LOCATION_TIMEOUT_MS}ms timeout...")
            val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                val cancellationToken = CancellationTokenSource()
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cancellationToken.token
                    ).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get current location", e)
                    null
                }
            }

            if (location != null) {
                Log.i(TAG, "Location obtained: lat=${location.latitude}, lon=${location.longitude}, alt=${location.altitude}")
            } else {
                Log.w(TAG, "Location not available within timeout")
            }

            return@withContext location

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when accessing location", e)
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting location", e)
            return@withContext null
        }
    }

    /**
     * Check if cached location is recent enough to use
     * v2.9.8: Helper function for cached location validation
     *
     * @param location The cached location to check
     * @param maxAgeMs Maximum age in milliseconds (default: 5 minutes)
     * @return true if location is recent enough, false otherwise
     */
    private fun isLocationRecent(location: Location, maxAgeMs: Long = MAX_LOCATION_AGE_MS): Boolean {
        val age = System.currentTimeMillis() - location.time
        return age < maxAgeMs
    }

    /**
     * Write EXIF metadata (Orientation + GPS) to saved JPEG
     * v2.9.6 Phase 1: EXIF Orientation support
     * v2.9.7 Day 1-2: GPS EXIF tagging
     *
     * @param uri The URI of the saved image
     * @param rotation Device rotation for orientation tag
     * @param location GPS location for GPS tags (nullable)
     */
    private fun writeExifMetadata(uri: Uri, rotation: Int, location: Location?) {
        val exifOrientation = when (rotation) {
            Surface.ROTATION_0 -> ExifInterface.ORIENTATION_NORMAL      // 1
            Surface.ROTATION_90 -> ExifInterface.ORIENTATION_ROTATE_90  // 6
            Surface.ROTATION_180 -> ExifInterface.ORIENTATION_ROTATE_180 // 3
            Surface.ROTATION_270 -> ExifInterface.ORIENTATION_ROTATE_270 // 8
            else -> ExifInterface.ORIENTATION_NORMAL
        }

        Log.d(TAG, "Writing EXIF metadata: rotation=$rotation → exif=$exifOrientation, hasLocation=${location != null}")

        context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
            val exif = ExifInterface(pfd.fileDescriptor)

            // Write orientation
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation.toString())
            Log.d(TAG, "EXIF Orientation set: $exifOrientation")

            // Write GPS data if available
            if (location != null) {
                try {
                    // Set GPS latitude and longitude
                    exif.setGpsInfo(location)

                    // Set GPS altitude
                    if (location.hasAltitude()) {
                        val altitude = location.altitude
                        val altitudeRef = if (altitude >= 0) "0" else "1"  // 0 = above sea level, 1 = below
                        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "${Math.abs(altitude)}/1")
                        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, altitudeRef)
                        Log.d(TAG, "GPS Altitude set: ${altitude}m (ref=$altitudeRef)")
                    }

                    Log.i(TAG, "GPS EXIF tags written: lat=${location.latitude}, lon=${location.longitude}, alt=${if (location.hasAltitude()) location.altitude else "N/A"}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to write GPS EXIF tags (non-fatal)", e)
                }
            } else {
                Log.d(TAG, "No GPS location available, skipping GPS EXIF tags")
            }

            // Save all attributes
            exif.saveAttributes()
            Log.i(TAG, "EXIF metadata saved successfully")
        } ?: run {
            Log.e(TAG, "Failed to open FileDescriptor for EXIF writing")
            throw Exception("Cannot open file descriptor for URI: $uri")
        }
    }

    /**
     * Switch camera between front and back
     *
     * v2.6.1: Changed to suspend function and delegates to CameraInputSource
     * MUST execute on Main thread to satisfy CameraX requirements
     */
    override suspend fun switchCamera() = withContext(Dispatchers.Main) {
        // Delegate to CameraInputSource (LiveCameraSource or StaticImageSource)
        // This will handle camera rebinding on Main thread
        Log.d(TAG, "Delegating camera switch to CameraInputSource")
        cameraInputSource.switchCamera()

        // Update legacy CameraController for compatibility
        _cameraController.cameraSelector = if (_cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    /**
     * Get current camera lens (platform-agnostic)
     *
     * v2.5.1: Returns CameraLens instead of exposing CameraSelector
     */
    override fun getCurrentCamera(): CameraLens {
        return when (_cameraController.cameraSelector) {
            CameraSelector.DEFAULT_FRONT_CAMERA -> CameraLens.FRONT
            else -> CameraLens.BACK
        }
    }

    /**
     * Select camera lens (front or back)
     *
     * v2.5.1: Uses platform-agnostic CameraLens
     */
    override fun selectCamera(lens: CameraLens) {
        _cameraController.cameraSelector = when (lens) {
            CameraLens.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraLens.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        }
        Log.d(TAG, "Camera switched to: $lens")
    }


    override fun setFlashMode(flashMode: Int) {
        _cameraController.imageCaptureFlashMode = flashMode
    }

    override fun getFlashMode(): Int {
        return _cameraController.imageCaptureFlashMode
    }

    /**
     * 啟用 HDR 模式（透過曝光補償）
     *
     * v2.3.0: 逆光場景自動 HDR
     * v2.6.2: Delegates to CameraInputSource for proper CameraControl access
     *
     * 注意：CameraX 沒有直接 HDR 控制，使用曝光補償作為替代
     */
    override fun enableHDRMode() {
        Log.d(TAG, "Delegating HDR enable to CameraInputSource")
        val result = cameraInputSource.enableHDR()
        if (result.isFailure) {
            Log.e(TAG, "Failed to enable HDR via CameraInputSource: ${result.exceptionOrNull()?.message}")
        }
    }

    /**
     * 停用 HDR 模式（重設曝光補償）
     *
     * v2.3.0: 逆光場景自動 HDR
     * v2.6.2: Delegates to CameraInputSource for proper CameraControl access
     */
    override fun disableHDRMode() {
        Log.d(TAG, "Delegating HDR disable to CameraInputSource")
        val result = cameraInputSource.disableHDR()
        if (result.isFailure) {
            Log.e(TAG, "Failed to disable HDR via CameraInputSource: ${result.exceptionOrNull()?.message}")
        }
    }

    /**
     * 檢查是否應啟用自動 HDR
     */
    override fun shouldEnableAutoHDR(lightingState: LightingState): Boolean {
        return when (lightingState) {
            is LightingState.HighContrast -> true  // High contrast detected → enable HDR
            is LightingState.Backlight -> true     // Backlight detected → enable HDR
            else -> false
        }
    }

    override fun startImageAnalysis() {
        if (isAnalysisStarted) {
            Log.d(TAG, "Image analysis already started")
            return
        }

        try {
            // v2.2.0: Use custom SmartCameraAnalyzer for integrated ML Kit + Lighting analysis
            smartAnalyzer = SmartCameraAnalyzer(
                faceDetector = faceDetector,
                objectDetector = objectDetector,
                analyzeLightingUseCase = analyzeLightingUseCase,
                onDetectionResult = { detectionResult ->
                    // Update detection state on main thread
                    _detectionResults.value = detectionResult
                },
                onLightingResult = { lightingState ->
                    // Update lighting state on main thread
                    _lightingResults.value = lightingState
                },
                minObjectConfidence = MIN_OBJECT_CONFIDENCE,
            )

            // Attach analyzer to camera controller
            _cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                smartAnalyzer!!
            )

            isAnalysisStarted = true
            Log.i(TAG, "Image analysis started with SmartCameraAnalyzer (ML Kit + Lighting)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start image analysis", e)
        }
    }

    override fun stopImageAnalysis() {
        if (!isAnalysisStarted) {
            Log.d(TAG, "Image analysis not started")
            return
        }

        try {
            // Cleanup analyzer resources (v2.2.0)
            smartAnalyzer?.cleanup()
            smartAnalyzer = null

            // 清除分析器
            _cameraController.clearImageAnalysisAnalyzer()
            _detectionResults.value = null
            _lightingResults.value = LightingState.Normal
            isAnalysisStarted = false
            Log.i(TAG, "Image analysis stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop image analysis", e)
        }
    }

    /**
     * 使用 ML Kit 進行人臉偵測
     * v2.5.1 Phase 7: Bitmap-based analysis
     */
    override suspend fun detectFaces(image: com.google.mlkit.vision.common.InputImage): List<com.google.mlkit.vision.face.Face> =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        if (continuation.isActive) {
                            continuation.resume(faces)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Face detection failed", exception)
                        if (continuation.isActive) {
                            continuation.resume(emptyList())
                        }
                    }
            }
        }

    /**
     * 使用 ML Kit 進行物件偵測
     * v2.5.1 Phase 7: Bitmap-based analysis
     */
    override suspend fun detectObjects(image: com.google.mlkit.vision.common.InputImage): List<com.google.mlkit.vision.objects.DetectedObject> =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                objectDetector.process(image)
                    .addOnSuccessListener { objects ->
                        // Filter by confidence
                        val filteredObjects = objects.filter { obj ->
                            obj.trackingId != null &&
                            obj.labels.any { it.confidence >= MIN_OBJECT_CONFIDENCE }
                        }

                        if (continuation.isActive) {
                            continuation.resume(filteredObjects)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Object detection failed", exception)
                        if (continuation.isActive) {
                            continuation.resume(emptyList())
                        }
                    }
            }
        }

    /**
     * 綁定相機到生命週期
     * v2.6.1: Delegates to LiveCameraSource if available
     */
    override fun bindToLifecycle(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        val liveCameraSource = cameraInputSource as? com.akirachang.ai.photographer.data.source.camera.LiveCameraSource
        if (liveCameraSource != null) {
            // 實機：使用 LiveCameraSource 綁定（管理預覽 + 拍照）
            liveCameraSource.bindToLifecycle(lifecycleOwner)
            Log.i(TAG, "Camera bound to lifecycle via LiveCameraSource (real device)")
        } else {
            // 模擬器：StaticImageSource 提供預覽，但 LifecycleCameraController 仍需綁定以支持拍照
            _cameraController.bindToLifecycle(lifecycleOwner)
            Log.i(TAG, "LifecycleCameraController bound to lifecycle (emulator with StaticImageSource)")
        }
    }

    /**
     * Set PreviewView surface provider for native preview rendering
     *
     * v2.8.6 Phase 2: PreviewView migration for Graphics memory fix
     *
     * Delegates to CameraInputSource implementation:
     * - LiveCameraSource: Connects to CameraX Preview UseCase
     * - StaticImageSource: No-op (bitmap-only)
     */
    override fun setPreviewSurfaceProvider(surfaceProvider: androidx.camera.core.Preview.SurfaceProvider?) {
        Log.d(TAG, "Delegating setPreviewSurfaceProvider to CameraInputSource")
        cameraInputSource.setPreviewSurfaceProvider(surfaceProvider)
    }

    /**
     * Set PreviewView reference for accurate coordinate transformation
     *
     * v2.11.1 P0-1: Coordinate system unification (Bug fix for out-of-preview tap focus)
     *
     * Delegates to CameraInputSource implementation:
     * - LiveCameraSource: Stores PreviewView reference for accurate metering point creation
     * - StaticImageSource: No-op (test images don't support autofocus)
     */
    override fun setPreviewView(previewView: androidx.camera.view.PreviewView?) {
        Log.d(TAG, "[v2.11.1 P0-1] Delegating setPreviewView to CameraInputSource")
        cameraInputSource.setPreviewView(previewView)
    }

    /**
     * Check if native preview is supported
     *
     * v2.8.6 Phase 2: PreviewView migration
     *
     * @return true for LiveCameraSource (real device), false for StaticImageSource (emulator)
     */
    override fun supportsNativePreview(): Boolean {
        return cameraInputSource.supportsNativePreview()
    }

    /**
     * Set aspect ratio mode and rebind camera
     * v2.10.0: UseCaseGroup + ViewPort architecture
     *
     * This triggers camera rebind to apply the new aspect ratio via ViewPort.
     */
    override suspend fun setAspectRatioMode(mode: com.akirachang.ai.photographer.presentation.camera.model.AspectRatioMode) {
        Log.d(TAG, "[v2.10.0] Setting aspect ratio mode: ${mode.displayName}")

        // Only LiveCameraSource supports dynamic aspect ratio
        val liveCameraSource = cameraInputSource as? com.akirachang.ai.photographer.data.source.camera.LiveCameraSource
        if (liveCameraSource == null) {
            Log.w(TAG, "Aspect ratio mode not supported by current camera source")
            return
        }

        // Set aspect ratio and trigger rebind
        liveCameraSource.setAspectRatioMode(mode)
        Log.i(TAG, "[v2.10.0] Aspect ratio mode set successfully: ${mode.displayName}")
    }

    /**
     * Focus on a specific point in the preview
     * v2.9.9: Tap-to-Focus support
     *
     * Delegates to CameraInputSource (LiveCameraSource uses FocusMeteringAction, StaticImageSource is no-op)
     */
    override suspend fun focusOnPoint(
        x: Float,
        y: Float,
        viewWidth: Int,
        viewHeight: Int
    ): Result<Unit> {
        return cameraInputSource.focusOnPoint(x, y, viewWidth, viewHeight)
    }

    /**
     * Lock AE/AF at a specific point (long press)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * Delegates to CameraInputSource (LiveCameraSource uses persistent FocusMeteringAction, StaticImageSource is no-op)
     */
    override suspend fun lockAEAF(
        x: Float,
        y: Float,
        viewWidth: Int,
        viewHeight: Int
    ): Result<Unit> {
        return cameraInputSource.lockAEAF(x, y, viewWidth, viewHeight)
    }

    /**
     * Cancel focus and metering (unlock AE/AF)
     * v2.9.9 Day 1-2: Traycer v2 AE/AF Lock support
     *
     * Delegates to CameraInputSource (LiveCameraSource uses cancelFocusAndMetering(), StaticImageSource is no-op)
     */
    override suspend fun cancelFocusAndMetering(): Result<Unit> {
        return cameraInputSource.cancelFocusAndMetering()
    }

    /** Set preview size (interface compliance — no-op after CoordinateMapper retirement) */
    override fun setPreviewSize(width: Int, height: Int) {
        Log.d(TAG, "setPreviewSize no-op (CoordinateMapper retired)")
    }

    /**
     * Read EXIF metadata from captured photo
     * v2.11.0 Phase 3: EXIF information display feature
     *
     * Extracts comprehensive EXIF metadata including file info, GPS data,
     * camera information, and capture parameters.
     *
     * @param uri Content URI of the captured photo
     * @return ExifInfo object with all metadata, or null if reading fails
     */
    override suspend fun readExifInfo(uri: Uri): com.akirachang.ai.photographer.domain.model.ExifInfo? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val exif = ExifInterface(pfd.fileDescriptor)

                // Read file basic information
                val (fileName, fileSize) = context.contentResolver.query(
                    uri,
                    arrayOf(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.SIZE
                    ),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

                        val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "Unknown"
                        val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L

                        name to size
                    } else {
                        "Unknown" to 0L
                    }
                } ?: ("Unknown" to 0L)

                // Format file size
                val fileSizeFormatted = when {
                    fileSize < 1024 -> "${fileSize} B"
                    fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                    else -> String.format("%.1f MB", fileSize / (1024f * 1024f))
                }

                // Read resolution
                val width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)?.toIntOrNull() ?: 0
                val height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)?.toIntOrNull() ?: 0
                val resolution = "$width x $height"

                // Read GPS information
                val latLong = exif.latLong
                val latitude = latLong?.get(0)
                val longitude = latLong?.get(1)
                val altitude = exif.getAltitude(0.0).takeIf { it != 0.0 }
                val hasGpsInfo = latitude != null && longitude != null

                // Read camera information
                val make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "Unknown"
                val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: "Unknown"
                val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE) ?: "SmartCameraApp"

                // Read orientation
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val orientationDescription = when (orientation) {
                    ExifInterface.ORIENTATION_NORMAL -> "Normal (0°)"
                    ExifInterface.ORIENTATION_ROTATE_90 -> "Rotated 90° CW"
                    ExifInterface.ORIENTATION_ROTATE_180 -> "Rotated 180°"
                    ExifInterface.ORIENTATION_ROTATE_270 -> "Rotated 270° CW"
                    else -> "Unknown"
                }

                // Read capture timestamp
                val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)

                Log.d(TAG, "EXIF read: file=$fileName, size=$fileSizeFormatted, res=$resolution, gps=$hasGpsInfo")

                com.akirachang.ai.photographer.domain.model.ExifInfo(
                    fileName = fileName,
                    fileSizeFormatted = fileSizeFormatted,
                    resolution = resolution,
                    dateTime = dateTime,
                    hasGpsInfo = hasGpsInfo,
                    latitude = latitude,
                    longitude = longitude,
                    altitude = altitude,
                    make = make,
                    model = model,
                    software = software,
                    orientationDescription = orientationDescription
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read EXIF info from $uri", e)
            null
        }
    }

    override fun getCameraControl(): androidx.camera.core.CameraControl? {
        // v2.12.0-15 SV-S5a-R2: Delegate to LiveCameraSource if available
        return (cameraInputSource as? com.akirachang.ai.photographer.data.source.camera.LiveCameraSource)
            ?.getCameraControl()
    }

    override fun getMeteringPointFactory(): androidx.camera.core.MeteringPointFactory? {
        // v2.12.0-15 SV-S5-Final: Delegate to LiveCameraSource if available
        return (cameraInputSource as? com.akirachang.ai.photographer.data.source.camera.LiveCameraSource)
            ?.getMeteringPointFactory()
    }

    override fun getFocalLengthFactor(): Float? {
        // D3-T3: Delegate to LiveCameraSource; returns null for StaticImageSource
        return (cameraInputSource as? com.akirachang.ai.photographer.data.source.camera.LiveCameraSource)
            ?.getFocalLengthFactor()
    }

}
