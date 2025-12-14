package local.mylab.smartcameraapp1.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
     */
    suspend fun getNextTestImage(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val imageName = testImages[currentIndex]
            currentIndex = (currentIndex + 1) % testImages.size

            context.assets.open("test_images/$imageName").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load test image")
            // 如果測試圖片不存在，創建一個簡單的測試圖片
            createFallbackTestImage()
        }
    }

    /**
     * 載入特定測試圖片
     */
    suspend fun getTestImage(name: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.assets.open("test_images/$name").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
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
        return bitmap
    }
}
