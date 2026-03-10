# Handover Summary — v2.12.0 AF Focus Fixes

**Date**: 2026-03-06
**Branch**: `feature/camera-core-v2.12.0`
**Test Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Current Status**: ⏳ **Pending Work NB Testing — v2.12.0-08 + v2.12.0-09**

---

## What Was Built (Home NB)

This batch fixes two AF-related issues identified during v2.12.0 development:

| Commit | Version | Description |
|--------|---------|-------------|
| `99b5586` | v2.12.0-08 | Focus frame AR clamp — uniform scale-up preserves aspect ratio |
| `fa28f3f` | v2.12.0-09 | OperationCanceledException → Idle (no backoff) |
| `912ee00` | Refactor | Consolidate docs: remove test_handoff/ and test_plans/ |

---

## Problem Statements

### v2.12.0-08: Focus Frame Square Bug

**Root cause**: `coerceAtLeast(minDp)` applied independently on W and H.
When both dimensions < 48dp (e.g., far-away face), both floored to 48dp → perfect square.

**Fix** (`CameraScreen.kt` ~line 717):
```
Old: scaledW = (rawW * scaleDown).coerceAtLeast(minDp)  // independent floor → square
     scaledH = (rawH * scaleDown).coerceAtLeast(minDp)

New: scaleUp = if (downW < minDp || downH < minDp) minDp / minOf(downW, downH) else 1f
     (downW * scaleUp) to (downH * scaleUp)               // uniform → AR preserved
```

### v2.12.0-09: Spurious Backoff on Rear Camera Face

**Root cause**: `CameraControl.OperationCanceledException` (CameraX-internal, not HAL failure)
was caught by generic `else` branch → `eyeAfThrottle.onFailure()` → 2s backoff.

**Fix** (`CameraViewModel.kt` ~line 1067):
Check `ex is OperationCanceledException || ex?.cause is OperationCanceledException`
→ reset to Idle without penalising throttle.

### v2.12.0-10: Sony Bug A + Bug B Focus Frame State Reset

**Bug A** (Front cam fixed frame size across distances):
- Root cause: `focusFrameWidthPx/HeightPx` set to `0f` only in dwell-fade path; ML Kit branch
  (when `enteredMLKitTrackingMode=true`) skips dwell fade entirely — frame dims never reset
  between taps → grow-only `maxOf` in `AfState.Locked` keeps the first locked size forever.
- Fix: `resolveTapTarget()` returns `TapTargetInfo.Subject` with `screenBoxPx`; ML Kit branch
  immediately computes `initialW = screenBoxPx.width() * BBOX_SCALE_FACE` and sets it on tap.

**Bug B** (Rear cam retains face frame shape after non-face tap):
- Root cause: OF/Static branches never set `focusFrameWidthPx/HeightPx = 0f` — old face dims persist.
- Fix: OF and Static branches now explicitly set `focusFrameWidthPx = 0f, focusFrameHeightPx = 0f`
  + `isGenericTapActive = true` → `FocusIndicator` shows 72dp square (per L2 §2).

**Files changed**:
- `CameraViewModel.kt`: `resolveTapTarget()` + 3 tap branches updated + `BBOX_SCALE_*` in companion
- `CameraScreen.kt`: `FocusIndicator` signature adds `isGenericTapActive`; 72dp branch in size calc
- `CameraState.kt`: `isGenericTapActive: Boolean = false` field

---

## Test Instructions for Work NB

**Full test guide**: `docs/test_results/v2.12.0/v2.12.0_08_09_work_nb_execution.md`

### Quick Setup

```bash
git pull origin feature/camera-core-v2.12.0
./gradlew assembleDeviceDebug
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Logcat (Sony)
adb -s HQ616R2713 logcat -c
adb -s HQ616R2713 logcat -v time CameraViewModel:D LiveCameraSource:I *:S \
  > docs/test_results/v2.12.0/logcat_v2.12.0-10_$(date +%Y%m%d).log &
```

### Test Cases Summary

| TC | Description | Fix |
|----|-------------|-----|
| TC-01 | Rear cam, far face — frame is NOT square | v2.12.0-08 |
| TC-02 | Front cam, normal distance — frame is NOT square | v2.12.0-08 |
| TC-03 | Rear cam, close face — large frame, AR preserved | v2.12.0-08 |
| TC-04 | Rapid taps rear cam — no 2s backoff pause | v2.12.0-09 |
| TC-05 | Normal AF regression — lock/dwell works | regression |
| TC-06a | Front cam: frame SCALES with face distance | v2.12.0-10 Bug A |
| TC-06b | Rear cam: non-face tap shows 72dp square (not face shape) | v2.12.0-10 Bug B |

### Key Logcat Patterns

```
# v2.12.0-09 operating correctly (no backoff):
[v2.12.0-09] OperationCanceledException — reset to Idle (no backoff)

# v2.12.0-07 regression still working:
[v2.12.0-07] Fixed-focus lens — AE/AWB metering only at (x, y)

# v2.12.0-10 Bug A fix — initial frame set from bbox on tap:
ML Kit tracking started: ID=<n>, type=FACE

# v2.12.0-10 Bug B fix — generic tap resets frame:
resolveTapTarget: no target at image=(...) — Generic

# Normal AF success:
Focus successful at (x, y)
```

---

## Test Results — Sony XQ-BT52 (v2.12.0-08+09, 2026-03-06)

**Date tested**: 2026-03-06
**Tester**: Work NB
**Logcat**: `logcat_sony_tc06_bugAB.log`

| TC | Description | Result | Notes |
|----|-------------|--------|-------|
| TC-01 | Rear cam far face — AR preserved | ✅ PASS | |
| TC-02 | Front cam normal dist — AR preserved | ✅ PASS | |
| TC-03 | Rear cam close face — large AR frame | ✅ PASS | |
| TC-04 | Rapid taps — no backoff | ✅ PASS | OperationCanceledException caught correctly |
| TC-05 | Normal AF regression | ✅ PASS | |
| TC-06 | TC-06 bugs found — see v2.12.0-10 | ❌ FAIL | Bug A + Bug B (details in SONY_BUG_ANALYSIS.md) |

**Overall Result**: PARTIAL PASS → v2.12.0-10 built to fix TC-06 bugs

---

## Test Results — Work NB re-test (v2.12.0-10, 2026-03-09)

**Date tested**: 2026-03-09
**Tester**: Work NB (Sony XQ-BT52)

| TC | Description | Result | Notes |
|----|-------------|--------|-------|
| TC-01 | Rear cam far face — AR preserved | ✅ PASS (prev) | |
| TC-02 | Front cam normal dist — AR preserved | ✅ PASS (prev) | |
| TC-03 | Rear cam close face — large AR frame | ✅ PASS (prev) | |
| TC-04 | Rapid taps — no backoff | ✅ PASS (prev) | |
| TC-05 | Normal AF regression | ✅ PASS (prev) | |
| TC-06a | Front cam: frame scales with face distance | ❌ FAIL | 對焦框仍為固定大小（重啟 App 後穩定重現；初次測試觀察到的 1 秒消失為一次性現象） |
| TC-06b | Rear cam: non-face tap shows 72dp square | ✅ PASS | 非人臉點擊已顯示 72dp 正方形，不再殘留臉部形狀 |

**Overall Result**: ⚠️ PARTIAL — TC-06a Bug A 仍未修復，需重啟 PDCA 閉環

### Additional Observations (2026-03-09)

| 現象 | 場景 | 嚴重度 |
|------|------|--------|
| AF hunting — 持續搜尋對焦狀態 | 前後鏡頭對準人臉時均出現，未點擊自然等待中也會持續觸發 AF | ⚠️ 需根因分析 |

**Logcat files**:
- `logcat_v2.12.0-10_sony_20260309.log` — TC-06a/b 測試
- `logcat_v2.12.0-10_sony_af_hunting_20260309.log` — AF hunting 捕捉

---

## v2.12.0-11 AF 防禦修復 — 交接給 Work NB（2026-03-10）

**Date built**: 2026-03-10
**Commit**: `17d6f91`
**Branch**: `feature/camera-core-v2.12.0`
**Target device**: Sony XQ-BT52 (`HQ616R2713`) + Pixel 9a (`59131JEBF17244`)
**Test guide**: `docs/test_results/v2.12.0/v2.12.0-11_work_nb_execution.md`

### What Was Fixed (Home NB, 2026-03-10)

| Sub-fix | Description |
|---------|-------------|
| P0-B/C | `isFixedFocusLens: StateFlow<Boolean>` — Repository → CameraState 傳遞路徑 |
| P0-D | `autoFocusOnSalientTarget()` grow-only `maxOf` 移除 — UI 框直接跟隨 ML Kit bbox |
| P1-A | Fixed-focus 永久鎖定分流 — `AfState.Locked` 後跳過 `focusOnPoint()` |
| P1-B | `switchCamera()` 補強 — `afState=Idle` + `eyeAfThrottle.reset()` |
| P2-A | `AfTriggerThrottleController` Suspended 模式（連續 3 次失敗 → 暫停） |
| P2-B | Hysteresis 2.5x 擴大（`AfState.Locked` 時 54px → 135px） |
| P2-C | UserTap 解除 Suspended — `focusOnPoint(isUserTap=true)` 呼叫 `eyeAfThrottle.reset()` |

### Test Cases for v2.12.0-11

| TC | Description | 預期行為 | 驗證版本 |
|----|-------------|---------|---------|
| TC-06a-v2 | 前鏡頭：對焦框隨人臉距離縮放 | 框大小跟隨人臉 bbox，不固定 | P0-D + P1-A |
| TC-06b | 後鏡頭：非人臉點擊顯示 72dp 正方形 | 72dp 正方形（已 v2.12.0-10 PASS） | Regression |
| TC-07 | AF Hunting 消除（前鏡頭） | Logcat 出現 `Fixed-focus Locked — UI-only update`，無持續 AF 觸發 | P1-A |
| TC-08 | AF Hunting 消除（後鏡頭） | Logcat 出現 `Hysteresis gate (Locked)` 阻擋微抖動，AF 不過度觸發 | P2-B |
| TC-09 | Suspended 模式（後鏡頭遮蔽 → 連續失敗） | Logcat: `Auto-retry suspended (failCount=3)` | P2-A |
| TC-10 | UserTap 解除 Suspended | Tap 後 Logcat: `UserTap: eyeAfThrottle.reset()` | P2-C |
| TC-11 | 鏡頭切換後 AF 狀態乾淨 | 切換後無 Locked/Failed 殘留，`switchCamera: eyeAfThrottle.reset()` | P1-B |
| TC-05 | Regression：正常 AF lock/dwell | 人臉 → 自動對焦 → Locked → dwell 動畫 | Regression |

### Key Logcat Patterns for v2.12.0-11

```
# P1-A: 前鏡頭 fixed-focus Locked 後跳過 HAL
[v2.12.0-11] Fixed-focus Locked — UI-only update, skipping focusOnPoint

# P1-B: 鏡頭切換重置
[v2.12.0-11] switchCamera: eyeAfThrottle.reset() + afState=Idle

# P2-A: Suspended 模式觸發
[v2.12.0-11] Auto-retry suspended (failCount=3) — awaiting UserTap

# P2-B: Hysteresis 阻擋微抖動（DEBUG log）
[v2.12.0-11] Hysteresis gate (Locked): delta=XXpx < 135px — skip

# P2-C: UserTap 解除 Suspended
[v2.12.0-11] UserTap: eyeAfThrottle.reset() — Suspended state cleared

# StateFlow 確認（啟動時）
[v2.12.0-11] isFixedFocusLens updated: false → CameraState synced   ← Sony 後鏡頭
[v2.12.0-11] isFixedFocusLens updated: true  → CameraState synced   ← 前鏡頭（Pixel 9a）
```

### Test Results — Sony XQ-BT52 (v2.12.0-11, 2026-03-10)

**Date tested**: 2026-03-10
**Tester**: Work NB (Sony XQ-BT52 `HQ616R2713`)
**Logcat**: `logcat_v2.12.0-11_sony_20260310.log`

| TC | Description | Result | Notes |
|----|-------------|--------|-------|
| TC-05 | Regression：正常 AF lock/dwell | ❌ FAIL | Locked 動畫 + α=0.4 淡出未觸發（FocusClamp isLocked=false UI 斷路） |
| TC-06a-v2 | 前鏡頭：框隨距離縮放 | ❌ FAIL | ViewModel bbox 值正確變化，但 Compose UI 未更新顯示（同根因） |
| TC-06b | 後鏡頭：72dp 正方形 | ✅ PASS | 非人臉點擊正方形框正常，regression 未發生 |
| TC-07 | 前鏡頭 AF Hunting 消除 | ⚠️ PARTIAL | 核心邏輯 PASS（`Fixed-focus Locked — UI-only update` 持續，無 Focus fail/success 循環）；Locked 動畫 FAIL（同根因） |
| TC-08 | 後鏡頭 Hysteresis 抑制 | ✅ PASS | `Hysteresis gate (Locked): delta=Xpx < 135px — skip` 全程正常攔截 |
| TC-09 | Suspended 模式觸發 | ⏭️ SKIP | Sony AF 能力強，無法製造連續 3 次失敗 |
| TC-10 | UserTap 解除 Suspended | ⏭️ SKIP | 前置條件 TC-09 未觸發 |
| TC-11 | 鏡頭切換 AF 重置 | ❌ FAIL（嚴重） | AfState.Locked 跨切換殘留 Race Condition，前鏡頭 HAL AF 死鎖需重啟 App |

**Overall Result**: ❌ FAIL — 2 項新 Bug 發現，需重啟 PDCA 閉環

---

### Test Results — Pixel 9a (v2.12.0-11, 2026-03-10)

**Date tested**: 2026-03-10
**Tester**: Work NB (Pixel 9a `59131JEBF17244`)

| TC | Description | Result | Notes |
|----|-------------|--------|-------|
| TC-05 | Regression：正常 AF lock/dwell | ❌ FAIL | 同 Sony Bug #1（FocusClamp isLocked=false UI 斷路） |
| TC-06a-v2 | 前鏡頭：框隨距離縮放 | ❌ FAIL（推斷） | 同根因，未個別執行 |
| TC-06b | 後鏡頭：72dp 正方形 | ✅ PASS（推斷） | Sony 已驗證，Pixel 9a 同版本 |
| TC-07 | 前鏡頭 AF Hunting 消除 | ⚠️ PARTIAL（推斷） | 同根因 |
| TC-08 | 後鏡頭 Hysteresis 抑制 | ✅ PASS（推斷） | 同根因邏輯正常 |
| TC-09 | Suspended 模式觸發 | ⏭️ SKIP | Pixel 9a AF 能力強，同 Sony 無法觸發 |
| TC-10 | UserTap 解除 Suspended | ⏭️ SKIP | 前置條件未觸發 |
| TC-11 | 鏡頭切換 AF 重置 | ❌ FAIL（嚴重） | Race Condition 重現：`AfState@3e6b26f` 切換後仍 Locked，前鏡頭 AF 死鎖 |

**Overall Result**: ❌ FAIL — Bug #1 + Bug #2 雙裝置均重現，問題為系統性

**附加觀察（Pixel 9a）**:
- Saliency score 門檻問題：臉部靠近畫面上方時 score=48.2 < 50f，Eye-AF 不觸發（需靠近畫面中央才能過門檻）

---

### Bug Report — v2.12.0-11 新發現（2026-03-10）

#### Bug #1：FocusClamp isLocked=false UI 斷路（影響 TC-05、TC-06a-v2、TC-07）

**現象**：`AfStateMachine` 內部 `AfState=Locked` 正確，但 UI 層 `FocusClamp` 收到 `isLocked=false`
- Locked 動畫（機械夾爪）❌ 未觸發
- Dwell 計時器 ❌ 未啟動
- Alpha 淡出至 0.4 ❌ 未發生
- 對焦框大小固定（TC-06a-v2：ViewModel bbox 正確變化，UI 未更新）

**Logcat 關鍵行**：
```
[v2.12.0-11] UI frame update (AfState=AfState$Locked@f050c37)  ← 內部 Locked ✅
FocusClamp: LaunchedEffect(focusSuccess=true, isLocked=false)   ← UI isLocked 未同步 ❌
FocusClamp: Clamp RESET (focusSuccess=null, isLocked=false)     ← 動畫被重置 ❌
```

**修復方向**：`FocusClamp` 的 `isLocked` 參數來源需對應 `AfState is AfState.Locked`

---

#### Bug #2：switchCamera() Race Condition — 前鏡頭 AF 死鎖（TC-11）

**現象**：後鏡頭對焦 Locked 後切換前鏡頭，前鏡頭 HAL AF 永久失效（需重啟 App 才能恢復）

**根因 Race Condition**：
```
1. 後鏡頭 in-flight focusOnPoint() coroutine 等待 HAL 結果
2. 用戶切換前鏡頭 → switchCamera() 執行 afState = AfState.Idle ✅
3. 【Race】in-flight coroutine 此時回傳 Success
   → afState = AfState.Locked ← 覆蓋剛設定的 Idle！
4. 前鏡頭啟動，isFixedFocusLens=true + afState=Locked
   → line 1466: Fixed-focus Locked → return（永久跳過 HAL AF）
5. 前鏡頭 AF 死鎖，無法自動恢復
```

**修復方向**：`switchCamera()` 必須先 **cancel** 所有 in-flight `focusOnPoint()` Job，再執行 `afState=Idle`

---

## v2.12.0 Full Roadmap Status

| Version | Content | Status |
|---------|---------|--------|
| v2.12.0-01 | Remove 1:1 AR + analysisSelector cap | ✅ |
| v2.12.0-02 | Remove analysisSelector setAspectRatioStrategy | ✅ |
| v2.12.0-03 | CameraViewModel adaptive throttle (EWMA) | ✅ |
| v2.12.0-04 | Adaptive focus frame 48-120dp + VISUAL_PADDING_SCALE 1.5 | ✅ |
| v2.12.0-05 | Eye-AF throttle + 4-way AfState + CancellationException fix | ✅ |
| v2.12.0-06 | AF UI fixes: FLAG_AE + grow-only frame + AR clamp + per-type scale | ✅ |
| v2.12.0-07 | Fixed-focus bypass (Pixel 9a front cam) + FixedFocusDetector | ✅ |
| v2.12.0-08 | Focus frame AR clamp uniform scale-up | ✅ PASS (Sony 2026-03-06) |
| v2.12.0-09 | OperationCanceledException → Idle (no backoff) | ✅ PASS (Sony 2026-03-06) |
| v2.12.0-10 | Bug A+B fix: resolveTapTarget + 72dp generic tap + frame state reset | ⚠️ PARTIAL — TC-06a FAIL (Sony 2026-03-09) |
| v2.12.0-11 | AF 防禦修復：Fixed-focus 永久鎖定 + Suspended 模式（commit `17d6f91`） | ❌ FAIL — 2 新 Bug，需重啟 PDCA（2026-03-10） |
| v2.12.0 | Stable tag — dual-camera AF complete | ⏳ Pending v2.12.0-11 PASS |

---

## After Work NB Test

**If PASS**: Update results table above, then Work NB push:
```bash
git add docs/test_results/v2.12.0/
git commit -m "[Test] v2.12.0-08+09: Pixel 9a verification — PASS

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
git push origin feature/camera-core-v2.12.0
```

**If FAIL**: Document failure in results table + logcat filename, push, then notify Home NB.
