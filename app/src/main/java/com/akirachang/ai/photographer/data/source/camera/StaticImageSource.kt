package com.akirachang.ai.photographer.data.source.camera

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.akirachang.ai.photographer.data.local.TestImageProvider
import timber.log.Timber
import javax.inject.Inject

/**
 * Static Image Source for Emulator/Testing
 *
 * Uses static test images to simulate camera preview stream.
 * Emits test images every 2 seconds for easy observation and composition analysis.
 */
class StaticImageSource @Inject constructor(
    private val testImageProvider: TestImageProvider
) : CameraInputSource {
    companion object {
        private const val TAG = "StaticImageSource"
    }

    private var isActive = false

    override fun getPreviewStream(): Flow<Bitmap> = flow {
        isActive = true
        Log.i(TAG, "getPreviewStream() called - Starting preview stream with test images")
        Timber.d("StaticImageSource: Starting preview stream with test images")

        var frameCount = 0
        while (isActive) {
            Log.d(TAG, "Fetching next test image (frame #$frameCount)...")
            val bitmap = testImageProvider.getNextTestImage()

            if (bitmap != null) {
                Log.i(TAG, "Emitting test image: ${bitmap.width}x${bitmap.height} (frame #$frameCount)")
                emit(bitmap)
                Timber.d("StaticImageSource: Emitted test image ${bitmap.width}x${bitmap.height}")
                frameCount++
            } else {
                Log.e(TAG, "TestImageProvider returned null bitmap!")
            }

            // Slow down for observation: 2 seconds per image (vs 30 FPS = 33ms)
            // v2.5.3: Reverted to 2s to prevent ML Kit memory exhaustion crash
            // Note: 7s delay caused SIGSEGV in libmlkitcommonpipeline.so on emulator
            Log.d(TAG, "Waiting 2 seconds before next frame...")
            delay(2000)
        }

        Log.w(TAG, "Preview stream ended - isActive = false")
    }

    override suspend fun takePicture(): Result<Bitmap> {
        Timber.d("StaticImageSource: Taking picture from test image")

        return testImageProvider.getTestImage("portrait_good_composition.jpg")?.let {
            Result.success(it)
        } ?: Result.failure(Exception("Failed to load test image"))
    }

    override fun stopPreview() {
        Log.i(TAG, "stopPreview() called")
        isActive = false
        Timber.d("StaticImageSource: Preview stopped")
    }

    override suspend fun setZoomRatio(ratio: Float): Result<Unit> {
        Timber.d("StaticImageSource: Zoom ratio set to $ratio (no-op for static images)")
        return Result.success(Unit)
    }

    override suspend fun switchCamera() {
        Log.i(TAG, "switchCamera() called - no-op for static images")
        Timber.d("StaticImageSource: Camera switch requested (no-op for test images)")
        // 模擬器使用測試圖片，沒有前後鏡頭區別，所以是空實現
    }
}
