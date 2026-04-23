# Cueshot 工作交接 — 20260423_2249

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| 無代碼變更 | — | 本次 Session 為純偵察診斷，無實際程式碼修改 |

---

## 偵察診斷結果彙整

### I1 — Edge Warmth / Sensor Gate 問題

| 項目 | 結論 |
|------|------|
| Pixel 9a | `MMC5616 Magnetometer accuracy=3` ✅，`tracking started` ✅，管線正常 |
| Sony XQ-BT52 | `mmc56x3x Magnetometer accuracy=3` ✅，`tracking started` ✅，**本次 Session 無法復現** |
| STAB-02 的 I1 | magnetometer 不可用為偶發暫態（CamX HAL 衝突），非穩定復現 |
| 修復決策 | **擱置**，待下次 Sony 復現再處理 |

**三層修復方案（備用，待 Sony 再次失敗時實作）**：
1. `DeviceSensorManagerImpl.areSensorsAvailable()` L97：`return hasAccelerometer`（去除 magnetometer 強制要求）
2. `startOrientationTracking()` L151：magnetometer != null 才 registerListener
3. `updateOrientationAngles()` L177：magnetometer 無效時用 `atan2(-accX, accZ)` fallback 計算 roll

### 左旋轉拍照驗證

| 裝置 | 結果 |
|------|------|
| Pixel 9a | ✅ 3840x2160 橫式正確，`currentRotation=1 (ROTATION_90)` 正常 |
| Sony XQ-BT52 | ✅ 3840x2160 橫式正確，targetRotation `0→1` 序列正常 |

### I6 — focusOnPoint L690 Exception（兩台均出現）

```
E/LiveCameraSource: Caused by: java.lang.IllegalArgumentException:
    None of the specified AF/AE/AWB MeteringPoints is supported on this camera.
    at FocusMeteringControl.startFocusAndMeteringInternal (L356)
```
非功能阻塞，但 Pixel 9a 也會出現（非僅 Sony 熱節流）。根因：MeteringPoint 座標偶爾超出感光元件有效區域。

### I2 — 綠色四角對焦框（完整根因確認）

**來源**：`FocusIndicatorOverlay.kt`（已掛載於 `CameraScreen.kt` L379）

**State Machine 分析**：

| State | 函數 | 形狀 | SSOT 合規 |
|-------|------|------|----------|
| State 1: Searching | `drawSearchingBase()` | 3層同心圓環 | ✅ 符合圓形規格 |
| State 2: Tracking | `drawTrackingFrame()` | Inner方框 + Outer四角L-bracket (cornerLen=32dp) | ❌ 違規 |
| State 3: Locked | `drawLockedFrame()` | 15%填充方框 + Inner邊框 + Outer四角L-bracket (6dp) | ❌ 違規 |
| Secondary Boxes | `overlayModel.secondaryBoxes` foreach | 四角括號 (bracketSize=10dp) | ❌ 違規 |

**CSO 裁決待定**（三個選項）：
- **選項 A（最小）**：State 1 保留，State 2/3 方框改圓形替代，Secondary boxes 刪除
- **選項 B（焦土）**：State 1 保留，State 2/3 全刪（由 HaloGuidanceLayer 圓環承擔）
- **選項 C（快速）**：State 1 + State 3 保留，State 2 Tracking frame + Secondary boxes 刪除

---

## 當前阻塞點

無（I1 暫態問題已擱置，I2 待 CSO 裁決後實作）

---

## 下一步待辦

1. **CSO 裁決 I2 修復選項**（A/B/C 三擇一）後，執行 `FocusIndicatorOverlay.kt` 修改
2. **I2 實作目標檔案**：
   - `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/FocusIndicatorOverlay.kt`
   - 刪除 `drawTrackingFrame()` 方框/L-bracket（依裁決選項）
   - 刪除 `drawLockedFrame()` 方框/L-bracket（依裁決選項）
   - 刪除 Secondary boxes forEach 區塊（L239-258）
3. **I3/I4 偵查**：讀取 `LevelIndicator.kt` 完整實作，確認 `tiltOffset` 計算與 `isLevel` 色彩邏輯
4. **I5 偵查**：HDR 中文 hardcode → `HDRWarningOverlay.kt` 改用 `stringResource(R.string.hdr_warning)`
5. **I6 調查**：`LiveCameraSource.kt` L690 `focusOnPoint()` MeteringPoint 座標邊界防禦

---

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| I1 三層修復暫擱置 | Sony 本次 magnetometer 可用，無法穩定復現，避免過度修復 | `DeviceSensorManagerImpl.kt` |
| I2 根因確認為 `FocusIndicatorOverlay` State 2/3 | grep + Read 確認 DetectionOverlay 已移除掛載，四角框來自 `drawTrackingFrame` + `drawLockedFrame` + `secondaryBoxes` | `FocusIndicatorOverlay.kt` |
| I2 修復選項提交 CSO 裁決 | State 1 圓環符合 SSOT，State 2/3 違規但涉及 tap-to-focus UX，需 CSO 判斷保留程度 | `FocusIndicatorOverlay.kt` |

---

## 🎯 Context 指標針（Context Pointers）

（新 Session Agent 請主動執行以下指令，**嚴禁依賴歷史記憶**）

1. **確認無新 commit（本次 Session 零代碼變更）**：
   ```bash
   git log --oneline -5
   ```
   預期最新 commit 仍為 `0eff91b [Docs] STAB-01/02`

2. **核心檔案最新狀態**：
   - Read `.claude/PROGRESS.md`（確認 STAB-03 狀態）
   - Read `CLAUDE.local.md`（ADB device ID）

3. **I2 修復目標檔案**（CSO 裁決後立即實作）：
   ```bash
   sed -n '230,260p' app/src/main/java/com/akirachang/ai/photographer/presentation/camera/FocusIndicatorOverlay.kt
   # Secondary boxes 區段（待刪除）
   ```

4. **I3/I4 偵查起點**：
   ```bash
   cat app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/LevelIndicator.kt
   ```

5. **I5 偵查起點**：
   ```bash
   grep -n "HDR\|hdr\|中文\|警告" app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/HDRWarningOverlay.kt
   ```

6. **I6 調查起點**：
   ```bash
   sed -n '680,700p' app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt
   ```

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（最高憲法）
- `.claude/PROGRESS.md` — 當前狀態（STAB-03 進行中）
- `CLAUDE.local.md` — ADB device ID
- `app/src/main/java/.../presentation/camera/FocusIndicatorOverlay.kt` — I2 修復目標（CSO 裁決後）
- 本交接文件（STAB-03 問題清單 + I2 State Machine 分析）

---

## 不需帶入的 Context

- 本次所有 logcat 原始輸出（結論已彙整至上方表格）
- I1 三層修復方案完整偵察過程（根因確認、sensor fusion 邏輯分析、getRotationMatrix 路徑）
- 左旋轉拍照 logcat 詳細數據（結論：兩台均正常）
- DeviceSensorManagerImpl 完整源碼分析（結論已記錄，備用修復方案已列出）
- FocusIndicatorOverlay State Machine 偵察過程（結論已整理至「三選項」裁決表）
