# Cueshot MVP Sprint Archive

## Archived Days

### D0 — 絞殺者手術 ✅ CLOSED (2026-04-09)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D0-T1 | `git checkout -b feature/cueshot-mvp` from v2.12.0-24 | ✅ DONE | — |
| D0-T2 | 移除 OpenCV dependency + SplashScreen 改為 ML Kit warm-up | ✅ DONE | `build.gradle.kts`, `MainActivity.kt` |
| D0-T3 | 移除 OpticalFlowTracker + 解耦 ImuCompensator | ✅ DONE | `SmartCameraAnalyzer.kt`, `CameraRepositoryImpl.kt`, `FeatureFlags.kt` |
| D0-T4 | 移除 Glassmorphism / Aurora Blue / Neon Glow UI | ✅ DONE | `FocusIndicatorOverlay.kt`, `ExpandableSettingsCard.kt` |
| D0-T5 | 四級降級 → 三級裝置相容骨架 (FULL/LIMITED/LEGACY) | ✅ DONE | `DeviceCapabilityTier.kt` (新建), `CameraModule.kt`, `CameraViewModel.kt` |
| D0-T6 | 簡化 Focus Pipeline + 更新對焦 UI 顏色 (Amber/Green) | ✅ DONE | `FocusIndicatorOverlay.kt` |
| D0-T7 | 編譯驗證 + grep 殘留檢查 + PROGRESS.md 確認 | ✅ DONE | — |

#### D0.1 — 編譯警告清創手術 (追加)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D0.1-T1 | `./gradlew assembleDeviceDebug` 取得完整 Warning 清單 | ✅ DONE | — |
| D0.1-T2 | 分類 Warning：Deprecation / Unused / Nullability / Cast | ✅ DONE | — |
| D0.1-T3 | 逐一修復所有 Warning（零容忍政策） | ✅ DONE | `LiveCameraSource.kt`, `FixedFocusDetector.kt`, `FocusIndicatorOverlay.kt` |
| D0.1-T4 | 重新編譯確認 Warning = 0 new | ✅ DONE | — |

#### D0.x — SV 補修系列（Traycer Path B）

| Ticket ID | Description | Status | Notes |
|-----------|-------------|--------|-------|
| D0.2 | KSP 技術債記錄至 PROGRESS.md + Backlog | ✅ DONE | 推遲至 D6 後 |
| SV-D0.1-01 | KDoc 焦土清理（OpticalFlow/Glassmorphism/Aurora）| ✅ DONE | — |
| SV-D0.1-02 | `DeviceCapabilityTier` sealed class 新建 | ✅ DONE | `domain/model/` |
| SV-D0.1-03 | Focus Locked 色彩規格確認 | ✅ DONE | White/Green 非 SSOT v3.3 規格，不執行替換 |
| SV-D0.1-08 | `focalLengthPx` → `focalLengthPixels` 重命名 | ✅ DONE | grep = 0 hits |
| SV-D0.1-10 | `drawLockedFrame()` 色彩路徑端到端一致 | ✅ DONE | `baseColor` 參數接入 `animatedColor` |
| D0.3 | Traycer Path B 嚴格補修（zero-hit + color contract + tier wiring）| ✅ DONE | Trust-but-verify 覆測通過；三張補修票達放行狀態（2026-04-09） |

### D1 — Rule Engine 骨架與 PTF 狀態機 ✅ CLOSED (2026-04-09)

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

### D2 — Rule Engine 實作 (構圖規則) ✅ CLOSED (2026-04-10)

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

### D3 — 視覺化層 ✅ CLOSED (2026-04-13)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D3-T0 | 前置焦土清理：刪除 `_trackingBoxFlow`、`compositionEngine`、`onTrackingBoxUpdated()`、2 dead imports；更新過期 KDoc | ✅ DONE | `presentation/camera/CameraViewModel.kt` |
| D3-T1 | 水平儀收斂：LevelIndicator 色碼對齊 SSOT (#50C864/#D4A030)；CameraState 新增 deviceRoll 欄位；ViewModel observeOrientation 同步 _state.deviceRoll | ✅ DONE (Path A) | `components/overlay/LevelIndicator.kt`, `CameraState.kt`, `CameraViewModel.kt` |
| D3-T1.1 | 全域色碼技術債歸檔：5 個殘留舊色碼檔案登記至 Backlog（D5 UI 精修統一處理） | ✅ DONE | `devspec/feature_record_Backlog.md`, `PROGRESS.md` |
| D3-T2 | 三分法網格 Overlay 對齊 Cueshot 規格：`GridOverlay.kt` 改為實線（移除 dashPathEffect）、alpha 0.4→0.5；`CameraScreen.kt` 清除舊 TODO comment；isGridEnabled 綁定已存在，Z-order 已正確 | ✅ DONE (Path A) | `components/overlay/GridOverlay.kt`, `presentation/camera/CameraScreen.kt` |
| D3-T3 | CameraCharacteristics 焦距還債：移除 `previewWidth ?: 800` 魔術數字；`LiveCameraSource` 在 camera binding 後讀取 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS` + `SENSOR_INFO_PHYSICAL_SIZE` 計算 factor；`CameraRepository` 介面新增 `getFocalLengthFactor()`；ViewModel 改用 `factor × previewWidth`，fail-safe 為 `previewWidth / 2f` | ✅ DONE | `data/source/camera/LiveCameraSource.kt`, `domain/repository/CameraRepository.kt`, `data/repository/CameraRepositoryImpl.kt`, `presentation/camera/CameraViewModel.kt` |
| SV-D3-T3-FIX-01 | SSOT 硬編碼清零：移除 LEGACY tier 的 `600f` 硬編碼常數，改為「所有 tier 一律 hardware-first」，取不到特徵時走動態 fail-safe | ✅ DONE (Path A) | `presentation/camera/CameraViewModel.kt` |
| SV-D3-T3-FIX-02 | stale防護：在 `unbind()` 與每次新 bind 開始前先將 `focalLengthFactor = null`，確保不跨 session 汙染 | ✅ DONE (Path A) | `data/source/camera/LiveCameraSource.kt` |
| SV-D3-T3-FIX-03 | fail-safe 契約明確化：fail-safe 明確綁定當前 viewport/preview 幾組何（非固定常數），補上 KDoc/註解說明（ImuCompensator 800f 幽靈清除 + CameraRepository null 雙重語義拆分） | ✅ DONE (Path A) | `presentation/camera/CameraViewModel.kt`, `domain/camera/ImuCompensator.kt`, `domain/repository/CameraRepository.kt` |
| SV-D3-T3-FIX-04 | Lint 破窗清零：`CameraCapabilityDetector.kt` + `LiveCameraSource.kt` 補 `@androidx.annotation.OptIn(ExperimentalCameraInfo + ExperimentalCamera2Interop)` | ✅ DONE | `data/source/camera/CameraCapabilityDetector.kt`, `data/source/camera/LiveCameraSource.kt` |
| D3-T4 | D3 視覺化層負向測試 (KAIROS Rule 10)：`CameraViewModelTest` (10 tests) + `ImuCompensatorTest` (6 tests)；提取 `computeFocalLengthPixels()` 為 internal companion 函數 | ✅ DONE | `presentation/camera/CameraViewModel.kt`, `test/.../CameraViewModelTest.kt` (新建), `test/.../ImuCompensatorTest.kt` (新建) |
| D3-T5 | D3 里里程碑終局歸檔：ADR 固化 (UnsafeOptIn 治理 + Fail-safe 契約 + Rule 10 落地 + ImuCompensator bootstrap 陷阱)；D3 封板；殘骸技術債登記 | ✅ DONE | `devspec/feature_record_Backlog.md`, `.claude/PROGRESS.md` |

### D4 — Edge Warmth 第二層 + 呼吸韻律 ✅ CLOSED (2026-04-14)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D4-T0 | 前置焦土：清除 `CameraScreen.kt` 的 `GuidelineOverlay` dead import | ✅ DONE (Path A) | `presentation/camera/CameraScreen.kt` |
| D4-T1 | Edge Warmth 第二層（浮動光點）：實作浮動光點 Overlay，接線至 `CameraState.deviceRoll` | ✅ DONE | `presentation/camera/overlay/EdgeWarmthLayer.kt` |
| SV-D4-T1-FIX-01 | EdgeWarmthLayer 實體接線：插入 CameraScreen 渲染堆棧 | ✅ DONE | `presentation/camera/CameraScreen.kt` |
| D4-T2 | 呼吸韻律初版：`#50C864` 綠色脈動動畫，接線至 `isAllAligned` | ✅ DONE (Path A) | `presentation/camera/overlay/EdgeWarmthLayer.kt`, `presentation/camera/CameraScreen.kt` |
| SV-D4-T2-FIX-01 | 快門外環互斥防禦：`if (isAligned && !state.isCapturing)` | ✅ DONE (Path A) | `presentation/camera/CameraScreen.kt` |
| D4-T4 | KAIROS Rule 10 測試矩陣：`CameraScreenStateTest` + `EdgeWarmthLayerTest` | ✅ DONE (Path A) | `test/.../CameraScreenStateTest.kt`, `androidTest/.../EdgeWarmthLayerTest.kt` |

### D5 — 優先級引擎 + 四大規則 ✅ CLOSED (2026-04-15)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D5-B1-T1 | `CompositionRule` 介面新增 `confidenceFactor: Float` 屬性 | ✅ DONE (Path A) | `domain/composition/CompositionRule.kt` |
| D5-B1-T4 | `ThirdsRule` + `HeadroomRule` 升級：weight→0.20f, confidenceFactor=0.80f | ✅ DONE (Path A) | `domain/composition/rules/ThirdsRule.kt`, `HeadroomRule.kt` |
| D5-B1-T2 | `PriorityEngine` 新建：SSOT §4.1 公式 | ✅ DONE (Path A) | `domain/composition/PriorityEngine.kt` |
| D5-B2-T3 | `AnalyzeCompositionUseCase` 接線 `PriorityEngine` | ✅ DONE (Path A) | `domain/usecase/AnalyzeCompositionUseCase.kt`, `test/.../AnalyzeCompositionUseCaseTest.kt` |

### D6 — EyeLineRule + GazeRule + HaloGuidanceLayer + Eye-AF ✅ CLOSED (2026-04-16)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D6-B1-T1 | `FaceLandmarkData` DTO 新建；`CompositionRule.evaluate()` 介面升級 | ✅ DONE (Path A) | `domain/model/FaceLandmarkData.kt`, `domain/composition/CompositionRule.kt`, `rules/ThirdsRule.kt`, `rules/HeadroomRule.kt` |
| D6-B1-T2 | `EyeLineRule` 實作（基於 `leftEyeNormalized`/`rightEyeNormalized`） | ✅ DONE (Path A) | `domain/composition/rules/EyeLineRule.kt`, `test/.../EyeLineRuleTest.kt`, `di/CompositionModule.kt` |
| D6-B1-T3 | `GazeRule` 實作（基於 `headEulerAngleY`） | ✅ DONE (Path A) | `domain/composition/rules/GazeRule.kt`, `test/.../GazeRuleTest.kt`, `di/CompositionModule.kt` |
| D6-B2-T2 | `HaloGuidanceLayer.kt` 視覺回饋層 | ✅ DONE (Path A) | `presentation/camera/components/HaloGuidanceLayer.kt` |
| D6-B3 | `EyeAfController` 實作：gaze unaligned → 後鏡頭 Eye-AF 觸發 | ✅ DONE (Path A) | `presentation/camera/components/EyeAfController.kt`, `presentation/camera/CameraViewModel.kt`, `test/.../camera/components/EyeAfControllerTest.kt` |

### D7 — PTF Tab toggle + 極簡 UI + 1s 快門冷卻 ✅ CLOSED (2026-04-17)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D7-B1-T1 | PTF Tab toggle：進入/退出 PassToFriendMode；300ms 切換 | ✅ DONE | `CameraState.kt`, `CameraViewModel.kt`, `components/CameraTabBar.kt`, `CameraScreen.kt` |
| D7-B1-T2 | 1s 快門冷卻：PTF 進入後 1s 內不可按快門 | ✅ DONE | `CameraViewModel.kt` |
| D7-B1-T3 | 極簡 UI：PTF 模式下隱藏 composition guides | ✅ DONE | `CameraScreen.kt` |
| D7-B2-FIX | CameraMode 架構重構與語意歸位 (Normal/PassToFriend) | ✅ DONE | `domain/camera/CameraMode.kt`, `presentation/camera/CameraState.kt` |
| D7-B2-T1 | 大快門 UI：PTF 模式下快門 72→80dp；進度環同步放大 | ✅ DONE | `CameraScreen.kt` |
| D7-B2-T3 | 首次教學 hint：PtfPreferences DataStore | ✅ DONE | `data/preferences/PtfPreferences.kt`, `CameraViewModel.kt` |

### D8 — PTF 規則引擎 + Halo 牽引 ✅ CLOSED (2026-04-20)

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D8-B1-T1 | Domain 模型新建：`PtfOffset`, `PtfRuleAggregator` AND-Gate | ✅ DONE | `domain/composition/PtfOffset.kt`, `PtfRuleAggregator.kt` |
| D8-B1-T2 | 三規則獨立物件：Framing, Angle, Level | ✅ DONE | `rules/PtfFramingRule.kt`, `rules/PtfAngleRule.kt`, `rules/PtfLevelRule.kt` |
| D8-B1-T3 | ViewModel 接線：EMA(α=0.3) face bbox 平滑 | ✅ DONE | `presentation/camera/CameraViewModel.kt`, `presentation/camera/CameraState.kt` |
| D8-B2-T2 | PTF Halo 牽引動畫實作：金色偏亮弧/綠色呼吸 | ✅ DONE | `HaloGuidanceLayer.kt`, `CameraScreen.kt` |
| D8-FIX | MVP UI 表面層除錯殘留絞殺（debug flags, orphans） | ✅ DONE | `CameraScreen.kt`, `LevelIndicator.kt`, `CameraViewModel.kt` |

---

### D11 — Pipeline 自動化強化

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D11-T1 | /qa→/sv Auto-Chain + PROGRESS 寫入拆分（Step 0 conf 化 / Step 4 責任分離 / Auto-Chain 銜接 / sv Step 10） | ✅ Done | `.claude/skills/qa/SKILL.md`, `.claude/skills/sv/SKILL.md` |
| D11-T2 | structural_verify.sh v1.3 + /qa Step 5（Check D/E/F 新增）| ✅ Done | `structural_verify.sh`, `qa/SKILL.md` |
| D11-T2-HF1 | structural_verify pre/post mode 切分（Check E 循環依賴修復）| ✅ Done | `structural_verify.sh`, `cross_review.sh`, `qa/SKILL.md` |

---

### D12 — Pipeline 微學習建議器

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| D12-T1 | Stop Hook CLAUDE.md 微學習建議器（session >50 行時輸出 ≤2 條 CLAUDE.md 更新建議）| ✅ Done | `.claude/hooks/stop.sh`, `.claude/settings.local.json` |

---

### INFRA — Pipeline 治理

| Ticket | 任務 | Status | 檔案 |
|--------|------|--------|------|
| INFRA-17 | DIFF_REF 四段式合約（STALE_DIFF_REF 分類 + Suggested base + 三腳本協作）| ✅ Done | `verify_checklist.sh`, `structural_verify.sh`, `cross_review.sh` |
| INFRA-18 | DIFF_END 鎖定 Ticket 審查終點（FUTURE_CONTAMINATION + locked range + /qa 自動寫入 + /sv 前置防護）| ✅ Done | `verify_checklist.sh`, `structural_verify.sh`, `cross_review.sh`, `qa/SKILL.md`, `sv/SKILL.md` |

---

## KAIROS Retrospectives

### KAIROS #1 — D2 (2026-04-10) ✅ EXECUTED

**Error Patterns**
- 🔴 座標契約漂移：ThirdsRule 初版直接以 UI 像素計算，導致隨尺寸漂移。
- 🔴 骨架接線不完整：D1 留下無意義佔位邏輯遮蔽未完成狀態。

**Decision Audit**
- `CompositionResult` 取代 `CompositionScore` ✅
- @Binds @IntoSet OCP 模式 ✅

**SSOT Drift**
- ThirdsRule / HeadroomRule 提前實作。
- ALIGNED_THRESHOLD = 85f (ThirdsRule) 自定。

---

### KAIROS #2 — D6 (2026-04-15) ✅ EXECUTED

**Error Patterns**
- 🔴 Android SDK 型別污染 JVM 測試：`PointF`/`RectF` fields 回傳 0。
- 🔴 Composable LaunchedEffect 置放違反 Slot-Table：置於 early return 後導致遺失。
- 🔴 KDoc 引用錯誤呼叫鏈。

**Decision Audit**
- `CoordinateTransformer` 純 Kotlin data class ✅
- GazeRule extreme yaw → Priority Inversion ✅

---

## Resolved Debt

- [x] [Coordinate] 移除 CameraViewModel 中的 `previewWidth ?: 800` 魔術數字 (D3)
- [x] [UI] 移除 A/B Test 黑屏佔位與除錯文字 (D8-FIX)
- [x] [UI] 移除角度數值渲染 (D8-FIX-04)

---

## Standby Mechanisms

- [Phase 2] Merge SOP: `devspec/MERGE_SOP.md`
- [Phase 2] Branch Releases: `devspec/BRANCH_RELEASES.md`
- [Phase 2] 啟用條件：第二位開發者加入 / 產品上架後維護

---

### P08 清場系列 ✅ CLOSED (2026-06-04)

| Session | Ticket | 任務 | Status | Commit |
| ------- | ------ | ---- | ------ | ------ |
| W | INFRA-20 | RFC 2119 標記 (16 files) | ✅ Done | `1c47797` |
| Y | INFRA-21 | CLAUDE.md 瘦身 173→145 | ✅ Done | `33f98ef` |
| Z | P08-E1 | PitchTractionLayer 清場 (-561 lines) | ✅ Done | `400bc7e` |
| AA | P08-F1 | LEVEL_FADE 常數修正 (pre-completed) | ✅ Done | `fa5afb8` |
| AB | P08-F2 | GridOverlay 三因子修復 | ✅ Done | `d50ddbd` |
| AC | P08-F2-AI2 | contentBounds→null | ✅ Done | `04f98ee` |
| AD | P08-F3 | Ghost Face 源頭過濾 | ✅ Done | `3e00e80` |
| AE | P08-F3-HF1 | ML Kit 單寫入點收斂 | ✅ Done | pending |

---

## Archived PROGRESS Sections（2026-06-07 INFRA-31）

### Stage0 Realignment（2026-05-05 啟動）

#### Active Tickets

- **TICKET-00** isAllAligned fix — P0 — Est 30m / Actual 35m (+5m) — DONE
- **TICKET-01** AmbientConstants SSOT — P0 — Est 30m / Actual 25m (-5m) — DONE

#### Wave Queue

W1: T00→T01 / W2: T02→T03→T04→T06 / W3: T08→T09→T05 / W4: T07→T10→T11→T12A→T13→T15 / W5: T12B→T14

#### Source Documents（Phase 0 基準，已封存）

- SSOT v4.0.5: `plans/architecture/SSOT_v4_0_5.md`
- Layout Spec v2.5.1: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Phase0 UI Contract: `plans/cso_briefing/Phase0_UI_Realignment_Contract_v1.md`

### Session Completion Notes（Session J → AE）

> **Session J 完成（2026-05-24）：RC-1~RC-5 全部修復 — Split-Brain(RC-1) / Await Cancel(RC-2) / FadingOut 中間態(RC-3) / FOCUS_FAILED_MS 常數化(RC-4) / Lock Rollback(RC-5)，QA PASS（181 tests, 0 failed）**
> **Session K 完成（2026-05-25）：RC-3-FOLLOWUP（Failed→Idle render fadeout 補齊）+ RC-4-FOLLOWUP（TapFocusRing magic number 常數治理）QA PASS（181 tests, 0 failed）**
> **Session L 完成（2026-05-26）：INFRA-002~005 — .gitignore session_handoff、/qa Step 0 Scope Gate verify_checklist.sh、CSO briefing 重組、CSO_Session_Startup_SOP 永駐層精簡（19K→2K）**
> **Session M 完成（2026-05-27）：純調查 — GazeAF 琥珀圓環根因定位（Ghost Face bbox + gazeScore=0.0 雙層 Bug）、PitchTraction 第5顆光點 Sony nav bar 遮蔽分析、Pixel 9a 預覽雙層覆蓋三因子分析、UITA SOP v2.1 更新**
> **Session N 完成（2026-05-29）：GridOverlay 不可見三層根因定位（alpha=0.08f/strokeWidth=0.5f/Z-order）、Sony logcat 驗證 contentBounds 正常、INFRA-14/15 git untrack cso_briefing+uita_project**
> **Session O 完成（2026-05-29）：D10-T1 Pipeline Boot 基礎建設、D10-T2 verify_checklist.sh conf 模式、D10-T2-HF1 /qa Step 4.5 Auto Commit Gate — 3 commits, 181 tests PASS**
> **Session P 完成（2026-05-29）：Pipeline v1.6 全鏈路 — D10-T2 HF2~HF4、V16-CR2（cross_review.sh v1.6 4-Verdict + Fail Budget）— HOTFIX-16~21，181 tests PASS**
> **Session Q 完成（2026-05-29）：D11-T1（/qa→/sv Auto-Chain）、D11-T2（structural_verify.sh v1.3 Check D/E/F）、D11-T2-HF1（--post flag）— HOTFIX-22~24，181 tests PASS**
> **Session S 完成（2026-05-31）：P05-R1 GazeAF Ghost Face 三層修復；INFRA-17 DIFF_REF 四段式合約 — HOTFIX-28 + INFRA-17，3 commits**
> **Session T 完成（2026-05-31）：INFRA-18 SV+Cross-Review PASS — DIFF_END 鎖定 Ticket 審查終點**
> **Session U 完成（2026-06-01）：P05-R1 Sony 實機三項驗收 PASS；INFRA-19 index-verify Skill；HOTFIX-30~32 治理文件維護**
> **Session V 完成（2026-06-01）：Ghost Face 渲染路徑根因定位；CSO_INDEX CONTRACT 更新至 P08；KAIROS K3-8/K3-9 候選登記**
> **Session W 完成（2026-06-02）：INFRA-RFC2119 — 16 個 rules/skills 文件加 RFC 2119 anchor；QA PASS；commit 1c47797**
> **Session X 完成（2026-06-02）：INFRA-RFC2119 SV+Cross-Review PASS；HOTFIX-38/39/40 ✅；dtaudit MUST NOT 修正**
> **Session Y 完成（2026-06-02）：INFRA-21 CLAUDE.md 行數瘦身 173→145 行；P08-L1 Contract Amendment 套用；Cross-Review R1 PASS**
> **Session Z 完成（2026-06-03）：P08-E1 PitchTractionLayer 清場（-561 lines）；Traycer execute_review ✅ PASS**
> **Session AA 完成（2026-06-03）：P08-F1 Pre-completed 確認閉環；HOTFIX-41 cross_review.sh 反引號 Bug 修正；Traycer execute_review ✅ PASS**
> **Session AB 完成（2026-06-03）：P08-F2 GridOverlay 三因子修復（alpha/strokeWidth/Z-order）；SV+Cross-Review R1 PASS**
> **Session AC 完成（2026-06-03）：P08-F2 AI-2 contentBounds→null；/qa PASS（181 tests）；Traycer execute_review ✅ PASS**
> **Session AD 完成（2026-06-04）：P08-F3 AI-1/AI-3/AI-4 實作閉環；INFRA-22/23 Telegram Stop Hook；Traycer PASS 狀態同步**
> **Session AE 完成（2026-06-04）：P08-F3-HF1 QA PASS（181 tests）；observeDetectionResults 刪除；HOTFIX-42/43；SV+Cross-Review R2 PASS；Traycer ✅ PASS**

> **Session S12 完成（2026-06-15）：P08-F4-FIX Pipeline Consolidation — SmartCameraAnalyzer 獨佔 ML Kit；Path B fromBitmap 移除；FaceDetector lifecycle-aware；switchCamera detectionResult=null；commits fd1c686→3a4a9e3；INFRA-51 ssot_context v1.7；SV+Cross-Review R3 PASS**

> **Session S13 完成（2026-06-15）：P08-F4-FIX-REPAIR — 修補 7 項 Invariant Violation（ML Kit 互斥 objectDetector 移除 AI-1/AI-2、null reset AI-3~5、StartAnalysis 生命週期接線 AI-6、detector lifecycle AI-7）；Build+Tests+Lint PASS；SV+Cross-Review R2 PASS；75adef9 ✅ Done**

> **Session S13 補充（2026-06-15）：P08-F4-FIX + P08-F4-FIX-REPAIR Traycer execute_review ✅ PASS · 雙 Ticket 完整閉環**

> **Session S22 完成（2026-06-25）：STATE-CONTRACT-01 GuidanceState 六態契約 AI-1～AI-6 實作完成；396 tests PASS（+34）；SV+Cross-Review R2 PASS；6f8ab9e+2edca83 ✅ Done**

> **Session S23-FIX-A 完成（2026-06-25）：STATE-CONTRACT-01-FIX-A HaloGuidanceLayerTest 全文重寫（159行）對齊 GuidanceState 六態 API；Cross-Review R3 PASS（R1 BLOCKER→R2 REPORT_FIX→R3 PASS）；7bfd8dd ✅ Done**

> **Session S23-FIX-B 完成（2026-06-25）：STATE-CONTRACT-01-FIX-B computeGuidanceStatePure 純函數抽取 + AC-3/4/5 reducer 單元測試閉環；Cross-Review R1 PASS；30653fb ✅ Done**

> **Session S23-FIX-C 完成（2026-06-25）：STATE-CONTRACT-01-FIX-C LANDMARK_NONE KDoc 三檔補齊 + PTF gate deviation 強化；Cross-Review R2 PASS；2c7ddff ✅ Done**

> **Session S24 完成（2026-06-30）：INFRA-REACH-01 Guard Input Reachability gate — static_blind_scan.sh §3.5.5 Dead-State 掃描 + /sv B-7 + /qa SKILL.md Reachability 紀律；Codex Cross-Review R1 PASS；2a63236 ✅ Done**

> **Session S24 補充（2026-06-30）：INFRA-REACH-01-FIX-AI3 cross_review.sh §3.5 PROMPT_HEADER 硬接線 + CROSS_REVIEW.md 文件補完；Codex Cross-Review R2 PASS（Guard Input Reachability: 無新 Guard 明確輸出）；K5-Rule 29 收割；3028bd1 ✅ Done**

> **S31（2026-07-05）：✅ INFRA-CR-PLANS-DIFF-01 Done（cross_review.sh ALLOWED-aware 排除，根治 plans/-scoped 空 diff bug · dogfood R1 PASS · 43a35fb）· 修好的完整審查揪出 SSOT-05 §4.3 type-drift（手動窄審曾遮蔽）→ 開 FIX-A**

| INFRA-SV-STATE-RECEIPT-01 | ✅ Done · Codex Cross-Review PASS（R1）· 拆 STATE_FILE 語意超載（關切 A round 記帳 vs 關切 B PASS attestation）· cross_review.sh PASS/ADVISORY 於 `clear_budget_state` 前寫 durable range-bound receipt 至 `.tmp/cross_review_pass/${TICKET_ID}`（不受兩 janitor 影響）· structural_verify.sh Check E re-anchor：讀 receipt 驗 VERDICT∈{PASS,ADVISORY} **且** DIFF_SCOPE 匹配當前 --post scope（mismatch→BLOCKER false-open 防線）· **AC-1 真實閉環**：Step 5 --post Check E ✅ 綠（解 SV-CHECK-E-REGRESSION）· 禁區零改（AC-3）· Build/Test/Lint N/A（bash -n + Check E 自測 5/5）· 3eae2b5 |

> **S32（2026-07-06）：✅ TRACTION-SSOT-05-FIX-A Done（R-T19 Option C §4.3 FoodCompositionScore→levelScore，解母票 R3 type-drift BLOCKER · Codex R1 PASS ssot_context 注入 · 0757dfc）→ 母票+FIX-A 同移 deliver/（reflog 閉合）**
> **S33（2026-07-06）：✅ INFRA-SKILLS-UC01 Done（/uc 新增「## 驗證性動作豁免」：執行 .sh MUST 用 canonical 格式，MUST NOT 憑記憶組裝參數）· Codex Cross-Review PASS · 47fe32d**
> **S34（2026-07-06）：⚠️ INFRA-REACH-02 8/9 AC（Reachability v2 AI-1~7 落地 + CSO M-1 後 Check G 標準化重構 + R-1 迴歸 + Codex R2 ADVISORY 46b8e24）→ 1 AC（第二 state holder 正向命中）待 Akira 裁決，票暫留 Pending/ → K4-50「AC 語義折損律」候選登記**
> **INFRA-XX（S28）稽核歸檔**：K4-3/6/8/9 經 grep 核查目標檔案均無對應條文，已改列 🔴 待補寫；KAIROS-BACKFILL-01（S28）已補寫落地。

> **S39-S40（2026-07-08~09）：✅ GAP-06-RENDER-DOT-01 Done（FIX-A 四閘全綠 · AI-1 Aligned 分支疊加 dot Canvas + AI-2 androidTest +4 · Codex R2 PASS + Traycer R2 PASS · `d04fe4a..504d49a` · targetPosition = GAP-06-TARGET placeholder · 已移 deliver/，Akira 2026-07-09 授權 CLOSURE）**

> **S41（2026-07-10）：✅ STATE-CONTRACT-03 Closed（GAP-07 afState producer wiring · CameraViewModel L2117 Focusing + L2137-2143 Locked/Failed · AI-2 整合測試 3/3 · Codex xhigh PASS · `09d825e` · [實機]煙測 PASS（Pixel 9a · Guard7a ×8）· AC-10 假過根因除 · Traycer R4 Path A APPROVE · 錨 `7e99f28`）**

> **D15 KAIROS 節點（2026-07-12）**：M3 Family Provenance 稽核 fresh 刷新（0 escalate，214 掃描/187 已登記）→ 三層回顧完成，5 議題收編為候選規則 K4-56~K4-60（`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`）：**K4-56** 共享視覺常數家族 sweep-or-register 律（正式收編 K4-η，SC-04/SC-05 二次同構分流已達收編門檻）· **K4-57** 雙真理源消費者掃描完備性律（正式收編 K4-ζ，SC-01 未掃 legacy `compositionResult.isAllAligned` 消費鏈之根因追溯）· **K4-58** 雙法複證律（正式收編 K4-ε）· **K4-59** 預簽決策樹保底出口律（降低域外裁決重複 Checkpoint 往返成本）· **K4-60** gate-integrity vs adjacent-scope blocker 分類律（SC-04 R1 Blocker 1 正確 spawn SC-05 為範例判例）。Decision Audit：SC-04 K4-η STOP 攔截 ✅ 正確（域外命中未誤修）；SC-01 未掃雙真理源 ⚠️ 需微調（已轉 K4-57 補防護，SC02-5 追蹤遷移）。SSOT Drift：無新增。Skill 壓測：`/dream` 本次為 D15 首跑；`m3_family_audit.sh` self-test + refresh 均 PASS；`/uc`/`/compact`/`/mdlint` 執行次數仍缺 telemetry，資訊不足無法判定。CLAUDE.md 新增建議：暫不直接併入本文（12 條上限已滿），K4-56/57/60 建議下輪隨 path-scoped rules 收編評估。**

> **S45（2026-07-13）：✅ STATE-CONTRACT-05 Done（Halo 半徑係數重錨 `HALO_RADIUS_SCALE` 0.6f→0.5f，SSOT §2.5.4，承接 Traycer SC-04 R1 Fix 1）· ContractTest C-ρ1 關係式取代裸數字快照（K4-54 同源紀律）· K4-η 首輪 13 命中裁決終態：白名單 ×1（`AmbientConstants.kt:125` radial-gradient 色標）+ PTF register-out ×3（`HaloGuidanceLayer.kt:264/276/291`，已定性待 SC-06 承接，見 Postponed #7）+ 語義域外灰 ×7 · Train② STOP→CSO 復工令補遺（HC#4/#5）後解除，PTF 三處零 byte 觸碰 · Build/Test/Lint 全綠 · `cad0ce6`（僅 `GuidanceStateConstants.kt` + `GuidanceStateContractTest.kt`）· R2 重送包（SC-04+SC-05 同錨）已組裝，待 Traycer 複驗）**

> **S47（2026-07-13）：✅ GAP-10-BREATH-RESTART Done（uc AI-1+AI-2 `HaloGuidanceLayer.kt` ptfBreathAlpha/normalBreathAlpha 兩處 `InfiniteRepeatableSpec` 補 `repeatMode = RepeatMode.Reverse`，比照既有 breathe-dot 範本；未變動 alpha 數值/週期/easing · 205/205 tests PASS · 0 new lint warning · ELF ALIGNED · Codex Cross-Review R1 PASS（無新 Guard，REACHABILITY: NONE）· `0acabd6`（code）+ `3e8d43e`（docs，hook 強制拆分）· **[實機] AC 待 D17 Akira 肉眼終審**，BREATH-KILL 預授權出口已知會，若肉眼仍判假則不需二審直接觸發回退）**

> **S48（2026-07-14）：✅ INFRA-HOOK-STOP-01 Done（`telegram_stop.sh` 消除 `set -euo pipefail` 靜默 `exit 1`：新增 `extract_ticket_id` 結構式 parser（`ticket_pattern.md` family-agnostic grammar），統一取代 SV/QA 分支舊 hardcoded family allowlist；新增 `telegram_stop_test.sh` 4/4 PASS 無網路回歸測試（原始 bug payload 重現案例含其中）· 205/205 tests PASS · 0 new lint warning · ELF ALIGNED · Codex Cross-Review R1 PASS（無新 Guard，REACHABILITY: NONE）· `70a34ec` · **[實機] AC ✅ 已驗證（新 Session 確認 `Stop hook error` 未再出現，Akira 2026-07-14 回報）**）**

> **S49（2026-07-14）：✅ INFRA-SKILLS-UC02 Done（`uc/SKILL.md` Ticket DONE 改為 QA 交棒信號 AI-1 + QA/SV Auto-Chain ownership 鎖定 AI-2（`/uc` 不得直呼 `/sv`）+ 非 DONE 動作停止語義保留 AI-3 · 25L diff（14+/11-）< MAX_LINES 45 · DIFF_REF 依 `verify_checklist.sh` STALE_DIFF_REF 建議由 `3e8d43e` 校正為 `5c2aa24`（簽票後 2 個無關 commit 落地）· 205/205 tests + 0 new lint + ELF ALIGNED · Codex Cross-Review R2 PASS（REACHABILITY: NONE；R1 因 sv_report.md 空 Excluded Payload 佔位項觸發 structural_verify Check C REPORT_FIX，移除後 R2 PASS）· `6b74eab` · 免 Traycer（純 prompt-layer 治理票，Codex PASS 即閉環，比照 [[governance-ticket-traycer-exempt]] 前例）· **AC2-5[可達性/行為性] 依 K5-Rule 29 誠實標 PARTIAL（僅驗證 Payload Emitter 層，Reviewer/Consumer 層待下次真實 `/uc` Ticket DONE 觸發實測）**）**

> **S50（2026-07-14）：✅ GAP-11-PORTRAIT-EMA 碼閉環（SSOT v4.0.7 §2.5.1（:536-541）Portrait bbox EMA `smoothed = last * 0.7 + new * 0.3` 補實作——定性為「已規格未實作」（`PORTRAIT_EMA` 原全專案零命中），根因錨 D17 `[BHA-01]` logcat 1354 筆（ML Kit bbox w/h 在 9 個量化值間切換，±4.79% → Halo 環半徑脈動）· AI-1 `GuidanceStateConstants.PORTRAIT_EMA_HISTORY/NEW` 常數家族落籍 + AI-2 `CameraViewModel.portraitEmaStep()` companion 純函數（單一真理源；`smoothGuidanceBboxes()` 對 bbox 四邊逐一委派，production 樹遞推式零內聯副本，grep 證明）+ EMA 狀態以 ML Kit `trackingId` 為鍵（人臉丟失／身份切換即棄史，防幽靈拖影）+ AI-3 ContractTest C-ε1 權重守恆關係式（`history + new == 1.0f`，K4-54 範式）· AC-2 行為測試以 9 值量化跳動序列驅動 production 遞推，斷言 SSOT 遞推式 + 波幅收斂；**純 Float 設計結構性規避 K2-Rule 13**（若建於 `RectF`，JVM stub 四邊全回 `0f` → `0*0.7 + 0*0.3 == 0` 恆真，測試綠燈但零驗證力）· HC1 EyeAF 輸入零平滑（`eyeAfController.process(faces = landmarkFaces)` 維持 raw list，K4-Rule 28 Branch A/B 分離成立）· HC2 全管線單一平滑點 · HC3 tap-focus／`AnalyzeLightingUseCase` 零觸碰 · +108L < MAX_LINES 110 · 207/207 tests + 0 lint error · `64b13a2` · **CSO Amendment A1 兩強制條款（單一真理源／AC-2 語義釘死）均已履行**；**兩顆 [實機] AC（人臉靜止 60s 波動 <±1% · 丟失→重入無拖影）併入 D19 實測腳本** · 附帶義務：`PTF_EMA_ALPHA=0.3f`（CameraViewModel.kt:140）與 Portrait 常數家族遞推式同構但消費 branch／平滑量／SSOT 血統三者分歧，合併需跨 branch 統一裁決 → 僅記錄不動碼，第三組 EMA 常數增殖風險已登記）**

> **歸檔（S50 併行）：`SSOT-V407-SWEEP-01` ✅ Done（CLAUDE.md 現行 SSOT 指針 v4_0_6→v4_0_7 · Codex Cross-Review PASS · `6a08bed` · 已移 deliver/）· `SSOT-V407-01` ✅ Done（SSOT v4.0.6→v4.0.7 七簇條文落地 · Codex Cross-Review PASS · `a881096` · 已移 deliver/）——依 `ssot-governance.md` Dashboard 紀律自 PROGRESS.md 最近 Ticket 表移出（保持 ≤80 行）**
