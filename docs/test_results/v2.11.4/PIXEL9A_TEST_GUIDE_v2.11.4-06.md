# Pixel 9a Test Guide — v2.11.4-06

**Version**: v2.11.4-06 (Fix double-unlock race + Camera2 persist + lens drift)
**Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Latest Commit**: `8953dc8`
**Date**: 2026-02-25

**Previous Iteration**: v2.11.4-05 → Pixel 9a FAIL (Test 5/6/7 FAIL, Observation 1)
**This Iteration Fixes**: Bug 1 (Test 5 race), Bug 2 (Test 6/7 screen lock), Bug 3 (blur after lock)

---

## Prerequisites

```bash
# Pull latest
git pull origin feature/camera-core-v2.11.0

# Build device debug APK
./gradlew :app:assembleDeviceDebug

# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Start Logcat (keep running during all tests)
adb -s 59131JEBF17244 logcat -v time CameraViewModel:I LiveCameraSource:I *:S
```

---

## Test 1 — AE/AF Lock Applied (Regression)

**Steps**:
1. Open app → live preview
2. Long-press on a subject → yellow indicator appears

**Expected logs**:
```
[v2.11.2] AE/AF lock applied at (x, y) - isFocusSuccessful=...
[v2.11.4-06] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF + CONTROL_LENS_FOCUS_DISTANCE=X.XXX applied
[AE/AF Debug] Logging ENABLED ...
```

**Note**: Log prefix changed from `[v2.11.4-03]` to `[v2.11.4-06]` — this is expected.
**Pass condition**: Yellow box shows, all 3 log lines appear in order.

---

## Test 2 — AF Stays INACTIVE 30s (Regression)

**Steps**:
1. Long-press to lock → observe `AF_STATE` in logcat for 30 seconds

**Expected**:
- `AF_STATE: 0 (INACTIVE)` stays 0 continuously
- ❌ Must NOT cycle to 3 (PASSIVE_SCAN) or 4 (PASSIVE_FOCUSED)

**Note**: `AF_STATE: 0 (INACTIVE)` is correct for `CONTROL_AF_MODE=OFF`.

---

## Test 3 — AE Stays Locked (Regression)

**Steps**:
1. Lock on a dimly-lit area
2. Wave hand or move to brighter area
3. Observe `AE_STATE` and ISO in logcat

**Expected**:
- `AE_STATE: LOCKED` stays locked
- ISO and exposure remain stable
- ❌ Must NOT see ISO jumping 10x+ after lock

---

## Test 4 — Unlock Doesn't Get Stuck (Regression)

**Steps**:
1. Long-press to lock (yellow box)
2. Tap screen to unlock
3. Immediately long-press again (rapid succession)
4. Repeat 3–5 times

**Expected**:
- Each cycle: lock → unlock → lock → unlock completes cleanly
- Yellow box disappears reliably on each unlock

**Expected logs (on unlock)**:
```
AE/AF unlocked by tap — passive AF resumed
[v2.11.4-04] AE/AF lock cleared — CONTINUOUS_PICTURE + AE auto restored
```

---

## Test 5 — Single-Tap Unlock, No Race ★ PRIMARY (Bug 1 Fix)

**What was wrong in -05**: Double-gesture caused two `cancelFocusAndMetering()` calls — first was "superseded", second succeeded. State was inconsistent during the race window.

**Fix in -06**: `_state.update{isAEAFLocked=false}` now fires BEFORE the suspend call. A 2nd coroutine sees `isAEAFLocked=false` and skips the unlock branch entirely.

**Steps**:
1. Long-press → yellow box appears (AE/AF Lock)
2. **Tap once** at any position on screen
3. Immediately tap a second time (simulate double-tap race)
4. Observe logcat

**Expected**:
- Yellow box **disappears** on the first tap
- AF scanning resumes automatically
- ❌ Must NOT see `"cancelFocusAndMetering superseded"` in log
- ❌ Must NOT see `"StandaloneCoroutine was cancelled"` error

**Expected log (clean single-unlock)**:
```
AE/AF unlocked by tap — passive AF resumed
```

**Pass**: Only one `"AE/AF unlocked"` line, no `"superseded"` error.
**Fail**: `"StandaloneCoroutine was cancelled"` + `"superseded"` appear.

---

## Test 6 — Screen Lock Clears Camera2 Options ★ PRIMARY (Bug 2 Fix)

**What was wrong in -05**: After screen lock/unlock, `AE_LOCK=true` + `AF_MODE=OFF` persisted in CameraX internal state (HAL ignored `clearCaptureRequestOptions()` while camera inactive). AF was frozen after screen unlock.

**Fix in -06**: `clearCaptureRequestOptions()` called unconditionally in `bindToLifecycle()` callback — guarantees clean state on every camera bind.

**Steps**:
1. Long-press → yellow box appears (AE/AF Lock)
2. Press power button to lock screen
3. Unlock screen (fingerprint / swipe)
4. Return to camera app

**Expected**:
- Yellow box is **gone** after screen unlock
- Preview brightness is **normal** (not dark/underexposed)
- **NEW**: `"Camera2 interop options cleared on bind"` visible in logcat
- ❌ Must NOT see yellow box persisting

**Expected logs (after screen unlock)**:
```
[v2.11.4-06] Camera2 interop options cleared on bind (clean state)
```

**Pass**: Yellow box gone + bind log present + preview normal.
**Fail**: Yellow box persists or `AF_STATE` still `INACTIVE` after unlock.

---

## Test 7 — No Long-Press Delay After Screen Unlock ★ PRIMARY (Bug 2 Fix)

**Steps**:
1. Ensure **no AE/AF lock** is active (no yellow box)
2. Press power button to lock screen
3. Unlock screen → return to camera
4. **Immediately long-press** to apply AE/AF Lock

**Expected**:
- Long-press triggers lock **without noticeable delay** (~1–2s for AF convergence is normal)
- ❌ Must NOT see significantly longer delay compared to before screen lock
- ❌ Must NOT see AF frozen / yellow box appearing immediately without convergence

**Expected logs**:
```
[v2.11.4-06] Camera2 interop options cleared on bind (clean state)
...
[v2.11.4-06] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF + CONTROL_LENS_FOCUS_DISTANCE=X.XXX applied
```

---

## Test 8 — Preview Stays Sharp After AE/AF Lock ★ PRIMARY (Bug 3 Fix)

**What was wrong in -05**: After `CONTROL_AF_MODE=OFF`, Pixel 9a reset `LENS_FOCUS_DISTANCE` to 0 (infinity). Lens physically moved away from converged position → visible blur in preview.

**Fix in -06**: `lastFocusDistance` tracked every frame in `onCaptureCompleted`. At lock success time, `CONTROL_LENS_FOCUS_DISTANCE=lastFocusDistance` is applied simultaneously with `AF_MODE=OFF`, pinning the lens at the converged position.

**Steps**:
1. Point camera at a subject ~0.5–2m away
2. Let AF converge naturally (green indicator appears, then fades)
3. **Long-press** to apply AE/AF Lock
4. Wait 3–5 seconds after yellow box appears
5. Observe preview sharpness

**Expected**:
- Preview **stays sharp** at the locked subject distance
- ❌ Must NOT see preview go blurry/soft after lock is applied
- Log shows `CONTROL_LENS_FOCUS_DISTANCE` value > 0

**Expected log**:
```
[v2.11.4-06] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF + CONTROL_LENS_FOCUS_DISTANCE=2.XXX applied
```

**Comparison test** (optional, to confirm fix):
- Look at previous test screenshots `docs/test_results/v2.11.4/Screenshot_20260224-154446.png` — shows blurry preview from -05.
- In -06, the same scenario should remain sharp.

**Pass**: Preview sharp + `LENS_FOCUS_DISTANCE > 0` in log.
**Fail**: Preview becomes blurry after lock + `LENS_FOCUS_DISTANCE=0.000` in log.

---

## Results Template

Copy to [PIXEL9A_TEST_REPORT_v2.11.4-06.md](PIXEL9A_TEST_REPORT_v2.11.4-06.md) after testing.

| Test | Result | Notes |
|------|--------|-------|
| Test 1: Lock applied (regression) | PASS / FAIL | |
| Test 2: AF stays INACTIVE 30s (regression) | PASS / FAIL | |
| Test 3: AE stays locked (regression) | PASS / FAIL | |
| Test 4: Rapid lock/unlock doesn't stick (regression) | PASS / FAIL | |
| Test 5: No double-cancel race (Bug 1) | PASS / FAIL | Check for "superseded" log |
| Test 6: Screen lock clears Camera2 options (Bug 2) | PASS / FAIL | Check "cleared on bind" log |
| Test 7: No delay after screen unlock (Bug 2) | PASS / FAIL | |
| Test 8: Preview stays sharp after lock (Bug 3) | PASS / FAIL | Check LENS_FOCUS_DISTANCE value |

---

## Save Logcat

```bash
# Save to test results
adb -s 59131JEBF17244 logcat -v time -d CameraViewModel:I LiveCameraSource:I *:S > docs/test_results/v2.11.4/logcat_v211406_20260225.log
```
