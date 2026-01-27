# Android 16 KB 頁面大小相容性分析報告

## 1. 現狀分析
根據 APK 分析結果，專案目前存在 16 KB 支援問題，主要集中在 **ML Kit** 的 Native 程式庫。

* **受影響檔案**：
    * `libmlkitcommonpipeline.so`
    * `libface_detector_v2_jni.so`
* **錯誤原因**：這些 `.so` 檔案使用的是 4 KB 的 LOAD section 對齊，而在 Android 15+ 的 16 KB 設備上需要 16 KB 對齊。
* **已知優點**：
    * OpenCV (`libopencv_java4.so`) 與 `libc++_shared.so` 已成功對齊（顯示為 16 KB），這得益於專案已使用 **NDK 27.0.12077973**。

## 2. 建議方案

### 方案 A：升級 ML Kit 版本（最推薦）
Google 已在較新版本的 ML Kit 中修復了對齊問題。請修改 `gradle/libs.versions.toml`：

```toml
[versions]
# 建議升級至以下版本或更高
mlkitObjectDetection = "17.0.3"
mlkitFaceDetection = "16.1.7"
```

### 方案 B：強制 AGP 進行對齊修正
在 `app/build.gradle.kts` 中明確啟用對齊優化。

```kotlin
android {
    packaging {
        jniLibs {
            // 確保原生庫在 APK 中不被壓縮，以便系統能進行 16KB 記憶體映射
            useLegacyPackaging = false
        }
    }
}
```

並在 `gradle.properties` 加入：

```properties
# 確保 App Bundle 在封裝時保持原生庫不壓縮
android.bundle.enableUncompressedNativeLibs=true
```

### 方案 C：驗證特定 Native 模組的編譯參數
針對專案內的 `opencv` 模組，確保其 `CMake` 或 `ndk-build` 過程繼承了 NDK 27 的預設參數。目前的 APK 顯示 OpenCV 已達標，因此維持現有配置即可。

## 3. 驗證方法
1. 執行 `./gradlew assembleDeviceDebug`。
2. 使用 Android Studio 的 **Build > Analyze APK...**。
3. 選取生成的 APK，檢查 **lib** 資料夾下的 `arm64-v8a` 與 `x86_64` 目錄。
4. 確認 **Alignment** 欄位皆顯示為 **16 KB** 且沒有黃色警告標誌。
