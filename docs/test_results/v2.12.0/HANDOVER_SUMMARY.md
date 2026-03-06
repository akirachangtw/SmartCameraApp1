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

---

## Test Instructions for Work NB

**Full test guide**: `docs/test_results/v2.12.0/v2.12.0_08_09_work_nb_execution.md`

### Quick Setup

```bash
git pull origin feature/camera-core-v2.12.0
./gradlew assembleDeviceDebug
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Logcat
adb -s 59131JEBF17244 logcat -c
adb -s 59131JEBF17244 logcat -v time CameraViewModel:D LiveCameraSource:I *:S \
  > docs/test_results/v2.12.0/logcat_v2.12.0-08-09_$(date +%Y%m%d).log &
```

### Test Cases Summary

| TC | Description | Fix |
|----|-------------|-----|
| TC-01 | Rear cam, far face — frame is NOT square | v2.12.0-08 |
| TC-02 | Front cam, normal distance — frame is NOT square | v2.12.0-08 |
| TC-03 | Rear cam, close face — large frame, AR preserved | v2.12.0-08 |
| TC-04 | Rapid taps rear cam — no 2s backoff pause | v2.12.0-09 |
| TC-05 | Normal AF regression — lock/dwell works | regression |
| TC-06 | Fixed-focus bypass regression (front cam) | regression |

### Key Logcat Patterns

```
# v2.12.0-09 operating correctly (no backoff):
[v2.12.0-09] OperationCanceledException — reset to Idle (no backoff)

# v2.12.0-07 regression still working:
[v2.12.0-07] Fixed-focus lens — AE/AWB metering only at (x, y)

# Normal AF success:
Focus successful at (x, y)

# Should NOT appear for OperationCanceledException:
Focus failed at (x, y)
```

---

## Test Results (Work NB — to be filled)

**Date tested**: __________
**Tester**: Work NB / Claude Code

| TC | Description | Result | Notes |
|----|-------------|--------|-------|
| TC-01 | Rear cam far face — AR preserved | ⏳ | |
| TC-02 | Front cam normal dist — AR preserved | ⏳ | |
| TC-03 | Rear cam close face — large AR frame | ⏳ | |
| TC-04 | Rapid taps — no backoff | ⏳ | |
| TC-05 | Normal AF regression | ⏳ | |
| TC-06 | Fixed-focus bypass regression | ⏳ | |

**Overall Result**: ⏳ PENDING

**Logcat file**: `logcat_v2.12.0-08-09_YYYYMMDD.log`

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
| v2.12.0-08 | Focus frame AR clamp uniform scale-up | ✅ Built — **⏳ Pending test** |
| v2.12.0-09 | OperationCanceledException → Idle (no backoff) | ✅ Built — **⏳ Pending test** |
| v2.12.0-10 | Rear cam object bbox mapping fix | ⏳ Not yet built |
| v2.12.0-11 | ObjectTrackingPipeline + FocusUiSmoother integration | ⏳ Not yet built |
| v2.12.0 | Stable tag — dual-camera AF complete | ⏳ Pending v2.12.0-10/11 |

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
