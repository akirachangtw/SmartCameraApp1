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
     * 注意：使用 MediaStore API 儲存照片不需要 READ_MEDIA_* 或 WRITE_EXTERNAL_STORAGE 權限
     * - Android 10+ (API 29+) 使用 Scoped Storage，應用可以直接寫入 MediaStore
     * - 只需要 CAMERA 權限即可拍照和儲存
     * - READ_MEDIA_* 權限只在需要讀取其他應用的照片時才需要（例如：顯示圖庫）
     */
    override suspend fun takePicture(): CaptureResult = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "Starting photo capture...")

            suspendCancellableCoroutine<CaptureResult> { continuation ->
                try {
                    val name = SimpleDateFormat(Constants.FILENAME_FORMAT, Locale.getDefault())
                        .format(System.currentTimeMillis())
                    Log.d(TAG, "Photo filename: $name")

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${Constants.PHOTOS_DIRECTORY}")
                        }
                    }

                    val outputOptions = ImageCapture.OutputFileOptions
                        .Builder(
                            context.contentResolver,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        .build()

                    // 使用 ContextCompat.getMainExecutor 以獲得更好的相容性
                    val executor = ContextCompat.getMainExecutor(context)

                    Log.d(TAG, "Initiating camera capture...")
                    _cameraController.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                try {
                                    val savedUri = output.savedUri
                                    if (savedUri != null) {
                                        Log.d(TAG, "Photo saved successfully: $savedUri")
                                        if (continuation.isActive) {
                                            continuation.resume(CaptureResult.Success(savedUri))
                                        }
                                    } else {
                                        Log.e(TAG, "Photo saved but URI is null")
                                        if (continuation.isActive) {
                                            continuation.resume(
                                                CaptureResult.Error(
                                                    Exception("照片保存失敗：無法獲取保存位置")
                                                )
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in onImageSaved callback", e)
                                    if (continuation.isActive) {
                                        continuation.resume(CaptureResult.Error(e))
                                    }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                                if (continuation.isActive) {
                                    continuation.resume(
                                        CaptureResult.Error(
                                            Exception("拍照失敗：${exception.message}", exception)
                                        )
                                    )
                                }
                            }
                        }
                    )

                    // 設置取消監聽器
                    continuation.invokeOnCancellation {
                        Log.w(TAG, "Photo capture was cancelled")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error setting up photo capture", e)
                    if (continuation.isActive) {
                        continuation.resume(
                            CaptureResult.Error(
                                Exception("設置拍照失敗：${e.message}", e)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in takePicture", e)
            CaptureResult.Error(Exception("拍照過程發生錯誤：${e.message}", e))
        }
    }

    override fun switchCamera() {
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
     *
     * 注意：CameraX 沒有直接 HDR 控制，使用曝光補償作為替代
     */
    override fun enableHDRMode() {
        try {
            val cameraControl = _cameraController.cameraControl
            if (cameraControl != null) {
                // 設定曝光補償 +1 stop
                cameraControl.setExposureCompensationIndex(1)
                Log.i(TAG, "HDR mode enabled (exposure compensation +1)")
            } else {
                Log.w(TAG, "Cannot enable HDR: CameraControl not available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable HDR mode", e)
        }
    }

    /**
     * 停用 HDR 模式（重設曝光補償）
     *
     * v2.3.0: 逆光場景自動 HDR
     */
    override fun disableHDRMode() {
        try {
            val cameraControl = _cameraController.cameraControl
            if (cameraControl != null) {
                // 重設曝光補償為 0
                cameraControl.setExposureCompensationIndex(0)
                Log.i(TAG, "HDR mode disabled (exposure compensation 0)")
            } else {
                Log.w(TAG, "Cannot disable HDR: CameraControl not available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable HDR mode", e)
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

}
