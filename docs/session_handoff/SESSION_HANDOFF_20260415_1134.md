# Cueshot 工作交接 — 20260415_1134

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `presentation/camera/components/HaloGuidanceLayer.kt` | `HaloGuidanceLayer()` | **新建**：GazeRule 琥珀金圓環 + EyeLineRule 虛線輔助線；remember/LaunchedEffect 置頂（slot-table 穩定性）；null/isAllAligned 雙重早返防禦；10fps 節流；TAG_HALO_AMBER/GREEN/HIDDEN 語意探針 |
| `presentation/camera/CameraState.kt` | data class 欄位 | 新增 `detectedFaces: List<FaceLandmarkData> = emptyList()`（D6-B2-T2 接線必要欄位） |
| `presentation/camera/CameraViewModel.kt` | `observeCompositionAdvice` pipeline | `_state.update` 同步更新 `detectedFaces = landmarkFaces`（與 compositionResult 鎖步） |
| `presentation/camera/CameraScreen.kt` | overlay 堆疊 | 插入 `HaloGuidanceLayer(faces = state.detectedFaces, compositionResult = state.compositionResult)` 於 EdgeWarmthLayer 之上 |
| `presentation/camera/overlay/HaloIndicator.kt` | KDoc | 移除過期 TODO，標注構圖 Halo 已由 HaloGuidanceLayer(D6-B2-T2) 負責 |
| `presentation/utils/NormalizedToPixelTransformer.kt` | 全檔 | **前次 Session 新建**（D6-B2-T1）：純數學座標橋樑；本次 T2 中引用 |

**本次 Session Commits（依序）**：
- `4d86fba` feat(presentation): implement HaloGuidanceLayer with 10fps throttling and slot-table stability (D6-B2-T2)
- `5b6d1ba` docs(progress): D6-B2-T2 + T2.1 finalize — HaloGuidanceLayer QA/SV pass, Traycer fixes

## 當前阻塞點

無

## 下一步待辦

1. **[D6-B2-T3] 新建 `HaloGuidanceLayerTest.kt`（androidTest）**：
   - 路徑：`app/src/androidTest/java/com/akirachang/ai/photographer/presentation/camera/components/HaloGuidanceLayerTest.kt`
   - 三條測試路徑：
     - `compositionResult = null` → 驗證 `TAG_HALO_HIDDEN` 存在於 composition tree
     - `isAllAligned = false` + `ruleScores["gaze"]?.isAligned == false` → 驗證 `TAG_HALO_AMBER` Canvas 存在
     - `isAllAligned = true` → 驗證 `TAG_HALO_GREEN` Canvas 存在（且 AMBER 不存在）
   - 架構參考：`androidTest/.../EdgeWarmthLayerTest.kt`（testTag 探針模式）
   - 需構造 `FaceLandmarkData`（`android.graphics.RectF` + `PointF`）和 `CompositionResult`

2. **[D6-B2 完成後]** `/qa` → `/sv` → commit → Traycer 終審

3. **[D6 KAIROS #2]** D6-B2-T3 完成後執行 `/dream`（預定 2026-04-16）

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `remember` + `LaunchedEffect` 必須置於所有 conditional return 之前 | Compose slot-table 規則：每次 recomposition 的 composable call 數必須一致，否則 null→非null 切換時觸發 IllegalStateException | `HaloGuidanceLayer.kt` |
| T2.1（Traycer 修復）：`LaunchedEffect` 從 `isAllAligned` early return 之後移至 `remember` 之後 | 消除 `isAllAligned=true → false` 狀態切換時的 100ms 首幀遺失；節流邏輯在被抑制期間仍持續監控數據流 | `HaloGuidanceLayer.kt` |
| `CameraState.detectedFaces` 欄位由 ViewModel 與 `compositionResult` 鎖步更新 | 保持 UDF 純潔：UI 不直接觸碰 ML Kit 原始 Face 型別；且保證 faces 與 ruleScores 永遠屬於同一偵測幀 | `CameraState.kt`、`CameraViewModel.kt` |
| `TAG_HALO_GREEN` Canvas 為空（intentionally empty）| `isAllAligned=true` 時視覺回饋由 EdgeWarmthLayer 呼吸漸層 + ShutterRing 負責；HaloGuidanceLayer 只留 testTag 探針供儀表板測試使用 | `HaloGuidanceLayer.kt` |

## 需帶入新對話的 Context

- `CLAUDE.md`（最高憲法）
- `PROGRESS.md`（D6 Tickets：B2-T1/T2 ✅ DONE，B2-T3 🔲 TODO）
- `presentation/camera/components/HaloGuidanceLayer.kt`（T3 測試目標）
- `androidTest/.../EdgeWarmthLayerTest.kt`（T3 架構參考：testTag 探針模式）
- `domain/model/CompositionResult.kt`（`RuleScore` + `isAllAligned` 結構）
- `domain/model/FaceLandmarkData.kt`（`boundingBoxNormalized: RectF`、眼部 `PointF?`）
- `presentation/utils/NormalizedToPixelTransformer.kt`（T3 驗收理解用）

## 不需帶入的 Context

- D6-B2-T2 實作過程（Compose slot-table 問題、LaunchedEffect 首次錯置除錯）
- SV 自動修復 SV-D6-B2-T2-FIX-01（已 commit 並封板）
- T2.1 Traycer 修復過程討論
- `settings.local.json` 權限白名單擴充（`gradlew *`、`tail *` 已加入，議題已結）
- `/qa`、`/sv` 執行細節
- 原子化 commit 執行過程
