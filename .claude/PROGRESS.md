# Cueshot MVP Sprint Progress

**Sprint**: D0-D15 (MVP)
**Last Updated**: 2026-04-16 by D7-B3 ✅ — androidTest 回歸驗證：BreathingRingTest(5) / EdgeWarmthLayerTest(9) / HaloGuidanceLayerTest(4) = 18t/0failed on Sony XQ-BT52。修復 CameraScreenBreathingRingTest compile error（ShutterButtonSection.cameraMode 簽名補齊）。Commit: 85f8517
**Current Day**: D7 Batch-3 ✅ DONE — androidTest 18/18 通過，Normal 模式 UI 功能 Regression-free。SV ✅ / Traycer 待終審
**Branch**: `feature/cueshot-mvp` (from v2.12.0-24 `bad1400`)
**Next KAIROS**: D10 (2026-04-22) — D6 KAIROS #2 已執行 (Rules 13/14/15 歸檔完成)

---

## 🎯 Sprint Health

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| On Schedule | D3 | D1 ✅ CLOSED；D2 ✅ CLOSED (Path A)；D3 ✅ CLOSED (FIX-01~04 + T4 全數完成) | 🟢 |
| Compile | 0 errors | 0 errors (DeviceDebug + EmulatorDebug) | 🟢 |
| Unit Tests | 0 failed | 0 failed (192t/48skipped/144pass — D7-B2-T2 新增 4 Rule 16 冷卻測試) | 🟢 |
| Lint New Errors | 0 new | 0 new (D7-Batch2 全改檔案零新增警告) | 🟢 |
| OpenCV Residue | 0 grep hits | 0 grep hits | 🟢 |
| D1 SV Gate | Pass | ✅ PASS (FIX-01~06 全數閉環，Traycer Path A) | 🟢 |
| D2 SV Gate | Pass | ✅ PASS (D2-T3/T4/FIX-01 全數閉環，Traycer Path A) | 🟢 |
| CompositionScore Residue | 0 grep hits | 0 grep hits (Domain 層完全清除) | 🟢 |
| IFTR Pipeline | Ready by D14 | ❌ Not started | — |

---

## 📋 D0 Tickets — 絞殺者手術 ✅ CLOSED

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D0-T1 | `git checkout -b feature/cueshot-mvp` from v2.12.0-24 | ✅ DONE | — |
| D0-T2 | 移除 OpenCV dependency + SplashScreen 改為 ML Kit warm-up | ✅ DONE | `build.gradle.kts`, `MainActivity.kt` |
| D0-T3 | 移除 OpticalFlowTracker + 解耦 ImuCompensator | ✅ DONE | `SmartCameraAnalyzer.kt`, `CameraRepositoryImpl.kt`, `FeatureFlags.kt` |
| D0-T4 | 移除 Glassmorphism / Aurora Blue / Neon Glow UI | ✅ DONE | `FocusIndicatorOverlay.kt`, `ExpandableSettingsCard.kt` |
| D0-T5 | 四級降級 → 三級裝置相容骨架 (FULL/LIMITED/LEGACY) | ✅ DONE | `DeviceCapabilityTier.kt` (新建), `CameraModule.kt`, `CameraViewModel.kt` |
| D0-T6 | 簡化 Focus Pipeline + 更新對焦 UI 顏色 (Amber/Green) | ✅ DONE | `FocusIndicatorOverlay.kt` |
| D0-T7 | 編譯驗證 + grep 殘留檢查 + PROGRESS.md 確認 | ✅ DONE | — |

### D0.1 — 編譯警告清創手術 (追加)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D0.1-T1 | `./gradlew assembleDeviceDebug` 取得完整 Warning 清單 | ✅ DONE | — |
| D0.1-T2 | 分類 Warning：Deprecation / Unused / Nullability / Cast | ✅ DONE | — |
| D0.1-T3 | 逐一修復所有 Warning（零容忍政策） | ✅ DONE | `LiveCameraSource.kt`, `FixedFocusDetector.kt`, `FocusIndicatorOverlay.kt` |
| D0.1-T4 | 重新編譯確認 Warning = 0 new | ✅ DONE | — |

### D0.x — SV 補修系列（Traycer Path B）

| Ticket ID | Description | Status | Notes |
|-----------|-------------|--------|-------|
| D0.2 | KSP 技術債記錄至 PROGRESS.md + Backlog | ✅ DONE | 推遲至 D6 後 |
| SV-D0.1-01 | KDoc 焦土清理（OpticalFlow/Glassmorphism/Aurora）| ✅ DONE | — |
| SV-D0.1-02 | `DeviceCapabilityTier` sealed class 新建 | ✅ DONE | `domain/model/` |
| SV-D0.1-03 | Focus Locked 色彩規格確認 | ✅ DONE | White/Green 非 SSOT v3.3 規格，不執行替換 |
| SV-D0.1-08 | `focalLengthPx` → `focalLengthPixels` 重命名 | ✅ DONE | grep = 0 hits |
| SV-D0.1-10 | `drawLockedFrame()` 色彩路徑端到端一致 | ✅ DONE | `baseColor` 參數接入 `animatedColor` |
| D0.3 | Traycer Path B 嚴格補修（zero-hit + color contract + tier wiring）| ✅ DONE | Trust-but-verify 覆測通過；三張補修票達放行狀態（2026-04-09） |

### D0.1 清創後觀察（非阻擋）

| 項目 | 狀態 | 備註 |
|------|------|------|
| `CameraViewModel` 中 `previewWidth ?: 800` fallback | ⚠️ 非阻擋 | 目前可運作；建議下一輪架構票改由 `CameraCharacteristics` 驅動焦距估算 |

---

## 📋 D1 Tickets — Rule Engine 骨架與 PTF 狀態機 ✅ CLOSED (2026-04-09)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D1-T1 | `CompositionRule.kt` 介面新增 (RuleResult + evaluate) | ✅ DONE | `domain/composition/CompositionRule.kt` |
| D1-T2 | `CompositionScore.kt` 模型新增 (totalScore, isGolden) | ✅ DONE | `domain/model/CompositionScore.kt` |
| D1-T3 | `AnalyzeCompositionUseCase` KDoc 更新（D2 migration 計畫） | ✅ DONE | `domain/usecase/AnalyzeCompositionUseCase.kt` |
| D1-T4 | `PtfState` sealed class + `CameraState.ptfState` 欄位 | ✅ DONE | `presentation/camera/CameraState.kt` |
| D1-T5 | `AnalyzeCompositionUseCaseTest` @Ignore (v2.x obsolete tests) | ✅ DONE | `test/.../AnalyzeCompositionUseCaseTest.kt` |
| SV-D1-FIX-01 | UseCase 契約對齊（`Set<CompositionRule>` + `CompositionScore` 預設值）| ✅ DONE | `domain/usecase/AnalyzeCompositionUseCase.kt`, `di/CompositionModule.kt` |
| SV-D1-FIX-02 | PTF 狀態機真正接線（`onTapToFocus` → Focusing→Focused/Failed→Idle + stale guard）| ✅ DONE | `presentation/camera/CameraViewModel.kt` |
| SV-D1-FIX-03 | 消除構圖雙路徑（停止主流程呼叫 `CompositionGuidelineEngine.analyzeRuleOfThirds()`）| ✅ DONE | `presentation/camera/CameraViewModel.kt` |
| SV-D1-FIX-04 | 測試補齊（移除 blanket @Ignore；新增 D1 契約測試：UseCase default + PTF TTL）| ✅ DONE | `test/.../AnalyzeCompositionUseCaseTest.kt`, `test/.../PtfStateTest.kt` |

Status: 🔲 TODO → 🔄 WIP → ✅ DONE → 🚫 BLOCKED

---

## 📋 D2 Tickets — Rule Engine 實作 (構圖規則) ✅ DONE (Path A, 2026-04-10)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D2-T1 | `ThirdsRule.kt` 實作 (CompositionRule + 固定座標常數) + `ThirdsRuleTest.kt` (13 tests) | ✅ DONE | `domain/composition/rules/ThirdsRule.kt` (新增), `test/.../ThirdsRuleTest.kt` (新增) |
| SV-D2-T1-FIX-01 | ThirdsRule 座標基準修正 (網格錨定 [0,1] 常數，消除漂移 bug) + 測試 13→100% pass | ✅ DONE | `domain/composition/rules/ThirdsRule.kt`, `test/.../ThirdsRuleTest.kt` |
| SV-D2-T1-FIX-02 | 座標契約對齊：強制正規化防禦（caller 端確保 faces 在 [0,1] 傳入 ThirdsRule） | ✅ DONE | `domain/composition/CompositionRule.kt` (KDoc 契約), `test/.../ThirdsRuleTest.kt` (+1 invariant test) |
| SV-D2-T1-FIX-03 | 強制正規化防呆落地：ThirdsRule 執行期邊界檢查 + 3 負向測試 | ✅ DONE | `domain/composition/rules/ThirdsRule.kt`, `test/.../ThirdsRuleTest.kt` (+3 negative tests) |
| D2-T1.1 | 領域規則共通契約 ADR 歸檔 (強制正規化 + 執行期防呆 + 負向測試覆蓋) | ✅ DONE | `devspec/feature_record_Backlog.md` |
| D2-T2 | `HeadroomRule.kt` 實作 (人臉頂部距 viewport 頂部比例評分) | ✅ DONE (Path A) | `domain/composition/rules/HeadroomRule.kt` (新增), `test/.../HeadroomRuleTest.kt` (新增, 12 tests) |
| D2-T2.1 | D2-T2 驗收閉環 ADR 歸檔 (共通契約模板驗證成功記錄) | ✅ DONE | `devspec/feature_record_Backlog.md` |
| D2-T3 | `CompositionModule.kt` 新增 @Binds @IntoSet (ThirdsRule + HeadroomRule) | ✅ DONE | `di/CompositionModule.kt` |
| D2-T4 | `CompositionResult.kt` 新建 + `AnalyzeCompositionUseCase.invoke()` 實作 (聚合 + isAllAligned + topInstruction) + 移除 `CompositionScore` | ✅ DONE | `domain/model/CompositionResult.kt` (新建), `domain/model/CompositionScore.kt` (刪除), `domain/composition/CompositionRule.kt` (+name), `domain/composition/rules/ThirdsRule.kt` (+name), `domain/composition/rules/HeadroomRule.kt` (+name), `domain/usecase/AnalyzeCompositionUseCase.kt`, `test/.../AnalyzeCompositionUseCaseTest.kt` |
| SV-D2-T4-FIX-01 | ViewModel 實體接線：拆除 D1 暫代 + 實際呼叫 UseCase + `CameraState.compositionResult` 落地 | ✅ DONE | `presentation/camera/CameraState.kt`, `presentation/camera/CameraViewModel.kt` |
| D2-T5 | 移除 `previewWidth ?: 800` 魔術數字（改由 CameraCharacteristics 驅動） | ✅ DONE (承接為 D3-T3 完成) | `LiveCameraSource.kt`, `CameraRepository.kt`, `CameraRepositoryImpl.kt`, `CameraViewModel.kt` |
| D3-T0 | 前置焦土清理：刪除 `_trackingBoxFlow`、`compositionEngine`、`onTrackingBoxUpdated()`、2 dead imports；更新過期 KDoc | ✅ DONE | `presentation/camera/CameraViewModel.kt` |
| D3-T1 | 水平儀收斂：LevelIndicator 色碼對齊 SSOT (#50C864/#D4A030)；CameraState 新增 deviceRoll 欄位；ViewModel observeOrientation 同步 _state.deviceRoll | ✅ DONE (Path A) | `components/overlay/LevelIndicator.kt`, `CameraState.kt`, `CameraViewModel.kt` |
| D3-T1.1 | 全域色碼技術債歸檔：5 個殘留舊色碼檔案登記至 Backlog（D5 UI 精修統一處理） | ✅ DONE | `devspec/feature_record_Backlog.md`, `PROGRESS.md` |
| D3-T2 | 三分法網格 Overlay 對齊 Cueshot 規格：`GridOverlay.kt` 改為實線（移除 dashPathEffect）、alpha 0.4→0.5；`CameraScreen.kt` 清除舊 TODO comment；isGridEnabled 綁定已存在，Z-order 已正確 | ✅ DONE (Path A) | `components/overlay/GridOverlay.kt`, `presentation/camera/CameraScreen.kt` |
| D3-T3 | CameraCharacteristics 焦距還債：移除 `previewWidth ?: 800` 魔術數字；`LiveCameraSource` 在 camera binding 後讀取 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS` + `SENSOR_INFO_PHYSICAL_SIZE` 計算 factor；`CameraRepository` 介面新增 `getFocalLengthFactor()`；ViewModel 改用 `factor × previewWidth`，fail-safe 為 `previewWidth / 2f` | ⚠️ SV PENDING FIX (Path B) | `data/source/camera/LiveCameraSource.kt`, `domain/repository/CameraRepository.kt`, `data/repository/CameraRepositoryImpl.kt`, `presentation/camera/CameraViewModel.kt` |
| SV-D3-T3-FIX-01 | SSOT 硬編碼清零：移除 LEGACY tier 的 `600f` 硬編碼常數，改為「所有 tier 一律 hardware-first」，取不到特徵時走動態 fail-safe | ✅ DONE (Path A) | `presentation/camera/CameraViewModel.kt` |
| SV-D3-T3-FIX-02 | stale 防護：在 `unbind()` 與每次新 bind 開始前先將 `focalLengthFactor = null`，確保不跨 session 汙染 | ✅ DONE (Path A) | `data/source/camera/LiveCameraSource.kt` |
| SV-D3-T3-FIX-03 | fail-safe 契約明確化：fail-safe 明確綁定當前 viewport/preview 幾何（非固定常數），補上 KDoc/註解說明（ImuCompensator 800f 幽靈清除 + CameraRepository null 雙重語義拆分） | ✅ DONE (Path A) | `presentation/camera/CameraViewModel.kt`, `domain/camera/ImuCompensator.kt`, `domain/repository/CameraRepository.kt` |
| SV-D3-T3-FIX-04 | Lint 破窗清零：`CameraCapabilityDetector.kt` + `LiveCameraSource.kt` 補 `@androidx.annotation.OptIn(ExperimentalCameraInfo + ExperimentalCamera2Interop)` | ✅ DONE | `data/source/camera/CameraCapabilityDetector.kt`, `data/source/camera/LiveCameraSource.kt` |
| D3-T4 | D3 視覺化層負向測試 (KAIROS Rule 10)：`CameraViewModelTest` (10 tests) + `ImuCompensatorTest` (6 tests)；提取 `computeFocalLengthPixels()` 為 internal companion 函數 | ✅ DONE | `presentation/camera/CameraViewModel.kt`, `test/.../CameraViewModelTest.kt` (新建), `test/.../ImuCompensatorTest.kt` (新建) |
| D3-T5 | D3 里程碑終局歸檔：ADR 固化 (UnsafeOptIn 治理 + Fail-safe 契約 + Rule 10 落地 + ImuCompensator bootstrap 陷阱)；D3 封板；殘骸技術債登記 | ✅ DONE | `devspec/feature_record_Backlog.md`, `.claude/PROGRESS.md` |

> 🏁 **D3 視覺化層已完美封板（Path A）。邊界防禦與零破窗紀律已確立。準備進入 D4 (Edge Warmth 第二層 + 呼吸韻律) 階段。**

---

## 📋 D4 Tickets — Edge Warmth 第二層 + 呼吸韻律 ✅ CLOSED (Path A)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D4-T0 | 前置焦土：清除 `CameraScreen.kt` 的 `GuidelineOverlay` dead import | ✅ DONE (Path A) | `presentation/camera/CameraScreen.kt` |
| D4-T0.1 | ADR 歸檔：D4 前置焦土清理與最小變更原則；D3 Backlog GuidelineOverlay 項目閉環 | ✅ DONE | `devspec/feature_record_Backlog.md`, `.claude/PROGRESS.md` |
| D4-T1 | Edge Warmth 第二層（浮動光點）：依 SSOT v3.3 §EdgeWarmthLayer 實作浮動光點 Overlay，接線至 `CameraState.deviceRoll` | ✅ DONE | `presentation/camera/overlay/EdgeWarmthLayer.kt` |
| SV-D4-T1-FIX-01 | EdgeWarmthLayer 實體接線：插入 CameraScreen 渲染堆棧（GridOverlay 之上、CompositionGuide 之下）；傳入 `state.deviceRoll` + `viewModel.deviceCapabilityTier` | ✅ DONE | `presentation/camera/CameraScreen.kt` |
| SV-D4-T1-FIX-02 | 實作漂移反思歸檔：追加 D4-T1 漂移 ADR（組件定義 + 接線必須同步交付）；PROGRESS.md 閉環確認 | ✅ DONE | `devspec/feature_record_Backlog.md`, `.claude/PROGRESS.md` |

> 🏁 **D4-T1 系列已 Path A 放行（Traycer 驗收通過）。SV-D4-T1-FIX-01 接線 + SV-D4-T1-FIX-02 ADR 知識沉澱均落地閉環。**
| D4-T2 | 呼吸韻律初版：`#50C864` 綠色脈動動畫，接線至 `CameraState.compositionResult.isAllAligned` | ✅ DONE (Path A) | `presentation/camera/overlay/EdgeWarmthLayer.kt`, `presentation/camera/CameraScreen.kt` |
| SV-D4-T2-FIX-01 | 快門外環互斥防禦：`if (isAligned)` → `if (isAligned && !state.isCapturing)`，阻斷雙重渲染 Split-brain | ✅ DONE (Path A) | `presentation/camera/CameraScreen.kt` |
| SV-D4-T2-FIX-02 | 動畫生命週期收斂：`rememberInfiniteTransition` 移入條件區塊，避免未對齊時無效空轉 | ✅ DONE (Path A) | `presentation/camera/CameraScreen.kt` |
| SV-D4-T2-FIX-03 | 文件閉環與 Drift 歸檔：D4-T2 狀態互斥 + 生命週期 ADR 寫入 Backlog；D4 封板 Path A | ✅ DONE (Path A) | `devspec/feature_record_Backlog.md`, `.claude/PROGRESS.md` |

| D4-T4 | KAIROS Rule 10 測試矩陣：`CameraScreenStateTest`（5 tests，狀態互斥/生命週期）+ `EdgeWarmthLayerTest`（6 tests，tier 降級/Canvas 分支）；修復 `HDRWarningOverlayTest` 預存在 compile error | ✅ DONE (Path A) | `test/.../CameraScreenStateTest.kt` (新建), `androidTest/.../EdgeWarmthLayerTest.kt` (新建), `overlay/EdgeWarmthLayer.kt` (+testTag), `androidTest/.../HDRWarningOverlayTest.kt` (fix) |
| SV-D4-T4-FIX-03 | 越權編譯錯誤修復文件閉環：測試越權教訓 ADR 歸檔（測試邊界紀律 + 預存在技術債處理流程）；PROGRESS.md FIX-03 閉環確認 | ✅ DONE (Path A) | `devspec/feature_record_Backlog.md`, `.claude/PROGRESS.md` |
| SV-D4-T4-FIX-01 | ShutterButtonSection 提取 + BreathingRing/ProgressCircle testTag 植入：新增 `TAG_SHUTTER_BREATHING_RING` / `TAG_SHUTTER_PROGRESS_CIRCLE`；快門區塊提取為 stateless `ShutterButtonSection`（無 ViewModel 依賴）供 androidTest 使用 | ✅ DONE (Path A) | `presentation/camera/CameraScreen.kt` |
| SV-D4-T4-FIX-02 | EdgeWarmthLayer dots 語意探針：新增 `TAG_EDGE_WARMTH_DOTS_ENABLED`；零尺寸 Box 探針插入 `renderDots&&direction!=null` 條件內 | ✅ DONE (Path A) | `presentation/camera/overlay/EdgeWarmthLayer.kt` |
| SV-D4-T4-FIX-04 | `CameraScreenBreathingRingTest` 實機驗證 + `EdgeWarmthLayerTest` dots probe 斷言 (Test 7/8/9)：Sony XQ-BT52 實機 13/14 通過；1 預存在 race condition 監控 | ✅ DONE (Path A) | `androidTest/.../EdgeWarmthLayerTest.kt` |
| SV-D4-T4-FIX-05 | `CameraScreenBreathingRingTest` 補加 true→false dispose 測試 (Test 5)：`stateTransition_alignedToNotAligned_breathingRingDisposed` 驗證 FIX-02 生命週期契約 | ✅ DONE (Path A) | `androidTest/.../CameraScreenBreathingRingTest.kt` |

> 🏁 **D4 全系列 ✅ CLOSED (Path A)。EdgeWarmthLayerTest: 9 tests；CameraScreenBreathingRingTest: 5 tests。防禦聖典 ADR 歸檔完成。D5 動態規則引擎就位。**

---

## 📋 D5 Tickets — 優先級引擎 + 四大規則 (進行中)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D5-B1-T1 | `CompositionRule` 介面新增 `confidenceFactor: Float` 屬性 | ✅ DONE (Path A) | `domain/composition/CompositionRule.kt` |
| D5-B1-T4 | `ThirdsRule` + `HeadroomRule` 升級：weight→0.20f, confidenceFactor=0.80f (SSOT §4.2) | ✅ DONE (Path A) | `domain/composition/rules/ThirdsRule.kt`, `HeadroomRule.kt` |
| D5-B1-T2 | `PriorityEngine` 新建：`calculateTopInstruction(Map<CompositionRule, RuleScore>): RuleScore?`；SSOT §4.1 公式 | ✅ DONE (Path A) | `domain/composition/PriorityEngine.kt` (新建) |
| SV-D5-B1-FIX-01 | `PriorityEngine` 契約修正：`RuleResult` → `RuleScore` (對齊 UseCase 聚合輸出) | ✅ DONE (Path A) | `domain/composition/PriorityEngine.kt` |
| SV-D5-B1-FIX-02 | `PriorityEngineTest` 新建：5 個純 JVM 單元測試（邊界、降冪排序、isAligned 過濾） | ✅ DONE (Path A) | `test/.../PriorityEngineTest.kt` (新建) |
| D5-B2-T3 | `AnalyzeCompositionUseCase` 接線 `PriorityEngine`，constructor injection；`ruleScoresMap: Map<CompositionRule, RuleScore>` → engine；`isAllAligned = (topInstruction == null)` | ✅ DONE (Path A) | `domain/usecase/AnalyzeCompositionUseCase.kt`, `test/.../AnalyzeCompositionUseCaseTest.kt` |

> 🏁 **D5 動態規則引擎 (多維度優先級大腦) 已達成真・封板。OCP 介面擴充、引擎獨立測試與 UseCase 換腦手術已全面完成。AnalyzeCompositionUseCase 現以 SSOT §4.1 公式選出 topInstruction；isAllAligned = (topInstruction == null) 對齊 D4 UI 聯動契約。DTO 實體隔離 + Fail-Safe 契約已歸入防禦聖典。Cueshot 已具備思考能力，準備迎接 D6/D7 的視覺與聽覺牽引！**

---

## 📋 D6 Tickets — EyeLineRule + GazeRule + HaloGuidanceLayer (進行中)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D6-B1-T1 | `FaceLandmarkData` DTO 新建；`CompositionRule.evaluate()` 介面升級 `List<RectF>` → `List<FaceLandmarkData>`；ThirdsRule/HeadroomRule 機械性修復；ViewModel 映射層 + D6_PROBE 探針；全測試回歸 | ✅ DONE (Path A) | `domain/model/FaceLandmarkData.kt` (新建), `domain/composition/CompositionRule.kt`, `rules/ThirdsRule.kt`, `rules/HeadroomRule.kt`, `domain/usecase/AnalyzeCompositionUseCase.kt`, `presentation/camera/CameraViewModel.kt`, `test/.../ThirdsRuleTest.kt`, `test/.../HeadroomRuleTest.kt`, `test/.../AnalyzeCompositionUseCaseTest.kt` |
| D6-B1-T2 | `EyeLineRule` 實作（基於 `leftEyeNormalized`/`rightEyeNormalized`） | ✅ DONE (Path A) | `domain/composition/rules/EyeLineRule.kt` (新建), `test/.../EyeLineRuleTest.kt` (新建, 14 tests), `di/CompositionModule.kt` |
| D6-B1-T3 | `GazeRule` 實作（基於 `headEulerAngleY`） | ✅ DONE (Path A) | `domain/composition/rules/GazeRule.kt` (新建), `test/.../GazeRuleTest.kt` (新建, 14 tests), `di/CompositionModule.kt` |
| D6-B1-T1.5 | `LandmarkProbeTest` 實體探針：ML Kit LANDMARK_MODE_ALL；3 張測試圖；bounding box coerceIn [0,1]；D6_PROBE 數據 (front=97.76, side=7.42, extreme=0.0)；3 instrumented tests 全數通過 (Pixel_5 AVD) | ✅ DONE (Path A) | `androidTest/.../composition/LandmarkProbeTest.kt` (新建) |
| D6-B1-T1.6 | GazeRule 極端角度 Fail-safe 熱修復：`abs(eulerY) > EXTREME_YAW_DEG(45f)` → early-return `score=100f, isAligned=true`（Priority Inversion 防禦）；GazeRuleTest +2 fail-safe 測試；LandmarkProbeTest 斷言修正 | ✅ DONE (Path A) | `domain/composition/rules/GazeRule.kt`, `test/.../GazeRuleTest.kt`, `androidTest/.../LandmarkProbeTest.kt` |
| D6-B2-T1 | `CoordinateTransformer` 座標橋樑：NormalizedPoint/PixelPoint/NormalizedRect/PixelRect data class；零 Android SDK 依賴；transformX/Y/Point/Rect 純數學實作；16 unit tests 全通過 | ✅ DONE (Path A) | `presentation/utils/CoordinateTransformer.kt` (新建), `test/.../CoordinateTransformerTest.kt` (新建) |
| D6-B2-T2 | `HaloGuidanceLayer.kt` 視覺回饋層：GazeRule 琥珀金圓環 + EyeLineRule 虛線輔助線；狀態互斥防禦（isAllAligned=true 隱藏/null 隱藏）；10fps 節流；`CameraState.detectedFaces` 欄位；`CameraScreen.kt` 接線（EdgeWarmthLayer 上方）；TAG_HALO_AMBER/GREEN/HIDDEN 語意探針 | ✅ DONE (Path A) | `presentation/camera/components/HaloGuidanceLayer.kt` (新建), `presentation/camera/CameraState.kt` (+detectedFaces), `presentation/camera/CameraViewModel.kt` (+detectedFaces update), `presentation/camera/CameraScreen.kt` (+HaloGuidanceLayer) |
| D6-B2-T3 | `HaloGuidanceLayerTest` androidTest（四路徑：null隱藏/琥珀金/綠色/狀態轉換節流）；mutableStateOf 驅動 recompose；4/4 PASS (Pixel_5 AVD) | ✅ DONE (Path A) | `androidTest/.../camera/components/HaloGuidanceLayerTest.kt` (新建) |
| D6-B3 | `EyeAfController` 實作：gaze unaligned → 後鏡頭 Eye-AF 觸發（前鏡頭硬體憲法防護；2s cooldown；眼部中點 → `NormalizedToPixelTransformer` → `cameraRepository.focusOnPoint()`）；`CameraViewModel.observeCompositionAdvice()` 接線；`EyeAfController.reset()` 於 `resetFocusState()` 呼叫；`EyeAfControllerTest` 9/9 PASS；`build.gradle.kts` +`isReturnDefaultValues` + `@RunWith(RobolectricTestRunner)` | ✅ DONE (Path A) | `presentation/camera/components/EyeAfController.kt` (新建), `presentation/camera/CameraViewModel.kt` (+eyeAfController, +D6-B3 gaze AF block), `test/.../camera/components/EyeAfControllerTest.kt` (新建, 9 tests), `app/build.gradle.kts` (+isReturnDefaultValues) |

> 🏁 **D6 全部 Batch ✅ DONE。B1 + B2 + B3 全數完成。EyeAfControllerTest 9/9 pass；前鏡頭防護兩道防線已驗收；KAIROS #2 /dream ✅ EXECUTED。**

---

## 📋 D7 Tickets — PTF Tab toggle + 極簡 UI + 1s 快門冷卻

### D7-Batch 0 — 技術債清零 ✅ DONE (2026-04-15)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D7-DEBT-01 | `HDRWarningOverlayTest` 3 條 state-transition 測試修復：改用 `mutableStateOf` 驅動重組，移除第二次 `setContent` 呼叫 | ✅ DONE | `androidTest/.../overlay/HDRWarningOverlayTest.kt` |
| D7-DEBT-02 | `EdgeWarmthLayerTest` Sony race condition 防禦：6 條 Canvas 斷言前加入 `waitForIdle()` | ✅ DONE | `androidTest/.../overlay/EdgeWarmthLayerTest.kt` |
| D7-DEBT-03 | `GazeRule` 常數語意拆分：`MAX_YAW_DEG` → `GUIDANCE_MAX_YAW_DEG`；`EXTREME_YAW_DEG` → `EXTREME_YAW_INVERSION_DEG`；新增 `const val NAME = "gaze"` | ✅ DONE | `domain/composition/rules/GazeRule.kt` |
| D7-DEBT-03b | `EyeLineRule` 補齊 `const val NAME = "eye_line"` companion const（對稱 GazeRule） | ✅ DONE | `domain/composition/rules/EyeLineRule.kt` |
| D7-DEBT-04 | `HaloGuidanceLayer` magic string 消除：`"gaze"` → `GazeRule.NAME`；`"eye_line"` → `EyeLineRule.NAME` | ✅ DONE | `presentation/camera/components/HaloGuidanceLayer.kt` |
| D7-DEBT-04b | `EyeAfController` magic string 消除：`GAZE_RULE_KEY = "gaze"` → `GazeRule.NAME` 直接引用 | ✅ DONE | `presentation/camera/components/EyeAfController.kt` |
| D7-DEBT-04c | `EyeAfControllerTest` 測試 magic string 同步消除：`"gaze"` → `GazeRule.NAME` | ✅ DONE | `test/.../camera/components/EyeAfControllerTest.kt` |
| D7-DEBT-04d | `CameraViewModel` D6_PROBE 探針移除（非生產邏輯清除） | ✅ DONE | `presentation/camera/CameraViewModel.kt` |

> 🏁 **D7-Batch0 技術債清零完成。UnitTests 188t/0failed/48skipped。Lint 0 new。準備進入 D7-Batch1 (PTF Tab toggle)。**

### D7-Batch 1 — PTF Tab toggle + 極簡 UI + 1s 快門冷卻 ✅ DONE (2026-04-15)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D7-B1-T1 | PTF Tab toggle：進入/退出 PassToFriendMode；300ms 切換；零 CameraX rebind | ✅ DONE | `CameraState.kt` (PassToFriendMode + TogglePassToFriendMode)；`CameraViewModel.kt` (togglePassToFriendMode)；`components/CameraTabBar.kt` (新建)；`CameraScreen.kt` (接線) |
| D7-B1-T2 | 1s 快門冷卻：PTF 進入後 1s 內不可按快門 | ✅ DONE | `CameraViewModel.kt` (lastCaptureTimeMs sentinel=Long.MIN_VALUE/2；capturePhoto Rule 16 Guard) |
| D7-B1-T3 | 極簡 UI：PTF 模式下隱藏 composition guides；EdgeWarmthLayer/BreathingRing 保留 | ✅ DONE | `CameraScreen.kt` (isPtfActive；GridOverlay/HaloGuidanceLayer/CompositionGuideOverlay/CompositionHint 條件隱藏)；`res/values/strings.xml` (PTF Tab 字串) |

> 🏁 **D7-Batch1 ✅ DONE。PassToFriendMode 狀態模型建立；CameraTabBar 300ms 動畫；Rule 16 雙重門控冷卻生效；極簡 UI 精確落實。Build ✅ / UnitTests 188t/0failed/48skipped / Lint 0 new。**

### D7-B2-FIX — CameraMode 架構重構與語意歸位 ✅ DONE (2026-04-15)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D7-B2-FIX-S1 | `CameraMode.kt` 新建（Step 1，前一 Session 已完成）| ✅ DONE | `domain/camera/CameraMode.kt` (新建；sealed interface；Normal + PassToFriend objects) |
| D7-B2-FIX-S2a | `CameraState.kt` 重構：移除 `PassToFriendMode` sealed class；`passToFriendMode` → `cameraMode: CameraMode`；`TogglePassToFriendMode` → `SetCameraMode(val mode: CameraMode)` | ✅ DONE | `presentation/camera/CameraState.kt` |
| D7-B2-FIX-S2b | `CameraViewModel.kt` 重構：`togglePassToFriendMode()` → `setCameraMode(mode)`；capturePhoto Guard 改判 `CameraMode.PassToFriend`；`resetFocusState()` 末尾加 sentinel reset | ✅ DONE | `presentation/camera/CameraViewModel.kt` |
| D7-B2-FIX-S2c | `CameraTabBar.kt` 重構：參數 `passToFriendMode: PassToFriendMode` → `cameraMode: CameraMode`；import 換層；KDoc 同步 | ✅ DONE | `presentation/camera/components/CameraTabBar.kt` |
| D7-B2-FIX-S2d | `CameraScreen.kt` 重構：isPtfActive 判斷改 `is CameraMode.PassToFriend`；toggle callback 改傳明確 mode；新增 domain import | ✅ DONE | `presentation/camera/CameraScreen.kt` |
| D7-B2-FIX-S3 | Backlog ADR 歸檔：ADR-D7-B2-FIX — CameraMode sealed interface 擴展契約（候選 KAIROS #3 Rule 17） | ✅ DONE | `devspec/feature_record_Backlog.md` |

> 🏁 **D7-B2-FIX ✅ DONE。PassToFriendMode → CameraMode 架構歸位；Multi-mode roadmap 基礎建立。Build ✅ / UnitTests 188t/0failed/48skipped / Lint 0 new。**

### D7-Batch 2 — PTF 大快門 UI + 首次教學 + ✕ 退出 ✅ DONE (2026-04-15)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D7-B2-T1 | 大快門 UI：PTF 模式下快門 72→80dp animateDpAsState (300ms)；進度環同步放大至 94dp | ✅ DONE | `CameraScreen.kt` (ShutterButtonSection: cameraMode 參數 + animateDpAsState 置頂) |
| D7-B2-T1b | ✕ 退出按鈕：TopEnd IconButton，PTF 模式顯示，點擊 SetCameraMode(Normal) | ✅ DONE | `CameraScreen.kt`；`strings.xml` (ptf_exit_cd) |
| D7-B2-T3 | 首次教學 hint：PtfPreferences DataStore + CameraViewModel 注入 + AnimatedVisibility hint | ✅ DONE | `data/preferences/PtfPreferences.kt` (新建)；`di/AppModule.kt` (+providePtfPreferences)；`CameraViewModel.kt` (+ptfPreferences, dismissPtfOnboarding)；`CameraState.kt` (+DismissPtfOnboarding event)；`CameraScreen.kt` (collectAsState + AnimatedVisibility hint block)；`strings.xml` (+ptf_onboarding_message/got_it) |
| D7-B2-T2 | Rule 16 冷卻護城河負向測試：`isPtfCooldownActive()` companion pure fn；`timeProvider` internal test seam；4 個 JVM 測試（哨兵首發 / 三步驟冷卻 / 邊界 1000ms / 邊界 999ms）| ✅ DONE | `CameraViewModel.kt` (+isPtfCooldownActive companion fn, +timeProvider internal)；`CameraViewModelTest.kt` (+4 tests) |

> 🏁 **D7-Batch2 ✅ DONE。大快門 80dp + ✕ 退出按鈕 + 首次教學 hint + Rule 16 負向測試全數完成。Build ✅ / UnitTests 192t/0failed/48skipped / Lint 0 new。Commit 1 (code): c4da634。**

### D7-Batch 3 — 系統級回歸驗證 ✅ DONE (2026-04-16)

| Ticket | 內容 | 狀態 | 影響檔案 |
|--------|------|------|---------|
| D7-B3-T1 | `CameraScreenBreathingRingTest` compile fix：補齊 `cameraMode = CameraMode.Normal` 至 5 個 `ShutterButtonSection` 呼叫點（D7-B2 簽名變更連帶修復） | ✅ DONE | `androidTest/.../CameraScreenBreathingRingTest.kt` |
| D7-B3-T2 | androidTest 回歸驗證（Sony XQ-BT52）：`CameraScreenBreathingRingTest`(5) + `EdgeWarmthLayerTest`(9) + `HaloGuidanceLayerTest`(4) = 18t/0failed | ✅ DONE | — |

> 🏁 **D7-Batch3 ✅ DONE。Normal 模式 UI 功能 Regression-free。androidTest 18/18 PASS on Sony XQ-BT52。SV ✅ PASS / Traycer 待終審。**

---

## 📌 Blocking Issues

| Issue | Severity | Discovered | Resolution |
|-------|----------|------------|------------|
| **[D0.3] Traycer Path B — 可驗證事實不一致** | ~~🔴 HIGH~~ | 2026-04-09 | ✅ 2026-04-09 Trust-but-verify 覆測通過，D0.1 清創關閉 |
| **[D1] SV Path B — D1 未達驗收閉環** | ~~🔴 HIGH~~ | 2026-04-09 | ✅ 2026-04-09 FIX-01~06 全數閉環，Traycer Path A 放行 |
| **[HDRWarningOverlayTest] 3 state-transition tests fail on device** | 🟡 LOW | 2026-04-12 | 非阻擋：預存在 `setContent` 多次呼叫設計 bug（commit `9b0abdc`）；需改用 `mutableStateOf` 驅動狀態切換。D5 前技術債登記，不影響 D4-T4 防禦網。 |

---

## 📅 Sprint Roadmap (D0-D15)

| Day | Date | Deliverable | KAIROS |
|-----|------|------------|--------|
| **D0** | 04-07 | 絞殺者手術：移除技術債，建立乾淨基底 | — |
| **D1** | 04-09 | CameraX preview + Tap-to-Focus + Edge Warmth 第一層 (pitch) | — |
| **D2** | 04-10 | Edge Warmth 完整 (roll) + 場景自動切換 | **#1** |
| **D3** | 04-13 | 水平儀 + 三分法網格 overlay | — |
| **D4** | 04-14 | Edge Warmth 第二層（浮動光點）+ 呼吸韻律初版 | — |
| **D5** | 04-15 | 優先級引擎骨架 + ThirdsRule + HeadroomRule | — |
| **D6** | 04-16 | EyeLineRule + GazeRule + HaloGuidanceLayer + Eye-AF | **#2** |
| **D7** | 04-17 | PTF Tab toggle + 極簡 UI + 1s 快門冷卻 | — |
| **D8** | 04-20 | PTF 規則引擎 + Halo 牽引 + 首次教學動畫 | — |
| **D9** | 04-21 | ML Kit Object Detection FOOD + 美食對焦 (3s 冷卻) | — |
| **D10** | 04-22 | FoodCompositionRule + MinimalistRule + 雜亂背景 UI | **#3** |
| **D11** | 04-23 | 鏡頭髒汙偵測（純 Kotlin Laplacian，無 OpenCV） | — |
| **D12** | 04-24 | 光線診斷（直方圖）+ 呼吸韻律完整版 + 三級裝置降級實作 | — |
| **D13** | 04-27 | Paywall + 評分引擎 + EXIF 寫入 + 拍後教學卡片 + DOF 骨架 | **#4** |
| **D14** | 04-28 | IFTR 埋點 + Settings UI + 多語系 (en/zh-TW/ja) | — |
| **D15** | 04-29 | 整合測試 + 餐廳實測 + 參數微調 | **#5 Final** |

---

## ✅ Completed (Cumulative)

### D0 (2026-04-07~09) — 絞殺者手術 ✅ CLOSED (Path A + Path B)
- [x] Branch created: `feature/cueshot-mvp`
- [x] OpenCV removed
- [x] OpticalFlowTracker removed, ImuCompensator decoupled
- [x] Glassmorphism / Aurora Blue / Neon Glow removed
- [x] 4-tier degradation → 3-tier DeviceCapabilityTier
- [x] Focus Pipeline simplified (Tap-to-Focus + cooldown only)
- [x] Compile PASS + grep residue = 0
- [x] D0.1 Warning 清創完成 (0 new warnings)
- [x] SV-D0.1 補修系列全數關閉 (focalLengthPixels rename, color contract, tier wiring)
- [x] ADR 寫入 devspec/feature_record_Backlog.md
- [x] D0.3 Trust-but-verify 覆測通過（tier-aware 補償 + locked 色彩路徑一致性確認）

### D2 (2026-04-10) — ✅ CLOSED (Path A, Traycer 放行)
- [x] `ThirdsRule.kt` 實作 + 17 tests pass (座標契約 + 負向測試)
- [x] `HeadroomRule.kt` 實作 + 12 tests pass (共通契約模板驗證)
- [x] `CompositionModule.kt` @Binds @IntoSet (Hilt OCP 模式確立)
- [x] `CompositionResult.kt` 新建 + `CompositionScore.kt` 焦土刪除
- [x] `AnalyzeCompositionUseCase.invoke()` 實作 (單幀聚合 + isAllAligned 動態判定)
- [x] `CameraState.compositionResult` 欄位落地 (compositionAdvice 移除)
- [x] `CameraViewModel.observeCompositionAdvice()` 實體接線 (D1 暫代拆除)
- [x] 驗收證據 1：`./gradlew testDeviceDebugUnitTest` — 104 tests, 0 failed
- [x] 驗收證據 2：`grep CompositionScore app/src` — 0 hits (Domain 層完全清除)
- [x] ADR 歸檔 (OCP Set Multibinding + CompositionResult 單幀聚合契約)
- [x] D2-T5 DEFERRED → D3 座標映射重建 (架構師裁決：D2 定義為純正規化引擎)
- **D2 里程碑驗收完成，純數學大腦已上線，準備進入 D3 (視覺化層)**

### D1 (2026-04-09) — ✅ CLOSED (Path A, Traycer 放行)
- [x] `CompositionRule.kt` 介面骨架建立
- [x] `CompositionScore.kt` 模型建立
- [x] `PtfState` sealed class + `CameraState.ptfState` 欄位
- [x] SV-D1-FIX-01：UseCase 契約對齊（`Set<CompositionRule>` + `CompositionScore` 預設值）
- [x] SV-D1-FIX-02：PTF 狀態機真正接線（Focusing→Focused/Failed→Idle + stale guard）
- [x] SV-D1-FIX-03：消除構圖雙路徑（停止主流程呼叫 `analyzeRuleOfThirds()`）
- [x] SV-D1-FIX-04：測試補齊（移除 blanket @Ignore + D1 契約測試）
- [x] SV-D1-FIX-05：PTF session token 競態防護（`currentPtfSessionId`）
- [x] SV-D1-FIX-06：Git 提交範圍清理（feat/docs 原子化分離）
- [x] D1.1：ADR 歸檔（devspec/feature_record_Backlog.md D1 三大決策）

---

## 🧠 Active Decisions / Deviations from SSOT

| Decision | SSOT Reference | Deviation? | Rationale |
|----------|---------------|------------|-----------|
| LowAngle → General fallback | §3.1 | Confirmed | v3.3: no LowAngleMode |
| SmileRule = Phase 1.5 | §4.4 | Confirmed | Cut from MVP |
| DOF = UI skeleton only | §12 MVP | Confirmed | Phase 1.5 implementation |
| Coaching intensity hidden | Tech v3 §5.1 | Confirmed | Needs IFTR baseline data |

---

## 🔴 KAIROS Findings (Auto-populated by /dream)

### KAIROS #1 — D2 (2026-04-10) ✅ EXECUTED

**Error Patterns**

🔴 架構性錯誤（→ CLAUDE.md 追加）:
1. **座標契約漂移**：ThirdsRule 初版（D2-T1）直接以 UI Viewport 像素為單位計算網格錨點，導致座標隨 UI 尺寸漂移。修復需 3 張連環補修票（SV-D2-T1-FIX-01/02/03）。根因：Rule 實作與 UseCase 呼叫端各自假設座標空間，無顯式歸一化強制。→ 提煉規則：「每個新 CompositionRule 實作，測試套件必須包含 ≥1 組負向測試（座標超界 + 空 faces 清單）」
2. **骨架接線不完整（D1 暫代殘留）**：D1 建立 `observeCompositionAdvice()` 時留下無意義佔位邏輯，而非直接跳過（TODO）；SV Gate 才發現 UseCase 根本未被呼叫，需 SV-D2-T4-FIX-01 補線。根因：Ticket 範疇定義「D1 是骨架，D2 才接線」，但骨架中有假性可編譯代碼遮蔽了未完成狀態。→ 提煉規則：「骨架 Ticket 中的未實作函數，必須使用 `error("Not implemented — will be wired in DX-TY")` 拋出，嚴禁回傳合法型別的空值偽裝完成」

🟡 一次性失誤:
- ThirdsRule 初版：`THIRDS_POINTS` 因輸入法問題誤用全形數字（已修正）
- `CompositionScore` → `CompositionResult` 遷移時，測試中遺留 3 個舊型別 import（焦土清除後 grep = 0 hits）

🟢 預防性發現:
- `_trackingBoxFlow` 仍存在於 CameraViewModel，但 D2 管線已不再消費它；KDoc（line ~1890）仍描述舊 throttle 路徑。非阻擋，D3 KDoc 清理時處理。
- `CompositionGuidelineEngine` import 仍殘留在 CameraViewModel line 29（被 `compositionEngine` 欄位 line 285 引用）。舊引擎尚未焦土，D3 或 /sv 掃描時需確認是否可刪除。
- `previewWidth ?: 800` 魔術數字（line ~1646）仍存在，DEFERRED → D3。

**Decision Audit**

| 決策 | 預期結果 | 實際結果 | 判定 |
|------|---------|---------|------|
| `CompositionResult` 取代 `CompositionScore` | 支援 topInstruction + isAllAligned + EXIF，減少 UI 轉換複雜度 | 完全符合，Domain 層 grep = 0 殘留，UI 欄位乾淨接入 | ✅ |
| @Binds @IntoSet OCP 模式 | 新增規則時零修改主引擎 | 驗證成功：HeadroomRule 加入不需改動 UseCase | ✅ |
| D2-T5 DEFERRED → D3 | 保持 D2 純數學引擎定義 | 魔術數字已標記，未造成 D2 阻塞 | ✅ |
| 拆除 `sample(300L)` debounce | D2 單幀純聚合，D5 再加平滑 | Raw 管線運作正常，無副作用 | ✅ |
| D1 骨架使用可編譯空值偽裝「完成」 | 預期降低 D1 工作量 | 實際導致 D2 需補 SV-D2-T4-FIX-01；骨架策略需修正 | ⚠️ 修正方案：見上方🔴規則 2 |

**SSOT Drift**

| 項目 | 類型 | SSOT 說 | 代碼做 | 行動 |
|------|------|---------|--------|------|
| ThirdsRule / HeadroomRule 所屬 Sprint | 架構漂移 | SSOT D5-6 才實作四規則 | D2 已提前實作 ThirdsRule + HeadroomRule | 📝 SSOT 規格需更新（D2 超前交付，但符合架構方向） |
| HeadroomRule weight = 0.8 | 數值 | SSOT 未定義各 rule weight | 代碼自行設定 0.8 | 📝 SSOT 補記（待 D5 優先級引擎 ADR 時歸檔正式 weight 表） |
| ALIGNED_THRESHOLD = 85f (ThirdsRule) | 數值 | SSOT 未定義 | 代碼自行設定 | 📝 同上，D5 調參時歸檔 |
| `_trackingBoxFlow` 仍存在 | 架構漂移 | D2 後管線不應有雙軌消費 | Flow 存在但無 consumer | 🔧 D3 清除（非阻擋）|

**Recommended CLAUDE.md Additions**

```
9. **骨架 Ticket 未實作強制聲明**：D1/骨架 Ticket 中的未實作函數，必須以
   `error("Not implemented — will be wired in DX-TY")` 顯式拋出，
   嚴禁回傳合法型別空值（如 `null`、`emptyList()`）偽裝完成。
   觸發來源：SV-D2-T4-FIX-01（D1 observeCompositionAdvice 暫代殘留）。

10. **Rule 負向測試強制覆蓋**：每個 CompositionRule 實作的測試套件，
    必須包含 ≥1 組負向測試（空 faces 清單 + 座標超界輸入 [0,1] 以外），
    驗證結果為 score=0 / isAligned=false（非拋出例外）。
    觸發來源：SV-D2-T1-FIX-01~03（ThirdsRule 座標漂移 bug）。
```

### KAIROS #2 — D6 (2026-04-15) ✅ EXECUTED

**Error Patterns**

🔴 架構性錯誤（→ CLAUDE.md 規則 13/14/15）:
1. **Android SDK 型別污染 JVM 測試（D6-B2-T1 + D6-B3）**：`android.graphics.PointF/RectF` 在 JVM unit test stub 環境下 fields 全回傳 `0`，導致兩次測試失敗。修復：D6-B2-T1 改用純 Kotlin data class；D6-B3 加 `@RunWith(RobolectricTestRunner::class)` + `@Config(sdk=[28])` + `isReturnDefaultValues=true`。→ 規則 13。
2. **Composable LaunchedEffect 置放違反 Slot-Table（D6-B2-T2.1）**：`HaloGuidanceLayer` 初版將 `LaunchedEffect` 置於 early return 後，狀態切換時首幀遺失。Traycer T2.1 修復。→ 規則 14。
3. **KDoc 引用錯誤的呼叫鏈（D6-B3）**：`EyeAfController` 描述第二道防線為 `CameraXFocusController`，實際路徑是 `LiveCameraSource.focusOnPoint()` + `FixedFocusDetector`。Traycer 要求 KDoc 校準。→ 規則 15。

🟡 一次性失誤:
- `EyeAfController.lastTriggerMs = 0L` → 首次呼叫被 throttle 擋住（`nowMs=1000L < 2000L`）；改為 `Long.MIN_VALUE / 2` 哨兵值
- `build.gradle.kts` 缺 `isReturnDefaultValues=true`，`Log.*` 在 JVM test 拋 RuntimeException
- `GazeRule` 初版對 extreme yaw（90°）給低分（Priority Inversion 問題）；D6-B1-T1.6 熱修復

🟢 預防性發現:
- `FaceLandmarkData` 仍依賴 `android.graphics.RectF/PointF`（SSOT-TYPE-DRIFT-01 已登記）；若要移至純 JVM 測試需遷移欄位型別
- `ruleScores` map key `"gaze"` 為 magic string，散落於 HaloGuidanceLayer + EyeAfController；D7 應提取至 `GazeRule.NAME` companion const
- `Gradle Configuration Cache` `:app:generateTestImages` 持續警告（非阻擋）
- `D5-TECH-DEBT-01`（HDRWarningOverlayTest + EdgeWarmthLayerTest）遞延出 D6，需在 D7 週期前排入

**Decision Audit**

| 決策 | 預期結果 | 實際結果 | 判定 |
|------|---------|---------|------|
| `CompositionRule.evaluate()` 破壞性升級為 `List<FaceLandmarkData>` | 消除中間映射層 | D6-B1-T1 一次完成，零回歸 | ✅ |
| `NormalizedToPixelTransformer` 純 Kotlin data class | 消除 Android SDK stub 問題 | 完全解決；命名更精確 | ✅ |
| GazeRule extreme yaw → Priority Inversion（score=100, isAligned=true） | 防止極端角度誤觸引導 | D6_PROBE 驗證正確 | ✅ |
| HaloGuidanceLayer remember/LaunchedEffect 置頂（T2.1） | 消除首幀遺失 | Traycer 通過；4/4 instrumented pass | ✅ |
| EyeAfController 呼叫 `cameraRepository.focusOnPoint()` 而非 CameraXFocusController | 最小化 integration surface | 功能正確；KDoc 已校準；第二道防線由 LiveCameraSource 承擔 | ⚠️ D7 追蹤：是否統一兩條 focus 呼叫路徑 |
| `D5-TECH-DEBT-01` 遞延出 D6 | 避免超出 D6 工作量 | 未阻塞 D6；但需 D7 前明確排入 | ⚠️ 需追蹤 |

**SSOT Drift**

| 項目 | 類型 | SSOT 說 | 代碼做 | 行動 |
|------|------|---------|--------|------|
| `FaceLandmarkData` 使用 Android SDK 型別 | 架構漂移 | 未定義 Domain 層 SDK 依賴限制 | 使用 RectF/PointF | 📝 技術 SSOT 補記：Domain Model 應遷移至純 Kotlin 型別（D7+） |
| `EyeAfController` 置於 `presentation/camera/components/` | 位置語意 | 未定義 Eye-AF controller 位置 | 與 UI 元件同層 | 📝 D7 考慮移至 `presentation/camera/focus/` |
| `ruleScores["gaze"]` magic string | 命名 | GazeRule.NAME 未在 SSOT 顯式定義 | 硬編碼字串散落 2 處 | 🔧 D7：提取至 GazeRule.NAME companion const |
| `RuleResult` 舊命名 | 命名漂移 | 歷史文件有 RuleResult 提及 | 代碼已統一為 RuleScore | 📝 技術 SSOT 文件移除 RuleResult 舊命名 |

**Recommended CLAUDE.md Additions（規則 13/14/15/16）**：已追加至 CLAUDE.md ✅

**KAIROS #2 補充盤點（2026-04-15 /dream 執行）**

| 待辦項目 | 類型 | 行動 | 排程 |
|---------|------|------|------|
| SSOT-TYPE-DRIFT-01：`RuleResult` 舊命名清除 | 文件漂移 | 技術 SSOT 移除 RuleResult 舊命名 | D7 |
| D5-TECH-DEBT-01：HDRWarningOverlayTest (3 tests) + EdgeWarmthLayerTest (Sony race) | 測試技術債 | 改用 mutableStateOf + waitForIdle() | D7 優先 |
| Gradle CC 警告：`:app:generateTestImages` | 建置警告 | 非阻擋；排查 task inputs 宣告 | D8+ |
| MAX_YAW_DEG vs EXTREME_YAW_DEG 語意拆分 | 命名漂移 | `GazeRule` 兩個閾值語意不同（引導上限 vs Priority Inversion 防禦），需加 KDoc 區分；考慮重命名為 `GUIDANCE_MAX_YAW_DEG` + `EXTREME_YAW_INVERSION_DEG` | D7 |
| Rule 16 收割：硬體呼叫雙重門控 | 新規則 | 已寫入 CLAUDE.md Rule 16 | ✅ 完成 |

### KAIROS #3 — D10 (2026-04-21)
- (will be populated: Object Detection FOOD confidence, bgCV thresholds, MinimalistRule coverage)

### KAIROS #4 — D13 (2026-04-24)
- (will be populated: scoring weights, Paywall UX, blur quality)

### KAIROS #5 Final — D15 (2026-04-28)
- (will be populated: IFTR initial data, device degradation, i18n quality, launch readiness)

---

## 📎 Quick Links

- SSOT: `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`
- Tech Spec: `SmartCameraApp_TechDeepDive_v3_20260405.md`
- Roadmap: `Cueshot_MVP_Phase1_Roadmap_v1.md`
- Architecture: CLAUDE.md §Architecture
- Gemini Prompts: `Cueshot_Gemini_Architect_Prompt_v2.md`
- D0 Surgery Package: `Cueshot_D0_Gemini_Feeding_Package.md`
- Last Handoff: `MVP策略對話Handoff_20260406.md`

---

## 💳 Debt / Backlog (技術債與待辦)

- [ ] [Infrastructure] Kapt to KSP migration (保留 K2 編譯器相容性，推遲至 MVP D6 階段後再執行，目前維持 1.9 降級模式以避免依賴重構的範圍蔓延)
- [ ] [Coordinate] 移除 CameraViewModel 中的 `previewWidth ?: 800` 魔術數字，推遲至 D3 座標映射重建時，改由 CameraCharacteristics 驅動。（D2 為純正規化數學引擎，Pixel 座標轉換排除在外）
- 🟡 **D5-TECH-DEBT-01**: Compose Test 基礎設施升級 (待執行於 KAIROS #2) — (1) `HDRWarningOverlayTest` 3 個狀態切換測試改 `mutableStateOf` 重寫；(2) `EdgeWarmthLayerTest` Sony race condition 補 `waitForIdle()` 防護；(3) 全局 `setContent` 重複掛載違規掃描。驗收：Sony XQ-BT52 全 PASS。詳見 `devspec/feature_record_Backlog.md`。

---

## 🗄️ Standby Mechanisms (Phase 2 啟用)

- Merge SOP: `devspec/MERGE_SOP.md` — 多分支時啟用
- Branch Releases: `devspec/BRANCH_RELEASES.md` — 多分支時啟用
- 啟用條件：第二位開發者加入 / 產品上架後維護 / 跨版本回歸測試
