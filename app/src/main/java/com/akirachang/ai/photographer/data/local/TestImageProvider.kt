package com.akirachang.ai.photographer.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提供測試圖片用於模擬器開發
 * 在實機上不會被使用
 */
@Singleton
class TestImageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TestImageProvider"
    }

    private val testImages = listOf(
        "portrait_good_composition.jpg",
        "portrait_bad_headroom.jpg",
        "portrait_off_center.jpg",
        "landscape_symmetry.jpg",
        "landscape_rule_of_thirds.jpg"
    )

    private var currentIndex = 0

    /**
     * 從 assets 載入測試圖片
     * v2.5.3: Enhanced logging with expected score information
     */
    suspend fun getNextTestImage(): Bitmap? = withContext(Dispatchers.IO) {
        val imageName = testImages[currentIndex]

        // v2.5.3: Structured logging for test identification
        Log.i("TestImageCycle", "=== Loading Test Image ===")
        Log.i("TestImageCycle", "Index: $currentIndex / ${testImages.size}")
        Log.i("TestImageCycle", "Name: $imageName")

        try {
            Log.d(TAG, "Attempting to open: test_images/$imageName")

            val bitmap = context.assets.open("test_images/$imageName").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

            if (bitmap != null) {
                Log.i("TestImageCycle", "✓ Loaded: $imageName (${bitmap.width}x${bitmap.height})")

                // v2.5.3: Log expected scores for validation
                logExpectedScores(imageName)

                currentIndex = (currentIndex + 1) % testImages.size
                bitmap
            } else {
                Log.e("TestImageCycle", "BitmapFactory returned null for: $imageName")
                Timber.e("Failed to decode bitmap: $imageName")
                currentIndex = (currentIndex + 1) % testImages.size
                createFallbackTestImage()
            }
        } catch (e: Exception) {
            Log.e("TestImageCycle", "Failed to load: $imageName", e)
            Timber.e(e, "Failed to load test image")
            currentIndex = (currentIndex + 1) % testImages.size
            createFallbackTestImage()
        }
    }

    /**
     * v2.5.3: Log expected composition scores for test validation
     */
    private fun logExpectedScores(imageName: String) {
        when (imageName) {
            "portrait_good_composition.jpg" -> {
                Log.d("TestImageCycle", "  Expected: Eye≥0.9, Headroom≥0.9, Thirds≥0.8 → Overall≥0.75")
            }
            "portrait_bad_headroom.jpg" -> {
                Log.d("TestImageCycle", "  Expected: Headroom 0.4-0.6 → Overall 0.4-0.6")
            }
            "portrait_off_center.jpg" -> {
                Log.d("TestImageCycle", "  Expected: Thirds 0.5-0.7 → Overall 0.5-0.7")
            }
            "landscape_symmetry.jpg" -> {
                Log.d("TestImageCycle", "  Expected: No faces → May not trigger composition analysis")
            }
            "landscape_rule_of_thirds.jpg" -> {
                Log.d("TestImageCycle", "  Expected: No faces → May not trigger composition analysis")
            }
        }
    }

    /**
     * 載入特定測試圖片
     */
    suspend fun getTestImage(name: String): Bitmap? = withContext(Dispatchers.IO) {
        Log.i(TAG, "Loading specific test image: $name")

        try {
            Log.d(TAG, "Attempting to open: test_images/$name")

            val bitmap = context.assets.open("test_images/$name").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

            if (bitmap != null) {
                Log.i(TAG, "Successfully loaded specific image: $name (${bitmap.width}x${bitmap.height})")
                bitmap
            } else {
                Log.e(TAG, "BitmapFactory.decodeStream() returned null for: $name")
                Timber.e("Failed to decode bitmap: $name")
                createFallbackTestImage()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load test image: $name", e)
            Timber.e(e, "Failed to load test image: $name")
            createFallbackTestImage()
        }
    }

    /**
     * 取得所有可用的測試圖片名稱
     */
    fun getAvailableTestImages(): List<String> = testImages

    /**
     * 當無法載入測試圖片時，創建一個簡單的備用圖片
     */
    private fun createFallbackTestImage(): Bitmap {
        val width = 1920
        val height = 1080
        Log.w(TAG, "Creating fallback test image: ${width}x${height}")

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 填充灰色背景
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.GRAY)

        // 在中心繪製一個白色矩形（模擬主體）
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.FILL
        }
        val rectSize = 400f
        canvas.drawRect(
            (width - rectSize) / 2,
            (height - rectSize) / 2,
            (width + rectSize) / 2,
            (height + rectSize) / 2,
            paint
        )

        Timber.d("Created fallback test image: ${width}x${height}")
        Log.i(TAG, "Fallback test image created successfully")
        return bitmap
    }
}
