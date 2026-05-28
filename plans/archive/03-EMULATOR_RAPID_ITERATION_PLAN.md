# SmartCameraApp - 模擬器快速迭代開發計畫
## V1.0 & V2.0 階段實施方案

---

## 📋 文件資訊

**文件名稱**: EMULATOR_RAPID_ITERATION_PLAN.md  
**版本**: 1.0  
**建立日期**: 2025-12-14  
**適用階段**: V1.0（基礎相機 + 三分法）、V2.0（進階構圖法）  
**開發工具**: VSCode + Android Studio Emulator  
**目標**: 最大化模擬器開發效率，減少實機依賴

---

## 目錄

- [一、計畫概述](#一計畫概述)
- [二、開發環境配置](#二開發環境配置)
- [三、V1.0 階段實施計畫](#三v10-階段實施計畫)
- [四、V2.0 階段實施計畫](#四v20-階段實施計畫)
- [五、測試資產管理](#五測試資產管理)
- [六、模擬器測試框架](#六模擬器測試框架)
- [七、開發工作流](#七開發工作流)
- [八、驗測清單](#八驗測清單)
- [九、常見問題與解決方案](#九常見問題與解決方案)

---

## 一、計畫概述

### 1.1 目標

建立一套完整的模擬器快速迭代開發流程，使開發者能夠在 VSCode 中：
- ✅ 快速開發和驗證 UI/UX 功能
- ✅ 測試業務邏輯和構圖算法
- ✅ 進行單元測試和整合測試
- ✅ 使用靜態測試圖片驗證 ML Kit 整合
- ⚠️ 初步驗證 CameraX 架構（非性能）
- ❌ 將實機測試限縮至關鍵性能驗證

### 1.2 開發原則

```
模擬器優先原則（Emulator-First Principle）:
├─ 80% 功能在模擬器完成開發和初測
├─ 15% 功能需模擬器+實機交叉驗證
└─ 5% 功能必須實機測試（相機性能、GPU 性能）
```

### 1.3 適用範圍

| 階段 | 模擬器適用度 | 主要驗測內容 |
|------|------------|-------------|
| **V1.0** | 85% | UI、構圖邏輯、ViewModel、ML Kit 架構 |
| **V2.0** | 75% | 進階 UI、算法邏輯、光影分析算法 |
| **V3.0** | 40% | Shader 編譯（性能不準） |
| **V4.0** | 30% | TFLite 載入（性能不準） |

### 1.4 不適用場景

**以下功能必須在實機測試，不在本計畫範圍內：**
- ❌ 相機實時預覽流暢度（FPS）
- ❌ 對焦、曝光、縮放實際效果
- ❌ GPU Shader 實時渲染性能
- ❌ TFLite 推論實時性能
- ❌ 電池消耗和熱管理
- ❌ 多設備兼容性測試

---

## 二、開發環境配置

### 2.1 VSCode 擴展安裝

```bash
# 必要擴展
code --install-extension vscjava.vscode-java-pack
code --install-extension vscjava.vscode-gradle
code --install-extension mathiasfrohlich.kotlin
code --install-extension usernamehw.errorlens

# 推薦擴展
code --install-extension eamodio.gitlens
code --install-extension Gruntfuggly.todo-tree
code --install-extension streetsidesoftware.code-spell-checker
```

### 2.2 Android Studio Emulator 配置

#### 建議的模擬器規格

```
名稱: SmartCamera_DevEmulator
設備: Pixel 6 (1080 x 2400)
API Level: 34 (Android 14)
RAM: 4GB
內部儲存: 8GB
Graphics: Hardware - GLES 3.0
```

#### 建立模擬器（命令列）

```bash
# 1. 安裝系統映像檔
sdkmanager "system-images;android-34;google_apis;x86_64"

# 2. 建立 AVD
avdmanager create avd \
  -n SmartCamera_DevEmulator \
  -k "system-images;android-34;google_apis;x86_64" \
  -d "pixel_6"

# 3. 啟動模擬器
emulator -avd SmartCamera_DevEmulator -gpu host
```

#### 模擬器性能優化

在 `~/.android/avd/SmartCamera_DevEmulator.avd/config.ini` 中添加：

```ini
# 性能優化
hw.keyboard=yes
hw.ramSize=4096
hw.gpu.enabled=yes
hw.gpu.mode=host
showDeviceFrame=no
fastboot.forceFastBoot=yes

# 相機支持
hw.camera.back=emulated
hw.camera.front=emulated
```

### 2.3 專案 Gradle 配置

#### build.gradle.kts (Project level)

```kotlin
// Top-level build file
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

#### build.gradle.kts (App level)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "local.mylab.smartcameraapp1"
    compileSdk = 34

    defaultConfig {
        applicationId = "local.mylab.smartcameraapp1"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            
            // 模擬器專用構建標記
            buildConfigField("Boolean", "EMULATOR_MODE", "true")
            buildConfigField("Boolean", "USE_TEST_IMAGES", "true")
            buildConfigField("Boolean", "ENABLE_PERFORMANCE_LOGGING", "false")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("Boolean", "EMULATOR_MODE", "false")
            buildConfigField("Boolean", "USE_TEST_IMAGES", "false")
            buildConfigField("Boolean", "ENABLE_PERFORMANCE_LOGGING", "true")
        }
    }
    
    // 產品風味：區分模擬器和實機
    flavorDimensions += "target"
    productFlavors {
        create("emulator") {
            dimension = "target"
            applicationIdSuffix = ".emulator"
            versionNameSuffix = "-emulator"
            
            buildConfigField("Boolean", "IS_EMULATOR_BUILD", "true")
            buildConfigField("Boolean", "MOCK_CAMERA_DATA", "true")
        }
        
        create("device") {
            dimension = "target"
            buildConfigField("Boolean", "IS_EMULATOR_BUILD", "false")
            buildConfigField("Boolean", "MOCK_CAMERA_DATA", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Kotlin
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // CameraX
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    
    // ML Kit
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation("com.google.mlkit:object-detection:17.0.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Accompanist (Permissions)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Timber (Logging)
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.5")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### 2.4 VSCode Tasks 配置

建立 `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "🚀 Build Emulator Debug",
      "type": "shell",
      "command": "./gradlew",
      "args": ["assembleEmulatorDebug"],
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      },
      "problemMatcher": []
    },
    {
      "label": "📱 Install to Emulator",
      "type": "shell",
      "command": "./gradlew",
      "args": ["installEmulatorDebug"],
      "group": "build",
      "dependsOn": ["🚀 Build Emulator Debug"],
      "presentation": {
        "reveal": "always"
      }
    },
    {
      "label": "🧪 Run Unit Tests",
      "type": "shell",
      "command": "./gradlew",
      "args": ["testEmulatorDebugUnitTest"],
      "group": "test",
      "presentation": {
        "reveal": "always"
      }
    },
    {
      "label": "🔍 Run Lint",
      "type": "shell",
      "command": "./gradlew",
      "args": ["lintEmulatorDebug"],
      "group": "test"
    },
    {
      "label": "🧹 Clean Build",
      "type": "shell",
      "command": "./gradlew",
      "args": ["clean"],
      "group": "build"
    },
    {
      "label": "📊 Generate Test Report",
      "type": "shell",
      "command": "./gradlew",
      "args": ["testEmulatorDebugUnitTest", "--tests", "*"],
      "group": "test",
      "presentation": {
        "reveal": "always"
      }
    },
    {
      "label": "🎯 Full Test Cycle",
      "dependsOrder": "sequence",
      "dependsOn": [
        "🧹 Clean Build",
        "🚀 Build Emulator Debug",
        "🧪 Run Unit Tests",
        "🔍 Run Lint"
      ],
      "group": "test"
    }
  ]
}
```

---

## 三、V1.0 階段實施計畫

### 3.1 V1.0 功能清單與模擬器適用性

| 功能模組 | 子功能 | 模擬器適用 | 優先級 | 預估時間 |
|---------|-------|-----------|-------|---------|
| **相機核心** |
| CameraX 整合 | UseCase 架構 | ✅ 架構可驗 | P0 | 2 天 |
| | Preview 綁定 | ⚠️ 部分可驗 | P0 | 1 天 |
| | ImageAnalysis 管線 | ✅ 完全可驗 | P0 | 2 天 |
| | 拍照功能 | ❌ 需實機 | P1 | 1 天 |
| **權限管理** |
| 權限請求流程 | UI 流程 | ✅ 完全可驗 | P0 | 1 天 |
| | 權限狀態管理 | ✅ 完全可驗 | P0 | 1 天 |
| **UI/Overlay** |
| 三分法格線 | 繪製邏輯 | ✅ 完全可驗 | P0 | 1 天 |
| 水平儀 | UI 和計算 | ✅ 完全可驗 | P0 | 1 天 |
| 主體框 Overlay | 繪製邏輯 | ✅ 完全可驗 | P0 | 1 天 |
| 提示氣泡 | 動畫和邏輯 | ✅ 完全可驗 | P0 | 1 天 |
| **ML Kit 整合** |
| 人臉偵測 | API 整合 | ✅ 靜態圖可驗 | P0 | 2 天 |
| | Bounding Box | ✅ 完全可驗 | P0 | 1 天 |
| | 實時性能 | ❌ 需實機 | P1 | - |
| **構圖引擎** |
| 三分法判斷 | 算法邏輯 | ✅ 完全可驗 | P0 | 2 天 |
| 頭部空間計算 | 算法邏輯 | ✅ 完全可驗 | P0 | 1 天 |
| 主體置中判斷 | 算法邏輯 | ✅ 完全可驗 | P0 | 1 天 |
| 構圖建議生成 | 業務邏輯 | ✅ 完全可驗 | P0 | 2 天 |

**總計**: 
- 可在模擬器完全驗測: 15 項（83%）
- 需實機測試: 3 項（17%）
- 預估總時間: 20 天

### 3.2 V1.0 專案結構

```
app/src/main/java/local/mylab/smartcameraapp1/
│
├── domain/                           # 業務邏輯層
│   ├── model/
│   │   ├── CompositionSuggestion.kt  # 構圖建議模型
│   │   ├── FaceInfo.kt               # 人臉資訊模型
│   │   ├── CompositionRule.kt        # 構圖規則
│   │   └── CameraState.kt            # 相機狀態
│   │
│   ├── usecase/
│   │   ├── AnalyzeCompositionUseCase.kt      # 構圖分析
│   │   ├── DetectFacesUseCase.kt             # 人臉偵測
│   │   ├── CalculateRuleOfThirdsUseCase.kt   # 三分法計算
│   │   └── GenerateSuggestionUseCase.kt      # 建議生成
│   │
│   └── repository/
│       ├── CameraRepository.kt               # 相機資料介面
│       └── MLKitRepository.kt                # ML Kit 介面
│
├── data/                             # 資料層
│   ├── repository/
│   │   ├── CameraRepositoryImpl.kt
│   │   └── MLKitRepositoryImpl.kt
│   │
│   ├── source/
│   │   ├── camera/
│   │   │   ├── CameraController.kt           # CameraX 控制器
│   │   │   ├── CameraDataSource.kt           # 相機資料源
│   │   │   └── FakeCameraDataSource.kt       # 模擬器用假資料源 ⭐
│   │   │
│   │   ├── ml/
│   │   │   ├── MLKitFaceDetector.kt          # ML Kit 偵測器
│   │   │   └── FakeMLKitDetector.kt          # 模擬器用假偵測器 ⭐
│   │   │
│   │   └── analysis/
│   │       ├── CompositionAnalyzer.kt        # 構圖分析器
│   │       ├── RuleOfThirdsAnalyzer.kt       # 三分法分析
│   │       └── HeadroomAnalyzer.kt           # 頭部空間分析
│   │
│   └── local/
│       ├── TestImageProvider.kt              # 測試圖片提供者 ⭐
│       └── PreferencesManager.kt             # 偏好設定管理
│
├── presentation/                     # 表現層
│   ├── camera/
│   │   ├── CameraScreen.kt                   # 相機畫面
│   │   ├── CameraViewModel.kt                # 相機 ViewModel
│   │   └── CameraState.kt                    # UI 狀態
│   │
│   ├── components/
│   │   ├── overlay/
│   │   │   ├── GridOverlay.kt                # 格線覆蓋層
│   │   │   ├── LevelIndicator.kt             # 水平儀
│   │   │   ├── FaceBoxOverlay.kt             # 人臉框
│   │   │   └── SuggestionBubble.kt           # 建議氣泡
│   │   │
│   │   └── CameraPreview.kt                  # 預覽元件
│   │
│   └── permissions/
│       ├── PermissionScreen.kt               # 權限請求畫面
│       └── PermissionViewModel.kt            # 權限 ViewModel
│
├── di/                               # 依賴注入
│   ├── AppModule.kt
│   ├── CameraModule.kt                       # 相機模組（含模擬器切換）⭐
│   ├── MLKitModule.kt                        # ML Kit 模組
│   └── RepositoryModule.kt
│
├── util/                             # 工具類
│   ├── Constants.kt
│   ├── Extensions.kt
│   └── Logger.kt
│
└── MainActivity.kt
```

**標記 ⭐ 的是專為模擬器測試設計的檔案**

### 3.3 V1.0 核心實現（模擬器版本）

#### 3.3.1 測試圖片提供者

```kotlin
// data/local/TestImageProvider.kt
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
            null
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
            null
        }
    }
    
    /**
     * 取得所有可用的測試圖片名稱
     */
    fun getAvailableTestImages(): List<String> = testImages
}
```

#### 3.3.2 假相機資料源（模擬器專用）

```kotlin
// data/source/camera/FakeCameraDataSource.kt
package local.mylab.smartcameraapp1.data.source.camera

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import local.mylab.smartcameraapp1.data.local.TestImageProvider
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
                Timber.d("FakeCameraDataSource: Emitted test image")
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
```

#### 3.3.3 真實相機資料源（實機專用）

```kotlin
// data/source/camera/RealCameraDataSource.kt
package local.mylab.smartcameraapp1.data.source.camera

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 實機專用的真實相機資料源
 * 使用 CameraX 進行實際相機操作
 */
class RealCameraDataSource @Inject constructor(
    // CameraX 相關依賴會在這裡注入
) : CameraDataSource {
    
    private val previewFlow = MutableStateFlow<Bitmap?>(null)
    
    override fun getPreviewStream(): Flow<Bitmap> {
        Timber.d("RealCameraDataSource: Starting real preview stream")
        // TODO: 實現 CameraX preview stream
        return previewFlow as Flow<Bitmap>
    }
    
    override suspend fun takePicture(): Result<Bitmap> {
        Timber.d("RealCameraDataSource: Taking real picture")
        // TODO: 實現 CameraX 拍照
        return Result.failure(Exception("Not implemented yet"))
    }
    
    override fun stopPreview() {
        Timber.d("RealCameraDataSource: Real preview stopped")
        // TODO: 停止 CameraX
    }
    
    override suspend fun setZoomRatio(ratio: Float): Result<Unit> {
        Timber.d("RealCameraDataSource: Setting zoom ratio to $ratio")
        // TODO: 實現縮放
        return Result.success(Unit)
    }
}
```

#### 3.3.4 相機資料源介面

```kotlin
// data/source/camera/CameraDataSource.kt
package local.mylab.smartcameraapp1.data.source.camera

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * 相機資料源介面
 * 可以有不同的實現（真實相機、假相機等）
 */
interface CameraDataSource {
    
    /**
     * 取得相機預覽流
     */
    fun getPreviewStream(): Flow<Bitmap>
    
    /**
     * 拍攝照片
     */
    suspend fun takePicture(): Result<Bitmap>
    
    /**
     * 停止預覽
     */
    fun stopPreview()
    
    /**
     * 設置縮放比例
     */
    suspend fun setZoomRatio(ratio: Float): Result<Unit>
}
```

#### 3.3.5 Hilt 模組配置（自動切換模擬器/實機）

```kotlin
// di/CameraModule.kt
package local.mylab.smartcameraapp1.di

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import local.mylab.smartcameraapp1.BuildConfig
import local.mylab.smartcameraapp1.data.source.camera.CameraDataSource
import local.mylab.smartcameraapp1.data.source.camera.FakeCameraDataSource
import local.mylab.smartcameraapp1.data.source.camera.RealCameraDataSource
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    
    /**
     * 自動判斷是否為模擬器，提供對應的資料源
     */
    @Provides
    @Singleton
    fun provideCameraDataSource(
        fakeCameraDataSource: FakeCameraDataSource,
        realCameraDataSource: RealCameraDataSource
    ): CameraDataSource {
        val isEmulator = isRunningOnEmulator()
        val useEmulatorMode = BuildConfig.EMULATOR_MODE || BuildConfig.IS_EMULATOR_BUILD
        
        return when {
            useEmulatorMode || isEmulator -> {
                Timber.d("Using FakeCameraDataSource (Emulator Mode)")
                fakeCameraDataSource
            }
            else -> {
                Timber.d("Using RealCameraDataSource (Device Mode)")
                realCameraDataSource
            }
        }
    }
    
    /**
     * 偵測是否在模擬器上運行
     */
    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }
}
```

#### 3.3.6 構圖分析 Use Case

```kotlin
// domain/usecase/AnalyzeCompositionUseCase.kt
package local.mylab.smartcameraapp1.domain.usecase

import local.mylab.smartcameraapp1.domain.model.CompositionSuggestion
import local.mylab.smartcameraapp1.domain.model.FaceInfo
import local.mylab.smartcameraapp1.domain.model.CompositionRule
import timber.log.Timber
import javax.inject.Inject

/**
 * 構圖分析 Use Case
 * 根據偵測到的人臉資訊分析構圖並提供建議
 */
class AnalyzeCompositionUseCase @Inject constructor() {
    
    /**
     * 分析構圖
     * @param faces 偵測到的人臉列表
     * @param imageWidth 圖片寬度
     * @param imageHeight 圖片高度
     * @return 構圖建議列表
     */
    operator fun invoke(
        faces: List<FaceInfo>,
        imageWidth: Int,
        imageHeight: Int
    ): List<CompositionSuggestion> {
        
        if (faces.isEmpty()) {
            return listOf(
                CompositionSuggestion(
                    rule = CompositionRule.NO_SUBJECT,
                    message = "未偵測到主體，請將主體放入畫面",
                    severity = CompositionSuggestion.Severity.WARNING
                )
            )
        }
        
        val suggestions = mutableListOf<CompositionSuggestion>()
        
        // 使用最大的人臉（假設為主體）
        val mainFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            ?: return suggestions
        
        // 1. 檢查三分法
        suggestions.addAll(checkRuleOfThirds(mainFace, imageWidth, imageHeight))
        
        // 2. 檢查頭部空間
        suggestions.addAll(checkHeadroom(mainFace, imageHeight))
        
        // 3. 檢查置中
        suggestions.addAll(checkCentering(mainFace, imageWidth, imageHeight))
        
        Timber.d("Generated ${suggestions.size} composition suggestions")
        return suggestions
    }
    
    /**
     * 檢查三分法
     */
    private fun checkRuleOfThirds(
        face: FaceInfo,
        imageWidth: Int,
        imageHeight: Int
    ): List<CompositionSuggestion> {
        val suggestions = mutableListOf<CompositionSuggestion>()
        
        // 計算人臉中心點
        val faceCenterY = face.boundingBox.centerY()
        
        // 上三分線（理想眼睛位置）
        val upperThirdLine = imageHeight / 3f
        val threshold = imageHeight * 0.1f // 10% 容差
        
        val distanceFromUpperThird = kotlin.math.abs(faceCenterY - upperThirdLine)
        
        when {
            faceCenterY < upperThirdLine - threshold -> {
                suggestions.add(
                    CompositionSuggestion(
                        rule = CompositionRule.RULE_OF_THIRDS,
                        message = "請將相機向下移動，使人臉接近上三分線",
                        severity = CompositionSuggestion.Severity.INFO
                    )
                )
            }
            faceCenterY > upperThirdLine + threshold -> {
                suggestions.add(
                    CompositionSuggestion(
                        rule = CompositionRule.RULE_OF_THIRDS,
                        message = "請將相機向上移動，使人臉接近上三分線",
                        severity = CompositionSuggestion.Severity.INFO
                    )
                )
            }
            else -> {
                // 符合三分法
                Timber.d("Face position matches rule of thirds")
            }
        }
        
        return suggestions
    }
    
    /**
     * 檢查頭部空間
     */
    private fun checkHeadroom(
        face: FaceInfo,
        imageHeight: Int
    ): List<CompositionSuggestion> {
        val suggestions = mutableListOf<CompositionSuggestion>()
        
        val headroomRatio = face.boundingBox.top.toFloat() / imageHeight
        
        when {
            headroomRatio < 0.05f -> {
                suggestions.add(
                    CompositionSuggestion(
                        rule = CompositionRule.HEADROOM,
                        message = "頭部空間過少，請向下移動相機",
                        severity = CompositionSuggestion.Severity.WARNING
                    )
                )
            }
            headroomRatio > 0.25f -> {
                suggestions.add(
                    CompositionSuggestion(
                        rule = CompositionRule.HEADROOM,
                        message = "頭部空間過多，請向上移動相機",
                        severity = CompositionSuggestion.Severity.INFO
                    )
                )
            }
            else -> {
                Timber.d("Headroom is appropriate")
            }
        }
        
        return suggestions
    }
    
    /**
     * 檢查置中
     */
    private fun checkCentering(
        face: FaceInfo,
        imageWidth: Int,
        imageHeight: Int
    ): List<CompositionSuggestion> {
        val suggestions = mutableListOf<CompositionSuggestion>()
        
        val faceCenterX = face.boundingBox.centerX()
        val imageCenterX = imageWidth / 2f
        val threshold = imageWidth * 0.15f // 15% 容差
        
        val distanceFromCenter = kotlin.math.abs(faceCenterX - imageCenterX)
        
        if (distanceFromCenter > threshold) {
            val direction = if (faceCenterX < imageCenterX) "右" else "左"
            suggestions.add(
                CompositionSuggestion(
                    rule = CompositionRule.CENTERING,
                    message = "主體偏離中心，請向${direction}移動相機",
                    severity = CompositionSuggestion.Severity.INFO
                )
            )
        }
        
        return suggestions
    }
}
```

#### 3.3.7 ViewModel 實現

```kotlin
// presentation/camera/CameraViewModel.kt
package local.mylab.smartcameraapp1.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import local.mylab.smartcameraapp1.domain.model.CompositionSuggestion
import local.mylab.smartcameraapp1.domain.model.FaceInfo
import local.mylab.smartcameraapp1.domain.usecase.AnalyzeCompositionUseCase
import local.mylab.smartcameraapp1.domain.usecase.DetectFacesUseCase
import local.mylab.smartcameraapp1.data.repository.CameraRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val detectFacesUseCase: DetectFacesUseCase,
    private val analyzeCompositionUseCase: AnalyzeCompositionUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(CameraUiState())
    val state: StateFlow<CameraUiState> = _state.asStateFlow()
    
    init {
        Timber.d("CameraViewModel initialized")
        startPreview()
    }
    
    /**
     * 開始預覽
     */
    fun startPreview() {
        viewModelScope.launch {
            Timber.d("Starting camera preview")
            
            cameraRepository.getPreviewStream()
                .collect { bitmap ->
                    Timber.d("Received frame: ${bitmap.width}x${bitmap.height}")
                    
                    // 偵測人臉
                    val faces = detectFacesUseCase(bitmap)
                    Timber.d("Detected ${faces.size} faces")
                    
                    // 分析構圖
                    val suggestions = analyzeCompositionUseCase(
                        faces = faces,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height
                    )
                    
                    // 更新 UI 狀態
                    _state.update {
                        it.copy(
                            currentFrame = bitmap,
                            detectedFaces = faces,
                            compositionSuggestions = suggestions,
                            isAnalyzing = true
                        )
                    }
                }
        }
    }
    
    /**
     * 停止預覽
     */
    fun stopPreview() {
        viewModelScope.launch {
            Timber.d("Stopping camera preview")
            cameraRepository.stopPreview()
            _state.update { it.copy(isAnalyzing = false) }
        }
    }
    
    /**
     * 拍照
     */
    fun takePicture() {
        viewModelScope.launch {
            Timber.d("Taking picture")
            _state.update { it.copy(isCapturing = true) }
            
            cameraRepository.takePicture()
                .onSuccess { bitmap ->
                    Timber.d("Picture taken successfully")
                    _state.update {
                        it.copy(
                            capturedImage = bitmap,
                            isCapturing = false
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to take picture")
                    _state.update { it.copy(isCapturing = false) }
                }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPreview()
        Timber.d("CameraViewModel cleared")
    }
}

/**
 * Camera UI State
 */
data class CameraUiState(
    val currentFrame: android.graphics.Bitmap? = null,
    val detectedFaces: List<FaceInfo> = emptyList(),
    val compositionSuggestions: List<CompositionSuggestion> = emptyList(),
    val isAnalyzing: Boolean = false,
    val isCapturing: Boolean = false,
    val capturedImage: android.graphics.Bitmap? = null
)
```

### 3.4 V1.0 單元測試範例

#### 3.4.1 ViewModel 測試

```kotlin
// presentation/camera/CameraViewModelTest.kt
package local.mylab.smartcameraapp1.presentation.camera

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import local.mylab.smartcameraapp1.data.repository.CameraRepository
import local.mylab.smartcameraapp1.domain.usecase.AnalyzeCompositionUseCase
import local.mylab.smartcameraapp1.domain.usecase.DetectFacesUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    
    private lateinit var viewModel: CameraViewModel
    private lateinit var cameraRepository: CameraRepository
    private lateinit var detectFacesUseCase: DetectFacesUseCase
    private lateinit var analyzeCompositionUseCase: AnalyzeCompositionUseCase
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        cameraRepository = mockk()
        detectFacesUseCase = mockk()
        analyzeCompositionUseCase = mockk()
        
        // 預設 Mock 行為
        every { cameraRepository.getPreviewStream() } returns flowOf()
        coEvery { detectFacesUseCase(any()) } returns emptyList()
        coEvery { analyzeCompositionUseCase(any(), any(), any()) } returns emptyList()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `初始化時狀態應為預設值`() {
        // Given & When
        viewModel = CameraViewModel(
            cameraRepository,
            detectFacesUseCase,
            analyzeCompositionUseCase
        )
        
        // Then
        val state = viewModel.state.value
        assertThat(state.currentFrame).isNull()
        assertThat(state.detectedFaces).isEmpty()
        assertThat(state.compositionSuggestions).isEmpty()
        assertThat(state.isAnalyzing).isFalse()
        assertThat(state.isCapturing).isFalse()
    }
    
    @Test
    fun `startPreview 應該開始分析幀`() = runTest {
        // Given
        val mockBitmap = mockk<android.graphics.Bitmap>(relaxed = true)
        every { mockBitmap.width } returns 1920
        every { mockBitmap.height } returns 1080
        
        every { cameraRepository.getPreviewStream() } returns flowOf(mockBitmap)
        
        viewModel = CameraViewModel(
            cameraRepository,
            detectFacesUseCase,
            analyzeCompositionUseCase
        )
        
        // When & Then
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.isAnalyzing).isFalse()
            
            advanceUntilIdle()
            
            val analyzingState = expectMostRecentItem()
            assertThat(analyzingState.isAnalyzing).isTrue()
            assertThat(analyzingState.currentFrame).isNotNull()
        }
    }
    
    @Test
    fun `takePicture 成功時應更新 capturedImage`() = runTest {
        // Given
        val mockBitmap = mockk<android.graphics.Bitmap>()
        coEvery { cameraRepository.takePicture() } returns Result.success(mockBitmap)
        
        viewModel = CameraViewModel(
            cameraRepository,
            detectFacesUseCase,
            analyzeCompositionUseCase
        )
        
        // When
        viewModel.takePicture()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.state.value
        assertThat(state.capturedImage).isEqualTo(mockBitmap)
        assertThat(state.isCapturing).isFalse()
    }
}
```

#### 3.4.2 構圖分析測試

```kotlin
// domain/usecase/AnalyzeCompositionUseCaseTest.kt
package local.mylab.smartcameraapp1.domain.usecase

import android.graphics.Rect
import com.google.common.truth.Truth.assertThat
import local.mylab.smartcameraapp1.domain.model.CompositionRule
import local.mylab.smartcameraapp1.domain.model.FaceInfo
import org.junit.Before
import org.junit.Test

class AnalyzeCompositionUseCaseTest {
    
    private lateinit var useCase: AnalyzeCompositionUseCase
    
    @Before
    fun setup() {
        useCase = AnalyzeCompositionUseCase()
    }
    
    @Test
    fun `沒有人臉時應提示無主體`() {
        // Given
        val faces = emptyList<FaceInfo>()
        val width = 1920
        val height = 1080
        
        // When
        val suggestions = useCase(faces, width, height)
        
        // Then
        assertThat(suggestions).hasSize(1)
        assertThat(suggestions[0].rule).isEqualTo(CompositionRule.NO_SUBJECT)
    }
    
    @Test
    fun `人臉在上三分線時不應有三分法建議`() {
        // Given
        val height = 1080
        val upperThird = height / 3  // 360
        
        val face = FaceInfo(
            boundingBox = Rect(
                400,    // left
                330,    // top (接近 360)
                600,    // right
                530     // bottom
            ),
            confidence = 0.9f
        )
        
        // When
        val suggestions = useCase(listOf(face), 1920, height)
        
        // Then
        val ruleOfThirdsSuggestions = suggestions.filter {
            it.rule == CompositionRule.RULE_OF_THIRDS
        }
        assertThat(ruleOfThirdsSuggestions).isEmpty()
    }
    
    @Test
    fun `人臉過低時應建議向上移動`() {
        // Given
        val height = 1080
        
        val face = FaceInfo(
            boundingBox = Rect(
                400,    // left
                700,    // top (遠低於上三分線 360)
                600,    // right
                900     // bottom
            ),
            confidence = 0.9f
        )
        
        // When
        val suggestions = useCase(listOf(face), 1920, height)
        
        // Then
        val ruleOfThirdsSuggestions = suggestions.filter {
            it.rule == CompositionRule.RULE_OF_THIRDS
        }
        assertThat(ruleOfThirdsSuggestions).isNotEmpty()
        assertThat(ruleOfThirdsSuggestions[0].message).contains("向上移動")
    }
    
    @Test
    fun `頭部空間過少時應提示`() {
        // Given
        val height = 1080
        
        val face = FaceInfo(
            boundingBox = Rect(
                400,    // left
                20,     // top (僅 20px，比例 < 5%)
                600,    // right
                220     // bottom
            ),
            confidence = 0.9f
        )
        
        // When
        val suggestions = useCase(listOf(face), 1920, height)
        
        // Then
        val headroomSuggestions = suggestions.filter {
            it.rule == CompositionRule.HEADROOM
        }
        assertThat(headroomSuggestions).isNotEmpty()
        assertThat(headroomSuggestions[0].message).contains("頭部空間過少")
    }
    
    @Test
    fun `人臉偏左時應建議向右移動`() {
        // Given
        val width = 1920
        val height = 1080
        
        val face = FaceInfo(
            boundingBox = Rect(
                200,    // left (中心點 300，遠小於畫面中心 960)
                300,    // top
                400,    // right
                500     // bottom
            ),
            confidence = 0.9f
        )
        
        // When
        val suggestions = useCase(listOf(face), width, height)
        
        // Then
        val centeringSuggestions = suggestions.filter {
            it.rule == CompositionRule.CENTERING
        }
        assertThat(centeringSuggestions).isNotEmpty()
        assertThat(centeringSuggestions[0].message).contains("向右移動")
    }
}
```

### 3.5 V1.0 測試檢查清單

```markdown
## V1.0 模擬器測試檢查清單

### UI 測試
- [ ] 三分法格線正確顯示
- [ ] 格線位置準確（上下左右各 1/3）
- [ ] 水平儀顯示正常
- [ ] 水平儀在不同角度正確反應
- [ ] 人臉框正確繪製
- [ ] 多人臉時所有框都顯示
- [ ] 建議氣泡正確位置顯示
- [ ] 氣泡文字可讀且完整
- [ ] 動畫流暢無卡頓（30fps+）

### 業務邏輯測試
- [ ] 無人臉時顯示「無主體」提示
- [ ] 人臉在正確位置時無建議
- [ ] 人臉過高時提示向下移動
- [ ] 人臉過低時提示向上移動
- [ ] 人臉偏左時提示向右移動
- [ ] 人臉偏右時提示向左移動
- [ ] 頭部空間過少時提示
- [ ] 頭部空間過多時提示
- [ ] 多個建議按優先級排序

### ViewModel 測試
- [ ] 初始狀態正確
- [ ] startPreview 正確啟動
- [ ] stopPreview 正確停止
- [ ] takePicture 成功更新狀態
- [ ] 錯誤處理正確
- [ ] StateFlow 正確發射
- [ ] 清理資源（onCleared）

### Repository 測試
- [ ] 模擬器模式使用 FakeCameraDataSource
- [ ] 測試圖片正確載入
- [ ] 預覽流正確發射
- [ ] 拍照功能正常
- [ ] 錯誤情況處理

### 單元測試覆蓋率
- [ ] ViewModel: > 70%
- [ ] UseCase: > 80%
- [ ] Analyzer: > 80%
- [ ] 整體: > 70%
```

---

## 四、V2.0 階段實施計畫

### 4.1 V2.0 功能清單與模擬器適用性

| 功能模組 | 子功能 | 模擬器適用 | 優先級 | 預估時間 |
|---------|-------|-----------|-------|---------|
| **進階 Overlay** |
| 引導線 Overlay | 線條繪製 | ✅ 完全可驗 | P0 | 2 天 |
| 對稱軸 Overlay | 軸線繪製 | ✅ 完全可驗 | P0 | 1 天 |
| 框架式 Overlay | 框架標註 | ✅ 完全可驗 | P1 | 2 天 |
| HDR 警示圖示 | 圖示顯示 | ✅ 完全可驗 | P1 | 1 天 |
| **圖像分析** |
| 引導線偵測 | Hough 線偵測 | ✅ 靜態圖可驗 | P0 | 3 天 |
| 對稱性分析 | 幾何計算 | ✅ 完全可驗 | P0 | 2 天 |
| 深度構圖分析 | 前景/背景 | ✅ 靜態圖可驗 | P1 | 3 天 |
| 填滿構圖偵測 | 主體面積 | ✅ 完全可驗 | P1 | 2 天 |
| **光影分析** |
| 直方圖計算 | 算法邏輯 | ✅ 完全可驗 | P0 | 2 天 |
| 逆光偵測 | 規則判斷 | ✅ 完全可驗 | P0 | 2 天 |
| 高對比偵測 | 數值分析 | ✅ 完全可驗 | P0 | 1 天 |
| HDR 建議 | 邏輯判斷 | ✅ 完全可驗 | P1 | 1 天 |
| **場景識別** |
| 場景分類器 | 規則引擎 | ✅ 靜態圖可驗 | P1 | 3 天 |
| | ML 模型（可選） | ⚠️ 部分可驗 | P2 | 5 天 |

**總計**: 
- 可在模擬器完全驗測: 12 項（80%）
- 部分可驗測: 1 項（7%）
- 需實機測試: 2 項（13%）
- 預估總時間: 30 天

### 4.2 V2.0 新增測試圖片

需要準備更多樣化的測試圖片：

```
app/src/debug/assets/test_images/
├── composition/
│   ├── leading_lines_road.jpg          # 道路引導線
│   ├── leading_lines_railway.jpg       # 鐵軌引導線
│   ├── symmetry_architecture.jpg       # 建築對稱
│   ├── symmetry_reflection.jpg         # 水面倒影
│   ├── framing_arch.jpg                # 拱門框架
│   ├── framing_window.jpg              # 窗框框架
│   ├── fill_frame_food.jpg             # 填滿構圖（食物）
│   └── depth_layers.jpg                # 前景/中景/背景
│
├── lighting/
│   ├── backlight_severe.jpg            # 嚴重逆光
│   ├── backlight_moderate.jpg          # 中度逆光
│   ├── high_contrast.jpg               # 高對比場景
│   ├── low_light.jpg                   # 低光場景
│   ├── overexposed.jpg                 # 過曝
│   └── underexposed.jpg                # 欠曝
│
└── scenes/
    ├── portrait_indoor.jpg             # 室內人像
    ├── portrait_outdoor.jpg            # 室外人像
    ├── landscape_mountain.jpg          # 山景風景
    ├── landscape_sea.jpg               # 海景風景
    ├── architecture_modern.jpg         # 現代建築
    ├── architecture_classical.jpg      # 古典建築
    ├── street_day.jpg                  # 白天街拍
    ├── street_night.jpg                # 夜間街拍
    └── food_overhead.jpg               # 俯拍食物
```

### 4.3 V2.0 核心實現

#### 4.3.1 引導線偵測器

```kotlin
// data/source/analysis/LeadingLinesDetector.kt
package local.mylab.smartcameraapp1.data.source.analysis

import android.graphics.Bitmap
import android.graphics.Point
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * 引導線偵測器
 * 使用簡化的 Hough Transform 偵測圖像中的主要線條
 */
class LeadingLinesDetector @Inject constructor() {
    
    data class Line(
        val start: Point,
        val end: Point,
        val angle: Double,  // 角度（度）
        val strength: Float // 線條強度
    )
    
    /**
     * 偵測引導線
     */
    fun detectLines(bitmap: Bitmap): List<Line> {
        Timber.d("Detecting leading lines in ${bitmap.width}x${bitmap.height} image")
        
        // 1. 邊緣偵測（簡化版 Canny）
        val edges = detectEdges(bitmap)
        
        // 2. 線條偵測（簡化版 Hough）
        val lines = houghLineDetection(edges, bitmap.width, bitmap.height)
        
        // 3. 過濾和合併相近線條
        val filteredLines = filterAndMergeLines(lines)
        
        Timber.d("Detected ${filteredLines.size} leading lines")
        return filteredLines
    }
    
    /**
     * 簡化的邊緣偵測
     */
    private fun detectEdges(bitmap: Bitmap): Array<BooleanArray> {
        val width = bitmap.width
        val height = bitmap.height
        val edges = Array(height) { BooleanArray(width) }
        
        val threshold = 30 // 邊緣閾值
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val pixel = bitmap.getPixel(x, y)
                val pixelRight = bitmap.getPixel(x + 1, y)
                val pixelDown = bitmap.getPixel(x, y + 1)
                
                val grayValue = getGrayValue(pixel)
                val grayRight = getGrayValue(pixelRight)
                val grayDown = getGrayValue(pixelDown)
                
                val gradX = kotlin.math.abs(grayRight - grayValue)
                val gradY = kotlin.math.abs(grayDown - grayValue)
                val gradient = sqrt((gradX * gradX + gradY * gradY).toDouble()).toInt()
                
                edges[y][x] = gradient > threshold
            }
        }
        
        return edges
    }
    
    /**
     * 簡化的 Hough 線條偵測
     */
    private fun houghLineDetection(
        edges: Array<BooleanArray>,
        width: Int,
        height: Int
    ): List<Line> {
        val lines = mutableListOf<Line>()
        
        // 簡化：只偵測水平、垂直和主要對角線
        val directions = listOf(
            Pair(1, 0),   // 水平
            Pair(0, 1),   // 垂直
            Pair(1, 1),   // 右下對角
            Pair(1, -1)   // 右上對角
        )
        
        for ((dx, dy) in directions) {
            val detectedLines = detectLinesInDirection(edges, width, height, dx, dy)
            lines.addAll(detectedLines)
        }
        
        return lines
    }
    
    /**
     * 在特定方向偵測線條
     */
    private fun detectLinesInDirection(
        edges: Array<BooleanArray>,
        width: Int,
        height: Int,
        dx: Int,
        dy: Int
    ): List<Line> {
        val lines = mutableListOf<Line>()
        val visited = Array(height) { BooleanArray(width) }
        val minLineLength = minOf(width, height) / 10 // 最小線條長度
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (edges[y][x] && !visited[y][x]) {
                    val linePoints = traceLine(edges, visited, x, y, dx, dy, width, height)
                    
                    if (linePoints.size >= minLineLength) {
                        val start = linePoints.first()
                        val end = linePoints.last()
                        val angle = calculateAngle(start, end)
                        val strength = linePoints.size.toFloat() / maxOf(width, height)
                        
                        lines.add(Line(start, end, angle, strength))
                    }
                }
            }
        }
        
        return lines
    }
    
    /**
     * 追蹤線條
     */
    private fun traceLine(
        edges: Array<BooleanArray>,
        visited: Array<BooleanArray>,
        startX: Int,
        startY: Int,
        dx: Int,
        dy: Int,
        width: Int,
        height: Int
    ): List<Point> {
        val points = mutableListOf<Point>()
        var x = startX
        var y = startY
        
        while (x in 0 until width && y in 0 until height && edges[y][x] && !visited[y][x]) {
            points.add(Point(x, y))
            visited[y][x] = true
            x += dx
            y += dy
        }
        
        return points
    }
    
    /**
     * 過濾和合併相近線條
     */
    private fun filterAndMergeLines(lines: List<Line>): List<Line> {
        // 簡化：只保留強度較高的線條
        return lines
            .sortedByDescending { it.strength }
            .take(5) // 最多保留 5 條主要線條
    }
    
    private fun getGrayValue(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
    
    private fun calculateAngle(start: Point, end: Point): Double {
        val dx = (end.x - start.x).toDouble()
        val dy = (end.y - start.y).toDouble()
        return atan2(dy, dx) * 180 / PI
    }
}
```

#### 4.3.2 光影分析器

```kotlin
// data/source/analysis/LightingAnalyzer.kt
package local.mylab.smartcameraapp1.data.source.analysis

import android.graphics.Bitmap
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

/**
 * 光影分析器
 * 分析圖像的光影特徵：亮度分布、對比度、逆光等
 */
class LightingAnalyzer @Inject constructor() {
    
    data class LightingInfo(
        val histogram: IntArray,           // 亮度直方圖 (0-255)
        val averageBrightness: Float,      // 平均亮度 (0-1)
        val contrast: Float,               // 對比度 (0-1)
        val isBacklit: Boolean,            // 是否逆光
        val isHighContrast: Boolean,       // 是否高對比
        val isLowLight: Boolean,           // 是否低光
        val hdrRecommended: Boolean        // 是否建議 HDR
    )
    
    /**
     * 分析光影
     */
    fun analyze(bitmap: Bitmap): LightingInfo {
        Timber.d("Analyzing lighting in ${bitmap.width}x${bitmap.height} image")
        
        // 1. 計算亮度直方圖
        val histogram = calculateHistogram(bitmap)
        
        // 2. 計算平均亮度
        val avgBrightness = calculateAverageBrightness(histogram)
        
        // 3. 計算對比度
        val contrast = calculateContrast(histogram)
        
        // 4. 偵測逆光
        val isBacklit = detectBacklight(histogram, avgBrightness)
        
        // 5. 偵測高對比
        val isHighContrast = contrast > 0.7f
        
        // 6. 偵測低光
        val isLowLight = avgBrightness < 0.3f
        
        // 7. HDR 建議
        val hdrRecommended = isHighContrast || isBacklit
        
        Timber.d("Lighting analysis: brightness=$avgBrightness, contrast=$contrast, " +
                "backlit=$isBacklit, highContrast=$isHighContrast, lowLight=$isLowLight")
        
        return LightingInfo(
            histogram = histogram,
            averageBrightness = avgBrightness,
            contrast = contrast,
            isBacklit = isBacklit,
            isHighContrast = isHighContrast,
            isLowLight = isLowLight,
            hdrRecommended = hdrRecommended
        )
    }
    
    /**
     * 計算亮度直方圖
     */
    private fun calculateHistogram(bitmap: Bitmap): IntArray {
        val histogram = IntArray(256)
        val width = bitmap.width
        val height = bitmap.height
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = getBrightness(pixel)
                histogram[brightness]++
            }
        }
        
        return histogram
    }
    
    /**
     * 計算平均亮度
     */
    private fun calculateAverageBrightness(histogram: IntArray): Float {
        var totalPixels = 0
        var weightedSum = 0L
        
        histogram.forEachIndexed { brightness, count ->
            totalPixels += count
            weightedSum += brightness * count
        }
        
        return if (totalPixels > 0) {
            (weightedSum.toFloat() / totalPixels) / 255f
        } else {
            0f
        }
    }
    
    /**
     * 計算對比度
     */
    private fun calculateContrast(histogram: IntArray): Float {
        val totalPixels = histogram.sum()
        val threshold = (totalPixels * 0.05).toInt() // 5% 閾值
        
        // 找到 5% 和 95% 位置的亮度值
        var accumulated = 0
        var darkPoint = 0
        var brightPoint = 255
        
        // 找暗部（5%）
        for (i in histogram.indices) {
            accumulated += histogram[i]
            if (accumulated >= threshold) {
                darkPoint = i
                break
            }
        }
        
        // 找亮部（95%）
        accumulated = 0
        for (i in histogram.indices.reversed()) {
            accumulated += histogram[i]
            if (accumulated >= threshold) {
                brightPoint = i
                break
            }
        }
        
        // 對比度 = (亮部 - 暗部) / 255
        return abs(brightPoint - darkPoint).toFloat() / 255f
    }
    
    /**
     * 偵測逆光
     * 規則：暗部佔比高且亮部也佔比高（兩極分化）
     */
    private fun detectBacklight(histogram: IntArray, avgBrightness: Float): Boolean {
        val totalPixels = histogram.sum()
        
        // 計算暗部（0-85）和亮部（170-255）的像素佔比
        val darkPixels = (0..85).sumOf { histogram[it] }
        val brightPixels = (170..255).sumOf { histogram[it] }
        
        val darkRatio = darkPixels.toFloat() / totalPixels
        val brightRatio = brightPixels.toFloat() / totalPixels
        
        // 逆光判定：暗部 > 30% 且亮部 > 20%
        return darkRatio > 0.3f && brightRatio > 0.2f
    }
    
    private fun getBrightness(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return ((0.299 * r + 0.587 * g + 0.114 * b).toInt()).coerceIn(0, 255)
    }
}
```

#### 4.3.3 場景分類器

```kotlin
// data/source/analysis/SceneClassifier.kt
package local.mylab.smartcameraapp1.data.source.analysis

import android.graphics.Bitmap
import local.mylab.smartcameraapp1.domain.model.FaceInfo
import local.mylab.smartcameraapp1.data.source.analysis.LeadingLinesDetector
import local.mylab.smartcameraapp1.data.source.analysis.LightingAnalyzer
import timber.log.Timber
import javax.inject.Inject

/**
 * 場景分類器
 * 根據圖像特徵判斷拍攝場景類型
 */
class SceneClassifier @Inject constructor(
    private val leadingLinesDetector: LeadingLinesDetector,
    private val lightingAnalyzer: LightingAnalyzer
) {
    
    enum class SceneType {
        PORTRAIT,       // 人像
        LANDSCAPE,      // 風景
        ARCHITECTURE,   // 建築
        STREET,         // 街拍
        FOOD,           // 食物
        NIGHT,          // 夜景
        UNKNOWN         // 未知
    }
    
    data class SceneInfo(
        val type: SceneType,
        val confidence: Float,  // 信心度 (0-1)
        val features: Map<String, Any>  // 場景特徵
    )
    
    /**
     * 分類場景
     */
    fun classify(
        bitmap: Bitmap,
        faces: List<FaceInfo>
    ): SceneInfo {
        Timber.d("Classifying scene...")
        
        val features = mutableMapOf<String, Any>()
        
        // 1. 分析光影
        val lightingInfo = lightingAnalyzer.analyze(bitmap)
        features["lighting"] = lightingInfo
        
        // 2. 偵測線條
        val lines = leadingLinesDetector.detectLines(bitmap)
        features["lineCount"] = lines.size
        
        // 3. 人臉資訊
        features["faceCount"] = faces.size
        if (faces.isNotEmpty()) {
            val totalFaceArea = faces.sumOf {
                it.boundingBox.width() * it.boundingBox.height()
            }
            val imageArea = bitmap.width * bitmap.height
            features["faceAreaRatio"] = totalFaceArea.toFloat() / imageArea
        }
        
        // 4. 根據特徵判斷場景
        val (sceneType, confidence) = determineScene(features, lightingInfo)
        
        Timber.d("Scene classified as $sceneType with confidence $confidence")
        
        return SceneInfo(
            type = sceneType,
            confidence = confidence,
            features = features
        )
    }
    
    /**
     * 根據特徵判斷場景類型
     */
    private fun determineScene(
        features: Map<String, Any>,
        lightingInfo: LightingAnalyzer.LightingInfo
    ): Pair<SceneType, Float> {
        
        val faceCount = features["faceCount"] as Int
        val faceAreaRatio = features["faceAreaRatio"] as? Float ?: 0f
        val lineCount = features["lineCount"] as Int
        
        // 規則引擎
        return when {
            // 人像：有人臉且佔比大
            faceCount > 0 && faceAreaRatio > 0.15f -> {
                SceneType.PORTRAIT to 0.9f
            }
            
            // 夜景：低光環境
            lightingInfo.isLowLight -> {
                SceneType.NIGHT to 0.8f
            }
            
            // 建築：有明顯線條且高對比
            lineCount > 3 && lightingInfo.isHighContrast -> {
                SceneType.ARCHITECTURE to 0.7f
            }
            
            // 風景：無人臉、光線充足
            faceCount == 0 && lightingInfo.averageBrightness > 0.4f -> {
                SceneType.LANDSCAPE to 0.6f
            }
            
            // 食物：中心區域有物體且高飽和度（此處簡化）
            faceCount == 0 && faceAreaRatio > 0.3f -> {
                SceneType.FOOD to 0.5f
            }
            
            // 街拍：有線條、有一些人臉
            lineCount > 1 && faceCount in 1..3 -> {
                SceneType.STREET to 0.6f
            }
            
            // 未知
            else -> {
                SceneType.UNKNOWN to 0.3f
            }
        }
    }
}
```

### 4.4 V2.0 單元測試範例

#### 4.4.1 光影分析測試

```kotlin
// data/source/analysis/LightingAnalyzerTest.kt
package local.mylab.smartcameraapp1.data.source.analysis

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LightingAnalyzerTest {
    
    private lateinit var analyzer: LightingAnalyzer
    
    @Before
    fun setup() {
        analyzer = LightingAnalyzer()
    }
    
    @Test
    fun `全黑圖片應判定為低光`() {
        // Given
        val bitmap = createSolidColorBitmap(100, 100, Color.BLACK)
        
        // When
        val result = analyzer.analyze(bitmap)
        
        // Then
        assertThat(result.isLowLight).isTrue()
        assertThat(result.averageBrightness).isLessThan(0.1f)
    }
    
    @Test
    fun `全白圖片應有高亮度`() {
        // Given
        val bitmap = createSolidColorBitmap(100, 100, Color.WHITE)
        
        // When
        val result = analyzer.analyze(bitmap)
        
        // Then
        assertThat(result.averageBrightness).isGreaterThan(0.9f)
        assertThat(result.isLowLight).isFalse()
    }
    
    @Test
    fun `黑白分明圖片應判定為高對比`() {
        // Given
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        // 上半部黑色，下半部白色
        for (y in 0 until 50) {
            for (x in 0 until 100) {
                bitmap.setPixel(x, y, Color.BLACK)
            }
        }
        for (y in 50 until 100) {
            for (x in 0 until 100) {
                bitmap.setPixel(x, y, Color.WHITE)
            }
        }
        
        // When
        val result = analyzer.analyze(bitmap)
        
        // Then
        assertThat(result.isHighContrast).isTrue()
        assertThat(result.contrast).isGreaterThan(0.7f)
    }
    
    @Test
    fun `逆光場景應被正確偵測`() {
        // Given: 創建逆光場景圖片（暗前景 + 亮背景）
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        // 中間區域暗（模擬人物剪影）
        for (y in 30 until 70) {
            for (x in 30 until 70) {
                bitmap.setPixel(x, y, Color.rgb(30, 30, 30))
            }
        }
        
        // 周圍區域亮（模擬背光）
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                if (x < 30 || x >= 70 || y < 30 || y >= 70) {
                    bitmap.setPixel(x, y, Color.rgb(240, 240, 240))
                }
            }
        }
        
        // When
        val result = analyzer.analyze(bitmap)
        
        // Then
        assertThat(result.isBacklit).isTrue()
        assertThat(result.hdrRecommended).isTrue()
    }
    
    private fun createSolidColorBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            for (x in 0 until width) {
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}
```

### 4.5 V2.0 測試檢查清單

```markdown
## V2.0 模擬器測試檢查清單

### 進階 UI 測試
- [ ] 引導線 Overlay 正確繪製
- [ ] 線條方向與偵測結果一致
- [ ] 對稱軸線位置準確
- [ ] 框架式標註正確顯示
- [ ] HDR 警示圖示在高對比場景顯示
- [ ] 多個 Overlay 同時顯示無衝突

### 圖像分析測試
- [ ] 引導線偵測：道路線條正確識別
- [ ] 引導線偵測：建築線條正確識別
- [ ] 對稱性分析：對稱建築正確判定
- [ ] 對稱性分析：非對稱場景正確判定
- [ ] 深度構圖：前景/中景/背景層次識別
- [ ] 填滿構圖：主體面積計算準確

### 光影分析測試
- [ ] 直方圖計算：全黑圖片正確
- [ ] 直方圖計算：全白圖片正確
- [ ] 直方圖計算：漸變圖片正確
- [ ] 逆光偵測：逆光場景正確判定
- [ ] 逆光偵測：正常光線不誤判
- [ ] 高對比偵測：黑白分明場景正確
- [ ] 低光偵測：暗場景正確判定
- [ ] HDR 建議：高對比場景建議 HDR
- [ ] HDR 建議：逆光場景建議 HDR

### 場景識別測試
- [ ] 人像場景：有人臉正確識別
- [ ] 風景場景：無人臉光線充足識別
- [ ] 建築場景：線條多高對比識別
- [ ] 夜景場景：低光環境識別
- [ ] 食物場景：中心物體識別
- [ ] 街拍場景：線條+少量人臉識別

### 單元測試覆蓋率
- [ ] LeadingLinesDetector: > 70%
- [ ] LightingAnalyzer: > 80%
- [ ] SceneClassifier: > 75%
- [ ] 整體: > 75%
```

---

## 五、測試資產管理

### 5.1 測試圖片組織

```
app/src/
├── debug/                              # 僅在 debug build 包含
│   └── assets/
│       └── test_images/
│           ├── README.md               # 圖片說明文檔
│           ├── composition/            # 構圖測試圖片
│           ├── lighting/               # 光影測試圖片
│           └── scenes/                 # 場景測試圖片
│
└── androidTest/                        # 儀器測試資產
    └── assets/
        └── test_images_small/          # 小尺寸測試圖片（快速測試）
```

### 5.2 測試圖片命名規範

```
格式: [category]_[feature]_[variant]_[resolution].jpg

範例:
- portrait_good_composition_1080p.jpg
- landscape_rule_of_thirds_hd.jpg
- backlight_severe_outdoor_720p.jpg
- symmetry_architecture_modern_1080p.jpg
- leading_lines_road_diagonal_hd.jpg
```

### 5.3 測試資產文檔

建立 `app/src/debug/assets/test_images/README.md`:

```markdown
# 測試圖片資產說明

## 圖片來源
- 部分圖片來自 Unsplash (https://unsplash.com)
- 部分為專案自行拍攝
- 所有圖片僅用於開發測試，不包含在 release build

## 圖片分類

### composition/ - 構圖測試圖片

| 檔名 | 描述 | 測試目標 |
|------|------|---------|
| leading_lines_road.jpg | 道路延伸至遠方 | 引導線偵測 |
| symmetry_architecture.jpg | 對稱建築正面 | 對稱性分析 |
| framing_arch.jpg | 拱門框架構圖 | 框架式構圖 |
| fill_frame_food.jpg | 特寫食物 | 填滿構圖 |

### lighting/ - 光影測試圖片

| 檔名 | 描述 | 測試目標 |
|------|------|---------|
| backlight_severe.jpg | 嚴重逆光人像 | 逆光偵測 |
| high_contrast.jpg | 高對比場景 | 對比度計算 |
| low_light.jpg | 低光環境 | 低光偵測 |

### scenes/ - 場景測試圖片

| 檔名 | 描述 | 測試目標 |
|------|------|---------|
| portrait_indoor.jpg | 室內人像 | 場景識別：人像 |
| landscape_mountain.jpg | 山景 | 場景識別：風景 |
| architecture_modern.jpg | 現代建築 | 場景識別：建築 |

## 使用方式

在模擬器模式下，這些圖片會被 `TestImageProvider` 自動載入。

```kotlin
// 範例：載入特定測試圖片
val bitmap = testImageProvider.getTestImage("backlight_severe.jpg")
```

## 維護規則

1. 新增圖片時更新此文檔
2. 保持圖片大小合理（< 2MB）
3. 使用 JPG 格式（PNG 僅用於需要透明度的情況）
4. 定期審查並移除不再使用的圖片
```

---

## 六、模擬器測試框架

### 6.1 自動化測試腳本

建立 `scripts/run_emulator_tests.sh`:

```bash
#!/bin/bash

# SmartCameraApp 模擬器自動化測試腳本

set -e

echo "========================================="
echo "SmartCameraApp Emulator Testing Suite"
echo "========================================="

# 1. 檢查模擬器是否運行
echo "Checking emulator status..."
adb devices | grep emulator
if [ $? -ne 0 ]; then
    echo "❌ No emulator detected. Please start emulator first."
    echo "   Run: emulator -avd SmartCamera_DevEmulator"
    exit 1
fi

echo "✅ Emulator detected"

# 2. 清理舊的構建
echo "Cleaning old builds..."
./gradlew clean

# 3. 構建模擬器版本
echo "Building emulator debug APK..."
./gradlew assembleEmulatorDebug

# 4. 運行單元測試
echo "Running unit tests..."
./gradlew testEmulatorDebugUnitTest

# 5. 安裝到模擬器
echo "Installing to emulator..."
./gradlew installEmulatorDebug

# 6. 運行儀器測試（可選）
# echo "Running instrumentation tests..."
# ./gradlew connectedEmulatorDebugAndroidTest

# 7. 生成測試報告
echo "Generating test reports..."
open app/build/reports/tests/testEmulatorDebugUnitTest/index.html

echo "========================================="
echo "✅ All tests completed successfully!"
echo "========================================="
```

### 6.2 快速驗證腳本

建立 `scripts/quick_verify.sh`:

```bash
#!/bin/bash

# 快速驗證腳本：僅構建和安裝，不運行測試

echo "Quick Verification Mode"
echo "========================"

# 構建
./gradlew assembleEmulatorDebug

# 安裝
adb install -r app/build/outputs/apk/emulator/debug/app-emulator-debug.apk

# 啟動應用
adb shell am start -n local.mylab.smartcameraapp1.emulator/.MainActivity

echo "✅ App installed and launched"
```

---

## 七、開發工作流

### 7.1 典型的一天開發流程（使用模擬器）

#### 早上 9:00-12:00：功能開發（VSCode）

```bash
# 1. 啟動模擬器（背景運行）
emulator -avd SmartCamera_DevEmulator -no-snapshot-load &

# 2. 打開 VSCode
cd ~/SmartCameraApp
code .

# 3. 開發新功能（例如：引導線 Overlay）
# 編輯 LeadingLinesOverlay.kt

# 4. 快速構建測試
Ctrl+Shift+B (或 Cmd+Shift+B)
# 選擇: 🚀 Build Emulator Debug

# 5. 運行單元測試
Ctrl+Shift+P → Tasks: Run Task → 🧪 Run Unit Tests

# 6. 提交代碼
git add .
git commit -m "feat: add leading lines overlay"
```

#### 下午 13:00-14:00：模擬器整合測試

```bash
# 1. 安裝到模擬器
Ctrl+Shift+P → Tasks: Run Task → 📱 Install to Emulator

# 2. 手動測試
# - 打開應用
# - 使用 TestImageProvider 切換測試圖片
# - 驗證 Overlay 顯示
# - 檢查構圖建議

# 3. 截圖記錄
adb exec-out screencap -p > screenshots/leading_lines_overlay.png

# 4. 記錄問題
# 在 VSCode 中標記 TODO 或創建 issue
```

#### 下午 14:00-17:00：修復和優化（VSCode）

```bash
# 根據測試發現的問題進行修復
# 再次運行單元測試確認
# 提交修復
git commit -m "fix: correct line angle calculation"
```

### 7.2 實機驗證時機

**何時需要切換到實機測試：**

1. **每週五**：週度整合測試
2. **V1.0 完成前**：完整功能驗證
3. **發現模擬器異常**：確認是否為模擬器問題
4. **性能相關問題**：FPS、延遲、記憶體
5. **發布候選版本**：最終驗證

---

## 八、驗測清單

### 8.1 V1.0 完整驗測清單

```markdown
# V1.0 模擬器驗測清單

## 環境準備
- [ ] 模擬器已創建並運行
- [ ] VSCode 擴展已安裝
- [ ] Gradle 任務配置完成
- [ ] 測試圖片資產已準備

## 單元測試（100% 模擬器）
- [ ] ViewModelTest: 全部通過
- [ ] UseCaseTest: 全部通過
- [ ] RepositoryTest: 全部通過
- [ ] 測試覆蓋率 > 70%

## UI 功能測試（100% 模擬器）
### 基礎 UI
- [ ] 應用啟動正常
- [ ] 權限請求流程正確
- [ ] 相機預覽顯示（使用測試圖片）
- [ ] UI 響應流暢（無明顯卡頓）

### 三分法格線
- [ ] 格線正確顯示（2 橫 2 豎）
- [ ] 格線位置準確（1/3 位置）
- [ ] 格線顏色清晰可見
- [ ] 格線可切換開關

### 水平儀
- [ ] 水平儀 UI 顯示
- [ ] 旋轉模擬器時水平儀響應
- [ ] 水平時顯示綠色
- [ ] 傾斜時顯示黃色/紅色

### 人臉框
- [ ] 測試圖片有人臉時顯示框
- [ ] 框的大小與人臉匹配
- [ ] 多人臉時所有框都顯示
- [ ] 無人臉時不顯示框

### 構圖建議
- [ ] 無主體時提示「請將主體放入畫面」
- [ ] 人臉過高時提示「向下移動」
- [ ] 人臉過低時提示「向上移動」
- [ ] 人臉偏左時提示「向右移動」
- [ ] 人臉偏右時提示「向左移動」
- [ ] 頭部空間過少時提示
- [ ] 頭部空間過多時提示

## 業務邏輯測試（100% 模擬器）
### 構圖分析
- [ ] 三分法判斷邏輯正確
- [ ] 頭部空間計算準確
- [ ] 置中判斷合理
- [ ] 建議優先級正確

### ML Kit 整合
- [ ] 人臉偵測 API 正常調用
- [ ] Bounding Box 數據正確
- [ ] 錯誤處理完善
- [ ] 使用靜態圖片可驗證

## 性能測試（部分模擬器可驗）
- [ ] UI 渲染流暢（目測 30fps+）
- [ ] 記憶體使用合理（< 200MB）
- [ ] 無明顯記憶體洩漏（長時間運行）
- [ ] 構圖分析延遲可接受（< 200ms）

## 邊界情況測試
- [ ] 無測試圖片時處理正常
- [ ] 圖片載入失敗時有錯誤提示
- [ ] 極大/極小圖片正常處理
- [ ] 多次啟動/停止預覽穩定

## 代碼質量
- [ ] Lint 檢查無 error
- [ ] 代碼格式化一致
- [ ] 日誌輸出合理（使用 Timber）
- [ ] 無硬編碼字串（使用 strings.xml）

## 文檔完整性
- [ ] README 更新
- [ ] 代碼註釋充足
- [ ] CHANGELOG 記錄
- [ ] TODO 標記清楚

## Git 狀態
- [ ] 所有變更已提交
- [ ] Commit message 清晰
- [ ] 分支命名規範
- [ ] 無未追蹤檔案

## 準備進入 V2.0
- [ ] V1.0 所有測試通過
- [ ] 核心架構穩定
- [ ] 測試框架完善
- [ ] 開發工作流順暢
```

### 8.2 V2.0 完整驗測清單

```markdown
# V2.0 模擬器驗測清單

## V2.0 新增功能測試

### 引導線偵測
- [ ] 道路線條正確偵測
- [ ] 鐵軌線條正確偵測
- [ ] 建築線條正確偵測
- [ ] 線條數量計算準確
- [ ] 線條角度計算正確

### 引導線 Overlay
- [ ] 偵測到的線條正確繪製
- [ ] 線條顏色清晰可見
- [ ] 線條方向與實際一致
- [ ] 可切換顯示/隱藏

### 對稱性分析
- [ ] 對稱建築正確判定
- [ ] 水面倒影正確判定
- [ ] 非對稱場景不誤判
- [ ] 對稱軸位置準確

### 對稱軸 Overlay
- [ ] 對稱軸線正確繪製
- [ ] 軸線位置居中
- [ ] 軸線顏色醒目
- [ ] 可切換顯示/隱藏

### 框架式構圖
- [ ] 拱門框架正確識別
- [ ] 窗框框架正確識別
- [ ] 框架邊界準確
- [ ] 框架 Overlay 正確顯示

### 深度構圖
- [ ] 前景層識別
- [ ] 中景層識別
- [ ] 背景層識別
- [ ] 層次建議合理

### 填滿構圖
- [ ] 主體面積計算準確
- [ ] 填滿比例判斷正確
- [ ] 建議「靠近」合理

### 光影分析
- [ ] 直方圖計算正確
- [ ] 平均亮度計算準確
- [ ] 對比度計算合理
- [ ] 逆光偵測準確
- [ ] 高對比偵測正確
- [ ] 低光偵測可靠
- [ ] HDR 建議合理

### HDR 警示
- [ ] 高對比場景顯示警示
- [ ] 逆光場景顯示警示
- [ ] 正常場景不顯示
- [ ] 圖示位置合適
- [ ] 圖示清晰可見

### 場景識別
- [ ] 人像場景正確識別
- [ ] 風景場景正確識別
- [ ] 建築場景正確識別
- [ ] 夜景場景正確識別
- [ ] 食物場景正確識別
- [ ] 街拍場景正確識別
- [ ] 信心度計算合理

## V2.0 單元測試
- [ ] LeadingLinesDetectorTest: 全部通過
- [ ] LightingAnalyzerTest: 全部通過
- [ ] SceneClassifierTest: 全部通過
- [ ] 測試覆蓋率 > 75%

## V2.0 整合測試
- [ ] 多個分析器協同工作
- [ ] 多個 Overlay 同時顯示
- [ ] 性能無明顯下降
- [ ] 記憶體占用合理

## V2.0 回歸測試
- [ ] V1.0 功能仍正常
- [ ] 三分法仍正確
- [ ] 人臉偵測仍準確
- [ ] 構圖建議仍合理

## 準備進入實機測試
- [ ] 所有模擬器測試通過
- [ ] 代碼品質良好
- [ ] 文檔完整更新
- [ ] 準備切換到 Android Studio
```

---

## 九、常見問題與解決方案

### 9.1 模擬器問題

#### Q1: 模擬器啟動緩慢

**症狀**: 模擬器啟動需要 5+ 分鐘

**解決方案**:
```bash
# 1. 啟用硬體加速
emulator -avd SmartCamera_DevEmulator -gpu host -accel on

# 2. 使用快照（首次啟動後）
emulator -avd SmartCamera_DevEmulator -no-snapshot-load

# 3. 增加 RAM
# 編輯 ~/.android/avd/SmartCamera_DevEmulator.avd/config.ini
hw.ramSize=4096
```

#### Q2: 模擬器相機顯示黑屏

**症狀**: 相機預覽顯示黑屏或花屏

**原因**: 模擬器相機是虛擬的，我們的方案使用靜態圖片

**驗證**:
```kotlin
// 確認正在使用 FakeCameraDataSource
// 檢查 Logcat:
// "Using FakeCameraDataSource (Emulator Mode)"
```

#### Q3: ML Kit 檢測不準確

**症狀**: 人臉偵測結果與實機差異大

**解決方案**: 這是預期行為，模擬器 ML Kit 性能較差

**建議**:
- 使用預先標記的測試圖片
- 模擬器專注測試邏輯，不測試檢測準確度
- 準確度驗證留給實機測試

### 9.2 構建問題

#### Q1: Gradle 同步失敗

```bash
# 解決方案 1: 清理快取
./gradlew clean
rm -rf .gradle build

# 解決方案 2: 刷新依賴
./gradlew --refresh-dependencies

# 解決方案 3: 檢查網絡代理
# 編輯 gradle.properties
systemProp.http.proxyHost=proxy.example.com
systemProp.http.proxyPort=8080
```

#### Q2: BuildConfig 找不到

**症狀**: `BuildConfig.EMULATOR_MODE` 未定義

**解決方案**:
```kotlin
// 確保 build.gradle.kts 中有
android {
    buildFeatures {
        buildConfig = true
    }
}

// 重新構建
./gradlew clean build
```

### 9.3 測試問題

#### Q1: 單元測試找不到測試圖片

**症狀**: `TestImageProvider` 無法載入圖片

**解決方案**:
```kotlin
// 使用 Robolectric 運行測試
@RunWith(RobolectricTestRunner::class)
class MyTest {
    @Config(sdk = [28])
    @Test
    fun testWithImage() {
        // ...
    }
}
```

#### Q2: Flow 測試超時

**症狀**: StateFlow/SharedFlow 測試hang住

**解決方案**:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @Test
    fun test() = runTest {
        // 測試代碼
        advanceUntilIdle()  // 重要！
    }
}
```

---

## 十、總結與下一步

### 10.1 模擬器快速迭代的優勢

1. **開發速度**: 構建-測試循環縮短 70%
2. **資源效率**: 記憶體和 CPU 占用減少 60%
3. **測試覆蓋**: 80% 功能可在模擬器驗證
4. **成本節省**: 減少實機設備需求

### 10.2 何時必須使用實機

1. V1.0 完成後的整合測試
2. 相機實時性能驗證
3. GPU Shader 實際效果
4. 多設備兼容性測試
5. 發布前最終驗證

### 10.3 建議的開發節奏

```
週一-週四: 模擬器開發 + 單元測試（VSCode）
週五:      實機整合測試（Android Studio）
每月底:    多設備兼容性測試
發布前:    完整實機測試套件
```

### 10.4 下一步行動

1. **立即執行**:
   - 配置開發環境
   - 建立測試資產庫
   - 建立第一個 V1.0 功能

2. **本週完成**:
   - 完成 CameraX 架構
   - 實現 FakeCameraDataSource
   - 建立單元測試框架

3. **本月目標**:
   - 完成 V1.0 所有功能
   - 模擬器測試全部通過
   - 準備第一次實機測試

---

**最後更新**: 2025-12-14  
**文件版本**: 1.0  
**專案**: SmartCameraApp1  
**作者**: Akira  
**適用範圍**: V1.0-V2.0 模擬器快速迭代開發
