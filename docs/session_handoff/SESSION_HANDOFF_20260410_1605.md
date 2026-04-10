# Cueshot 工作交接 — 20260410_1605

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/src/main/java/.../domain/composition/CompositionRule.kt` | `evaluate()` KDoc | 更新為完整正規化座標契約警告（[0.0,1.0]，禁止 pixel，caller 責任） |
| `app/src/main/java/.../domain/composition/rules/ThirdsRule.kt` | `evaluate()` | 新增 `hasInvalidCoordinate` 執行期邊界防呆（越界/負值/malformed 靜默降級） |
| `app/src/test/java/.../domain/composition/rules/ThirdsRuleTest.kt` | 全部 17 tests | 補充 invariant test（同中心不同大小分數相同）+ 3 負向測試（pixel/負值/malformed） |
| `app/src/main/java/.../domain/composition/rules/HeadroomRule.kt` | 全部 | 新增 HeadroomRule 實作；`weight=0.8f`；ratio=[top/faceHeight]；ideal=[0.1,0.2]→100分；越界靜默降級 |
| `app/src/test/java/.../domain/composition/rules/HeadroomRuleTest.kt` | 全部 12 tests | 新增 Robolectric 測試；含正向(完美/邊界/越遠)/負向(pixel/負值/malformed)/IMU不變性/多臉選最大 |
| `devspec/feature_record_Backlog.md` | ADR 區塊 | 追加「領域規則共通契約」ADR + D2-T2 HeadroomRule 驗證紀錄 |
| `.claude/PROGRESS.md` | D2 Tickets + Sprint Health | SV-D2-T1-FIX-02/03 ✅、D2-T1.1 ✅、D2-T2 ✅ (Path A)、D2-T2.1 ✅；計數 104t/48skipped/56pass |

## 當前阻塞點

無

## 下一步待辦

1. **D2-T3**：修改 `app/src/main/java/.../di/CompositionModule.kt`
   - 新增 `@Binds @IntoSet` 為 `ThirdsRule` 和 `HeadroomRule` 綁定進 Hilt multibinding
   - 注意：`CompositionModule` 必須是 `abstract class`（D1 ADR 鐵律）

2. **D2-T4**：實作 `app/src/main/java/.../domain/usecase/AnalyzeCompositionUseCase.kt` 的 `invoke()`
   - 加權平均所有規則的 score：`totalScore = Σ(rule.weight * score) / Σ(rule.weight)`
   - `CompositionScore(totalScore = weightedAvg, isGolden = totalScore >= 0.8f)`（注意 isGolden 用 0-1 scale，非 0-100）

3. **D2-T5**：移除 `CameraViewModel.kt` 中的 `previewWidth ?: 800` 魔術數字，改由 `CameraCharacteristics` 讀取

4. **D2 收工後執行 `/dream` (KAIROS #1)**：回顧 IMU accuracy、Edge Warmth alpha tuning、scene switch hysteresis

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| 邊界防呆採靜默降級 `RuleResult(0f, false)` 而非拋出例外 | 相機管線為高頻即時系統，例外拋出代價過高 | `ThirdsRule.kt`, `HeadroomRule.kt`，及後續所有 Rule |
| HeadroomRule 的 `isAligned` 判斷改為 `>=` / `<=` 顯式比對，不用 Kotlin `in` range | `ratio in 0.1f..0.2f` 在浮點邊界（如 `0.2f * 0.4f = 0.080000006f`）可能誤判，導致 `score=99` 而非 `100` | `HeadroomRule.kt`, `HeadroomRuleTest.kt` |
| 測試邊界值使用「直接構造」(`top=0.1f, height=0.5f`) 而非乘法推算 | 浮點乘法誤差（`0.2f * 0.4f ≠ 0.08f`）導致邊界測試不穩定 | `HeadroomRuleTest.kt` |
| 共通契約模板固化為 ADR | D2-T1~T2 兩個 Rule 一致落地，確認模板穩定；後續 EyeLine/Gaze 必須依樣實作 | `devspec/feature_record_Backlog.md` |

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 最高憲法 + 工作流 SOP
- `.claude/PROGRESS.md` — 當前進度（D2 IN PROGRESS，D2-T1~T2 ✅，下一步 D2-T3）
- `app/src/main/java/.../di/CompositionModule.kt` — D2-T3 需修改此檔案（Hilt multibinding）
- `app/src/main/java/.../domain/usecase/AnalyzeCompositionUseCase.kt` — D2-T4 需實作 `invoke()`
- `app/src/main/java/.../domain/composition/rules/HeadroomRule.kt` — 共通契約模板參考實作
- `devspec/feature_record_Backlog.md` 最後一段 ADR — 共通契約三原則 + 驗證紀錄

## 不需帶入的 Context

- SV-D2-T1-FIX-01~03 診斷過程（座標漂移、浮點誤差、防呆細節已閉環）
- ThirdsRule 的歷史修復迭代（只有最終結果重要）
- HeadroomRule 的浮點 bug 調試過程（已在測試中固化為正確構造方式）
- D0 / D1 所有手術細節（已完全關閉）
- 本次對話中的 /qa /sv 迭代輸出
