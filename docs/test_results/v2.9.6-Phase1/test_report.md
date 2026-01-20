# v2.9.6 Phase 1 測試報告

**測試時間**: 2026-01-19 21:45 - 22:10
**測試裝置**: Google Pixel 9a (59131JEBF17244)
**Android 版本**: 16
**APK 版本**: v2.9.6-phase1 (Build 52)
**測試執行者**: Claude Code Assistant

---

## 測試摘要

| 測試階段 | 結果 | 說明 |
|---------|------|------|
| Phase 1: 初始化驗證 | **PASS** | 5 個關鍵 log 全部出現 |
| Phase 2: OrientationListener 觸發 | **PASS** | 手動旋轉時正確響應 |
| Phase 3: 照片方向測試 | **FAIL** | EXIF Orientation 為空 |

**整體結果**: ❌ **FAIL** - 發現 2 個問題需要修復

---

## Phase 1: 初始化驗證

### 測試目標
驗證 OrientationEventListener 在應用啟動時正確初始化

### 測試結果: ✅ PASS

### 關鍵 Log 驗證

| Log | 狀態 | 數值 |
|-----|------|------|
| `Camera bound successfully` | ✅ 出現 | - |
| `Camera sensor orientation` | ✅ 出現 | 90° |
| `Initial device rotation` | ✅ 出現 | 0 (Portrait) |
| `OrientationEventListener enabled` | ✅ 出現 | - |
| `Initial targetRotation set to` | ✅ 出現 | 0 |

### 完整初始化 Log
```
21:52:42.175 I LiveCameraSource: ✅ Camera bound successfully: Preview + ImageAnalysis + ImageCapture
21:52:42.175 I LiveCameraSource: Camera sensor orientation: 90°
21:52:42.175 I LiveCameraSource: Initial device rotation: 0
21:52:42.177 I LiveCameraSource: OrientationEventListener enabled
21:52:42.177 D LiveCameraSource: ImageCapture targetRotation updated: 0
21:52:42.177 I LiveCameraSource: Initial targetRotation set to: 0
21:52:42.252 I LiveCameraSource: OrientationEventListener first trigger confirmed
```

---

## Phase 2: OrientationListener 觸發測試

### 測試目標
驗證 OrientationEventListener 響應裝置旋轉

### 測試結果: ✅ PASS

### 測試操作
1. 手動旋轉裝置: Portrait → Landscape Right → Portrait

### 關鍵 Log
```
21:53:34.695 D LiveCameraSource: ImageCapture targetRotation updated: 3
21:53:34.695 D LiveCameraSource: Orientation changed: 61° → rotation=3
```

### 驗證點
- ✅ `OrientationEventListener first trigger confirmed` - 已出現
- ✅ `Orientation changed: XXX° → rotation=Y` - 正確觸發
- ✅ `targetRotation updated` - ImageCapture rotation 已更新

---

## Phase 3: 照片方向測試

### 測試目標
驗證照片 EXIF Orientation 符合預期值

### 測試結果: ❌ FAIL

### 拍攝照片

| 測試案例 | 檔名 | 尺寸 |
|---------|------|------|
| TC-001: Portrait (0°) | 2026-01-19-22-01-11-961.jpg | 4000x3000 |
| TC-002: Landscape Left | 2026-01-19-22-04-06-856.jpg | 4000x3000 |
| TC-003: Landscape Right | 2026-01-19-22-07-23-674.jpg | 4000x3000 |

### EXIF 分析結果

| 測試案例 | 預期 EXIF | 實際 EXIF | 結果 |
|---------|----------|----------|------|
| TC-001: Portrait | 1 (Normal) | **None** | ❌ FAIL |
| TC-002: Landscape Left | 6 (Rotate 90° CW) | **None** | ❌ FAIL |
| TC-003: Landscape Right | 8 (Rotate 90° CCW) | **None** | ❌ FAIL |

### 問題分析

**根本原因**: 照片儲存時沒有寫入 EXIF Orientation

**問題位置**: `CameraRepositoryImpl.kt` 的 `saveBitmapToMediaStore()` 函數

```kotlin
// 目前的實作 - 只使用 bitmap.compress()，不會保留 EXIF
val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
```

**修復建議**: 需要使用 `ExifInterface` 在儲存照片後寫入 Orientation tag

---

## 額外發現的問題

### Issue #1: 橫向旋轉時預覽畫面不正確

**問題描述**: 當手機從 Portrait 旋轉到 Landscape Right 時，預覽畫面被向右旋轉 90 度

**詳細資訊**: 參見 `preview_issue_log.md`

**可能原因**:
- Preview 的 `imageProxy.imageInfo.rotationDegrees` 更新後，`transformBitmap` 的旋轉邏輯可能需要調整
- Activity 鎖定 Portrait 模式對 Preview 旋轉計算的影響

---

## 測試產出檔案

| 檔案 | 說明 |
|------|------|
| `photos/SmartCamera/2026-01-19-22-01-11-961.jpg` | Portrait 測試照片 |
| `photos/SmartCamera/2026-01-19-22-04-06-856.jpg` | Landscape Left 測試照片 |
| `photos/SmartCamera/2026-01-19-22-07-23-674.jpg` | Landscape Right 測試照片 |
| `preview_issue_log.md` | 預覽畫面問題記錄 |
| `test_report.md` | 本測試報告 |

---

## 待修復項目

### 高優先級

1. **[BUG] 照片無 EXIF Orientation**
   - 位置: `CameraRepositoryImpl.saveBitmapToMediaStore()`
   - 修復: 使用 `ExifInterface` 寫入 Orientation tag
   - 需要: 從 LiveCameraSource 取得當前 rotation 值

2. **[BUG] 橫向旋轉預覽畫面不正確**
   - 位置: `LiveCameraSource.transformBitmap()`
   - 分析: 需進一步調查 rotationDegrees 與實際旋轉角度的關係

---

## 結論

v2.9.6 Phase 1 的 OrientationEventListener 初始化和觸發機制已**正確實作**，但照片儲存流程缺少 EXIF Orientation 寫入，導致照片方向資訊遺失。

**建議**: 在進行下一階段前，先修復 EXIF Orientation 問題。

---

**報告生成時間**: 2026-01-19 22:10
**報告版本**: 1.0
