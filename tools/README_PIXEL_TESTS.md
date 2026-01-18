# Pixel 測試工具使用指南

本目錄包含用於 Pixel 裝置測試的 PowerShell 腳本,專門驗證 v2.8.4 的效能改善。

---

## 🎯 快速開始

### 1. 記憶體測試 (推薦先執行)

**目的**: 驗證 v2.8.4 是否解決 Pixel 9a 的 +79MB 記憶體增長問題

**執行**:
```powershell
.\tools\pixel_memory_test.ps1 -DurationMinutes 10
```

**預期時間**: 10-15 分鐘

**輸出**:
- `tmp\pixel_memory_test\pixel_memory_YYYYMMDD_HHMMSS.log` - 詳細日誌
- `tmp\pixel_memory_test\pixel_memory_YYYYMMDD_HHMMSS.csv` - CSV 數據
- `tmp\pixel_memory_test\pixel_memory_YYYYMMDD_HHMMSS.logcat` - 完整 Logcat

**成功標準**:
- ✅ Growth < 50MB (優秀)
- ✅ Growth < 79MB (改善)
- ⚠️ Growth ~79MB (持平)
- ❌ Growth > 79MB (退化)

---

## 📊 測試腳本說明

### pixel_memory_test.ps1

**功能**:
- 每 30 秒採樣記憶體使用
- 監控 Native Heap、Java Heap、Total PSS
- 自動計算 vs v2.8.0 的改善幅度
- 檢查 BitmapPool 統計 (Keys, Hit Rate)
- 生成詳細報告和 CSV 數據

**參數**:
```powershell
-DurationMinutes <int>  # 測試時長 (預設: 10)
-PackageName <string>   # App package name (預設: com.akirachang.ai.photographer)
-OutputDir <string>     # 輸出目錄 (預設: tmp\pixel_memory_test)
```

**範例**:
```powershell
# 3 分鐘快速測試
.\tools\pixel_memory_test.ps1 -DurationMinutes 3

# 10 分鐘完整測試 (推薦)
.\tools\pixel_memory_test.ps1 -DurationMinutes 10

# 20 分鐘長期穩定性測試
.\tools\pixel_memory_test.ps1 -DurationMinutes 20
```

---

## 🔍 測試前準備

### 1. 連接 Pixel 裝置

```powershell
# 檢查連接
adb devices

# 應該看到:
# List of devices attached
# <device-id>    device
```

### 2. 建置並安裝 App

```powershell
# 建置 Release build (重要!)
.\gradlew assembleDeviceRelease

# 安裝到裝置
adb install -r app\build\outputs\apk\device\release\app-device-release.apk
```

### 3. 設置測試環境

**重要事項**:
- ✅ 開啟桌燈 (良好光線條件)
- ✅ 關閉其他背景 App
- ✅ 裝置放置穩定,不移動
- ✅ 不要在測試期間操作裝置

**檢查清單**:
```powershell
# 1. 確認裝置型號
adb shell getprop ro.product.model
# 預期: Pixel 9a

# 2. 確認 Android 版本
adb shell getprop ro.build.version.release
# 預期: 16

# 3. 確認沒有其他 App 干擾
adb shell ps | Select-String "photographer"
```

---

## 📈 讀取測試結果

### 即時監控

測試執行期間,腳本會即時顯示:

```
Time  | Native  | Java   | Total  | Growth | Status
------|---------|--------|--------|--------|--------
0.5 m | 124.5 MB| 31.2 MB| 502.3 MB| +0.0 MB| ✅ OK
1.0 m | 125.1 MB| 31.8 MB| 510.7 MB| +8.4 MB| ✅ OK
1.5 m | 126.3 MB| 32.1 MB| 518.2 MB| +15.9 MB| ✅ OK
...
```

**顏色說明**:
- 🟢 綠色: Growth < 50MB (優秀)
- 🟡 黃色: Growth 50-79MB (警告)
- 🔴 紅色: Growth > 79MB (高於 v2.8.0)

### 測試摘要

測試完成後會顯示:

```
=====================================
Test Summary
=====================================
Duration: 10.0 minutes
Samples: 20

Memory Statistics:
  Baseline: 502.3 MB
  Final: 548.7 MB
  Max: 550.2 MB
  Min: 501.8 MB
  Growth: +46.4 MB

Component Breakdown:
  Native Heap: 124.5 MB → 145.2 MB (+20.7 MB)
  Java Heap: 31.2 MB → 35.8 MB (+4.6 MB)

Comparison with v2.8.0:
  v2.8.0 (3 min): +79 MB
  v2.8.4 (3 min normalized): +13.9 MB
  Improvement: -65.1 MB (-82.4%)

Verdict:
  Growth rate: +4.6 MB/min
  ✅ EXCELLENT: Memory growth well under control
```

### CSV 數據分析

CSV 檔案可用於繪製圖表:

```powershell
# 使用 Excel 或 Python 繪製記憶體趨勢圖
Import-Csv tmp\pixel_memory_test\pixel_memory_*.csv
```

---

## 🎓 解讀結果

### 情境 1: 記憶體問題已解決 ✅

**指標**:
- Growth: < 50MB
- Growth rate: < 5 MB/min
- BitmapPool Keys: 1
- BitmapPool Hit Rate: 99%

**結論**:
```
✅ EXCELLENT: Memory growth well under control
✅ RECOMMENDATION: v2.8.4 memory optimization is effective on Pixel
   Safe to proceed with v3.0 UI/UX development
```

**下一步**: 繼續 v3.0 UI/UX 開發

---

### 情境 2: 有改善但不顯著 ⚠️

**指標**:
- Growth: 50-79MB
- Growth rate: 5-8 MB/min
- BitmapPool Keys: 1-2
- BitmapPool Hit Rate: 95-99%

**結論**:
```
⚠️  ACCEPTABLE: Memory growth similar to v2.8.0
⚠️  RECOMMENDATION: v2.8.4 shows marginal improvement
   Monitor memory during v3.0 development
```

**下一步**:
1. 檢查 BitmapPool 日誌是否有異常
2. 執行更長時間測試 (20 分鐘)
3. v3.0 開發時密切監控記憶體

---

### 情境 3: 記憶體問題未改善或惡化 ❌

**指標**:
- Growth: > 79MB
- Growth rate: > 8 MB/min
- BitmapPool 可能異常

**結論**:
```
❌ CONCERN: Memory growth worse than v2.8.0
❌ RECOMMENDATION: Investigate v2.8.4 memory issue on Pixel
   Consider delaying v3.0 until memory is optimized
```

**下一步**:
1. 檢查 Logcat 是否有洩漏跡象
2. 使用 Android Studio Profiler 深入分析
3. 比較 v2.8.0 和 v2.8.4 的 Logcat 差異

---

## 🔧 故障排除

### 問題 1: 裝置連接失敗

```powershell
❌ No device connected!
```

**解決**:
```powershell
# 檢查 USB 連接
adb devices

# 重啟 adb server
adb kill-server
adb start-server

# 確認 USB 調試已開啟 (裝置設定)
```

---

### 問題 2: App 未安裝

```powershell
❌ App not installed!
```

**解決**:
```powershell
# 建置 Release APK
.\gradlew assembleDeviceRelease

# 安裝
adb install -r app\build\outputs\apk\device\release\app-device-release.apk
```

---

### 問題 3: App 未運行

```powershell
❌ App not running! Please start the app manually.
```

**解決**:
1. 手動啟動 App
2. 進入 Live Camera Mode
3. 授予相機權限
4. 按 Enter 繼續測試

---

### 問題 4: 記憶體資料無法取得

```powershell
⚠️  Failed to get memory info at X.X min
```

**可能原因**:
- App 崩潰或被系統殺掉
- USB 連接中斷
- ADB 權限問題

**檢查**:
```powershell
# 確認 App 仍在運行
adb shell pidof com.akirachang.ai.photographer

# 確認 USB 連接
adb devices

# 手動取得記憶體資料
adb shell dumpsys meminfo com.akirachang.ai.photographer
```

---

## 📊 v2.8.0 vs v2.8.4 對比

### v2.8.0 Pixel 9a 基準 (2026-01-06)

| 指標 | 數值 |
|------|------|
| Test Duration | 3:21 (201 秒) |
| Average FPS | 26.6 |
| Memory Baseline | 502MB |
| Memory Final | 581MB |
| **Memory Growth** | **+79MB** |
| Native Heap Growth | +84MB |
| Java Heap Growth | +20MB |
| Drop Rate | 0.00% |

**問題**: Memory Growth +79MB 較高 (超出 v2.8.0 目標)

### v2.8.4 預期改善

| 指標 | v2.8.0 | v2.8.4 目標 |
|------|--------|------------|
| Memory Growth (3 min) | +79MB | **< 50MB** |
| Growth Rate | ~24 MB/min | **< 5 MB/min** |
| BitmapPool Keys | N/A | **1** |
| BitmapPool Hit Rate | N/A | **99%** |
| Native Heap Growth | +84MB | **< 50MB** |

**關鍵改善**:
1. BitmapPool 優化 (Keys 10+ → 1, Hit Rate 99%)
2. 減少每幀記憶體分配
3. Paint 物件重用

---

## ✅ 測試檢查清單

### 測試前

- [ ] Pixel 裝置已連接 (`adb devices`)
- [ ] 裝置型號確認為 Pixel 9a
- [ ] Android 版本確認為 16
- [ ] Release build 已建置
- [ ] Release APK 已安裝
- [ ] 桌燈已開啟 (良好光線)
- [ ] 測試場景已準備 (穩定表面)
- [ ] 其他 App 已關閉

### 測試中

- [ ] App 已啟動
- [ ] 已進入 Live Camera Mode
- [ ] 相機權限已授予
- [ ] 光線條件良好 (目視確認)
- [ ] 腳本正在運行
- [ ] 即時數據正常顯示
- [ ] 未觸碰或移動裝置

### 測試後

- [ ] 測試摘要已顯示
- [ ] Verdict 已確認
- [ ] 輸出檔案已生成:
  - [ ] .log 檔案
  - [ ] .csv 檔案
  - [ ] .logcat 檔案
- [ ] 結果已記錄到測試報告

---

## 📞 需要協助?

如果測試遇到問題:

1. **檢查測試計劃**: `docs\V2.8.4_PIXEL_RETEST_PLAN.md`
2. **查看完整 Logcat**: `tmp\pixel_memory_test\*.logcat`
3. **檢查 CSV 數據**: 使用 Excel 或 Python 分析趨勢

---

**建立日期**: 2026-01-08
**版本**: v2.8.4
**用途**: Pixel 裝置記憶體問題驗證
