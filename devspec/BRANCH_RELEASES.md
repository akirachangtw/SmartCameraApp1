# 分支發布記錄

> **目的**: 記錄各功能分支的最終修正版，確保合併時使用正確的版本
> **更新頻率**: 每次分支達到穩定狀態時更新

---

## v2.10.0 系列

### v2.10.2-final (2026-02-09) ✅ 最終修正版
- **Tag**: `v2.10.2-final`
- **Commit**: `189cc85` (origin/feature/aspect-ratio-usecasegroup-v2.10.0)
- **Branch**: `feature/aspect-ratio-usecasegroup-v2.10.0`
- **Status**: ✅ **穩定，可安全合併**

**包含功能**:
- ✅ Aspect Ratio Toggle (4:3/16:9) + UseCaseGroup + ViewPort
- ✅ EXIF 資訊顯示 (GPS, 曝光參數, 裝置資訊)
- ✅ Settings UI 現代化 (可摺疊卡片, 位置權限管理)
- ✅ v2.10.2 Bug 修復 (Permission UX + Aspect Ratio + Toast Regression)

**關鍵提交**:
- `6501ac2`: [Fix] v2.10.2: Bug fixes - Permission UX + Aspect Ratio + Toast Regression
- `d7db1e5`: [Feature] v2.10.0: Add EXIF info + Photo info sheet + Location permission
- `7948186`: [Feature] v2.10.0: Aspect ratio WYSIWYG fix + cross-device validation
- `9c412ca`: [Feature] v2.10.0: UseCaseGroup + ViewPort aspect ratio implementation

**測試結果**:
- ✅ Pixel 9a: PASS
- ✅ Sony XQ-BT52: PASS
- 測試報告: `docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md`

**決議文檔**:
- `devspec/feature_record_Backlog.md` (Preview / Resolution / Aspect Ratio 章節)
- `devspec/feature_record_Backlog.md` (Photo Capture Feedback 章節)
- `docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md`
- `docs/test_results/v2.10.0-fix/CROSS_DEVICE_TEST_REPORT.md`

**已知問題**: 無

**合併到**:
- ✅ Merged: `feature/camera-core-v2.11.0` → v2.11.1 (2026-02-13, commit 70e1d67)

---

### v2.10.0 (2026-02-08) ⚠️ 已棄用
- **Tag**: `v2.10.0`
- **Commit**: `0440242` (feature/aspect-ratio-usecasegroup-v2.10.0, 本地分支)
- **Status**: ⚠️ **已棄用 - 缺少 v2.10.2 修復，請使用 v2.10.2-final**

**為什麼棄用**:
- 缺少 Permission UX 修復
- 缺少 Aspect Ratio 切換修復
- 缺少 Toast Regression 修復

**替代版本**: 使用 `v2.10.2-final`

---

## v2.11.0 系列

### v2.11.4 (2026-02-25) ✅ 已合併至 main
- **Branch**: `feature/camera-core-v2.11.0`
- **Stable Commit**: `e8427e4` (versionName `2.11.4`, versionCode `83`)
- **Latest Iteration**: `f6aba2a` (v2.11.4-10)
- **Merged**: `918894b` → `main` (2026-02-25, fast-forward)
- **Status**: ✅ **Tests 1-8 全部 PASS（Pixel 9a 2026-02-25）；已合併 main**

**功能目標**: AE/AF Lock UX + Camera2 Interop

**迭代歷程**:
| 迭代 | Commit | 內容 | Pixel 9a |
|------|--------|------|----------|
| v2.11.4-01 | `62dcd41`~`11b3c94` | Saliency AF fix + focus jump fix | — |
| v2.11.4-02 | `e09d4b7` | CONTROL_AE_LOCK=true via Camera2 | — |
| v2.11.4-03 | `4e64361` | CONTROL_AF_MODE=OFF (AF frozen) | — |
| v2.11.4-04 | `f98d9ec` | Fix unlock stuck (concurrent cancel) | ✅ PASS |
| v2.11.4-05 | `91333c5` | Single-tap unlock + screen lock auto-clear | ❌ FAIL (Test 5/6/7) |
| v2.11.4-06 | `8953dc8` | Fix double-unlock race + Camera2 persist + lens drift | ❌ FAIL (Test 6/7) + Bug 4 NEW |
| v2.11.4-07 | `5a73ec4` | Clear Camera2 Interop on Lifecycle STARTED (Bug 2+4) | ❌ FAIL (onStart before HAL ready) |
| v2.11.4-08 | `caa9d9b` | L2 Refined: AeAfLockState enum + CameraState.OPEN + sessionId | ⚠️ PARTIAL (guard too strict — race) |
| v2.11.4-09 | `98bcf67` | Fix CameraState.OPEN guard: LOCKED→auto-invalidate (race fix) | ✅ Test 6 PASS |
| **v2.11.4-10** | `f6aba2a` | Fix cancelFocusAndMetering deadlock (AF_MODE=OFF hang) | ✅ Test 6 PASS |

**v2.11.4-06 測試結果（Work NB 2026-02-25）**:
- ✅ Test 1–3, 5, 8 PASS（Bug 1 race 修復 ✅、Bug 3 focusDist 修復 ✅）
- ❌ Test 6 FAIL：`bindToLifecycle` bind-time clear 在螢幕解鎖時不觸發
- ❌ Test 7 FAIL：`AF_MODE=OFF` 殘留，第 2 次 lock 5s delay（Bug 4 新發現）

**v2.11.4-07 分析（onStart() 根因）**:
- ❌ `DefaultLifecycleObserver.onStart()` fires before HAL is ready → `clearCaptureRequestOptions()` silently ignored

**v2.11.4-08 修復內容（L2 Refined Architecture）**:
- ✅ **Bug 2** (螢幕解鎖後暗畫面)：`CameraState.OPEN` Flow observer — HAL 真正 OPEN 後才 clear（更可靠）
- ✅ **Bug 4** (5s delay)：保留 `lockAEAF()` pre-clear（FIFO 順序）
- ✅ **State Machine**：`AeAfLockState` enum（UNLOCKED/LOCK_REQUESTED/LOCKED/UNLOCK_REQUESTED）取代 boolean flag
- ✅ **Stale session guard**：`sessionId` 在每次 `bindToLifecycle()` 遞增，防止舊 session callback 污染狀態

**測試文件**:
- `docs/test_results/v2.11.4/HANDOVER_SUMMARY.md` (SSOT)
- `docs/test_results/v2.11.4/PIXEL9A_TEST_GUIDE_v2.11.4-06.md`
- `docs/test_results/v2.11.4/logcat_v211406_20260225.log` (v2.11.4-06 FAIL 證據)

**Critical Warnings**: 見 `devspec/feature_record_Backlog.md` → AE/AF Lock — Camera2 Interop section（5 個坑點）

---

### v2.11.3 (2026-02-21) ✅ 穩定
- **Branch**: `feature/camera-core-v2.11.0`
- **Commit**: `bbd679c`
- **Build Code**: 76（未在 build.gradle 中更新，以 commit 為準）
- **Status**: ✅ **穩定 - Pixel 9a 驗證**

**包含功能**:
- ✅ **logicCenter/trackedCenter 架構分離（Phase 1）**
  - `logicCenter`: unclamped 真實位置 → detectFeatures() ROI 中心
  - `trackedCenter`: clamped UI 輸出（maxDistancePerFrame 速度限制）
- ✅ **2-pass RANSAC**（2-cluster median≈0 修復）
- ✅ **Fix H**: `trackFromBitmap` pending apply
- ✅ **LK error filter**: 過濾錯誤的 LK 追蹤點
- ✅ **Config tuning**: minQuality 0.3→0.2, maxTrackingDistance 200→250, maxDistancePerFrame 25→40

**關鍵提交**:
- `bbd679c`: [Fix] v2.11.3: OF tracking stability — logicCenter/trackedCenter split + 2-pass RANSAC

**測試結果**:
- ✅ Pixel 9a: 951 lines, **0 LOST events**, quality=0.97-0.98, dist≈0px (stationary)

**決議文檔**:
- `devspec/feature_record_Backlog.md` (Optical Flow Tracking 章節)
- `docs/features/v2.11.3/` (如有)

**已知問題**: 無

---

### v2.11.2 (2026-02-18) ✅ 穩定
- **Branch**: `feature/camera-core-v2.11.0`
- **Commit**: `fec8296` (Fix G, 最終修正)
- **Build Code**: 75（未在 build.gradle 中更新，以 commit 為準）
- **Status**: ✅ **穩定 - Pixel 9a 驗證**

**包含功能**:
- ✅ **Bug A**: `CameraViewModel.lockAEAF()` — 停止 OF + 單一原子 state update
- ✅ **Bug B**: `LiveCameraSource.lockAEAF()` — 移除 `isFocusSuccessful` gate
- ✅ **Bug C**: `OpticalFlowConfig.DEFAULT` — redetectionInterval: 15→5, ransacDistanceThreshold: 0.5→1.0
- ✅ **Fix D**: `updateContentBounds()` rotation-aware 尺寸交換（portrait 正確映射）
- ✅ **Fix E**: `focusJob: Job?` 取消機制 + `lockAEAF` focusPoint 還原
- ✅ **Fix F**: `onDetectionResult` 收到有效 imageWidth 時重建 CoordinateTransformer
- ✅ **Fix G**: OF 追蹤座標比例錯誤修正

**關鍵提交**:
- `6fd95d1`: [Fix] v2.11.2: AE/AF lock coordinate + animation + OF tracking stability
- `fec8296`: [Fix] v2.11.2: Fix OF tracking coordinate scale error (Fix G)

**測試結果**:
- ✅ Pixel 9a: Tap→OF tracking 跟隨主體，Lock 黃框正確出現

**決議文檔**:
- `devspec/feature_record_Backlog.md` (Camera Core Features 章節)
- `docs/features/v2.11.2/Pixel9_Focus-tracking-lock-function-analysis.md`

**已知問題**: 無

---

### v2.11.1 (2026-02-18) ✅ 穩定
- **Branch**: `feature/camera-core-v2.11.0`
- **Commit**: `8c86068` (Focus Indicator 最終修正)
- **Build Code**: 74
- **Status**: ✅ **穩定 - Pixel 9a 驗證**

**包含功能**:
- ✅ **v2.11.0 功能**:
  - Eye-AF (眼睛優先自動對焦) - ML Kit Face Detection
  - Smart Subject Focus (智能主體對焦) - Saliency-based
  - Rectangle + Corner Brackets 視覺設計 (Cyan #00E5FF)
  - Neon Glow 雙層輝光效果
- ✅ **v2.10.2 功能**:
  - Aspect Ratio Toggle (4:3/16:9) + UseCaseGroup + ViewPort
  - EXIF 資訊顯示 (GPS, 曝光參數, 裝置資訊)
  - Settings UI 現代化 (可摺疊卡片, 位置權限管理)
  - v2.10.2 Bug 修復 (Permission UX + Aspect Ratio + Toast Regression)
- ✅ **UI 清理**: 移除 Debug 文字、Pixel 9a 記憶體訊息、長寬比單行標籤
- ✅ **Focus Indicator 4 視覺語言**: 機械咬合 3-phase tween + Dwell 淡化 + 顏色 + Glow
- ✅ **座標系統統一**: CoordinateTransformer + PreviewViewportContainer

**關鍵提交**:
- `1f404fe`: [Feature] v2.11.1: Coordinate system unification + UI cleanup (Pixel 9a verified)
- `27474a2`: [Feature] v2.11.1: Focus Indicator 3-phase tween + Dwell animations
- `8c86068`: [Feature] v2.11.1: Fix Focus Indicator animations (Pixel 9a verified)

**測試結果**:
- ✅ Pixel 9a: PASS (Focus Indicator 視覺語言、座標系統、UI 清理)
- ✅ Emulator: PASS (無 FATAL/ERROR)

**決議文檔**:
- `devspec/feature_record_Backlog.md` (Camera UI 清理, Focus Indicator Visual Design 章節)
- `docs/features/v2.11.0/v2.11.0_FINAL_SALIENCY_AND_VISUAL_DESIGN.md`

**已知問題**: 無

---

## v2.12.0 系列

### 當前狀態 (2026-03-19) 🚧 開發中
- **Branch**: `feature/camera-core-v2.12.0`
- **Latest Commit**: `14501ae` (v2.12.0-23 HEAD)
- **Status**: 🚧 **開發中 — 對焦系統精煉 + IMU 陀螺儀補償 + Clean Architecture 重構進行中**

**功能目標**:
- Eye-AF 動態節流 + 固定焦距旁路（Pixel 9a 前鏡頭）
- AF 防禦修復（Session ID + 狀態機守衛 + FocusIndicator 雙路徑）
- Focus Pipeline V2（Clean Architecture DI + Legacy pipeline 移除）
- IMU 陀螺儀補償（`ImuCompensator` → `OpticalFlowTracker` 整合）
- Interface Segregation（`DeviceSensorManager` / `OrientationSensorManager` 分離）

---

#### v2.12.0 完整迭代歷程

| 迭代 | Commit | 內容摘要 | 測試狀態 |
|------|--------|----------|----------|
| v2.12.0-01 | `c1646fd` | 移除 RATIO_1_1 + analysisSelector cap ≤640×480 | — |
| v2.12.0-02 | `28b501f` | 移除 analysisSelector setAspectRatioStrategy（Sony HAL Quirk 修復）| — |
| v2.12.0-03 | `bdc65c0` | CameraViewModel Adaptive EWMA Throttle（Device mode `.sample(100ms)`）| — |
| v2.12.0-04 | `6e5cd67` | FocusIndicator 自適應框 48-120dp（ML Kit bbox）+ VISUAL_PADDING_SCALE 1.5 | ✅ Emulator + Sony + Pixel 9a |
| v2.12.0-05 | `6ed13e4` | Eye-AF `AfTriggerThrottleController` + `withTimeout(3s)` + 4-way `AfState` + `CancellationException` fix | — |
| v2.12.0-06 | `9e2280b` | AF UI 修復：FLAG_AE + grow-only frame + AR-preserving clamp + per-type scale | — |
| v2.12.0-07 | `aabb54c` | Fixed-focus bypass（Pixel 9a 前鏡頭）+ `FixedFocusDetector` + `ObjectTrackingPipeline` + `FocusUiSmoother`（ref impl）| — |
| v2.12.0-08 | `99b5586` | Focus frame AR clamp — uniform scale-up 保持長寬比 | ✅ PASS Sony 2026-03-06 |
| v2.12.0-09 | `fa28f3f` | `OperationCanceledException` → Idle（no backoff）| ✅ PASS Sony 2026-03-06 |
| v2.12.0-10 | `10a1536` | Sony Bug A+B：`resolveTapTarget` + 72dp generic tap + frame state reset | ⚠️ PARTIAL（TC-06a FAIL）|
| v2.12.0-11 | `17d6f91` | AF 防禦：`isFixedFocusLens` StateFlow + grow-only 移除 + Suspended 模式 | ❌ FAIL — 2 新 Bug（2026-03-10）|
| v2.12.0-12 | （整合於 `f13462f`）| Bug #1: `isFocusIndicatorLocked` + Bug #2: `resetFocusState()` + HAL guard | ✅ Build PASS |
| v2.12.0-13 | `f13462f` | T1: bbox 原子更新 + T2: FocusIndicator 雙路徑分離 + T3: Session ID 第二道防線 | ✅ Build PASS — 待實機驗收 |
| v2.12.0-14 | `7187406` | T1: Coordinate Truth（移除 3 個 legacy fallback）+ T2: Release `USE_PREVIEWVIEW_DISPLAY=true` + SV-F1: `resolvedH` guard | ✅ Build PASS — 待實機驗收 |
| v2.12.0-15 | `96b975d` | FocusModule DI + `FOCUS_PIPELINE_V2` + Dumb Pipe + T1-T3 AF Hunting fix + Phase 2 FocusOverlay + SV-BatchA/B | ⚠️ PARTIAL PASS Sony 2026-03-14 |
| v2.12.0-16 | `69c5448` | Epic Surgery：移除 legacy focus pipeline + 消除 split-brain | ✅ Build PASS |
| v2.12.0-17 | `0660a97` | 新增本地 Skills（`/sv` + `/qa`）+ 最高憲法開發紀律（devspec 文件）| — |
| v2.12.0-18 | `287b7bc` | Phase 3 Professional Camera UX Polish | ✅ Build PASS |
| v2.12.0-19 | `74dc315` | ML Kit `LANDMARK_MODE_NONE` crash fix + `FocusIndicatorOverlay` wiring + Scorched Earth cleanup | ✅ Build PASS |
| v2.12.0-20 | `db234dc` | IMU Foundation：`DeviceSensorManager` gyroscope + `ImuCompensator` | ✅ Build PASS |
| v2.12.0-21 | `7a486a6` | Unified `GyroscopeData` namespace → Domain layer | ✅ Build PASS |
| v2.12.0-22 | `259b830` | `DeviceSensorManager` interface extraction（Clean Architecture Ticket 1.5）| ✅ Build PASS、SV PASS |
| v2.12.0-23 | `14501ae` | IMU compensation wired into `OpticalFlowTracker` pipeline（Fix 2A reset + 座標換算對齊）| ✅ Build PASS、SV PASS |
| v2.12.0-24 | `bad1400` | Interface Segregation：`OrientationSensorManager` 介面隔離，切斷 Presentation → Impl 直接依賴（Fix 3A-R）| ✅ Build PASS、SV PASS |

---

#### 關鍵測試里程碑

**v2.12.0-08/09（Sony XQ-BT52，Work NB 2026-03-06）**:
- ✅ Focus frame AR clamp：正方形框修復確認
- ✅ `OperationCanceledException` 不觸發 backoff 確認

**v2.12.0-11（2026-03-10）❌ FAIL**:
- **Bug #1**：AF 死鎖 — `resetFocusState()` 先 reset state 再 cancel，Race Condition 使 stale HAL callback 污染狀態機
- **Bug #2**：AF 永不鎖定 — P1-A guard `isFixedFocusLens && afState is Locked` 與 Race Condition 交互，`AfState.Locked` 被過早攔截
- 測試報告：`docs/test_results/v2.12.0/HANDOVER_SUMMARY.md`

**v2.12.0-15 Sony（2026-03-13）❌ FAIL → ⚠️ PARTIAL PASS（Fix T1-T3，2026-03-14）**:
- 回歸問題：AF Hunting（`lens=NotFocused` 循環）+ Yellow Lock 幾乎不觸發 + 鏡頭切換後框殘留 ~2s
- Fix T1-T3 後：Sony PARTIAL PASS，T3b 定點框 + AR 比例問題仍待後續 PDCA
- Logcat：`docs/test_results/v2.12.0/logcat_v2.12.0-15_sony_20260313.log`

---

#### 架構守則（v2.12.0 新確立，勿違反）

**v2.12.0-12 三條守則**:
1. `CameraState.isFocusIndicatorLocked = isAEAFLocked`（只有長按/鎖定圖示的 Yellow Lock，Eye-AF `AfState.Locked` 仍為 Cyan）
2. `resetFocusState()` 順序強制：`focusJob?.cancel()` → `_state.update(Idle)` → `eyeAfThrottle.reset()`
3. HAL success callback 必須通過 `afState !is AfState.Focusing` 守衛（防 stale callback 汙染）

**v2.12.0-14 四條守則**:
1. `CoordinateTransformer` 為唯一幾何真相來源，禁止回退 `CoordinateMapper`
2. `USE_PREVIEWVIEW_DISPLAY` release/debug 一致（均 `true`）
3. 座標票據修改禁止連帶改 UI 層（`CameraScreen.kt` / `FocusIndicator`）
4. `resolvedH` guard 必須對應高度變數 `effectiveImageHeight > 0`

**v2.12.0-23 IMU 積分守則**:
- `startTracking()` 第一行必須 `imuCompensator.reset()`（防 re-tap Coordinate Spike）
- 三點 reset 時機：`startTracking()` / `stopTracking()`-LOST / `switchCamera()`
- 座標換算：`compensatedDx = medDx - imuDx * scaleX`（downsampled space 對齊後相減）

**v2.12.0-24 Interface Segregation 守則**:
- `DeviceSensorManager`：專供 IMU 陀螺儀高頻積分（`startGyroscopeTracking()`）
- `OrientationSensorManager`：專供裝置方向旋轉（`startOrientationTracking()`，LevelIndicator）
- Hilt 多重綁定單例：兩個 `@Binds` 指向同一 `@Singleton DeviceSensorManagerImpl`，避免重複硬體 Sensor 註冊

---

**關鍵提交（完整索引）**:
- `c1646fd`: v2.12.0-01 analysisSelector cap
- `28b501f`: v2.12.0-02 Sony HAL fix
- `bdc65c0`: v2.12.0-03 EWMA throttle
- `6e5cd67`: v2.12.0-04 adaptive focus frame
- `6ed13e4`: v2.12.0-05 Eye-AF throttle + CancellationException fix
- `9e2280b`: v2.12.0-06 AF UI fixes
- `aabb54c`: v2.12.0-07 Fixed-focus bypass + ObjectTrackingPipeline
- `99b5586`: v2.12.0-08 Focus frame AR clamp
- `fa28f3f`: v2.12.0-09 OperationCanceledException fix
- `10a1536`: v2.12.0-10 Sony Bug A+B (final)
- `17d6f91`: v2.12.0-11 AF defensive fixes
- `f13462f`: v2.12.0-13 FocusIndicator dual path + bbox atomic + Session ID
- `7187406`: v2.12.0-14 Coordinate Truth + PreviewView Release alignment + SV-F1
- `96b975d`: v2.12.0-15 Phase 2 SV-BatchB (最終 fix 版)
- `69c5448`: v2.12.0-16 Epic Surgery (legacy pipeline 全移除)
- `287b7bc`: v2.12.0-18 UX Polish
- `74dc315`: v2.12.0-19 FocusIndicatorOverlay wiring
- `db234dc`: v2.12.0-20 IMU Foundation
- `7a486a6`: v2.12.0-21 GyroscopeData domain unification
- `259b830`: v2.12.0-22 DeviceSensorManager interface extraction
- `14501ae`: v2.12.0-23 IMU → OpticalFlowTracker pipeline
- `bad1400`: v2.12.0-24 OrientationSensorManager interface segregation (Fix 3A-R)

**決議文檔**:
- `devspec/feature_record_Backlog.md` → v2.12.0 對焦 / IMU / 架構各章節
- `docs/test_results/v2.12.0/HANDOVER_SUMMARY.md`（Work NB SSOT）
- `docs/test_results/v2.12.0/logcat_v2.12.0-15_sony_20260313.log`
- `plans/proposals/Camera_focus_specification_SSOT.md`（Cyan vs Yellow 規範）

**已知問題**:
- ⚠️ v2.12.0-15 Fix T3b：Sony 定點框 + AR 比例問題未完全解決，需帶 Logcat 回 Traycer `trigger-workflow` PDCA
- ⚠️ v2.12.0-13/14：Build PASS 但待實機驗收（部分問題已由 v2.12.0-15 Fix 覆蓋）
- ⚠️ v2.12.0-23：`focalLengthPx = 2000f` 為暫定常數，待後續從 `CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS` 動態讀取

---

## 版本命名規則

### Tag 命名格式
- **功能版本**: `v<major>.<minor>.<patch>` (例如: v2.10.0)
- **修正版本**: `v<major>.<minor>.<patch>-final` (例如: v2.10.2-final)
- **候選版本**: `v<major>.<minor>.<patch>-rc<number>` (例如: v2.11.0-rc1)

### 狀態標示
- ✅ **穩定，可安全合併**: 已通過所有測試，可直接合併到其他分支
- 🚧 **開發中**: 功能未完成或未完整測試
- ⚠️ **已棄用**: 不應使用，已有更新的版本替代
- 🔴 **有已知問題**: 存在 Bug 或問題，需修復後才能合併

---

## 合併檢查清單

在合併任何分支前，請確認：
1. [ ] 確認使用的是**最終修正版**（`-final` tag）
2. [ ] 閱讀 `devspec/feature_record_Backlog.md` 相關章節
3. [ ] 檢查測試報告，確認所有測試通過
4. [ ] 閱讀「已知問題」，評估影響
5. [ ] 執行 `devspec/MERGE_SOP.md` 的完整流程

---

**最後更新**: 2026-03-19
**維護者**: Claude Sonnet 4.6 + Akira Chang
