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

## Focus Indicator Visual Design (v2.11.1)

### ✅ Confirmed - 保留的視覺語言（僅此五項）
**過去決議**：Focus Indicator 僅使用以下五種視覺語言，其他動畫全部刪除 | Decision: 2026-02-16

1. **機械咬合動畫** — 對焦成功時，四個括號向中心咬合 4dp | Verified: pending v2.11.1
   - **Effect**: 科技感 + 精準感的視覺反饋
   - **Timing**: 對焦成功後觸發（Success → Locked 轉換）
   - **Reference**: `v2.11.0_corner_bracket_feasibility.md` Lines 345-399

2. **瞬間縮放大小（咔嗒動畫）** — 對焦成功瞬間，Scale 1.0 → 0.9 → 1.0 (1000ms) | Verified: pending v2.11.1
   - **Effect**: 模擬快門「咔嗒」聲的視覺節奏感
   - **Timing**: 對焦成功瞬間（Success 階段）

3. **淡化 Alpha** — Dwell 階段 Alpha 1.0 → 0.4，線條從 2.5dp → 1.5dp | Verified: pending v2.11.1
   - **Effect**: 減少視覺干擾，呈現機械咬合的穩定感
   - **Timing**: 對焦成功後 1500-2000ms（持續停留）
   - **Reference**: `v2.11.0_corner_bracket_feasibility.md` Lines 301-306

4. **圖示顏色變化** — 狀態改變時的顏色轉換 | Verified: pending v2.11.1
   - **White**: 一般對焦狀態（Searching, Success, Dwell）
   - **Yellow**: AE/AF Lock 鎖定狀態

5. **Glow 輝光效果** — 靜態霓虹光暈（所有狀態） | Verified: pending v2.11.1
   - **Effect**: 科技感視覺增強，提升介面質感
   - **Implementation**: 使用 BlurMaskFilter 或雙層 Canvas 繪製
   - **Note**: 靜態 Glow，不做呼吸脈衝動畫
   - **Reference**: `v2.11.0_corner_bracket_feasibility.md` Lines 268-277

### ✅ Confirmed - 刪除的視覺語言（不再使用）
**過去決議**：以下動畫全部刪除，不再實作 | Decision: 2026-02-16

- ❌ **呼吸脈衝動畫** — Searching 階段的 Glow Alpha 動態變化（0.2 ↔ 0.3 脈衝）（已刪除）
  - **Reason**: 簡化視覺語言，減少不必要的持續動畫
  - **Clarification**: Glow 輝光效果本身**保留**（靜態光暈），僅刪除 Alpha 呼吸脈衝動畫
  - **Original design**: `v2.11.0_FINAL_RECOMMENDATION.md` Lines 143-171 (Phase 2B)
  - **Final decision**: `v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md` Line 208「動畫: ❌ 無呼吸動畫（已刪除）」

- ❌ **停止呼吸動畫** — 對焦成功時停止呼吸脈衝轉換（已刪除）
  - **Reason**: 呼吸脈衝動畫本身已刪除，此轉換動畫不再需要

- ❌ **其他動畫效果** — 包括但不限於：
  - 抖動（Jitter）
  - 旋轉（Rotation）
  - 複雜的彈性動畫（除了咔嗒縮放和機械咬合外）
  - 任何未在「保留的視覺語言」列表中的動畫

### ⚠️ Critical Warning
**DO NOT re-add deleted animations**
- v2.11.1 design decision: **ONLY 5 visual languages allowed**
  1. Mechanical clamp (向中心咬合)
  2. Instant scale (瞬間縮放)
  3. Alpha fade (淡化透明度 + 線條變細)
  4. Color change (顏色變化)
  5. **Glow effect (靜態輝光，不做呼吸脈衝)**
- **Glow breathing pulse animation** (Alpha 0.2↔0.3) → **removed in v2.11.0/v2.11.1**
  - Static Glow effect (雙層繪製) → **retained** (v2.11.0 FINAL_SALIENCY_AND_VISUAL_DESIGN.md)
- "Stop breathing" transition → **removed (no longer needed)**
- Keep visual language **minimal and focused**
- If adding new animations, must have **explicit user approval** and update this section

### 📚 Reference Documents (v2.11.0 決議)
- **最終設計規範**: `docs/features/v2.11.0/v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md`
  - Line 178: 搜尋中動畫 - 呼吸動畫（Alpha 脈衝）→ ❌ 刪除
  - Line 179: 霓虹輝光 - 雙層繪製（Glow + Core）→ ✅ 保留
  - Lines 387-431: 霓虹輝光效果實作（雙層繪製）
- **評估報告**: `docs/features/v2.11.0/v2.11.0_FINAL_RECOMMENDATION.md`
  - Lines 103-139: Phase 2A 霓虹輝光效果 ⭐ 強烈推薦
  - Lines 143-171: Phase 2B 呼吸訊號動畫 ✅ 推薦實作（但被最終設計推翻）
- **工作流程驗證**: `devspec/GIT_WORKFLOW_PHASE1_IMPLEMENTATION_REPORT.md`
  - Phase 6: Merge Readiness - 文檔同步驗證（Lines 71-95）
  - 驗證工具: `python tools/check_merge_readiness.py`
  - 本次修正符合 MERGE_SOP Phase 2 文檔同步要求

### 🔍 Code Locations
- (Pending implementation) Mechanical clamp animation: `CameraScreen.kt` - FocusIndicator
- (Pending implementation) Dwell stroke animation: `CameraScreen.kt` - FocusIndicator

### 📋 Backlog
- [ ] [v2.11.1] 實作機械咬合動畫（向中心移動 4dp）
- [ ] [v2.11.1] 實作 Glow 輝光效果（雙層繪製：Glow 層 + Core 層）
- [ ] [v2.11.1] 實作 Dwell 線條變細動畫（2.5dp → 1.5dp）
- [ ] [v2.11.1] 實作「咔嗒」縮放動畫（1.0 → 0.9 → 1.0，1000ms）
- [ ] [v2.11.1] 確認顏色變化正確（White → Yellow for Lock）

---

## Camera Core Features (v2.11.0 / v3.5.0) - 從 v2.9.9 延後

### 📋 Backlog

#### v2.11.0 規劃（2026-04，補完 v2.9.9 原規劃 + 方案 B）
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

