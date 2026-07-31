# P08 Codebase Sanitization Contract

**文件編號**: CONTRACT-P08-20260601
**簽發**: CSO (Claude Opus 4.6, claude.ai)
**日期**: 2026-06-01
**基準文件**: SSOT v4.0.5 · Layout Spec v2.5.1 · P05 Contract (Frozen) · Pipeline v1.6 · Unified Ticket Template v3
**適用範圍**: Phase 0.8 代碼清場整備 — 三波次推進（Eliminate → Foundation → Legacy → Visual Baseline Gate）

---

## §0 Executive Summary

Phase 0.5 實機驗測 + P05 修復過程揭露：代碼庫中存在 SSOT 否定的 orphan 渲染元件、ML Kit 管線架構缺陷、以及跨多個 Overlay 的基礎常數偏差。P05 的 Strangler Fig 策略成功建立了 Tap-to-Focus 系統，但 scope 限制導致三類結構性問題無法在其框架內解決。

**戰略決策**：停止 P05 修補作業，啟動 Phase 0.8 三波次清場：
1. **Wave E (Eliminate)**：先消滅 SSOT 否定的 orphan code → 縮小代碼面積 → 降低後續票的碰撞半徑
2. **Wave F (Foundation)**：修正地基常數與架構缺陷 → 所有 UI 元素在正確基底上運行
3. **Wave L (Legacy)**：清理 P05 殘餘驗收 + 缺口填補 → 完成 Photo Mode 全部修復
4. **Gate**：Phase 0.5 Visual Baseline — 八場景實機截圖，作為 Phase 0.8 Done 條件

**P05 Contract 處置**：P05 Contract 狀態變更為 **Frozen** — 不再接受新票。已完成票（T1/T2/T5/T6）保留。未完成票遷移至 P08 對應波次。

---

## §1 CSO 戰略裁決（不可修改）

| 決策 | 裁決 | 理由 |
|------|------|------|
| P05 繼續修補 | 🔴 停止 | scope 溢出 — 問題數量已超過 P05 框架設計容量 |
| Phase 0.8 波次順序 | **E → F → L → Gate** | 消滅先行 = 縮小面積 → 地基修復 diff 更乾淨 → Legacy 在穩固基底上閉環 |
| CameraViewModel 修改 | ⚠️ **F-3 限定開放** | 僅限 ML Kit 管線合一 + bbox 源頭過濾。禁止新增無關函數。F-3 是唯一 Traycer execute_review 票 |
| PitchTractionLayer.kt | 🔴 **徹底清場** | SSOT §2.2：「不存在任何名為 Edge Warmth 的渲染元件」+ PitchTractionConstants 無 DOT_SPECS = orphan |
| Traycer 配額 | **F-3 唯一** | Phase 0.8 最高風險節點（碰 CameraViewModel ML Kit 管線核心）。其餘票 /qa + /sv + cross_review 覆蓋 |
| 一票一功能 | ✅ 繼承 P05 | 每票只改一個 concern |

---

## §2 P05 遷移對照表

| P05 項目 | 原狀態 | P08 去向 | 備註 |
|----------|--------|---------|------|
| T1 Eye-AF 視覺關閉 | ✅ Done | — | 不動 |
| T2 TapFocusRing 新建 | ✅ Done | — | 不動 |
| T3 TapFocusController | Code Done, 待 Traycer | → L-2 | 批次 strict_verification |
| T4 Scrim TopBar 漸層 | Pending | → L-3 | SSOT 規格有、代碼缺 |
| T5 快門白色 | ✅ Done | — | 不動 |
| T6 場景判斷移除 | ✅ Done | — | 不動 |
| T7 Level 退場 | Pending (Addendum) | → F-1 | 常數修正部分（LEVEL_FADE_RANGE_DEG） |
| R1 GazeAF | QA PASS, CR blocked | → L-1 | Option A → R2 形式閉環 |
| C2 PitchTraction | 未開票 | → E-1 | drawFloatingDots 消滅 |

---

## §3 Wave E — 消滅不該有功能

### P08-E1: PitchTractionLayer 清場（drawFloatingDots + Edge Warmth gradient 徹底消滅）

**Mode**: normal    **Effort**: Medium
**前置依賴**: 無

#### Verify Config
```
TICKET_ID=P08-E1
CREATED=2026-06-01
PRESET=refactor
ALLOWED=PitchTractionLayer.kt,CameraScreen.kt,AmbientConstants.kt
FORBIDDEN=CameraViewModel.kt,HaloGuidanceLayer.kt,TapFocusController.kt
MAX_LINES=200
DIFF_REF=[執行時取 HEAD]
```

#### Context

SSOT v4.0.5 §2.2 白紙黑字：「三通道均為 IMU 或 ML Kit 驅動，**不存在任何名為 Edge Warmth 的渲染元件**」。SSOT §2.3 PitchTraction 規格定義為「右側垂直指示柱：中軸線 + 定位環 + 浮動圓點」（即 `PitchTractionConstants` 中的 COLUMN/AXIS/RING/DOT 常數），不是邊緣漸層。

當前 `PitchTractionLayer.kt` 實作了完全不同的視覺系統：
- `drawPitchTractionGradient()` — Edge Warmth 邊緣漸層（amber/radial vignette）
- `drawFloatingDots()` + `DOT_SPECS` — 5 顆浮動光點（SSOT `PitchTractionConstants` 無此定義）
- `drawAlignedBreathingGradient()` — 四邊綠色呼吸漸層

三者皆為 SSOT 否定的 orphan rendering。

#### Action Items

##### AI-1: 消滅 drawFloatingDots 及所有依賴
- **檔案**: `PitchTractionLayer.kt`
- **消滅範圍**: `DotSpec` data class、`DOT_SPECS` list、`drawFloatingDots()` 函數、`TAG_PITCH_TRACTION_DOTS_ENABLED` 常數、`dotAlphas` 動畫邏輯、`dotPaint` remember block、所有 dot 相關 import
- **目標值**: 上述元素全部刪除，零殘留
- [ ] 完成

##### AI-2: 消滅 drawPitchTractionGradient（Edge Warmth gradient）
- **檔案**: `PitchTractionLayer.kt`
- **消滅範圍**: `drawPitchTractionGradient()` 函數、`EDGE_BAND_FRACTION` 常數、`WarmAmber` color（如僅用於此函數）、`TractionDirection` enum（如僅用於梯度方向）、`ROLL_THRESHOLD_DEG`（如僅用於梯度觸發）
- **目標值**: 上述元素全部刪除，零殘留
- **注意**: 確認 `WarmAmber` 和 `TractionDirection` 是否有其他消費者再決定是否刪除
- [ ] 完成

##### AI-3: 消滅 drawAlignedBreathingGradient
- **檔案**: `PitchTractionLayer.kt`
- **消滅範圍**: `drawAlignedBreathingGradient()` 函數、`AlignGreen` color（如僅用於此函數）、`BREATHING_ANIM_SPEC`（如僅用於此函數）
- **目標值**: 上述元素全部刪除，零殘留
- [ ] 完成

##### AI-4: CameraScreen 調用點清理
- **檔案**: `CameraScreen.kt`
- **現值**: `PitchTractionLayer(...)` Composable 調用
- **目標值**: 移除調用。如果 `PitchTractionLayer.kt` 清場後為空殼（僅剩 package + import），整個檔案刪除
- [ ] 完成

##### AI-5: 測試檔案同步清理
- **範圍**: 搜尋 `PitchTractionLayerTest` 或引用 `TAG_PITCH_TRACTION_*` 的測試檔案
- **目標值**: 移除所有測試該 orphan 行為的 test case。保留不相關的測試
- [ ] 完成

#### Hard Constraints
1. 禁止修改 SSOT §2.3 定義的 `PitchTractionConstants`（那是正確的指示柱規格，未來 W2 實作用）
2. 禁止新增任何渲染邏輯 — 本票純刪除，零新增
3. 若 `PitchTractionLayer.kt` 清場後仍有非 orphan 的 Composable 存活（可能性低），保留檔案但重命名為精確用途名稱

#### Error Handling
- 發現 `WarmAmber` / `TractionDirection` / `BREATHING_ANIM_SPEC` 被 PitchTractionLayer 以外的檔案引用 → 停止回報（需 CSO 裁決依賴關係）
- `assembleDebug` 失敗且錯誤指向刪除的 symbol → 列出所有引用點回報

#### Acceptance Criteria
- [ ] `PitchTractionLayer.kt` 不存在 或 不含任何 `drawFloatingDots` / `drawPitchTractionGradient` / `drawAlignedBreathingGradient` 函數
- [ ] `CameraScreen.kt` 不含 `PitchTractionLayer` 調用
- [ ] grep 全 codebase 無 `drawFloatingDots` / `EDGE_BAND_FRACTION` / `DOT_SPECS` 殘留引用
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案 + 測試檔案

---

## §4 Wave F — 修正地基

### P08-F1: LEVEL_FADE_RANGE_DEG 常數修正

**Mode**: normal    **Effort**: Low
**前置依賴**: P08-E1（消滅波完成後再修地基）

#### Verify Config
```
TICKET_ID=P08-F1
CREATED=2026-06-01
PRESET=hotfix
ALLOWED=AmbientConstants.kt
FORBIDDEN=CameraViewModel.kt,LevelIndicator.kt
MAX_LINES=10
DIFF_REF=[執行時取 HEAD]
```

#### Action Items

##### AI-1: 修正 LEVEL_FADE_RANGE_DEG
- **檔案**: `AmbientConstants.kt`
- **現值**: `const val LEVEL_FADE_RANGE_DEG = 5f`
- **目標值**: `const val LEVEL_FADE_RANGE_DEG = 10f  // SSOT §2.0.1 D-10: N=10°`
- [ ] 完成

#### Hard Constraints
1. 僅修改此一行。AmbientConstants.kt 其他常數禁止觸碰

#### Acceptance Criteria
- [ ] `AmbientConstants.kt` 中 `LEVEL_FADE_RANGE_DEG = 10f`
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 AmbientConstants.kt

---

### P08-F2: GridOverlay 三因子修復

**Mode**: normal    **Effort**: Medium
**前置依賴**: P08-E1

#### Verify Config
```
TICKET_ID=P08-F2
CREATED=2026-06-01
PRESET=bugfix
ALLOWED=GridOverlay.kt,CameraScreen.kt
FORBIDDEN=CameraViewModel.kt
MAX_LINES=80
DIFF_REF=[執行時取 HEAD]
```

#### Context
Session N 根因分析確認三因子：alpha=0.08f（太低）、strokeWidth=0.5f（太細）、Z-order（被其他 Overlay 遮蓋）。CSO 待裁決具體數值 — Claude Code 執行前需提交 Discovery Report。

#### Action Items

##### AI-1: Discovery — 讀取 GridOverlay.kt 現值
- 回報：current alpha / strokeWidth / Z-order position in CameraScreen.kt
- [ ] 完成

##### AI-2: 修正 alpha + strokeWidth
- **檔案**: `GridOverlay.kt`
- **目標值**: alpha ≥ 0.20f、strokeWidth ≥ 1.0dp（精確數值由 Discovery 後 CSO 裁決）
- [ ] 完成

##### AI-3: Z-order 調整（如需要）
- **檔案**: `CameraScreen.kt`
- **目標值**: GridOverlay 渲染順序在所有 content overlay 之上（僅低於 Focus ring + 控制項）
- [ ] 完成

#### Acceptance Criteria
- [ ] GridOverlay 在 Preview 上目視可見
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

---

### P08-F3: ML Kit 管線合一 + Ghost Face 源頭過濾

**Mode**: uc    **Effort**: High
**前置依賴**: P08-E1 + P08-F1 + P08-F2（在最乾淨的代碼基上執行）

#### Verify Config
```
TICKET_ID=P08-F3
CREATED=2026-06-01
PRESET=refactor
ALLOWED=CameraViewModel.kt,CameraScreen.kt,HaloGuidanceLayer.kt
FORBIDDEN=TapFocusController.kt,EyeAfController.kt,LiveCameraSource.kt
MAX_LINES=300
DIFF_REF=[執行時取 HEAD]
```

#### Context — Ghost Face 壁癌根因

ML Kit 偶發回報 bbox 越界的 ghost face（`box.left < 0` 或 `box.right > imageWidth`）。當前代碼有三條消費路徑，P05-R1 僅修補路徑 A（AF 觸發），路徑 B（渲染）和路徑 C（PTF）完全裸奔：

| 路徑 | 消費者 | P05-R1 防護 | 結果 |
|------|--------|------------|------|
| A: AF 觸發 | EyeAfController | ✅ bbox filter + gazeScore | 已修復 |
| B: 渲染 | observeCompositionAdvice → detectedFaces → HaloGuidanceLayer | 🔴 無 | 琥珀圓環亂竄 |
| C: PTF | observePtfAlignment → PtfRuleAggregator | 🔴 無 | EMA 平滑器汙染 |

**放大器**：`observeDetectionResults()`（line ~1632）在 init 中已註解掉，但需驗證 `cameraRepository.detectionResults` Flow 是否有其他消費者。若確認單管線，仍需在唯一寫入點加源頭過濾。

#### Action Items

##### AI-1: Discovery — 驗證管線狀態
- 搜尋全 codebase 中所有 `cameraRepository.detectionResults` 的 collect/onEach/launchIn 調用
- 搜尋全 codebase 中所有 `_state.update { it.copy(detectionResult =` 的寫入點
- 回報：實際活躍的 detectionResult 寫入管線數量 + 各自的行號
- [x] 完成（DRA Protocol: Discovery only, 不修改）

#### CSO 裁決 — F-3 AI-1 三維交叉驗證（2026-06-04）

##### 裁決 1 — F-2 安全性：正式結案

center-crop 語意擔憂為 false alarm：

- `GridOverlay` 的 `contentBounds = null` 是 composable 參數，不觸碰 `state.contentBounds`
- `HaloGuidanceLayer` 用獨立的 `NormalizedToPixelTransformer`，與 `contentBounds` 零交集
- Halo 座標系對 ScaleType 變更天然免疫

F-2 五項修改清單（alpha + strokeWidth + KDoc + contentBounds null + Z-order）正式完整閉環，無需追加防護。

##### 裁決 2 — F-3 範疇：維持 Contract 原始 scope，不擴充

`calculateContentBounds` 修正**不進入 F-3**。理由：

1. `PreviewViewportContainer.kt` 不在 F-3 ALLOWED 中
2. F-3 已是 Effort: High / MAX_LINES=300 — Phase 0.8 風險最高的單票
3. `contentBounds` 影響 TapFocus 精度，非 Ghost Face 或 Halo — 不同 concern 不塞同票
4. 修 `calculateContentBounds` 需同時驗收 TapFocus 命中精度（Sony 邊緣），增加驗收面積

##### 裁決 3 — contentBounds 殘餘 Bug 處置：歸入 Visual Baseline Gate 觀測項

不開新票。TapFocus 精度偏差主要影響 Sony 21:9 螢幕邊緣 ~550px 區域，核心使用場景（中央區域對焦）不受影響。Gate 場景 2 Sony 實測將捕獲問題；發現問題 → 開 P08-HF。

**歸檔指令**：本輪發現（contentBounds 三路徑獨立性 + NormalizedToPixelTransformer 天然免疫性）具備事後參閱價值，已確認執行 Obsidian 歸檔。

##### AI-2: 管線合一（如確認雙管線）
- 若 `observeDetectionResults()` 雖已註解但有其他路徑寫入 `detectionResult` → 合併為單一入口
- 若確認僅 `observePreviewForAnalysis()` 寫入 → 跳過此 AI，在 AI-3 直接於該入口加過濾
- [ ] 完成

##### AI-3: Ghost Face 源頭過濾（在 detectionResult 寫入 _state 之前）
- **檔案**: `CameraViewModel.kt`
- **位置**: `observePreviewForAnalysis()` 中 `_state.update { it.copy(detectionResult = ...)` 之前
- **目標值**: 過濾 `detectionResult.faces`，移除所有 bbox 越界的 face：
```kotlin
val validFaces = detectionResult.faces.filter { face ->
    val box = face.boundingBox
    box.left >= 0 && box.top >= 0 &&
    box.right <= detectionResult.imageWidth &&
    box.bottom <= detectionResult.imageHeight
}
val sanitizedResult = detectionResult.copy(faces = validFaces)
// 後續使用 sanitizedResult 取代 detectionResult
```
- **覆蓋範圍**: 此過濾在 _state 寫入前執行，一次覆蓋所有下游消費者（路徑 A/B/C + 未來新增）
- [ ] 完成

##### AI-4: 防禦性末端 guard（HaloGuidanceLayer）
- **檔案**: `HaloGuidanceLayer.kt` → `drawGazeHalo()`
- **現值**: 僅檢查 `rectWidth <= 0f || rectHeight <= 0f`
- **目標值**: 新增 normBbox [0,1] 邊界驗證（冗餘防線）：
```kotlin
val box = face.boundingBoxNormalized
if (box.left < 0f || box.top < 0f || box.right > 1f || box.bottom > 1f) return
```
- [ ] 完成

#### Hard Constraints
1. CameraViewModel 修改僅限 ML Kit 管線邏輯（detectionResult 寫入路徑 + ghost face 過濾）
2. 禁止新增 CameraViewModel 公開 API
3. 禁止修改 TapFocusController / EyeAfController / LiveCameraSource（已由 P05-R1 封閉）
4. `detectionResult.copy(faces = validFaces)` — 僅過濾 faces，不動 objects

#### Error Handling
- Discovery 發現第三條未知管線 → 停止回報
- `assembleDebug` 失敗且指向 DetectionResult 結構變更 → 回報 copy() 可用性
- bbox 過濾後 validFaces 為空的場景需保留 — 不得因無 face 而跳過 detectionResult 更新（否則 stale data 殘留）

#### Acceptance Criteria
- [ ] grep 全 codebase `_state.update.*detectionResult` 僅一處寫入點
- [ ] 寫入點之前有 bbox [0,1] 過濾邏輯
- [ ] HaloGuidanceLayer.drawGazeHalo 有 normBbox [0,1] guard
- [ ] Logcat 確認：ghost face 被過濾時輸出 `[P08-F3] Filtered N ghost faces with out-of-bounds bbox`
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

#### Traycer 配額
**本票指定 Traycer `execute_review`** — Phase 0.8 唯一使用 Traycer 的票。理由：碰 CameraViewModel ML Kit 管線核心，影響全部 Detection Overlay + AF + PTF，風險等級最高。

---

### P08-F4: BUG-SONY-ROTATION-CONST

**Mode**: normal    **Effort**: Medium
**前置依賴**: P08-F3

#### Verify Config
```
TICKET_ID=P08-F4
CREATED=2026-06-01
PRESET=bugfix
ALLOWED=[Discovery 後確定]
FORBIDDEN=CameraViewModel.kt
MAX_LINES=80
DIFF_REF=[執行時取 HEAD]
```

#### Context
Sony XQ-BT52 rotation 恆為 0 — 導致座標轉換錯誤。需 Discovery 確認 rotation 值來源與修正點。

#### Action Items

##### AI-1: Discovery — 追查 rotation 值來源
- 搜尋 `rotationDegrees` 在 `DetectionResult` 中的填充路徑
- 確認 Sony 裝置回報的 `InputImage.rotationDegrees` 是否為 0
- 回報根因 + 建議修正方案
- [ ] 完成

*(後續 AI-N 由 Discovery 結果決定)*

#### Acceptance Criteria
- [ ] Sony XQ-BT52 rotation 正確反映裝置旋轉
- [ ] `assembleDebug` PASS

---

### P08-F5: BUG-PTF-HALO-SIZE

**Mode**: normal    **Effort**: Low
**前置依賴**: P08-F3

#### Verify Config
```
TICKET_ID=P08-F5
CREATED=2026-06-01
PRESET=bugfix
ALLOWED=HaloGuidanceLayer.kt,HaloIndicator.kt
FORBIDDEN=CameraViewModel.kt
MAX_LINES=50
DIFF_REF=[執行時取 HEAD]
```

#### Context
bbox → 圓半徑轉換錯誤 — Halo 尺寸偏離 SSOT 規格。需 Discovery 確認具體偏差公式。

#### Action Items

##### AI-1: Discovery — 比對 SSOT 規格與代碼公式
- 讀取 SSOT §2.5 Halo 規格中的尺寸定義
- 比對 `HaloGuidanceLayer.drawGazeHalo()` 中 radius 計算
- 回報偏差量 + 修正公式
- [ ] 完成

*(後續 AI-N 由 Discovery 結果決定)*

#### Acceptance Criteria
- [ ] Halo 半徑符合 SSOT §2.5 規格
- [ ] `assembleDebug` PASS

---

### P08-F6: Pixel9aLayout 雙層覆蓋

**Mode**: normal    **Effort**: Medium
**前置依賴**: P08-E1

#### Verify Config
```
TICKET_ID=P08-F6
CREATED=2026-06-01
PRESET=bugfix
ALLOWED=[Discovery 後確定]
FORBIDDEN=CameraViewModel.kt
MAX_LINES=80
DIFF_REF=[執行時取 HEAD]
```

#### Context
Session N 三因子分析已完成，Pixel 9a 特定 layout 觸發雙層 overlay 覆蓋。需 Discovery 確認具體修正點。

#### Action Items

##### AI-1: Discovery — Pixel 9a layout 三因子確認
- 讀取 Layout Spec v2.5.1 中 Pixel 9a 相關定義
- 比對 CameraScreen.kt 中裝置判斷邏輯
- 回報根因 + 建議修正方案
- [ ] 完成

*(後續 AI-N 由 Discovery 結果決定)*

#### Acceptance Criteria
- [ ] Pixel 9a 無雙層 overlay 覆蓋
- [ ] `assembleDebug` PASS

---

## §5 Wave L — 陳年清理

### P08-L1: P05-R1 GazeAF 形式閉環

**Mode**: normal    **Effort**: Low
**前置依賴**: P08-F3（F-3 的源頭過濾使 R1 成為冗餘防線，但仍需形式閉環）

#### Action Items

##### AI-1: sv_report.md 補寫 CSO Authorization Override
- 新增段落：
```markdown
## CSO Authorization Override
P05-R1 verify_cmd.conf ALLOWED 欄位明列 EyeAfController.kt，
構成 ssot_context.md §3 所定義之「明確 Intent 授權」例外。
授權鏈：CSO Contract P05-R1 → verify_cmd.conf ALLOWED → 覆蓋 §3 Scope Ban。
```
- [ ] 完成

##### AI-2: Cross-Review R2 — CSO Override Skip

- **CSO 裁決** (Session VI, 2026-06-02): R2 跳過，直接進入 Traycer 批次路徑
- **理由**: (1) Layer 3.4 機械閘門高概率 BLOCK — reasoning-based scope evaluation does not occur (2) F-3 前置依賴未就位 (3) verify_cmd.conf 原始 DIFF_REF/DIFF_END 無記錄可還原
- **路徑**: P05-R1 直接併入 L-1 + L-2 Traycer 批次 strict_verification
- [x] 完成（CSO override — 無需執行）

---

### P08-L2: P05-T3 Traycer 批次閉環

**狀態**: Code Done, SV + Cross-Review PASS, 待 Traycer strict_verification
**動作**: 與 L-1 打包批次提交 Traycer

---

### P08-L3: P05-T4 Scrim TopBar 漸層遮罩

**Mode**: normal    **Effort**: Medium
**前置依賴**: P08-F3 + P08-F2（GridOverlay Z-order 確定後再做 Scrim 避免衝突）

#### Verify Config
```
TICKET_ID=P08-L3
CREATED=2026-06-01
PRESET=feature
ALLOWED=CameraScreen.kt,[Scrim 檔案 — Discovery 後確定]
FORBIDDEN=CameraViewModel.kt
MAX_LINES=100
DIFF_REF=[執行時取 HEAD]
```

#### Context
SSOT 規格定義 TopBar scrim（y=0 α0.22 → y=120px α0），代碼缺失。P05-T4 原始規格保留，在 Phase 0.8 乾淨基底上實作。

#### Action Items

##### AI-1: Discovery — 確認 Scrim 渲染位置
- SSOT 附錄中 Scrim 規格
- CameraScreen.kt 中 TopBar 區域的 Z-order
- 回報：建議實作位置（新 Composable 或內嵌）
- [ ] 完成

*(後續 AI-N 由 Discovery 結果決定)*

#### Acceptance Criteria
- [ ] TopBar 區域有 α0.22→α0 漸層遮罩
- [ ] Scrim 不遮蓋 Focus ring / GridOverlay / 控制項
- [ ] `assembleDebug` PASS

---

## §6 Gate — Phase 0.5 Visual Baseline

**觸發條件**: Wave E + F + L 全部 Done
**執行者**: Akira（實機操作）+ CSO（截圖比對裁決）

### 八場景清單

| # | 場景 | 驗證重點 | 裝置 |
|---|------|---------|------|
| 1 | 無人臉 · 靜態 | 無琥珀圓環、無 Edge Warmth gradient | Sony + Pixel |
| 2 | 單人臉 · 正面 | Halo 尺寸正確、Focus ring 正常；Sony XQ-BT52 追加：點擊螢幕上方 1/5 和下方 1/5 區域人臉，確認 TapFocusRing 出現在點擊位置（偏移 → 開 P08-HF 修正 calculateContentBounds FILL_CENTER 邏輯） | Sony + Pixel |
| 3 | 多人臉 | 無 ghost face 圓環、Halo 數量 = 真實人臉數 | Sony + Pixel |
| 4 | 俯拍食物 (45°) | 無 PitchTraction gradient、Level 正確退場 | Sony |
| 5 | 手持微顫 (0-3°) | Level opacity ≤ 0.30、無閃爍 | Sony + Pixel |
| 6 | GridOverlay 開啟 | 格線目視可見 | Pixel 9a |
| 7 | PTF 遞手機模式 | Halo 正確、無 ghost face 干擾 | Sony |
| 8 | Scrim TopBar | 漸層遮罩可見 | Sony + Pixel |

### Done 條件
- 八場景全 PASS → Phase 0.8 Done，進入 Wave 2 CameraScreen triple-strike
- 任一場景 FAIL → 開 P08-HF（Hotfix）修補後重測

---

## §7 Traycer 配額總結

| 票 | Traycer 等級 | 預估費用 |
|----|-------------|---------|
| E-1 | /qa + /sv + cross_review | $0 |
| F-1 ~ F-2 | /qa + /sv + cross_review | $0 |
| **F-3** | **execute_review** | **~$0.25** |
| F-4 ~ F-6 | /qa + /sv + cross_review | $0 |
| L-1 + L-2 | 批次 strict_verification | ~$0.25 |
| L-3 | /qa + /sv + cross_review | $0 |
| **Total** | | **~$0.50** |

養兵千日，用在一時。F-3 是 Phase 0.8 唯一碰 CameraViewModel 核心管線的票，也是唯一值得 Traycer 投入的高風險節點。

---

## §8 執行時序建議

```
Day 1: E-1 PitchTractionLayer 清場
Day 2: F-1 常數修正 + F-2 GridOverlay
Day 3: F-3 管線合一 + 源頭過濾（最長 — 可能跨兩天）
Day 4: F-4/F-5/F-6 裝置修復
Day 5: L-1/L-2/L-3 Legacy 閉環
Day 6: Visual Baseline Gate（八場景實機截圖）
```

---

**End of P08 Codebase Sanitization Contract v1.0**
