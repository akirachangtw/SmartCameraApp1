# Cueshot 工作交接 — 2026-04-17 15:24

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `domain/composition/PtfOffset.kt` | data class | 新增純 Kotlin normalized [0,1] 位移結構，含 `ZERO` sentinel |
| `domain/composition/PtfRuleName.kt` | enum | 新增 `FRAMING / ANGLE / LEVEL` 規則標籤（優先級排序） |
| `domain/composition/PtfAlignmentResult.kt` | data class | 新增三欄聚合結果（isAligned, offset, failedRules），含 `NO_FACE` sentinel |
| `domain/composition/PtfRuleAggregator.kt` | `aggregate()` | 新增 AND-Gate 聚合器：歐氏距離 framing、非對稱 pitch [-15°,25°]、roll <8°；純函數無狀態 |
| `domain/composition/rules/PtfFramingRule.kt` | `evaluate()` | 新增 Framing 規則物件：Euclidean dist < 0.12 |
| `domain/composition/rules/PtfAngleRule.kt` | `evaluate()` | 新增 Angle 規則物件：pitch ∈ [-15f, 25f]（含邊界） |
| `domain/composition/rules/PtfLevelRule.kt` | `evaluate()` | 新增 Level 規則物件：abs(roll) < 8f（不含邊界） |
| `di/CompositionModule.kt` | `providePtfRuleAggregator()` | 新增 `@PtfEngine` qualifier + `@Provides @Singleton`；Normal 模式 Set 完全隔離 |
| `test/.../PtfRuleAggregatorTest.kt` | 全部 | 新增 10 tests：AND-Gate / null face / 單規則失敗 / 三規則失敗 / offset 方向 / pitch 邊界 |
| `test/.../rules/PtfAngleRuleTest.kt` | 全部 | 新增 8 tests：含邊界 ±0.1f 正負向 |
| `test/.../rules/PtfFramingRuleTest.kt` | 全部 | 新增 7 tests：含 threshold 邊界 |
| `test/.../rules/PtfLevelRuleTest.kt` | 全部 | 新增 7 tests：含邊界 8f exact |
| `plans/architecture/D8_B1_Architecture_Memo.md` | 全部 | 新增架構決策備忘錄（隔離策略/座標系/EMA/DI） |

## Git Commits（本 Session）

本次修改尚未 commit（working tree 有未追蹤/已修改檔案）。下一步需執行原子化提交：

```
Commit 1 [Feature]: D8-B1-T1+T2 PTF rule engine domain layer + DI wiring
Commit 2 [Docs]:    D8-B1 architecture memo + session handoff
```

## 當前阻塞點

無

## 下一步待辦

### 立即執行（開新 Session 後）
1. **Git commit**：原子化提交兩筆（code + docs 分離，鐵律 2）
2. **更新 PROGRESS.md**：新增 D8-B1-T1+T2 ✅ DONE 條目；unit tests 224t/0failed

### D8-B1-T3（下一個 Ticket）：ViewModel 接線
3. 在 `CameraViewModel.kt` 注入 `@PtfEngine PtfRuleAggregator`
4. 在 `observeCompositionAdvice()` 或新增 `observePtfAlignment()` flow，當 `cameraMode == CameraMode.PassToFriend` 時呼叫 `aggregator.aggregate()`
5. 在 `CameraState.kt` 新增 `ptfAlignmentResult: PtfAlignmentResult` 欄位（預設 `NO_FACE`）
6. EMA(α=0.3) 需在 ViewModel 映射層對 face bbox 做平滑，再傳入 aggregator（不在 aggregator 內部做）

### D8-B2（Halo 牽引 UI）
7. 確認 `HaloGuidanceLayer` 共用策略 vs 新建 `PtfHaloLayer`（需在 D8-B2 /normal 對齊）
8. UI 層將 `PtfOffset(dx, dy)` 乘以 viewSize 轉為 px，驅動 Halo 引力場動畫

## 關鍵決策（本次對話中做出的）

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `PtfOffset` 用純 Kotlin data class，normalized [0,1] | Domain 層零 Compose SDK 依賴（Rule 13）；CSO 裁定，SSOT 補丁 §3.3/§3.4 同步修訂 | `PtfAlignmentResult.kt`、`HaloGuidanceLayer`（UI 層做 px 轉換） |
| Framing 用歐氏距離（非 abs(dx) \|\| abs(dy)）| SSOT 補丁 §3.3 規格；圓形容忍區語意正確 | `PtfRuleAggregator.kt`、`PtfFramingRule.kt` |
| Angle 規則為裝置 pitch（非 headEulerAngleY）| `FaceLandmarkData` 只有 yaw；PTF 姿態判斷需裝置 pitch（IMU） | `PtfRuleAggregator.aggregate()` 新增 `devicePitch` 參數 |
| `@PtfEngine` Qualifier + `@Provides`（非 `@Binds @IntoSet`）| 避免 Normal 模式 `Set<CompositionRule>` 被汙染 | `CompositionModule.kt`、`CameraViewModel.kt`（注入點需加 qualifier） |
| EMA 解耦至 ViewModel 上游 | Aggregator 保持純函數，無狀態，可直接 JVM 測試 | `CameraViewModel.kt`（D8-B1-T3 接線時實作） |

## SV 驗收結果

- 🔨 Compile: ✅ PASS
- 🔍 Lint: ✅ PASS (`:app:lintDeviceDebug` 0 new warnings)
- 📦 16KB ELF: ✅ ALIGNED
- 🧹 Orphans: ✅ CLEAN
- 📐 SSOT: ✅ ALIGNED（補丁 §3.3/§3.4 全對齊）
- 📊 Unit Tests: 224t / 0 failed（PTF 新增 32 tests）

## 需帶入新對話的 Context

- `.claude/CLAUDE.md` — 架構規則（特別 Rules 13、16）
- `.claude/PROGRESS.md` — 確認 D8-B1-T1+T2 後更新狀態
- `CLAUDE.local.md` — ADB device ID
- `plans/architecture/D8_B1_Architecture_Memo.md` — D8-B1 架構決策（隔離策略、座標系、EMA）
- `plans/architecture/SSOT_v3.3_Section_4_5_D8_Patch.md` — PTF 規則引擎規格（修訂版）
- `presentation/camera/CameraViewModel.kt` — D8-B1-T3 注入目標
- `presentation/camera/CameraState.kt` — 需新增 `ptfAlignmentResult` 欄位

## 不需帶入的 Context

- D8-B1 /normal 架構對齊討論過程（決策已固化於備忘錄）
- SSOT 補丁修訂討論（`offset` 格式、`@PtfEngine` 命名已拍板）
- `PtfOffset` vs `androidx.compose.Offset` 的辯論（已裁定 normalized [0,1]）
- SV 三階段執行細節（已通過，結論已記錄）
- D7 所有封板內容（已 push 至 GitHub，無需重讀）
