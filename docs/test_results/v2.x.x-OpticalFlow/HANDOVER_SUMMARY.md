# v2.x.x Optical Flow Point Tracking - 交接摘要

## 📋 版本資訊

- **功能分支**: `feature/optical-flow-phase5-6`
- **基礎版本**: v2.9.9-Day1-2 (versionCode: 60)
- **開發日期**: 2026-02-02 ~ 2026-02-03
- **交接類型**: Home NB → Work NB (實機測試)
- **測試設備**: Pixel 9a

## 🎯 功能概述

本功能實作 **Optical Flow Point Tracking（光學流動點追蹤）**，使用 OpenCV Lucas-Kanade 稀疏光學流算法，讓使用者點擊預覽畫面任意一點後，對焦指標可連續跟隨該視觉特徵點移動。

### 追蹤優先順序（三模式）
1. **ML Kit 追蹤** — 點擊已偵測到的人臉/物件 → 使用 ML Kit bounding box 跟隨
2. **Optical Flow 追蹤** — 點擊空白處（無 ML Kit 目標）→ 使用 LK 光學流動跟隨
3. **靜態對焦** — 若 Optical Flow 也失敗 → 傳統 tap-to-focus

## 📦 Phase 1-6 實作範圍

| Phase | 內容 | 狀態 |
|-------|------|------|
| Phase 1 | CameraState 擴展 (trackedPointActive/Quality/Lost) | ✅ 完成 |
| Phase 2 | OpticalFlowTracker + OpticalFlowConfig (LK 核心) | ✅ 完成 |
| Phase 3 | CoordinateMapper (PreviewView ↔ ImageAnalysis 座標轉換) | ✅ 完成 |
| Phase 4 | SmartCameraAnalyzer 整合 (ImageProxy + Bitmap 雙管道) | ✅ 完成 |
| Phase 5 | CameraViewModel 整合 (tap handler 三模式路由 + 狀態回調) | ✅ 完成 |
| Phase 6 | 模擬器驗證 (虛擬3D相機 + prevGray bug 修復) | ✅ 完成 |
| Phase 7 | Focus indicator fade-out animation — Alpha priority + tracking lost | ✅ 完成 |
| Phase 8 | Multi-point LK optical flow tracking | ✅ 完成 |
| Phase 9A | isJitterFrame feedback loop removal | ✅ 完成 |
| Phase 9B-fix | Front camera focus indicator jump (spring stiffness + velocity clamp) | ✅ 本次交接 |

## 🔧 Phase 6 具體變更（本次交接）

### 修改檔案 (3 個)

1. **`app/build.gradle.kts`** (line 108)
   - emulator flavor: `MOCK_CAMERA_DATA` 從 `true` 改為 `false`
   - 目的：讓模擬器使用虛擬 3D 相機（LiveCameraSource）進行光學流動測試

2. **`app/src/main/.../di/CameraModule.kt`**
   - 簡化 `provideCameraInputSource()` 為單一 `MOCK_CAMERA_DATA` 開關判斷
   - 移除 `isRunningOnEmulator()` 運行時偵測邏輯（不再需要）

3. **`app/src/main/.../data/opencv/OpticalFlowTracker.kt`** (line 77)
   - `startTracking()` 開始時加入 `releasePreviousFrame()`
   - **Bug 修復**：Re-tap 時殘留的舊 `prevGray` 會導致 LK 在錯誤框架中搜尋新點 → status=0 即失蹤
   - 修復後 re-tap 可正確切換追蹤目標

### Feature Flag
- `BuildConfig.ENABLE_OPTICAL_FLOW_TRACKING` = `true`（debug + release 均開啟）
- 由 `FeatureFlags.enableOpticalFlowTracking` 包裝

## ✅ 模擬器驗證結果 (Home NB - gphone64 API 36)

| 測試項目 | 結果 | 數據 |
|----------|------|------|
| 紋理區域追蹤穩定性 | ✅ PASS | 連續 19+ 秒, quality=0.99-1.00 |
| Re-tap 目標切換 | ✅ PASS | 修復前 instant fail → 修復後正確切換 |
| 無紋理區域處理 | ✅ PASS | 正確報告 tracking lost (LK 預期行為) |
| LK 追蹤延遲 | ✅ PASS | P50=2ms, P95<37ms (目標 <20ms P95) |
| FPS / Frame Drop | ✅ PASS | 15-21 FPS, 0% drop |
| 記憶體穩定 | ✅ PASS | RSS 241-265MB, 無泄漏detected |
| 熱管理 | ✅ PASS | Thermal=NONE |

## 🎯 實機測試重點 (Work NB - Pixel 9a)

模擬器使用虛擬3D相機（靜止場景），實機需驗證：

1. **動態場景追蹤** — 手持移動相機時，追蹤點隨視觉特徵移動
2. **Re-tap 快速切換** — 在追蹤活躍中迅速點另一位置
3. **追蹤丧失與恢復** — 物件移出畫面後的行為
4. **Pixel 9a 效能** — 注意該設備 ML Kit TPU 記憶體限制（500ms throttle）
5. **與 Traycer v2 整合** — 確認 ML Kit 追蹤優先於 Optical Flow
6. **AE/AF Lock 倂存** — 長按鎖定後再點擊是否正確啟動 Optical Flow

---

## 🔧 Phase 9B-fix 測試重點（本次交接 — 前置鏡頭對焦指標跳動修正）

### 變更內容

**Issue A — 視覺延遲 + 跳動**
- `OpticalFlowConfig.kt`: `maxDistancePerFrame` 15f → **25f**（減少每幀跳動幅度）
- `CameraScreen.kt` FocusIndicator: spring `StiffnessMedium` → **StiffnessHigh**（動畫延遲減少 ~40%）

**Issue B — Alpha 錯誤淡出**
- `CameraViewModel.kt`: 淡出 guard 加入 `&& !trackedPointActive`
- 防止 optical flow 追蹤中錯誤觸發淡出序列

**Fade-out 正確行為（已修正，與代碼一致）：**
```
對焦成功 → Alpha 1.0 保持 1 秒 → 500ms 淡化 → Alpha 0.4 持續顯示（下次點擊重置）
```

### 測試案例

| # | 測試項目 | 預期結果 |
|---|---------|---------|
| 1 | 前置鏡頭點擊人臉 | 圓圈穩定跟隨，無可見跳動 |
| 2 | 前置鏡頭點擊牆壁（無紋理）| 圓圈穩定，無跳動 |
| 3 | 快速移動鏡頭（前置）| 圓圈平滑跟隨，無明顯延遲 |
| 4 | 對焦成功後超過 1 秒（無追蹤）| 1 秒後淡化至 Alpha 0.4 並持續顯示 |
| 5 | 後置鏡頭測試 | 無回歸，行為與之前相同 |

### Logcat 監測
```bash
adb -s 59131JEBF17244 logcat -s OpticalFlowTracker:V CameraViewModel:V | grep -E "Velocity clamped|focusIndicatorFading"
```

---

## 📝 重要技術背景

### LK 追蹤限制
Lucas-Kanade 依賴**局部紋理特徵**進行追蹤。以下區域**無法追蹤**（正常行為）：
- 均勻色彩的天空、墻面、白色物體表面
- 反射面（鏡子、螢幕）
- 完全黑暗區域

**建議測試點位**：物件邊緣、人體服裝、背景物件等紋理豐富處。

### 座標映射
- PreviewView 觸碰座標 → `CoordinateMapper.previewToImage()` → 圖像空間 (480x640)
- OpticalFlowTracker 內部下采樣至 640x480 進行 LK 計算
- 追蹤結果回映射至 PreviewView 座標 → UI 更新

### 雙管道架構
- **ImageProxy 管道**: SmartCameraAnalyzer.analyze() — ML Kit + 原本 LK 路徑
- **Bitmap 管道**: CameraViewModel 的 observePreviewForAnalysis() → trackFromBitmap() — Phase 6 新增的 LK 追蹤入口
- 兩管道均可觸發 LK 追蹤，Bitmap 管道為模擬器的主要入口

## 📂 測試產出存放位置

所有測試產出請放至：`docs/test_results/v2.x.x-OpticalFlow/`
- 測試報告: `PIXEL9A_TEST_REPORT.md`
- Logcat: `logcat_optical_flow.log`
- 截圖: `screenshot_*.png`

## 🔗 參考

- 本文件為單一真實來源 (SSOT)
- 詳細測試步驟: [`PIXEL9A_TEST_GUIDE.md`](PIXEL9A_TEST_GUIDE.md)
