# Feature Record & Backlog
Last updated: 2026-02-07

全項目功能開發的唯一議題入口。
- **Confirmed**: 測試驗證過的行為（修改前必須有新測試依據）
- **Code Locations**: 關鍵代碼位置（行號變動時更新此處）
- **Backlog**: 開放議題索引。詳細分析放 `docs/issues/`。新議題進入時填一行，解決後移至 Confirmed 或刪除。
- **新增功能**: 在文件底部追加同樣結構的 section

---

## Optical Flow Tracking

### ✅ Confirmed
- Fade-out: 對焦成功 → Alpha 1.0 (1s) → fade 500ms → Alpha 0.4 持續 | Verified: 2026-02-05
- ML Kit 追蹤中: Alpha 由 `updateTrackedObjectPosition` 獨立管控，不觸發標準淡出 | Verified: 2026-02-05
- maxDistancePerFrame: 25f (downsampled coords) | Verified: pending Pixel 9a
- Spring stiffness: StiffnessHigh (900f) animatedX/Y | Verified: pending Pixel 9a

### 🔍 Code Locations
- Fade guard (`trackedObjectId == null`):  `CameraViewModel.kt:712`
- focusIndicatorFading = true:             `CameraViewModel.kt:714`
- Alpha 0.4 render (`isFading`):           `CameraScreen.kt:567`
- Spring stiffness X / Y:                  `CameraScreen.kt:532, 545`
- maxDistancePerFrame:                     `OpticalFlowConfig.kt:93`

### 📋 Backlog
- [ ] [9B] maxDistancePerFrame 25f — pending Pixel 9a 實機驗證
- [ ] [9B] Spring StiffnessHigh — pending Pixel 9a 實機驗證
- [ ] ML Kit 前置鏡頭座標鏡像偏移 — pending 驗證 | 詳細: `docs/issues/front_camera_mlkit_mirror.md`

---

## Preview / Resolution

### ✅ Confirmed
- Preview 固定 16:9 AspectRatioStrategy — 修正 1.6x FOV 放大問題 | Verified: 2026-02-07 (Phase A)
- ImageCapture 同步 16:9 — WYSIWYG 拍照 | Verified: 2026-02-07 (Phase A)

### 🔍 Code Locations
- Preview selector (16:9):    `LiveCameraSource.kt:858-865`
- ImageCapture selector (16:9): `LiveCameraSource.kt:889-896`
- ImageAnalysis selector (原有): `LiveCameraSource.kt:868-886`

### 📋 Backlog
- [ ] [Phase B] 長寬比 Toggle UI (4:3/16:9/1:1) — pending 設計與實作

---

## LumiFrame AI（景深 + 虛擬光源）

### 📋 Backlog
- [ ] ARCore Depth API 可行性驗證 PoC — pending 評估
- [ ] AGSL bokeh shader 實作 — pending ARCore PoC 成功
- [ ] 視覺協議動畫（搜尋/鎖定/淡出）— pending 設計確認

---
