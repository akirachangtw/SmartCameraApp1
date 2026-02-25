# Pixel 9a Test Report — v2.11.4-06

**Version**: v2.11.4-06
**Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Tester**: Work NB
**Date**: 2026-02-25
**Build Commit**: `ce078da` (includes LENS_FOCUS_DISTANCE compile fix on top of `8953dc8`)

---

## Test Results

| Test | Result | Notes |
|------|--------|-------|
| Test 1: Lock applied log (regression) | PASS | `[v2.11.4-06]` prefix confirmed; LENS_FOCUS_DISTANCE logged |
| Test 2: AF stays INACTIVE 30s (regression) | PASS | AF_STATE=INACTIVE held 30s, no degradation |
| Test 3: AE stays locked (regression) | PASS | Overexposed when moved to bright area — AE lock confirmed |
| Test 4: Rapid lock/unlock doesn't stick | PASS (with note) | Cycles cleanly; ~1/10 chance of 5s delay (see Bug 4) |
| Test 5: No double-cancel race | **PASS** ✅ | Bug 1 FIXED — only 1 `lock cleared` + 1 `unlocked by tap` |
| Test 6: Screen lock clears Camera2 options | FAIL | Same as v2.11.4-05 — bind fix doesn't trigger on screen lock/unlock |
| Test 7: No long-press delay after screen unlock | FAIL | Same as v2.11.4-05 — AF_MODE=OFF residual persists |
| Test 8: Preview stays sharp after lock | **PASS** ✅ | Bug 3 FIXED — focusDist maintained at converged value (not 0.000) |

**Overall**: FAIL

---

## Logcat Evidence

```
# Test 1 — Lock applied, new log prefix
11:18:41.385  [v2.11.2] AE/AF lock applied at (599.0, 1150.0996) - isFocusSuccessful=true
11:18:41.386  [v2.11.4-06] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF + CONTROL_LENS_FOCUS_DISTANCE=2.0595412 applied
11:18:41.386  [AE/AF Debug] Logging ENABLED — will log AF_STATE, AE_STATE, ISO, exposure, focusDist every 1s

# Test 2 — AF_STATE=INACTIVE sustained (30s, shown: 22s)
11:18:42.420  [AE/AF Debug] AF_STATE=INACTIVE, AE_STATE=LOCKED, AE_LOCK=true, exposure=20ms, ISO=186, focusDist=2.060
11:18:43.442  [AE/AF Debug] AF_STATE=INACTIVE, AE_STATE=LOCKED, AE_LOCK=true, exposure=20ms, ISO=186, focusDist=2.060
# (AF_STATE=INACTIVE held continuously — no degradation to PASSIVE_SCAN or PASSIVE_FOCUSED)

# Test 5 — Single tap unlock: only ONE lock cleared (v2.11.4-05 had TWO)
11:40:29.566  [v2.11.4-04] AE/AF lock cleared — CONTINUOUS_PICTURE + AE auto restored  ← only once
11:40:29.566 W cancelFocusAndMetering superseded — cleanup already applied  ← guard blocked 2nd call
11:40:29.567  AE/AF unlocked by tap — passive AF resumed

# Test 6 — Screen lock: cancelFocusAndMetering fails (Camera is not active)
11:22:22.859  Unlocking AE/AF
11:22:23.037  [v2.11.4-04] AE/AF lock cleared — CONTINUOUS_PICTURE + AE auto restored
11:22:23.041  java.util.concurrent.ExecutionException: CameraControl$OperationCanceledException: Camera is not active.
11:22:23.041  (Stack: onActivityPreStopped → unlockAEAF → cancelFocusAndMetering)
# Camera2 interop options cleared on bind — NOT triggered after screen unlock
# (no bind event between 11:22 and 11:26; clearCaptureRequestOptions skipped)
11:26:14.398  [AE/AF Debug] AF_STATE=INACTIVE, AE_STATE=LOCKED, AE_LOCK=true, exposure=16.599051ms, ISO=29
# → Dark preview confirmed (locked at dim-area exposure, camera resumed in normal light)

# Test 7 — 5s delay after screen unlock (same root cause as Test 6)
11:48:55.471  1st lock (after app restart): isFocusSuccessful=true  ← clean state, fast
              (unlock tap)
11:49:04.072  2nd lock: isFocusSuccessful=false  ← 5-second timeout, AF_MODE=OFF residual
11:49:08.721  java.util.concurrent.ExecutionException: Camera is not active.  ← again during screen lock

# Test 8 — focusDist frozen at converged value (Bug 3 FIXED)
11:48:55.471  [v2.11.4-06] CONTROL_LENS_FOCUS_DISTANCE=2.9743207 applied
11:48:56.519  [AE/AF Debug] AF_STATE=INACTIVE, AE_LOCK=true, focusDist=2.974 diopters  ← constant
11:48:57.523  [AE/AF Debug] AF_STATE=INACTIVE, AE_LOCK=true, focusDist=2.974 diopters  ← constant
# (focusDist does NOT drop to 0.000 — lens stays at converged position)
# User confirmed: preview stays sharp after lock ✅
```

---

## Bug Summary

| Bug | Severity | Status | Description |
|-----|----------|--------|-------------|
| Bug 1: Double cancelFocusAndMetering race | High | **FIXED ✅** | `isAEAFLocked=false` before suspend call prevents 2nd coroutine; guard logs `superseded` |
| Bug 2: Camera2 settings persist after screen lock | High | **STILL FAILING** | `clearCaptureRequestOptions()` on bind only fires on app start/process restart; screen lock/unlock does NOT rebind → fix never triggers |
| Bug 3: focusDist=0.000 after CONTROL_AF_MODE=OFF | Medium | **FIXED ✅** | `CONTROL_LENS_FOCUS_DISTANCE=lastFocusDistance` applied simultaneously; lens stays at converged position |
| Bug 4 (New): `cancelFocusAndMetering` intermittent failure | High | NEW | `Camera is not active` thrown during normal tap-unlock lifecycle transitions; leaves `AF_MODE=OFF` residual → ~1/10 5s delay in normal use; consistent after screen lock/unlock |

### Bug 4 Detail — New Finding

**Symptom**: After app restart, 1st lock is always fast. After the 1st tap-unlock, the 2nd long-press has ~1/10 chance of 5-second delay. After screen lock/unlock or background switch, the delay occurs consistently and requires app restart to recover.

**Root cause**: `cancelFocusAndMetering()` (called during `unlockAEAF`) sometimes throws `Camera is not active` while the camera is transitioning between lifecycle states (not just during screen lock). When this happens, `CONTROL_AF_MODE=OFF` is not cleared from Camera2 Interop. The next `startFocusAndMetering` call cannot converge (AF is hardware-frozen), and CameraX times out after 5 seconds → `isFocusSuccessful=false`.

**Why v2.11.4-06 bind fix doesn't help**: The `clearCaptureRequestOptions on bind` fires on app start / process restart (demonstrated by 1st lock being fast), but NOT on screen lock/unlock or background switch because CameraX does not rebind on `onStop → onStart` activity lifecycle (it only rebinds when `bindToLifecycle()` is explicitly called again).

**Proper fix direction**: Call `clearCaptureRequestOptions()` in a lifecycle observer on `Lifecycle.State.STARTED` (in addition to, or instead of, the bind callback), so it fires reliably on every camera resume.

---

## Notes / Observations

### Observation 1 — Bug 3 Fix Confirmed (focusDist maintained)

| Version | focusDist after lock | Preview |
|---------|---------------------|---------|
| v2.11.4-05 | 0.000 diopters → blur | Severely blurred |
| v2.11.4-06 | 2.060–3.994 diopters (constant) | Sharp ✅ |

The simultaneous application of `CONTROL_LENS_FOCUS_DISTANCE` with `AF_MODE=OFF` successfully pins the lens at the converged position.

### Observation 2 — Bug 1 Fix Confirmed (superseded guard active)

In v2.11.4-05, double-gesture produced two `lock cleared` events (231ms apart).
In v2.11.4-06, only one `lock cleared` fires. The guard log `cancelFocusAndMetering superseded — cleanup already applied` confirms the second coroutine was blocked by the early `isAEAFLocked=false` state update.

### Observation 3 — Bug 2 Partial Fix

The `clearCaptureRequestOptions on bind` works correctly for the **app start** case (1st lock always fast after restart). However, it does not cover the more common use case of **screen lock/unlock** or **background/foreground switch**, because the camera is not rebound in these scenarios.

---

## Decision

- [ ] v2.11.4-06 PASS → proceed to v2.11.4-07
- [x] FAIL → return to Home NB

**Passing**: Test 1, 2, 3, 4, 5, 8
**Failing**: Test 6, 7 (Bug 2 incomplete fix)
**New bug found**: Bug 4 (intermittent `cancelFocusAndMetering` failure → 5s delay)

**Return to Home NB** for fix on:
1. Bug 2 (remaining): `clearCaptureRequestOptions()` must fire on every camera resume, not just on bind — use `Lifecycle.State.STARTED` observer
2. Bug 4 (new): `cancelFocusAndMetering()` intermittently fails with `Camera is not active` during normal lifecycle transitions; residual `AF_MODE=OFF` causes persistent 5s delay; root cause is the same as Bug 2 (Camera2 Interop not cleared reliably on resume)
