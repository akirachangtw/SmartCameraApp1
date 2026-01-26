# v2.9.6 Phase 2.1 - Pixel 9a 實體裝置測試報告

## 測試資訊

| 項目 | 內容 |
|------|------|
| **測試版本** | v2.9.6-phase2.1 (Build 53) |
| **測試裝置** | Google Pixel 9a |
| **Android 版本** | Android 16 (API 36) |
| **測試日期** | 2026-01-26 |
| **測試人員** | User + Claude Code |
| **APK** | `app-device-debug.apk` (現場編譯安裝) |

---

## 測試總結

| 測試類別 | 測試案例數 | 通過 | 條件通過 | 失敗 | 通過率 |
|---------|-----------|------|---------|------|--------|
| Android 15 相容性 | 6 | 5 | 1 | 0 | 100% (含條件) |
| Phase 2 旋轉修復 | 7 | 7 | 0 | 0 | 100% |
| **總計** | **13** | **12** | **1** | **0** | **100%** |

### 測試結論

- [x] ⚠️ **條件通過** - 核心功能全部正常，16KB 頁面對齊警告存在但不影響功能

---

## 測試案例 1: Android 15 相容性驗證

### 測試結果: ⚠️ 條件通過

| 檢查項目 | 預期結果 | 實際結果 | 狀態 | 備註 |
|---------|---------|---------|------|------|
| 16 KB 對齊警告數量 | 0 | 多個 | ⚠️ | PageSizeMismatchDialog 出現 |
| libopencv_java4.so | 成功載入 | 成功載入 | ✅ | OpenCV 4.13.0 初始化正常 |
| libface_detector_v2_jni.so | 成功載入 | 成功載入 | ✅ | ML Kit 正常 |
| libmlkitcommonpipeline.so | 成功載入 | 成功載入 | ✅ | ML Kit 正常 |
| 其他原生函式庫 | 全部成功 | 全部成功 | ✅ | |
| 應用啟動 | 無錯誤 | 正常啟動 | ✅ | 相機預覽正常開啟 |

### 詳細發現

#### ⚠️ PageSizeMismatchDialog 警告

啟動時出現 Android 系統級 `PageSizeMismatchDialog` 對話框，警告以下原生函式庫未對齊 16KB 頁面邊界：

- `libopencv_java4.so` (OpenCV 4.13.0)
- `libface_detector_v2_jni.so` (ML Kit)
- `libmlkitcommonpipeline.so` (ML Kit)
- `libc++_shared.so`
- `libsurface_util_jni.so`
- `libimage_processing_util_jni.so`
- `libdatastore_shared_counter.so`
- `libandroidx.graphics.path.so`

**影響評估**:
- 點擊「確定」後 App 正常運作
- 所有原生函式庫成功載入並正常執行
- 此為 Android 16 的前瞻性警告，目前不影響功能
- 根因：上游函式庫（OpenCV、ML Kit）尚未發布 16KB 對齊版本

#### ✅ 解析度升級確認

- 裝置成功選擇 **1280x720** 解析度（相較先前的 640x480）
- Logcat 顯示正常初始化

---

## 測試案例 2: Phase 2 旋轉修復驗證

### 測試結果: ✅ 全部通過

用戶在實體裝置上以 6 個方向（前後鏡頭各 3 方向）測試預覽顯示和拍照功能，確認全部方向正確。

| 測試項目 | 預期結果 | 實際結果 | 狀態 | 測試照片 |
|---------|---------|---------|------|---------|
| 後置 - Portrait | 正確顯示 | ✅ 正確 | ✅ | `SmartCamera_...-後置鏡頭-Portrait.jpg` |
| 後置 - Landscape Left | 正確顯示 | ✅ 正確 | ✅ | `SmartCamera_...-後置鏡頭-landscape-left.jpg` |
| 後置 - Landscape Right | 正確顯示（不向左轉 90°） | ✅ 正確 | ✅ | `SmartCamera_...-後置鏡頭-landscape-right.jpg` |
| 前置 - Portrait | 正確顯示（不上下顛倒） | ✅ 正確 | ✅ | `SmartCamera_...-前置鏡頭-Portrait.jpg` |
| 前置 - Landscape Left | 正確顯示 | ✅ 正確 | ✅ | `SmartCamera_...-前置鏡頭-landscape-left.jpg` |
| 前置 - Landscape Right | 正確顯示（不向左轉 180°） | ✅ 正確 | ✅ | `SmartCamera_...-前置鏡頭-landscape-right.jpg` |
| AI Overlay 對齊 | 全部正確 | ✅ 正確 | ✅ | 用戶確認 |

### 後置鏡頭驗證細節

後置鏡頭對準辦公桌上的 Dell 電腦螢幕拍攝，以螢幕左下角的粉紅色衛生紙包裝作為方向參考點：

- **Portrait**: 手機直立拍攝螢幕，衛生紙位置符合預期，畫面方向正確
- **Landscape Left**: 手機向左橫放拍攝螢幕，衛生紙在正確相對位置，畫面方向正確
- **Landscape Right**: 手機向右橫放拍攝螢幕，衛生紙在正確相對位置，畫面方向正確

### 前置鏡頭驗證細節

前置鏡頭自拍，以人物正立方向作為驗證基準：

- **Portrait**: 手機直立自拍，人物方向正確
- **Landscape Left**: 手機向左橫放自拍，人物方向正確正立
- **Landscape Right**: 手機向右橫放自拍，人物方向正確

### 比對驗證

用戶額外提供 Pixel 內建相機 (PXL) 前鏡頭拍攝照片作為參考基準：

| 比對項目 | SmartCamera APP | Pixel 內建相機 (PXL) | 一致性 |
|---------|----------------|---------------------|--------|
| 前置 - Portrait | 正確 | 正確 | ✅ |
| 前置 - Landscape Right | 正確 | 正確 | ✅ |
| 前置 - Landscape Left | 正確 | 正確 | ✅ |

### 關於 JPG 檔案在 PC 上顯示方向的附註

在 PC 上檢視 SmartCamera 拍攝的 JPG 檔案時，部分照片顯示方向可能與預期不同（如 Portrait 照片在 PC 上可能顯示為橫向）。這是因為：

1. **CameraX 預設行為**：`ImageCapture` 將感測器原始方向的像素資料寫入 JPG
2. **EXIF 問題**：Phase 1 測試已確認照片缺少 EXIF Orientation 標籤
3. **裝置端正確**：用戶在實體裝置上確認預覽和拍照方向均正確
4. **此為已知議題**：EXIF 修復將在後續 Phase 中處理

---

## 測試證據清單

### SmartCamera APP 拍攝（6 張）

| 檔案名稱 | 鏡頭 | 方向 |
|---------|------|------|
| `SmartCamera_2026-01-26-17-12-12-後置鏡頭-Portrait.jpg` | 後置 | Portrait |
| `SmartCamera_2026-01-26-17-12-21-後置鏡頭-landscape-left.jpg` | 後置 | Landscape Left |
| `SmartCamera_2026-01-26-17-12-28-後置鏡頭-landscape-right.jpg` | 後置 | Landscape Right |
| `SmartCamera_2026-01-26-17-13-00-前置鏡頭-Portrait.jpg` | 前置 | Portrait |
| `SmartCamera_2026-01-26-17-13-18-前置鏡頭-landscape-left.jpg` | 前置 | Landscape Left |
| `SmartCamera_2026-01-26-17-13-32-前置鏡頭-landscape-right.jpg` | 前置 | Landscape Right |

### Pixel 內建相機比對用（3 張）

| 檔案名稱 | 鏡頭 | 方向 |
|---------|------|------|
| `PXL_20260126_093401705_前鏡頭-Portrait.jpg` | 前置 | Portrait |
| `PXL_20260126_093411904_前鏡頭-Landscape-right.jpg` | 前置 | Landscape Right |
| `PXL_20260126_093417974_前鏡頭-Landscape-left.jpg` | 前置 | Landscape Left |

### Logcat 紀錄

| 檔案 | 用途 |
|------|------|
| `tmp/pixel9a_android15_startup.txt` | Android 15 啟動 logcat |

---

## 關鍵發現

### ✅ 成功項目

1. **Phase 2 旋轉修復完全成功** - 前後鏡頭在 3 個方向（共 6 組合）預覽和拍照方向全部正確，解決了 Phase 1 發現的旋轉問題
2. **原生函式庫全部正常載入** - OpenCV 4.13.0、ML Kit 等核心函式庫功能正常
3. **應用穩定性良好** - 無崩潰、無錯誤，相機預覽和拍照功能正常
4. **解析度升級成功** - Pixel 9a 正確使用 1280x720 解析度
5. **與 Pixel 內建相機一致** - 前置鏡頭拍照方向與 Pixel 原生相機完全一致

### ⚠️ 注意事項

1. **PageSizeMismatchDialog 警告** - Android 16 顯示 16KB 頁面對齊警告
   - 影響：需用戶點擊「確定」才能繼續
   - 根因：上游函式庫未支援 16KB 對齊
   - 優先級：低（不影響功能，僅影響 UX）
   - 修復方案：等待 OpenCV / ML Kit 發布 16KB 對齊版本

2. **EXIF Orientation 缺失** - 拍攝的 JPG 檔案在 PC 上檢視時方向可能不正確
   - 影響：照片在非 Android 裝置上檢視時方向可能錯誤
   - 根因：CameraX ImageCapture 未寫入 EXIF Orientation 標籤
   - 優先級：中（Phase 1 已知議題，需後續修復）

---

## 與 Phase 1 比較

| 項目 | Phase 1 (模擬器) | Phase 2.1 (Pixel 9a) | 改善狀態 |
|------|-----------------|---------------------|---------|
| 預覽方向 - Portrait | ❌ 異常 | ✅ 正確 | 已修復 |
| 預覽方向 - Landscape Right | ❌ 上下顛倒 | ✅ 正確 | 已修復 |
| 預覽方向 - Landscape Left | ✅ 正確 | ✅ 正確 | 維持 |
| 前置鏡頭方向 | ❌ 異常 | ✅ 正確 | 已修復 |
| 拍照方向 (裝置端) | ❌ 錯亂 | ✅ 正確 | 已修復 |
| EXIF Orientation | ❌ 缺失 | ❌ 缺失 | 未處理 (已知議題) |
| 16KB 對齊 | N/A (模擬器) | ⚠️ 警告 | 待上游修復 |

---

## 下一步行動

1. **[低優先] 16KB 頁面對齊**
   - 追蹤 OpenCV 5.x 和 ML Kit 更新，等待支援 16KB 對齊
   - 考慮是否在 AndroidManifest 中加入 `android:extractNativeLibs="true"` 作為暫時方案

2. **[中優先] EXIF Orientation 修復**
   - 在 ImageCapture 回調中加入 EXIF Orientation 標籤寫入
   - 確保照片在所有平台上正確顯示方向

3. **[選擇性] 額外測試**
   - FPS 效能測量（預期 18-22）
   - AI 處理畫質改善驗證

---

**報告版本**: 1.0
**建立日期**: 2026-01-26
**最後更新**: 2026-01-26
