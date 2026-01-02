package com.akirachang.ai.photographer.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
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
import com.akirachang.ai.photographer.data.source.camera.CameraInputSource
import com.akirachang.ai.photographer.data.source.camera.SmartCameraAnalyzer
import com.akirachang.ai.photographer.domain.model.CameraLens
import com.akirachang.ai.photographer.domain.model.CaptureResult
import com.akirachang.ai.photographer.domain.model.DetectionResult
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCase
import com.akirachang.ai.photographer.presentation.camera.model.LightingState
import com.akirachang.ai.photographer.util.Constants
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CameraRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyzeLightingUseCase: AnalyzeLightingUseCase,
    private val cameraInputSource: CameraInputSource
) : CameraRepository {

    companion object {
        private const val TAG = "CameraRepositoryImpl"

        // Filtering thresholds
        private const val MIN_OBJECT_CONFIDENCE = 0.5f
        private const val MIN_FACE_SIZE = 0.10f  // Google ML Kit official default value
    }

    // 偵測結果狀態流
    private val _detectionResults = MutableStateFlow<DetectionResult?>(null)
    override val detectionResults: StateFlow<DetectionResult?> = _detectionResults.asStateFlow()

    // 光影分析結果狀態流 (v2.2.0)
    private val _lightingResults = MutableStateFlow<LightingState>(LightingState.Normal)
    val lightingResults: StateFlow<LightingState> = _lightingResults.asStateFlow()

    // 分析是否已啟動
    private var isAnalysisStarted = false

    // Custom analyzer instance (v2.2.0)
    private var smartAnalyzer: SmartCameraAnalyzer? = null

    // ML Kit 偵測器（延遲初始化）
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(MIN_FACE_SIZE)
            .enableTracking()
            .build()

        FaceDetection.getClient(options).also {
            Log.i(TAG, "Face detector initialized with LANDMARK_MODE_ALL for v2.3.0 eye position analysis")
        }
    }

    private val objectDetector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        ObjectDetection.getClient(options).also {
            Log.i(TAG, "Object detector initialized")
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

            // Step 2: 保存 Bitmap 到 MediaStore
            return@withContext saveBitmapToMediaStore(bitmap)

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in takePicture", e)
            CaptureResult.Error(Exception("拍照過程發生錯誤：${e.message}", e))
        }
    }

    /**
     * 保存 Bitmap 到 MediaStore
     *
     * @param bitmap The bitmap to save
     * @return CaptureResult with saved URI or error
     */
    private suspend fun saveBitmapToMediaStore(bitmap: Bitmap): CaptureResult = withContext(Dispatchers.IO) {
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
                return@withContext CaptureResult.Success(uri)
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
                minObjectConfidence = MIN_OBJECT_CONFIDENCE
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

}
