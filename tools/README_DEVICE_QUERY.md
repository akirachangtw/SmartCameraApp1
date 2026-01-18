# Device Camera Query Tools

快速查詢 Android 實體裝置的相機硬體支援解析度資訊

---

## 🎯 目的

透過遠端連接實體手機，快速獲取：
- 裝置製造商與型號
- 相機支援的所有解析度列表
- SmartCameraApp 選擇的策略與目標解析度
- 過濾後的可用解析度

**用途**:
- 驗證智慧解析度選擇機制
- 測試新裝置的相機規格
- 收集裝置資料庫
- 除錯相機相關問題

---

## 📦 可用工具

### 1. query_camera_quick.ps1 (推薦) ⭐⭐⭐⭐⭐

**Windows PowerShell 腳本**，一鍵獲取完整相機資訊

**特點**:
- ✅ 全自動化流程
- ✅ 彩色輸出，易讀
- ✅ 生成詳細報告檔案
- ✅ 支援跳過安裝（快速模式）

**使用方式**:

```powershell
# 完整模式（會安裝 app）
.\tools\query_camera_quick.ps1

# 快速模式（使用已安裝的 app）
.\tools\query_camera_quick.ps1 -SkipInstall
```

**執行時間**: 20-30 秒（快速模式：10-15 秒）

**輸出檔案**: `tmp/device_info/{Manufacturer}_{Model}_{Timestamp}.log`

---

### 2. query_device_camera_info.sh

**Linux/macOS Bash 腳本**，功能同上

**使用方式**:

```bash
# 給予執行權限（首次）
chmod +x tools/query_device_camera_info.sh

# 執行
./tools/query_device_camera_info.sh
```

---

## 🚀 快速開始

### 前置需求

1. **adb 已安裝**: Android SDK Platform Tools
2. **裝置已連接**: USB 除錯已啟用
3. **裝置已授權**: 在裝置上點選「允許 USB 除錯」

### 檢查環境

```powershell
# 檢查 adb
adb version

# 檢查連接的裝置
adb devices
```

應顯示類似：
```
List of devices attached
HQ616R2713    device
```

### 執行查詢

```powershell
# Windows
cd SmartCameraApp1
.\tools\query_camera_quick.ps1 -SkipInstall

# Linux/macOS
cd SmartCameraApp1
./tools/query_device_camera_info.sh
```

---

## 📊 輸出範例

### Console 輸出

```
======================================
  Camera Resolution Query Tool
  Using SmartCameraApp v2.9.0-beta5
======================================

[1/8] Checking adb...
      adb found
[2/8] Checking connected devices...
      Device connected
[3/8] Getting device info...
      Manufacturer: Sony
      Model: XQ-BT52
      Android: 12 (API 31)
[4/8] Skipping installation (--SkipInstall)
[5/8] Granting camera permission...
      Permission granted
[6/8] Launching app and capturing logs...
      App launched, waiting 10 seconds...
[7/8] Extracting camera info...
      Logs captured
[8/8] Results:

======================================
  Camera Information
======================================

Device Detection:
  Sony XQ-BT52, 21 resolutions

Supported Resolutions:
  YUV_420_888 (21 total)

Resolution List:
  176x144, 320x240, 240x320, 352x288, 640x480, 720x480,
  720x720, 1280x720, 720x1280, 1080x1080, 1680x720,
  1440x1080, 1920x1080, 2520x1080, 1920x1440, 3264x1400,
  2448x2448, 3264x1840, 3840x1644, 4032x1728, 3840x2160

Acceptable Resolutions (480x360-1280x720):
  640x480, 720x480, 720x720, 1280x720

Strategy Applied:
  Sony: 640x480

Final Selection:
  target=640x480, filter≤800x600

Filtered Resolutions:
  640x480, 320x240, 352x288, 176x144, 720x480, 240x320

======================================

Summary:
  Device: Sony XQ-BT52
  Android: 12 (API 31)
  Supported Resolutions: 21
  Strategy: Sony
  Selected Resolution: 640x480

Done!
```

### 檔案輸出

**位置**: `tmp/device_info/Sony_XQ-BT52_20260113_113339.log`

**內容**: 完整的 logcat 日誌，包含所有 `[v2.9.0-beta5]` 標籤的資訊

---

## 🎓 理解輸出結果

### 關鍵資訊說明

#### 1. Device Detection
```
Device: Sony XQ-BT52, 21 resolutions
```
- 裝置識別成功
- 查詢到 21 個支援的解析度

#### 2. Supported Resolutions
```
YUV_420_888 (21 total)
```
- 相機支援的格式
- 總共 21 個可用解析度

#### 3. Resolution List
```
176x144, 320x240, ..., 3840x2160
```
- 完整的支援解析度列表
- 從最小到最大排序

#### 4. Acceptable Resolutions
```
640x480, 720x480, 720x720, 1280x720
```
- 在 480x360 ~ 1280x720 範圍內的解析度
- 這些是候選解析度

#### 5. Strategy Applied
```
Sony: 640x480
或
Pixel: 800x600
或
Generic: 640x480
```
- 套用的裝置策略
- 選擇的目標解析度

#### 6. Final Selection
```
target=640x480, filter≤800x600
```
- **target**: CameraX 的目標解析度
- **filter**: ResolutionFilter 的上限

#### 7. Filtered Resolutions
```
640x480, 320x240, 352x288, 176x144, 720x480, 240x320
```
- 經過 ResolutionFilter 過濾後的可用解析度
- CameraX 從這些選項中選擇最接近 target 的

---

## 🔧 進階用法

### 批次測試多個裝置

```powershell
# 1. 連接裝置 A
adb -s DEVICE_A_SERIAL ...

# 2. 執行查詢
$env:ANDROID_SERIAL="DEVICE_A_SERIAL"
.\tools\query_camera_quick.ps1 -SkipInstall

# 3. 切換裝置 B
$env:ANDROID_SERIAL="DEVICE_B_SERIAL"
.\tools\query_camera_quick.ps1 -SkipInstall

# 4. 比較結果
Compare-Object (Get-Content tmp/device_info/DeviceA_*.log) (Get-Content tmp/device_info/DeviceB_*.log)
```

### 自動化測試腳本

```powershell
# test_all_devices.ps1
$devices = @("HQ616R2713", "59131JEBF17244")

foreach ($device in $devices) {
    Write-Host "Testing device: $device" -ForegroundColor Cyan
    $env:ANDROID_SERIAL = $device
    .\tools\query_camera_quick.ps1 -SkipInstall
    Write-Host ""
}

# 生成比較報告
Write-Host "Generating comparison report..." -ForegroundColor Yellow
# ... (自訂比較邏輯)
```

---

## 📂 輸出檔案結構

```
tmp/device_info/
├── Sony_XQ-BT52_20260113_113339.log          # Sony 裝置日誌
├── Google_Pixel_9a_20260112_225631.log       # Pixel 裝置日誌
├── Samsung_SM-G998B_20260113_140512.log      # Samsung 裝置日誌
└── DEVICE_CAMERA_SUMMARY.md                  # 總結報告（手動建立）
```

---

## 🐛 常見問題

### Q1: "adb not found"
**A**: 安裝 Android SDK Platform Tools 並加入 PATH

### Q2: "No devices connected"
**A**:
1. 確認裝置已連接
2. 確認 USB 除錯已啟用
3. 在裝置上授權電腦

### Q3: "Multiple devices connected"
**A**: 使用 `$env:ANDROID_SERIAL="DEVICE_ID"` 指定裝置

### Q4: 輸出顯示 "33" 而非實際資訊
**A**: 這是 PowerShell 字串解析問題，檢查日誌檔案有完整資訊

### Q5: App 啟動後沒有日誌
**A**:
1. 確認 app 版本為 v2.9.0-beta5
2. 手動啟動 app 確認運作正常
3. 延長等待時間（修改 `Start-Sleep -Seconds 10` → 15）

---

## 🔍 驗證結果正確性

### 檢查清單

- [ ] Device Detection 顯示正確的製造商與型號
- [ ] Supported Resolutions 數量 > 0
- [ ] Strategy 為 Sony/Pixel/Generic 之一
- [ ] Final Selection 的解析度合理（480x360 ~ 1280x720）
- [ ] Filtered Resolutions 包含 Final Selection 的 target

### 預期結果對照

| 裝置 | 策略 | Target | 範例 |
|------|------|--------|------|
| Sony XQ-BT52 | Sony | 640x480 | ✅ 正確 |
| Pixel 9a | Pixel | 800x600 | ✅ 正確 |
| Pixel 8 | Pixel | 800x600 | ✅ 正確 |
| Samsung S21 | Generic | 640x480 或 800x600 | ✅ 正確 |

---

## 📚 相關文件

### 開發文件
- [REMOTE_DEVICE_CAMERA_QUERY_GUIDE.md](../docs/REMOTE_DEVICE_CAMERA_QUERY_GUIDE.md) - 詳細技術指南
- [V2.9.0-BETA5_SMART_RESOLUTION_IMPLEMENTATION.md](../docs/V2.9.0-BETA5_SMART_RESOLUTION_IMPLEMENTATION.md) - 實作細節

### 測試報告
- [V2.9.0-BETA5_PIXEL9A_TEST_REPORT.md](../docs/V2.9.0-BETA5_PIXEL9A_TEST_REPORT.md) - Pixel 9a 測試
- [V2.9.0-BETA5_FINAL_SUMMARY.md](../docs/V2.9.0-BETA5_FINAL_SUMMARY.md) - 跨裝置總結

### 效能比較
- [V2.9.0-BETA5_VS_V2.8.6_PERFORMANCE_COMPARISON.md](../docs/V2.9.0-BETA5_VS_V2.8.6_PERFORMANCE_COMPARISON.md)

---

## 💡 提示與技巧

### 快速比較兩個裝置

```powershell
# 提取關鍵資訊
$sony = Get-Content tmp/device_info/Sony_*.log | Select-String "Strategy|Final Selection"
$pixel = Get-Content tmp/device_info/Google_Pixel*.log | Select-String "Strategy|Final Selection"

Write-Host "Sony:" -ForegroundColor Yellow
$sony
Write-Host "`nPixel:" -ForegroundColor Yellow
$pixel
```

### 建立裝置資料庫

收集多個裝置的資訊後，可以建立一個資料庫：

```json
{
  "devices": [
    {
      "manufacturer": "Sony",
      "model": "XQ-BT52",
      "total_resolutions": 21,
      "acceptable": ["640x480", "720x480", "720x720", "1280x720"],
      "strategy": "Sony",
      "target": "640x480"
    },
    {
      "manufacturer": "Google",
      "model": "Pixel 9a",
      "total_resolutions": 29,
      "acceptable": ["640x360", "640x480", "720x480", "800x600", "1024x576", "1280x720"],
      "strategy": "Pixel",
      "target": "800x600"
    }
  ]
}
```

---

## 📞 支援

如有問題或建議，請：
1. 檢查 [常見問題](#常見問題)
2. 查閱 [相關文件](#相關文件)
3. 查看 logcat 完整日誌
4. 提交 Issue 到專案倉庫

---

**工具版本**: v1.0
**最後更新**: 2026-01-13
**維護者**: SmartCameraApp 開發團隊
