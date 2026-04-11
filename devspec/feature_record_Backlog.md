# Feature Record & Backlog
Last updated: 2026-03-20

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

## v2.12.0-15 Phase 2 SV-BatchB — Learnings（2026-03-14）

> SV-BatchB 驗收通過。三張票涵蓋：Compose 視覺生命週期、前鏡頭 Yellow 防線、多人目標優先序驗證 Log。

---

### §C. Compose 動畫視覺生命週期守則（FocusOverlay VisualLocked 進入事件）

**Rule**: FocusOverlay 的動畫 alpha 必須由 Compose 狀態機（`LaunchedEffect` + `animateFloatAsState`）驅動，禁止直接讀取 `overlayModel.alpha`（靜態值，無法反映生命週期轉換）。

**VisualLocked 視覺生命週期**（SSOT 定義，勿修改）：
```
進入 VisualLocked
  └→ showClawAnimation = true
  └→ alphaTarget = 1.0f（animateFloatAsState tween(400ms)）
  └→ delay(1000ms)
  └→ 仍在 VisualLocked？→ alphaTarget = 0.4f + showClawAnimation = false
                         → 否（已切換）→ 不執行（自然消滅）
```

**Key design decisions**:

| 決策 | 原因 |
|------|------|
| `LaunchedEffect(visualTargetState, overlayModel.primaryBox)` 雙 key | primaryBox 變化（同 state 下框移動）也需重觸發，確保夾爪跟隨新目標 |
| `prevVisualState` 區分「新進入」vs「持續」 | 防止 box 位置更新時（primaryBox key 觸發）誤重置 1000ms 計時器 |
| `animateFloatAsState(tween(400))` 而非 `Thread.sleep` | Compose 動畫在 recomposition 中安全，`Thread.sleep` 會阻塞主執行緒並導致 ANR |
| `showClawAnimation` 獨立 flag | 夾爪（corner brackets）與 alpha 生命週期解耦，可獨立控制消滅時機 |

**Canvas drawLine API 正確簽名（防錯）**:
```kotlin
// ✅ 正確：最後參數是 strokeWidth: Float
drawLine(color, start: Offset, end: Offset, strokeWidth: Float)

// ❌ 錯誤：drawLine 不接受 Stroke 物件
drawLine(color, start, end, Stroke(width = ...))   // 編譯失敗
```

**Code Location**: `FocusOverlay.kt` — `LaunchedEffect` block + `showClawAnimation` Canvas section

**How to apply**:
- 修改 FocusOverlay 動畫行為時，**只改 `alphaTarget` 的賦值與 `delay()` 時長**，不動 `animateFloatAsState` 架構
- 新增 `VisualTargetState` 枚舉值時，必須在 `LaunchedEffect when` block 中補齊對應分支（否則 Kotlin `when` exhaustive 警告）
- `prevVisualState` 更新必須在 `LaunchedEffect` 最後一行（所有分支之後），確保每次都記錄最新狀態

---

### §D. 前鏡頭 Yellow Lock 三層防禦架構

**Rule**: 前鏡頭（`isFrontCamera=true`）在系統中存在三道獨立防線，任一層皆可阻止 Yellow Lock 出現。三層防禦**不可互相替代**，必須全部保留。

**三層防禦表**：

| 層級 | 檔案 | 機制 | 觸發條件 |
|------|------|------|---------|
| L1 Policy | `DefaultEyeAfPolicyResolver.kt` Rule 1 | `isAeAfLockRequested → REJECT（return None）` | 最早攔截，阻止 HAL 觸發 |
| L2 StateMachine | `DefaultFocusStateMachine.reduceExposureState()` | `isFrontCamera → return Unlocked + Log.w` | Policy 漏網時的第二道防線 |
| L3 UI（現有）| `CameraScreen.kt` FocusIndicator | `isLocked = isAEAFLocked`（Yellow 只在後鏡頭） | 視覺層最終保護 |

**為什麼需要三層**：
- L1 是 intent 層攔截（policy 決策），但若 `userIntent` 組裝有誤（未傳入正確 `isAeAfLockRequested`），L1 可能漏網
- L2 是狀態機層強制覆寫，即使 L1 未攔截，`ExposureLockState` 仍強制為 `Unlocked`
- L3 是 UI 層的最終保護，確保即使 state 層有 `AEAFLocked`，前鏡頭也不渲染 Yellow

**`FocusStateInput.isFrontCamera` 傳遞守則**：
- `runNewFocusPipeline()` 中所有三個 `FocusStateInput` 建構點必須傳入 `isFrontCamera`
- 使用 `currentState.currentCamera == CameraLens.FRONT`（early return 路徑）
- 使用 `_state.value.currentCamera == CameraLens.FRONT`（async coroutine callback 路徑）
- `isFrontCamera = false` default 值只作為防呆，現有呼叫點必須明確傳入

**Log 識別**（Logcat 驗證用）：
```
# L1 攔截（Policy 層）
[v2.12.0-15] REJECT: front camera — manual tap or AE/AF lock not allowed (isManualTap=false, isAeAfLock=true)

# L2 攔截（StateMachine 層）
[v2.12.0-15 T3-F2] Front camera AE/AF lock request rejected — forced Unlocked
```

**How to apply**:
- 新增 `FocusStateInput` 建構點時，必須檢查是否在 `runNewFocusPipeline()` 內，若是則必須補入 `isFrontCamera`
- 修改 `reduceExposureState()` 時，前鏡頭 guard 必須置於函式最頂端（在 `isAeAfLockRequested` 判斷之前）
- 禁止將 L2 guard 移入 `buildOverlayModel()`（顏色層）—— ExposureLockState 本身必須正確，不只是顏色正確

---

### §E. Multi-Face 優先序驗證 Log 守則（DefaultTargetTracker）

**Rule**: `DefaultTargetTracker.update()` 的 primary 選擇必須同時輸出 `effectiveScore`（含 eyeBonus + typeBonus 加權後的最終分數），以便 Logcat 驗證優先序是否符合 SSOT。

**SSOT 優先序**（數值為 typeBonus，eyeBonus 額外 +2.0f）：

| 類型 | typeBonus | 有眼睛加成 | 最終 effective score 範例 |
|------|-----------|-----------|--------------------------|
| TAP_SUBJECT / TAP_TEXTURE | +10.0f | +2.0f | priorityScore + 12.0f |
| FACE（有眼睛） | +1.0f | +2.0f | priorityScore + 3.0f |
| FACE（無眼睛） | +1.0f | — | priorityScore + 1.0f |
| OBJECT | +0.5f | — | priorityScore + 0.5f |

**Log 格式**（Logcat tag: `DefaultTargetTracker`）：
```
[v2.12.0-15 T4-F1] update: 3 targets, primary=face_123 type=FACE hasEyes=true effectiveScore=4.2
```

**為什麼 Log 要重算 score（不直接用 maxByOrNull 的中間值）**：
- `maxByOrNull` 的 lambda 是內聯計算，結果不儲存
- Log 區塊獨立重算確保 log 與 selection 邏輯完全一致（copy 而非 reference）
- 日後修改 typeBonus 係數時，Log 和 selection 需同步更新（兩處）

**How to apply**:
- 修改 typeBonus 係數時，必須同步更新 `maxByOrNull` 和 Log 兩個 `when` block
- 若新增 `TargetType` enum 值，兩個 `when` block 均需補齊（否則編譯 exhaustive 錯誤）
- `primaryScore` log 精度預設為 Float 預設輸出，若需要更精確可用 `"%.2f".format(primaryScore)`

---

## SV-FIX-1R 焦土清除準則 — Dead Code 反向掃描（v2.12.0-17，2026-03-15）

> **背景**：v2.12.0-16 以 `runNewFocusPipeline()` 取代整個 legacy 自動對焦流程，但留下了兩個死亡殘骸：`autoFocusOnSalientTarget()` 的 `return` 後 legacy 區塊（~100 行），以及不再有有效呼叫點的 `focusOnPoint()` 函式（~200 行）。SV-FIX-1R 的任務是完全清除這些殘骸。

### §F. 焦土清除四條準則

**準則 1 — `return` 後的 dead code 必須立即刪除，不得「暫存參考」**

```kotlin
// ❌ 禁止此模式：
private fun autoFocusOnSalientTarget() {
    runNewFocusPipeline()
    return          // ← return 後方的所有程式碼為不可達 dead code
    val currentState = _state.value   // 永遠不會執行
    // ... 100 行 legacy 邏輯 ...
}

// ✅ 正確清除：
private fun autoFocusOnSalientTarget() {
    runNewFocusPipeline()
}
```

- **為什麼不能「暫存參考」**：dead code 在編譯器 / IDE 靜態分析下不會觸發警告（Kotlin 對不可達程式碼的告警只在確定性 return 之後），容易在數個版本後遺忘其存在，最終成為永久殭屍。
- **判斷標準**：只要函式開頭執行後 `return` 是無條件的（不在 if 分支內），其後所有程式碼即為 dead code，一律刪除。

**準則 2 — 無有效呼叫點的私有函式必須整體刪除（含 KDoc 與輔助邏輯）**

刪除步驟：
1. 全文搜尋目標函式名稱（含型態相同的函式呼叫）
2. 確認所有呼叫點均位於 dead code 路徑或本函式自身
3. 同步刪除：函式本體 + 雙層 KDoc + 僅此函式使用的 helper 函式

本次案例：
- 刪除 `focusOnPoint()` 本體（~200 行，含雙層 KDoc）
- 連帶刪除 `registerTapToFocusIntent()`（唯一呼叫點在 `focusOnPoint()` 內）

**準則 3 — 詞彙歸零：函式名稱、欄位名稱必須從所有位置清除（含註解與 KDoc）**

刪除主函式後，必須執行全域 Grep 掃描：
```
搜尋詞彙                          預期結果
focusOnPoint                    → 0 結果（含 KDoc 引用、log 說明）
focusFrameWidthPx               → 0 結果（含多行 // 註解）
FOCUS_PIPELINE_V2               → 0 結果（已在 v2.12.0-16 清除）
```

殘留位置類型（常被忽略）：
- 其他函式頂端的 `// Fix E: Cancel stale focusOnPoint() coroutine` 類說明
- `private var focusJob` 欄位的說明性註解
- 其他函式的 KDoc `@see` 或行內參照
- `getMeteringPointFactory` 的實作說明（`// mirrors LiveCameraSource.focusOnPoint()`）

**準則 4 — 修正後的 KDoc 與註解必須仍為真陳述，不可刪除語意**

刪除被引用函式後，引用該函式的 KDoc 段落不應直接刪除整行，而應改寫為仍成立的語意：

| 原始（已失效）| 修正後（仍成立）|
|---|---|
| `set the initial focusFrameWidthPx/HeightPx immediately` | `update focus frame dimensions immediately` |
| `Does NOT replace focusOnPoint() — manual tap still uses the legacy path` | `Manual tap uses setTapToFocusIntent() which is consumed on the next analysis frame` |
| `Cancel stale focusOnPoint() coroutine to prevent its delay(500) → focusPoint=null` | `Cancel any in-flight focus coroutine to prevent stale state updates racing with lockAEAF` |

---

### §G. 反向掃描清單（Dead Code 清除驗收用）

每次執行 legacy 清除後，依序掃描以下清單，**全部通過才算驗收完成**：

#### Step 1 — 呼叫點掃描
```bash
# 在目標檔案內搜尋被刪除的函式名稱
grep -n "deletedFunctionName" CameraViewModel.kt
# 預期：0 結果
```

#### Step 2 — 欄位/符號掃描（同時搜尋所有舊符號名稱）
```bash
grep -rn "focusFrameWidthPx\|FOCUS_PIPELINE_V2\|focusOnPoint" \
  app/src/main/java/
# 預期：0 結果於 CameraViewModel.kt；其他檔案的 repository 介面定義保留（不在清除範圍）
```

#### Step 3 — 孤兒 Helper 掃描
- 列出被刪除主函式所呼叫的全部 private helper 函式
- 逐一確認每個 helper 是否仍有其他呼叫點
- 無其他呼叫點 → 連帶刪除

#### Step 4 — KDoc / 註解一致性確認
- 全文搜尋已刪除的函式名稱（含在字串中）
- 確認沒有任何 `// ... focusOnPoint ...` 風格的行內說明殘留
- 確認 `focusJob` 等關聯欄位的說明性註解已更新

#### Step 5 — 編譯驗證
```bash
./gradlew assembleDeviceDebug
# 預期：BUILD SUCCESSFUL，0 errors
```

**本次 SV-FIX-1R 執行結果**（2026-03-15）：

| 掃描項目 | 結果 |
|---|---|
| `focusOnPoint` in CameraViewModel.kt | ✅ 0 matches |
| `focusFrameWidthPx` 全域 | ✅ 0 matches |
| `FOCUS_PIPELINE_V2` 全域 | ✅ 0 matches（v2.12.0-16 已清除）|
| 孤兒 helper `registerTapToFocusIntent()` | ✅ 連帶刪除 |
| `assembleDeviceDebug` | ✅ BUILD SUCCESSFUL |

---

## SV-FIX-2 Front/Fixed-Focus 硬體契約 — FLAG_AF 防線（v2.12.0-17，2026-03-15）

> **背景**：`CameraXFocusController.apply()` 使用單參數 `FocusMeteringAction.Builder(cxPoint)`，CameraX 預設行為是 `FLAG_AF | FLAG_AE | FLAG_AWB` 全部啟用。前鏡頭無 AF 馬達，送出 `FLAG_AF` 後 HAL 靜默失敗或產生 AF Hunting，但不拋出例外，難以從 Logcat 直接定位根因。

### §H. Front-Camera FLAG_AF 硬體契約守則

**核心規則**：`FocusMeteringAction.Builder` 第二參數（flags）**必須明確指定**，絕對禁止使用無參數的單參數建構子讓 CameraX 自行選擇 flags。

```kotlin
// ❌ 禁止此模式：
FocusMeteringAction.Builder(cxPoint).build()
// → CameraX 預設 FLAG_AF | FLAG_AE | FLAG_AWB，前鏡頭收到 FLAG_AF 後 HAL 靜默失敗

// ✅ 正確：依硬體能力明確指定
if (geometryContext.isFrontCamera || !decision.shouldTriggerFocus) {
    FocusMeteringAction.Builder(cxPoint, FocusMeteringAction.FLAG_AE or FocusMeteringAction.FLAG_AWB)
        .build()   // 不設 autoCancelDuration — 前鏡頭無 AF 馬達，不需 timeout
} else {
    FocusMeteringAction.Builder(cxPoint, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
        .apply { setAutoCancelDuration(AF_TIMEOUT_SEC, TimeUnit.SECONDS) }
        .build()
}
```

**判斷優先序**（`isFrontCamera` 為主要守門條件）：

| 條件 | Flags | `setAutoCancelDuration` | 理由 |
|------|-------|------------------------|------|
| `isFrontCamera == true`（無論 policy）| `FLAG_AE \| FLAG_AWB` | ❌ 不設定 | 硬體無 AF 馬達；即使 policy 誤送 `shouldTriggerFocus=true` 也強制攔截 |
| `shouldTriggerFocus == false`（後鏡頭）| `FLAG_AE \| FLAG_AWB` | ❌ 不設定 | 純曝光調整，不觸發 AF 掃描 |
| 後鏡頭 + `shouldTriggerFocus == true` | `FLAG_AF \| FLAG_AE` | ✅ `AF_TIMEOUT_SEC` | 完整 AF 流程，需 timeout 防 HAL 死鎖 |

**為什麼 `isFrontCamera` 必須是主要守門條件（而非 `shouldTriggerFocus`）**：
- `shouldTriggerFocus` 由 `EyeAfPolicyResolver` 決定，policy 邏輯複雜，未來改動可能誤送 `true`
- `isFrontCamera` 是硬體事實，不受 policy 層影響，永遠不會誤判
- 將硬體契約防線置於最靠近 HAL 呼叫點處（`CameraXFocusController`），確保即使 policy 層出錯也能兜底

**`setAutoCancelDuration` 不設定於前鏡頭的原因**：
- 前鏡頭走 AE-only 路徑，`FLAG_AF` 不送出，HAL 無 AF 掃描動作，無需 timeout guard
- 若對 AE-only action 設定 `setAutoCancelDuration`，部分 HAL 實作可能在 timeout 後強制 cancel metering，反而中斷曝光收斂

### §I. FLAG_AF 防線在架構中的位置

```
User Tap / Eye-AF Detection
        ↓
EyeAfPolicyResolver.resolve()    ← shouldTriggerFocus 決策層（可能誤判）
        ↓
FocusTriggerThrottle gate
        ↓
CameraXFocusController.apply()   ← 🔴 FLAG_AF 最終防線（isFrontCamera 硬體守門）
        ↓
cameraControl.startFocusAndMetering(action)
```

**守則**：任何修改 `CameraXFocusController.apply()` flags 邏輯的 PR，必須確認：
1. `FocusMeteringAction.Builder` 在整個檔案中出現次數為 **2**（if 分支 + else 分支），不多不少
2. 前鏡頭分支（`isFrontCamera == true`）的 Builder 第二參數絕對不含 `FLAG_AF`
3. 後鏡頭分支（`shouldTriggerFocus == true`）的 Builder 第二參數包含 `FLAG_AF`

**Log 識別**（Logcat 驗證執行路徑用）：
```
# 前鏡頭 / AE-only 路徑
[v2.12.0-17][SV-FIX-2] AE+AWB only (front/fixed-focus) isFrontCamera=true shouldTriggerFocus=false

# 後鏡頭 AF 路徑
[v2.12.0-17][SV-FIX-2] AF+AE (rear camera) shouldTriggerFocus=true
```

**Code Location**：`CameraXFocusController.kt:88-102`（`// Build CameraX MeteringAction` 區塊）

**參考實作**（既有正確範例）：`LiveCameraSource.kt` 的 `aeOnlyAction` 建構（`FLAG_AE | FLAG_AWB`）

**How to apply**：
- 每次觸碰 `FocusMeteringAction.Builder` 相關程式碼時，先確認 `isFrontCamera` 是否為最外層判斷
- 新增相機鏡頭類型（如超廣角、長焦）時，確認其 AF 能力後再決定 flags；有疑慮時默認 `FLAG_AE | FLAG_AWB`
- 禁止在 Policy 層（`EyeAfPolicyResolver`）或 ViewModel 層修改 flags——flags 邏輯只屬於 `CameraXFocusController`

---

## SV-FIX-3 Matrix Truth — ViewModel / Mapper 職責邊界（v2.12.0-17，2026-03-15）

> **背景**：`rebuildNewPipelineSensorToViewMatrix()` 在 `CoordinateTransformer` 尚未就緒時，自行用 `bounds.width() / effectiveImgW` 計算 FIT_CENTER 近似 matrix。這段 fallback 與 `PreviewViewGeometryMapper` 既有的 `matrix == null` FIT_CENTER fallback 形成職責重疊，且在旋轉 90°/270° 時 scale ratio 計算錯誤（bounds 與 effectiveImgW 的座標系不一致），並留有 `TODO(SB-F2-future)` 違反零 TODO 政策。

### §J. ViewModel / Mapper 幾何職責邊界守則

**核心規則**：ViewModel 只負責「有 transformer 時提供精確 matrix」，Mapper 負責「無 matrix 時的 FIT_CENTER fallback」。兩層職責不得重疊。

```
CameraViewModel.rebuildNewPipelineSensorToViewMatrix()
    ├── transformer 可用  → latestSensorToViewMatrix = 三點反推 affine matrix  ✅ ViewModel 職責
    └── transformer 不可用 → latestSensorToViewMatrix = null                   ✅ ViewModel 職責止於此

PreviewViewGeometryMapper.mapRectImageToView()
    ├── context.sensorToViewMatrix != null → matrix.mapPoints(pts)             ✅ Mapper 主路徑
    └── context.sensorToViewMatrix == null → FIT_CENTER scale + letterbox       ✅ Mapper fallback 職責
```

**禁止模式**（已在 SV-FIX-3 清除）：

```kotlin
// ❌ ViewModel 自行實作 FIT_CENTER 近似 — 職責錯置，且計算有誤
val scaleX = bounds.width() / effectiveImgW.toFloat()   // ← contentBounds 與 image 座標系不一致
val scaleY = bounds.height() / effectiveImgH.toFloat()  //   旋轉 90°/270° 時產生錯誤 scale ratio
latestSensorToViewMatrix = android.graphics.Matrix().apply {
    if (isFront) { setScale(-scaleX, scaleY); postTranslate(bounds.right, bounds.top) }
    else         { setScale(scaleX, scaleY);  postTranslate(bounds.left,  bounds.top) }
}
// TODO(SB-F2-future): Replace with real matrix ... ← 違反零 TODO 政策

// ✅ 正確：transformer 不可用時直接歸零，職責還給 Mapper
latestSensorToViewMatrix = null
Log.d(TAG, "[v2.12.0-17] transformer not ready — sensorToViewMatrix=null, Mapper will use FIT_CENTER fallback")
```

**為什麼 ViewModel 的 FIT_CENTER 近似計算是錯的**：

| 旋轉角度 | `effectiveImgW` 語意 | `bounds.width()` 語意 | scaleX 結果 |
|----------|---------------------|-----------------------|------------|
| 0° / 180° | sensor 寬（橫向） | PreviewView 寬（橫向）| 近似正確 |
| 90° / 270° | sensor 高（已 swap，變成「縱向寬」）| PreviewView 寬（橫向）| **錯誤**：用縱向尺寸除橫向尺寸 |

Pixel 9a 主鏡頭預設旋轉 90°，因此此近似在實機上必然產生錯誤的幾何映射。

**`TODO` 作為技術債指示器的問題**：
- `TODO(SB-F2-future)` 表示「等待 CameraX API」，但實際上正確解法就是「transformer 不可用時讓 Mapper fallback」，不需要任何新 API
- 帶有遠期標籤的 TODO 容易在數個版本後成為永久殭屍，且掩蓋了「當下已有正確解」的事實

**How to apply**：
- 任何涉及 sensor→view 座標轉換的新邏輯，必須先確認職責歸屬：「需要精確幾何 → ViewModel 三點反推 matrix」，「需要 fallback → Mapper 已有實作，不重複」
- 修改 `rebuildNewPipelineSensorToViewMatrix()` 時，確認函式末尾只有一種 null 路徑（transformer 不可用），不得新增任何近似計算分支
- `PreviewViewGeometryMapper` 的 `matrix == null` fallback 路徑是此設計的底板，**禁止刪除或弱化**

**Code Location**：`CameraViewModel.kt` `rebuildNewPipelineSensorToViewMatrix()` 末段（`// transformer not ready` 區塊）

---

## SV-FIX-3R Matrix Truth 最終收斂 — 三點反推移除與 toMatrix() 直接建構（v2.12.0-17，2026-03-16）

> **背景**：`rebuildNewPipelineSensorToViewMatrix()` 原先以 `CoordinateTransformer.imageToScreen()` 採樣 3 個角點，再用 `Matrix.setValues()` 反推仿射矩陣。此方法需要 NaN guard（`isFinite()` 檢查），且邏輯間接；矩陣來源不透明。SV-FIX-3R 將此移除，改在 `CoordinateTransformer` 加入 `toMatrix()` 方法，直接從已知幾何欄位建構矩陣，消除採樣誤差與 NaN 風險。

### §K. 三點反推 vs 直接建構守則

**核心規則**：若目標 Transform 持有所有幾何欄位（scale、translate、mirror），應直接建構 `android.graphics.Matrix`，不得以採樣 + 反推方式繞行。

**三點反推的問題（已清除）**：

```kotlin
// ❌ 三點採樣反推 — 間接、需 NaN guard、不透明
val (sx0, sy0) = transformer.imageToScreen(0f, 0f)
val (sx1, sy1) = transformer.imageToScreen(effectiveImgW.toFloat(), 0f)
val (sx2, sy2) = transformer.imageToScreen(0f, effectiveImgH.toFloat())
if (!sx0.isFinite() || ...) { /* fallback */ }
// Build affine matrix from 3-point correspondence.
latestSensorToViewMatrix = android.graphics.Matrix().apply {
    setValues(floatArrayOf(
        (sx1 - sx0) / effectiveImgW, (sx2 - sx0) / effectiveImgH, sx0,
        (sy1 - sy0) / effectiveImgW, (sy2 - sy0) / effectiveImgH, sy0,
        0f, 0f, 1f,
    ))
}
```

**正確實作（CoordinateTransformer.toMatrix()）**：

```kotlin
// ✅ 直接建構 — 透明、無 NaN 風險、職責歸屬明確
fun toMatrix(): android.graphics.Matrix {
    val scaleX = contentBounds.width() / imageWidth
    val scaleY = contentBounds.height() / imageHeight
    return android.graphics.Matrix().apply {
        if (isFrontCamera) {
            setValues(floatArrayOf(
                -scaleX, 0f, contentBounds.right,   // mirror: translateX = right boundary
                0f, scaleY, contentBounds.top,
                0f, 0f, 1f,
            ))
        } else {
            setValues(floatArrayOf(
                scaleX, 0f, contentBounds.left,
                0f, scaleY, contentBounds.top,
                0f, 0f, 1f,
            ))
        }
    }
}
```

**呼叫端（rebuildNewPipelineSensorToViewMatrix）**：

```kotlin
// ✅ 一行完成，無 NaN guard 需求
val transformer = currentState.coordinateTransformer
if (transformer != null) {
    latestSensorToViewMatrix = transformer.toMatrix()
    Log.d(TAG, "[v2.12.0-17][SV-FIX-3R] sensorToViewMatrix set from CoordinateTransformer.toMatrix()")
    return
}
// transformer not ready → null → Mapper fallback（職責邊界同 §J）
latestSensorToViewMatrix = null
```

**為何三點反推數學等價但不應使用**：

| | 三點採樣反推 | toMatrix() 直接建構 |
|---|---|---|
| NaN 風險 | 有（需 isFinite() guard） | 無（除數 imageWidth/Height > 0 已在建構時驗證）|
| 透明度 | 低（中間浮點誤差累積） | 高（直接對應幾何欄位）|
| 維護性 | 差（新增座標系需重新驗證公式）| 好（字面對應 contentBounds / scale）|
| 職責歸屬 | ViewModel 重複 Transformer 邏輯 | Transformer 自身持有，ViewModel 只呼叫 |

**How to apply**：
- 任何需要從 `CoordinateTransformer` 取矩陣的場景，呼叫 `toMatrix()`，禁止再用 `imageToScreen()` 三點採樣
- 新增幾何欄位到 `CoordinateTransformer` 時，同步更新 `toMatrix()` 的對應項（新增 skew、perspective 等）
- `toMatrix()` 是 `CoordinateTransformer` 的「矩陣形式輸出」，與 `imageToScreen()` 的「點轉換輸出」並列存在，兩者數學等價但使用場景不同

**Code Location**：`PreviewViewportContainer.kt` `CoordinateTransformer.toMatrix()`（新增）；`CameraViewModel.kt` `rebuildNewPipelineSensorToViewMatrix()`（移除三點反推，改呼叫 `toMatrix()`）

---

## SV-FIX-3R-FINAL Geometry Truth 架構收斂 — Matrix 建構遷移至 PreviewViewGeometryMapper（v2.12.0-17，2026-03-16）

> **背景**：SV-FIX-3R 將三點反推移除後，`toMatrix()` 仍住在 `CoordinateTransformer`（presentation 層）。架構複審確立此為層次邊界違規：V2 管線（`PreviewViewGeometryMapper`）不應依賴 presentation 層提供矩陣。SV-FIX-3R-FINAL 將 matrix 建構邏輯遷移至 `PreviewViewGeometryMapper.createSensorToViewMatrix()`，使 V2 管線完全自治。

### §L. ViewModel 僅接線、Mapper 承接幾何與 Fallback（SSOT 邊界）

**這是本專案幾何管線最重要的一條邊界守則，違反此守則將造成職責重疊、坐標系混亂與層次污染。**

#### 邊界定義

```
【ViewModel 的唯一職責】
CameraViewModel.rebuildNewPipelineSensorToViewMatrix()
    ├── contentBounds 有效 + imageDims 有效
    │       → newPipelineGeometryMapper.createSensorToViewMatrix(bounds, w, h, isFront)
    │       → latestSensorToViewMatrix = 結果
    └── 任一無效
            → latestSensorToViewMatrix = null
            → 不計算任何幾何，交棒給 Mapper

【Mapper 的完整職責】
PreviewViewGeometryMapper.mapRectImageToView(rect, context)
    ├── context.sensorToViewMatrix != null
    │       → matrix.mapPoints(pts)              ← 精確路徑
    └── context.sensorToViewMatrix == null
            → applyFitCenterFallback(pts, context) ← FIT_CENTER fallback（禁止刪除）

【PreviewViewGeometryMapper.createSensorToViewMatrix()】
    ← matrix 建構邏輯唯一來源（v2.12.0-17 起）
    ← ViewModel 只呼叫，不計算幾何
```

#### 為何 ViewModel 不應自行建構 Matrix

| 場景 | ViewModel 自行建構的問題 |
|------|------------------------|
| transformer 可用 | ViewModel 重複 Transformer/Mapper 的幾何邏輯 → 兩份邏輯需同步維護 |
| transformer 不可用 | ViewModel 用 `bounds.width() / imgW` 近似 → 旋轉 90°/270° 時 scale 計算錯誤（§J 已記錄）|
| 前鏡頭 mirror | ViewModel 手工 `setScale(-scaleX, ...)` → 與 Mapper mirror 邏輯重複，不一致時產生雙重 mirror |

#### SV-FIX-3R-FINAL 後的正確實作

**`PreviewViewGeometryMapper.createSensorToViewMatrix()`（新增，data 層）**：

```kotlin
// ✅ Matrix 建構唯一來源 — data/domain 層，V2 管線自治
fun createSensorToViewMatrix(
    contentBounds: RectF,
    imageWidth: Int,
    imageHeight: Int,
    isFrontCamera: Boolean,
): Matrix {
    val scaleX = contentBounds.width() / imageWidth
    val scaleY = contentBounds.height() / imageHeight
    return Matrix().apply {
        if (isFrontCamera) {
            setValues(floatArrayOf(-scaleX, 0f, contentBounds.right, 0f, scaleY, contentBounds.top, 0f, 0f, 1f))
        } else {
            setValues(floatArrayOf(scaleX, 0f, contentBounds.left, 0f, scaleY, contentBounds.top, 0f, 0f, 1f))
        }
    }
}
```

**`CameraViewModel.rebuildNewPipelineSensorToViewMatrix()`（接線，presentation 層）**：

```kotlin
// ✅ ViewModel 僅接線：取 contentBounds + isFrontCamera，呼叫 Mapper，不計算幾何
val contentBounds = currentState.contentBounds
val isFrontCamera = currentState.currentCamera == CameraLens.FRONT
if (!contentBounds.isEmpty) {
    latestSensorToViewMatrix = newPipelineGeometryMapper.createSensorToViewMatrix(
        contentBounds, effectiveImgW, effectiveImgH, isFrontCamera
    )
    return
}
latestSensorToViewMatrix = null  // Mapper fallback 接手
```

#### 層次邊界遷移歷史（§J → §K → §L）

| 版本 | 問題 | 修復 |
|------|------|------|
| SV-FIX-3（§J）| ViewModel 自行 FIT_CENTER 近似，旋轉 90° scale 錯誤 | 移除近似，transformer 不可用時直接 null |
| SV-FIX-3R（§K）| 三點採樣反推（imageToScreen × 3 + setValues），NaN 風險 | 改 `CoordinateTransformer.toMatrix()` 直接建構 |
| SV-FIX-3R-FINAL（§L）| `toMatrix()` 住在 presentation 層，V2 管線依賴外層 | 將建構邏輯遷移至 `PreviewViewGeometryMapper.createSensorToViewMatrix()`，ViewModel 僅接線 |

**How to apply**：
- **新增幾何轉換需求時**：邏輯寫在 `PreviewViewGeometryMapper`，ViewModel 只提供輸入參數並接收結果
- **修改 `rebuildNewPipelineSensorToViewMatrix()` 時**：函式內只允許「取 state 欄位 → 呼叫 `newPipelineGeometryMapper.createSensorToViewMatrix()`」，不允許任何 scale/translate 計算
- **`applyFitCenterFallback()` 是 Mapper 的底板**：禁止刪除、弱化或在 ViewModel 複製此邏輯
- **`CoordinateTransformer.toMatrix()`**：仍保留供 OF 路徑（legacy）使用，但 V2 管線禁止呼叫

**Code Location**：
- `PreviewViewGeometryMapper.kt:110` `createSensorToViewMatrix()`（新增）
- `CameraViewModel.kt` `rebuildNewPipelineSensorToViewMatrix()`（改呼叫 `createSensorToViewMatrix`，不再依賴 `coordinateTransformer`）

---

## Phase 3 UX Polish — 專業相機體驗最終語義收斂（v2.12.0-18，2026-03-16）

> **背景**：Focus Pipeline V2 地基清理完畢（v2.12.0-17）後，進入 UX 拋光階段。
> 本節記錄三項設計決策的最終語義，作為後續版本的 SSOT 驗收基線。

---

### §M. Fixed-Focus LensFocusState 語義正規化（SV-PH3-FIX）

#### 問題根源
v2.12.0-18 初版（Task 2）將 `isFixedFocus && primaryTarget != null` 對應到 `UnsupportedFixedFocus`。
這導致 `FocusOverlay` 的夾爪動畫在前鏡頭永遠不觸發，因為 `LensFocusState` 語義傳遞到 UI 層時
`UnsupportedFixedFocus` 表示「硬體不支援」而非「已鎖定」。

#### 正確語義（SV-PH3-FIX，最終定版）

```kotlin
// DefaultFocusStateMachine.reduceLensState()
if (input.isFixedFocus) {
    return if (input.trackedTargetState.primaryTarget != null) {
        LensFocusState.Focused        // 視覺欺騙：直接宣告鎖定，不等待 HAL
    } else {
        LensFocusState.NotRequested   // 無目標：靜默，不顯示任何鎖定狀態
    }
}
```

#### 架構純潔性保證
- `isFixedFocus == true` 路徑在 `focusControllerResult` 被讀取之前就已 `return`
- HAL AF 回調路徑（`focusControllerResult?.opticalResult`）只在 `isFixedFocus == false` 時執行
- `UnsupportedFixedFocus` 語義保留給 HAL 主動回報 `OpticalFocusState.UNSUPPORTED` 的情境（非固定焦距、但 HAL 無法執行 AF）

#### 三軸狀態在固定焦距下的完整行為

| 軸 | 輸入條件 | 輸出 |
|----|---------|------|
| VisualTargetState | 由 `DefaultEyeAfPolicyResolver` 決定（SYNTHETIC_LOCK → VisualLocked） | 正常流程 |
| LensFocusState | `isFixedFocus=true && primaryTarget != null` | `Focused`（**§M 新定義**） |
| LensFocusState | `isFixedFocus=true && primaryTarget == null` | `NotRequested` |
| ExposureLockState | `isFrontCamera=true` | 強制 `Unlocked`（§D 守則不變） |

**How to apply**：
- 修改 `reduceLensState()` 時，`isFixedFocus` guard 必須是第一個判斷，且必須提前 return
- 禁止在 `isFixedFocus == true` 路徑中讀取 `focusControllerResult`（同步純潔性）
- `UnsupportedFixedFocus` 不代表「固定焦距鏡頭」—— 它代表「HAL 告知 AF 不支援」，兩者語義不同

---

### §N. 樂觀點擊 UI — 零延遲 Yellow→Cyan 過渡（Task 1）

#### 設計決策
使用者點擊畫面時，在硬體 AF 啟動前先顯示 Yellow 72dp 佔位框（`TrackingStable` 狀態），
約 100ms 後由正常 pipeline 升級為 Cyan `VisualLocked`（夾爪觸發）。

- **Yellow 在此情境的語義**：「系統已收到指令、正在確認目標」，與 AE/AF Lock Yellow 不同
- **非 AE/AF Lock 語義**：不觸發 `ExposureLockState.AEAFLocked`，Yellow 只存活於 `pendingTapToFocusIntent` 有效期內（≤750ms）
- `FocusOverlay` 的 `animateColorAsState(tween 200ms)` 負責 Yellow→Cyan 顏色動畫

#### 實作邊界規則

```
setTapToFocusIntent() 的職責：
  1. 設定 pendingTapToFocusIntent（供 pipeline 消費）
  2. 立即 push 一個 FocusUiState(TrackingStable, YELLOW, 72dp box at tap coords)
  → 不觸發任何 HAL 呼叫
  → 不修改 isAEAFLocked 欄位

runNewFocusPipeline() 在下一幀（~100ms）自然覆蓋此狀態
```

**SV 驗收線**：`tap_optimistic` stableId 只出現在 `setTapToFocusIntent()` 的 push 中，
`runNewFocusPipeline()` 的任何輸出絕不應產生 `primaryStableId = "tap_optimistic"`。

**How to apply**：
- `setTapToFocusIntent()` 是唯一允許直接 push Yellow FocusUiState 的地方
- `DefaultFocusStateMachine` 禁止在 `buildOverlayModel()` 中產生 YELLOW unless `ExposureLockState.AEAFLocked`
- 如需調整 72dp 大小，修改 `halfPx = 72f * density / 2f` 算式；density 來自 `FocusOnPoint.density`

---

### §O. Primary Monopoly — 1-Euro Filter 主目標壟斷守則（Task 3）

#### 設計決策
只有 `primaryTarget` 的 `rectView`（映射至 OverlayModel.primaryBox）經過 `RectFilter`（1-Euro Filter）平滑。
`secondaryBoxes` 使用 ML Kit 原始座標，不平滑，渲染為 10dp 四角括號、固定 alpha 0.3。

#### 架構邊界

```
ML Kit 偵測 → TargetTracker.update() → TrackedTargetState（所有 target 均為原始座標）
                                              ↓
                               FocusStateMachine.reduce()
                                              ↓
                                       FocusUiState
                                              ↓
                                applySmoother()          ← 唯一平滑點（ViewModel）
                                  ├── primaryBox → RectFilter.filter()  ← 1-Euro 平滑
                                  └── secondaryBoxes → 原樣傳遞（不觸碰）
                                              ↓
                               _state.update(focusUiState = smoothedUiState)
```

#### 不變量（Invariant）

1. `applySmoother()` 只修改 `overlayModel.primaryBox`，不修改 `secondaryBoxes`
2. `primaryStableId` 改變時，`primaryTargetSmoother.reset()` 必須先於下一次 `filter()` 呼叫
3. `resetFocusState()` 與 `switchCamera()` 兩個路徑均須呼叫 `primaryTargetSmoother.reset()`
4. 平滑後的 `primaryBox` 僅用於 UI 渲染；AF 硬體座標（`primaryTarget.rectView` 原始值）不受影響

#### FocusOverlay Secondary 渲染規格

```kotlin
// 固定規格（不得動態化）
val secondaryColor = Color(0xFF00F2FF).copy(alpha = 0.3f)  // 固定 Cyan, 固定 0.3
val bracketSize = 10.dp.toPx()   // 四角括號長度
val bracketStroke = strokeWidthPx * 0.7f  // 比 primary 細
// 繪製：四角 [ ] 線段（不是完整矩形框）
// 不使用 animatedAlpha（secondary 沒有生命週期動畫）
```

**SV 驗收線**：`FocusOverlay` 內禁止對 `secondaryBoxes` 呼叫任何 `animateXxx` 或 `LaunchedEffect`。

**How to apply**：
- 新增平滑邏輯時，只能在 `applySmoother()` 函式內操作
- 禁止在 `DefaultTargetTracker`、`FocusStateMachine` 或 `EyeAfPolicyResolver` 中進行座標平滑
- `RectFilter` 參數（`minCutoff=1.0f, beta=0.007f`）為調校基線；若需調整，同步更新此節

**Code Location**：
- `CameraViewModel.kt` `applySmoother()`（新增，主平滑入口）
- `CameraViewModel.kt` `primaryTargetSmoother: RectFilter`（欄位）
- `FocusUiSmoother.kt` `RectFilter` / `OneEuroFilter`（已有實作，§O 正式啟用）
- `FocusOverlay.kt` secondary brackets 渲染區塊

---

## 🚀 [Phase 3] 專業相機 UX 拋光 (Pragmatic Refactor) 結案與架構真理

**結案日期**: 2026-03-16
**版本**: v2.12.0-18

**核心成就**: 完成「樂觀 UI」、「前鏡頭視覺欺騙」與「主從壟斷」實作，並通過 Traycer 最終無情法官驗收 (Path A)。

在 Phase 3 的實戰與驗收中，我們確立了以下 3 項絕對不可破壞的 SSOT 驗收基線。未來的任何重構或功能迭代，皆不得違反此處定義的語義與效能邊界。

---

### ⚖️ 真理一：前鏡頭「視覺欺騙」與狀態語義正規化 (Fixed-Focus Normalization)

**硬體現實**：前鏡頭 (Fixed-Focus) 物理上不支援 AF，若向 HAL 送出 `FLAG_AF` 將導致狀態機死鎖。

**狀態機語義 (SSOT)**：為支援 UI 的「鎖定成功（極光藍夾爪）」視覺回饋，`DefaultFocusStateMachine.reduceLensState()` 在處理 `isFixedFocus == true` 時，絕對不可回傳 `UnsupportedFixedFocus`。

**強制映射**：

| 條件 | 強制輸出 |
|------|---------|
| `isFixedFocus == true && primaryTarget != null` | `LensFocusState.Focused` |
| `isFixedFocus == true && primaryTarget == null` | `LensFocusState.NotRequested` |

**防禦目的**：解耦底層物理限制與上層 UI 渲染，透過合法的狀態偽裝達成完美的「樂觀 UX 體驗」。

> **SV 驗收線**：`reduceLensState()` 內 `isFixedFocus` guard 必須是第一個判斷並提前 return，禁止在此路徑中讀取 `focusControllerResult`（HAL 同步純潔性）。`UnsupportedFixedFocus` 的語義是「HAL 告知 AF 不支援」，非「這是固定焦距鏡頭」——兩者語義不同，不可混用。

---

### ⚡ 真理二：HAL 回調解耦 (No Waiting for HAL)

**同步路徑優先**：在 Tap-to-Focus（點擊對焦）或前鏡頭 AE 鎖定場景中，狀態機必須在發起硬體 Action 後的同步路徑中立刻推進狀態，瞬間在畫面上畫出 72dp 黃色預設框或觸發鎖定動畫。

**絕對禁區**：
- 嚴禁使用 `Thread.sleep` 進行任何 UI 延遲過渡
- 嚴禁前鏡頭狀態機「等待（Await/Suspend）」來自 CameraX HAL 的 `FocusControllerResult` 回調才更新畫面

**實作邊界**：

```
setTapToFocusIntent() 同步路徑：
  1. 設定 pendingTapToFocusIntent（供 pipeline 消費）
  2. 立即 push FocusUiState(TrackingStable, YELLOW, 72dp box at tap coords)
  → 不觸發任何 HAL 呼叫
  → 不修改 isAEAFLocked 欄位

runNewFocusPipeline() 在下一幀（~100ms）自然覆蓋此狀態

FocusOverlay.animateColorAsState(tween 200ms) 自動處理 Yellow→Cyan 顏色過渡
```

> **SV 驗收線**：`tap_optimistic` stableId 只出現在 `setTapToFocusIntent()` 的 push 中。`runNewFocusPipeline()` 的任何輸出絕不應產生 `primaryStableId = "tap_optimistic"`。`DefaultFocusStateMachine.buildOverlayModel()` 禁止產生 `OverlayColor.YELLOW` unless `ExposureLockState == AEAFLocked`。

---

### 👑 真理三：主從壟斷與效能壁壘 (Primary Monopoly & Smoothing Isolation)

**算力分配不平等**：手機 CPU 算力必須極端傾斜給 `primaryTarget`（主目標）。

**架構邊界 (SSOT)**：

| 對象 | 平滑 | Alpha 動畫 | 渲染格式 |
|------|------|-----------|---------|
| Primary Target | ✅ 1-Euro Filter（`RectFilter`） | ✅ 完整呼吸/漸變動畫 | 完整矩形框 + 夾爪 |
| Secondary Targets | ❌ 嚴禁傳入平滑器 | ❌ 嚴禁 `animateXxx` | 10dp 四角 `[ ]` brackets，固定 alpha=0.3 |

**不變量（Invariant）**：
1. `applySmoother()` 是唯一平滑閘門，只修改 `overlayModel.primaryBox`，不修改 `secondaryBoxes`
2. `primaryStableId` 改變時，`primaryTargetSmoother.reset()` 必須先於下一次 `filter()` 呼叫
3. `resetFocusState()` 與 `switchCamera()` 兩個路徑均須呼叫 `primaryTargetSmoother.reset()`
4. 平滑後的 `primaryBox` 僅用於 UI 渲染；AF 硬體座標（`primaryTarget.rectView` 原始值）不受影響

**防禦目的**：防止未來 AI 模型產生「公平對待所有目標」的 Additive Bias，導致多目標場景下發生嚴重的 UI 掉幀與設備發燙。

> **SV 驗收線**：`FocusOverlay` 內禁止對 `secondaryBoxes` 呼叫任何 `animateXxx` 或 `LaunchedEffect`。禁止在 `DefaultTargetTracker`、`FocusStateMachine` 或 `EyeAfPolicyResolver` 中進行座標平滑。

---

**Phase 3 結案聲明**：以上三條真理構成 v2.12.0 對焦系統的最終架構護城河。任何未來版本（無論是 Claude 5.0、GPT-6 或其他 AI）在修改對焦系統前，必須先閱讀並遵守此三條真理。違反者視為架構污染，應立即回滾。

---

## ML Kit 安全基線 (v2.12.0-19)

**確立日期**: 2026-03-18
**版本**: v2.12.0-19（SV-MLKIT-FIX-01）

本節記錄三條因實機 Crash 確立的 ML Kit 鐵律。任何修改 ML Kit 偵測器初始化或 ImageProxy 處理的 PR，必須逐條核對。

---

### ✅ 鐵律 A：LANDMARK_MODE_NONE 為安全基線（禁止回退 ALL）

**問題根因**：`LANDMARK_MODE_ALL` 要求 ML Kit 從裝置自動下載 landmark 模型。若下載失敗、模型不相容或裝置型號不在 Google Play Services 支援列表，`FaceDetector` 在推論時拋出 `"Unknown landmark type"` Exception，導致靜默 Crash（無 ANR，直接 skip frame，但 detection pipeline 死亡）。

**強制基線**：

| 參數 | 禁止值 | 強制值 | 原因 |
|------|--------|--------|------|
| `setLandmarkMode` | `LANDMARK_MODE_ALL` | `LANDMARK_MODE_NONE` | 設備相容性 Crash |
| `setContourMode` | `CONTOUR_MODE_ALL` | `CONTOUR_MODE_NONE` | 同上，contour 模型更重 |

**降級路徑（Eye-AF 不受影響）**：Eye-AF 的「眼睛座標」由 `CameraViewModel:929` 的 face bbox center fallback 提供，精度略降但功能完整。`EyePosition` domain model 保留（forward compat），但 `extractEyePositions()` 已刪除。

**禁止行為**：
- ❌ 任何情況下重新啟用 `LANDMARK_MODE_ALL`（包括「特定設備優化」flag）
- ❌ 用 `try-catch` 包住 `LANDMARK_MODE_ALL` 然後 fallback — 這會掩蓋問題並使 Crash 變成性能劣化

> **SV 驗收線**：`MLKitDetector.kt` 與 `CameraRepositoryImpl.kt` 兩個偵測器初始化路徑必須同步為 `LANDMARK_MODE_NONE`。任一路徑回退 ALL 即觸發 SV 失敗。

**Code Locations**：
- `MLKitDetector.kt` `faceDetector` lazy init（Device/legacy path）
- `CameraRepositoryImpl.kt` `faceDetector` lazy init（Emulator/SmartCameraAnalyzer path）

---

### ✅ 鐵律 B：imageProxy.close() 必須在跳幀路徑同步呼叫

**問題根因**：CameraX ImageAnalysis 的 backpressure 策略（`STRATEGY_KEEP_ONLY_LATEST`）要求每個 `ImageProxy` 必須被 `close()`。若 frame-skip 路徑忘記呼叫，CameraX 管線會在下一幀等待舊 buffer 釋放，直到 timeout 觸發 `IllegalStateException` 或相機凍結。

**Frame-skip 模式（SmartCameraAnalyzer L0 gate）**：

```kotlin
// CORRECT — skip 路徑必須立即 close
frameCount++
if (frameCount % analysisInterval != 0) {
    imageProxy.close()   // ← 這行不可省略
    return
}
```

**三個必須 close 的路徑**：

| 路徑 | close 位置 | 備注 |
|------|-----------|------|
| L0 frame-skip | `imageProxy.close()` 在 `return` 前 | 同步，不進入 coroutine |
| L1 time-gate skip | `imageProxy.close()` 在 `return` 前 | 同步，不進入 coroutine |
| 正常分析路徑 | `finally { imageProxy.close() }` | 在 `launch` coroutine 的 finally |

**禁止行為**：
- ❌ 在 skip 路徑漏掉 `imageProxy.close()`
- ❌ 把 `imageProxy.close()` 放在 coroutine launch 之外的 `finally` 然後 launch 內又 close（double close）
- ❌ 在 L0/L1 skip 路徑進入任何 suspend 函式再 close

> **SV 驗收線**：`analyze()` 函式中每一個 `return` 語句前方（在相同 scope 層級）必須有 `imageProxy.close()`。唯一例外是 `return` 在 coroutine 內部且 finally 已覆蓋。

**Code Location**：`SmartCameraAnalyzer.kt` `analyze()` — L0 gate（line ~272）、L1 gate（line ~280）、launch finally block。

---

### ✅ 鐵律 C：mediaImage null-safe guard（!! 絕對禁止）

**問題根因**：`imageProxy.image` 在以下情況返回 null：裝置記憶體壓力、ImageReader buffer 已被 recycle、或 YUV_420_888 buffer 在 CameraX internal 時序邊界被釋放。`imageProxy.image!!` 在此情況拋出 NPE，導致 `analyze()` 協程死亡，CameraX pipeline 進入不可恢復狀態直到 unbind。

**強制寫法**：

```kotlin
// CORRECT
val mediaImage = imageProxy.image ?: run {
    Log.w(TAG, "[v2.12.0] mediaImage null — aborting")
    return@launch   // 讓 finally 執行 imageProxy.close()
}
val inputImage = InputImage.fromMediaImage(mediaImage, ...)

// FORBIDDEN
val inputImage = InputImage.fromMediaImage(imageProxy.image!!, ...)
```

**適用範圍**：所有呼叫 `imageProxy.image` 的位置，包括 `MLKitDetector.detect()`、`SmartCameraAnalyzer.analyze()`、未來任何新增的 analyzer。

> **SV 驗收線**：全專案禁止 `imageProxy.image!!`。Grep `image!!` 零容忍。

**Code Locations**：
- `SmartCameraAnalyzer.kt` `analyze()` launch block（line ~310）
- `MLKitDetector.kt` `detect()` — null guard + caller 負責 close 的 KDoc 已確立

---

### 📋 ML Kit 初始化參數總覽（v2.12.0-19 基線）

| 參數 | 人臉偵測器 | 物件偵測器 |
|------|-----------|-----------|
| mode | `PERFORMANCE_MODE_FAST` | `STREAM_MODE` |
| landmark | `LANDMARK_MODE_NONE` 🔴 | N/A |
| contour | `CONTOUR_MODE_NONE` 🔴 | N/A |
| classification | `CLASSIFICATION_MODE_NONE` | ✅ enabled |
| tracking | `enableTracking()` | `enableMultipleObjects()` |
| minFaceSize | `0.15f`（MLKitDetector）/ `MIN_FACE_SIZE`（CameraRepositoryImpl） | N/A |

🔴 = 不可修改的 Crash 防禦參數

---

## Compose 局部重組防禦模式 (v2.12.0-19)

**確立日期**: 2026-03-18
**版本**: v2.12.0-19（FocusIndicatorOverlay 接線票據）

本節記錄「StateFlow 傳入子 Composable 後內部 collect」的局部重組防禦模式，防止高頻 UI 狀態（如每幀更新的對焦座標）污染父層 Composable 的重組節奏。

---

### ✅ Confirmed：局部重組防禦三步驟

**問題根因**：`CameraScreen` 頂層 `val state by viewModel.state.collectAsState()` 訂閱整個 `CameraState`。若 `focusUiState`（包含 X/Y 座標、bbox）在每幀更新，`CameraState.equals()` 每幀都不同，導致 `CameraScreen` 整體（包括快門按鈕、Grid、HDR 按鈕）以 60fps 全量重組——CPU 爆炸的根本原因。

**解法三步驟**：

#### Step 1：ViewModel 暴露孤立 StateFlow

```kotlin
// CameraViewModel.kt
val focusUiStateFlow: StateFlow<FocusUiState> =
    _state.map { it.focusUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FocusUiState.EMPTY,
        )
```

**Why Eagerly**：`Eagerly` 確保 ViewModel 建立時立即訂閱 `_state`，無 cold start delay。`WhileSubscribed` 在 screen 旋轉時會重新訂閱，造成一幀閃爍。

#### Step 2：子 Composable 接受 StateFlow，內部 collect

```kotlin
// FocusIndicatorOverlay.kt
@Composable
fun FocusIndicatorOverlay(
    focusUiStateFlow: StateFlow<FocusUiState>,  // ← Flow ref，非展開值
    modifier: Modifier = Modifier,
) {
    val focusUiState by focusUiStateFlow.collectAsStateWithLifecycle()
    // 所有後續讀取 focusUiState.overlayModel / .visualTargetState
    // 產生的 recomposition 只影響此 composable 及其子樹
}
```

**Why collectAsStateWithLifecycle（非 collectAsState）**：lifecycle-aware，App 進入後台時自動取消訂閱，避免在不可見狀態下持續消費 CPU。

#### Step 3：父層 CameraScreen 傳遞 Flow 引用，不展開

```kotlin
// CameraScreen.kt — 正確
FocusIndicatorOverlay(
    focusUiStateFlow = viewModel.focusUiStateFlow,  // ← 傳 Flow 物件
    modifier = Modifier.fillMaxSize(),
)

// CameraScreen.kt — 禁止
val focusUiState = state.focusUiState               // ← 從頂層 state 展開
FocusOverlay(
    overlayModel = focusUiState.overlayModel,        // ← 傳展開值 → 頂層重組
    visualTargetState = focusUiState.visualTargetState,
)
```

---

### ⚠️ Critical Warning：此模式的適用邊界

**何時應用此模式**（高頻 + 獨立）：
- 對焦 UI（每幀更新座標）✅
- Optical Flow 追蹤點（每幀更新）✅
- FPS 計數器（每秒更新）✅

**何時不需要此模式**（低頻 or 已有頂層依賴）：
- Flash mode、HDR mode（使用者操作才變更，頂層讀取 OK）
- isCapturing（拍照瞬間觸發，影響快門按鈕外觀，本來就需要頂層重組）
- Error state（低頻）

**禁止行為**：
- ❌ 為了「看起來整潔」把所有狀態都拆成獨立 StateFlow 並傳下去——過度拆分增加 ViewModel 複雜度且無益
- ❌ 在 `CameraScreen` 頂層 `val focusUiState by viewModel.focusUiStateFlow.collectAsState()` 然後把展開值傳下去——這等於沒做防禦（頂層仍然每幀重組）
- ❌ 將 `SharingStarted.WhileSubscribed(5000)` 用於視覺座標 Flow——5秒 timeout 在畫面旋轉時造成動畫 glitch

---

### 🔍 Code Locations（v2.12.0-19）

| 組件 | 位置 | 作用 |
|------|------|------|
| `focusUiStateFlow` 定義 | `CameraViewModel.kt:106-117` | 孤立 Focus StateFlow |
| 內部 collect | `FocusIndicatorOverlay.kt:92` | `collectAsStateWithLifecycle()` |
| CameraScreen 接線 | `CameraScreen.kt:388-392` | 傳 Flow 引用，不展開 |
| 舊 FocusOverlay（已刪） | 焦土政策刪除（v2.12.0-19 SV） | 零呼叫站點 → 刪除 |

---

### 📋 Backlog

- [x] FocusIndicatorOverlay 接線 — ✅ 完成 (v2.12.0-19, 2026-03-18) | 局部重組防禦已驗證（Build PASS）
- [ ] 評估 Optical Flow 追蹤點是否需要同樣模式（`trackedPoint` / `trackedPointActive`）
- [ ] `currentFps` StateFlow 考慮同樣模式（FPSOverlay 目前從頂層讀取）

---

## Clean Architecture Sensor DI 模式 (v2.12.0-22 Ticket 1.5)

**確立日期**: 2026-03-18
**版本**: v2.12.0-22（Ticket 1.5 — DeviceSensorManager 介面抽取）
**Commit**: `725f04c`

本節記錄「Domain 純介面 + Data 具體實作 + Hilt @Binds/@Provides 雙路徑」的標準模式，適用於所有需要 Android 硬體隔離的 Domain 合約。

---

### ✅ Confirmed：Domain 介面抽取三步驟

#### Step 1：Domain 層定義純介面（零 Android import）

```kotlin
// domain/sensor/DeviceSensorManager.kt
package com.akirachang.ai.photographer.domain.sensor

import kotlinx.coroutines.flow.Flow

interface DeviceSensorManager {
    fun startGyroscopeTracking(): Flow<GyroscopeData>
}
```

**不變量（Invariant）**：
- 介面宣告體內**絕對禁止** `import android.*`
- 唯一允許的 import 是 Kotlin/KotlinX 標準庫（`Flow`、`kotlinx.coroutines.*`）
- 領域模型（如 `GyroscopeData`）獨立拆到同 package 的獨立檔案（`GyroscopeData.kt`）

#### Step 2：Data 層具體實作 implement 介面

```kotlin
// data/sensor/DeviceSensorManagerImpl.kt
class DeviceSensorManagerImpl(context: Context) : DeviceSensorManager {
    // Android SensorManager 實作細節全在此層
    override fun startGyroscopeTracking(): Flow<GyroscopeData> = callbackFlow { ... }

    // 非介面方法（Presentation 層特有需求）保留在 Impl
    fun areSensorsAvailable(): Boolean = ...
    fun startOrientationTracking(): Flow<OrientationData> = ...
    fun hasGyroscope(): Boolean = ...
}
```

**設計決策**：presentation 層特有方法（`areSensorsAvailable`、`startOrientationTracking`）**不放進 domain 介面**，CameraViewModel 注入具體型別即可存取。

#### Step 3：DI 雙路徑 — @Provides 具體型 + @Binds 介面

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    // Domain 介面綁定（供 domain-layer 或 future 組件注入）
    @Binds @Singleton
    abstract fun bindDeviceSensorManager(impl: DeviceSensorManagerImpl): DeviceSensorManager

    companion object {
        // 具體型提供（供 CameraViewModel 等需要完整 API 的 caller 注入）
        @Provides @Singleton
        fun provideDeviceSensorManagerImpl(@ApplicationContext context: Context): DeviceSensorManagerImpl =
            DeviceSensorManagerImpl(context)
    }
}
```

**Hilt 雙路徑規則**：
- `@Provides` 回傳具體型 `DeviceSensorManagerImpl` → Hilt 能注入具體型
- `@Binds` 將具體型映射至介面 `DeviceSensorManager` → Hilt 能注入介面
- **`@Provides` 必須在 `companion object`**（Hilt abstract module 限制）
- **`@Binds` 必須在 abstract class 本體**（不可在 companion object）

---

### ⚠️ Critical Warning：Hilt @Binds 陷阱

**禁止行為**：
- ❌ `@Provides` 和 `@Binds` 都在 companion object 或都在 abstract class 外 → Hilt 編譯錯誤
- ❌ `@Provides` 回傳介面型別、自行 `return impl` → 語意模糊，應改用 `@Binds`
- ❌ 在 domain 介面引入 `android.hardware.*` — 破壞 Clean Architecture 隔離

**CameraViewModel 注入策略**：
- CameraViewModel 注入 **`DeviceSensorManagerImpl`**（具體型），可使用全部 API
- 未來 domain UseCase 注入 **`DeviceSensorManager`**（介面型），只用 `startGyroscopeTracking()`

---

### 🔍 Code Locations（v2.12.0-22 基線）

| 組件 | 位置 | 作用 |
|------|------|------|
| Domain 介面 | `domain/sensor/DeviceSensorManager.kt` | 純合約，零 Android import |
| 領域模型 | `domain/sensor/GyroscopeData.kt` | 獨立 data class，同 package |
| 具體實作 | `data/sensor/DeviceSensorManagerImpl.kt` | 全部 Android 實作細節 |
| DI 綁定 | `di/AppModule.kt` | `@Binds` 介面 + `@Provides` 具體型 |
| ViewModel 注入點 | `CameraViewModel.kt:60` | `DeviceSensorManagerImpl`（具體型） |

---

### 📋 Backlog

- [x] DeviceSensorManager 介面抽取 — ✅ 完成 (v2.12.0-22, 2026-03-18) | Build PASS、SV PASS
- [x] IMU 整合接線（Ticket 2 + Fix 2A）：`ImuCompensator` 注入 `OpticalFlowTracker` ✅ 完成 (v2.12.0-23, 2026-03-18) | Build PASS、SV PASS
- [x] IMU ViewModel 接線（Ticket 3 + Fix 3A-R）：`CameraViewModel` 收集陀螺儀 Flow → `accumulate()`；`OrientationSensorManager` 介面隔離；Presentation 層完全切斷對 `DeviceSensorManagerImpl` 的直接依賴 ✅ 完成 (v2.12.0-24, 2026-03-18) | Build PASS、SV PASS

---

### [v2.12.0] IMU 補償與感測器架構 (Architecture Learnings)

- **介面隔離原則 (Interface Segregation Principle)**：在重構 `DeviceSensorManagerImpl` 時，嚴格區分了 `DeviceSensorManager`（專供 IMU 陀螺儀高頻積分）與 `OrientationSensorManager`（專供裝置方向旋轉）。ViewModel 僅依賴需要的介面，徹底切斷對 Impl 的具體依賴。
- **Hilt 多重綁定單例 (Multi-Binding Singleton)**：在 `AppModule` 中，透過多個 `@Binds` 將不同的 Domain 介面指向同一個 `@Singleton` 的 `DeviceSensorManagerImpl`，既滿足了 Clean Architecture，又避免了重複註冊硬體 Sensor 的效能浪費。
- **IMU 狀態機防禦**：`ImuCompensator.reset()` 必須在 `startTracking()`（防 re-tap spike）、`stopTracking()`/LOST（防垃圾積分污染下次追蹤）、`switchCamera()`（切鏡頭後運動向量失效）三個時機呼叫。
- **焦距預設值**：`focalLengthPx = 2000f` 為暫定常數；後續 Ticket 需從 `CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS` 動態讀取並轉換為像素（`fPx = f_mm / sensorPixelPitch_mm`）。

---

### 🔑 v2.12.0-23 關鍵學習（IMU 積分狀態機守則）

**守則：`startTracking()` 開頭必須呼叫 `imuCompensator.reset()`**

**背景**：`ImuCompensator` 在後台持續累積陀螺儀積分。若追蹤因 LOST 停止後，使用者在短時間內重新點擊（re-tap），此時 `startTracking()` 建立新 Session，但前次 LOST 到 re-tap 之間累積的 IMU 積分尚未被消費。下一幀 `consumeCameraMotion()` 將一次性吐出這段積分，造成 `compensatedDx/compensatedDy` 出現大幅偏移，導致追蹤框瞬移（Coordinate Spike）。

**正確的三點 reset 策略**：

| 時機 | 位置 | 作用 |
|------|------|------|
| 新 Session 啟動（re-tap） | `startTracking()` **第一行** | 清除前次 LOST→tap 間隙的垃圾積分，防 Spike |
| 追蹤正常/異常停止 | `stopTracking()` | 清除追蹤中殘留積分，防下次 startTracking 前繼續累積 |
| 相機切換 | 外部呼叫 `imuCompensator.reset()` | 新鏡頭坐標系不同，舊積分無效 |

**關鍵坐標換算（downsampled space）**：
```
imuDx/imuDy = consumeCameraMotion(focalLengthPx)  // 原始圖像像素空間
compensatedDx = medDx - imuDx * scaleX             // 轉換至 downsampled 空間再相減
compensatedDy = medDy - imuDy * scaleY
```
`medDx/medDy` 是 LK 在 downsampled frame 的位移；`imuDx/imuDy` 是原始解析度空間的位移；必須乘以 `scaleX/scaleY` 才能對齊坐標系後相減。

**L3 Strict Verification 判決**（2026-03-18）：Fix 2A 補上 `startTracking()` 漏缺的 reset，消除狀態機漏洞。

---


---

### 🔑 v2.12.0-26 關鍵學習（幾何管線防禦守則）

**守則 1：無效幾何必須傳播為 null，絕對不可回傳 Identity Matrix**

**背景**：`createSensorToViewMatrix()` 早期版本在 `imageWidth <= 0` 或 `contentBounds.width() <= 0f` 時回傳 `Matrix()`（單位矩陣）。單位矩陣是「外觀無害的偽裝者」——它不會拋出 Exception，但會讓所有座標對應到 (0, 0)，導致對焦框靜默地定位到畫面左上角，**而非觸發可觀察的錯誤**。這種沉默失敗比 Crash 更危險，因為它在 Logcat 中不留任何痕跡。

**正確做法**：
- `createSensorToViewMatrix()` 回傳 `Matrix?`，無效輸入時 `return null`
- `buildNewPipelineGeometryContext()` 回傳 `GeometryContext?`，`effectiveImgW/H <= 0` 時 `return null`
- 呼叫端使用 `?: return` 或 `?: return emptyList()` 明確中斷執行
- `latestSensorToViewMatrix = null` 讓 `PreviewViewGeometryMapper` 自然退回 FIT_CENTER fallback，這是已驗證的安全路徑

**禁止行為**：
- ❌ `return Matrix()` — 偽裝成功，座標靜默歸零
- ❌ `return GeometryContext(imageWidth=0, ...)` — 下游除法仍會產生 Infinity
- ❌ 在 Log.e 之後繼續往下建構 Context — Log 警告無效，仍執行危險運算

---

**守則 2：影像分析管線存活性必須有心跳 Log**

**背景**：在 Sony XQ-BT52 實機測試中，發生了「管線靜默死亡」現象——AF hunting loop 讓 `StandaloneCoroutine was cancelled` 洗板，但 `[v2.8.4 E2E]` 完全消失。純靠 `[v2.8.4 E2E]` 無法區分「管線正常但無人臉」與「管線已死亡」兩種狀態，導致診斷時間大幅延長。

**正確做法**：在 `observePreviewForAnalysis()` 的 `onEach` 最開頭，加入週期性心跳 log：
```kotlin
if (mlkitProcessedFrames % 30L == 0L) {
    Log.i("SmartCameraAnalyzer", "[SP-A Heartbeat] bitmap analysis alive frame=$mlkitProcessedFrames")
}
```
- **心跳間隔**：每 30 幀（約 3–9 秒，依 EWMA throttle 而定）
- **驗收標準**：Sony 實機見到 `frame=90→120→150…` 穩定遞增 = 管線存活
- **心跳消失診斷路徑**：`previewBitmap` flow 未發幀 → 檢查相機權限（重新安裝後權限被清除）→ 或相機 binding 失敗

**此心跳為強制性診斷基礎設施，不可在未來重構中移除。**

---

**守則 3：SP-C In-Flight 鎖定 — 被動 AF 不得自我取消**

**背景**：ML Kit 以 ~3 FPS 更新 DetectionResult，每次更新都觸發 `runNewFocusPipeline()`。舊版 Step G 無條件執行 `focusControllerJob?.cancel()`，導致 Sony HAL 的 AF 請求在完成前就被取消，下一幀又立即觸發新請求，形成 `StandaloneCoroutine was cancelled` 的自我取消無限迴圈。

**正確做法**：
```kotlin
val isJobActive = focusControllerJob?.isActive == true
if (isJobActive && !userIntent.isTapToFocusActive) {
    Log.v(TAG, "[SP-C] Skipping AF request: previous job still in-flight")
    // fall through to Step H for UI update — do NOT cancel active job
} else {
    focusControllerJob?.cancel()
    focusControllerJob = viewModelScope.launch { ... }
}
```
同時，`catch (CancellationException)` 必須在 `catch (Exception)` 之前攔截，`throw e` 重新拋出，確保 coroutine 正確標記為 cancelled 狀態，且不汙染 throttle 狀態機的 `onFailure()` 計數。

**`CancellationException` 黃金原則**（延伸自 v2.12.0-05）：
- Passive AF 被 user tap 中斷 → `CancellationException` → `throw e`，不呼叫 `onFailure()`
- Sony HAL 取消 future → `CancellationException` → 同上
- 真正的硬體錯誤 → `Exception`（非 Cancellation）→ `onFailure()`，顯示錯誤

---

## v2.13.0 智能構圖引擎 — 關鍵防禦策略

### Ticket 1: CompositionGuidelineEngine (Rule of Thirds)

**守則 1：純函數引擎設計 (Pure Function Architecture)**

`CompositionGuidelineEngine` 徹底抽離 Android 視圖層，僅依賴數學運算與 `android.graphics.RectF`，達成 100% 可測試性與無副作用 (Stateless)。

**設計原則**：
- 無 `Context`、無 `ViewModel`、無 `LiveData`/`StateFlow` 依賴
- 所有輸入透過參數傳遞，所有輸出透過回傳值，無 side-effect
- 可直接在 JUnit 單元測試中實例化，無需 `@RunWith(AndroidJUnit4::class)`
- Domain 層演算法修改時，不需觸碰任何 UI（`CameraScreen.kt`、`FocusIndicator`）或 ViewModel

**禁止行為**：
- ❌ 在 Engine 內部讀取 `View` 尺寸或任何 Android Context
- ❌ 在 Engine 內部 emit Flow 或修改 StateFlow
- ❌ 在 Engine 內部呼叫 `Log.e` 以外的任何 UI 操作

---

**守則 2：無效幾何提早退場 (Invalid Geometry Early-Return)**

針對 `viewportWidth <= 0f`、`viewportHeight <= 0f` 或 `targetRect` 面積為零的異常狀況，實作嚴格的 Guard Clause，防止除以零或靜默失敗。

```kotlin
if (viewportWidth <= 0f || viewportHeight <= 0f) {
    Log.w(TAG, "[CompositionEngine] Invalid viewport: ${viewportWidth}x${viewportHeight}")
    return CompositionAdvice.None
}
if (targetRect.isEmpty) {
    Log.w(TAG, "[CompositionEngine] Invalid targetRect: $targetRect")
    return CompositionAdvice.None
}
```

**回傳 `CompositionAdvice.None` 而非拋出 Exception**：保持呼叫端簡潔（`when` 分支直接處理），且不中斷相機管線。

---

**守則 3：動態容忍度策略 (Tolerance-by-Max-Dimension)**

放棄固定像素閾值，改採 `max(viewportW, viewportH)` 作為 `ALIGNMENT_TOLERANCE` 的計算基準。

```kotlin
val tolerancePx = max(viewportWidth, viewportHeight) * ALIGNMENT_TOLERANCE
```

**原因**：
- 固定像素閾值（如 50px）在高解析度螢幕上過嚴，在低解析度上過寬
- 橫拍（寬 > 高）與直拍（高 > 寬）模式切換時，判定標準自動按比例縮放
- `ALIGNMENT_TOLERANCE = 0.05f`（5%）在 1080p 螢幕上對應約 108px，在 480p 上對應約 48px——符合視覺感知比例

---

**守則 4：軸向死區防護 (Axial Deadzone Guard)**

引入 2px 的軸向防抖閾值（`DEADZONE_PX = 2f`），徹底根絕「0.1px 微小偏移」所導致的高頻率方向提示閃爍 (Ping-pong Effect)。

```kotlin
val adjustX = if (abs(deltaX) > DEADZONE_PX) deltaX else 0f
val adjustY = if (abs(deltaY) > DEADZONE_PX) deltaY else 0f
```

**典型場景**：目標中心點在黃金交叉點正上方 1.5px（手持抖動），無死區時每幀交替輸出「向左」「向右」，造成 UI ping-pong；加入死區後鎖定為 `Aligned`，直到偏移超過 2px 才觸發 `Adjust`。

**與 v2.12.0 的關聯**：此守則與 v2.12.0-11 的 Hysteresis 2.5x（`maxOf` grow-only frame）同源，均為防止視覺跳動的「死區/遲滯」設計模式，適用於任何需要穩定輸出的測量場景。

---

### 🔑 v2.12.0-27 關鍵學習（Tap Intent TTL 主動清除 — SP-B 雙保險設計）

**背景**：使用者點擊畫面時，`setTapToFocusIntent()` 立刻發出樂觀 UI（72dp 黃色佔位框）並設定 `pendingTapToFocusIntent`。若該位置無任何可追蹤特徵（空白牆壁、純色背景），ML Kit 不會偵測到任何目標，`clearTapToFocusIntent()` 也不會被呼叫，導致黃框永久凍結在畫面上，無法自動清除。

---

**守則 1：Tap Intent 必須同時具備被動 TTL 與主動 TTL 兩道防線**

舊版設計只有「被動 TTL」：`currentTapToFocusIntent()` 在每一幀被呼叫時檢查是否超過 750ms。但如果管線沒有新幀進來（場景靜止、ML Kit 不觸發），被動 TTL 永遠不被執行。

**正確做法（雙保險）**：

```kotlin
// 第一道：被動 TTL（既有）— 每幀讀取時檢查
private fun currentTapToFocusIntent(nowMs: Long = ...): TapToFocusIntent? {
    val intent = pendingTapToFocusIntent ?: return null
    if ((nowMs - intent.timestampMs) > TAP_INTENT_WINDOW_MS) {
        pendingTapToFocusIntent = null
        return null
    }
    return intent
}

// 第二道：主動 TTL（SP-B 新增）— 計時器主動清除
private var tapClearJob: Job? = null

private fun setTapToFocusIntent(...) {
    pendingTapToFocusIntent = TapToFocusIntent(...)
    // ... 發出樂觀 UI ...

    tapClearJob?.cancel()
    tapClearJob = viewModelScope.launch {
        delay(TAP_INTENT_WINDOW_MS)
        if (pendingTapToFocusIntent != null) {
            pendingTapToFocusIntent = null
            val isStillPlaceholder = _state.value.focusUiState.overlayModel?.primaryStableId == "tap_optimistic"
            if (isStillPlaceholder) {
                Log.v(TAG, "[SP-B] Tap intent TTL expired — clearing optimistic Yellow box.")
                _state.update { it.copy(focusUiState = FocusUiState.EMPTY) }
            } else {
                Log.v(TAG, "[SP-B] Tap intent TTL expired — real tracking active, placeholder already replaced.")
            }
        }
    }
}
```

**禁止行為**：
- ❌ 只依賴被動 TTL：場景靜止時 ML Kit 不發幀，被動 TTL 永不執行 → 黃框永久殘留
- ❌ `tapClearJob` 不取消舊計時器就啟動新計時器：快速連點導致舊計時器誤刪新的點擊佔位框

---

**守則 2：主動 TTL 清除前必須以 `primaryStableId` 確認 UI 狀態，禁止盲目覆蓋**

**核心問題**：`runNewFocusPipeline()` 消費 tap intent 後，只更新 `focusUiState`（發出 Cyan），但**不呼叫** `clearTapToFocusIntent()`，因此 `pendingTapToFocusIntent` 在真實追蹤接管後依然非 null。若 `tapClearJob` 在 750ms 後盲目執行 `FocusUiState.EMPTY`，將覆蓋已經顯示中的 Cyan 追蹤框。

**正確做法（primaryStableId 互斥判斷）**：

| `primaryStableId` 值 | 含義 | tapClearJob 應執行動作 |
|---|---|---|
| `"tap_optimistic"` | 仍在顯示樂觀佔位框，無真實追蹤 | ✅ 清空，emit `FocusUiState.EMPTY` |
| 其他值（stableId, face_xxx）| 真實追蹤已接管 | ⛔ 跳過，不動 UI state |
| `null`（overlayModel = null）| 狀態已是 None | ⛔ 跳過，已無需清除 |

樂觀 UI 的 `primaryStableId = "tap_optimistic"` 是設計上刻意標記的哨兵值，讓下游可以安全區分「佔位符」與「真實追蹤目標」。

**`clearTapToFocusIntent()` 的責任邊界**：
- 必須同時 cancel `tapClearJob`，確保計時器在 `resetFocusState()` / `lockAEAF()` 時被解除
- `pendingTapToFocusIntent = null` 只是清除 ViewModel 內部意圖，不直接影響 `focusUiState`
- UI 的清除責任由 `tapClearJob`（主動）或 `FocusStateMachine`（正常流程）負責

```kotlin
private fun clearTapToFocusIntent() {
    tapClearJob?.cancel()  // SP-B: Disarm TTL timer when intent is consumed or reset
    tapClearJob = null
    pendingTapToFocusIntent = null
}
```

---

**驗收標準（The Blank Wall Test）**：
1. 對空白牆壁點擊 → 黃框出現
2. 靜止等待 ~750ms → 黃框**自動消失**（SP-B 主動清除）
3. 對有人臉的場景點擊 → 黃框出現 → ~100ms 後轉為 Cyan → **Cyan 保持不消失**（primaryStableId guard 生效）

---

## Infrastructure / Build

### 📋 Backlog

- [ ] [Infrastructure] Kapt to KSP migration (保留 K2 編譯器相容性，推遲至 MVP D6 階段後再執行，目前維持 1.9 降級模式以避免依賴重構的範圍蔓延)

---

## 🏛️ 架構決策紀錄 (ADR) - D0 絞殺者手術 (v2.13.0-base)

**決策日期**: 2026-04-07 ~ 2026-04-09 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`

* **Tier-aware 架構確立**：導入 `DeviceCapabilityTier` (FULL/LIMITED/LEGACY) 取代原有的 4 級效能降級，並嚴格透過 DI 注入綁定硬體能力，杜絕孤島代碼。
* **色彩契約強制對齊**：UI 狀態顏色必須 100% 遵循 SSOT v3.3 (Focusing=Amber `#D4A030`, Focused=Green `#50C864`)。代碼必須向規格低頭，嚴禁為了遷就舊有邏輯 (Cyan) 而妥協。
* **RenderScript 完全移除**：`RenderScriptYuvConverter.kt` 刪除，`LiveCameraSource.kt` 移除所有 RS 呼叫點，YUV→RGBA 轉換改由 ML Kit 原生處理。
* **OpticalFlowTracker 焦土**：`OpticalFlowTracker` 及所有相關 DI 參數全數移除，`ImuCompensator` 改為獨立注入，不再依賴 OF 管線。
* **consumeCameraMotion 接線原則**：`ImuCompensator.consumeCameraMotion()` 必須在 per-frame analysis pipeline (`observePreviewForAnalysis`) 中呼叫，而非在 `startImuCompensation()` 的 accumulate flow 中消費（accumulate 與 consume 必須分離）。

---

## 🏛️ 架構決策紀錄 (ADR) - D0.x 階段驗收防禦機制

**決策日期**: 2026-04-09 | **觸發**: SV Trust-but-verify 覆測 + Traycer Path B 判定 | **Branch**: `feature/cueshot-mvp`

### 1. Tier-aware 設備相容性補償
- **決策**：建立 `DeviceCapabilityTier` (FULL/LIMITED/LEGACY) 必須徹底實作接線，嚴禁宣告孤島 (Orphan Scaffold)。任何硬體能力的降級或特例處理，必須透過 DI 注入的 Tier 進行路由，而非在 UI 層 hardcode 判斷。
- **風險**：若 Tier 僅宣告未消費（log-only），等同於假性降級保護，裝置相容性崩潰時 Debug 難度極高（無路由可追蹤）。
- **驗收標準**：grep `DeviceCapabilityTier` 必須出現在 DI 注入點與下游消費端（非僅 `when(tier)` log 輸出）。

### 2. Locked 色彩路徑一致性
- **決策**：UI 狀態顏色必須 100% 絕對服從 SSOT (v3.3) 規範。當發現實作代碼（如 `Cyan`）與規格書（如 `Amber #D4A030` / `Green #50C864`）衝突時，**代碼必須向規格低頭**。嚴禁為了遷就舊有邏輯而創造未經授權的 Color Mapping 避震器。
- **風險**：色彩路徑若在多層（`OverlayColor` enum → `drawLockedFrame()` → `animatedColor`）中各自獨立映射，任一層漏接會導致 Locked 狀態顏色靜默失效，QA 視覺測試難以捕捉。
- **驗收標準**：`OverlayColor.FOCUSED`（或等價 enum）的色彩值必須端到端追蹤至 Compose render 層，每一個中間轉換點均需明確傳遞，不得有 hardcode fallback。

### 3. SV 反向掃描防禦教訓
- Traycer Path B 判定（可驗證事實不一致）的根源：SV 工具描述功能而非驗證功能，導致「看起來完整」但實際未接線。
- 後續所有 Scaffold 類 Ticket（建立介面/sealed class/Tier enum）在 SV 時必須加做**反向掃描**：確認新建物件在至少一個下游消費點被真實呼叫，而非僅存在於宣告端。

---

## 🏛️ 架構決策紀錄 (ADR) - D1 骨架與狀態機

**決策日期**: 2026-04-09 | **Traycer 判定**: Path A (放行，補修後) | **Branch**: `feature/cueshot-mvp`

### 1. PTF 競態防護 (Session Token)
- **決策**：狀態機的防呆不可依賴 Data Class 的相等性（`Focused(x,y) == Focused(x,y)` 無法區分新舊請求）。必須強制使用 Session Token（`currentPtfSessionId: Long`）作為唯一的跨請求隔離與覆蓋防護機制。
- **風險**：若無 Token，快速連點同一座標時，舊 coroutine 的 2 秒 delay 會誤判新 Focused 狀態為「自己設置的」並提早 reset 至 Idle，造成視覺閃爍與狀態機失控。
- **驗收標準**：`onTapToFocus()` 必須在 coroutine 外遞增 `currentPtfSessionId`，並在 Defence A（結果回傳後）與 Defence B（delay 後）兩點各做一次 token 比對，不符則 `return@launch`。

### 2. Hilt 集合綁定規範
- **決策**：空集合的 `@Multibinds` 必須宣告在 `abstract class` 中；Dagger/Hilt 不支援在 `object` 中使用此標註。
- **風險**：誤用 `object` 會導致 Hilt kapt 編譯期 `MissingBinding` 錯誤，且錯誤訊息通常指向消費端而非宣告端，難以追蹤根源。
- **驗收標準**：`CompositionModule` 必須為 `abstract class`，`@Multibinds abstract fun` 無方法體。

### 3. Flow 狀態機過渡期靜默化原則
- **決策**：在靜默舊有邏輯時，保留 Flow 生命週期結構（`.sample().filterNotNull()...`），僅替換 `.onEach {}` 的內容，以最小化重構對訂閱鏈的破壞。
- **風險**：若連同 Flow 訂閱結構一起替換，可能引入 coroutine scope leak、訂閱時序改變等隱性問題，且難以在 D1 階段完整驗證。
- **驗收標準**：`observeCompositionAdvice()` 的 `.catch { }` + `.launchIn(viewModelScope)` 保持原樣；只有 `.onEach {}` 內容改為強制 `CompositionAdvice.None`。


---

## 🏛️ 架構決策紀錄 (ADR) - 領域規則共通契約 (Domain Rule Contract)

**決策日期**: 2026-04-10 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D2-T1 + SV-D2-T1-FIX-01 ~ FIX-03

1. **強制正規化 (Forced Normalization)**：所有 `CompositionRule` 的輸入座標必須嚴格遵守 `[0.0, 1.0]` 的正規化空間。禁止依賴 Viewport 或 Pixel 進行計算。
2. **執行期防呆 (Runtime Fail-safe)**：註解不是防禦。每個 Rule 內部必須在第一行實作邊界檢查（out-of-range 負值或 >1、malformed rect），遇到異常強制靜默降級回傳 `RuleResult(0f, false)`。
3. **負向測試覆蓋 (Negative Test Coverage)**：所有的 RuleTest 都必須強制包含 Pixel 誤入、越界座標、非法矩形的負向測試，確保防呆機制永不回歸。
* **驗證紀錄 (D2-T2 HeadroomRule)**：共通契約模板（強制正規化 + faces.isEmpty() 防禦 + 越界防呆 + 負向測試）已在 HeadroomRule 成功落地並通過嚴格驗收。此模板正式成為後續所有新 Rule (如 EyeLine, Gaze) 的強制性起手式，絕不允許退回無防呆狀態。

## 🏛️ 架構決策紀錄 (ADR) - 依賴反轉與開閉原則 (D2-T3 Set Multibinding)

**決策日期**: 2026-04-10 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D2-T3

* **依賴反轉與開閉原則 (D2-T3 Set Multibinding)**：主引擎 (`AnalyzeCompositionUseCase`) 對具體規則的依賴，必須絕對透過 Hilt 的 `Set<CompositionRule>` 注入。新增規則時，僅允許使用 `@Binds @IntoSet` 追加綁定，**絕對禁止修改主引擎代碼**。此模式確立了 Rule Engine 的 Open-Closed Principle (OCP)，是後續擴充的唯一合法途徑。

## 🏛️ 架構決策紀錄 (ADR) - 引擎單幀聚合契約 (D2-T4 CompositionResult)

**決策日期**: 2026-04-10 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D2-T4 + SV-D2-T4-FIX-01

* **引擎單幀聚合契約 (D2-T4 CompositionResult)**：
  - `CompositionResult` 取代了舊有的單一分數，成為引擎輸出的唯一 SSOT。它必須包含 `topInstruction` (引導用)、`ruleScores` (評分用) 以及 `isAllAligned` (呼吸觸發用)。
  - `isAllAligned` 必須是動態判定 (`rules.all { ... }`)，絕對禁止硬編碼數量。
  - **端對端接線策略**：ViewModel 必須直接消費 `AnalyzeCompositionUseCase` 的輸出，絕對禁止在 ViewModel 層實作防抖或平滑化（Debounce 必須在未來的專用層實作），維持引擎輸出的單幀純潔性。

## 📌 技術債登記 - D3 座標映射重建

**登記日期**: 2026-04-10 | **預計處理**: D3 | **優先級**: 中

* **[Coordinate Mapping] 移除 `CameraViewModel` 中的 `previewWidth ?: 800` 墊檔值**：D2 被嚴格定義為純正規化 (0.0~1.0) 數學引擎，所有 Pixel 座標轉換排除在外。此魔術數字推遲至 D3 建立真實預覽座標映射時，改由 `CameraCharacteristics` 驅動，消除 `ImuCompensator` 焦距估算的硬編碼依賴。

## 📌 技術債登記 - D3-T1 全域色碼收斂

**登記日期**: 2026-04-10 | **預計處理**: D5 UI 精修 | **優先級**: 低

* **[UI SSOT] 全域色碼收斂**：清除專案內殘留的舊色碼 (`0xFF4CAF50`, `0xFFFFA726`)，統一替換為 SSOT v3.3 規格的 `#50C864` (Green，對齊完成) 與 `#D4A030` (Amber，引導中)。D3-T1 已修正 `LevelIndicator.kt`，但以下檔案仍殘留舊色碼，須在專屬 Ticket 中一次性收斂，**禁止逐票分散修改**（避免反覆觸發高風險檔案 pre-commit hook）：
  - `CompositionGuideOverlay.kt`（line 76）
  - `CompositionHint.kt`（line 61）
  - `DetectionOverlay.kt`（line 104）
  - `FPSOverlay.kt`（line 57）
  - `CameraScreen.kt`（line 451，HDR 狀態色彩，需確認是否同屬 SSOT 管轄）

## 🏛️ 架構決策紀錄 (ADR) - 里程碑延期管理準則 (Milestone Deferral Policy)

**決策日期**: 2026-04-10 | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D2-T5.1

* **受控延期 (Controlled Deferral)**：里程碑允許以 Path A (Approve) 狀態關閉，但其中任何標記為 `Deferred` (推遲) 的任務，**必須同時具備明確的下游承接票號、驗收門檻，並且在所有文件區塊 (如 Sprint Health, Debt/Backlog, Milestone 結論) 中維持敘述一致**。嚴禁產生「宣稱零技術債」與「存在 Deferred 項目」的語意衝突。

## 🏛️ 架構決策紀錄 (ADR) - D3 UI 前置焦土清理原則 (Scorched Earth Policy)

**決策日期**: 2026-04-10 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D3-T0 (KAIROS #1 預防性發現承接)

* **UI 前置焦土清理原則 (Scorched Earth Policy)**：在進行跨里程碑的 UI 或架構翻新前，必須強制清除上一代殘留的無消費者 Flow (如 `_trackingBoxFlow`) 與過期註解 (如舊的 debounce / throttle 路徑描述)。絕不允許新舊管線並存，以徹底根絕雙軌邏輯 (Split-brain) 與幽靈訂閱造成的 UI 渲染異常。
  - **根因**：D2 拆除 `sample(300L)` 後，`_trackingBoxFlow` 失去唯一消費者，但 `onTrackingBoxUpdated()` 函數與 KDoc 仍保留，形成可被誤用的殭屍接口。KAIROS #1 掃描發現後，D3-T0 於 D3 開工前強制清除。
  - **強制時機**：每個 Sprint 節點（Dx 開工前），必須執行一次「無消費者 Flow」grep 掃描。若命中，開工第一票必為焦土清理票，優先於任何功能實作。

## 🏛️ 架構決策紀錄 (ADR) - D3-T1 UI 狀態鏡像與一元化收斂準則

**決策日期**: 2026-04-11 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D3-T1 (水平儀收斂)

* **UI 狀態鏡像與單一路徑收斂 (State Mirroring & Unification Convergence)**：在過渡期（如 D3），為避免破壞既有 UI，允許底層 Sensor 狀態產生「鏡像」（例如同時存在獨立的 `orientationState` Flow 與聚合在 `CameraState.deviceRoll` 內的狀態）。但此為暫時性技術債，**必須在 D5 (Rule Engine 視覺化階段) 強制將所有 UI 消費端收斂至單一來源 `CameraState`**，徹底消滅雙來源語義漂移的風險。

## 🏛️ 架構決策紀錄 (ADR) - D3-T2 視覺層級與渲染防禦 (Grid & Z-Order)

**決策日期**: 2026-04-11 | **Traycer 判定**: Path A (放行) | **Branch**: `feature/cueshot-mvp`
**觸發票據**: D3-T2 (三分法網格 Overlay)

* **視覺層級與渲染防禦 (D3-T2 Grid & Z-Order)**：
  1. **先防呆再渲染**：所有 Overlay 組件必須偵測 Viewport 狀態。若 `contentBounds.isEmpty` 或寬高為 0，必須強制靜默不繪製，防止邊界溢位或無效繪製。
  2. **Z-Order 堆疊契約**：Camera Screen 的圖層順序必須固定為：Preview -> Grid (背景線) -> Guideline/Hint (動態光影) -> Indicators (互動元件)。此順序確保視覺引導能正確覆蓋背景網格。
  3. **焦土清理預告**：`CameraScreen.kt` 中 `GuidelineOverlay` dead import（line 106）為已知技術債，須在下一個焦土 Ticket 中清除。
