# v2.9.9 Phase 9B-fix: Front Camera Focus Indicator Jump — 交接摘要

## 📋 版本資訊

- **功能分支**: `feature/optical-flow-phase5-6`
- **基礎版本**: v2.9.9 (versionCode: 60)
- **開發日期**: 2026-02-04 ~ 2026-02-05
- **交接類型**: Home NB → Work NB (實機測試)
- **測試設備**: Pixel 9a (Device ID: `59131JEBF17244`)

---

## 🎯 本次交接範圍

修復前置鏡頭使用 Optical Flow 追蹤時「對焦指標圓圈跳動」的兩個問題：

| 變更項目 | 說明 |
|---------|------|
| **Issue A 修正** | `maxDistancePerFrame` 15f→25f + Spring `StiffnessHigh` — 減少視覺延遲與追趕跳躍 |
| **Issue B guard 移除** | 淡化至 Alpha 0.4 是 Optical Flow 追蹤中的正確行為，已移除錯誤的 `!trackedPointActive` guard |

---

## 🔧 修改檔案 (3 個)

### 1. `OpticalFlowConfig.kt` (line 93) — Issue A
```kotlin
val maxDistancePerFrame: Float = 25f  // Phase 9B-fix: 15f→25f
```
- 下采樣座標中允許每幀最大位移 15f → 25f
- Preview 座標換算：25 × 2 × 1.726 ≈ 86 px/frame（仍在可接受範圍）
- 減少連續幀累積誤差，避免「遲到後一次補齊」的跳躍效果

### 2. `CameraScreen.kt` (lines 532, 545) — Issue A
```kotlin
stiffness = Spring.StiffnessHigh  // Phase 9B-fix: StiffnessMedium→StiffnessHigh
```
- FocusIndicator `animatedX` / `animatedY` 的 Spring 動畫參數
- `StiffnessMedium`(500f) → `StiffnessHigh`(900f)，動畫延遲減少 ~40%
- 圓圈更快跟上目標位置，視覺更光滑

### 3. `CameraViewModel.kt` (line 712) — Issue B guard 移除
```kotlin
if (_state.value.trackedObjectId == null) {
```
- 移除錯誤添加的 `&& !trackedPointActive` guard
- ML Kit 追蹤時（`trackedObjectId != null`）由 `updateTrackedObjectPosition` 獨立管控 Alpha
- Optical Flow 追蹤 + 靜態對焦均執行標準淡出：Alpha 1.0 → 1s → fade → Alpha 0.4

---

## 📋 Fade-out 行為規範

規範已移至 [`devspec/feature_record_Backlog.md`](../../../devspec/feature_record_Backlog.md) — Optical Flow Tracking section。
本交接中 Fade-out 相關的預期行為請參考該文件的 ✅ Confirmed 部分。

---

## ✅ 測試案例（5 項）

| # | 測試項目 | 預期結果 | 優先度 |
|---|---------|---------|--------|
| 1 | 前置鏡頭點擊人臉 | 圓圈穩定跟隨，無可見跳動 | CRITICAL |
| 2 | 前置鏡頭點擊牆壁（無紋理）| 圓圈穩定，無跳動，tracking lost 後正常消失 | HIGH |
| 3 | 快速移動鏡頭（前置）| 圓圈平滑跟隨，無明顯延遲或追趕跳躍 | CRITICAL |
| 4 | 對焦成功後超過 1 秒（無追蹤）| 1 秒後淡化至 Alpha 0.4 並持續顯示 | HIGH |
| 5 | 後置鏡頭測試 | 無回歸，行為與 Phase 9A 一致 | MEDIUM |

詳細測試步驟參考：[`PIXEL9A_TEST_GUIDE.md`](PIXEL9A_TEST_GUIDE.md)

---

## 📂 測試產出存放位置

所有測試產出請放至本目錄 `docs/test_results/v2.9.9-Phase9B-fix/`：

| 檔案 | 說明 |
|------|------|
| `PIXEL9A_TEST_REPORT.md` | 測試報告（Work NB 填寫） |
| `logcat_phase9bfix.log` | Logcat 日誌 |
| `screenshot_*.png` | 截圖 |

---

## 🔗 參考文件

- 功能議題唯一入口 (SSOT): [`devspec/feature_record_Backlog.md`](../../../devspec/feature_record_Backlog.md)
- 詳細測試步驟: [`PIXEL9A_TEST_GUIDE.md`](PIXEL9A_TEST_GUIDE.md)
- 上階段交接摘要: [`../v2.x.x-OpticalFlow/HANDOVER_SUMMARY.md`](../v2.x.x-OpticalFlow/HANDOVER_SUMMARY.md)
