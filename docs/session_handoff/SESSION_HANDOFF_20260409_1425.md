# Cueshot 工作交接 — 2026-04-09

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `.claude/PROGRESS.md` | Sprint Roadmap | D1 起所有日期順延 1 天（D1=04-09, D2=04-10 ... D15=04-29）；Next KAIROS 改為 D2 (04-10) |
| `.claude/PROGRESS.md` | Current Day / Sprint Health | 更新為 D2 (2026-04-09)；Compile/Lint 指標全綠 |
| `.claude/PROGRESS.md` | D1 Tickets 區塊 | 新增 D1-T1~T5 全數 ✅ DONE |
| `.claude/skills/uc/SKILL.md` | 輸出規則 1 | 明確封鎖工具呼叫間的任何語言過渡文字（含日文） |
| `devspec/feature_record_Backlog.md` | 底部 | 新增 ADR 區塊（D0 絞殺者手術 5 項架構決策） |
| `app/.../presentation/camera/CameraViewModel.kt` | `observePreviewForAnalysis()` | SV-D0.1-09：tier-aware `focalLengthPixels` 計算 + `consumeCameraMotion()` per-frame 呼叫 |
| `app/.../domain/composition/CompositionRule.kt` | 全檔（新建） | `RuleResult` data class + `CompositionRule` interface（D1 骨架） |
| `app/.../domain/model/CompositionScore.kt` | 全檔（新建） | `CompositionScore(totalScore, isGolden)` aggregate 模型 |
| `app/.../domain/usecase/AnalyzeCompositionUseCase.kt` | KDoc | 更新：D1 決策記錄 + D2 migration TODO |
| `app/.../presentation/camera/CameraState.kt` | 全檔 | 新增 `PtfState` sealed class (Idle/Focusing/Focused/Failed) + `ptfState` 欄位 |
| `app/.../domain/model/DeviceCapabilityTier.kt` | 全檔（新建，D0） | FULL/LIMITED/LEGACY sealed class + `fromCamera2Level()` |
| `app/src/test/.../AnalyzeCompositionUseCaseTest.kt` | class 頭部 | `@Ignore` annotation（v2.x 具體邏輯測試 obsolete，D2 重寫） |

---

## 當前阻塞點

無

---

## 下一步待辦

1. **D2-T1（優先）**：實作 `ThirdsRule.kt`（`domain/composition/rules/ThirdsRule.kt`）— 實作 `CompositionRule` 介面，輸入 `faces: List<RectF>` + pitch/roll，計算最近三分法交叉點距離，返回 `RuleResult(score, isAligned)`。參考 `CompositionGuidelineEngine.analyzeRuleOfThirds()` 既有數學邏輯。

2. **D2-T2**：實作 `HeadroomRule.kt`（`domain/composition/rules/HeadroomRule.kt`）— 檢查人臉頂部距 viewport 頂部的空間比例（理想 5~15%），返回 `RuleResult`。

3. **D2-T3**：建立 `di/CompositionModule.kt` — Hilt `@Multibinds` 將 `ThirdsRule`、`HeadroomRule` 綁定至 `Set<CompositionRule>`。

4. **D2-T4**：遷移 `AnalyzeCompositionUseCase` constructor — 改為 `Set<@JvmSuppressWildcards CompositionRule>` 注入，實作加權平均分數計算，返回 `CompositionScore` 而非 `CompositionAdvice`。

5. **D2-T5**：移除 `previewWidth ?: 800` 魔術數字（PROGRESS.md Backlog [Coordinate]）— 改由 `CameraCharacteristics` 讀取真實焦距估算值，位置：`CameraViewModel.observePreviewForAnalysis()` 第 1593 行附近。

6. **KAIROS #1（D2 結束時，04-10）**：執行 `/dream` 回顧 IMU accuracy、Edge Warmth alpha tuning、scene switch hysteresis。

---

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `AnalyzeCompositionUseCase` constructor 維持現狀，不提前遷移至 `Set<CompositionRule>` | 需同步建立 `CompositionModule.kt` (Hilt `@Multibinds`)，D1 做會引入未授權 DI 結構修改，推遲至 D2 原子化執行 | `AnalyzeCompositionUseCase.kt` |
| ViewModel 不新增 `onTapToFocus()` | 現有 `setTapToFocusIntent()` + `FocusStateMachine`（v2.12.0）已完整，新增會造成雙重狀態衝突 | `CameraViewModel.kt` |
| `PtfState` 與 `FocusUiState` 並存 | `PtfState` 是 Rule Engine 高層協調狀態，`focusUiState` 是 overlay 渲染狀態，職責不同不衝突 | `CameraState.kt` |
| `AnalyzeCompositionUseCaseTest` 改 `@Ignore` 而非刪除 | D0 焦土後 v2.x 具體邏輯測試 obsolete，但保留檔案架構以便 D2 重寫，避免從零建測試 | `AnalyzeCompositionUseCaseTest.kt` |
| Sprint Roadmap 順延 1 天 | D0 實際執行至 04-09（含 SV 補修系列），D1 無法在 04-08 完成 | `PROGRESS.md` |

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md`
- `.claude/PROGRESS.md`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/CompositionRule.kt`（D2-T1/T2 實作的介面定義）
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/CompositionGuidelineEngine.kt`（既有 RuleOfThirds 數學邏輯，D2-T1 參考）
- `app/src/main/java/com/akirachang/ai/photographer/domain/usecase/AnalyzeCompositionUseCase.kt`（D2-T4 遷移目標）
- `app/src/main/java/com/akirachang/ai/photographer/di/`（D2-T3 DI module 新增位置）

---

## 不需帶入的 Context

- SV-D0.1 系列補修細節（全數 commit 完成，grep 驗證 0 殘留）
- D0.4 ADR 歸檔過程（已寫入 `devspec/feature_record_Backlog.md`）
- UC mode 日文問題修復過程（已修入 `SKILL.md`，規則已生效）
- `AnalyzeCompositionUseCaseTest` @Ignore 的調查過程（原因：v2.x 具體邏輯在 D0 清掉，結論已記錄於 commit message）
- Sprint Roadmap 日期計算過程（結果已寫入 PROGRESS.md）
- Git 鐵律 2 的測試 commit 分離過程（已完成，流程已知）
