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
- **前置鏡頭 ML Kit 座標對齊與方向性**：預覽方向與實際方向一致，拍照圖片為鏡像（正常行為）。所有方向（Portrait, Landscape Left, Landscape Right）顯示正確 | Verified: 2026-01-26 (v2.9.6 Phase 2.1, Pixel 9a)
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
- [x] ML Kit 前置鏡頭座標鏡像偏移 — ✅ 已驗證正常 (v2.9.6 Phase 2.1) | 測試報告: `docs/test_results/v2.9.6-Phase2.1/Pixel9a_Physical_Test_Report.md`

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

#### v2.11.0 規劃（2026-04，補完 v2.9.9 原規劃 + 方案 B）
- [ ] **對焦功能 (Tap-to-Focus)** - 簡化版
  - CameraX FocusMeteringAction API
  - 點擊對焦圓圈動畫
  - 前後鏡頭支援（檢查 AF 能力）
  - 參考: `plans/v2.9.9-LumiFrame-AI-Focusing-light-shallow-deep.md`
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

- [ ] **完整 EXIF 寫入**（拍照時）
  - ISO 感光度
  - 快門速度 (Shutter Speed)
  - 焦距 (Focal Length)
  - 光圈值 (F-number)
  - 曝光補償 (Exposure Compensation)
  - 白平衡 (White Balance)
  - 使用 Camera2Interop 獲取相機參數
  - 寫入至 ImageCapture.Metadata

- [ ] **微距模式檢測**（可選功能）
  - 檢測裝置是否支援 Macro 模式
  - 裝置不支援時自動隱藏
  - 使用 CameraInfo.hasCapability(CAPABILITY_MACRO)
  - 硬體相依性高，需廣泛設備驗證

- [ ] **連拍模式**（長按快門）
  - 長按快門鍵觸發連拍
  - 高速連續拍攝
  - 用於捕捉運動瞬間或挑選最佳表情

#### v3.5.0 規劃（2026-08）
- [ ] **GPS 精度指示器**
  - 即時顯示 GPS 精度等級（優秀 < 10m / 良好 10-30m / 一般 > 30m）
  - 衛星數量顯示
  - 定位狀態指示（搜尋中 / 已定位 / 無訊號）
  - 相機畫面右上角小圖示

- [ ] **位置資訊即時顯示**
  - GPS 座標（緯度 / 經度）
  - 海拔高度
  - 地址反查（使用 Geocoding API，需網路）
  - 半透明浮動面板（可折疊）

- [ ] **夜間模式**
  - 深色主題適配
  - 低光預覽增強
  - 感光度動態調整

### ❌ 不實現
- 雙擊預覽畫面拍照 (Touch to Capture) - 暫不考慮（誤觸風險高，優先級低）

### 📝 版本校正說明
- 原規劃版本: v2.9.9
- 實際狀況: v2.10.2 未實現
- 重新分配: v2.11.0（相機核心 + 方案 B）/ v3.5.0（GPS 視覺化）
- 校正文檔: `devspec/VERSION_CORRECTION_v2.9.9_to_v2.11.0.md`
- 版本定位: v2.11.0 = "補完 v2.9.9 原規劃功能 + 新增基礎功能"
- 方案選擇: 採用方案 B（積極方案，6 項功能，2-3 週）

---

