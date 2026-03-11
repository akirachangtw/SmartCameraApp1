# Claude Implementation Guide: v2.12.0-10 Front Cam AF & Focus Hunt Defense (L2)

> **Role**: Principal Android Architect
> **Target Audience**: Claude Code (Implementation Agent)
> **Objective**: Hardening the camera focus pipeline against OEM hardware quirks (specifically Sony XQ-BT52), preventing AF Hunting infinite loops, and guaranteeing Unidirectional Data Flow (UDF) for UI rendering.

This document serves as your definitive architectural contract. Do not deviate from these defensive patterns without escalating.

---

## 1. Hardware Capability Matrix (Phase 1: Detector Hardening)

**The Vulnerability**: 
The Sony XQ-BT52 (and potentially other OEMs) violates Camera2 API contracts. `LENS_INFO_MINIMUM_FOCUS_DISTANCE` fluctuates unpredictably (`> 0.0f` on some sessions) on a front camera that is physically fixed-focus. This causes the App to attempt `FLAG_AF` requests which instantly throw `Focus Failed`.

**The Defense Strategy**:
Relying on a single hardware characteristic for capability detection is a critical flaw. We must use a composite checking mechanism.

*   **Implementation Directive in `FixedFocusDetector.kt`**:
    *   Query `CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES`.
    *   If the returned integer array **only** contains `CaptureRequest.CONTROL_AF_MODE_OFF` (0), the lens is conclusively **Fixed Focus**.
    *   `isFixedFocus` must return `true` if *either* `LENS_INFO_MINIMUM_FOCUS_DISTANCE == 0.0f` *OR* `afModes.contentEquals(intArrayOf(0))`.
    *   Cache this value accurately upon camera binding.

## 2. Sealed Class State Machine Defense (Phase 2: AF Hunting Elimination)

**The Vulnerability**:
The `AfTriggerThrottleController` operates on a temporal cooldown (`postLockCooldown`). For a fixed-focus lens, when cooldown expires, the AI face detection re-triggers focus because there is no concept of "permanent stability". For an auto-focus lens, micro-jitters in ML Kit bounding boxes break the stability threshold and cause infinite re-scanning (AF Hunting).

**The Defense Strategy**:
State transitions must be deterministic and mutually exclusive. An `AfState` must guard against nonsensical operations based on hardware capabilities.

*   **Implementation Directive in `CameraViewModel.kt` / `AfStateMachine`**:
    *   **Fixed-Focus Permanent Lock**: When `isFixedFocus == true`, `autoFocusOnSalientTarget()` must effectively become a **No-Op** for hardware triggers. It should set the state to `AfState.Locked` once, and subsequent evaluations should *skip* calling `cameraRepository.focusOnPoint()` entirely. (AE/AWB metering tap is acceptable, but automatic repeating is forbidden).
    *   **AF Hysteresis Expansion**: For rear cameras (`isFixedFocus == false`), the re-trigger threshold (`delta_px`) after reaching `AfState.Locked` must be widened. If the face moves within a ~10-15% bounding box tolerance relative to the previously locked anchor point, **DO NOT** transition back to `Focusing`.
    *   **Failed State Backoff Limits**: Ensure `AfState.Failed` implements an exponential backoff that eventually pauses automatic retries until a manual `UserTap` resets the state.

## 3. UDF Decoupling (Phase 3: Guaranteed UI Rendering)

**The Vulnerability (Bug A)**:
The UI Focus Box size (`focusFrameWidthPx`/`HeightPx`) was coupled conditionally to the hardware `AfState`. When the hardware hit `AfState.Failed` (due to the Sony bug), the state machine blocked further updates to the box dimensions, causing the UI to freeze at the "first spotted" size.

**The Defense Strategy**:
UI state generation and Hardware control must be explicitly bifurcated (Unidirectional Data Flow). Hardware failure must *never* stop the UI from rendering the AI's reality.

*   **Implementation Directive in `CameraViewModel.kt`**:
    *   Extract bounding box size updates completely out of the `AfState` conditional gates inside `autoFocusOnSalientTarget()`.
    *   Every time ML Kit emits a salient target (Face > 50 score), **unconditionally** update `uiFocusRect` or (`focusFrameWidthPx`/`HeightPx`) so the Compose UI can animate tracking smoothly. 
    *   The `grow-only` (`maxOf`) bounding box logic currently active during `AfState.Locked` must be removed or heavily modified. The UI box must be allowed to shrink dynamically as the user moves away from the lens, even if the hardware reports `Locked`. Use the `TargetTracker` EMA smoothing (as planned in prior L2 docs) instead of rigid `maxOf` clamps. 

## 4. Destructive Edge Conditions Mitigation

When implementing the above, you must guard against these catastrophic scenarios:

*   **Edge Case: Rapid Lens Switching (Front <-> Rear)**
    *   *Risk:* The cached `isFixedFocus` value or `AfState` leaks between lenses.
    *   *Defense:* Completely destroy and instantiate a new `AfStateMachine` and `FixedFocusDetector` instance upon `CameraSelector` switch. Nullify all cached ML Kit targets immediately to prevent a rear-camera bounding box from flashing on the front-camera preview.
*   **Edge Case: Extreme Low Light (BBox Noise)**
    *   *Risk:* ML Kit bounding boxes flutter wildly in low light, defeating the expanded Hysteresis threshold and triggering AF Hunting.
    *   *Defense:* The `AfTriggerThrottleController` should dynamically scale its movement tolerance threshold (`delta_px`) based directly on the saliency score or a temporal confidence metric. A lower score requires a larger physical movement to break the `Locked` state.
*   **Edge Case: Multiple Faces "Ping-Pong"**
    *   *Risk:* Two faces have identical scores, causing the system to swap targets every frame, destroying AF locks.
    *   *Defense:* Implement `trackingId` stickiness. Once a face is `Locked`, apply an artificial +20% score boost to that specific `trackingId`. The system will only swap faces if a different face becomes overwhelmingly larger/more salient.
