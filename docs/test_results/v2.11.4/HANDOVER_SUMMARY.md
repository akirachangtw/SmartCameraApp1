# Handover Summary — v2.11.4 Pixel 9a Test

**Date**: 2026-02-25
**Branch**: `feature/camera-core-v2.11.0`
**Test Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Handover**: Work NB → Home NB (for Bug 2 + Bug 4 fix)

---

## Current Status

| Iteration | Home NB | Work NB | Result |
|-----------|---------|---------|--------|
| v2.11.4-01~03 | ✅ Built | ✅ Installed | 부분 기능 확인 |
| v2.11.4-04 | ✅ Built | ✅ Installed | ✅ Unlock stuck fixed |
| v2.11.4-05 | ✅ Built | ✅ Tested | ❌ Test 5/6/7 FAIL + Observation 1 |
| **v2.11.4-06** | ✅ Built | ✅ **Tested 2026-02-25** | ❌ **Test 6/7 FAIL + Bug 4 NEW** |
| v2.11.4-07 | ✅ Built | ❌ FAIL (onStart before HAL) | onStart fires before HAL ready — clear ignored |
| v2.11.4-08 | ✅ Built | ⚠️ PARTIAL | Guard race: LOCKED state blocks clear on screen unlock |
| **v2.11.4-09** | ✅ Built | ✅ **Test 6 PASS 2026-02-25** | Full regression pending — **→ This handover** |

---

## All Commits in This Branch

| Commit | Version | Description |
|--------|---------|-------------|
| `62dcd41` | v2.11.4-01 | Fix saliency auto-focus never triggering when face present |
| `8ba3447` | v2.11.4-01 | Revert tracking animation to StiffnessHigh |
| `11b3c94` | v2.11.4-01 | Fix focus indicator jumping to face on tap |
| `e09d4b7` | v2.11.4-02 | True AE lock via CONTROL_AE_LOCK=true (Camera2 Interop) |
| `4e64361` | v2.11.4-03 | Add CONTROL_AF_MODE=OFF for permanent AF lock |
| `f98d9ec` | v2.11.4-04 | Fix AE/AF unlock stuck (concurrent cancelFocusAndMetering) |
| `6662121` | v2.11.4-05 | Single-tap unlock + auto-unlock on screen lock |
| `91333c5` | v2.11.4-05 | Fix: return@launch after unlock + unlockAEAF guard |
| `8953dc8` | v2.11.4-06 | Fix double-unlock race + Camera2 persist + lens drift |
| `ce078da` | v2.11.4-06 | Fix LENS_FOCUS_DISTANCE compile error |

**Latest commit (Work NB)**: `b74aeb1` (docs update)

---

## v2.11.4-06 Test Results (Work NB 2026-02-25) — FAIL

| Test | Result | Notes |
|------|--------|-------|
| Test 1: Lock applied (regression) | ✅ PASS | `[v2.11.4-06]` prefix + LENS_FOCUS_DISTANCE logged |
| Test 2: AF stays INACTIVE 30s (regression) | ✅ PASS | AF_STATE=INACTIVE held 30s |
| Test 3: AE stays locked (regression) | ✅ PASS | Overexposed in bright area — AE lock confirmed |
| Test 4: Rapid lock/unlock (regression) | ✅ PASS (with note) | Rapid cycles clean; but 2nd lock after any unlock has consistent 5s delay |
| Test 5: No double-cancel race | ✅ **PASS** | Bug 1 FIXED — only 1 `lock cleared` + guard `superseded` blocked 2nd call |
| Test 6: Screen lock clears Camera2 options | ❌ FAIL | Bind fix doesn't trigger on screen lock/unlock (no rebind) |
| Test 7: No long-press delay after screen unlock | ❌ FAIL | AF_MODE=OFF residual persists; 5s delay consistent after screen lock |
| Test 8: Preview stays sharp after lock | ✅ **PASS** | Bug 3 FIXED — focusDist maintained at converged value (not 0.000) |

**Evidence**: `logcat_v211406_20260225.log`, `PIXEL9A_TEST_REPORT_v2.11.4-06.md`

---

## Bug Summary After v2.11.4-06

| Bug | Severity | Status | Description |
|-----|----------|--------|-------------|
| Bug 1: Double cancelFocusAndMetering race | High | ✅ **FIXED** | `isAEAFLocked=false` before suspend call prevents 2nd coroutine |
| Bug 2: Camera2 settings persist after screen lock | High | ❌ **STILL FAILING** | `clearCaptureRequestOptions()` on bind only fires on process restart; screen lock/unlock does NOT rebind |
| Bug 3: focusDist=0.000 after CONTROL_AF_MODE=OFF | Medium | ✅ **FIXED** | `CONTROL_LENS_FOCUS_DISTANCE=lastFocusDistance` applied simultaneously |
| Bug 4: `cancelFocusAndMetering` intermittent failure | High | 🆕 **NEW** | `Camera is not active` thrown during tap-unlock transitions → `AF_MODE=OFF` residual → 5s delay on 2nd lock |

---

## What Needs to Be Tested in v2.11.4-08

### Fix Architecture (L2 Refined — Home NB)

**v2.11.4-07 failure analysis**:
- `DefaultLifecycleObserver.onStart()` fires **before** HAL is ready — `clearCaptureRequestOptions()` silently ignored
- Camera HAL re-opens after `onStart()`, re-applying stale Camera2 options → same Bug 2 symptom

**v2.11.4-08 solution (L2 Reactive Camera Controller)**:

```kotlin
// CameraState.OPEN fires only when HAL is truly ready for capture requests
val currentSession = ++sessionId
camera.cameraInfo.cameraState
    .asFlow()
    .filter { it.type == CameraState.Type.OPEN }
    .onEach {
        if (currentSession == sessionId && aeAfLockState == AeAfLockState.UNLOCKED) {
            Camera2CameraControl.from(camera.cameraControl).clearCaptureRequestOptions()
        }
    }
    .launchIn(sourceScope)
```

**Additional improvements**:
- `AeAfLockState` enum (UNLOCKED/LOCK_REQUESTED/LOCKED/UNLOCK_REQUESTED) replaces boolean flag
- `sessionId` stale callback guard — increments each `bindToLifecycle()`, discards old callbacks

### Verification Commands (after fix)

```bash
git pull origin feature/camera-core-v2.11.0

# Build device APK
./gradlew :app:assembleDeviceDebug

# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Start Logcat (watch for [v2.11.4-08] tags)
adb -s 59131JEBF17244 logcat -v time CameraViewModel:I LiveCameraSource:I *:S
```

**Expected logcat on screen unlock**:
```
[v2.11.4-08] Camera2 options cleared on CameraState.OPEN (session #N)
```
**NOT expected (would indicate issue)**:
```
[v2.11.4-08] CameraState.OPEN — skip clear: ...state=LOCKED
```

### Test Focus for v2.11.4-08

| Test | Check Point |
|------|------------|
| **Test 6** ★ | Screen lock/unlock → yellow box gone + preview normal brightness |
| **Test 7** ★ | After screen unlock → long-press fast (<2s), no 5s delay |
| Test 4 | 2nd lock after normal tap-unlock → no 5s delay (Bug 4) |
| Test 1–3, 5, 8 | Regression — must remain PASS |

See [PIXEL9A_TEST_GUIDE_v2.11.4-06.md](PIXEL9A_TEST_GUIDE_v2.11.4-06.md) for full test steps (Tests 6/7 still apply).

---

## v2.11.4-05 Test Results (Work NB 2026-02-24) — FAIL (archived)

| Test | Result | Root Cause |
|------|--------|------------|
| Test 1: Lock applied | ✅ PASS | — |
| Test 2: AF stays INACTIVE | ✅ PASS | — |
| Test 3: AE stays locked | ✅ PASS | — |
| Test 4: Unlock doesn't stick | ✅ PASS | — |
| Test 5: Single-tap unlock | ❌ FAIL | Double-gesture race: 2nd cancel "superseded" |
| Test 6: Screen lock auto-clear | ❌ FAIL | Camera2 options persist after inactive-state clear |
| Test 7: No long-press delay | ❌ FAIL | AF frozen after screen unlock (same root cause as T6) |
| Observation 1: Preview blur | ❌ FAIL | focusDist=0.000 after AF_MODE=OFF |

**Evidence**: `logcat_v211405_20260224.log`, `Screenshot_20260224-154446.png`

---

## After Fixing (Home NB Push)

```bash
# After Work NB verifies v2.11.4-08 PASS:
git add app/src/main/java/com/akirachang/ai/photographer/ devspec/ docs/
git commit -m "[Test] v2.11.4-08: Pixel 9a verification results"
git push origin feature/camera-core-v2.11.0
```

Then update this file's status table and notify Work NB to pull for re-verification.
