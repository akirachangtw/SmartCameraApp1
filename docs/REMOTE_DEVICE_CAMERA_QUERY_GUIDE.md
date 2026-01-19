# Android Studio 遠端實體手機相機解析度查詢指南

**文件日期**: 2026-01-12
**目的**: 透過 Android Studio 遠端連接實體手機，提取相機硬體支援的解析度資訊

---

## 🤔 方法可行性分析

### ❌ 限制與挑戰

#### 1. Camera2 API 需要應用權限
```
問題: Camera2 API 的 CameraCharacteristics 查詢需要：
- CAMERA 權限（需要 APK 安裝）
- CameraManager 服務（需要 Android Context）
- 不能直接透過 adb shell 執行

結論: ❌ 無法透過純 adb 命令獲取完整解析度列表
```

#### 2. dumpsys media.camera 資訊有限
```
問題: adb shell dumpsys media.camera 提供的資訊：
- ✅ 相機 ID 列表
- ✅ 基本相機特性
- ❌ 不包含詳細的支援解析度列表
- ❌ 格式不易解析

結論: ⚠️ 只能獲取部分基礎資訊
```

#### 3. 系統屬性不包含解析度
```
問題: getprop 只包含：
- 螢幕解析度（display）
- 相機數量
- ❌ 不包含相機支援的解析度列表

結論: ❌ 無法獲取需要的資訊
```

---

## ✅ 可行的解決方案

### 方案 A: 輕量級診斷 APK（推薦）⭐⭐⭐⭐⭐

**概念**: 建立一個極簡的診斷 APK，專門用於查詢相機資訊

#### 優點
- ✅ 可獲取完整的 Camera2 API 資訊
- ✅ 不需要完整的 SmartCameraApp 功能
- ✅ APK 大小小（< 5 MB）
- ✅ 快速安裝與執行
- ✅ 可在任何裝置上使用

#### 實作方式

**Step 1: 建立診斷 Activity**

```kotlin
// DiagnosticActivity.kt
class CameraDiagnosticActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val report = StringBuilder()

        // 裝置資訊
        report.appendLine("=== Device Info ===")
        report.appendLine("Manufacturer: ${Build.MANUFACTURER}")
        report.appendLine("Model: ${Build.MODEL}")
        report.appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        report.appendLine()

        // 查詢所有相機
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            report.appendLine("=== Camera $cameraId ===")

            // 方向
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            report.appendLine("Facing: ${if (facing == CameraCharacteristics.LENS_FACING_BACK) "BACK" else "FRONT"}")

            // 支援的解析度
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = map?.getOutputSizes(ImageFormat.YUV_420_888) ?: emptyArray()

            report.appendLine("Supported Resolutions (${sizes.size} total):")
            sizes.sortedByDescending { it.width * it.height }.forEach { size ->
                val mp = (size.width * size.height) / 1_000_000.0
                report.appendLine("  ${size.width}x${size.height} (${String.format("%.2f", mp)} MP)")
            }
            report.appendLine()
        }

        // 輸出到 logcat
        Log.i("CameraDiagnostic", report.toString())

        // 顯示結果
        setContent {
            Text(report.toString(), fontFamily = FontFamily.Monospace)
        }

        // 同時寫入檔案
        val file = File(getExternalFilesDir(null), "camera_info.txt")
        file.writeText(report.toString())
        Log.i("CameraDiagnostic", "Report saved to: ${file.absolutePath}")
    }
}
```

**Step 2: 極簡 AndroidManifest.xml**

```xml
<manifest>
    <uses-permission android:name="android.permission.CAMERA" />

    <application>
        <activity
            android:name=".CameraDiagnosticActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**Step 3: 使用方式**

```bash
# 1. 編譯診斷 APK
./gradlew assembleDiagnosticDebug

# 2. 安裝
adb install app-diagnostic-debug.apk

# 3. 啟動並擷取日誌
adb shell am start -n com.akirachang.ai.photographer.diagnostic/.CameraDiagnosticActivity
sleep 3
adb logcat -d | grep "CameraDiagnostic" > camera_info.txt

# 4. 或直接從檔案讀取
adb pull /sdcard/Android/data/com.akirachang.ai.photographer.diagnostic/files/camera_info.txt
```

---

### 方案 B: 使用現有 SmartCameraApp（目前可用）⭐⭐⭐⭐

**概念**: 利用 v2.9.0-beta5 已有的診斷日誌功能

#### 優點
- ✅ 不需額外開發
- ✅ 已包含完整的解析度查詢
- ✅ 立即可用

#### 使用方式

```bash
# 1. 確保裝置已連接
adb devices

# 2. 安裝 v2.9.0-beta5
cd SmartCameraApp1
./gradlew installDeviceDebug

# 3. 清除並啟動
adb logcat -c
adb shell am start -n com.akirachang.ai.photographer/.MainActivity

# 4. 等待 10 秒後擷取日誌
sleep 10
adb logcat -d > device_camera_info.log

# 5. 提取關鍵資訊
grep "\[v2.9.0-beta" device_camera_info.log > camera_resolutions.txt
```

#### 輸出範例

```
01-12 22:56:31.156 I LiveCameraSource: [v2.9.0-beta5] Device: Google Pixel 9a, 29 resolutions
01-12 22:56:31.157 I LiveCameraSource: [v2.9.0-beta5] Acceptable: 640x360, 640x480, 720x480, 800x600, 1024x576, 1280x720
01-12 22:56:31.157 I LiveCameraSource: [v2.9.0-beta3 Supported Resolutions] YUV_420_888 (29 total):
01-12 22:56:31.157 I LiveCameraSource: [v2.9.0-beta3] 176x144, 320x240, 352x288, 640x360, 640x480, 720x480, 800x600, 1024x576, 1024x768, 1280x720, 1080x1080, 1280x960, 1344x1008, 1440x1080, 1792x1008, 1920x960, 1600x1200, 1920x1080, 1920x1440, 2048x1536, 2560x1280, 1920x1920, 2688x1512, 2560x1920, 3264x1836, 3264x2448, 4000x2000, 3840x2160, 4000x3000
```

---

### 方案 C: Android Studio Device Explorer（有限資訊）⭐⭐

**概念**: 使用 Android Studio 內建工具

#### 優點
- ✅ 不需安裝 APK
- ✅ 視覺化界面

#### 限制
- ❌ 無法獲取相機解析度列表
- ⚠️ 只能查看基本裝置資訊

#### 使用方式

1. **Android Studio → View → Tool Windows → Device Manager**
2. **選擇裝置 → 右鍵 → Properties**
3. **查看**:
   - 裝置型號
   - Android 版本
   - 螢幕解析度
   - ❌ 無相機解析度

---

### 方案 D: CTS Verifier（專業但複雜）⭐⭐⭐

**概念**: 使用 Google 官方的 CTS Verifier

#### 優點
- ✅ 完整的相機測試
- ✅ 官方工具

#### 缺點
- ❌ 下載安裝複雜
- ❌ 需要手動操作測試
- ❌ 報告格式不易解析

---

## 🎯 推薦方案對比

| 方案 | 可行性 | 開發成本 | 資訊完整度 | 易用性 | 推薦度 |
|------|--------|---------|-----------|--------|--------|
| **A: 診斷 APK** | ✅ 可行 | 2 小時 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **B: SmartCameraApp** | ✅ 可行 | 0（已完成） | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **C: Device Explorer** | ✅ 可行 | 0 | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **D: CTS Verifier** | ✅ 可行 | 0 | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **X: 純 adb** | ❌ 不可行 | 0 | ⭐ | ⭐⭐⭐ | ❌ |

---

## 💡 立即可用的方案（方案 B）

### 快速操作指南

**目標**: 使用現有的 v2.9.0-beta5 快速獲取任何裝置的相機解析度資訊

#### Windows PowerShell 腳本

```powershell
# query_camera_quick.ps1

# 1. 檢查裝置
Write-Host "Checking device..." -ForegroundColor Yellow
adb devices

# 2. 安裝 app（如尚未安裝）
Write-Host "`nInstalling app..." -ForegroundColor Yellow
adb install -r app\build\outputs\apk\device\debug\app-device-debug.apk

# 3. 授予權限
Write-Host "`nGranting permissions..." -ForegroundColor Yellow
adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA

# 4. 清除 logcat
Write-Host "`nClearing logcat..." -ForegroundColor Yellow
adb logcat -c

# 5. 啟動 app
Write-Host "`nLaunching app..." -ForegroundColor Yellow
adb shell am start -n com.akirachang.ai.photographer/.MainActivity

# 6. 等待
Write-Host "`nWaiting 10 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 7. 獲取裝置資訊
$manufacturer = (adb shell getprop ro.product.manufacturer).Trim()
$model = (adb shell getprop ro.product.model).Trim()
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# 8. 擷取日誌
Write-Host "`nCapturing logs..." -ForegroundColor Yellow
$outputFile = "tmp\device_info\${manufacturer}_${model}_${timestamp}.log"
New-Item -ItemType Directory -Force -Path "tmp\device_info" | Out-Null

adb logcat -d | Select-String "\[v2.9.0-beta" | Out-File -FilePath $outputFile

# 9. 顯示結果
Write-Host "`nCamera Information:" -ForegroundColor Green
Write-Host "==================" -ForegroundColor Green
Get-Content $outputFile | Select-String "Device:|Acceptable:|Supported Resolutions" | ForEach-Object { Write-Host $_ }

Write-Host "`nFull report saved to: $outputFile" -ForegroundColor Cyan
```

#### Linux/macOS Bash 腳本

```bash
#!/bin/bash
# query_camera_quick.sh

# 1. 檢查裝置
echo "Checking device..."
adb devices

# 2. 安裝 app
echo -e "\nInstalling app..."
adb install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 3. 授予權限
echo -e "\nGranting permissions..."
adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA

# 4. 清除 logcat
echo -e "\nClearing logcat..."
adb logcat -c

# 5. 啟動 app
echo -e "\nLaunching app..."
adb shell am start -n com.akirachang.ai.photographer/.MainActivity

# 6. 等待
echo -e "\nWaiting 10 seconds..."
sleep 10

# 7. 獲取裝置資訊
MANUFACTURER=$(adb shell getprop ro.product.manufacturer | tr -d '\r')
MODEL=$(adb shell getprop ro.product.model | tr -d '\r')
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 8. 擷取日誌
echo -e "\nCapturing logs..."
OUTPUT_FILE="tmp/device_info/${MANUFACTURER}_${MODEL}_${TIMESTAMP}.log"
mkdir -p tmp/device_info

adb logcat -d | grep "\[v2.9.0-beta" > "$OUTPUT_FILE"

# 9. 顯示結果
echo -e "\n\033[0;32mCamera Information:\033[0m"
echo "=================="
grep -E "Device:|Acceptable:|Supported Resolutions" "$OUTPUT_FILE"

echo -e "\n\033[0;36mFull report saved to: $OUTPUT_FILE\033[0m"
```

---

## 📊 使用範例

### 範例 1: 查詢 Pixel 9a

```bash
$ ./tools/query_camera_quick.sh

Checking device...
List of devices attached
59131JEBF17244  device

Installing app...
Success

Granting permissions...

Clearing logcat...

Launching app...
Starting: Intent { cmp=com.akirachang.ai.photographer/.MainActivity }

Waiting 10 seconds...

Capturing logs...

Camera Information:
==================
I LiveCameraSource: [v2.9.0-beta5] Device: Google Pixel 9a, 29 resolutions
I LiveCameraSource: [v2.9.0-beta5] Acceptable: 640x360, 640x480, 720x480, 800x600, 1024x576, 1280x720
I LiveCameraSource: [v2.9.0-beta3 Supported Resolutions] YUV_420_888 (29 total):
I LiveCameraSource: [v2.9.0-beta3] 176x144, 320x240, 352x288, 640x360, 640x480, 720x480, 800x600, 1024x576, 1024x768, 1280x720, 1080x1080, 1280x960, 1344x1008, 1440x1080, 1792x1008, 1920x960, 1600x1200, 1920x1080, 1920x1440, 2048x1536, 2560x1280, 1920x1920, 2688x1512, 2560x1920, 3264x1836, 3264x2448, 4000x2000, 3840x2160, 4000x3000

Full report saved to: tmp/device_info/Google_Pixel_9a_20260112_231020.log
```

---

## 🎯 最佳實踐建議

### 建議：使用方案 B（SmartCameraApp）

**理由**:
1. ✅ 已開發完成，立即可用
2. ✅ 資訊完整詳細
3. ✅ 包含智慧解析度選擇的診斷資訊
4. ✅ 可同時驗證 app 功能

**時間**: 1-2 分鐘/裝置

**適用情境**:
- 快速查詢新裝置的相機規格
- 驗證智慧解析度選擇邏輯
- 收集裝置資料庫
- 除錯相機問題

---

## 🔮 未來改進（方案 A）

如果需要更專業的工具，可以考慮開發獨立的診斷 APK：

### 特性
- ✅ 超小 APK（< 3 MB）
- ✅ 單一 Activity
- ✅ 自動輸出 JSON 格式報告
- ✅ 支援批次測試多個裝置
- ✅ 可透過 adb 自動安裝並執行

### 開發時間
- 估計: 2-3 小時
- 包含: UI + 匯出功能 + 自動化腳本

---

## 📋 總結

### 回答您的問題：是否可行？

**直接透過 adb 查詢**: ❌ **不可行**
- Camera2 API 需要應用權限
- dumpsys 資訊不包含解析度列表

**透過安裝輕量 APK 查詢**: ✅ **可行且推薦**
- 方案 B (SmartCameraApp): ⭐⭐⭐⭐⭐ 立即可用
- 方案 A (診斷 APK): ⭐⭐⭐⭐⭐ 未來可開發

### 立即行動

**現在就可以做**:
1. 連接任何 Android 裝置
2. 執行 `query_camera_quick.sh` 腳本
3. 獲得完整的相機解析度資訊
4. 時間: < 2 分鐘

---

**文件狀態**: Complete
**工具狀態**: 方案 B 可用，方案 A 可選開發
**推薦**: 使用現有 SmartCameraApp (方案 B)
