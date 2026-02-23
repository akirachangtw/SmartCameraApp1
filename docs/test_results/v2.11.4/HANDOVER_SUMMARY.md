# Handover Summary — v2.11.4 Pixel 9a Test

**Date**: 2026-02-23
**Branch**: `feature/camera-core-v2.11.0`
**Test Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Handover**: Home NB → Work NB

---

## What Was Done (Home NB)

### Commits in this handover

| Commit | Version | Description |
|--------|---------|-------------|
| `11b3c94` | v2.11.4-01 | Fix focus indicator jumping to face on tap (Bug 2) |
| `62dcd41` | v2.11.4-01 | Fix saliency auto-focus never triggering when face present (Bug 1) |
| `8ba3447` | v2.11.4-01 | Revert tracking animation to StiffnessHigh (Fix 3) |
| `e09d4b7` | v2.11.4-02 | True AE lock via CONTROL_AE_LOCK=true (Camera2 Interop) |
| `4e64361` | v2.11.4-03 | Add CONTROL_AF_MODE=OFF for permanent AF lock |

### What v2.11.4-03 Does

**Problem**: After AE/AF Lock (long-press), AF degrades from `FOCUSED_LOCKED (5)` → `PASSIVE_SCAN (3)` within ~3s. v2.11.4-02 fixed AE lock but AF still cycled back to passive scanning.

**Fix**: Added `CONTROL_AF_MODE=OFF` to `lockAEAF()` success callback in `LiveCameraSource.kt`. Disables the AF algorithm entirely after metering completes — lens stays at the converged focal distance indefinitely.

**Key**: `AF_STATE` will show `INACTIVE (0)` after lock — this is **correct behavior** for `MODE=OFF`, not a bug. Like switching a DSLR to MF mode.

---

## What Work NB Needs To Do

See [PIXEL9A_TEST_GUIDE.md](PIXEL9A_TEST_GUIDE.md) for full test steps.

**Quick summary**:
1. Pull latest branch
2. Build & install debug APK
3. Run Logcat test (long-press → confirm `AF_STATE` stays `INACTIVE (0)`)
4. Physical test (lock → move camera → confirm focus holds at original distance)
5. Fill in [PIXEL9A_TEST_REPORT.md](PIXEL9A_TEST_REPORT.md) and push

---

## APK Build Command (Work NB)

```bash
cd <project_root>
./gradlew :app:assembleDebug
adb -s 59131JEBF17244 install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## After Testing

Push test results back:
```bash
git add docs/test_results/v2.11.4/
git commit -m "[Test] v2.11.4-03: Pixel 9a AE/AF lock verification results"
git push
```
