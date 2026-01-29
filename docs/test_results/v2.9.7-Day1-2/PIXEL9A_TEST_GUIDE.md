# v2.9.7 Day 1-2: Pixel 9a 實機測試指南

**版本**: v2.9.7-Day1-2-device
**測試日期**: 2026-01-29（或之後）
**測試設備**: Pixel 9a (16KB page size)
**前置作業**: 模擬器穩定性測試已通過 ✅

---

## 📋 測試目標

### 主要目標

驗證 GPS EXIF 功能在實機上的**完整運作**：
1. ✅ GPS 位置成功獲取
2. ✅ GPS EXIF 標籤正確寫入（經緯度、海拔、時間戳記）
3. ✅ EXIF 資料準確性驗證

### 次要目標

確認模擬器測試結果在實機上一致：
1. GPS 開關 UI 正常
2. 錯誤處理機制正常
3. 無崩潰或效能問題

---

## 🚀 測試前準備

### 1. 編譯 Device 版本 APK

```bash
# 在 Home NB 上執行
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1

# 編譯 Device Debug 版本
./gradlew.bat assembleDeviceDebug

# APK 位置
app/build/outputs/apk/device/debug/app-device-debug.apk
```

**版本確認**:
- versionCode: 59
- versionName: **2.9.7-Day1-2** (不含 -emulator 後綴)

### 2. 傳輸 APK 到 Work NB

**方法 1：USB 隨身碟**
```bash
# 複製 APK 到隨身碟
copy app\build\outputs\apk\device\debug\app-device-debug.apk E:\
```

**方法 2：Git 提交**（如果檔案不大）
```bash
# 暫存到 tmp/ 目錄（不會被 commit）
copy app\build\outputs\apk\device\debug\app-device-debug.apk tmp\v2.9.7-Day1-2-device.apk
```

### 3. 準備測試文檔

**必須傳輸的文檔**:
- ✅ `EMULATOR_TEST_REPORT_FINAL.md` - 模擬器測試結果
- ✅ `PIXEL9A_TEST_GUIDE.md` - 本文檔
- ✅ `EMULATOR_STABILITY_TEST_PLAN.md` - 參考測試計劃

---

## 📱 實機測試步驟

### 準備階段

#### 1. 連接 Pixel 9a

```bash
# 在 Work NB 上執行
adb devices

# 確認設備已連接
# 應該看到：
# List of devices attached
# XXXXXXXXXX      device
```

#### 2. 安裝 APK

```bash
# 卸載舊版本（如果有）
adb uninstall com.akirachang.ai.photographer

# 安裝新版本
adb install app-device-debug.apk
# 或
adb install tmp\v2.9.7-Day1-2-device.apk

# 確認安裝成功
adb shell pm list packages | grep akirachang
# 應該看到：
# package:com.akirachang.ai.photographer
```

#### 3. 授予必要權限

```bash
# 相機權限
adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA

# 儲存權限（Android 13+）
adb shell pm grant com.akirachang.ai.photographer android.permission.READ_MEDIA_IMAGES
adb shell pm grant com.akirachang.ai.photographer android.permission.READ_MEDIA_VIDEO

# 位置權限（GPS 測試用）
adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_COARSE_LOCATION
```

---

### 測試階段

#### 測試 1：應用啟動和版本驗證

**步驟**:
1. 啟動應用
2. 點擊右上角設置圖標
3. 滾動到底部查看 **Camera Diagnostics**

**驗證點**:
- [ ] 應用正常啟動，相機預覽顯示
- [ ] App Version 顯示：**2.9.7-Day1-2** (無 -emulator)
- [ ] 無崩潰或錯誤

**截圖**: `Screenshot_<timestamp>_version.png`

---

#### 測試 2：GPS 開關 UI 驗證

**步驟**:
1. 在設置頁面滾動到 GPS 開關
2. 確認 UI 顯示正確

**驗證點**:
- [ ] GPS 位置標記開關顯示
- [ ] 圖標：📍
- [ ] 描述文字正確
- [ ] 預設狀態：關閉

**截圖**: `Screenshot_<timestamp>_gps_off.png`

---

#### 測試 3：拍照測試 - GPS 關閉

**步驟**:
1. 確保 GPS 開關為 **OFF**
2. 返回相機頁面
3. 拍照
4. 收集 Logcat

**預期結果**:
```
✅ 拍照正常完成
✅ Logcat 顯示：「GPS tagging disabled in settings」
✅ 照片保存成功
```

**Logcat 收集**:
```bash
# 清除舊日誌
adb logcat -c

# 拍照後收集日誌
adb logcat -d > tmp/test3_gps_off.log

# 過濾關鍵日誌
adb logcat -d | grep "CameraRepository" > tmp/test3_gps_off_filtered.log
```

**驗證點**:
- [ ] Logcat 顯示：`GPS tagging disabled in settings`
- [ ] Logcat 顯示：`hasLocation=false`
- [ ] Logcat 顯示：`EXIF metadata saved successfully`
- [ ] 拍照時間 < 2 秒

---

#### 測試 4：開啟 GPS 開關

**步驟**:
1. 進入設置頁面
2. 點擊 GPS 開關 → **ON**
3. 確認開關狀態改變

**驗證點**:
- [ ] 開關成功切換為 ON（藍色）
- [ ] 開關狀態持久化（離開後再回來仍為 ON）

**截圖**: `Screenshot_<timestamp>_gps_on.png`

---

#### 測試 5：拍照測試 - GPS 開啟但室內無信號

**步驟**:
1. 確保 GPS 開關為 **ON**
2. 確保位置權限已授予（步驟 3 已授予）
3. **在室內**拍照（GPS 信號弱或無信號）
4. 觀察拍照時間和 Logcat

**預期結果**:
```
✅ 拍照正常完成（可能等待 5 秒）
✅ Logcat 顯示：「Requesting current location with 5000ms timeout...」
✅ Logcat 顯示：「Location not available within timeout」或成功獲取
✅ 拍照時間 < 6 秒（5 秒 timeout + 1 秒處理）
```

**Logcat 收集**:
```bash
adb logcat -c
# 拍照
adb logcat -d | grep "CameraRepository" > tmp/test5_gps_indoor.log
```

**驗證點**:
- [ ] 拍照正常完成
- [ ] 拍照時間 < 6 秒（即使 timeout）
- [ ] 無 ANR 或崩潰
- [ ] Logcat 有 timeout 或位置獲取相關日誌

---

#### 測試 6：拍照測試 - GPS 開啟且戶外有信號 ⭐ **關鍵測試**

**步驟**:
1. 確保 GPS 開關為 **ON**
2. **到戶外**或窗邊（GPS 信號良好處）
3. 等待 GPS 定位成功（狀態列顯示 GPS 圖標）
4. 拍照
5. 收集 Logcat
6. 檢查照片 EXIF

**預期結果**:
```
✅ 拍照正常完成（< 5 秒，通常 < 2 秒）
✅ Logcat 顯示：「Location obtained: lat=X, lon=Y, alt=Z」
✅ Logcat 顯示：「GPS EXIF tags written: lat=X, lon=Y, alt=Z」
✅ Logcat 顯示：「EXIF metadata saved successfully」
```

**Logcat 收集**:
```bash
adb logcat -c
# 拍照
adb logcat -d | grep "CameraRepository" > tmp/test6_gps_outdoor.log
```

**照片 EXIF 驗證**:
```bash
# 找到最新照片
adb shell "ls -lt /sdcard/Pictures/SmartCameraApp | head -3"

# 拉取照片
adb pull /sdcard/Pictures/SmartCameraApp/<照片名稱> tmp/

# 在 Work NB 上使用 exiftool 檢查 EXIF
# (如果已安裝 exiftool)
exiftool tmp/<照片名稱> | grep GPS

# 或使用線上工具：https://exifdata.com/
```

**EXIF 預期內容**:
```
GPS Latitude         : XX deg X' X.XX" N (或 S)
GPS Longitude        : XXX deg X' X.XX" E (或 W)
GPS Altitude         : XXX m Above Sea Level
GPS Date/Time        : YYYY:MM:DD HH:MM:SS
GPS Map Datum        : WGS-84
GPS Latitude Ref     : North (或 South)
GPS Longitude Ref    : East (或 West)
GPS Altitude Ref     : Above Sea Level
```

**驗證點**:
- [ ] Logcat 顯示 `Location obtained`
- [ ] Logcat 顯示正確的經緯度和海拔
- [ ] Logcat 顯示 `GPS EXIF tags written`
- [ ] Logcat 顯示 `hasLocation=true`
- [ ] EXIF 包含 GPS Latitude
- [ ] EXIF 包含 GPS Longitude
- [ ] EXIF 包含 GPS Altitude
- [ ] EXIF 包含 GPS Date/Time
- [ ] GPS 資料與實際位置相符（可用 Google Maps 驗證）

**截圖**:
- `Screenshot_<timestamp>_outdoor_photo.png` - 拍照畫面
- `exif_screenshot.png` - EXIF 資料截圖

---

#### 測試 7：關閉 GPS 開關並驗證

**步驟**:
1. 進入設置，關閉 GPS 開關 → **OFF**
2. 返回相機頁面
3. 拍照
4. 收集 Logcat

**預期結果**:
```
✅ 拍照正常完成
✅ Logcat 顯示：「GPS tagging disabled in settings」
✅ 不再嘗試獲取位置
```

**驗證點**:
- [ ] GPS 開關成功關閉
- [ ] Logcat 顯示 GPS 已禁用
- [ ] 拍照速度快（< 1 秒）

---

## 🐛 問題排查

### 常見問題

#### 1. 無法獲取位置（Timeout）

**可能原因**:
- 室內 GPS 信號弱
- 位置服務未開啟
- 位置權限未授予

**排查步驟**:
```bash
# 檢查位置權限
adb shell dumpsys package com.akirachang.ai.photographer | grep "ACCESS_FINE_LOCATION"

# 確認位置服務已開啟
# 在手機上：Settings > Location > 確保已開啟

# 到戶外測試
```

#### 2. EXIF 無 GPS 資料

**可能原因**:
- GPS 開關未開啟
- 位置未成功獲取（檢查 Logcat）
- EXIF 寫入失敗（檢查 Logcat）

**排查步驟**:
```bash
# 檢查 Logcat
adb logcat -d | grep -i "gps\|location\|exif"

# 確認開關狀態
# 進入設置查看 GPS 開關是否為 ON
```

#### 3. 應用崩潰

**排查步驟**:
```bash
# 收集崩潰日誌
adb logcat -d | grep -E "FATAL|AndroidRuntime" > tmp/crash.log

# 檢查 Hilt DI 錯誤
adb logcat -d | grep -i "dagger\|hilt"
```

---

## 📊 測試記錄表格

### 測試結果記錄

| 測試編號 | 測試項目 | 結果 | Logcat | 截圖 | 備註 |
|---------|---------|------|--------|------|------|
| 1 | 應用啟動和版本驗證 | ☐ PASS ☐ FAIL | ☐ | ☐ | |
| 2 | GPS 開關 UI 驗證 | ☐ PASS ☐ FAIL | ☐ | ☐ | |
| 3 | 拍照 - GPS 關閉 | ☐ PASS ☐ FAIL | ☐ | ☐ | |
| 4 | 開啟 GPS 開關 | ☐ PASS ☐ FAIL | ☐ | ☐ | |
| 5 | 拍照 - GPS 開啟室內 | ☐ PASS ☐ FAIL | ☐ | ☐ | |
| 6 | 拍照 - GPS 開啟戶外 ⭐ | ☐ PASS ☐ FAIL | ☐ | ☐ | |
| 7 | 關閉 GPS 開關 | ☐ PASS ☐ FAIL | ☐ | ☐ | |

---

## 📝 測試報告模板

### 完成測試後填寫

**測試人員**: _____________
**測試日期**: _____________
**測試時間**: _____________
**測試地點**: _____________
**設備資訊**: Pixel 9a
**Android 版本**: _____________

**測試結果**:
- [ ] 全部通過
- [ ] 部分通過（詳述失敗項目）
- [ ] 失敗（詳述原因）

**GPS 位置資訊** (測試 6 戶外測試):
- 實際位置（Google Maps）: _____________
- 照片 EXIF Latitude: _____________
- 照片 EXIF Longitude: _____________
- 照片 EXIF Altitude: _____________
- 誤差範圍: ☐ < 10m ☐ < 50m ☐ > 50m

**效能觀察**:
- 拍照速度（GPS 關閉）: _______ 秒
- 拍照速度（GPS 開啟無信號）: _______ 秒
- 拍照速度（GPS 開啟有信號）: _______ 秒
- 有無 ANR: ☐ 無 ☐ 有（描述）
- 有無崩潰: ☐ 無 ☐ 有（描述）

**其他發現**:
```
[記錄任何異常行為、建議或改進點]
```

---

## 📤 測試產出

### 必須提交的檔案

**在 Work NB 上整理以下檔案到 `docs/test_results/v2.9.7-Day1-2/` 目錄**:

1. **測試報告**: `PIXEL9A_TEST_REPORT.md`
2. **Logcat 日誌**:
   - `test3_gps_off.log`
   - `test5_gps_indoor.log`
   - `test6_gps_outdoor.log` ⭐
3. **截圖**:
   - 版本驗證截圖
   - GPS 開關 OFF 截圖
   - GPS 開關 ON 截圖
   - 戶外拍照截圖
   - EXIF 資料截圖 ⭐
4. **測試照片** (帶 GPS EXIF):
   - 至少 1 張戶外照片（包含完整 GPS EXIF）

### Git 提交

```bash
# 在 Work NB 上執行
git add docs/test_results/v2.9.7-Day1-2/PIXEL9A_TEST_REPORT.md
git add docs/test_results/v2.9.7-Day1-2/*.log
git add docs/test_results/v2.9.7-Day1-2/*.png
git add docs/test_results/v2.9.7-Day1-2/*.jpg

git commit -m "[Test] v2.9.7-Day1-2: Pixel 9a 實機測試報告

測試結果：[PASS/FAIL]
- GPS EXIF 功能驗證
- 位置獲取正常
- EXIF 資料準確

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push
```

---

## 🔗 參考文檔

- [模擬器測試報告](EMULATOR_TEST_REPORT_FINAL.md)
- [測試計劃](EMULATOR_STABILITY_TEST_PLAN.md)
- [Roadmap 2.4](../../plans/02.4-smart_camera_app_roadmap.md#day-1-2-gps-exif-寫入--high)

---

**文檔版本**: 1.0
**建立日期**: 2026-01-29
**建立者**: Claude Code (AI Assistant)
