# Cueshot MVP Sprint Progress

**Sprint**: D0-D15 (MVP)
**Last Updated**: 2026-04-11 by D3-T3 (CameraCharacteristics 焦距還債)
**Current Day**: D3 🔄 IN PROGRESS — D1 ✅ CLOSED；D2 ✅ CLOSED；D3-T0/T1/T2/T3 ✅ DONE；D3 ✅ CLOSED（待 Traycer 終審）
**Branch**: `feature/cueshot-mvp` (from v2.12.0-24 `bad1400`)
**Next KAIROS**: D6 (2026-04-16) — D2 KAIROS #1 已執行 (ADR 歸檔完成)

---

## 🎯 Sprint Health

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| On Schedule | D3 | D1 ✅ CLOSED；D2 ✅ CLOSED (Path A)；D3-T0/T1/T2/T3 ✅ DONE；D3 待 Traycer 終審 | 🟢 |
| Compile | 0 errors | 0 errors (DeviceDebug) | 🟢 |
| Unit Tests | 0 failed | 0 failed (ThirdsRule: 17pass; HeadroomRule: 12pass; AnalyzeComposition: 50t/48ignored/2pass; PtfState: 2pass; AnalyzeLighting: 20pass; BitmapPool: 2pass; Total: 104t/48skipped/56pass) | 🟢 |
| Lint New Errors | 0 new | 0 new (1 pre-existing in `CameraCapabilityDetector.kt`, D0 前存在) | 🟢 |
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
| D3-T3 | CameraCharacteristics 焦距還債：移除 `previewWidth ?: 800` 魔術數字；`LiveCameraSource` 在 camera binding 後讀取 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS` + `SENSOR_INFO_PHYSICAL_SIZE` 計算 factor；`CameraRepository` 介面新增 `getFocalLengthFactor()`；ViewModel 改用 `factor × previewWidth`，fail-safe 為 `previewWidth / 2f` | ✅ DONE (Path A) | `data/source/camera/LiveCameraSource.kt`, `domain/repository/CameraRepository.kt`, `data/repository/CameraRepositoryImpl.kt`, `presentation/camera/CameraViewModel.kt` |

---

## 📌 Blocking Issues

| Issue | Severity | Discovered | Resolution |
|-------|----------|------------|------------|
| **[D0.3] Traycer Path B — 可驗證事實不一致** | ~~🔴 HIGH~~ | 2026-04-09 | ✅ 2026-04-09 Trust-but-verify 覆測通過，D0.1 清創關閉 |
| **[D1] SV Path B — D1 未達驗收閉環** | ~~🔴 HIGH~~ | 2026-04-09 | ✅ 2026-04-09 FIX-01~06 全數閉環，Traycer Path A 放行 |

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

### KAIROS #2 — D6 (2026-04-15)
- (will be populated: ML Kit stability, Face Detection params, Halo smoothness)

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

---

## 🗄️ Standby Mechanisms (Phase 2 啟用)

- Merge SOP: `devspec/MERGE_SOP.md` — 多分支時啟用
- Branch Releases: `devspec/BRANCH_RELEASES.md` — 多分支時啟用
- 啟用條件：第二位開發者加入 / 產品上架後維護 / 跨版本回歸測試
