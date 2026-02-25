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
- ✅ Aspect Ratio Toggle (4:3/16:9/1:1) + UseCaseGroup + ViewPort
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

### v2.11.4 (2026-02-25) 🚧 Pixel 9a 驗證中（v2.11.4-08）
- **Branch**: `feature/camera-core-v2.11.0`
- **Latest Commit**: pending (v2.11.4-08)
- **Build Code**: `80` (versionName `2.11.4-08`)
- **Status**: 🚧 **待 Pixel 9a 驗證** — v2.11.4-08 L2 Refined approach（AeAfLockState enum + CameraState.OPEN）

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
| **v2.11.4-08** | pending | L2 Refined: AeAfLockState enum + CameraState.OPEN + sessionId | ⏳ Pending |

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
  - Aspect Ratio Toggle (4:3/16:9/1:1) + UseCaseGroup + ViewPort
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

**最後更新**: 2026-02-13
**維護者**: Claude Sonnet 4.5 + Akira Chang
