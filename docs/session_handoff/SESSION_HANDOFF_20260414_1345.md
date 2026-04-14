# Cueshot 工作交接 — 20260414_1345

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `domain/model/FaceLandmarkData.kt` | 全新建立 | Domain DTO：`boundingBoxNormalized: RectF`、`leftEyeNormalized: PointF?`、`rightEyeNormalized: PointF?`、`headEulerAngleY: Float`；所有欄位正規化至 [0,1] |
| `domain/composition/CompositionRule.kt` | `evaluate()` 簽名 | `List<RectF>` → `List<FaceLandmarkData>`；移除 `import android.graphics.RectF`；新增 `import FaceLandmarkData` |
| `domain/composition/rules/ThirdsRule.kt` | `evaluate()` 實作 | 參數改為 `List<FaceLandmarkData>`；所有幾何操作改讀 `face.boundingBoxNormalized`；移除 `import RectF` |
| `domain/composition/rules/HeadroomRule.kt` | `evaluate()` 實作 | 同 ThirdsRule 機械性修復；`primary.boundingBoxNormalized.top` 等 |
| `domain/usecase/AnalyzeCompositionUseCase.kt` | `invoke()` 參數 | `faces: List<RectF>` → `faces: List<FaceLandmarkData>`；移除 `import android.graphics.RectF`；新增 `import FaceLandmarkData` |
| `presentation/camera/CameraViewModel.kt` | `observeCompositionAdvice()` | `normalizedFaces` → `landmarkFaces`（`mapIndexed`）；從 ML Kit `Face` 提取 LEFT_EYE / RIGHT_EYE 正規化座標 + `headEulerAngleY`；組裝 `FaceLandmarkData`；idx==0 輸出 `D6_PROBE` Log |
| `test/.../ThirdsRuleTest.kt` | 全部 evaluate 呼叫 | 加入 `RectF.asLandmark()` helper；所有 `listOf(face)` → `listOf(face.asLandmark())` |
| `test/.../HeadroomRuleTest.kt` | 全部 evaluate 呼叫 | 同 ThirdsRuleTest 模式 |
| `test/.../AnalyzeCompositionUseCaseTest.kt` | D2 Contract Test | `listOf(RectF(...))` → `listOf(FaceLandmarkData(boundingBoxNormalized=RectF(...), ...))` |
| `.claude/PROGRESS.md` | D6 Tickets 表格 + header | D6-B1-T1 ✅ DONE；新增 D6 Tickets 區塊（B1-T2/T3 TODO）；Unit Tests 133t/85pass |

**⚠️ 未 commit 的變更（git status 仍有 working tree 修改）**：
- 所有上述 `.kt` 檔案尚未 commit
- `PROGRESS.md` 尚未 commit
- 需遵循鐵律 2 拆分為 Commit 1（原始碼）、Commit 2（Test）、Commit 3（Docs）

**本 Session 已 commit 的前置工作（D5/D6-Batch0）**：
- `8e6a547` feat(domain): wire PriorityEngine into AnalyzeCompositionUseCase (D5-B2-T3)
- `58b9793` docs(arch): finalize D5 milestone, archive domain wiring ADRs
- `c2307da` docs(arch): register SSOT type drift technical debt for KAIROS #2

## 當前阻塞點

無

## 下一步待辦

1. **[立即] git commit — 原始碼（鐵律 2 Commit 1）**：
   ```
   git add app/src/main/java/.../FaceLandmarkData.kt \
           app/src/main/java/.../CompositionRule.kt \
           app/src/main/java/.../ThirdsRule.kt \
           app/src/main/java/.../HeadroomRule.kt \
           app/src/main/java/.../AnalyzeCompositionUseCase.kt \
           app/src/main/java/.../CameraViewModel.kt
   git commit -m "feat(domain): introduce FaceLandmarkData DTO and evolve CompositionRule interface (D6-B1-T1)"
   ```

2. **[立即] git commit — Test（鐵律 2 Commit 2）**：
   ```
   git add app/src/test/.../ThirdsRuleTest.kt \
           app/src/test/.../HeadroomRuleTest.kt \
           app/src/test/.../AnalyzeCompositionUseCaseTest.kt
   git commit -m "test(domain): migrate rule tests to FaceLandmarkData interface (D6-B1-T1)"
   ```

3. **[立即] git commit — Docs（鐵律 2 Commit 3）**：
   ```
   git add .claude/PROGRESS.md
   git commit -m "docs(progress): D6-B1-T1 DONE, add D6 tickets table"
   ```

4. **[D6-B1-T2] 實作 `EyeLineRule`**：
   - 新建 `domain/composition/rules/EyeLineRule.kt`
   - 讀取 `FaceLandmarkData.leftEyeNormalized` / `rightEyeNormalized`
   - 計算眼線水平度（roll 方向偏差）
   - 在 `di/CompositionModule.kt` 加入 `@Binds @IntoSet`
   - 新增 `EyeLineRuleTest.kt`

5. **[D6-B1-T3] 實作 `GazeRule`**：
   - 新建 `domain/composition/rules/GazeRule.kt`
   - 讀取 `FaceLandmarkData.headEulerAngleY`
   - 計算 gaze 偏離中心的程度
   - 在 `di/CompositionModule.kt` 加入
   - 新增 `GazeRuleTest.kt`

6. **[D6 KAIROS #2] 執行 `/dream`（日期：2026-04-16）**：
   - 執行前先觀測 D6_PROBE Logcat 數據並記錄
   - 處理 `D5-TECH-DEBT-01` Compose Test 基礎設施升級

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `CompositionRule.evaluate()` 介面直接升級為 `List<FaceLandmarkData>`（破壞性變更） | 而非在 UseCase 內做雙重映射，避免引入無意義的中間轉換層 | `CompositionRule.kt`、所有 Rule 實作、所有測試 |
| ThirdsRule/HeadroomRule 讀取 `face.boundingBoxNormalized`，業務邏輯不變 | 機械性替換，確保回歸測試零失敗 | `ThirdsRule.kt`、`HeadroomRule.kt` |
| `FaceLandmarkData` 暫用 `android.graphics.RectF/PointF` | 與現有 Rule 同等級技術債；SSOT-TYPE-DRIFT-01 已登記，KAIROS #2 統一處理 | `FaceLandmarkData.kt`（技術債） |
| 探針僅輸出 `idx == 0`（第一張人臉） | 避免多人臉場景 Log 洪水 | `CameraViewModel.kt` |
| 測試中加入 `RectF.asLandmark()` extension helper | 最小改動量完成 17+12 個測試的轉換，不引入 `FaceLandmarkData` 建構子重複 | `ThirdsRuleTest.kt`、`HeadroomRuleTest.kt` |

## 需帶入新對話的 Context

- `CLAUDE.md`（規則 #10 Bash safety、KAIROS #11/#12）
- `PROGRESS.md`（D6-B1-T1 DONE，B1-T2/T3 待實作）
- `domain/model/FaceLandmarkData.kt`（D6-B1-T2/T3 的輸入型別）
- `domain/composition/CompositionRule.kt`（介面契約）
- `di/CompositionModule.kt`（新 Rule 需加入的 DI 模組）
- `domain/composition/rules/ThirdsRule.kt`（參考已有 Rule 的實作模式）

## 不需帶入的 Context

- D6-B1-T1 實作過程（FaceLandmarkData 建立、ThirdsRule/HeadroomRule 機械性修復）
- 所有 `/qa`、`/sv` 驗收過程（全 PASS）
- D5-B1/B2 全部過程（已封板歸檔，commit 均已完成）
- D6-Batch0 文件登記過程（已 commit `c2307da`）
- Session token / `/status` / `/compact` 討論
- 架構衝突分析過程（探針位置討論、兩個版本的 Task 迭代過程）
