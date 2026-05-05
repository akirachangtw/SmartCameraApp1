# Cueshot Full Audit and Tickets

**Reviewer**: GPT Independent Architecture Auditor  
**Date**: 2026-05-05  
**Inputs**: `plans/cso_briefing/Cueshot_Verification_Matrix_v1.md` + `plans/cso_briefing/Cueshot_Merged_Spec_v3_4_1_Stage0.md` + `.claude/PROGRESS.md`  
**Scope**: `app/src/main/`, Gradle dependency files, and progress supplement scan. No app code changed.

---

## 0. Executive Dashboard

### Verdict

Current codebase is not yet aligned with Merged Spec v3.4.1-Stage0. It contains useful Cueshot v3.3/D6-D8 foundations, especially CameraX binding, fixed-focus bypass, PTF state, StateFlow UDF, composition rule scaffolding, and Edge Warmth/Halo prototypes. However, the Stage0 target state introduces a new SSOT surface (`AmbientConstants`, Pitch/Level Traction, Pill Toggle, scene state isolation, device tier degradation, Paywall boundaries) that is mostly absent or only partially represented.

Input integrity note: `Cueshot_Verification_Matrix_v1.md` declares "75" total verification items in its summary, but the actual PART tables contain 81 numbered rows. This report audits all 81 numbered rows and treats the source summary as stale.

### Matrix Summary

| 判定 | Count | Meaning |
|------|------:|---------|
| MATCH | 9 | Correct or substantially aligned |
| DRIFT | 20 | Exists but diverges from Stage0 target |
| ABSENT | 47 | Missing required structure or behavior |
| CONFLICT | 5 | Existing behavior directly violates spec |
| ZOMBIE | 0 | Obsolete file found |
| TOTAL | 81 | Actual numbered matrix rows; source summary says 75 |

### Highest-Risk Findings

| # | Finding | Severity | Evidence |
|---|---------|----------|----------|
| 1 | No `AmbientConstants`; Stage0 constants are scattered or missing. | High | `util/Constants.kt`, `CameraViewModel.kt`, `EdgeWarmthLayer.kt`, `HaloGuidanceLayer.kt` define local constants. |
| 2 | ML Kit Face Detection and Object Detection run together, violating mutual exclusion. | Critical | `MLKitDetector.detect()` calls both detectors; `SmartCameraAnalyzer.analyze()` also processes faces then objects. |
| 3 | PTF UI is still a full-width tab bar, not the 128dp x 28dp Pill Toggle. | High | `CameraTabBar.kt` uses `fillMaxWidth().height(44.dp)` and green selected tabs. |
| 4 | Camera preview/layout still uses full-screen FIT_CENTER container, not the two-block 664px/180px Stage0 layout. | High | `CameraScreen.kt` uses `PreviewViewportContainer(fillMaxSize())`; `AspectRatioMode.toScaleType` returns `FIT_CENTER`. |
| 5 | Scene model is not implemented as Stage0 scenes; Food/Portrait are commented as future work. | High | `CameraMode.kt` only has `Normal` and `PassToFriend`; Food/Portrait are reserved comments. |
| 6 | Paywall domain boundary is absent, including EXIF/write-path interception and blur overlay. | Medium | No `FreeTierLimitTracker`, `PaywallTrigger`, `PaywallEvent`; EXIF write path has no paywall hook. |
| 7 | D8 progress supplement still has active runtime blockers: `isAllAligned` false path and Eye-AF periodic retrigger. | Critical | `.claude/PROGRESS.md` lists I8/I7; `AnalyzeCompositionUseCase.kt` uses `topInstruction == null`, and `EyeAfController` has only a 2000ms time throttle. |

---

## 1. Evidence Map

| Area | Evidence |
|------|----------|
| Constants | `util/Constants.kt` exists, but no `AmbientConstants`; Cueshot timing/color constants are local to UI classes. |
| Layout | `CameraScreen.kt` uses `PreviewViewportContainer(... fillMaxSize())`; top row is overlay but no scrim; bottom row has no fixed 180px band. |
| Pill/PTF | `CameraTabBar.kt` exists; `PtfTabBar` not found. `setCameraMode()` is pure state. PTF onboarding is an `AnimatedVisibility` panel, not the specified bottom sheet. |
| Edge Warmth | `EdgeWarmthLayer.kt` exists with amber/green colors, but uses roll-only direction and `EDGE_BAND_FRACTION = 0.15f`, not 60px or scene mapping. |
| Level Traction | Only legacy `LevelIndicator.kt`; no opacity formula, no Pitch Traction. |
| Halo | `HaloGuidanceLayer.kt` supports gaze/eye-line and PTF center ring, but no group halo, food halo, or specified EMA set. |
| Focus | `FixedFocusDetector.kt`, `LiveCameraSource.focusOnPoint()`, `DefaultEyeAfPolicyResolver.kt`, and `FocusTriggerThrottle.kt` provide strong fixed-focus and throttle foundations. Focus UI does not match Stage0 four-state Focus Ring visual spec. |
| ML Kit | Face and Object detectors both exist and are executed together in multiple paths. |
| Device tier | `DeviceCapabilityTier` and Hilt provider exist; no `DeviceTierClassifier` or feature degradation policy matrix. |
| Paywall | No domain interfaces or UI blur/paywall boundary. |
| Progress supplement | `.claude/PROGRESS.md` marks I8 (`isAllAligned` always false) and I7 (EyeAfController 2s retrigger) as unresolved active blockers. |

---

## 2. Verification Matrix: 81 Numbered Items

### PART 1: Global Constants

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 1.1 | ABSENT | No `object AmbientConstants`; only generic `Constants.kt`. | TICKET-01 |
| 1.2 | ABSENT | No `LEVEL_FADE_RANGE_DEG`; Level UI uses fixed thresholds. | TICKET-01 |
| 1.3 | ABSENT | No `SCRIM_HEIGHT_PX = 120f`; no scrim implementation. | TICKET-01 |
| 1.4 | ABSENT | No `HYSTERESIS_SCENE_MS`; only comments mention 500ms. | TICKET-01 |
| 1.5 | ABSENT | Breathing periods are local constants in UI files, not SSOT. | TICKET-01 |
| 1.6 | ABSENT | No `SHUTTER_FLASH_DURATION_MS`; no white shutter flash path. | TICKET-01 |
| 1.7 | CONFLICT | Constants are scattered across `CameraViewModel`, `EdgeWarmthLayer`, `HaloGuidanceLayer`, `FocusIndicatorOverlay`. | TICKET-01 |

### PART 2: Screen Layout

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 2.1 | DRIFT | Preview uses full-size `PreviewViewportContainer`; no 664px/78.7% model. | TICKET-02 |
| 2.2 | DRIFT | Bottom controls are a bottom-aligned `Row` with padding, not 180px control band. | TICKET-02 |
| 2.3 | DRIFT | Top controls are floating overlay, but scrim is absent. | TICKET-02 |
| 2.4 | ABSENT | No black scrim gradient 0.22 to 0 at 120px. | TICKET-02 |
| 2.5 | DRIFT | Z-order partially matches, but includes legacy HDR/Composition/Focus ordering and no scrim layer. | TICKET-02 |
| 2.6 | CONFLICT | `GridOverlay` default is white alpha 0.5 and stroke 2f, not alpha 0.08 and 0.5px. | TICKET-02 |
| 2.7 | ABSENT | No divider at y=664px with white alpha 0.05. | TICKET-02 |

### PART 3: Pill Toggle

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 3.1 | DRIFT | `CameraTabBar` exists; it is still tab-bar shaped, not Stage0 Pill Toggle. | TICKET-03 |
| 3.2 | CONFLICT | `CameraTabBar` root uses `fillMaxWidth().height(44.dp)`, not 128dp x 28dp. | TICKET-03 |
| 3.3 | CONFLICT | Selected tabs use green background; no 60dp x 24dp `#4A4A4A` indicator. | TICKET-03 |
| 3.4 | DRIFT | Uses `Icons.Filled.CameraAlt` and `PeopleAlt` at 22dp; spec requires outlined CameraAlt/People at 20dp. | TICKET-03 |
| 3.5 | CONFLICT | Selected icon is black, unselected is white alpha 0.75; spec requires `#CCCCCC`/`#666666`. | TICKET-03 |
| 3.6 | DRIFT | Uses 300ms `tween` without explicit `FastOutSlowInEasing`. | TICKET-03 |
| 3.7 | MATCH | `setCameraMode()` only updates `CameraState.cameraMode`; no CameraX rebind or ML Kit close. | TICKET-03 |
| 3.8 | DRIFT | PTF cooldown is based on last accepted capture, not a 1s lockout after entering PTF. | TICKET-04 |
| 3.9 | DRIFT | First-time PTF hint is an overlay column, not `Bottom Sheet max(screenHeight x 0.20, 96dp)`. | TICKET-04 |
| 3.10 | ABSENT | No bottom-band color transition `#0D0D0D` to `#1A1714`. | TICKET-02 |
| 3.11 | DRIFT | Shutter enlarges 72 to 80dp, but flip remains visible and exit button is top-right, not shutter-left. | TICKET-04 |
| 3.12 | MATCH | No `PtfTabBar` file or reference found. | None |

### PART 4: Edge Warmth + Pitch/Level Traction

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 4.1 | ABSENT | No `PitchTractionConfig` data class. | TICKET-05 |
| 4.2 | ABSENT | No `FOOD_45` config. | TICKET-05 |
| 4.3 | ABSENT | No `FOOD_90` config. | TICKET-05 |
| 4.4 | ABSENT | No `selectPitchConfig` hysteresis 70/80. | TICKET-05 |
| 4.5 | DRIFT | `drawEdgeWarmthGradient` exists, but uses 15% band, not 60px inward edge. | TICKET-05 |
| 4.6 | MATCH | Edge Warmth amber uses `#D4A030` and max alpha `0.20f`. | TICKET-05 |
| 4.7 | ABSENT | No `(maxDeviation / 5f).coerceIn(0f, 1f)` opacity formula. | TICKET-05 |
| 4.8 | DRIFT | `LevelIndicator` uses binary visibility/color; aligned state remains green instead of alpha to 0. | TICKET-05 |
| 4.9 | ABSENT | No landscape MVP hide guard for Level Traction. | TICKET-05 |
| 4.10 | ABSENT | No six-scene Edge Warmth behavior map. | TICKET-06 |

### PART 5: Halo

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 5.1 | DRIFT | Normal Halo follows face bbox/gaze state, not explicit thirds/headroom/gaze-space target model. | TICKET-07 |
| 5.2 | ABSENT | No single-person Halo EMA 0.7/0.3. | TICKET-07 |
| 5.3 | ABSENT | No food Halo following largest food bbox. | TICKET-07 |
| 5.4 | ABSENT | No food EMA 0.8/0.2. | TICKET-07 |
| 5.5 | ABSENT | No `GroupHaloResult`. | TICKET-07 |
| 5.6 | ABSENT | No `calculateGroupHalo` with 2/3/4+ person y-targets. | TICKET-07 |
| 5.7 | ABSENT | No `smoothGroupHalo` EMA 0.75/0.25. | TICKET-07 |
| 5.8 | ABSENT | No group alpha = single alpha - 0.05. | TICKET-07 |
| 5.9 | DRIFT | Amber/green colors exist, but normal Halo alpha/group rules do not match Stage0. | TICKET-07 |
| 5.10 | MATCH | PTF Halo target is frame center with gravitational offset. | TICKET-07 |

### PART 6: PTF Special Rules

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 6.1 | MATCH | `PtfRuleAggregator` evaluates Framing, Angle, Level as three-rule AND gate. | TICKET-04 |
| 6.2 | ABSENT | No PTF Level Traction color path; current PTF Halo animates amber to green. | TICKET-05 |
| 6.3 | ABSENT | No PTF Level reference layer. | TICKET-05 |

### PART 7: Shutter Outer Ring Animation Mutex

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 7.1 | DRIFT | No explicit `isBreathing`; mutex is implicit via `isAligned && !isCapturing`. | TICKET-10 |
| 7.2 | DRIFT | Capture shows progress ring; no static green freeze + 300ms flash + cooldown sequence. | TICKET-10 |
| 7.3 | MATCH | Shutter breathing alpha 0.10 to 0.18, 2000ms, FastOutSlowIn is implemented. | TICKET-10 |
| 7.4 | ABSENT | No white flash alpha 0.45 to 0 for 300ms. | TICKET-10 |
| 7.5 | ABSENT | No LEGACY 500ms shutter cooldown branch. | TICKET-10 |

### PART 8: Device Tier Degradation

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 8.1 | ABSENT | No `DeviceTierClassifier`; only `DeviceCapabilityTier` + Hilt provider. | TICKET-09 |
| 8.2 | MATCH | FULL/LIMITED/LEGACY mapping exists through Camera2 hardware level. | TICKET-09 |
| 8.3 | DRIFT | No explicit FULL feature policy. | TICKET-09 |
| 8.4 | ABSENT | No LIMITED flag to disable clutter frame. | TICKET-09 |
| 8.5 | ABSENT | No LEGACY flags for smudge/subtractive/clutter/light shutdown. | TICKET-09 |
| 8.6 | ABSENT | No LEGACY group-Halo downgrade to max-face single Halo. | TICKET-07 |
| 8.7 | DRIFT | Edge Warmth base is not disabled by tier, but there is no explicit 60fps guarantee/policy; decorative dots are disabled on LEGACY. | TICKET-09 |

### PART 9: Canvas Call Budget

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 9.1 | DRIFT | No central draw-call budget or per-state accounting; Canvas calls are scattered across overlays. | TICKET-14 |
| 9.2 | ABSENT | No Level reference pre-rendered Bitmap cache. | TICKET-14 |

### PART 10: Food Mode Rule Table

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 10.1 | ABSENT | No generic `AngleRule`; only `PtfAngleRule`. | TICKET-11 |
| 10.2 | ABSENT | No generic `LevelRule`; only `PtfLevelRule` and legacy `LevelIndicator`. | TICKET-11 |
| 10.3 | ABSENT | No `FoodCompositionRule`. | TICKET-11 |
| 10.4 | ABSENT | No `LightRule`; lighting use case exists but not as paid rule. | TICKET-11 |
| 10.5 | ABSENT | No `MinimalistRule`. | TICKET-11 |
| 10.6 | ABSENT | No paid/free feature flags for FoodComposition/Light/Minimalist. | TICKET-12 |

### PART 11: Focus Ring

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 11.1 | DRIFT | `FocusIndicatorOverlay` exists but renders ambient concentric rings, not Focusing/Focused/Failed/Locked spec. | TICKET-13 |
| 11.2 | ABSENT | No Failed 4px shake visual. | TICKET-13 |
| 11.3 | ABSENT | No Locked double ring plus `AE/AF` label. | TICKET-13 |

### PART 12: Breathing Rhythm

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 12.1 | MATCH | `ShutterButtonSection` breathes alpha 0.10 to 0.18 when aligned. | TICKET-10 |
| 12.2 | MATCH | Shutter `InfiniteTransition` is scoped inside the conditional render block. | TICKET-10 |

### PART 13: Scene Decision Logic

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 13.1 | ABSENT | `CameraMode` only implements `Normal` and `PassToFriend`; Food/Portrait are comments. | TICKET-06 |
| 13.2 | ABSENT | No real 500ms scene switching hysteresis. | TICKET-06 |

### PART 14: Performance Frame Slicing

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 14.1 | ABSENT | No five-frame ML Kit/Histogram/Smudge scheduler; current path uses adaptive throttle. | TICKET-14 |

### PART 15-16: Freemium / Paywall

| # | 判定 | Evidence | Ticket |
|---|------|----------|--------|
| 15.1 | ABSENT | No `FreeTierLimitTracker`. | TICKET-12 |
| 15.2 | ABSENT | No `PaywallTrigger`. | TICKET-12 |
| 15.3 | ABSENT | No `PaywallEvent`. | TICKET-12 |
| 15.4 | ABSENT | No paid-feature flag boundary for FoodComposition/Light/Minimalist. | TICKET-12 |

---

## 2.1 Progress Supplement Checks

These checks come from `.claude/PROGRESS.md`; they are not part of the numbered matrix, but they are active D8 blockers and must be resolved before Stage0 implementation can be trusted.

| ID | 判定 | Evidence | Required Action |
|----|------|----------|-----------------|
| I8 | CONFLICT | `AnalyzeCompositionUseCase.kt` derives `isAllAligned` from `topInstruction == null`; progress notes this can stay false due to HeadroomRule and ThirdsRule geometry conflict. | TICKET-00 |
| I7 | DRIFT | `EyeAfController` only enforces a 2000ms trigger cooldown and does not skip when lens is already focused with target drift below 10px. | Add a focused-drift skip guard before approving C1 VCM decoupling. |

---

## 3. C1-C5 Architecture Pressure Test

### C1: Hardware Limits

| Check | 判定 | Evidence | Action |
|-------|------|----------|--------|
| Front camera fixed-focus bypass | MATCH | `FixedFocusDetector` checks minimum focus distance / AF modes; `LiveCameraSource.focusOnPoint()` bypasses FLAG_AF for fixed-focus. | Preserve. |
| VCM motor delay decoupled from visual tracking | DRIFT | `EyeAfController` has 2000ms cooldown and Halo has 10fps throttle; new focus pipeline also throttles. But old and new paths both exist and can diverge. | Consolidate focus trigger gate. |
| ML Kit Face/Object mutual exclusion + 500ms hysteresis | CONFLICT | `MLKitDetector`, `SmartCameraAnalyzer`, and `CameraViewModel.analyzeImageWithMLKit()` run face and object detection together. | TICKET-08 |
| IMU sampling and Edge Warmth render independent | DRIFT | Gyroscope uses `SENSOR_DELAY_FASTEST`; Edge Warmth consumes `deviceRoll` from state. No explicit 200Hz to 60fps pipe or EW scheduler. | TICKET-05/TICKET-14 |

### C2: State Machine Defense

| Check | 判定 | Evidence | Action |
|-------|------|----------|--------|
| Sealed scene isolation | ABSENT | `CameraMode` lacks Food/Portrait/General sealed state data. | TICKET-06 |
| Scene hysteresis 500ms | ABSENT | No scene hysteresis implementation. | TICKET-06 |
| Pill Toggle pure Compose state | MATCH | `setCameraMode()` mutates state only. | Preserve in TICKET-03. |
| Breathing vs shutter flash mutex | DRIFT | Breathing vs capture is implicit; flash path absent. | TICKET-10 |
| Level Traction 0 to 5 opacity | ABSENT | No Stage0 Level Traction. | TICKET-05 |
| Landscape Level Traction hidden | ABSENT | No landscape guard. | TICKET-05 |

### C3: UDF Decoupling

| Check | 判定 | Evidence | Action |
|-------|------|----------|--------|
| StateFlow ViewModel to Compose | MATCH | `CameraViewModel.state` and `focusUiStateFlow`; Compose observes flows. | Preserve. |
| No `runBlocking` / direct CameraX to Compose chain | MATCH | No `runBlocking` found; CameraX callbacks update repository/ViewModel state. | Preserve. |
| CameraX callback Executor vs Main thread separated | MATCH | `LiveCameraSource` uses `analysisExecutor`; CameraX binding and focus operations use controlled executors. | Preserve. |
| ML Kit results through Channel/StateFlow, not analyzer direct UI | DRIFT | Repository uses StateFlow, but analyzers still expose callbacks; direct UI is avoided, but boundary is not clean. | TICKET-08 |

### C4: Three-Level Device Degradation

| Check | 判定 | Evidence | Action |
|-------|------|----------|--------|
| DeviceTierClassifier | ABSENT | No classifier class; `DeviceCapabilityTier` is lower-level model only. | TICKET-09 |
| Edge Warmth 60fps all tiers | DRIFT | Base EW is not explicitly capped/degraded, but no 60fps guarantee exists. | TICKET-14 |
| LEGACY group Halo downgrade | ABSENT | Group Halo absent. | TICKET-07 |
| LEGACY 500ms shutter cooldown | ABSENT | PTF cooldown is fixed 1000ms after accepted capture; no tier branch. | TICKET-10 |
| LIMITED disables clutter frame | ABSENT | No clutter feature flag. | TICKET-09 |
| LEGACY disables smudge/subtractive/clutter/light | ABSENT | No degradation policy. | TICKET-09 |

### C5: Paywall Boundary

| Check | 判定 | Evidence | Action |
|-------|------|----------|--------|
| `FreeTierLimitTracker` | ABSENT | No interface. | TICKET-12 |
| Paid feature flags | ABSENT | `FeatureFlags` only covers focus features. | TICKET-12 |
| EXIF write path paywall hook | ABSENT | `CameraRepositoryImpl.writeExifMetadata()` has no paywall interceptor. | TICKET-12 |
| Blur overlay z-order | ABSENT | No blur overlay/paywall UI. | TICKET-12 |

---

## 4. Coarse Ticket List

### TICKET-00: Repair isAllAligned Aggregation Blocker

**Priority**: P0  
**Type**: Fix  
**Source**: Matrix confirmed item G7, `.claude/PROGRESS.md` I8, PART 17 architecture alignment  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/usecase/AnalyzeCompositionUseCase.kt`

**Scope Ban**:
- Do not modify individual composition rule geometry in this ticket.
- Do not alter `PriorityEngine` ranking.
- Do not change UI rendering.

**Work**: Replace the current `topInstruction == null` alignment derivation with a defensible aggregation rule that matches the active D8 decision: weighted average score >= 70. Keep `topInstruction` as priority output only; it must not be the SSOT for all-aligned state.

**Acceptance**:
- `isAllAligned` is not derived from `topInstruction == null`.
- HeadroomRule and ThirdsRule can both fail independently without permanently forcing IFTR to zero when aggregate score is acceptable.
- Unit coverage verifies below-threshold and above-threshold aggregate cases.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-01: Establish Stage0 Constants SSOT

**Priority**: P0  
**Type**: New / Refactor  
**Source**: PART 1, C2  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/config/AmbientConstants.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/overlay/EdgeWarmthLayer.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/HaloGuidanceLayer.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`

**Scope Ban**:
- Do not edit CameraX binding files.
- Do not rename existing domain model outputs.

**Work**: Add `AmbientConstants` exactly as specified by Merged Spec PART 1. Replace local duplicated Cueshot constants only where behavior already depends on Stage0 values. Do not move unrelated app constants from `util/Constants.kt`.

**Acceptance**:
- `AmbientConstants.ALIGNED_THRESHOLD_DEG == 5f`
- `LEVEL_FADE_RANGE_DEG == 5f`
- `SCRIM_HEIGHT_PX == 120f`
- `HYSTERESIS_SCENE_MS == 500L`
- `BREATHE_CYCLE_MS == 2000L`
- `SHUTTER_FLASH_DURATION_MS == 300L`

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-02: Rebuild Stage0 Camera Layout Shell

**Priority**: P0  
**Type**: Refactor  
**Source**: PART 2  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/CameraPreview.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/model/AspectRatioMode.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/GridOverlay.kt`

**Scope Ban**:
- Do not modify repository/data camera binding.
- Do not change focus policy.

**Work**: Implement the two-block view model: viewfinder band and 180px control band, add top scrim `0 -> 120px`, add divider alpha 0.05, and tune grid to alpha 0.08 / stroke 0.5px. Reconcile PreviewView scale with Stage0 `FILL_CENTER` without breaking existing coordinate mapping tests.

**Acceptance**:
- Top scrim exists with `AmbientConstants.SCRIM_HEIGHT_PX`.
- Bottom control band has stable height.
- Grid defaults match Stage0.
- Overlay order matches L0-L6 target.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-03: Replace CameraTabBar Visuals With Stage0 Pill Toggle

**Priority**: P0  
**Type**: Rewrite  
**Source**: PART 3, C2  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/CameraTabBar.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
- `app/src/main/res/values/strings.xml`

**Scope Ban**:
- Do not modify CameraX, ML Kit, or repository code.

**Work**: Keep the existing pure-state `CameraMode` transition, but rewrite the UI to Stage0 Pill Toggle dimensions, colors, icons, touch target, and `FastOutSlowInEasing`.

**Acceptance**:
- Root visible size is 128dp x 28dp with 48dp touch target.
- Indicator is 60dp x 24dp, fill `#4A4A4A`.
- Selected/unselected icon tints are `#CCCCCC` / `#666666`.
- Toggle still calls `CameraUiEvent.SetCameraMode` only.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-04: Correct PTF Entry, Onboarding, and Control Mask

**Priority**: P0  
**Type**: Fix  
**Source**: PART 3, PART 6  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/PtfRuleAggregator.kt`

**Scope Ban**:
- Do not alter normal-mode composition rules.
- Do not rebind camera on PTF entry/exit.

**Work**: Add PTF entry timestamp state for 1s post-entry shutter lockout. Convert first-time PTF hint to the specified bottom sheet bounds. Hide flip/gallery controls, place the exit button at the Stage0 position, and preserve the three-rule PTF aggregator.

**Acceptance**:
- PTF entry blocks shutter for 1000ms even before first capture.
- PTF exit has both Pill Photo side and close button path.
- Flip/gallery controls are hidden in PTF.
- `PtfRuleAggregator` remains stateless.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-05: Add Pitch Traction and Replace Legacy Level Indicator

**Priority**: P0  
**Type**: New / Rewrite  
**Source**: PART 4, PART 6, C2  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/config/PitchTractionConfig.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/LevelIndicator.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/overlay/EdgeWarmthLayer.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`

**Scope Ban**:
- Do not change sensor manager implementation.
- Do not alter focus pipeline.

**Work**: Add `PitchTractionConfig` with `FOOD_45` and `FOOD_90`. Implement level opacity formula using `AmbientConstants.LEVEL_FADE_RANGE_DEG`. Hide Level Traction in landscape MVP and enforce PTF fixed green level color.

**Acceptance**:
- `FOOD_45` = 45f / 0.30f / 5f.
- `FOOD_90` = 90f / 0.20f / 5f.
- Level opacity is 0 at aligned and 1 at 5 degrees.
- PTF level guide uses green only.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-06: Introduce Stage0 Scene State and Hysteresis

**Priority**: P0  
**Type**: New  
**Source**: PART 13, C2  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/camera/CameraMode.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/overlay/SceneTransitionEffect.kt`

**Scope Ban**:
- Do not modify ML Kit detector internals in this ticket.
- Do not alter UI drawing details beyond scene state consumption.

**Work**: Add sealed scene representation for PTF, Food, Portrait, General, Minimalist where needed by Stage0. Implement priority `PTF > pitch > 35 Food > face Portrait > General` with 500ms hysteresis and state reset on scene exit.

**Acceptance**:
- Previous scene target state cannot leak into new scene.
- Food/Portrait are implemented code paths, not comments.
- Hysteresis uses `AmbientConstants.HYSTERESIS_SCENE_MS`.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-07: Rebuild Halo Domain and Rendering

**Priority**: P1  
**Type**: New / Rewrite  
**Source**: PART 5, C4  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/HaloGuidanceLayer.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/model/CompositionResult.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/model/DeviceCapabilityTier.kt`

**Scope Ban**:
- Do not rewrite ML Kit detector.
- Do not change `PtfRuleAggregator` three-rule contract.

**Work**: Add the missing normal/Food/PTF/group Halo behaviors, including group target y values, EMA factors, alpha compensation, and LEGACY max-face downgrade.

**Acceptance**:
- Single portrait, group portrait, food bbox, PTF, minimalist, and general cases render per PART 5.
- LEGACY group Halo downgrades to max-face single Halo.
- Alpha values match Stage0.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-08: Enforce ML Kit Detector Mutual Exclusion

**Priority**: P0  
**Type**: Rewrite  
**Source**: C1, C3  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/data/source/ml/MLKitDetector.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/SmartCameraAnalyzer.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt`

**Scope Ban**:
- Do not alter UI rendering.
- Do not remove existing detector clients until replacement path is proven.

**Work**: Route detector selection by scene, never running face and object detection simultaneously. Add a 500ms hysteresis gate for Face/Object switching and ensure analyzer outputs enter ViewModel through StateFlow/Channel-like boundary only.

**Acceptance**:
- No single frame invokes both Face and Object detection.
- Switching detector family waits 500ms.
- Analyzer callbacks do not update Compose state directly.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-09: Add DeviceTierClassifier and Degradation Policy

**Priority**: P1  
**Type**: New  
**Source**: PART 8, C4  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/model/DeviceCapabilityTier.kt`
- `app/src/main/java/com/akirachang/ai/photographer/di/CameraModule.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/config/FeatureFlags.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`

**Scope Ban**:
- Do not change CameraX binding behavior.
- Do not introduce runtime benchmarking in this ticket unless strictly needed.

**Work**: Add the classifier/policy boundary expected by Matrix. Preserve Camera2 hardware-level mapping but expose explicit FULL/LIMITED/LEGACY feature flags for clutter, smudge, subtractive whitespace, light, group Halo, and shutter cooldown.

**Acceptance**:
- FULL/LIMITED/LEGACY policies are inspectable from domain/presentation.
- LIMITED disables clutter frame.
- LEGACY disables smudge, subtractive, clutter, and light.
- Edge Warmth base remains enabled across tiers.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-10: Implement Shutter Flash and Tiered Cooldown Mutex

**Priority**: P1  
**Type**: Fix  
**Source**: PART 7, PART 12, C2/C4  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/config/AmbientConstants.kt`

**Scope Ban**:
- Do not alter camera capture repository.
- Do not modify PTF rule engine.

**Work**: Add explicit shutter animation state so breathing and flash cannot overlap. Implement white flash alpha 0.45 to 0 over 300ms and LEGACY 500ms cooldown/static wait.

**Acceptance**:
- Capture freezes breathing, plays flash, then waits cooldown.
- FULL/LIMITED use 300ms; LEGACY uses 500ms with static wait.
- `InfiniteTransition` remains conditional.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-11: Add Stage0 Food and Paid Composition Rules

**Priority**: P1  
**Type**: New  
**Source**: PART 10  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/rules/AngleRule.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/rules/LevelRule.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/rules/FoodCompositionRule.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/rules/LightRule.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/composition/rules/MinimalistRule.kt`

**Scope Ban**:
- Do not wire rules into UI in this ticket.
- Do not change existing PTF rules.

**Work**: Add domain-only rule classes with data sources and paid/free categorization aligned to PART 10. Keep each rule pure and UI-free.

**Acceptance**:
- All five expected rule classes exist.
- Angle/Level are free.
- FoodComposition/Light/Minimalist are paid-gated candidates.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-12: Add Paywall Boundary and EXIF Intercept Point

**Priority**: P1  
**Type**: New  
**Source**: PART 15-16, C5  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/domain/paywall/FreeTierLimitTracker.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/paywall/PaywallTrigger.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/paywall/PaywallEvent.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/PhotoInfoBottomSheet.kt`

**Scope Ban**:
- Do not implement billing provider.
- Do not alter camera capture behavior except adding a no-op intercept boundary.

**Work**: Add the domain interfaces exactly as in PART 16. Add no-op/default paywall intercept hooks for score/PTF limits, paid feature flags, EXIF write path, and blur overlay insertion point.

**Acceptance**:
- Interfaces and enum exist with specified method/event names.
- EXIF write path has a visible paywall decision point.
- Paid diagnosis card blur layer can be inserted with known z-order.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-13: Align Focus Ring Visual Spec

**Priority**: P2  
**Type**: Rewrite  
**Source**: PART 11  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/FocusIndicatorOverlay.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/model/focus/FocusUiState.kt`
- `app/src/main/java/com/akirachang/ai/photographer/domain/model/focus/FocusStates.kt`

**Scope Ban**:
- Do not modify hardware focus policy.
- Do not modify CameraX focus controller.

**Work**: Map existing focus state model to the Stage0 visual states: Focusing, Focused, Failed, Locked. Add failed 4px shake and locked double ring plus `AE/AF` text.

**Acceptance**:
- All four visual states render.
- Failed shake amplitude is 4px.
- Locked state includes double ring and `AE/AF`.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

### TICKET-14: Add Performance Scheduler and Canvas Budget Guard

**Priority**: P2  
**Type**: New  
**Source**: PART 9, PART 14  
**Target files**:
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/SmartCameraAnalyzer.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/overlay/LevelIndicator.kt`
- `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/overlay/EdgeWarmthLayer.kt`

**Scope Ban**:
- Do not rewrite ML Kit detector selection here; depend on TICKET-08.
- Do not change visual specs.

**Work**: Add five-frame work slicing and a lightweight draw-call accounting/test hook for Canvas layers. Add pre-rendered Level reference Bitmap cache after Level Traction exists.

**Acceptance**:
- Frame schedule matches PART 14.
- Draw-call estimate remains under 15 per state.
- State 2 Level reference uses cached/pre-rendered asset.

**Defensive Warning**: Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.

---

## 5. Risk Hotspots

| # | Risk | Files | Severity | Mitigation |
|---|------|-------|----------|------------|
| 1 | Running Face and Object detection together can exceed thermal/FPS budget and violates ML Kit mutual exclusion. | `MLKitDetector.kt`, `SmartCameraAnalyzer.kt`, `CameraViewModel.kt` | Critical | TICKET-08 before Food/Object-driven Halo. |
| 2 | Stage0 scene state is not isolated; adding Food/Portrait inside current `CameraMode` without reset will leak PTF EMA/focus state. | `CameraMode.kt`, `CameraState.kt`, `CameraViewModel.kt` | High | TICKET-06 sealed scene + reset contracts. |
| 3 | Preview coordinate system currently relies on FIT_CENTER while Stage0 asks FILL_CENTER; naive layout fix can break focus/ML mapping. | `CameraPreview.kt`, `AspectRatioMode.kt`, `PreviewViewportContainer.kt`, `LiveCameraSource.kt` | High | TICKET-02 must verify coordinate mapping before changing scale type. |
| 4 | Old/new focus paths coexist, increasing risk of duplicate HAL focus triggers. | `EyeAfController.kt`, `CameraViewModel.kt`, `DefaultEyeAfPolicyResolver.kt`, `FocusTriggerThrottle.kt` | High | Consolidate trigger authority during TICKET-08 or a follow-up focus gate audit. |
| 5 | Edge Warmth exists but is not Stage0 Pitch/Scene-aware; patching UI only will not fix semantic mismatch. | `EdgeWarmthLayer.kt`, `CameraScreen.kt`, `DeviceSensorManagerImpl.kt` | Medium | TICKET-05 must add domain config before UI tuning. |
| 6 | Paywall has no domain boundary; bolting UI blur later can miss EXIF/score/write-path hooks. | `CameraRepositoryImpl.kt`, `PhotoInfoBottomSheet.kt`, missing `domain/paywall/*` | Medium | TICKET-12 creates interface boundary before paid UI. |
| 7 | IFTR can remain zero because `isAllAligned` is tied to priority-result absence instead of aggregate score. | `AnalyzeCompositionUseCase.kt`, `.claude/PROGRESS.md` | Critical | TICKET-00 must run before visual Stage0 tickets. |
| 8 | Eye-AF can retrigger every 2s even after focus is already stable. | `EyeAfController.kt`, `CameraViewModel.kt` | Medium | Add focused-lens + drift<10px skip guard before C1 approval. |

---

## 6. Scope Control Notes

- Do not modify app code from this audit report.
- Do not implement tickets out of order without re-checking dependencies.
- TICKET-00, TICKET-01, TICKET-02, TICKET-03, TICKET-06, and TICKET-08 are P0 because they define alignment truth, state, constants, layout, and detector safety.
- Current worktree already contains unrelated modified/deleted/untracked files outside this report. Do not revert them unless explicitly requested.
