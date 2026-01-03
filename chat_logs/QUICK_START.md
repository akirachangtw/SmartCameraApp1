# 🚀 SmartCameraApp - 快速開始

## ✅ 環境已配置完成

模擬器快速迭代開發環境已成功配置！

### 📦 生成的 APK
- **位置**: `app/build/outputs/apk/emulator/debug/app-emulator-debug.apk`
- **大小**: 93MB
- **Variant**: emulatorDebug
- **狀態**: ✅ 構建成功

## 🎯 快速命令

### VSCode 任務（推薦）
按 `Ctrl+Shift+B` 選擇任務：
- 🚀 Build Emulator Debug (預設)
- 📱 Install to Emulator
- 🧪 Run Unit Tests

### 命令列
```bash
# 構建模擬器版本
./gradlew.bat assembleEmulatorDebug

# 安裝到模擬器
./gradlew.bat installEmulatorDebug

# 構建實機版本
./gradlew.bat assembleDeviceDebug

# 執行單元測試
./gradlew.bat testEmulatorDebugUnitTest

# 清理
./gradlew.bat clean
```

## 📁 關鍵檔案

### 新建立的檔案
- ✅ [.vscode/tasks.json](.vscode/tasks.json) - VSCode 任務配置
- ✅ [app/build.gradle.kts](app/build.gradle.kts) - Gradle flavors 配置
- ✅ [TestImageProvider.kt](app/src/main/java/local/mylab/smartcameraapp1/data/local/TestImageProvider.kt) - 測試圖片提供者
- ✅ [CameraDataSource.kt](app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/CameraDataSource.kt) - 相機資料源介面
- ✅ [FakeCameraDataSource.kt](app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/FakeCameraDataSource.kt) - 模擬器用假資料源
- ✅ [RealCameraDataSource.kt](app/src/main/java/local/mylab/smartcameraapp1/data/source/camera/RealCameraDataSource.kt) - 實機用真資料源
- ✅ [CameraModule.kt](app/src/main/java/local/mylab/smartcameraapp1/di/CameraModule.kt) - Hilt 自動切換模組

### 資源目錄
- ✅ [app/src/debug/assets/test_images/](app/src/debug/assets/test_images/) - 測試圖片目錄（請添加測試圖片）

## 🎮 如何使用

### 1. 添加測試圖片（可選）
將測試圖片放入 `app/src/debug/assets/test_images/`：
- portrait_good_composition.jpg
- portrait_bad_headroom.jpg
- portrait_off_center.jpg
- landscape_symmetry.jpg
- landscape_rule_of_thirds.jpg

**如果沒有測試圖片，系統會自動生成備用圖片。**

### 2. 啟動模擬器
```bash
# 從 Android Studio 或命令列啟動模擬器
emulator -avd <your_emulator_name>
```

### 3. 安裝應用
```bash
# 方法 1: VSCode 任務
Ctrl+Shift+B → 選擇 "📱 Install to Emulator"

# 方法 2: 命令列
./gradlew.bat installEmulatorDebug
```

### 4. 查看日誌
系統會自動顯示使用的資料源：
```
D/CameraModule: 🎮 Using FakeCameraDataSource (Emulator Mode)
D/FakeCameraDataSource: Starting preview stream
D/FakeCameraDataSource: Emitted test image 1920x1080
```

## 🔄 Build Variants

當前可用的構建變體：

| Variant | 用途 | 資料源 |
|---------|------|--------|
| emulatorDebug | 模擬器開發 | FakeCameraDataSource |
| emulatorRelease | 模擬器測試 | FakeCameraDataSource |
| deviceDebug | 實機開發 | RealCameraDataSource |
| deviceRelease | 實機發布 | RealCameraDataSource |

## 📚 文檔

- [完整開發指南](EMULATOR_DEVELOPMENT_GUIDE.md)
- [實施計畫](chat_logs/03-EMULATOR_RAPID_ITERATION_PLAN.md)

## 🐛 故障排除

### 構建失敗
```bash
./gradlew.bat clean
./gradlew.bat assembleEmulatorDebug
```

### 找不到 BuildConfig
確保已啟用 `buildConfig` feature in [app/build.gradle.kts](app/build.gradle.kts#L76)

### Hilt 錯誤
確保應用已添加 Hilt 註解：
- `@HiltAndroidApp` on Application
- `@AndroidEntryPoint` on Activity

## ✨ 下一步

1. **添加測試圖片** - 或使用自動生成的備用圖片
2. **整合現有 CameraViewModel** - 使用新的 CameraDataSource
3. **執行單元測試** - 驗證功能正確性
4. **實機驗證** - 切換到 deviceDebug 進行最終測試

---

**狀態**: ✅ 環境配置完成
**構建測試**: ✅ 通過
**最後更新**: 2025-12-14 14:46
