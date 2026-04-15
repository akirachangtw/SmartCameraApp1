# Cueshot 工作交接 — 2026-04-15 16:48

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/androidTest/.../overlay/HDRWarningOverlayTest.kt` | `stateTransition_fromNormalToBacklight`, `stateTransition_fromBacklightToNormal`, `stateTransition_betweenWarningStates` | DEBT-01：改用 `var state by mutableStateOf(...)` 驅動重組，移除錯誤的雙重 `setContent` 呼叫（Rule 14 Compose Slot-Table） |
| `app/src/androidTest/.../overlay/EdgeWarmthLayerTest.kt` | 全部 Canvas 斷言測試（Tests 1/2/3/7/8/9） | DEBT-02：每個 `setContent` 後加入 `waitForIdle()` 消除 Sony race condition |
| `app/src/main/.../rules/GazeRule.kt` | `companion object` | DEBT-03：`MAX_YAW_DEG` → `GUIDANCE_MAX_YAW_DEG`；`EXTREME_YAW_DEG` → `EXTREME_YAW_INVERSION_DEG`（Priority Inversion 防禦語意）；新增 `const val NAME = "gaze"` |
| `app/src/main/.../rules/EyeLineRule.kt` | `companion object` | DEBT-03b：新增 `const val NAME = "eye_line"`（與 GazeRule 對稱） |
| `app/src/main/.../components/HaloGuidanceLayer.kt` | `ruleScores` 查詢（line ~135-136） | DEBT-04：`"gaze"` → `GazeRule.NAME`；`"eye_line"` → `EyeLineRule.NAME`；新增對應 import |
| `app/src/main/.../components/EyeAfController.kt` | `companion object GAZE_RULE_KEY` | DEBT-04b：`private const val GAZE_RULE_KEY = "gaze"` → `private val GAZE_RULE_KEY = GazeRule.NAME`；新增 import |
| `app/src/test/.../components/EyeAfControllerTest.kt` | `unalignedGazeResult()`, `alignedGazeResult()` | DEBT-04c：`"gaze"` literal → `GazeRule.NAME`；新增 GazeRule import |
| `app/src/test/.../rules/GazeRuleTest.kt` | 測試注解（line ~120, ~141） | DEBT-03 同步：注解中 `EXTREME_YAW_DEG` → `EXTREME_YAW_INVERSION_DEG` |
| `app/src/main/.../camera/CameraViewModel.kt` | face mapping lambda（line ~1930-1934） | DEBT-04d：移除 `D6_PROBE` debug Log（非生產邏輯清除） |
| `.claude/CLAUDE.md` | KAIROS #2 產出區塊 | 追加 Rule 16：硬體呼叫雙重門控規則（前鏡頭憲法 guard + 節流哨兵值 `Long.MIN_VALUE/2`） |
| `.claude/PROGRESS.md` | header、Sprint Health、D7 Tickets 區塊 | D7-Batch0 ✅ DONE；D7-Batch1 Ticket 骨架建立；Unit Tests 更新至 188t/0f/48s |

## 當前阻塞點

無

## 下一步待辦

1. **D7-B1-T1 (PTF Tab toggle)**：讀取 `presentation/camera/CameraScreen.kt` 與 `CameraState.kt`，確認 `ptfState` 接線現狀；實作進入/退出 `PassToFriendMode` 的 Tab toggle UI（300ms 切換，零 CameraX rebind）
2. **D7-B1-T2 (1s 快門冷卻)**：在 `CameraViewModel.kt` 的 PTF 進入邏輯中加入 1s 快門冷卻保護（參考現有 `ptfSessionId` 機制）
3. **D7-B1-T3 (極簡 UI)**：PTF 模式下隱藏 composition guides（`HaloGuidanceLayer`、`GridOverlay`）；綠色 Halo 接管引導

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `EXTREME_YAW_INVERSION_DEG` 維持 45f（與 `GUIDANCE_MAX_YAW_DEG` 相同值） | 當前設計意圖：score=0 的角度同時觸發 Priority Inversion fail-safe；兩值相等是合理設計，語意拆分目的是讓意圖可讀，非改變邏輯 | `GazeRule.kt` |
| `EyeLineRule.NAME` 同步新增（非 task 要求，主動補齊） | 與 GazeRule.NAME 保持對稱；HaloGuidanceLayer 同時使用兩個 rule key，不做就會留下 half-fix | `EyeLineRule.kt`, `HaloGuidanceLayer.kt` |
| Rule 16 寫入 CLAUDE.md（KAIROS #2 補收割） | Traycer 候選規則「硬體呼叫防護必須雙重門控」在 EyeAfController 已有實作範例，值得提煉為正式規則供後續 D9（美食對焦）遵守 | `CLAUDE.md` |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別是 Rules 13/14/15/16 為本 Session 新增/補充）
- `.claude/PROGRESS.md` — D7 Ticket 清單（Batch1 待開工）
- `app/src/main/.../camera/CameraScreen.kt` — D7-B1 需了解 PTF Tab 接線現狀
- `app/src/main/.../camera/CameraState.kt` — 確認 `ptfState` 欄位定義
- `app/src/main/.../camera/CameraViewModel.kt` — 確認 PTF 狀態機接線位置

## 不需帶入的 Context

- D7-Batch0 所有 debug 過程（Compose 雙重 setContent 根因、race condition 分析、magic string 溯源）— 已修復並 commit，根因已解決
- KAIROS #2 完整回顧細節（Error Patterns、Decision Audit）— 已寫入 PROGRESS.md，新對話讀 PROGRESS.md 即可
- Rule 16 候選評估過程 — 已決策並落地 CLAUDE.md
- `EXTREME_YAW_INVERSION_DEG` 語意分析 — 已決策維持 45f，注解已補充，不需再討論
