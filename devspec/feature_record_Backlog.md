# Feature Record & Backlog
Last updated: 2026-02-10

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

## Preview / Resolution / Aspect Ratio (v2.10.0)

### ✅ Confirmed
- Preview 固定 16:9 AspectRatioStrategy — 修正 1.6x FOV 放大問題 | Verified: 2026-02-07 (Phase A)
- ImageCapture 同步 16:9 — WYSIWYG 拍照 | Verified: 2026-02-07 (Phase A)
- **長寬比 Toggle (4:3/16:9/1:1)** — UseCaseGroup + ViewPort 架構 | Verified: 2026-02-08 (v2.10.0-fix)
- **ViewPort.FIT** — 保留完整 FOV，避免雙層裁切 | Verified: 2026-02-08 (Sony + Pixel 9a)
- **PreviewView.FIT_CENTER 統一** — 所有模式避免額外裁切 | Verified: 2026-02-08 (Sony + Pixel 9a)
- **1:1 ResolutionFilter** — 動態選擇正方形解析度（Sony: 2448², Pixel 9a: 1920²）| Verified: 2026-02-08

### 🔍 Code Locations
- ViewPort.FIT:                `LiveCameraSource.kt:932-940`
- 1:1 ResolutionFilter:        `LiveCameraSource.kt:868-888`
- PreviewView.ScaleType:       `AspectRatioMode.kt:132-133`
- AspectRatioMode enum:        `AspectRatioMode.kt:19-68`
- Toggle logic:                `CameraViewModel.kt` (aspect ratio state)

### 📋 Backlog
- [x] [Phase B] 長寬比 Toggle UI (4:3/16:9/1:1) — ✅ 完成 (v2.10.0-fix, 2026-02-08)
- [ ] 建立設備解析度資料庫 — 記錄不同設備的 1:1 解析度選擇
- [ ] 新增 UI 提示 — 首次使用說明黑邊（letterbox）是正常行為

---

## LumiFrame AI（景深 + 虛擬光源）

### 📋 Backlog
- [ ] ARCore Depth API 可行性驗證 PoC — pending 評估
- [ ] AGSL bokeh shader 實作 — pending ARCore PoC 成功
- [ ] 視覺協議動畫（搜尋/鎖定/淡出）— pending 設計確認

---

## Photo Capture Feedback (v2.9.8 → v2.10.2)

### ✅ Confirmed
- 照片保存反饋機制 — 移除 Snackbar，改用 UI 視覺反饋 | Verified: 2026-01-30 (v2.9.8)
  - **Why removed**: Snackbar 重複顯示問題（每次進入 CameraScreen 都觸發）
  - **Alternative solution**: 按鈕禁用 + CircularProgressIndicator（60ms 反饋）
  - **User feedback**: 「拍照中/完成皆以快門按鈕動畫為主, 皆以視覺為主」
  - **Test result**: 100% 通過，無用戶抱怨
  - **Final decision**: 保持簡潔 UI，不添加額外按鈕 | Verified: 2026-02-09 (v2.10.2)

### 🔍 Code Locations
- 快門反饋: `CameraScreen.kt:151-154` (v2.10.2 移除 Snackbar)
- 視覺反饋: `CameraScreen.kt:445-483` (按鈕禁用 + 進度指示器)
- 狀態管理: `CameraState.kt:21` (lastCapturedImageUri)

### ⚠️ Critical Warning
**DO NOT re-add Snackbar for "照片已儲存"**
- v2.9.8 already removed it due to repeated display issue
- lastCapturedImageUri state persists without clear mechanism
- LaunchedEffect monitoring this state will trigger on every CameraScreen re-entry
- Use UI visual feedback instead (button disable + progress indicator)
- **Regression history**: v2.10.1 (commit d7db1e5) re-added Snackbar for "查看資訊" feature → caused regression → fixed in v2.10.2

**DO NOT add photo info button**
- User feedback: 「刪除照片資訊按鈕->非用戶使用習慣」| 2026-02-09
- Keep UI minimal - only shutter button with visual feedback
- EXIF info access via PhotoInfoBottomSheet (if needed) can be triggered by long-press or Settings

### 📋 Backlog
- [x] 照片保存 Snackbar 移除 — ✅ 完成 (v2.9.8, 2026-01-30)
- [x] 照片資訊按鈕嘗試 → ❌ 移除（非用戶使用習慣）(v2.10.2, 2026-02-09)
- [ ] (Optional) EXIF 資訊訪問方式重新設計（長按快門按鈕或在 Settings 中查看）

---

---

## Camera Core Features (v2.11.0 / v3.5.0) - 從 v2.9.9 延後

### 📋 Backlog

#### v2.11.0 規劃（2026-04，補完 v2.9.9 原規劃）
- [ ] **對焦功能 (Tap-to-Focus)** - 簡化版
  - CameraX FocusMeteringAction API
  - 點擊對焦圓圈動畫
  - 前後鏡頭支援（檢查 AF 能力）
  - 參考: `docs/plan/v2.9.9-LumiFrame-AI-Focusing-light-shallow-deep.md`
  - ❌ 不含 ML Kit 追蹤、景深虛化（移至 v4.0.0 LumiFrame）

- [ ] **自拍定時器 (3s/5s/10s)**
  - 三段式定時器循環切換
  - 倒數計時 UI（螢幕中央大字）
  - 震動回饋
  - 倒數期間可取消

- [ ] **Zoom 控制 UI** - 簡易版
  - 滑桿控制 + Pinch-to-Zoom 手勢
  - 焦點位置鎖定
  - Zoom 範圍指示（0.5x - 4.0x）

#### v3.5.0 規劃（2026-08）
- [ ] **EXIF 查看器升級** - 完整版
  - 完整 EXIF 參數顯示
  - GPS 增強（座標 + 海拔 + 地址反查）
  - 搜尋和複製功能
  - 單位轉換
  - 升級自 v2.10.0 PhotoInfoBottomSheet

### ❌ 不實現
- 雙擊預覽畫面拍照 (Touch to Capture) - 暫不考慮（誤觸風險高，優先級低）

### 📝 版本校正說明
- 原規劃版本: v2.9.9
- 實際狀況: v2.10.2 未實現
- 重新分配: v2.11.0（相機核心）/ v3.5.0（EXIF 升級）
- 校正文檔: `devspec/VERSION_CORRECTION_v2.9.9_to_v2.11.0.md`
- 版本定位: v2.11.0 = "補完 v2.9.9 原規劃功能"

---

