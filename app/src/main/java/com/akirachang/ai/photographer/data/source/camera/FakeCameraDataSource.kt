package com.akirachang.ai.photographer.data.source.camera

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.akirachang.ai.photographer.data.local.TestImageProvider
import timber.log.Timber
import javax.inject.Inject

/**
 * 模擬器專用的假相機資料源
 * 使用靜態測試圖片模擬相機預覽流
 */
class FakeCameraDataSource @Inject constructor(
    private val testImageProvider: TestImageProvider
) : CameraDataSource {

    private var isActive = false

    override fun getPreviewStream(): Flow<Bitmap> = flow {
        isActive = true
        Timber.d("FakeCameraDataSource: Starting preview stream")

        while (isActive) {
            val bitmap = testImageProvider.getNextTestImage()
            if (bitmap != null) {
                emit(bitmap)
                Timber.d("FakeCameraDataSource: Emitted test image ${bitmap.width}x${bitmap.height}")
            }

            // 模擬 30 FPS
            delay(33)
        }
    }

    override suspend fun takePicture(): Result<Bitmap> {
        Timber.d("FakeCameraDataSource: Taking picture")

        return testImageProvider.getTestImage("portrait_good_composition.jpg")?.let {
            Result.success(it)
        } ?: Result.failure(Exception("Failed to load test image"))
    }

    override fun stopPreview() {
        isActive = false
        Timber.d("FakeCameraDataSource: Preview stopped")
    }

    override suspend fun setZoomRatio(ratio: Float): Result<Unit> {
        Timber.d("FakeCameraDataSource: Zoom ratio set to $ratio (no-op)")
        return Result.success(Unit)
    }
}
