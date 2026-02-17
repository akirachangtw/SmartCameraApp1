# Known Issues - v2.11.1 Camera Coordinate System

**Status**: 🔴 In Progress (P0-P3 修復中)
**Created**: 2026-02-16
**Device**: Pixel 9a
**Related Docs**: `docs/features/v2.11.1/v2.11.1_Camera_Autofocus_Issues-ChatGPT.md`

---

## Root Cause Analysis

### 核心問題：座標系統不一致

**現象**：三套不同的座標系統沒有統一映射
1. **視覺 Overlay** - 全螢幕座標（GridOverlay, FocusIndicator）
2. **AI 偵測** - ImageAnalysis/Bitmap 座標（ML Kit Face Detection, Optical Flow）
3. **對焦測光** - SurfaceOrientedMeteringPointFactory 座標（focusOnPoint）

**影響範圍**：
- PreviewView 使用 `ScaleType.FIT_CENTER` + `ViewPort.FIT` → 產生 letterbox 黑邊
- 座標映射缺失 → 導致所有 5 個 Bug

---

## Issue List (按優先級排序)

### 🔴 P0: 座標系統統一 (CRITICAL)

#### Issue #1: 預覽畫面外可以點擊對焦 ⚠️ **NEW**
**發現時間**: 2026-02-16 (實機測試)
**嚴重程度**: CRITICAL
**影響**: UX 異常行為

**描述**:
- 用戶可以在長寬比例的預覽畫面**以外**區域點擊觸發對焦
- 預期行為：只允許在預覽畫面**內**點擊對焦定點
- 實際行為：黑邊區域（letterbox）也可以觸發對焦

**技術原因**:
```kotlin
// LiveCameraSource.kt Line 578-597
override suspend fun focusOnPoint(
    x: Float,
    y: Float,
    viewWidth: Int,  // ⚠️ 全螢幕寬度，包含黑邊
    viewHeight: Int  // ⚠️ 全螢幕高度，包含黑邊
): Result<Unit> {
    // 問題：沒有檢查點擊是否在 contentBounds 內
    val factory = SurfaceOrientedMeteringPointFactory(
        viewWidth.toFloat(),
        viewHeight.toFloat()
    )
    val focusPoint = factory.createPoint(x, y)  // ⚠️ 黑邊區域也會被映射到感光元件
}
```

**修復計劃**: P0-1（使用 PreviewView.meteringPointFactory + contentBounds 檢查）

---

#### Issue #2: Grid 線超出預覽範圍
**嚴重程度**: HIGH
**影響**: 視覺錯亂

**描述**:
- 三分格線在切換長寬比時（1:1, 4:3, 16:9）超出預覽邊界
- 格線延伸到黑邊區域

**技術原因**:
- `GridOverlay` 使用 `fillMaxSize()` 繪製在整個螢幕
- PreviewView 使用 FIT_CENTER（有黑邊）
- 沒有限制格線只繪製在 contentRect 內

**修復計劃**: P0-2 (PreviewViewportContainer) + P2

---

### 🟠 P1: 對焦機制修正

#### Issue #3: Eye-AF 無自動對焦功能
**嚴重程度**: HIGH
**影響**: 核心功能失效

**描述**:
- 前後鏡頭的 Eye-AF 都沒有自動對焦
- Eye landmark 有抓到，但沒有驅動硬體 AF

**技術原因**:
1. Eye 座標（ML Kit）沒有正確轉換到 PreviewView 顯示區
2. 對焦點落到無效區域或 letterbox 黑邊
3. 沒有持續驅動 metering（只更新 UI 框位置）

**修復計劃**: P1（Eye-AF 優先級 +30 + 自適應節流）

---

### 🟡 P2: 狀態機與視覺

#### Issue #4: Smart Subject Focus 狀態機制失效
**嚴重程度**: MEDIUM
**影響**: UX 體驗不佳

**子問題 4.1: 對焦框大小固定**
- 對焦框不會根據物件大小調整（固定 48dp）
- 應該使用 bounding box 動態計算尺寸（48-120dp）

**子問題 4.2: 對焦狀態一直維持「對焦中」**
- `focusSuccess` 無法變成 `true`（座標不準導致對焦失敗）
- 缺少超時機制（應該 3s timeout）

**子問題 4.3: 沒有淡化動畫（Alpha 1.0 → 0.4）**
- Fade 邏輯只在「非 tracking」分支執行
- 一旦進入 Smart Focus/自動追蹤，alpha 維持 1.0

**修復計劃**: P2（狀態機重構 + 自適應框大小 + timeout）

---

#### Issue #5: 未看到雙層結構
**嚴重程度**: LOW
**影響**: 視覺設計不符合規範

**描述**:
- 目前只有單一框 + 角落括號
- 規範要求：外層角落括號 + 內層實線四邊形

**修復計劃**: P2（拆分兩個 geometry 繪製）

---

### 🟢 P3: 追蹤品質

#### Issue #6: 對焦追蹤效果差，容易偏移定點
**嚴重程度**: MEDIUM
**影響**: 追蹤體驗不佳

**技術原因**:
1. CoordinateMapper 假設 CENTER_CROP，但實際是 FIT_CENTER
2. Optical Flow 參數未優化
3. 缺少濾波機制（抖動、漂移）

**修復計劃**: P3（Optical Flow 參數調整 + 濾波 + 熱控監控）

---

## 修復順序

```
P0 (座標系統統一) → P1 (對焦機制) → P2 (狀態機視覺) → P3 (追蹤品質)
```

**預估時間**: 13-15 小時
**涉及檔案**: 8 個
**代碼變動**: 約 380 行

---

## 驗證檢查清單

### P0 驗證
- [ ] Grid 線不超出預覽邊界（1:1, 4:3, 16:9）
- [ ] 黑邊區域無法觸發對焦 ⚠️ **NEW**
- [ ] Eye-AF 對焦點正確映射到預覽位置

### P1 驗證
- [ ] Eye-AF 前鏡頭自動對焦正常
- [ ] Eye-AF 後鏡頭自動對焦正常
- [ ] 節流機制正常（2s 基礎 + Eye 移動例外）

### P2 驗證
- [ ] 對焦框大小隨物件調整（48-120dp）
- [ ] 對焦狀態正確轉換（IDLE → FOCUSING → SUCCESS → DWELL）
- [ ] 淡化動畫正常（Alpha 1.0 → 0.4）
- [ ] 對焦超時保護（3s timeout）
- [ ] 雙層結構顯示（外括號 + 內實線）

### P3 驗證
- [ ] Optical Flow 追蹤穩定（無明顯漂移）
- [ ] 熱控監控正常降級
- [ ] RANSAC 閾值調整有效

---

## References

- **診斷報告**: `docs/features/v2.11.1/v2.11.1_Camera_Autofocus_Issues-ChatGPT.md`
- **修復計劃**: `.claude/plans/delightful-popping-unicorn.md`
- **測試文檔**: `docs/test_results/v2.11.1/`
