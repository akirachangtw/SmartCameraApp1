# Cueshot 工作交接 — 2026-04-17 21:10

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `presentation/camera/CameraViewModel.kt` | `observePtfAlignment()` | 函數入口新增 `ptfEmaX = null` / `ptfEmaY = null` 強制清零（Gate 1 — Flow 訂閱起點防禦） |
| `presentation/camera/CameraViewModel.kt` | `onCleared()` | 補齊 `ptfEmaX/Y = null` + `_state.update { ptfAlignmentResult = NO_FACE }`（Gate 4 — ViewModel 銷毀防禦） |
| `.claude/settings.local.json` | `permissions.allow` | 新增 `"Bash(git stash *)"` 白名單（QA lint baseline 比對工具用） |
| `.claude/PROGRESS.md` | D8-B1 區塊 | D8-B1 標記 `✅ CLOSED (Path A)`；新增 `SSOT-ORPHAN-RULE-01` 技術債；新增 `Candidate Rule 17` |

## 當前阻塞點

無

## 下一步待辦

### 立即執行（D8-B2）
1. **D8-B2-T1 架構決策（`/normal` 討論）**：確認 PTF Halo 元件共用策略
   - 閱讀 `plans/architecture/D8_B1_Architecture_Memo.md` §決策 5
   - 閱讀 `plans/architecture/SSOT_v3.3_Section_4_5_D8_Patch.md`（PTF Halo 視覺規格）
   - 核心問題：沿用 `HaloGuidanceLayer.kt` 還是新建 `PtfHaloLayer.kt`？
   - 關鍵差異：PTF Halo 需顯示引力場方向（`PtfOffset.dx/dy`），Normal Halo 顯示琥珀金圓環

2. **D8-B2-T2 PTF Halo 牽引動畫實作**（架構決策後執行）：
   - 讀取 `CameraState.ptfAlignmentResult`（已就緒）
   - UI 層將 `PtfOffset(dx, dy)` × `viewSize` 轉為 px（在 Composable 內，非 Domain 層）
   - `#50C864` 綠色 Halo 引力場動畫；`isAligned=true` 時切換呼吸韻律

3. **D8-B2-T3 androidTest 回歸驗證**（Sony XQ-BT52）

### Git Commits（本 Session 已完成）
- `892a330` `[Fix]` D8-B1-FIX: CameraViewModel.kt 4-gate EMA 防禦 ✅
- `cd2dc55` `[Docs]` PROGRESS.md + SSOT-ORPHAN-RULE-01 + Candidate Rule 17 ✅

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| EMA 重置用「函數入口直接清零」而非 Flow `onStart { }` | `onStart` 插入後 Kotlin 型別推斷鏈斷裂（`Pair<DetectionResult?, CameraMode>` 解構失敗），函數入口方案語意等價且無副作用 | `CameraViewModel.observePtfAlignment()` |
| `PtfRuleAggregator` FIX-02 調查結論：無魔術數字，推遲重構至 D10 | 0.12f/15f/25f/8f 均已提取為 companion 具名常數；雙份邏輯風險登記為 `SSOT-ORPHAN-RULE-01` | `PtfRuleAggregator.kt`（未修改） |
| `onCleared()` 補齊 `_state.update(ptfAlignmentResult = NO_FACE)` | 防止 ViewModel 重建時 StateFlow 殘留舊 PTF 對齊結果汙染新 Session UI | `CameraViewModel.onCleared()` |

## SV 驗收結果

- 🔨 Compile: ✅ PASS
- 🔍 Lint: ✅ PASS (0 new warnings, 558 baseline)
- 📦 16KB ELF: ✅ ALIGNED
- 🧹 Orphans: ✅ CLEAN
- 📐 SSOT: ✅ ALIGNED
- 📊 Unit Tests: 224t / 0 failed

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（Rules 13/14/15/16）
- `.claude/PROGRESS.md` — 確認 D8-B1 ✅ CLOSED，D8-B2 TODO
- `CLAUDE.local.md` — ADB device ID
- `plans/architecture/D8_B1_Architecture_Memo.md` — §決策 5（Halo 元件共用策略，D8-B2-T1 必讀）
- `plans/architecture/SSOT_v3.3_Section_4_5_D8_Patch.md` — PTF Halo 視覺規格
- `presentation/camera/CameraState.kt` — 確認 `ptfAlignmentResult` 欄位
- `presentation/camera/components/HaloGuidanceLayer.kt` — D8-B2 修改目標（或新建 PtfHaloLayer）

## 不需帶入的 Context

- D8-B1-FIX 的 `onStart` 型別推斷失敗過程（已解決，用函數入口方案）
- Lint baseline 比對方式（git stash 法，已驗證有效）
- FIX-02 魔術數字調查細節（結論：無需修改，已登記 SSOT-ORPHAN-RULE-01）
- QA/SV 執行過程（全數通過，無遺留問題）
- D8-B1 T1/T2/T3 實作細節（已封板）
