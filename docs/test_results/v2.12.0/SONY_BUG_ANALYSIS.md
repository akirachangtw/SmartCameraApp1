# Sony XQ-BT52 Bug Analysis — v2.12.0-08/09 TC-06

**Date**: 2026-03-06
**Device**: Sony XQ-BT52 (HQ616R2713)
**Branch**: `feature/camera-core-v2.12.0`
**Logcat**: `logcat_sony_tc06_bugAB.log`

---

## Test Results Summary

| TC | Result | Notes |
|----|--------|-------|
| TC-01 | ✅ PASS | Rear cam far face — AR preserved |
| TC-02 | ✅ PASS | Front cam normal distance — AR preserved |
| TC-03 | ✅ PASS | Rear cam close face — AR preserved |
| TC-04 | ✅ PASS | Rapid taps — no backoff |
| TC-05 | ✅ PASS | Normal AF regression |
| TC-06 | ❌ FAIL | Two bugs found (see below) |

**Overall**: PARTIAL PASS

---

## Bug A — Front Camera: Focus Frame Size Fixed Regardless of Face Distance

**Observed**: On front camera, tapping a face at close distance vs. far distance shows the
same focus frame size. Frame does not scale with face proximity.

**Scope**: Front camera only. Rear camera TC-01/03 (far/close face) pass correctly.

**Logcat**: Front camera operations not captured in `logcat_sony_tc06_bugAB.log`
(user performed Bug A test before live logcat session started — needs re-capture).

**Hypothesis**:
- The "grow-only" logic in `CameraViewModel.kt:1373-1376` during `AfState.Locked`
  may be causing the frame to remain at the "locked" size after first focus lock.
- `maxOf(focusWidthPx, currentState.focusFrameWidthPx)` prevents shrink during Locked state.
- If the front camera locks quickly and stays Locked, the frame never shrinks as the face moves away.
- Alternative: Sony front camera coordinate mapping may produce a fixed bbox size in image space.

**Files to investigate**:
- `CameraViewModel.kt:1367-1377` — grow-only logic
- `CameraViewModel.kt:1360-1365` — bbox → screen coordinate conversion via transformer
- `LiveCameraSource.kt` — front camera `isFront` detection and bbox reporting

---

## Bug B — Rear Camera: Focus Frame Retains Face Shape After Switching to Non-Face Target

**Observed**: After focusing on a face (rear camera), tapping on an empty area or a different
object shows a focus frame with the shape/size of the previous face focus frame.

**Logcat Evidence** (`logcat_sony_tc06_bugAB.log`):

```
# Face tap at 16:17:02
findTappedTarget: tapped face trackingId=440, bbox=Rect(114, 85 - 261, 233)
Smart Subject Focus: frame=496.125x666.0px    ← face frame set

# Non-face tap at 16:17:06
findTappedTarget: no target at image=(361.3333, 514.1667)
FocusClamp RESET                              ← only scale animation resets
# focusFrameWidthPx/HeightPx NOT reset → old 496x666 dimensions persist
```

**Root Cause** (`CameraViewModel.kt`):

1. Face tap → `enteredMLKitTrackingMode = true`
2. Focus succeeds → ML Kit path skips dwell fade entirely (`:1050` `if (!enteredMLKitTrackingMode)`)
3. `focusFrameWidthPx = 0f, focusFrameHeightPx = 0f` at `:1057-1058` **never executed**
4. Next tap on non-face → `_state.update` at `:971-983` or `:1007-1016` does NOT reset
   `focusFrameWidthPx/HeightPx` → old face dimensions persist
5. `FocusIndicator` (`CameraScreen.kt:722`) receives stale face dimensions → shows face-sized frame

**Fix direction** (`CameraViewModel.kt`):
In the non-ML Kit tap branches (Optical Flow / Static), reset focus frame dimensions immediately:
```kotlin
// In the tappedTarget == null (OF) and else (Static) branches:
focusFrameWidthPx = 0f,
focusFrameHeightPx = 0f
```
This forces `FocusIndicator` to fall back to the 48×48dp default when the new tap is not a face.

---

## Additional Observation — TC-04 on Sony

`OperationCanceledException` IS being caught, but the `[v2.12.0-09]` log tag was not always
visible in filtered logcat. Full logcat shows:

```
W/LiveCameraSource: Focus operation cancelled
D/CameraViewModel: focusOnPoint cancelled (new tap / camera switch)
E/LiveCameraSource: java.util.concurrent.ExecutionException:
    androidx.camera.core.CameraControl$OperationCanceledException:
    Cancelled by another startFocusAndMetering()
```

The `focusOnPoint cancelled` path exits early via `return@launch` BEFORE reaching the
`OperationCanceledException` check at `:1071`. This means rapid-tap cancellations bypass
the v2.12.0-09 guard entirely — but the behaviour is still correct (no backoff, Idle reset)
because the cancellation happens in the `cancellationException` catch block upstream.

TC-04 PASS is valid.
