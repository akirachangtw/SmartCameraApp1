# Pixel 9a Test Guide — v2.11.4-03

**Version**: v2.11.4-03 (CONTROL_AF_MODE=OFF for permanent AF lock)
**Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Date**: 2026-02-23

---

## Prerequisites

```bash
# Pull latest
git pull origin feature/camera-core-v2.11.0

# Build debug APK
./gradlew :app:assembleDebug

# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/debug/app-debug.apk

# Start Logcat (keep running during tests)
adb -s 59131JEBF17244 logcat -v time CameraViewModel:I LiveCameraSource:I *:S
```

---

## Test 1 — AE/AF Lock Applied (Logcat)

**Steps**:
1. Open app → live preview
2. Long-press on a subject to activate AE/AF Lock
3. Observe logcat output

**Expected logs** (in order):
```
[v2.11.2] AE/AF lock applied at (x, y) - isFocusSuccessful=...
[v2.11.4-03] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF applied (true AE+AF lock)
[AE/AF Debug] Logging ENABLED — will log AF_STATE, AE_STATE, ISO, exposure, focusDist every 1s
```

**Pass condition**: Log line `[v2.11.4-03] CONTROL_AE_LOCK=true + CONTROL_AF_MODE=OFF applied` appears immediately after lock.

---

## Test 2 — AF Stays Locked (Logcat, 30s observation)

**Steps**:
1. Long-press to lock → keep preview open for 30 seconds
2. Watch `AF_STATE` in debug logs

**Expected behavior**:
- `AF_STATE: 0 (INACTIVE)` — stays `0` continuously ← **CORRECT** (MODE=OFF)
- ❌ Must NOT cycle to `3 (PASSIVE_SCAN)` or `4 (PASSIVE_FOCUSED)` within 30s

**Note**: `AF_STATE: 0 (INACTIVE)` is correct behavior for `CONTROL_AF_MODE=OFF`. The AF algorithm is disabled; lens position is frozen. This is equivalent to switching a DSLR to Manual Focus.

---

## Test 3 — AE Stays Locked (Logcat)

**Steps**:
1. Lock on a dimly-lit area
2. Wave hand in front of camera or move to brighter area
3. Observe `AE_STATE` and `ISO` in debug logs

**Expected behavior**:
- `AE_STATE: LOCKED` — stays locked
- ISO and exposure values remain stable (no large swings)
- ❌ Must NOT see ISO jumping 10x+ after lock

---

## Test 4 — Physical Focus Hold

**Steps**:
1. Lock focus on a subject ~50cm away
2. Tap subject position for Long Press lock (yellow indicator appears)
3. Move camera to point at a subject ~2m away
4. Observe whether foreground distance stays blurred (depth cue)

**Expected behavior**:
- Near subject (original distance): sharp
- Far subject (new distance after moving camera): blurred / out of focus
- Focus does NOT re-adjust to new distance ← confirms lens held position

**Note**: On Pixel 9a this may be subtle due to small sensor DOF. Best results: use macro subject (~20-30cm) and move to >1m.

---

## Test 5 — Unlock Restores Auto Focus

**Steps**:
1. Apply AE/AF Lock (yellow indicator)
2. Tap elsewhere or press unlock (if available) to cancel lock
3. Observe logcat

**Expected logs**:
```
[v2.11.4-03] AE/AF lock cleared — auto exposure + focus restored
```

**Expected behavior**: After unlock, AF resumes normal operation (subject at new distance focuses correctly within 1-2s).

---

## Results Template

Copy to [PIXEL9A_TEST_REPORT.md](PIXEL9A_TEST_REPORT.md) after testing.

| Test | Result | Notes |
|------|--------|-------|
| Test 1: Lock applied log | PASS / FAIL | |
| Test 2: AF stays INACTIVE 30s | PASS / FAIL | |
| Test 3: AE stays locked | PASS / FAIL | |
| Test 4: Physical focus hold | PASS / FAIL | |
| Test 5: Unlock restores AF | PASS / FAIL | |

---

## Save Logcat

```bash
# Save to test results (replace YYYYMMDD with today's date)
adb -s 59131JEBF17244 logcat -v time -d CameraViewModel:I LiveCameraSource:I *:S > docs/test_results/v2.11.4/logcat_v2114_03_YYYYMMDD.log
```
