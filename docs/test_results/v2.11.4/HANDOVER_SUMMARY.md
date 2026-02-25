# Handover Summary — v2.11.4 Pixel 9a Test

**Date**: 2026-02-25
**Branch**: `feature/camera-core-v2.11.0`
**Test Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Handover**: Home NB → Work NB

---

## Current Status

| Iteration | Home NB | Work NB | Result |
|-----------|---------|---------|--------|
| v2.11.4-01~03 | ✅ Built | ✅ Installed | 부분 기능 확인 |
| v2.11.4-04 | ✅ Built | ✅ Installed | ✅ Unlock stuck fixed |
| v2.11.4-05 | ✅ Built | ✅ Tested | ❌ Test 5/6/7 FAIL + Observation 1 |
| **v2.11.4-06** | ✅ Committed | ⏳ Pending | **→ This handover** |

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
| `8953dc8` | **v2.11.4-06** | **Fix double-unlock race + Camera2 persist + lens drift** |

**Latest commit**: `8953dc8`

---

## v2.11.4-05 Test Results (Work NB 2026-02-24) — FAIL

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

## What Was Fixed in v2.11.4-06

### Bug 1 — Double cancelFocusAndMetering Race (Test 5)
**Root cause**: `_state.update{isAEAFLocked=false}` was AFTER the `cancelFocusAndMetering()` suspend call. A 2nd coroutine (double-gesture) still saw `isAEAFLocked=true` and launched another cancel → "superseded" error.

**Fix** (`CameraViewModel.kt:886`): Move `_state.update{isAEAFLocked=false}` BEFORE the suspend. The 2nd coroutine now sees `false` immediately and skips the unlock branch.

### Bug 2 — Camera2 Options Persist After Screen Lock (Test 6/7)
**Root cause**: `clearCaptureRequestOptions()` in ON_STOP was silently ignored (camera inactive). CameraX re-applied `AE_LOCK=true + AF_MODE=OFF` on re-bind → AF frozen after screen unlock.

**Fix** (`LiveCameraSource.kt`, after `bindToLifecycle`): Unconditional `clearCaptureRequestOptions()` every time camera binds. Guarantees clean state regardless of how the previous session ended.

### Bug 3 — focusDist=0.000 After AF_MODE=OFF (Observation 1)
**Root cause**: Pixel 9a firmware resets `LENS_FOCUS_DISTANCE` to 0 (infinity) when `AF_MODE=OFF` is applied. Lens physically drifts to infinity → visible blur.

**Fix** (`LiveCameraSource.kt`):
- New `@Volatile lastFocusDistance: Float` field — updated every frame in `onCaptureCompleted`
- `lockAEAF` success callback now also sets `CONTROL_LENS_FOCUS_DISTANCE=lastFocusDistance`, pinning the lens at the converged position

---

## APK Build Command (Work NB)

```bash
# Pull latest
git pull origin feature/camera-core-v2.11.0

# Verify latest commit is 8953dc8
git log --oneline -3

# Build device APK
./gradlew :app:assembleDeviceDebug

# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Start Logcat (keep running during tests)
adb -s 59131JEBF17244 logcat -v time CameraViewModel:I LiveCameraSource:I *:S
```

---

## Test Focus for This Handover

Primary tests (new bugs fixed in -06):
- **Test 5**: No double-cancel race — expect NO `"superseded"` in log
- **Test 6**: Screen lock clears Camera2 options — expect `"Camera2 interop options cleared on bind"` log + yellow box gone
- **Test 7**: No long-press delay after screen unlock
- **Test 8**: Preview stays sharp after lock — expect `LENS_FOCUS_DISTANCE > 0` in log + no blur

Regression tests (should still pass):
- **Test 1–4**: AE/AF lock still works correctly

See [PIXEL9A_TEST_GUIDE_v2.11.4-06.md](PIXEL9A_TEST_GUIDE_v2.11.4-06.md) for full test steps.

---

## After Testing

Push results back:
```bash
git add docs/test_results/v2.11.4/
git commit -m "[Test] v2.11.4-06: Pixel 9a re-verification results"
git push
```
