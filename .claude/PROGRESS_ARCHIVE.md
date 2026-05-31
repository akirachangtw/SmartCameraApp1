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
