# Cueshot 工作交接 — 20260414_2133

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `androidTest/.../composition/LandmarkProbeTest.kt` | 全新建立 | D6-B1-T1.5 實體探針：ML Kit LANDMARK_MODE_ALL；3 張測試圖；bounding box `coerceIn(0f,1f)`；`targetContext` 讀取 debug assets；3 instrumented tests 全數通過 (Pixel_5 AVD) |
| `domain/composition/rules/GazeRule.kt` | `evaluate()` / KDoc / companion | D6-B1-T1.6 Fail-safe：`abs(eulerY) > EXTREME_YAW_DEG(45f)` → early-return `score=100f, isAligned=true`；KDoc 更新為正確語意（skip signal，非低分懲罰）|
| `test/.../GazeRuleTest.kt` | `yaw 90°` 斷言 / 新增 2 tests | `yaw 90°` 改為期待 `isAligned=true`；新增 `extreme yaw 60°` / `extreme yaw -60°` fail-safe 測試 |
| `androidTest/.../composition/LandmarkProbeTest.kt` | `testExtremeSideFace_60deg` | GazeRule 斷言從 `assertFalse/score=0f` → `assertTrue/score=100f` |
| `app/src/debug/assets/test_images/` | 新增 3 張圖 | `portrait_front_0deg.jpg`, `portrait_side_30deg.jpg`, `portrait_extreme_side_60deg.jpg` 納入版控 |
| `tools/prompts_v2_faces.txt` | 納管 | 測試圖生成 Prompt 資產納入 git 版控 |
| `.claude/PROGRESS.md` | KAIROS #2 / Sprint Health | 新增 2 項 KAIROS #2 debt items；GazeRule: 14→16 pass；D6-B1-T1.5/T1.6 Ticket 補登 |
| `devspec/feature_record_Backlog.md` | 末尾 | 新增 `[D6-TECH-DEBT-02]` Gradle Configuration Cache 警告登記 |

**本次 Session Commits（依序）**：
- `e1e3860` fix(domain): D6-B1-T1.6 GazeRule extreme-yaw fail-safe + LandmarkProbeTest D6_PROBE
- `d926bde` docs(progress): D6-B1 admin wrap-up — tech debt registration + KAIROS #2 items

## 當前阻塞點

無

## 下一步待辦

1. **[D6-B2-T1] 新建 `HaloGuidanceLayer.kt`**：
   - 路徑：`app/src/main/java/.../presentation/camera/overlay/HaloGuidanceLayer.kt`
   - 規格：統一圓形 Halo；`#D4A030` 琥珀金（`isAligned=false`）/ `#50C864` 綠色（`isAligned=true`）
   - 輸入參數：`compositionResult: CompositionResult?`（`isAllAligned` 欄位）
   - 架構參考：`EdgeWarmthLayer.kt`（浮動光點模式）
   - None/Empty 狀態：`compositionResult == null` → 完全透明，禁止畫預設色

2. **[D6-B2-T2] 接線至 `CameraScreen.kt`**：
   - 在 overlay 堆棧中插入 `HaloGuidanceLayer`（`EdgeWarmthLayer` 之上或之下，參考 Z-order）
   - 傳入 `state.compositionResult`
   - 從 `CameraState.kt` 確認 `compositionResult: CompositionResult?` 欄位存在

3. **[D6-B2-T3] 新建 `HaloGuidanceLayerTest`**（androidTest）：
   - 驗證 `isAligned=false` → 琥珀金渲染路徑（testTag 探針）
   - 驗證 `isAligned=true` → 綠色渲染路徑
   - 驗證 `compositionResult=null` → 無渲染

4. **[D6-B2 完成後] 執行 `/qa` → `/sv` → commit → 呼叫 Traycer 終審**

5. **[D6 KAIROS #2] 待 D6-B2 完成後執行 `/dream`（預定 2026-04-16）**：
   - D6_PROBE 數據已備妥（front=97.76, side=7.42, extreme=100.0/fail-safe）
   - 處理 `D5-TECH-DEBT-01`（Compose Test 基礎設施）
   - 處理 `SSOT-TYPE-DRIFT-01`（RuleScore vs RuleResult 命名）
   - 處理 `D6-TECH-DEBT-02`（generateTestImages ExecOperations 重構）

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| 極端角度 `abs(eulerY) > 45f` → `isAligned=true`（Fail-safe，非 0 分懲罰） | `isAligned=false` + `score=0f` 會觸發 PriorityEngine 優先推出「需修正」指令，但被攝者根本無法配合；正確語意是「跳過此規則」而非「此構圖有缺陷」 | `GazeRule.kt`、`GazeRuleTest.kt`、`LandmarkProbeTest.kt` |
| `EXTREME_YAW_DEG` 與 `MAX_YAW_DEG` 值相同（均 45f）但語意分離 | `MAX_YAW_DEG` = 線性公式分母（score 到 0 的邊界）；`EXTREME_YAW_DEG` = skip gate（超過此值進入 fail-safe）；兩者數值碰巧一致，但未來可能需要獨立調整（如 `EXTREME_YAW_DEG=50f`） | `GazeRule.kt` companion object |
| 測試圖資產使用 `targetContext`（非 `instrumentation().context`） | debug assets 打包在 app APK，不在 test APK；`instrumentation().context` 讀不到 `test_images/` | `LandmarkProbeTest.kt` |
| `portrait_side_30deg.jpg` 實際 EulerY ≈ 41.7°（非標籤 30°） | ML Kit 透視測量值，相機焦距導致角度偏大；屬物理正常偏差，不修正標籤 | `LandmarkProbeTest.kt` 備註 |

## D6_PROBE 實測數據（供 KAIROS #2 參考）

| 圖片 | ML Kit EulerY | GazeRule Score | isAligned |
|------|--------------|---------------|-----------|
| `portrait_front_0deg.jpg` | -1.0° | 97.76f | true |
| `portrait_side_30deg.jpg` | 41.7° | 7.42f | false |
| `portrait_extreme_side_60deg.jpg` | 59.9° | 100.0f | **true (fail-safe)** |

## 需帶入新對話的 Context

- `CLAUDE.md`（最高憲法，語言紀律 Rule 8）
- `PROGRESS.md`（D6 Tickets：B1 全數 DONE，B2 TODO）
- `presentation/camera/overlay/EdgeWarmthLayer.kt`（HaloGuidanceLayer 架構參考）
- `presentation/camera/CameraScreen.kt`（HaloGuidanceLayer 接線目標）
- `presentation/camera/CameraState.kt`（`compositionResult` 欄位）
- `domain/model/CompositionResult.kt`（`isAllAligned` 欄位定義）
- `domain/composition/rules/GazeRule.kt`（Fail-safe 決策已封板，僅供參考）

## 不需帶入的 Context

- D6-B1-T1.5 LandmarkProbeTest 除錯過程（targetContext vs context、coerceIn 修正）
- D6-B1-T1.6 Fail-safe 推導過程（已 commit 封板）
- D6-B1-FIX 行政收尾討論
- 本 Session /qa、/sv 執行過程
- tools/prompts_v2_faces.txt 納管討論
