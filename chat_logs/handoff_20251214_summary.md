# 模擬器快速迭代開發環境 - 配置總結

**日期**: 2025-12-14
**任務**: 實施模擬器快速迭代開發計畫
**狀態**: ✅ 完成

## 📋 完成的任務

### 1. ✅ Gradle Build Flavors 配置
**檔案**: [app/build.gradle.kts](../app/build.gradle.kts)

**新增內容**:
- 兩個 product flavors: `emulator` 和 `device`
- BuildConfig 標記自動區分模擬器/實機模式
- 測試依賴（Mockk, Turbine, Truth, Coroutines Test）
- Timber logging 依賴
- `buildConfig` feature 啟用

**Build Variants**:
- `emulatorDebug` / `emulatorRelease`
- `deviceDebug` / `deviceRelease`

### 2. ✅ VSCode 任務配置
**檔案**: [.vscode/tasks.json](../.vscode/tasks.json)

**可用任務**:
- 🚀 Build Emulator Debug (預設)
- 📱 Install to Emulator
- 🧪 Run Unit Tests
- 🔍 Run Lint
- 🧹 Clean Build
- 📊 Generate Test Report
- 🎯 Full Test Cycle
- 🚀 Build Device Debug
- 📱 Install to Device

### 3. ✅ 測試圖片提供者
**檔案**: [app/src/main/java/local/mylab/smartcameraapp1/data/local/TestImageProvider.kt](../app/src/main/java/local/mylab/smartcameraapp1/data/local/TestImageProvider.kt)

**功能**:
- 從 assets 載入測試圖片
- 自動循環測試圖片
- 失敗時自動生成備用圖片（灰色背景 + 白色矩形）
- 支援特定圖片載入

### 4. ✅ 相機資料源架構
**檔案**:
- [CameraDataSource.kt](../app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/CameraDataSource.kt) - 介面
- [FakeCameraDataSource.kt](../app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/FakeCameraDataSource.kt) - 模擬器實現
- [RealCameraDataSource.kt](../app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/RealCameraDataSource.kt) - 實機實現（待完成）

**介面方法**:
```kotlin
interface CameraDataSource {
    fun getPreviewStream(): Flow<Bitmap>
    suspend fun takePicture(): Result<Bitmap>
    fun stopPreview()
    suspend fun setZoomRatio(ratio: Float): Result<Unit>
}
```

**FakeCameraDataSource 特性**:
- 使用測試圖片模擬預覽流
- 30 FPS 模擬（33ms delay）
- 完整的日誌記錄
- 支援所有介面方法

### 5. ✅ Hilt 依賴注入自動切換
**檔案**: [app/src/main/java/local/mylab/smartcameraapp1/di/CameraModule.kt](../app/src/main/java/local/mylab/smartcameraapp1/di/CameraModule.kt)

**切換邏輯**:
```kotlin
模擬器模式 (FakeCameraDataSource):
- BuildConfig.EMULATOR_MODE == true
- BuildConfig.IS_EMULATOR_BUILD == true
- isRunningOnEmulator() == true

實機模式 (RealCameraDataSource):
- 其他情況
```

**偵測方法**:
- Build fingerprint
- Device model
- Manufacturer
- Brand/Device/Product 名稱

### 6. ✅ 測試圖片資源目錄
**檔案**: [app/src/debug/assets/test_images/](../app/src/debug/assets/test_images/)

**預期圖片**:
- portrait_good_composition.jpg
- portrait_bad_headroom.jpg
- portrait_off_center.jpg
- landscape_symmetry.jpg
- landscape_rule_of_thirds.jpg

**注意**: 圖片可選，系統會在缺失時自動生成備用圖片。

### 7. ✅ 文檔
**建立的文檔**:
- [EMULATOR_DEVELOPMENT_GUIDE.md](../EMULATOR_DEVELOPMENT_GUIDE.md) - 完整開發指南
- [QUICK_START.md](../QUICK_START.md) - 快速開始指南
- [app/src/debug/assets/test_images/README.md](../app/src/debug/assets/test_images/README.md) - 測試圖片說明

## 🧪 測試結果

### 構建測試
```bash
./gradlew.bat assembleEmulatorDebug
```
**結果**: ✅ BUILD SUCCESSFUL in 40s

### 生成的 APK
- **路徑**: `app/build/outputs/apk/emulator/debug/app-emulator-debug.apk`
- **大小**: 93MB
- **Package ID**: `local.mylab.smartcameraapp1.emulator`
- **Version**: 2.4.6-emulator

## 📊 專案影響

### 新增檔案 (8)
1. `.vscode/tasks.json`
2. `app/src/main/java/local/mylab/smartcameraapp1/data/local/TestImageProvider.kt`
3. `app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/CameraDataSource.kt`
4. `app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/FakeCameraDataSource.kt`
5. `app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/RealCameraDataSource.kt`
6. `app/src/main/java/local/mylab/smartcameraapp1/di/CameraModule.kt`
7. `app/src/debug/assets/test_images/README.md`
8. `EMULATOR_DEVELOPMENT_GUIDE.md`

### 修改檔案 (1)
1. `app/build.gradle.kts`

### 新增目錄 (4)
1. `.vscode/`
2. `app/src/main/java/local/mylab/smartcameraapp1/data/local/`
3. `app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/`
4. `app/src/debug/assets/test_images/`

## 🎯 下一步建議

### 立即可做
1. **添加測試圖片** - 放入 `app/src/debug/assets/test_images/`
2. **執行單元測試** - `./gradlew.bat testEmulatorDebugUnitTest`
3. **安裝到模擬器** - `./gradlew.bat installEmulatorDebug`

### 整合現有程式碼
1. **更新 CameraViewModel** - 使用新的 `CameraDataSource` 介面
2. **更新 Repository** - 注入 `CameraDataSource` 而非直接使用 CameraX
3. **移除舊的相機初始化邏輯** - 使用新的架構

### V1.0 實施
參考 [03-EMULATOR_RAPID_ITERATION_PLAN.md](03-EMULATOR_RAPID_ITERATION_PLAN.md) 第三章：
- 構圖分析 Use Case
- ViewModel 實現
- UI Overlay 實現
- 單元測試

## 🔑 關鍵特性

### 1. 完全自動化
- 無需手動切換模式
- BuildConfig 自動設置
- Hilt 自動注入正確的實現

### 2. 開發效率
- VSCode 一鍵構建和安裝
- 快速迭代（無需實機）
- 單元測試完整支援

### 3. 擴展性
- 易於添加新的測試圖片
- 易於擴展測試場景
- 易於切換到實機測試

### 4. 日誌完整
- 清楚標示當前模式（🎮 模擬器 / 📱 實機）
- 詳細的資料源日誌
- BuildConfig 值完整輸出

## 💡 使用範例

### VSCode 快捷鍵
```
Ctrl+Shift+B  → 快速構建
Ctrl+Shift+P  → Tasks: Run Task → 選擇任務
```

### 命令列快速開發
```bash
# 開發循環
./gradlew.bat installEmulatorDebug && adb logcat | grep -E "(CameraModule|FakeCameraDataSource)"

# 測試循環
./gradlew.bat testEmulatorDebugUnitTest

# 發布前驗證
./gradlew.bat installDeviceDebug
```

## 📌 重要注意事項

1. **測試圖片非必需** - 系統會自動生成備用圖片
2. **Timber 已添加** - 用於日誌記錄
3. **RealCameraDataSource 待實現** - 標記為 TODO
4. **BuildConfig feature 已啟用** - 可使用 BuildConfig.xxx
5. **模擬器偵測完整** - 覆蓋各種模擬器類型

## 🎓 學習資源

- [Gradle Product Flavors](https://developer.android.com/studio/build/build-variants)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)
- [VSCode Tasks](https://code.visualstudio.com/docs/editor/tasks)

## ✅ 驗證清單

- [x] Gradle flavors 配置正確
- [x] VSCode tasks 可用
- [x] 模擬器版本可構建
- [x] 實機版本可構建
- [x] APK 生成成功
- [x] Hilt 模組配置正確
- [x] 測試依賴完整
- [x] 文檔完整

---

**配置完成時間**: 2025-12-14 14:46
**構建狀態**: ✅ SUCCESS
**準備狀態**: ✅ 可開始開發
