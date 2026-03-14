# Feature Record & Backlog
Last updated: 2026-02-25

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
- maxDistancePerFrame: 40f (downsampled coords) | Verified: 2026-02-21 (v2.11.3, Pixel 9a) — 原 25f→40f，logicCenter 分離後 UI 可更快跟隨
- **Spring stiffness: StiffnessMedium animatedX/Y** | Verified: pending Pixel 9a (v2.11.4)
  - 原 StiffnessHigh 回彈速度過快（Sony 設備實測），改為 StiffnessMedium 使追蹤動畫更流暢
  - DampingRatioNoBouncy 保留（無振盪，純滑動跟隨）

### 🔍 Code Locations
- Fade guard (`trackedObjectId == null`):  `CameraViewModel.kt:712`
- focusIndicatorFading = true:             `CameraViewModel.kt:714`
- Alpha 0.4 render (`isFading`):           `CameraScreen.kt:567`
- Spring stiffness X / Y:                  `CameraScreen.kt:570, 581` (v2.11.4: StiffnessMedium)
- maxDistancePerFrame:                     `OpticalFlowConfig.kt:95`

### 📋 Backlog
- [x] [9B] maxDistancePerFrame: 25f→40f — ✅ 完成 (v2.11.3, 2026-02-21, Pixel 9a) | logicCenter 分離消除漂移風險
- [x] [9B] Spring Stiffness 調整 — ✅ StiffnessHigh→StiffnessMedium (v2.11.4, 2026-02-21) | Sony 設備追蹤動畫過快問題修復
- [x] ML Kit 前置鏡頭座標鏡像偏移 — ✅ 已驗證正常 (v2.9.6 Phase 2.1) | 測試報告: `docs/test_results/v2.9.6-Phase2.1/Pixel9a_Physical_Test_Report.md`

---

## Preview / Resolution / Aspect Ratio (v2.10.0)

### ✅ Confirmed
- Preview 固定 16:9 AspectRatioStrategy — 修正 1.6x FOV 放大問題 | Verified: 2026-02-07 (Phase A)
- ImageCapture 同步 16:9 — WYSIWYG 拍照 | Verified: 2026-02-07 (Phase A)
- **長寬比 Toggle (4:3/16:9)** — UseCaseGroup + ViewPort 架構 | Verified: 2026-02-08 (v2.10.0-fix)
- **ViewPort.FIT** — 保留完整 FOV，避免雙層裁切 | Verified: 2026-02-08 (Sony + Pixel 9a)
- **PreviewView.FIT_CENTER 統一** — 所有模式避免額外裁切 | Verified: 2026-02-08 (Sony + Pixel 9a)
- ~~**1:1 ResolutionFilter**~~ — **已移除**（v2.12.0，commit b93d181）：1:1 為數位裁切非 sensor 原生；Pixel 9a 1920×1920 ImageProxy 造成 FPS 崩潰

### 🔍 Code Locations
- ViewPort.FIT:                `LiveCameraSource.kt:932-940`
- PreviewView.ScaleType:       `AspectRatioMode.kt:132-133`
- AspectRatioMode enum:        `AspectRatioMode.kt:19-68` （僅 RATIO_4_3 / RATIO_16_9）
- Toggle logic:                `CameraViewModel.kt` (aspect ratio state)

### 📋 Backlog
- [x] [Phase B] 長寬比 Toggle UI (4:3/16:9) — ✅ 完成 (v2.10.0-fix, 2026-02-08)
- [x] ~~建立設備解析度資料庫 — 記錄不同設備的 1:1 解析度選擇~~ — 已取消（1:1 移除）
- [ ] 新增 UI 提示 — 首次使用說明黑邊（letterbox）是正常行為

---

## Camera UI 清理 (v2.11.1)

### ✅ Confirmed
- **移除 Debug 文字** — CameraPreview.kt 左上角綠色 "PreviewView Mode (Native GPU Rendering)" 已移除 | Verified: 2026-02-17 (Pixel 9a)
  - **Why removed**: Debug 模式字串不應出現在正常操作畫面
  - **File**: `CameraPreview.kt` - NativePreviewPath composable

- **移除 Pixel 9a 記憶體訊息** — CameraViewModel.kt 底部中央的 Pixel 9a 記憶體優化訊息已移除 | Verified: 2026-02-17 (Pixel 9a)
  - **Why removed**: 設備特定訊息對使用者無意義，造成視覺干擾
  - **File**: `CameraViewModel.kt` - init block Pixel 9a detection block

- **長寬比 UI 簡化（Settings）** — SettingsScreen.kt 長寬比選項改為單行並排 + 簡短標籤 | Verified: 2026-02-17 (Pixel 9a)
  - **Before**: 垂直排列（3 行），標籤含長說明文字（如 "1:1 - 正方形"）
  - **After (v2.11.1)**: 水平單行並排（`◯ 4:3  ◯ 16:9  ◯ 1:1`），標籤僅顯示比例數字
  - **After (v2.12.0)**: 移除 1:1，僅保留（`◯ 4:3  ◯ 16:9`）— commit b93d181
  - **File**: `SettingsScreen.kt` - AspectRatio section

### ⚠️ Critical Warning
**DO NOT re-add any of the following**:
- ❌ 任何 DEBUG 字串或模式標示覆蓋在相機預覽畫面上（包括 "PreviewView Mode"、"Bitmap Mode" 等）
  - 例外：`BitmapPreviewPath` 中的 "Bitmap Mode\n(Emulator Fallback)" 保留（模擬器測試用）
- ❌ Pixel 9a（或任何特定設備）的記憶體/效能優化訊息
- ❌ 長寬比設定的長說明文字（維持簡短比例標籤）
- ❌ 垂直排列長寬比選項（維持水平單行）

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

### ✅ Confirmed - 保留的視覺語言（僅此四項）
**過去決議**：Focus Indicator 僅使用以下四種視覺語言，其他動畫全部刪除 | Decision: 2026-02-16

1. **機械咬合動畫（「咔嗒」節奏感）** — 對焦成功時，Scale 1.0 → 0.75 → 1.2 → 1.0（三階段 tween） | Verified: 2026-02-18 (Pixel 9a)
   - **Effect**: 模擬快門「咔嗒」聲的視覺節奏感，傳達「對焦成功」
   - **Timing**: 對焦成功瞬間（Success 階段）
   - **Duration**: **350ms**（Phase 1: 150ms 縮小 + Phase 2: 120ms 彈回超過 + Phase 3: 80ms 落定）
   - **Animation**: 3-phase explicit tween（取代 spring，確保彈回可見）
     - Phase 1: 1.0 → 0.75（150ms, FastOutLinearInEasing）— 咔嗒縮小
     - Phase 2: 0.75 → 1.2（120ms, FastOutSlowInEasing）— 明顯彈回超過原始大小
     - Phase 3: 1.2 → 1.0（80ms, LinearOutSlowInEasing）— 穩定落定
   - **⚠️ v2.11.0 設計問題**: 原定義為 1000ms（500ms+500ms），但與「咔嗒」描述矛盾
   - **✅ v2.11.1 修正**: D-008 決議修正為 350ms；spring 動畫振幅不足（~5-13%）→ 改用 3-phase tween
   - **Reference**:
     - v2.11.0 原設計: `v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md` Lines 228-277
     - v2.11.1 修正: `DEVELOPMENT_DECISION_LOG_v2.11.1.md` D-008 決議

2. **淡化 Alpha + 線條變細** — Dwell 階段 Alpha 1.0 → 0.4，線條從 2.5dp → 1.5dp | Verified: 2026-02-17 (Pixel 9a)
   - **Effect**: 減少視覺干擾，呈現機械咬合的穩定感
   - **Timing**: 對焦成功後 1500-2000ms（持續停留）
   - **Duration**: 500ms（同步動畫）
   - **Reference**: `v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md` Lines 287-318

3. **圖示顏色變化** — 狀態改變時的顏色轉換 | Verified: 2026-02-17 (Pixel 9a)
   - **Cyan** (#00E5FF): 一般對焦狀態（Searching, Success, Dwell）
   - **Cyber Yellow** (#FFD700): AE/AF Lock 鎖定狀態
   - **Reference**: `v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md` Lines 339-344

4. **Glow 輝光效果（靜態霓虹光暈）** — 所有狀態統一應用 | Verified: 2026-02-17 (Pixel 9a)
   - **Effect**: 科技感視覺增強，提升介面質感（「懸浮光束」美學）
   - **Implementation**: 雙層 Canvas 繪製（Glow 層 alpha=0.3 + Core 層）
   - **Note**: **靜態 Glow**，不做呼吸脈衝動畫（Alpha 動態變化已刪除）
   - **Reference**: `v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md` Lines 387-431

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
- v2.11.1 design decision: **ONLY 4 visual languages allowed**
  1. **Mechanical clamp / 「咔嗒」animation** (Scale 1.0→0.75→1.2→1.0, 3-phase tween, 350ms)
     - ⚠️ NOT 1000ms (v2.11.0 design flaw, corrected by D-008)
     - ⚠️ NOT spring animation (visually imperceptible bounce ~5-13%) → use explicit 3-phase tween
  2. **Alpha fade + Stroke thinning** (Alpha 1.0→0.4, Stroke 2.5dp→1.5dp)
  3. **Color change** (Cyan ↔ Cyber Yellow)
  4. **Static Glow effect** (雙層繪製，不做呼吸脈衝)
- **Glow breathing pulse animation** (Alpha 0.2↔0.3) → **removed in v2.11.0/v2.11.1**
  - Static Glow effect (雙層繪製) → **retained** (v2.11.0 FINAL_SALIENCY_AND_VISUAL_DESIGN.md)
- **"Stop breathing" transition** → **removed (no longer needed)**
- **"Bracket clamp 4dp" as separate animation** → **removed (misunderstanding, see D-007/D-008)**
  - Clarification: "Mechanical clamp" = Scale animation, NOT separate bracket animation
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
- Mechanical clamp animation: `CameraScreen.kt:600-621` - LaunchedEffect(focusSuccess) 3-phase tween
- Other states (searching/locked): `CameraScreen.kt:623-633` - LaunchedEffect(isFocusing, isLocked)
- Dwell stroke animation: `CameraScreen.kt:636-640` - animatedStrokeWidth
- Dwell alpha animation: `CameraScreen.kt:645-655` - alpha animateFloatAsState (isFading 優先於 isTracking)
- Color change: `CameraScreen.kt:658-661` - color val (Cyan / Cyber Yellow)
- Glow dual-layer: `CameraScreen.kt:666+` - Canvas drawRect ×2

### 📋 Backlog
- [x] [v2.11.1] 實作機械咬合動畫（3-phase tween: Scale 1.0→0.75→1.2→1.0，350ms total）| Verified: 2026-02-18 (Pixel 9a)
  - Phase 1: 縮小 1.0→0.75（150ms, FastOutLinearInEasing）— 咔嗒感
  - Phase 2: 彈回超過 0.75→1.2（120ms, FastOutSlowInEasing）— 明顯擴張可見
  - Phase 3: 落定 1.2→1.0（80ms, LinearOutSlowInEasing）— 穩定
  - ⚠️ 原 spring 動畫（DampingRatioLowBouncy + stiffness=400）振幅不足，改用 explicit tween
- [x] [v2.11.1] 實作 Glow 輝光效果（雙層繪製：Glow 層 alpha=0.3 + Core 層）| Verified: 2026-02-17 (Pixel 9a)
- [x] [v2.11.1] 實作 Dwell 淡化動畫（Alpha 1.0→0.4 + 線條 2.5dp→1.5dp，同步 500ms）| Verified: 2026-02-17 (Pixel 9a)
- [x] [v2.11.1] 機械咬合時長修正：1000ms → 350ms（D-008 決議）| Verified: 2026-02-17 (Pixel 9a)
- [x] [v2.11.1] 確認顏色變化正確（Cyan #00E5FF → Cyber Yellow #FFD700 for Lock）| Verified: 2026-02-17 (Pixel 9a)

---

## Smart Subject Focus / Eye-AF Auto-Focus (v2.11.4)

### ✅ Confirmed
- **saliency score 公式修復**: `normalizedSize × 60f + centrality × 40f`（0-100 範圍）| Verified: pending Pixel 9a (v2.11.4)
  - **Before (Bug)**: 0.6f/0.4f weights → base 0-1, face +30 → max ~31f，threshold `> 50f` 永遠無法達到 → auto-focus **從未觸發**
  - **After (Fix 1)**: 60f/40f weights → base 0-100, face +30 → range 30-130, threshold 50f 可達
- **isUserTap 獨立節流**: auto-focus 不再佔用 `lastManualTapTime` | Verified: pending Pixel 9a (v2.11.4)
  - **Before (Bug)**: `focusOnPoint()` 總是設 `lastManualTapTime = now` → auto-focus 觸發後自我封鎖 5s
  - **After (Fix 2)**: `isUserTap=false` 跳過 `lastManualTapTime`，改用獨立 `lastAutoFocusTime`（2s throttle）
- **ML Kit tracking 期間不淡出**: `enteredMLKitTrackingMode` 本地變數 | Verified: pending Pixel 9a (v2.11.4)
  - **Before (Bug)**: focus 成功後總是進入 dwell fade（1s delay → alpha 0.4），ML Kit 追蹤模式下指示器會消失
  - **After (Fix 3)**: coroutine 啟動時捕捉進入的分支，focus 成功後條件性跳過 fade（ML Kit 模式保持 alpha 1.0）
  - **原 trackedObjectId == null guard 已移除（v2.11.1）**：ML Kit 立即重新賦值 trackingId → guard 導致 fade 從未觸發
  - **修正方法**：改用 `var enteredMLKitTrackingMode = false` 本地變數在 coroutine 啟動時捕捉

### 🔍 Code Locations
- calculateSaliencyScore() Fix 1:  `CameraViewModel.kt` ~1105（60f/40f weights）
- lastAutoFocusTime Fix 2:         `CameraViewModel.kt:111`（獨立 auto-focus 節流）
- AUTO_FOCUS_THROTTLE_MS:          `CameraViewModel.kt:112`（2000L）
- isUserTap param Fix 2:           `CameraViewModel.kt:853`（focusOnPoint signature）
- enteredMLKitTrackingMode Fix 3:  `CameraViewModel.kt:894`（本地變數宣告）
- fade skip logic Fix 3:           `CameraViewModel.kt:974`（`if (!enteredMLKitTrackingMode)`）

### ⚠️ Critical Warning
- **Fix 1 Root Cause**: `calculateSaliencyScore()` 的 weight 使用 float literal（`0.6f`/`0.4f`），每次修改公式時務必確認 threshold `50f` 與 weight scale 一致
- **Fix 2 注意**: `isUserTap=false` 的呼叫點有 2 處（autoFocusOnSalientTarget + motion refocus），新增 focusOnPoint 呼叫時必須指定 `isUserTap`
- **Fix 3 與 MEMORY.md 的差異**: MEMORY.md 記錄的是「trackedObjectId == null guard 已移除」，Fix 3 是下一步修正（不用 state check，改用本地變數）

### 📋 Backlog
- [x] [v2.11.4] Fix 1: `calculateSaliencyScore()` 公式 0.6f→60f, 0.4f→40f — ML Kit auto-focus 從未觸發根本原因
- [x] [v2.11.4] Fix 2: `focusOnPoint(isUserTap)` 參數 + `lastAutoFocusTime` 獨立節流 — auto-focus 自我封鎖問題
- [x] [v2.11.4] Fix 3: `enteredMLKitTrackingMode` 本地變數 — ML Kit 追蹤中 focus indicator 不應淡出
- [x] [v2.12.0-04] 自適應對焦框大小 — `boundingBox.width() × 0.8` → `coerceIn(48dp, 120dp)` ✅ 完成 commit `610d143` + VISUAL_PADDING_SCALE=1.5 `22b0d02`
- [x] [v2.11.1] PreviewViewportContainer — 統一 Overlay 座標系統（Grid / Detection / Focus 全部限制在 contentBounds）✅ 早在 v2.11.1 完整實作（非 v2.12.0 待辦，Roadmap 標記有誤）| 驗證: emulator Pixel_5 16:9 + Sony XQ-BT52 4:3 黑邊排除正確

---

## ~~FPS 1:1 Analysis Pipeline~~ → 移除 1:1 模式（v2.12.0）✅ 已完成

### 📋 決議（2026-02-27）
- **設計決策**: 移除 1:1 模式，只保留 4:3（sensor 原生）和 16:9（螢幕填充）
- **根因**: Pixel 9a 1:1 模式 ImageProxy 為 1920×1920（3.69 MP，比 4:3 大 12 倍）→ FPS 崩潰
- **決策理由**: 1:1 為數位裁切，非 sensor 原生比例；現代主流手機螢幕只支援 4:3/16:9 為核心
- **Commit**: `b93d181` [Refactor] v2.12.0: Remove 1:1 aspect ratio mode
- **研究文件（保留供參考）**: `docs/features/v2.11.3/v2.11.3_FPS_1to1_L1.md`, `docs/features/v2.11.4/v2.11.4-06_Pinpoint_FPS_Bottleneck-L2.md`

### 📋 Backlog
- [x] ~~FPS 指標來源確認~~ — 已取消（根本方案為移除 1:1）
- [x] ~~ImageAnalysis cap~~ — 已取消（根本方案為移除 1:1）
- [x] ~~ResolutionSelector 排查~~ — 已取消（根本方案為移除 1:1）

---

## EXIF 寫入與顯示增強 (v2.11.4)

### 📋 狀態
- **根本問題**: `bitmap.compress(JPEG)` 丟失所有硬體 EXIF；舊版 `writeExifMetadata()` 只寫回 Orientation + GPS
- **v2.11.4 已實作** (2026-02-21): 基礎欄位寫入 + 完整曝光資訊讀取/顯示

### ✅ Confirmed (v2.11.4, 2026-02-21)

| # | 描述 | Code Location |
|---|------|---------------|
| W1 | 寫入 `TAG_MAKE` / `TAG_MODEL` / `TAG_DATETIME` / `TAG_SOFTWARE` | `CameraRepositoryImpl.writeExifMetadata()` |
| R1 | 讀取曝光 EXIF: `TAG_ISO_SPEED_RATINGS`, `TAG_EXPOSURE_TIME`, `TAG_F_NUMBER`, `TAG_FOCAL_LENGTH`, `TAG_FOCAL_LENGTH_IN_35MM_FILM`, `TAG_LENS_MODEL`, `TAG_FLASH`, `TAG_WHITE_BALANCE`, `TAG_METERING_MODE` | `CameraRepositoryImpl.readExifInfo()` |
| D1 | PhotoInfoBottomSheet — 新增「曝光資訊」區塊（快門/光圈/ISO/焦距/等效焦距） | `PhotoInfoBottomSheet.kt` |
| M1 | `ExifInfo.kt` 新增欄位: `iso, exposureTime, fNumber, focalLengthMm, focalLength35mm, lensModel, flash, whiteBalance, meteringMode` | `ExifInfo.kt` |

### ⚠️ Known Limitation
- **ISO/ExposureTime/FNumber/FocalLength 值來自 JPEG EXIF**（由硬體相機寫入）
  - 以 CameraX ImageCapture 拍攝的 JPEG：Pixel 9a 的系統相機 API 會寫入完整 EXIF ✅
  - 透過 `bitmap.compress()` 重壓的影像：會失去硬體 EXIF（目前 SmartCameraApp 走此路徑）
  - **實際效果**: 裝置廠商相機 EXIF 仍可正確讀取並顯示，新拍照片仍缺 ISO/曝光

### 📋 Backlog — 後續改善
- [ ] [v2.12.0] Camera2Interop `TotalCaptureResult` 讀取 ISO / ExposureTime / FNumber / FocalLength → 在 `writeExifMetadata()` 中寫入（需 `Camera2CameraControl`）
- [ ] [v2.12.0] 改用 `ImageCapture` 直接輸出 JPEG（不走 bitmap.compress 路徑），保留硬體 EXIF
- [ ] [v2.12.0] `@SuppressWarnings("deprecation")` 或 API 版本分支處理 `TAG_ISO_SPEED_RATINGS` 棄用警告

---

## AE/AF Lock — Camera2 Interop (v2.11.4)

### ✅ Confirmed (v2.11.4, 2026-02-25 Pixel 9a — Tests 1-8 ALL PASS)

| # | 描述 | Commit |
|---|------|--------|
| B1 | **Double cancelFocusAndMetering race 修復** — `_state.update{isAEAFLocked=false}` 移至 suspend 前，第 2 個 coroutine 見到 false 直接跳過 | v2.11.4-06 `8953dc8` |
| B3 | **focusDist=0 修復** — 鎖定時同時設 `LENS_FOCUS_DISTANCE=lastFocusDistance`，Pixel 9a 不再重置鏡頭位置至無限遠 | v2.11.4-06 `8953dc8` |
| B2 | **螢幕解鎖後暗畫面修復** — `CameraState.OPEN` Flow observer + `AeAfLockState` enum（LOCKED→auto-invalidate guard） | v2.11.4-09 `98bcf67` |
| B4 | **第 2 次 lock 5s delay 修復** — `cancelFocusAndMetering()` 前先 `clearCaptureRequestOptions()` 移除 AF_MODE=OFF | v2.11.4-10 `f6aba2a` |

**Pixel 9a 完整回歸測試（2026-02-25 v2.11.4-10）**:
| Test | 結果 |
|------|------|
| Test 1: 長按鎖定 | ✅ PASS |
| Test 2: AF 維持 30s | ✅ PASS |
| Test 3: AE 鎖定確認 | ✅ PASS |
| Test 4: 快速 lock/unlock（第 2 次 <2s） | ✅ PASS |
| Test 5: 無雙 cancel race | ✅ PASS |
| Test 6: tap 解鎖後曝光恢復 | ✅ PASS |
| Test 7: 螢幕解鎖後長按速度（<2s） | ✅ PASS |
| Test 8: 鎖定後預覽清晰 | ✅ PASS |

### ⚠️ Critical Warning — Camera2 Interop & CameraX Lifecycle

---

#### 坑點 1：`clearCaptureRequestOptions()` 在 ON_STOP 時被 HAL 忽略
- **症狀**：螢幕鎖定/解鎖後，`AE_LOCK=true` + `AF_MODE=OFF` 殘留 → 預覽畫面過暗、AF 凍結
- **根因**：camera inactive 時 HAL 忽略所有 capture request；ON_STOP 時呼叫 clear 無效
- **❌ 錯誤做法**：`bindToLifecycle()` callback 或 `DisposableEffect(ON_STOP)` 內呼叫 clear（v2.11.4-06 嘗試過，仍 FAIL）
- **✅ 正確做法**：`DefaultLifecycleObserver.onStart()` — 每次 camera resume（含螢幕解鎖、背景切換）時 clear
- **關鍵約束**：必須配合 `isAEAFLockedInSource` flag guard，避免清除使用者主動啟動的 lock
- **版本記錄**：v2.11.4-07（commit `5a73ec4`）

---

#### 坑點 2：`clearCaptureRequestOptions()` 與 `startFocusAndMetering()` 非同步順序問題
- **症狀**：解鎖後第 2 次 long-press 有 5s delay（每次解鎖幾乎必現），須重啟 APP 才能恢復正常
- **根因**：`clearCaptureRequestOptions()` 返回 `ListenableFuture`；若在 clear **完成前** 就呼叫 `startFocusAndMetering()`，兩者共用同一 camera executor（FIFO 隊列），`AF_MODE=OFF` 殘留在隊列中 → 新的 AF scan 被舊的 OFF 命令覆蓋
- **❌ 錯誤做法**：cancelFocusAndMetering 後直接 startFocusAndMetering（不等待 clear）
- **✅ 正確做法**：在 `lockAEAF()` 呼叫 `startFocusAndMetering()` **之前**先排入 pre-clear（利用 FIFO 保證：clear 先處理，再執行 AF scan）
- **版本記錄**：v2.11.4-07（commit `5a73ec4`）

---

#### 坑點 3：Pixel 9a — `AF_MODE=OFF` 後 `LENS_FOCUS_DISTANCE` 重置為 0（無限遠）
- **症狀**：AE/AF lock 後畫面模糊（鏡頭物理移動至無限遠位置）
- **根因**：Pixel 9a 在 `CONTROL_AF_MODE=OFF` 生效後，HAL 自動將 `LENS_FOCUS_DISTANCE` 重置為 0（0 diopters = 無限遠），推翻剛剛收斂的焦點位置
- **❌ 錯誤做法**：僅設 `AF_MODE=OFF`，不同時固定焦距
- **✅ 正確做法**：在 `lockAEAF()` success callback 時，同時設 `CONTROL_LENS_FOCUS_DISTANCE=lastFocusDistance`（在 `onCaptureCompleted` 每幀更新的最後收斂值）
- **注意**：此為 Pixel 9a 特有行為，其他設備可能無此問題；建議保留此機制作為通用防禦
- **版本記錄**：v2.11.4-06（commit `8953dc8`）

---

#### 坑點 4：`bindToLifecycle()` 只在 process 啟動時呼叫，螢幕鎖定/解鎖不觸發 rebind
- **症狀**：以為 bind-time clear 能處理螢幕解鎖後的殘留，但測試證明無效
- **根因**：CameraX 的 `onStop → onStart` 路徑（螢幕鎖定/解鎖、背景切換）**不會**重新呼叫 `bindToLifecycle()`，僅第一次 bind 或 `unbindAll()` 後 rebind 才會觸發
- **❌ 錯誤假設**：「每次回到相機都會觸發 bindToLifecycle callback」
- **✅ 正確理解**：螢幕解鎖 = `onStart()`（無 rebind）；需用 `DefaultLifecycleObserver.onStart()` 攔截
- **版本記錄**：v2.11.4-07（commit `5a73ec4`，v2.11.4-06 踩坑後修正）

---

#### 坑點 5：`cancelFocusAndMetering()` 雙次呼叫 race（"superseded" error）
- **症狀**：雙手勢觸發兩個 coroutine → 第 1 個被 "superseded" cancel → state 未清乾淨
- **根因**：解鎖 branch 的 `_state.update{isAEAFLocked=false}` 放在 suspend call **之後**，race window 內第 2 個 coroutine 仍看到 `isAEAFLocked=true` → 再次呼叫 cancelFocusAndMetering
- **✅ 正確做法**：`_state.update{isAEAFLocked=false}` 移至 `cancelFocusAndMetering()` suspend call **之前**
- **版本記錄**：v2.11.4-06（commit `8953dc8`，`CameraViewModel.kt:886`）

---

#### 坑點 6：`CameraState.OPEN` guard 過嚴（`== UNLOCKED` 判斷）
- **症狀**：螢幕鎖定後 ON_STOP 途中的 cancel 與 camera shutdown 競爭 → `aeAfLockState` 停在 LOCKED → OPEN 時 guard 阻擋 clear
- **根因**：guard 條件 `aeAfLockState == UNLOCKED` 在 LOCKED 狀態下不允許 clear，但此時 LOCKED 是 stale 的（螢幕鎖定時沒有正確 cancel）
- **❌ 錯誤做法**：guard `== UNLOCKED`（過嚴，不允許 LOCKED 狀態的 auto-invalidate）
- **✅ 正確做法**：guard `!= LOCK_REQUESTED`（允許 LOCKED 觸發 auto-invalidate + clear）
- **版本記錄**：v2.11.4-09（commit `98bcf67`）

---

#### 坑點 7：`AF_MODE=OFF` 導致 `cancelFocusAndMetering()` future 死鎖
- **症狀**：tap 解鎖後黃框消失但預覽仍過曝（AE_LOCK=true 殘留在 HAL）
- **根因**：CameraX 的 `cancelFocusAndMetering()` 返回 `ListenableFuture`，等待 AF state change 確認取消；但 `AF_MODE=OFF` 使 `AF_STATE` 永遠是 INACTIVE → future 永不完成（死鎖）。cleanup code（`clearCaptureRequestOptions()`, `logAeAfState=false`）在 `future.addListener()` 內 → 永遠不執行 → `AE_LOCK=true` 殘留
- **為什麼黃框消失**：`focusOnPoint()` 在呼叫 suspend `cancelFocusAndMetering()` 前，已設 `isAEAFLocked=false`（UI 更新），但 hardware 從未清除
- **❌ 錯誤做法**：直接呼叫 `cameraControl.cancelFocusAndMetering()`（AF_MODE=OFF 狀態下會死鎖）
- **✅ 正確做法**：先 `clearCaptureRequestOptions()` 移除 `AF_MODE=OFF`，再呼叫 `cancelFocusAndMetering()`（AF_MODE 恢復 CONTINUOUS_PICTURE → future ~300ms 正常完成）
- **版本記錄**：v2.11.4-10（commit `f6aba2a`）

---

### 🔍 Code Locations (v2.11.4-10)
- `isAEAFLockedInSource` flag:       `LiveCameraSource.kt:101`
- Pre-clear before lock:             `LiveCameraSource.kt:739`（lockAEAF — FIFO pre-clear）
- Set locked flag on success:        `LiveCameraSource.kt:771`
- Clear flag in cancelFocusAndMetering: `LiveCameraSource.kt:851`
- DefaultLifecycleObserver.onStart(): `LiveCameraSource.kt:1191`
- lastFocusDistance tracking:        `LiveCameraSource.kt:97`（每幀 onCaptureCompleted 更新）
- CameraViewModel race fix:          `CameraViewModel.kt:886`（_state.update 在 suspend 前）

### 📋 Backlog
- [x] [v2.11.4-06] Bug 1: Double cancel race — `_state.update` 移至 suspend 前 ✅ Pixel 9a PASS
- [x] [v2.11.4-06] Bug 3: focusDist=0 — `LENS_FOCUS_DISTANCE=lastFocusDistance` 同時設定 ✅ Pixel 9a PASS
- [x] [v2.11.4-09] Bug 2: 螢幕解鎖後暗畫面 — `CameraState.OPEN` observer + LOCKED→auto-invalidate ✅ Test 7 Pixel 9a PASS
- [x] [v2.11.4-10] Bug 4: tap 解鎖後 AE_LOCK 殘留 — `cancelFocusAndMetering()` 前先 `clearCaptureRequestOptions()` ✅ Test 6 Pixel 9a PASS
- [x] **v2.11.4 完成** — Tests 1-8 全部 PASS（Pixel 9a 2026-02-25）

---

## AF 防禦修復 — Fixed-focus 永久鎖定 + Suspended 保護（v2.12.0-11）

### 📋 問題背景（2026-03-10）

v2.12.0-10 Sony 實機測試發現兩個互相關聯的 AF 問題：
1. **AF Hunting（前鏡頭）**：Pixel 9a 前鏡頭為 fixed-focus（無 VCM），`startFocusAndMetering()` 永遠回傳 `isFocusSuccessful=false` → `onFailure()` 觸發指數退避（2s→4s→8s→repeat），形成無限循環
2. **grow-only 框阻擋縮小**：`autoFocusOnSalientTarget()` 中 `AfState.Locked` 時 `maxOf()` 阻止框縮小 → ML Kit 新一幀更小 bbox 無法反映到 UI

### ✅ 已確認的防禦決策（v2.12.0-11，2026-03-10）

#### A. Fixed-focus 識別路徑（StateFlow 傳遞）

| 層 | 實作 | 說明 |
|---|------|------|
| `LiveCameraSource` | `FixedFocusDetector.isFixedFocus(camera.cameraInfo)` | `bindToLifecycle()` 後立即偵測，結果同步到 `_isFixedFocusLens: MutableStateFlow` |
| `CameraRepository` | `val isFixedFocusLens: StateFlow<Boolean>` | 介面宣告，`CameraRepositoryImpl` 代理 `LiveCameraSource`；`StaticImageSource` 回傳 `MutableStateFlow(false)` |
| `CameraState` | `val isFixedFocusLens: Boolean = false` | ViewModel `init` 透過 `collect` 同步 |

**⚠️ 注意**：`@Volatile private var isFixedFocus`（`LiveCameraSource` 內部）保留不動，`_isFixedFocusLens.value = isFixedFocus` 複用同一次查詢結果（不重複呼叫 `FixedFocusDetector`）。

#### B. Fixed-focus 永久鎖定分流

```
autoFocusOnSalientTarget() 中：
  ML Kit bbox → 更新 UI 框（直接，不再 maxOf）
  ↓
  if (isFixedFocusLens && AfState.Locked) → return  ← 跳過 HAL 呼叫
  ↓
  eyeAfThrottle.shouldTrigger() → focusOnPoint()
```

- **前鏡頭（fixed-focus）**：第一次成功對焦進入 `AfState.Locked` 後，後續 ML Kit 幀只更新 UI 框，完全不呼叫 `startFocusAndMetering()`
- **後鏡頭（AF 鏡頭）**：`isFixedFocusLens=false`，走原有完整 AF 路徑

#### C. grow-only maxOf 移除

- **移除前**：`AfState.Locked` 時 `maxOf(focusWidthPx, currentState.focusFrameWidthPx)` → 框只能長大，不能縮小
- **移除後**：`updatedW = focusWidthPx`，UI 框直接跟隨 ML Kit bbox
- **視覺平滑**：由 `animateDpAsState(Spring(StiffnessMedium))` 提供，不需要 grow-only 保護

#### D. AfTriggerThrottleController — Suspended 模式

```
連續失敗 ≥ 3 次 → consecutiveFailCount >= maxFailCount → shouldTrigger() 永遠 false
解除途徑：UserTap → focusOnPoint(isUserTap=true) → eyeAfThrottle.reset()
         鏡頭切換 → switchCamera() → eyeAfThrottle.reset()
```

- **目的**：防止 AF 無限 backoff 循環（2s→4s→8s→2s→...）
- **`onSuccess()`**：`consecutiveFailCount = 0`（成功後自動解除）
- **`reset()`**：`consecutiveFailCount = 0`（UserTap / 鏡頭切換後解除）

#### E. Hysteresis 擴大（AfState.Locked 時）

- **條件**：`isArmed && AfState.Locked`
- **門檻**：`effectiveThreshold = rescanThresholdPx * 2.5f`（約 135px，原 54px）
- **目的**：抑制 ML Kit bbox 微抖動（< 54px）在 Locked 後誤觸發重新對焦

#### F. switchCamera() 補強

| 新增 | 說明 |
|------|------|
| `afState = AfState.Idle` | 切換後 AF 狀態歸零，避免 Locked/Failed 殘留 |
| `eyeAfThrottle.reset()` | 清除 isArmed / cooldown / consecutiveFailCount |
| `eyeAfStabilityTracker.reset()` | 清除舊鏡頭座標，避免第一幀 delta 偏大 |

### ⚠️ Critical Warnings

1. **固定 `@Volatile private var isFixedFocus` 不可刪除**：`LiveCameraSource.focusOnPoint()` 仍使用此變數作為 bypass 判斷（line ~683）；`_isFixedFocusLens StateFlow` 是額外的觀察路徑，不是替代品
2. **Fixed-focus guard 位置**：必須在 `_state.update { focusFrameWidthPx }` **之後**、`shouldTrigger()` **之前** — 確保 UI 框仍然更新，只跳過 HAL 呼叫
3. **Suspended 模式不影響 UserTap**：`focusOnPoint(isUserTap=true)` 在進入 `focusJob launch` 前呼叫 `eyeAfThrottle.reset()`，確保使用者手動點擊永遠有效
4. **Sony HAL 特性**：Sony XQ-BT52 後鏡頭 `afModes=[0,1,3]`（無 CONTINUOUS_PICTURE=4），但 `FixedFocusDetector.isFixedFocus()` 檢查 `afModes.isEmpty()` 或全為 `AF_MODE_OFF(0)`，Sony 不觸發 fixed-focus 路徑 ✅

### 📋 Backlog

- [x] P0-B `isFixedFocusLens` StateFlow 傳遞路徑（Repository 層） ✅
- [x] P0-C `CameraState.isFixedFocusLens` + ViewModel init collect ✅
- [x] P0-D `autoFocusOnSalientTarget()` grow-only maxOf 移除 ✅
- [x] P1-A Fixed-focus 永久鎖定分流（跳過 focusOnPoint） ✅
- [x] P1-B `switchCamera()` 補強（afState reset + eyeAfThrottle.reset） ✅
- [x] P2-A `AfTriggerThrottleController` Suspended 模式（consecutiveFailCount） ✅
- [x] P2-B Hysteresis 擴大 2.5x（AfState.Locked） ✅
- [x] P2-C UserTap 解除 Suspended（focusOnPoint isUserTap=true 路徑） ✅

---

## AF 視覺語義雙路徑 + Session ID 第二道防線（v2.12.0-13）

### 📋 問題背景（2026-03-10）

v2.12.0-12 Build PASS 後，實機測試（Sony XQ-BT52）發現：
1. **Bug #1（FocusClamp 未觸發）**：Eye-AF 成功（`afState=Locked`）後，`FocusClamp` LaunchedEffect 進入 `else → scale.snapTo(1.0f)`，機械夾爪動畫未執行，dwell fade 未啟動
2. **Bug #1b（updateTrackedObjectPosition bbox 不更新）**：ML Kit 追蹤時，`_state.update` 只更新 `focusPoint`，未同步更新 `focusFrameWidthPx/HeightPx`，對焦框大小凍結在 tap 時的初始值
3. **Bug #2（Session ID 穿透）**：v2.12.0-12 的 `afState !is Focusing` 守衛是協作式取消的第一道防線，但 HAL callback 在 `cancel()` 與 `afState=Idle` 之間的極小視窗仍可穿透

### ✅ 已確認的防禦決策（v2.12.0-13，2026-03-10）

#### A. FocusIndicator 雙視覺路徑分離

**SSOT 規範**（不可違背）：

| 視覺路徑 | 觸發條件 | 顏色 | 框大小 |
|---------|---------|------|--------|
| Eye-AF / 物件對焦 | `afState is AfState.Locked` | Cyan `#00F2FF` | 動態 bbox |
| AE/AF 長按鎖定 | `isAEAFLocked = true` | Yellow `#FFD700` | 固定 72dp |

**實作要點**：
- `CameraState.isFocusIndicatorLocked = isAEAFLocked`（**只對應 Yellow**，`afState=Locked` 不加入此條件）
- `FocusIndicator` 新增 `isEyeAfLocked: Boolean` 參數，由呼叫處傳入 `state.afState is AfState.Locked`
- `alpha when` 優先順序：`isLocked(Yellow) → isEyeAfLocked(Cyan) → isFading(0.4) → isTracking → else`
- `showCornerBrackets = focusSuccess == true || isLocked || isEyeAfLocked`

**⚠️ 禁止違背**：`isLocked` 與 `isEyeAfLocked` 語義嚴格分離。Eye-AF 成功絕不走 Yellow 路徑。Composable 不得在內部自行做 `|| afState is Locked` 合併。

#### B. updateTrackedObjectPosition — 原子性 bbox + focusPoint 同步

**問題根因**：`shouldUpdate` 區塊的 `_state.update` 只更新 `focusPoint`，ML Kit 追蹤期間 `focusFrameWidthPx/HeightPx` 凍結在 tap 初始值。

**修復方式**：在 `when (trackedType)` 各分支中，於計算 `centerX/centerY` 的同時，透過 `imageRectToScreenRectF()` 計算 `newFrameW/newFrameH`，並在同一個 `_state.update` 呼叫中一次寫入：

```kotlin
current.copy(
    focusPoint = Pair(screenX, screenY),
    focusFrameWidthPx  = newFrameW ?: current.focusFrameWidthPx,
    focusFrameHeightPx = newFrameH ?: current.focusFrameHeightPx
)
```

- `newFrameW ?: current.focusFrameWidthPx`：transformer 不可用時保留現有值，不破壞 legacy fallback
- FACE 使用 `BBOX_SCALE_FACE (1.5f)`；OBJECT 使用 `BBOX_SCALE_OBJECT (1.8f)`
- 兩個 `transformer` 讀取在各自的 `when` 分支內（避免跨分支 race）

#### C. Session ID 第二道防線（HAL callback 穿透防禦）

**防禦層次**：

| 層 | 機制 | 版本 |
|---|------|------|
| 第一道 | `afState !is AfState.Focusing` 狀態校驗 | v2.12.0-12 |
| 第二道 | `capturedSessionId != focusSessionId` Session ID 校驗 | v2.12.0-13 |

**實作要點**：
- `private var focusSessionId: Int = 0`（Main thread 存取，無需 AtomicInteger）
- `resetFocusState()` 中：`focusJob?.cancel()` → `focusSessionId++` → `_state.update(Idle)`（順序不可調換）
- `focusOnPoint()` coroutine 最開頭：`val capturedSessionId = ++focusSessionId`
- HAL 成功/失敗路徑：在 `afState !is Focusing` 守衛之後，再加 `capturedSessionId != focusSessionId` 校驗
- `isOperationCancelled` 與 `TimeoutCancellationException` 路徑**不需要** Session ID 守衛（本地取消，非 HAL 穿透）

**Logcat 驗證模式**：
```
# 正常對焦（Session ID 匹配）
[v2.12.0-13] focusOnPoint: sessionId=5 captured
Focus successful at (x, y)

# 切換鏡頭後的過期回調（Session ID 不匹配）
[v2.12.0-12] switchCamera: resetFocusState() called
[v2.12.0-13] Stale HAL success dropped (captured=5, current=6)
```

### ⚠️ Critical Warnings

1. **`isFocusIndicatorLocked` 語義永遠是 `isAEAFLocked`**：v2.12.0-12 L2 指南曾建議 `|| afState is Locked`，但已確認錯誤，SSOT 規範和 CameraState 批注均明確說明 Eye-AF Locked 為 Cyan，不切 Yellow
2. **`updateTrackedObjectPosition` bbox 必須原子更新**：禁止拆成兩次 `_state.update`，否則 UI 會在兩幀之間看到位置已更新但框大小未更新的不一致狀態
3. **Session ID `++` 順序**：`resetFocusState()` 中 `focusSessionId++` 必須在 `focusJob?.cancel()` 之後、`_state.update` 之前，確保遞增發生在 cancel 之後（即使協作取消有延遲）
4. **`focusOnPoint()` 的 `capturedSessionId` 作用域**：在 `try { }` 最開頭宣告，整個 coroutine 的 HAL 回調均可存取；不可在 `withTimeout` 之後才宣告

### 📋 Backlog

- [x] T1: `updateTrackedObjectPosition()` bbox 原子更新（`newFrameW/H` + 同一 `_state.update`） ✅
- [x] T2: `FocusIndicator` 雙路徑分離（`isEyeAfLocked` 參數 + alpha/showCornerBrackets 更新） ✅
- [x] T3: `focusSessionId` Session ID 防禦（5 個插入點）✅

---

## Coordinate Truth Consolidation + PreviewView Alignment（v2.12.0-14）

### 📋 問題背景（2026-03-11）

1. **Coordinate fallback 導致幾何失準**：`resolveTapTarget()` 與 `focusOnPoint()` OF 路徑在 `coordinateTransformer == null` 時，退回 `CoordinateMapper`（CENTER_CROP 幾何）或 raw screen coords，導致 Eye-AF 框偏移與 hit-test 失準
2. **雞生蛋問題（bootstrap deadlock）**：`updateContentBounds()` 在 App 啟動時（ML Kit 尚未回傳第一幀）`detectionResult == null` → `effectiveImageWidth = 0` → `transformer = null`；後續所有 tap 均走 legacy fallback
3. **Release build 幾何不一致**：`USE_PREVIEWVIEW_DISPLAY = false`（release）vs `true`（debug），導致 Release build 使用 `ContentScale.Crop`（bitmap path），4 個幾何真相（overlay / tap / HAL metering / OF）全部不對齊

### ✅ 已確認的防禦決策（v2.12.0-14，2026-03-11）

#### A. CoordinateTransformer 為唯一幾何真相來源

**鐵律**：`CoordinateTransformer`（FIT_CENTER 幾何）是唯一正確的座標真相來源。禁止在任何路徑回退 `CoordinateMapper`（CENTER_CROP 幾何）或 raw screen coords。

**已清除的 3 個 legacy fallback（`CameraViewModel.kt`）**：

| 位置 | 舊行為 | 新行為 |
|------|--------|--------|
| `resolveTapTarget()` — `transformer == null` | `x to y`（raw coords hit-test，幾何錯誤） | `return TapTargetInfo.Generic` |
| `focusOnPoint()` OF — tap 在 content bounds 外 | `startOpticalFlowTracking(x, y)`（CoordinateMapper） | `return@launch` |
| `focusOnPoint()` OF — `transformer == null` | `startOpticalFlowTracking(x, y)`（CoordinateMapper） | `return@launch` |

**Bootstrap 快取機制**（解決雞生蛋問題）：
```kotlin
// CameraViewModel.kt — updateContentBounds()
private var lastKnownImageWidth: Int = 0   // v2.12.0-14
private var lastKnownImageHeight: Int = 0  // v2.12.0-14

if (effectiveImageWidth > 0 && effectiveImageHeight > 0) {
    lastKnownImageWidth  = effectiveImageWidth
    lastKnownImageHeight = effectiveImageHeight
}
val resolvedW = if (effectiveImageWidth > 0) effectiveImageWidth else lastKnownImageWidth
val resolvedH = if (effectiveImageHeight > 0) effectiveImageHeight else lastKnownImageHeight
// ⚠️ resolvedH 的 guard 必須用 effectiveImageHeight > 0，不可誤用 effectiveImageWidth
```

#### B. Release/Debug 對齊 USE_PREVIEWVIEW_DISPLAY

**問題根因**：v2.8.6 出於保守考量，release build 關閉 `USE_PREVIEWVIEW_DISPLAY`。但事實上 PreviewView 路徑（FIT_CENTER + `contentBounds`）記憶體更穩定（~95 MB vs bitmap path ~215 MB），且必須與 debug build 一致才能確保幾何真相對齊。

**修復**：`app/build.gradle.kts` release buildType 改為：
```kotlin
// v2.12.0-14: Enabled for release — geometry truth aligned with debug build
buildConfigField("Boolean", "USE_PREVIEWVIEW_DISPLAY", "true")
```

**鏈式邏輯**：`USE_PREVIEWVIEW_DISPLAY && supportsNativePreview`（`LiveCameraSource` 永遠回傳 `true`）→ `useNativePreview = true` → `CameraPreview.kt:64` 走 PreviewView 路徑

#### C. Scope Discipline：UI 層禁止被座標票據連帶改動

**鐵律**：座標修復票據（Coordinate Truth / CoordinateMapper cleanup）**只允許修改 ViewModel 層**，絕對禁止在同一 ticket 改動：
- `CameraScreen.kt`
- `FocusIndicator` / `FocusClamp` composable
- 任何動畫邏輯、顏色邏輯
- `CoordinateTransformer` 類別本身

**v2.12.0-14 SV-F1 案例**：T1/T2 驗收中發現 `CameraScreen.kt` 的 alpha 優先序被連帶調換（`isFading` 移至 `isEyeAfLocked` 之前），SV-F1 票據要求立即回滾。正確順序：
```
isLocked(Yellow) → isEyeAfLocked(Cyan) → isFading(0.4) → isTracking → else
```

#### D. SV-Fix 票據模式（Strict Verification Fix）

**模式說明**：`strict_verification` Quality Gate 發現 implementation drift 後，產出 `SV-Fxx` 票據，Claude Code 依票據僅修復偏移點，不做範圍外改動。Build 後立即停止，等待下一輪 QG 驗收。

**本輪效果**：
- SV-F1 修復 2 個偏移（`resolvedH` guard 邏輯 + `CameraScreen.kt` 回滾），build 19s 即收斂
- 閉環總耗時：< 5 分鐘，zero side-effect

### ⚠️ Critical Warnings

1. **`resolvedH` guard 必須對應高度**：`if (effectiveImageHeight > 0)` — 禁止複製貼上時誤用 `effectiveImageWidth`
2. **Metering fallback 已升級為 `Log.e`**：`[v2.12.0-14] CRITICAL: PreviewView not set` 若在 Logcat 出現，表示 PreviewView 未正常注入，必須調查
3. **OF fallback 已硬性移除**：`transformer == null` 或 tap 在 content bounds 外時，OF 路徑直接 `return@launch`，不再靜默走舊路徑，tap 無反應為預期行為（等待 transformer 就緒）

### 📋 Backlog

- [x] T1: Coordinate Truth Consolidation — 移除 3 個 legacy fallback + bootstrap 快取機制 ✅
- [x] T2: Release build `USE_PREVIEWVIEW_DISPLAY = true` + Metering fallback 升級 `Log.e` ✅
- [x] SV-F1: `resolvedH` guard 修正 + `CameraScreen.kt` alpha 順序回滾 ✅

---

## domain.model.focus 純資料模型建立（v2.12.0-15 Step 1）

### ✅ Confirmed

- **12 個純資料模型檔案**已建立於 `domain/model/focus/`，Build PASS | 2026-03-13
  - `FramePacket`, `DetectionFrameContext`, `FocusTarget`（+ `TargetType`/`TargetSource`）
  - `FocusStates`（`VisualTargetState`/`LensFocusState`/`ExposureLockState`）
  - `GeometryContext`, `LensCapability`, `TrackedTargetState`, `UserIntentState`
  - `EyeAfPolicyModels`（`EyeAfPolicyInput`/`EyeAfDecision`/`MeteringAnchor`）
  - `FocusControllerResult`（+ `OpticalFocusState`）, `FocusStateInput`（+ `SystemEvent`）, `OverlayModel`（+ `OverlayColor`/`OverlayAnimation`）
  - 規格依據：`plans/proposals/focus_l2_feasibility_spec.md` §17.11 + §17.12.3

### ⚠️ Critical Warnings — Scope Discipline（兩條鐵律，不可違背）

#### 鐵律 1：Step 1 = 只新增 `domain.model.focus`，舊邏輯零修改

**本輪案例**：Step 1 驗收時，`SmartCameraAnalyzer.kt` 與 `CameraViewModel.kt` 含有前一個 ticket（T3 Eye-AF chain repair）的 `eyePositions` 修改，被 Quality Gate 判定為 scope drift。

**處理流程**：
1. `git checkout HEAD -- SmartCameraAnalyzer.kt CameraViewModel.kt`（直接還原）
2. `FocusOrchestrator.kt`（untracked）→ 刪除，徹底退出變更集合
3. Build 重新驗收，變更集合只剩 `domain/model/focus/` 12 檔

**預防原則**：
- Step 開始前先確認 `git status` 為乾淨（或只含本 Step 應有的檔案）
- Step 完成後，`git diff HEAD --stat` 必須**只顯示本 Step 的新增檔案**，無任何 M（修改）
- 不同 ticket 的修改不可混入同一 Step 驗收

#### 鐵律 2：遇到 scope drift → SV 單點修復票快速收斂，比重做更穩定

**模式說明**（與 v2.12.0-14 SV-F1 相同）：
- Quality Gate 發現偏移後，產出 `SV-Sxx` / `SV-Fxx` 票據
- Claude Code 依票據**只修復偏移點**，不做範圍外任何改動
- Build 後立即停止，等待下一輪 QG 驗收

**本輪案例（SV-S1 + SV-S1.1）**：
| 票據 | 偏移點 | 修復動作 |
|------|--------|---------|
| SV-S1 | `SmartCameraAnalyzer.kt` + `CameraViewModel.kt` 含範圍外 `eyePositions` 修改 | `git checkout HEAD --` 還原兩檔 |
| SV-S1.1 | `FocusOrchestrator.kt` 出現在 untracked（不屬 Step 1） | 刪除檔案 |

**為什麼比重做更穩定**：
- 每個 SV 票據只處理一個偏移點，範圍清晰，不引入新邏輯
- 每次 SV 後都重跑 Build + `git diff HEAD --stat` 確認乾淨，可重複驗收

### 🔍 Code Locations

- `domain/model/focus/`: `app/src/main/java/com/akirachang/ai/photographer/domain/model/focus/`
- 規格來源: `plans/proposals/focus_l2_feasibility_spec.md` §17.11.2 ~ §17.11.10

### 📋 Backlog

- [x] Step 1: `domain/model/focus/` 12 個純資料模型 ✅ Build PASS 2026-03-13
- [x] SV-S1: 還原 `SmartCameraAnalyzer.kt` + `CameraViewModel.kt` 範圍外修改 ✅
- [x] SV-S1.1: 移除 `FocusOrchestrator.kt` 出現在變更集合 ✅
- [ ] Step 2: GeometryMapper 介面 + `CoordinateTransformer` 橋接
- [ ] Step 3: EyeAfPolicyResolver 實作
- [ ] Step 4: FocusStateMachine 拆分

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

- [x] **EXIF 寫入增強（基礎）** (v2.11.4) — Make/Model/DateTime/Software 已寫入
- [ ] **完整 EXIF 曝光資訊寫入**（拍照時，v2.12.0）
  - ISO 感光度 / 快門速度 / 光圈值 / 焦距
  - 使用 Camera2Interop `TotalCaptureResult` 獲取相機參數
  - 或改用 ImageCapture 直接輸出 JPEG（保留硬體 EXIF）

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

## Focus Domain Model Pipeline — 架構守則（v2.12.0-15 SV-S5-Final）

### ✅ Confirmed（2026-03-13）

本節記錄 v2.12.0-15 SV-S5-Final + SV-S5-Final-R2 兩輪 Quality Gate 收斂後確立的三條架構守則。

---

#### 守則 1：SmartCameraAnalyzer 是「Dumb Pipe」— 職責邊界鐵律

**定義**（`SmartCameraAnalyzer.kt` KDoc §17.12.5）：

| 職責 | 允許 ✅ | 禁止 ❌ |
|------|---------|---------|
| 資料輸入 | 接收 CameraX ImageProxy / Bitmap | — |
| 偵測 | ML Kit Face + Object 偵測 | Target ranking / saliency scoring |
| 輸出 | `DetectionResult`（image-space coords）+ `LightingState` | UI state 決策 / UI geometry 計算 |
| 光流 | OF 原始追蹤結果（image-space coords）via callback | 轉換至 preview-space（CoordinateMapper 路徑）|

**CoordinateMapper 退役結論**（SV-S5-Final-R2，2026-03-13）：

- `import com.akirachang.ai.photographer.util.CoordinateMapper` — **已從 SmartCameraAnalyzer 移除**
- `previewWidth / previewHeight` 欄位 — **已移除**（純粹為 CoordinateMapper 服務）
- `setPreviewSize(width, height)` — **已從 SmartCameraAnalyzer 移除**；`CameraRepositoryImpl.setPreviewSize()` 降格為 no-op + `Log.d`
- `startOpticalFlowTracking(x: Float, y: Float)`（preview-coord 路徑）— **已從 SmartCameraAnalyzer 移除**；`CameraRepositoryImpl.startOpticalFlowTracking()` 降格為 no-op + `Log.w`

**正確路徑**：
```
CameraViewModel（tap 事件）
  → CoordinateTransformer.mapToImageCoords()   ← FIT_CENTER 幾何，唯一真相
  → cameraRepository.startOpticalFlowTrackingImageCoords(imageX, imageY)
  → SmartCameraAnalyzer.startOpticalFlowTrackingImageCoords()   ← image-space，no mapping
```

**⚠️ 絕對禁止**：在 SmartCameraAnalyzer 內部做任何座標轉換（`previewToImage`、`imageToPreview`）或 UI 決策。Analyzer 只見 image-space 數據。

---

#### 守則 2：幾何單一真相 — GeometryMapper（取代 CoordinateMapper）

**v2.12.0-15 確立的幾何層次**：

| 組件 | 職責 | 幾何模型 |
|------|------|---------|
| `CoordinateTransformer`（legacy ViewModel 路徑）| tap → image-space 座標 | FIT_CENTER |
| `GeometryMapper`（新 pipeline 介面）| `FocusTarget` bbox → 螢幕座標 | FIT_CENTER |
| `PreviewViewGeometryMapper`（`GeometryMapper` 實作）| image-space RectF → screen RectF | FIT_CENTER + ViewPort |
| ~~`CoordinateMapper`~~（已退役）| preview ↔ image-space | CENTER_CROP（幾何錯誤）|

**`CoordinateMapper` 退役完整清單**（截至 2026-03-13）：

| 位置 | 狀態 |
|------|------|
| `SmartCameraAnalyzer.kt` import | ✅ 已移除 |
| `SmartCameraAnalyzer.startOpticalFlowTracking()` 呼叫 | ✅ 整個函式已移除 |
| `SmartCameraAnalyzer.trackFromBitmap()` else 分支 | ✅ 已改為 discard + `Log.w` |
| `CameraViewModel.focusOnPoint()` OF fallback 路徑 | ✅ 已於 v2.12.0-14 改為 `return@launch` |
| `CameraRepositoryImpl.setPreviewSize()` | ✅ 已降格為 no-op |
| `CameraRepositoryImpl.startOpticalFlowTracking()` | ✅ 已降格為 no-op |

**唯一殘留**：`CoordinateMapper` 類別本身保留（未刪除），作為 OF legacy bitmap path 的靜態工具。若未來 OF 全面升級，可整體移除。

---

#### 守則 3：Focus Pipeline 硬體 Provider — Lifecycle-Safe 接線模式

**問題根源**：`CameraControl` 與 `MeteringPointFactory` 均依附 camera lifecycle，不可在 ViewModel init 時提前取值（camera 未 bind）。

**v2.12.0-15 SV-S5a-R2 + SV-S5-Final 確立的正確接線模式**：

```kotlin
// ✅ 正確：Lambda 懶取值 — 呼叫時才解析，lifecycle-safe
CameraXFocusController(
    getCameraControl      = { cameraRepository.getCameraControl() },
    getMeteringPointFactory = { cameraRepository.getMeteringPointFactory() },
)

// 接線路徑（每層均 nullable，lifecycle-safe）：
// CameraXFocusController
//   → CameraRepository.getCameraControl()        (interface)
//   → CameraRepositoryImpl.getCameraControl()     (delegates to LiveCameraSource if available)
//   → LiveCameraSource.getCameraControl()         = if (::camera.isInitialized) camera.cameraControl else null
//
// CameraXFocusController
//   → CameraRepository.getMeteringPointFactory()  (interface)
//   → CameraRepositoryImpl.getMeteringPointFactory() (delegates to LiveCameraSource if available)
//   → LiveCameraSource.getMeteringPointFactory()  = previewView?.meteringPointFactory
```

**禁止的模式**：
```kotlin
// ❌ 錯誤：在 ViewModel init block 直接取值 — camera 尚未 bind，得到 null 後永遠不更新
private val cameraControl = cameraRepository.getCameraControl()  // ← 永遠是 null

// ❌ 錯誤：在 constructor 存硬體物件 — 造成 lifecycle leak
CameraXFocusController(cameraControl = liveCameraSource.camera.cameraControl)
```

**FocusModule DI 綁定**（`di/FocusModule.kt`，`@InstallIn(ViewModelComponent::class)`）：
- 4 組 `@Provides` 綁定：`GeometryMapper`、`TargetTracker`、`EyeAfPolicyResolver`、`FocusController`
- `provideFocusController` 透過 `CameraRepository` 注入，確保 lazy lambda 接線模式在 DI 圖中一致

### ⚠️ Critical Warnings

1. **`CoordinateMapper` 路徑已完全退役**：任何新增 OF 追蹤入口必須使用 `startOpticalFlowTrackingImageCoords()`，禁止復活 `startOpticalFlowTracking(x, y)`（preview-coord）路徑
2. **Analyzer 無 preview 尺寸概念**：`SmartCameraAnalyzer` 不再持有 `previewWidth/Height`，呼叫 `setPreviewSize()` 為 no-op。座標轉換由 ViewModel 層的 `CoordinateTransformer` / `GeometryMapper` 負責
3. **硬體 Provider 必須 lazy**：`getCameraControl()` 與 `getMeteringPointFactory()` 任一在 camera bind 前呼叫均回傳 `null`（lifecycle-safe）；`CameraXFocusController` 必須在 `apply()` 時才呼叫，不可在 constructor 預取

### 🔍 Code Locations

| 組件 | 路徑 |
|------|------|
| SmartCameraAnalyzer（Dumb Pipe）| `data/source/camera/SmartCameraAnalyzer.kt` |
| GeometryMapper 介面 | `domain/focus/geometry/GeometryMapper.kt` |
| PreviewViewGeometryMapper 實作 | `data/focus/geometry/PreviewViewGeometryMapper.kt` |
| CameraXFocusController（Lazy Provider）| `data/focus/control/CameraXFocusController.kt` |
| LiveCameraSource.getCameraControl() | `LiveCameraSource.kt` ~line 1446 |
| LiveCameraSource.getMeteringPointFactory() | `LiveCameraSource.kt` ~line 1456 |
| CameraRepository 介面新增方法 | `domain/repository/CameraRepository.kt` ~line 302-311 |
| FocusModule DI | `di/FocusModule.kt` |
| CameraViewModel FOCUS_PIPELINE_V2 flag | `presentation/camera/CameraViewModel.kt:159` |

### 📋 Backlog

- [x] SV-S5-Final: FocusModule DI 綁定（4 組）+ `getMeteringPointFactory()` 接線 ✅ 2026-03-13
- [x] SV-S5-Final: `FOCUS_PIPELINE_V2 = BuildConfig.DEBUG`（DEBUG 啟用 / RELEASE 關閉）✅ 2026-03-13
- [x] SV-S5-Final-R2: SmartCameraAnalyzer CoordinateMapper 退役（import + 3 函式 + else 分支）✅ 2026-03-13
- [x] SV-S5-Final-R2: CameraRepositoryImpl 連帶修復（setPreviewSize / startOpticalFlowTracking no-op）✅ 2026-03-13
- [x] Step 6: FocusOverlay Composable（消費 `CameraState.focusUiState.overlayModel`）✅ 2026-03-14
- [x] Step 6: CameraScreen.kt 接入 FocusOverlay（`FOCUS_PIPELINE_V2=true` 條件下）✅ 2026-03-14
- [x] SV-BatchA: FocusOverlay 三重透明防禦 + GeometryMapper matrix-aware mirror 修復 ✅ 2026-03-14
- [ ] T3b Ghost Box: 定點/物件對焦框切換時 36dp 殘留（`focusFrameWidthPx/HeightPx` 未清零）— 待 Traycer PDCA
- [ ] 實機驗收：Sony HQ616R2713 Logcat `[v2.12.0-15] Pipeline: visual=TrackingStable` + `FocusController result: triggerSent=true`

---

## v2.12.0-15 Phase 2 SV-BatchA — 關鍵防禦守則（2026-03-14）

### §A. FocusOverlay 三重透明防禦守則

**Rule**: `FocusOverlay.kt` 必須在 Canvas 繪製前通過三道獨立 guard，缺一不可。

```kotlin
if (overlayModel == null) return          // Guard 1: 無 model → 絕對不渲染
if (visualTargetState == VisualTargetState.None) return  // Guard 2: None 狀態 → 絕對不渲染
if (overlayModel.primaryBox == null) return  // Guard 3: 無主框 → 連 secondary 也不渲染
```

**Why (SV-BatchA Fix A 案例)**:
- 初始實作只有 Guard 1 + 2，缺少 Guard 3
- 當 `primaryBox == null` 但 `secondaryBoxes` 非空時，仍會在畫面上繪製候選框（ghost secondary brackets）
- 根因：`secondaryBoxes` 是候選框集合，沒有主框時不應存在任何視覺輸出

**How to apply**:
- 任何 Composable 消費 `OverlayModel` 時，三道 guard 必須完整保留，不可合併或省略
- Guard 3 必須在「有 overlayModel 但 primaryBox 為 null」的 partial 狀態下攔截所有渲染
- 禁止改為「如無 primaryBox 就用 fallback 36dp 框」— 這是 v2.12.0-15 設計紅線

---

### §B. PreviewViewGeometryMapper Matrix-Aware Mirror 防雙重鏡像守則

**Rule**: 前鏡頭手動鏡像 `pts[i] = previewW - pts[i]` 必須附帶 `&& matrix == null` 條件。

```kotlin
// ✅ 正確（matrix-aware）
if (context.isFrontCamera && context.isMirroredPreview && matrix == null) {
    val previewW = context.previewWidth.toFloat()
    for (i in pts.indices step 2) {
        pts[i] = previewW - pts[i]
    }
}

// ❌ 錯誤（會造成雙重鏡像）
if (context.isFrontCamera && context.isMirroredPreview) {
    // matrix != null 時 sensorToViewMatrix 已包含鏡像，再次反轉 = 雙重鏡像
}
```

**Why (SV-BatchA Fix B 案例)**:
- `sensorToViewMatrix`（由 CameraX CoordinateTransformer 提供）**已包含**前鏡頭的鏡像轉換
- T2 實作時移除了 `&& matrix == null` 條件，目的是「確保前鏡頭一定鏡像」
- 結果：matrix 存在時套用兩次反轉 → 前鏡頭 bounding box 顯示在畫面錯誤側
- Fix B 還原 `&& matrix == null`，確保手動鏡像只在 FIT_CENTER fallback 路徑（matrix=null）執行

**兩條路徑明確分工**:

| 路徑 | matrix 狀態 | 鏡像來源 | 手動反轉 |
|------|------------|---------|---------|
| CameraX CoordinateTransformer | `matrix != null` | `sensorToViewMatrix` 內建 | ❌ 不執行 |
| FIT_CENTER Fallback | `matrix == null` | 無 | ✅ 手動執行 |

**How to apply**:
- 任何修改 `PreviewViewGeometryMapper.transformPoints()` 時，`matrix == null` 條件是**絕對必要的**，不可移除
- 相同守則適用於任何直接操作前鏡頭座標反轉的位置
- 修改前必須確認：「此路徑的 sensorToViewMatrix 是否已含鏡像？」

---

