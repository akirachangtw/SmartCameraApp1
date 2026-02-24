# Pixel 9a Test Guide — v2.11.4-05

**Version**: v2.11.4-05 (Unlock UX + Screen lock auto-clear)
**Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Latest Commit**: `91333c5`
**Date**: 2026-02-24

---

## Prerequisites

```bash
# Pull latest
git pull origin feature/camera-core-v2.11.0

# Build device debug APK
./gradlew :app:assembleDeviceDebug

# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Start Logcat (keep running during tests)
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
[v2.11.4-03] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF applied (true AE+AF lock)
[AE/AF Debug] Logging ENABLED ...
```

**Pass condition**: Yellow box shows, log lines appear in order.

---

## Test 2 — AF Stays INACTIVE 30s (Regression)

**Steps**:
1. Long-press to lock → observe `AF_STATE` in logcat for 30 seconds

**Expected**:
- `AF_STATE: 0 (INACTIVE)` stays 0 continuously
- ❌ Must NOT cycle to 3 (PASSIVE_SCAN) or 4 (PASSIVE_FOCUSED)

**Note**: `AF_STATE: 0 (INACTIVE)` is correct for `CONTROL_AF_MODE=OFF`. Lens is frozen at converged position.

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

## Test 4 — Unlock Doesn't Get Stuck (v2.11.4-04)

**Steps**:
1. Long-press to lock (yellow box)
2. Tap screen to unlock
3. Immediately long-press again (rapid succession)
4. Repeat 3–5 times

**Expected**:
- Each cycle: lock → unlock → lock → unlock completes cleanly
- Yellow box disappears reliably on each unlock
- ❌ Must NOT see yellow box persisting after unlock

**Expected logs (on unlock)**:
```
[v2.11.4-04] AE/AF lock cleared — CONTINUOUS_PICTURE + AE auto restored
Focus and metering cancelled - returned to auto mode
AE/AF unlocked, returned to auto mode
```

---

## Test 5 — Single-Tap Unlock (v2.11.4-05 Fix A) ★ Primary

**Steps**:
1. Long-press → yellow box appears (AE/AF Lock)
2. **Tap once** at any position on screen
3. Observe immediately

**Expected**:
- Yellow box **disappears** on the first tap
- AF scanning resumes automatically (no second tap needed)
- ❌ Must NOT require a second tap to clear the yellow box or start AF scanning

**Expected log on first tap**:
```
AE/AF unlocked by tap — passive AF resumed
```

**If behavior**: Yellow disappears, then AF scanning starts on its own → ✅ PASS
**If behavior**: Yellow disappears, nothing happens, need 2nd tap → ❌ FAIL

---

## Test 6 — Screen Lock Auto-Clears Yellow Box (v2.11.4-05 Fix B) ★ Primary

**Steps**:
1. Long-press → yellow box appears (AE/AF Lock)
2. Press power button to lock screen
3. Unlock screen (fingerprint / swipe)
4. Return to camera app (should be in foreground immediately)

**Expected**:
- Yellow box is **gone** after screen unlock
- Preview brightness is **normal** (not dark)
- ❌ Must NOT see yellow box persisting after screen unlock
- ❌ Must NOT see dark/underexposed preview after screen unlock

**Expected log (on screen lock)**:
```
Unlocking AE/AF
[v2.11.4-04] AE/AF lock cleared — CONTINUOUS_PICTURE + AE auto restored
AE/AF unlocked, returned to auto mode
```

---

## Test 7 — No Long-Press Delay After Screen Unlock (v2.11.4-05 guard) ★ Primary

**Steps**:
1. Ensure **no AE/AF lock** is active (no yellow box)
2. Press power button to lock screen
3. Unlock screen → return to camera
4. **Immediately long-press** to apply AE/AF Lock

**Expected**:
- Long-press triggers lock **without noticeable delay** (~1–2s for focus to converge is normal)
- ❌ Must NOT take significantly longer than a long-press before the screen was locked

**Expected log (on screen lock with NO prior lock)**:
```
Unlocking AE/AF skipped — not locked
```
*(confirms the guard prevented unnecessary Camera2 operations)*

---

## Results Template

Copy to [PIXEL9A_TEST_REPORT.md](PIXEL9A_TEST_REPORT.md) after testing.

| Test | Result | Notes |
|------|--------|-------|
| Test 1: Lock applied log (regression) | PASS / FAIL | |
| Test 2: AF stays INACTIVE 30s (regression) | PASS / FAIL | |
| Test 3: AE stays locked (regression) | PASS / FAIL | |
| Test 4: Rapid lock/unlock doesn't stick | PASS / FAIL | |
| Test 5: Single-tap unlock | PASS / FAIL | |
| Test 6: Screen lock auto-clears yellow box | PASS / FAIL | |
| Test 7: No long-press delay after screen unlock | PASS / FAIL | |

---

## Save Logcat

```bash
# Save to test results (replace YYYYMMDD with today's date)
adb -s 59131JEBF17244 logcat -v time -d CameraViewModel:I LiveCameraSource:I *:S > docs/test_results/v2.11.4/logcat_v211405_YYYYMMDD.log
```
