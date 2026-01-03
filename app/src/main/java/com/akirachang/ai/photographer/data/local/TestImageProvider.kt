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
 * v2.7.0: Dynamic image discovery from assets
 * 在實機上不會被使用
 */
@Singleton
class TestImageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TestImageProvider"
        private const val TEST_IMAGES_DIR = "test_images"
    }

    /**
     * v2.7.0: Lazy-loaded dynamic image list
     * Automatically discovers all test images from assets directory
     * No manual maintenance required when adding new images
     */
    private val testImages: List<String> by lazy {
        discoverTestImages()
    }

    private var currentIndex = 0

    /**
     * v2.7.0: Dynamically discover test images from assets directory
     * Replaces hardcoded list with runtime scanning
     */
    private fun discoverTestImages(): List<String> {
        return try {
            val imageFiles = context.assets.list(TEST_IMAGES_DIR)
                ?.filter { it.endsWith(".jpg", ignoreCase = true) || it.endsWith(".png", ignoreCase = true) }
                ?.sorted() // Alphabetical order for consistency
                ?: emptyList()

            Log.i(TAG, "Discovered ${imageFiles.size} test images")
            imageFiles.forEach { Log.d(TAG, "  - $it") }

            if (imageFiles.isEmpty()) {
                Log.w(TAG, "No test images found in assets/$TEST_IMAGES_DIR")
                Log.w(TAG, "Fallback images will be used")
            }

            imageFiles
        } catch (e: Exception) {
            Log.e(TAG, "Failed to discover test images", e)
            Timber.e(e, "Test image discovery failed")
            emptyList()
        }
    }

    /**
     * 從 assets 載入測試圖片
     * v2.5.3: Enhanced logging with expected score information
     * v2.7.0: Works with dynamically discovered image list
     */
    suspend fun getNextTestImage(): Bitmap? = withContext(Dispatchers.IO) {
        // v2.7.0: Handle empty image list gracefully
        if (testImages.isEmpty()) {
            Log.w(TAG, "No test images available, returning fallback")
            return@withContext createFallbackTestImage()
        }

        val imageName = testImages[currentIndex]

        // v2.5.3: Structured logging for test identification
        Log.i("TestImageCycle", "=== Loading Test Image ===")
        Log.i("TestImageCycle", "Index: $currentIndex / ${testImages.size}")
        Log.i("TestImageCycle", "Name: $imageName")

        try {
            Log.d(TAG, "Attempting to open: $TEST_IMAGES_DIR/$imageName")

            val bitmap = context.assets.open("$TEST_IMAGES_DIR/$imageName").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

            if (bitmap != null) {
                Log.i("TestImageCycle", "✓ Loaded: $imageName (${bitmap.width}x${bitmap.height})")

                // v2.7.0: Generic categorization (replaces hardcoded expected scores)
                logImageCategory(imageName)

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
     * v2.7.0: Categorize image by filename prefix for logging
     * Generic categorization suitable for any test image
     */
    private fun logImageCategory(imageName: String) {
        when {
            imageName.startsWith("hdr_") -> {
                Log.d("TestImageCycle", "  Category: HDR Test (v2.6.0)")
            }
            imageName.startsWith("light_") -> {
                Log.d("TestImageCycle", "  Category: Lighting Analysis")
            }
            imageName.startsWith("portrait_") -> {
                Log.d("TestImageCycle", "  Category: Composition Test")
            }
            imageName.startsWith("landscape_") -> {
                Log.d("TestImageCycle", "  Category: Landscape Composition")
            }
            imageName.startsWith("comp_") -> {
                Log.d("TestImageCycle", "  Category: Composition Basics")
            }
            else -> {
                Log.d("TestImageCycle", "  Category: General Test")
            }
        }
    }

    /**
     * 載入特定測試圖片
     * v2.7.0: Updated to use TEST_IMAGES_DIR constant
     */
    suspend fun getTestImage(name: String): Bitmap? = withContext(Dispatchers.IO) {
        Log.i(TAG, "Loading specific test image: $name")

        try {
            Log.d(TAG, "Attempting to open: $TEST_IMAGES_DIR/$name")

            val bitmap = context.assets.open("$TEST_IMAGES_DIR/$name").use { inputStream ->
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
