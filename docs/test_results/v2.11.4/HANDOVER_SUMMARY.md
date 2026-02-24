# Handover Summary — v2.11.4 Pixel 9a Test

**Date**: 2026-02-24
**Branch**: `feature/camera-core-v2.11.0`
**Test Device**: Pixel 9a (Device ID: `59131JEBF17244`)
**Handover**: Home NB → Work NB

---

## Commits in This Handover

| Commit | Version | Description |
|--------|---------|-------------|
| `62dcd41` | v2.11.4-01 | Fix saliency auto-focus never triggering when face present (Bug 1) |
| `8ba3447` | v2.11.4-01 | Revert tracking animation to StiffnessHigh (Fix 3) |
| `11b3c94` | v2.11.4-01 | Fix focus indicator jumping to face on tap (Bug 2) |
| `e09d4b7` | v2.11.4-02 | True AE lock via CONTROL_AE_LOCK=true (Camera2 Interop) |
| `4e64361` | v2.11.4-03 | Add CONTROL_AF_MODE=OFF for permanent AF lock |
| `f98d9ec` | v2.11.4-04 | Fix AE/AF unlock stuck (concurrent cancelFocusAndMetering exception) |
| `6662121` | v2.11.4-05 | Single-tap unlock visual feedback + auto-unlock on screen lock |
| `91333c5` | v2.11.4-05 | Fix: return@launch after unlock + unlockAEAF guard |

**Latest commit**: `91333c5`

---

## What Was Fixed (v2.11.4-04 + v2.11.4-05)

### v2.11.4-03 (Already verified by Home NB)
- `CONTROL_AF_MODE=OFF` → lens frozen at converged distance after lock
- `AF_STATE: 0 (INACTIVE)` is **correct** for MODE=OFF, not a bug

### v2.11.4-04 — Unlock stuck after rapid tap/unlock
**Problem**: Calling `cancelFocusAndMetering()` twice in quick succession threw `OperationCanceledException`, skipping the Camera2 interop cleanup (`clearCaptureRequestOptions`). This left `AE_LOCK=true` + `AF_MODE=OFF` persistent — yellow box reappeared on next lock even after unlock.

**Fix**: Moved Camera2 cleanup **before** `future.get()` in the listener callback (unconditional). `OperationCanceledException` is now treated as non-fatal.

### v2.11.4-05 — Single-tap unlock UX + Screen lock auto-clear

**Fix A: Single-tap unlock**
- **Before**: 2 taps needed (fall-through focusOnPoint while HAL still in AF_MODE=OFF → silent fail)
- **After**: 1 tap = unlock only. `return@launch` after unlock; passive continuous AF resumes automatically via v2.11.4-04's `CONTINUOUS_PICTURE` cleanup.
- Expected behavior: Yellow box disappears on first tap; AF scanning starts automatically.

**Fix B: Screen lock auto-unlock**
- **Before**: Screen lock → unlock cycle left yellow box visible + preview dark.
- **After**: `DisposableEffect` observing `Lifecycle.Event.ON_STOP` fires `UnlockAEAF` when app goes to background.
- Guard added to `unlockAEAF()`: only calls `cancelFocusAndMetering()` if actually locked — prevents HAL disturbance and long-press delay after screen unlock.

---

## APK Build Command (Work NB)

```bash
# Pull latest
git pull origin feature/camera-core-v2.11.0

# Build device APK
./gradlew :app:assembleDeviceDebug

# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

---

## Test Focus for This Handover

Primary tests (new in this handover):
- **Test 4**: Unlock doesn't get stuck (v2.11.4-04)
- **Test 5**: Single-tap unlock (1 tap = done) (v2.11.4-05 Fix A)
- **Test 6**: Screen lock auto-clears yellow box (v2.11.4-05 Fix B)
- **Test 7**: No long-press delay after screen unlock (v2.11.4-05 guard)

Secondary tests (regression):
- **Test 1-3**: AE/AF lock still works correctly (v2.11.4-03 regression)

See [PIXEL9A_TEST_GUIDE.md](PIXEL9A_TEST_GUIDE.md) for full test steps.

---

## After Testing

Push results back:
```bash
git add docs/test_results/v2.11.4/
git commit -m "[Test] v2.11.4-05: Pixel 9a unlock UX verification results"
git push
```
