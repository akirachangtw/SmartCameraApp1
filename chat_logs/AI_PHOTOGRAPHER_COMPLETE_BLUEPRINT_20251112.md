# SmartCameraApp1 - AI即時攝影師完整開發藍圖

**文檔版本**: 1.0
**建立日期**: 2025-11-12
**基於文檔**: AI即時攝影師_專項開發計畫.md
**遵循規範**: CLAUDE.md

---

## 目錄

1. [專案現狀分析](#1-專案現狀分析)
2. [完整開發藍圖](#2-完整開發藍圖)
3. [開發環境需求](#3-開發環境需求)
4. [技術架構設計](#4-技術架構設計)
5. [效能優化策略](#5-效能優化策略)
6. [開發規範整合](#6-開發規範整合)
7. [風險管理](#7-風險管理)
8. [學習資源](#8-學習資源)

---

## 1. 專案現狀分析

### 1.1 技術架構現況

**基礎配置**:
- **應用 ID**: `local.mylab.smartcameraapp1`
- **Compile SDK**: 36 (Android 15 preview)
- **Target SDK**: 36
- **Min SDK**: 24 (Android 7.0)
- **Kotlin 版本**: 2.0.21
- **Build 工具**: Gradle 8.10.1

**架構模式**: Clean Architecture + MVVM
```
app/src/main/java/local/mylab/smartcameraapp1/
├── domain/          # 領域層 (業務邏輯)
├── data/            # 數據層 (具體實現)
├── presentation/    # 表現層 (UI)
├── di/              # 依賴注入 (Hilt)
└── util/            # 工具類
```

### 1.2 已完成功能清單

**✅ UI 框架**:
- Jetpack Compose (100% Compose-first)
- Material 3 with Dynamic Color
- Edge-to-Edge 支援

**✅ 相機功能**:
- CameraX 1.4.1 完整整合
- 即時預覽 (Preview UseCase)
- 照片拍攝 (Image Capture)
- 圖像分析管線 (Image Analysis) - **已啟用但未使用**
- 前後鏡頭切換
- 三段式閃光燈控制 (OFF → AUTO → ON)
- MediaStore 照片儲存
- Android 7-15 權限適配

**✅ 核心庫整合**:
```kotlin
androidx-core-ktx: 1.17.0
androidx-camera-*: 1.4.1
compose-bom: 2024.09.00
hilt: 2.51
coroutines: 1.9.0
coil: 2.5.0
accompanist-permissions: 0.36.0
androidx-datastore-preferences: 1.1.1
```

**✅ 權限處理**:
```xml
<!-- 已配置權限 -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

### 1.3 關鍵缺口識別

**❌ AI/ML 功能完全未實作**:
- ML Kit 未整合 (Object Detection, Face Detection)
- TensorFlow Lite 未整合
- 無任何圖像分析邏輯
- 無 GPU 加速渲染管線 (OpenGL ES / Vulkan)
- 無構圖引導演算法
- 無光影分析系統

**⚠️ 架構預留位置**:
- `data/source/camera/` - 空目錄 (預留 AI 數據源)
- `domain/usecase/` - 空目錄 (預留 AI 業務邏輯)
- `presentation/camera/components/overlay/` - 不存在 (預留 UI 覆蓋層)

---

## 2. 完整開發藍圖

### 階段一：基礎建設與啟發式引導 (V1.0)

**時間預估**: 2 個月 (8 週)
**核心目標**: 整合 ML Kit，實現基礎構圖引導與 UI 覆蓋層

#### Week 1-2: ML Kit 整合

**任務清單**:
- [ ] 在 `app/build.gradle.kts` 添加依賴
  ```kotlin
  implementation("com.google.mlkit:object-detection:17.0.1")
  implementation("com.google.mlkit:face-detection:16.1.5")
  ```
- [ ] 建立 `data/source/ml/ImageAnalyzer.kt`
  ```kotlin
  class ImageAnalyzer @Inject constructor() : ImageAnalysis.Analyzer {
      override fun analyze(imageProxy: ImageProxy) {
          // ML Kit 偵測邏輯
      }
  }
  ```
- [ ] 建立 `data/source/ml/MLKitDetector.kt` (封裝 ML Kit API)
- [ ] 建立 `domain/model/DetectionResult.kt` 數據類
  ```kotlin
  data class DetectionResult(
      val faces: List<Face>,
      val objects: List<DetectedObject>,
      val timestamp: Long
  )
  ```
- [ ] 在 CameraRepositoryImpl 中連接 ImageAnalysis
- [ ] 在實體設備測試偵測效能 (記錄 FPS 與延遲)

**技術要點**:
- 使用 `ImageProxy` 避免不必要的 Bitmap 轉換
- 設定 `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` 避免積壓
- 在背景執行緒執行分析 (使用 Executors.newSingleThreadExecutor())
- 實作幀率節流 (例: 每秒分析 10 幀)

#### Week 3-4: 構圖引導引擎

**任務清單**:
- [ ] 建立 `domain/usecase/AnalyzeCompositionUseCase.kt`
- [ ] 建立 `domain/model/CompositionSuggestion.kt`
  ```kotlin
  sealed class CompositionSuggestion {
      object Perfect : CompositionSuggestion()
      data class MoveSubject(val direction: Direction) : CompositionSuggestion()
      data class AdjustHeadroom(val tooMuch: Boolean) : CompositionSuggestion()
      object NoSubjectDetected : CompositionSuggestion()
  }
  ```
- [ ] 實作三分法檢查演算法
  ```kotlin
  fun checkRuleOfThirds(face: Face, imageSize: Size): Boolean {
      val thirds = imageSize.width / 3
      val faceCenter = face.boundingBox.centerX()
      // 檢查是否接近三分線交點
  }
  ```
- [ ] 實作頭部空間檢查演算法
- [ ] 實作主體位置分析 (邊緣 vs 中心)
- [ ] 建立測試案例 (單元測試)

**構圖規則**:
1. **三分法**: 主體中心是否在三分線交點附近 (±10% 容差)
2. **頭部空間**: 人臉上方空間佔畫面 5-15% 為佳
3. **邊緣檢查**: 主體不應過於靠近畫面邊緣 (<5% 邊距)
4. **主體檢測**: 至少偵測到一個主體 (人臉或物體)

#### Week 5-6: UI 覆蓋層系統

**任務清單**:
- [ ] 建立 `presentation/camera/components/overlay/` 目錄
- [ ] 實作 `GridOverlay.kt` (三分法網格線)
  ```kotlin
  @Composable
  fun GridOverlay(modifier: Modifier = Modifier) {
      Canvas(modifier = modifier.fillMaxSize()) {
          // 繪製垂直與水平三分線
      }
  }
  ```
- [ ] 實作 `LevelIndicator.kt` (水平儀)
  ```kotlin
  @Composable
  fun LevelIndicator(
      roll: Float,  // 從感應器取得
      modifier: Modifier = Modifier
  )
  ```
- [ ] 實作 `CompositionGuide.kt` (動態提示)
  ```kotlin
  @Composable
  fun CompositionGuide(
      suggestion: CompositionSuggestion,
      modifier: Modifier = Modifier
  )
  ```
- [ ] 實作 `DetectionBoxes.kt` (偵測框顯示，調試用)
- [ ] 整合到 `CameraScreen.kt`
  ```kotlin
  Box {
      CameraPreview(controller)
      GridOverlay()
      CompositionGuide(viewModel.suggestion)
  }
  ```
- [ ] 實作覆蓋層開關設定 (儲存到 DataStore)

**UI 設計要點**:
- 網格線使用半透明白色 (#FFFFFF80)
- 提示文字使用對比色背景確保可讀性
- 動畫效果使用 `animateDpAsState` / `animateColorAsState`
- 提供「專業模式」與「簡易模式」兩種 UI 密度

#### Week 7-8: 效能優化與測試

**任務清單**:
- [ ] 降低 ImageAnalysis 解析度至 640x480
- [ ] 實作幀率監控工具 (顯示 FPS)
- [ ] 實作延遲監控 (偵測到顯示的時間差)
- [ ] 在中階手機測試 (Pixel 7a / Samsung A54)
  - 目標: 維持 30 FPS 預覽
  - ML Kit 分析延遲 < 100ms
- [ ] 記憶體洩漏檢查 (LeakCanary)
- [ ] 電池消耗測試 (Android Profiler)
- [ ] 撰寫 V1.0 完成報告

**效能基準**:
| 設備等級 | 目標 FPS | ML Kit 延遲 | 記憶體使用 |
|---------|---------|-------------|-----------|
| 高階    | 60 FPS  | < 50ms      | < 200MB   |
| 中階    | 30 FPS  | < 100ms     | < 150MB   |
| 低階    | 20 FPS  | < 150ms     | < 100MB   |

#### V1.0 可交付成果

- ✅ 即時人臉與物體偵測
- ✅ 三分法網格線覆蓋層
- ✅ 水平儀指示器
- ✅ 基礎構圖建議 (4-5 種情境)
- ✅ 效能測試報告
- ✅ 技術文檔

---

### 階段二：動態構圖與光影分析 (V2.0)

**時間預估**: 2 個月 (8 週)
**核心目標**: 實現智能化構圖建議與場景光影分析

#### Week 1-3: 進階構圖演算法

**任務清單**:
- [ ] 添加 OpenCV 依賴 (可選)
  ```kotlin
  implementation("org.opencv:opencv-android:4.8.0")
  ```
- [ ] 實作引導線偵測 (Hough Line Transform)
  ```kotlin
  fun detectLeadingLines(image: Mat): List<Line> {
      val edges = Imgproc.Canny(image, 50, 150)
      val lines = Imgproc.HoughLinesP(edges, ...)
      return filterDominantLines(lines)
  }
  ```
- [ ] 實作對稱性分析 (比對左右兩側直方圖)
- [ ] 實作人像專用規則
  - 眼睛位置檢查 (應在上三分線附近)
  - 視線方向分析 (使用 ML Kit Face Contour)
  - 肩膀水平檢查
- [ ] 實作景深建議 (基於多物體距離估計)
- [ ] 建立構圖評分系統 (0-100 分)
  ```kotlin
  data class CompositionScore(
      val overall: Int,           // 綜合評分
      val ruleOfThirds: Int,      // 三分法得分
      val balance: Int,           // 平衡性得分
      val leadingLines: Int,      // 引導線得分
      val headroom: Int           // 頭部空間得分
  )
  ```

**演算法細節**:
- **引導線**: 偵測角度在 30-60° 或 120-150° 的線條
- **對稱性**: 計算左右兩側 HSV 直方圖的相關係數
- **眼睛位置**: 理想位置在畫面上方 1/3 處 (黃金比例)
- **景深**: 根據 bounding box 大小估計距離

#### Week 4-6: 光影分析系統

**任務清單**:
- [ ] 建立 `data/source/analysis/HistogramAnalyzer.kt`
  ```kotlin
  class HistogramAnalyzer {
      fun analyze(imageProxy: ImageProxy): HistogramData {
          // 計算亮度直方圖 (256 bins)
      }
  }
  ```
- [ ] 建立 `data/source/analysis/LightingDetector.kt`
  ```kotlin
  class LightingDetector {
      fun detectBacklight(
          histogram: HistogramData,
          faceRegion: Rect
      ): BacklightResult

      fun detectHighContrast(
          histogram: HistogramData
      ): Boolean
  }
  ```
- [ ] 實作高反差場景偵測
  ```kotlin
  fun isHighContrast(histogram: IntArray): Boolean {
      val darkPixels = histogram.slice(0..50).sum()
      val brightPixels = histogram.slice(200..255).sum()
      val midtones = histogram.slice(51..199).sum()
      return (darkPixels + brightPixels) > midtones * 0.5
  }
  ```
- [ ] 實作逆光場景偵測
  ```kotlin
  fun isBacklit(
      histogram: IntArray,
      faceBrightness: Float,
      backgroundBrightness: Float
  ): Boolean {
      return backgroundBrightness > faceBrightness * 1.5
  }
  ```
- [ ] 實作陰影/高光區域識別
- [ ] 建立 `domain/model/LightingCondition.kt`
  ```kotlin
  sealed class LightingCondition {
      object Normal : LightingCondition()
      object HighContrast : LightingCondition()
      data class Backlit(val severity: Float) : LightingCondition()
      object LowLight : LightingCondition()
      object Overexposed : LightingCondition()
  }
  ```

**光影判斷規則**:
- **高反差**: 直方圖兩端 (0-50, 200-255) 像素佔比 > 50%
- **逆光**: 背景平均亮度 > 人臉亮度 × 1.5
- **低光**: 平均亮度 < 40 (0-255 scale)
- **過曝**: 像素 > 250 的佔比 > 10%

#### Week 7-8: 智能建議系統

**任務清單**:
- [ ] 建立優先級系統
  ```kotlin
  enum class SuggestionPriority {
      CRITICAL,    // 紅色，嚴重問題 (例: 主體被截斷)
      IMPORTANT,   // 黃色，重要建議 (例: 逆光)
      SUGGESTION   // 綠色，優化建議 (例: 可調整構圖)
  }
  ```
- [ ] 實作情境化提示文字生成器
  ```kotlin
  fun generateAdvice(
      composition: CompositionScore,
      lighting: LightingCondition
  ): List<ShootingAdvice>
  ```
- [ ] 實作拍照時機建議
  ```kotlin
  fun shouldSuggestCapture(
      compositionScore: Int,
      lighting: LightingCondition,
      faceSmiling: Boolean
  ): Boolean {
      return compositionScore > 80 &&
             lighting is LightingCondition.Normal &&
             faceSmiling
  }
  ```
- [ ] UI 改進: 視覺化光影警告圖示
- [ ] 實作建議歷史記錄 (用於學習使用者偏好)

**建議範例**:
| 情境 | 優先級 | 提示文字 |
|-----|-------|---------|
| 逆光 + 人臉偵測 | CRITICAL | "逆光偵測! 建議開啟閃光燈或調整角度" |
| 構圖偏離三分法 | IMPORTANT | "將主體向右移動以符合三分法" |
| 完美時機 | SUGGESTION | "構圖完美! 按下快門" |
| 頭部空間過多 | SUGGESTION | "可向上移動相機縮小頭部空間" |

#### V2.0 可交付成果

- ✅ 進階構圖演算法 (引導線、對稱性、人像規則)
- ✅ 即時光影分析 (直方圖、高反差、逆光偵測)
- ✅ 優先級建議系統
- ✅ 拍照時機智能提示
- ✅ 構圖評分系統 (0-100)
- ✅ 視覺化光影警告 UI

---

### 階段三：即時光影渲染 (V3.0)

**時間預估**: 3 個月 (12 週)
**核心目標**: 在預覽畫面即時套用 HDR 與 Tone Mapping 效果

⚠️ **警告**: 此階段技術難度極高，需要深入 OpenGL ES 知識

#### Week 1-4: GLES 渲染管線

**任務清單**:
- [ ] 學習 OpenGL ES 3.0+ 基礎 (若不熟悉)
- [ ] 建立 `data/source/rendering/GLESRenderer.kt`
  ```kotlin
  class GLESRenderer : GLSurfaceView.Renderer {
      override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {}
      override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
      override fun onDrawFrame(gl: GL10?) {}
  }
  ```
- [ ] 建立 CameraX → GLSurfaceView 管線
  ```kotlin
  // 使用 SurfaceTexture 作為橋接
  val surfaceTexture = SurfaceTexture(textureId)
  val surface = Surface(surfaceTexture)
  cameraController.setSurfaceProvider { request ->
      request.provideSurface(surface, ...)
  }
  ```
- [ ] 實作基礎 Vertex Shader
  ```glsl
  // vertex_shader.glsl
  attribute vec4 a_position;
  attribute vec2 a_texCoord;
  varying vec2 v_texCoord;

  void main() {
      gl_Position = a_position;
      v_texCoord = a_texCoord;
  }
  ```
- [ ] 實作基礎 Fragment Shader (直通)
  ```glsl
  // fragment_base.glsl
  precision mediump float;
  varying vec2 v_texCoord;
  uniform sampler2D u_texture;

  void main() {
      gl_FragColor = texture2D(u_texture, v_texCoord);
  }
  ```
- [ ] 實作 `ShaderProgram.kt` (Shader 編譯與管理)
- [ ] 實作 `TextureConverter.kt` (相機幀 → GPU 紋理)
- [ ] 測試基礎渲染管線 (確保畫面正常顯示)

**技術要點**:
- 使用 `GLES30` API (不是 `GLES20`)
- 相機紋理使用 `GLES11Ext.GL_TEXTURE_EXTERNAL_OES`
- 注意紋理座標轉換 (相機可能是旋轉的)
- 使用 FBO (Framebuffer Object) 實現多 pass 渲染

#### Week 5-8: Tone Mapping 實作

**任務清單**:
- [ ] 研究 Tone Mapping 演算法
  - Reinhard (簡單但效果一般)
  - ACES (電影級，複雜但效果極佳)
  - Filmic (遊戲常用，平衡效果與效能)
- [ ] 實作 Reinhard Tone Mapping Shader
  ```glsl
  // fragment_tone_mapping.glsl
  vec3 reinhardToneMapping(vec3 color) {
      return color / (color + vec3(1.0));
  }
  ```
- [ ] 實作 ACES Tone Mapping Shader
  ```glsl
  vec3 ACESFilmicToneMapping(vec3 x) {
      float a = 2.51;
      float b = 0.03;
      float c = 2.43;
      float d = 0.59;
      float e = 0.14;
      return clamp((x*(a*x+b))/(x*(c*x+d)+e), 0.0, 1.0);
  }
  ```
- [ ] 實作自動參數調整
  ```kotlin
  fun calculateToneMappingParams(histogram: IntArray): ToneMappingParams {
      val avgBrightness = calculateAverage(histogram)
      val exposure = 1.0f / avgBrightness
      val contrast = calculateContrast(histogram)
      return ToneMappingParams(exposure, contrast)
  }
  ```
- [ ] 整合直方圖分析結果到 Shader
  ```kotlin
  GLES30.glUniform1f(exposureHandle, params.exposure)
  GLES30.glUniform1f(contrastHandle, params.contrast)
  ```
- [ ] 實作高光壓縮與陰影提亮
  ```glsl
  vec3 enhanceDynamicRange(vec3 color, float shadows, float highlights) {
      // 提亮陰影
      color = pow(color, vec3(1.0 - shadows * 0.5));
      // 壓縮高光
      color = 1.0 - pow(1.0 - color, vec3(1.0 + highlights));
      return color;
  }
  ```
- [ ] A/B 測試不同演算法 (記錄使用者偏好)

**Shader 優化技巧**:
- 預計算常數值 (在 CPU 端計算後傳入)
- 使用 `mediump` 精度降低 GPU 負擔
- 避免分支 (if/else)，使用 `mix()` 或 `step()`
- 合併多個 pass 減少渲染次數

#### Week 9-10: 使用者控制

**任務清單**:
- [ ] 新增設定頁面 (使用 Compose)
- [ ] 實作 HDR 強度滑桿 (0-100%)
  ```kotlin
  @Composable
  fun HDRIntensitySlider(
      value: Float,
      onValueChange: (Float) -> Unit
  )
  ```
- [ ] 實作預覽效果開/關切換
  ```kotlin
  var hdrEnabled by remember { mutableStateOf(true) }
  Switch(checked = hdrEnabled, onCheckedChange = { hdrEnabled = it })
  ```
- [ ] 實作 Tone Mapping 演算法選擇器
  ```kotlin
  enum class ToneMappingAlgorithm {
      NONE, REINHARD, ACES, FILMIC
  }
  ```
- [ ] 儲存設定到 DataStore
  ```kotlin
  suspend fun saveSetting(key: String, value: Any) {
      dataStore.edit { preferences ->
          preferences[stringPreferencesKey(key)] = value.toString()
      }
  }
  ```
- [ ] 實作「重置為預設值」功能
- [ ] 新增即時預覽比對功能 (左右分屏: 原始 vs 處理後)

**UI 設計**:
```
設定畫面
├── [開關] 啟用 HDR 預覽
├── [滑桿] HDR 強度: 0 ━━●━━━━━ 100
├── [下拉] Tone Mapping 演算法
│   ├── 無
│   ├── Reinhard (推薦)
│   ├── ACES (電影級)
│   └── Filmic (平衡)
├── [滑桿] 陰影提亮: 0 ━━━●━━━ 100
├── [滑桿] 高光壓縮: 0 ━━━━●━━ 100
└── [按鈕] 重置為預設值
```

#### Week 11-12: 效能調優

**任務清單**:
- [ ] 使用 Android GPU Inspector 分析效能
  - 識別 GPU 瓶頸
  - 檢查 Shader 編譯時間
  - 監控紋理傳輸開銷
- [ ] 降低 Shader 複雜度
  - 移除不必要的計算
  - 使用查找表 (LUT) 加速色調映射
- [ ] 實作查找表 (LUT) 優化
  ```kotlin
  // 預計算 LUT (256x256 RGB 映射表)
  val lut = generateToneMappingLUT(algorithm)
  val lutTexture = uploadLUTtoGPU(lut)
  ```
  ```glsl
  // 在 Shader 中使用 LUT
  vec3 toneMappedColor = texture2D(u_lut, color.rg).rgb;
  ```
- [ ] 測試各廠牌手機相容性
  - Mali GPU (華為、小米部分機型)
  - Adreno GPU (高通，主流)
  - PowerVR GPU (聯發科部分機型)
- [ ] 實作降級策略
  ```kotlin
  val useSimplifiedShader = when {
      gpuVendor.contains("Mali") && gpuVersion < 600 -> true
      totalRam < 4GB -> true
      else -> false
  }
  ```
- [ ] 撰寫效能測試報告 (各機型 FPS、功耗)

**效能目標**:
| 設備等級 | 目標 FPS | GPU 使用率 | 功耗增加 |
|---------|---------|-----------|---------|
| 高階    | 60 FPS  | < 60%     | < 15%   |
| 中階    | 30 FPS  | < 80%     | < 25%   |
| 低階    | 關閉功能 | N/A      | N/A     |

#### V3.0 可交付成果

- ✅ 完整 GLES 渲染管線
- ✅ 多種 Tone Mapping 演算法 (Reinhard/ACES/Filmic)
- ✅ 自動參數調整 (基於直方圖)
- ✅ 使用者可控 HDR 強度
- ✅ 設定介面 (演算法選擇、參數調整)
- ✅ 效能優化 (LUT、降級策略)
- ✅ 多廠牌相容性測試報告

---

### 階段四：AI 智能補光 (V4.0)

**時間預估**: 3-4 個月 (12-16 週)
**核心目標**: 使用 TFLite 人像分割模型實現即時智能補光

⚠️ **警告**: 此階段需要 AI 模型訓練經驗，建議考慮使用預訓練模型

#### Week 1-6: TFLite 模型訓練 (離線任務)

**選項 A: 自行訓練**

**任務清單**:
- [ ] 收集人像分割資料集
  - 推薦資料集: COCO Person Segmentation, Supervisely Person Dataset
  - 最少需要: 10,000 張標記圖片
- [ ] 建立訓練環境
  ```python
  # requirements.txt
  tensorflow==2.13.0
  tensorflow-datasets
  pillow
  numpy
  opencv-python
  ```
- [ ] 選擇模型架構
  - DeepLab V3+ (精度高但較慢)
  - MobileNet V3 + U-Net (輕量級)
  - MediaPipe Selfie Segmentation (Google 預訓練)
- [ ] 訓練模型
  ```python
  model = create_segmentation_model(
      backbone='mobilenet_v3_small',
      input_size=(256, 256)
  )
  model.fit(train_dataset, epochs=50, ...)
  ```
- [ ] 量化模型 (FP16 或 INT8)
  ```python
  converter = tf.lite.TFLiteConverter.from_keras_model(model)
  converter.optimizations = [tf.lite.Optimize.DEFAULT]
  converter.target_spec.supported_types = [tf.float16]
  tflite_model = converter.convert()
  ```
- [ ] 驗證模型效能
  - 目標: 推理時間 < 50ms (在 Pixel 7a)
  - 目標: 模型大小 < 5MB
  - 目標: IoU (Intersection over Union) > 0.85

**選項 B: 使用預訓練模型 (推薦)**

- [ ] 下載 MediaPipe Selfie Segmentation 模型
  ```
  https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite
  ```
- [ ] 評估模型效能 (在目標設備測試)
- [ ] 如需調整，進行 Transfer Learning

#### Week 7-9: TFLite 整合

**任務清單**:
- [ ] 添加 TFLite 依賴
  ```kotlin
  implementation("org.tensorflow:tensorflow-lite:2.13.0")
  implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")
  implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
  ```
- [ ] 將模型放入 `assets/models/`
  ```
  app/src/main/assets/models/
  └── portrait_segmentation.tflite
  ```
- [ ] 建立 `data/source/ai/TFLiteSegmentationModel.kt`
  ```kotlin
  class TFLiteSegmentationModel @Inject constructor(
      @ApplicationContext private val context: Context
  ) {
      private lateinit var interpreter: Interpreter

      fun initialize() {
          val model = loadModelFile("models/portrait_segmentation.tflite")
          val options = Interpreter.Options().apply {
              // 啟用 GPU Delegate
              addDelegate(GpuDelegate())
              setNumThreads(4)
          }
          interpreter = Interpreter(model, options)
      }

      fun segment(bitmap: Bitmap): FloatArray {
          // 執行推理
      }
  }
  ```
- [ ] 建立 `ModelLoader.kt` (處理模型載入)
- [ ] 建立 `SegmentationResult.kt`
  ```kotlin
  data class SegmentationResult(
      val mask: FloatArray,      // 0.0-1.0, 人像機率
      val width: Int,
      val height: Int,
      val inferenceTime: Long    // 毫秒
  )
  ```
- [ ] 整合到 ImageAnalysis 管線
  ```kotlin
  override fun analyze(imageProxy: ImageProxy) {
      val bitmap = imageProxy.toBitmap()
      val segmentationResult = tfliteModel.segment(bitmap)
      // 傳遞給 Shader
  }
  ```
- [ ] 實作 Mask → GPU 紋理轉換
  ```kotlin
  fun uploadMaskToGPU(mask: FloatArray): Int {
      val textureId = IntArray(1)
      GLES30.glGenTextures(1, textureId, 0)
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0])
      // 上傳 mask 數據
      return textureId[0]
  }
  ```

**效能優化**:
- 使用 GPU Delegate (可加速 2-5 倍)
- 降低輸入解析度 (256x256 通常足夠)
- 與 ML Kit 交替執行 (避免 GPU 過載)

#### Week 10-12: 智能補光 Shader

**任務清單**:
- [ ] 實作智能補光 Fragment Shader
  ```glsl
  // fragment_smart_fill_light.glsl
  precision mediump float;

  varying vec2 v_texCoord;
  uniform sampler2D u_texture;      // 原始相機幀
  uniform sampler2D u_mask;         // 人像分割 Mask
  uniform float u_fillStrength;     // 補光強度 (0.0-1.0)

  void main() {
      vec4 originalColor = texture2D(u_texture, v_texCoord);
      float maskValue = texture2D(u_mask, v_texCoord).r;

      // 只針對人像區域 (mask > 0.5) 進行提亮
      if (maskValue > 0.5) {
          // S-Curve 提亮 (保持自然)
          vec3 brightened = pow(originalColor.rgb, vec3(1.0 - u_fillStrength * 0.3));

          // 邊緣羽化 (避免生硬邊界)
          float feather = smoothstep(0.4, 0.6, maskValue);
          originalColor.rgb = mix(originalColor.rgb, brightened, feather);
      }

      gl_FragColor = originalColor;
  }
  ```
- [ ] 實作自然過渡演算法
  ```glsl
  // 高斯模糊 Mask 邊緣
  vec3 gaussianBlur(sampler2D tex, vec2 uv, float radius) {
      // 5x5 高斯核心
  }
  ```
- [ ] 根據逆光程度自動調整補光強度
  ```kotlin
  fun calculateFillStrength(backlitSeverity: Float): Float {
      return when {
          backlitSeverity > 2.0 -> 1.0f     // 嚴重逆光，最大補光
          backlitSeverity > 1.5 -> 0.7f     // 中度逆光
          backlitSeverity > 1.2 -> 0.4f     // 輕度逆光
          else -> 0.0f                       // 正常光線，不補光
      }
  }
  ```
- [ ] 實作色溫補償 (避免補光後偏色)
  ```glsl
  vec3 colorTemperatureCompensation(vec3 color, float strength) {
      // 補光後略微增加暖色調
      color.r *= 1.0 + strength * 0.05;
      color.g *= 1.0 + strength * 0.03;
      return color;
  }
  ```
- [ ] 整合到完整渲染管線
  ```
  Camera Frame → Tone Mapping Pass → Smart Fill Light Pass → Output
  ```

**補光演算法要點**:
- 使用 S-Curve 而非線性提亮 (更自然)
- Mask 邊緣必須羽化 (feather)，避免明顯邊界
- 不要過度補光 (保持最大 40% 提亮)
- 補償色溫避免偏藍

#### Week 13-16: 最終優化

**任務清單**:
- [ ] 瘋狂效能優化
  - TFLite 推理與 ML Kit 交替執行
  - 使用 RenderScript 加速 Mask 處理 (如果需要)
  - 實作多級 LOD (Level of Detail)
    - 高階設備: 512x512 Mask
    - 中階設備: 256x256 Mask
    - 低階設備: 關閉功能
- [ ] A/B 測試補光演算法參數
  - 測試組 A: 強補光 (strength = 1.0)
  - 測試組 B: 中補光 (strength = 0.7)
  - 測試組 C: 弱補光 (strength = 0.4)
  - 收集使用者偏好數據
- [ ] 使用者測試
  - 招募 20-30 位測試者
  - 收集反饋問卷 (補光效果、自然度、效能)
  - 迭代優化參數
- [ ] 撰寫完整技術文檔
  - 架構圖
  - API 文檔
  - 效能測試報告
  - 使用者指南
- [ ] 準備 V4.0 發布
  - 版本號: 4.0.0
  - Release Notes
  - 更新 Google Play 商店描述

**最終效能目標**:
| 功能 | 高階設備 | 中階設備 | 低階設備 |
|-----|---------|---------|---------|
| TFLite 推理 | < 30ms | < 50ms | 關閉 |
| 完整管線延遲 | < 100ms | < 150ms | N/A |
| FPS | 60 | 30 | 20 (無 AI) |
| 記憶體 | < 300MB | < 200MB | < 150MB |

#### V4.0 可交付成果

- ✅ TFLite 人像分割模型整合
- ✅ GPU Delegate 加速
- ✅ 智能補光 Shader (選擇性提亮)
- ✅ 自動補光強度調整 (基於逆光程度)
- ✅ 色溫補償演算法
- ✅ 完整效能優化 (中階機 30fps)
- ✅ 使用者測試報告
- ✅ 完整技術文檔

---

## 3. 開發環境需求

### 3.1 硬體需求

#### 開發主機 (必要)

**最低配置**:
- CPU: AMD Ryzen 5 / Intel Core i5 (6 核心)
- RAM: 16GB
- SSD: 256GB
- 作業系統: Windows 10/11, macOS 12+, Ubuntu 20.04+

**建議配置**:
- CPU: AMD Ryzen 7 / Intel Core i7 (8 核心以上)
- RAM: 32GB (Android Studio + 模擬器 + IDE 需大量記憶體)
- SSD: 512GB NVMe (快速編譯)
- 作業系統: Windows 11 / macOS 14+ / Ubuntu 22.04

#### AI 訓練機 (V4.0 階段，可選)

**本地訓練機**:
- GPU: NVIDIA RTX 3070 / 4070 或以上 (至少 8GB VRAM)
- RAM: 32GB 以上
- SSD: 1TB (儲存資料集與訓練檢查點)
- CUDA: 11.8 或以上

**雲端訓練 (替代方案)**:
- **Google Colab Pro**: $10/月，適合小規模訓練 (<1000 epochs)
- **Google Cloud Vertex AI**: 適合大規模訓練，按使用量計費
- **Kaggle Notebooks**: 免費 GPU (每週 30 小時)，適合實驗

#### 測試設備 (必要)

⚠️ **絕對不能只用模擬器**: 相機與 GPU 效能無法準確模擬

**必備設備清單**:

| 等級 | 設備範例 | 數量 | 用途 |
|-----|---------|------|-----|
| 高階 | Pixel 8 Pro, S24 Ultra | 1台 | 驗證效能上限、最佳體驗 |
| 中階 | Pixel 7a, A54 | 1-2台 | **最重要**，基準測試 |
| 低階 | 任何 Android 7.0+ | 1台 | 相容性測試、優雅降級 |

**推薦中階測試機**:
- Google Pixel 7a (參考價: 約 $499)
  - GPU: Mali-G710 MC10
  - RAM: 8GB
  - 代表大部分使用者設備
- Samsung Galaxy A54 (參考價: 約 $449)
  - GPU: Adreno 642L
  - RAM: 6GB/8GB
  - Samsung 用戶佔比高

**為什麼中階機最重要**:
- 高階機 (< 5% 用戶): 效能過剩，任何功能都流暢
- 中階機 (60-70% 用戶): 效能瓶頸明顯，優化重點
- 低階機 (25-35% 用戶): 基礎功能即可，AI 功能優雅降級

### 3.2 軟體環境

#### 開發工具

| 工具 | 版本 | 用途 |
|-----|------|-----|
| Android Studio | Ladybug 2024.2.1+ | 主 IDE |
| Kotlin | 2.0.21 | 開發語言 |
| Gradle | 8.10+ | 建構工具 |
| JDK | 11 (專案) / 17 (本機) | Java 環境 |
| Git | 2.40+ | 版本控制 |
| Android SDK | API 24-36 | Android 平台 |

#### 核心 Library (已整合)

```kotlin
// 已整合的依賴
dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.11.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.camera:camera-extensions:1.4.1")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
```

#### 待整合 Library (按階段)

**V1.0 階段**:
```kotlin
// ML Kit
implementation("com.google.mlkit:object-detection:17.0.1")
implementation("com.google.mlkit:face-detection:16.1.5")
```

**V2.0 階段 (可選)**:
```kotlin
// OpenCV (進階圖像處理)
implementation("org.opencv:opencv-android:4.8.0")
```

**V4.0 階段**:
```kotlin
// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.13.0")
implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
```

#### AI 訓練環境 (V4.0)

**Python 環境**:
```bash
# 使用 Python 3.9-3.11
pip install tensorflow==2.13.0
pip install tensorflow-datasets
pip install keras
pip install numpy pillow opencv-python
pip install matplotlib jupyter  # 用於視覺化
```

**TensorFlow Lite 轉換**:
```python
# convert_model.py
import tensorflow as tf

model = tf.keras.models.load_model('segmentation_model.h5')
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]
tflite_model = converter.convert()

with open('portrait_segmentation.tflite', 'wb') as f:
    f.write(tflite_model)
```

### 3.3 開發環境設定

#### Android Studio 設定

**記憶體設定** (`studio.vmoptions`):
```
-Xms2048m
-Xmx8192m
-XX:ReservedCodeCacheSize=1024m
```

**Gradle 設定** (`gradle.properties`):
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
org.gradle.parallel=true
org.gradle.caching=true
kotlin.incremental=true
```

**推薦 Plugin**:
- Kotlin
- Compose Multiplatform IDE Support
- Android Profiler
- LeakCanary (記憶體洩漏檢測)

#### 實體設備設定

**啟用開發者選項**:
1. 設定 → 關於手機 → 連續點擊「版本號」7次
2. 開發者選項 → 啟用「USB 除錯」
3. 開發者選項 → 啟用「GPU 渲染模式分析」(效能監控)

**ADB 連接測試**:
```bash
adb devices
# 應顯示已連接設備
```

---

## 4. 技術架構設計

### 4.1 應用層架構圖

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────────────────────────────────────────┐   │
│  │           Jetpack Compose UI (Material 3)        │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐ │   │
│  │  │  Camera    │  │  Settings  │  │   Gallery  │ │   │
│  │  │  Screen    │  │  Screen    │  │   Screen   │ │   │
│  │  └─────┬──────┘  └────────────┘  └────────────┘ │   │
│  │        │                                          │   │
│  │        ├─ CameraPreview                           │   │
│  │        ├─ Overlay Components                      │   │
│  │        │   ├─ GridOverlay (三分法)               │   │
│  │        │   ├─ LevelIndicator (水平儀)            │   │
│  │        │   ├─ CompositionGuide (構圖提示)        │   │
│  │        │   └─ DetectionBoxes (偵測框)            │   │
│  │        └─ Control Buttons                         │   │
│  └────────────────────────┬───────────────────────────┘   │
│                           │                               │
│  ┌────────────────────────▼───────────────────────────┐   │
│  │              ViewModel (Hilt)                      │   │
│  │  - CameraViewModel                                 │   │
│  │  - State Management (StateFlow)                    │   │
│  │  - Event Handling (CameraUiEvent)                  │   │
│  └────────────────────────┬───────────────────────────┘   │
└───────────────────────────┼───────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────┐
│                      Domain Layer                         │
│  ┌──────────────────────────────────────────────────┐    │
│  │                   Use Cases                       │    │
│  │  - AnalyzeCompositionUseCase                      │    │
│  │  - GenerateGuideUseCase                           │    │
│  │  - DetectLightingUseCase                          │    │
│  │  - SegmentPortraitUseCase (V4.0)                  │    │
│  └─────────────────────┬────────────────────────────┘    │
│                        │                                  │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │              Repository Interfaces                │    │
│  │  - CameraRepository                               │    │
│  │  - AIRepository (V1.0+)                           │    │
│  └─────────────────────┬────────────────────────────┘    │
│                        │                                  │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │              Domain Models                        │    │
│  │  - CaptureResult                                  │    │
│  │  - DetectionResult                                │    │
│  │  - CompositionSuggestion                          │    │
│  │  - LightingCondition                              │    │
│  │  - SegmentationResult (V4.0)                      │    │
│  └───────────────────────────────────────────────────┘    │
└───────────────────────────┬───────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────┐
│                       Data Layer                          │
│  ┌──────────────────────────────────────────────────┐    │
│  │           Repository Implementations              │    │
│  │  - CameraRepositoryImpl (已完成)                  │    │
│  │  - AIRepositoryImpl (V1.0+)                       │    │
│  └─────────────────────┬────────────────────────────┘    │
│                        │                                  │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │                Data Sources                       │    │
│  │  ┌──────────────┐  ┌──────────────┐              │    │
│  │  │   camera/    │  │     ml/      │              │    │
│  │  │  (CameraX)   │  │  (ML Kit +   │              │    │
│  │  │              │  │   TFLite)    │              │    │
│  │  └──────────────┘  └──────────────┘              │    │
│  │  ┌──────────────┐  ┌──────────────┐              │    │
│  │  │  analysis/   │  │  rendering/  │              │    │
│  │  │ (Histogram,  │  │  (GLES,      │              │    │
│  │  │  Lighting)   │  │   Shaders)   │              │    │
│  │  └──────────────┘  └──────────────┘              │    │
│  └───────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────┘
```

### 4.2 相機處理管線

```
┌──────────────────┐
│  Camera Hardware │
└────────┬─────────┘
         │
         ▼
┌──────────────────────────────┐
│      CameraX API             │
│  (LifecycleCameraController) │
└────────┬────────────┬────────┘
         │            │
         │            └──────────────────┐
         ▼                               ▼
┌─────────────────┐          ┌─────────────────────────┐
│  Preview        │          │  ImageAnalysis          │
│  UseCase        │          │  UseCase                │
│                 │          │  (640x480, YUV_420_888) │
└────────┬────────┘          └───────────┬─────────────┘
         │                               │
         │                  ┌────────────▼────────────┐
         │                  │   Frame Throttling      │
         │                  │   (10 fps analysis)     │
         │                  └────────────┬────────────┘
         │                               │
         │                  ┌────────────▼────────────┐
         │                  │     ML Pipeline         │
         │                  │  ┌──────────────────┐   │
         │                  │  │  ML Kit Detector │   │
         │                  │  │  - Face Detection│   │
         │                  │  │  - Object Det.   │   │
         │                  │  └────────┬─────────┘   │
         │                  │           │             │
         │                  │  ┌────────▼─────────┐   │
         │                  │  │ TFLite Segmenter │   │ (V4.0)
         │                  │  │ - Portrait Mask  │   │
         │                  │  └────────┬─────────┘   │
         │                  └───────────┬─────────────┘
         │                              │
         │                  ┌───────────▼─────────────┐
         │                  │  Analysis Pipeline      │
         │                  │  ┌──────────────────┐   │
         │                  │  │ Histogram        │   │
         │                  │  │ Analyzer         │   │
         │                  │  └────────┬─────────┘   │
         │                  │           │             │
         │                  │  ┌────────▼─────────┐   │
         │                  │  │ Composition      │   │
         │                  │  │ Engine           │   │
         │                  │  └────────┬─────────┘   │
         │                  │           │             │
         │                  │  ┌────────▼─────────┐   │
         │                  │  │ Lighting         │   │
         │                  │  │ Detector         │   │
         │                  │  └────────┬─────────┘   │
         │                  └───────────┬─────────────┘
         │                              │
         │                  ┌───────────▼─────────────┐
         │                  │  Results Aggregation    │
         │                  │  - Suggestions          │
         │                  │  - Lighting Condition   │
         │                  │  - Segmentation Mask    │
         │                  └───────────┬─────────────┘
         │                              │
         ▼                              ▼
┌────────────────────────────────────────────────────────┐
│           GPU Rendering Pipeline (V3.0+)               │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Input: Camera Frame (OES Texture)              │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                  │
│  ┌───────────────────▼─────────────────────────────┐   │
│  │  Pass 1: Tone Mapping                           │   │
│  │  - Fragment Shader: Reinhard/ACES/Filmic        │   │
│  │  - Uniforms: exposure, contrast                 │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                  │
│  ┌───────────────────▼─────────────────────────────┐   │
│  │  Pass 2: Smart Fill Light (V4.0)                │   │
│  │  - Input: Tone-mapped frame + Segmentation Mask │   │
│  │  - Shader: Selective brightening                │   │
│  │  - Uniforms: fillStrength, featherRadius        │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                  │
│  ┌───────────────────▼─────────────────────────────┐   │
│  │  Output: Enhanced Frame                          │   │
│  └──────────────────────────────────────────────────┘   │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│              Compose UI Overlay Layer                  │
│  - GridOverlay (三分法網格)                            │
│  - CompositionGuide (動態提示)                         │
│  - LevelIndicator (水平儀)                             │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│              Final Display (用戶看到的畫面)             │
└────────────────────────────────────────────────────────┘
```

### 4.3 目錄結構設計

```
app/src/main/java/local/mylab/smartcameraapp1/
│
├── domain/                                 # 領域層 (業務邏輯)
│   ├── model/                              # 領域模型
│   │   ├── CaptureResult.kt               # [已存在] 拍照結果
│   │   ├── DetectionResult.kt             # [新增 V1.0] ML Kit 偵測結果
│   │   ├── CompositionSuggestion.kt       # [新增 V1.0] 構圖建議
│   │   ├── CompositionScore.kt            # [新增 V2.0] 構圖評分
│   │   ├── LightingCondition.kt           # [新增 V2.0] 光影狀況
│   │   ├── ShootingAdvice.kt              # [新增 V2.0] 拍攝建議
│   │   ├── SegmentationResult.kt          # [新增 V4.0] 分割結果
│   │   └── ToneMappingParams.kt           # [新增 V3.0] Tone Mapping 參數
│   │
│   ├── repository/                         # Repository 接口
│   │   ├── CameraRepository.kt            # [已存在] 相機操作接口
│   │   └── AIRepository.kt                # [新增 V1.0] AI 功能接口
│   │
│   └── usecase/                            # 用例層
│       ├── AnalyzeCompositionUseCase.kt   # [新增 V1.0] 構圖分析
│       ├── GenerateGuideUseCase.kt        # [新增 V1.0] 產生引導建議
│       ├── DetectLightingUseCase.kt       # [新增 V2.0] 光影偵測
│       ├── CalculateScoreUseCase.kt       # [新增 V2.0] 評分計算
│       └── SegmentPortraitUseCase.kt      # [新增 V4.0] 人像分割
│
├── data/                                   # 數據層 (具體實現)
│   ├── repository/                         # Repository 實現
│   │   ├── CameraRepositoryImpl.kt        # [已存在] 相機實現
│   │   └── AIRepositoryImpl.kt            # [新增 V1.0] AI 功能實現
│   │
│   └── source/                             # 數據源
│       ├── camera/                         # [預留] 相機數據源
│       │   └── CameraDataSource.kt        # [可選] 相機數據源封裝
│       │
│       ├── ml/                             # [新增 V1.0] ML Kit
│       │   ├── ImageAnalyzer.kt           # ImageAnalysis 處理器
│       │   ├── MLKitDetector.kt           # ML Kit API 封裝
│       │   ├── FaceDetector.kt            # 人臉偵測封裝
│       │   └── ObjectDetector.kt          # 物體偵測封裝
│       │
│       ├── analysis/                       # [新增 V2.0] 圖像分析
│       │   ├── HistogramAnalyzer.kt       # 直方圖分析
│       │   ├── LightingDetector.kt        # 光影偵測
│       │   ├── CompositionEngine.kt       # 構圖引擎
│       │   └── SceneClassifier.kt         # 場景分類
│       │
│       ├── ai/                             # [新增 V4.0] TensorFlow Lite
│       │   ├── TFLiteSegmentationModel.kt # TFLite 模型封裝
│       │   ├── ModelLoader.kt             # 模型載入器
│       │   └── MaskProcessor.kt           # Mask 後處理
│       │
│       └── rendering/                      # [新增 V3.0] GPU 渲染
│           ├── GLESRenderer.kt            # GLES 渲染器
│           ├── ShaderProgram.kt           # Shader 程式管理
│           ├── TextureConverter.kt        # 紋理轉換
│           └── RenderPipeline.kt          # 渲染管線編排
│
├── presentation/                           # 表現層 (UI)
│   └── camera/                             # 相機模組
│       ├── CameraScreen.kt                # [已存在] 主相機屏幕
│       ├── CameraViewModel.kt             # [已存在] ViewModel
│       ├── CameraState.kt                 # [已存在] UI 狀態
│       │
│       └── components/                     # UI 元件
│           ├── CameraPreview.kt           # [已存在] 相機預覽
│           ├── PermissionRequestScreen.kt # [已存在] 權限請求
│           ├── MediaPermissionHandler.kt  # [已存在] 媒體權限
│           │
│           └── overlay/                    # [新增 V1.0] 覆蓋層元件
│               ├── GridOverlay.kt         # 三分法網格
│               ├── LevelIndicator.kt      # 水平儀
│               ├── CompositionGuide.kt    # 構圖引導
│               ├── DetectionBoxes.kt      # 偵測框 (調試)
│               ├── LightingIndicator.kt   # [V2.0] 光影指示器
│               └── ScoreDisplay.kt        # [V2.0] 評分顯示
│
├── di/                                     # 依賴注入
│   ├── AppModule.kt                       # [已存在] App 模組
│   ├── CameraModule.kt                    # [可選] 相機模組
│   └── AIModule.kt                        # [新增 V1.0] AI 模組
│
├── ui/theme/                               # UI 主題
│   ├── Color.kt                           # [已存在] 顏色定義
│   ├── Theme.kt                           # [已存在] 主題配置
│   └── Type.kt                            # [已存在] 排版配置
│
├── util/                                   # 工具類
│   ├── Constants.kt                       # [已存在] 常數定義
│   ├── PermissionUtils.kt                 # [已存在] 權限工具
│   ├── MathUtils.kt                       # [新增 V1.0] 數學工具
│   └── ImageUtils.kt                      # [新增 V1.0] 圖像工具
│
├── MainActivity.kt                         # [已存在] 主 Activity
└── SmartCameraApplication.kt              # [已存在] Application

app/src/main/assets/
├── models/                                 # [新增 V4.0] AI 模型
│   └── portrait_segmentation.tflite       # 人像分割模型
│
└── shaders/                                # [新增 V3.0] GLSL Shaders
    ├── vertex_shader.glsl                 # 頂點著色器
    ├── fragment_base.glsl                 # 基礎片段著色器
    ├── fragment_tone_mapping.glsl         # Tone Mapping
    └── fragment_smart_fill_light.glsl     # 智能補光
```

---

## 5. 效能優化策略

### 5.1 影像分析優化

#### 解析度優化
```kotlin
// 在 ImageAnalyzer 中設定低解析度
imageAnalysis.setTargetResolution(Size(640, 480))  // 而非 1920x1080
```

**理由**:
- ML Kit 偵測精度在 640x480 已足夠
- 解析度降低 4 倍 → 處理時間減少約 75%
- 記憶體使用減少 75%

#### 策略選擇
```kotlin
imageAnalysis.setBackpressureStrategy(
    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST  // 放棄過時幀
)
```

**理由**:
- 避免分析積壓 (backlog)
- 確保分析的是最新畫面
- 防止延遲累積

#### 幀率節流
```kotlin
class ThrottledImageAnalyzer(
    private val targetFps: Int = 10,
    private val analyzer: (ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastAnalysisTime = 0L
    private val minInterval = 1000L / targetFps

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime >= minInterval) {
            analyzer(image)
            lastAnalysisTime = currentTime
        }
        image.close()  // 必須關閉！
    }
}
```

**建議幀率**:
- V1.0 (僅 ML Kit): 10 fps
- V2.0 (+ 光影分析): 8 fps
- V4.0 (+ TFLite): 5-8 fps (視設備)

#### 執行緒策略
```kotlin
private val analysisExecutor = Executors.newSingleThreadExecutor()

imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
    // 分析邏輯在背景執行緒執行
}
```

**注意事項**:
- 不要使用 `Dispatchers.Main`
- 單執行緒避免競爭條件
- 記得釋放 `ImageProxy` (`image.close()`)

### 5.2 ML 模型優化

#### TFLite 模型量化
```python
# 訓練時量化
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]

# FP16 量化 (推薦)
converter.target_spec.supported_types = [tf.float16]

# INT8 量化 (更激進，需代表性資料集)
def representative_dataset():
    for _ in range(100):
        yield [np.random.rand(1, 256, 256, 3).astype(np.float32)]

converter.representative_dataset = representative_dataset
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
```

**量化效果**:
| 量化方式 | 模型大小 | 推理速度 | 精度損失 |
|---------|---------|---------|---------|
| FP32 (無) | 100% | 基準 | 0% |
| FP16 | 50% | 1.5-2x | <1% |
| INT8 | 25% | 2-3x | 1-3% |

#### GPU Delegate
```kotlin
val options = Interpreter.Options().apply {
    addDelegate(GpuDelegate(
        GpuDelegate.Options().apply {
            setInferencePreference(
                GpuDelegate.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER
            )
        }
    ))
    setNumThreads(4)  // CPU 執行緒數
}
interpreter = Interpreter(modelBuffer, options)
```

**GPU 加速效果**:
- 高階設備 (Adreno 700+): 3-5x 加速
- 中階設備 (Adreno 600+): 2-3x 加速
- 低階設備 (Mali 400+): 可能更慢 (不建議)

#### 模型輸入優化
```kotlin
// 避免不必要的 Bitmap 轉換
fun preprocessImageProxy(imageProxy: ImageProxy): ByteBuffer {
    // 直接從 YUV 轉換為模型輸入格式
    val yBuffer = imageProxy.planes[0].buffer
    // ... 直接處理 YUV 數據
}
```

### 5.3 GPU 渲染優化

#### Shader 優化技巧

**1. 使用適當精度**
```glsl
// ❌ 過高精度 (浪費)
precision highp float;

// ✅ 適當精度 (大多數情況)
precision mediump float;

// ✅ 低精度變數 (顏色計算)
lowp vec4 color;
```

**2. 避免分支**
```glsl
// ❌ 使用 if/else (GPU 效能殺手)
if (maskValue > 0.5) {
    color = brighten(color);
}

// ✅ 使用 mix() 或 step()
float mask = step(0.5, maskValue);
color = mix(color, brighten(color), mask);
```

**3. 預計算常數**
```kotlin
// 在 CPU 端計算
val exposure = 1.0f / avgBrightness
val contrast = calculateContrast(histogram)

// 傳入 Shader
GLES30.glUniform1f(exposureHandle, exposure)
GLES30.glUniform1f(contrastHandle, contrast)
```

```glsl
// Shader 中直接使用
uniform float u_exposure;
uniform float u_contrast;

void main() {
    vec3 color = texture2D(u_texture, v_texCoord).rgb;
    color *= u_exposure;  // 不需重新計算
}
```

#### 查找表 (LUT) 優化

**建立 LUT**:
```kotlin
fun generateToneMappingLUT(
    algorithm: ToneMappingAlgorithm,
    size: Int = 256
): IntArray {
    val lut = IntArray(size * size * 3)
    for (r in 0 until size) {
        for (g in 0 until size) {
            val color = floatArrayOf(r / 255f, g / 255f, 0f)
            val mapped = algorithm.apply(color)
            lut[(r * size + g) * 3 + 0] = (mapped[0] * 255).toInt()
            lut[(r * size + g) * 3 + 1] = (mapped[1] * 255).toInt()
            lut[(r * size + g) * 3 + 2] = (mapped[2] * 255).toInt()
        }
    }
    return lut
}
```

**在 Shader 中使用**:
```glsl
uniform sampler2D u_lut;  // 2D 查找表紋理

vec3 toneMappingWithLUT(vec3 color) {
    // 直接查表，避免複雜計算
    return texture2D(u_lut, color.rg).rgb;
}
```

**效能提升**: 複雜 Tone Mapping 計算從 ~10ms → ~1ms

#### 渲染管線優化

**雙緩衝 / 三緩衝**:
```kotlin
class DoubleBufferedRenderer {
    private val framebuffers = IntArray(2)
    private var currentFBO = 0

    fun swapBuffers() {
        currentFBO = 1 - currentFBO
    }

    fun getCurrentFBO() = framebuffers[currentFBO]
}
```

**減少紋理傳輸**:
```kotlin
// ❌ 每幀都上傳 Mask (慢)
GLES30.glTexImage2D(..., maskData)

// ✅ 只在 Mask 更新時上傳
if (maskUpdated) {
    GLES30.glTexSubImage2D(..., maskData)
    maskUpdated = false
}
```

### 5.4 記憶體優化

#### Bitmap 管理
```kotlin
class BitmapPool {
    private val pool = mutableListOf<Bitmap>()

    fun obtain(width: Int, height: Int): Bitmap {
        return pool.removeFirstOrNull()
            ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun recycle(bitmap: Bitmap) {
        bitmap.eraseColor(Color.TRANSPARENT)
        pool.add(bitmap)
    }
}
```

#### ImageProxy 及時釋放
```kotlin
override fun analyze(image: ImageProxy) {
    try {
        // 處理邏輯
    } finally {
        image.close()  // 必須在 finally 中關閉！
    }
}
```

#### 記憶體洩漏檢測
```kotlin
// build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
```

### 5.5 降級策略

#### 設備分級
```kotlin
enum class DeviceTier {
    HIGH, MEDIUM, LOW
}

fun getDeviceTier(): DeviceTier {
    val activityManager = getSystemService<ActivityManager>()
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memoryInfo)

    val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024)
    val gpuRenderer = GLES30.glGetString(GLES30.GL_RENDERER)

    return when {
        totalRamGB >= 8 && gpuRenderer.contains("Adreno 7") -> DeviceTier.HIGH
        totalRamGB >= 6 && gpuRenderer.contains("Adreno 6") -> DeviceTier.MEDIUM
        else -> DeviceTier.LOW
    }
}
```

#### 功能降級
```kotlin
val features = when (getDeviceTier()) {
    DeviceTier.HIGH -> Features(
        mlKitEnabled = true,
        tfliteEnabled = true,
        glesRendering = true,
        analysisResolution = Size(640, 480),
        analysisFps = 10
    )
    DeviceTier.MEDIUM -> Features(
        mlKitEnabled = true,
        tfliteEnabled = false,  // 關閉 TFLite
        glesRendering = true,
        analysisResolution = Size(480, 360),
        analysisFps = 8
    )
    DeviceTier.LOW -> Features(
        mlKitEnabled = false,  // 關閉所有 AI 功能
        tfliteEnabled = false,
        glesRendering = false,
        analysisResolution = Size(320, 240),
        analysisFps = 5
    )
}
```

---

## 6. 開發規範整合

### 6.1 遵循 CLAUDE.md 規範

本專案為 Android 應用，主要使用 Kotlin 開發，但若需建立自動化腳本（如建構、部署、測試腳本），應遵循 `CLAUDE.md` 規範。

#### 腳本檔頭模板

```bash
#!/bin/bash
#
# Script Name:    build_and_test.sh
# Description:    Build APK, run tests, and generate reports
# Version:        1.0
# Date:           2025-11-12
# Author:         SmartCamera Team
# Usage:          ./build_and_test.sh [OPTIONS]
#
# Required Options:
#   -v          Show version and brief description
#
# Optional Parameters:
#   -h, --help  Show help message
#   -t TYPE     Build type (debug/release)
#   -d DEVICE   Target device ID
#   -s          Skip tests
#

# Display Symbols
OK_SYMBOL="[V]"
WARN_SYMBOL="[!]"
FAIL_SYMBOL="[X]"
INFO_SYMBOL="[i]"

# Script Version
SCRIPT_VERSION="1.0"
SCRIPT_NAME="build_and_test.sh"
LAST_UPDATED="2025-11-12"
```

#### 變數命名規範

```bash
# ✅ 良好範例 (≥3字元, snake_case, 有意義)
build_type="debug"
target_device_id="emulator-5554"
apk_output_directory="app/build/outputs/apk"
test_results_file="test_results.xml"
max_retry_count=3

# ❌ 避免使用
bt="debug"              # 太短
d="emulator-5554"       # 無意義
ap="app/build/..."      # 縮寫不清楚
x=3                     # 太短且無意義
```

#### 參數處理範例

```bash
# Handle -v parameter (required)
show_version() {
    echo "${SCRIPT_NAME} v${SCRIPT_VERSION}"
    echo "Build and test automation for SmartCamera App"
    echo "Last Updated: ${LAST_UPDATED}"
    exit 0
}

# Handle parameters
while [[ $# -gt 0 ]]; do
    case $1 in
        -v)
            show_version
            ;;
        -h|--help)
            show_help
            ;;
        -t)
            build_type="$2"
            shift 2
            ;;
        -d)
            target_device_id="$2"
            shift 2
            ;;
        -s)
            skip_tests=true
            shift
            ;;
        *)
            echo "${FAIL_SYMBOL} Error: Unknown parameter $1"
            echo "Use -h for help"
            exit 1
            ;;
    esac
done
```

#### 錯誤處理

```bash
# Check if Gradle wrapper exists
if [[ ! -f "./gradlew" ]]; then
    echo "${FAIL_SYMBOL} Error: gradlew not found in current directory"
    exit 1
fi

# Build APK
echo "${INFO_SYMBOL} Building APK..."
if ./gradlew assemble${build_type^} 2>&1 | tee build.log; then
    echo "${OK_SYMBOL} Build successful"
else
    echo "${FAIL_SYMBOL} Build failed. Check build.log for details"
    exit 1
fi
```

### 6.2 Git 版本控制

#### Commit 訊息格式

```
[版本號] 簡短說明 (不超過 50 字元)

詳細變更內容：
- 新增功能 A
- 修正錯誤 B
- 優化效能 C

Related: #issue_number (如適用)
```

#### Commit 範例

```
[1.0] Add ML Kit face detection integration

Changes:
- Add ML Kit dependencies to build.gradle.kts
- Implement ImageAnalyzer.kt for face detection
- Create DetectionResult.kt domain model
- Integrate face detection with CameraX ImageAnalysis
- Add unit tests for detection logic

Performance: Face detection runs at 10 fps on Pixel 7a
```

```
[1.1] Implement composition guide overlay UI

Changes:
- Create GridOverlay.kt for rule-of-thirds grid
- Create CompositionGuide.kt for dynamic suggestions
- Add overlay toggle in settings
- Update CameraScreen.kt to display overlays

UI: Material 3 design with accessibility support
```

```
[2.0-v1] Fix histogram analysis crash on low-light scenes

Changes:
- Add null check in HistogramAnalyzer.analyze()
- Clamp histogram values to valid range [0, 255]
- Add error logging for debugging

Fix: #42 (App crashes in dark environments)
```

#### 分支策略

```
master (main)
  ├── develop
  │   ├── feature/v1.0-ml-kit-integration
  │   ├── feature/v1.0-composition-engine
  │   ├── feature/v1.0-ui-overlay
  │   ├── feature/v2.0-lighting-analysis
  │   ├── feature/v3.0-gles-rendering
  │   └── feature/v4.0-tflite-segmentation
  │
  ├── release/v1.0
  ├── release/v2.0
  ├── release/v3.0
  └── release/v4.0
```

**分支命名規則**:
- `feature/[版本]-[功能描述]`: 新功能開發
- `bugfix/[issue號]-[簡短描述]`: Bug 修復
- `hotfix/[緊急問題描述]`: 緊急修補
- `release/[版本號]`: 發布分支

#### .gitignore 建議

```gitignore
# Android Studio
.gradle/
.idea/
*.iml
local.properties

# Build outputs
build/
app/build/

# Sensitive files
google-services.json
keystore.properties
*.jks

# Large files (use Git LFS)
*.tflite
*.onnx
*.pb

# Logs
*.log
chat_logs/*.txt  # 對話記錄不提交 (本地使用)
```

### 6.3 對話記錄管理

根據 `CLAUDE.md` 規範，對話記錄應儲存於 `chat_logs/` 目錄。

**現有對話記錄**:
```
chat_logs/
├── AI即時攝影師_專項開發計畫.md          # 原始計畫文檔
├── AI_PHOTOGRAPHER_COMPLETE_BLUEPRINT_20251112.md  # 本藍圖文檔
├── CONVERSATION_SUMMARY_20251112.md      # 對話摘要 (待建立)
└── ... (其他技術文檔)
```

**對話記錄檔案命名**:
- 技術文檔: `DOCUMENT_NAME_YYYYMMDD.md`
- 對話摘要: `CONVERSATION_SUMMARY_YYYYMMDD.md`
- 會議記錄: `MEETING_NOTES_YYYYMMDD.md`

---

## 7. 風險管理

### 7.1 技術風險

| 風險項目 | 可能性 | 影響程度 | 緩解措施 |
|---------|-------|---------|---------|
| GLES 學習曲線陡峭 | 高 | 高 | - 提前學習 OpenGL ES<br>- 使用現有 Shader 範例<br>- 考慮使用 Filament 引擎 |
| 中階機效能不足 | 中 | 高 | - 提前在中階機測試<br>- 實作降級策略<br>- 使用 LUT 優化 |
| GPU 廠牌相容性問題 | 中 | 中 | - 多廠牌測試 (Mali/Adreno/PowerVR)<br>- 提供 CPU 回退方案 |
| TFLite 模型精度不足 | 中 | 中 | - 使用預訓練模型 (MediaPipe)<br>- 充分資料集訓練<br>- A/B 測試 |
| 記憶體洩漏導致 OOM | 低 | 高 | - 使用 LeakCanary<br>- Bitmap 回收池<br>- 及時釋放 ImageProxy |

### 7.2 時程風險

| 階段 | 預估時間 | 風險因素 | 緩衝方案 |
|-----|---------|---------|---------|
| V1.0 | 2 個月 | ML Kit 整合問題 | +2 週緩衝 |
| V2.0 | 2 個月 | OpenCV 學習 | 可跳過 OpenCV，使用簡化演算法 |
| V3.0 | 3 個月 | GLES 技術難度 | **+4 週緩衝**，最高風險 |
| V4.0 | 3-4 個月 | 模型訓練時間 | 優先使用預訓練模型 |

**總時程**: 10-11 個月 (含緩衝: 12-13 個月)

### 7.3 資源風險

| 資源 | 需求 | 風險 | 解決方案 |
|-----|------|------|---------|
| 測試設備 | 3 台 (高/中/低階) | 預算不足 | 優先購買 1 台中階機 |
| AI 訓練機 | RTX 3070+ | 硬體成本高 | 使用 Colab Pro ($10/月) |
| 開發時間 | 全職 10-11 個月 | 時間不足 | 分階段發布，V1.0 先上線 |
| 團隊技能 | GLES + AI 專業 | 技能缺口 | 線上課程學習 + 外部諮詢 |

### 7.4 使用者風險

| 風險項目 | 可能性 | 影響 | 緩解措施 |
|---------|-------|------|---------|
| 使用者不理解 AI 建議 | 高 | 中 | - 提供教學模式<br>- 簡化提示文字<br>- 加入動畫引導 |
| 補光效果不自然 | 中 | 高 | - 保守補光參數<br>- 提供手動關閉選項<br>- A/B 測試 |
| 電池消耗增加 | 中 | 中 | - 優化 GPU 使用率<br>- 提供省電模式<br>- 透明化功耗資訊 |
| 隱私疑慮 (AI 處理) | 低 | 高 | - 明確說明本機處理<br>- 不上傳任何影像<br>- 開放原始碼 |

---

## 8. 學習資源

### 8.1 CameraX 與 ML Kit

#### 官方文檔
- [CameraX 官方指南](https://developer.android.com/training/camerax)
- [ML Kit 快速開始](https://developers.google.com/ml-kit)
- [ML Kit Face Detection](https://developers.google.com/ml-kit/vision/face-detection)
- [ML Kit Object Detection](https://developers.google.com/ml-kit/vision/object-detection)

#### Codelabs
- [CameraX Getting Started](https://codelabs.developers.google.com/codelabs/camerax-getting-started)
- [ML Kit with CameraX](https://codelabs.developers.google.com/codelabs/mlkit-android)

#### 範例專案
- [Android CameraX Samples](https://github.com/android/camera-samples)
- [ML Kit Samples](https://github.com/googlesamples/mlkit)

### 8.2 OpenGL ES 與 GPU 程式設計

#### 基礎教學
- [OpenGL ES for Android](https://developer.android.com/guide/topics/graphics/opengl)
- [Learn OpenGL](https://learnopengl.com/) (桌面版，概念相通)
- [The Book of Shaders](https://thebookofshaders.com/)

#### Shader 學習
- [Shader Toy](https://www.shadertoy.com/) - 線上 Shader 編輯器
- [GLSL Sandbox](http://glslsandbox.com/)
- [Shadertoy HOWTO](https://shadertoyunofficial.wordpress.com/)

#### 進階主題
- [GPU Gems (免費線上版)](https://developer.nvidia.com/gpugems/gpugems/contributors)
- [Real-Time Rendering Resources](https://www.realtimerendering.com/)

### 8.3 TensorFlow Lite

#### 官方資源
- [TFLite 官方文檔](https://www.tensorflow.org/lite)
- [TFLite 模型優化](https://www.tensorflow.org/lite/performance/best_practices)
- [TFLite GPU Delegate](https://www.tensorflow.org/lite/performance/gpu)

#### 人像分割
- [人像分割範例](https://www.tensorflow.org/lite/examples/segmentation/overview)
- [MediaPipe Selfie Segmentation](https://google.github.io/mediapipe/solutions/selfie_segmentation.html)
- [DeepLab 模型](https://github.com/tensorflow/models/tree/master/research/deeplab)

#### 模型訓練
- [TensorFlow 官方教學](https://www.tensorflow.org/tutorials)
- [Keras 官方文檔](https://keras.io/)
- [PyImageSearch (實戰教學)](https://pyimagesearch.com/)

### 8.4 Tone Mapping 與色彩科學

#### 學術論文
- [Reinhard Tone Mapping](http://www.cmap.polytechnique.fr/~peyre/cours/x2005signal/hdr_photographic.pdf)
- [Filmic Tone Mapping](http://filmicworlds.com/blog/filmic-tonemapping-operators/)
- [ACES (Academy Color Encoding System)](https://github.com/ampas/aces-dev)

#### 實作參考
- [HDR Tone Mapping in Unity](https://catlikecoding.com/unity/tutorials/custom-srp/hdr/)
- [Tone Mapping Algorithms Comparison](https://bruop.github.io/tonemapping/)

### 8.5 效能優化

#### Android Profiler
- [Android Profiler 指南](https://developer.android.com/studio/profile/android-profiler)
- [Memory Profiler](https://developer.android.com/studio/profile/memory-profiler)
- [GPU Inspector](https://developer.android.com/agi)

#### 效能最佳實踐
- [Android 效能最佳實踐](https://developer.android.com/topic/performance)
- [Kotlin 效能技巧](https://kotlinlang.org/docs/performance.html)
- [Jetpack Compose 效能](https://developer.android.com/jetpack/compose/performance)

### 8.6 推薦書籍

| 書名 | 適用階段 | 難度 |
|-----|---------|------|
| *Real-Time Rendering (4th Edition)* | V3.0-V4.0 | 高 |
| *OpenGL ES 3.0 Programming Guide* | V3.0 | 中 |
| *Hands-On Machine Learning (3rd Edition)* | V4.0 | 中 |
| *Android Development with Kotlin* | 全階段 | 低-中 |
| *GPU Gems 3* (免費) | V3.0 | 高 |

---

## 9. 開發檢查清單

### 9.1 專案啟動檢查

#### 環境準備
- [ ] Android Studio 更新至 Ladybug 2024.2.1+
- [ ] JDK 17 安裝並配置
- [ ] Git 配置完成 (user.name, user.email)
- [ ] 至少 1 台實體測試手機已連接
- [ ] ADB 測試成功 (`adb devices`)

#### 專案設定
- [ ] Clone 專案並成功開啟
- [ ] Gradle Sync 成功
- [ ] 專案可成功編譯並運行
- [ ] 權限測試通過 (相機、媒體存取)
- [ ] 閱讀現有程式碼 (CameraRepositoryImpl, CameraViewModel)

#### 團隊準備
- [ ] Git 分支策略確立
- [ ] Code Review 流程建立
- [ ] 溝通管道建立 (Slack/Discord)
- [ ] 開發規範共識 (CLAUDE.md)
- [ ] 技術文檔共享位置確定

### 9.2 每階段開始檢查

#### V1.0 啟動
- [ ] 建立 `feature/v1.0-ml-kit-integration` 分支
- [ ] 閱讀 ML Kit 官方文檔
- [ ] 閱讀 CameraX ImageAnalysis 文檔
- [ ] 規劃目錄結構 (`data/source/ml/`, `domain/usecase/`)
- [ ] 設定開發里程碑 (Week 1-8)

#### V2.0 啟動
- [ ] 完成 V1.0 所有功能
- [ ] V1.0 效能測試通過
- [ ] 建立 `feature/v2.0-lighting-analysis` 分支
- [ ] OpenCV 整合決策 (使用或不使用)
- [ ] 研究直方圖演算法

#### V3.0 啟動
- [ ] 完成 V2.0 所有功能
- [ ] 學習 OpenGL ES 基礎 (如不熟悉)
- [ ] 建立 `feature/v3.0-gles-rendering` 分支
- [ ] 研究 Tone Mapping 演算法
- [ ] 準備 Shader 測試環境

#### V4.0 啟動
- [ ] 完成 V3.0 所有功能
- [ ] 決定 TFLite 模型來源 (自訓練 or 預訓練)
- [ ] 建立 `feature/v4.0-tflite-segmentation` 分支
- [ ] 準備 AI 訓練環境 (如需自訓練)
- [ ] 下載 MediaPipe 模型 (如使用預訓練)

### 9.3 每階段結束檢查

#### 功能完整性
- [ ] 所有計畫功能已實作
- [ ] 所有 TODO 已解決或記錄
- [ ] 無已知 Critical Bug
- [ ] 錯誤處理完善 (try-catch, null check)

#### 測試
- [ ] 單元測試覆蓋率 > 70%
- [ ] UI 測試通過 (Espresso)
- [ ] 手動測試通過 (測試用例表)
- [ ] 迴歸測試通過 (舊功能未受影響)

#### 效能
- [ ] 效能測試完成 (高/中/低階設備)
- [ ] FPS 達標 (高階 60, 中階 30)
- [ ] 記憶體使用在目標範圍內
- [ ] 電池消耗測試完成
- [ ] 無記憶體洩漏 (LeakCanary 檢查)

#### 文檔
- [ ] 技術文檔更新 (架構、API)
- [ ] 使用者指南更新 (如適用)
- [ ] CHANGELOG.md 更新
- [ ] README.md 更新 (如有新功能)

#### 版本控制
- [ ] 所有變更已 commit
- [ ] Commit 訊息符合規範
- [ ] 版本號正確更新
- [ ] Tag 建立 (例: `v1.0.0`)
- [ ] 合併到 develop 分支

#### 發布準備
- [ ] 建立 Release Notes
- [ ] 更新 Google Play 商店描述
- [ ] 準備宣傳素材 (截圖、影片)
- [ ] Beta 測試計畫制定

### 9.4 程式碼品質檢查

#### Kotlin 風格
- [ ] 遵循 Kotlin Coding Conventions
- [ ] 使用 ktlint 自動格式化
- [ ] 避免使用 `!!` (非空斷言)
- [ ] 適當使用 Coroutines (不阻塞主執行緒)

#### Android 最佳實踐
- [ ] ViewModel 不持有 Context
- [ ] LiveData / StateFlow 正確使用
- [ ] Lifecycle 正確處理
- [ ] 資源正確釋放 (onDestroy, onCleared)

#### Clean Architecture
- [ ] Domain 層不依賴 Android 框架
- [ ] Repository 模式正確實作
- [ ] Use Case 單一職責
- [ ] 依賴方向正確 (Presentation → Domain ← Data)

#### 安全性
- [ ] 無硬編碼敏感資訊 (API Key, Password)
- [ ] 使用 ProGuard / R8 混淆
- [ ] HTTPS 強制使用 (如有網路請求)
- [ ] 相機/媒體權限正確請求

---

## 10. 附錄

### 10.1 專案時程甘特圖

```
月份    1   2   3   4   5   6   7   8   9   10  11  12
V1.0   [████████]
       ML Kit   UI Overlay

V2.0           [████████]
               Composition  Lighting

V3.0                   [████████████]
                       GLES  Tone Mapping  Optimize

V4.0                               [████████████████]
                                   TFLite  Smart Fill  Test
```

### 10.2 關鍵績效指標 (KPI)

#### 技術 KPI
- **V1.0**: ML Kit 偵測延遲 < 100ms (中階機)
- **V2.0**: 構圖評分準確率 > 80% (與專業攝影師評分對比)
- **V3.0**: GLES 渲染維持 30 fps (中階機)
- **V4.0**: TFLite 推理時間 < 50ms (中階機)

#### 使用者 KPI
- 照片品質提升: 使用 AI 建議後，照片評分提升 > 20%
- 使用率: 70% 使用者保持 AI 功能開啟
- 滿意度: NPS (淨推薦值) > 50
- 留存率: 7 日留存 > 40%, 30 日留存 > 20%

### 10.3 版本發布計畫

| 版本 | 預計完成時間 | 發布管道 | 目標使用者 |
|-----|-------------|---------|-----------|
| V1.0 Alpha | 第 2 個月 | 內部測試 | 開發團隊 |
| V1.0 Beta | 第 2.5 個月 | Google Play (Beta) | 100 位測試者 |
| V1.0 Release | 第 3 個月 | Google Play (公開) | 全部使用者 |
| V2.0 Beta | 第 5 個月 | Google Play (Beta) | 500 位測試者 |
| V2.0 Release | 第 5.5 個月 | Google Play (公開) | 全部使用者 |
| V3.0 Beta | 第 8 個月 | Google Play (Beta) | 1000 位測試者 |
| V3.0 Release | 第 8.5 個月 | Google Play (公開) | 全部使用者 |
| V4.0 Beta | 第 11 個月 | Google Play (Beta) | 2000 位測試者 |
| V4.0 Release | 第 12 個月 | Google Play (公開) | 全部使用者 |

### 10.4 聯絡資訊

- **專案 GitHub**: (待建立)
- **技術文檔**: `chat_logs/` 目錄
- **問題追蹤**: GitHub Issues
- **開發討論**: (團隊溝通管道)

---

## 文檔元資訊

- **文檔版本**: 1.0
- **建立日期**: 2025-11-12
- **最後更新**: 2025-11-12
- **作者**: Claude (Anthropic) + SmartCamera Team
- **基於文檔**:
  - `AI即時攝影師_專項開發計畫.md`
  - `CLAUDE.md` (開發規範)
- **文檔狀態**: 完成 ✅
- **下次更新**: V1.0 啟動後更新實際進度

---

**本藍圖為活文檔 (Living Document)，應隨專案進展持續更新。**
