# Test Handoff: Phase 7 Anti-Jitter Implementation

**Date**: 2026-02-04
**Branch**: `feature/optical-flow-phase5-6`
**Version**: `2.9.9-Day1-2`
**From**: Home NB (Emulator tested)
**To**: Work NB (Pixel 9a real device test)

---

## 1. Summary

Phase 7 Anti-Jitter implementation completed. Front-facing camera tracking jitter fix ready for real device validation.

### Problem
- Front-facing camera tracking exhibits abnormal jitter (30-35px jumps)
- Normal tracking: distance < 5px
- Jitter frames: quality ~0.80, distance 30-35px

### Solution (Traycer A+B Combined)
1. **Velocity Clamping (Plan B)**: Limit max displacement to 15px/frame
2. **Jitter Rejection (Plan A)**: Filter low-quality large jumps (quality < 0.85 AND distance > 10px)

---

## 2. Code Changes

### Modified Files

| File | Changes |
|------|---------|
| `OpticalFlowConfig.kt` | +3 parameters (~25 lines) |
| `OpticalFlowTracker.kt` | +2 methods + track() logic (~45 lines) |

### New Config Parameters

```kotlin
val maxDistancePerFrame: Float = 15f           // Velocity clamp threshold
val minQualityForJumpDetection: Float = 0.85f  // Jitter quality threshold
val maxDistanceForJumpDetection: Float = 10f   // Jitter distance threshold
```

### New Methods

| Method | Purpose |
|--------|---------|
| `clampVelocity()` | Limit per-frame displacement, preserve direction |
| `isJitterFrame()` | Detect low-quality + large-jump frames |

---

## 3. Emulator Test Results (Home NB)

| Metric | Value | Status |
|--------|-------|--------|
| Quality | 0.97-1.00 | PASS |
| Distance | 0.0-0.7px | PASS |
| Drift correction | 0.3-1.0px | PASS |
| Jitter detected | None | Expected (emulator has no jitter) |
| Build | SUCCESS | PASS |

**Conclusion**: No regression in normal tracking behavior.

---

## 4. Work NB Test Instructions

### 4.1 Prerequisites

1. Pull latest code:
   ```bash
   git pull origin feature/optical-flow-phase5-6
   ```

2. Build device debug APK:
   ```bash
   ./gradlew assembleDeviceDebug
   ```

3. Install on Pixel 9a:
   ```bash
   adb install -r app/build/outputs/apk/device/debug/app-device-debug.apk
   ```

### 4.2 Test Procedure

1. **Launch App** and switch to **front-facing camera**
2. **Tap on screen** to start optical flow tracking
3. **Observe tracking behavior** - focus indicator should NOT jitter
4. **Monitor logcat** for anti-jitter logs:
   ```bash
   adb logcat -s OpticalFlowTracker:V | grep -E "Velocity clamped|Jitter rejected"
   ```

### 4.3 Expected Results

| Scenario | Before Fix | After Fix |
|----------|------------|-----------|
| Normal tracking | distance < 5px | Same |
| Jitter spike | 30-35px jump visible | Clamped to 15px OR rejected |
| Logcat | No anti-jitter logs | "Velocity clamped" or "Jitter rejected" when jitter occurs |

### 4.4 Pass Criteria

- [ ] No visible jitter/flicker on front-facing camera tracking
- [ ] Logcat shows "Velocity clamped" or "Jitter rejected" when jitter would occur
- [ ] Normal tracking quality maintained (quality > 0.85, distance < 5px typical)
- [ ] Drift correction still working (drift < 2px)

---

## 5. Log Analysis Guide

### Normal Tracking
```
Track: quality=0.98, distance=0.3px, error=1.2, elapsed=1ms
```

### Velocity Clamped (Plan B triggered)
```
Velocity clamped: 25.3px -> 15.0px
Track: quality=0.92, distance=15.0px, error=8.5, elapsed=2ms
```

### Jitter Rejected (Plan A triggered)
```
Jitter rejected: quality=0.80, distance=32.5px
```

---

## 6. Reference Documents

- L1 Plan: `docs/plan/v2.9.9_Day1-2_vivid-doodling-moler_L1.md`
- L2 Feasibility: `docs/plan/v2.9.9_Day1-2_vivid-doodling-moler_L2.md`
- Traycer Solution: `docs/plan/v2.9.9_Day1-2_vivid-doodling-moler_L2-Traycer.md`

---

## 7. Test Report Template

After testing, please fill in:

```markdown
# Pixel 9a Test Report - Phase 7 Anti-Jitter

**Date**: YYYY-MM-DD
**Tester**: [Name]
**Device**: Pixel 9a
**Build**: app-device-debug.apk

## Results

| Test Case | Result | Notes |
|-----------|--------|-------|
| Front camera jitter | PASS/FAIL | |
| Velocity clamping | Triggered/Not triggered | |
| Jitter rejection | Triggered/Not triggered | |
| Normal tracking quality | PASS/FAIL | |
| Drift correction | PASS/FAIL | |

## Logcat Samples

[Paste relevant logcat here]

## Conclusion

[Overall assessment]
```

---

**Please proceed with Pixel 9a testing and report results.**
