# Claude Implementation Guide (L2) - v2.12.0-13

## 1. Architectural Review & Objectives
As a Senior Android Architect with 10 years of experience, I have rigorously reviewed the L1 Context (`v2.12.0-13_camera_focus_spec_driven_dev-L1_Context.md`). Our primary directive is to strictly enforce the SSOT (Single Source of Truth) visual semantics for camera focus while guaranteeing robust state synchronization between the Camera HAL, ViewModel, and Compose UI across varied hardware configurations.

### Objective Summary:
*   Clarify and physically separate the visual paths for Eye-AF/Object AF (Cyan, Claw animation) vs AE/AF Lock (Yellow, Breathing).
*   Fix the `FocusClamp` rendering and bounding box scaling issues (Bug #1).
*   Implement robust Session ID defense against destructive HAL callback race conditions during lens switching (Bug #2).

---

## 2. Hardware/Software Capabilities Matrix Defense
We must gracefully handle the quirks of the Pixel 9a and Sony XQ-BT52, particularly regarding the front-facing fixed-focus lens constraints.

*   **Rule 2.1 (Hardware Bypass)**: For Front Cameras (`isFixedFocusLens = true` or `afModes=[0]`), the HAL will not perform physical lens movement. We must securely bypass the hardware AF trigger and *immediately* synthesize an `AfState.Locked` state to satisfy the UI's expectation.
*   **Rule 2.2 (Consumer Ignorance)**: The UI layer must not care whether the lock came from a physical HAL callback or a synthesized fixed-focus bypass. The Compose layer strictly and blindly reacts to `AfState.Locked`.

---

## 3. Sealed Class State Machine Defense
The UI state derivation is currently flawed due to the overloading of the `isFocusIndicatorLocked` boolean, violating the principle of single responsibility.

*   **Rule 3.1 (State Sovereignty)**: `AfState` (Sealed Class) is the absolute truth for autofocus progress.
*   **Rule 3.2 (Semantic Strictness)**: Do NOT overload `isFocusIndicatorLocked` to mean "any kind of lock". According to the SSOT, `isFocusIndicatorLocked` should map *exclusively* to the Yellow AE/AF long-press lock (`isAEAFLocked`).
*   **Rule 3.3 (Divergent Visual Paths)**: The Cyan Eye-AF lock (Claw animation) must be triggered by checking `afState is AfState.Locked` directly in the UI, logically decoupled from the Yellow lock.
*   **Actionable Defense**: In `CameraState.kt`, define `val isFocusIndicatorLocked: Boolean get() = isAEAFLocked`. Ensure the `FocusClamp`'s trigger condition evaluates `state.afState is AfState.Locked`.

---

## 4. UDF (Unidirectional Data Flow) Decoupling
The UI is failing to update the focus frame size and animations correctly because the Compose layer's triggers are disjointed from the ViewModel's state emissions.

*   **Rule 4.1 (Dumb UI)**: `viewModels` emit deterministic state; `Composables` observe and render. There should be zero business logic or complex conditional merging inside `CameraScreen.kt`.
*   **Rule 4.2 (Dimension Streaming)**: `focusFrameWidthPx` and `focusFrameHeightPx` must flow unconditionally from `CameraState` to the `FocusIndicator` modifiers. 
*   **Actionable Defense**: Implement the 1-second Dwell Timer as a standalone `LaunchedEffect` keyed on `afState is AfState.Locked`. Upon timeout, transition the Alpha value to `0.4f` smoothly using Compose's `animateFloatAsState`, ensuring the animation is a pure function of time and state.

---

## 5. Destructive Boundary Conditions
Boundary conditions such as rapid camera switching, component lifecycle events, and app backgrounding must be anticipated and neutralized.

*   **Rule 5.1 (Camera Switch Race Condition)**: When `switchCamera()` is invoked while a focus operation is pending, the belated HAL callback for the *previous* lens might arrive and mutate the state of the *new* lens, causing permanent desynchronization (Bug #2).
*   **Session ID Defense**: Inject a `focusSessionId` (an atomic incrementing integer) into the `focusOnPoint` request pipeline.
*   **Actionable Defense**: 
    1. In `CameraViewModel.kt`, before updating `afState` to `Locked` or `Failed` from the async camera callback, verify `currentFocusSessionId == capturedSessionId`. If mismatched, silently drop the callback and log the collision.
    2. Additionally, verify `lensFacing` remains consistent with the request origin.
    3. Ensure `resetFocusState()` immediately increments the `focusSessionId` to proactively invalidate any pending in-flight callbacks.

---

## 6. Implementation Checklist for Claude Code

*To be strictly executed during the code modification phase:*

- [ ] **`CameraState.kt`**: Refactor `isFocusIndicatorLocked` to strictly return `isAEAFLocked`. Add documentation clarifying that Eye-AF UI evaluates `afState` directly.
- [ ] **`CameraScreen.kt` (FocusIndicator)**:
  - [ ] Rewire the `FocusClamp` trigger to use `state.afState is AfState.Locked` instead of the overloaded boolean.
  - [ ] Implement the Cyan Mechanical Claw animation explicitly for `AfState.Locked` and the Yellow Breathing animation for `isFocusIndicatorLocked`.
  - [ ] Inject a `LaunchedEffect(key1 = state.afState)` that, when `Locked`, delays for `1000L` and updates an alpha state variable to `0.4f`. Reset alpha to `1.0f` on state exit.
  - [ ] Bind border/box size modifiers dynamically to `state.focusFrameWidthPx` and `state.focusFrameHeightPx`.
- [ ] **`CameraViewModel.kt`**:
  - [ ] Introduce a private `currentFocusSessionId: Int = 0`.
  - [ ] In `focusOnPoint()`, increment `currentFocusSessionId` and capture it locally in the coroutine.
  - [ ] Inside the CameraX/Camera2 callback lambda, assert: `if (capturedSessionId != currentFocusSessionId) { return@Callback }`.
  - [ ] Ensure `resetFocusState()` increments `currentFocusSessionId` and invokes `focusJob?.cancel()`.
