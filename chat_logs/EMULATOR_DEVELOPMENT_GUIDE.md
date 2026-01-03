# 模擬器快速迭代開發指南

## 🎯 概述

本專案已配置完成模擬器快速迭代開發環境，讓您能在 VSCode 中高效開發和測試 SmartCameraApp。

## 📁 專案結構

```
SmartCameraApp1/
├── .vscode/
│   └── tasks.json                    # VSCode 任務配置
├── app/
│   ├── build.gradle.kts              # 已配置 emulator/device flavors
│   └── src/
│       ├── main/
│       │   └── java/.../
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   └── TestImageProvider.kt        # 測試圖片提供者
│       │       │   └── source/camera/
│       │       │       ├── CameraDataSource.kt         # 相機資料源介面
│       │       │       ├── FakeCameraDataSource.kt     # 模擬器用假資料源
│       │       │       └── RealCameraDataSource.kt     # 實機用真資料源
│       │       └── di/
│       │           └── CameraModule.kt                 # 自動切換模擬器/實機
│       └── debug/
│           └── assets/test_images/   # 測試圖片目錄
└── chat_logs/
    └── 03-EMULATOR_RAPID_ITERATION_PLAN.md  # 完整實施計畫
```

## 🚀 快速開始

### 1. 構建模擬器版本

使用 VSCode 任務面板（`Ctrl+Shift+P` > `Tasks: Run Task`）或終端命令：

```bash
# 構建模擬器 Debug 版本
./gradlew.bat assembleEmulatorDebug

# 安裝到模擬器
./gradlew.bat installEmulatorDebug
```

### 2. VSCode 快捷任務

在 VSCode 中按 `Ctrl+Shift+B` 可以看到以下任務：

- 🚀 **Build Emulator Debug** - 構建模擬器版本（預設）
- 📱 **Install to Emulator** - 安裝到模擬器
- 🧪 **Run Unit Tests** - 執行單元測試
- 🔍 **Run Lint** - 執行程式碼檢查
- 🧹 **Clean Build** - 清理構建
- 📊 **Generate Test Report** - 生成測試報告
- 🎯 **Full Test Cycle** - 完整測試循環
- 🚀 **Build Device Debug** - 構建實機版本
- 📱 **Install to Device** - 安裝到實機

### 3. 添加測試圖片

將測試圖片放入以下目錄：

```
app/src/debug/assets/test_images/
├── portrait_good_composition.jpg
├── portrait_bad_headroom.jpg
├── portrait_off_center.jpg
├── landscape_symmetry.jpg
└── landscape_rule_of_thirds.jpg
```

**建議規格：**
- 尺寸：1920x1080 或更高
- 格式：JPG 或 PNG
- 大小：< 5MB

## 🎮 模擬器 vs 實機模式

### 自動切換機制

系統會根據以下條件自動選擇資料源：

```kotlin
// 模擬器模式（使用假資料源）
- BuildConfig.EMULATOR_MODE == true (debug build)
- BuildConfig.IS_EMULATOR_BUILD == true (emulator flavor)
- 偵測到運行在模擬器上

// 實機模式（使用真資料源）
- 其他情況
```

### Build Variants 說明

| Variant | 用途 | 資料源 | 測試圖片 |
|---------|------|--------|---------|
| `emulatorDebug` | 模擬器開發 | FakeCameraDataSource | ✅ |
| `emulatorRelease` | 模擬器測試 | FakeCameraDataSource | ✅ |
| `deviceDebug` | 實機開發 | RealCameraDataSource | ❌ |
| `deviceRelease` | 實機發布 | RealCameraDataSource | ❌ |

## 📝 開發工作流

### 典型開發循環

1. **開發階段**（模擬器）
   ```bash
   # 修改程式碼
   # 構建並安裝
   ./gradlew.bat installEmulatorDebug

   # 或使用 VSCode 任務：📱 Install to Emulator
   ```

2. **測試階段**（模擬器）
   ```bash
   # 執行單元測試
   ./gradlew.bat testEmulatorDebugUnitTest

   # 或使用 VSCode 任務：🧪 Run Unit Tests
   ```

3. **驗證階段**（實機）
   ```bash
   # 切換到實機模式
   ./gradlew.bat installDeviceDebug

   # 或使用 VSCode 任務：📱 Install to Device
   ```

### 日誌查看

系統會自動記錄當前使用的資料源：

```
D/CameraModule: 🎮 Using FakeCameraDataSource (Emulator Mode)
D/CameraModule:   - BuildConfig.EMULATOR_MODE: true
D/CameraModule:   - BuildConfig.IS_EMULATOR_BUILD: true
D/CameraModule:   - isRunningOnEmulator(): true

D/FakeCameraDataSource: Starting preview stream
D/FakeCameraDataSource: Emitted test image 1920x1080
```

## 🧪 單元測試

### 執行測試

```bash
# 執行所有單元測試
./gradlew.bat testEmulatorDebugUnitTest

# 執行特定測試
./gradlew.bat testEmulatorDebugUnitTest --tests "CameraViewModelTest"

# 生成測試報告
./gradlew.bat testEmulatorDebugUnitTest
# 報告位置：app/build/reports/tests/testEmulatorDebugUnitTest/index.html
```

### 測試依賴

已添加以下測試庫：
- JUnit 4
- Mockk (Kotlin mocking)
- Truth (Google assertions)
- Turbine (Flow testing)
- Coroutines Test
- Robolectric

## 🔧 進階配置

### 自定義 BuildConfig 標記

在 `app/build.gradle.kts` 中：

```kotlin
buildTypes {
    debug {
        buildConfigField("Boolean", "EMULATOR_MODE", "true")
        buildConfigField("Boolean", "USE_TEST_IMAGES", "true")
        buildConfigField("Boolean", "ENABLE_PERFORMANCE_LOGGING", "false")
    }
}

productFlavors {
    create("emulator") {
        buildConfigField("Boolean", "IS_EMULATOR_BUILD", "true")
        buildConfigField("Boolean", "MOCK_CAMERA_DATA", "true")
    }
}
```

### 在程式碼中使用

```kotlin
if (BuildConfig.USE_TEST_IMAGES) {
    // 使用測試圖片
} else {
    // 使用真實相機
}
```

## 📊 模擬器適用性

### V1.0 功能（當前版本）

| 功能 | 模擬器可驗測 | 說明 |
|------|------------|------|
| UI/Overlay | ✅ 100% | 格線、水平儀、建議氣泡 |
| 構圖算法 | ✅ 100% | 三分法、頭部空間、置中判斷 |
| ML Kit 架構 | ✅ 靜態圖 | 人臉偵測（使用測試圖片） |
| ViewModel | ✅ 100% | 狀態管理、Flow |
| Repository | ✅ 100% | 資料層邏輯 |
| 相機預覽 | ⚠️ 部分 | 架構可驗，效果需實機 |
| 相機性能 | ❌ 需實機 | FPS、延遲、記憶體 |

### V2.0 功能（未來）

| 功能 | 模擬器可驗測 | 說明 |
|------|------------|------|
| 進階 Overlay | ✅ 100% | 引導線、對稱軸、框架 |
| 圖像分析 | ✅ 靜態圖 | Hough 線偵測、對稱性 |
| 光影分析 | ✅ 100% | 直方圖、逆光、對比度 |
| 場景識別 | ✅ 靜態圖 | 規則引擎分類 |

## 🐛 故障排除

### 構建失敗

```bash
# 清理並重新構建
./gradlew.bat clean
./gradlew.bat assembleEmulatorDebug
```

### BuildConfig 未生成

確保 `build.gradle.kts` 中啟用了 `buildConfig`：

```kotlin
buildFeatures {
    compose = true
    buildConfig = true  // ← 必須啟用
}
```

### 測試圖片無法載入

1. 確認圖片位於 `app/src/debug/assets/test_images/`
2. 檢查檔案名稱是否正確
3. 如果圖片缺失，系統會自動生成備用圖片

### Hilt 注入錯誤

確保已添加必要的 Hilt 註解：
- `@HiltAndroidApp` on Application
- `@AndroidEntryPoint` on Activity/Fragment
- `@Inject` on constructors

## 📚 參考資料

- [完整實施計畫](chat_logs/03-EMULATOR_RAPID_ITERATION_PLAN.md)
- [CameraDataSource.kt](app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/CameraDataSource.kt)
- [CameraModule.kt](app/src/main/java/local/mylab/smartcameraapp1/di/CameraModule.kt)
- [VSCode Tasks](/.vscode/tasks.json)

## 🎓 最佳實踐

1. **80% 模擬器開發** - 大部分功能在模擬器上完成
2. **15% 交叉驗證** - 部分功能需模擬器+實機對比
3. **5% 實機專屬** - 僅性能相關需實機測試

4. **快速迭代** - 使用 VSCode 任務快速構建安裝
5. **單元測試先行** - 在模擬器上驗證邏輯正確性
6. **最後實機驗證** - 完成功能後再進行實機測試

---

**建立日期**: 2025-12-14
**版本**: v1.0
**狀態**: ✅ 環境已配置完成
