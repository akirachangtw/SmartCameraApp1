# v2.9.8: Pixel 9a 實機測試指南

**目標版本**: v2.9.8
**測試設備**: Pixel 9a (16KB page size)
**測試類型**: GPS 快門延遲修復驗證
**預估時間**: 30-45 分鐘

---

## 📋 測試準備

### 1. 編譯與安裝

```bash
# 編譯 Device 版本 APK（Work NB 上執行）
./gradlew assembleDeviceDebug

# 確認設備連接
adb devices

# 安裝 APK
adb install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

### 2. 權限確認

啟動應用後，確認以下權限已授予：
- ✅ 相機權限
- ✅ 位置權限（精確位置）
- ✅ 儲存權限（照片存取）

### 3. 測試環境

- **室內測試**: 辦公室環境（GPS 信號弱）
- **戶外測試**: 開闊地帶（GPS 信號強）
- **網路**: 建議開啟 Wi-Fi（輔助 GPS）

---

## 🧪 測試場景

### 測試場景 A：GPS 關閉（快速反饋驗證）

**目標**: 驗證 GPS OFF 時快門反饋 < 500ms

**步驟**:
1. 開啟應用，進入設定面板
2. 確認 GPS 開關為 **OFF**
3. 離開設定面板（驗證無「照片已保存」訊息）
4. 拍攝 3-5 張照片

**觀察重點**:
- ✅ 按下快門後，按鈕立即變灰（透明度 0.5）
- ✅ **不應出現**「照片已保存」文字訊息
- ✅ **不應出現**外圈進度指示器（GPS OFF）
- ✅ 按鈕快速恢復正常（< 500ms）
- ✅ 照片成功保存至相簿

**預期結果**:
- 快門反饋時間: < 100ms（模擬器為 60ms）
- GPS EXIF: 無（hasLocation=false）

---

### 測試場景 B：GPS ON 室內（非同步處理驗證）⭐ 關鍵測試

**目標**: 驗證 GPS ON 時非同步處理，UI 立即反饋

**步驟**:
1. 進入設定面板，開啟 GPS 開關
2. 停留在室內（GPS 信號弱）
3. 拍攝 3-5 張照片

**觀察重點**:
- ✅ 按下快門後，按鈕立即變灰（透明度 0.5）
- ✅ **不應出現**「照片已保存」文字訊息
- ✅ **應出現**外圈進度指示器（白色，3dp 粗細）
- ✅ 按鈕快速恢復正常（< 500ms，即使 GPS 尚未取得）
- ✅ 照片成功保存至相簿

**預期結果**:
- 快門反饋時間: < 500ms（不等待 GPS）
- GPS EXIF: 可能有或無（取決於快取位置）
- Logcat: 應看到「Async GPS EXIF tagging completed」

**Logcat 監控**:
```bash
# 開啟新終端視窗
adb logcat -s CameraRepositoryImpl:* -v time | tee tmp/v2.9.8_pixel9a_indoor.log
```

---

### 測試場景 C：GPS ON 戶外（完整 GPS 流程驗證）⭐ 關鍵測試

**目標**: 驗證戶外 GPS 位置獲取與 EXIF 寫入

**步驟**:
1. 移動至戶外開闊地帶（例如：內湖行愛路77巷）
2. 確認 GPS 開關為 **ON**
3. 等待 GPS 信號穩定（觀察通知列 GPS 圖示）
4. 拍攝 5-10 張照片

**觀察重點**:
- ✅ 按鈕立即變灰，快速恢復（< 500ms）
- ✅ 外圈進度指示器短暫顯示
- ✅ 無文字訊息
- ✅ 照片成功保存

**預期結果**:
- 快門反饋時間: < 500ms（即使 GPS 處理中）
- GPS EXIF: 應完整寫入（經緯度、海拔、時間戳）
- GPS 成功率: > 80%（5-10 張中至少 8 張有 GPS）

**驗證 GPS EXIF**:
- 使用 Google 相簿查看照片資訊
- 確認經緯度、海拔資料正確
- 或使用 EXIF 檢視工具（如 `exiftool`）

---

### 測試場景 D：快速連續拍照（防重複驗證）

**目標**: 驗證快門按鈕禁用機制

**步驟**:
1. GPS 開關設為 **ON**（確保有處理延遲）
2. 快速連續按快門 5-10 次（模擬用戶誤觸）

**觀察重點**:
- ✅ 按鈕變灰期間，再次點擊無效
- ✅ 只觸發 1 次拍照（或最多 2-3 次，視點擊速度）
- ✅ 沒有大量重複拍照

**預期結果**:
- 防止連續拍照: 有效
- 相簿中照片數量: < 5 張（顯著少於點擊次數）

---

### 測試場景 E：Logcat 完整驗證

**目標**: 分析非同步 GPS 處理的完整流程

**步驟**:
1. 啟動 Logcat 監控
2. GPS ON，拍攝 3 張照片
3. 停止 Logcat，分析日誌

**Logcat 啟動**:
```bash
adb logcat -c
adb logcat -s CameraRepositoryImpl:* CameraViewModel:* -v time > tmp/v2.9.8_pixel9a_full.log
```

**關鍵日誌檢查**:
```
D CameraRepositoryImpl: Starting photo capture via CameraInputSource...
I CameraRepositoryImpl: Bitmap captured successfully: ...
I CameraRepositoryImpl: Photo saved successfully to: ...  ← 立即返回
D CameraRepositoryImpl: GPS tagging enabled in settings
D CameraRepositoryImpl: Using cached location (age: XXXms): ...  ← 或 Requesting current location
D CameraRepositoryImpl: Async GPS EXIF tagging completed for: ...  ← 背景完成
```

**驗證重點**:
1. 「Photo saved successfully」與「Async GPS EXIF tagging completed」之間的時間差
2. GPS 位置來源（cached vs current）
3. 位置獲取時間（age）
4. 沒有錯誤或崩潰

---

## 📊 測試記錄表

### 場景 A：GPS OFF

| 照片編號 | 時間戳 | 快門反饋時間 | GPS EXIF | 備註 |
|---------|--------|-------------|----------|------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

### 場景 B：GPS ON 室內

| 照片編號 | 時間戳 | 快門反饋時間 | GPS EXIF | Logcat 確認 |
|---------|--------|-------------|----------|------------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

### 場景 C：GPS ON 戶外

| 照片編號 | 時間戳 | GPS 座標 | 海拔 | GPS 年齡 |
|---------|--------|---------|------|---------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |
| 4 | | | | |
| 5 | | | | |

### 場景 D：快速連續拍照

| 測試次數 | 點擊次數 | 實際拍照數 | 防重複效果 |
|---------|---------|-----------|-----------|
| 1 | 10 | | |
| 2 | 10 | | |

---

## 🎯 驗收標準

### 必須通過（P0）

- ✅ GPS OFF 快門反饋 < 500ms
- ✅ GPS ON 快門反饋 < 500ms（不等待 GPS）
- ✅ 無「照片已保存」文字訊息誤觸發
- ✅ 照片成功保存（100% 成功率）
- ✅ GPS EXIF 正確寫入（戶外測試 > 80% 成功率）

### 應該通過（P1）

- ✅ 外圈進度指示器正確顯示（GPS ON 時）
- ✅ 快速連續拍照防重複有效
- ✅ Logcat 無錯誤或崩潰

### 可選驗證（P2）

- ✅ UI 視覺反饋是否足夠明確（用戶體驗）
- ✅ GPS 快取位置優先使用（降低延遲）
- ✅ GPS timeout 2 秒生效

---

## 📝 測試報告模板

完成測試後，請創建 `PIXEL9A_TEST_REPORT.md`，參考以下結構：

```markdown
# v2.9.8: Pixel 9a 實機測試報告

**版本**: v2.9.8
**測試日期**: YYYY-MM-DD
**測試時間**: HH:MM - HH:MM
**測試設備**: Pixel 9a (16KB page size)
**Android 版本**: Android 15
**測試人員**: Work NB Test Team

## 測試執行摘要

| 測試項目 | 結果 | 實際表現 | 目標 | 備註 |
|---------|------|---------|------|------|
| ... | ... | ... | ... | ... |

## 測試場景詳細結果

### 場景 A: GPS OFF
...

### 場景 B: GPS ON 室內
...

### 場景 C: GPS ON 戶外
...

## 問題與建議

## 結論
```

---

## 🔍 常見問題排查

### Q1: GPS 位置一直無法獲取

**可能原因**:
- 位置權限未授予（精確位置）
- GPS 信號弱（室內）
- 裝置 GPS 功能關閉

**解決方法**:
1. 確認應用權限設定
2. 移動至戶外開闊處
3. 檢查裝置設定 → 位置 → 模式（高精確度）

### Q2: 照片沒有 GPS EXIF

**正常情況**:
- GPS 開關為 OFF
- GPS timeout（2 秒內未取得）
- 室內無 GPS 信號

**異常情況**:
- 戶外 GPS 信號強但仍無 EXIF → 檢查 Logcat 錯誤

### Q3: 快門反饋仍然很慢（> 1 秒）

**檢查項目**:
1. 確認 APK 版本為 v2.9.8
2. 檢查 Logcat 是否有錯誤
3. 確認不是在等待 GPS（應立即返回）

---

## 📤 測試交付

完成測試後，請提交以下內容至 Git：

1. **測試報告**: `docs/test_results/v2.9.8/PIXEL9A_TEST_REPORT.md`
2. **Logcat 日誌**: `docs/test_results/v2.9.8/*.log`
3. **測試照片**: 選擇 3-5 張代表性照片（如需要）
4. **EXIF 驗證**: 照片 EXIF 資訊截圖或文字記錄

**提交方式**:
```bash
git add docs/test_results/v2.9.8/
git commit -m "[Test] v2.9.8: Pixel 9a 實機測試結果

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
git push
```

---

**測試指南建立時間**: 2026-01-30 14:30
**參考文檔**:
- 模擬器測試報告: `EMULATOR_TEST_REPORT.md`
- 交接摘要: `HANDOVER_SUMMARY.md`
