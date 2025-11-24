package local.mylab.smartcameraapp1.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.DetectedObject
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
import local.mylab.smartcameraapp1.domain.model.CaptureResult
import local.mylab.smartcameraapp1.domain.model.DetectionResult
import local.mylab.smartcameraapp1.domain.repository.CameraRepository
import local.mylab.smartcameraapp1.util.Constants
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CameraRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraRepository {

    companion object {
        private const val TAG = "CameraRepositoryImpl"

        // Filtering thresholds
        private const val MIN_OBJECT_CONFIDENCE = 0.5f
        private const val MIN_FACE_SIZE = 0.10f  // v1.4.1: Reduced from 0.15f to detect smaller/distant faces
    }

    // 偵測結果狀態流
    private val _detectionResults = MutableStateFlow<DetectionResult?>(null)
    override val detectionResults: StateFlow<DetectionResult?> = _detectionResults.asStateFlow()

    // 分析是否已啟動
    private var isAnalysisStarted = false

    // ML Kit 偵測器（延遲初始化）
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(MIN_FACE_SIZE)
            .enableTracking()
            .build()

        FaceDetection.getClient(options).also {
            Log.i(TAG, "Face detector initialized")
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

    override fun getCameraController(): LifecycleCameraController = _cameraController

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

    override fun getCurrentCameraSelector(): CameraSelector {
        return _cameraController.cameraSelector
    }

    override fun setFlashMode(flashMode: Int) {
        _cameraController.imageCaptureFlashMode = flashMode
    }

    override fun getFlashMode(): Int {
        return _cameraController.imageCaptureFlashMode
    }

    override fun startImageAnalysis() {
        if (isAnalysisStarted) {
            Log.d(TAG, "Image analysis already started")
            return
        }

        try {
            // 使用官方 MlKitAnalyzer 並配置為 VIEW_REFERENCED 座標系統
            // 這樣 ML Kit 會自動將座標轉換到 PreviewView 空間，無需手動轉換
            val mlKitAnalyzer = MlKitAnalyzer(
                listOf(faceDetector, objectDetector),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(context)
            ) { result: MlKitAnalyzer.Result ->
                // 從結果中提取人臉與物件
                val faces = result.getValue(faceDetector) ?: emptyList()
                val objects = result.getValue(objectDetector) ?: emptyList()

                // 過濾低信心度物件
                val filteredObjects = objects.filter { obj ->
                    val confidence = obj.labels.firstOrNull()?.confidence ?: 0f
                    confidence >= MIN_OBJECT_CONFIDENCE
                }

                // 更新偵測結果
                // 注意：座標已經在 View 空間，無需儲存影像尺寸或旋轉資訊
                _detectionResults.value = DetectionResult(
                    faces = faces,
                    objects = filteredObjects,
                    imageWidth = 0,  // 不再需要
                    imageHeight = 0,  // 不再需要
                    rotationDegrees = 0,  // 不再需要
                    timestamp = System.currentTimeMillis()
                )

                Log.d(
                    TAG,
                    "Detection updated: ${faces.size} faces, ${filteredObjects.size} objects (${objects.size - filteredObjects.size} filtered)"
                )
            }

            // 將 MlKitAnalyzer 附加到相機控制器
            _cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                mlKitAnalyzer
            )

            isAnalysisStarted = true
            Log.i(TAG, "Image analysis started with MlKitAnalyzer (VIEW_REFERENCED coordinates)")
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
            // 清除分析器
            _cameraController.clearImageAnalysisAnalyzer()
            _detectionResults.value = null
            isAnalysisStarted = false
            Log.i(TAG, "Image analysis stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop image analysis", e)
        }
    }
}
