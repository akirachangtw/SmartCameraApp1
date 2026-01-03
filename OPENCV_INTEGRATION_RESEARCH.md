# OpenCV 整合技術研究報告

**研究日期**: 2025-12-30
**研究者**: Claude Code
**目標版本**: OpenCV 4.9.0+ (推薦) / 4.8.0 (備選)
**目標**: Phase 2 (v2.7.0) - 引導線檢測 (Leading Lines Detection)

---

## 📋 目錄

1. [OpenCV Android SDK 整合方案](#1-opencv-android-sdk-整合方案)
2. [Hough Transform 引導線檢測](#2-hough-transform-引導線檢測)
3. [Canny Edge Detection 邊緣檢測](#3-canny-edge-detection-邊緣檢測)
4. [影像轉換與預處理](#4-影像轉換與預處理)
5. [性能優化建議](#5-性能優化建議)
6. [實作建議](#6-實作建議)

---

## 1. OpenCV Android SDK 整合方案

### 1.1 版本選擇建議

#### 🥇 推薦方案: OpenCV 4.9.0+ (Maven Central)

**理由**:
- ✅ 官方支持 Maven Central（自 4.9.0 起）
- ✅ Gradle 自動依賴管理
- ✅ 無需手動下載 SDK
- ✅ 易於維護和更新
- ✅ Android Studio 原生支持

**依賴配置**:
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("org.opencv:opencv:4.10.0")  // 最新穩定版
    // 或
    implementation("org.opencv:opencv:4.9.0")   // 首個支持 Maven 的版本
}
```

**優點**:
- 簡單易用，一行代碼完成整合
- 自動處理依賴衝突
- 支持 Gradle 構建快取
- 容易升級到新版本

#### 🥈 備選方案: OpenCV 4.8.0 (手動整合)

**理由**: 如需使用特定版本 4.8.0（較舊，2023 年發布）

**整合步驟**:
1. 下載 OpenCV Android SDK 4.8.0
   - 官方下載: [https://opencv.org/releases/](https://opencv.org/releases/)
   - GitHub Releases: [https://github.com/opencv/opencv/releases/tag/4.8.0](https://github.com/opencv/opencv/releases/tag/4.8.0)

2. 導入 SDK 模組
   ```bash
   # 解壓 OpenCV-android-sdk.zip
   unzip OpenCV-android-sdk.zip

   # 複製 SDK 到專案
   cp -r OpenCV-android-sdk/sdk path/to/project/opencv-sdk
   ```

3. 在 `settings.gradle.kts` 添加模組
   ```kotlin
   include(":opencv-sdk")
   ```

4. 在 `app/build.gradle.kts` 添加依賴
   ```kotlin
   dependencies {
       implementation(project(":opencv-sdk"))
   }
   ```

**缺點**:
- ⚠️ 需手動管理 SDK 檔案
- ⚠️ 增加專案體積（~40 MB）
- ⚠️ 升級困難
- ⚠️ 版本較舊，缺少新功能

---

### 1.2 初始化設置

#### 方法 1: Application 類別初始化（推薦）

```kotlin
// app/src/main/java/com/akirachang/ai/photographer/SmartCameraApplication.kt
package com.akirachang.ai.photographer

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartCameraApplication : Application() {

    companion object {
        private const val TAG = "SmartCameraApp"
        var isOpenCVInitialized = false
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // 初始化 OpenCV
        initializeOpenCV()
    }

    private fun initializeOpenCV() {
        try {
            if (OpenCVLoader.initDebug()) {
                isOpenCVInitialized = true
                Log.i(TAG, "OpenCV initialized successfully")
            } else {
                isOpenCVInitialized = false
                Log.e(TAG, "OpenCV initialization failed")
            }
        } catch (e: Exception) {
            isOpenCVInitialized = false
            Log.e(TAG, "OpenCV initialization exception", e)
        }
    }
}
```

#### 方法 2: Hilt Module 提供（進階）

```kotlin
// app/src/main/java/com/akirachang/ai/photographer/di/OpenCVModule.kt
package com.akirachang.ai.photographer.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.opencv.android.OpenCVLoader
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenCVModule {

    @Provides
    @Singleton
    fun provideOpenCVStatus(@ApplicationContext context: Context): Boolean {
        return OpenCVLoader.initDebug()
    }
}
```

---

### 1.3 AndroidManifest.xml 配置

```xml
<!-- app/src/main/AndroidManifest.xml -->
<application
    android:name=".SmartCameraApplication"
    android:allowBackup="true"
    ...>

    <!-- 如使用手動整合 4.8.0，需添加以下 -->
    <activity
        android:name="org.opencv.android.CameraActivity"
        android:configChanges="orientation|screenSize|keyboardHidden"
        android:exported="false" />
</application>
```

---

## 2. Hough Transform 引導線檢測

### 2.1 算法原理

**Hough Line Transform** 是一種在圖像中檢測直線的經典算法。它將笛卡爾坐標系中的點轉換為極坐標系中的曲線，通過投票機制找到共線的點。

**工作流程**:
```
輸入圖像
  ↓
邊緣檢測 (Canny)
  ↓
Hough 變換
  ↓
線段過濾
  ↓
輸出檢測到的線
```

---

### 2.2 核心參數詳解

#### Standard Hough Transform (`Imgproc.HoughLines`)

```kotlin
Imgproc.HoughLines(
    image: Mat,          // 輸入二值化邊緣圖像
    lines: Mat,          // 輸出線段（極坐標表示）
    rho: Double,         // 距離精度（像素）
    theta: Double,       // 角度精度（弧度）
    threshold: Int       // 累加器閾值
)
```

**參數詳解**:

| 參數 | 建議值 | 說明 | 調優指南 |
|------|--------|------|---------|
| **rho** | 1.0 | 距離分辨率（像素） | • 增大：更快但精度降低<br>• 減小：更精確但計算量增加<br>• 推薦：1-2 像素 |
| **theta** | CV_PI/180 (1°) | 角度分辨率（弧度） | • 增大：更快但角度粗糙<br>• 減小：角度更精確<br>• 推薦：1-2 度 |
| **threshold** | 100-150 | 最小投票數 | • 增大：檢測更少但更可靠的線<br>• 減小：檢測更多線但可能有誤判<br>• 推薦：根據圖像調整 |

#### Probabilistic Hough Transform (`Imgproc.HoughLinesP`) - 推薦

```kotlin
Imgproc.HoughLinesP(
    image: Mat,              // 輸入二值化邊緣圖像
    lines: Mat,              // 輸出線段（笛卡爾坐標）
    rho: Double,             // 距離精度
    theta: Double,           // 角度精度
    threshold: Int,          // 累加器閾值
    minLineLength: Double,   // 最小線段長度
    maxLineGap: Double       // 最大線段間隙
)
```

**額外參數**:

| 參數 | 建議值 | 說明 | 調優指南 |
|------|--------|------|---------|
| **minLineLength** | 50-100 | 最小線段長度（像素） | • 增大：過濾短線<br>• 減小：保留更多細節<br>• 根據圖像分辨率調整 |
| **maxLineGap** | 10-20 | 允許的最大間隙（像素） | • 增大：連接更多斷裂線段<br>• 減小：更嚴格的線段檢測<br>• 推薦：線段長度的 10-20% |

**HoughLinesP 優點**:
- ✅ 更快（僅處理隨機子集）
- ✅ 直接返回線段端點（易於使用）
- ✅ 適合實時處理

---

### 2.3 參數調優策略

#### 場景 1: 建築攝影（水平/垂直線為主）

```kotlin
val config = HoughConfig(
    rho = 1.0,
    theta = Math.PI / 180,    // 1 度
    threshold = 100,
    minLineLength = 80.0,     // 較長線段
    maxLineGap = 10.0         // 較小間隙
)
```

**理由**: 建築線條清晰，需要較高精度

#### 場景 2: 風景攝影（對角線/引導線）

```kotlin
val config = HoughConfig(
    rho = 2.0,                // 可稍微降低精度
    theta = Math.PI / 90,     // 2 度
    threshold = 80,           // 降低閾值（檢測更多線）
    minLineLength = 60.0,
    maxLineGap = 15.0
)
```

**理由**: 自然場景線條較不明顯，需降低閾值

#### 場景 3: 室內攝影（收斂線）

```kotlin
val config = HoughConfig(
    rho = 1.0,
    theta = Math.PI / 180,
    threshold = 120,
    minLineLength = 100.0,    // 更長線段（透視線）
    maxLineGap = 20.0         // 允許較大間隙
)
```

---

### 2.4 動態參數調整

```kotlin
class AdaptiveHoughConfig(private val imageWidth: Int, private val imageHeight: Int) {

    fun getOptimalParams(): HoughParams {
        val minDimension = min(imageWidth, imageHeight)

        return HoughParams(
            rho = 1.0,
            theta = Math.PI / 180,
            // 基於圖像大小動態調整閾值
            threshold = (minDimension * 0.1).toInt(),
            // 最小線段長度為最小尺寸的 5%
            minLineLength = minDimension * 0.05,
            // 最大間隙為最小尺寸的 2%
            maxLineGap = minDimension * 0.02
        )
    }
}

data class HoughParams(
    val rho: Double,
    val theta: Double,
    val threshold: Int,
    val minLineLength: Double,
    val maxLineGap: Double
)
```

---

## 3. Canny Edge Detection 邊緣檢測

### 3.1 算法原理

**Canny Edge Detection** 是一種多階段邊緣檢測算法，由 John F. Canny 於 1986 年提出。

**處理流程**:
```
1. 高斯模糊 (Noise Reduction)
   ↓
2. 梯度計算 (Gradient Calculation)
   ↓
3. 非最大值抑制 (Non-Maximum Suppression)
   ↓
4. 雙閾值檢測 (Double Threshold)
   ↓
5. 邊緣追蹤 (Edge Tracking by Hysteresis)
```

---

### 3.2 核心參數

```kotlin
Imgproc.Canny(
    image: Mat,           // 輸入灰度圖像
    edges: Mat,           // 輸出邊緣圖像
    threshold1: Double,   // 低閾值
    threshold2: Double,   // 高閾值
    apertureSize: Int,    // Sobel 核大小（3, 5, 7）
    L2gradient: Boolean   // 是否使用 L2 範數
)
```

**參數詳解**:

| 參數 | 建議值 | 說明 | 調優指南 |
|------|--------|------|---------|
| **threshold1** | 50-100 | 低閾值 | • 控制弱邊緣檢測<br>• 增大：更少邊緣但更可靠<br>• 減小：更多細節但可能有噪聲 |
| **threshold2** | 150-200 | 高閾值 | • 控制強邊緣檢測<br>• Canny 推薦 ratio 2:1 或 3:1<br>• threshold2 = 2 * threshold1 |
| **apertureSize** | 3 | Sobel 核大小 | • 3: 快速，適合一般場景<br>• 5/7: 更平滑，適合噪聲多的場景 |
| **L2gradient** | true | 梯度計算方式 | • true: 更準確但較慢（L2 範數）<br>• false: 更快但精度略低（L1 範數） |

---

### 3.3 閾值選擇策略

#### 方法 1: 固定比例 (2:1 或 3:1)

```kotlin
val threshold1 = 50.0
val threshold2 = threshold1 * 2.0  // 100.0 (Canny 推薦 2:1)

Imgproc.Canny(
    grayImage,
    edges,
    threshold1,
    threshold2,
    apertureSize = 3,
    L2gradient = true
)
```

#### 方法 2: Otsu 自動閾值

```kotlin
fun getCannyThresholds(image: Mat): Pair<Double, Double> {
    val otsuThreshold = Imgproc.threshold(
        image,
        Mat(),
        0.0,
        255.0,
        Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU
    )

    val threshold1 = otsuThreshold * 0.5  // 低閾值 = Otsu * 0.5
    val threshold2 = otsuThreshold * 1.0  // 高閾值 = Otsu * 1.0

    return Pair(threshold1, threshold2)
}
```

#### 方法 3: 自適應閾值（基於圖像統計）

```kotlin
fun getAdaptiveCannyThresholds(image: Mat): Pair<Double, Double> {
    val mean = Core.mean(image).`val`[0]
    val stdDev = Mat()
    val meanMat = Mat()
    Core.meanStdDev(image, meanMat, stdDev)
    val sigma = stdDev.get(0, 0)[0]

    val threshold1 = max(0.0, mean - sigma)
    val threshold2 = min(255.0, mean + sigma)

    return Pair(threshold1, threshold2)
}
```

---

### 3.4 預處理最佳實踐

#### 完整預處理流程

```kotlin
fun preprocessForEdgeDetection(input: Mat): Mat {
    val preprocessed = Mat()

    // 1. 轉換為灰度（如需要）
    if (input.channels() > 1) {
        Imgproc.cvtColor(input, preprocessed, Imgproc.COLOR_BGR2GRAY)
    } else {
        input.copyTo(preprocessed)
    }

    // 2. 高斯模糊降噪（5x5 核）
    val blurred = Mat()
    Imgproc.GaussianBlur(
        preprocessed,
        blurred,
        Size(5.0, 5.0),  // 核大小
        1.4              // sigma
    )

    // 3. (可選) 直方圖均衡化增強對比度
    val equalized = Mat()
    Imgproc.equalizeHist(blurred, equalized)

    return equalized
}
```

**預處理步驟說明**:

| 步驟 | 目的 | 參數建議 |
|------|------|---------|
| **灰度轉換** | 減少計算量 | COLOR_BGR2GRAY |
| **高斯模糊** | 降低噪聲影響 | 核大小 5x5, sigma 1.4 |
| **直方圖均衡** | 增強對比度 | 在低對比度場景使用 |

---

### 3.5 場景特定配置

#### 場景 1: 室內低光環境

```kotlin
val config = CannyConfig(
    gaussianKernelSize = Size(7.0, 7.0),  // 更大的模糊核
    threshold1 = 30.0,                     // 較低閾值
    threshold2 = 90.0,                     // 3:1 比例
    apertureSize = 5,                      // 更大的 Sobel 核
    useHistogramEqualization = true        // 啟用直方圖均衡化
)
```

#### 場景 2: 室外強光環境

```kotlin
val config = CannyConfig(
    gaussianKernelSize = Size(3.0, 3.0),  // 較小的模糊核
    threshold1 = 80.0,                     // 較高閾值
    threshold2 = 160.0,                    // 2:1 比例
    apertureSize = 3,                      // 標準 Sobel 核
    useHistogramEqualization = false       // 對比度已足夠
)
```

---

## 4. 影像轉換與預處理

### 4.1 ImageProxy 到 Mat 轉換

```kotlin
package com.akirachang.ai.photographer.data.opencv

import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import android.graphics.Bitmap

object ImageConverter {

    /**
     * 將 CameraX ImageProxy 轉換為 OpenCV Mat
     *
     * @param imageProxy CameraX 圖像
     * @return OpenCV Mat (BGR 格式)
     */
    fun imageProxyToMat(imageProxy: ImageProxy): Mat {
        // 方法 1: 通過 Bitmap 轉換（推薦 - 支持所有格式）
        val bitmap = imageProxyToBitmap(imageProxy)
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // 轉換顏色空間: RGBA → BGR
        val bgrMat = Mat()
        Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_RGBA2BGR)

        return bgrMat
    }

    /**
     * 將 ImageProxy 轉換為 Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("Failed to decode image")
    }

    /**
     * 將 Bitmap 轉換為 OpenCV Mat
     */
    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // RGBA → BGR
        val bgrMat = Mat()
        Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_RGBA2BGR)

        return bgrMat
    }

    /**
     * 將 Mat 轉換為 Bitmap（用於顯示）
     */
    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(
            mat.cols(),
            mat.rows(),
            Bitmap.Config.ARGB_8888
        )

        // BGR → RGBA
        val rgbaMat = Mat()
        Imgproc.cvtColor(mat, rgbaMat, Imgproc.COLOR_BGR2RGBA)

        Utils.matToBitmap(rgbaMat, bitmap)

        return bitmap
    }
}
```

---

### 4.2 色彩空間轉換

```kotlin
object ColorSpaceConverter {

    /**
     * BGR 轉灰度
     */
    fun bgrToGray(bgrMat: Mat): Mat {
        val grayMat = Mat()
        Imgproc.cvtColor(bgrMat, grayMat, Imgproc.COLOR_BGR2GRAY)
        return grayMat
    }

    /**
     * BGR 轉 HSV（用於顏色過濾）
     */
    fun bgrToHsv(bgrMat: Mat): Mat {
        val hsvMat = Mat()
        Imgproc.cvtColor(bgrMat, hsvMat, Imgproc.COLOR_BGR2HSV)
        return hsvMat
    }

    /**
     * 檢查圖像是否為灰度
     */
    fun isGrayscale(mat: Mat): Boolean {
        return mat.channels() == 1
    }
}
```

---

## 5. 性能優化建議

### 5.1 記憶體管理

**問題**: OpenCV Mat 對象需手動釋放記憶體

```kotlin
class LeadingLineDetector {

    fun detectLines(input: Mat): List<Line> {
        // 中間變量
        var grayMat: Mat? = null
        var blurredMat: Mat? = null
        var edgesMat: Mat? = null
        var linesMat: Mat? = null

        try {
            grayMat = Mat()
            Imgproc.cvtColor(input, grayMat, Imgproc.COLOR_BGR2GRAY)

            blurredMat = Mat()
            Imgproc.GaussianBlur(grayMat, blurredMat, Size(5.0, 5.0), 1.4)

            edgesMat = Mat()
            Imgproc.Canny(blurredMat, edgesMat, 50.0, 150.0)

            linesMat = Mat()
            Imgproc.HoughLinesP(
                edgesMat, linesMat, 1.0, Math.PI / 180, 100, 50.0, 10.0
            )

            return extractLines(linesMat)

        } finally {
            // 確保釋放記憶體
            grayMat?.release()
            blurredMat?.release()
            edgesMat?.release()
            linesMat?.release()
        }
    }
}
```

---

### 5.2 圖像降採樣

**目的**: 減少處理時間，對於大圖像尤其重要

```kotlin
object ImageOptimizer {

    /**
     * 降採樣圖像（保持寬高比）
     *
     * @param input 輸入圖像
     * @param maxDimension 最大尺寸（寬或高）
     * @return 降採樣後的圖像
     */
    fun downsample(input: Mat, maxDimension: Int = 800): Mat {
        val width = input.cols()
        val height = input.rows()
        val maxSize = max(width, height)

        // 如果已經小於目標尺寸，直接返回
        if (maxSize <= maxDimension) {
            return input.clone()
        }

        val scale = maxDimension.toDouble() / maxSize.toDouble()
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val downsampled = Mat()
        Imgproc.resize(
            input,
            downsampled,
            Size(newWidth.toDouble(), newHeight.toDouble()),
            0.0,
            0.0,
            Imgproc.INTER_AREA  // 降採樣使用 INTER_AREA
        )

        return downsampled
    }
}
```

**性能影響**:
- 1920x1080 → 800x450: **~6x 加速**
- 4000x3000 → 800x600: **~25x 加速**

---

### 5.3 ROI (Region of Interest) 處理

**目的**: 僅處理感興趣區域，忽略無關部分

```kotlin
fun detectLinesInROI(input: Mat, roiRect: Rect): List<Line> {
    // 提取 ROI
    val roi = Mat(input, roiRect)

    val lines = try {
        // 在 ROI 中檢測線段
        detectLines(roi)
    } finally {
        roi.release()
    }

    // 將 ROI 坐標轉換回原圖坐標
    return lines.map { line ->
        Line(
            x1 = line.x1 + roiRect.x,
            y1 = line.y1 + roiRect.y,
            x2 = line.x2 + roiRect.x,
            y2 = line.y2 + roiRect.y
        )
    }
}
```

---

### 5.4 多線程處理

**警告**: OpenCV Mat 不是線程安全的

```kotlin
class ParallelLineDetector {

    private val executorService = Executors.newSingleThreadExecutor()

    fun detectLinesAsync(
        input: Mat,
        callback: (List<Line>) -> Unit
    ) {
        // 複製一份避免並發問題
        val inputCopy = input.clone()

        executorService.submit {
            try {
                val lines = detectLines(inputCopy)
                callback(lines)
            } finally {
                inputCopy.release()
            }
        }
    }

    fun shutdown() {
        executorService.shutdown()
    }
}
```

---

## 6. 實作建議

### 6.1 建議的模組結構

```
app/src/main/java/com/akirachang/ai/photographer/
├── data/
│   └── opencv/
│       ├── ImageConverter.kt          # 圖像格式轉換
│       ├── ColorSpaceConverter.kt     # 色彩空間轉換
│       ├── ImageOptimizer.kt          # 圖像優化工具
│       └── OpenCVInitializer.kt       # OpenCV 初始化
├── domain/
│   └── usecase/
│       ├── DetectLeadingLinesUseCase.kt  # 引導線檢測 UseCase
│       └── model/
│           ├── Line.kt                   # 線段模型
│           └── LeadingLineType.kt        # 引導線類型
└── presentation/
    └── camera/
        └── components/
            └── overlay/
                └── LeadingLineOverlay.kt  # 引導線 Overlay UI
```

---

### 6.2 DetectLeadingLinesUseCase 實作草圖

```kotlin
package com.akirachang.ai.photographer.domain.usecase

import com.akirachang.ai.photographer.data.opencv.ImageConverter
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import javax.inject.Inject

class DetectLeadingLinesUseCase @Inject constructor(
    private val imageConverter: ImageConverter
) {

    suspend operator fun invoke(image: ImageProxy): LeadingLineResult {
        return withContext(Dispatchers.Default) {
            var mat: Mat? = null
            try {
                // 1. 轉換為 Mat
                mat = imageConverter.imageProxyToMat(image)

                // 2. 預處理
                val preprocessed = preprocessImage(mat)

                // 3. 邊緣檢測
                val edges = detectEdges(preprocessed)

                // 4. 線段檢測
                val lines = detectLines(edges)

                // 5. 分類線段（水平/垂直/對角）
                val classified = classifyLines(lines)

                LeadingLineResult(
                    horizontalLines = classified.horizontal,
                    verticalLines = classified.vertical,
                    diagonalLines = classified.diagonal
                )
            } finally {
                mat?.release()
            }
        }
    }

    private fun preprocessImage(input: Mat): Mat {
        // 實作預處理流程
    }

    private fun detectEdges(input: Mat): Mat {
        // 實作 Canny 邊緣檢測
    }

    private fun detectLines(edges: Mat): List<Line> {
        // 實作 Hough 線段檢測
    }

    private fun classifyLines(lines: List<Line>): ClassifiedLines {
        // 實作線段分類
    }
}
```

---

### 6.3 測試策略

#### 單元測試

```kotlin
class DetectLeadingLinesUseCaseTest {

    @Test
    fun `should detect horizontal lines in architecture image`() {
        // 使用測試圖片（建築物水平線）
        val testImage = loadTestImage("architecture_horizontal.jpg")

        val result = detectLeadingLinesUseCase(testImage)

        assertTrue(result.horizontalLines.isNotEmpty())
        assertTrue(result.horizontalLines.size >= 3) // 至少 3 條水平線
    }

    @Test
    fun `should detect diagonal lines in landscape image`() {
        val testImage = loadTestImage("landscape_diagonal.jpg")

        val result = detectLeadingLinesUseCase(testImage)

        assertTrue(result.diagonalLines.isNotEmpty())
    }
}
```

#### 準備測試圖片

建議準備以下測試圖片集：
- `architecture_horizontal.jpg` - 建築物水平線
- `architecture_vertical.jpg` - 建築物垂直線
- `landscape_diagonal.jpg` - 風景對角線
- `indoor_converging.jpg` - 室內收斂線
- `low_contrast.jpg` - 低對比度場景
- `high_noise.jpg` - 高噪聲場景

---

## 📚 參考資源

### OpenCV Android SDK 整合
- [OpenCV Android Development Guide](https://docs.opencv.org/4.x/d5/df8/tutorial_dev_with_OCV_on_Android.html)
- [OpenCV Android Usage Models](https://opencv.org/opencv4android-usage-models/)
- [QuickBirdEng OpenCV Android](https://github.com/QuickBirdEng/opencv-android)
- [Android Studio OpenCV Installation Guide](https://proandroiddev.com/android-studio-step-by-step-guide-to-download-and-install-opencv-for-android-9ddcb78a8bc3)

### Hough Transform
- [OpenCV Hough Line Transform Tutorial](https://docs.opencv.org/3.4/d9/db0/tutorial_hough_lines.html)
- [OpenCV-Python Hough Lines Tutorial](https://opencv24-python-tutorials.readthedocs.io/en/latest/py_tutorials/py_imgproc/py_houghlines/py_houghlines.html)
- [Parameters in Hough Transform](https://answers.opencv.org/question/145326/parameters-in-hough-transform/)
- [Hough Lines Transform Explained](https://medium.com/@tomasz.kacmajor/hough-lines-transform-explained-645feda072ab)

### Canny Edge Detection
- [OpenCV Canny Edge Detection](https://docs.opencv.org/4.x/da/d22/tutorial_py_canny.html)
- [Mastering Edge Detection with OpenCV](https://medium.com/@noel.benji/a-guide-to-robust-edge-detection-with-opencv-1d703506e014)
- [OpenCV Canny Edge Detector Tutorial](https://docs.opencv.org/4.x/da/d5c/tutorial_canny_detector.html)
- [Edge Detection Best Practices](https://opencv.org/blog/edge-detection-using-opencv/)

---

## ✅ 總結與建議

### 關鍵決策

1. **OpenCV 版本**:
   - ✅ 推薦使用 **OpenCV 4.9.0+** (Maven Central)
   - 如需特定版本，手動整合 4.8.0 也可行但維護成本較高

2. **線段檢測算法**:
   - ✅ 推薦使用 **HoughLinesP** (Probabilistic Hough Transform)
   - 更快、更實用、直接返回端點坐標

3. **邊緣檢測參數**:
   - ✅ 使用 **自適應閾值**（基於圖像統計）
   - 推薦 threshold2:threshold1 = 2:1 或 3:1

4. **性能優化**:
   - ✅ 圖像降採樣至 800px (對大圖像)
   - ✅ 使用 ROI 處理（如可能）
   - ✅ 確保記憶體正確釋放

### 下一步行動

1. **Day 2**: 完善技術文檔（補充實作範例）
2. **Day 3-5**: 建立獨立原型分支，實作演算法原型
3. **Day 6+**: 等待 v2.6.0 實機測試完成後，整合到主應用

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
