# v2.9.6 Phase 1 快速指令參考

**用途**: Work NB 測試執行快速查詢手冊
**適用環境**: Windows PowerShell + ADB
**最後更新**: 2026-01-16

---

## 📱 裝置連接與資訊查詢

### 檢查連接裝置

```bash
# List all connected devices
adb devices

# List with detailed info
adb devices -l

# Expected output format:
# HQ616R2713    device product:XQ-BT52 model:XQ_BT52 device:XQ-BT52
```

### 查詢裝置資訊

```bash
# Get device model
adb shell getprop ro.product.model

# Get Android version
adb shell getprop ro.build.version.release

# Get device serial number
adb shell getprop ro.serialno

# Get device manufacturer
adb shell getprop ro.product.manufacturer

# Get full device info
adb shell getprop | Select-String -Pattern "ro.product|ro.build.version"
```

### 設置預設裝置

```powershell
# PowerShell: Set environment variable
$env:ANDROID_SERIAL = "HQ616R2713"

# Verify
echo $env:ANDROID_SERIAL

# All subsequent adb commands will use this device automatically
# (No need to use -s flag)
```

### ADB Server 管理

```bash
# Restart ADB server (fix connection issues)
adb kill-server
adb start-server

# Check ADB version
adb version
```

---

## 📦 應用管理

### 安裝與卸載

```bash
# Install APK (replace if exists)
adb install -r path\to\app-debug.apk

# Install to specific device
adb -s HQ616R2713 install -r app-debug.apk

# Uninstall app
adb uninstall com.akirachang.ai.photographer

# Uninstall but keep data
adb uninstall -k com.akirachang.ai.photographer
```

### 應用狀態查詢

```bash
# Check if app is installed
adb shell pm list packages | Select-String "akirachang"

# Get app version
adb shell dumpsys package com.akirachang.ai.photographer | Select-String "versionName"

# Check if app is running
adb shell pidof com.akirachang.ai.photographer

# Get app process info
adb shell ps | Select-String "akirachang"
```

### 應用啟動與停止

```bash
# Launch MainActivity
adb shell am start -n com.akirachang.ai.photographer/.MainActivity

# Launch with intent extras
adb shell am start -n com.akirachang.ai.photographer/.MainActivity --es "extra_key" "extra_value"

# Force stop app
adb shell am force-stop com.akirachang.ai.photographer

# Clear app data
adb shell pm clear com.akirachang.ai.photographer
```

### 應用權限管理

```bash
# List all permissions
adb shell dumpsys package com.akirachang.ai.photographer | Select-String "permission"

# Grant camera permission
adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA

# Grant storage permission
adb shell pm grant com.akirachang.ai.photographer android.permission.WRITE_EXTERNAL_STORAGE
adb shell pm grant com.akirachang.ai.photographer android.permission.READ_EXTERNAL_STORAGE
```

---

## 📝 Logcat 操作

### 基本 Logcat 指令

```bash
# Clear logcat buffer
adb logcat -c

# View live logcat
adb logcat

# View logcat from specific device
adb -s HQ616R2713 logcat

# Save logcat to file
adb logcat > logcat_output.txt

# Save last 1000 lines
adb logcat -t 1000 > logcat_last_1000.txt
```

### Logcat 過濾

```bash
# Filter by tag
adb logcat -s LiveCameraSource:*

# Filter multiple tags
adb logcat -s LiveCameraSource:* CameraViewModel:*

# Filter by priority (D=Debug, I=Info, W=Warning, E=Error)
adb logcat LiveCameraSource:D *:S

# PowerShell: Filter with Select-String
adb logcat | Select-String -Pattern "LiveCameraSource|Orientation"

# Grep-style filter (case-insensitive)
adb logcat | Select-String -Pattern "error" -CaseSensitive:$false
```

### Logcat 進階用法

```bash
# Export logcat dump (all buffered logs)
adb logcat -d > logcat_dump.txt

# Monitor logcat in background (PowerShell Job)
$Job = Start-Job -ScriptBlock {
    param($Device, $OutputFile)
    adb -s $Device logcat > $OutputFile
} -ArgumentList "HQ616R2713", "logcat_bg.txt"

# Check job status
Get-Job

# Stop background logcat
Stop-Job -Job $Job
Remove-Job -Job $Job

# View logcat with timestamps
adb logcat -v time

# View logcat with thread info
adb logcat -v threadtime
```

---

## 📷 相機與拍照操作

### UI 自動化 - 點擊操作

```bash
# Tap at coordinates (X, Y)
adb shell input tap 540 2100

# Long press (duration in ms)
adb shell input swipe 540 2100 540 2100 500

# Swipe (from X1,Y1 to X2,Y2 in duration ms)
adb shell input swipe 540 1000 540 500 300
```

### UI 結構分析

```bash
# Dump UI hierarchy
adb shell uiautomator dump

# Pull UI dump to PC
adb pull /sdcard/window_dump.xml .

# View file in PowerShell
Get-Content window_dump.xml

# Search for specific UI element
Get-Content window_dump.xml | Select-String -Pattern "FloatingActionButton"
```

### 快門按鈕座標查找

```xml
<!-- Example UI dump search pattern -->
<!-- Look for: -->
<node class="android.widget.FloatingActionButton"
      bounds="[504,2074][576,2146]"
      content-desc="Capture Photo" />

<!-- Calculate center coordinates: -->
<!-- X = (504 + 576) / 2 = 540 -->
<!-- Y = (2074 + 2146) / 2 = 2110 -->
```

```bash
# Test tap on calculated coordinates
adb shell input tap 540 2110
```

---

## 🔄 裝置方向控制

### 方向設定指令

```bash
# Portrait (0°) - Home button at bottom
adb shell settings put system user_rotation 0

# Landscape Right (90°) - Home button on left
adb shell settings put system user_rotation 1

# Upside Down (180°) - Home button at top
adb shell settings put system user_rotation 2

# Landscape Left (270°) - Home button on right
adb shell settings put system user_rotation 3
```

### 方向對應表

| user_rotation | Physical Rotation | Home Button Position | EXIF Expected |
|--------------|-------------------|---------------------|---------------|
| 0 | 0° (Portrait) | Bottom | 1 (Normal) |
| 1 | 90° (Landscape Right) | Left | 8 (Rotate 90° CCW) |
| 2 | 180° (Upside Down) | Top | 3 (Rotate 180°) |
| 3 | 270° (Landscape Left) | Right | 6 (Rotate 90° CW) |

### 查詢當前方向

```bash
# Get current rotation setting
adb shell settings get system user_rotation

# Get accelerometer rotation status (0=off, 1=on)
adb shell settings get system accelerometer_rotation
```

### 啟用/禁用自動旋轉

```bash
# Disable auto-rotation (lock orientation)
adb shell settings put system accelerometer_rotation 0

# Enable auto-rotation
adb shell settings put system accelerometer_rotation 1
```

---

## 📂 檔案管理

### 瀏覽檔案系統

```bash
# List files in directory
adb shell ls /sdcard/DCIM/SmartCamera/

# List with details (size, date)
adb shell ls -lh /sdcard/DCIM/SmartCamera/

# List sorted by time (newest first)
adb shell ls -lt /sdcard/DCIM/SmartCamera/

# Count files in directory
adb shell "ls /sdcard/DCIM/SmartCamera/*.jpg | wc -l"
```

### 檔案傳輸

```bash
# Pull single file
adb pull /sdcard/DCIM/SmartCamera/photo.jpg .

# Pull entire directory
adb pull /sdcard/DCIM/SmartCamera/ local_photos/

# Pull with specific device
adb -s HQ616R2713 pull /sdcard/DCIM/SmartCamera/ .

# Push file to device
adb push local_file.txt /sdcard/Download/
```

### 檔案操作

```bash
# Create directory
adb shell mkdir -p /sdcard/TestDir

# Delete file
adb shell rm /sdcard/DCIM/SmartCamera/photo.jpg

# Delete all jpg files
adb shell "rm /sdcard/DCIM/SmartCamera/*.jpg"

# Delete directory recursively
adb shell rm -rf /sdcard/TestDir

# Copy file on device
adb shell cp /sdcard/source.jpg /sdcard/dest.jpg

# Move file on device
adb shell mv /sdcard/old.jpg /sdcard/new.jpg
```

### 照片查找

```bash
# Find recent photos (last 10 minutes)
adb shell "find /sdcard -name '*.jpg' -type f -mmin -10"

# Find large photos (> 1MB)
adb shell "find /sdcard/DCIM -name '*.jpg' -type f -size +1M"

# Count total photos
adb shell "find /sdcard/DCIM -name '*.jpg' | wc -l"
```

---

## 🖼️ 截圖操作

### 正確的截圖方法

```bash
# Step 1: Capture screenshot on device
adb shell "screencap -p > /sdcard/Download/screenshot.png"

# Step 2: Pull to PC
adb pull /sdcard/Download/screenshot.png tmp/screenshot.png

# Step 3: Clean up
adb shell "rm /sdcard/Download/screenshot.png"
```

### ⚠️ 錯誤方法（會導致 PNG 損壞）

```bash
# ❌ NEVER USE THIS (causes line-ending corruption):
adb shell screencap -p > screenshot.png

# ❌ NEVER USE THIS (PowerShell encoding issues):
adb shell screencap -p | Out-File screenshot.png
```

### 連續截圖腳本範例

```powershell
# Take 5 screenshots with 2-second intervals
for ($i = 1; $i -le 5; $i++) {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    adb shell "screencap -p > /sdcard/Download/screen_$timestamp.png"
    adb pull "/sdcard/Download/screen_$timestamp.png" "tmp/screenshots/"
    adb shell "rm /sdcard/Download/screen_$timestamp.png"
    Start-Sleep -Seconds 2
}
```

---

## 🔬 EXIF 分析

### Python EXIF 分析腳本

```bash
# Analyze single photo
python tools/analyze_exif.py path/to/photo.jpg

# Analyze entire directory
python tools/analyze_exif.py tmp/phase1_photos/

# Save output to file
python tools/analyze_exif.py tmp/phase1_photos/ > exif_report.txt
```

### EXIF Orientation 對照表

| EXIF Value | Description | Physical Rotation |
|-----------|-------------|-------------------|
| 1 | Normal (0°) | Portrait, Home at bottom |
| 2 | Mirrored | Horizontal flip |
| 3 | Rotated 180° | Upside down |
| 4 | Mirrored & Rotated 180° | Vertical flip |
| 5 | Mirrored & Rotated 90° CW | - |
| 6 | Rotated 90° CW | Landscape Right |
| 7 | Mirrored & Rotated 90° CCW | - |
| 8 | Rotated 90° CCW | Landscape Left |

### 安裝 EXIF 分析依賴

```bash
# Check Python version
python --version

# Install Pillow library
pip install pillow

# Or use pip3
pip3 install pillow

# Verify installation
pip list | Select-String pillow
```

---

## 🛠️ 系統資訊與除錯

### 系統資訊查詢

```bash
# Get device properties
adb shell getprop

# Get CPU info
adb shell cat /proc/cpuinfo

# Get memory info
adb shell cat /proc/meminfo

# Get battery info
adb shell dumpsys battery

# Get screen info
adb shell wm size
adb shell wm density
```

### 應用除錯資訊

```bash
# Get app memory usage
adb shell dumpsys meminfo com.akirachang.ai.photographer

# Get app CPU usage
adb shell top -n 1 | Select-String "akirachang"

# Get app thread info
adb shell ps -t | Select-String "akirachang"

# Get app stack trace (if crashed)
adb shell dumpsys dropbox --print | Select-String "akirachang"
```

### 效能監控

```bash
# Monitor FPS (requires Android 6.0+)
adb shell dumpsys gfxinfo com.akirachang.ai.photographer framestats

# Monitor GPU rendering
adb shell dumpsys gfxinfo com.akirachang.ai.photographer

# Monitor network usage
adb shell dumpsys netstats | Select-String "akirachang"
```

---

## 📦 測試資料管理

### 建立測試目錄結構

```powershell
# Create timestamped test directory
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$TEST_DIR = "tmp\v2.9.6_phase1_$TIMESTAMP"
New-Item -ItemType Directory -Force -Path "$TEST_DIR\photos"
New-Item -ItemType Directory -Force -Path "$TEST_DIR\logcat"
New-Item -ItemType Directory -Force -Path "$TEST_DIR\screenshots"

Write-Host "Test directory created: $TEST_DIR"
```

### 批次拉取測試資料

```powershell
# Pull photos
adb pull /sdcard/DCIM/SmartCamera/ "$TEST_DIR\photos\"

# Pull logcat
adb logcat -d > "$TEST_DIR\logcat\logcat_dump.txt"

# Pull crash logs
adb pull /sdcard/Android/data/com.akirachang.ai.photographer/files/logs/ "$TEST_DIR\logs\"
```

### 壓縮測試資料

```powershell
# Compress test directory
Compress-Archive -Path $TEST_DIR -DestinationPath "$TEST_DIR.zip"

Write-Host "Test data archived: $TEST_DIR.zip"
```

---

## 🔐 權限管理

### 查詢應用權限

```bash
# List all permissions
adb shell dumpsys package com.akirachang.ai.photographer | Select-String "permission"

# Check specific permission
adb shell dumpsys package com.akirachang.ai.photographer | Select-String "CAMERA"
```

### 授予權限

```bash
# Camera permission
adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA

# Storage permissions
adb shell pm grant com.akirachang.ai.photographer android.permission.WRITE_EXTERNAL_STORAGE
adb shell pm grant com.akirachang.ai.photographer android.permission.READ_EXTERNAL_STORAGE

# Location permissions (if needed)
adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_COARSE_LOCATION
```

### 撤銷權限

```bash
# Revoke camera permission
adb shell pm revoke com.akirachang.ai.photographer android.permission.CAMERA
```

---

## 🚨 故障排除快速指令

### ADB 連接問題

```bash
# Full reset
adb kill-server
adb start-server
adb devices

# Check USB connection
adb usb

# Reconnect over USB
adb reconnect
```

### 應用無響應

```bash
# Force stop
adb shell am force-stop com.akirachang.ai.photographer

# Clear app data and restart
adb shell pm clear com.akirachang.ai.photographer
adb shell am start -n com.akirachang.ai.photographer/.MainActivity
```

### 照片找不到

```bash
# Search all possible locations
adb shell "find /sdcard -name '*.jpg' -type f -mmin -30"

# Check app-specific directory
adb shell ls /sdcard/Android/data/com.akirachang.ai.photographer/files/

# Check DCIM directories
adb shell ls /sdcard/DCIM/
```

### Logcat 無輸出

```bash
# Clear and restart logcat
adb logcat -c
adb logcat -v time

# Check if app is running
adb shell pidof com.akirachang.ai.photographer

# Restart app
adb shell am force-stop com.akirachang.ai.photographer
adb shell am start -n com.akirachang.ai.photographer/.MainActivity
```

---

## ⚡ 快速測試流程範本

### 完整測試腳本範例

```powershell
# v2.9.6 Phase 1 Quick Test Script
$DEVICE = "HQ616R2713"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$TEST_DIR = "tmp\phase1_test_$TIMESTAMP"

# 1. Setup
New-Item -ItemType Directory -Force -Path $TEST_DIR
adb -s $DEVICE logcat -c

# 2. Launch app
adb -s $DEVICE shell am start -n com.akirachang.ai.photographer/.MainActivity
Start-Sleep -Seconds 3

# 3. Capture logcat
adb -s $DEVICE logcat -d > "$TEST_DIR\logcat.txt"

# 4. Take photos
adb -s $DEVICE shell settings put system user_rotation 0
adb -s $DEVICE shell input tap 540 2100
Start-Sleep -Seconds 2

adb -s $DEVICE shell settings put system user_rotation 3
adb -s $DEVICE shell input tap 540 2100
Start-Sleep -Seconds 2

adb -s $DEVICE shell settings put system user_rotation 1
adb -s $DEVICE shell input tap 540 2100
Start-Sleep -Seconds 2

# 5. Pull photos
adb -s $DEVICE pull /sdcard/DCIM/SmartCamera/ "$TEST_DIR\photos\"

# 6. Analyze EXIF
python tools/analyze_exif.py "$TEST_DIR\photos\" > "$TEST_DIR\exif_report.txt"

Write-Host "✓ Test completed: $TEST_DIR"
```

---

## 📚 參考資料

### ADB 官方文檔
- [Android Debug Bridge (adb)](https://developer.android.com/studio/command-line/adb)

### PowerShell 常用指令
```powershell
# Get command help
Get-Help <command>

# List files
Get-ChildItem

# Search text in files
Select-String -Path "file.txt" -Pattern "search_term"

# Create directory
New-Item -ItemType Directory -Path "path"

# Delete directory
Remove-Item -Recurse -Force "path"
```

---

**文檔版本**: 1.0
**最後更新**: 2026-01-16
**維護者**: Claude Code Assistant (Home NB)
**適用專案**: SmartCameraApp1 v2.9.6
