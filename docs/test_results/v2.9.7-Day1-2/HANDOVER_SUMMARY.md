# v2.9.7 Day 1-2: Work NB 實機測試交接摘要

**交接日期**: 2026-01-29
**交接內容**: GPS EXIF 功能實機測試
**測試設備**: Pixel 9a

---

## ✅ Home NB 已完成

### 1. 模擬器穩定性測試 ✅
- **測試通過率**: 83.3% (5/6)
- **關鍵驗證**: 5 秒 timeout 機制正常，錯誤處理完善
- **詳細報告**: [EMULATOR_TEST_REPORT_FINAL.md](EMULATOR_TEST_REPORT_FINAL.md)

### 2. Device APK 編譯 ✅
- **版本**: v2.9.7-Day1-2 (versionCode: 59)
- **Build Type**: Device Debug
- **檔案位置**: `tmp/v2.9.7-Day1-2-device.apk`
- **檔案大小**: 246 MB

### 3. 測試文檔準備 ✅
- ✅ 模擬器測試報告
- ✅ 實機測試指南（含詳細步驟）
- ✅ 測試記錄表格

---

## 📦 需要傳輸到 Work NB 的檔案

### 必要檔案（使用 USB 隨身碟）

```
📁 v2.9.7-Day1-2-Handover/
├── 📄 v2.9.7-Day1-2-device.apk          (246 MB) ⭐ 必須
├── 📄 HANDOVER_SUMMARY.md               (本文檔)
├── 📄 PIXEL9A_TEST_GUIDE.md             (實機測試指南) ⭐ 必須
├── 📄 EMULATOR_TEST_REPORT_FINAL.md     (模擬器測試報告)
└── 📄 EMULATOR_STABILITY_TEST_PLAN.md   (參考測試計劃)
```

### 從哪裡複製

```bash
# 在 Home NB 上執行
cd d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1

# 方法 1：複製到桌面暫存資料夾（推薦）
mkdir %USERPROFILE%\Desktop\v2.9.7-Day1-2-Handover
copy tmp\v2.9.7-Day1-2-device.apk %USERPROFILE%\Desktop\v2.9.7-Day1-2-Handover\
copy docs\test_results\v2.9.7-Day1-2\*.md %USERPROFILE%\Desktop\v2.9.7-Day1-2-Handover\

# 方法 2：直接複製到 USB 隨身碟（假設是 E:）
mkdir E:\v2.9.7-Day1-2-Handover
copy tmp\v2.9.7-Day1-2-device.apk E:\v2.9.7-Day1-2-Handover\
copy docs\test_results\v2.9.7-Day1-2\*.md E:\v2.9.7-Day1-2-Handover\
```

---

## 🚀 Work NB 快速開始

### 1. 連接 Pixel 9a

```bash
# 確認設備連接
adb devices

# 應該看到：
# List of devices attached
# XXXXXXXXXX      device
```

### 2. 安裝 APK

```bash
# 安裝（從 USB 隨身碟或複製到的位置）
cd <APK所在目錄>

adb uninstall com.akirachang.ai.photographer
adb install v2.9.7-Day1-2-device.apk
```

### 3. 授予權限

```bash
adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA
adb shell pm grant com.akirachang.ai.photographer android.permission.READ_MEDIA_IMAGES
adb shell pm grant com.akirachang.ai.photographer android.permission.READ_MEDIA_VIDEO
adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.akirachang.ai.photographer android.permission.ACCESS_COARSE_LOCATION
```

### 4. 開始測試

**參考文檔**: [PIXEL9A_TEST_GUIDE.md](PIXEL9A_TEST_GUIDE.md)

**關鍵測試**（共 7 個測試）:
1. 應用啟動和版本驗證
2. GPS 開關 UI 驗證
3. 拍照測試 - GPS 關閉
4. 開啟 GPS 開關
5. 拍照測試 - GPS 開啟室內（驗證 timeout）
6. ⭐ **拍照測試 - GPS 開啟戶外**（關鍵測試）
7. 關閉 GPS 開關驗證

**預計測試時間**: 30-45 分鐘

---

## 🎯 測試重點

### ⭐ 最關鍵測試：測試 6 - GPS 開啟戶外拍照

**目標**: 驗證 GPS EXIF 完整寫入

**步驟**:
1. 確保 GPS 開關 ON
2. 到**戶外**或窗邊（GPS 信號良好處）
3. 等待 GPS 定位（狀態列顯示 GPS 圖標）
4. 拍照
5. 收集 Logcat

**預期 Logcat**:
```
I/CameraRepositoryImpl: Location obtained: lat=X, lon=Y, alt=Z
I/CameraRepositoryImpl: GPS EXIF tags written: lat=X, lon=Y, alt=Z
I/CameraRepositoryImpl: EXIF metadata saved successfully
```

**EXIF 驗證**:
```bash
# 拉取照片
adb shell "ls -lt /sdcard/Pictures/SmartCameraApp | head -3"
adb pull /sdcard/Pictures/SmartCameraApp/<最新照片> .

# 檢查 EXIF（如有 exiftool）
exiftool <照片名稱> | grep GPS
```

**必須包含**:
- ✅ GPS Latitude
- ✅ GPS Longitude
- ✅ GPS Altitude
- ✅ GPS Date/Time

---

## 📊 預期測試結果

### 成功標準

**全部測試通過** 需滿足：
1. ✅ 應用穩定，無崩潰
2. ✅ GPS 開關正常工作
3. ✅ GPS 關閉時拍照正常（< 2 秒）
4. ✅ GPS 開啟室內時 timeout 正常（約 5 秒）
5. ✅ **GPS 開啟戶外時位置獲取成功** ⭐
6. ✅ **EXIF 包含完整 GPS 資料** ⭐
7. ✅ GPS 資料準確（與 Google Maps 對比誤差 < 50m）

### 部分失敗的處理

**如果測試 6 失敗**（無法獲取位置或 EXIF 無資料）:
1. 檢查 Logcat 錯誤訊息
2. 確認位置服務已開啟（Settings > Location）
3. 確認位置權限已授予
4. 嘗試不同地點（更開闊的戶外）
5. 記錄詳細錯誤資訊

---

## 📝 測試產出

### 必須產出的檔案

**存放位置**: `docs/test_results/v2.9.7-Day1-2/`

1. **測試報告**: `PIXEL9A_TEST_REPORT.md`
   - 測試結果摘要
   - 每個測試的結果
   - GPS 位置資訊
   - 效能觀察
   - 問題和建議

2. **Logcat 日誌**:
   - `test3_gps_off.log` - GPS 關閉測試
   - `test5_gps_indoor.log` - GPS 開啟室內測試
   - `test6_gps_outdoor.log` ⭐ - GPS 開啟戶外測試（最重要）

3. **截圖**:
   - 版本驗證
   - GPS 開關 OFF/ON
   - 戶外拍照畫面
   - EXIF 資料截圖 ⭐

4. **測試照片**:
   - 至少 1 張帶完整 GPS EXIF 的戶外照片

### Logcat 收集方法

```bash
# 測試前清除
adb logcat -c

# 執行測試（拍照）

# 測試後收集
adb logcat -d > test<編號>_<名稱>.log

# 過濾版本
adb logcat -d | grep "CameraRepository" > test<編號>_<名稱>_filtered.log
```

---

## 📤 提交測試結果

### Git 提交命令

```bash
# 在 Work NB 上，測試完成後執行
cd <專案目錄>

# 添加測試結果
git add docs/test_results/v2.9.7-Day1-2/PIXEL9A_TEST_REPORT.md
git add docs/test_results/v2.9.7-Day1-2/*.log
git add docs/test_results/v2.9.7-Day1-2/*.png
git add docs/test_results/v2.9.7-Day1-2/*.jpg

# 提交
git commit -m "[Test] v2.9.7-Day1-2: Pixel 9a 實機測試 - GPS EXIF 功能驗證

測試結果：[PASS/FAIL]
- GPS EXIF 功能完整驗證
- 位置獲取成功
- EXIF 資料準確（誤差 < XXm）

測試環境：
- 設備：Pixel 9a
- Android 版本：XX
- 測試地點：[地點]

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

# 推送（如果 remote 已設定）
git push
```

---

## 🔗 快速連結

- [實機測試指南](PIXEL9A_TEST_GUIDE.md) ⭐ 詳細步驟
- [模擬器測試報告](EMULATOR_TEST_REPORT_FINAL.md) - 參考結果
- [測試計劃](EMULATOR_STABILITY_TEST_PLAN.md) - 原始計劃

---

## ❓ 問題聯絡

如遇到問題，請記錄以下資訊：
1. 測試編號和步驟
2. 完整 Logcat 錯誤訊息
3. 截圖或照片
4. 設備資訊和 Android 版本

---

**交接摘要版本**: 1.0
**建立日期**: 2026-01-29 14:05
**建立者**: Claude Code (AI Assistant)

🚀 **祝測試順利！**
