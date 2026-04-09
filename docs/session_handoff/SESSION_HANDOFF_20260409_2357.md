# Cueshot 工作交接 — 2026-04-09

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/.../di/CompositionModule.kt` | 全檔（新建）| Hilt `@Multibinds abstract class`，提供空 `Set<CompositionRule>` 綁定 |
| `app/.../domain/usecase/AnalyzeCompositionUseCase.kt` | 全檔 | constructor 改為 `Set<@JvmSuppressWildcards CompositionRule>`；invoke 簽名改為 `(faces, imuPitch, imuRoll): CompositionScore`；回傳骨架 `CompositionScore(0f, false)` |
| `app/.../presentation/camera/CameraViewModel.kt` | `onUiEvent(FocusOnPoint)` | 額外呼叫 `onTapToFocus(x, y, viewWidth, viewHeight)` |
| `app/.../presentation/camera/CameraViewModel.kt` | `onTapToFocus()` (新增) | PTF 狀態機 + session token 競態防護（`currentPtfSessionId`）；Idle→Focusing→Focused/Failed→Idle；Defence A/B 兩點 token 比對 |
| `app/.../presentation/camera/CameraViewModel.kt` | `observeCompositionAdvice()` | 主通道 + 次通道 `analyzeRuleOfThirds()` 靜默化，強制 `CompositionAdvice.None`；Flow 生命週期結構保留 |
| `app/.../presentation/camera/CameraViewModel.kt` | class body | 新增 `private var currentPtfSessionId = 0L` |
| `app/src/test/.../AnalyzeCompositionUseCaseTest.kt` | 全檔 | 移除 class-level `@Ignore`；setup 改用 `emptySet()`；52 個舊 `@Test` 各加 `@Ignore("TODO: Cueshot Ticket D2...")`；舊方法體清空；新增 2 個 D1 契約測試 |
| `app/src/test/.../presentation/camera/PtfStateTest.kt` | 全檔（新建）| PtfState data class 等價性測試（2 pass）|
| `.claude/skills/uc/SKILL.md` | 規則 6/7/8 | 新增結構化完成回報格式 + 自動接力 /qa 規則 |
| `devspec/feature_record_Backlog.md` | 底部 ADR 區塊 | 新增 D0.x ADR（tier-aware + color path）+ D1 ADR（PTF token / Hilt Multibinds / Flow 靜默化三大決策）|
| `.claude/PROGRESS.md` | 全域狀態 | D1 CLOSED；D2 IN PROGRESS；Blocking Issues 清零；FIX-01~06 + D1.1 全數 ✅ |

---

## 當前阻塞點

無

---

## 下一步待辦

1. **D2-T1（優先）**：新建 `app/.../domain/composition/rules/ThirdsRule.kt`
   - 實作 `CompositionRule` 介面（參考 `CompositionRule.kt` 的 `evaluate()` 簽名）
   - 輸入：`faces: List<RectF>`, `imuPitch: Float`, `imuRoll: Float`
   - 邏輯：計算人臉中心到最近三分法交叉點（4 個）的距離，距離 < 閾值則 `isAligned = true`
   - 數學參考：`CompositionGuidelineEngine.analyzeRuleOfThirds()` 的既有邏輯
   - 輸出：`RuleResult(score: Float, isAligned: Boolean)`

2. **D2-T2**：新建 `app/.../domain/composition/rules/HeadroomRule.kt`
   - 實作 `CompositionRule` 介面
   - 邏輯：計算人臉頂部距 viewport 頂部的比例（理想 5~15%），偏離則扣分
   - 輸出：`RuleResult(score, isAligned)`

3. **D2-T3**：新建 `app/.../di/CompositionModule.kt` 的 Rule 綁定（目前已有空 `@Multibinds`）
   - 新增 `@Binds @IntoSet` 將 `ThirdsRule` 和 `HeadroomRule` 綁定至 `Set<CompositionRule>`

4. **D2-T4**：修改 `AnalyzeCompositionUseCase.invoke()`
   - 實作加權平均：遍歷 `rules`，呼叫每個 `rule.evaluate(faces, imuPitch, imuRoll)`，計算 `totalScore`
   - `isGolden` = `totalScore >= 0.8f`（或 SSOT 定義閾值）

5. **D2-T5**：移除 `CameraViewModel.observePreviewForAnalysis()` 第 ~1593 行的 `previewWidth ?: 800` 魔術數字
   - 改由 `CameraCharacteristics` 讀取真實焦距估算值

6. **KAIROS #1（D2 結束時，2026-04-10）**：執行 `/dream` 回顧 IMU accuracy、Edge Warmth alpha tuning、scene switch hysteresis

---

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| PTF 狀態機使用 session token（非 data class ==）防競態 | `Focused(x,y) == Focused(x,y)` 無法區分新舊請求；同座標快速連點時舊 timer 會誤清新狀態 | `CameraViewModel.onTapToFocus()` |
| `CompositionModule` 用 `abstract class`（非 `object`）| Hilt `@Multibinds` 不支援 `object`，否則 kapt 編譯期 `MissingBinding` | `di/CompositionModule.kt` |
| Flow 靜默化只替換 `.onEach {}` 內容，保留訂閱鏈結構 | 最小化重構風險，避免 coroutine scope leak 或訂閱時序改變 | `CameraViewModel.observeCompositionAdvice()` |
| 舊測試方法體清空（非刪除）以通過 Kotlin 編譯 | `@Ignore` 不跳過編譯期型別檢查；舊簽名不相容新 UseCase 必須清空方法體 | `AnalyzeCompositionUseCaseTest.kt` |

---

## 需帶入新對話的 Context

- `.claude/CLAUDE.md`
- `.claude/PROGRESS.md`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/CompositionRule.kt`（D2-T1/T2 實作的介面定義）
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/CompositionGuidelineEngine.kt`（既有 RuleOfThirds 數學邏輯，D2-T1 參考）
- `app/src/main/java/com/akirachang/ai/photographer/di/CompositionModule.kt`（D2-T3 需在此新增 @Binds @IntoSet）
- `app/src/main/java/com/akirachang/ai/photographer/domain/usecase/AnalyzeCompositionUseCase.kt`（D2-T4 實作加權邏輯）

---

## 不需帶入的 Context

- SV-D1-FIX-01~06 的實作細節（全數 commit 完成，結果在 PROGRESS.md）
- `AnalyzeCompositionUseCaseTest` 舊測試清空的過程（python 批次腳本，已完成）
- D0.x / D1 ADR 歸檔過程（已寫入 `devspec/feature_record_Backlog.md`）
- `/uc` SKILL.md 規則更新過程（已生效）
- Traycer Path B → Path A 的補修過程（歷史，不影響 D2）
